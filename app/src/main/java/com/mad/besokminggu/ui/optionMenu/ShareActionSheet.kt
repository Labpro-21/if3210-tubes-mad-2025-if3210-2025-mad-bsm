package com.mad.besokminggu.ui.optionMenu


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mad.besokminggu.R

class ShareActionSheet(
    private val onQR: () -> Unit,
    private val onOther: () -> Unit,
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_share_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val qrButton = view.findViewById<LinearLayout>(R.id.share_qr_button)
        val otherButton = view.findViewById<LinearLayout>(R.id.share_other_button)

        qrButton.setOnClickListener {
            onQR()
            dismiss()
        }

        otherButton.setOnClickListener {
            onOther()
            dismiss()
        }
    }


}
