package com.github.khangnt.mcp.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.khangnt.mcp.annotation.ConvertType.TYPE_CUSTOM;
import static com.github.khangnt.mcp.annotation.ConvertType.TYPE_ENCODE_AUDIO;
import static com.github.khangnt.mcp.annotation.ConvertType.TYPE_ENCODE_VIDEO;
import static com.github.khangnt.mcp.annotation.ConvertType.TYPE_MIX_MEDIA;


@IntDef({TYPE_ENCODE_AUDIO, TYPE_ENCODE_VIDEO, TYPE_MIX_MEDIA, TYPE_CUSTOM})
@Retention(RetentionPolicy.SOURCE)
public @interface ConvertType {
    /**
     * Convert a/an video/audio file to audio only format
     **/
    int TYPE_ENCODE_AUDIO = 0;
    /**
     * Convert a video file to other video format
     **/
    int TYPE_ENCODE_VIDEO = 1;
    /**
     * Concat, trim, cut audio/video file(s)
     **/
    int TYPE_MIX_MEDIA = 2;
    /**
     * Run a command provided by user
     **/
    int TYPE_CUSTOM = 3;
}
