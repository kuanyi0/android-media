package com.yikuan.androidmedia.base;

import android.media.MediaCodec;
import android.os.Build;

import androidx.annotation.RequiresApi;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class AsyncCodec<T extends CodecParam> extends BaseCodec<T> {
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
}
