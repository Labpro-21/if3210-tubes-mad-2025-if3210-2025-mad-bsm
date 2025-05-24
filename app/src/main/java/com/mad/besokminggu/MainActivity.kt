package com.mad.besokminggu

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.mad.besokminggu.data.model.toSong
import com.mad.besokminggu.databinding.ActivityMainBinding
import com.mad.besokminggu.manager.AudioFileHelper
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.network.ConnectionStateMonitor
import com.mad.besokminggu.network.OnNetworkAvailableCallbacks
import com.mad.besokminggu.ui.adapter.OnlineSongAdapter
import com.mad.besokminggu.ui.login.LoginActivity
import com.mad.besokminggu.ui.topSongs.TopSongsViewModel
import com.mad.besokminggu.ui.viewTracks.MiniPlayerView
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.OnlineSongsViewModel
import com.mad.besokminggu.viewModels.SongTracksViewModel
import com.mad.besokminggu.viewModels.TokenViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val songViewModel : SongTracksViewModel by viewModels()
    private val userViewModel : UserViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private val topSongsViewModel: TopSongsViewModel by viewModels()
    private val onlineSongsViewModel: OnlineSongsViewModel by viewModels()

    private lateinit var connectionMonitor: ConnectionStateMonitor

    fun onOpenTrackSong(){
        val fullPlayer = binding.fullPlayer
        if(fullPlayer == null)return

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
        if(fullPlayer == null){
            return
        }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FileHelper.init(context = applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView? = binding.navView
        val fullPlayer : FragmentContainerView? = binding.fullPlayer
        val miniPlayer : MiniPlayerView = binding.miniPlayer

            // Wait until views are loaded
        binding.root.post {
            val navController = findNavController(R.id.nav_host_fragment_activity_main)

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
        fullPlayer?.visibility = View.GONE

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

        fullPlayer?.post {
            val closeButton : ImageButton = fullPlayer.findViewById(R.id.collapse_button)
            closeButton.setOnClickListener {
                songViewModel.hideFullPlayer()
            }
        }

        songViewModel.anySongDeleted.observe (this){song ->
            AudioFileHelper.deleteFile(song.audioFileName)
            CoverFileHelper.deleteFile(song.coverFileName)
            AudioPlayerManager.stop()

            Toast.makeText(this, "Song ${song.title} has been deleted", Toast.LENGTH_SHORT).show()
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
//
//        // Top Songs
//        topSongsViewModel.topSongs.observe(this) { response ->
//            when (response) {
//                is ApiResponse.Success -> {
//                    Log.d("TOP_SONGS", "Top Songs: ${response.data}")
//                }
//                is ApiResponse.Failure -> {
//                    Log.e("TOP_SONGS", "Top Songs: Failed to load")
//                }
//                is ApiResponse.Loading -> {
//                    Log.d("TOP_SONGS", "Top Songs: Loading...")
//                }
//            }
//        }
//        topSongsViewModel.getTopSongsGlobal(
//            coroutinesErrorHandler = object : CoroutinesErrorHandler {
//                override fun onError(message: String) {
//                    Log.e("TOP_SONGS", "Error: ${message}")
//                }
//            },
//        )
//        topSongsViewModel.getTopSongsCountry(
//            country = "ID",
//            coroutinesErrorHandler = object : CoroutinesErrorHandler {
//                override fun onError(message: String) {
//                    Log.e("TOP_SONGS", "Error: ${message}")
//                }
//            },
//        )

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

    override fun onPause() {
        // Unregister
        connectionMonitor.disable()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectionMonitor.disable()
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