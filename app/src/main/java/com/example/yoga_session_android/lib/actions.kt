package com.example.yoga_session_android.lib

import java.io.File

fun pickJsonFile(dirPath: String): String? {
    val jsonFile = File(dirPath).listFiles()?.firstOrNull { it.extension == "json" }
    return jsonFile?.absolutePath
}