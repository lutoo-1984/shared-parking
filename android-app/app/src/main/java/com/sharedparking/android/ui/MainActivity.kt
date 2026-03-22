package com.sharedparking.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sharedparking.android.R
import com.sharedparking.android.databinding.ActivityMainBinding

/**
 * 主Activity，包含底部导航和Fragment容器
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 使用ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置工具栏
        setSupportActionBar(binding.toolbar)

        // 获取NavHostFragment和NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 配置AppBar与NavController的关联
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_search,
                R.id.navigation_bookings,
                R.id.navigation_profile
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 底部导航与NavController关联
        binding.bottomNavigationView.setupWithNavController(navController)

        // 设置导航监听
        setupNavigation()
    }

    /**
     * 设置导航监听
     */
    private fun setupNavigation() {
        // 监听目的地变化，更新标题等
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.toolbar.title = getString(R.string.tab_home)
                }
                R.id.navigation_search -> {
                    binding.toolbar.title = getString(R.string.tab_search)
                }
                R.id.navigation_bookings -> {
                    binding.toolbar.title = getString(R.string.tab_bookings)
                }
                R.id.navigation_profile -> {
                    binding.toolbar.title = getString(R.string.tab_profile)
                }
            }
        }
    }

    /**
     * 处理返回按钮
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    /**
     * 返回键处理
     */
    override fun onBackPressed() {
        // 如果当前不是首页，则导航到首页
        if (navController.currentDestination?.id != R.id.navigation_home) {
            binding.bottomNavigationView.selectedItemId = R.id.navigation_home
        } else {
            super.onBackPressed()
        }
    }
}