package com.unotag.mokone.pushNotification

import com.unotag.mokone.MokSDK
import com.unotag.mokone.utils.MetaDataReader

class NotificationRenderer {

    companion object{
        fun getSmallNotificationIcon() : Int {
            val context = MokSDK.getAppContext()
            val smallIcon = MetaDataReader.readManifest(MokSDK.getAppContext(), "MOK_SMALL_NOTIFICATION_ICON")
             return context.resources.getIdentifier(smallIcon, "drawable", context.packageName);
        }
    }
}