package com.example.music

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PlayerActivity : AppCompatActivity() {

    private lateinit var btnPlayPause: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var btnPrev: ImageView
    private lateinit var songTitleText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var tvTime: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var currentIndex = 0
    private var songList: List<Song> = emptyList()
    private var isPlaying = false

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        btnPlayPause = findViewById(R.id.btnPlayPause)
        btnNext = findViewById(R.id.btnNext)
        btnPrev = findViewById(R.id.btnPrev)
        songTitleText = findViewById(R.id.currentSongTitle)
        seekBar = findViewById(R.id.seekBar)
        tvTime = findViewById(R.id.tvTime)

        // Receive data from MainActivity
        val path = intent.getStringExtra("path")
        val title = intent.getStringExtra("title")
        songList = SongDataHolder.songs
        currentIndex = songList.indexOfFirst { it.path == path }

        playSong()

        updateSeekBar()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) {}
            override fun onStopTrackingTouch(seekbar: SeekBar?) {}
        })

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnNext.setOnClickListener {
            if (currentIndex < songList.size - 1) {
                currentIndex++
                playSong()
            } else {
                Toast.makeText(this, "No next song", Toast.LENGTH_SHORT).show()
            }
        }

        btnPrev.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                playSong()
            } else {
                Toast.makeText(this, "No previous song", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun playSong() {
        val song = songList[currentIndex]
        songTitleText.text = song.title

        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@PlayerActivity, Uri.parse(song.path))
            prepare()
            start()

            // Auto-play next song when this one finishes
            setOnCompletionListener {
                if (currentIndex < songList.size - 1) {
                    currentIndex++
                    playSong()
                }
            }
        }

        isPlaying = true
        btnPlayPause.setImageResource(R.drawable.pause)

        mediaPlayer?.let {
            seekBar.max = it.duration
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let {
            seekBar.progress = it.currentPosition
            tvTime.text = formatTime(it.currentPosition)
        }

        handler.postDelayed({ updateSeekBar() }, 1000)
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = milliseconds / 1000 / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun togglePlayPause() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.pause()
                btnPlayPause.setImageResource(R.drawable.play)
                isPlaying = false
            } else {
                it.start()
                btnPlayPause.setImageResource(R.drawable.pause)
                isPlaying = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacksAndMessages(null)

}
}
