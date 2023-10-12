package com.unotag.mokone.inAppMessage.ui

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import androidx.annotation.NonNull
import com.unotag.mokone.R

class IAMWebViewDialog(
    @NonNull context: Context,
    private val htmlContent: String
) : Dialog(context,  R.style.NoPaddingDialog) {

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

        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        WebView.setWebContentsDebuggingEnabled(true)
        //if (type == raw){
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
   // }else {
        //webView.loadUrl(htmlContent)
    //}
        // Adjust the dialog's size based on the content height
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window?.setLayout(layoutParams.width, layoutParams.height)
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener?.invoke()
    }
}
