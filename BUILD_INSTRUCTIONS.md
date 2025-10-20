# تعليمات البناء لتطبيق ذكريات AR

هذا الدليل يوضح كيفية بناء واختبار تطبيق ذكريات AR على أجهزة Android.

## المتطلبات الأساسية

قبل البدء، تأكد من تثبيت:

1. **Flutter SDK 3.22.0 أو أحدث**
   ```bash
   flutter --version
   ```
   
   إذا لم يكن مثبتًا، قم بتحميله من [موقع Flutter الرسمي](https://docs.flutter.dev/get-started/install)

2. **Android Studio** مع Android SDK
   - Android SDK Platform 24 أو أحدث
   - Android Build Tools
   - Android Emulator (اختياري للاختبار)

3. **Java Development Kit (JDK) 17**
   ```bash
   java -version
   ```

4. **Google Maps API Key**
   - احصل على مفتاح API مجاني من [Google Cloud Console](https://console.cloud.google.com/)
   - فعّل Google Maps SDK for Android
   - راجع [دليل الحصول على API Key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

## الإعداد الأولي

### إعداد Google Maps API Key

1. افتح ملف `android/app/src/main/AndroidManifest.xml`
2. ابحث عن السطر:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="YOUR_GOOGLE_MAPS_API_KEY_HERE"/>
   ```
3. استبدل `YOUR_GOOGLE_MAPS_API_KEY_HERE` بمفتاح API الخاص بك

## خطوات البناء

### 1. تثبيت المكتبات

```bash
cd /path/to/ar_memory_app
flutter pub get
```

### 2. التحقق من البيئة

```bash
flutter doctor
```

تأكد من عدم وجود مشاكل في البيئة (يجب أن تظهر علامات ✓ خضراء)

### 3. بناء APK للإصدار

```bash
flutter build apk --release
```

سيتم إنشاء ملف APK في:
```
build/app/outputs/flutter-apk/app-release.apk
```

### 4. بناء APK للتطوير (Debug)

```bash
flutter build apk --debug
```

## اختبار التطبيق

### على جهاز حقيقي

1. قم بتوصيل جهاز Android بالكمبيوتر عبر USB
2. فعّل "خيارات المطور" و "تصحيح USB" على الجهاز
3. شغّل التطبيق:
   ```bash
   flutter run
   ```

### على المحاكي (Emulator)

1. افتح Android Studio
2. شغّل المحاكي من AVD Manager
3. شغّل التطبيق:
   ```bash
   flutter run
   ```

## بناء ملف APK بأحجام مختلفة

### Split APKs (ملفات منفصلة لكل معمارية)

```bash
flutter build apk --split-per-abi
```

سينتج عن هذا:
- `app-armeabi-v7a-release.apk` (للأجهزة القديمة 32-bit)
- `app-arm64-v8a-release.apk` (للأجهزة الحديثة 64-bit)
- `app-x86_64-release.apk` (للمحاكيات)

### App Bundle (للنشر على Google Play)

```bash
flutter build appbundle --release
```

## استكشاف الأخطاء

### خطأ: Gradle Build Failed

```bash
cd android
./gradlew clean
cd ..
flutter clean
flutter pub get
flutter build apk --release
```

### خطأ: SDK not found

تأكد من تعيين متغيرات البيئة:
```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### خطأ: License not accepted

```bash
flutter doctor --android-licenses
```

## التحقق من الأذونات

التطبيق يحتاج الأذونات التالية:
- ✅ CAMERA - للوصول إلى الكاميرا
- ✅ ACCESS_FINE_LOCATION - لتحديد الموقع الدقيق
- ✅ ACCESS_COARSE_LOCATION - لتحديد الموقع التقريبي
- ✅ INTERNET - للاتصال بالإنترنت (للخرائط)

هذه الأذونات معرفة في ملف `android/app/src/main/AndroidManifest.xml`

## البناء التلقائي باستخدام GitHub Actions

تم إعداد GitHub Actions للبناء التلقائي. عند الـ push إلى branch `main` أو `copilot/start-new-ar-memory-app`، سيتم:
1. بناء APK تلقائيًا
2. رفع APK كـ artifact يمكن تحميله

يمكن تحميل APK من صفحة Actions في GitHub.

## تحليل الكود

```bash
flutter analyze
```

## تنسيق الكود

```bash
flutter format lib/
```

## معلومات إضافية

- **Package Name**: `com.example.ar_memory_app`
- **Min SDK Version**: 24 (Android 7.0)
- **Target SDK Version**: 34 (Android 14)

## الدعم

للمزيد من المعلومات:
- [Flutter Documentation](https://docs.flutter.dev/)
- [Android Developer Guide](https://developer.android.com/)
