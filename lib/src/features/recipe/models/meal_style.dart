enum MealStyle {
  normal('عادي'),
  healthy('صحي'),
  indulgent('دسم');

  const MealStyle(this.label);
  final String label;

  String get promptValue => switch (this) {
        MealStyle.normal => 'balanced everyday meal',
        MealStyle.healthy => 'healthy low-fat meal',
        MealStyle.indulgent => 'rich and indulgent meal',
      };
}
