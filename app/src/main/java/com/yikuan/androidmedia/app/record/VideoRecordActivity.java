package com.yikuan.androidmedia.app.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidmedia.app.R;
import com.yikuan.androidmedia.app.databinding.ActivityVideoRecordBinding;
import com.yikuan.androidmedia.app.record.MediaRecordService;

public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VideoRecordActivity";
    private ActivityVideoRecordBinding mBinding;
    private Intent mMediaRecorderServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVideoRecordBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.video_record);
        mBinding.btnStart.setOnClickListener(this);
        mBinding.btnStop.setOnClickListener(this);
        mBinding.btnStop.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
    protected void onActivityResult(int requestCode, final int resultCode, @Nullable final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            mMediaRecorderServiceIntent = new Intent(this, MediaRecordService.class);
            Log.d(TAG, "onActivityResult: " + resultCode + ", " + data);
            mMediaRecorderServiceIntent.putExtra(MediaRecordService.RESULT_CODE, resultCode);
            mMediaRecorderServiceIntent.putExtra(MediaRecordService.RESULT_DATA, data);
            startForegroundService(mMediaRecorderServiceIntent);
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
        stopService(mMediaRecorderServiceIntent);
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }
}