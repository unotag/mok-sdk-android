package com.unotag.mokone.pushNotification.fcm

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RestrictTo
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.unotag.mokone.utils.MokLogger

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PushNotificationPermissionHandler(
    private val mContext: Context,
    private val mActivity: Activity
) {

    fun isNotificationPermissionGranted(): Boolean {
        return if (ContextCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            MokLogger.log(MokLogger.LogLevel.INFO, "Permission is already granted")
            true
        } else {
            false
        }
    }


    fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // Permission is granted at install time on older devices
            MokLogger.log(
                MokLogger.LogLevel.DEBUG,
                "Permission is granted at install time on older devices"
            )
            return
        }

        if (ContextCompat.checkSelfPermission(
                mContext,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            MokLogger.log(MokLogger.LogLevel.INFO, "Permission is already granted")
            return
        } else {
            MokLogger.log(MokLogger.LogLevel.INFO, "Permission is denied")
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                mActivity,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        ) {
            // Show rationale if needed
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Notification permission permanently denied")
            showPermissionRationale()
        } else {
            // Request permission
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Requesting permission")
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101
            )
        }
    }

    private fun showPermissionRationale() {
        val builder = AlertDialog.Builder(mActivity)

        builder.setTitle("Permission Required")
        builder.setMessage("This app requires notification permission. Please allow the permission in the app settings.")

        builder.setPositiveButton("Go to Settings") { dialog, which ->
            openNotificationSettings()
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


    fun openNotificationSettings() {
        val intent = Intent()
        intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // for Android 5-7
        intent.putExtra("app_package", mContext.packageName)
        intent.putExtra("app_uid", mContext.applicationInfo.uid)

        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", mContext.packageName)
        mContext.startActivity(intent)
    }


}

