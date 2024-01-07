package com.unotag.mokone.inAppMessage.ui

import android.content.DialogInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unotag.mokone.databinding.FragmentIAMIamgeViewBottomSheetBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.inAppMessage.data.JsonData
import com.unotag.mokone.utils.MokLogger

class IAMImageViewBottomSheetFragment : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_IN_APP_MESSAGE_DATA = "in_app_message_data"

        fun newInstance(inAppMessageItem: InAppMessageItem): IAMImageViewBottomSheetFragment {
            val fragment = IAMImageViewBottomSheetFragment()
            val args = Bundle()
            args.putSerializable(ARG_IN_APP_MESSAGE_DATA, inAppMessageItem)
            fragment.arguments = args
            return fragment
        }
    }

    private var mInAppMessageItem: InAppMessageItem? = null
    private lateinit var binding: FragmentIAMIamgeViewBottomSheetBinding
    private lateinit var dismissListener: IAMImageViewBottomSheetDismissListener

    fun setIAMImageViewBottomSheetDismissListener(listener: IAMImageViewBottomSheetDismissListener) {
        dismissListener = listener
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        dismissListener?.onIAMImageViewBottomSheetDismiss()
    }

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

        binding = FragmentIAMIamgeViewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mInAppMessageItem?.jsonData?.let { initViews(it) } ?: run{
            MokLogger.log(MokLogger.LogLevel.ERROR, "Json data is empty")
        }

//        binding.confirmBtn.setOnClickListener {
//            dismiss()
//        }
    }


    private fun initViews(jsonData: JsonData) {
        binding.contentIv.setImageURI(Uri.parse(jsonData.image))
    }
}

interface IAMImageViewBottomSheetDismissListener {
    fun onIAMImageViewBottomSheetDismiss()
}