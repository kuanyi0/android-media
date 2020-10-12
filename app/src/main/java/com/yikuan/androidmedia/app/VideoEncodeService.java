package com.yikuan.androidmedia.app;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.yikuan.androidcommon.util.ScreenUtils;
import com.yikuan.androidmedia.encode.VideoEncoder;

import java.nio.ByteBuffer;
import java.util.Objects;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class VideoEncodeService extends Service {
    public static final String RESULT_CODE = "resultCode";
    public static final String RESULT_DATA = "resultData";
    private static final String TAG = "VideoEncodeService";
    private VideoEncoder mVideoEncoder;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    public VideoEncodeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = new NotificationCompat.Builder(this, "").build();
        startForeground(1, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(intent.getIntExtra(RESULT_CODE, 0),
                (Intent) Objects.requireNonNull(intent.getParcelableExtra(RESULT_DATA)));
        initEncoder();
        initVirtualDisplay();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initVirtualDisplay() {
        Surface surface = mVideoEncoder.getInputSurface();
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(),
                ScreenUtils.getScreenDpi(), DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null);
        mVideoEncoder.start();
    }

    private void initEncoder() {
        mVideoEncoder = new VideoEncoder();
        mVideoEncoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                Log.d(TAG, "onInputBufferAvailable: " + index);
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.d(TAG, "onOutputBufferAvailable: " + index);
                ByteBuffer byteBuffer = codec.getOutputBuffer(index);
                Log.d(TAG, "onOutputBufferAvailable: " + byteBuffer.toString());
                codec.releaseOutputBuffer(index, false);
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
        VideoEncoder.Param param = new VideoEncoder.Param(MediaFormat.MIMETYPE_VIDEO_AVC, ScreenUtils.getScreenWidth(),
                ScreenUtils.getScreenHeight(), 8 * 1024 * 1024, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface, 30, 1);
        mVideoEncoder.configure(param);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mVideoEncoder.stop();
        mVideoEncoder.release();
        mVirtualDisplay.release();
        mMediaProjection.stop();
    }
}
