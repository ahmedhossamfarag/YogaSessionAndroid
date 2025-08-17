package com.example.yoga_session_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.documentfile.provider.DocumentFile
import com.example.yoga_session_android.databinding.MainActivityBinding
import com.example.yoga_session_android.lib.pickJsonFile

class MainActivity : ComponentActivity() {
    private lateinit var binding: MainActivityBinding
    private var fileDocument: DocumentFile? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pickFolderButton.setOnClickListener {
            openFilePicker()
        }
        binding.nextButton.setOnClickListener {
            goToDetails()
        }
    }

    private val openDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                handleDirPath(DocumentFile.fromTreeUri(this, uri))
            }
        }

    fun openFilePicker() {
        openDocumentLauncher.launch(null)
    }

    fun handleDirPath(dir: DocumentFile?){
        this.fileDocument = if (dir != null) pickJsonFile(dir) else null
        if (fileDocument != null){
            binding.filePathTextView.text = fileDocument?.uri.toString()
            binding.fileIconImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.file_blue))
            binding.nextButton.isEnabled = true
        }else{
            binding.filePathTextView.text = getString(R.string.no_file_selected)
            binding.fileIconImage.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.file_gray))
            binding.nextButton.isEnabled = false
        }
    }

    fun goToDetails(){
        Shared.fileDocument = fileDocument
        val intent = Intent(this, DetailsActivity::class.java)
        startActivity(intent)
    }

}