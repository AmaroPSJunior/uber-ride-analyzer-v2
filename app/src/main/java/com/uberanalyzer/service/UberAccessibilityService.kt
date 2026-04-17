package com.uberanalyzer.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.uberanalyzer.analyzer.RideAnalyzer
import com.uberanalyzer.overlay.OverlayService
import com.uberanalyzer.parser.RideParser

class UberAccessibilityService : AccessibilityService() {
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val rootNode = rootInActiveWindow ?: return
        val allText = mutableListOf<String>()
        collectAllText(rootNode, allText)
        
        if (allText.isEmpty()) return
        
        val fullText = allText.joinToString(" | ")
        
        if (fullText.contains("R$") || fullText.contains("km")) {
            val intent = Intent("DEBUG_LOG")
            intent.putExtra("log_text", fullText)
            sendBroadcast(intent)
        }

        RideParser.parse(fullText)?.let { rideData ->
            if (System.currentTimeMillis() - lastProcessedTime > 4000) {
                lastProcessedTime = System.currentTimeMillis()
                val analysis = RideAnalyzer.analyze(rideData)
                val intent = Intent(this, OverlayService::class.java).apply {
                    putExtra(OverlayService.EXTRA_PRICE, rideData.price)
                    putExtra(OverlayService.EXTRA_DISTANCE, rideData.distanceKm)
                    putExtra(OverlayService.EXTRA_TIME, rideData.timeMin)
                    putExtra(OverlayService.EXTRA_CATEGORY, rideData.category.displayName)
                    putExtra(OverlayService.EXTRA_SCORE, analysis.score)
                    putExtra(OverlayService.EXTRA_RATING, analysis.rating.name)
                }
                startService(intent)
            }
        }
    }

    private fun collectAllText(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        node.text?.let { list.add(it.toString()) }
        for (i in 0 until node.childCount) {
            collectAllText(node.getChild(i), list)
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        val intent = Intent("DEBUG_LOG")
        intent.putExtra("log_text", "Leitor conectado e pronto!")
        sendBroadcast(intent)
    }

    companion object {
        private var lastProcessedTime = 0L
    }
}
