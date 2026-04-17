package com.uberanalyzer.analyzer

import com.uberanalyzer.model.RideData
import com.uberanalyzer.model.ScoreRating

object RideAnalyzer {
    data class AnalysisResult(val score: Double, val rating: ScoreRating, val pricePerKm: Double)

    fun analyze(ride: RideData): AnalysisResult {
        val pricePerKm = ride.price / ride.distanceKm
        val score = (pricePerKm * ride.category.weight * (if (ride.timeMin <= 15) 1.2 else 1.0)).coerceIn(0.0, 10.0)
        return AnalysisResult(score, ScoreRating.fromScore(score), pricePerKm)
    }
}
