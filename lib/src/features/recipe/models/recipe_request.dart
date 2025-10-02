import 'meal_style.dart';
import 'health_profile.dart';

class RecipeRequest {
  const RecipeRequest({
    required this.ingredients,
    required this.mealStyle,
    this.healthProfile = const HealthProfile(),
  });

  final String ingredients;
  final MealStyle mealStyle;
  final HealthProfile healthProfile;
}
