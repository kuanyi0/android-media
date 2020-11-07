package com.yikuan.androidmedia.app.record;

import android.os.Build;
import androidx.annotation.RequiresApi;
import com.yikuan.androidcommon.util.DateUtils;
import com.yikuan.androidmedia.app.Constant;
import com.yikuan.androidmedia.app.base.MediaProjectionService;
import com.yikuan.androidmedia.record.ScreenRecorder;

/**
 * @author yikuan
 * @date 2020/10/24
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecordService extends MediaProjectionService {
    private ScreenRecorder mScreenRecorder = new ScreenRecorder();

    @Override
    protected void onStart() {
        ScreenRecorder.Param param = new ScreenRecorder.Param(mMediaProjection, Constant.DIR_VIDEO_RECORD + "/" +
                DateUtils.formatTimeFileName() + ".mp4");
        mScreenRecorder.configure(param);
    }

    @Override
    protected void onStop() {
        mScreenRecorder.stop();
    }

    @Override
    public void start() {
        super.start();
        mScreenRecorder.start();
    }

    @Override
    public void resume() {
        super.resume();
        mScreenRecorder.resume();
    }

    @Override
    public void pause() {
        super.pause();
        mScreenRecorder.pause();
    }

    @Override
    public void stop() {
        super.stop();
        mScreenRecorder.stop();
    }
}
