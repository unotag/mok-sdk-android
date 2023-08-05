package com.unotag.mokone.db
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [InAppMessageEntity::class], version = 1)
abstract class MokDb : RoomDatabase() {
    abstract fun inAppMessageDao(): InAppMessageDao
}
