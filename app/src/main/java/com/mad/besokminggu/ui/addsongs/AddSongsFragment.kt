package com.mad.besokminggu.ui.addsongs

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.ui.library.LibraryViewModel
import java.util.Date

class AddSongsFragment : BottomSheetDialogFragment() {
    private lateinit var uploadPhoto: FrameLayout
    private lateinit var uploadFile: FrameLayout
    private lateinit var songTitle: EditText
    private lateinit var songArtist: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var uploadPhotoIcon: ImageView
    private lateinit var uploadPhotoLabel: TextView
    private lateinit var uploadFileLabel: TextView

    private var selectedImageUri: Uri? = null
    private var selectedSongUri: Uri? = null

    private val viewModel: LibraryViewModel by activityViewModels()

    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_addsongs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        uploadPhoto = view.findViewById(R.id.uploadPhoto)
        uploadFile = view.findViewById(R.id.uploadFile)
        songTitle = view.findViewById(R.id.titleEdit)
        songArtist = view.findViewById(R.id.artistEdit)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        uploadPhotoIcon = view.findViewById(R.id.uploadPhotoIcon)
        uploadPhotoLabel = view.findViewById(R.id.uploadPhotoLabel)
        uploadFileLabel = view.findViewById(R.id.uploadFileLabel)

        uploadPhoto.setOnClickListener {
            pickImage()
        }

        uploadFile.setOnClickListener {
            pickAudio()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        saveButton.setOnClickListener {
            saveSong()
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        val returnCursor = requireContext().contentResolver.query(uri, null, null, null, null)
        returnCursor?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return "unknown_file"
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            uploadPhotoIcon.setImageURI(it)
            uploadPhotoLabel.text = getFileNameFromUri(it)
        }
    }

    private val pickAudioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedSongUri = it
            uploadFileLabel.text = getFileNameFromUri(it)
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun pickAudio() {
        pickAudioLauncher.launch("audio/*")
    }

    private fun saveSong() {
        val title = songTitle.text.toString().trim()
        val artist = songArtist.text.toString().trim()

        if (title.isEmpty() || artist.isEmpty() || selectedSongUri == null || selectedImageUri == null) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newSong = Song(
            title = title,
            artist = artist,
            coverResId = R.drawable.cover_starboy,
            filePath = selectedSongUri.toString(),
            isLiked = false,
            isPlayed = false,
            createdAt = Date(),
            lastPlayedAt = null
        )

        viewModel.insertSong(newSong)

        Toast.makeText(requireContext(), "Song added: $title by $artist", Toast.LENGTH_SHORT).show()

        dismiss()
    }
}
