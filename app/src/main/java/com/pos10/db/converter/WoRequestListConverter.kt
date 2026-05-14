package com.pos10.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pos10.db.entity.WoRequestEntity

class WoRequestListConverter {

    @TypeConverter
    fun fromWoRequestList(list: List<WoRequestEntity>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toWoRequestList(data: String): List<WoRequestEntity> {
        val listType = object : TypeToken<List<WoRequestEntity>>() {}.type
        return Gson().fromJson(data, listType)
    }
}
