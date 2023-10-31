package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import com.unotag.mokone.R
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger

class IAMWebViewDialog(
    context: Context,
    private val inAppMessageItem: InAppMessageItem
) : Dialog(context, R.style.NoPaddingDialog) {

    private var onDismissListener: (() -> Unit)? = null

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.i_a_m_webview_dialog_layout)

        window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
        window?.decorView?.setPadding(0, 0, 0, 0)

        val closeIv = findViewById<ImageView>(R.id.close_iv)
        closeIv.setOnClickListener {
            dismiss()
            onDismissListener?.invoke()
        }

        if (inAppMessageItem.jsonData?.html != null) {
            initWebView(inAppMessageItem.jsonData.html)
        } else {
            MokLogger.log(
                MokLogger.LogLevel.INFO,
                "Trying to load IAM webView but html not found"
            )
        }


        // Adjust the dialog's size based on the content height
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setLayout(layoutParams.width, layoutParams.height)
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(html: String) {
        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener?.invoke()
    }
}
