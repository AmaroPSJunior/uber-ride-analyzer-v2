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
        // Heartbeat to UI every 3 seconds
        if (System.currentTimeMillis() - lastLogTime > 3000) {
            lastLogTime = System.currentTimeMillis()
            sendDebugLog("Monitorando... [${AccessibilityEvent.eventTypeToString(event.eventType)}]")
        }

        // Use rootInActiveWindow to get the full screen
        val rootNode = rootInActiveWindow ?: return
        
        // Scan the entire screen text
        val allText = mutableListOf<String>()
        collectAllText(rootNode, allText)
        val fullText = allText.joinToString(" | ")

        // Detection logic: Must have "R$" AND "Aceitar/Selecionar"
        val lowerText = fullText.lowercase()
        val hasPrice = lowerText.contains("r$")
        val hasAccept = lowerText.contains("aceitar") || lowerText.contains("selecionar") || lowerText.contains("confirmar")

        if (hasPrice && hasAccept) {
            // Find the specific card node for accurate parsing
            val actionButton = findActionNode(rootNode)
            val textToParse = if (actionButton != null) {
                val cardNode = findCardParent(actionButton)
                val cardStrings = mutableListOf<String>()
                collectAllText(cardNode, cardStrings)
                cardStrings.joinToString(" | ")
            } else {
                fullText
            }

            // Verify if the text looks like a valid ride again
            if (textToParse.contains("R$") && (textToParse.lowercase().contains("km") || textToParse.lowercase().contains("distância"))) {
                processRide(textToParse)
            }
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
        if (now - lastProcessedTime < 4000) return // Avoid spamming services
        
        val settings = com.uberanalyzer.settings.SettingsManager(this)
        val minKm = settings.getMinKmValue().toDouble()
        val minHour = settings.getMinHourValue().toDouble()

        RideParser.parse(text)?.let { rideData ->
            lastProcessedTime = now
            sendDebugLog("SOLICITAÇÃO: R$ ${rideData.price} | ${rideData.distanceKm} KM")
            
            val analysis = RideAnalyzer.analyze(rideData, minKm, minHour)
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
    }
}
