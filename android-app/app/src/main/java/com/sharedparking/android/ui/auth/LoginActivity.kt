package com.sharedparking.android.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.sharedparking.android.databinding.ActivityLoginBinding
import com.sharedparking.android.ui.MainActivity
import com.sharedparking.android.viewmodel.AuthViewModel
import com.sharedparking.android.viewmodel.AuthState

/**
 * 登录Activity
 * 用户邮箱/密码登录，验证码登录选项
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
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

        // 登录按钮点击
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        // 注册链接点击
        binding.tvRegister.setOnClickListener {
            navigateToRegister()
        }

        // 忘记密码点击
        binding.tvForgotPassword.setOnClickListener {
            navigateToForgotPassword()
        }

        // 验证码登录切换
        binding.switchLoginMethod.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // 切换到验证码登录
                showCaptchaLogin()
            } else {
                // 切换到密码登录
                showPasswordLogin()
            }
        }

        // 发送验证码按钮
        binding.btnSendCaptcha.setOnClickListener {
            sendCaptcha()
        }

        // 演示模式点击
        binding.tvDemoMode.setOnClickListener {
            authViewModel.demoLogin()
        }
    }

    /**
     * 观察ViewModel数据
     */
    private fun observeViewModel() {
        // 观察登录状态
        authViewModel.loginState.observe(this) { state ->
            when (state) {
                is AuthState.Loading -> {
                    showLoading(true)
                    binding.tvDemoMode.visibility = View.GONE
                }
                is AuthState.Success -> {
                    showLoading(false)
                    binding.tvDemoMode.visibility = View.GONE
                    Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                    navigateToMain()
                }
                is AuthState.Error -> {
                    showLoading(false)
                    showError(state.message)
                    binding.tvDemoMode.visibility = View.VISIBLE
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
     * 尝试登录
     */
    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val captcha = binding.etCaptcha.text.toString().trim()

        if (binding.switchLoginMethod.isChecked) {
            // 验证码登录
            if (validateCaptchaInput(email, captcha)) {
                // 实际应该调用验证码登录API，这里简化为直接调用密码登录
                authViewModel.login(email, "default_password")
            }
        } else {
            // 密码登录
            if (validatePasswordInput(email, password)) {
                authViewModel.login(email, password)
            }
        }
    }

    /**
     * 验证密码登录输入
     */
    private fun validatePasswordInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "请输入邮箱"
            return false
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = "请输入密码"
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "邮箱格式不正确"
            return false
        }

        if (password.length < 6) {
            binding.tilPassword.error = "密码长度至少6位"
            return false
        }

        binding.tilEmail.error = null
        binding.tilPassword.error = null
        return true
    }

    /**
     * 验证验证码登录输入
     */
    private fun validateCaptchaInput(phone: String, captcha: String): Boolean {
        if (phone.isEmpty()) {
            binding.tilPhone.error = "请输入手机号"
            return false
        }

        if (captcha.isEmpty()) {
            binding.tilCaptcha.error = "请输入验证码"
            return false
        }

        if (phone.length != 11) {
            binding.tilPhone.error = "手机号格式不正确"
            return false
        }

        if (captcha.length != 6) {
            binding.tilCaptcha.error = "验证码为6位数字"
            return false
        }

        binding.tilPhone.error = null
        binding.tilCaptcha.error = null
        return true
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
        authViewModel.sendCaptcha(phone, "login")
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
     * 显示验证码登录界面
     */
    private fun showCaptchaLogin() {
        binding.layoutPassword.visibility = View.GONE
        binding.layoutCaptcha.visibility = View.VISIBLE
        binding.btnLogin.text = "验证码登录"
    }

    /**
     * 显示密码登录界面
     */
    private fun showPasswordLogin() {
        binding.layoutPassword.visibility = View.VISIBLE
        binding.layoutCaptcha.visibility = View.GONE
        binding.btnLogin.text = "登录"
    }

    /**
     * 显示加载状态
     */
    private fun showLoading(loading: Boolean) {
        binding.btnLogin.isEnabled = !loading
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
     * 跳转到注册页面
     */
    private fun navigateToRegister() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    /**
     * 跳转到忘记密码页面
     */
    private fun navigateToForgotPassword() {
        // 跳转到忘记密码页面
        Toast.makeText(this, "忘记密码功能开发中", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 重置登录状态
        authViewModel.resetLoginState()
        authViewModel.resetCaptchaState()
    }
}