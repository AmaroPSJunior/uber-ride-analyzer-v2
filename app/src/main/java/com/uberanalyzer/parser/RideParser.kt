package com.uberanalyzer.parser

import com.uberanalyzer.model.RideCategory
import com.uberanalyzer.model.RideData
import java.util.Locale

object RideParser {
    fun parse(text: String): RideData? {
        val lowerText = text.lowercase(Locale.getDefault())
        if (!lowerText.contains("r$")) return null

        // Price extraction
        val priceRegex = Regex("R\\$\\s*([0-9]+[,.][0-9]{2})")
        val prices = priceRegex.findAll(text).mapNotNull { 
            it.groupValues[1].replace(",", ".").toDoubleOrNull()
        }.toList()
        val price = prices.maxOrNull() ?: return null

        // Distance extraction (pickup + trip)
        val distanceRegex = Regex("([0-9]+[,.][0-9]+)\\s*km")
        val kms = distanceRegex.findAll(lowerText).mapNotNull { 
            it.groupValues[1].replace(",", ".").toDoubleOrNull()
        }.toList()
        // Uber often shows: "X km away" and "Y km trip". We want X + Y.
        val dist = if (kms.size >= 2) {
            // Check if one value is suspiciously small (pickup) and add them
            kms.sum()
        } else {
            kms.firstOrNull() ?: 1.0
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
