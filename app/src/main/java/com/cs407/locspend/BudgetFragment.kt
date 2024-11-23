package com.cs407.locspend

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cs407.locspend.data.Budget
import com.cs407.locspend.data.BudgetDatabase
import com.cs407.locspend.data.User
import com.cs407.locspend.data.UserBudgetRelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * A simple [Fragment] subclass.
 * Use the [BudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BudgetFragment (
    private val injectedUserViewModel: UserViewModel? = null
) : Fragment() {
    lateinit var totalBudgetText : TextView
    lateinit var totalSpentText : TextView
    lateinit var totalRemainingText : TextView
    lateinit var budgetPercentageText : TextView
    lateinit var monthPercentageText : TextView
    private lateinit var userViewModel: UserViewModel
    private lateinit var budgetDB : BudgetDatabase
    private lateinit var categories : List<String>

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        budgetDB = BudgetDatabase.getDatabase(requireContext())
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }
        categories =
            arrayOf("Dining", "Grocery", "Clothing","Transportation","Entertainment","Miscellaneous").toList()
        val userState = userViewModel.userState.value
        lifecycleScope.launch {
            var countBudget = budgetDB.budgetDao().userBudgetCount(userState.id)
            if (countBudget == 0) {
                for (i in categories.indices){
                    val newBudget = Budget(
                        budgetCategory = categories[i],
                        budgetAmount = 0.0,
                        budgetSpent = 0.0,
                        budgetId = 0,
                        budgetDetail = "",
                        budgetPath = "",
                        lastEdited = Calendar.getInstance().time
                    )
                    val newRowId = budgetDB.budgetDao().upsert(newBudget)
                    val newBudgetId = budgetDB.budgetDao().getByRowId(newRowId)
                    budgetDB.budgetDao().insertRelation(UserBudgetRelation(userState.id, newBudgetId))

                }
            }
        }
    }

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
        val budgetItemClothing = view.findViewById<View>(R.id.budget_table_clothing)
        val budgetItemTransportation = view.findViewById<View>(R.id.budget_table_transportation)
        val budgetItemEntertainment = view.findViewById<View>(R.id.budget_table_entertainment)
        val budgetItemMiscellaneous = view.findViewById<View>(R.id.budget_table_miscellaneous)

        // Total budget/spent text views
        totalBudgetText = view.findViewById<TextView>(R.id.total_budget)
        totalSpentText = view.findViewById<TextView>(R.id.total_spent)
        budgetPercentageText = view.findViewById<TextView>(R.id.percent_budget)
        totalRemainingText = view.findViewById<TextView>(R.id.total_remaining)
        monthPercentageText = view.findViewById<TextView>(R.id.percent_month)

        var categoryBudget = 0.0
        var budgetSpent = 0.0
        var remaining = 0.0

        // Set values for "Dining" budget item
        lifecycleScope.launch{

            val budget = budgetDB.budgetDao().getByCategory("Dining")
            categoryBudget = budget.budgetAmount
            budgetSpent = budget.budgetSpent
            remaining = categoryBudget - budgetSpent

        }

        budgetItemDining.findViewById<TextView>(R.id.category).text = "Dining"
        budgetItemDining.findViewById<TextView>(R.id.budget_value).text = categoryBudget.toString()
        budgetItemDining.findViewById<TextView>(R.id.spent_value).text = budgetSpent.toString()
        budgetItemDining.findViewById<TextView>(R.id.remaining_value).text = remaining.toString()

        // Set values for "Groceries" budget item
        lifecycleScope.launch{


            val budget = budgetDB.budgetDao().getByCategory("Grocery")
            categoryBudget = budget.budgetAmount
            budgetSpent = budget.budgetSpent
            remaining = categoryBudget - budgetSpent

        }

        budgetItemGroceries.findViewById<TextView>(R.id.category).text = "Grocery"
        budgetItemGroceries.findViewById<TextView>(R.id.budget_value).text = categoryBudget.toString()
        budgetItemGroceries.findViewById<TextView>(R.id.spent_value).text = budgetSpent.toString()
        budgetItemGroceries.findViewById<TextView>(R.id.remaining_value).text = remaining.toString()

        // Set values for "Clothing" budget item
        lifecycleScope.launch{

            val budget = budgetDB.budgetDao().getByCategory("Clothing")
            categoryBudget = budget.budgetAmount
            budgetSpent = budget.budgetSpent
            remaining = categoryBudget - budgetSpent

        }

        budgetItemClothing.findViewById<TextView>(R.id.category).text = "Clothing"
        budgetItemClothing.findViewById<TextView>(R.id.budget_value).text = categoryBudget.toString()
        budgetItemClothing.findViewById<TextView>(R.id.spent_value).text = budgetSpent.toString()
        budgetItemClothing.findViewById<TextView>(R.id.remaining_value).text = remaining.toString()

        //Set values for "Transportation" budget item
        lifecycleScope.launch{

            val budget = budgetDB.budgetDao().getByCategory("Transportation")
            categoryBudget = budget.budgetAmount
            budgetSpent = budget.budgetSpent
            remaining = categoryBudget - budgetSpent

        }

        budgetItemTransportation.findViewById<TextView>(R.id.category).text = "Transportation"
        budgetItemTransportation.findViewById<TextView>(R.id.budget_value).text = categoryBudget.toString()
        budgetItemTransportation.findViewById<TextView>(R.id.spent_value).text = budgetSpent.toString()
        budgetItemTransportation.findViewById<TextView>(R.id.remaining_value).text = remaining.toString()

        // Set values for "Entertainment" budget item
        lifecycleScope.launch{

            val budget = budgetDB.budgetDao().getByCategory("Entertainment")
            categoryBudget = budget.budgetAmount
            budgetSpent = budget.budgetSpent
            remaining = categoryBudget - budgetSpent

        }

        budgetItemEntertainment.findViewById<TextView>(R.id.category).text = "Entertainment"
        budgetItemEntertainment.findViewById<TextView>(R.id.budget_value).text = categoryBudget.toString()
        budgetItemEntertainment.findViewById<TextView>(R.id.spent_value).text = budgetSpent.toString()
        budgetItemEntertainment.findViewById<TextView>(R.id.remaining_value).text = remaining.toString()

        // Set values for "Miscellaneous" budget item
        lifecycleScope.launch{

            val budget = budgetDB.budgetDao().getByCategory("Miscellaneous")
            categoryBudget = budget.budgetAmount
            budgetSpent = budget.budgetSpent
            remaining = categoryBudget - budgetSpent

        }

        budgetItemMiscellaneous.findViewById<TextView>(R.id.category).text = "Miscellaneous"
        budgetItemMiscellaneous.findViewById<TextView>(R.id.budget_value).text = categoryBudget.toString()
        budgetItemMiscellaneous.findViewById<TextView>(R.id.spent_value).text = budgetSpent.toString()
        budgetItemMiscellaneous.findViewById<TextView>(R.id.remaining_value).text = remaining.toString()

        // Update the total budget information at the top
        lifecycleScope.launch{
            updateTotalBudget()
        }

        // Create onClickListener for the add spending button on each category
        val addButton = budgetItemDining.findViewById<ImageButton>(R.id.add_button)
        addButton.setOnClickListener {
            showDialog(budgetItemDining)
        }


        val addButton1 = budgetItemGroceries.findViewById<ImageButton>(R.id.add_button)
        addButton1.setOnClickListener {
            showDialog(budgetItemGroceries)
        }

        val addButton2 = budgetItemClothing.findViewById<ImageButton>(R.id.add_button)
        addButton2.setOnClickListener {
            showDialog(budgetItemClothing)
        }

        val addButton3 = budgetItemTransportation.findViewById<ImageButton>(R.id.add_button)
        addButton3.setOnClickListener {
            showDialog(budgetItemTransportation)
        }

        val addButton4 = budgetItemEntertainment.findViewById<ImageButton>(R.id.add_button)
        addButton4.setOnClickListener {
            showDialog(budgetItemEntertainment)
        }

        val addButton5 = budgetItemMiscellaneous.findViewById<ImageButton>(R.id.add_button)
        addButton5.setOnClickListener {
            showDialog(budgetItemMiscellaneous)
        }

    }

    private suspend fun addSpendingToBudgetTable(
        budgetTable: View,
        amount: Int
    ) {
        //get budget table category
        var category = budgetTable.findViewById<TextView>(R.id.category).text
        val currentSpent = budgetTable.findViewById<TextView>(R.id.spent_value)
        val currentRemaining = budgetTable.findViewById<TextView>(R.id.remaining_value)
        val userState = userViewModel.userState.value
        var total_budget = 0.0
        var spent = 0.0
        var remaining = 0.0

        // get the total budget for the category, amount spent, and current remaining
        lifecycleScope.launch {
            val budget = budgetDB.budgetDao().getByCategory(category.toString())
            total_budget = budget.budgetAmount
            spent = budget.budgetSpent
            var id = budget.budgetId

            if (spent != null) {
                spent = spent + amount
                budgetDB.budgetDao().upsertBudget(Budget(
                    budgetCategory = category.toString(),
                    budgetAmount = total_budget,
                    budgetSpent = spent,
                    budgetId = 1,
                    budgetDetail = "",
                    budgetPath = "",
                    lastEdited = Calendar.getInstance().time
                ), userState.id
                )
            }

            if (remaining != null) {
                remaining = total_budget - spent
            }

        }

        //put new values in the budget table
        currentSpent.text = spent.toString()
        currentRemaining.text = remaining.toString()
        updateTotalBudget()

    }

    private fun showDialog(categoryView : View) {
        val input = EditText(requireContext())
        input.hint = "$0.00"
        AlertDialog.Builder(requireContext())
            .setMessage("Amount:")
            .setView(input)
            .setPositiveButton("Ok") {dialog,_ ->
                val userInput = input.text.toString()
                lifecycleScope.launch {
                    withContext(Dispatchers.IO) {
                        addSpendingToBudgetTable(categoryView, userInput.toInt())
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") {dialog,_ ->
                dialog.dismiss()
            }
            .show()
            .create()
    }

    private suspend fun updateTotalBudget(){
        var totalBudget = 0.0
        var totalSpent = 0.0
        var newRemaining = 0.0
        var newPercentage = 0.0

        val userState = userViewModel.userState.value

        // add up all budget/spent totals from the database
        val budgets = budgetDB.deleteDao().getAllBudgetIdsByUser(userState.id)
        for (budget in budgets) {
            val current_budget = budgetDB.budgetDao().getById(budget)
            totalBudget += current_budget.budgetAmount
            totalSpent += current_budget.budgetSpent
        }

        if (totalBudget != 0.0){
            newPercentage = (totalBudget - totalSpent)/totalBudget
        } else {
            newPercentage = 0.0
        }
        newRemaining = totalBudget - totalSpent

        //get percentage through the month
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val total_days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var monthPercentage = ((total_days - day)/total_days) * 100


        totalRemainingText.text = newRemaining.toString()
        totalSpentText.text = totalSpent.toString()
        budgetPercentageText.text = newPercentage.toString()
        monthPercentageText.text = monthPercentage.toString()
    }
}


