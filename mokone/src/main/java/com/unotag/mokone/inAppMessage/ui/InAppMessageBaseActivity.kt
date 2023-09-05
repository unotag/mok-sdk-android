package com.unotag.mokone.inAppMessage.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.unotag.mokone.R

class InAppMessageBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_in_app_message_base)

        val iAMBottomSheetFragment = IAMBottomSheetFragment()
        iAMBottomSheetFragment.show(supportFragmentManager, iAMBottomSheetFragment.tag)
    }
}