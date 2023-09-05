package com.unotag.mokone.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface InAppMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inAppMessageEntity: InAppMessageEntity)

    @Query("SELECT * FROM in_app_messages")
    suspend fun getAllInAppMessages(): List<InAppMessageEntity>

}