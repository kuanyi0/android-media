package com.yikuan.androidmedia.encode;

import com.yikuan.androidmedia.codec.BaseCodec;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public class VideoParam extends BaseCodec.Param {
    /**
     * 视频宽度
     */
    int width;
    /**
     * 视频高度
     */
    int height;
    /**
     * 比特率
     *
     * 1080p: 8/12Mbps
     * 720p: 4/5Mbps
     * 576p: 3/3.5Mbps
     * 480p: 2/2.5Mbps
     * 432p: 1.8Mbps
     * 360p: 1.5Mbps
     * 240p: 1Mbps
     */
    int bitRate;
    /**
     * 颜色格式
     *
     * @see android.media.MediaCodecInfo.CodecCapabilities#COLOR_FormatSurface
     */
    int colorFormat;
    /**
     * 帧率
     *
     * 电影：24
     * 电视：25/30
     * 液晶显示器：60-75
     * CRT显示器：60-85
     * 3D显示器：120
     */
    int frameRate;
    /**
     * I帧间隔
     *
     * 1
     */
    int iFrameInterval;

    public VideoParam(String type, int width, int height, int bitRate, int colorFormat, int frameRate, int iFrameInterval) {
        this.type = type;
        this.width = width;
        this.height = height;
        this.bitRate = bitRate;
        this.colorFormat = colorFormat;
        this.frameRate = frameRate;
        this.iFrameInterval = iFrameInterval;
    }
}
