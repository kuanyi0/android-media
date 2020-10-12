package com.yikuan.androidmedia.record;

import android.media.AudioFormat;
import android.media.AudioRecord;

import com.yikuan.androidmedia.base.State;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yikuan
 * @date 2020/09/17
 */
public class AudioRecorder {
    private AudioRecord mAudioRecord;
    private State mState = State.UNINITIALIZED;
    private byte[] mAudioData;
    private ExecutorService mExecutorService;
    private Runnable mRecordRunnable;
    private Callback mCallback;

    public void configure(AudioParams audioParams) {
        if (mState != State.UNINITIALIZED) {
            return;
        }
        int bufferSizeInBytes = AudioRecord.getMinBufferSize(audioParams.sampleRateInHz, audioParams.channelConfig, audioParams.audioFormat);
        mAudioRecord = new AudioRecord(audioParams.audioSource, audioParams.sampleRateInHz, audioParams.channelConfig, audioParams.audioFormat, bufferSizeInBytes);
        mAudioData = new byte[bufferSizeInBytes];
        mState = State.CONFIGURED;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

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

    public void stop() {
        if (mState != State.RUNNING) {
            return;
        }
        mAudioRecord.stop();
        mState = State.STOPPED;
    }

    public void reset() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mAudioRecord.release();
        mAudioRecord = null;
        mState = State.UNINITIALIZED;
    }

    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mAudioRecord.release();
        mAudioRecord = null;
        mState = State.RELEASED;
    }

    public State getState() {
        return mState;
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

    public static class AudioParams {
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

        public AudioParams(int audioSource, int sampleRateInHz, int channelConfig, int audioFormat) {
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
