package com.yikuan.androidmedia.app;

import com.yikuan.androidcommon.util.PathUtils;

/**
 * @author yikuan
 * @date 2020/09/22
 */
public class Constant {
    public static final String DIR_ROOT = PathUtils.getExternalStoragePath() + "android-media";
    public static final String DIR_AUDIO_RECORD = DIR_ROOT + "/" + "audio-record";
    public static final String DIR_VIDEO_RECORD = DIR_ROOT + "/" + "video-record";
    public static final String DIR_AUDIO_ENCODE = DIR_ROOT + "/" + "audio-encode";
    public static final String DIR_VIDEO_ENCODE = DIR_ROOT + "/" + "video-encode";

    public static final String[] DIRS = new String[]{
            DIR_AUDIO_RECORD,
            DIR_VIDEO_RECORD,
            DIR_AUDIO_ENCODE,
            DIR_VIDEO_ENCODE
    };
}
