package com.mad.besokminggu

import android.content.ComponentName
import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import androidx.activity.viewModels
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout

import android.widget.ImageButton
import android.widget.TextView
import androidx.annotation.OptIn
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.mad.besokminggu.data.model.toSong
import com.google.common.util.concurrent.MoreExecutors
import com.mad.besokminggu.data.model.Song
import com.mad.besokminggu.databinding.ActivityMainBinding
import com.mad.besokminggu.manager.AudioFileHelper
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.manager.IGetPermissionListener
import com.mad.besokminggu.manager.PermissionHelper
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.network.ConnectionStateMonitor
import com.mad.besokminggu.network.OnNetworkAvailableCallbacks
import com.mad.besokminggu.data.services.PlaybackService
import com.mad.besokminggu.manager.PlaybackQueueManager
import com.mad.besokminggu.ui.viewTracks.MiniPlayerView
import com.mad.besokminggu.ui.login.LoginActivity
import com.mad.besokminggu.ui.topSongs.TopSongsViewModel
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.OnlineSongsViewModel
import com.mad.besokminggu.viewModels.SongTracksViewModel
import com.mad.besokminggu.viewModels.TokenViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.util.Date
import javax.inject.Inject



@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IGetPermissionListener {

    private lateinit var binding: ActivityMainBinding

    private val songViewModel : SongTracksViewModel by viewModels()
    private val userViewModel : UserViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private lateinit var controller : MediaController;
    private val topSongsViewModel: TopSongsViewModel by viewModels()
    private val onlineSongsViewModel: OnlineSongsViewModel by viewModels()

    @Inject
    lateinit var queueManager: PlaybackQueueManager


    private lateinit var connectionMonitor: ConnectionStateMonitor
    @Inject
    lateinit var permissionHelper: PermissionHelper

    lateinit var navController : androidx.navigation.NavController

    private val requestLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        )
        { isGranted: Boolean ->
            permissionHelper.handleSinglePermissionResult(this, isGranted)
        }

    // OnActivityResult to handle permission result.
    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                checkPermission()
            }
        }

    override fun onPermissionGranted() {

    }

    override fun onPermissionDenied() {
        checkPermission()
    }

    override fun onPermissionRationale() {
        permissionAlertDialog()
    }

    fun onOpenTrackSong(){
        val fullPlayer = binding.fullPlayer
        if(false)return

        fullPlayer.translationY = fullPlayer.height.toFloat()
        fullPlayer.alpha = 0f
        fullPlayer.visibility = View.VISIBLE
        fullPlayer.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(300)
            .start()
        binding.miniPlayer.visibility = View.GONE
    }

    fun onCloseTrackSong(){
        val fullPlayer = binding.fullPlayer

        fullPlayer.animate()
            .translationY(fullPlayer.height.toFloat())
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                fullPlayer.visibility = View.GONE
                fullPlayer.translationY = 0f
                fullPlayer.alpha = 1f
                if(songViewModel.isAnySongPlayed()){
                    binding.miniPlayer.visibility = View.VISIBLE
                }else{
                    binding.miniPlayer.visibility = View.GONE
                }
            }
            .start()
    }


    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionHelper.setPermissionListener(this)
        checkPermission()

        FileHelper.init(context = applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ContextCompat.startForegroundService(this, Intent(this, PlaybackService::class.java));


        val serviceComponent = ComponentName(this, PlaybackService::class.java)
        val token = SessionToken(this, serviceComponent)

        lifecycleScope.launch(Dispatchers.IO) {
            val future = MediaController.Builder(this@MainActivity, token)
                .buildAsync()

            val ctrl = future.get()

            withContext(Dispatchers.Main) {
                controller = ctrl
            AudioPlayerManager.init(controller,queueManager);
            }

        }

        val navView: BottomNavigationView? = binding.navView
        val fullPlayer : FragmentContainerView = binding.fullPlayer
        val miniPlayer : MiniPlayerView = binding.miniPlayer

            // Wait until views are loaded
        binding.root.post {
            navController = findNavController(R.id.nav_host_fragment_activity_main)

            miniPlayer.setNavigationController(navController)

            // Setup for BottomNavigationView (portrait)
//            navView?.setupWithNavController(navController)
            navView?.setOnNavigationItemSelectedListener{menuItem ->
                onCloseTrackSong()
                navController.navigate(menuItem.itemId)
                menuItem.isChecked = true
                true
            }

            // Setup for NavigationView (landscape)
            val sideNavView = findViewById<NavigationView>(R.id.side_nav_view)
            sideNavView?.setNavigationItemSelectedListener { menuItem ->
                menuItem.isChecked = true
                navController.navigate(menuItem.itemId)
                onCloseTrackSong()
                true
            }
        }

        // Initialize Connection State Monitor
        connectionMonitor = ConnectionStateMonitor(this, object : OnNetworkAvailableCallbacks {
            override fun onPositive() {
                runOnUiThread {
                    showSnackbar(
                        "Internet connection is available.",
                        binding.root,
                        1
                    )
                }
            }

            override fun onNegative() {
                runOnUiThread {
                    showSnackbar(
                        "No Internet Connection",
                        binding.root,
                        2
                    )
                }
            }

            override fun onError(s: String) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, s, Toast.LENGTH_LONG).show()
                }
            }
        })

        // Register Connection State Monitor
        try {
            Log.d("LOGIN_ACTIVITY", "Registering Connection Monitor")
            connectionMonitor.enable()
        } catch (e: SecurityException) {
            // Handle case where permission is missing
            Toast.makeText(this, "Network monitoring not available", Toast.LENGTH_LONG).show()
        }

        // Check Token
        tokenViewModel._accessToken.observe(this) {token ->

            if (token == null) {
                Log.d("MainActivity", "Token is null, starting LoginActivity")
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        lifecycleScope.launch {
            tokenViewModel.getToken()
        }

        miniPlayer.visibility = View.GONE
        fullPlayer.visibility = View.GONE

        miniPlayer.setOnClickListener {
            songViewModel.showFullPlayer()
        }

        miniPlayer.setFragmentManager(fragmentManager = supportFragmentManager)

        miniPlayer.observeViewModel()

        songViewModel.isFullPlayerVisible.observe(this) { isVisible ->
            if (isVisible){
                onOpenTrackSong()
                miniPlayer.visibility = View.GONE

            }else{
                onCloseTrackSong()
            }

        }

        songViewModel.currentSongDuration.observe(this){ duration ->
            lifecycleScope.launch {
                val song = songViewModel.playedSong.value
                if(song== null){
                    return@launch;
                }
                val songId = song.id
                if(duration < 0 ){
                    return@launch
                }
                val now = Date()
                val durationInSeconds = duration / 1000
                songViewModel.incrementSongPlayedTime(songId, durationInSeconds.toInt(),now)
            }
        }

        fullPlayer.post {
            val closeButton : ImageButton = fullPlayer.findViewById(R.id.collapse_button)
            closeButton.setOnClickListener {
                songViewModel.hideFullPlayer()
            }
        }

        songViewModel.anySongDeleted.observe (this){song ->
            AudioFileHelper.deleteFile(song.audioFileName)
            CoverFileHelper.deleteFile(song.coverFileName)

            Toast.makeText(this, "Song ${song.title} has been deleted", Toast.LENGTH_SHORT).show()
        }

        songViewModel.warningText.observe(this) {text ->
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }


        // User View
        userViewModel.profileResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    userViewModel._profile.postValue(response.data)
                }
                is ApiResponse.Failure -> {

                }
                is ApiResponse.Loading -> {

                }
            }
        }

        // Deep Link handler
        val intents = intent
        val data: Uri? = intents.data

        if (data != null && data.scheme == "purrytify" && data.host == "song") {
            val songId: String? = data.lastPathSegment

            songId?.let {
                loadSong(it.toInt())
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val data: Uri? = intent?.data

        if (data != null && data.scheme == "purrytify" && data.host == "song") {
            val songId: String? = data.lastPathSegment

            songId?.let {
                loadSong(it.toInt())
            }
        }
    }

    override fun onPause() {

        // Unregister
        connectionMonitor.disable()
        super.onPause()
    }

    override fun onDestroy() {

        super.onDestroy()
        connectionMonitor.disable()
    }

    private fun checkPermission() {
        permissionHelper.apply {
            if (!hasPermission(
                    this@MainActivity as AppCompatActivity,
                    Manifest.permission.CAMERA
                )
            ) {
                requestPermission(Manifest.permission.CAMERA, requestLauncher)
            }
        }
    }

    private fun permissionAlertDialog() {
        AlertDialog.Builder(this).apply {
            setTitle("Permission Required")
            setMessage("Camera permission is required to scan QR codes. Please enable it in settings.")

            setPositiveButton("Yes") { dialog, _ ->
                permissionHelper.openAppSettingPage(this@MainActivity as AppCompatActivity, resultLauncher)
                dialog.dismiss()
            }

            setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                checkPermission()
            }
            show()
        }
    }

    /**
     * Show a Snackbar with a message.
     *
     * @param message The message to display in the Snackbar.
     * @param view The view to find a parent from.
     * @param type The type of Snackbar (1 for short notice (e.g. connection available), 2 for indefinite (e.g. no connection)).
     */
    private fun showSnackbar(message: String, view: View, type: Int = 1) {
        try {
            val snackbar = Snackbar.make(
                view,
                message,
                if (type == 1) Snackbar.LENGTH_SHORT else Snackbar.LENGTH_INDEFINITE
            )

            val snackbarView = snackbar.view
            val params = snackbarView.layoutParams as FrameLayout.LayoutParams
            params.gravity = Gravity.TOP
            snackbarView.layoutParams = params

            snackbarView.setPadding(16, 4, 16, 4)

            val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.textSize = 18f
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            textView.setPadding(0,0,0,0)

            snackbar.setText(message)

            snackbar.setBackgroundTint(
                ContextCompat.getColor(
                    this,
                    if (type == 1) R.color.accent else R.color.muted
                )
            )

            snackbar.show()
        } catch (e: Exception) {

            Log.e("LOGIN_ACTIVITY", "Error showing Snackbar: ${e.message}")
        }
    }

    private fun loadSong(songId: Int) {
        onlineSongsViewModel.getSongById(songId, object : CoroutinesErrorHandler {
            override fun onError(message: String) {
                Log.e("DeepLink", "Error loading song: $message")
            }
        })

        topSongsViewModel.topSongs.observe(this) { songList ->
            when (songList) {
                is ApiResponse.Loading -> {
//                    binding.progressBar?.visibility = View.VISIBLE
                }

                is ApiResponse.Success -> {
//                    songAdapter.submitList(songList.data)
                    topSongsViewModel.updateSongsRepo(songList.data)
                }

                is ApiResponse.Failure -> {
//                    binding.progressBar?.visibility = View.GONE
                    // Handle error state
                }

                else -> {
                    Log.d("TopGlobalFragment", "State: ${songList.javaClass}")
                }
            }
        }

        topSongsViewModel.getTopSongsGlobal(
            coroutinesErrorHandler = object : CoroutinesErrorHandler {
                override fun onError(message: String) {
                    Log.e("TopGlobalFragment", "Error: ${message}")
                }
            },
        )

        songViewModel._isOnlineSong.postValue(true)

        onlineSongsViewModel.song.observe(this) { response ->
            when (response) {
                is ApiResponse.Success -> {
                    val song = response.data
                    Log.d("DeepLink", "Loaded song: ${song.title}")
                    lifecycleScope.launch {
                        songViewModel.playSong(
                            song = song.toSong(),
                            isOnline = true
                        )
                        songViewModel.showFullPlayer()
                    }
                }
                is ApiResponse.Failure -> {
                    Log.e("DeepLink", "Failed to load song")
                }
                is ApiResponse.Loading -> {
                    Log.d("DeepLink", "Loading song...")
                }
            }
        }
    }
}