package com.yikuan.androidmedia.encode;

import android.annotation.SuppressLint;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;

import com.yikuan.androidmedia.codec.BaseCodec;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public class VideoEncodeParam extends BaseCodec.Param {
    @SuppressLint("InlinedApi")
    public static final String TYPE = MediaFormat.MIMETYPE_VIDEO_AVC;
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1080;
    public static final int BIT_RATE = 8 * 1024 * 1024;
    @SuppressLint("InlinedApi")
    public static final int COLOR_FORMAT = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    public static final int FRAME_RATE = 30;
    public static final int I_FRAME_INTERVAL = 1;

    /**
     * 视频宽度
     */
    int width = WIDTH;
    /**
     * 视频高度
     */
    int height = HEIGHT;
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
    int bitRate = BIT_RATE;
    /**
     * 颜色格式
     *
     * @see android.media.MediaCodecInfo.CodecCapabilities#COLOR_FormatSurface
     */
    int colorFormat = COLOR_FORMAT;
    /**
     * 帧率
     *
     * 电影：24
     * 电视：25/30
     * 液晶显示器：60-75
     * CRT显示器：60-85
     * 3D显示器：120
     */
    int frameRate = FRAME_RATE;
    /**
     * I帧间隔
     *
     * 1
     * iFrameInterval * frameRate
     * eg. 30 * 1 frames
     */
    int iFrameInterval = I_FRAME_INTERVAL;

    public VideoEncodeParam() {
        super(TYPE);
    }

    public static class Builder extends BaseCodec.Param.Builder<VideoEncodeParam> {

        public Builder() {
            super(VideoEncodeParam.class);
        }

        public Builder setType(String type) {
            param.type = type;
            return this;
        }

        public Builder setWidth(int width) {
            param.width = width;
            return this;
        }

        public Builder setHeight(int height) {
            param.height = height;
            return this;
        }

        public Builder setBitrate(int bitRate) {
            param.bitRate = bitRate;
            return this;
        }

        public Builder setColorFormat(int colorFormat) {
            param.colorFormat = colorFormat;
            return this;
        }

        public Builder setFrameRate(int frameRate) {
            param.frameRate = frameRate;
            return this;
        }

        public Builder setIFrameInterval(int iFrameInterval) {
            param.iFrameInterval = iFrameInterval;
            return this;
        }
    }
}
