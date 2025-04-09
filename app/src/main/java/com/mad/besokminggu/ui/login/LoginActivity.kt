package com.mad.besokminggu.ui.login

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mad.besokminggu.MainActivity
import com.mad.besokminggu.R
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.databinding.ActivityLoginBinding
import com.mad.besokminggu.network.ConnectionStateMonitor
import com.mad.besokminggu.network.OnNetworkAvailableCallbacks
import com.mad.besokminggu.viewModels.AuthViewModel
import com.mad.besokminggu.viewModels.CoroutinesErrorHandler
import com.mad.besokminggu.viewModels.TokenViewModel
import com.mad.besokminggu.viewModels.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    private val _profile = MutableLiveData<Profile>()
    val profile: LiveData<Profile> get() = _profile

    private lateinit var connectionMonitor: ConnectionStateMonitor

    private val errorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            runOnUiThread {
                Log.e("LOGIN_ACTIVITY", "Error :  ${message}")
                binding.loadingContainer?.visibility = View.GONE
                showLoginFailed("Error! $message")
            }
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val password = binding.password
        val loginButton = binding.ButtonLogin
        val loading = binding.loadingContainer

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
                    Toast.makeText(this@LoginActivity, s, Toast.LENGTH_LONG).show()
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


        authViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both email / password is valid
            loginButton.isEnabled = loginState.isDataValid

            if (loginState.emailError != null) {
                email.error = getString(loginState.emailError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        authViewModel.loginResponse.observe(this@LoginActivity) {
            when (it) {
                is ApiResponse.Failure -> {
                    loading?.visibility = View.GONE
                    Log.e("LOGIN_ACTIVITY", "Status : ${it.code} | Message: ${it.errorMessage}",)
                    showLoginFailed(it.errorMessage)
                }
                is ApiResponse.Loading -> {
                    loading?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    Log.d("LOGIN_ACTIVITY", "Successfully Login",)

                    lifecycleScope.launch {
                        tokenViewModel.saveToken(it.data.accessToken, it.data.refreshToken)
                        showLoginSuccess()
                    }

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        tokenViewModel._accessToken.observe(this@LoginActivity) { token ->
            if (token != null) {
                // TODO(Implement Token Verification)

                showLoginSuccess()
                setResult(RESULT_OK)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            loading?.visibility = View.GONE
        }


        email.afterTextChanged {
            authViewModel.loginDataChanged(
                email.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                authViewModel.loginDataChanged(
                    email.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        if (loginButton.isEnabled) {
                            authViewModel.login(
                                LoginBody(email.text.toString(), password.text.toString()),
                                errorHandler
                            )
                        }
                }
                false
            }

            loginButton.setOnClickListener {
                loading?.visibility = View.VISIBLE
                if (loginButton.isEnabled){
                    authViewModel.login(
                        LoginBody(email.text.toString(), password.text.toString()),
                        errorHandler
                    )
                }
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

    private fun showLoginSuccess() {
        val welcome = getString(R.string.welcome)

        userViewModel.profileResponse.observe(this@LoginActivity, Observer { it ->
            when (it) {
                is ApiResponse.Failure -> {
                    Log.e("LOGIN_ACTIVITY", "Status : ${it.code} | Message: ${it.errorMessage}")
                    showLoginFailed(it.errorMessage)
                }
                is ApiResponse.Loading -> {
                    binding.loadingContainer?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    Log.d("LOGIN_ACTIVITY", "Successfully Login",)
                    binding.loadingContainer?.visibility = View.GONE

                    // Initiate successful logged-in experience
                    Toast.makeText(applicationContext, "$welcome ${it.data.username}", Toast.LENGTH_LONG).show()
                }
            }
        })

        userViewModel.getProfile(errorHandler)
    }

    private fun showLoginFailed(errorString: String) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
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
            val textView = snackbarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
            textView.textSize = 18f
            textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER

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
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}