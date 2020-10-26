package com.yikuan.androidmedia.app.record;

import android.media.AudioFormat;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.yikuan.androidcommon.util.DateUtils;
import com.yikuan.androidcommon.util.ScreenUtils;
import com.yikuan.androidmedia.app.Constant;
import com.yikuan.androidmedia.app.base.MediaProjectionService;
import com.yikuan.androidmedia.encode.AudioEncodeParam;
import com.yikuan.androidmedia.encode.VideoEncodeParam;
import com.yikuan.androidmedia.mux.MediaMuxerHelper;
import com.yikuan.androidmedia.record.AudioRecorder;
import com.yikuan.androidmedia.record.ProjectionParam;
import com.yikuan.androidmedia.record.ScreenRecorder;

/**
 * @author yikuan
 * @date 2020/10/24
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class ScreenRecordService extends MediaProjectionService {
    private ScreenRecorder mScreenRecorder = new ScreenRecorder();

    @Override
    protected void start() {
        AudioRecorder.Param audioRecordParam = new AudioRecorder.Param(MediaRecorder.AudioSource.MIC,
                44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioEncodeParam audioEncodeParam = new AudioEncodeParam(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1, 96000);
        ScreenRecorder.AudioParam audioParam = new ScreenRecorder.AudioParam(audioRecordParam, audioEncodeParam);
        ProjectionParam projectionParam = new ProjectionParam(mMediaProjection, ScreenUtils.getScreenDpi(),
                ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight());
        VideoEncodeParam videoEncodeParam = new VideoEncodeParam(MediaFormat.MIMETYPE_VIDEO_AVC,
                ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), 8 * 1024 * 1024,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface, 30, 1);
        ScreenRecorder.VideoParam videoParam = new ScreenRecorder.VideoParam(projectionParam, videoEncodeParam);
        MediaMuxerHelper.Param muxerParam = new MediaMuxerHelper.Param(Constant.DIR_VIDEO_RECORD + "/" +
                DateUtils.formatTimeFileName() + ".mp4", MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        mScreenRecorder.configure(audioParam, videoParam, muxerParam);
        mScreenRecorder.start();
    }

    @Override
    protected void stop() {
        mScreenRecorder.stop();
    }
}
