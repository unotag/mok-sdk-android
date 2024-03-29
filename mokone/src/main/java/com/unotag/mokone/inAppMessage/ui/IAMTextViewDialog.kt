package com.unotag.mokone.inAppMessage.ui

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.unotag.mokone.R
import com.unotag.mokone.databinding.IAMTextViewDialogLayoutBinding
import com.unotag.mokone.inAppMessage.data.InAppMessageItem
import com.unotag.mokone.utils.MokLogger

class IAMTextViewDialog(
    context: Context,
    private val inAppMessageItem: InAppMessageItem
) : Dialog(context, R.style.NoPaddingDialog) {

    private var onDismissListener: (() -> Unit)? = null

    fun setOnDismissListener(listener: () -> Unit) {
        onDismissListener = listener
    }

    private val binding: IAMTextViewDialogLayoutBinding by lazy {
        IAMTextViewDialogLayoutBinding.inflate(LayoutInflater.from(context))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        window?.setBackgroundDrawableResource(R.drawable.dialog_rounded_corners)
        window?.decorView?.setPadding(0, 0, 0, 0)

        binding.confirmBtn.setOnClickListener {
            dismiss()
            onDismissListener?.invoke()
        }

        if (inAppMessageItem.jsonData != null) {
            val data = inAppMessageItem.jsonData
            binding.titleTv.text = data.title
            binding.descriptionTv.text = data.text
            val image = data.image
            if (image != null) {
                binding.contentIv.setImageURI(Uri.parse(data.image))
            }
        } else {
            MokLogger.log(
                MokLogger.LogLevel.INFO,
                "Trying to load IAM webView but json data not found"
            )
        }



        //Adjust the dialog's size based on the content height
        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val screenWidth = context.resources.displayMetrics.widthPixels
        layoutParams.width = screenWidth - 100
        window?.setLayout(layoutParams.width, layoutParams.height)
    }

    override fun dismiss() {
        super.dismiss()
        onDismissListener?.invoke()
    }
}
