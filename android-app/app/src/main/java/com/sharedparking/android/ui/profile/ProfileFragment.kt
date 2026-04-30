package com.sharedparking.android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.FragmentProfileBinding
import com.sharedparking.android.ui.auth.LoginActivity
import com.sharedparking.android.ui.booking.BookingDetailActivity
import com.sharedparking.android.ui.booking.MyBookingsActivity
import com.sharedparking.android.ui.messages.MessagesActivity
import com.sharedparking.android.ui.parking.MySpotsActivity
import com.sharedparking.android.viewmodel.AuthViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        setupUI()
        observeUserData()
    }

    private fun setupUI() {
        binding.ivAvatar.setOnClickListener {
            Toast.makeText(requireContext(), "头像功能开发中", Toast.LENGTH_SHORT).show()
        }

        binding.menuMySpots.setOnClickListener { navigateToMySpots() }
        binding.menuMyBookings.setOnClickListener { navigateToMyBookings() }
        binding.menuFavorites.setOnClickListener { navigateToFavorites() }
        binding.menuMessages.setOnClickListener { navigateToMessages() }
        binding.menuSettings.setOnClickListener { navigateToSettings() }
        binding.menuHelp.setOnClickListener { navigateToHelp() }
        binding.menuAbout.setOnClickListener { navigateToAbout() }
        binding.menuLogout.setOnClickListener { logout() }
    }

    private fun observeUserData() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.tvUserName.text = it.username
                binding.tvUserEmail.text = it.email
                binding.tvUserPhone.text = it.phone

                binding.tvVerificationStatus.text = if (it.isVerified) "已认证" else "未认证"
                binding.tvVerificationStatus.setTextColor(
                    requireContext().getColor(
                        if (it.isVerified) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                    )
                )
            }
        }
    }

    private fun navigateToMySpots() {
        startActivity(Intent(requireContext(), MySpotsActivity::class.java))
    }

    private fun navigateToMyBookings() {
        startActivity(Intent(requireContext(), MyBookingsActivity::class.java))
    }

    private fun navigateToFavorites() {
        // 跳转到搜索页展示收藏
        val intent = Intent(requireContext(), com.sharedparking.android.ui.parking.ParkingSearchActivity::class.java)
        startActivity(intent)
        Toast.makeText(requireContext(), "请在搜索页查看收藏", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToMessages() {
        startActivity(Intent(requireContext(), MessagesActivity::class.java))
    }

    private fun navigateToSettings() {
        Toast.makeText(requireContext(), "设置功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHelp() {
        Toast.makeText(requireContext(), "帮助功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToAbout() {
        Toast.makeText(requireContext(), "共享停车位 v1.0", Toast.LENGTH_LONG).show()
    }

    private fun logout() {
        authViewModel.logout()
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
