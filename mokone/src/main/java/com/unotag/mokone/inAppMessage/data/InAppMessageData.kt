package com.unotag.mokone.inAppMessage.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class InAppMessageData(
    @SerializedName("data")
    val data: List<InAppMessageItem?>?,
    @SerializedName("success")
    val success: Boolean?,
    @SerializedName("total")
    val total: Int?
)

data class InAppMessageItem(
    @SerializedName("in_app_id")
    val inAppId: String?,
    @SerializedName("org_id")
    val orgId: String?,
    @SerializedName("ClientId")
    val clientId: String?,
    @SerializedName("json_data")
    val jsonData: JsonData?,
    @SerializedName("comment")
    val comment: String?, // You can replace with the actual type
    @SerializedName("isActive")
    val isActive: Boolean?,
    @SerializedName("updatedBy")
    val updatedBy: String?, // You can replace with the actual type
    @SerializedName("createdBy")
    val createdBy: String?, // You can replace with the actual type
    @SerializedName("createdAt")
    val createdAt: String?,
    @SerializedName("updatedAt")
    val updatedAt: String?,
    @SerializedName("read")
    val read: Boolean?,
    @SerializedName("type")
    val type: String? // You can replace with the actual type
) : Serializable

data class JsonData(
    @SerializedName("popup_configs")
    val popupConfigs: PopupConfigs?,
    @SerializedName("in_app_click_action")
    val inAppClickAction: String?,
    @SerializedName("text")
    val text: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("image")
    val image: String?, // You can replace with the actual type
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("category")
    val category: String?,
    @SerializedName("html")
    val html: String?,
    ) : Serializable

data class PopupConfigs(
    @SerializedName("sound")
    val sound: String?,
    @SerializedName("template_type")
    val templateType: String?,
    @SerializedName("template_size")
    val templateSize: String?,
    @SerializedName("number_of_times_view")
    val numberOfTimesView: String?,
    @SerializedName("number_of_seconds_view")
    val numberOfSecondsView: String?,
    @SerializedName("web_url")
    val webUrl: String?,
    @SerializedName("video_url")
    val videoUrl: String?,
    @SerializedName("start_time")
    val startTime: String?,
    @SerializedName("end_time")
    val endTime: String?
): Serializable
