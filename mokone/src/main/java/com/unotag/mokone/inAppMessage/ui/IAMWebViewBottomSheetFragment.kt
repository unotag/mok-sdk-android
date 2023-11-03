package com.unotag.mokone.inAppMessage.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.unotag.mokone.databinding.FragmentIAMWebviewBottomSheetBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem


class IAMWebViewBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_IN_APP_MESSAGE_DATA = "in_app_message_data"

        fun newInstance(inAppMessageData: String): IAMWebViewBottomSheetFragment {
            val fragment = IAMWebViewBottomSheetFragment()
            val args = Bundle()
            args.putString(ARG_IN_APP_MESSAGE_DATA, inAppMessageData)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var mInAppMessageId: String
    private lateinit var mInAppMessageItem : InAppMessageItem

    private lateinit var binding: FragmentIAMWebviewBottomSheetBinding
    private var dismissListener: OnIAMPopupDismissListener? = null

    fun setOnDismissListener(listener: OnIAMPopupDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onDismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        arguments?.let {
            val inAppMessageItemString = it.getString(ARG_IN_APP_MESSAGE_DATA)
            mInAppMessageItem = Gson().fromJson(inAppMessageItemString, InAppMessageItem::class.java)

            this.mInAppMessageId = mInAppMessageItem.inAppId ?: "NA"
        }

        binding = FragmentIAMWebviewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mInAppMessageItem.jsonData?.html?.let { initWebView(it) }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(html: String) {
        binding.webViewBottomSheet.settings.javaScriptEnabled = true
        binding.webViewBottomSheet.settings.javaScriptEnabled = true
        binding.webViewBottomSheet.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
    }
}