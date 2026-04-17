package com.uberanalyzer.service

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.uberanalyzer.analyzer.RideAnalyzer
import com.uberanalyzer.overlay.OverlayService
import com.uberanalyzer.parser.RideParser

class UberNotificationService : NotificationListenerService() {
    companion object {
        var isRunning = false
        var lastCapturedText = "Nenhuma notificação da Uber detectada ainda."
        var notificationCount = 0
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        isRunning = true
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        isRunning = false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkg = sbn?.packageName ?: return
        
        // Capture ANYTHING related to Uber for diagnostic
        if (pkg.contains("uber", ignoreCase = true)) {
            notificationCount++
            val notification = sbn.notification ?: return
            val extras = notification.extras ?: return
            
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val fullText = "APP: $pkg\nTÍTULO: $title\nTEXTO: $text"
            
            lastCapturedText = fullText

            // Try to parse and show overlay if data is valid
            RideParser.parse("$title $text")?.let { rideData ->
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
}
