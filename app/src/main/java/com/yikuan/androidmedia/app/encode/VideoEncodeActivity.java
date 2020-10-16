package com.yikuan.androidmedia.app.encode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidmedia.app.R;
import com.yikuan.androidmedia.app.databinding.ActivityVideoEncodeBinding;

public class VideoEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VideoEncodeActivity";
    private ActivityVideoEncodeBinding mBinding;
    private Intent mVideoEncodeServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVideoEncodeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.btnStart.setOnClickListener(this);
        mBinding.btnStop.setOnClickListener(this);
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            mVideoEncodeServiceIntent = new Intent(this, VideoEncodeService.class);
            mVideoEncodeServiceIntent.putExtra(VideoEncodeService.RESULT_CODE, resultCode);
            mVideoEncodeServiceIntent.putExtra(VideoEncodeService.RESULT_DATA, data);
            startForegroundService(mVideoEncodeServiceIntent);
        } else {
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void start() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, 0);
        mBinding.btnStart.setEnabled(false);
        mBinding.btnStop.setEnabled(true);
    }

    private void stop() {
        stopService(mVideoEncodeServiceIntent);
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }
}