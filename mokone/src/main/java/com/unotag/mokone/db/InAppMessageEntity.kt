package com.unotag.mokone.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "in_app_messages")
data class InAppMessageEntity(
    val title: String,
    val body: String,
    val imageUrl: String?,
    val startDate: Long?,
    val endDate: Long?,
    val campaignName: String?,
    val deepLink: String?,
    val viewType: String?,
    val videoUrl: String?,

) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
}
