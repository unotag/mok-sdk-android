package com.unotag.mokone.inAppMessage.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.unotag.mokone.R
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger


enum class MessageType {
    HTML,
    NORMAL,
    WEB,
    UNKNOWN
}

class InAppMessageBaseActivity() : AppCompatActivity(), OnIAMPopupDismissListener {

    private lateinit var mInAppMessageId: String
    private lateinit var mUserId: String

    private val fullScreenWebViewResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val markAsSeen = data?.getBooleanExtra("mark_as_seen", false)
                markInAppMessageAsRead()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val inAppMessageItemString = intent.getStringExtra("in_app_message_data")

        val inAppMessageItem = Gson().fromJson(inAppMessageItemString, InAppMessageItem::class.java)

        this.mInAppMessageId = inAppMessageItem.inAppId ?: "NA"
        this.mUserId = inAppMessageItem.clientId ?: "NA"

        if (inAppMessageItemString != null) {
            popupTypeDecisionEngine(inAppMessageItemString, inAppMessageItem)
        }
    }


    private fun popupTypeDecisionEngine(
        inAppMessageItemString: String,
        inAppMessageItem: InAppMessageItem
    ) {
        val isHtmlType: Boolean = !inAppMessageItem.jsonData?.html.isNullOrEmpty()
        val isNormalType: Boolean = !inAppMessageItem.jsonData?.title.isNullOrEmpty()
        val isWebSiteType: Boolean =
            !inAppMessageItem.jsonData?.popupConfigs?.webUrl.isNullOrEmpty()

        val messageType = determineMessageType(isHtmlType, isNormalType, isWebSiteType)

        when (messageType) {
            MessageType.HTML, MessageType.NORMAL, MessageType.UNKNOWN -> {
                popupStyleDecisionEngine(
                    inAppMessageItemString,
                    inAppMessageItem
                )
            }
            MessageType.WEB -> {
                launchIAMFullScreenWebViewActivity(inAppMessageItemString)
            }
        }
    }


    private fun determineMessageType(
        isHtmlType: Boolean,
        isNormalType: Boolean,
        isWebSiteType: Boolean
    ): MessageType {
        return when {
            isHtmlType -> MessageType.HTML
            !isHtmlType && isNormalType && isWebSiteType -> MessageType.NORMAL
            !isHtmlType && !isNormalType && isWebSiteType -> MessageType.WEB
            else -> MessageType.UNKNOWN
        }
    }

    private fun popupStyleDecisionEngine(
        inAppMessageItemString: String,
        inAppMessageItem: InAppMessageItem
    ) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> launchIAMWebViewDialog(inAppMessageItem)
            "bottom_sheet" -> launchIAMWebViewBottomSheet(inAppMessageItemString, inAppMessageItem)
            "full_page" -> { }
            "pip_video" -> { }
            else -> launchIAMWebViewDialog(inAppMessageItem)
        }
    }


    private fun launchIAMWebViewDialog(inAppMessageItem: InAppMessageItem) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMWebViewDialog launched")
        val dialog = IAMWebViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            markInAppMessageAsRead()
            finish()
        }
        dialog.show()
    }


    private fun launchIAMWebViewBottomSheet(
        inAppMessageItemString: String,
        inAppMessageItem: InAppMessageItem
    ) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMWebViewBottomSheet launched")
        val iAMWebViewBottomSheetFragment = IAMWebViewBottomSheetFragment.newInstance(inAppMessageItemString)
        iAMWebViewBottomSheetFragment.setOnDismissListener(this)
        iAMWebViewBottomSheetFragment.isCancelable = true
        iAMWebViewBottomSheetFragment.show(
            supportFragmentManager,
            iAMWebViewBottomSheetFragment.tag
        )
    }



    private fun launchIAMFullScreenWebViewActivity(inAppMessageItemString: String) {
        MokLogger.log(MokLogger.LogLevel.INFO, "IAMFullScreenWebViewActivity launched")
        val intent = Intent(this, IAMFullScreenWebViewActivity::class.java)
        intent.putExtra("in_app_message_data", inAppMessageItemString)
        fullScreenWebViewResultLauncher.launch(intent)
    }


    private fun markInAppMessageAsRead() {
        if (mUserId.isNotEmpty()) {
            val inAppMessageHandler = InAppMessageHandler(this, mUserId)
            inAppMessageHandler.markIAMReadInLocalAndServer(mInAppMessageId, null)
        } else {
            MokLogger.log(MokLogger.LogLevel.ERROR, "User Id is null, contact mok team")
        }
    }


    override fun onDismiss() {
        markInAppMessageAsRead()
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}
