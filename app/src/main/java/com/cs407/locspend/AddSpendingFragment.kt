package com.cs407.locspend

import android.content.Context
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.compose.material3.Button
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView


/**
 * A simple [Fragment] subclass.
 * Use the [AddSpendingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddSpendingFragment (
    private val injectedUserViewModel: UserViewModel? = null
) : Fragment() {
    private lateinit var logoutButton: Button
    private lateinit var userViewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }

        val addSpendingButton = view?.findViewById<ImageButton>(R.id.imageButton)
        val categoryView = view?.findViewById<TextView>(R.id.categoryTextView)
        val addSpendingButton2 = view?.findViewById<ImageButton>(R.id.imageButton2)
        val categoryView2 = view?.findViewById<TextView>(R.id.categoryTextView2)
        val addSpendingButton3 = view?.findViewById<ImageButton>(R.id.imageButton3)
        val categoryView3 = view?.findViewById<TextView>(R.id.categoryTextView3)
        val addSpendingButton4 = view?.findViewById<ImageButton>(R.id.imageButton4)
        val categoryView4 = view?.findViewById<TextView>(R.id.categoryTextView4)
        val addSpendingButton5 = view?.findViewById<ImageButton>(R.id.imageButton5)
        val categoryView5 = view?.findViewById<TextView>(R.id.categoryTextView5)
        val addSpendingButton6 = view?.findViewById<ImageButton>(R.id.imageButton6)
        val categoryView6 = view?.findViewById<TextView>(R.id.categoryTextView6)



        if (addSpendingButton != null) {
            val category = categoryView?.text.toString()
            addSpendingButton.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton2 != null) {
            val category = categoryView2?.text.toString()
            addSpendingButton2.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton3 != null) {
            val category = categoryView3?.text.toString()
            addSpendingButton3.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton4 != null) {
            val category = categoryView4?.text.toString()
            addSpendingButton4.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton5 != null) {
            val category = categoryView5?.text.toString()
            addSpendingButton5.setOnClickListener {
                showDialog(category)
            }
        }

        if (addSpendingButton6 != null) {
            val category = categoryView6?.text.toString()
            addSpendingButton6.setOnClickListener {
                showDialog(category)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_spending, container, false)
        logoutButton = view.findViewById(R.id.test_logout)

        // Inflate the layout for this fragment
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logoutButton.setOnClickListener {
            // navigate to another fragment after successful logout
            userViewModel.setUser(UserState())
            lifecycleScope.launch {
                Navigation.findNavController(view)
                    .navigate(R.id.action_addSpendingFragment_to_loginFragment)
            }
        }
    }

    private fun showDialog(category : String) {
        val input = EditText(requireContext())
        input.hint = "$0.00"
        AlertDialog.Builder(requireContext())
            .setMessage("Amount:")
            .setView(input)
            .setPositiveButton("Ok") {dialog,_ ->
                val userInput = input.text.toString()
                // needs to change in the database...
                //addSpending(category, userInput.toInt())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }

    }
}