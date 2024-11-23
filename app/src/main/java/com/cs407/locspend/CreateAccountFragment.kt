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
import android.widget.CheckBox
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
import androidx.navigation.Navigation



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
    private lateinit var termsAndServicesCheckBox : CheckBox
    private lateinit var errorText : TextView
    private lateinit var cancelCreate : TextView

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
        termsAndServicesCheckBox = view.findViewById(R.id.termsAndServicesCheckBox)
        errorText = view.findViewById(R.id.errorTextView)
        cancelCreate = view.findViewById(R.id.cancelCreate)

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

        numberEditText.doAfterTextChanged {
            errorText.visibility = View.GONE
        }

        emailEditText.doAfterTextChanged {
            errorText.visibility = View.GONE
        }

        termsAndServicesCheckBox.setOnClickListener {
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
                    Navigation.findNavController(view)
                        .navigate(R.id.action_createAccountFragment_to_homeFragment)
                } else {
                    errorText.visibility = View.VISIBLE
                }
            }
        }

        cancelCreate.setOnClickListener {
            Navigation.findNavController(view).navigate(R.id.action_createAccountFragment_to_loginFragment)
        }
    }

    private suspend fun createAccount(
        name: String,
        passwdPlain: String
    ) : Boolean {
        if (name.isEmpty() || passwdPlain.isEmpty() ||
            numberEditText.text.isEmpty() || emailEditText.text.isEmpty() || !termsAndServicesCheckBox.isChecked) {
            Log.d("Empty Fields", "Username/Password/Number/Email cannot be empty")
            return false
        }
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