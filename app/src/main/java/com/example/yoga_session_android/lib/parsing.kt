package com.example.yoga_session_android.lib

import org.json.JSONObject
import java.io.File

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

fun parseAssets(json: JSONObject, filepath: String) : Assets {
    if (!json.has("image") || !json.has("audio")) {
        throw ParsingException("Invalid session file. File is missing assets.")
    }
    val dir = File(filepath).parent
    val assets = Assets(
        json.getJSONArray("images").let {seq ->
            sequence {
                for (i in 0 until seq.length())
                    yield(seq.getString(i) to  File(dir, seq.getString(i)).absolutePath)
            }.toMap() },
        json.getJSONArray("audio").let {seq ->
            sequence {
                for (i in 0 until seq.length())
                    yield(seq.getString(i) to File(dir, seq.getString(i)).absolutePath)
            }.toMap() }
    )
    assets.images.forEach { if (!File(it.value).exists()) throw ParsingException("Invalid session file. Image file ${it.value} does not exist.") }
    assets.audios.forEach { if (!File(it.value).exists()) throw ParsingException("Invalid session file. Audio file ${it.value} does not exist.") }
    return assets
}

fun parseFrame(json: JSONObject, images: Map<String, String>) : Frame {
    if (arrayOf("imageRef", "startSec", "endSec", "text").any { !json.has(it) }){
        throw ParsingException("Invalid session file. Some frame keys are missing.")
    }
    if (!images.containsKey(json.getString("imageRef"))){
        throw ParsingException("Invalid session file. Frame has invalid imageRef.")
    }
    return Frame(
        images[json.getString("imageRef")]!!,
        images[json.getString("text")]!!,
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

fun parseSession(filepath: String) : Session {
    val content = File(filepath).readText(Charsets.UTF_8)
    val json = JSONObject(content)
    if (json.has("metadata")){
        val metadata = parseMetadata(json.getJSONObject("metadata"))
        if (json.has("assets")){
            val assets = parseAssets(json.getJSONObject("assets"), filepath)
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