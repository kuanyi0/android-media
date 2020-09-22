package com.yikuan.androidmedia.app;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidmedia.app.databinding.ActivityAudioEncodeBinding;

public class AudioEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityAudioEncodeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAudioEncodeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.audio_encode);
        mBinding.btnStart.setOnClickListener(this);
        mBinding.btnStop.setOnClickListener(this);
        mBinding.btnStop.setEnabled(false);
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