package com.mad.besokminggu.ui.profile

import MonthlySummaryCapsule
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mad.besokminggu.MapsActivity
import com.mad.besokminggu.R
import com.mad.besokminggu.data.repositories.SongRepository
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.network.SessionManager
import com.mad.besokminggu.ui.adapter.CapsuleAdapter
import com.mad.besokminggu.ui.capsule.PdfGenerator
import com.mad.besokminggu.ui.capsule.TimeListenedViewModel
import getMonthKeyFormat
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {
    @Inject lateinit var repository: SongRepository
    @Inject
    lateinit var sessionManager: SessionManager

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

    fun getStreakStringForMonth(month: String): String {
        // Kalau kamu simpan streak info per bulan, pakai repository.getStreakForMonth(ownerId, month)
        return if (month == "March 2025") "5-day streak\nMar 21–25\npray - pray" else "No streak"
    }

    suspend fun generateChartsForEachMonth(
        summaries: List<MonthlySummaryCapsule>,
        repository: SongRepository,
        sessionManager: SessionManager
    ): Map<String, Bitmap?> {
        val result = mutableMapOf<String, Bitmap?>()
        val ownerId = sessionManager.getUserProfile()?.id ?: return emptyMap()

        for (summary in summaries) {
            val monthKey = summary.getMonthKeyFormat()
            val rawData = repository.getPlayedMinutesPerDay(ownerId, monthKey) // suspend
            val weeklyBuckets = MutableList(5) { 0 }

            rawData?.forEach { day ->
                val weekIndex = (day.day.toInt() - 1) / 7
                if (weekIndex in 0..4) {
                    weeklyBuckets[weekIndex] += day.minutes
                }
            }

            result[summary.month] = generateChartBitmap(weeklyBuckets)
        }

        return result
    }




    fun generateChartBitmap(weeklyMinutes: List<Int>): Bitmap {
        val width = 500
        val height = 250
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val background = Paint().apply { color = Color.WHITE }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), background)

        val linePaint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 4f
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isAntiAlias = true
        }

        // Judul chart
        canvas.drawText("Time Listened", 160f, 30f, textPaint)

        // Sumbu dan garis
        val stepX = 80
        val baseY = 200
        var lastX = 0
        var lastY = baseY - (weeklyMinutes.getOrNull(0) ?: 0) * 5

        weeklyMinutes.forEachIndexed { i, value ->
            val x = i * stepX + 40
            val y = baseY - value * 5
            canvas.drawCircle(x.toFloat(), y.toFloat(), 6f, linePaint)
            if (i > 0) canvas.drawLine(lastX.toFloat(), lastY.toFloat(), x.toFloat(), y.toFloat(), linePaint)
            canvas.drawText("W${i + 1}", x.toFloat() - 15, baseY + 25f, textPaint)
            lastX = x
            lastY = y
        }

        return bitmap
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

            binding.soundCapsuleIcon?.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val summaries = profileViewModel.monthlySummaries.value ?: emptyList()

                    if (summaries.isEmpty()) {
                        Toast.makeText(requireContext(), "Summary belum tersedia", Toast.LENGTH_SHORT).show()
                        return@launch
                    }

                    val chartMap = generateChartsForEachMonth(summaries, repository, sessionManager)
                    val streakMap = summaries.associate { it.month to getStreakStringForMonth(it.month) }

                    val file = PdfGenerator.generateMultiMonthPDF(requireContext(), summaries, streakMap, chartMap)

                    val uri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/pdf")
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    }

                    startActivity(intent)
                }
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