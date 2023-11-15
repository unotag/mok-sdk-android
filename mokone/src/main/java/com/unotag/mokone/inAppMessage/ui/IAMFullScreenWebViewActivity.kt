package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.unotag.mokone.databinding.FragmentIamFullScreenWebViewBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger


class IAMFullScreenWebViewFragment : Fragment() {

    private var fullScreenWebViewClosedListener: FullScreenWebViewClosedListener? = null

    private lateinit var binding: FragmentIamFullScreenWebViewBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentIamFullScreenWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun setFullScreenWebViewClosedListener(listener: FullScreenWebViewClosedListener) {
        fullScreenWebViewClosedListener = listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val inAppMessageItem: InAppMessageItem? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireActivity().intent.getSerializableExtra("in_app_message_data", InAppMessageItem::class.java)
            } else {
                requireActivity().intent.getSerializableExtra("in_app_message_data") as InAppMessageItem
            }


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
        binding.fullScreenWebview.setBackgroundColor(Color.WHITE)
        binding.fullScreenWebview.settings.javaScriptEnabled = true
        binding.fullScreenWebview.loadUrl(url)
    }


    override fun onDestroy() {
        super.onDestroy()
        fullScreenWebViewClosedListener?.onFullScreenWebViewClosed()
    }
}

interface FullScreenWebViewClosedListener {
    fun onFullScreenWebViewClosed()
}
