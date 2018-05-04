package com.github.khangnt.mcp

import android.support.annotation.IntRange
import android.support.annotation.StringRes
import com.github.khangnt.mcp.annotation.ConvertType
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.*

const val TYPE_AUDIO = 0
const val TYPE_VIDEO = 1

private fun colorArrayOf(vararg longValues: Long): IntArray {
    return IntArray(longValues.size, { longValues[it].toInt() })
}

// TODO: create some CmdBuilderFragment

enum class ConvertCommand(
        @ConvertType val type: Int, @StringRes val shortName: Int,
        val colors: IntArray,
        @IntRange(from = 1) val minInputCount: Int,
        @IntRange(from = 1) val maxInputCount: Int,
        val fragmentFactory: () -> CommandBuilderFragment

) {
    CONVERT_MP3(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_mp3,
            colors = colorArrayOf(0xffFC5C7D, 0xff6A82FB), // SublimeLight
            minInputCount = 1, maxInputCount = Int.MAX_VALUE,
            fragmentFactory = ::Mp3CmdBuilderFragment
    ),
    CONVERT_AAC(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_aac,
            colors = colorArrayOf(0xff11998e, 0xff38ef7d), // Quepal
            minInputCount = 1, maxInputCount = Int.MAX_VALUE,
            fragmentFactory = ::AacCmdBuilderFragment
    ),
    CONVERT_FLAC(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_flac,
            colors = colorArrayOf(0xff74ebd5, 0xffACB6E5), // DigitalWater
            minInputCount = 1, maxInputCount = Int.MAX_VALUE,
            fragmentFactory = ::FlacCmdBuilderFragment
    ),
    CONVERT_MP4(
            type = ConvertType.TYPE_ENCODE_VIDEO, shortName = R.string.short_name_mp4,
            colors = colorArrayOf(0xff2980b9, 0xff2c3e50), // Nighthawk
            minInputCount = 1, maxInputCount = Int.MAX_VALUE,
            fragmentFactory = ::Mp4CmdBuilderFragment
    )
    ;

}

