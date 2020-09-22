package com.yikuan.androidmedia.encode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;

import java.io.IOException;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class VideoEncoder {
    private MediaCodec mMediaCodec;
    private MediaFormat mOutputFormat;
    private State mState = State.UNINITIALIZED;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void configure(Param param, Surface surface) {
        try {
            mMediaCodec = MediaCodec.createEncoderByType(param.type);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mMediaCodec.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {

            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {

            }
        });
        MediaFormat mediaFormat = new MediaFormat();
        mMediaCodec.configure(mediaFormat, surface, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mState = State.CONFIGURED;
    }

    public void start() {
        if (mState != State.CONFIGURED) {
            return;
        }
        mMediaCodec.start();
        mState = State.RUNNING;
    }

    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaCodec.stop();
        mState = State.STOPPED;
    }

    public void release() {
        if (mState == State.UNINITIALIZED) {
            return;
        }
        mMediaCodec.release();
        mMediaCodec = null;
        mState = State.RELEASED;
    }

    public State getState() {
        return mState;
    }

    public static class Param {
        private String type;

    }

}
