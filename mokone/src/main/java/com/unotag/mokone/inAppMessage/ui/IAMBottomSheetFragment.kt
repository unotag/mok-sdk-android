package com.unotag.mokone.inAppMessage.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unotag.mokone.databinding.FragmentIAMBottomSheetBinding

class IAMBottomSheetFragment : BottomSheetDialogFragment() {


    private lateinit var binding : FragmentIAMBottomSheetBinding
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
        binding = FragmentIAMBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }


}