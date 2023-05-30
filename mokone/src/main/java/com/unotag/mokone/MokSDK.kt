package com.unotag.mokone

import MokApiCallTask
import MokLogger
import android.content.Context
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

    fun updateUser() {
        val apiCallTask = MokApiCallTask(this)
        apiCallTask.performApiCall(
            "https://dummy.restapiexample.com/api/v1/employees",
            MokApiCallTask.HttpMethod.GET
        )
    }

    // Implement the MokApiCallTask.ApiCallback interface methods
    override fun onSuccess(response: JSONObject) {
        MokLogger.log(MokLogger.LogLevel.DEBUG, response.toString())
    }

    override fun onError(error: Exception) {
        MokLogger.log(MokLogger.LogLevel.ERROR, error.message.toString())
    }



}
