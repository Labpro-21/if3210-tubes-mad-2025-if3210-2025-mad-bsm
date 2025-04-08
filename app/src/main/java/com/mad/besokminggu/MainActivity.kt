package com.mad.besokminggu

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController

import androidx.navigation.ui.setupWithNavController
import androidx.transition.Visibility
import com.mad.besokminggu.databinding.ActivityMainBinding
import com.mad.besokminggu.worker.RefreshTokenWorker
import com.mad.besokminggu.manager.FileHelper
import com.mad.besokminggu.viewModels.SongTracksViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val songViewModel : SongTracksViewModel by viewModels()


    fun onOpenTrackSong(){

        binding.fullPlayer?.translationY = binding.fullPlayer?.height!!.toFloat()
        binding.fullPlayer?.alpha = 0f
        binding.fullPlayer?.visibility = View.VISIBLE
        binding.fullPlayer?.animate()
            ?.translationY(0f)
            ?.alpha(1f)
            ?.setDuration(300)
            ?.start()
        binding.miniPlayer.visibility = View.GONE
    }

    fun onCloseTrackSong(){
        binding.fullPlayer.animate()
            .translationY(binding.fullPlayer.height.toFloat())
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                binding.fullPlayer.visibility = View.GONE
                binding.fullPlayer.translationY = 0f
                binding.fullPlayer.alpha = 1f
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
                true
            }
        }

        binding.miniPlayer.visibility = View.GONE;
        binding.fullPlayer.visibility = View.GONE;

        binding.miniPlayer.setOnClickListener {
            songViewModel.showFullPlayer()
        }

        binding.miniPlayer.observeViewModel();

        songViewModel.isFullPlayerVisible.observe(this) { isVisible ->
            if (isVisible){
                onOpenTrackSong();
                binding.miniPlayer.visibility = View.GONE

            }else{
                onCloseTrackSong();
            }

        }

        binding.fullPlayer.post {
            val closeButton : ImageButton = binding.fullPlayer.findViewById(R.id.collapse_button)
            closeButton.setOnClickListener {
                songViewModel.hideFullPlayer()
            }
        }
    }
}