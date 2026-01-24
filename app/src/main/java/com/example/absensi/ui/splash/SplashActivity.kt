package com.example.absensi.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.absensi.MainActivity
import com.example.absensi.data.local.UserPreference
import com.example.absensi.ui.login.LoginActivity

class SplashActivity : AppCompatActivity() {

    private val SPLASH_DELAY = 2000L // 2 detik

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
