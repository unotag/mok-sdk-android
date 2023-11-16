package com.unotag.mokone.services

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesService(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = sharedPreferences.edit()

    companion object {
        const val USER_ID_KEY: String = "user_id"
        const val FCM_TOKEN: String = "fcm_token"
    }

    fun saveString(key: String, value: String) {
        editor.putString(key, value)
        editor.apply()
    }

    fun getString(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: ""
    }

    fun saveInt(key: String, value: Int) {
        editor.putInt(key, value)
        editor.apply()
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    // You can add similar methods for other data types like Boolean, Float, etc.

    fun remove(key: String) {
        editor.remove(key)
        editor.apply()
    }

    fun clearAllPreferences() {
        editor.clear()
        editor.apply()
    }
}
