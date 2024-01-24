package com.unotag.mokone.webView

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.unotag.mokone.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {

    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityWebViewBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val intent = intent
        val url = intent.getStringExtra("URL")

        viewBinding.webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                viewBinding.progressBar.visibility = View.VISIBLE
                viewBinding.progressBar.progress = newProgress

                if (newProgress == 100) {
                    viewBinding.progressBar.visibility = View.GONE
                }
            }
        }

        if (url != null) {
            initWebView(url)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(url: String) {
        viewBinding.webView.setBackgroundColor(Color.WHITE)
        viewBinding.webView.settings.javaScriptEnabled = true
        viewBinding.webView.loadUrl(url)
    }
}


