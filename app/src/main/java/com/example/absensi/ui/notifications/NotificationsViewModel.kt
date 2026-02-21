package com.example.absensi.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absensi.data.local.UserPreference

class NotificationsViewModel(
    private val prefs : UserPreference
) : ViewModel() {


    private val _name = MutableLiveData<String>()
    private val _id = MutableLiveData<String>()

    val name: LiveData<String> = _name
    val id: LiveData<String> = _id




    init {
        _name.value = prefs.getNama()
        _id.value = prefs.getId()


    }
}