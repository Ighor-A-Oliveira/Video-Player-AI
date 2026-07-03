package com.example.animeplayer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.TrackSelectionDialogBuilder
import com.example.animeplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null
    private lateinit var trackSelector: DefaultTrackSelector
    private var episodes: List<Episode> = emptyList()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        episodes = intent.getParcelableArrayListExtra("EPISODES") ?: emptyList()
        currentIndex = intent.getIntExtra("START_INDEX", 0)

        initializePlayer()
    }

    private fun initializePlayer() {
        trackSelector = DefaultTrackSelector(this)
        player = ExoPlayer.Builder(this).setTrackSelector(trackSelector).build().also {
            binding.playerView.player = it
            val mediaItems = episodes.map { ep -> MediaItem.fromUri(ep.uriString) }
            it.setMediaItems(mediaItems, currentIndex, 0L)
            
            applyTrackPreferences()
            
            // Resume progress
            val prefs = getSharedPreferences("PlayerPrefs", MODE_PRIVATE)
            val lastPos = prefs.getLong("resume_${episodes[currentIndex].uriString}", 0L)
            it.seekTo(currentIndex, lastPos)

            it.addListener(object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    currentIndex = it.currentMediaItemIndex
                    applyTrackPreferences()
                }
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) startAutoPlayTimer()
                }
            })
            it.prepare()
            it.play()
        }
        binding.btnTracks.setOnClickListener { showTrackSelector() }
    }

    private fun applyTrackPreferences() {
        val seriesName = episodes[currentIndex].seriesName
        val prefs = getSharedPreferences("PlayerPrefs", MODE_PRIVATE)
        trackSelector.parameters = trackSelector.parameters.buildUpon()
            .setPreferredAudioLanguage(prefs.getString("${seriesName}_audio", null))
            .setPreferredTextLanguage(prefs.getString("${seriesName}_sub", null))
            .build()
    }

    private fun showTrackSelector() {
        TrackSelectionDialogBuilder(this, "Áudio e Legendas", player!!).build().show()
    }

    private fun startAutoPlayTimer() {
        if (currentIndex < episodes.size - 1) {
            Toast.makeText(this, "Próximo episódio em 5s", Toast.LENGTH_SHORT).show()
            Handler(Looper.getMainLooper()).postDelayed({ player?.seekToNextMediaItem() }, 5000)
        }
    }

    override fun onPause() {
        super.onPause()
        player?.let {
            val prefs = getSharedPreferences("PlayerPrefs", MODE_PRIVATE)
            prefs.edit().putLong("resume_${episodes[currentIndex].uriString}", it.currentPosition).apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
    }
}