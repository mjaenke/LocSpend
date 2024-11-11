package com.cs407.locspend

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/**
 * A simple [Fragment] subclass.
 * Use the [BudgetFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BudgetFragment : Fragment() {
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
    }
}