package com.yikuan.androidmedia.encode;

import com.yikuan.androidmedia.base.CodecParam;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public class AudioParam extends CodecParam {
    /**
     * 采样率
     *
     * 音频CD：44100
     * miniDV数码视频camcorder：32000
     * FM调频广播：24000, 22050
     * AM调幅广播：11025
     * 电话：8000
     */
    int sampleRate;
    /**
     * 通道数
     *
     * 单声道：1
     * 立体声：2
     */
    int channel;
    /**
     * 比特率
     *
     * 32000
     * 64000
     * 96000
     * 128000
     */
    int bitRate;

    public AudioParam(String type, int sampleRate, int channel, int bitRate) {
        this.type = type;
        this.sampleRate = sampleRate;
        this.channel = channel;
        this.bitRate = bitRate;
    }
}
