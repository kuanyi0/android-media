package com.yikuan.androidmedia.record;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.yikuan.androidcommon.util.ScreenUtils;
import com.yikuan.androidmedia.base.State;
import com.yikuan.androidmedia.base.Worker1;
import com.yikuan.androidmedia.codec.SyncCodec;
import com.yikuan.androidmedia.encode.AudioEncodeParam;
import com.yikuan.androidmedia.encode.AudioEncoder;
import com.yikuan.androidmedia.encode.VideoEncodeParam;
import com.yikuan.androidmedia.encode.VideoEncoder;
import com.yikuan.androidmedia.mux.MediaMuxerHelper2;

import java.nio.ByteBuffer;

/**
 * @author yikuan
 * @date 2020/10/22
 */
public class ScreenRecorder2 extends Worker1<ScreenRecordParam> {
    private static final String TAG = "ScreenRecorder2";
    private AudioRecorder mAudioRecorder = new AudioRecorder();
    private VideoRecorder mVideoRecorder = new VideoRecorder();
    private AudioEncoder mAudioEncoder = new AudioEncoder();
    private VideoEncoder mVideoEncoder = new VideoEncoder();
    private MediaMuxerHelper2 mMediaMuxerHelper = new MediaMuxerHelper2();
    private Callback mCallback;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void configure(ScreenRecordParam param) {
        AudioRecorder.Param audioRecordParam = new AudioRecorder.Param(param.audioSource,
                param.sampleRateInHz, param.channelConfig, param.audioFormat);
        AudioEncodeParam audioEncodeParam = new AudioEncodeParam.Builder()
                .setSampleRate(param.sampleRateInHz)
                .setChannel(audioRecordParam.getChannel())
                .setBitRate(param.audioBitRate)
                .setMaxInputSize(audioRecordParam.getBufferSizeInBytes())
                .setAacProfile(param.aacProfile)
                .build();
        ScreenRecordParam.AudioParam audioParam = new ScreenRecordParam.AudioParam(audioRecordParam, audioEncodeParam);

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
        ScreenRecordParam.VideoParam videoParam = new ScreenRecordParam.VideoParam(projectionParam, videoEncodeParam);

        MediaMuxerHelper2.Param muxerParam = new MediaMuxerHelper2.Param(audioRecordParam.getPtsPerSample(), param.path, param.format);
        configure(audioParam, videoParam, muxerParam);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void configure(ScreenRecordParam.AudioParam audioParam, ScreenRecordParam.VideoParam videoParam, MediaMuxerHelper2.Param muxerParam) {
        checkCurrentStateInStates(State.UNINITIALIZED);
        configureAudio(audioParam);
        configureVideo(videoParam);
        configureMuxer(muxerParam);
        mState = State.CONFIGURED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureAudio(ScreenRecordParam.AudioParam audioParam) {
        mAudioEncoder.setCallback(new SyncCodec.Callback() {
            @Override
            public void onOutputFormatChanged(MediaFormat format) {
                Log.d(TAG, "[audio]onOutputFormatChanged: ");
                mMediaMuxerHelper.addAudioTrackAndStart(format);
            }

            @Override
            public void onOutputBufferAvailable(int index, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
                mMediaMuxerHelper.writeAudio(byteBuffer, bufferInfo);
            }
        });
        mAudioEncoder.configure(audioParam.encodeParam);
        mAudioRecorder.setCallback(new AudioRecorder.Callback() {
            @Override
            public void onDataAvailable(byte[] data) {
                mAudioEncoder.write(data, mMediaMuxerHelper.getAudioPts());
            }

            @Override
            public void onDataError(int error) {
                Log.d(TAG, "[audio]onDataError: ");
            }
        });
        mAudioRecorder.configure(audioParam.recordParam);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void configureVideo(ScreenRecordParam.VideoParam videoParam) {
        mVideoEncoder.setCallback(new SyncCodec.Callback() {
            @Override
            public void onOutputFormatChanged(MediaFormat format) {
                Log.d(TAG, "[video]onOutputFormatChanged: ");
                mMediaMuxerHelper.addVideoTrackAndStart(format);
            }

            @Override
            public void onOutputBufferAvailable(int index, ByteBuffer byteBuffer, MediaCodec.BufferInfo bufferInfo) {
                mMediaMuxerHelper.writeVideo(byteBuffer, bufferInfo);
            }
        });
        mVideoEncoder.configure(videoParam.encodeParam);
        mVideoRecorder.configure(videoParam.projectionParam, mVideoEncoder.getInputSurface());
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void configureMuxer(MediaMuxerHelper2.Param muxerParam) {
        mMediaMuxerHelper.configure(muxerParam);
        mMediaMuxerHelper.setCallback(new MediaMuxerHelper2.Callback() {
            @Override
            public void onStarted() {
                if (mCallback != null) {
                    mCallback.onStarted();
                }
            }

            @Override
            public void onResumed() {
                if (mCallback != null) {
                    mCallback.onResumed();
                }
            }

            @Override
            public void onPaused() {
                if (mCallback != null) {
                    mCallback.onPaused();
                }
            }

            @Override
            public void onStopped() {
                if (mCallback != null) {
                    mCallback.onStopped();
                }
            }
        });
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
        mVideoEncoder.startAndRead();
        mState = State.RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void resume() {
        if (mState == State.RUNNING) {
            return;
        }
        checkCurrentStateInStates(State.PAUSED);
        mAudioRecorder.start();
        mVideoRecorder.start();
        mMediaMuxerHelper.resume();
        mState = State.RUNNING;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void pause() {
        if (mState == State.PAUSED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING);
        mAudioRecorder.stop();
        mVideoRecorder.stop();
        mMediaMuxerHelper.pause();
        mState = State.PAUSED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void stop() {
        if (mState == State.STOPPED) {
            return;
        }
        checkCurrentStateInStates(State.RUNNING, State.PAUSED);
        mAudioRecorder.stop();
        mVideoRecorder.stop();
        mAudioEncoder.stop();
        mVideoEncoder.stop();
        mMediaMuxerHelper.stop();
        mState = State.STOPPED;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void release() {
        if (mState == State.UNINITIALIZED || mState == State.RELEASED) {
            return;
        }
        mAudioRecorder.release();
        mVideoRecorder.release();
        mAudioEncoder.release();
        mVideoEncoder.release();
        mMediaMuxerHelper.release();
        mState = State.RELEASED;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    public long getPts() {
        return isRunning() ? mMediaMuxerHelper.getPts() : 0;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public interface Callback {
        void onStarted();

        void onResumed();

        void onPaused();

        void onStopped();
    }
}
