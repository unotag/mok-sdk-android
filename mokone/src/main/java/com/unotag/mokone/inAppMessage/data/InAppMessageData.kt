import androidx.room.Ignore
import androidx.room.Entity
import com.unotag.mokone.db.InAppMessageEntity

data class InAppMessageData(
    val title: String,
    val body: String,
    val imageUrl: String?,
    val startDate: Long?,
    val endDate: Long?,
    val campaignName: String?,
    val deepLink: String?,
    val viewType: String?,
    val videoUrl: String?
) {
    // This annotation ignores the field during Room database operations
    @Ignore
    val id: Long = 0

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
            videoUrl = videoUrl
        )
    }

    companion object {
        fun fromEntity(entity: InAppMessageEntity): InAppMessageData {
            return InAppMessageData(
                title = entity.title,
                body = entity.body,
                imageUrl = entity.imageUrl,
                startDate = entity.startDate,
                endDate = entity.endDate,
                campaignName = entity.campaignName,
                deepLink = entity.deepLink,
                viewType = entity.viewType,
                videoUrl = entity.videoUrl
            )
        }

        fun fromMap(data: Map<String, String>): InAppMessageData {
            return InAppMessageData(
                title = data["title"] ?: "",
                body = data["body"] ?: "",
                imageUrl = data["imageUrl"],
                startDate = data["startDate"]?.toLongOrNull(),
                endDate = data["endDate"]?.toLongOrNull(),
                campaignName = data["campaignName"],
                deepLink = data["deepLink"],
                viewType = data["viewType"],
                videoUrl = data["videoUrl"]
            )
        }
    }
}
