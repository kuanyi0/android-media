package com.yikuan.androidmedia.app;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.yikuan.androidcommon.util.DateUtils;
import com.yikuan.androidcommon.util.FileUtils;
import com.yikuan.androidcommon.util.PathUtils;
import com.yikuan.androidcommon.util.PermissionUtils;
import com.yikuan.androidmedia.app.databinding.ActivityAudioRecordBinding;
import com.yikuan.androidmedia.record.AudioRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AudioRecordActivity";
    private static final String DIR = "android-media/audio-record";
    private static final String FILE_PROVIDER_AUTHORITY = "com.yikuan.androidmedia.app.fileprovider";
    private static final String[] mPermissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ActivityAudioRecordBinding mBinding;
    private AudioRecorder mAudioRecorder;
    private File mDir;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAudioRecordBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.audio_record);
        mBinding.btnStart.setOnClickListener(this);
        mBinding.btnStop.setEnabled(false);
        mBinding.btnStop.setOnClickListener(this);
        if (!PermissionUtils.isGranted(mPermissions)) {
            ActivityCompat.requestPermissions(this, mPermissions, 0);
        } else {
            initialize();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            }
        }
        initialize();
    }

    private void initialize() {
        AudioRecorder.AudioParams audioParams = new AudioRecorder.AudioParams(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.configure(audioParams);
        mDir = new File(PathUtils.getExternalStoragePath() + "/" + DIR);
        try {
            FileUtils.forceMkdir(mDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                start();
                break;
            case R.id.btn_stop:
                stop();
                break;
            default:
                break;
        }
    }

    private void start() {
        String path = mDir.getAbsolutePath() + "/" + DateUtils.formatTimeFileName();
        mFile = new File(path);
        mAudioRecorder.setCallback(new AudioRecorder.Callback() {
            FileOutputStream fileOutputStream;

            @Override
            public void onDataAvailable(byte[] data) {
                if (fileOutputStream == null) {
                    try {
                        fileOutputStream = new FileOutputStream(mFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.write(data);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onDataError(int error) {
                Log.e(TAG, "data error: " + error);
            }
        });
        mAudioRecorder.start();
        mBinding.btnStart.setEnabled(false);
        mBinding.btnStop.setEnabled(true);
    }

    private void stop() {
        mAudioRecorder.stop();
        new AlertDialog.Builder(this)
                .setTitle(R.string.audio_record_finish_title)
                .setMessage(String.format(getResources().getString(R.string.output_file), mFile.getAbsolutePath()))
                .setPositiveButton(R.string.open_finder, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File dir = mFile;
                        if (dir == null) {
                            return;
                        }
                        Uri uri;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            uri = FileProvider.getUriForFile(AudioRecordActivity.this, FILE_PROVIDER_AUTHORITY, dir);
                        } else {
                            uri = Uri.fromFile(dir);
                        }
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setDataAndType(uri, "*/*");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Log.e(TAG, "open finder error");
                        }
                    }
                })
                .create()
                .show();
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
        }
    }
}