package com.yikuan.androidmedia.app;

import android.media.MediaCodecInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidmedia.app.databinding.ActivityAudioEncodeBinding;
import com.yikuan.androidmedia.codec.MediaCodecUtils;

import java.util.ArrayList;
import java.util.List;

public class AudioEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityAudioEncodeBinding mBinding;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAudioEncodeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.audio_encode);
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

    private void start() {
        mBinding.btnStart.setEnabled(false);
        mBinding.btnStop.setEnabled(true);
    }

    private void stop() {
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }
}