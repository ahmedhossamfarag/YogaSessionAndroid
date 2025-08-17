package com.example.yoga_session_android.lib

import android.content.ContentResolver
import androidx.documentfile.provider.DocumentFile
import org.json.JSONObject

class ParsingException(message: String) : Exception(message)

fun parseMetadata(json: JSONObject) : SessionMetadata {
    if (arrayOf("id", "title", "category", "defaultLoopCount", "tempo").any { !json.has(it) }){
        throw ParsingException("Invalid session file. File is missing metadata.")
    }
    return SessionMetadata(
        json.getString("id"),
        json.getString("title"),
        json.getString("category"),
        json.getInt("defaultLoopCount"),
        json.getString("tempo")
    )
}

fun parseAssets(json: JSONObject, dir: DocumentFile) : Assets {
    if (!json.has("images") || !json.has("audio")) {
        throw ParsingException("Invalid session file. File is missing assets.")
    }
    val images = dir.findFile("Images")
    val audio = dir.findFile("Audio")
    if (images == null || audio == null) {
        throw ParsingException("Invalid session file. File is missing assets.")
    }
    val assets = Assets(
        json.getJSONObject("images").let {seq ->
            sequence {
                for (i in 0 until seq.length()) {
                    val name = seq.names()!!.getString(i)
                    yield(
                        name to images.findFile(seq.getString(name))
                    )
                }
            }.toMap() },
        json.getJSONObject("audio").let {seq ->
            sequence {
                for (i in 0 until seq.length()) {
                    val name = seq.names()!!.getString(i)
                    yield(
                        name to audio.findFile(seq.getString(name))
                    )
                }
            }.toMap() }
    )
    assets.images.forEach { if (it.value == null) throw ParsingException("Invalid session file. Image file ${it.value} does not exist.") }
    assets.audios.forEach { if (it.value == null) throw ParsingException("Invalid session file. Audio file ${it.value} does not exist.") }
    return assets
}

fun parseFrame(json: JSONObject, images: Map<String, DocumentFile?>) : Frame {
    if (arrayOf("imageRef", "startSec", "endSec", "text").any { !json.has(it) }){
        throw ParsingException("Invalid session file. Some frame keys are missing.")
    }
    if (!images.containsKey(json.getString("imageRef"))){
        throw ParsingException("Invalid session file. Frame has invalid imageRef.")
    }
    return Frame(
        images[json.getString("imageRef")]!!,
        json.getString("text"),
        json.getInt("startSec"),
        json.getInt("endSec")
    )
}

fun parseSegment(json: JSONObject, assets: Assets, metadata: SessionMetadata) : Segment {
    if (arrayOf("type", "name", "audioRef", "durationSec", "script").any { !json.has(it) }){
        throw ParsingException("Invalid session file. Some segment keys are missing.")
    }
    if (!assets.audios.containsKey(json.getString("audioRef"))){
        throw ParsingException("Invalid session file. Segment has invalid audioRef.")
    }
    if (json.getString("type") != "segment" && json.getString("type") != "loop"){
        throw ParsingException("Invalid session file. Segment has invalid type.")
    }
    val frames = json.getJSONArray("script").let {seq ->
        sequence<Frame> {
            for (i in 0 until seq.length())
                yield(parseFrame(seq.getJSONObject(i), assets.images))
        }.toList().toTypedArray()
    }
    val loopable = json.getString("type") == "loop"
    return Segment(
        json.getString("name"),
        assets.audios[json.getString("audioRef")]!!,
        json.getInt("durationSec"),
        frames,
        loopable,
        loopCount = if (loopable) if (json.has("iterations")) json.getInt("iterations") else metadata.defaultLoopCount else 1
    )
}

fun parseSession(documentFile: DocumentFile, contentResolver: ContentResolver) : Session {
    val content = contentResolver.openInputStream(documentFile.uri)!!.bufferedReader().readText()
    val json = JSONObject(content)
    if (json.has("metadata")){
        val metadata = parseMetadata(json.getJSONObject("metadata"))
        if (json.has("assets")){
            val assets = parseAssets(json.getJSONObject("assets"), documentFile.parentFile!!)
            if (json.has("sequence")){
                val segments = json.getJSONArray("sequence").let {seq -> sequence { for (i in 0 until seq.length()) yield(parseSegment(seq.getJSONObject(i), assets, metadata)) }.toList().toTypedArray() }
                return Session(metadata, segments)
            }else{
                throw ParsingException("Invalid session file. File is missing sequence.")
            }
        }else{
            throw ParsingException("Invalid session file. File is missing assets.")
        }
    }else{
        throw ParsingException("Invalid session file. File is missing metadata.")
    }
}