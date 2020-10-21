package com.yikuan.androidmedia.mux;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yikuan
 * @date 2020/10/21
 */
class MediaMuxerHelper {
    private MediaMuxer mMediaMuxer;
    private State mState = State.UNINITIALIZED;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void configure(Param param) {
        if (mState != State.UNINITIALIZED) {
            return;
        }
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
        if (mState != State.CONFIGURED) {
            return -1;
        }
        return mMediaMuxer.addTrack(mediaFormat);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void start() {
        if (mState != State.CONFIGURED) {
            return;
        }
        mMediaMuxer.start();
        mState = State.RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void write(int trackIndex, ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaMuxer.writeSampleData(trackIndex, buffer, bufferInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaMuxer.stop();
        mState = State.STOPPED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void reset() {
        release();
        mState = State.UNINITIALIZED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaMuxer.release();
        mMediaMuxer = null;
        mState = State.RELEASED;
    }

    public State getState() {
        return mState;
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
