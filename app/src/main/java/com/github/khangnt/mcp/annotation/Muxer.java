package com.github.khangnt.mcp.annotation;

import android.support.annotation.StringDef;

import static com.github.khangnt.mcp.annotation.Muxer.DNXHD;
import static com.github.khangnt.mcp.annotation.Muxer.FLAC;
import static com.github.khangnt.mcp.annotation.Muxer.FLV;
import static com.github.khangnt.mcp.annotation.Muxer.GIF;
import static com.github.khangnt.mcp.annotation.Muxer.IMAGE2;
import static com.github.khangnt.mcp.annotation.Muxer.IMAGE2PIPE;
import static com.github.khangnt.mcp.annotation.Muxer.IPOD;
import static com.github.khangnt.mcp.annotation.Muxer.MATROSKA;
import static com.github.khangnt.mcp.annotation.Muxer.MJPEG;
import static com.github.khangnt.mcp.annotation.Muxer.MOV;
import static com.github.khangnt.mcp.annotation.Muxer.MP3;
import static com.github.khangnt.mcp.annotation.Muxer.MP4;
import static com.github.khangnt.mcp.annotation.Muxer.OGG;
import static com.github.khangnt.mcp.annotation.Muxer.OPUS;
import static com.github.khangnt.mcp.annotation.Muxer.SRT;
import static com.github.khangnt.mcp.annotation.Muxer.WAV;
import static com.github.khangnt.mcp.annotation.Muxer.WEBM;
import static com.github.khangnt.mcp.annotation.Muxer.WEBVTT;

/**
 * <pre><code>
 * $ ffmpeg -formats
 * File formats:
 * D. = Demuxing supported
 * .E = Muxing supported
 * --
 * D  aac
 * D  avfoundation
 * D  avi
 * DE dnxhd
 * DE flac
 * DE flv
 * DE gif
 * D  h261
 * D  h263
 * D  h264
 * DE image2
 * DE image2pipe
 * E ipod
 * D  lavfi
 * E matroska
 * D  matroska,webm
 * DE mjpeg
 * E mov
 * D  mov,mp4,m4a,3gp,3g2,mj2
 * DE mp3
 * E mp4
 * DE ogg
 * E opus
 * DE srt
 * DE wav
 * E webm
 * DE webvt
 * </code></pre>
 */

@SuppressWarnings("SpellCheckingInspection")
@StringDef({DNXHD, FLAC, FLV, GIF, IMAGE2, IMAGE2PIPE, IPOD, MATROSKA, MJPEG, MOV,
        MP3, MP4, OGG, OPUS, SRT, WAV, WEBM, WEBVTT})
public @interface Muxer {
    String DNXHD = "dnxhd";
    String FLAC = "flac";
    String FLV = "flv";
    String GIF = "gif";
    String IMAGE2 = "image2";
    String IMAGE2PIPE = "image2pipe";
    String IPOD = "ipod";
    String MATROSKA = "matroska";
    String MJPEG = "mjpeg";
    String MOV = "mov";
    String MP3 = "mp3";
    String MP4 = "mp4";
    String OGG = "ogg";
    String OPUS = "opus";
    String SRT = "srt";
    String WAV = "wav";
    String WEBM = "webm";
    String WEBVTT = "webvtt";
}
