package com.yikuan.androidmedia.encode;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;

import java.io.IOException;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class VideoEncoder {
    private MediaCodec mMediaCodec;
    private Param mParam;
    private MediaCodec.Callback mCodecCallback;
    private State mState = State.UNINITIALIZED;

    public void setCallback(MediaCodec.Callback callback) {
        mCodecCallback = callback;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void configure(Param param) {
        if (mState != State.UNINITIALIZED) {
            return;
        }
        try {
            mMediaCodec = MediaCodec.createEncoderByType(param.type);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (mCodecCallback == null) {
            return;
        }
        mMediaCodec.setCallback(mCodecCallback);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(param.type, param.width, param.height);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, param.bitRate);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, param.colorFormat);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, param.frameRate);
        mediaFormat.setInteger(MediaFormat.KEY_CAPTURE_RATE, param.frameRate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, param.iFrameInterval);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mParam = param;
        mState = State.CONFIGURED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public Surface getInputSurface() {
        if (mState != State.CONFIGURED) {
            return null;
        }
        return mMediaCodec.createInputSurface();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void start() {
        if (mState != State.CONFIGURED && mState != State.STOPPED) {
            return;
        }
        if (mState == State.STOPPED) {
            configure(mParam);
        }
        mMediaCodec.start();
        mState = State.RUNNING;
    }

    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaCodec.stop();
        mState = State.STOPPED;
    }

    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaCodec.release();
        mMediaCodec = null;
        mState = State.RELEASED;
    }

    public State getState() {
        return mState;
    }

    public static class Param {
        /**
         * MIME类型
         *
         * @see MediaFormat#MIMETYPE_VIDEO_AVC
         */
        private String type;
        /**
         * 视频宽度
         */
        private int width;
        /**
         * 视频高度
         */
        private int height;
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
        private int bitRate;
        /**
         * 颜色格式
         *
         * @see android.media.MediaCodecInfo.CodecCapabilities#COLOR_FormatSurface
         */
        private int colorFormat;
        /**
         * 帧率
         *
         * 电影：24
         * 电视：25/30
         * 液晶显示器：60-75
         * CRT显示器：60-85
         * 3D显示器：120
         */
        private int frameRate;
        /**
         * I帧间隔
         *
         * 1
         */
        private int iFrameInterval;

        public Param(String type, int width, int height, int bitRate, int colorFormat, int frameRate, int iFrameInterval) {
            this.type = type;
            this.width = width;
            this.height = height;
            this.bitRate = bitRate;
            this.colorFormat = colorFormat;
            this.frameRate = frameRate;
            this.iFrameInterval = iFrameInterval;
        }
    }

}
