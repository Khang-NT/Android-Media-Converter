package com.github.khangnt.mcp.annotation;

import android.support.annotation.IntDef;

import static com.github.khangnt.mcp.annotation.JobStatus.COMPLETED;
import static com.github.khangnt.mcp.annotation.JobStatus.FAILED;
import static com.github.khangnt.mcp.annotation.JobStatus.PENDING;
import static com.github.khangnt.mcp.annotation.JobStatus.RUNNING;

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

@IntDef({RUNNING, PENDING, COMPLETED, FAILED})
public @interface JobStatus {
    int RUNNING = 0;
    int PENDING = 1;
    int COMPLETED = 2;
    int FAILED = 3;
}
