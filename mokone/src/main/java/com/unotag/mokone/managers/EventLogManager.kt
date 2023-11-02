package com.unotag.mokone.managers

import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.utils.MokLogger
import org.json.JSONObject

class EventLogManager {

    fun requestLogEvent(
        userId: String,
        eventName: String,
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

}