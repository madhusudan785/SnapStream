package com.example.videoplay.VideoPlayer

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.example.videoplay.databinding.ActivityVideoPlayerBinding
import com.example.videoplay.utils.AppConstants
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource

class VideoPlayer : AppCompatActivity() {
    private lateinit var binding: ActivityVideoPlayerBinding
    private lateinit var player: ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //fetching videoId
        val videoId = intent.getStringExtra("VIDEO_ID") ?: return

        // Configure custom LoadControl for buffering
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                32 * 1024, // Min buffer before start
                64 * 1024, // Max buffer size
                16 * 1024, // Buffer before playback
                32 * 1024  // Buffer after rebuffer
            )
            .build()

        player = ExoPlayer.Builder(this)
            .setLoadControl(loadControl)
            .build()
        binding.playerView.player = player


        // Construct the URI for the video stream
        val videoUri = AppConstants.BASE_URL + "api/v1/stream/$videoId/master.m3u8".toUri()
        Log.d("ThumbnailCheck", "video path: ${videoUri}")
        val dataSourceFactory = DefaultDataSource.Factory(this)
        val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(videoUri))

        player.setMediaSource(hlsMediaSource)
        player.prepare()
        player.playWhenReady = true

        observePlayerState()

    }

    private fun observePlayerState() {
        val progressBar: ProgressBar = binding.loadingIndicator

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        progressBar.visibility = View.GONE
                        Log.d("ExoPlayer", "Playback is ready")
                    }

                    Player.STATE_BUFFERING -> {
                        progressBar.visibility = View.VISIBLE
                        Log.d("ExoPlayer", "Buffering video")
                    }

                    Player.STATE_ENDED -> Log.d("ExoPlayer", "Playback ended")
                    Player.STATE_IDLE -> Log.d("ExoPlayer", "Player is idle")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("ExoPlayer", "Player error: ${error.message}", error)
                when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> {
                        Toast.makeText(
                            this@VideoPlayer,
                            "Network connection failed. Please check your internet connection.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> {
                        Toast.makeText(
                            this@VideoPlayer,
                            "Bad HTTP response from server. Please try again later.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    PlaybackException.ERROR_CODE_DECODING_FAILED -> {
                        Toast.makeText(
                            this@VideoPlayer,
                            "Decoding error. This video might be corrupted.",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        Toast.makeText(
                            this@VideoPlayer,
                            "An unknown error occurred. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }


                player.stop() // Stops the player
            }
        })
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            player.release()
        } catch (e: IllegalStateException) {
            Log.e("ExoPlayer", "Error releasing player: ${e.message}")
        }
    }
}


