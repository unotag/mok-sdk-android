package com.unotag.mokone.inAppMessage.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.unotag.mokone.databinding.FragmentIAMWebviewBottomSheetBinding
import org.sufficientlysecure.htmltextview.HtmlHttpImageGetter


class IAMWebViewBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding : FragmentIAMWebviewBottomSheetBinding
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
        binding = FragmentIAMWebviewBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.htmlText.setHtml("<div class='bee-popup-container'><style> .bee-popup-row-1 .bee-popup-col-1, .bee-popup-row-2 .bee-popup-col-1, .bee-popup-row-2 .bee-popup-col-2, .bee-popup-row-3 .bee-popup-col-1 { padding-bottom: 5px; padding-top: 5px } .bee-popup-container div, .bee-popup-container p { margin: 0; padding: 0 } .bee-popup-container img { border: 0 } .bee-popup-container { color: #000; font-family: Arial, Helvetica Neue, Helvetica, sans-serif } .bee-popup-container * { box-sizing: border-box } .bee-popup-container a, .bee-popup-row-1 .bee-popup-col-1 .bee-popup-block-1 a, .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li a { color: #0068a5 } .bee-popup-container p { margin: 0 } .bee-popup-container .bee-popup-row { position: relative } .bee-popup-container .bee-popup-row-content { max-width: 500px; position: relative; margin: 0 auto; display: flex } .bee-popup-container .bee-popup-row-content .bee-popup-col-w3 { flex: 3 } .bee-popup-container .bee-popup-row-content .bee-popup-col-w9 { flex: 9 } .bee-popup-container .bee-popup-row-content .bee-popup-col-w12 { flex: 12 } .bee-popup-image { overflow: auto } .bee-popup-image .bee-popup-center { margin: 0 auto } .bee-popup-row-2 .bee-popup-col-2 .bee-popup-block-1 { width: 100% } .bee-popup-list ul { margin: 0; padding: 0 } .bee-popup-image img { display: block; width: 100% } .bee-popup-social .icon img { max-height: 32px } .bee-popup-paragraph { overflow-wrap: anywhere } @media (max-width:520px) { .bee-popup-row-content:not(.no_stack) { display: block; max-width: 250px } } .bee-popup-row-1, .bee-popup-row-2, .bee-popup-row-3 { background-repeat: no-repeat } .bee-popup-row-1 .bee-popup-row-content { background-repeat: no-repeat; color: #000 } .bee-popup-row-1 .bee-popup-col-1 { border-bottom: 0 solid transparent; border-left: 0 solid transparent; border-right: 0 solid transparent; border-top: 0 solid transparent } .bee-popup-row-2 .bee-popup-row-content, .bee-popup-row-3 .bee-popup-row-content { background-repeat: no-repeat; border-radius: 0; color: #000 } .bee-popup-row-3 .bee-popup-col-1 .bee-popup-block-1 { padding: 10px; text-align: center } .bee-popup-row-1 .bee-popup-col-1 .bee-popup-block-1, .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 { padding: 10px; color: #000; direction: ltr; font-size: 14px; font-weight: 400; letter-spacing: 0; line-height: 120%; text-align: left } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 ul { list-style-type: revert; list-style-position: inside } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li:not(:last-child) { margin-bottom: 0 } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li ul { margin-top: 0 } .bee-popup-row-2 .bee-popup-col-1 .bee-popup-block-1 li li { margin-left: 40px } .bee-popup-row-1 .bee-popup-col-1 .bee-popup-block-1 p:not(:last-child) { margin-bottom: 16px } </style><div class='bee-popup-rows-container'><div class='bee-popup-row bee-popup-row-1'><div class='bee-popup-row-content'><div class='bee-popup-col bee-popup-col-1 bee-popup-col-w12'><div class='bee-popup-block bee-popup-block-1 bee-popup-paragraph'><p>Buy our Products and Get additional 10% Off</p></div></div></div></div><div class='bee-popup-row bee-popup-row-2'><div class='bee-popup-row-content'><div class='bee-popup-col bee-popup-col-1 bee-popup-col-w3'><div class='bee-popup-block bee-popup-block-1 bee-popup-list'><ul><li>Buy Products worth 100</li><li>Get Voucher Code</li><li>Apply on our website</li><li>Hurray</li></ul></div></div><div class='bee-popup-col bee-popup-col-2 bee-popup-col-w9'><div class='bee-popup-block bee-popup-block-1 bee-popup-image'><img class='bee-popup-center bee-popup-autowidth' src='https://d15k2d11r6t6rl.cloudfront.net/public/users/Integrators/cc122e38-1c4d-41f6-988c-cf7ba39e0148/lzp4gxx5n1/discont%20voucher.webp' style='max-width:375px;' alt=''/></div></div></div></div><div class='bee-popup-row bee-popup-row-3'><div class='bee-popup-row-content'><div class='bee-popup-col bee-popup-col-1 bee-popup-col-w12'><div class='bee-popup-block bee-popup-block-1 bee-popup-social'><div class='content'><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://www.facebook.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/facebook@2x.png' alt='Facebook' title='Facebook'/></a></span><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://twitter.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/twitter@2x.png' alt='Twitter' title='Twitter'/></a></span><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://instagram.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/instagram@2x.png' alt='Instagram' title='Instagram'/></a></span><span class='icon' style='padding:0 2.5px 0 2.5px;'><a href='https://www.linkedin.com/' target='_blank'><img src='https://app-rsrc.getbee.io/public/resources/social-networks-icon-sets/circle-color/linkedin@2x.png' alt='LinkedIn' title='LinkedIn'/></a></span></div></div></div></div></div></div></div>",
            HtmlHttpImageGetter(binding.htmlText)
        );
    }

}