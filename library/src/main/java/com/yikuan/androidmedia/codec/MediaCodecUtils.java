package com.yikuan.androidmedia.codec;

import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yikuan
 * @date 2020/09/21
 */
public class MediaCodecUtils {

    public static List<MediaCodecInfo> getEncoders() {
        List<MediaCodecInfo> encoders = new ArrayList<>();
        MediaCodecList list;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            for (MediaCodecInfo info : list.getCodecInfos()) {
                if (info.isEncoder()) {
                    encoders.add(info);
                }
            }
        } else {
            int count = MediaCodecList.getCodecCount();
            for (int i = 0; i < count; i++) {
                MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
                if (info.isEncoder()) {
                    encoders.add(info);
                }
            }
        }
        return encoders;
    }

    public static List<MediaCodecInfo> getDecoders() {
        List<MediaCodecInfo> decoders = new ArrayList<>();
        MediaCodecList list;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            list = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
            for (MediaCodecInfo info : list.getCodecInfos()) {
                if (!info.isEncoder()) {
                    decoders.add(info);
                }
            }
        } else {
            int count = MediaCodecList.getCodecCount();
            for (int i = 0; i < count; i++) {
                MediaCodecInfo info = MediaCodecList.getCodecInfoAt(i);
                if (!info.isEncoder()) {
                    decoders.add(info);
                }
            }
        }
        return decoders;
    }

    public static List<MediaFormat> getMediaFormat(File file) {
        List<MediaFormat> list = new ArrayList<>();
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int trackCount = extractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            list.add(format);
        }
        extractor.release();
        return list;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String findEncoderByFormat(MediaFormat format) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        for (MediaCodecInfo info : getEncoders()) {
            MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mime);
            if (caps != null && caps.isFormatSupported(format)) {
                return info.getName();
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static String findDecoderByFormat(MediaFormat format) {
        String mime = format.getString(MediaFormat.KEY_MIME);
        for (MediaCodecInfo info : getDecoders()) {
            MediaCodecInfo.CodecCapabilities caps = info.getCapabilitiesForType(mime);
            if (caps != null && caps.isFormatSupported(format)) {
                return info.getName();
            }
        }
        return null;
    }

    public static String findEncoderByType(String mimeType) {
        for (MediaCodecInfo info : getEncoders()) {
            for (String t : info.getSupportedTypes()) {
                if (t.equalsIgnoreCase(mimeType)) {
                    return info.getName();
                }
            }
        }
        return null;
    }

    public static String findDecoderByType(String mimeType) {
        for (MediaCodecInfo info : getDecoders()) {
            for (String t : info.getSupportedTypes()) {
                if (t.equalsIgnoreCase(mimeType)) {
                    return info.getName();
                }
            }
        }
        return null;
    }
}
