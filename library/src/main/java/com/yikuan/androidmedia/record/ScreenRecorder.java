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
    private long mTotalAudioRecordCount;
    private long mStartTime;
    private long mPauseTime;
    private long mPausePts;
    private long mPauseDuration;
    private long mIdleDuration;
    private boolean mComputeStartIdleDuration;
    private boolean mComputeResumeIdleDuration;

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
                long oldCount = mTotalAudioRecordCount;
                mTotalAudioRecordCount++;
                long pts = getAudioPts();
                long timePts = getTimePts();
                long limit = mAudioRecorder.getMiniPtsDuration();
                if (pts - timePts < -limit) {
                    Log.e(TAG, "[audio]onDataAvailable: pts early!");
                    mTotalAudioRecordCount++;
                }
                if (pts - timePts > limit) {
                    Log.e(TAG, "[audio]onDataAvailable: pts late, discard!");
                    mTotalAudioRecordCount--;
                    return;
                }
                if (mTotalAudioRecordCount > oldCount) {
                    mAudioEncoder.write(data, getAudioPts());
                }
            }

            @Override
            public void onDataError(int error) {
                Log.d(TAG, "[audio]onDataError: ");
            }
        });
        mAudioRecorder.configure(audioParam.recordParam);
    }

    private long getAudioPts() {
        return mAudioRecorder.computePtsByCount(mTotalAudioRecordCount);
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
        long timePts = getTimePts();
        if (mState == State.PAUSED && timePts > mPausePts / 1000) {
            Log.e(TAG, (trackIndex == mAudioTrackIndex ? "[audio]" : "[video]") + "writeIntoMuxer: invalid, " +
                    "because already paused, pausePts = " + mPausePts / 1000_000_000f + "s, timePts = " + timePts / 1000_000f + "s");
            return;
        }
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
    private long getTimePts() {
        if (mComputeStartIdleDuration || mComputeResumeIdleDuration) {
            computeIdleDuration();
        }
        return (SystemClock.elapsedRealtimeNanos() - mStartTime - mIdleDuration) / 1000;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private synchronized void computeIdleDuration() {
        if (mComputeStartIdleDuration) {
            mIdleDuration = SystemClock.elapsedRealtimeNanos() - mStartTime;
            Log.d(TAG, "computeIdleDuration: start, " + mIdleDuration / 1000_000_000f + "s");
            mComputeStartIdleDuration = false;
        }
        if (mComputeResumeIdleDuration) {
            mIdleDuration = SystemClock.elapsedRealtimeNanos() - mStartTime - mPausePts;
            Log.d(TAG, "computeIdleDuration: resume, " + mIdleDuration / 1000_000_000f + "s");
            mComputeResumeIdleDuration = false;
        }
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
        mComputeStartIdleDuration = true;
        mTotalAudioRecordCount = -1;
        mPauseDuration = 0;
        mIdleDuration = 0;
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
        mComputeResumeIdleDuration = true;
        mPauseDuration += SystemClock.elapsedRealtimeNanos() - mPauseTime;
        Log.d(TAG, "resume: pause duration = " + mPauseDuration / 1000_000_000f + "s");
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
        mPausePts = mPauseTime - mStartTime - mPauseDuration;
        Log.d(TAG, "pause: pts = " + mPausePts / 1000_000_000f + "s");
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
