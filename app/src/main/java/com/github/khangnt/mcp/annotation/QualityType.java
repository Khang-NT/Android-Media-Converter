package com.github.khangnt.mcp.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.khangnt.mcp.annotation.QualityType.CBR;
import static com.github.khangnt.mcp.annotation.QualityType.VBR;

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

@IntDef({VBR, CBR})
@Retention(RetentionPolicy.SOURCE)
public @interface QualityType {
    int VBR = 0; // Variable bitrate
    int CBR = 1; // Constant bitrate
}
