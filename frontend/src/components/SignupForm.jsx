import { useState } from "react";

const emptyForm = {
  age: "",
  gender: "",
  heightCm: "",
  weightKg: "",
  workType: "",
  workHours: "",
  shift: "DAY",
  activityLevel: "MODERATE",
  foodPreference: "NON_VEGETARIAN",
  allergies: "",
  dislikes: "",
  mealsPerDay: "",
  cookingAvailable: true,
  fitnessLevel: "BEGINNER",
  exerciseMinutes: "",
  exerciseLocation: "HOME",
  equipment: "",
  primaryGoal: "WEIGHT_MANAGEMENT",
  target: "",
};

function toProfilePayload(name, form) {
  return {
    name,
    age: Number(form.age),
    gender: form.gender,
    heightCm: Number(form.heightCm),
    weightKg: Number(form.weightKg),
    workProfile: {
      workType: form.workType,
      workHours: Number(form.workHours),
      shift: form.shift,
      activityLevel: form.activityLevel,
    },
    nutritionProfile: {
      foodPreference: form.foodPreference,
      allergies: form.allergies || "None",
      dislikes: form.dislikes || "None",
      mealsPerDay: Number(form.mealsPerDay),
      cookingAvailable: form.cookingAvailable,
    },
    fitnessProfile: {
      fitnessLevel: form.fitnessLevel,
      exerciseMinutes: Number(form.exerciseMinutes),
      exerciseLocation: form.exerciseLocation,
      equipment: form.equipment || "None",
    },
    healthGoal: {
      primaryGoal: form.primaryGoal,
      target: form.target || "Not specified",
    },
  };
}

export default function SignupForm({ name, onSubmit, submitting, error }) {
  const [form, setForm] = useState(emptyForm);

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }));
  }

  function handleSubmit(event) {
    event.preventDefault();
    onSubmit(toProfilePayload(name, form));
  }

  return (
    <form className="signup-form" onSubmit={handleSubmit}>
      <h2>Let's set up your profile, {name}</h2>
      <p className="signup-form__hint">
        This is a one-time setup so the coach can tailor its advice to you.
      </p>

      <fieldset>
        <legend>Personal information</legend>
        <label>
          Age
          <input
            type="number"
            required
            min="1"
            value={form.age}
            onChange={(e) => update("age", e.target.value)}
          />
        </label>
        <label>
          Gender
          <input
            type="text"
            required
            value={form.gender}
            onChange={(e) => update("gender", e.target.value)}
          />
        </label>
        <label>
          Height (cm)
          <input
            type="number"
            required
            min="1"
            step="0.1"
            value={form.heightCm}
            onChange={(e) => update("heightCm", e.target.value)}
          />
        </label>
        <label>
          Weight (kg)
          <input
            type="number"
            required
            min="1"
            step="0.1"
            value={form.weightKg}
            onChange={(e) => update("weightKg", e.target.value)}
          />
        </label>
      </fieldset>

      <fieldset>
        <legend>Work & lifestyle</legend>
        <label>
          Work type
          <input
            type="text"
            placeholder="Desk / Field / Driver / Manual / Student / Other"
            required
            value={form.workType}
            onChange={(e) => update("workType", e.target.value)}
          />
        </label>
        <label>
          Working hours per day
          <input
            type="number"
            required
            min="0"
            value={form.workHours}
            onChange={(e) => update("workHours", e.target.value)}
          />
        </label>
        <label>
          Shift
          <select
            value={form.shift}
            onChange={(e) => update("shift", e.target.value)}
          >
            <option value="DAY">Day</option>
            <option value="NIGHT">Night</option>
            <option value="ROTATING">Rotating</option>
          </select>
        </label>
        <label>
          Daily activity level
          <select
            value={form.activityLevel}
            onChange={(e) => update("activityLevel", e.target.value)}
          >
            <option value="LOW">Low</option>
            <option value="MODERATE">Moderate</option>
            <option value="HIGH">High</option>
          </select>
        </label>
      </fieldset>

      <fieldset>
        <legend>Nutrition</legend>
        <label>
          Food preference
          <select
            value={form.foodPreference}
            onChange={(e) => update("foodPreference", e.target.value)}
          >
            <option value="VEGETARIAN">Vegetarian</option>
            <option value="NON_VEGETARIAN">Non-vegetarian</option>
            <option value="VEGAN">Vegan</option>
            <option value="EGGETARIAN">Eggetarian</option>
          </select>
        </label>
        <label>
          Food allergies
          <input
            type="text"
            placeholder="None"
            value={form.allergies}
            onChange={(e) => update("allergies", e.target.value)}
          />
        </label>
        <label>
          Foods you dislike
          <input
            type="text"
            placeholder="None"
            value={form.dislikes}
            onChange={(e) => update("dislikes", e.target.value)}
          />
        </label>
        <label>
          Typical meals per day
          <input
            type="number"
            required
            min="1"
            value={form.mealsPerDay}
            onChange={(e) => update("mealsPerDay", e.target.value)}
          />
        </label>
        <label className="signup-form__checkbox">
          <input
            type="checkbox"
            checked={form.cookingAvailable}
            onChange={(e) => update("cookingAvailable", e.target.checked)}
          />
          I have access to cooking
        </label>
      </fieldset>

      <fieldset>
        <legend>Fitness</legend>
        <label>
          Fitness level
          <select
            value={form.fitnessLevel}
            onChange={(e) => update("fitnessLevel", e.target.value)}
          >
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
          </select>
        </label>
        <label>
          Exercise time available (minutes/day)
          <input
            type="number"
            required
            min="0"
            value={form.exerciseMinutes}
            onChange={(e) => update("exerciseMinutes", e.target.value)}
          />
        </label>
        <label>
          Exercise location
          <select
            value={form.exerciseLocation}
            onChange={(e) => update("exerciseLocation", e.target.value)}
          >
            <option value="HOME">Home</option>
            <option value="GYM">Gym</option>
            <option value="OUTDOOR">Outdoor</option>
          </select>
        </label>
        <label>
          Available equipment
          <input
            type="text"
            placeholder="None / Basic / Full gym"
            value={form.equipment}
            onChange={(e) => update("equipment", e.target.value)}
          />
        </label>
      </fieldset>

      <fieldset>
        <legend>Goal</legend>
        <label>
          Main goal
          <select
            value={form.primaryGoal}
            onChange={(e) => update("primaryGoal", e.target.value)}
          >
            <option value="WEIGHT_MANAGEMENT">Weight management</option>
            <option value="STRENGTH">Strength</option>
            <option value="FITNESS">Fitness</option>
            <option value="HEALTHY_EATING">Healthy eating</option>
          </select>
        </label>
        <label>
          Describe your target
          <input
            type="text"
            value={form.target}
            onChange={(e) => update("target", e.target.value)}
          />
        </label>
      </fieldset>

      {error && <p className="signup-form__error">{error}</p>}

      <button type="submit" disabled={submitting}>
        {submitting ? "Setting up..." : "Start coaching"}
      </button>
    </form>
  );
}
