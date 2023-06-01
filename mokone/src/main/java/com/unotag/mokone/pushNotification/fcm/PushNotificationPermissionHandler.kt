package com.unotag.mokone.pushNotification.fcm

import MokLogger
import android.Manifest
import androidx.activity.result.ActivityResultLauncher

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.FragmentActivity

interface PushNotificationPermissionCallback {
    fun onPermissionGranted()
    fun onPermissionDenied()
}

object PushNotificationPermissionHandler {
    private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermission(
        context: Context,
        callback: PushNotificationPermissionCallback,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        this.requestPermissionLauncher = requestPermissionLauncher

        if (isPushNotificationPermissionGranted(context)) {
            callback.onPermissionGranted()
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Push Notification Permission Granted")
        } else {
            requestPushNotificationPermission(context, callback)
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Push Notification Permission Granted")

        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isPushNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PermissionChecker.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PermissionChecker.PERMISSION_GRANTED
        } else {
            // Handle the case for devices with SDK_INT < M (e.g., Android versions prior to Marshmallow)
            false
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPushNotificationPermission(
        context: Context,
        callback: PushNotificationPermissionCallback
    ) {
        if (context is FragmentActivity) {
            val activity = context as FragmentActivity
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                // Show rationale or explanation for permission
                // This is optional and can be customized based on your app's requirements
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Handle if the context is not a FragmentActivity
            callback.onPermissionDenied()
        }
    }

    fun handlePermissionRequestResult(
        requestCode: Int,
        grantResults: IntArray,
        callback: PushNotificationPermissionCallback
    ) {
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callback.onPermissionGranted()
            } else {
                callback.onPermissionDenied()
            }
        }
    }

    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
        } else {
            // Handle the case for devices with SDK_INT < M (e.g., Android versions prior to Marshmallow)
            null
        }

        intent?.let {
            context.startActivity(intent)
        }
    }
}
