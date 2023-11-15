package com.unotag.mokone.managers

import android.content.Context
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.pushNotification.fcm.MokFirebaseMessagingService
import com.unotag.mokone.services.SharedPreferencesService
import com.unotag.mokone.utils.MokLogger
import org.json.JSONObject

class UserSessionManager(
    private val context: Context,
    private val apiCallTask: MokApiCallTask,
    private val messagingService: MokFirebaseMessagingService,
    private val sharedPrefsService: SharedPreferencesService
) {

    fun requestUpdateUser(
        userId: String,
        data: JSONObject?,
        callback: ((success: JSONObject?, errorMessage: String?) -> Unit)?
    ) {
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_REGISTRATION + userId,
            MokApiCallTask.HttpMethod.PATCH,
            MokApiCallTask.MokRequestMethod.WRITE,
            data
        ) { result ->
            handleUpdateUserResult(result, userId, callback)
        }
    }

    private fun handleUpdateUserResult(
        result: MokApiCallTask.ApiResult,
        userId: String,
        callback: ((success: JSONObject?, errorMessage: String?) -> Unit)?
    ) {
        when (result) {
            is MokApiCallTask.ApiResult.Success -> {
                val response = result.response
                savePersistenceUserId(userId)
                callback?.invoke(response, null)
                updateUserFCMToken()
            }
            is MokApiCallTask.ApiResult.Error -> {
                val error = result.exception
                callback?.invoke(null, error.localizedMessage)
            }
            else -> {
                callback?.invoke(null, "Something went wrong")
            }
        }
    }

    private fun updateUserFCMToken() {
        messagingService.getFCMToken { token, _ ->
            handleFCMTokenUpdate(token)
        }
    }

    private fun handleFCMTokenUpdate(token: String?) {
        if (token != null) {
            savePersistenceFCMToken(token)
            compareAndUpdateUserFcmToken(token)
        }
    }

    private fun compareAndUpdateUserFcmToken(token: String) {
        val persistenceFCMToken = getPersistenceFCMToken()
        if (persistenceFCMToken != token) {
            val userId = getPersistenceUserId()
            val jsonBody = JSONObject().apply {
                put("fcm_registration_token", token)
            }
            requestUpdateUser(userId, jsonBody, null)
        } else {
            MokLogger.log(MokLogger.LogLevel.INFO, "FCM token not changed, ignoring update")
        }
    }

    private fun savePersistenceFCMToken(token: String) {
        sharedPrefsService.saveString(SharedPreferencesService.FCM_TOKEN, token)
        MokLogger.log(MokLogger.LogLevel.INFO, "FCM token saved in shared storage")
    }

    private fun getPersistenceFCMToken(): String {
        val fcmToken = sharedPrefsService.getString(SharedPreferencesService.FCM_TOKEN)
        MokLogger.log(MokLogger.LogLevel.INFO, "FCM token value from shared storage: $fcmToken")
        return fcmToken
    }

    private fun savePersistenceUserId(userId: String) {
        sharedPrefsService.saveString(SharedPreferencesService.USER_ID_KEY, userId)
        MokLogger.log(MokLogger.LogLevel.INFO, "User ID saved in shared storage")
    }

    fun getPersistenceUserId(): String {
        val userId = sharedPrefsService.getString(SharedPreferencesService.USER_ID_KEY)
        MokLogger.log(MokLogger.LogLevel.INFO, "User ID value from shared storage: $userId")
        return userId
    }

    fun requestLogoutUser() {
        sharedPrefsService.clearAllPreferences()
        MokLogger.log(MokLogger.LogLevel.INFO, "User logout performed")
    }
}
