package com.unotag.mokone.pushNotification.call


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class CallNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            CallNotificationHandler.ACTION_ACCEPT_CALL -> {
                // Handle accept call action
                Toast.makeText(context, "Call Accepted", Toast.LENGTH_SHORT).show()
            }
            CallNotificationHandler.ACTION_DECLINE_CALL -> {
                // Handle decline call action
                Toast.makeText(context, "Call Declined", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
