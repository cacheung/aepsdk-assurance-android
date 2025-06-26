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

import android.util.Log
import com.adobe.marketing.mobile.edge.media.Media
import com.adobe.marketing.mobile.edge.media.MediaConstants
import com.adobe.marketing.mobile.edge.media.MediaTracker

class MediaTrackingService {
    private var mediaTracker: MediaTracker? = null
    private var isSessionActive = false
    private var currentVideoTitle: String? = null
    private var currentVideoUrl: String? = null
    
    companion object {
        private const val TAG = "MediaTrackingService"
    }
    
    /**
     * Initialize media tracking for a video
     */
    fun initializeTracking(videoTitle: String, videoUrl: String) {
        try {
            Log.d(TAG, "Initializing media tracking for video: $videoTitle")
            currentVideoTitle = videoTitle
            currentVideoUrl = videoUrl
            
            // Create media tracker configuration for Edge Network
            val config = mapOf(
                "config.channel" to "Assurance Leanback Test App",
                "config.downloadedcontent" to false,
                "config.appVersion" to "1.0.0"
            )
            
            // Create media tracker with configuration
            mediaTracker = Media.createTracker(config)
            
            Log.d(TAG, "Media tracker created successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing media tracking", e)
        }
    }
    
    /**
     * Start media session
     */
    fun startSession(videoDurationSeconds: Long = 600) {
        try {
            val title = currentVideoTitle ?: "Unknown Video"
            val tracker = mediaTracker ?: return
            
            Log.d(TAG, "Starting media session for: $title")
            
            // Create media object
            val mediaInfo = Media.createMediaObject(
                title,
                title.hashCode().toString(),
                videoDurationSeconds.toInt(),
                MediaConstants.StreamType.VOD,
                Media.MediaType.Video
            )
            
            // Create metadata
            val metadata = createVideoMetadata(title)
            
            // Start tracking session
            tracker.trackSessionStart(mediaInfo, metadata)
            isSessionActive = true
            
            Log.d(TAG, "Media session started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting media session", e)
        }
    }
    
    /**
     * Track play event
     */
    fun trackPlay() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackPlay()
            Log.d(TAG, "Tracked play event")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking play event", e)
        }
    }
    
    /**
     * Track pause event
     */
    fun trackPause() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackPause()
            Log.d(TAG, "Tracked pause event")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking pause event", e)
        }
    }
    
    /**
     * Track seek start event
     */
    fun trackSeekStart() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackEvent(Media.Event.SeekStart, null, null)
            Log.d(TAG, "Tracked seek start event")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking seek start event", e)
        }
    }
    
    /**
     * Track seek complete event
     */
    fun trackSeekComplete() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackEvent(Media.Event.SeekComplete, null, null)
            Log.d(TAG, "Tracked seek complete event")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking seek complete event", e)
        }
    }
    
    /**
     * Track buffer start event
     */
    fun trackBufferStart() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackEvent(Media.Event.BufferStart, null, null)
            Log.d(TAG, "Tracked buffer start event")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking buffer start event", e)
        }
    }
    
    /**
     * Track buffer complete event
     */
    fun trackBufferComplete() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackEvent(Media.Event.BufferComplete, null, null)
            Log.d(TAG, "Tracked buffer complete event")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking buffer complete event", e)
        }
    }
    
    /**
     * Update current playhead position
     */
    fun updatePlayhead(positionSeconds: Long) {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.updateCurrentPlayhead(positionSeconds.toInt())
        } catch (e: Exception) {
            Log.e(TAG, "Error updating playhead", e)
        }
    }
    
    /**
     * Update Quality of Experience (QoE) information
     */
    fun updateQoE(bitrate: Long = 1000000, startupTime: Long = 2000, fps: Long = 30, droppedFrames: Long = 0) {
        try {
            if (!isSessionActive) return
            
            val qoeInfo = Media.createQoEObject(bitrate.toInt(), startupTime.toInt(), fps.toInt(), droppedFrames.toInt())
            mediaTracker?.updateQoEObject(qoeInfo)
            
            Log.d(TAG, "Updated QoE: bitrate=$bitrate, startupTime=$startupTime, fps=$fps, droppedFrames=$droppedFrames")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating QoE", e)
        }
    }
    
    /**
     * Track error event
     */
    fun trackError(errorId: String) {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackError(errorId)
            Log.d(TAG, "Tracked error: $errorId")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking error event", e)
        }
    }
    
    /**
     * Track session complete
     */
    fun trackComplete() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackComplete()
            Log.d(TAG, "Tracked session complete")
        } catch (e: Exception) {
            Log.e(TAG, "Error tracking complete event", e)
        }
    }
    
    /**
     * End media session
     */
    fun endSession() {
        try {
            if (!isSessionActive) return
            
            mediaTracker?.trackSessionEnd()
            isSessionActive = false
            
            Log.d(TAG, "Media session ended")
        } catch (e: Exception) {
            Log.e(TAG, "Error ending media session", e)
        }
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        try {
            if (isSessionActive) {
                endSession()
            }
            mediaTracker = null
            currentVideoTitle = null
            currentVideoUrl = null
            
            Log.d(TAG, "Media tracking cleaned up")
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
        }
    }
    
    /**
     * Create video metadata based on video information
     */
    private fun createVideoMetadata(videoTitle: String): Map<String, String> {
        return mapOf(
            MediaConstants.VideoMetadataKeys.SHOW to "Assurance Leanback Test App",
            MediaConstants.VideoMetadataKeys.EPISODE to videoTitle,
            MediaConstants.VideoMetadataKeys.GENRE to "Technology",
            MediaConstants.VideoMetadataKeys.NETWORK to "Adobe Experience Platform",
            MediaConstants.VideoMetadataKeys.RATING to "TV-G",
            MediaConstants.VideoMetadataKeys.ORIGINATOR to "Adobe",
            MediaConstants.VideoMetadataKeys.MVPD to "Adobe",
            MediaConstants.VideoMetadataKeys.DAY_PART to "primetime",
            MediaConstants.VideoMetadataKeys.FEED to "live",
            MediaConstants.VideoMetadataKeys.STREAM_FORMAT to "hd",
            "customKey" to "customValue",
            "videoUrl" to (currentVideoUrl ?: "")
        )
    }
    
    /**
     * Check if session is currently active
     */
    fun isSessionActive(): Boolean = isSessionActive
} 