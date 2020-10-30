package com.yikuan.androidmedia.mux;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * @author yikuan
 * @date 2020/10/21
 */
public class MediaMuxerHelper extends Worker1<MediaMuxerHelper.Param> {
    private MediaMuxer mMediaMuxer;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void configure(Param param) {
        checkCurrentStateInStates(State.UNINITIALIZED);
        try {
            mMediaMuxer = new MediaMuxer(param.path, param.format);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mState = State.CONFIGURED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public int addTrack(MediaFormat mediaFormat) {
        checkCurrentStateInStates(State.CONFIGURED);
        return mMediaMuxer.addTrack(mediaFormat);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void start() {
        if (mState == State.RUNNING) {
            return;
        }
        checkCurrentStateInStates(State.CONFIGURED);
        mMediaMuxer.start();
        mState = State.RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public synchronized void write(int trackIndex, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        if (!isRunning()) {
            return;
        }
        mMediaMuxer.writeSampleData(trackIndex, buffer, bufferInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public synchronized void stop() {
        if (mState == State.STOPPED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING);
        mMediaMuxer.stop();
        mState = State.STOPPED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaMuxer.release();
        mMediaMuxer = null;
        mState = State.RELEASED;
    }

    public static class Param {
        /**
         * 输出路径
         */
        private String path;
        /**
         * 输出媒体格式
         *
         * @see MediaMuxer.OutputFormat
         */
        private int format;

        public Param(String path, int format) {
            this.path = path;
            this.format = format;
        }
    }
}
