package com.yikuan.androidmedia.encode;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.yikuan.androidmedia.codec.BaseCodec;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public class AudioEncodeParam extends BaseCodec.Param {
    @SuppressLint("InlinedApi")
    public static final String TYPE = MediaFormat.MIMETYPE_AUDIO_AAC;
    public static final int SAMPLE_RATE = 44100;
    public static final int CHANNEL = 1;
    public static final int BIT_RATE = 64000;
    public static final int MAX_INPUT_SIZE = 4 * 1024;
    public static final int AAC_PROFILE = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    /**
     * 采样率
     *
     * 音频CD：44100
     * miniDV数码视频camcorder：32000
     * FM调频广播：24000, 22050
     * AM调幅广播：11025
     * 电话：8000
     */
    int sampleRate = SAMPLE_RATE;
    /**
     * 通道数
     *
     * 单声道：1
     * 立体声：2
     */
    int channel = CHANNEL;
    /**
     * 比特率
     *
     * 32000
     * 64000
     * 96000
     * 128000
     *
     * sampleRate * bit * channel / 18
     * e.g. 44100 * 16 * 2 / 18 = 78400
     */
    int bitRate = BIT_RATE;
    /**
     * 最小输入大小
     *
     * 4 * 1024
     */
    int maxInputSize = MAX_INPUT_SIZE;
    /**
     * AAC质量
     *
     * @see MediaCodecInfo.CodecProfileLevel#AACObjectLC
     */
    int aacProfile = AAC_PROFILE;

    public AudioEncodeParam() {
        super(TYPE);
    }

    public static class Builder extends BaseCodec.Param.Builder<AudioEncodeParam> {

        public Builder() {
            super(AudioEncodeParam.class);
        }

        public Builder setType(String type) {
            param.type = type;
            return this;
        }

        public Builder setSampleRate(int sampleRate) {
            param.sampleRate = sampleRate;
            return this;
        }

        public Builder setChannel(int channel) {
            param.channel = channel;
            return this;
        }

        public Builder setBitRate(int bitRate) {
            param.bitRate = bitRate;
            return this;
        }

        public Builder setMaxInputSize(int maxInputSize) {
            param.maxInputSize = maxInputSize;
            return this;
        }

        public Builder setAacProfile(int aacProfile) {
            param.aacProfile = aacProfile;
            return this;
        }
    }
}
