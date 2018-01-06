package com.github.khangnt.mcp.exception

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class ChecksumException(
        expected: String,
        actual: String
) : Exception("Check sum failed, expected '$expected' but receive '$actual'") 