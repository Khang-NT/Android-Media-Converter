package com.github.khangnt.mcp.exception

import java.io.IOException

/**
 * Created by Khang NT on 1/2/18.
 * Email: khang.neon.1997@gmail.com
 */

class HttpResponseCodeException(
        val code: Int,
        val statusMessage: String,
        val errorBody: String
) : IOException("$code - $statusMessage\n$errorBody")