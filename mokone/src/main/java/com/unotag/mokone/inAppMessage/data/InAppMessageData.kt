package com.unotag.mokone.inAppMessage.data
import com.unotag.mokone.db.InAppMessageEntity

data class InAppMessageData(
    val id: Long,
    val title: String,
    val body: String,
    val imageUrl: String?,
    val startDate: Long?,
    val endDate: Long?,
    val campaignName: String?,
    val deepLink: String?,
    val viewType: String?,
    val videoUrl: String?,
    val popupHtml : String?,
    val isSeen : Boolean
) {

    // Conversion functions
    fun toEntity(): InAppMessageEntity {
        return InAppMessageEntity(
            title = title,
            body = body,
            imageUrl = imageUrl,
            startDate = startDate,
            endDate = endDate,
            campaignName = campaignName,
            deepLink = deepLink,
            viewType = viewType,
            videoUrl = videoUrl,
            popupHtml =popupHtml,
            isSeen = false
        )
    }

    companion object {
        fun fromEntity(entity: InAppMessageEntity): InAppMessageData {
            return InAppMessageData(
                id = entity.id,
                title = entity.title,
                body = entity.body,
                imageUrl = entity.imageUrl,
                startDate = entity.startDate,
                endDate = entity.endDate,
                campaignName = entity.campaignName,
                deepLink = entity.deepLink,
                viewType = entity.viewType,
                videoUrl = entity.videoUrl,
                popupHtml = entity.popupHtml,
                isSeen = entity.isSeen
            )
        }

        fun fromMap(data: Map<String, String>): InAppMessageData {
            return InAppMessageData(
                id = 0,
                title = data["title"] ?: "",
                body = data["body"] ?: "",
                imageUrl = data["imageUrl"],
                startDate = data["startDate"]?.toLongOrNull(),
                endDate = data["endDate"]?.toLongOrNull(),
                campaignName = data["campaignName"],
                deepLink = data["deepLink"],
                viewType = data["viewType"],
                videoUrl = data["videoUrl"],
                popupHtml = data["popup_html"],
                isSeen = false
            )
        }
    }
}
