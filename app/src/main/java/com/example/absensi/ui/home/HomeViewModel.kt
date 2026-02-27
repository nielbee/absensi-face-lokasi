package com.example.absensi.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.absensi.data.local.UserPreference
import com.example.absensi.remote.ApiService
import com.example.absensi.remote.DetailResponse
import kotlinx.coroutines.launch

class HomeViewModel(
    private val api: ApiService,
    private val pref: UserPreference
) : ViewModel() {

    private val _detailData = MutableLiveData<List<DetailResponse>?>()
    val detailData: LiveData<List<DetailResponse>?> = _detailData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _nama = MutableLiveData<String>()
    val nama: LiveData<String> = _nama

    private val _nisn = MutableLiveData<String>()
    val nisn: LiveData<String> = _nisn

    fun loadUser() {
        _nama.value = pref.getNama()
        _nisn.value = pref.getId()
        fetchDetail()
    }

    private fun fetchDetail() {
        val id = pref.getId()
        if (id.isEmpty()) return

        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = api.detail(id)
                if (response.isSuccessful) {
                    _detailData.value = response.body()
                } else {
                    _error.value = "Gagal mengambil data: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
