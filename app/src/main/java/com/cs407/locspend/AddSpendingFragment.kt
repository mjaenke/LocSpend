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
import com.cs407.locspend.data.BudgetDatabase
import java.util.Calendar


/**
 * A simple [Fragment] subclass.
 * Use the [AddSpendingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddSpendingFragment (
    private val injectedUserViewModel: UserViewModel? = null
) : Fragment() {
    private lateinit var budgetDB : BudgetDatabase
    private lateinit var logoutButton: Button
    private lateinit var userViewModel: UserViewModel
    private lateinit var addSpendingButton: ImageButton
    private lateinit var categoryView: TextView
    private lateinit var addSpendingButton2: ImageButton
    private lateinit var categoryView2: TextView
    private lateinit var addSpendingButton3: ImageButton
    private lateinit var categoryView3: TextView
    private lateinit var addSpendingButton4: ImageButton
    private lateinit var categoryView4: TextView
    private lateinit var addSpendingButton5: ImageButton
    private lateinit var categoryView5: TextView
    private lateinit var addSpendingButton6: ImageButton
    private lateinit var categoryView6: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_spending, container, false)
        logoutButton = view.findViewById(R.id.test_logout)

        // For Adding button
        addSpendingButton = view.findViewById<ImageButton>(R.id.imageButton)
        categoryView = view.findViewById<TextView>(R.id.categoryTextView)
        addSpendingButton2 = view.findViewById<ImageButton>(R.id.imageButton2)
        categoryView2 = view.findViewById<TextView>(R.id.categoryTextView2)
        addSpendingButton3 = view.findViewById<ImageButton>(R.id.imageButton3)
        categoryView3 = view.findViewById<TextView>(R.id.categoryTextView3)
        addSpendingButton4 = view.findViewById<ImageButton>(R.id.imageButton4)
        categoryView4 = view.findViewById<TextView>(R.id.categoryTextView4)
        addSpendingButton5 = view.findViewById<ImageButton>(R.id.imageButton5)
        categoryView5 = view.findViewById<TextView>(R.id.categoryTextView5)
        addSpendingButton6 = view.findViewById<ImageButton>(R.id.imageButton6)
        categoryView6 = view.findViewById<TextView>(R.id.categoryTextView6)



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
        var category = categoryView.text.toString()
        addSpendingButton.setOnClickListener {
            Log.d("Dialog1", "Button clicked")
            showDialog(category)
        }
        category = categoryView2.text.toString()
        addSpendingButton2.setOnClickListener {
            showDialog(category)
        }
        category = categoryView3.text.toString()
        addSpendingButton3.setOnClickListener {
            showDialog(category)
        }
        category = categoryView4.text.toString()
        addSpendingButton4.setOnClickListener {
            showDialog(category)
        }
        category = categoryView5.text.toString()
        addSpendingButton5.setOnClickListener {
            showDialog(category)
        }
        category = categoryView6.text.toString()
        addSpendingButton6.setOnClickListener {
            showDialog(category)
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
                lifecycleScope.launch {
                    addSpending(category, userInput.toDouble())
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private suspend fun addSpending(
        budgetCategory: String,
        amount: Double
    ) {
        val userState = userViewModel.userState.value
        var total_budget = 0.0
        var spent = 0.0

        // get the total budget for the category, amount spent, and current remaining
        lifecycleScope.launch {
            val budget = budgetDB.budgetDao().getByCategory(budgetCategory.toString())
            total_budget = budget.budgetAmount
            spent = budget.budgetSpent
            var id = budget.budgetId

            if (spent != null) {
                spent = spent + amount
                budgetDB.budgetDao().upsertBudget(
                    Budget(
                        budgetCategory = budgetCategory.toString(),
                        budgetAmount = total_budget,
                        budgetSpent = spent,
                        budgetId = 1,
                        budgetDetail = "",
                        budgetPath = "",
                        lastEdited = Calendar.getInstance().time
                    ), userState.id
                )
            }
        }
    }
}