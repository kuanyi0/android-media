package com.yikuan.androidmedia.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.yikuan.androidcommon.util.FileUtils;
import com.yikuan.androidcommon.util.PermissionUtils;
import com.yikuan.androidmedia.app.databinding.ActivityMainBinding;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String[] mPermissions = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.btnAudioRecord.setOnClickListener(this);
        mBinding.btnVideoRecord.setOnClickListener(this);
        mBinding.btnAudioEncode.setOnClickListener(this);
        mBinding.btnVideoEncode.setOnClickListener(this);
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
        for (String dir : Constant.DIRS) {
            try {
                FileUtils.forceMkdir(new File(dir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
            case R.id.btn_audio_encode:
                startActivity(new Intent(this, AudioEncodeActivity.class));
                break;
            case R.id.btn_video_encode:
                startActivity(new Intent(this, VideoEncodeActivity.class));
                break;
            default:
                break;
        }
    }
}