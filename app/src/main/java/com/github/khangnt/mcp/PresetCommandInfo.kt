package com.github.khangnt.mcp

import android.content.res.Resources
import android.graphics.drawable.GradientDrawable
import android.support.annotation.DrawableRes
import android.support.annotation.IntRange
import android.support.annotation.StringRes
import com.github.khangnt.mcp.annotation.ConvertType
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.CommandBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.aac.AacCmdBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.flac.FlacCmdBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.mp3.Mp3CmdBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.mp4.Mp4CmdBuilderFragment
import com.github.khangnt.mcp.ui.jobmaker.cmdbuilder.opus.OpusCmdBuilderFragment

private fun colorArrayOf(vararg longValues: Long): IntArray {
    return IntArray(longValues.size, { longValues[it].toInt() })
}

enum class Gradient(val colors: IntArray) {
    Disabled(colorArrayOf(0x80323232, 0x80434343)),
    SublimeLight(colorArrayOf(0xffFC5C7D, 0xff6A82FB)),
    Quepal(colorArrayOf(0xff11998e, 0xff38ef7d)),
    DigitalWater(colorArrayOf(0xff74ebd5, 0xffACB6E5)),
    Nighthawk(colorArrayOf(0xff2980b9, 0xff2c3e50)),
    Piglet(colorArrayOf(0xffee9ca7, 0xffffdde1)),
    KokoCaramel(colorArrayOf(0xffd1913c, 0xffffd194)),
    Turquoiseflow(colorArrayOf(0xff136a8a, 0xff267871)),
    SoundCloud(colorArrayOf(0xfffe8c00, 0xfff83600)),
    Mini(colorArrayOf(0xff30e8bf, 0xffff8235)),
    EasyMed(colorArrayOf(0xffdce35b, 0xff45b649)),
    Friday(colorArrayOf(0xff83a4d4, 0xffb6fbff))
    ;

    fun getDrawable(
            orientation: GradientDrawable.Orientation = GradientDrawable.Orientation.LEFT_RIGHT
    ) = GradientDrawable(orientation, colors)
}

interface PresetCommand {
    fun getTag(): String
    fun getTitle(resources: Resources): String
    fun createCommandBuilderFragment(): CommandBuilderFragment
}

enum class ConvertCommand(
        @ConvertType val type: Int, @StringRes val shortName: Int, val gradient: Gradient,
        val fragmentFactory: () -> CommandBuilderFragment

): PresetCommand {
    CONVERT_MP3(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_mp3,
            gradient = Gradient.SublimeLight,
            fragmentFactory = ::Mp3CmdBuilderFragment
    ),
    CONVERT_AAC(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_aac,
            gradient = Gradient.Quepal,
            fragmentFactory = ::AacCmdBuilderFragment
    ),
    CONVERT_FLAC(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_flac,
            gradient = Gradient.DigitalWater,
            fragmentFactory = ::FlacCmdBuilderFragment
    ),
    CONVERT_OPUS(
            type = ConvertType.TYPE_ENCODE_AUDIO, shortName = R.string.short_name_opus,
            gradient = Gradient.EasyMed,
            fragmentFactory = ::OpusCmdBuilderFragment
    ),
    CONVERT_MP4(
            type = ConvertType.TYPE_ENCODE_VIDEO, shortName = R.string.short_name_mp4,
            gradient = Gradient.Nighthawk,
            fragmentFactory = ::Mp4CmdBuilderFragment
    ),
    ;

    override fun createCommandBuilderFragment(): CommandBuilderFragment = fragmentFactory()

    override fun getTitle(resources: Resources): String {
        return resources.getString(R.string.title_convert, resources.getString(shortName))
    }

    override fun getTag(): String = name

}

enum class EditCommand(
        @StringRes val label: Int, @DrawableRes val iconRes: Int, val gradient: Gradient,
        @IntRange(from = 1) val minInputCount: Int,
        @IntRange(from = 1) val maxInputCount: Int,
        val fragmentFactory: () -> CommandBuilderFragment

): PresetCommand {
    CUT_LENGTH(
            label = R.string.label_cut_length, iconRes = R.drawable.ic_content_cut_black_24dp,
            gradient = Gradient.Piglet,
            minInputCount = 1, maxInputCount = 1,
            fragmentFactory = ::Mp3CmdBuilderFragment
    ),
    MERGE_VIDEO(
            label = R.string.label_merge_video, iconRes = R.drawable.ic_library_video_black_24dp,
            gradient = Gradient.KokoCaramel,
            minInputCount = 2, maxInputCount = 10,
            fragmentFactory = ::Mp3CmdBuilderFragment
    ),
    MERGE_AUDIO(
            label = R.string.label_merge_audio, iconRes = R.drawable.ic_library_music_black_24dp,
            gradient = Gradient.Turquoiseflow,
            minInputCount = 2, maxInputCount = 10,
            fragmentFactory = ::Mp3CmdBuilderFragment
    ),
    RESIZE_VIDEO(
            label = R.string.label_resize_video, iconRes = R.drawable.ic_aspect_ratio_black_24dp,
            gradient = Gradient.SoundCloud,
            minInputCount = 1, maxInputCount = 1,
            fragmentFactory = ::Mp3CmdBuilderFragment
    ),
    VIDEO_SPEED(
            label = R.string.label_video_speed, iconRes = R.drawable.ic_slow_motion_video_black_24dp,
            gradient = Gradient.Mini,
            minInputCount = 1, maxInputCount = 1,
            fragmentFactory = ::Mp3CmdBuilderFragment
    );

    override fun createCommandBuilderFragment(): CommandBuilderFragment = fragmentFactory()

    override fun getTitle(resources: Resources): String = resources.getString(label)

    override fun getTag(): String = name
}

