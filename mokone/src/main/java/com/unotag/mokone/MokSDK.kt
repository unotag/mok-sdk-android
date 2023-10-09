package com.unotag.mokone

import InAppMessageData
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.room.Room
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.unotag.mokone.core.MokSDKConstants
import com.unotag.mokone.db.InAppMessageEntity
import com.unotag.mokone.db.MokDb
import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivity
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import com.unotag.mokone.utils.MokLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            if (readKey == null || writeKey == null) {
                MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
            }
            readSavedInAppMessage()
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "Manifest meta is null")
        }
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


    private fun readSavedInAppMessage() {
        val db = Room.databaseBuilder(getAppContext(), MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val inAppMessageDao = db.inAppMessageDao()
            val savedInAppMessages: List<InAppMessageEntity> = inAppMessageDao.getAllInAppMessages()

            if (savedInAppMessages.isNotEmpty()) {
                MokLogger.log(
                    MokLogger.LogLevel.DEBUG, "Message count size:${savedInAppMessages.size ?: 0}"
                )
                // Process the list of saved in-app messages as needed
                for (inAppMessageEntity in savedInAppMessages) {
                    // Convert InAppMessageEntity to InAppMessageData and perform actions
                    val inAppMessageData = InAppMessageData.fromEntity(inAppMessageEntity)
                    // Handle the in-app message data

//                    showInAppMessageDialog(
//                        "<div class='bee-popup-container'><style> .bee-popup-row-1 .bee-popup-col-1, .bee-popup-row-2 .bee-popup-col-1, .bee-popup-row-2 .bee-popup-col-2, .bee-popup-row-3 .bee-popup-col-1 { padding-bottom: 0px; padding-top: 0px } .bee-popup-container div, .bee-popup-container p { margin: 0; padding: 0,background-color: #FF0000 } .bee-popup-container img { border: 0 } .bee-popup-container { color: #FF0000; font-family: Arial, Helvetica Neue, Helvetica, sans-serif } .bee-popup-container * { box-sizing: border-box } .bee-popup-container a, .bee-popup-row-1 .bee-popup-col-1 .bee-popup-block-1 a, .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li a { color: #0068a5 } .bee-popup-container p { margin: 0 } .bee-popup-container .bee-popup-row { position: relative } .bee-popup-container .bee-popup-row-content { max-width: 500px; position: relative; margin: 0 auto; display: flex } .bee-popup-container .bee-popup-row-content .bee-popup-col-w3 { flex: 3 } .bee-popup-container .bee-popup-row-content .bee-popup-col-w9 { flex: 9 } .bee-popup-container .bee-popup-row-content .bee-popup-col-w12 { flex: 12 } .bee-popup-image { overflow: auto } .bee-popup-image .bee-popup-center { margin: 0 auto } .bee-popup-row-2 .bee-popup-col-2 .bee-popup-block-1 { width: 100% } .bee-popup-list ul { margin: 0; padding: 0 } .bee-popup-image img { display: block; width: 100% } .bee-popup-social .icon img { max-height: 32px } .bee-popup-paragraph { overflow-wrap: anywhere } @media (max-width:520px) { .bee-popup-row-content:not(.no_stack) { display: block; max-width: 250px } } .bee-popup-row-1, .bee-popup-row-2, .bee-popup-row-3 { background-repeat: no-repeat } .bee-popup-row-1 .bee-popup-row-content { background-repeat: no-repeat; color: #000 } .bee-popup-row-1 .bee-popup-col-1 { border-bottom: 0 solid transparent; border-left: 0 solid transparent; border-right: 0 solid transparent; border-top: 0 solid transparent } .bee-popup-row-2 .bee-popup-row-content, .bee-popup-row-3 .bee-popup-row-content { background-repeat: no-repeat; border-radius: 0; color: #000 } .bee-popup-row-3 .bee-popup-col-1 .bee-popup-block-1 { padding: 10px; text-align: center } .bee-popup-row-1 .bee-popup-col-1 .bee-popup-block-1, .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 { padding: 10px; color: #000; direction: ltr; font-size: 14px; font-weight: 400; letter-spacing: 0; line-height: 120%; text-align: left } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 ul { list-style-type: revert; list-style-position: inside } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li:not(:last-child) { margin-bottom: 0 } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li ul { margin-top: 0 } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li li { margin-left: 40px } .bee-popup-row-1 .bee-popup-col-1 .bee-popup-block-1 p:not(:last-child) { margin-bottom: 16px } </style><div class='bee-popup-rows-container'><div class='bee-popup-row bee-popup-row-1'><div class='bee-popup-row-content'><div class='bee-popup-col bee-popup-col-1 bee-popup-col-w12'><div class='bee-popup-block bee-popup-block-1 bee-popup-paragraph'><p>Buy our Products and Get additional 10% Off</p></div></div></div></div><div class='bee-popup-row bee-popup-row-2'><div class='bee-popup-row-content'><div class='bee-popup-col bee-popup-col-1 bee-popup-col-w3'><div class='bee-popup-block bee-popup-block-1 bee-popup-list'><ul><li>Buy Products worth 100</li><li>Get Voucher Code</li><li>Apply on our website</li><li>Hurray</li></ul></div></div><div class='bee-popup-col bee-popup-col-2 bee-popup-col-w9'><div class='bee-popup-block bee-popup-block-1 bee-popup-image'><img class='bee-popup-center bee-popup-autowidth' src='https://d15k2d11r6t6rl.cloudfront.net/public/users/Integrators/cc122e38-1c4d-41f6-988c-cf7ba39e0148/lzp4gxx5n1/discont%20voucher.webp' style='max-width:375px;' alt=''/></div></div></div></div><div class='bee-popup-row bee-popup-row-3'><div class='bee-popup-row-content'><div class='bee-popup-col bee-popup-col-1 bee-popup-col-w12'><div class='bee-popup-block bee-popup-block-1 bee-popup-social'><div class='content'><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://www.facebook.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/facebook@2x.png' alt='Facebook' title='Facebook'/></a></span><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://twitter.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/twitter@2x.png' alt='Twitter' title='Twitter'/></a></span><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://instagram.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/instagram@2x.png' alt='Instagram' title='Instagram'/></a></span><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://www.linkedin.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/linkedin@2x.png' alt='LinkedIn' title='LinkedIn'/></a></span></div></div></div></div></div></div></div>"
//                    )

                    inAppMessageData.popupHtml?.let { showInAppMessageDialog(it) }

                    inAppMessageDao.markAsSeen(inAppMessageData.id)

                }
                // Call the method to show the in-app message dialog
            }
        }
    }

    private fun showInAppMessageDialog(message: String) {
        MokLogger.log(
            MokLogger.LogLevel.DEBUG,
            "showInAppMessageDialog called with message: $message"
        )
        val intent = Intent(getAppContext(), InAppMessageBaseActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("message_key", message)
        context.startActivity(intent)
    }


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
        parameter: JSONObject?,
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

}

