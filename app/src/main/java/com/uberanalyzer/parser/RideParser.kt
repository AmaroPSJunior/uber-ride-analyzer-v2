package com.uberanalyzer.parser

import com.uberanalyzer.model.RideCategory
import com.uberanalyzer.model.RideData
import java.util.Locale

object RideParser {
    fun parse(text: String): RideData? {
        val lowerText = text.lowercase(Locale.getDefault())
        if (!lowerText.contains("r$")) return null

        // Price: Matches R$ XX,XX - specifically look for the main price
        // We match R$ followed by numbers. We take the first one found in the card usually.
        val priceRegex = Regex("R\\$\\s*([0-9]{1,3}(?:[.][0-9]{3})*[,.][0-9]{2})")
        val prices = priceRegex.findAll(text).mapNotNull { 
            val clean = it.groupValues[1].replace(".", "").replace(",", ".")
            clean.toDoubleOrNull() 
        }.toList()
        // In the request card, the largest price is usually the payout
        val price = prices.maxOrNull() ?: return null

        // Distance: Matches XX,X km or XX.X km
        val distanceRegex = Regex("([0-9]+[,.][0-9]+)\\s*km")
        val kms = distanceRegex.findAll(lowerText).mapNotNull { 
            it.groupValues[1].replace(",", ".").toDoubleOrNull() 
        }.toList()
        // Uber request card usually has 2 KM values: Pickup and Trip. 
        // We sum them for actual total effort.
        val dist = if (kms.size >= 2) {
            kms.sum()
        } else if (kms.isNotEmpty()) {
            kms[0]
        } else {
            1.0 // Fallback
        }

        // Time extraction (pickup + trip)
        val timeRegex = Regex("([0-9]+)\\s*(min|minutos)")
        val mins = timeRegex.findAll(lowerText).mapNotNull { 
            it.groupValues[1].toIntOrNull()
        }.toList()
        // Similar to distance, we sum the times
        val time = if (mins.size >= 2) {
            mins.sum()
        } else {
            mins.firstOrNull() ?: 5
        }

        return RideData(price, dist, time, RideCategory.fromString(text), text)
    }
}
