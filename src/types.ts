export enum RideCategory {
  X = "X",
  Comfort = "Comfort",
  Black = "Black",
  Flash = "Flash"
}

export const CategoryWeights: Record<RideCategory, number> = {
  [RideCategory.X]: 1.0,
  [RideCategory.Comfort]: 1.2,
  [RideCategory.Black]: 1.5,
  [RideCategory.Flash]: 0.8,
};

export enum ScoreRating {
  POOR = "Ruim",
  FAIR = "Regular",
  GOOD = "Bom",
  EXCELLENT = "Excelente"
}

export interface RideData {
  id: string;
  price: number;
  distanceKm: number;
  timeMin: number;
  category: RideCategory;
  timestamp: Date;
}

export interface AnalysisResult {
  score: number;
  rating: ScoreRating;
  pricePerKm: number;
}
