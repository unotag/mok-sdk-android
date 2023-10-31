package com.unotag.mokone.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "in_app_messages")
data class InAppMessageEntity(
    @PrimaryKey
    val inAppMessageId: String = "",
    val inAppMessageAsString: String?,
    val isSeen: Boolean = false
)