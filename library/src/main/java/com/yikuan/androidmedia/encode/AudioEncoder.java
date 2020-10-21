package com.yikuan.androidmedia.encode;

import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.yikuan.androidmedia.codec.SyncCodec;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class AudioEncoder extends SyncCodec<AudioParam> {
    public static int MAX_INPUT_SIZE = 4 * 1024;

    @Override
    protected boolean isEncoder() {
        return true;
    }

    @Override
    protected MediaFormat configureMediaFormat() {
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(mParam.type, mParam.sampleRate, mParam.channel);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mParam.bitRate);
        // Optional
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);
        // Optional
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        return mediaFormat;
    }
}
