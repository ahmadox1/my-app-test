# تطبيق ذكريات AR (AR Memory App)

تطبيق Flutter للواقع المعزز يتيح للمستخدمين حفظ ومشاركة الذكريات في المواقع الجغرافية باستخدام الكاميرا والخرائط.

## المزايا الرئيسية

- 📷 **كاميرا AR**: عرض الذكريات في الواقع المعزز عبر الكاميرا
- 🗺️ **خريطة تفاعلية**: عرض جميع الذكريات على الخريطة (مثل خريطة سناب شات)
- 📍 **حفظ موقع الذكريات**: ربط كل ذكرى بموقع جغرافي محدد
- 🔔 **تنبيهات القرب**: إشعارات عند الاقتراب من ذكريات أخرى
- 💾 **حفظ محلي**: تخزين الذكريات محليًا على الجهاز
- 🌐 **واجهة عربية**: دعم كامل للغة العربية

## المتطلبات

- [Flutter 3.22.0](https://docs.flutter.dev/get-started/install) أو أحدث
- Android SDK 24 أو أحدث
- كاميرا وGPS على الجهاز
- مفتاح Google Maps API (للحصول على مفتاح مجاني، راجع [Google Maps Platform](https://developers.google.com/maps/documentation/android-sdk/get-api-key))

## الإعداد

### 1. إعداد Google Maps API Key

1. احصل على مفتاح API من [Google Cloud Console](https://console.cloud.google.com/)
2. افتح ملف `android/app/src/main/AndroidManifest.xml`
3. استبدل `YOUR_GOOGLE_MAPS_API_KEY_HERE` بمفتاح API الخاص بك

### 2. تثبيت المكتبات والتشغيل

```bash
# تثبيت المكتبات
flutter pub get

# تشغيل التطبيق
flutter run
```

## بناء APK لنظام Android

لبناء ملف APK للتطبيق:

```bash
flutter build apk --release
```

سيتم إنشاء ملف APK في المسار:
```
build/app/outputs/flutter-apk/app-release.apk
```

## بنية المشروع

```
lib/
├── main.dart
└── src/
    ├── models/
    │   └── memory.dart
    ├── services/
    │   ├── location_service.dart
    │   └── memory_storage_service.dart
    └── views/
        ├── camera_ar_page.dart
        └── memory_map_page.dart
```

## الأذونات المطلوبة

- **CAMERA**: للوصول إلى الكاميرا
- **ACCESS_FINE_LOCATION**: لتحديد الموقع الدقيق
- **ACCESS_COARSE_LOCATION**: لتحديد الموقع التقريبي

## كيفية الاستخدام

1. **فتح التطبيق**: يفتح مباشرة على شاشة الكاميرا
2. **كتابة ذكرى**: استخدم حقل النص في الأسفل لكتابة ذكرى
3. **حفظ الذكرى**: اضغط على أيقونة الإرسال لحفظ الذكرى في موقعك الحالي
4. **عرض الخريطة**: اضغط على زر "عرض الخريطة" لرؤية جميع الذكريات
5. **مشاهدة الذكريات القريبة**: تظهر الذكريات القريبة تلقائيًا على شاشة الكاميرا

## التطوير المستقبلي

- [ ] دمج Firebase لمشاركة الذكريات مع المستخدمين الآخرين
- [ ] إضافة دعم للصور والفيديو
- [ ] تحسين واجهة AR
- [ ] إضافة مرشحات وتأثيرات AR

## الترخيص

هذا المشروع متاح تحت رخصة MIT.

---

## المستندات الإضافية

- 📄 [PROJECT_SUMMARY.md](PROJECT_SUMMARY.md) - ملخص شامل للمشروع والميزات المنفذة
- 🔧 [BUILD_INSTRUCTIONS.md](BUILD_INSTRUCTIONS.md) - تعليمات مفصلة لبناء APK
- 🐛 [TROUBLESHOOTING.md](TROUBLESHOOTING.md) - دليل حل المشاكل الشائعة
- 🚀 [setup.sh](setup.sh) - سكريبت للإعداد السريع (Linux/macOS)

