package com.github.khangnt.mcp.util

import com.github.khangnt.mcp.DEFAULT_IO_BUFFER_LENGTH
import java.io.FileInputStream
import java.security.MessageDigest

object Md5Checksum {
    @Throws(Exception::class)
    fun createChecksum(filename: String): String {
        FileInputStream(filename).use { fis ->
            val buffer = ByteArray(DEFAULT_IO_BUFFER_LENGTH)
            val complete = MessageDigest.getInstance("MD5")
            var numRead: Int

            do {
                numRead = fis.read(buffer)
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead)
                }
            } while (numRead != -1)
            return bytesToHex(complete.digest())
        }
    }

    fun bytesToHex(bytes: ByteArray): String {
        val hexArray = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        val hexChars = CharArray(bytes.size * 2)
        var v: Int
        for (j in bytes.indices) {
            v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

}