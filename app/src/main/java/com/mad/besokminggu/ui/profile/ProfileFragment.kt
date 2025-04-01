package com.mad.besokminggu.ui.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mad.besokminggu.databinding.FragmentProfileBinding
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.ui.login.LoginActivity
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TokenViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri
import com.bumptech.glide.Glide

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()

    private val errorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            println("---------------- ERROR --------------")
            println(message)
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
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textProfile
        val logoutButton = binding.logoutButton
        val profileImage = binding.profileImage
        val textUsername = binding.textUsername
        val textLocation = binding.textLocation

        val baseImageUrl = "http://34.101.226.132:3000/uploads/profile-picture/"

        // Observe the profile data
        userViewModel.profileResponse.observe(viewLifecycleOwner) { profile ->
            when (profile) {
                is ApiResponse.Success -> {
                    val userProfile = profile.data
                    textView.text = userProfile.toString()
                    textUsername.text = userProfile.username
                    textLocation.text = userProfile.location
                    val imagePath = baseImageUrl + userProfile.profilePhoto

                    // Load the image using Glide
                    Glide.with(this)
                        .load(imagePath)
                        .into(profileImage)
                }
                is ApiResponse.Failure -> {
                    textView.text = "Error: ${profile.errorMessage}"
                    textUsername.text = "No username available"
                    textLocation.text = "No location available"
                }
                is ApiResponse.Loading -> {
                    textView.text = "Loading..."
                }
            }
        }

        userViewModel.getProfile(errorHandler)

        logoutButton.setOnClickListener({
            tokenViewModel.deleteToken()
            userViewModel.getProfile(errorHandler)

            Toast.makeText(requireContext(), "Logout Successfully!", Toast.LENGTH_SHORT).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}