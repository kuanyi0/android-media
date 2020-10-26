package com.yikuan.androidmedia.record;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker2;

import java.io.IOException;

/**
 * @author yikuan
 * @date 2020/09/20
 */
public class MediaRecorderHelper extends Worker2<ProjectionParam, MediaRecorderHelper.MediaParam> {
    private static final String TAG = "MediaRecorderHelper";
    private MediaRecorder mMediaRecorder;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private Callback mCallback;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void configure(ProjectionParam projectionParam, MediaParam mediaParam) {
        if (mState != State.UNINITIALIZED) {
            return;
        }
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(mediaParam.audioSource);
        mMediaRecorder.setVideoSource(mediaParam.videoSource);
        mMediaRecorder.setOutputFormat(mediaParam.format);
        mMediaRecorder.setAudioEncoder(mediaParam.audioEncoder);
        mMediaRecorder.setVideoEncoder(mediaParam.videoEncoder);
        mMediaRecorder.setVideoSize(mediaParam.width, mediaParam.height);
        mMediaRecorder.setOutputFile(mediaParam.output);
        try {
            mMediaRecorder.prepare();
            mMediaProjection = projectionParam.projection;
            mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG, projectionParam.width, projectionParam.height,
                    projectionParam.dpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mMediaRecorder.getSurface(), null, null);
            mState = State.CONFIGURED;
        } catch (IOException e) {
            e.printStackTrace();
            mCallback.onError(e.toString());
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void start() {
        if (mState != State.CONFIGURED) {
            return;
        }
        mMediaRecorder.start();
        mState = State.RUNNING;
    }

    @Override
    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaRecorder.stop();
        mState = State.STOPPED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaRecorder.release();
        mMediaRecorder = null;
        mVirtualDisplay.release();
        mVirtualDisplay = null;
        mMediaProjection.stop();
        mMediaProjection = null;
        mState = State.RELEASED;
    }

    public static class MediaParam {
        /**
         * 音频输入源
         *
         * @see MediaRecorder.AudioSource
         */
        private int audioSource;
        /**
         * 视频输入源
         *
         * @see MediaRecorder.VideoSource
         */
        private int videoSource;
        /**
         * 音频编码器
         *
         * @see MediaRecorder.AudioEncoder
         */
        private int audioEncoder;
        /**
         * 视频编码器
         *
         * @see MediaRecorder.VideoEncoder
         */
        private int videoEncoder;
        /**
         * 视频宽度
         */
        private int width;
        /**
         * 视频高度
         */
        private int height;
        /**
         * 视频格式
         *
         * @see MediaRecorder.OutputFormat
         */
        private int format;
        /**
         * 视频输出路径
         */
        private String output;

        public MediaParam(int audioSource, int videoSource, int audioEncoder, int videoEncoder, int width, int height, int format, String output) {
            this.audioSource = audioSource;
            this.videoSource = videoSource;
            this.audioEncoder = audioEncoder;
            this.videoEncoder = videoEncoder;
            this.width = width;
            this.height = height;
            this.format = format;
            this.output = output;
        }
    }

    public interface Callback {
        void onError(String error);
    }
}
