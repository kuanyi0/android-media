package com.yikuan.androidmedia.mux;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.BuildConfig;
import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker1;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * @author yikuan
 * @date 2020/10/21
 */
public class MediaMuxerHelper extends Worker1<MediaMuxerHelper.Param> {
    private static final String TAG = "MediaMuxerHelper";
    private MediaMuxer mMediaMuxer;
    private Param mParam;
    private int mAudioTrack;
    private int mVideoTrack;
    private long mStartTime;
    private long mPauseTime;
    private long mAudioSampleCount;
    private long mLastAudioPts;
    private CountDownLatch mLatch;
    private Callback mCallback;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void configure(Param param) {
        checkCurrentStateInStates(State.UNINITIALIZED);
        try {
            mMediaMuxer = new MediaMuxer(param.path, param.format);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        mParam = param;
        mAudioTrack = -1;
        mVideoTrack = -1;
        mLatch = new CountDownLatch(1);
        mState = State.CONFIGURED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addAudioTrackAndStart(MediaFormat mediaFormat) {
        addAudioTrack(mediaFormat);
        start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addVideoTrackAndStart(MediaFormat mediaFormat) {
        addVideoTrack(mediaFormat);
        start();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addAudioTrack(MediaFormat mediaFormat) {
        if (mAudioTrack > 0) {
            return;
        }
        checkCurrentStateInStates(State.CONFIGURED);
        mAudioTrack = mMediaMuxer.addTrack(mediaFormat);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void addVideoTrack(MediaFormat mediaFormat) {
        if (mVideoTrack > 0) {
            return;
        }
        checkCurrentStateInStates(State.CONFIGURED);
        mVideoTrack = mMediaMuxer.addTrack(mediaFormat);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public synchronized void start() {
        if (isRunning()) {
            return;
        }
        checkCurrentStateInStates(State.CONFIGURED);
        if (mAudioTrack < 0 || mVideoTrack < 0) {
            return;
        }
        mMediaMuxer.start();
        mStartTime = SystemClock.elapsedRealtimeNanos();
        mState = State.RUNNING;
        mLatch.countDown();
        if (mCallback != null) {
            mCallback.onStarted();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void resume() {
        if (isRunning()) {
            return;
        }
        checkCurrentStateInStates(State.PAUSED);
        mStartTime += SystemClock.elapsedRealtimeNanos() - mPauseTime;
        mState = State.RUNNING;
        if (mCallback != null) {
            mCallback.onResumed();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void writeAudio(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        waitIfNotStart();
        if (!isRunning()) {
            Log.e(TAG, "writeAudio: invalid!");
            return;
        }
        long pts = bufferInfo.presentationTimeUs;
        if (pts <= mLastAudioPts) {
            return;
        }
        mLastAudioPts = pts;
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "writeAudio: " + bufferInfo.offset + ", " + bufferInfo.size + ", "
                    + bufferInfo.flags + ", " + bufferInfo.presentationTimeUs);
        }
        mMediaMuxer.writeSampleData(mAudioTrack, buffer, bufferInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void writeVideo(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        waitIfNotStart();
        if (!isRunning()) {
            Log.e(TAG, "writeVideo: invalid!");
            return;
        }
        bufferInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
        bufferInfo.presentationTimeUs = getPts();
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "writeVideo: " + bufferInfo.offset + ", " + bufferInfo.size + ", "
                    + bufferInfo.flags + ", " + bufferInfo.presentationTimeUs);
        }
        mMediaMuxer.writeSampleData(mVideoTrack, buffer, bufferInfo);
    }

    private void waitIfNotStart() {
        if (mLatch == null) {
            return;
        }
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mLatch = null;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void pause() {
        if (mState == State.PAUSED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING);
        mPauseTime = SystemClock.elapsedRealtimeNanos();
        mState = State.PAUSED;
        if (mCallback != null) {
            mCallback.onPaused();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public synchronized void stop() {
        if (mState == State.STOPPED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING, State.PAUSED);
        mMediaMuxer.stop();
        mState = State.STOPPED;
        if (mCallback != null) {
            mCallback.onStopped();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaMuxer.release();
        mMediaMuxer = null;
        mState = State.RELEASED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public long getPts() {
        if (isRunning()) {
            return (SystemClock.elapsedRealtimeNanos() - mStartTime) / 1000;
        } else if (mState == State.PAUSED) {
            return (mPauseTime - mStartTime) / 1000;
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public long getAudioPts() {
        if (!isRunning()) {
            return 0;
        }
        mAudioSampleCount++;
        long audioPts = mParam.audioPtsPerSample * mAudioSampleCount;
        long recordPts = getPts();
        long delta = audioPts - recordPts;
        long count = Math.abs(delta / mParam.audioPtsPerSample);
        if (delta < -mParam.audioPtsPerSample) {
            Log.e(TAG, "getAudioPts: late " + count + ", catch up!");
            mAudioSampleCount += count;
        } else if (delta > mParam.audioPtsPerSample) {
            Log.e(TAG, "getAudioPts: early " + count + ", discard!");
            mAudioSampleCount--;
        }
        return mParam.audioPtsPerSample * mAudioSampleCount;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public static class Param {
        /**
         * 每个音频采样的pts
         */
        private long audioPtsPerSample;
        /**
         * 输出路径
         */
        private String path;
        /**
         * 输出媒体格式
         *
         * @see MediaMuxer.OutputFormat
         */
        private int format;

        public Param(long audioPtsPerSample, String path, int format) {
            this.audioPtsPerSample = audioPtsPerSample;
            this.path = path;
            this.format = format;
        }
    }

    public interface Callback {
        void onStarted();

        void onResumed();

        void onPaused();

        void onStopped();
    }
}
