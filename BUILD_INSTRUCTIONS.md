# تعليمات بناء APK

## المتطلبات الأساسية

قبل بناء التطبيق، تأكد من توفر:

1. **Flutter SDK** (3.22.0 أو أحدث)
   - [تعليمات التثبيت](https://docs.flutter.dev/get-started/install)

2. **حساب Hugging Face** (مجاني)
   - [التسجيل](https://huggingface.co/join)
   - [الحصول على API Token](https://huggingface.co/settings/tokens)

3. **Android SDK** (يتم تثبيته تلقائياً مع Flutter)

## خطوات البناء

### 1. تثبيت المكتبات

```bash
cd /path/to/my-app-test
flutter pub get
```

### 2. التحقق من البيئة

```bash
flutter doctor
```

تأكد من أن جميع العناصر المطلوبة لـ Android متوفرة. إذا كانت هناك مشاكل، اتبع التعليمات المعروضة.

### 3. بناء APK مع مفتاح API (موصى به)

```bash
flutter build apk --release --dart-define=HF_API_TOKEN=hf_your_actual_token_here
```

**استبدل `hf_your_actual_token_here` بمفتاح API الخاص بك من Hugging Face.**

سيتم إنشاء الملف في:
```
build/app/outputs/flutter-apk/app-release.apk
```

### 4. بناء APK بدون مفتاح API (وضع تجريبي)

```bash
flutter build apk --release
```

⚠️ **ملاحظة:** في هذه الحالة، التطبيق سيعمل في الوضع التجريبي ويعرض وصفات احتياطية بدلاً من استخدام نموذج الذكاء الاصطناعي.

### 5. بناء APK منفصل لكل معمارية (حجم أصغر)

```bash
flutter build apk --split-per-abi --release --dart-define=HF_API_TOKEN=hf_your_token
```

سيتم إنشاء ملفات APK منفصلة:
- `app-armeabi-v7a-release.apk` (للأجهزة القديمة 32-bit)
- `app-arm64-v8a-release.apk` (للأجهزة الحديثة 64-bit)
- `app-x86_64-release.apk` (للمحاكيات)

## التحقق من البناء

بعد بناء APK، يمكنك تثبيته على جهاز Android:

### 1. باستخدام ADB

```bash
adb install build/app/outputs/flutter-apk/app-release.apk
```

### 2. نقل الملف يدوياً

انسخ ملف APK إلى جهاز Android وقم بتثبيته مباشرة.

## اختبار استخدام AI

للتأكد من أن التطبيق يستخدم نموذج الذكاء الاصطناعي:

1. **افتح التطبيق**
2. **أدخل مكونات مختلفة** (مثل: دجاج، أرز، طماطم)
3. **اختر نوع الوجبة** (عادية، صحية، أو دسمة)
4. **أدخل البيانات الصحية** (اختياري)
5. **اضغط "اقترح وصفة"**

إذا كان مفتاح API صحيحاً:
- ✅ ستظهر رسالة "جاري التحميل..."
- ✅ ستستغرق العملية بضع ثوانٍ (اتصال بالإنترنت)
- ✅ ستحصل على وصفة مخصصة ومختلفة في كل مرة

إذا لم يكن هناك مفتاح API:
- ⚠️ ستحصل على وصفة احتياطية بسيطة فوراً
- ⚠️ نفس الوصفة في كل مرة

## حل المشاكل الشائعة

### مشكلة: "Gradle wrapper not found" أو "gradlew: command not found"
**الحل:** تأكد من أن ملفات Gradle wrapper موجودة في المستودع:
- `android/gradlew` (Unix/Mac/Linux)
- `android/gradlew.bat` (Windows)
- `android/gradle/wrapper/gradle-wrapper.jar`
- `android/gradle/wrapper/gradle-wrapper.properties`

هذه الملفات مضمنة في المستودع ويجب أن تكون موجودة. إذا كانت مفقودة، يمكنك إعادة إنشائها باستخدام:
```bash
cd android
gradle wrapper --gradle-version 8.3
```

### مشكلة: "flutter command not found"
**الحل:** أضف Flutter إلى PATH:
```bash
export PATH="$PATH:/path/to/flutter/bin"
```

### مشكلة: "Android license not accepted"
**الحل:**
```bash
flutter doctor --android-licenses
```
ثم اقبل جميع الرخص.

### مشكلة: "Gradle build failed"
**الحل:**
```bash
cd android
./gradlew clean
cd ..
flutter clean
flutter pub get
flutter build apk --release
```

### مشكلة: "No connected devices"
**الحل:** تأكد من:
- تفعيل "خيارات المطور" و "USB Debugging" على جهاز Android
- توصيل الجهاز عبر USB
- تشغيل `adb devices` للتحقق

## بناء للتوزيع (App Bundle)

للرفع على Google Play Store، استخدم:

```bash
flutter build appbundle --release --dart-define=HF_API_TOKEN=hf_your_token
```

سيتم إنشاء الملف في:
```
build/app/outputs/bundle/release/app-release.aab
```

## الأمان

⚠️ **مهم جداً:**
- لا تشارك مفتاح API الخاص بك علنياً
- لا ترفع الـ APK أو الكود مع مفتاح API مضمّن على GitHub أو أي منصة عامة
- استخدم طرق آمنة لتضمين المفتاح في الإنتاج (مثل: environment variables، secure storage)

## ملاحظات إضافية

### حجم APK
- APK واحد شامل: ~40-50 MB
- APK لكل معمارية: ~20-25 MB لكل واحد

### متطلبات التشغيل
- Android 5.0 (API level 21) أو أحدث
- اتصال بالإنترنت (لاستخدام نموذج AI)
- ~100 MB مساحة فارغة

### الأداء
- أول استدعاء للنموذج قد يأخذ 5-10 ثوانٍ (تهيئة النموذج على خوادم Hugging Face)
- الاستدعاءات اللاحقة عادة أسرع (2-5 ثوانٍ)

## الدعم

إذا واجهت أي مشاكل في البناء:
1. راجع ملف [AI_VERIFICATION.md](AI_VERIFICATION.md) للتحقق من استخدام AI
2. راجع [Flutter documentation](https://docs.flutter.dev/)
3. افتح issue على GitHub

---

تم إنشاء هذا الملف لمساعدة المطورين في بناء APK للتطبيق بشكل صحيح.
