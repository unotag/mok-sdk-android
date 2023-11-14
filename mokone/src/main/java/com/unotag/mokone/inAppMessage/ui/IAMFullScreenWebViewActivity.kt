package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.unotag.mokone.databinding.ActivityIamfullScreenWebViewBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger

class IAMFullScreenWebViewActivity : AppCompatActivity() {


    private lateinit var binding: ActivityIamfullScreenWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val inAppMessageItem: InAppMessageItem? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("in_app_message_data", InAppMessageItem::class.java)
            } else {
                intent.getSerializableExtra("in_app_message_data") as InAppMessageItem
            }

        binding = ActivityIamfullScreenWebViewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        inAppMessageItem?.jsonData?.popupConfigs?.webUrl?.let { initWebView(it) } ?: run {
            MokLogger.log(MokLogger.LogLevel.ERROR, "URL is null, update url from mok.one template")
        }



        binding.fullScreenWebview.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progressBar.visibility = View.VISIBLE
                binding.progressBar.progress = newProgress

                if (newProgress == 100) {
                    binding.progressBar.visibility = View.GONE
                }
            }
        }

    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(url: String) {
        binding.fullScreenWebview.settings.javaScriptEnabled = true
        binding.fullScreenWebview.loadUrl(url)
    }

    override fun onSupportNavigateUp(): Boolean {
        val resultIntent = Intent()
        setResult(RESULT_OK, resultIntent)
        finish()
        return true
    }
}
