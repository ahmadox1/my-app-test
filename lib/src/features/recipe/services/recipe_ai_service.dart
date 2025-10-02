import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/health_profile.dart';
import '../models/meal_style.dart';
import '../models/recipe_request.dart';
import '../models/recipe_suggestion.dart';
import 'calorie_calculator.dart';

class RecipeAIService {
  RecipeAIService({http.Client? httpClient})
      : _httpClient = httpClient ?? http.Client();

  final http.Client _httpClient;
  final _calorieCalculator = const CalorieCalculator();

  static const _modelId = 'Qwen/Qwen2.5-0.5B-Instruct';
  static const _endpointBase = 'https://api-inference.huggingface.co/models/';
  static const String _token = String.fromEnvironment('HF_API_TOKEN');

  Future<RecipeSuggestion> generateRecipe(RecipeRequest request) async {
    final calories = _calorieCalculator.estimateDailyCalories(request.healthProfile);
    final prompt = _buildPrompt(request, calories);

    if (_token.isEmpty) {
      return _fallbackRecipe(request, calories);
    }

    final response = await _httpClient.post(
      Uri.parse('$_endpointBase$_modelId'),
      headers: {
        'Authorization': 'Bearer $_token',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({
        'inputs': prompt,
        'parameters': {
          'max_new_tokens': 512,
          'temperature': 0.7,
          'top_p': 0.9,
          'return_full_text': false,
        },
      }),
    );

    if (response.statusCode >= 200 && response.statusCode < 300) {
      final body = jsonDecode(response.body);
      final generated = _extractText(body);
      if (generated != null && generated.isNotEmpty) {
        return RecipeSuggestion.fromText(generated);
      }
    }

    return _fallbackRecipe(request, calories);
  }

  String _buildPrompt(RecipeRequest request, int? calories) {
    final buffer = StringBuffer()
      ..writeln('You are a helpful Arabic culinary assistant. Provide a detailed Arabic recipe.')
      ..writeln('### Task')
      ..writeln('اقترح وصفة ${request.mealStyle.label} باستخدام المكونات التالية: ${request.ingredients}.')
      ..writeln('املأ الاستجابة بالعناصر التالية:')
      ..writeln('1. عنوان قصير للوصفة.')
      ..writeln('2. فقرة وصف موجزة باللهجة العربية الفصحى.')
      ..writeln('3. قائمة المكونات بنقاط.')
      ..writeln('4. خطوات التحضير مرقمة.')
      ..writeln('5. إجمالي السعرات الحرارية التقديرية إن أمكن.');

    if (request.healthProfile.goal != null) {
      buffer.writeln('الهدف الغذائي للمستخدم: ${request.healthProfile.goal!.label}.');
    }
    if (calories != null) {
      buffer.writeln('الحد الأقصى الموصى به للسعرات الحرارية: $calories سعرة حرارية.');
    }

    buffer
      ..writeln('### Style')
      ..writeln('اكتب باللغة العربية الحديثة مع نبرة ودودة ومحترفة.');

    return buffer.toString();
  }

  RecipeSuggestion _fallbackRecipe(RecipeRequest request, int? calories) {
    final style = request.mealStyle;
    final text = '''# وصفة ${style.label} سريعة

المكونات المقترحة:${calories != null ? '\n- إجمالي السعرات المستهدفة: ~$calories سعرة' : ''}
- ${request.ingredients.replaceAll(',', '\n- ')}

الخطوات
1. جهّز المكونات واغسلها جيدًا.
2. سخّن مقلاة على نار متوسطة مع القليل من زيت الزيتون.
3. أضف المكونات وفق ترتيب نضجها وقلّب حتى تنضج.
4. تبّل بالبهارات المفضلة وقدّم الطبق ساخنًا.

الإجمالي التقريبي للسعرات: ${calories ?? 550} سعرة.''';

    return RecipeSuggestion.fromText(text);
  }

  void dispose() {
    _httpClient.close();
  }

  String? _extractText(dynamic body) {
    if (body is List && body.isNotEmpty) {
      final first = body.first;
      if (first is Map && first['generated_text'] is String) {
        return first['generated_text'] as String;
      }
      if (first is Map && first['generated_text'] is List) {
        return first['generated_text'].join('\n');
      }
    } else if (body is Map && body['generated_text'] is String) {
      return body['generated_text'] as String;
    }
    return null;
  }
}
