package com.uberanalyzer.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.uberanalyzer.analyzer.RideAnalyzer
import com.uberanalyzer.overlay.OverlayService
import com.uberanalyzer.parser.RideParser
import java.util.concurrent.Executors

class UberAccessibilityService : AccessibilityService() {
    
    private val executor = Executors.newSingleThreadExecutor()
    private var isTaskPending = false
    
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: ""
        if (!pkg.contains("ubercab")) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED && 
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        if (System.currentTimeMillis() - lastLogTime > 10000) {
            lastLogTime = System.currentTimeMillis()
            sendDebugLog("Vigiando Uber [Turbo Mode]...")
        }

        // Quick return for the Accessibility Thread
        if (isTaskPending) return
        
        isTaskPending = true
        executor.execute {
            try {
                performTurboScan()
            } finally {
                isTaskPending = false
            }
        }
    }

    private fun performTurboScan() {
        val rootNode = rootInActiveWindow ?: return
        try {
            val sb = StringBuilder()
            collectTextFast(rootNode, sb)
            val fullText = sb.toString()

            if (fullText == lastFullText) return
            lastFullText = fullText

            val lowerText = fullText.lowercase()
            if (lowerText.contains("r$") && (lowerText.contains("aceitar") || lowerText.contains("selecionar") || lowerText.contains("confirmar"))) {
                processRideInstant(fullText)
            }
        } finally {
            try { rootNode.recycle() } catch (e: Exception) {}
        }
    }

    @Deprecated("Use collectTextFast for turbo performance")
    private fun findActionNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? = null

    private fun processRideInstant(text: String) {
        val now = System.currentTimeMillis()
        if (now - lastProcessedTime < 500) return // Instant Mode: 0.5s cooldown
        
        val settings = com.uberanalyzer.settings.SettingsManager(this)
        val minKm = settings.getMinKmValue().toDouble()
        val minHour = settings.getMinHourValue().toDouble()

        RideParser.parse(text)?.let { rideData ->
            lastProcessedTime = now
            sendDebugLog("RIDE: R$ ${rideData.price} | ${rideData.distanceKm}KM")
            
            val analysis = RideAnalyzer.analyze(rideData, minKm, minHour)
            
            // SAVE TO HISTORY DATABASE
            com.uberanalyzer.db.RideHistoryManager(this).saveRide(rideData, analysis.score, analysis.rating.name)

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

    private fun collectTextFast(node: AccessibilityNodeInfo?, sb: StringBuilder) {
        if (node == null) return
        node.text?.let { if (it.isNotBlank()) sb.append(it).append(" | ") }
        node.contentDescription?.let { if (it.isNotBlank()) sb.append(it).append(" | ") }
        
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                collectTextFast(child, sb)
                try { child.recycle() } catch (e: Exception) {}
            }
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
