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

import android.view.ViewGroup
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import android.content.Context
import android.util.Log
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import android.graphics.drawable.GradientDrawable
import android.widget.FrameLayout
import android.widget.TextView
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.graphics.Typeface

class CardPresenter : Presenter() {
    
    companion object {
        private const val TAG = "CardPresenter"
        private const val CARD_WIDTH = 480
        private const val CARD_HEIGHT = 320
    }
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val context = parent.context
        
        // Create a modern card view with enhanced styling
        val cardView = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(CARD_WIDTH, CARD_HEIGHT)
            isFocusable = true
            isFocusableInTouchMode = true
            
            // Create a linear layout for content
            val contentLayout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                setPadding(32, 32, 32, 32)
            }
            
            // Add title text view with enhanced styling
            val titleView = TextView(context).apply {
                id = android.R.id.title
                textSize = 24f  // Bigger text
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                gravity = Gravity.CENTER
                maxLines = 2
                setShadowLayer(4f, 0f, 2f, ContextCompat.getColor(context, android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 16, 16, 12)
                }
            }
            
            // Add description text view with enhanced styling
            val descriptionView = TextView(context).apply {
                id = android.R.id.text1
                textSize = 18f  // Bigger text
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                typeface = Typeface.create("sans-serif", Typeface.NORMAL)
                gravity = Gravity.CENTER
                maxLines = 3
                lineHeight = (textSize * 1.3f).toInt()
                setShadowLayer(2f, 0f, 1f, ContextCompat.getColor(context, android.R.color.black))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(16, 12, 16, 16)
                }
            }
            
            contentLayout.addView(titleView)
            contentLayout.addView(descriptionView)
            addView(contentLayout)
        }
        
        return ViewHolder(cardView)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        try {
            val video = item as Video
            val cardView = viewHolder.view as FrameLayout
            
            Log.d(TAG, "Setting card title: ${video.title}")
            Log.d(TAG, "Setting card content: ${video.description}")
            
            // Set title and description
            val titleView = cardView.findViewById<TextView>(android.R.id.title)
            val descriptionView = cardView.findViewById<TextView>(android.R.id.text1)
            
            titleView?.text = video.title
            descriptionView?.text = video.description
            
            // Create beautiful gradient backgrounds with vibrant colors
            val gradientDrawable = createEnhancedGradientBackground(cardView.context, video.title)
            cardView.background = gradientDrawable
            
            // Add focus change animation for better UX
            cardView.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    view.animate()
                        .scaleX(1.08f)
                        .scaleY(1.08f)
                        .setDuration(200)
                        .start()
                } else {
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(200)
                        .start()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error binding view holder", e)
        }
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        try {
            val cardView = viewHolder.view as FrameLayout
            cardView.background = null
            cardView.setOnFocusChangeListener(null)
            cardView.clearAnimation()
        } catch (e: Exception) {
            Log.e(TAG, "Error unbinding view holder", e)
        }
    }
    
    private fun createEnhancedGradientBackground(context: Context, title: String): GradientDrawable {
        val colors = when {
            title.contains("Quick Connect", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_blue),
                ContextCompat.getColor(context, R.color.gradient_end_blue)
            )
            title.contains("Edge SendEvent", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_green),
                ContextCompat.getColor(context, R.color.gradient_end_green)
            )
            title.contains("Analytics", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_purple),
                ContextCompat.getColor(context, R.color.gradient_end_purple)
            )
            title.contains("Big Buck Bunny", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_orange),
                ContextCompat.getColor(context, R.color.gradient_end_orange)
            )
            title.contains("Elephant Dream", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_purple),
                ContextCompat.getColor(context, R.color.gradient_end_purple)
            )
            title.contains("Sintel", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_green),
                ContextCompat.getColor(context, R.color.gradient_end_green)
            )
            title.contains("Video", ignoreCase = true) -> intArrayOf(
                ContextCompat.getColor(context, R.color.gradient_start_orange),
                ContextCompat.getColor(context, R.color.gradient_end_orange)
            )
            else -> intArrayOf(
                ContextCompat.getColor(context, R.color.background_medium),
                ContextCompat.getColor(context, R.color.background_light)
            )
        }
        
        return GradientDrawable(GradientDrawable.Orientation.BR_TL, colors).apply {
            cornerRadius = 24f
            gradientType = GradientDrawable.LINEAR_GRADIENT
            setGradientCenter(0.3f, 0.7f)
        }
    }
} 