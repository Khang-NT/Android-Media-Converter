package com.github.khangnt.mcp.annotation;

import android.support.annotation.StringDef;

import static com.github.khangnt.mcp.annotation.MediaFormat.AAC;
import static com.github.khangnt.mcp.annotation.MediaFormat.FLAC;
import static com.github.khangnt.mcp.annotation.MediaFormat.FLV;
import static com.github.khangnt.mcp.annotation.MediaFormat.M4A;
import static com.github.khangnt.mcp.annotation.MediaFormat.MP3;
import static com.github.khangnt.mcp.annotation.MediaFormat.MP4;
import static com.github.khangnt.mcp.annotation.MediaFormat.OGG;
import static com.github.khangnt.mcp.annotation.MediaFormat.OPUS;
import static com.github.khangnt.mcp.annotation.MediaFormat._3GP;

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

@StringDef({MP3, AAC, FLAC, OGG, OPUS, M4A, MP4, FLV, _3GP})
public @interface MediaFormat {
    String MP3 = "mp3";
    String AAC = "aac";
    String FLAC = "flac";
    String OGG = "ogg";
    String OPUS = "opus";
    String M4A = "m4a";

    String MP4 = "mp4";
    String FLV = "flv";
    String _3GP = "3gp";
}
