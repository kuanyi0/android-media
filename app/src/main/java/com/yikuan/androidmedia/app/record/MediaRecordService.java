package com.yikuan.androidmedia.app.record;

import android.media.MediaRecorder;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yikuan.androidcommon.util.DateUtils;
import com.yikuan.androidcommon.util.ScreenUtils;
import com.yikuan.androidmedia.app.Constant;
import com.yikuan.androidmedia.app.base.MediaProjectionService;
import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.record.MediaRecorderHelper;
import com.yikuan.androidmedia.record.ProjectionParam;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MediaRecordService extends MediaProjectionService {
    private static final String TAG = "MediaRecorderService";
    private MediaRecorderHelper mMediaRecorderHelper;

    public MediaRecordService() {
    }

    @Override
    protected void start() {
        startRecord();
    }

    private void startRecord() {
        ProjectionParam projectionParam = new ProjectionParam(mMediaProjection,
                ScreenUtils.getScreenDpi(), ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        MediaRecorderHelper.MediaParam mediaParam = new MediaRecorderHelper.MediaParam(MediaRecorder.AudioSource.MIC,
                MediaRecorder.VideoSource.SURFACE, MediaRecorder.AudioEncoder.AAC, MediaRecorder.VideoEncoder.H264,
                ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), MediaRecorder.OutputFormat.MPEG_4,
                Constant.DIR_VIDEO_RECORD + "/" + DateUtils.formatTimeFileName() + ".mp4");
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
    protected void stop() {
        mMediaRecorderHelper.stop();
        mMediaRecorderHelper.release();
    }
}
