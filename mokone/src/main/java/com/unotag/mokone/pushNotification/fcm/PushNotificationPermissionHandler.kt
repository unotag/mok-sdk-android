package com.unotag.mokone.pushNotification.fcm

import com.unotag.mokone.utils.MokLogger
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class PushNotificationPermissionHandler(private val activity: AppCompatActivity) {

    private var permissionLauncher: ActivityResultLauncher<String>? = null

    init {
        setupPermissionLauncher()
    }

    private fun setupPermissionLauncher() {
        permissionLauncher =
            activity.registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    // Permission granted
                    // You can perform actions here when the permission is granted
                    MokLogger.log(MokLogger.LogLevel.INFO, "Notification permission granted")
                } else {
                    // Permission denied
                    MokLogger.log(MokLogger.LogLevel.DEBUG, "Notification permission denied")
                    //showPermissionRationale()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // Permission is granted at install time on older devices
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Permission is granted at install time on older devices")
            return
        }

        if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is already granted
            MokLogger.log(MokLogger.LogLevel.INFO, "Permission is already granted")
            return
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.POST_NOTIFICATIONS)) {
            // Show rationale if needed
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Notification permission permanently denied")
            showPermissionRationale()
        } else {
            // Request permission
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Requesting permission")
            permissionLauncher?.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun showPermissionRationale() {
        MaterialAlertDialogBuilder(activity)
            .setTitle("Permission Required")
            .setMessage("This app requires notification permission. Please allow the permission in the app settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openNotificationSettings()
            }
            .setNegativeButton("Cancel") { _, _ ->
                // Handle cancellation
            }
            .show()
    }

    private fun openNotificationSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:${activity.packageName}")
            activity.startActivity(intent)
        } else {
            // Handle the case for devices with SDK_INT < M (e.g., Android versions prior to Marshmallow)
            null
        }
    }
}

