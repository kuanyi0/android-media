package com.yikuan.androidmedia.base;

import android.media.MediaFormat;

/**
 * @author yikuan
 * @date 2020/10/12
 */
public abstract class CodecParam {
    /**
     * MIME类型
     *
     * @see MediaFormat#MIMETYPE_AUDIO_AAC
     * @see MediaFormat#MIMETYPE_VIDEO_AVC
     */
    public String type;
}
