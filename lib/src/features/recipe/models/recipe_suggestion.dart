class RecipeSuggestion {
  const RecipeSuggestion({
    required this.title,
    required this.description,
    required this.ingredients,
    required this.steps,
    this.estimatedCalories,
  });

  final String title;
  final String description;
  final List<String> ingredients;
  final List<String> steps;
  final int? estimatedCalories;

  factory RecipeSuggestion.fromText(String text) {
    final lines = text.split('\n').map((line) => line.trim()).toList();
    final ingredients = <String>[];
    final steps = <String>[];
    String? title;
    StringBuffer description = StringBuffer();

    List<String> currentList = ingredients;
    for (final line in lines) {
      if (line.isEmpty) continue;
      if (line.startsWith('#')) {
        title = line.replaceFirst('#', '').trim();
        continue;
      }
      if (line.contains('Ingredients') || line.contains('المكونات')) {
        currentList = ingredients;
        continue;
      }
      if (line.contains('Steps') || line.contains('الخطوات')) {
        currentList = steps;
        continue;
      }
      if (line.startsWith('-') || line.startsWith('*')) {
        currentList.add(line.replaceFirst(RegExp(r'^[-*]\s*'), ''));
      } else if (RegExp(r'^\d+').hasMatch(line)) {
        currentList = steps;
        currentList.add(line.replaceFirst(RegExp(r'^\d+[\).]*\s*'), ''));
      } else {
        description.writeln(line);
      }
    }

    return RecipeSuggestion(
      title: title ?? 'وصفة مقترحة',
      description: description.toString().trim(),
      ingredients: ingredients.isEmpty ? ['لم يتم تحديد المكونات.'] : ingredients,
      steps: steps.isEmpty ? ['لم يتم تحديد الخطوات.'] : steps,
      estimatedCalories: _extractCalories(text),
    );
  }

  static int? _extractCalories(String text) {
    final match = RegExp(r'(\d{2,4})\s*(kcal|سعرة)').firstMatch(text);
    if (match != null) {
      return int.tryParse(match.group(1)!);
    }
    return null;
  }
}
