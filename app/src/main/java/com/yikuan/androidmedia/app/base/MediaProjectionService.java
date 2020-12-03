package com.yikuan.androidmedia.app.base;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Objects;

/**
 * @author yikuan
 * @date 2020/10/15
 */
public abstract class MediaProjectionService extends Service {
    public static final String RESULT_CODE = "resultCode";
    public static final String RESULT_DATA = "resultData";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CHANNEL_NAME = "channel_name";
    protected MediaProjection mMediaProjection;
    private MediaProjectionBinder mBinder = new MediaProjectionBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
        startForeground(1, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getProjectionAndStart(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        getProjectionAndStart(intent);
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mBinder = null;
        return super.onUnbind(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getProjectionAndStart(Intent intent) {
        MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        mMediaProjection = mediaProjectionManager.getMediaProjection(intent.getIntExtra(RESULT_CODE, 0),
                (Intent) Objects.requireNonNull(intent.getParcelableExtra(RESULT_DATA)));
        onStart();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onDestroy() {
        super.onDestroy();
        onStop();
        mMediaProjection.stop();
    }

    protected abstract void onStart();

    protected abstract void onStop();

    public class MediaProjectionBinder extends Binder {
        public MediaProjectionService getService() {
            return MediaProjectionService.this;
        }
    }

    public boolean isConnected() {
        return mBinder != null;
    }

    public void start() {
    }

    public void resume() {
    }

    public void pause() {
    }

    public void stop() {
    }
}
