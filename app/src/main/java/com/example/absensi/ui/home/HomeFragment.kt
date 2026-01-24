package com.example.absensi.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.absensi.data.local.UserPreference
import com.example.absensi.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val pref = UserPreference(requireContext())
        val factory = HomeViewModelFactory(pref)

        homeViewModel =
            ViewModelProvider(this, factory)[HomeViewModel::class.java]

        homeViewModel.loadUser()

        homeViewModel.nama.observe(viewLifecycleOwner) {
            binding.txtNama.text = it
        }

        homeViewModel.nisn.observe(viewLifecycleOwner) {
            binding.txtNisn.text = it
        }

        homeViewModel.id_jurusan.observe(viewLifecycleOwner) {
            binding.txtJurusan.text = it
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

