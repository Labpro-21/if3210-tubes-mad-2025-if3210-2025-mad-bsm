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
import androidx.core.content.FileProvider
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
    private lateinit var uploadSongLabel: TextView

    private var selectedImageUri: Uri? = null
    private var selectedSongUri: Uri? = null

    private val viewModel: LibraryViewModel by activityViewModels()
    private val userViewModel: UserViewModel by activityViewModels()

    private var isEditMode = false
    private var songID: Int? = null
    private var songTitleArg: String? = null
    private var artistName: String? = null
    private var songFilePath: String? = null
    private var songImagePath: String? = null

    private var coverEdited = false
    private var fileEdited = false

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            isEditMode = bundle.getBoolean("isEditMode", false)
            songID = bundle.getInt("songID")
            songTitleArg = bundle.getString("songTitle")
            artistName = bundle.getString("artistName")
            songFilePath = bundle.getString("songFilePath")
            songImagePath = bundle.getString("songImagePath")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uploadPhoto = view.findViewById(R.id.uploadPhoto)
        uploadFile = view.findViewById(R.id.uploadFile)
        songTitle = view.findViewById(R.id.titleEdit)
        songArtist = view.findViewById(R.id.artistEdit)
        saveButton = view.findViewById(R.id.saveButton)
        cancelButton = view.findViewById(R.id.cancelButton)
        uploadPhotoIcon = view.findViewById(R.id.uploadPhotoIcon)
        uploadPhotoLabel = view.findViewById(R.id.uploadPhotoLabel)
        uploadFileLabel = view.findViewById(R.id.uploadFileLabel)
        uploadSongLabel = view.findViewById(R.id.uploadSongLabel)

        if (isEditMode) {
            uploadSongLabel.setText("Edit Song")
            songTitle.setText(songTitleArg)
            songArtist.setText(artistName)

            songImagePath?.let { image ->
                val File = CoverFileHelper.getFile(image)
                File?.let { file ->
                    val uri = Uri.fromFile(file)
                    uploadPhotoIcon.setImageURI(uri)
                    selectedImageUri = uri
                }
            }

            songFilePath?.let { file ->
                val File = AudioFileHelper.getFile(file)
                File?.let { file ->
                    val uri = Uri.fromFile(file)
                    selectedSongUri = uri
                }
            }
        }

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
            coverEdited = true
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
                "${requireContext().packageName}.provider",
                file
            )
        } catch (e: Exception) {
            Log.e("AddSongsFragment", "Failed to save embedded image: ${e.message}")
            null
        }
    }

    private val pickAudioLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            fileEdited = true
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

                    selectedImageUri = saveEmbeddedImageToTempFile(bitmap)
                    uploadPhotoLabel.text = "Embedded Cover"
                    coverEdited = true
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
        var audioFile: File? = null
        if (fileEdited) {
            val audioBytes = resolver.openInputStream(selectedSongUri!!)?.readBytes()

            if (audioBytes == null) {
                Toast.makeText(requireContext(), "Failed to read selected files", Toast.LENGTH_SHORT).show()
                return
            }

            val audioExt = FileHelper.getFileExtension(requireContext(),selectedSongUri!!)
            audioFile = AudioFileHelper.saveGeneratedFile(
                audioBytes, extension = audioExt
            )

            if (audioFile == null) {
                Toast.makeText(requireContext(), "Failed to save audio file", Toast.LENGTH_SHORT).show()
                return
            }
        }

        var imageFile: File? = null
        if (coverEdited) {
            val imageBytes = resolver.openInputStream(selectedImageUri!!)?.readBytes()

            if (imageBytes == null) {
                Toast.makeText(requireContext(), "Failed to read cover file", Toast.LENGTH_SHORT).show()
                return
            }

            val imageExt = FileHelper.getFileExtension(requireContext(),selectedImageUri!!)
            imageFile = CoverFileHelper.saveGeneratedFile(
                imageBytes, extension = imageExt
            )

            if (imageFile == null) {
                Toast.makeText(requireContext(), "Failed to save files", Toast.LENGTH_SHORT).show()
                return
            }
        }

        if (isEditMode) {
            songID?.let { id ->
                viewModel.getSong(id).observe(viewLifecycleOwner) { song ->
                    val updatedSong = Song(
                        id = song.id,
                        title = title,
                        artist = artist,
                        ownerId = song.ownerId,
                        coverFileName = imageFile?.name ?: song.coverFileName,
                        audioFileName = audioFile?.name ?: song.audioFileName,
                        isLiked = song.isLiked,
                        createdAt = song.createdAt,
                        lastPlayedAt = song.lastPlayedAt
                    )

                    viewModel.updateSong(updatedSong)
                }
            }
        } else {
            if (imageFile != null && audioFile != null) {
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
            }
        }


        Toast.makeText(requireContext(), "Song added: $title by $artist", Toast.LENGTH_SHORT).show()
        dismiss()

    }

}
