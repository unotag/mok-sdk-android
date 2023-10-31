package com.unotag.mokone

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.unotag.mokone.core.MokSDKConstants
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import com.unotag.mokone.utils.MokLogger
import org.json.JSONObject

class MokSDK private constructor(private val context: Context) {

    private lateinit var pushNotificationPermissionHandler: PushNotificationPermissionHandler

    companion object {
        private var instance: MokSDK? = null
        private var appContext: Context? = null

        // Get the singleton instance of MokSDK
        fun getInstance(context: Context): MokSDK {
            if (instance == null) {
                instance = MokSDK(context.applicationContext)
                appContext = context.applicationContext
            }
            return instance as MokSDK
        }

        fun getAppContext(): Context {
            return appContext
                ?: throw IllegalStateException("MokSDK has not been initialized. Call getInstance() first.")
        }
    }


    fun initMokSDK(isProductionEvn: Boolean) {
        MokSDKConstants.IS_PRODUCTION_ENV = isProductionEvn
        val bundle: Bundle? = getApiKeyFromManifest(context)
        if (bundle != null) {
            val readKey = bundle.getString("MOK_READ_KEY")
            val writeKey = bundle.getString("MOK_WRITE_KEY")
            if (readKey != null || writeKey != null) {
                MokSDKConstants.READ_KEY = readKey!!
                MokSDKConstants.WRITE_KEY = writeKey!!
                MokLogger.log(MokLogger.LogLevel.DEBUG, "READ_KEY : ${MokSDKConstants.READ_KEY}")
                MokLogger.log(MokLogger.LogLevel.DEBUG, "WRITE_KEY : ${MokSDKConstants.WRITE_KEY}")
            } else {
                MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
            }
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "Manifest meta is null")
        }


        //TODO: when user id is not avail
        requestIAMFromServerAndShow()
    }

    //TODO: make this private before going live
    fun getApiKeyFromManifest(context: Context): Bundle? {
        return try {
            val appInfo: ApplicationInfo =
                context.applicationContext.packageManager.getApplicationInfo(
                    context.packageName, PackageManager.GET_META_DATA
                )
            appInfo.metaData
        } catch (e: PackageManager.NameNotFoundException) {
            // Handle the exception if the package name is not found
            MokLogger.log(MokLogger.LogLevel.ERROR, "NameNotFoundException")
            null
        }
    }


//region FetchFCM Notification status, permission, settings

    fun getFCMToken(callback: (String?, String?) -> Unit) {
        Firebase.messaging.token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                MokLogger.log(MokLogger.LogLevel.DEBUG, "FCM token: $token")
                callback(token, null)
            } else {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR,
                    "Fetching FCM registration token failed",
                    task.exception
                )
                callback(null, task.exception?.localizedMessage)
            }
        }
    }

    private fun initializePushNotificationPermissionHandler(activity: Activity) {
        pushNotificationPermissionHandler = PushNotificationPermissionHandler(
            context.applicationContext,
            activity
        )
    }

    fun requestNotificationPermission(activity: Activity) {
        initializePushNotificationPermissionHandler(activity)
        pushNotificationPermissionHandler.requestPermission()
    }

    fun openNotificationSettings(activity: Activity) {
        initializePushNotificationPermissionHandler(activity)
        pushNotificationPermissionHandler.openNotificationSettings()
    }

    fun isNotificationPermissionGranted(activity: Activity): Boolean {
        initializePushNotificationPermissionHandler(activity)
        return pushNotificationPermissionHandler.isNotificationPermissionGranted()
    }
//endregion


//region UpdateUser, triggerWorkflow, logActivity requestInAppMessageDataFromServer API
    fun updateUser(
        userId: String,
        data: JSONObject?,
        callback: (success: JSONObject?, errorMessage: String?) -> Unit
    ) {
        val apiCallTask = MokApiCallTask()
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_REGISTRATION + userId,
            MokApiCallTask.HttpMethod.PATCH,
            MokApiCallTask.MokRequestMethod.WRITE,
            data
        ) { result ->
            when (result) {
                is MokApiCallTask.ApiResult.Success -> {
                    val response = result.response
                    callback(response, null)
                }

                is MokApiCallTask.ApiResult.Error -> {
                    val error = result.exception
                    callback(null, error.localizedMessage)
                }

                else -> {
                    callback(null, "Something went wrong")
                }
            }
        }
    }

    fun triggerWorkflow(
        workflowId: String,
        data: JSONObject,
        callback: (success: JSONObject?, error: String?) -> Unit
    ) {
        val apiCallTask = MokApiCallTask()
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_TRIGGER_WORKFLOW + workflowId,
            MokApiCallTask.HttpMethod.POST,
            MokApiCallTask.MokRequestMethod.WRITE,
            data
        ) { result ->
            when (result) {
                is MokApiCallTask.ApiResult.Success -> {
                    val response = result.response
                    callback(response, null)
                }

                is MokApiCallTask.ApiResult.Error -> {
                    val error = result.exception
                    callback(null, error.localizedMessage)
                }

                else -> {
                    callback(null, "Something went wrong")
                }

            }
        }
    }

    fun logActivity(
        eventName: String,
        userId: String,
        parameter: JSONObject? = null,
        callback: (success: JSONObject?, error: String?) -> Unit
    ) {
        val data = JSONObject()
        data.put("event_name", eventName)
        parameter?.keys()?.forEach { key ->
            data.put(key, parameter.get(key))
        }

        val apiCallTask = MokApiCallTask()
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_ADD_USER_ACTIVITY + userId,
            MokApiCallTask.HttpMethod.POST,
            MokApiCallTask.MokRequestMethod.WRITE,
            data
        ) { result ->
            when (result) {
                is MokApiCallTask.ApiResult.Success -> {
                    val response = result.response
                    callback(response, null)
                    MokLogger.log(
                        MokLogger.LogLevel.DEBUG, "User activity logged successfully"

                    )
                }

                is MokApiCallTask.ApiResult.Error -> {
                    val error = result.exception
                    callback(null, error.localizedMessage)
                }

                else -> {
                    callback(null, "Something went wrong")

                }
            }
        }
    }


//endregion

    //region In App messages

    fun requestIAMFromServerAndShow() {
        val inAppMessageHandler = InAppMessageHandler(context)
        inAppMessageHandler?.fetchIAMFromServerAndSaveToDB(
            "MOASDK_001"
        ) { inAppMessageData: InAppMessageData?, errorMessage: String? ->
            inAppMessageHandler.showInAppMessages(30)
        }
    }

//endregion


    //TODO: Delete this before going live
    fun updateApiKeys(readKey: String, writeKey: String) {
        MokSDKConstants.READ_KEY = readKey
        MokSDKConstants.WRITE_KEY = writeKey
        MokLogger.log(MokLogger.LogLevel.DEBUG, "READ_KEY : ${MokSDKConstants.READ_KEY}")
        MokLogger.log(MokLogger.LogLevel.DEBUG, "WRITE_KEY : ${MokSDKConstants.WRITE_KEY}")
    }

    //TODO: Delete this before going live
    fun updateEnv(isRelease: Boolean) = if (isRelease) {
        MokSDKConstants.IS_PRODUCTION_ENV = isRelease
        MokApiConstants.BASE_URL = "https://live.mok.one/api/customer/v1.2"
    } else {
        MokSDKConstants.IS_PRODUCTION_ENV = isRelease
        MokApiConstants.BASE_URL = "https://dev.mok.one/api/customer/v1.2"
    }

    //TODO: Delete this before going live
    fun getHostUrl(): String {
        return MokApiConstants.BASE_URL
    }

    //TODO: Delete this before going live
    fun markInAppAsRead() {
    }

}
