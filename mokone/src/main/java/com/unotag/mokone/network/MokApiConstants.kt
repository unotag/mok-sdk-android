package com.unotag.mokone.network

import com.unotag.mokone.core.MokSDKConstants.Companion.IS_DEVELOPMENT_ENV

class MokApiConstants {
    companion object {
        var BASE_URL =
            if (IS_DEVELOPMENT_ENV) "https://dev.mok.one/api/customer/v1.2" else "https://live.mok.one/api/customer/v1.2"
        const val URL_REGISTRATION = "/registration/"
        const val URL_TRIGGER_WORKFLOW = "/trigger/"
        const val URL_ADD_USER_ACTIVITY = "/add-user-activity/"
    }
}