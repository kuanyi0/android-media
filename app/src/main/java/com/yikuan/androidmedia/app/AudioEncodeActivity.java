package com.yikuan.androidmedia.app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidcommon.util.ThreadPoolManager;
import com.yikuan.androidmedia.app.databinding.ActivityAudioEncodeBinding;
import com.yikuan.androidmedia.util.MediaCodecUtils;
import com.yikuan.androidmedia.encode.AudioEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AudioEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AudioEncodeActivity";
    private ActivityAudioEncodeBinding mBinding;
    private AudioEncoder mAudioEncoder;
    private File mSourceFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAudioEncodeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.audio_encode);
        initView();
        initEncoder();
        initSource();
    }

    private void initView() {
        mBinding.btnSelectFile.setOnClickListener(this);
        mBinding.btnStart.setOnClickListener(this);
        mBinding.btnStop.setOnClickListener(this);
        mBinding.btnStop.setEnabled(false);
        List<String> list = new ArrayList<>();
        List<MediaCodecInfo> encoders =  MediaCodecUtils.getEncoders();
        for (MediaCodecInfo info : encoders) {
            list.add(info.getName());
        }
        mBinding.encoderList.setList(list);
    }

    private void initEncoder() {
        mAudioEncoder = new AudioEncoder();
        @SuppressLint("InlinedApi") AudioEncoder.Param param = new AudioEncoder.Param(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1, 96000);
        mAudioEncoder.configure(param);
        mAudioEncoder.setCallback(new AudioEncoder.Callback() {
            @Override
            public void onOutputFormatChanged(MediaFormat format) {
                Log.d(TAG, "onOutputFormatChanged: " + format);
            }

            @Override
            public void onOutputAvailable(byte[] output) {
                Log.d(TAG, "onOutputAvailable: " + Arrays.toString(output));
            }
        });
    }

    private void initSource() {
        File dir = new File(Constant.DIR_AUDIO_RECORD);
        File[] list = dir.listFiles();
        if (list != null && list.length > 0) {
            mSourceFile = list[0];
            mBinding.tvPath.setText(mSourceFile.getAbsolutePath());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_file:
                selectFile();
                break;
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

    private void selectFile() {
        Utils.selectFile(this, Constant.DIR_AUDIO_ENCODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + data);
        if (data != null) {
            Uri uri = data.getData();
            String path = Utils.getPathFromUri(uri);
            Log.d(TAG, "onActivityResult: path = " + path);
            mSourceFile = new File(path);
            mBinding.tvPath.setText(path);
        }
    }

    private void start() {
        if (mSourceFile == null) {
            return;
        }
        mAudioEncoder.start();
        try {
            final FileInputStream inputStream = new FileInputStream(mSourceFile);
            ThreadPoolManager.getInstance().execute(new Runnable() {
                byte[] data = new byte[AudioEncoder.MAX_INPUT_SIZE];
                @Override
                public void run() {
                    while (true) {
                        try {
                            int length = inputStream.read(data);
                            if (length > 0) {
                                mAudioEncoder.encode(data);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(AudioEncodeActivity.this, R.string.encode_finish, Toast.LENGTH_SHORT).show();
                                        stop();
                                    }
                                });
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        mBinding.btnStart.setEnabled(false);
        mBinding.btnStop.setEnabled(true);
    }

    private void stop() {
        mAudioEncoder.stop();
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioEncoder.release();
    }
}