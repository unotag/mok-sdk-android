package com.unotag.mokone.network

import com.unotag.mokone.core.MokSDKConstants

class MokApiConstants {
    companion object {
        var BASE_URL =
            if (MokSDKConstants.IS_PRODUCTION_ENV) "https://live.mok.one/api/customer/v1.2" else "https://dev.mok.one/api/customer/v1.2"
        const val URL_REGISTRATION = "/registration/"
        const val URL_TRIGGER_WORKFLOW = "/trigger/"
        const val URL_ADD_USER_ACTIVITY = "/add-user-activity/"
        const val URL_IN_APP_MESSAGE_DATA = "/in_app_operation_data/"
        const val URL_PENDING_IN_APP_MESSAGE = "/pending-popups/"
        const val URL_MARK_READ_IN_APP_MESSAGE = "/mark_read_in_app/"
    }
}