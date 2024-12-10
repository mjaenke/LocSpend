package com.cs407.locspend

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import com.cs407.locspend.data.BudgetDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.navigation.Navigation

class LoginFragment(
    private val injectedUserViewModel: UserViewModel? = null 
) : Fragment()  {

    private lateinit var usernameEditText : EditText
    private lateinit var passwordEditText : EditText
    private lateinit var loginButton : Button
    private lateinit var createAccount : TextView
    private lateinit var errorText: TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var budgetDB: BudgetDatabase

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

        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }

        userPasswdKV = requireContext().getSharedPreferences(getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        budgetDB =  BudgetDatabase.getDatabase(requireContext())

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
            val userName = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()

            // navigate to another fragment after successful login
            lifecycleScope.launch {
                val validUser = withContext(Dispatchers.IO) {
                    getUserPasswd(userName, password)
                }
                if (validUser) {
                    val userId = withContext(Dispatchers.IO) {
                        budgetDB.userDao().getByName(userName).userId
                    }
                    userViewModel.setUser(
                        UserState(
                            id = userId,
                            name = userName,
                            passwd = password
                        )
                    )
                    Log.d("Valid User", "True")
                    Navigation.findNavController(view).navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    Log.d("Valid User", "False")
                    errorText.visibility = View.VISIBLE
                }
            }

            // show an error message if either username or password is empty
            if (userName.isEmpty() || password.isEmpty()){
                errorText.text = "Username/Password cannot be empty"
                errorText.visibility = View.VISIBLE
            }
        }

        createAccount.setOnClickListener{
            lifecycleScope.launch {
                // Clear DB for testing
                /*
                userPasswdKV.edit().clear().apply()
                budgetDB.deleteDao().deleteAllUsers()
                budgetDB.deleteDao().deleteAllBudgets()
                 */
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
        val hashedPassword = hash(passwdPlain)
        val userExist = userPasswdKV.contains(name)
        val password = userPasswdKV.getString(name, null)

        if (userExist && hashedPassword != password){
            Log.d("Password", "Incorrect Password for user: $name")
            return false
        }

        if (!userExist) {
            Log.d("New User", "Username does not exist: $name")
            // Display: User does not exist. Please create an account.
            return false
        }
        return true
    }

    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}