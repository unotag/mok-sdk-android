package com.unotag.mokone.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle

class MetaDataReader {

    companion object {
         fun readManifest(context: Context, key: String): String? {
            return try {
                val appInfo: ApplicationInfo =
                    context.applicationContext.packageManager.getApplicationInfo(
                        context.packageName,
                        PackageManager.GET_META_DATA
                    )
                val metaData = appInfo.metaData
                metaData?.getString(key)
            } catch (e: PackageManager.NameNotFoundException) {
                // Handle the exception if the package name is not found
                MokLogger.log(MokLogger.LogLevel.ERROR, "NameNotFoundException")
                null
            }
        }
    }
}