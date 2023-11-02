package com.unotag.mokone.helper

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import com.unotag.mokone.utils.MokLogger

class ManifestReader(private val context: Context) {

    companion object {
        const val MOK_READ_KEY = "MOK_READ_KEY"
        const val MOK_WRITE_KEY = "MOK_WRITE_KEY"
    }

    private fun getMetaData(): Bundle? {
        return try {
            val appInfo: ApplicationInfo =
                context.applicationContext.packageManager.getApplicationInfo(
                    context.packageName, PackageManager.GET_META_DATA
                )
            appInfo.metaData
        } catch (e: PackageManager.NameNotFoundException) {
            MokLogger.log(MokLogger.LogLevel.ERROR, "NameNotFoundException")
            null
        }
    }

    fun readString(key: String): String {
        val bundle = getMetaData()
        val keyValue = bundle?.getString(key) ?: ""
        MokLogger.log(
            MokLogger.LogLevel.DEBUG, "Manifest $key : $keyValue"
        )
        return keyValue
    }

    fun readInt(key: String): Int {
        val bundle = getMetaData()
        return bundle?.getInt(key) ?: 0
    }

    // You can add similar functions for other data types
}
