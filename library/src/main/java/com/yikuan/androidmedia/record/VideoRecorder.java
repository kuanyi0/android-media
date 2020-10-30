package com.yikuan.androidmedia.record;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker2;

/**
 * @author yikuan
 * @date 2020/09/20
 */
public class VideoRecorder extends Worker2<ProjectionParam, Surface> {
    private static final String TAG = "VideoRecorder";
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private ProjectionParam mProjectionParam;
    private Surface mSurface;

    @Override
    public void configure(ProjectionParam projectionParam, Surface surface) {
        checkCurrentStateInStates(State.UNINITIALIZED);
        mProjectionParam = projectionParam;
        mSurface = surface;
        mState = State.CONFIGURED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void start() {
        if (mState == State.RUNNING) {
            return;
        }
        checkCurrentStateInStates(State.CONFIGURED);
        mMediaProjection = mProjectionParam.projection;
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, mProjectionParam.width, mProjectionParam.height,
                mProjectionParam.dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mSurface, null, null);
        mState = State.RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void stop() {
        if (mState == State.STOPPED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING);
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        mMediaProjection.stop();
        mMediaProjection = null;
        mState = State.STOPPED;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mProjectionParam = null;
        mSurface = null;
        mState = State.RELEASED;
    }
}
