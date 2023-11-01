package com.unotag.mokone.inAppMessage

import android.content.Context
import android.content.Intent
import androidx.room.Room
import com.google.gson.Gson
import com.unotag.mokone.db.InAppMessageEntity
import com.unotag.mokone.db.MokDb
import com.unotag.mokone.inAppMessage.data.InAppMessageData
import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivity
import com.unotag.mokone.inAppMessage.ui.InAppMessageBaseActivityFinishListener
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

class InAppMessageHandler(private val context: Context, private val userId: String) :
    InAppMessageBaseActivityFinishListener {


    fun fetchIAMFromServerAndSaveToDB(
        callback: (inAppMessageData: InAppMessageData?, error: String?) -> Unit
    ) {
        val apiCallTask = MokApiCallTask()
        apiCallTask.performApiCall(
            //MokApiConstants.BASE_URL + MokApiConstants.URL_IN_APP_MESSAGE_DATA + "?external_player_id=$userId",
            MokApiConstants.BASE_URL + MokApiConstants.URL_PENDING_IN_APP_MESSAGE + userId,
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
                                saveInAppMessageInLocalDb(inAppMessageId, inAppMessageAsString)
                            }
                        }
                        // Await all the deferred results
                        runBlocking {
                            deferredResults?.awaitAll()
                        }

                        callback(inAppMessageData, null)
                        MokLogger.log(
                            MokLogger.LogLevel.DEBUG,
                            "In-App Message data fetched successfully"
                        )
                    } catch (e: Exception) {
                        MokLogger.log(
                            MokLogger.LogLevel.DEBUG,
                            e.localizedMessage
                        )
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


    private suspend fun saveInAppMessageInLocalDb(
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

    fun markIAMReadInLocalAndServer(
        inAppMessageId: String,
        callback: (inAppMessageData: String?, error: String?) -> Unit
    ) {
        val apiCallTask = MokApiCallTask()
        apiCallTask.performApiCall(
            MokApiConstants.BASE_URL + MokApiConstants.URL_MARK_READ_IN_APP_MESSAGE + userId + "?in_app_id=$inAppMessageId",
            MokApiCallTask.HttpMethod.PATCH,
            MokApiCallTask.MokRequestMethod.WRITE
        ) { result ->
            when (result) {
                is MokApiCallTask.ApiResult.Success -> {
                    val response = result.response
                    try {
                        callback(response.toString(), null)

                        markIAMAsSeenLocally(inAppMessageId)

                        MokLogger.log(
                            MokLogger.LogLevel.DEBUG,
                            "In-App Message mark as read successfully"
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


    private fun markIAMAsSeenLocally(inAppMessageId: String): Deferred<Unit> {
        return CoroutineScope(Dispatchers.IO).async {
            val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()

            try {
                val inAppMessageDao = db.inAppMessageDao()
                inAppMessageDao.markAsSeen(inAppMessageId)
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR,
                    "Failed to mark in-app message as seen in the local database: ${e.message}"
                )
            } finally {
                //db.close()
            }
        }
    }


    private suspend fun fetchIAMEntry(limit: Int): List<InAppMessageEntity>? {
        val db = Room.databaseBuilder(context, MokDb::class.java, "mok-database").build()

        return try {
            val inAppMessageDao = db.inAppMessageDao()
            inAppMessageDao.getMessages(limit)
        } catch (e: Exception) {
            MokLogger.log(
                MokLogger.LogLevel.ERROR,
                "Error fetching latest in-app message entry from the database: ${e.message}"
            )
            null
        } finally {
            // db.close()
        }
    }

    private fun launchIAMBaseActivity(inAppMessageItem: String) {
        val intent = Intent(context, InAppMessageBaseActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("in_app_message_data", inAppMessageItem)
        context.startActivity(intent)
    }

    fun showInAppMessages(limit: Int? = 1) {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        coroutineScope.launch {
            try {
                val inAppMessageEntries = fetchIAMEntry(limit ?: 1)
                if (inAppMessageEntries != null) {
                    withContext(Dispatchers.Main) {
                        for (inAppMessageEntry in inAppMessageEntries) {
                            if (inAppMessageEntry.inAppMessageAsString != null) {
                                val inAppMessageActivity = InAppMessageBaseActivity()
//                                inAppMessageActivity.setFinishListener(this)
//                                launchIAMBaseActivity(inAppMessageEntry.inAppMessageAsString)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                MokLogger.log(
                    MokLogger.LogLevel.ERROR,
                    "Error showing in-app message dialog: ${e.message}"
                )
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

    override fun onInAppMessageClosed(inAppMessageId: String) {
        markIAMReadInLocalAndServer(
            inAppMessageId
        ) { inAppMessageData: String?, errorMessage: String? ->
        }
    }

}
