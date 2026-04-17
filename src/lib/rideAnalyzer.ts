import { RideData, AnalysisResult, ScoreRating, CategoryWeights } from '../types';

export function analyzeRide(ride: RideData): AnalysisResult {
  const pricePerKm = ride.distanceKm > 0 ? ride.price / ride.distanceKm : 0;
  
  // Weights based on the Kotlin implementation found in the user's repo
  const weight = CategoryWeights[ride.category] || 1.0;
  const timeBonus = ride.timeMin <= 15 ? 1.2 : 1.0;
  
  let score = pricePerKm * weight * timeBonus;
  
  // Normalizing to 0-10 range as in the original code
  // Assuming a baseline of ~3-4 BRL/km as a "good" score
  score = Math.min(Math.max(score, 0), 10);

  let rating = ScoreRating.POOR;
  if (score >= 8.5) rating = ScoreRating.EXCELLENT;
  else if (score >= 6.5) rating = ScoreRating.GOOD;
  else if (score >= 4.5) rating = ScoreRating.FAIR;

  return {
    score,
    rating,
    pricePerKm
  };
}
