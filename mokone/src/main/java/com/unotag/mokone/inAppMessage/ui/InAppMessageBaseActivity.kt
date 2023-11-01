package com.unotag.mokone.inAppMessage.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.unotag.mokone.R
import com.unotag.mokone.inAppMessage.data.InAppMessageItem

class InAppMessageBaseActivity() : AppCompatActivity() {


    private lateinit var  mInAppMessageId : String
    private lateinit var inAppMessageBaseActivityFinishListener : InAppMessageBaseActivityFinishListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val inAppMessageItemString = intent.getStringExtra("in_app_message_data")

        val inAppMessageItem = Gson().fromJson(inAppMessageItemString, InAppMessageItem::class.java)

        this.mInAppMessageId = inAppMessageItem.inAppId ?: "NA"

        popupDecisionEngine(inAppMessageItem)
    }

    fun setFinishListener(listener: InAppMessageBaseActivityFinishListener) {
        inAppMessageBaseActivityFinishListener = listener
    }


    private fun popupDecisionEngine(inAppMessageItem: InAppMessageItem) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> {
                //TODO: create title, image, body popup
                loadIAMWebViewDialog(inAppMessageItem)
            }
            "bottom_sheet" -> {
                //TODO: create title, image, body popup
                loadIAMWebViewBottomSheet()
            }
            "full_page" -> {
                //TODO: create full screen frag which acc web url

            }
            "pip_video" -> {

            }
            else -> {
                loadIAMWebViewDialog(inAppMessageItem)
            }
        }
    }

    private fun loadIAMWebViewDialog(inAppMessageItem: InAppMessageItem) {
        val dialog = IAMWebViewDialog(this, inAppMessageItem)
        dialog.setOnDismissListener {
            finish()
        }
        dialog.show()
    }

    private fun loadIAMWebViewBottomSheet() {
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

}


interface InAppMessageBaseActivityFinishListener{
   fun onInAppMessageClosed(inAppMessageId: String)
}
