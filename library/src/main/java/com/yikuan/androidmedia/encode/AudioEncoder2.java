package com.yikuan.androidmedia.encode;

import android.media.MediaFormat;

import com.yikuan.androidmedia.codec.AsyncCodec;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class AudioEncoder2 extends AsyncCodec<AudioEncodeParam> {

    @Override
    protected boolean isEncoder() {
        return true;
    }

    @Override
    protected MediaFormat configureMediaFormat() {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(mParam.type, mParam.sampleRate, mParam.channel);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mParam.bitRate);
        // Optional
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mParam.maxInputSize);
        // Optional
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, mParam.aacProfile);
        return mediaFormat;
    }
}
