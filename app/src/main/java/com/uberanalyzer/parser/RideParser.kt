package com.uberanalyzer.parser

import com.uberanalyzer.model.RideCategory
import com.uberanalyzer.model.RideData
import java.util.Locale

object RideParser {
    private val priceRegex = Regex("R\\$\\s*([0-9]{1,3}(?:[.][0-9]{3})*[,.][0-9]{2})")
    private val distanceRegex = Regex("([0-9]+[,.][0-9]+)\\s*km")
    private val timeRegex = Regex("([0-9]+)\\s*(min|minutos)")

    fun parse(text: String): RideData? {
        val lowerText = text.lowercase(Locale.getDefault())
        if (!lowerText.contains("r$")) return null

        // Price extraction
        val prices = priceRegex.findAll(text).mapNotNull { 
            val clean = it.groupValues[1].replace(".", "").replace(",", ".")
            clean.toDoubleOrNull() 
        }.toList()
        
        val price = prices.maxOrNull() ?: return null

        // Distance extraction
        val kms = distanceRegex.findAll(lowerText).mapNotNull { 
            it.groupValues[1].replace(",", ".").toDoubleOrNull() 
        }.toList()
        
        val dist = if (kms.size >= 2) kms.sum() else if (kms.isNotEmpty()) kms[0] else 1.0

        // Time extraction
        val mins = timeRegex.findAll(lowerText).mapNotNull { 
            it.groupValues[1].toIntOrNull()
        }.toList()
        
        val time = if (mins.size >= 2) mins.sum() else mins.firstOrNull() ?: 5

        return RideData(price, dist, time, RideCategory.fromString(text), text)
    }
}
