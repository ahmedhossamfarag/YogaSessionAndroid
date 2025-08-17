package com.example.yoga_session_android.lib

import android.content.Context
import android.media.MediaPlayer
import androidx.documentfile.provider.DocumentFile

class Frame(val image: DocumentFile, val text: String, val startSec: Int, val endSec: Int)

class Segment(val name: String, val audio: DocumentFile, val durationSec: Int, val script: Array<Frame>, val loopable: Boolean, val loopCount: Int){
    lateinit var audioPlayer: MediaPlayer
    lateinit var timeline: Array<Frame?>
    var currentSec: Int = 0
    var currentLoop: Int = 1

    val isAvailable get() = script.isNotEmpty() && loopCount > 0

    fun init(context: Context){
        audioPlayer = MediaPlayer()
        audioPlayer.setDataSource(context, audio.uri)
        audioPlayer.prepare()
        audioPlayer.isLooping = true
        timeline = arrayOfNulls(durationSec)
        for (frame in script){
            for (sec in frame.startSec until frame.endSec){
                timeline[sec] = frame
            }
        }
        if (timeline[0] == null) {
            timeline[0] = timeline.first { f -> f != null }
        }
        for (i in 1 until timeline.size){
            if (timeline[i] == null) {
                timeline[i] = timeline[i-1]
            }
        }
    }

    fun pause() {
        if (audioPlayer.isPlaying) audioPlayer.pause()
    }

    fun play() {
        if (!audioPlayer.isPlaying) audioPlayer.start()
    }

    val hasNext get() = currentSec < durationSec || currentLoop < loopCount

    val nextFrame: Frame?
        get() {
            if (currentSec >= durationSec){
                currentSec = 0
                currentLoop++
            }
            val frame = timeline[currentSec]
            currentSec++
            return frame
        }

    fun exit(){
        audioPlayer.stop()
        audioPlayer.release()
        timeline = arrayOfNulls(0)
    }

}

class SessionMetadata(val id: String, val title: String, val category: String, val defaultLoopCount: Int, val tempo: String)


class Assets(val images: Map<String, DocumentFile?>, val audios: Map<String, DocumentFile?>)

class Session(val metadata: SessionMetadata, val segments: Array<Segment>){
    var currentSegment: Segment? = null
    var currentSegmentIndex: Int = -1
    lateinit var context: Context

    val isAvailable get() = segments.isNotEmpty()

    val durationSec get() = segments.sumOf { it.durationSec * it.loopCount }

    fun init(context: Context){
        this.context = context
        currentSegment = segments.firstOrNull { it.isAvailable }
        if (currentSegment != null){
            currentSegmentIndex = segments.indexOf(currentSegment)
            currentSegment?.init(context)
        }
    }

    fun next(): Frame? {
        if (currentSegment == null) return null
        if (currentSegment!!.hasNext){
            return currentSegment!!.nextFrame
        } else {
            currentSegment!!.exit()
            currentSegment = segments.sliceArray(currentSegmentIndex + 1 until segments.size).firstOrNull { it.isAvailable }
            if (currentSegment == null) return null
            currentSegmentIndex = segments.indexOf(currentSegment)
            currentSegment!!.init(context)
            currentSegment!!.play()
            return currentSegment!!.nextFrame
        }
    }

    fun pause(){
        currentSegment?.pause()
    }

    fun play(){
        currentSegment?.play()
    }

    val progress get() = segments.sliceArray(0..currentSegmentIndex).sumOf { (it.durationSec * (it.currentLoop - 1)) + it.currentSec - 1 }

    fun exit(){
        currentSegment?.exit()
    }
}