package com.github.khangnt.mcp

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import com.github.khangnt.mcp.ui.presetcmd.aac.ConvertAacFragment
import com.github.khangnt.mcp.ui.presetcmd.flac.ConvertFlacFragment
import com.github.khangnt.mcp.ui.presetcmd.mp3.ConvertMp3Fragment
import com.github.khangnt.mcp.ui.presetcmd.mp4.ConvertMp4Fragment

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
            type = TYPE_AUDIO, shortName = "MP3", colors = colorArrayOf(0xffFC5C7D, 0xff6A82FB), // SublimeLight
            titleRes = R.string.preset_command_convert_mp3_title,
            descriptionRes = R.string.preset_command_convert_mp3_des,
            convertFragmentFactory = ::ConvertMp3Fragment
    ),
    CONVERT_AAC(
            type = TYPE_AUDIO, shortName = "AAC", colors = colorArrayOf(0xff11998e, 0xff38ef7d), // Quepal
            titleRes = R.string.preset_command_convert_aac_title,
            descriptionRes = R.string.preset_command_convert_aac_des,
            convertFragmentFactory = ::ConvertAacFragment
    ),
    CONVERT_FLAC(
            type = TYPE_AUDIO, shortName = "FLAC", colors = colorArrayOf(0xff74ebd5, 0xffACB6E5), // DigitalWater
            titleRes = R.string.preset_command_convert_flac_title,
            descriptionRes = R.string.preset_command_convert_flac_des,
            convertFragmentFactory = ::ConvertFlacFragment
    ),
    CONVERT_MP4(
            type = TYPE_VIDEO, shortName = "MP4", colors = colorArrayOf(0xff2980b9, 0xff2c3e50), // Nighthawk
            titleRes = R.string.preset_command_convert_mp4_title,
            descriptionRes = R.string.preset_command_convert_mp4_des,
            convertFragmentFactory = ::ConvertMp4Fragment
    )
    ;
}

