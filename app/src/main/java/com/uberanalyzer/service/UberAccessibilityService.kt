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
        
        // Find the "Action Card" (the popup containing ride info)
        // We look for the "Selecionar" or "Aceitar" button to identify the card
        val actionButton = findActionNode(rootNode)
        
        if (actionButton != null) {
            // Get text ONLY from the card container (parent of the action button or the node itself)
            // Uber request card usually has a specific structure. 
            // We'll collect text from the card and its children.
            val cardNode = findCardContainer(actionButton)
            val cardText = mutableListOf<String>()
            collectAllText(cardNode, cardText)
            
            val fullText = cardText.joinToString(" | ")
            
            // Only process if it looks like a ride (has price and distance)
            if (fullText.contains("R$") && (fullText.lowercase().contains("km") || fullText.lowercase().contains("distância"))) {
                processRide(fullText)
            }
        }
    }

    private fun findActionNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
        if (node == null) return null
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        
        if (text == "selecionar" || text == "aceitar" || text == "confirmar" || 
            contentDesc == "selecionar" || contentDesc == "aceitar") {
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
        // Uber popup is usually 4-6 levels deep from the root of the card
        for (i in 0 until 5) {
            current.parent?.let { current = it } ?: break
        }
        return current
    }

    private fun processRide(fullText: String) {
        val intent = Intent("DEBUG_LOG")
        intent.putExtra("log_text", "CARD: $fullText")
        sendBroadcast(intent)

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
        intent.putExtra("log_text", "Leitor conectado e pronto!")
        sendBroadcast(intent)
    }

    companion object {
        private var lastProcessedTime = 0L
    }
}
