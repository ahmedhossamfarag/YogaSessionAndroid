package com.example.yoga_session_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.example.yoga_session_android.databinding.MainActivityBinding

class MainActivity : ComponentActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pickButton.setOnClickListener {
            openFilePicker()
        }
    }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                // Persist permission if needed
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                // Now you can use the Uri (e.g. read file, upload, etc.)
                Log.d("FilePicker", "Selected file: $uri")
            }
        }

    fun openFilePicker() {
        openDocumentLauncher.launch(null) // restrict MIME types here
    }

}