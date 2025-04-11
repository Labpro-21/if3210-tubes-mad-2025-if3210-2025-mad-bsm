package com.mad.besokminggu.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
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

@AndroidEntryPoint
class ProfileFragment : Fragment() {

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
        val profileImage = binding.profileImage
        val textUsername = binding.textUsername
        val textLocation = binding.textLocation

        val likedSongsCount = binding.textLikedSongsNumber
        val totalSongsCount = binding.textTotalSongsNumber
        val listenedSongsCount = binding.textListenedSongsNumber

        val baseImageUrl = "http://34.101.226.132:3000/uploads/profile-picture/"

        // init connection monitor
        connectionMonitor = ConnectionStateMonitor(requireContext(), object : OnNetworkAvailableCallbacks {
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

            userViewModel.getProfile(errorHandler)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}