package com.example.absensi.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.absensi.data.local.UserPreference
import com.example.absensi.remote.ApiService

class HomeViewModelFactory(
    private val api: ApiService,
    private val pref: UserPreference
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(api, pref) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
