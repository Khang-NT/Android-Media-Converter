package com.github.khangnt.mcp.annotation;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.khangnt.mcp.annotation.ConvertType.TYPE_ENCODE_AUDIO;
import static com.github.khangnt.mcp.annotation.ConvertType.TYPE_ENCODE_VIDEO;


@IntDef({TYPE_ENCODE_AUDIO, TYPE_ENCODE_VIDEO})
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
}
