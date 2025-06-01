package com.example.music

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var songRecyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private val songList = mutableListOf<Song>()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadSongs()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        songRecyclerView = findViewById(R.id.songRecyclerView)
        songRecyclerView.layoutManager = LinearLayoutManager(this)

        progressBar = findViewById(R.id.progressBar)

        checkAndRequestPermission()
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            loadSongs()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadSongs() {
        progressBar.visibility = View.VISIBLE

        Thread {
            songList.clear()

            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val projection = arrayOf(
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DATE_MODIFIED
            )

            // Sort by modified date descending (newest first)
            val sortOrder = "${MediaStore.Audio.Media.DATE_MODIFIED} DESC"

            val cursor = contentResolver.query(uri, projection, null, null, sortOrder)

            cursor?.use {
                val titleIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)

                while (it.moveToNext()) {
                    val title = it.getString(titleIndex)
                    val path = it.getString(dataIndex)

                    if (path.endsWith(".mp3") || path.endsWith(".wav")) {
                        songList.add(Song(title, path, R.drawable.ic_music))
                    }
                }
            }

            SongDataHolder.songs = songList

            runOnUiThread {
                progressBar.visibility = View.GONE
                val adapter = SongAdapter(songList) { song ->
                    SongDataHolder.songs = songList

                    val intent = Intent(this, PlayerActivity::class.java).apply {
                        putExtra("path", song.path)
                        putExtra("title", song.title)
                    }
                    startActivity(intent)
                }
                songRecyclerView.adapter = adapter
            }
        }.start()
    }
}
