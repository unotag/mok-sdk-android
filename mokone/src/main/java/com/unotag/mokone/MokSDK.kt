package com.unotag.mokone

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.unotag.mokone.core.MokSDKConstants
import com.unotag.mokone.helper.ManifestReader
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.managers.EventLogManager
import com.unotag.mokone.managers.UserSessionManager
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.pip.ui.PiPActivity
import com.unotag.mokone.pushNotification.fcm.MokFirebaseMessagingService
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import com.unotag.mokone.services.SharedPreferencesService
import com.unotag.mokone.utils.MokLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.ref.WeakReference

object MokSDK {

    private var appContextRef: WeakReference<Context>? = null

    private lateinit var pushNotificationPermissionHandler: PushNotificationPermissionHandler

    private val apiCallTask = MokApiCallTask()
    private val mokFirebaseMessagingService = MokFirebaseMessagingService()
    private val sharedPrefsService by lazy { SharedPreferencesService(appContext) }
    private val userSessionManager by lazy {
        UserSessionManager(
            appContext,
            apiCallTask,
            mokFirebaseMessagingService,
            sharedPrefsService
        )
    }

     val appContext: Context
        get() = appContextRef?.get()
            ?: throw IllegalStateException("MokSDK has not been initialized. Call getInstance() first.")


    fun getInstance(context: Context): MokSDK {
        if (appContextRef == null || appContextRef?.get() == null) {
            appContextRef = WeakReference(context.applicationContext)
        }
        return this
    }

    fun initMokSDK(isProdEnv: Boolean, delayMillis: Long? = 5000, maxDisplayedIAMs: Int? = 5) {
        MokSDKConstants.IS_PRODUCTION_ENV = isProdEnv

        val manifestReader = ManifestReader(appContext)
        val readKey = manifestReader.readString(ManifestReader.MOK_READ_KEY)
        val writeKey = manifestReader.readString(ManifestReader.MOK_WRITE_KEY)
        if (readKey.isNotEmpty() || writeKey.isNotEmpty()) {
            MokSDKConstants.READ_KEY = readKey
            MokSDKConstants.WRITE_KEY = writeKey
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
        }

        GlobalScope.launch {
            kotlinx.coroutines.delay(delayMillis ?: 1000)
            //requestIAMFromServerAndShow(maxDisplayedIAMs ?: 5)
        }
    }

    fun enableProductionEnvironment(value: Boolean) {
        MokSDKConstants.IS_PRODUCTION_ENV = value
    }

    fun enableInAppMessages(value: Boolean) {
        if (value) {
            requestIAMFromServerAndShow()
        }
    }

//region FetchFCM Notification status, permission, settings

    fun requestFCMToken(callback: (String?, String?) -> Unit) {
        val mokFirebaseMessagingService = MokFirebaseMessagingService()
        mokFirebaseMessagingService.getFCMToken(callback)
    }

    private fun initializePushNotificationPermissionHandler(activity: Activity) {
        pushNotificationPermissionHandler = PushNotificationPermissionHandler(
            appContext.applicationContext,
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
        userSessionManager.requestUpdateUser(userId, data, callback)
    }

    fun getUserId(): String {
        return userSessionManager.getPersistenceUserId()
    }

    fun logoutUser() {
        userSessionManager.requestLogoutUser()
    }

    fun logEvent(
        userId: String,
        eventName: String,
        parameter: JSONObject? = null,
        callback: (success: JSONObject?, error: String?) -> Unit
    ) {
        val eventLogManager = EventLogManager()
        eventLogManager.requestLogEvent(userId, eventName, parameter, callback)
    }

//endregion

//region In App messages
    fun requestIAMFromServerAndShow(maxDisplayedIAMs: Int = 5) {
        val sharedPreferencesService = SharedPreferencesService(appContext)
        val userId = sharedPreferencesService.getString(SharedPreferencesService.USER_ID_KEY, "")
        if (userId.isNotEmpty()) {
            val inAppMessageHandler = InAppMessageHandler(appContext, userId)
            inAppMessageHandler.fetchIAMFromServerAndSaveToDB(
            ) { inAppMessageData: InAppMessageData?, errorMessage: String? ->
                   MokLogger.log(MokLogger.LogLevel.INFO, "callback received from fetchIAMFromServerAndSaveToDB")
                inAppMessageHandler.showInAppMessages(maxDisplayedIAMs)
            }
        } else {
            MokLogger.log(
                MokLogger.LogLevel.ERROR,
                "User is not registered, kindly register the user with a unique CLIENT_ID"
            )
        }
    }

//endregion

//region Pip
fun launchPipVideo() {
    val intent = Intent(appContext, PiPActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    appContext.startActivity(intent)
}//region Pip



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
