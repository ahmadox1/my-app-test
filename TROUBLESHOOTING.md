# دليل حل المشاكل الشائعة
# Troubleshooting Common Issues

## مشاكل البناء / Build Issues

### 1. خطأ: Google Maps API Key غير موجود
**Error: Google Maps API Key not found**

**الأعراض:**
- الخريطة لا تظهر
- شاشة رمادية بدلاً من الخريطة
- رسالة خطأ في logcat

**الحل:**
1. احصل على API Key من [Google Cloud Console](https://console.cloud.google.com/)
2. افتح `android/app/src/main/AndroidManifest.xml`
3. استبدل `YOUR_GOOGLE_MAPS_API_KEY_HERE` بمفتاحك:
   ```xml
   <meta-data
       android:name="com.google.android.geo.API_KEY"
       android:value="AIzaSy...your_actual_key"/>
   ```

### 2. خطأ: Gradle Build Failed
**Error: Gradle Build Failed**

**الأعراض:**
```
FAILURE: Build failed with an exception.
```

**الحل:**
```bash
cd android
./gradlew clean
cd ..
flutter clean
flutter pub get
flutter build apk --release
```

### 3. خطأ: Android Licenses Not Accepted
**Error: Android Licenses Not Accepted**

**الأعراض:**
```
Android license status unknown.
```

**الحل:**
```bash
flutter doctor --android-licenses
```
ثم اضغط 'y' لقبول جميع الرخص.

### 4. خطأ: MinSdkVersion مختلف
**Error: MinSdkVersion conflict**

**الأعراض:**
```
Manifest merger failed : uses-sdk:minSdkVersion 21 cannot be smaller than version 24
```

**الحل:**
تأكد من أن `android/app/build.gradle` يحتوي على:
```gradle
defaultConfig {
    minSdk = 24  // أو أعلى
}
```

### 5. خطأ: Flutter SDK Not Found
**Error: Flutter SDK Not Found**

**الأعراض:**
```
flutter: command not found
```

**الحل:**
أضف Flutter إلى PATH:
```bash
export PATH="$PATH:/path/to/flutter/bin"
```

أو أضفه بشكل دائم في `~/.bashrc` أو `~/.zshrc`:
```bash
echo 'export PATH="$PATH:/path/to/flutter/bin"' >> ~/.bashrc
source ~/.bashrc
```

## مشاكل وقت التشغيل / Runtime Issues

### 6. الكاميرا لا تعمل
**Camera not working**

**الأسباب المحتملة:**
1. **لم يتم منح الإذن:**
   - اذهب إلى إعدادات التطبيق
   - فعّل إذن الكاميرا

2. **الكاميرا مستخدمة من تطبيق آخر:**
   - أغلق جميع التطبيقات الأخرى
   - أعد تشغيل الجهاز

3. **نسخة Android قديمة:**
   - تأكد من أن Android 7.0 أو أحدث

### 7. الموقع لا يعمل
**Location not working**

**الحلول:**
1. **تفعيل GPS:**
   - اذهب إلى الإعدادات
   - فعّل "الموقع" أو "GPS"

2. **منح الأذونات:**
   - إعدادات التطبيق
   - فعّل "الموقع"

3. **استخدم الجهاز الحقيقي:**
   - المحاكي قد لا يدعم GPS بشكل صحيح
   - استخدم جهاز Android حقيقي للاختبار

### 8. الخريطة لا تظهر
**Map not showing**

**الحلول:**
1. **تحقق من API Key** (راجع #1)
2. **تحقق من الاتصال بالإنترنت**
3. **تحقق من Google Maps SDK:**
   - تأكد من تفعيله في Google Cloud Console
   - تحقق من حصة الاستخدام المجانية

### 9. الذكريات لا تُحفظ
**Memories not saving**

**الأسباب المحتملة:**
1. **لم يتم تحديد الموقع:**
   - انتظر حتى يتم تحديد الموقع أولاً
   - تأكد من أن GPS يعمل

2. **نص فارغ:**
   - تأكد من كتابة نص الذكرى

3. **مشكلة في التخزين:**
   - تحقق من مساحة التخزين المتوفرة
   - امنح أذونات التخزين إذا طُلب ذلك

## مشاكل الأداء / Performance Issues

### 10. التطبيق بطيء
**App is slow**

**الحلول:**
1. **أغلق التطبيقات الأخرى**
2. **امسح ذاكرة cache:**
   ```bash
   flutter clean
   flutter pub get
   flutter build apk --release
   ```
3. **استخدم نسخة Release بدلاً من Debug:**
   ```bash
   flutter run --release
   ```

### 11. الكاميرا متقطعة
**Camera is laggy**

**الحلول:**
1. **خفض دقة الكاميرا:**
   في `camera_ar_page.dart`، غيّر:
   ```dart
   ResolutionPreset.high  // إلى
   ResolutionPreset.medium
   ```

2. **أغلق التطبيقات الأخرى**

3. **استخدم جهاز أحدث:**
   - الأجهزة القديمة قد تكافح مع AR

## مشاكل GitHub Actions

### 12. فشل البناء التلقائي
**Automated build failing**

**التحقق من السجلات:**
1. اذهب إلى GitHub → Actions
2. افتح run الفاشل
3. اقرأ error logs

**الحلول الشائعة:**
1. **تحقق من تحديثات المكتبات:**
   - قد تكون بعض المكتبات غير متوافقة
   - حدّث pubspec.yaml

2. **تحقق من إعدادات Java:**
   - تأكد من استخدام JDK 17

3. **تحقق من Flutter version:**
   - تأكد من استخدام Flutter 3.22.0 أو أحدث

## الحصول على المساعدة / Getting Help

إذا لم تحل المشكلة:

1. **تحقق من السجلات:**
   ```bash
   flutter run -v  # للسجلات المفصلة
   ```

2. **افتح issue على GitHub:**
   - قدم وصفاً مفصلاً للمشكلة
   - أضف السجلات ولقطات الشاشة
   - اذكر نسخة Android والجهاز

3. **موارد مفيدة:**
   - [Flutter Documentation](https://docs.flutter.dev/)
   - [Stack Overflow](https://stackoverflow.com/questions/tagged/flutter)
   - [Flutter Community](https://flutter.dev/community)

## نصائح عامة / General Tips

✅ **استخدم أحدث نسخة من Flutter**
✅ **جرب على جهاز حقيقي بدلاً من المحاكي**
✅ **تحقق من الأذونات دائماً**
✅ **اقرأ رسائل الخطأ بعناية**
✅ **نظف المشروع عند حدوث مشاكل غريبة**

---

*آخر تحديث: 2024*
