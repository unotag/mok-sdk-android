package com.unotag.mokone

import android.app.Activity
import android.content.Context
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.unotag.mokone.core.MokSDKConstants
import com.unotag.mokone.helper.ManifestReader
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.managers.EventLogManager
import com.unotag.mokone.managers.UserSessionManager
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import com.unotag.mokone.services.SharedPreferencesService
import com.unotag.mokone.utils.MokLogger
import org.json.JSONObject

class MokSDK private constructor() {

    private lateinit var mContext: Context
    private lateinit var pushNotificationPermissionHandler: PushNotificationPermissionHandler

    companion object {
        private var instance: MokSDK? = null
        private var appContext: Context? = null

        // Get the singleton instance of MokSDK
        fun getInstance(context: Context): MokSDK {
            if (instance == null) {
                instance = MokSDK()
                appContext = context.applicationContext
                instance?.mContext = context.applicationContext
            }
            return instance as MokSDK
        }

        fun getAppContext(): Context {
            return appContext
                ?: throw IllegalStateException("MokSDK has not been initialized. Call getInstance() first.")
        }
    }


    fun initMokSDK(isProdEnv: Boolean) {
        MokSDKConstants.IS_PRODUCTION_ENV = isProdEnv

        val manifestReader = ManifestReader(mContext)
        val readKey = manifestReader.readString(ManifestReader.MOK_READ_KEY)
        val writeKey = manifestReader.readString(ManifestReader.MOK_WRITE_KEY)
        if (readKey.isNotEmpty() || writeKey.isNotEmpty()) {
            MokSDKConstants.READ_KEY = readKey
            MokSDKConstants.WRITE_KEY = writeKey
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
        }

        requestIAMFromServerAndShow()
    }

    fun enableProductionEnvironment(value: Boolean) {
        MokSDKConstants.IS_PRODUCTION_ENV = value
    }

    fun enableInAppMessages(value: Boolean){
        if (value) {
            requestIAMFromServerAndShow()
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
            mContext.applicationContext,
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


    //region UpdateUser, logEvent
    fun updateUser(
        userId: String,
        data: JSONObject?,
        callback: (success: JSONObject?, errorMessage: String?) -> Unit
    ) {
        val userSessionManager = UserSessionManager(mContext)
        userSessionManager.requestUpdateUser(userId, data, callback)
    }

    fun getUserId(): String{
        val userSessionManager = UserSessionManager(mContext)
        return userSessionManager.getPersistenceUserId()
    }

    fun logoutUser(){
        val userSessionManager = UserSessionManager(mContext)
        userSessionManager.requestLogoutUser()
    }

    fun logEvent(
        userId: String,
        eventName: String,
        parameter: JSONObject? = null,
        callback: (success: JSONObject?, error: String?) -> Unit
    ){
        val eventLogManager = EventLogManager()
        eventLogManager.requestLogEvent(userId, eventName, parameter, callback)
    }

//endregion

    //region In App messages
     fun requestIAMFromServerAndShow() {
        val sharedPreferencesService = SharedPreferencesService(mContext)
        val userId = sharedPreferencesService.getString(SharedPreferencesService.USER_ID_KEY, "")
        if (userId.isNotEmpty()) {
            val inAppMessageHandler = InAppMessageHandler(mContext, userId)
            inAppMessageHandler.fetchIAMFromServerAndSaveToDB(
            ) { inAppMessageData: InAppMessageData?, errorMessage: String? ->
                inAppMessageHandler.showInAppMessages(5)
            }
        } else {
            MokLogger.log(
                MokLogger.LogLevel.ERROR,
                "User is not registered, kindly register the user with a unique CLIENT_ID"
            )
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
