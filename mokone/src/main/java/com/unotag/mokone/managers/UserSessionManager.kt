package com.unotag.mokone.managers

import android.content.Context
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.services.SharedPreferencesService
import com.unotag.mokone.utils.MokLogger
import org.json.JSONObject

class UserSessionManager(private val context: Context) {

    fun requestUpdateUser(
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
                    savePersistenceUserId(userId)
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

    private fun savePersistenceUserId(userId: String) {
        val sharedPrefsService = SharedPreferencesService(context)
        sharedPrefsService.saveString(SharedPreferencesService.USER_ID_KEY, userId)
        MokLogger.log(MokLogger.LogLevel.INFO, "User ID saved in shared storage")
    }

    fun getPersistenceUserId(): String {
        val sharedPrefsService = SharedPreferencesService(context)
        val userId = sharedPrefsService.getString(SharedPreferencesService.USER_ID_KEY)
        MokLogger.log(MokLogger.LogLevel.INFO, "User ID value from shared storage : $userId")
        return userId
    }


    fun requestLogoutUser() {
        val sharedPreferencesService = SharedPreferencesService(context)
        sharedPreferencesService.clearAllPreferences()
        MokLogger.log(MokLogger.LogLevel.INFO, "User logout performed")
    }

}