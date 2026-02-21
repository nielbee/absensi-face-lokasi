package com.example.absensi.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.absensi.databinding.FragmentNotificationsBinding
import com.example.absensi.data.local.UserPreference




class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        // 1️⃣ Buat UserPreference dengan context
        val prefs = UserPreference(requireContext())

        // 2️⃣ Buat Factory
        val factory = NotificationsViewModelFactory(prefs)

        // 3️⃣ Buat ViewModel via Factory
        val viewModel = ViewModelProvider(this, factory)
            .get(NotificationsViewModel::class.java)

        // 4️⃣ Observe LiveData
        viewModel.name.observe(viewLifecycleOwner) {
            binding.textStudentName.text = it
        }

        viewModel.id.observe(viewLifecycleOwner) {
            binding.textStudentNisn.text = it
        }



        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
