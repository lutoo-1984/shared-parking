package com.sharedparking.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.ActivityRegisterBinding
import com.sharedparking.android.ui.MainActivity
import com.sharedparking.android.viewmodel.AuthViewModel
import com.sharedparking.android.viewmodel.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 注册Activity
 * 用户注册功能，包含手机验证码验证
 */
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化ViewModel
        authViewModel = ViewModelProvider(this).get(AuthViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    /**
     * 初始化UI
     */
    private fun setupUI() {
        // 设置返回按钮
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        // 注册按钮点击
        binding.btnRegister.setOnClickListener {
            attemptRegister()
        }

        // 发送验证码按钮
        binding.btnSendCaptcha.setOnClickListener {
            sendCaptcha()
        }

        // 已有账号登录
        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }

        // 服务条款勾选
        binding.cbAgreement.setOnCheckedChangeListener { _, isChecked ->
            binding.btnRegister.isEnabled = isChecked
        }
    }

    /**
     * 观察ViewModel数据
     */
    private fun observeViewModel() {
        // 观察注册状态
        authViewModel.registerState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    showLoading(true)
                }
                is AuthState.Success -> {
                    showLoading(false)
                    Toast.makeText(this, "注册成功", Toast.LENGTH_SHORT).show()
                    // 注册成功后自动登录（这里简化处理）
                    navigateToMain()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                else -> {
                    showLoading(false)
                }
            }
        }

        // 观察验证码发送状态
        authViewModel.captchaState.observe(this) { state ->
            when (state) {
                is com.sharedparking.android.viewmodel.CaptchaState.Loading -> {
                    binding.btnSendCaptcha.isEnabled = false
                    binding.btnSendCaptcha.text = "发送中..."
                }
                is com.sharedparking.android.viewmodel.CaptchaState.Success -> {
                    binding.btnSendCaptcha.isEnabled = false
                    binding.btnSendCaptcha.text = "60秒后重试"
                    startCaptchaCountdown()
                    Toast.makeText(this, "验证码发送成功", Toast.LENGTH_SHORT).show()
                }
                is com.sharedparking.android.viewmodel.CaptchaState.Error -> {
                    binding.btnSendCaptcha.isEnabled = true
                    binding.btnSendCaptcha.text = "发送验证码"
                    Toast.makeText(this, "验证码发送失败: ${state.message}", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    binding.btnSendCaptcha.isEnabled = true
                    binding.btnSendCaptcha.text = "发送验证码"
                }
            }
        }
    }

    /**
     * 尝试注册
     */
    private fun attemptRegister() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        val captcha = binding.etCaptcha.text.toString().trim()

        if (validateInput(username, email, phone, password, confirmPassword, captcha)) {
            // 先验证验证码
            CoroutineScope(Dispatchers.Main).launch {
                val isCodeValid = authViewModel.verifyCode(phone, captcha, "register")
                if (isCodeValid) {
                    // 验证码正确，进行注册
                    authViewModel.register(username, email, phone, password, password, captcha)
                } else {
                    showError("验证码错误或已过期")
                }
            }
        }
    }

    /**
     * 验证输入
     */
    private fun validateInput(
        username: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        captcha: String
    ): Boolean {
        // 验证用户名
        if (username.isEmpty()) {
            binding.tilUsername.error = "请输入用户名"
            return false
        }

        if (username.length < 2 || username.length > 20) {
            binding.tilUsername.error = "用户名长度为2-20位"
            return false
        }

        // 验证邮箱
        if (email.isEmpty()) {
            binding.tilEmail.error = "请输入邮箱"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "邮箱格式不正确"
            return false
        }

        // 验证手机号
        if (phone.isEmpty()) {
            binding.tilPhone.error = "请输入手机号"
            return false
        }

        if (phone.length != 11) {
            binding.tilPhone.error = "手机号格式不正确"
            return false
        }

        // 验证密码
        if (password.isEmpty()) {
            binding.tilPassword.error = "请输入密码"
            return false
        }

        if (password.length < 6) {
            binding.tilPassword.error = "密码长度至少6位"
            return false
        }

        // 验证确认密码
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "请确认密码"
            return false
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "两次输入的密码不一致"
            return false
        }

        // 验证验证码
        if (captcha.isEmpty()) {
            binding.tilCaptcha.error = "请输入验证码"
            return false
        }

        if (captcha.length != 6) {
            binding.tilCaptcha.error = "验证码为6位数字"
            return false
        }

        // 验证服务条款
        if (!binding.cbAgreement.isChecked) {
            Toast.makeText(this, "请同意服务条款和隐私政策", Toast.LENGTH_SHORT).show()
            return false
        }

        // 清除所有错误
        clearErrors()
        return true
    }

    /**
     * 清除所有错误提示
     */
    private fun clearErrors() {
        binding.tilUsername.error = null
        binding.tilEmail.error = null
        binding.tilPhone.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
        binding.tilCaptcha.error = null
    }

    /**
     * 发送验证码
     */
    private fun sendCaptcha() {
        val phone = binding.etPhone.text.toString().trim()

        if (phone.isEmpty()) {
            binding.tilPhone.error = "请输入手机号"
            return
        }

        if (phone.length != 11) {
            binding.tilPhone.error = "手机号格式不正确"
            return
        }

        binding.tilPhone.error = null
        authViewModel.sendCaptcha(phone, "register")
    }

    /**
     * 开始验证码倒计时
     */
    private fun startCaptchaCountdown() {
        object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnSendCaptcha.text = "${millisUntilFinished / 1000}秒后重试"
                binding.btnSendCaptcha.isEnabled = false
            }
            override fun onFinish() {
                binding.btnSendCaptcha.text = "获取验证码"
                binding.btnSendCaptcha.isEnabled = true
            }
        }.start()
    }

    /**
     * 显示加载状态
     */
    private fun showLoading(loading: Boolean) {
        binding.btnRegister.isEnabled = !loading
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
    }

    /**
     * 显示错误信息
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * 跳转到主页面
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * 跳转到登录页面
     */
    private fun navigateToLogin() {
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 重置注册状态
        authViewModel.resetRegisterState()
        authViewModel.resetCaptchaState()
    }
}