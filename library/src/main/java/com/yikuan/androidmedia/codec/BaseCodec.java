package com.yikuan.androidmedia.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker1;

import java.io.IOException;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class BaseCodec<T extends BaseCodec.Param> extends Worker1<T> {
    protected MediaCodec mMediaCodec;
    protected T mParam;

    @Override
    public void configure(T param) {
        if (mState != State.UNINITIALIZED) {
            return;
        }
        try {
            if (isEncoder()) {
                mMediaCodec = MediaCodec.createEncoderByType(param.type);
            } else {
                mMediaCodec = MediaCodec.createDecoderByType(param.type);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mParam = param;
        internalConfigure();
        mState = State.CONFIGURED;
    }

    private void internalConfigure() {
        prepare();
        mMediaCodec.configure(configureMediaFormat(), null, null, isEncoder() ? MediaCodec.CONFIGURE_FLAG_ENCODE : 0);
    }

    @Override
    public void start() {
        if (mState != State.CONFIGURED && mState != State.STOPPED) {
            return;
        }
        if (mState == State.STOPPED) {
            mMediaCodec.stop();
            internalConfigure();
        }
        mMediaCodec.start();
        mState = State.RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Surface getInputSurface() {
        if (mState != State.CONFIGURED) {
            return null;
        }
        return mMediaCodec.createInputSurface();
    }

    @Override
    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mState = State.STOPPED;
    }

    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        mState = State.RELEASED;
    }

    /**
     * 是否是编码器
     *
     * @return {@code true} if it's encoder, {@code false} otherwise
     */
    protected abstract boolean isEncoder();
    /**
     * Codec准备
     */
    protected abstract void prepare();
    /**
     * Codec配置时的MediaFormat
     *
     * @return MediaFormat
     */
    protected abstract MediaFormat configureMediaFormat();

    public abstract static class Param {
        /**
         * MIME类型
         *
         * @see MediaFormat#MIMETYPE_AUDIO_AAC
         * @see MediaFormat#MIMETYPE_VIDEO_AVC
         */
        public String type;
    }
}