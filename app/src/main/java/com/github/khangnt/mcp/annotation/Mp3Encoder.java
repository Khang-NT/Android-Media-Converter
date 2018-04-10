package com.github.khangnt.mcp.annotation;

import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.khangnt.mcp.annotation.Encoders.LIBMP3LAME;
import static com.github.khangnt.mcp.annotation.Encoders.LIBSHINE;


/**
 * Created by Khang NT on 4/10/18.
 * Email: khang.neon.1997@gmail.com
 */
@SuppressWarnings("SpellCheckingInspection")
@StringDef({LIBMP3LAME, LIBSHINE})
@Retention(RetentionPolicy.SOURCE)
public @interface Mp3Encoder {
}
