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
import com.cs407.locspend.data.UserBudgetRelation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.absoluteValue

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
    lateinit var dateText : TextView
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
        dateText = view.findViewById<TextView>(R.id.date)

        lifecycleScope.launch {
            initializeBudgets()
            updateBudgetUI(requireView())
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

    private fun addSpendingToBudgetTable(
        budgetTable: View,
        amount: Double
    ) {
        //get budget table category
        var category = budgetTable.findViewById<TextView>(R.id.category).text
        Log.d("Category Modified", category.toString())
        val currentSpent = budgetTable.findViewById<TextView>(R.id.spent_value)
        val currentRemaining = budgetTable.findViewById<TextView>(R.id.remaining_value)
        val userState = userViewModel.userState.value
        var total_budget = 0.0
        var spent = 0.0
        var remaining = 0.0

        // get the total budget for the category, amount spent, and current remaining
        lifecycleScope.launch {
            val budget = budgetDB.budgetDao().getByCategory(category.toString(), userState.id)
            Log.d("Budget", budget.toString())
            total_budget = budget.budgetAmount
            spent = budget.budgetSpent
            Log.d("Top Spent", spent.toString())
            Log.d("Amount", amount.toString())

            spent = spent + amount
            budgetDB.budgetDao().upsertBudget(Budget(
                budgetCategory = category.toString(),
                budgetAmount = total_budget,
                budgetSpent = spent,
                budgetId = budget.budgetId,
                budgetDetail = "",
                budgetPath = "",
                lastEdited = Calendar.getInstance().time
            ), userState.id)

            remaining = total_budget - spent

            //put new values in the budget table
            updateBudgetUI(requireView())
            updateTotalBudget()
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
                lifecycleScope.launch {
                    addSpendingToBudgetTable(categoryView, userInput.toDouble())
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
        var newThruBudgetPercent = 0

        val userState = userViewModel.userState.value

        // add up all budget/spent totals from the database
        val budgets = budgetDB.deleteDao().getAllBudgetIdsByUser(userState.id)
        for (budget in budgets) {
            val current_budget = budgetDB.budgetDao().getById(budget)
            totalBudget += current_budget.budgetAmount
            totalSpent += current_budget.budgetSpent
        }

        if (totalBudget != 0.0){
            newPercentage = (totalSpent/totalBudget) * 100
            newThruBudgetPercent = newPercentage.toInt()
        }
        newRemaining = totalBudget - totalSpent

        //get percentage through the month
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val total_days = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthPercentage = ((day / total_days.toDouble()) * 100).toInt()

        // Format values to 2 decimal places
        val totalBudgetFormatted = String.format("%.2f", totalBudget)
        val totalSpentFormatted = String.format("%.2f", totalSpent)
        val newRemainingFormatted = String.format("%.2f", newRemaining.absoluteValue)

        // Update UI with formatted values
        totalBudgetText.text = "Total Budget: $$totalBudgetFormatted"
        if (newRemaining < 0) {
            totalRemainingText.text = "Total Remaining: -$$newRemainingFormatted"
        } else {
            totalRemainingText.text = "Total Remaining: $$newRemainingFormatted"
        }
        totalSpentText.text = "Total Spent: $$totalSpentFormatted"
        budgetPercentageText.text = newThruBudgetPercent.toString() + "%"
        monthPercentageText.text = monthPercentage.toString() + "%"
    }

    private suspend fun updateBudgetUI(view: View) {
        withContext(Dispatchers.IO) {
            val userState = userViewModel.userState.value
            Log.d("User State UI", userState.toString())
            val categoriesWithBudgets = categories.map { category ->
                category to budgetDB.budgetDao().getByCategory(category, userState.id)
            }

            withContext(Dispatchers.Main) {
                val calendar = Calendar.getInstance()
                val month  = arrayOf("January", "February", "March", "April", "May", "June", "July", "August",
                    "September", "October", "November", "December")[Calendar.getInstance().get(Calendar.MONTH)]

                val year = calendar.get(Calendar.YEAR).toString()
                dateText.text = month + " " + year
                Log.d("Categories", categoriesWithBudgets.toString())
                categoriesWithBudgets.forEach { (category, budget) ->
                    Log.d("Budget", budget.toString())
                    val budgetItem = when (category) {
                        "Dining" -> view.findViewById<View>(R.id.budget_table_dining)
                        "Grocery" -> view.findViewById<View>(R.id.budget_table_groceries)
                        "Clothing" -> view.findViewById<View>(R.id.budget_table_clothing)
                        "Transportation" -> view.findViewById<View>(R.id.budget_table_transportation)
                        "Entertainment" -> view.findViewById<View>(R.id.budget_table_entertainment)
                        "Miscellaneous" -> view.findViewById<View>(R.id.budget_table_miscellaneous)
                        else -> null
                    }

                    budgetItem?.let {
                        Log.d("Budget Item", budget.toString())
                        val categoryBudget = budget.budgetAmount
                        val budgetSpent = budget.budgetSpent
                        val remaining = categoryBudget - budgetSpent

                        val categoryBudgetFormatted = String.format("%.2f", categoryBudget)
                        val budgetSpentFormatted = String.format("%.2f", budgetSpent)
                        val remainingFormatted = String.format("%.2f", remaining.absoluteValue)



                        it.findViewById<TextView>(R.id.category).text = category
                        it.findViewById<TextView>(R.id.budget_value).text = "$$categoryBudgetFormatted"
                        it.findViewById<TextView>(R.id.spent_value).text = "$$budgetSpentFormatted"
                        if (remaining < 0) {
                            it.findViewById<TextView>(R.id.remaining_value).text = "-$$remainingFormatted"
                        } else {
                            it.findViewById<TextView>(R.id.remaining_value).text = "$$remainingFormatted"
                        }
                    }
                }
            }
        }
    }

    private suspend fun initializeBudgets() {
        val userState = userViewModel.userState.value
        withContext(Dispatchers.IO) {
            Log.d("User State", userState.toString())
            val userId = userState.id

            val countBudget = budgetDB.budgetDao().userBudgetCount(userId)
            if (countBudget == 0) {
                categories.forEach { category ->
                    val newBudget = Budget(
                        budgetCategory = category,
                        budgetAmount = 0.0,
                        budgetSpent = 0.0,
                        budgetId = 0,
                        budgetDetail = "",
                        budgetPath = "",
                        lastEdited = Calendar.getInstance().time
                    )
                    val newRowId = budgetDB.budgetDao().upsert(newBudget)
                    val newBudgetId = budgetDB.budgetDao().getByRowId(newRowId)
                    budgetDB.budgetDao().insertRelation(UserBudgetRelation(userId, newBudgetId))
                }
            }
        }
    }
}


