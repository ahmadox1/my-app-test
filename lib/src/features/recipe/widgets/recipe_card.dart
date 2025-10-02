import 'package:flutter/material.dart';

import '../models/recipe_suggestion.dart';

class RecipeCard extends StatelessWidget {
  const RecipeCard({
    super.key,
    required this.suggestion,
  });

  final RecipeSuggestion? suggestion;

  @override
  Widget build(BuildContext context) {
    if (suggestion == null) {
      return const SizedBox.shrink();
    }
    final recipe = suggestion!;
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: const Color(0xFF0C3F38).withOpacity(0.8),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: Colors.white.withOpacity(0.04)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            recipe.title,
            style: const TextStyle(
              fontSize: 20,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            recipe.description,
            style: const TextStyle(height: 1.6, color: Color(0xFFB8D5CB)),
          ),
          const SizedBox(height: 16),
          _buildSection('المكونات', recipe.ingredients),
          const SizedBox(height: 16),
          _buildSection('الخطوات', recipe.steps, numbered: true),
          if (recipe.estimatedCalories != null) ...[
            const SizedBox(height: 16),
            Row(
              children: [
                const Icon(Icons.local_fire_department, color: Color(0xFFF7D46D)),
                const SizedBox(width: 8),
                Text(
                  'السعرات التقديرية: ${recipe.estimatedCalories} سعرة',
                  style: const TextStyle(fontWeight: FontWeight.w600),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSection(String title, List<String> items, {bool numbered = false}) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          title,
          style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 16),
        ),
        const SizedBox(height: 8),
        ...items.asMap().entries.map(
              (entry) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      numbered ? '${entry.key + 1}.' : '•',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Text(
                        entry.value,
                        style: const TextStyle(height: 1.6),
                      ),
                    ),
                  ],
                ),
              ),
            ),
      ],
    );
  }
}
