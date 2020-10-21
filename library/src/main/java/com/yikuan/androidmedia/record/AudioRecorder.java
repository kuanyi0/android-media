package com.yikuan.androidmedia.record;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yikuan
 * @date 2020/09/17
 */
public class AudioRecorder extends Worker1<AudioRecorder.Param> {
    private AudioRecord mAudioRecord;
    private byte[] mAudioData;
    private ExecutorService mExecutorService;
    private Runnable mRecordRunnable;
    private Callback mCallback;

    @Override
    public void configure(Param param) {
        if (mState != State.UNINITIALIZED) {
            return;
        }
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(param.sampleRateInHz, param.channelConfig, param.audioFormat);
        mAudioRecord = new AudioRecord(param.audioSource, param.sampleRateInHz, param.channelConfig, param.audioFormat, bufferSizeInBytes);
        mAudioData = new byte[bufferSizeInBytes];
        mState = State.CONFIGURED;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void start() {
        if (mState != State.CONFIGURED) {
            return;
        }
        mAudioRecord.startRecording();
        mState = State.RUNNING;
        if (mExecutorService == null) {
            mExecutorService = Executors.newSingleThreadExecutor();
        }
        if (mRecordRunnable == null) {
            mRecordRunnable = new RecordRunnable();
        }
        mExecutorService.execute(mRecordRunnable);
    }

    @Override
    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mAudioRecord.stop();
        mState = State.STOPPED;
    }

    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mAudioRecord.release();
        mAudioRecord = null;
        mState = State.RELEASED;
    }

    private class RecordRunnable implements Runnable {
        @Override
        public void run() {
            while (mState == State.RUNNING && mCallback != null) {
                int read = mAudioRecord.read(mAudioData, 0, mAudioData.length);
                if (read >= 0) {
                    mCallback.onDataAvailable(mAudioData);
                } else {
                    mCallback.onDataError(read);
                }
            }
        }
    }

    public static class Param {
        /**
         * 音频源
         *
         * 麦克风：{@link android.media.MediaRecorder.AudioSource#MIC}
         */
        private int audioSource;
        /**
         * 采样率
         *
         * 音频CD：44100
         * miniDV数码视频camcorder：32000
         * FM调频广播：24000, 22050
         * AM调幅广播：11025
         * 电话：8000
         */
        private int sampleRateInHz;
        /**
         * 声道设置
         *
         * 单声道：{@link AudioFormat#CHANNEL_IN_MONO}
         * 立体声：{@link AudioFormat#CHANNEL_IN_STEREO}
         */
        private int channelConfig;
        /**
         * 编码制式
         *
         * 主流：{@link AudioFormat#ENCODING_PCM_16BIT}
         * 低质量：{@link AudioFormat#ENCODING_PCM_8BIT}
         */
        private int audioFormat;

        public Param(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
            this.audioSource = audioSource;
            this.sampleRateInHz = sampleRateInHz;
            this.channelConfig = channelConfig;
            this.audioFormat = audioFormat;
        }
    }

    public interface Callback {
        void onDataAvailable(byte[] data);

        void onDataError(int error);
    }
}
