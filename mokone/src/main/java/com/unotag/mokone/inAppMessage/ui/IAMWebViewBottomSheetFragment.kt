package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unotag.mokone.databinding.FragmentIAMWebviewBottomSheetBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger


class IAMWebViewBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_IN_APP_MESSAGE_DATA = "in_app_message_data"

        fun newInstance(inAppMessageItem: InAppMessageItem): IAMWebViewBottomSheetFragment {
            val fragment = IAMWebViewBottomSheetFragment()
            val args = Bundle()
            args.putSerializable(ARG_IN_APP_MESSAGE_DATA, inAppMessageItem)
            fragment.arguments = args
            return fragment
        }
    }

    private var mInAppMessageItem: InAppMessageItem? = null
    private lateinit var binding: FragmentIAMWebviewBottomSheetBinding
    private lateinit var dismissListener: IAMWebViewBottomSheetDismissListener

    fun setIAMWebViewBottomSheetDismissListener(listener: IAMWebViewBottomSheetDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener.onIAMWebViewBottomSheetDismiss()
    }

    @Suppress("DEPRECATION")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            mInAppMessageItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                it.getSerializable(ARG_IN_APP_MESSAGE_DATA, InAppMessageItem::class.java)
            } else {
                it.getSerializable(ARG_IN_APP_MESSAGE_DATA) as InAppMessageItem
            }
        }

        binding = FragmentIAMWebviewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mInAppMessageItem?.jsonData?.html?.let { initWebView(it) } ?: run{
            MokLogger.log(MokLogger.LogLevel.ERROR, "HTML content is empty")
        }

        binding.closeIv.setOnClickListener {
            dismiss()
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(html: String) {
        binding.webViewBottomSheet.settings.javaScriptEnabled = true
        binding.webViewBottomSheet.settings.javaScriptEnabled = true
        binding.webViewBottomSheet.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}

interface IAMWebViewBottomSheetDismissListener {
    fun onIAMWebViewBottomSheetDismiss()
}