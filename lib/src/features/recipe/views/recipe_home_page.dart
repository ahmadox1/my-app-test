import 'package:flutter/material.dart';

import '../models/health_profile.dart';
import '../models/meal_style.dart';
import '../models/recipe_request.dart';
import '../models/recipe_suggestion.dart';
import '../services/recipe_ai_service.dart';
import '../widgets/health_data_card.dart';
import '../widgets/illustration_header.dart';
import '../widgets/meal_style_selector.dart';
import '../widgets/recipe_card.dart';

class RecipeHomePage extends StatefulWidget {
  const RecipeHomePage({super.key});

  @override
  State<RecipeHomePage> createState() => _RecipeHomePageState();
}

class _RecipeHomePageState extends State<RecipeHomePage> {
  final _ingredientsController = TextEditingController();
  final _weightController = TextEditingController();
  final _heightController = TextEditingController();
  final _ageController = TextEditingController();
  final _service = RecipeAIService();

  MealStyle _selectedMealStyle = MealStyle.normal;
  HealthGoal? _goal;
  bool _showHealthData = false;
  bool _isLoading = false;
  RecipeSuggestion? _suggestion;

  @override
  void dispose() {
    _ingredientsController.dispose();
    _weightController.dispose();
    _heightController.dispose();
    _ageController.dispose();
    _service.dispose();
    super.dispose();
  }

  Future<void> _suggestRecipe() async {
    final ingredients = _ingredientsController.text.trim();
    if (ingredients.isEmpty) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('يرجى إدخال المكونات أولًا.')),
      );
      return;
    }

    final profile = HealthProfile(
      weightKg: double.tryParse(_weightController.text.replaceAll(',', '.')),
      heightCm: double.tryParse(_heightController.text.replaceAll(',', '.')),
      age: int.tryParse(_ageController.text),
      goal: _goal,
    );

    setState(() {
      _isLoading = true;
    });

    try {
      final request = RecipeRequest(
        ingredients: ingredients,
        mealStyle: _selectedMealStyle,
        healthProfile: profile,
      );
      final suggestion = await _service.generateRecipe(request);
      setState(() {
        _suggestion = suggestion;
      });
    } catch (error) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('حدث خطأ غير متوقع: $error')),
      );
    } finally {
      if (mounted) {
        setState(() {
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final gradient = const LinearGradient(
      colors: [Color(0xFF05332F), Color(0xFF0A4A3E)],
      begin: Alignment.topCenter,
      end: Alignment.bottomCenter,
    );

    return Directionality(
      textDirection: TextDirection.rtl,
      child: Scaffold(
        body: Container(
          decoration: BoxDecoration(gradient: gradient),
          child: SafeArea(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  const IllustrationHeader(),
                  const SizedBox(height: 16),
                  TextField(
                    controller: _ingredientsController,
                    textDirection: TextDirection.rtl,
                    decoration: const InputDecoration(
                      hintText: 'مثال: دجاج، زنجبيل، بصل...'
                          '\nأدخل المكونات التي ترغب باستخدامها',
                      prefixIcon: Icon(Icons.receipt_long, color: Colors.white70),
                    ),
                    maxLines: 2,
                  ),
                  const SizedBox(height: 20),
                  MealStyleSelector(
                    value: _selectedMealStyle,
                    onChanged: (style) {
                      setState(() => _selectedMealStyle = style);
                    },
                  ),
                  const SizedBox(height: 20),
                  HealthDataCard(
                    expanded: _showHealthData,
                    onToggle: () {
                      setState(() => _showHealthData = !_showHealthData);
                    },
                    weightController: _weightController,
                    heightController: _heightController,
                    ageController: _ageController,
                    goal: _goal,
                    onGoalChanged: (goal) {
                      setState(() => _goal = goal);
                    },
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    child: ElevatedButton.icon(
                      onPressed: _isLoading ? null : _suggestRecipe,
                      icon: const Icon(Icons.lightbulb),
                      label: Text(_isLoading ? 'جاري التحميل...' : 'اقترح وصفة!'),
                    ),
                  ),
                  const SizedBox(height: 24),
                  AnimatedSwitcher(
                    duration: const Duration(milliseconds: 250),
                    child: _isLoading
                        ? const Center(
                            child: Padding(
                              padding: EdgeInsets.all(24),
                              child: CircularProgressIndicator(),
                            ),
                          )
                        : RecipeCard(suggestion: _suggestion),
                  ),
                  const SizedBox(height: 40),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
