package com.example.absensi.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import com.example.absensi.data.local.UserPreference
import com.example.absensi.databinding.ActivityLoginBinding
import com.example.absensi.remote.RetrofitClient
import com.example.absensi.remote.VerifikasiResponse
import com.example.absensi.ui.face.FaceRegisterActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var pref: UserPreference

    private val api = RetrofitClient.api

    private val viewModel: LoginViewModel by lazy {
        LoginViewModel(api, pref)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pref = UserPreference(this)

        observeState()
        setupAction()
    }

    private fun setupAction() {
        binding.btnLogin.setOnClickListener {
            val id = binding.edtId.text.toString().trim()

            if (id.isEmpty()) {
                Toast.makeText(this, "Masukkan ID GTK", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.verifikasiId(id)
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->

                    // Loading
                    binding.progressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    // Error
                    state.error?.let {
                        Toast.makeText(this@LoginActivity, it, Toast.LENGTH_SHORT).show()
                    }

                    // Popup
                    if (state.showPopup && state.verifikasiData != null) {
                        showVerifikasiPopup(state.verifikasiData)
                    }
                }
            }
        }
    }

    private fun showVerifikasiPopup(data: VerifikasiResponse) {
        VerifikasiDialog(
            this,
            data,
            onConfirm = {
                pref.saveVerifikasi(data)
                viewModel.popupConfirmed()

                startActivity(
                    Intent(this, FaceRegisterActivity::class.java)
                )
            },
            onCancel = {
                viewModel.popupConfirmed()
            }
        ).show()
    }
}
