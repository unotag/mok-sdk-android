package com.unotag.mokone.pushNotification.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.unotag.mokone.R
import com.unotag.mokone.pushNotification.NotificationRenderer
import com.unotag.mokone.utils.MokLogger
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MokFirebaseMessagingService : FirebaseMessagingService() {

//    private var bigLargeIcon: Bitmap? = null
//    private var bigPicture: Bitmap? = null


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        MokLogger.log(MokLogger.LogLevel.DEBUG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            MokLogger.log(MokLogger.LogLevel.DEBUG, "Message data payload: ${remoteMessage.data}")
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]

            if (title.isNullOrEmpty()) {
                   MokLogger.log(MokLogger.LogLevel.DEBUG, "THIS IS A IN APP MESG")

                remoteMessage.data["get_in_app_msg_data"]

            } else if (remoteMessage.data.containsKey("image")) {
                val image = remoteMessage.data["image"]
                val bigIcon = remoteMessage.data["icon"]
                val bigPicture = getBitmapFromUrl(image)
                val bigLargeIcon: Bitmap? = getBitmapFromUrl(bigIcon)
                sendNotification(title, body, bigPicture, bigLargeIcon)
            } else {
                sendNotification(title, body)
            }

            // Check if data needs to be processed by long running job
            //if (isLongRunningJob()) {
            // For long-running tasks (10 seconds or more) use WorkManager.
            //   scheduleJob()
//            else {
//                // Handle message within 10 seconds
//                handleNow()
//            }
        }

        // Check if message contains a notification payload.
//        remoteMessage.notification?.let {
//            MokLogger.log(MokLogger.LogLevel.DEBUG, "Message Notification Body: ${it.body}")
//            it.body?.let { body -> sendNotification() }
//        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    private fun isLongRunningJob() = true

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        MokLogger.log(MokLogger.LogLevel.DEBUG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
//        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
//        WorkManager.getInstance(this).beginWith(work).enqueue()
//        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        MokLogger.log(MokLogger.LogLevel.DEBUG, "Short lived task is done.")
        //   sendNotification()
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        MokLogger.log(MokLogger.LogLevel.DEBUG, "sendRegistrationTokenToServer($token)")
    }

    private fun sendNotification(
        title: String?,
        messageBody: String?,
        bigPicture: Bitmap? = null,
        bigLargeIcon: Bitmap? = null
    ) {
        val requestCode = 0
        val launchIntent = getLaunchIntent()
        val pendingIntent = getPendingIntent(launchIntent, requestCode)
        val channelId = getString(R.string.default_notification_channel_id)
        val notificationBuilder = buildNotification(title, messageBody, bigPicture, bigLargeIcon, channelId, pendingIntent)
        notify(notificationBuilder)
    }

    private fun getLaunchIntent(): Intent? {
        val launchIntent = applicationContext.packageManager.getLaunchIntentForPackage(applicationContext.packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return launchIntent
    }

    private fun getPendingIntent(intent: Intent?, requestCode: Int): PendingIntent? {
        return PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun buildNotification(
        title: String?,
        messageBody: String?,
        bigPicture: Bitmap?,
        bigLargeIcon: Bitmap?,
        channelId: String,
        pendingIntent: PendingIntent?
    ): NotificationCompat.Builder {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        return NotificationCompat.Builder(this, channelId)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bigPicture)
                    .bigLargeIcon(bigLargeIcon)
            )
            .setSmallIcon(NotificationRenderer.getSmallNotificationIcon())
            .setContentTitle(title)
            .setContentText(messageBody)
            .setLargeIcon(bigLargeIcon)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
    }

    private fun notify(notificationBuilder: NotificationCompat.Builder) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = notificationBuilder.build().channelId
            val channel = NotificationChannel(
                channelId, "Default Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }


    private fun getBitmapFromUrl(imageUrl: String?): Bitmap? {
        return try {
            val url = URL(imageUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: Exception) {
            MokLogger.log(
                MokLogger.LogLevel.ERROR,
                "Error in getting notification image: " + e.localizedMessage
            )
            null
        }
    }


    private fun handleInAppNotification(data: Map<String, String>) {
        try {
            //val inAppMessageData = InAppMessageData.fromMap(data)

//            val db = Room.databaseBuilder(applicationContext, MokDb::class.java, "mok-database").build()
//            val scope = CoroutineScope(Dispatchers.IO)
//
//            scope.launch {
//                try {
//                    val inAppMessageDao = db.inAppMessageDao()
//                    val inAppMessageEntity = inAppMessageData.toEntity()
//                    inAppMessageDao.insert(inAppMessageEntity)
//                } catch (e: Exception) {
//                    MokLogger.log(MokLogger.LogLevel.ERROR, "Error inserting in-app message: ${e.message}")
//                } finally {
//                    db.close() // Close the database when done
//                }
//            }
        } catch (e: Exception) {
            MokLogger.log(
                MokLogger.LogLevel.ERROR,
                "Error handling in-app notification: ${e.message}"
            )
        }
    }

}