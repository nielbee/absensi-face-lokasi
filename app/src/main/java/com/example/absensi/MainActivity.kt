package com.example.absensi

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.absensi.data.local.UserPreference
import com.example.absensi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pref: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = UserPreference(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Dynamically set start destination to Dashboard if logged in
        if (savedInstanceState == null && pref.isLogin()) {
            val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
            navGraph.setStartDestination(R.id.navigation_dashboard)
            navController.graph = navGraph
        }

        // Link Bottom Navigation with NavController
        navView.setupWithNavController(navController)
    }
}
