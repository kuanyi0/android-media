package com.yikuan.androidmedia.app.record;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidmedia.app.R;
import com.yikuan.androidmedia.app.base.MediaProjectionService;
import com.yikuan.androidmedia.app.databinding.ActivityVideoRecordBinding;

public class VideoRecordActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "VideoRecordActivity";
    private static final int MEDIA_RECORDER = 1;
    private static final int SCREEN_RECORDER = 2;
    private int mRecorder = SCREEN_RECORDER;
    private long mStartTime;
    private long mPauseDuration;
    private ActivityVideoRecordBinding mBinding;
    private MediaProjectionService mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected: ");
            MediaProjectionService.MediaProjectionBinder binder = (MediaProjectionService.MediaProjectionBinder) service;
            mService = binder.getService();
            internalStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected: ");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVideoRecordBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.video_record);
        mBinding.btnStart.setOnClickListener(this);
        mBinding.btnResume.setOnClickListener(this);
        mBinding.btnPause.setOnClickListener(this);
        mBinding.btnStop.setOnClickListener(this);
        mBinding.btnResume.setEnabled(false);
        mBinding.btnPause.setEnabled(false);
        mBinding.btnStop.setEnabled(false);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                start();
                break;
            case R.id.btn_resume:
                resume();
                break;
            case R.id.btn_pause:
                pause();
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
            Class<? extends MediaProjectionService> cls;
            if (mRecorder == MEDIA_RECORDER) {
                cls = MediaRecordService.class;
            } else {
                cls = ScreenRecordService.class;
            }
            Intent recordServiceIntent = new Intent(this, cls);
            Log.d(TAG, "onActivityResult: " + resultCode + ", " + data);
            recordServiceIntent.putExtra(MediaRecordService.RESULT_CODE, resultCode);
            recordServiceIntent.putExtra(MediaRecordService.RESULT_DATA, data);
            bindService(recordServiceIntent, mConnection, Service.BIND_AUTO_CREATE);
        } else {
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void start() {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Intent intent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(intent, 0);
    }

    private void internalStart() {
        mService.start();
        mStartTime = SystemClock.elapsedRealtime();
        mBinding.chronometer.setBase(SystemClock.elapsedRealtime());
        mBinding.chronometer.start();
        mBinding.btnStart.setEnabled(false);
        mBinding.btnResume.setEnabled(false);
        mBinding.btnPause.setEnabled(true);
        mBinding.btnStop.setEnabled(true);
    }

    private void resume() {
        mService.resume();
        mStartTime = SystemClock.elapsedRealtime();
        mBinding.chronometer.setBase(SystemClock.elapsedRealtime() - mPauseDuration);
        mBinding.chronometer.start();
        mBinding.btnResume.setEnabled(false);
        mBinding.btnPause.setEnabled(true);
    }

    private void pause() {
        mService.pause();
        mPauseDuration += SystemClock.elapsedRealtime() - mStartTime;
        mBinding.chronometer.stop();
        mBinding.btnResume.setEnabled(true);
        mBinding.btnPause.setEnabled(false);
    }

    private void stop() {
        unbindService(mConnection);
        mBinding.chronometer.stop();
        mBinding.btnStart.setEnabled(true);
        mBinding.btnResume.setEnabled(false);
        mBinding.btnPause.setEnabled(false);
        mBinding.btnStop.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mService != null && mService.isConnected()) {
            stop();
        }
    }
}