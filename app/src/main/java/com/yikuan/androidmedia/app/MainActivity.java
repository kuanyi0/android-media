package com.yikuan.androidmedia.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.yikuan.androidmedia.app.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.btnAudioRecord.setOnClickListener(this);
        mBinding.btnVideoRecord.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_audio_record:
                startActivity(new Intent(this, AudioRecordActivity.class));
                break;
            case R.id.btn_video_record:
                startActivity(new Intent(this, VideoRecordActivity.class));
                break;
            default:
                break;
        }
    }
}