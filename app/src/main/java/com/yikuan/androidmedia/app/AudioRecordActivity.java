package com.yikuan.androidmedia.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidcommon.util.DateUtils;
import com.yikuan.androidmedia.app.databinding.ActivityAudioRecordBinding;
import com.yikuan.androidmedia.record.AudioRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class AudioRecordActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "AudioRecordActivity";
    private ActivityAudioRecordBinding mBinding;
    private AudioRecorder mAudioRecorder;
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
        initialize();
    }

    private void initialize() {
        AudioRecorder.AudioParams audioParams = new AudioRecorder.AudioParams(MediaRecorder.AudioSource.MIC, 44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.configure(audioParams);
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
        String path = Constant.DIR_AUDIO_RECORD + "/" + DateUtils.formatTimeFileName();
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
                        Utils.selectFile(AudioRecordActivity.this, mFile.getParentFile());
                    }
                })
                .create()
                .show();
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioRecorder != null) {
            mAudioRecorder.release();
        }
    }
}