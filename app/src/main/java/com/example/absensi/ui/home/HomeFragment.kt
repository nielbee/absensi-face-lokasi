package com.example.absensi.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.absensi.data.local.UserPreference
import com.example.absensi.databinding.FragmentHomeBinding
import com.example.absensi.remote.RetrofitClient

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var pref: UserPreference

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        pref = UserPreference(requireContext())
        val api = RetrofitClient.api
        val factory = HomeViewModelFactory(api, pref)

        homeViewModel =
            ViewModelProvider(this, factory)[HomeViewModel::class.java]

        homeViewModel.loadUser()

        setupAction()
        observeViewModel()

        return binding.root
    }

    private fun setupAction() {
        binding.btnHadir.setOnClickListener {
            openBrowser()
        }
        
        binding.btnTidakHadir.setOnClickListener {
            openBrowser()
        }
    }

    private fun openBrowser() {
        val idGuru = pref.getId()
        if (idGuru.isNotEmpty()) {
            val url = "https://absenguruv2.smkn1labuanbajo.com/public/detail/$idGuru"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "ID Guru tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        homeViewModel.nama.observe(viewLifecycleOwner) {
            binding.txtNama.text = it
        }

        homeViewModel.nisn.observe(viewLifecycleOwner) {
            binding.txtNisn.text = it
        }

        homeViewModel.detailData.observe(viewLifecycleOwner) { detailList ->
            detailList?.let { list ->
                val hadirCount = list.count { it.keterangan == "HADIR" }
                val tidakHadirCount = list.size - hadirCount
                
                binding.txtCountHadir.text = hadirCount.toString()
                binding.txtCountTidakHadir.text = tidakHadirCount.toString()

                val lastEntry = list.firstOrNull { it.tanggal_kehadiran != null }
                if (lastEntry != null) {
                    binding.txtJurusan.text = "Terakhir: ${lastEntry.jam_masuk ?: "-"} s/d ${lastEntry.jam_keluar ?: "-"}"
                } else {
                    binding.txtJurusan.text = "Belum ada riwayat absensi"
                }
            }
        }

        homeViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        homeViewModel.isLoading.observe(viewLifecycleOwner) {
            // Handle loading state
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
