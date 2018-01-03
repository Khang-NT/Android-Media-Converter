package com.github.khangnt.mcp.exception

/**
 * Created by Khang NT on 1/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class UnhappyExitCodeException(code: Int, log: String? = null) :
        Exception("Exit code: $code" + (if (log !== null) "\n$log" else ""))
