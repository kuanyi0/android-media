package com.yikuan.androidmedia.app.encode;

import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.yikuan.androidcommon.util.ThreadPoolManager;
import com.yikuan.androidmedia.app.Constant;
import com.yikuan.androidmedia.app.R;
import com.yikuan.androidmedia.app.Utils;
import com.yikuan.androidmedia.app.databinding.ActivityAudioEncodeBinding;
import com.yikuan.androidmedia.encode.AudioEncoder;
import com.yikuan.androidmedia.encode.AudioEncoder2;
import com.yikuan.androidmedia.encode.AudioParam;
import com.yikuan.androidmedia.util.MediaCodecUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AudioEncodeActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AudioEncodeActivity";
    private static final boolean ASYNC_MODE = true;
    private ActivityAudioEncodeBinding mBinding;
    private AudioEncoder mAudioEncoder;
    private AudioEncoder2 mAudioEncoder2;
    private AudioParam mParam = new AudioParam(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1, 96000);
    private File mSourceFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAudioEncodeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setTitle(R.string.audio_encode);
        initView();
        if (ASYNC_MODE) {
            initEncoder();
        } else {
            initEncoder2();
        }
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
        mAudioEncoder.configure(mParam);
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

    private void initEncoder2() {
        mAudioEncoder2 = new AudioEncoder2();
        mAudioEncoder2.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                Log.d(TAG, "onInputBufferAvailable: ");
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.d(TAG, "onOutputBufferAvailable: ");
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                Log.d(TAG, "onError: ");
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                Log.d(TAG, "onOutputFormatChanged: ");
            }
        });
        mAudioEncoder2.configure(mParam);
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
        if (mAudioEncoder != null) {
            mAudioEncoder.start();
            ThreadPoolManager.getInstance().execute(new Runnable() {
                byte[] data = new byte[AudioEncoder.MAX_INPUT_SIZE];

                @Override
                public void run() {
                    final FileInputStream inputStream;
                    try {
                        inputStream = new FileInputStream(mSourceFile);
                    } catch (FileNotFoundException ex) {
                        ex.printStackTrace();
                        return;
                    }
                    while (true) {
                        try {
                            int length = inputStream.read(data);
                            if (length > 0) {
                                mAudioEncoder.write(data);
                            } else {
                                encodeFinish();
                                break;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            mAudioEncoder2.start();
        }
        mBinding.btnStart.setEnabled(false);
        mBinding.btnStop.setEnabled(true);
    }

    private void encodeFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AudioEncodeActivity.this, R.string.encode_finish, Toast.LENGTH_SHORT).show();
                stop();
            }
        });
    }

    private void stop() {
        if (mAudioEncoder != null) {
            mAudioEncoder.stop();
        } else {
            mAudioEncoder2.stop();
        }
        mBinding.btnStart.setEnabled(true);
        mBinding.btnStop.setEnabled(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioEncoder != null) {
            mAudioEncoder.release();
        } else {
            mAudioEncoder2.release();
        }
    }
}