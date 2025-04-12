package com.mad.besokminggu.ui.addsongs

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.media.MediaMetadataRetriever
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.activityViewModels
import android.app.Dialog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mad.besokminggu.R
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.manager.AudioFileHelper
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.ui.library.LibraryViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Date
import java.io.File
import java.io.FileOutputStream

@AndroidEntryPoint
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
    private val userViewModel: UserViewModel by activityViewModels()

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { dlg ->
            val bottomSheet = dialog.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet?.let {
                BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
        return dialog
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

    private fun saveEmbeddedImageToTempFile(bitmap: android.graphics.Bitmap): Uri? {
        return try {
            val file = File.createTempFile("cover_", ".jpg", requireContext().cacheDir)
            val fos = FileOutputStream(file)
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, fos)
            fos.flush()
            fos.close()
            androidx.core.content.FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider", // update this to match your manifest FileProvider
                file
            )
        } catch (e: Exception) {
            Log.e("AddSongsFragment", "Failed to save embedded image: ${e.message}")
            null
        }
    }

    private val pickAudioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedSongUri = it
            uploadFileLabel.text = getFileNameFromUri(it)

            // Initialize MediaMetadataRetriever
            val retriever = android.media.MediaMetadataRetriever()
            try {
                retriever.setDataSource(requireContext(), it)

                // Extract title and artist from metadata
                val extractedTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                val extractedArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

                if (!extractedTitle.isNullOrBlank()) {
                    songTitle.setText(extractedTitle)
                }

                if (!extractedArtist.isNullOrBlank()) {
                    songArtist.setText(extractedArtist)
                }

                // Extract embedded picture
                val embeddedPicture = retriever.embeddedPicture
                if (embeddedPicture != null) {
                    val bitmap = android.graphics.BitmapFactory.decodeByteArray(embeddedPicture, 0, embeddedPicture.size)
                    uploadPhotoIcon.setImageBitmap(bitmap)

                    // Optional: Save the image to a file if needed later
                    selectedImageUri = saveEmbeddedImageToTempFile(bitmap)
                    uploadPhotoLabel.text = "Embedded Cover"
                }

            } catch (e: Exception) {
                Log.e("AddSongsFragment", "Failed to extract metadata: ${e.message}")
            } finally {
                retriever.release()
            }
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

        val resolver = requireContext().contentResolver

        //  Read bytes from selected URIs
        val audioBytes = resolver.openInputStream(selectedSongUri!!)?.readBytes()
        val imageBytes = resolver.openInputStream(selectedImageUri!!)?.readBytes()

        if (audioBytes == null || imageBytes == null) {
            Toast.makeText(requireContext(), "Failed to read selected files", Toast.LENGTH_SHORT).show()
            return
        }

        val audioExt = FileHelper.getFileExtension(requireContext(),selectedSongUri!!)
        val imageExt = FileHelper.getFileExtension(requireContext(),selectedImageUri!!)


        val audioFile = AudioFileHelper.saveGeneratedFile(
            audioBytes, extension = audioExt
        )
        val imageFile = CoverFileHelper.saveGeneratedFile(
            imageBytes, extension = imageExt
        )

        if (audioFile == null || imageFile == null) {
            Toast.makeText(requireContext(), "Failed to save files", Toast.LENGTH_SHORT).show()
            return
        }

        // 🔽 Create Song with just the file names
        val newSong = Song(
            title = title,
            artist = artist,
            ownerId = userViewModel.profile.value?.id ?: -1,
            coverFileName = imageFile.name,
            audioFileName = audioFile.name,
            isLiked = false,
            createdAt = Date(),
            lastPlayedAt = null
        )

        viewModel.insertSong(newSong)

        Toast.makeText(requireContext(), "Song added: $title by $artist", Toast.LENGTH_SHORT).show()
        dismiss()

    }

}
