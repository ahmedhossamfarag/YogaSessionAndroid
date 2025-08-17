package com.example.yoga_session_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.example.yoga_session_android.databinding.DetailsActivityBinding
import com.example.yoga_session_android.lib.ParsingException
import com.example.yoga_session_android.lib.Session
import com.example.yoga_session_android.lib.parseSession

class DetailsActivity : ComponentActivity() {
    lateinit var binding: DetailsActivityBinding
    lateinit var session: Session

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DetailsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.detailsNextButton.setOnClickListener {
            goToShow()
        }
        parseFile()
    }

    fun parseFile() {
        try{
            session = parseSession(Shared.fileDocument!!, contentResolver)
            binding.detailsListView.adapter = DetailsAdapter(this, session.segments)
            binding.detailsNextButton.isEnabled = true
        }catch (e: ParsingException){
            binding.errorTextView.text = e.message
        }catch (e: Exception){
            binding.errorTextView.text = e.message
        }
    }

    fun goToShow() {
        Shared.session = session
        val intent = Intent(this, ShowActivity::class.java)
        startActivity(intent)
    }
}