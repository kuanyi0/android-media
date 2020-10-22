package com.yikuan.androidmedia.record;

import android.media.projection.MediaProjection;

/**
 * @author yikuan
 * @date 2020/10/22
 */
public class ProjectionParam {
    MediaProjection projection;
    int width;
    int height;
    int dpi;

    public ProjectionParam(MediaProjection projection, int dpi, int width, int height) {
        this.projection = projection;
        this.width = width;
        this.height = height;
        this.dpi = dpi;
    }
}
