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
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import android.util.Log
import androidx.core.content.ContextCompat
import com.adobe.marketing.mobile.Event
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.ExperienceEvent
import com.adobe.marketing.mobile.EdgeCallback
import com.adobe.marketing.mobile.EdgeEventHandle
import android.content.Intent

class MainActivity : FragmentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity onCreate() called")
        setContentView(R.layout.activity_main)
        
        if (savedInstanceState == null) {
            try {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_browse_fragment, MainFragment())
                    .commit()
            } catch (e: Exception) {
                Log.e(TAG, "Error creating fragment", e)
            }
        }
    }
    
    class MainFragment : BrowseSupportFragment() {
        
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            
            try {
                setupUIElements()
                loadRows()
                setupEventHandlers()
            } catch (e: Exception) {
                Log.e(TAG, "Error in MainFragment onCreate", e)
            }
        }
        

        
        private fun setupUIElements() {
            // Set the title
            title = getString(R.string.app_name)
            
            // Set basic leanback settings
            setHeadersState(HEADERS_ENABLED)
            setHeadersTransitionOnBackEnabled(true)
            
            // Set search icon color
            try {
                setSearchAffordanceColor(ContextCompat.getColor(requireContext(), R.color.accent))
            } catch (e: Exception) {
                Log.e(TAG, "Error setting search color", e)
            }
            
            // Set brand color
            setBrandColor(ContextCompat.getColor(requireContext(), R.color.primary))
        }
        
        private fun loadRows() {
            try {
                val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
                val cardPresenter = CardPresenter()
                
                // Home Row
                val homeAdapter = ArrayObjectAdapter(cardPresenter)
                homeAdapter.add(Video(
                    "Quick Connect", 
                    "Click to start Adobe Assurance session for debugging", 
                    "https://example.com/assurance.jpg"
                ))
                homeAdapter.add(Video(
                    "Edge SendEvent", 
                    "Click to send a sample event using Edge.sendEvent API", 
                    "https://example.com/edge.jpg"
                ))
                homeAdapter.add(Video(
                    "Analytics", 
                    "Click to send a track action using MobileCore.trackAction", 
                    "https://example.com/analytics.jpg"
                ))
                
                val homeHeader = HeaderItem(0, "Home")
                rowsAdapter.add(ListRow(homeHeader, homeAdapter))
                
                // Videos Row with real open source videos
                val videosAdapter = ArrayObjectAdapter(cardPresenter)
                videosAdapter.add(Video(
                    "Big Buck Bunny", 
                    "A short animated film by the Blender Institute", 
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/BigBuckBunny.jpg",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                    "Animation"
                ))
                videosAdapter.add(Video(
                    "Elephant Dream", 
                    "The first Blender Open Movie from 2006", 
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/ElephantsDream.jpg",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                    "Animation"
                ))
                videosAdapter.add(Video(
                    "Sintel", 
                    "A short film produced by the Blender Institute", 
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/images/Sintel.jpg",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
                    "Animation"
                ))
                
                val videosHeader = HeaderItem(1, "Videos")
                rowsAdapter.add(ListRow(videosHeader, videosAdapter))
                
                adapter = rowsAdapter
            } catch (e: Exception) {
                Log.e(TAG, "Error loading rows", e)
            }
        }
        
        private fun setupEventHandlers() {
            try {
                onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
                    when (item) {
                        is Video -> {
                            Log.d(TAG, "Video clicked: ${item.title}")
                            
                            // Send Adobe Assurance event
                            sendAssuranceEvent(item.title)
                            
                            // Handle video selection
                            when {
                                item.title.contains("Quick Connect", ignoreCase = true) -> {
                                    Log.d(TAG, "Starting Assurance session...")
                                    try {
                                        Assurance.startSession()
                                        Log.d(TAG, "Assurance.startSession() called successfully")
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error starting Assurance session", e)
                                    }
                                }
                                item.title.contains("Edge SendEvent", ignoreCase = true) -> {
                                    Log.d(TAG, "=== Starting Edge Event Send ===")
                                    try {
                                        // Create sample XDM data
                                        val xdmData = mapOf(
                                            "eventType" to "commerce.productViews",
                                            "commerce" to mapOf(
                                                "productViews" to mapOf(
                                                    "value" to 1
                                                )
                                            ),
                                            "productListItems" to listOf(
                                                mapOf(
                                                    "SKU" to "sample-sku-123",
                                                    "name" to "Sample Product",
                                                    "quantity" to 1,
                                                    "priceTotal" to 99.99
                                                )
                                            ),
                                            "_id" to "sample-event-${System.currentTimeMillis()}"
                                        )
                                        
                                        Log.d(TAG, "XDM Data created: $xdmData")
                                        
                                        // Create ExperienceEvent
                                        val experienceEvent = ExperienceEvent.Builder()
                                            .setXdmSchema(xdmData)
                                            .build()
                                        
                                        Log.d(TAG, "ExperienceEvent created successfully")
                                        
                                        // Send event using Edge.sendEvent
                                        Edge.sendEvent(experienceEvent) { handles ->
                                            Log.d(TAG, "=== Edge.sendEvent CALLBACK RECEIVED ===")
                                            Log.d(TAG, "Number of handles received: ${handles.size}")
                                            handles.forEachIndexed { index, handle ->
                                                Log.d(TAG, "Handle $index - Type: ${handle.type}")
                                                Log.d(TAG, "Handle $index - Payload: ${handle.payload}")
                                            }
                                            Log.d(TAG, "=== Edge.sendEvent CALLBACK COMPLETE ===")
                                        }
                                        
                                        Log.d(TAG, "Edge.sendEvent() called successfully - waiting for callback...")
                                    } catch (e: Exception) {
                                        Log.e(TAG, "=== ERROR in Edge Event Send ===", e)
                                        Log.e(TAG, "Error message: ${e.message}")
                                        Log.e(TAG, "Error cause: ${e.cause}")
                                    }
                                }
                                item.title.contains("Analytics", ignoreCase = true) -> {
                                    Log.d(TAG, "Sending track action...")
                                    try {
                                        // Create context data for the action
                                        val contextData = mapOf(
                                            "action.name" to "tv_app_button_click",
                                            "action.type" to "user_interaction",
                                            "screen.name" to "home_view",
                                            "app.version" to "1.0.0",
                                            "device.type" to "android_tv",
                                            "timestamp" to System.currentTimeMillis().toString()
                                        )
                                        
                                        // Send track action using MobileCore.trackAction
                                        MobileCore.trackAction(
                                            "TV App - Track Action Button",
                                            contextData
                                        )
                                        
                                        Log.d(TAG, "MobileCore.trackAction() called successfully")
                                        Log.d(TAG, "Action: TV App - Track Action Button")
                                        Log.d(TAG, "Context Data: $contextData")
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error sending track action", e)
                                    }
                                }
                                else -> {
                                    // Handle video playback
                                    if (item.videoUrl.isNotEmpty()) {
                                        Log.d(TAG, "Starting video playback: ${item.title}")
                                        try {
                                            val intent = Intent(requireContext(), VideoPlayerActivity::class.java).apply {
                                                putExtra(VideoPlayerActivity.EXTRA_VIDEO_TITLE, item.title)
                                                putExtra(VideoPlayerActivity.EXTRA_VIDEO_URL, item.videoUrl)
                                                putExtra(VideoPlayerActivity.EXTRA_VIDEO_DESCRIPTION, item.description)
                                            }
                                            startActivity(intent)
                                            Log.d(TAG, "VideoPlayerActivity started successfully")
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error starting video player", e)
                                        }
                                    } else {
                                        Log.d(TAG, "No video URL available for: ${item.title}")
                                    }
                                }
                            }
                        }
                    }
                }
                
                onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
                    Log.d(TAG, "Item selected: ${(item as? Video)?.title}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up event handlers", e)
            }
        }
        
        private fun sendAssuranceEvent(videoTitle: String) {
            try {
                val eventData = mapOf(
                    "videoTitle" to videoTitle,
                    "action" to "video_selected",
                    "timestamp" to System.currentTimeMillis(),
                    "platform" to "Android TV",
                    "appVersion" to "1.0.0"
                )
                
                val event = Event.Builder(
                    "Video Selection Event",
                    "com.adobe.assurance.test",
                    "com.adobe.assurance.test"
                ).setEventData(eventData).build()
                
                MobileCore.dispatchEvent(event)
            } catch (e: Exception) {
                Log.e(TAG, "Error sending Assurance event", e)
            }
        }
    }
} 