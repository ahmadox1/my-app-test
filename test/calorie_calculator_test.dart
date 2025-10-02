import 'package:flutter_test/flutter_test.dart';

import 'package:ai_recipe_chef/src/features/recipe/models/health_profile.dart';
import 'package:ai_recipe_chef/src/features/recipe/services/calorie_calculator.dart';

void main() {
  group('CalorieCalculator', () {
    const calculator = CalorieCalculator();

    test('returns null when profile incomplete', () {
      final profile = const HealthProfile(weightKg: 70);
      expect(calculator.estimateDailyCalories(profile), isNull);
    });

    test('calculates maintenance calories for maintain goal', () {
      final profile = const HealthProfile(
        weightKg: 70,
        heightCm: 175,
        age: 30,
        goal: HealthGoal.maintain,
      );
      final result = calculator.estimateDailyCalories(profile);
      expect(result, isNotNull);
      expect(result, greaterThan(1800));
      expect(result, lessThan(2600));
    });

    test('adjusts calories for weight loss goal', () {
      final profile = const HealthProfile(
        weightKg: 70,
        heightCm: 175,
        age: 30,
        goal: HealthGoal.lose,
      );
      final result = calculator.estimateDailyCalories(profile);
      expect(result, lessThan(2200));
    });
  });
}
