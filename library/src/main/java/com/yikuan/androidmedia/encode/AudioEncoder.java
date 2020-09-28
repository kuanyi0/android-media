package com.yikuan.androidmedia.encode;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import com.yikuan.androidmedia.base.State;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class AudioEncoder {
    public static int MAX_INPUT_SIZE = 1024 * 8;
    private MediaCodec mMediaCodec;
    private Param mParam;
    private ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;
    private MediaCodec.BufferInfo mOutputBufferInfo;
    private State mState = State.UNINITIALIZED;
    private Callback mCallback;

    public void configure(Param param) {
        try {
            mMediaCodec = MediaCodec.createEncoderByType(param.type);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        MediaFormat mediaFormat = MediaFormat.createAudioFormat(param.type, param.sampleRate, param.channel);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, param.bitRate);
        // Optional
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, MAX_INPUT_SIZE);
        // Optional
        mediaFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
        mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mOutputBufferInfo = new MediaCodec.BufferInfo();
        mParam = param;
        mState = State.CONFIGURED;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void start() {
        if (mState != State.CONFIGURED && mState != State.STOPPED) {
            return;
        }
        if (mState == State.STOPPED) {
            configure(mParam);
        }
        mMediaCodec.start();
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            mInputBuffers = mMediaCodec.getInputBuffers();
            mOutputBuffers = mMediaCodec.getOutputBuffers();
        }
        mState = State.RUNNING;
    }

    public void encode(byte[] data) {
        if (mState != State.RUNNING) {
            return;
        }
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                inputBuffer = mMediaCodec.getInputBuffer(inputBufferIndex);
            } else {
                inputBuffer = mInputBuffers[inputBufferIndex];
            }
            inputBuffer.put(data);
            mMediaCodec.queueInputBuffer(inputBufferIndex, 0, data.length, 0, 0);
        }
        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, 0);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                outputBuffer = mMediaCodec.getOutputBuffer(outputBufferIndex);
            } else {
                outputBuffer = mOutputBuffers[outputBufferIndex];
            }
            byte[] bytes = new byte[mOutputBufferInfo.size];
            outputBuffer.get(bytes);
            mCallback.onOutputAvailable(bytes);
            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(mOutputBufferInfo, 0);
        }
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            MediaFormat bufferFormat = mMediaCodec.getOutputFormat();
            mCallback.onOutputFormatChanged(bufferFormat);
        }
    }

    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mMediaCodec.stop();
        mState = State.STOPPED;
    }

    public void release() {
        if (mState == State.UNINITIALIZED) {
            return;
        }
        mMediaCodec.release();
        mMediaCodec = null;
    }

    public State getState() {
        return mState;
    }

    public static class Param {
        /**
         * MIME类型
         *
         * @see MediaFormat#MIMETYPE_AUDIO_AAC
         */
        private String type;
        /**
         * 采样率
         *
         * 音频CD：44100
         * miniDV数码视频camcorder：32000
         * FM调频广播：24000, 22050
         * AM调幅广播：11025
         * 电话：8000
         */
        private int sampleRate;
        /**
         * 通道数
         *
         * 单声道：1
         * 立体声：2
         */
        private int channel;
        /**
         * 比特率
         *
         * 32000
         * 64000
         * 96000
         * 128000
         */
        private int bitRate;

        public Param(String type, int sampleRate, int channel, int bitRate) {
            this.type = type;
            this.sampleRate = sampleRate;
            this.channel = channel;
            this.bitRate = bitRate;
        }
    }

    public interface Callback {
        void onOutputFormatChanged(MediaFormat format);

        void onOutputAvailable(byte[] output);
    }
}
