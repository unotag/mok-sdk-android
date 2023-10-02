package com.unotag.mokone

import MokApiCallTask
import com.unotag.mokone.utils.MokLogger
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
import com.unotag.mokone.network.MokApiConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.json.JSONObject
import kotlinx.coroutines.launch

class MokSDK private constructor(private val context: Context) {

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


    fun initMokSDK(isProductionEvn : Boolean) {
        MokSDKConstants.IS_PRODUCTION_ENV = isProductionEvn
        val bundle: Bundle? = getApiKeyFromManifest(context)
        if (bundle != null) {
            val readKey = bundle.getString("MOK_READ_KEY")
            val writeKey = bundle.getString("MOK_WRITE_KEY")
            if (readKey != null && writeKey != null) {
                MokSDKConstants.READ_KEY = readKey
                MokSDKConstants.WRITE_KEY = writeKey
                MokLogger.log(MokLogger.LogLevel.DEBUG, "READ_KEY : ${MokSDKConstants.READ_KEY}")
                MokLogger.log(MokLogger.LogLevel.DEBUG, "WRITE_KEY : ${MokSDKConstants.WRITE_KEY}")
            } else {
                MokLogger.log(MokLogger.LogLevel.ERROR, "READ/WRITE key is missing")
            }
            readSavedInAppMessage()
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "Manifest meta is null")
        }
    }

    private fun getApiKeyFromManifest(context: Context): Bundle? {
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
                }
                // Call the method to show the in-app message dialog
                showInAppMessageDialog()
            }
        }
    }

    private fun showInAppMessageDialog() {
        MokLogger.log(MokLogger.LogLevel.DEBUG, "showInAppMessageDialog called")
        val intent = Intent(getAppContext(), InAppMessageBaseActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
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
                callback(null,task.exception?.localizedMessage)
            }
        }
    }
}

