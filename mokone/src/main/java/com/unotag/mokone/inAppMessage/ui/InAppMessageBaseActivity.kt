package com.unotag.mokone.inAppMessage.ui

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unotag.mokone.R
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger


enum class MessageType {
    HTML,
    TEXT,
    IMAGE,
    WEB,
    UNKNOWN
}

class InAppMessageBaseActivity : AppCompatActivity(), IAMTextViewBottomSheetDismissListener,
    IAMImageViewBottomSheetDismissListener, IAMWebViewBottomSheetDismissListener,
FullScreenWebViewClosedListener {

    private lateinit var mInAppMessageId: String
    private lateinit var mUserId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val inAppMessageItem: InAppMessageItem? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("in_app_message_data", InAppMessageItem::class.java)
            } else {
                intent.getSerializableExtra("in_app_message_data") as InAppMessageItem
            }

        this.mInAppMessageId = inAppMessageItem?.inAppId ?: "NA"
        this.mUserId = inAppMessageItem?.clientId ?: "NA"

        if (inAppMessageItem != null) {
            iAMContentTypeDecisionEngine(inAppMessageItem)
        }
    }


    private fun iAMContentTypeDecisionEngine(inAppMessageItem: InAppMessageItem) {
        val hasRawHtmlContent: Boolean = !inAppMessageItem.jsonData?.html.isNullOrEmpty()
        val hasTextContent: Boolean = !inAppMessageItem.jsonData?.title.isNullOrEmpty()
        val hasImageContent: Boolean = !inAppMessageItem.jsonData?.image.isNullOrEmpty()
        val hasWebSiteContent: Boolean = !inAppMessageItem.jsonData?.popupConfigs?.webUrl.isNullOrEmpty()

        val messageType = determineMessageType(
            hasRawHtmlContent,
            hasTextContent,
            hasImageContent,
            hasWebSiteContent
        )

        when (messageType) {
            MessageType.HTML -> {
                iAMWebViewTypeDecisionEngine(inAppMessageItem)
            }

            MessageType.TEXT -> {
                iAMTextViewTypeDecisionEngine(inAppMessageItem)
            }

            MessageType.IMAGE -> {
                iAMImageViewTypeDecisionEngine(inAppMessageItem)
            }

            MessageType.WEB -> {
                launchIAMFullScreenWebViewFragment(inAppMessageItem)
            }

            MessageType.UNKNOWN -> {
                finish()
                MokLogger.log(MokLogger.LogLevel.DEBUG, "IAM type is UNKNOWN")
            }
        }
    }

    private fun determineMessageType(
        hasRawHtml: Boolean,
        hasText: Boolean,
        hasImage: Boolean,
        hasWebSite: Boolean,
    ): MessageType {
        return when {
            hasRawHtml -> MessageType.HTML
            hasText -> MessageType.TEXT
            hasImage -> MessageType.IMAGE
            hasWebSite -> MessageType.WEB
            else -> MessageType.UNKNOWN
        }
    }

    private fun iAMWebViewTypeDecisionEngine(
        inAppMessageItem: InAppMessageItem,
    ) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> launchIAMWebViewDialog(inAppMessageItem)
            "bottom_sheet" -> launchIAMWebViewBottomSheet(inAppMessageItem)
            "full_page" -> {}
            "pip_video" -> {}
            else -> launchIAMWebViewDialog(inAppMessageItem)
        }
    }

    private fun iAMTextViewTypeDecisionEngine(
        inAppMessageItem: InAppMessageItem,
    ) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> launchIAMTextViewDialog(inAppMessageItem)
            "bottom_sheet" -> launchIAMTextViewBottomSheet(inAppMessageItem)
            "full_page" -> {}
            "pip_video" -> {}
            else -> launchIAMTextViewDialog(inAppMessageItem)
        }
    }

    private fun iAMImageViewTypeDecisionEngine(
        inAppMessageItem: InAppMessageItem,
    ) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> launchIAMImageViewDialog(inAppMessageItem)
            "bottom_sheet" -> launchIAMImageViewBottomSheet(inAppMessageItem)
            "full_page" -> {}
            "pip_video" -> {}
            else -> launchIAMTextViewBottomSheet(inAppMessageItem)
        }
    }


    private fun launchIAMWebViewDialog(inAppMessageItem: InAppMessageItem) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAM WebView Dialog launched")
        val dialog = IAMWebViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            markIAMAsReadAndCloseActivity()
        }
        dialog.show()
    }

    private fun launchIAMTextViewDialog(inAppMessageItem: InAppMessageItem) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAM TextView Dialog launched")
        val dialog = IAMTextViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            markIAMAsReadAndCloseActivity()
        }
        dialog.show()
    }

    private fun launchIAMImageViewDialog(inAppMessageItem: InAppMessageItem) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAM ImageView Dialog launched")
        val dialog = IAMImageViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            markIAMAsReadAndCloseActivity()
        }
        dialog.show()
    }


    private fun launchIAMWebViewBottomSheet(
        inAppMessageItem: InAppMessageItem
    ) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAM WebView BottomSheet launched")
        val iAMWebViewBottomSheetFragment =
            IAMWebViewBottomSheetFragment.newInstance(inAppMessageItem)
        iAMWebViewBottomSheetFragment.setIAMWebViewBottomSheetDismissListener(this)
        iAMWebViewBottomSheetFragment.isCancelable = false
        iAMWebViewBottomSheetFragment.show(
            supportFragmentManager,
            iAMWebViewBottomSheetFragment.tag
        )
    }

    private fun launchIAMTextViewBottomSheet(
        inAppMessageItem: InAppMessageItem
    ) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAM TextView BottomSheet launched")
        val iAMTextViewBottomSheetFragment =
            IAMTextViewBottomSheetFragment.newInstance(inAppMessageItem)
        iAMTextViewBottomSheetFragment.setIAMTextViewBottomSheetDismissListener(this)
        iAMTextViewBottomSheetFragment.isCancelable = false
        iAMTextViewBottomSheetFragment.show(
            supportFragmentManager,
            iAMTextViewBottomSheetFragment.tag
        )
    }

    private fun launchIAMImageViewBottomSheet(
        inAppMessageItem: InAppMessageItem
    ) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAM ImageView BottomSheet launched")
        val iAMImageViewBottomSheetFragment =
            IAMImageViewBottomSheetFragment.newInstance(inAppMessageItem)
        iAMImageViewBottomSheetFragment.setIAMImageViewBottomSheetDismissListener(this)
        iAMImageViewBottomSheetFragment.isCancelable = false
        iAMImageViewBottomSheetFragment.show(
            supportFragmentManager,
            iAMImageViewBottomSheetFragment.tag
        )
    }



    private fun launchIAMFullScreenWebViewFragment(inAppMessageItem: InAppMessageItem) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMFullScreenWebViewFragment launched")

        val fragment = IAMFullScreenWebViewFragment()
        fragment.setFullScreenWebViewClosedListener(this)
        val args = Bundle().apply {
            putSerializable("in_app_message_data", inAppMessageItem)
        }
        fragment.arguments = args
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun markInAppMessageAsRead() {
        if (mUserId.isNotEmpty()) {
            val inAppMessageHandler = InAppMessageHandler(this, mUserId)
            inAppMessageHandler.markIAMReadInLocalAndServer(mInAppMessageId, null)
            inAppMessageHandler.markIAMAsSeenLocally(mInAppMessageId)
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "User Id is null, contact mok team")
        }
    }

    private fun markIAMAsReadAndCloseActivity() {
        finish()
        markInAppMessageAsRead()
    }


    override fun onFullScreenWebViewClosed() {
        markIAMAsReadAndCloseActivity()
    }

    override fun onIAMImageViewBottomSheetDismiss() {
        markIAMAsReadAndCloseActivity()
    }

    override fun onIAMTextViewBottomSheetDismiss() {
        markIAMAsReadAndCloseActivity()
    }

    override fun onIAMWebViewBottomSheetDismiss() {
        markIAMAsReadAndCloseActivity()
    }
}
