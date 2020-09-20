package com.yikuan.androidmedia.app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.yikuan.androidcommon.util.DateUtils;
import com.yikuan.androidcommon.util.FileUtils;
import com.yikuan.androidcommon.util.PathUtils;
import com.yikuan.androidcommon.util.ScreenUtils;
import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.record.MediaRecorderHelper;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class MediaRecorderService extends Service {
    public static final String RESULT_CODE = "resultCode";
    public static final String RESULT_DATA = "resultData";
    private static final String TAG = "MediaRecorderService";
    private static final String DIR = "android-media/video-record";
    private MediaRecorderHelper mMediaRecorderHelper;
    private File mDir;

    public MediaRecorderService() {
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mDir = new File(PathUtils.getExternalStoragePath() + "/" + DIR);
        try {
            FileUtils.forceMkdir(mDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        MediaProjection mediaProjection = mediaProjectionManager.getMediaProjection(intent.getIntExtra(RESULT_CODE, 0),
                (Intent) Objects.requireNonNull(intent.getParcelableExtra(RESULT_DATA)));
        startRecord(mediaProjection);
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startRecord(MediaProjection mediaProjection) {
        Log.d(TAG, "startRecord: " + mediaProjection);
        MediaRecorderHelper.ProjectionParam projectionParam = new MediaRecorderHelper.ProjectionParam(mediaProjection,
                ScreenUtils.getScreenDpi(), ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        MediaRecorderHelper.MediaParam mediaParam = new MediaRecorderHelper.MediaParam(MediaRecorder.AudioSource.MIC,
                MediaRecorder.VideoSource.SURFACE, MediaRecorder.AudioEncoder.AAC, MediaRecorder.VideoEncoder.H264,
                ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), MediaRecorder.OutputFormat.MPEG_4,
                mDir.getAbsolutePath() + "/" + DateUtils.formatTimeFileName() + ".mp4");
        mMediaRecorderHelper = new MediaRecorderHelper();
        mMediaRecorderHelper.configure(projectionParam, mediaParam);
        mMediaRecorderHelper.setCallback(new MediaRecorderHelper.Callback() {
            @Override
            public void onError(String error) {
                Log.e(TAG, "video record error: " + error);
            }
        });
        if (mMediaRecorderHelper.getState() == State.CONFIGURED) {
            mMediaRecorderHelper.start();
        } else {
            Log.e(TAG, "video record error");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMediaRecorderHelper.stop();
        mMediaRecorderHelper.release();
    }
}
