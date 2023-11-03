package com.unotag.mokone.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InAppMessageDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(inAppMessageEntity: InAppMessageEntity)

    @Query("SELECT COUNT(*) FROM in_app_messages")
    suspend fun getMessageCount(): Int

    @Query("SELECT * FROM in_app_messages")
    suspend fun getAllInAppMessages(): List<InAppMessageEntity>

    @Query("SELECT * FROM in_app_messages WHERE isSeen = 0 LIMIT :limit")
    suspend fun getUnseenMessages(limit: Int): List<InAppMessageEntity>

    @Query("UPDATE in_app_messages SET isSeen = 1 WHERE inAppMessageId = :id")
    suspend fun markAsSeen(id: String)

    @Query("UPDATE in_app_messages SET isSeen = 0")
    suspend fun resetIsSeenToFalse()

    @Query("DELETE FROM in_app_messages")
    suspend fun deleteAllInAppMessages()
}
