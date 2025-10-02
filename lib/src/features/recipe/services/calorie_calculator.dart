import 'dart:math';

import '../models/health_profile.dart';

class CalorieCalculator {
  const CalorieCalculator();

  int? estimateDailyCalories(HealthProfile profile) {
    if (!profile.isComplete) return null;
    final weight = profile.weightKg!;
    final height = profile.heightCm!;
    final age = profile.age!;

    // معادلة Mifflin-St Jeor مع افتراض النشاط المعتدل.
    final bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
    final maintenance = bmr * 1.375;

    final adjustment = switch (profile.goal!) {
      HealthGoal.maintain => 0.0,
      HealthGoal.lose => -300,
      HealthGoal.gain => 300,
    };

    return max(1200, (maintenance + adjustment).round());
  }
}
