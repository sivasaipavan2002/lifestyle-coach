const numberFormat = new Intl.NumberFormat(undefined, {
  maximumFractionDigits: 1,
});

function Bar({ value, goal }) {
  const percent = goal ? Math.min(100, Math.round((value / goal) * 100)) : 0;

  return (
    <div className="nutrition-bar">
      <div className="nutrition-bar__track">
        <div className="nutrition-bar__fill" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
}

export default function NutritionDashboard({ nutrition, goals }) {
  const values = nutrition ?? {
    calories: 0,
    protein: 0,
    carbohydrates: 0,
    fat: 0,
    fiber: 0,
  };

  return (
    <div className="nutrition-dashboard">
      <div className="nutrition-dashboard__primary">
        <div className="nutrition-metric">
          <div className="nutrition-metric__head">
            <span className="nutrition-metric__label">Calories</span>
            <span className="nutrition-metric__value">
              {numberFormat.format(values.calories)}
              {goals?.calories ? (
                <span className="nutrition-metric__goal">
                  {" "}
                  / {numberFormat.format(goals.calories)} kcal
                </span>
              ) : (
                " kcal"
              )}
            </span>
          </div>
          {goals?.calories && <Bar value={values.calories} goal={goals.calories} />}
        </div>

        <div className="nutrition-metric">
          <div className="nutrition-metric__head">
            <span className="nutrition-metric__label">Protein</span>
            <span className="nutrition-metric__value">
              {numberFormat.format(values.protein)}g
              {goals?.protein && (
                <span className="nutrition-metric__goal">
                  {" "}
                  / {numberFormat.format(goals.protein)}g
                </span>
              )}
            </span>
          </div>
          {goals?.protein && <Bar value={values.protein} goal={goals.protein} />}
        </div>
      </div>

      <div className="nutrition-dashboard__secondary">
        <div>
          <span>{numberFormat.format(values.carbohydrates)}g</span>
          carbs
        </div>
        <div>
          <span>{numberFormat.format(values.fat)}g</span>
          fat
        </div>
        <div>
          <span>{numberFormat.format(values.fiber)}g</span>
          fiber
        </div>
      </div>
    </div>
  );
}
