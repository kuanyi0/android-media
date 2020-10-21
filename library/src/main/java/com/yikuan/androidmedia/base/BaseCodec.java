package com.yikuan.androidmedia.base;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import java.io.IOException;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class BaseCodec<T extends CodecParam> {
    protected MediaCodec mMediaCodec;
    protected T mParam;
    protected volatile State mState = State.UNINITIALIZED;

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

    public void start() {
        if (mState != State.CONFIGURED && mState != State.STOPPED) {
            return;
        }
        if (mState == State.STOPPED) {
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

    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mState = State.STOPPING;
        mMediaCodec.stop();
        mState = State.STOPPED;
    }

    public void reset() {
        release();
        mState = State.UNINITIALIZED;
    }

    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaCodec.release();
        mMediaCodec = null;
        mState = State.RELEASED;
    }

    public State getState() {
        return mState;
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
}