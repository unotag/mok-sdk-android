package com.unotag.mokone.inAppMessage

import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.unotag.mokone.db.InAppMessageEntity
import com.unotag.mokone.db.MokDb
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivity
import com.unotag.mokone.utils.MokLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppMessageHandler(private val context: Context) {

    fun showInAppMessageDialog(message: String) {
        MokLogger.log(
            MokLogger.LogLevel.DEBUG,
            "showInAppMessageDialog called with message: $message"
        )
        val intent = Intent(context, InAppMessageBaseActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("message_key", message)
        context.startActivity(intent)
    }

    fun readSavedInAppMessage() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val inAppMessageDao = db.inAppMessageDao()
                val savedInAppMessages: List<InAppMessageEntity> = inAppMessageDao.getAllInAppMessages()

                if (savedInAppMessages.isNotEmpty()) {
                    MokLogger.log(
                        MokLogger.LogLevel.DEBUG, "Message count size:${savedInAppMessages.size}"
                    )
                    // Process the list of saved in-app messages as needed
                    for (inAppMessageEntity in savedInAppMessages) {
                        // Convert InAppMessageEntity to com.unotag.mokone.inAppMessage.data.InAppMessageData and perform actions
                        val inAppMessageData = InAppMessageData.fromEntity(inAppMessageEntity)

                        try {
                            inAppMessageData.popupHtml?.let { showInAppMessageDialog(it) }
                        } catch (e: Exception) {
                            MokLogger.log(
                                MokLogger.LogLevel.ERROR, "Error showing in-app message dialog: ${e.message}"
                            )
                        }
                    }
                    // Call the method to show the in-app message dialog
                }
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR, "Error reading saved in-app messages: ${e.message}"
                )
            } finally {
                db.close() // Close the database when done
            }
        }
    }

    fun readTopSavedInAppMessage() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val inAppMessageDao = db.inAppMessageDao()
                val savedInAppMessages: List<InAppMessageEntity> = inAppMessageDao.getAllInAppMessages()

                if (savedInAppMessages.isNotEmpty()) {
                    MokLogger.log(
                        MokLogger.LogLevel.DEBUG, "Message count size:${savedInAppMessages.size}"
                    )

                    // Retrieve the top (first) in-app message from the list
                    val topMessage = savedInAppMessages.last()

                    // Convert InAppMessageEntity to com.unotag.mokone.inAppMessage.data.InAppMessageData and perform actions
                    val inAppMessageData = InAppMessageData.fromEntity(topMessage)

                    try {
                        inAppMessageData.popupHtml?.let { showInAppMessageDialog(it) }
                    } catch (e: Exception) {
                        MokLogger.log(
                            MokLogger.LogLevel.ERROR, "Error showing in-app message dialog: ${e.message}"
                        )
                    }

                    // Uncomment the following lines if you want to mark the message as seen
                    // if (!inAppMessageData.isSeen) {
                    //     inAppMessageDao.markAsSeen(inAppMessageData.id)
                    // }

                    // Call the method to show the in-app message dialog
                }
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR, "Error reading top saved in-app message: ${e.message}"
                )
            } finally {
                db.close() // Close the database when done
            }
        }
    }

    fun deleteAllInAppMessages() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val inAppMessageDao = db.inAppMessageDao()
            inAppMessageDao.deleteAllInAppMessages()
        }
        MokLogger.log(MokLogger.LogLevel.INFO, "All in app messages deleted successfully")
    }

    fun resetIsSeenToUnSeen() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            val inAppMessageDao = db.inAppMessageDao()
            inAppMessageDao.resetIsSeenToFalse()
        }
        MokLogger.log(
            MokLogger.LogLevel.INFO,
            "All in app messages resetIsSeenToUnSeen successfully"
        )
    }

    suspend fun getIAMCount(): String {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()

        return withContext(Dispatchers.IO) {
            val inAppMessageDao = db.inAppMessageDao()
            val count = inAppMessageDao.getMessageCount()
            MokLogger.log(MokLogger.LogLevel.INFO, "IAM count: $count")
            count.toString() // Convert count to a String and return it
        }
    }
}
