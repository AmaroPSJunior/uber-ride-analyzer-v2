package com.uberanalyzer.model

data class RideData(val price: Double, val distanceKm: Double, val timeMin: Int, val category: RideCategory, val raw: String)

enum class RideCategory(val displayName: String, val weight: Double) {
    UBER_X("UberX", 1.0), COMFORT("Comfort", 1.3), BLACK("Black", 1.6), UNKNOWN("Uber", 1.0);
    companion object {
        fun fromString(s: String) = when {
            s.contains("Black", true) -> BLACK
            s.contains("Comfort", true) -> COMFORT
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
