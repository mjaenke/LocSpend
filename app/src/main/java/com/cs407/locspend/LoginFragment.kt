package com.cs407.locspend

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import kotlinx.coroutines.launch
import java.security.MessageDigest

class LoginFragment() : Fragment()  {

    private lateinit var usernameEditText : EditText
    private lateinit var passwordEditText : EditText
    private lateinit var loginButton : Button
    private lateinit var createAccount : TextView
    private lateinit var errorText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        loginButton = view.findViewById(R.id.loginButton)
        createAccount = view.findViewById(R.id.createAccount)
        errorText = view.findViewById(R.id.errorTextView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usernameEditText.doAfterTextChanged {
            errorText.visibility = View.GONE
        }

        passwordEditText.doAfterTextChanged {
            errorText.visibility = View.GONE
        }

        // set the login button click action
        loginButton.setOnClickListener {
            // get the username and password from the EditTexts
            val name = usernameEditText.text.toString()
            val passwd = passwordEditText.text.toString()

            // navigate to another fragment after successful login
            lifecycleScope.launch {
                val login_success = getUserPasswd(name, passwd)
                if (login_success) {
                    // navigate to the home page
                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    errorText.visibility = View.VISIBLE
                }
            }

            // show an error message if either username or password is empty
            if (name.isEmpty() || passwd.isEmpty()){
                errorText.text = "Username/Password cannot be empty"
                errorText.visibility = View.VISIBLE
            }
        }

        createAccount.setOnClickListener{
            lifecycleScope.launch {
                //navigate to the create account page
                Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_createAccountFragment)
            }
        }
    }

    private suspend fun getUserPasswd(
        name: String,
        passwdPlain: String
    ): Boolean {
        // hash the plain password
        val hashedPasswd = hash(passwdPlain)

        return true
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }


}