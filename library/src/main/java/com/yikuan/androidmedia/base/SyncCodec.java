package com.yikuan.androidmedia.base;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class SyncCodec<T extends CodecParam> extends BaseCodec<T> {
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private MediaCodec.BufferInfo mOutputBufferInfo = new MediaCodec.BufferInfo();
    private Callback mCallback;

    @Override
    protected void prepare() {

    }

    @Override
    public void start() {
        super.start();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mInputBuffers = mMediaCodec.getInputBuffers();
            mOutputBuffers = mMediaCodec.getOutputBuffers();
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void write(byte[] data) {
        if (mState != State.RUNNING) {
            return;
        }
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = mInputBuffers[inputBufferIndex];
            }
            inputBuffer.put(data);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
        }
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, 0);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
            } else {
                outputBuffer = mOutputBuffers[outputBufferIndex];
            }
            byte[] bytes = new byte[mOutputBufferInfo.size];
            outputBuffer.get(bytes);
            mCallback.onOutputAvailable(bytes);
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, 0);
        }
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat mediaFormat = mMediaCodec.getOutputFormat();
            mCallback.onOutputFormatChanged(mediaFormat);
        }
    }

    public void read() {
        if (mState != State.RUNNING) {
            return;
        }
        int index = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, 0);
        if (index >= 0) {
            ByteBuffer outputBuffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                outputBuffer = mMediaCodec.getOutputBuffer(index);
            } else {
                outputBuffer = mOutputBuffers[index];
            }
            byte[] bytes = new byte[mOutputBufferInfo.size];
            outputBuffer.get(bytes);
            mCallback.onOutputAvailable(bytes);
            mMediaCodec.releaseOutputBuffer(index, false);
        } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat mediaFormat = mMediaCodec.getOutputFormat();
            mCallback.onOutputFormatChanged(mediaFormat);
        }
    }

    public interface Callback {
        void onOutputFormatChanged(MediaFormat format);

        void onOutputAvailable(byte[] output);
    }
}
