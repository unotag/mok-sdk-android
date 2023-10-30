package com.unotag.mokone.db
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [InAppMessageEntity::class], version = 1)
@TypeConverters(InAppMessageDataConverter::class)
abstract class MokDb : RoomDatabase() {
    abstract fun inAppMessageDao(): InAppMessageDao
}