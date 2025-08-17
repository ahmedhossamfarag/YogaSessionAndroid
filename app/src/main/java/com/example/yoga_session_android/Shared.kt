package com.example.yoga_session_android

import androidx.documentfile.provider.DocumentFile
import com.example.yoga_session_android.lib.Session

class Shared {
    companion object {
        var fileDocument: DocumentFile? = null
        var session: Session? = null
    }
}