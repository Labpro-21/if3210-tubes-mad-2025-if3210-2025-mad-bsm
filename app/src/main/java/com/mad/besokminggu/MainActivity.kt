package com.mad.besokminggu

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController

import androidx.navigation.ui.setupWithNavController
import com.mad.besokminggu.databinding.ActivityMainBinding
import com.mad.besokminggu.manager.AudioFileHelper
import com.mad.besokminggu.manager.AudioPlayerManager
import com.mad.besokminggu.manager.CoverFileHelper
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.ui.viewTracks.MiniPlayerView
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val songViewModel : SongTracksViewModel by viewModels()

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
            navView?.setupWithNavController(navController)

            // Setup for NavigationView (landscape)
            val sideNavView = findViewById<com.google.android.material.navigation.NavigationView>(R.id.side_nav_view)
            sideNavView?.setNavigationItemSelectedListener { menuItem ->
                menuItem.isChecked = true
                navController.navigate(menuItem.itemId)
                onCloseTrackSong()
                true
            }
        }

        miniPlayer.visibility = View.GONE
        fullPlayer?.visibility = View.GONE

        miniPlayer.setOnClickListener {
            songViewModel.showFullPlayer()
        }

        miniPlayer.observeViewModel()

        songViewModel.isFullPlayerVisible.observe(this) { isVisible ->
            if (isVisible){
                onOpenTrackSong()
                binding.miniPlayer.visibility = View.GONE

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

    }
}