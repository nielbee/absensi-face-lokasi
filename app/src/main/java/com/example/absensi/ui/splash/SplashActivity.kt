package com.example.absensi.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.absensi.MainActivity
import com.example.absensi.data.local.UserPreference
import com.example.absensi.ui.login.LoginActivity
import java.io.File

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (isDeviceRooted()) {
            showRootWarning()
        } else {
            proceedToNextActivity()
        }
    }

    private fun isDeviceRooted(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk",
            "/sbin/su",
            "/system/bin/su",
            "/system/xbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun showRootWarning() {
        AlertDialog.Builder(this)
            .setTitle("Perangkat Tidak Aman")
            .setMessage("Aplikasi ini tidak dapat berjalan pada perangkat yang di-root demi keamanan.")
            .setCancelable(false)
            .setPositiveButton("Keluar") { _, _ ->
                finish()
            }
            .show()
    }

    private fun proceedToNextActivity() {
        val pref = UserPreference(this)
        Handler(Looper.getMainLooper()).postDelayed({
            if (pref.isLogin()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, SPLASH_DELAY)
    }
}
