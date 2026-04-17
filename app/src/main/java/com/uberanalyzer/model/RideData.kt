package com.uberanalyzer.model

data class RideData(val price: Double, val distanceKm: Double, val timeMin: Int, val category: RideCategory, val raw: String)

enum class RideCategory(val displayName: String, val weight: Double, val colorHex: String) {
    UBER_X("UberX", 1.0, "#F2121212"), 
    COMFORT("Comfort", 1.3, "#F21A237E"), 
    BLACK("Black", 1.6, "#F2000000"), 
    FLASH("Flash", 0.9, "#F2E65100"),
    UNKNOWN("Uber", 1.0, "#F2121212");
    companion object {
        fun fromString(s: String) = when {
            s.contains("Black", true) -> BLACK
            s.contains("Comfort", true) -> COMFORT
            s.contains("Flash", true) -> FLASH
            else -> UBER_X
        }
    }
}

enum class ScoreRating(val label: String, val colorHex: String) {
    EXCELLENT("Excelente", "#4CAF50"), GOOD("Boa", "#8BC34A"), AVERAGE("OK", "#FFC107"), BAD("Ruim", "#F44336");
    companion object {
        fun fromScore(s: Double) = when { s >= 8.0 -> EXCELLENT; s >= 6.0 -> GOOD; s >= 4.0 -> AVERAGE; else -> BAD }
    }
}
