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
    WEB,
    UNKNOWN
}

class InAppMessageBaseActivity() : AppCompatActivity(), OnIAMPopupDismissListener,
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
        val hasWebSiteContent: Boolean =
            !inAppMessageItem.jsonData?.popupConfigs?.webUrl.isNullOrEmpty()

        val messageType = determineMessageType(hasRawHtmlContent, hasTextContent, hasWebSiteContent)

        when (messageType) {
            MessageType.HTML -> {
                iAMWebViewTypeDecisionEngine(inAppMessageItem)
            }

            MessageType.TEXT -> {
                iAMTextViewTypeDecisionEngine(inAppMessageItem)
            }

            MessageType.WEB -> {
                launchIAMFullScreenWebViewFragment(inAppMessageItem)
            }

            MessageType.UNKNOWN -> {
                MokLogger.log(MokLogger.LogLevel.DEBUG, "IAM type is UNKNOWN")
            }
        }
    }

    private fun determineMessageType(
        hasRawHtml: Boolean,
        hasText: Boolean,
        hasWebSite: Boolean
    ): MessageType {
        return when {
            hasRawHtml -> MessageType.HTML
            hasText && !hasWebSite -> MessageType.TEXT
            !hasText && hasWebSite -> MessageType.WEB
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
            "normal" -> {}
            "bottom_sheet" -> launchIAMBottomSheet(inAppMessageItem)
            "full_page" -> {}
            "pip_video" -> {}
            else -> launchIAMBottomSheet(inAppMessageItem)
        }
    }


    private fun launchIAMWebViewDialog(inAppMessageItem: InAppMessageItem) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMWebViewDialog launched")
        val dialog = IAMWebViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            markIAMAsReadAndCloseActivity()
        }
        dialog.show()
    }


    private fun launchIAMWebViewBottomSheet(
        inAppMessageItem: InAppMessageItem
    ) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMWebViewBottomSheet launched")
        val iAMWebViewBottomSheetFragment =
            IAMWebViewBottomSheetFragment.newInstance(inAppMessageItem)
        iAMWebViewBottomSheetFragment.setOnDismissListener(this)
        iAMWebViewBottomSheetFragment.isCancelable = true
        iAMWebViewBottomSheetFragment.show(
            supportFragmentManager,
            iAMWebViewBottomSheetFragment.tag
        )
    }

    private fun launchIAMBottomSheet(
        inAppMessageItem: InAppMessageItem
    ) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMBottomSheet launched")
        val iAMBottomSheetFragment =
            IAMBottomSheetFragment.newInstance(inAppMessageItem)
        iAMBottomSheetFragment.setOnDismissListener(this)
        iAMBottomSheetFragment.isCancelable = true
        iAMBottomSheetFragment.show(
            supportFragmentManager,
            iAMBottomSheetFragment.tag
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
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "User Id is null, contact mok team")
        }
    }

    private fun markIAMAsReadAndCloseActivity() {
        markInAppMessageAsRead()
        finish()
    }

    override fun onDismiss() {
        markIAMAsReadAndCloseActivity()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onFullScreenWebViewClosed() {
        markIAMAsReadAndCloseActivity()
    }


}
