package com.yikuan.androidmedia.record;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;

import com.yikuan.androidmedia.encode.AudioEncodeParam;
import com.yikuan.androidmedia.encode.VideoEncodeParam;

/**
 * @author yikuan
 * @date 2021/02/08
 */
public class ScreenRecordParam {
    public static class AudioParam {
        AudioRecorder.Param recordParam;
        AudioEncodeParam encodeParam;

        public AudioParam(AudioRecorder.Param recordParam, AudioEncodeParam encodeParam) {
            this.recordParam = recordParam;
            this.encodeParam = encodeParam;
        }
    }

    public static class VideoParam {
        ProjectionParam projectionParam;
        VideoEncodeParam encodeParam;

        public VideoParam(ProjectionParam projectionParam, VideoEncodeParam encodeParam) {
            this.projectionParam = projectionParam;
            this.encodeParam = encodeParam;
        }
    }

    // See AudioRecorder.Param and AudioEncodeParam
    int audioSource = MediaRecorder.AudioSource.MIC;
    int sampleRateInHz = 44100;
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int audioBitRate = 64000;
    int aacProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

    // See ProjectionParam and VideoEncodeParam
    MediaProjection projection;
    int width = 1080;
    int height = 1920;
    int videoBitRate = 8 * 1024 * 1024;
    @SuppressLint("InlinedApi")
    int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
    int frameRate = 30;
    int iFrameInterval = 1;

    // See MediaMuxerHelper.Param
    String path;
    @SuppressLint("InlinedApi")
    int format = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

    public ScreenRecordParam(MediaProjection projection, String path) {
        this.projection = projection;
        this.path = path;
    }

    public void setAudioSource(int audioSource) {
        this.audioSource = audioSource;
    }

    public void setSampleRateInHz(int sampleRateInHz) {
        this.sampleRateInHz = sampleRateInHz;
    }

    public void setChannelConfig(int channelConfig) {
        this.channelConfig = channelConfig;
    }

    public void setAudioFormat(int audioFormat) {
        this.audioFormat = audioFormat;
    }

    public void setAudioBitRate(int audioBitRate) {
        this.audioBitRate = audioBitRate;
    }

    public void setAacProfile(int aacProfile) {
        this.aacProfile = aacProfile;
    }

    public void setProjection(MediaProjection projection) {
        this.projection = projection;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setVideoBitRate(int videoBitRate) {
        this.videoBitRate = videoBitRate;
    }

    public void setColorFormat(int colorFormat) {
        this.colorFormat = colorFormat;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public void setIFrameInterval(int iFrameInterval) {
        this.iFrameInterval = iFrameInterval;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFormat(int format) {
        this.format = format;
    }
}
