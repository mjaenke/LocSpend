package com.cs407.locspend

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView

/**
 * A simple [Fragment] subclass.
 * Use the [BudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BudgetFragment : Fragment() {
    lateinit var totalBudgetText : TextView
    lateinit var totalSpentText : TextView
    lateinit var totalRemainingText : TextView
    lateinit var budgetPercentageText : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find each included layout by its unique ID
        val budgetItemDining = view.findViewById<View>(R.id.budget_table_dining)
        val budgetItemGroceries = view.findViewById<View>(R.id.budget_table_groceries)

        // Total budget/spent text views
        totalBudgetText = view.findViewById<TextView>(R.id.total_budget)
        totalSpentText = view.findViewById<TextView>(R.id.total_spent)
        budgetPercentageText = view.findViewById<TextView>(R.id.percent_budget)
        totalRemainingText = view.findViewById<TextView>(R.id.total_remaining)

        // Set values for "Dining" budget item
        budgetItemDining.findViewById<TextView>(R.id.category).text = "Dining"
        budgetItemDining.findViewById<TextView>(R.id.budget_value).text = "$125.00"
        budgetItemDining.findViewById<TextView>(R.id.spent_value).text = "$45.00"
        budgetItemDining.findViewById<TextView>(R.id.remaining_value).text = "$80.00"

        // Set values for "Groceries" budget item
        budgetItemGroceries.findViewById<TextView>(R.id.category).text = "Groceries"
        budgetItemGroceries.findViewById<TextView>(R.id.budget_value).text = "$300.00"
        budgetItemGroceries.findViewById<TextView>(R.id.spent_value).text = "$150.00"
        budgetItemGroceries.findViewById<TextView>(R.id.remaining_value).text = "$150.00"


        // Create onClickListener for the add spending button on each category
        val categoryView = budgetItemDining
        val addButton = categoryView.findViewById<TextView>(R.id.category)
        addButton.setOnClickListener {
            showDialog(categoryView)
        }
    }



    private fun addSpending(
        budgetTable: View,
        amount: Int
    ) {
        //get budget table category
        var category = budgetTable.findViewById<TextView>(R.id.category).text
        val currentSpent = budgetTable.findViewById<TextView>(R.id.spent_value)
        val currentRemaining = budgetTable.findViewById<TextView>(R.id.remaining_value)

        // get the total budget for the category, amount spent, and current remaining
        var categoryBudget = 100
        var spent = stringToInt(currentSpent.text.toString())
        var remaining = stringToInt(currentSpent.text.toString())

        if (spent != null) {
            spent = spent + amount
        }

        if (remaining != null) {
            remaining = remaining - amount
        }

        //update this in the database

        //put new values in the budget table
        currentSpent.text = spent.toString()
        currentRemaining.text = remaining.toString()

    }

    fun stringToInt(input: String): Int? {
        return try {
            input.toInt()
        } catch (e: NumberFormatException) {
            null // Return null if the string cannot be converted
        }
    }

    private fun showDialog(categoryView : View) {
        val input = EditText(requireContext())
        input.hint = "$0.00"
        AlertDialog.Builder(requireContext())
            .setMessage("Amount:")
            .setView(input)
            .setPositiveButton("Ok") {dialog,_ ->
                val userInput = input.text.toString()
                addSpending(categoryView, userInput.toInt())
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }

    }

    private fun updateTotalBudget(){
        val total = 100
        val newSpent = 0
        val newRemaining = 0
        var newPercentage = 0

        // need database to find real values!

        if (total != 0){
            newPercentage = (total - newRemaining)/total
        } else {
            newPercentage = 0
        }
        totalRemainingText.text = newRemaining.toString()
        totalSpentText.text = newSpent.toString()
        budgetPercentageText.text = newPercentage.toString()






    }
}

