package com.unotag.mokone.inAppMessage.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.unotag.mokone.R
import com.unotag.mokone.inAppMessage.data.InAppMessageItem

class InAppMessageBaseActivity : AppCompatActivity(), OnIAMPopupDismissListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val inAppMessageItemString = intent.getStringExtra("in_app_message_data")

        val inAppMessageItem = Gson().fromJson(inAppMessageItemString, InAppMessageItem::class.java)

        popupDecisionEngine(inAppMessageItem)
    }


    private fun popupDecisionEngine(inAppMessageItem: InAppMessageItem) {
        when (inAppMessageItem.jsonData?.popupConfigs?.templateType) {
            "normal" -> {
                loadIAMWebViewDialog(inAppMessageItem)
            }
            "bottom_sheet" -> {
                loadIAMWebViewBottomSheet()
            }
            "full_page" -> {

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

    override fun onDismiss() {
        // Finish the activity when the fragment is dismissed
        finish()
    }
}