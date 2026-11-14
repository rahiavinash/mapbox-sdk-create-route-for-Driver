package com.solvay.UI.chat

import android.content.Context
import android.graphics.PorterDuff
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.solvay.R

class AudioPlayerView private constructor(
    private val context: Context,
    private val playPauseButton: ImageView,
    private val seekBar: SeekBar,
    private val durationView: TextView,
    private val audioUrl: String,
    private val onStop: (() -> Unit)? = null
) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private val handler = Handler(Looper.getMainLooper())

    init {
        setupListeners()
    }

    private fun setupListeners() {
        playPauseButton.setOnClickListener {
            if (isPlaying) pauseAudio() else playAudio()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playAudio() {
        // Release any existing global instance
        currentInstance?.release()

        // Update this as the current instance
        currentInstance = this

        mediaPlayer = MediaPlayer().apply {
            setDataSource(audioUrl)
            prepareAsync()
            setOnPreparedListener {
                seekBar.max = it.duration
                it.start()
                this@AudioPlayerView.isPlaying = true
                playPauseButton.setImageResource(R.drawable.video_pause_button)
                playPauseButton.setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN)
                updateSeekBar()
            }
            setOnCompletionListener {
                this@AudioPlayerView.isPlaying = false
                playPauseButton.setImageResource(R.drawable.play)
                seekBar.progress = 0
                durationView.text = "00:00"
            }
        }
    }

    private fun pauseAudio() {
        mediaPlayer?.pause()
        isPlaying = false
        playPauseButton.setImageResource(R.drawable.play)
    }

    private fun updateSeekBar() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                mediaPlayer?.let {
                    seekBar.progress = it.currentPosition
                    durationView.text = formatTime(it.currentPosition)
                    if (isPlaying) handler.postDelayed(this, 500)
                }
            }
        }, 0)
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        handler.removeCallbacksAndMessages(null)

        playPauseButton.setImageResource(R.drawable.play)
        seekBar.progress = 0
        durationView.text = "00:00"

        onStop?.invoke()
    }

    private fun formatTime(millis: Int): String {
        val minutes = millis / 1000 / 60
        val seconds = (millis / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    companion object {
        private var currentInstance: AudioPlayerView? = null

        fun create(
            context: Context,
            playPauseButton: ImageView,
            seekBar: SeekBar,
            durationView: TextView,
            audioUrl: String
        ): AudioPlayerView {
            currentInstance?.release()

            val instance = AudioPlayerView(
                context,
                playPauseButton,
                seekBar,
                durationView,
                audioUrl
            )
            currentInstance = instance
            return instance
        }

        fun releaseCurrent() {
            currentInstance?.release()
            currentInstance = null
        }
    }
}