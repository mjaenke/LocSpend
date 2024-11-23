package com.cs407.locspend

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.cs407.locspend.data.Budget
import com.cs407.locspend.data.BudgetDatabase
import kotlinx.coroutines.launch

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
            val countBudget = budgetDB.budgetDao().userBudgetCount(userState.id)
            if (countBudget == 0) {
                for (category in categories){
                    budgetDB.budgetDao().upsertBudget(
                        Budget(
                            budgetCategory = category,
                            budgetAmount = 0,
                            budgetSpent = 0,
                            budgetId = categories.indexOf(category),
                            budgetDetail = TODO(),
                            budgetPath = TODO(),
                            lastEdited = TODO(),
                        ), userState.id
                    )
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



    private suspend fun addSpending(
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
                    budgetCategory = category,
                    budgetAmount = total_budget,
                    budgetSpent = spent
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

        totalRemainingText.text = newRemaining.toString()
        totalSpentText.text = totalSpent.toString()
        budgetPercentageText.text = newPercentage.toString()


    }

    /*
    private fun loadBudgets() {
        val userState = userViewModel.userState.value
        val userId = userState.id

        if (userId == 0) {
            Log.e("NoteListFragment", "Invalid user ID")
            return
        }
        val pager = Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5),
            pagingSourceFactory = { noteDB.userDao().getUsersWithNoteListsByIdPaged(userId) }
        )
        lifecycleScope.launch {
            pager.flow.cachedIn(lifecycleScope).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

     */
}