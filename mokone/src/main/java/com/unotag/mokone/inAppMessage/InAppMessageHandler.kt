package com.unotag.mokone.inAppMessage

import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.google.gson.Gson
import com.unotag.mokone.db.InAppMessageEntity
import com.unotag.mokone.db.MokDb
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivity
import com.unotag.mokone.network.MokApiCallTask
import com.unotag.mokone.network.MokApiConstants
import com.unotag.mokone.utils.MokLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class InAppMessageHandler(private val context: Context) {


    fun fetchIAMFromServerAndSaveToDB(
        userId: String,
        callback: (inAppMessageData: InAppMessageData?, error: String?) -> Unit
    ) {
        val apiCallTask = MokApiCallTask()
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_IN_APP_MESSAGE_DATA + "?external_player_id=$userId",
            MokApiCallTask.HttpMethod.GET,
            MokApiCallTask.MokRequestMethod.READ
        ) { result ->
            when (result) {
                is MokApiCallTask.ApiResult.Success -> {
                    val response = result.response
                    try {
                        val gson = Gson()
                        val inAppMessageData =
                            gson.fromJson(response.toString(), InAppMessageData::class.java)

                        // Use async to save each in-app message sequentially
                        val deferredResults = inAppMessageData.data?.map { inAppMessage ->
                            val inAppMessageId = inAppMessage?.inAppId
                            val inAppMessageAsString = Gson().toJson(inAppMessage)
                            CoroutineScope(Dispatchers.IO).async {
                                saveInAppMessageDataToRoom(inAppMessageId, inAppMessageAsString)
                            }
                        }
                        // Await all the deferred results
                        runBlocking {
                            deferredResults?.awaitAll()
                        }

                        callback(inAppMessageData, null)
                        MokLogger.log(
                            MokLogger.LogLevel.DEBUG,
                            "In-App Message data fetched and saved to Room successfully"
                        )
                    } catch (e: Exception) {
                        callback(null, "Failed to parse the API response")
                    }
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

    private suspend fun saveInAppMessageDataToRoom(
        inAppMessageId: String?,
        inAppMessageAsString: String?
    ): Deferred<Unit> {
        return CoroutineScope(Dispatchers.IO).async {
            val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()

            try {
                val inAppMessageEntity =
                    InAppMessageEntity(inAppMessageId ?: "", inAppMessageAsString)
                val inAppMessageDao = db.inAppMessageDao()
                inAppMessageDao.insert(inAppMessageEntity)
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR,
                    "Error inserting in-app message: ${e.message}"
                )
            } finally {
                //db.close()
            }
        }
    }


    private fun fetchLatestIAMEntry(): InAppMessageItem? {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()

        try {
            val inAppMessageDao = db.inAppMessageDao()
            val latestMessage = inAppMessageDao.getLatestMessage()

            return Gson().fromJson(
                latestMessage?.inAppMessageAsString,
                InAppMessageItem::class.java
            )
        } catch (e: Exception) {
            MokLogger.log(
                MokLogger.LogLevel.ERROR,
                "Error fetching latest in-app message entry from the database: ${e.message}"
            )
            return null
        } finally {
            db.close()
        }
    }

    private fun showIAMWebViewDialog(message: InAppMessageItem) {
        MokLogger.log(
            MokLogger.LogLevel.DEBUG,
            "showInAppMessageDialog msg : ${message.jsonData?.title}"
        )

        val intent = Intent(context, InAppMessageBaseActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("message_key", message.jsonData?.title)
        context.startActivity(intent)
    }


    fun showIAM() {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val inAppMessageItem = fetchLatestIAMEntry()
                withContext(Dispatchers.Main) {
                    if (inAppMessageItem != null)
                        showIAMWebViewDialog(inAppMessageItem)
                }
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR,
                    "Error showing in-app message dialog: ${e.message}"
                )
            }
        }
    }


    fun readTopSavedInAppMessage() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                val inAppMessageDao = db.inAppMessageDao()
                val savedInAppMessages: List<InAppMessageEntity> =
                    inAppMessageDao.getAllInAppMessages()

                if (savedInAppMessages.isNotEmpty()) {
                    MokLogger.log(
                        MokLogger.LogLevel.DEBUG, "Message count size:${savedInAppMessages.size}"
                    )

                    // Retrieve the top (first) in-app message from the list
                    val topMessage = savedInAppMessages.last()

                    // Convert InAppMessageEntity to com.unotag.mokone.inAppMessage.data.InAppMessageData and perform actions
                    //val inAppMessageData = InAppMessageData.fromEntity(topMessage)
                    try {
                        //  inAppMessageData.popupHtml?.let { showInAppMessageDialog(it) }
                    } catch (e: Exception) {
                        MokLogger.log(
                            MokLogger.LogLevel.ERROR,
                            "Error showing in-app message dialog: ${e.message}"
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
                //db.close() // Close the database when done
            }
        }
    }

    suspend fun deleteAllInAppMessages() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        try {
            val inAppMessageDao = db.inAppMessageDao()
            inAppMessageDao.deleteAllInAppMessages()
            MokLogger.log(MokLogger.LogLevel.INFO, "All in app messages deleted successfully")
        } catch (e: Exception) {
            MokLogger.log(
                MokLogger.LogLevel.ERROR, "Error deleting all in-app messages: ${e.message}"
            )
        } finally {
            db.close()
        }
    }

    suspend fun resetIsSeenToUnSeen() {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()
        try {
            val inAppMessageDao = db.inAppMessageDao()
            //inAppMessageDao.resetIsSeenToFalse()
            MokLogger.log(
                MokLogger.LogLevel.INFO,
                "All in app messages resetIsSeenToUnSeen successfully"
            )
        } catch (e: Exception) {
            MokLogger.log(
                MokLogger.LogLevel.ERROR, "Error resetting isSeen to unSeen: ${e.message}"
            )
        } finally {
            db.close()
        }
    }

    suspend fun getIAMCount(): String {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()

        return try {
            val inAppMessageDao = db.inAppMessageDao()
            val count = inAppMessageDao.getMessageCount()
            MokLogger.log(MokLogger.LogLevel.INFO, "IAM count: $count")
            count.toString()
        } catch (e: Exception) {
            MokLogger.log(MokLogger.LogLevel.ERROR, "Error getting IAM count: ${e.message}")
            ""
        } finally {
            db.close()
        }
    }

}
