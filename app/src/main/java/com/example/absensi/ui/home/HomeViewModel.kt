package com.example.absensi.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.absensi.data.local.UserPreference

class HomeViewModel(
    private val pref: UserPreference
) : ViewModel() {

    private val _nama = MutableLiveData<String>()
    val nama: LiveData<String> = _nama

    private val _nisn = MutableLiveData<String>()
    val nisn: LiveData<String> = _nisn

    private val _jurusan = MutableLiveData<String>()
    val id_jurusan : LiveData<String> = _jurusan
    private val _text = MutableLiveData<String>().apply {
        value = "Selamat Datang"
    }
    val text: LiveData<String> = _text

    fun loadUser(){
        _nama.value = pref.getNama()
        _nisn.value = pref.getNisn()
        _jurusan.value = pref.getJurusan()

    }

    fun isLogin(): Boolean = pref.isLogin()
}