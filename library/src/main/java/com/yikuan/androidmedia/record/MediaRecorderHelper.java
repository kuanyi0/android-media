package com.yikuan.androidmedia.record;

import android.hardware.display.DisplayManager;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;

import com.yikuan.androidmedia.base.State;

import java.io.IOException;

import androidx.annotation.RequiresApi;

/**
 * @author yikuan
 * @date 2020/09/20
 */
public class MediaRecorderHelper {
    private static final String TAG = "MediaRecorderHelper";
    private MediaRecorder mMediaRecorder;
    private Callback mCallback;
    private State mState = State.UNINITIALIZED;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
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
            projectionParam.projection.createVirtualDisplay(TAG, projectionParam.width, projectionParam.height, projectionParam.dpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mMediaRecorder.getSurface(), null, null);
            mState = State.CONFIGURED;
        } catch (IOException e) {
            e.printStackTrace();
            mCallback.onError(e.toString());
        }
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void start() {
        if (mState != State.CONFIGURED) {
            return;
        }
        mMediaRecorder.start();
        mState = State.RUNNING;
    }

    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaRecorder.stop();
        mState = State.STOPPED;
    }

    public void release() {
        if (mState == State.UNINITIALIZED) {
            return;
        }
        mMediaRecorder.release();
        mMediaRecorder = null;
        mState = State.RELEASED;
    }

    public State getState() {
        return mState;
    }

    public static class ProjectionParam {
        private MediaProjection projection;
        private int dpi;
        private int width;
        private int height;

        public ProjectionParam(MediaProjection projection, int dpi, int width, int height) {
            this.projection = projection;
            this.dpi = dpi;
            this.width = width;
            this.height = height;
        }
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