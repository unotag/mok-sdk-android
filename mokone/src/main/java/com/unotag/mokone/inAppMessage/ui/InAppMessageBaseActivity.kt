package com.unotag.mokone.inAppMessage.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.unotag.mokone.R

class InAppMessageBaseActivity : AppCompatActivity(), OnIAMPopupDismissListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val message = intent.getStringExtra("message_key")

        if (message != null) {
            loadIAMWevViewDialog(message)
        }

        //  loadIAMWebViewBottomSheet()

    }

    private fun loadIAMWevViewDialog(message: String) {
        val dialog = IAMWebViewDialog(this, message)
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