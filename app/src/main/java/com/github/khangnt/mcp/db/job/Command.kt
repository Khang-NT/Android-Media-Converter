package com.github.khangnt.mcp.db.job

import com.github.khangnt.mcp.annotation.Muxer
import com.github.khangnt.mcp.util.toListString
import com.github.khangnt.mcp.util.toMapString
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by Khang NT on 12/30/17.
 * Email: khang.neon.1997@gmail.com
 */

data class Command(
        val inputs: List<String>,
        val output: String,
        val outputFormat: String,
        val args: String,
        val environmentVars: Map<String, String>
) {
    init {
        if (inputs.isEmpty()) {
            throw IllegalArgumentException("`inputs` must not empty")
        }
    }

    fun toJson(): JSONObject {
        return JSONObject()
                .putOpt("inputs", JSONArray(inputs))
                .putOpt("output", output)
                .putOpt("outputFormat", outputFormat)
                .putOpt("args", args)
                .putOpt("environmentVars", JSONObject(environmentVars))
    }

    companion object {
        fun from(jsonObject: JSONObject): Command {
            return Command(jsonObject.getJSONArray("inputs").toListString(),
                    jsonObject.getString("output"),
                    jsonObject.getString("outputFormat"),
                    jsonObject.getString("args"),
                    jsonObject.getJSONObject("environmentVars").toMapString())
        }
    }
}
