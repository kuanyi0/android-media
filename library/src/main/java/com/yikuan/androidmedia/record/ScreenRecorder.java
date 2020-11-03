package com.yikuan.androidmedia.record;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker3;
import com.yikuan.androidmedia.codec.SyncCodec;
import com.yikuan.androidmedia.encode.AudioEncodeParam;
import com.yikuan.androidmedia.encode.AudioEncoder;
import com.yikuan.androidmedia.encode.VideoEncodeParam;
import com.yikuan.androidmedia.encode.VideoEncoder2;
import com.yikuan.androidmedia.mux.MediaMuxerHelper;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * @author yikuan
 * @date 2020/10/22
 */
public class ScreenRecorder extends Worker3<ScreenRecorder.AudioParam, ScreenRecorder.VideoParam, MediaMuxerHelper.Param> {
    private static final String TAG = "ScreenRecorder";
    private static final int TRACK_AUDIO = 1;
    private static final int TRACK_VIDEO = 2;
    private AudioRecorder mAudioRecorder = new AudioRecorder();
    private VideoRecorder mVideoRecorder = new VideoRecorder();
    private AudioEncoder mAudioEncoder = new AudioEncoder();
    private VideoEncoder2 mVideoEncoder = new VideoEncoder2();
    private MediaMuxerHelper mMediaMuxerHelper = new MediaMuxerHelper();
    private CountDownLatch mCountDownLatch;
    private int mAudioTrackIndex;
    private int mVideoTrackIndex;
    private long mStartTime;
    private long mPauseTime;
    private long mResumeTime;
    private long mEarliestTime;
    private long mIdleTime;
    private long mTotalAudioRecordSize;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void configure(AudioParam audioParam, VideoParam videoParam, MediaMuxerHelper.Param muxerParam) {
        checkCurrentStateInStates(State.UNINITIALIZED);
        configureAudio(audioParam);
        configureVideo(videoParam);
        mMediaMuxerHelper.configure(muxerParam);
        mCountDownLatch = new CountDownLatch(1);
        mState = State.CONFIGURED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureAudio(AudioParam audioParam) {
        mAudioTrackIndex = -1;
        mTotalAudioRecordSize = 0;
        mAudioEncoder.setCallback(new SyncCodec.Callback() {
            @Override
            public void onOutputFormatChanged(MediaFormat format) {
                Log.d(TAG, "[audio]onOutputFormatChanged: ");
                addTrackAndStartMuxer(TRACK_AUDIO, format);
            }

            @Override
            public void onOutputBufferAvailable(int index, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
                writeIntoMuxer(mAudioTrackIndex, byteBuffer, bufferInfo);
            }
        });
        mAudioEncoder.configure(audioParam.encodeParam);
        mAudioRecorder.setCallback(new AudioRecorder.Callback() {
            @Override
            public void onDataAvailable(byte[] data) {
                mAudioEncoder.write(data, getAudioPts());
            }

            @Override
            public void onDataError(int error) {
                Log.d(TAG, "[audio]onDataError: ");
            }
        });
        mAudioRecorder.configure(audioParam.recordParam);
    }

    private long getAudioPts() {
        return mAudioRecorder.computePtsByCount(++mTotalAudioRecordSize);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureVideo(VideoParam videoParam) {
        mVideoTrackIndex = -1;
        mVideoEncoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                ByteBuffer outputBuffer = mVideoEncoder.read(index, info);
                if (outputBuffer != null) {
                    writeIntoMuxer(mVideoTrackIndex, outputBuffer, info);
                }
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {
                Log.d(TAG, "[video]onError: ");
            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                Log.d(TAG, "[video]onOutputFormatChanged: ");
                addTrackAndStartMuxer(TRACK_VIDEO, format);
            }
        });
        mVideoEncoder.configure(videoParam.encodeParam);
        mVideoRecorder.configure(videoParam.projectionParam, mVideoEncoder.getInputSurface());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private synchronized void addTrackAndStartMuxer(int trackType, MediaFormat mediaFormat) {
        int index = mMediaMuxerHelper.addTrack(mediaFormat);
        if (trackType == TRACK_AUDIO) {
            mAudioTrackIndex = index;
        } else if (trackType == TRACK_VIDEO) {
            mVideoTrackIndex = index;
        }
        if (mAudioTrackIndex >= 0 && mVideoTrackIndex >= 0) {
            Log.d(TAG, "addTrackAndStartMuxer: audioTrackIndex = " + mAudioTrackIndex + ", videoTrackIndex = " + mVideoTrackIndex);
            mMediaMuxerHelper.start();
            mCountDownLatch.countDown();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void writeIntoMuxer(int trackIndex, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!isRunning()) {
            Log.e(TAG, "writeIntoMuxer: invalid!");
            return;
        }
        long timePts = computeTimePts();
        if (trackIndex == mVideoTrackIndex) {
            bufferInfo.presentationTimeUs = timePts;
        }
        Log.d(TAG, (trackIndex == mAudioTrackIndex ? "[audio]" : "[video]") + "writeIntoMuxer: size = " +
                bufferInfo.size + ", offset = " + bufferInfo.offset + ", flags = " + bufferInfo.flags +
                ", pts = [" + timePts / 1000_000f + "s / " + bufferInfo.presentationTimeUs / 1000_000f +
                "s, delta = " + (bufferInfo.presentationTimeUs - timePts) / 1000_000f + "s]");
        mMediaMuxerHelper.write(trackIndex, byteBuffer, bufferInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private long computeTimePts() {
        long timePts = getTimePts();
        timePts = fixTimePts(timePts);
        return timePts;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private synchronized long fixTimePts(long timePts) {
        long oldTimePts = timePts;
        if (mEarliestTime != 0) {
            mStartTime += SystemClock.elapsedRealtimeNanos() - mEarliestTime;
            timePts = getTimePts();
            mEarliestTime = 0;
            Log.e(TAG, "fixTimePts: when start, " + oldTimePts / 1000_000f + "s -> " + timePts / 1000_000f + "s");
        }
        if (mResumeTime != 0) {
            mIdleTime += SystemClock.elapsedRealtimeNanos() - mResumeTime;
            timePts = getTimePts();
            mResumeTime = 0;
            Log.e(TAG, "fixTimePts: when resume, " + oldTimePts / 1000_000f + "s -> " + timePts / 1000_000f + "s");
        }
        return timePts;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private long getTimePts() {
        return (SystemClock.elapsedRealtimeNanos() - mStartTime - mIdleTime) / 1000;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void start() {
        if (mState == State.RUNNING) {
            return;
        }
        checkCurrentStateInStates(State.CONFIGURED);
        mAudioRecorder.start();
        mVideoRecorder.start();
        mAudioEncoder.start();
        mVideoEncoder.start();
        mState = State.RUNNING;
        mStartTime = SystemClock.elapsedRealtimeNanos();
        mEarliestTime = mStartTime;
        mIdleTime = 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void resume() {
        if (mState == State.RUNNING) {
            return;
        }
        checkCurrentStateInStates(State.PAUSED);
        mAudioRecorder.start();
        mVideoRecorder.start();
        mState = State.RUNNING;
        mResumeTime = SystemClock.elapsedRealtimeNanos();
        mIdleTime += (mResumeTime - mPauseTime);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void pause() {
        if (mState == State.PAUSED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING);
        mAudioRecorder.stop();
        mVideoRecorder.stop();
        mState = State.PAUSED;
        mPauseTime = SystemClock.elapsedRealtimeNanos();
        Log.d(TAG, "pause: " + (mPauseTime - mStartTime - mIdleTime) / 1000_000_000f + "s");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void stop() {
        if (mState == State.STOPPED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING, State.PAUSED);
        mMediaMuxerHelper.stop();
        mAudioEncoder.stop();
        mVideoEncoder.stop();
        mAudioRecorder.stop();
        mVideoRecorder.stop();
        mState = State.STOPPED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mMediaMuxerHelper.release();
        mAudioEncoder.release();
        mVideoEncoder.release();
        mAudioRecorder.release();
        mVideoRecorder.release();
        mState = State.RELEASED;
    }

    public static class AudioParam {
        private AudioRecorder.Param recordParam;
        private AudioEncodeParam encodeParam;

        public AudioParam(AudioRecorder.Param recordParam, AudioEncodeParam encodeParam) {
            this.recordParam = recordParam;
            this.encodeParam = encodeParam;
        }
    }

    public static class VideoParam {
        private ProjectionParam projectionParam;
        private VideoEncodeParam encodeParam;

        public VideoParam(ProjectionParam projectionParam, VideoEncodeParam encodeParam) {
            this.projectionParam = projectionParam;
            this.encodeParam = encodeParam;
        }
    }
}
