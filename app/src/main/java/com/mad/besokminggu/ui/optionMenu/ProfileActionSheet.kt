package com.mad.besokminggu.ui.optionMenu


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mad.besokminggu.R

class ProfileActionSheet(
    private val onPhoto: () -> Unit,
    private val onPicture: () -> Unit,
) : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_profile_actions, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoButton = view.findViewById<LinearLayout>(R.id.take_photo_button)
        val pictureButton = view.findViewById<LinearLayout>(R.id.picture_button)

        photoButton.setOnClickListener {
            onPhoto()
            dismiss()
        }

        pictureButton.setOnClickListener {
            onPicture()
            dismiss()
        }
    }


}
