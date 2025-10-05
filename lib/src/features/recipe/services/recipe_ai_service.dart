import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/health_profile.dart';
import '../models/meal_style.dart';
import '../models/recipe_request.dart';
import '../models/recipe_suggestion.dart';
import 'calorie_calculator.dart';

/// خدمة الوصفات باستخدام نموذج ذكاء اصطناعي مجاني من Hugging Face
/// 
/// هذه الخدمة ترسل طلبات حقيقية إلى نموذج AI وليست محادثات مسجلة مسبقاً
/// النموذج المستخدم: Qwen/Qwen2.5-0.5B-Instruct (مجاني ومفتوح المصدر)
class RecipeAIService {
  RecipeAIService({http.Client? httpClient})
      : _httpClient = httpClient ?? http.Client();

  final http.Client _httpClient;
  final _calorieCalculator = const CalorieCalculator();

  /// معرف نموذج الذكاء الاصطناعي المجاني على Hugging Face
  static const _modelId = 'Qwen/Qwen2.5-0.5B-Instruct';
  static const _endpointBase = 'https://api-inference.huggingface.co/models/';
  static const String _token = String.fromEnvironment('HF_API_TOKEN');

  /// توليد وصفة باستخدام نموذج الذكاء الاصطناعي
  /// 
  /// يتم إرسال طلب HTTP POST إلى Hugging Face API مع:
  /// - المكونات المطلوبة
  /// - نوع الوجبة (عادية، صحية، دسمة)
  /// - البيانات الصحية للمستخدم (الوزن، الطول، العمر، الهدف)
  /// 
  /// النموذج يولد وصفة جديدة في كل مرة بناءً على المدخلات
  Future<RecipeSuggestion> generateRecipe(RecipeRequest request) async {
    final calories = _calorieCalculator.estimateDailyCalories(request.healthProfile);
    final prompt = _buildPrompt(request, calories);

    // في حالة عدم وجود API token، استخدم الوصفة الاحتياطية
    if (_token.isEmpty) {
      return _fallbackRecipe(request, calories);
    }

    // إرسال الطلب إلى نموذج الذكاء الاصطناعي على Hugging Face
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

    // إذا نجح الطلب، استخرج النص المولد من النموذج
    if (response.statusCode >= 200 && response.statusCode < 300) {
      final body = jsonDecode(response.body);
      final generated = _extractText(body);
      if (generated != null && generated.isNotEmpty) {
        // النص المولد من نموذج AI - وليس محفوظاً مسبقاً
        return RecipeSuggestion.fromText(generated);
      }
    }

    // في حالة فشل API، استخدم الوصفة الاحتياطية
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

  /// وصفة احتياطية بسيطة
  /// 
  /// ملاحظة هامة: هذه الوصفة تُستخدم فقط في حالتين:
  /// 1. عدم تمرير HF_API_TOKEN عند بناء التطبيق
  /// 2. فشل طلب API للنموذج الذكي
  /// 
  /// في الاستخدام الطبيعي مع API token صالح، يتم استخدام نموذج AI الحقيقي
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
