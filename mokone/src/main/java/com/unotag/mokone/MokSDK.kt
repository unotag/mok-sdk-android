package com.unotag.mokone

import MokApiCallTask
import MokLogger
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.unotag.mokone.core.MokSDKConstants
import com.unotag.mokone.network.MokApiConstants
import org.json.JSONObject

class MokSDK private constructor(private val context: Context) : MokApiCallTask.ApiCallback {

    companion object {
        private var instance: MokSDK? = null
        // Get the singleton instance of com.unotag.mokone.MokSDK
        fun getInstance(context: Context): MokSDK {
            if (instance == null) {
                instance = MokSDK(context.applicationContext)
            }
            return instance as MokSDK
        }
    }

    fun initMokSDK() {
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
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "Manifest meta is null")
        }
    }

    private fun getApiKeyFromManifest(context: Context): Bundle? {
        return try {
            val appInfo: ApplicationInfo =
                context.applicationContext.packageManager.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
            appInfo.metaData
        } catch (e: PackageManager.NameNotFoundException) {
            // Handle the exception if the package name is not found
            MokLogger.log(MokLogger.LogLevel.ERROR, "NameNotFoundException")
            null
        }
    }

    fun updateUser(userId: String, data: JSONObject) {
        val apiCallTask = MokApiCallTask(this)
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_REGISTRATION + userId,
            MokApiCallTask.HttpMethod.PATCH,
            MokApiCallTask.MokRequestMethod.WRITE,
            data
        )
    }

    fun triggerWorkflow(workflowId: String, data: JSONObject) {
        val apiCallTask = MokApiCallTask(this)
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_TRIGGER_WORKFLOW + workflowId,
            MokApiCallTask.HttpMethod.POST,
            MokApiCallTask.MokRequestMethod.WRITE,
            data
        )
    }



    // Implement the MokApiCallTask.ApiCallback interface methods
    override fun onSuccess(response: JSONObject) {
        MokLogger.log(MokLogger.LogLevel.DEBUG, response.toString())
    }

    override fun onError(error: Exception) {
        MokLogger.log(MokLogger.LogLevel.ERROR, error.message.toString())
    }

    fun getFCMToken(){
        Firebase.messaging.token.addOnCompleteListener(
            OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    MokLogger.log(MokLogger.LogLevel.DEBUG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                val token = task.result
                // Log and toast
                MokLogger.log(MokLogger.LogLevel.DEBUG, "token: $token")
            },
        )
    }
}
