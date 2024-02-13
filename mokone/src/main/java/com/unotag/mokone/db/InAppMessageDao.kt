package com.unotag.mokone.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unotag.mokone.utils.MokLogger

@Dao
interface InAppMessageDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inAppMessageEntity: InAppMessageEntity) {
        val existingMessage =
            getAllInAppMessages().find { it.inAppMessageId == inAppMessageEntity.inAppMessageId }
        if (existingMessage != null) {
            // Handle conflict: Update isSeen to 0 and re-insert
            existingMessage.isSeen = false
            update(existingMessage)
            MokLogger.log(MokLogger.LogLevel.DEBUG, "UPDATED : IAM updated in local DB")
        } else {
            // No conflict, perform a regular insert
            actualInsert(inAppMessageEntity)
            MokLogger.log(MokLogger.LogLevel.DEBUG, " INSERT: IAM inserted in local DB")
        }
    }

    @Insert
    suspend fun actualInsert(inAppMessageEntity: InAppMessageEntity)

    @Update
    suspend fun update(inAppMessageEntity: InAppMessageEntity)

    @Query("SELECT COUNT(*) FROM in_app_messages")
    suspend fun getMessageCount(): Int

    @Query("SELECT * FROM in_app_messages")
    suspend fun getAllInAppMessages(): List<InAppMessageEntity>

    @Query("SELECT * FROM in_app_messages WHERE isSeen = 0 LIMIT :limit")
    suspend fun getUnseenMessages(limit: Int): List<InAppMessageEntity>

    @Query("DELETE FROM in_app_messages WHERE inAppMessageId = :id")
    suspend fun deleteInAppMessage(id: String)

    @Query("DELETE FROM in_app_messages")
    suspend fun deleteAllInAppMessages()
}
