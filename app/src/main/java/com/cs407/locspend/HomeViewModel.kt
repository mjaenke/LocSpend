package com.cs407.locspend
import androidx.lifecycle.ViewModel

class HomeViewModel: ViewModel() {
    var address: String = ""
    var category: String = ""
    var budget: Double = 0.0
    var spent: Double = 0.0
    var remaining: Double = 0.0
    var percentMonth: Int = 0
    var percentBudget: Int = 0
}