package com.cs407.locspend

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.cs407.locspend.data.Budget
import com.cs407.locspend.data.BudgetDatabase
import kotlinx.coroutines.launch
import java.util.Calendar


/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment (
    private val injectedUserViewModel: UserViewModel? = null
): Fragment() {
    private lateinit var diningBudgetEditText: EditText
    private lateinit var groceryBudgetEditText: EditText
    private lateinit var clothingBudgetEditText: EditText
    private lateinit var transportationBudgetEditText: EditText
    private lateinit var entertainmentBudgetEditText: EditText
    private lateinit var miscBudgetEditText: EditText
    private lateinit var budgetDB: BudgetDatabase

    private lateinit var logoutButton: Button
    private lateinit var userViewModel: UserViewModel

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        budgetDB = BudgetDatabase.getDatabase(requireContext())
        userViewModel = if (injectedUserViewModel != null) {
            injectedUserViewModel
        } else {
            ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        }
        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        logoutButton = view.findViewById(R.id.test_logout)
        diningBudgetEditText = view.findViewById(R.id.diningBudgetEditText)
        groceryBudgetEditText = view.findViewById(R.id.groceryBudgetEditText)
        clothingBudgetEditText = view.findViewById(R.id.clothingBudgetEditText)
        transportationBudgetEditText = view.findViewById(R.id.transportationBudgetEditText)
        entertainmentBudgetEditText = view.findViewById(R.id.entertainmentBudgetEditText)
        miscBudgetEditText = view.findViewById(R.id.miscBudgetEditText)


        // Initialize dark mode switch
        val darkModeSwitch: Switch = view.findViewById(R.id.darkModeSwitch)

        // Set the switch state based on saved preferences
        val isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkModeEnabled

        // Listener for the switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the preference
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()

            // Apply the theme
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Set listeners for each EditText
        setBudgetEditTextListener(diningBudgetEditText, "Dining")
        setBudgetEditTextListener(groceryBudgetEditText, "Grocery")
        setBudgetEditTextListener(clothingBudgetEditText, "Clothing")
        setBudgetEditTextListener(transportationBudgetEditText, "Transportation")
        setBudgetEditTextListener(entertainmentBudgetEditText, "Entertainment")
        setBudgetEditTextListener(miscBudgetEditText, "Miscellaneous")

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Fetch and display current budget values
        populateBudgetValues()

        logoutButton.setOnClickListener {
            // navigate to another fragment after successful logout
            lifecycleScope.launch {
                Navigation.findNavController(view)
                    .navigate(R.id.action_settingsFragment_to_loginFragment)
            }
        }
    }

    private fun setBudgetEditTextListener(editText: EditText, category: String) {
        editText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                val budget = editText.text.toString().toDoubleOrNull()
                if (budget != null) {
                    addBudgetDB(category, budget)
                }
                true
            } else {
                false
            }
        }
    }

    private fun addBudgetDB(category: String, budget: Double) {
        val userState = userViewModel.userState.value
        lifecycleScope.launch {
            val cur = budgetDB.budgetDao().getByCategory(category, userState.id)
            budgetDB.budgetDao().upsertBudget(
                Budget(
                budgetCategory = category,
                budgetAmount = budget,
                budgetSpent = cur.budgetSpent,
                budgetId = cur.budgetId,
                budgetDetail = "",
                budgetPath = "",
                lastEdited = Calendar.getInstance().time
            ), userState.id
            )
        }
    }

    private fun populateBudgetValues() {
        val userState = userViewModel.userState.value
        lifecycleScope.launch {
            val diningBudget = budgetDB.budgetDao().getByCategory("Dining", userState.id).budgetAmount
            diningBudgetEditText.setText(String.format("%.2f", diningBudget))

            val groceryBudget = budgetDB.budgetDao().getByCategory("Grocery", userState.id).budgetAmount
            groceryBudgetEditText.setText(String.format("%.2f", groceryBudget))

            val clothingBudget = budgetDB.budgetDao().getByCategory("Clothing", userState.id).budgetAmount
            clothingBudgetEditText.setText(String.format("%.2f", clothingBudget))

            val transportationBudget = budgetDB.budgetDao().getByCategory("Transportation", userState.id).budgetAmount
            transportationBudgetEditText.setText(String.format("%.2f", transportationBudget))

            val entertainmentBudget = budgetDB.budgetDao().getByCategory("Entertainment", userState.id).budgetAmount
            entertainmentBudgetEditText.setText(String.format("%.2f", entertainmentBudget))

            val miscBudget = budgetDB.budgetDao().getByCategory("Miscellaneous", userState.id).budgetAmount
            miscBudgetEditText.setText(String.format("%.2f", miscBudget))
        }
    }
}