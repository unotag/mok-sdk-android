package com.unotag.mokone.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.unotag.mokone.inAppMessage.data.InAppMessageData

class InAppMessageDataConverter {
    @TypeConverter
    fun fromJson(json: String): InAppMessageData {
        return Gson().fromJson(json, InAppMessageData::class.java)
    }

    @TypeConverter
    fun toJson(inAppMessageData: InAppMessageData): String {
        return Gson().toJson(inAppMessageData)
    }
}
