package com.mad.besokminggu.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.mad.besokminggu.MainActivity
import com.mad.besokminggu.R
import com.mad.besokminggu.network.ApiResponse
import com.mad.besokminggu.data.model.LoginBody
import com.mad.besokminggu.data.model.Profile
import com.mad.besokminggu.databinding.ActivityLoginBinding
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

    private val errorHandler = object : CoroutinesErrorHandler {
        override fun onError(message: String) {
            runOnUiThread {
                println("---------------- ERROR --------------")
                println(message)
                binding.loadingContainer?.visibility = View.GONE
                showLoginFailed("Error! $message")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = binding.email
        val password = binding.password
        val loginButton = binding.ButtonLogin
        val loading = binding.loadingContainer!!

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
                    loading.visibility = View.GONE
                    println("-------------- ERROR --------------")
                    println(it.code)
                    println(it.errorMessage)
                    showLoginFailed(it.errorMessage)
                }
                is ApiResponse.Loading -> {
                    loading.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    println("-------------- SUCCESS --------------")
                    println(it)

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
                setResult(Activity.RESULT_OK)

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            loading.visibility = View.GONE
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
                        authViewModel.login(
                            LoginBody(email.text.toString(), password.text.toString()),
                            errorHandler
                        )
                }
                false
            }

            loginButton.setOnClickListener {
                loading.visibility = View.VISIBLE
                authViewModel.login(
                    LoginBody(email.text.toString(), password.text.toString()),
                    errorHandler
                )
            }
        }
    }

    private fun showLoginSuccess() {
        val welcome = getString(R.string.welcome)

        userViewModel.profileResponse.observe(this@LoginActivity, Observer { it ->
            when (it) {
                is ApiResponse.Failure -> {
                    println("-------------- ERROR --------------")
                    println(it.code)
                    println(it.errorMessage)
                    showLoginFailed(it.errorMessage)
                }
                is ApiResponse.Loading -> {
                    binding.loadingContainer?.visibility = View.VISIBLE
                }
                is ApiResponse.Success -> {
                    println("-------------- SUCCESS --------------")
                    println(it)
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