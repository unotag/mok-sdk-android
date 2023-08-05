package com.unotag.mokone.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface InAppMessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(inAppMessageEntity: InAppMessageEntity)

}