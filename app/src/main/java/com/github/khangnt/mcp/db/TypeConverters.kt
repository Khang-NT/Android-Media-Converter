package com.github.khangnt.mcp.db

import androidx.room.TypeConverter
import com.github.khangnt.mcp.db.job.Command
import org.json.JSONObject

/**
 * Created by Khang NT on 4/3/18.
 * Email: khang.neon.1997@gmail.com
 */

class TypeConverters {

    @TypeConverter
    fun comandToJson(command: Command): String {
        return command.toJson().toString()
    }

    @TypeConverter
    fun commandFromJson(json: String): Command {
        return Command.from(JSONObject(json))
    }

}