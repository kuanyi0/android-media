package com.yikuan.androidmedia.record;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.yikuan.androidcommon.util.ScreenUtils;
import com.yikuan.androidmedia.BuildConfig;
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
    private int mAudioCountOffset;
    private long mTotalAudioRecordCount;
    private long mAudioMiniPtsDuration;
    private long mStartTime;
    private long mPauseTime;
    private long mPausePts;
    private long mPauseDuration;
    private long mIdleDuration;
    private boolean mComputeStartIdleDuration;
    private boolean mComputeResumeIdleDuration;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void configure(Param param) {
        AudioRecorder.Param audioRecordParam = new AudioRecorder.Param(param.audioSource,
                param.sampleRateInHz, param.channelConfig, param.audioFormat);
        AudioEncodeParam audioEncodeParam = new AudioEncodeParam.Builder()
                .setSampleRate(param.sampleRateInHz)
                .setChannel(audioRecordParam.getChannel())
                .setBitRate(param.audioBitRate)
                .setMaxInputSize(audioRecordParam.getMiniBufferSize())
                .setAacProfile(param.aacProfile)
                .build();
        AudioParam audioParam = new AudioParam(audioRecordParam, audioEncodeParam);

        ProjectionParam projectionParam = new ProjectionParam(param.projection,
                ScreenUtils.getScreenDpi(), ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        VideoEncodeParam videoEncodeParam = new VideoEncodeParam.Builder()
                .setWidth(param.width)
                .setHeight(param.height)
                .setBitrate(param.videoBitRate)
                .setColorFormat(param.colorFormat)
                .setFrameRate(param.frameRate)
                .setIFrameInterval(param.iFrameInterval)
                .build();
        VideoParam videoParam = new VideoParam(projectionParam, videoEncodeParam);

        MediaMuxerHelper.Param muxerParam = new MediaMuxerHelper.Param(param.path, param.format);
        configure(audioParam, videoParam, muxerParam);
    }

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
                mTotalAudioRecordCount++;
                if (mAudioCountOffset < 0) {
                    Log.e(TAG, "[audio]onDataAvailable: pts early " + mAudioCountOffset + ", catch up!");
                    mTotalAudioRecordCount -= mAudioCountOffset;
                    mAudioCountOffset = 0;
                }
                if (mAudioCountOffset > 0) {
                    Log.e(TAG, "[audio]onDataAvailable: pts late " + mAudioCountOffset + ", discard!");
                    mTotalAudioRecordCount--;
                    mAudioCountOffset--;
                    return;
                }
                mAudioEncoder.write(data, getAudioPts());
            }

            @Override
            public void onDataError(int error) {
                Log.d(TAG, "[audio]onDataError: ");
            }
        });
        mAudioRecorder.configure(audioParam.recordParam);
        mAudioMiniPtsDuration = mAudioRecorder.getMiniPtsDuration();
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
        if (mCountDownLatch != null) {
            try {
                mCountDownLatch.await();
                mCountDownLatch = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        long timePts = getTimePts();
        if (mState == State.PAUSED && timePts > mPausePts / 1000) {
            Log.e(TAG, (trackIndex == mAudioTrackIndex ? "[audio]" : "[video]") + "writeIntoMuxer: invalid, " +
                    "because already paused, pausePts = " + mPausePts / 1000_000_000f + "s, timePts = " + timePts / 1000_000f + "s");
            return;
        }
        if (trackIndex == mAudioTrackIndex) {
            long audioPts = bufferInfo.presentationTimeUs;
            mAudioCountOffset = (int) ((audioPts - timePts) / mAudioMiniPtsDuration);
        }
        if (trackIndex == mVideoTrackIndex) {
            if (mComputeStartIdleDuration || mComputeResumeIdleDuration) {
                computeIdleDuration();
                timePts = getTimePts();
            }
            bufferInfo.presentationTimeUs = timePts;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, (trackIndex == mAudioTrackIndex ? "[audio]" : "[video]") + "writeIntoMuxer: size = " +
                    bufferInfo.size + ", offset = " + bufferInfo.offset + ", flags = " + bufferInfo.flags +
                    ", pts = [" + timePts / 1000_000f + "s / " + bufferInfo.presentationTimeUs / 1000_000f +
                    "s, delta = " + (bufferInfo.presentationTimeUs - timePts) / 1000_000f + "s]");
        }
        mMediaMuxerHelper.write(trackIndex, byteBuffer, bufferInfo);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private long getTimePts() {
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

    public static class Param {
        // See AudioRecorder.Param and AudioEncodeParam
        private int audioSource = MediaRecorder.AudioSource.MIC;
        private int sampleRateInHz = 44100;
        private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
        private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        private int audioBitRate = 64000;
        private int aacProfile = MediaCodecInfo.CodecProfileLevel.AACObjectLC;

        // See ProjectionParam and VideoEncodeParam
        private MediaProjection projection;
        private int width = 1080;
        private int height = 1920;
        private int videoBitRate = 8 * 1024 * 1024;
        @SuppressLint("InlinedApi")
        private int colorFormat = MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface;
        private int frameRate = 30;
        private int iFrameInterval = 1;

        // See MediaMuxerHelper.Param
        private String path;
        @SuppressLint("InlinedApi")
        private int format = MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4;

        public Param(MediaProjection projection, String path) {
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
}
