/*
 * Copyright 2025 Adobe. All rights reserved.
 * This file is licensed to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. You may obtain a copy
 * of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under
 * the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
 * OF ANY KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.adobe.marketing.mobile.assuranceleanbacktestapp

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.LinearLayout
import android.view.Gravity
import android.graphics.Color
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.MobileCore
import java.util.Timer
import java.util.TimerTask

class VideoPlayerActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "VideoPlayerActivity"
        const val EXTRA_VIDEO_TITLE = "video_title"
        const val EXTRA_VIDEO_URL = "video_url"
        const val EXTRA_VIDEO_DESCRIPTION = "video_description"
    }
    
    private var exoPlayer: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private lateinit var titleTextView: TextView
    private lateinit var descriptionTextView: TextView
    private lateinit var mediaTrackingService: MediaTrackingService
    private var playheadTimer: Timer? = null
    private var isPlaying = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "VideoPlayerActivity onCreate() called")
        
        val videoTitle = intent.getStringExtra(EXTRA_VIDEO_TITLE) ?: "Unknown Video"
        val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: ""
        val videoDescription = intent.getStringExtra(EXTRA_VIDEO_DESCRIPTION) ?: ""
        
        Log.d(TAG, "Playing video: $videoTitle")
        Log.d(TAG, "Video URL: $videoUrl")
        
        // Initialize media tracking service
        mediaTrackingService = MediaTrackingService()
        try {
            mediaTrackingService.initializeTracking(videoTitle, videoUrl)
            Log.d(TAG, "MediaTrackingService initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaTrackingService", e)
        }
        
        // Send Adobe Assurance event for video playback
        sendVideoPlaybackEvent(videoTitle, videoUrl)
        
        setupUI(videoTitle, videoUrl, videoDescription)
    }
    
    private fun setupUI(videoTitle: String, videoUrl: String, videoDescription: String) {
        // Create main layout
        val mainLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.BLACK)
        }
        
        // Create title text view
        titleTextView = TextView(this).apply {
            text = videoTitle
            textSize = 24f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(50, 30, 50, 20)
        }
        
        // Create description text view
        descriptionTextView = TextView(this).apply {
            text = videoDescription
            textSize = 16f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER
            setPadding(50, 0, 50, 30)
        }
        
        // Create ExoPlayer view
        playerView = PlayerView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            useController = true
        }
        
        // Add views to layout
        mainLayout.addView(titleTextView)
        mainLayout.addView(descriptionTextView)
        mainLayout.addView(playerView)
        
        setContentView(mainLayout)
        
        // Set up video playback
        setupVideoPlayback(videoUrl)
    }
    
    private fun setupVideoPlayback(videoUrl: String) {
        try {
            Log.d(TAG, "Setting up ExoPlayer...")
            
            // Initialize ExoPlayer with basic configuration
            exoPlayer = ExoPlayer.Builder(this).build()
            
            // Bind player to view first
            playerView.player = exoPlayer
            
            // Set up video event listeners
            setupVideoEventListeners()
            
            // Determine video URL to use
            val finalVideoUrl = when {
                videoUrl.isNotEmpty() -> {
                    Log.d(TAG, "Using provided video URL: $videoUrl")
                    videoUrl
                }
                else -> {
                    val defaultUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                    Log.d(TAG, "Using default video URL: $defaultUrl")
                    defaultUrl
                }
            }
            
            // Create media item
            val mediaItem = MediaItem.fromUri(Uri.parse(finalVideoUrl))
            Log.d(TAG, "Created MediaItem for: $finalVideoUrl")
            
            // Set media item and prepare
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            
            // Start media tracking session
            try {
                mediaTrackingService.startSession()
                Log.d(TAG, "Media tracking session started")
            } catch (e: Exception) {
                Log.e(TAG, "Error starting media tracking session", e)
            }
            
            // Start playback after a short delay to ensure everything is ready
            exoPlayer?.playWhenReady = true
            
            Log.d(TAG, "ExoPlayer setup completed successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up video playback", e)
            e.printStackTrace()
            mediaTrackingService.trackError("VIDEO_SETUP_ERROR")
            
            // Don't finish immediately, let user see the error
            // finish()
        }
    }
    
    private fun setupVideoEventListeners() {
        exoPlayer?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                Log.d(TAG, "Playback state changed: $playbackState")
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        Log.d(TAG, "Player state: IDLE")
                    }
                    Player.STATE_BUFFERING -> {
                        Log.d(TAG, "Player state: BUFFERING")
                        mediaTrackingService.trackBufferStart()
                    }
                    Player.STATE_READY -> {
                        Log.d(TAG, "Player state: READY - Video prepared")
                        mediaTrackingService.updateQoE()
                        startPlayheadTracking()
                    }
                    Player.STATE_ENDED -> {
                        Log.d(TAG, "Player state: ENDED - Video completed")
                        mediaTrackingService.trackComplete()
                        stopPlayheadTracking()
                    }
                }
            }
            
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                Log.d(TAG, "Is playing changed: $isPlayingNow (was: $isPlaying)")
                if (isPlayingNow && !isPlaying) {
                    // Video started playing
                    Log.d(TAG, "Video started playing")
                    isPlaying = true
                    mediaTrackingService.trackPlay()
                } else if (!isPlayingNow && isPlaying) {
                    // Video paused
                    Log.d(TAG, "Video paused")
                    isPlaying = false
                    mediaTrackingService.trackPause()
                }
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer error occurred: ${error.message}")
                Log.e(TAG, "Error code: ${error.errorCode}")
                Log.e(TAG, "Error cause: ${error.cause}")
                error.printStackTrace()
                mediaTrackingService.trackError("VIDEO_PLAYBACK_ERROR_${error.errorCode}")
                
                // Don't finish on error, let user see what happened
                // finish()
            }
        })
    }
    
    private fun startPlayheadTracking() {
        playheadTimer = Timer().apply {
            scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    // Must access ExoPlayer on the main thread
                    runOnUiThread {
                        try {
                            exoPlayer?.let { player ->
                                if (isPlaying && player.isPlaying) {
                                    val currentPosition = player.currentPosition / 1000L // Convert to seconds
                                    mediaTrackingService.updatePlayhead(currentPosition)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error updating playhead", e)
                        }
                    }
                }
            }, 0, 1000) // Update every second
        }
    }
    
    private fun stopPlayheadTracking() {
        playheadTimer?.cancel()
        playheadTimer = null
    }
    
    private fun sendVideoPlaybackEvent(videoTitle: String, videoUrl: String) {
        try {
            val eventData = mapOf(
                "videoTitle" to videoTitle,
                "videoUrl" to videoUrl,
                "action" to "video_playback_started",
                "timestamp" to System.currentTimeMillis(),
                "platform" to "Android TV",
                "appVersion" to "1.0.0"
            )
            
            val event = Event.Builder(
                "Video Playback Event",
                "com.adobe.assurance.test",
                "com.adobe.assurance.test"
            ).setEventData(eventData).build()
            
            MobileCore.dispatchEvent(event)
            Log.d(TAG, "Video playback event sent: $eventData")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending video playback event", e)
        }
    }
    
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
        exoPlayer?.let { player ->
            if (player.isPlaying) {
                player.pause()
                mediaTrackingService.trackPause()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }
    
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
        exoPlayer?.pause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
        
        try {
            stopPlayheadTracking()
            mediaTrackingService.endSession()
            mediaTrackingService.cleanup()
            
            exoPlayer?.release()
            exoPlayer = null
            
            Log.d(TAG, "Cleanup completed successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
} 