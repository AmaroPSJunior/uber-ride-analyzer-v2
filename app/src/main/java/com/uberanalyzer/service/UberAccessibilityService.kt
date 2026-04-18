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
        val pkg = event.packageName?.toString() ?: ""
        if (!pkg.contains("ubercab")) return

        // Instant filter for relevant UI changes
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && 
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        // Heartbeat to UI every 5 seconds
        if (System.currentTimeMillis() - lastLogTime > 5000) {
            lastLogTime = System.currentTimeMillis()
            sendDebugLog("Vigiando Uber... [Instant Mode]")
        }

        val rootNode = rootInActiveWindow ?: return
        val allText = mutableListOf<String>()
        collectAllText(rootNode, allText)
        val fullText = allText.joinToString(" | ")

        // Optimization: skip if nothing changed on screen
        if (fullText == lastFullText) return
        lastFullText = fullText

        val lowerText = fullText.lowercase()
        if (lowerText.contains("r$") && (lowerText.contains("aceitar") || lowerText.contains("selecionar") || lowerText.contains("confirmar"))) {
            // Priority check for faster response
            processRide(fullText)
        }
    }

    private fun findActionNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = (node.text ?: "").toString().lowercase()
        val desc = (node.contentDescription ?: "").toString().lowercase()
        
        if (text.contains("aceitar") || text.contains("selecionar") || desc.contains("aceitar") || desc.contains("selecionar")) {
            return node
        }
        
        for (i in 0 until node.childCount) {
            val result = findActionNode(node.getChild(i))
            if (result != null) return result
        }
        return null
    }

    private fun findCardParent(node: AccessibilityNodeInfo): AccessibilityNodeInfo {
        var current = node
        // Traverse up to find a container (usually the card is 5-7 levels up)
        for (i in 0 until 7) {
            current.parent?.let { current = it } ?: break
        }
        return current
    }

    private fun processRide(text: String) {
        val now = System.currentTimeMillis()
        if (now - lastProcessedTime < 800) return // Instant: 0.8s cooldown
        
        val settings = com.uberanalyzer.settings.SettingsManager(this)
        val minKm = settings.getMinKmValue().toDouble()
        val minHour = settings.getMinHourValue().toDouble()

        RideParser.parse(text)?.let { rideData ->
            lastProcessedTime = now
            sendDebugLog("RIDE: R$ ${rideData.price} | ${rideData.distanceKm}KM")
            
            val intent = Intent(this, OverlayService::class.java).apply {
                val analysis = RideAnalyzer.analyze(rideData, minKm, minHour)
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

    private fun collectAllText(node: AccessibilityNodeInfo?, list: MutableList<String>) {
        if (node == null) return
        node.text?.let { if (it.isNotBlank()) list.add(it.toString()) }
        node.contentDescription?.let { if (it.isNotBlank()) list.add(it.toString()) }
        for (i in 0 until node.childCount) {
            collectAllText(node.getChild(i), list)
        }
    }

    private fun sendDebugLog(text: String) {
        val intent = Intent("DEBUG_LOG")
        intent.putExtra("log_text", text)
        intent.setPackage(packageName) // Restrict broadcast to the app for more reliability
        sendBroadcast(intent)
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        sendDebugLog("Leitor pronto para detectar Uber!")
    }

    companion object {
        private var lastProcessedTime = 0L
        private var lastLogTime = 0L
        private var lastFullText = ""
    }
}
