package com.example.yoga_session_android

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.yoga_session_android.databinding.ShowActivityBinding
import com.example.yoga_session_android.lib.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ShowActivity : ComponentActivity() {
    lateinit var binding: ShowActivityBinding
    lateinit var session: Session
    lateinit var job: Job
    var playing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = Shared.session!!
        binding = ShowActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.playButton.setOnClickListener {
            togglePlay()
        }
        binding.progressBar.max = session.durationSec
        session.init(this)
        updateView()
        job = CoroutineScope(Dispatchers.Main).launch {
            while(isActive) {
                delay(1000)
                if (playing)  updateView()
            }
        }
    }

    fun updateView(){
        val frame = session.next()
        if (frame == null) {
            job.cancel()
            if (playing) togglePlay()
            binding.progressBar.progress = session.durationSec
            session.exit()
            return
        }
        binding.frameTextView.text = frame.text
        binding.frameImageView.setImageBitmap(BitmapFactory.decodeStream(contentResolver.openInputStream(frame.image.uri)))
        binding.progressBar.progress = session.progress
    }


    fun togglePlay() {
        if (playing) {
            playing = false
            binding.playButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.play_icon))
            session.pause()
        } else {
            playing = true
            binding.playButton.setImageDrawable(AppCompatResources.getDrawable(this, R.drawable.pause_icon))
            session.play()
        }
    }
}