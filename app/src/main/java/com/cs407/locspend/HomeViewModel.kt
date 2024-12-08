package com.cs407.locspend
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {
    var address: String = ""
    var category: String = ""
    var budget: Int = 0
    var spent: Int = 0
    var percentMonth: Int = 0
    var percentBudget: Int = 0
}