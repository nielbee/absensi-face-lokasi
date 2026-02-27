package com.example.absensi.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.absensi.data.local.UserPreference
import com.example.absensi.remote.ApiService

class NotificationsViewModelFactory(
    private val api: ApiService,
    private val prefs: UserPreference
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(api, prefs) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
