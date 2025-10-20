# دليل البدء السريع - تطبيق ذكريات AR
# Quick Start Guide - AR Memory App

## البدء في 5 دقائق / Get Started in 5 Minutes

### الخطوة 1: تثبيت المتطلبات / Step 1: Install Requirements

```bash
# تحقق من تثبيت Flutter / Check Flutter installation
flutter --version

# إذا لم يكن مثبتاً، حمّله من:
# If not installed, download from:
# https://docs.flutter.dev/get-started/install
```

### الخطوة 2: إعداد Google Maps / Step 2: Setup Google Maps

1. اذهب إلى / Go to: https://console.cloud.google.com/
2. أنشئ مشروع جديد / Create new project
3. فعّل Google Maps SDK for Android / Enable Google Maps SDK for Android
4. أنشئ API Key / Create API Key
5. افتح / Open: `android/app/src/main/AndroidManifest.xml`
6. استبدل / Replace:
   ```xml
   YOUR_GOOGLE_MAPS_API_KEY_HERE
   ```
   بمفتاحك / with your key

### الخطوة 3: تثبيت المكتبات / Step 3: Install Dependencies

```bash
cd path/to/ar_memory_app
flutter pub get
```

### الخطوة 4: تشغيل التطبيق / Step 4: Run the App

```bash
# على جهاز متصل أو محاكي / On connected device or emulator
flutter run

# أو استخدم السكريبت / Or use the script
chmod +x setup.sh
./setup.sh
```

### الخطوة 5: بناء APK / Step 5: Build APK

```bash
flutter build apk --release

# ستجد APK في / APK will be at:
# build/app/outputs/flutter-apk/app-release.apk
```

---

## استخدام التطبيق / Using the App

### 📷 عرض الكاميرا / Camera View

- **يفتح تلقائياً** عند تشغيل التطبيق
- **Opens automatically** when app starts

### ✍️ إضافة ذكرى / Add Memory

1. اكتب نصاً في الحقل بالأسفل / Write text in bottom field
2. اضغط أيقونة الإرسال / Press send icon
3. يتم الحفظ بموقعك الحالي / Saved at current location

### 🗺️ عرض الخريطة / View Map

1. اضغط زر "عرض الخريطة" / Press "View Map" button
2. شاهد جميع الذكريات / See all memories
3. اضغط على علامة لعرض التفاصيل / Tap marker for details

### 👀 مشاهدة الذكريات القريبة / View Nearby Memories

- تظهر تلقائياً على شاشة الكاميرا
- Shows automatically on camera screen
- ضمن نطاق 50 متر / Within 50 meters radius

---

## استكشاف المشاكل السريع / Quick Troubleshooting

### الكاميرا لا تعمل / Camera not working
```bash
# امنح أذونات الكاميرا / Grant camera permissions
الإعدادات → التطبيقات → ذكريات AR → الأذونات
Settings → Apps → AR Memories → Permissions
```

### الموقع لا يعمل / Location not working
```bash
# فعّل GPS وامنح الأذونات / Enable GPS and grant permissions
الإعدادات → الموقع → تشغيل
Settings → Location → ON
```

### الخريطة فارغة / Map is empty
```bash
# تحقق من API Key / Check API Key
android/app/src/main/AndroidManifest.xml
```

### مشاكل البناء / Build issues
```bash
flutter clean
flutter pub get
flutter build apk --release
```

---

## الأوامر المفيدة / Useful Commands

### تشغيل بوضع التطوير / Run in debug mode
```bash
flutter run
```

### تشغيل بوضع الإصدار / Run in release mode
```bash
flutter run --release
```

### مشاهدة السجلات / View logs
```bash
flutter run -v
```

### تحليل الكود / Analyze code
```bash
flutter analyze
```

### تشغيل الاختبارات / Run tests
```bash
flutter test
```

### تنسيق الكود / Format code
```bash
flutter format lib/
```

---

## الحصول على المساعدة / Get Help

📖 **الوثائق / Documentation:**
- [README.md](README.md) - دليل شامل
- [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - تعليمات البناء
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - حل المشاكل
- [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - ملخص المشروع

🐛 **الإبلاغ عن مشكلة / Report Issue:**
- افتح issue على GitHub
- Open an issue on GitHub

💬 **المجتمع / Community:**
- [Flutter Discord](https://discord.gg/flutter)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/flutter)

---

## نصيحة الخبراء / Pro Tips

✅ **استخدم جهاز حقيقي** للاختبار بدلاً من المحاكي
   **Use real device** for testing instead of emulator

✅ **نظف المشروع** إذا واجهت مشاكل غريبة
   **Clean project** if you face weird issues

✅ **حدّث Flutter** بانتظام للحصول على آخر التحسينات
   **Update Flutter** regularly for latest improvements

✅ **اقرأ رسائل الخطأ** بعناية - عادة تحتوي على الحل
   **Read error messages** carefully - they usually contain the solution

---

**مدة الإعداد المتوقعة / Expected Setup Time:**
- بيئة جديدة تماماً: 30-60 دقيقة
- Fresh environment: 30-60 minutes

- بيئة Flutter موجودة: 5-10 دقائق  
- Existing Flutter setup: 5-10 minutes

**حجم التحميل / Download Size:**
- Flutter SDK: ~2 GB
- المكتبات / Dependencies: ~200 MB
- APK النهائي / Final APK: ~40 MB

---

🎉 **استمتع بتطوير تطبيق الذكريات!**
   **Enjoy developing the AR Memory App!**
