package com.github.khangnt.mcp.annotation;

import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.khangnt.mcp.annotation.Encoders.AAC;
import static com.github.khangnt.mcp.annotation.Encoders.DNXHD;
import static com.github.khangnt.mcp.annotation.Encoders.FLAC;
import static com.github.khangnt.mcp.annotation.Encoders.FLV;
import static com.github.khangnt.mcp.annotation.Encoders.GIF;
import static com.github.khangnt.mcp.annotation.Encoders.H263;
import static com.github.khangnt.mcp.annotation.Encoders.LIBMP3LAME;
import static com.github.khangnt.mcp.annotation.Encoders.LIBOPUS;
import static com.github.khangnt.mcp.annotation.Encoders.LIBSHINE;
import static com.github.khangnt.mcp.annotation.Encoders.LIBVORBIS;
import static com.github.khangnt.mcp.annotation.Encoders.MJPEG;
import static com.github.khangnt.mcp.annotation.Encoders.MPEG4;
import static com.github.khangnt.mcp.annotation.Encoders.PNG;
import static com.github.khangnt.mcp.annotation.Encoders.SRT;
import static com.github.khangnt.mcp.annotation.Encoders.SUBRIP;
import static com.github.khangnt.mcp.annotation.Encoders.WEBVTT;

/**
 * <pre><code>
 * $ ffmpeg -encoders
 * Encoders:
 * V..... = Video
 * A..... = Audio
 * S..... = Subtitle
 * .F.... = Frame-level multithreading
 * ..S... = Slice-level multithreading
 * ...X.. = Codec is experimental
 * ....B. = Supports draw_horiz_band
 * .....D = Supports direct rendering method 1
 * ------
 * V.S... dnxhd
 * V..... flv                   (codec flv1)
 * V..... gif
 * V..... h263
 * VFS... mjpeg
 * V.S... mpeg4
 * VF.... png
 * A..... aac
 * A..... flac
 * A..... libmp3lame            (codec mp3)
 * A..... libshine              (codec mp3)
 * A..... libopus               (codec opus)
 * A..... libvorbis             (codec vorbis)
 * S..... srt                   (codec subrip)
 * S..... subrip
 * S..... webvtt
 * </code></pre>
 */
@SuppressWarnings("SpellCheckingInspection")
@StringDef({DNXHD, FLV, GIF, H263, MJPEG, MPEG4, PNG, AAC, FLAC, LIBMP3LAME, LIBSHINE,
        LIBOPUS, LIBVORBIS, SRT, SUBRIP, WEBVTT})
@Retention(RetentionPolicy.SOURCE)
public @interface Encoders {
    String DNXHD = "dnxhd";
    String FLV = "flv";
    String GIF = "gif";
    String H263 = "h263";
    String MJPEG = "mjpeg";
    String MPEG4 = "mpeg4";
    String PNG = "png";
    String AAC = "aac";
    String FLAC = "flac";
    String LIBMP3LAME = "libmp3lame";
    String LIBSHINE = "libshine";
    String LIBOPUS = "libopus";
    String LIBVORBIS = "libvorbis";
    String SRT = "srt";
    String SUBRIP = "subrip";
    String WEBVTT = "webvtt";
}
