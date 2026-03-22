package com.sharedparking.android.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.FragmentProfileBinding
import com.sharedparking.android.ui.auth.LoginActivity
import com.sharedparking.android.viewmodel.AuthViewModel

/**
 * 个人中心Fragment
 * 用户信息、设置、退出登录等
 */
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

        // 初始化ViewModel
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        setupUI()
        observeUserData()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置头像点击
        binding.ivAvatar.setOnClickListener {
            // 修改头像
        }

        // 设置菜单项点击
        binding.menuMySpots.setOnClickListener {
            navigateToMySpots()
        }

        binding.menuMyBookings.setOnClickListener {
            navigateToMyBookings()
        }

        binding.menuFavorites.setOnClickListener {
            navigateToFavorites()
        }

        binding.menuMessages.setOnClickListener {
            navigateToMessages()
        }

        binding.menuSettings.setOnClickListener {
            navigateToSettings()
        }

        binding.menuHelp.setOnClickListener {
            navigateToHelp()
        }

        binding.menuAbout.setOnClickListener {
            navigateToAbout()
        }

        binding.menuLogout.setOnClickListener {
            logout()
        }
    }

    /**
     * 观察用户数据
     */
    private fun observeUserData() {
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            user?.let {
                // 更新用户信息
                binding.tvUserName.text = it.username
                binding.tvUserEmail.text = it.email
                binding.tvUserPhone.text = it.phone

                // 显示已验证状态
                if (it.isVerified) {
                    binding.tvVerificationStatus.text = "已认证"
                    binding.tvVerificationStatus.setTextColor(requireContext().getColor(android.R.color.holo_green_dark))
                } else {
                    binding.tvVerificationStatus.text = "未认证"
                    binding.tvVerificationStatus.setTextColor(requireContext().getColor(android.R.color.holo_red_dark))
                }
            }
        }
    }

    /**
     * 跳转到我的车位
     */
    private fun navigateToMySpots() {
        // val intent = Intent(requireContext(), MySpotsActivity::class.java)
        // startActivity(intent)
    }

    /**
     * 跳转到我的预订
     */
    private fun navigateToMyBookings() {
        // val intent = Intent(requireContext(), MyBookingsActivity::class.java)
        // startActivity(intent)
    }

    /**
     * 跳转到我的收藏
     */
    private fun navigateToFavorites() {
        // 跳转到收藏页面
    }

    /**
     * 跳转到消息中心
     */
    private fun navigateToMessages() {
        // val intent = Intent(requireContext(), MessagesActivity::class.java)
        // startActivity(intent)
    }

    /**
     * 跳转到设置
     */
    private fun navigateToSettings() {
        // 跳转到设置页面
    }

    /**
     * 跳转到帮助
     */
    private fun navigateToHelp() {
        // 跳转到帮助页面
    }

    /**
     * 跳转到关于
     */
    private fun navigateToAbout() {
        // 跳转到关于页面
    }

    /**
     * 退出登录
     */
    private fun logout() {
        authViewModel.logout()
        navigateToLogin()
    }

    /**
     * 跳转到登录页面
     */
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