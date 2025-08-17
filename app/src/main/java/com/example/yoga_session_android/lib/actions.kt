package com.example.yoga_session_android.lib

import androidx.documentfile.provider.DocumentFile

fun pickJsonFile(dir: DocumentFile): DocumentFile? {
    val jsonFile = dir.listFiles().firstOrNull { it.name?.endsWith(".json") == true }
    return jsonFile
}