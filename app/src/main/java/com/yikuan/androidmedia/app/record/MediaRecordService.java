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
import com.yikuan.androidmedia.record.MediaRecordHelper;
import com.yikuan.androidmedia.record.ProjectionParam;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MediaRecordService extends MediaProjectionService {
    private static final String TAG = "MediaRecorderService";
    private MediaRecordHelper mMediaRecordHelper;

    public MediaRecordService() {
    }

    @Override
    protected void start() {
        startRecord();
    }

    private void startRecord() {
        ProjectionParam projectionParam = new ProjectionParam(mMediaProjection,
                ScreenUtils.getScreenDpi(), ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        MediaRecordHelper.MediaParam mediaParam = new MediaRecordHelper.MediaParam(MediaRecorder.AudioSource.MIC,
                MediaRecorder.VideoSource.SURFACE, MediaRecorder.AudioEncoder.AAC, MediaRecorder.VideoEncoder.H264,
                ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), MediaRecorder.OutputFormat.MPEG_4,
                Constant.DIR_VIDEO_RECORD + "/" + DateUtils.formatTimeFileName() + ".mp4");
        mMediaRecordHelper = new MediaRecordHelper();
        mMediaRecordHelper.configure(projectionParam, mediaParam);
        if (mMediaRecordHelper.getState() == State.CONFIGURED) {
            mMediaRecordHelper.start();
        } else {
            Log.e(TAG, "video record error");
        }
    }

    @Override
    protected void stop() {
        mMediaRecordHelper.stop();
        mMediaRecordHelper.release();
    }
}
