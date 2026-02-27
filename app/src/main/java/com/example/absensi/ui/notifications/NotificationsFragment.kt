package com.example.absensi.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.absensi.databinding.FragmentNotificationsBinding
import com.example.absensi.data.local.UserPreference
import com.example.absensi.remote.RetrofitClient
import com.example.absensi.BuildConfig as AppConfig

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        val prefs = UserPreference(requireContext())
        val api = RetrofitClient.api
        val factory = NotificationsViewModelFactory(api, prefs)

        val viewModel = ViewModelProvider(this, factory)
            .get(NotificationsViewModel::class.java)

        viewModel.name.observe(viewLifecycleOwner) {
            binding.textStudentName.text = it
        }

        viewModel.id.observe(viewLifecycleOwner) {
            binding.textStudentNisn.text = it
        }

        viewModel.ijinResult.observe(viewLifecycleOwner) { result ->
            if (result.isNotEmpty()) {
                Toast.makeText(requireContext(), result, Toast.LENGTH_LONG).show()
            }
        }

        binding.buttonIjin.setOnClickListener {
            val keterangan = binding.textIjin.text.toString()
            if (keterangan.isEmpty()) {
                Toast.makeText(requireContext(), "Keterangan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.sendIjin(AppConfig.APP_KEY, keterangan)
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
