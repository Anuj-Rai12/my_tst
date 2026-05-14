package com.pos10.db.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.pos10.db.entity.Checklist

class WoCheckListConverter {

    @TypeConverter
    fun fromChecklistList(list: List<Checklist>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun toChecklistList(data: String): List<Checklist> {
        val listType = object : TypeToken<List<Checklist>>() {}.type
        return Gson().fromJson(data, listType)
    }
}
