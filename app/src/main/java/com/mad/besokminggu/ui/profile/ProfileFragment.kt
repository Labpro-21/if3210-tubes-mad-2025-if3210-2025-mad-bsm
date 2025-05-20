package com.mad.besokminggu.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.mad.besokminggu.databinding.FragmentProfileBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.login.LoginActivity
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TokenViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide
import com.mad.besokminggu.network.ConnectionStateMonitor
import com.mad.besokminggu.network.OnNetworkAvailableCallbacks
import com.mad.besokminggu.ui.optionMenu.ProfileActionSheet
import java.io.File

@AndroidEntryPoint
class ProfileFragment : Fragment() {


    private lateinit var imageUri: Uri
    private var selectedImageUri: Uri? = null
    private lateinit var profileImage: ImageView

    private val userViewModel: UserViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()

    private lateinit var connectionMonitor: ConnectionStateMonitor

    private val errorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            Log.e("PROFILE_FRAGMENT", message)
            Toast.makeText(null, "Error! $message", Toast.LENGTH_SHORT).show()
        }
    }

    private var _binding: FragmentProfileBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val profileViewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val profileLayout = binding.profileLayout
        val noConnectionLayout = binding.noConnectionProfileLayout

        val logoutButton = binding.logoutButton
        profileImage = binding.profileImage
        val textUsername = binding.textUsername
        val textLocation = binding.textLocation

        val likedSongsCount = binding.textLikedSongsNumber
        val totalSongsCount = binding.textTotalSongsNumber
        val listenedSongsCount = binding.textListenedSongsNumber

        val baseImageUrl = "http://34.101.226.132:3000/uploads/profile-picture/"

        binding.editProfileButton?.setOnClickListener {
            ProfileActionSheet(
                onPhoto = {
                    takeImage()
                },
                onPicture = {
                    pickImage()
                }
            ).show(parentFragmentManager, "ProfileActionSheet")
        }


        // init connection monitor
        connectionMonitor =
            ConnectionStateMonitor(requireContext(), object : OnNetworkAvailableCallbacks {
                override fun onPositive() {
                    profileLayout?.visibility = View.VISIBLE
                    noConnectionLayout?.visibility = View.GONE
                }

                override fun onNegative() {
                    profileLayout?.visibility = View.GONE
                    noConnectionLayout?.visibility = View.VISIBLE
                }

                override fun onError(s: String) {
                }
            })

        if (connectionMonitor.hasNetworkConnection()) {
            profileLayout?.visibility = View.VISIBLE
            noConnectionLayout?.visibility = View.GONE

            // Observe the profile data
            userViewModel.profileResponse.observe(viewLifecycleOwner) { profile ->
                when (profile) {
                    is ApiResponse.Success -> {
                        val userProfile = profile.data

                        textUsername.text = userProfile.username
                        textLocation.text = userProfile.location
                        val imagePath = baseImageUrl + userProfile.profilePhoto

                        // Load the image using Glide
                        Glide.with(this)
                            .load(imagePath)
                            .into(profileImage)
                    }

                    is ApiResponse.Failure -> {

                        textUsername.text = "No username available"
                        textLocation.text = "No location available"
                    }

                    is ApiResponse.Loading -> {

                    }
                }
            }
//            userViewModel.getProfile(errorHandler)

            userViewModel.profile.observe(viewLifecycleOwner) {
                textUsername.text = it.username
                textLocation.text = it.location
                val imagePath = baseImageUrl + it.profilePhoto

                // Load the image using Glide
                Glide.with(this)
                    .load(imagePath)
                    .into(profileImage)
            }

            // TEST UPLOAD PHOTO
            userViewModel.profilePhotoResponse.observe(viewLifecycleOwner) {
                when (it) {
                    is ApiResponse.Success -> {
                        val userProfile = it.data

                        Log.d("ProfileFragment", "Uploaded New Image! ${userProfile.message}")
                    }

                    is ApiResponse.Failure -> {

                    }

                    is ApiResponse.Loading -> {

                    }
                }
            }

            // Observe Song data
            profileViewModel.likedSongsCount.observe(viewLifecycleOwner) { count ->
                likedSongsCount?.text = count.toString()
            }
            profileViewModel.songsCount.observe(viewLifecycleOwner) { count ->
                totalSongsCount?.text = count.toString()
            }
            profileViewModel.listenedSongsCount.observe(viewLifecycleOwner) { count ->
                listenedSongsCount?.text = count.toString()
            }
            profileViewModel.loadCounts()

            logoutButton.setOnClickListener({
                tokenViewModel.deleteToken()
                userViewModel.getProfile(errorHandler)

                Toast.makeText(requireContext(), "Logout Successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            })
        } else {
            profileLayout?.visibility = View.GONE
            noConnectionLayout?.visibility = View.VISIBLE
        }

        return root
    }

    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Unable to open input stream from URI")
        val file = File.createTempFile("upload_", ".tmp", context.cacheDir)
        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return file
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
            val file = uriToFile(requireContext(), it)
            userViewModel.patchProfile(coroutinesErrorHandler = errorHandler, profilePhoto = file)
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private val takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            profileImage.setImageURI(imageUri)
            val file = uriToFile(requireContext(), imageUri)
            userViewModel.patchProfile(coroutinesErrorHandler = errorHandler, profilePhoto = file)
        } else {
            Toast.makeText(context, "Image capture failed", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCamera() {
        val imageFile = File(requireContext().cacheDir, "camera_profile_photo.jpg")
        imageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        takePictureLauncher.launch(imageUri)
    }

    private fun checkAndLaunchCamera() {
        when {
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                Toast.makeText(requireContext(), "Camera permission is required", Toast.LENGTH_SHORT).show()
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun takeImage() {
        checkAndLaunchCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}