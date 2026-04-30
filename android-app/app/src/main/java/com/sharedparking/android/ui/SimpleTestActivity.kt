package com.sharedparking.android.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sharedparking.android.databinding.ActivitySimpleTestBinding

class SimpleTestActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySimpleTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySimpleTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnTest.setOnClickListener {
            binding.btnTest.text = "点击成功！"
        }
    }
}