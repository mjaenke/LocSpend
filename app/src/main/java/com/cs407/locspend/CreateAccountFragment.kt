package com.cs407.locspend

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.cs407.locspend.data.BudgetDatabase
import com.cs407.locspend.data.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest


/**
 * A simple [Fragment] subclass.
 * Use the [CreateAccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateAccountFragment : Fragment() {
    private lateinit var usernameEditText : EditText
    private lateinit var passwordEditText : EditText
    private lateinit var numberEditText : EditText
    private lateinit var emailEditText : EditText
    private lateinit var getStartedButton : Button
    private lateinit var errorText : TextView

    private lateinit var userPasswdKV: SharedPreferences
    private lateinit var budgetDB: BudgetDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_account, container, false)

        usernameEditText = view.findViewById(R.id.usernameEditText)
        passwordEditText = view.findViewById(R.id.passwordEditText)
        numberEditText = view.findViewById(R.id.numberEditText)
        emailEditText = view.findViewById(R.id.emailEditText)
        getStartedButton = view.findViewById(R.id.getStartedButton)
        errorText = view.findViewById(R.id.errorTextView)

        userPasswdKV = requireContext().getSharedPreferences(getString(R.string.userPasswdKV), Context.MODE_PRIVATE)
        budgetDB =  BudgetDatabase.getDatabase(requireContext())

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        usernameEditText.doAfterTextChanged {
            errorText.visibility = View.GONE
        }

        passwordEditText.doAfterTextChanged {
            errorText.visibility = View.GONE
        }
        getStartedButton.setOnClickListener {
            // get the username and password from the EditTexts
            val userName = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            val number = numberEditText.text.toString()
            val email = emailEditText.text.toString()

            lifecycleScope.launch {
                val createSuccess = withContext(Dispatchers.IO){
                    createAccount(userName, password)
                }
                if (createSuccess) {
                    // navigate to another fragment after successful signup
                    Log.d("Navigate", "Navigating to home fragment")
                    //findNavController().navigate(R.id.action_loginFragment_to_HomeFragment)
                } else {
                    errorText.visibility = View.VISIBLE
                }
            }

        }
    }

    private suspend fun createAccount(
        name: String,
        passwdPlain: String
    ) : Boolean {
        val hashedPassword = hash(passwdPlain)
        val userExist = userPasswdKV.contains(name)
        if (!userExist) {
            Log.d("New User", "Username does not exist: $name")
            val newUser = User(0, name)
            budgetDB.userDao().insert(newUser)
            userPasswdKV.edit().putString(name, hashedPassword).apply()
            return true
        } else {
            Log.d("Exisiting User", "Username already exists: $name")
            return false
        }
    }
    private fun hash(input: String): String {
        return MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            .fold("") { str, it -> str + "%02x".format(it) }
    }
}