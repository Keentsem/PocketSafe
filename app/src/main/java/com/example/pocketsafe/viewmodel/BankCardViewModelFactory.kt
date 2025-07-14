package com.example.pocketsafe.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating BankCardViewModel instances
 * Follows the manual ViewModel factory pattern used throughout the app
 */
class BankCardViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BankCardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BankCardViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
