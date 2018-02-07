package com.github.khangnt.mcp

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import com.github.khangnt.mcp.ui.presetcmd.mp3.ConvertMp3Fragment

const val TYPE_AUDIO = 0
const val TYPE_VIDEO = 1

private fun colorArrayOf(vararg longValues: Long): IntArray {
    return IntArray(longValues.size, { longValues[it].toInt() })
}

enum class PresetCommand(
        val type: Int,
        val shortName: String,
        val colors: IntArray,
        @StringRes val titleRes: Int,
        @StringRes val descriptionRes: Int,
        val convertFragmentFactory: () -> Fragment
) {
    CONVERT_MP3(
            type = TYPE_AUDIO, shortName = "MP3", colors = colorArrayOf(0xffFC5C7D, 0xff6A82FB),
            titleRes = R.string.preset_command_convert_mp3_title,
            descriptionRes = R.string.preset_command_convert_mp3_des,
            convertFragmentFactory = ::ConvertMp3Fragment
    ),

    ;
}

