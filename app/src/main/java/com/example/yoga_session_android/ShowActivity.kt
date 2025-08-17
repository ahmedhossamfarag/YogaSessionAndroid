package com.example.yoga_session_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.yoga_session_android.databinding.ShowActivityBinding

class ShowActivity : ComponentActivity() {
    lateinit var binding: ShowActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ShowActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}