package com.example.absensi.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absensi.data.local.UserPreference

class NotificationsViewModel(
    private val prefs : UserPreference
) : ViewModel() {


    private val _studentName = MutableLiveData<String>()
    private val _studentNisn = MutableLiveData<String>()
    private val _studentClas = MutableLiveData<String>()
    val studentName: LiveData<String> = _studentName
    val studentNisn: LiveData<String> = _studentNisn
    val studentClas: LiveData<String> = _studentClas



    init {
        _studentName.value = prefs.getNama()
        _studentNisn.value = prefs.getNisn()
        _studentClas.value = prefs.getJurusan()

    }
}