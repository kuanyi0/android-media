package com.yikuan.androidmedia.codec;

import android.media.MediaCodec;
import android.media.MediaFormat;

import java.nio.ByteBuffer;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class SyncCodec<T extends BaseCodec.Param> extends BaseCodec<T> {
    private static final int TIMEOUT_US = 10000;
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

    public synchronized void write(byte[] data, long pts) {
        if (!isRunning()) {
            return;
        }
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(TIMEOUT_US);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = mInputBuffers[inputBufferIndex];
                inputBuffer.clear();
            }
            inputBuffer.put(data);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, pts, 0);
        }
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, TIMEOUT_US);
        while (outputBufferIndex >= 0) {
            readOutputBuffer(outputBufferIndex);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, TIMEOUT_US);
        }
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED && mCallback != null) {
            MediaFormat mediaFormat = mMediaCodec.getOutputFormat();
            mCallback.onOutputFormatChanged(mediaFormat);
        }
    }

    public synchronized void read() {
        if (!isRunning()) {
            return;
        }
        int index = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, 0);
        if (index >= 0) {
            readOutputBuffer(index);
        } else if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED && mCallback != null) {
            MediaFormat mediaFormat = mMediaCodec.getOutputFormat();
            mCallback.onOutputFormatChanged(mediaFormat);
        }
    }

    private void readOutputBuffer(int index) {
        if ((mOutputBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            mOutputBufferInfo.size = 0;
        }
        ByteBuffer outputBuffer;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            outputBuffer = mMediaCodec.getOutputBuffer(index);
        } else {
            outputBuffer = mOutputBuffers[index];
        }
        if (mOutputBufferInfo.size > 0 && mCallback != null) {
            mCallback.onOutputBufferAvailable(index, outputBuffer, mOutputBufferInfo);
        }
        mMediaCodec.releaseOutputBuffer(index, false);
    }

    public interface Callback {
        void onOutputFormatChanged(MediaFormat format);

        void onOutputBufferAvailable(int index, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo);
    }
}
