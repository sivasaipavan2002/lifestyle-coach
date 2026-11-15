const ACTIVITY_MULTIPLIER = {
  LOW: 1.2,
  MODERATE: 1.55,
  HIGH: 1.725,
};

/**
 * Rough daily calorie/protein targets from the user's profile, using
 * the Mifflin-St Jeor BMR formula and a general 1.6g/kg protein
 * guideline -- a reasonable default, not a medical prescription.
 */
export function computeDailyGoals(profile) {
  if (!profile) {
    return null;
  }

  const { age, gender, heightCm, weightKg, workProfile } = profile;

  if (!age || !heightCm || !weightKg) {
    return null;
  }

  const isFemale = (gender || "").trim().toLowerCase().startsWith("f");

  const bmr = isFemale
    ? 10 * weightKg + 6.25 * heightCm - 5 * age - 161
    : 10 * weightKg + 6.25 * heightCm - 5 * age + 5;

  const multiplier =
    ACTIVITY_MULTIPLIER[workProfile?.activityLevel] ?? ACTIVITY_MULTIPLIER.MODERATE;

  return {
    calories: Math.round(bmr * multiplier),
    protein: Math.round(weightKg * 1.6),
  };
}
