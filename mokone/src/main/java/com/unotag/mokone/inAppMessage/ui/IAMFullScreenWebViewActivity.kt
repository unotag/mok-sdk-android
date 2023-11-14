package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
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


        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val resultIntent = Intent()
                setResult(RESULT_OK, resultIntent)
                finish()
//                when {
//                    binding.fullScreenWebview.canGoBack() -> binding.fullScreenWebview.goBack()
//                }
            }
        }

        onBackPressedDispatcher.addCallback(onBackPressedCallback)
//
       // disableOnBackPressedCallback(binding.fullScreenWebview, onBackPressedCallback)

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
        binding.fullScreenWebview.setBackgroundColor(Color.WHITE);

        binding.fullScreenWebview.settings.javaScriptEnabled = true
        binding.fullScreenWebview.loadUrl(url)
    }


    private fun disableOnBackPressedCallback(
        webView: WebView,
        onBackPressedCallback: OnBackPressedCallback
    ) {
        webView.webViewClient = object : WebViewClient() {
            override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                // Disable the on-back press callback if there are no more questions in the
                // WebView to go back to, allowing us to exit the WebView and go back to
                // the fragment.
                webView.canGoBack().also { onBackPressedCallback.isEnabled = it }

            }
        }
    }
}
