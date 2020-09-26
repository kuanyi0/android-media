package com.yikuan.androidmedia.encode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class AudioEncoder {
    private MediaCodec mMediaCodec;
    private byte[] mSource;
    private State mState = State.UNINITIALIZED;
    private ExecutorService mExecutorService;
    private Runnable mEncodeRunnable;
    private Callback mCallback;

    public void configure(byte[] source, Param param) {
        mSource = source;
        try {
            mMediaCodec = MediaCodec.createEncoderByType(param.type);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        MediaFormat mediaFormat = new MediaFormat();
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mState = State.CONFIGURED;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void start() {
        if (mState != State.CONFIGURED) {
            return;
        }
        mMediaCodec.start();
        mState = State.RUNNING;
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
        if (mEncodeRunnable == null) {
            mEncodeRunnable = new EncodeRunnable();
        }
        mExecutorService.execute(mEncodeRunnable);
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
    }

    public State getState() {
        return mState;
    }

    private class EncodeRunnable implements Runnable {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void run() {
            while (mState == State.RUNNING && mCallback != null) {
                int inputBufferIndex = mMediaCodec.dequeueInputBuffer(0);
                if (inputBufferIndex >= 0) {
                    ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
                    inputBuffer.put(mSource);
                    mMediaCodec.queueInputBuffer(inputBufferIndex, 0, inputBuffer.limit(), 0, 0);
                }
                MediaCodec.BufferInfo outputBufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(outputBufferInfo, 0);
                if (outputBufferIndex >= 0) {
                    ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
                    mCallback.onOutputAvailable(outputBuffer.array());
                    mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                } else if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat bufferFormat = mMediaCodec.getOutputFormat(outputBufferIndex);
                    mCallback.onOutputFormatChanged(bufferFormat);
                }
            }
        }
    }

    public static class Param {
        private String type;
    }

    public interface Callback {
        void onOutputFormatChanged(MediaFormat format);

        void onOutputAvailable(byte[] output);
    }
}
