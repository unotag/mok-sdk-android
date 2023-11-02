package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.unotag.mokone.databinding.ActivityIamfullScreenWebViewBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem

class IAMFullScreenWebViewActivity : AppCompatActivity() {

    private lateinit var mInAppMessageId: String

    private lateinit var binding: ActivityIamfullScreenWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inAppMessageItemString = intent.getStringExtra("in_app_message_data")

        val inAppMessageItem = Gson().fromJson(inAppMessageItemString, InAppMessageItem::class.java)

        this.mInAppMessageId = inAppMessageItem.inAppId ?: "NA"


        binding = ActivityIamfullScreenWebViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        inAppMessageItem.jsonData?.popupConfigs?.webUrl?.let { initWebView(it) }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(url: String) {
        binding.fullScreenWebview.settings.javaScriptEnabled = true
        binding.fullScreenWebview.loadUrl(url)
    }



}
