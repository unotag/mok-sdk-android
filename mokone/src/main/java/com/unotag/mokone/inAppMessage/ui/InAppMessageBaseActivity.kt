package com.unotag.mokone.inAppMessage.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.unotag.mokone.R
import com.unotag.mokone.inAppMessage.InAppMessageHandler
import com.unotag.mokone.inAppMessage.data.InAppMessageItem

class InAppMessageBaseActivity() : AppCompatActivity(), OnIAMPopupDismissListener {

    private lateinit var mInAppMessageId: String

    private val fullScreenWebViewResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val userId = data?.getStringExtra("user_id")
                val markAsSeen = data?.getBooleanExtra("mark_as_seen", false)
                markInAppMessageAsRead(userId)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val inAppMessageItemString = intent.getStringExtra("in_app_message_data")

        val inAppMessageItem = Gson().fromJson(inAppMessageItemString, InAppMessageItem::class.java)

        this.mInAppMessageId = inAppMessageItem.inAppId ?: "NA"

        //popupDecisionEngine(inAppMessageItem)
        launchIAMFullScreenWebViewActivity(inAppMessageItemString!!)

    }


    private fun popupDecisionEngine(inAppMessageItemString: String, inAppMessageItem: InAppMessageItem) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> {
                //TODO: create title, image, body popup
                launchIAMWebViewDialog(inAppMessageItem)
            }

            "bottom_sheet" -> {
                //TODO: create title, image, body popup
                launchIAMWebViewBottomSheet(inAppMessageItemString,inAppMessageItem)
            }

            "full_page" -> {
                launchIAMFullScreenWebViewActivity(inAppMessageItemString)
            }

            "pip_video" -> {
            }

            else -> {
                launchIAMWebViewDialog(inAppMessageItem)
            }
        }
    }

    private fun launchIAMWebViewDialog(inAppMessageItem: InAppMessageItem) {
        val dialog = IAMWebViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            markInAppMessageAsRead(inAppMessageItem.clientId)
            finish()
        }
        dialog.show()
    }


    private fun launchIAMWebViewBottomSheet(inAppMessageItemString: String, inAppMessageItem: InAppMessageItem) {
        val iAMWebViewBottomSheetFragment = IAMWebViewBottomSheetFragment()

        // Set the listener for the fragment
        iAMWebViewBottomSheetFragment.setOnDismissListener(this)

        // Prevent dismissal when clicked outside
        iAMWebViewBottomSheetFragment.isCancelable = false

        iAMWebViewBottomSheetFragment.show(
            supportFragmentManager,
            iAMWebViewBottomSheetFragment.tag
        )
    }

    private fun launchIAMFullScreenWebViewActivity(inAppMessageItemString: String) {
        val intent = Intent(this, IAMFullScreenWebViewActivity::class.java)
        intent.putExtra("in_app_message_data", inAppMessageItemString)
        fullScreenWebViewResultLauncher.launch(intent)
    }


    private fun markInAppMessageAsRead(userId: String?) {
        if (userId != null) {
            val inAppMessageHandler = InAppMessageHandler(this, userId)
            inAppMessageHandler.markIAMReadInLocalAndServer(mInAppMessageId, null)
        }
    }


    override fun onDismiss() {
        // TODO("Not yet implemented")
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}


interface InAppMessageBaseActivityFinishListener {
    fun onInAppMessageClosed(inAppMessageId: String)
}
