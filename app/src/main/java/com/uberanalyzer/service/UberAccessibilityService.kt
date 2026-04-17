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
        // Log basic event for heartbeat helping identifying if reader is alive
        if (System.currentTimeMillis() - lastLogTime > 3000) {
            lastLogTime = System.currentTimeMillis()
            val heartbeat = Intent("DEBUG_LOG")
            heartbeat.putExtra("log_text", "Monitorando Uber... (Event: ${AccessibilityEvent.eventTypeToString(event.eventType)})")
            sendBroadcast(heartbeat)
        }

        val source = event.source ?: return
        
        // Find the "Aceitar" or "Selecionar" button which strictly identifies the request card
        val actionButton = findActionNode(source)
        
        if (actionButton != null) {
            val cardNode = findCardContainer(actionButton)
            val cardText = mutableListOf<String>()
            collectAllText(cardNode, cardText)
            
            val fullText = cardText.joinToString(" | ")
            
            // Log what we found inside the card for debug
            val dbgIntent = Intent("DEBUG_LOG")
            dbgIntent.putExtra("log_text", "Card Detectado: ${if(fullText.length > 60) fullText.take(60) + "..." else fullText}")
            sendBroadcast(dbgIntent)

            // Check if it's a ride request
            val lowerText = fullText.lowercase()
            if (lowerText.contains("r$") && (lowerText.contains("km") || lowerText.contains("distância"))) {
                processRide(fullText)
            }
        }
    }

    private fun findActionNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        
        if (text.contains("aceitar") || text.contains("selecionar") || 
            contentDesc.contains("aceitar") || contentDesc.contains("selecionar")) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val result = findActionNode(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    private fun findCardContainer(node: AccessibilityNodeInfo): AccessibilityNodeInfo {
        var current = node
        // Traverse up to find a container that likely holds the whole card
        for (i in 0 until 6) {
            current.parent?.let { current = it } ?: break
        }
        return current
    }

    private fun processRide(fullText: String) {
        RideParser.parse(fullText)?.let { rideData ->
            if (System.currentTimeMillis() - lastProcessedTime > 4000) {
                lastProcessedTime = System.currentTimeMillis()
                val analysis = RideAnalyzer.analyze(rideData)
                val intentOver = Intent(this, OverlayService::class.java).apply {
                    putExtra(OverlayService.EXTRA_PRICE, rideData.price)
                    putExtra(OverlayService.EXTRA_DISTANCE, rideData.distanceKm)
                    putExtra(OverlayService.EXTRA_TIME, rideData.timeMin)
                    putExtra(OverlayService.EXTRA_CATEGORY, rideData.category.displayName)
                    putExtra(OverlayService.EXTRA_SCORE, analysis.score)
                    putExtra(OverlayService.EXTRA_RATING, analysis.rating.name)
                }
                startService(intentOver)
            }
        }
    }

    private fun collectAllText(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        node.text?.let { if (it.isNotBlank()) list.add(it.toString()) }
        node.contentDescription?.let { if (it.isNotBlank()) list.add(it.toString()) }
        for (i in 0 until node.childCount) {
            collectAllText(node.getChild(i), list)
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        val intent = Intent("DEBUG_LOG")
        intent.putExtra("log_text", "Aguardando solicitação da Uber...")
        sendBroadcast(intent)
    }

    companion object {
        private var lastProcessedTime = 0L
        private var lastLogTime = 0L
    }
}
