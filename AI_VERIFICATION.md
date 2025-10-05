# التحقق من استخدام نموذج الذكاء الاصطناعي

## ملخص

هذا التطبيق **يستخدم نموذج ذكاء اصطناعي حقيقي ومجاني** وليس محادثات مسجلة مسبقاً.

## تفاصيل النموذج المستخدم

- **اسم النموذج:** `Qwen/Qwen2.5-0.5B-Instruct`
- **المنصة:** Hugging Face Inference API
- **النوع:** نموذج لغوي مفتوح المصدر ومجاني
- **الرابط:** https://huggingface.co/Qwen/Qwen2.5-0.5B-Instruct

## كيف يعمل التطبيق؟

### 1. إدخال المستخدم
عندما يدخل المستخدم:
- المكونات (مثل: دجاج، زنجبيل، بصل)
- نوع الوجبة (عادية، صحية، دسمة)
- البيانات الصحية (الوزن، الطول، العمر، الهدف الغذائي)

### 2. بناء الـ Prompt
يتم بناء prompt مخصص يحتوي على جميع معلومات المستخدم في الدالة `_buildPrompt()`:

```dart
String _buildPrompt(RecipeRequest request, int? calories) {
  final buffer = StringBuffer()
    ..writeln('You are a helpful Arabic culinary assistant. Provide a detailed Arabic recipe.')
    ..writeln('### Task')
    ..writeln('اقترح وصفة ${request.mealStyle.label} باستخدام المكونات التالية: ${request.ingredients}.')
    // ... المزيد من التفاصيل
  return buffer.toString();
}
```

### 3. إرسال الطلب إلى نموذج AI
يتم إرسال طلب HTTP POST إلى Hugging Face API:

```dart
final response = await _httpClient.post(
  Uri.parse('$_endpointBase$_modelId'),
  headers: {
    'Authorization': 'Bearer $_token',
    'Content-Type': 'application/json',
  },
  body: jsonEncode({
    'inputs': prompt,  // يحتوي على معلومات المستخدم
    'parameters': {
      'max_new_tokens': 512,
      'temperature': 0.7,
      'top_p': 0.9,
      'return_full_text': false,
    },
  }),
);
```

### 4. استقبال الرد من النموذج
النموذج يولد وصفة جديدة في كل مرة بناءً على المدخلات المختلفة.

## الوصفة الاحتياطية (_fallbackRecipe)

الوصفة الاحتياطية **ليست** الطريقة الافتراضية للعمل، بل تُستخدم فقط في حالتين:

### الحالة الأولى: عدم وجود API Token
```dart
if (_token.isEmpty) {
  return _fallbackRecipe(request, calories);
}
```
إذا لم يتم تمرير `HF_API_TOKEN` عند بناء التطبيق.

### الحالة الثانية: فشل طلب API
```dart
if (response.statusCode >= 200 && response.statusCode < 300) {
  // استخدام رد النموذج
  return RecipeSuggestion.fromText(generated);
}
return _fallbackRecipe(request, calories);  // فقط عند الفشل
```

## كيفية التحقق من استخدام AI الحقيقي؟

### 1. فحص الكود
راجع الملف `lib/src/features/recipe/services/recipe_ai_service.dart`:
- السطور 33-49: إرسال الطلب إلى Hugging Face API
- السطر 18: تعريف النموذج المستخدم
- السطور 56-65: بناء prompt مخصص لكل طلب

### 2. بناء APK مع API Token
```bash
flutter build apk --release --dart-define=HF_API_TOKEN=hf_your_actual_token
```

عند استخدام token صالح، سيتم إرسال جميع الطلبات إلى النموذج الذكي.

### 3. اختبار التطبيق
- جرّب إدخال مكونات مختلفة
- غيّر نوع الوجبة
- أدخل بيانات صحية مختلفة
- ستلاحظ أن الوصفات تتغير بناءً على المدخلات

### 4. مراقبة الشبكة
إذا فحصت حركة الشبكة في التطبيق، ستجد طلبات POST إلى:
```
https://api-inference.huggingface.co/models/Qwen/Qwen2.5-0.5B-Instruct
```

## الخلاصة

✅ التطبيق يستخدم نموذج ذكاء اصطناعي حقيقي ومجاني من Hugging Face  
✅ كل طلب يُرسل إلى النموذج مع معلومات المستخدم  
✅ النموذج يولد وصفات جديدة في كل مرة  
✅ الوصفات ليست محفوظة مسبقاً أو مسجلة  
✅ الوصفة الاحتياطية تُستخدم فقط عند عدم وجود API token أو فشل الطلب  

## ملاحظة للمطورين

إذا أردت تغيير النموذج المستخدم إلى نموذج آخر مجاني من Hugging Face، ببساطة غيّر قيمة `_modelId` في الملف `recipe_ai_service.dart`:

```dart
static const _modelId = 'اسم-النموذج-الجديد';
```

تأكد من أن النموذج الجديد يدعم text generation ومتاح عبر Inference API المجاني.
