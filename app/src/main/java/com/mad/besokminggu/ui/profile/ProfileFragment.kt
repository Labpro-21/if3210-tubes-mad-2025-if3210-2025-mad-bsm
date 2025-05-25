package com.mad.besokminggu.ui.profile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
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
import com.mad.besokminggu.databinding.FragmentProfileBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.login.LoginActivity
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TokenViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.mad.besokminggu.network.ConnectionStateMonitor
import com.mad.besokminggu.network.OnNetworkAvailableCallbacks
import com.mad.besokminggu.ui.optionMenu.ProfileActionSheet
import java.io.File
import com.google.android.gms.location.*
import android.location.Location
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.MapsActivity
import com.mad.besokminggu.R
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.ui.adapter.CapsuleAdapter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var imageUri: Uri
    private var selectedImageUri: Uri? = null
    private lateinit var profileImage: ImageView
    private lateinit var textLocation: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient

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
    private val profileViewModel: ProfileViewModel by viewModels()

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val profileLayout = binding.profileLayout
        val noConnectionLayout = binding.noConnectionProfileLayout

        val logoutButton = binding.logoutButton
        profileImage = binding.profileImage
        val textUsername = binding.textUsername
        textLocation = binding.textLocation

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




        textLocation.setOnClickListener {
            val intent = Intent(requireContext(), MapsActivity::class.java)
            startActivityForResult(intent, 1002)
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

            // Observe Song data
            profileViewModel.likedSongsCount.observe(viewLifecycleOwner) { count ->
                likedSongsCount?.text = count.toString()
            }
            profileViewModel.songsCount.observe(viewLifecycleOwner) { count ->
                totalSongsCount?.text = count.toString()
            }
            profileViewModel.listenedSongsCount.observe(viewLifecycleOwner) { count ->
                listenedSongsCount?.text = count.toString()

                if (count == 0) {
                    binding.soundCapsuleHeader?.visibility = View.GONE
                    binding.recyclerCapsules?.visibility = View.GONE
                    binding.noDataText?.visibility = View.VISIBLE
                } else {
                    binding.soundCapsuleHeader?.visibility = View.VISIBLE
                    binding.recyclerCapsules?.visibility = View.VISIBLE
                    binding.noDataText?.visibility = View.GONE
                }
            }

            profileViewModel.loadCounts()

            logoutButton.setOnClickListener({
                tokenViewModel.deleteToken()
                userViewModel.getProfile(errorHandler)

                Toast.makeText(requireContext(), "Logout Successfully!", Toast.LENGTH_SHORT).show()

                val intent = Intent(requireContext(), LoginActivity::class.java)
                startActivity(intent)
            })


            // Location
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        location?.let {
                            val countryCode = getCountryCodeFromLocation(location, requireContext())
                            Log.d("LOCATION", "country Code: $countryCode")
                            Log.d("LOCATION", "Latitude: ${location.latitude} Longitude: ${location.longitude}")


                            userViewModel.getProfile(errorHandler)
                            textLocation.text = countryCode
                            userViewModel.patchProfile(errorHandler, location = countryCode)
                        } ?: Log.d("LOCATION", "No location available")
                    }
            } else {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )

                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { location: Location? ->
                            location?.let {
                                val countryCode = getCountryCodeFromLocation(location, requireContext())
                                Log.d("LOCATION", "country Code: $countryCode")
                                Log.d("LOCATION", "Latitude: ${location.latitude} Longitude: ${location.longitude}")


                                userViewModel.getProfile(errorHandler)
                                textLocation.text = countryCode
                                userViewModel.patchProfile(errorHandler, location = countryCode)
                            } ?: Log.d("LOCATION", "No location available")
                        }
                }
            }
        } else {
            profileLayout?.visibility = View.GONE
            noConnectionLayout?.visibility = View.VISIBLE
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = binding.recyclerCapsules
        recycler?.layoutManager = LinearLayoutManager(requireContext())
        val navController = NavHostFragment.findNavController(this@ProfileFragment)
        profileViewModel.monthlySummaries.observe(viewLifecycleOwner) { summaries ->
            recycler?.adapter = CapsuleAdapter(
                summaries,
                onArtistDetailClick = {
                    findNavController().navigate(R.id.topArtistCapsuleFragment)
                },
                onSongDetailClick = {
                    val currentMonthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(
                        Date()
                    )
                    val bundle = Bundle().apply {
                        putString("ARG_MONTH_LABEL", currentMonthLabel)
                    }
                    findNavController().navigate(R.id.topSongCapsuleFragment, bundle)
                },
                onTimeDetailClick = {
                    findNavController().navigate(R.id.fragment_time_listened_root)
                }
            )
        }


        profileViewModel.streakInfo.observe(viewLifecycleOwner) { streak ->
            val container = binding.root.findViewById<FrameLayout>(com.mad.besokminggu.R.id.streakCardContainer)
            container.removeAllViews()

            if (streak != null) {
                val streakView = layoutInflater.inflate(com.mad.besokminggu.R.layout.fragement_streak, container, false)

                val title = streakView.findViewById<TextView>(com.mad.besokminggu.R.id.text_streak_title)
                val subtitle = streakView.findViewById<TextView>(com.mad.besokminggu.R.id.text_streak_subtitle)
                val date = streakView.findViewById<TextView>(com.mad.besokminggu.R.id.text_streak_date)
                val cover = streakView.findViewById<ImageView>(com.mad.besokminggu.R.id.cover_streak)

                title.text = "You had a ${streak.streakLength}-day streak"
                subtitle.text = "You played ${streak.streakSongTitle} by ${streak.streakSongArtist}"

                val sdf = SimpleDateFormat("MMM d", Locale.getDefault())
                val year = SimpleDateFormat("yyyy", Locale.getDefault())
                date.text = "${sdf.format(streak.startDate)}–${sdf.format(streak.endDate)}, ${year.format(streak.endDate)}"

                val file = CoverFileHelper.getFile(streak.coverFileName)
                if (file != null && file.exists()) {
                    cover.setImageURI(Uri.fromFile(file))
                } else {
                    cover.setImageResource(com.mad.besokminggu.R.drawable.cover_streak)
                }

                container.addView(streakView)
                container.visibility = View.VISIBLE
            } else {
                container.visibility = View.GONE
            }
        }
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

    private fun getCountryCodeFromLocation(location: Location, context: Context): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return if (addresses != null && addresses.isNotEmpty()) {
            addresses[0].countryCode
        } else {
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1002 && resultCode == RESULT_OK) {
            val lat = data?.getDoubleExtra("lat", 0.0)
            val lng = data?.getDoubleExtra("lng", 0.0)
            val location = Location("").apply {
                latitude = lat ?: 0.0
                longitude = lng ?: 0.0
            }

            val countryCode = getCountryCodeFromLocation(location, requireContext())
            Log.d("LOCATION", "country Code: $countryCode")
            Log.d("LOCATION", "Latitude: ${location.latitude} Longitude: ${location.longitude}")


            userViewModel.getProfile(errorHandler)
            textLocation.text = countryCode
            userViewModel.patchProfile(errorHandler, location = countryCode)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}