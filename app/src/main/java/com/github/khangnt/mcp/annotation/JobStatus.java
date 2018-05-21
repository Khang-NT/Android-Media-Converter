package com.github.khangnt.mcp.annotation;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.github.khangnt.mcp.annotation.JobStatus.COMPLETED;
import static com.github.khangnt.mcp.annotation.JobStatus.FAILED;
import static com.github.khangnt.mcp.annotation.JobStatus.PENDING;
import static com.github.khangnt.mcp.annotation.JobStatus.PREPARING;
import static com.github.khangnt.mcp.annotation.JobStatus.READY;
import static com.github.khangnt.mcp.annotation.JobStatus.RUNNING;

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

@IntDef({PENDING, PREPARING, READY, RUNNING, COMPLETED, FAILED})
@Retention(RetentionPolicy.SOURCE)
public @interface JobStatus {
    // database version 1
//    int RUNNING = 0;
//    int PENDING = 1;
//    int COMPLETED = 2;
//    int FAILED = 3;
//    int PREPARING = 4;
//    int READY = 5;

    // database version 2
    int RUNNING = 5;

    int PREPARING = 4;
    int READY = 3;

    int PENDING = 2;

    int COMPLETED = 1;
    int FAILED = 0;
}
