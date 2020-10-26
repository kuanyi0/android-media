package com.yikuan.androidmedia.codec;

import android.media.MediaCodec;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;

import java.nio.ByteBuffer;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class AsyncCodec<T extends BaseCodec.Param> extends BaseCodec<T> {
    private MediaCodec.Callback mCodecCallback;

    public void setCallback(MediaCodec.Callback callback) {
        mCodecCallback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void prepare() {
        if (mCodecCallback == null) {
            return;
        }
        mMediaCodec.setCallback(mCodecCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void write(int index, byte[] data, long pts) {
        if (mState != State.RUNNING) {
            return;
        }
        ByteBuffer inputBuffer = mMediaCodec.getInputBuffer(index);
        inputBuffer.put(data, 0, data.length);
        mMediaCodec.queueInputBuffer(index, 0, data.length, pts, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ByteBuffer read(int index) {
        if (mState != State.RUNNING) {
            return null;
        }
        ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(index);
        mMediaCodec.releaseOutputBuffer(index, false);
        return outputBuffer;
    }
}
