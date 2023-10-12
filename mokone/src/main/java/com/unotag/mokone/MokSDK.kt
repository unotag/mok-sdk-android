package com.unotag.mokone

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
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivity
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.pushNotification.fcm.PushNotificationPermissionHandler
import com.unotag.mokone.utils.MokLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

                readTopSavedInAppMessage()

            }else{
                MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
            }
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


    fun readSavedInAppMessage() {
        val db = Room.databaseBuilder(getAppContext(), MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val inAppMessageDao = db.inAppMessageDao()
                val savedInAppMessages: List<InAppMessageEntity> = inAppMessageDao.getAllInAppMessages()

                if (savedInAppMessages.isNotEmpty()) {
                    MokLogger.log(
                        MokLogger.LogLevel.DEBUG, "Message count size:${savedInAppMessages.size}"
                    )
                    // Process the list of saved in-app messages as needed
                    for (inAppMessageEntity in savedInAppMessages) {
                        // Convert InAppMessageEntity to com.unotag.mokone.inAppMessage.data.InAppMessageData and perform actions
                        val inAppMessageData = InAppMessageData.fromEntity(inAppMessageEntity)

                        try {
                            inAppMessageData.popupHtml?.let { showInAppMessageDialog(it) }
                        } catch (e: Exception) {
                            MokLogger.log(
                                MokLogger.LogLevel.ERROR, "Error showing in-app message dialog: ${e.message}"
                            )
                        }

                        // Uncomment the following lines if you want to mark messages as seen
                        // if (!inAppMessageData.isSeen) {
                        //     inAppMessageDao.markAsSeen(inAppMessageData.id)
                        // }
                    }
                    // Call the method to show the in-app message dialog
                }
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR, "Error reading saved in-app messages: ${e.message}"
                )
            } finally {
                db.close() // Close the database when done
            }
        }
    }



    fun readTopSavedInAppMessage() {
        val db = Room.databaseBuilder(getAppContext(), MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val inAppMessageDao = db.inAppMessageDao()
                val savedInAppMessages: List<InAppMessageEntity> = inAppMessageDao.getAllInAppMessages()

                if (savedInAppMessages.isNotEmpty()) {
                    MokLogger.log(
                        MokLogger.LogLevel.DEBUG, "Message count size:${savedInAppMessages.size}"
                    )

                    // Retrieve the top (first) in-app message from the list
                    val topMessage = savedInAppMessages.last()

                    // Convert InAppMessageEntity to com.unotag.mokone.inAppMessage.data.InAppMessageData and perform actions
                    val inAppMessageData = InAppMessageData.fromEntity(topMessage)

                    try {
                        inAppMessageData.popupHtml?.let { showInAppMessageDialog(it) }
                    } catch (e: Exception) {
                        MokLogger.log(
                            MokLogger.LogLevel.ERROR, "Error showing in-app message dialog: ${e.message}"
                        )
                    }

                    // Uncomment the following lines if you want to mark the message as seen
                    // if (!inAppMessageData.isSeen) {
                    //     inAppMessageDao.markAsSeen(inAppMessageData.id)
                    // }

                    // Call the method to show the in-app message dialog
                }
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR, "Error reading top saved in-app message: ${e.message}"
                )
            } finally {
                db.close() // Close the database when done
            }
        }
    }



    fun deleteAllInAppMessages() {
        val db = Room.databaseBuilder(getAppContext(), MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val inAppMessageDao = db.inAppMessageDao()
            inAppMessageDao.deleteAllInAppMessages()
        }
        MokLogger.log(MokLogger.LogLevel.INFO, "All in app messages deleted successfully")
    }


    fun resetIsSeenToUnSeen() {
        val db = Room.databaseBuilder(getAppContext(), MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val inAppMessageDao = db.inAppMessageDao()
            inAppMessageDao.resetIsSeenToFalse()
        }
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "All in app messages resetIsSeenToUnSeen successfully"
        )
    }

    suspend fun getIAMCount(): String {
        val db = Room.databaseBuilder(getAppContext(), MokDb::class.java, "mok-database").build()

        return withContext(Dispatchers.IO) {
            val inAppMessageDao = db.inAppMessageDao()
            val count = inAppMessageDao.getMessageCount()
            MokLogger.log(MokLogger.LogLevel.INFO, "IAM count: $count")
            count.toString() // Convert count to a String and return it
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

    //TODO: Delete this before going live
    fun markInAppAsRead() {
    }

}
