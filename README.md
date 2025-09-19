# Smart Gaming Assistant 🎮

مساعد الألعاب الذكي - تطبيق Android لتحليل الألعاب وتقديم النصائح الذكية

## 📱 About / حول المشروع

Smart Gaming Assistant is an Android application that provides intelligent gaming tips and analysis. The app uses simplified implementations to ensure reliable building without external dependencies.

مساعد الألعاب الذكي هو تطبيق أندرويد يوفر نصائح ذكية وتحليل للألعاب. يستخدم التطبيق تطبيقات مبسطة لضمان البناء بشكل موثوق بدون تبعيات خارجية.

## 🚀 Quick Start / بداية سريعة

### Prerequisites / المتطلبات
- Java 17+
- Android SDK (API 24+)
- Gradle (handled by wrapper)

### Building Locally / البناء محليًا

**Method 1: Using the build script (Recommended)**
```bash
# Auto-detect best build method
./build-local.sh

# Or specify build method
./build-local.sh gradle    # Force Gradle build
./build-local.sh custom    # Force custom script build
./build-local.sh clean     # Clean artifacts
./build-local.sh check     # Check environment
```

**Method 2: Direct Gradle build**
```bash
./gradlew assembleDebug
```

**Method 3: Custom build script**
```bash
./build_apk.sh
```

### GitHub Actions / إجراءات GitHub

The project includes a comprehensive CI/CD pipeline that:
- ✅ Builds APK using multiple methods (Gradle + fallback)
- ✅ Creates releases automatically on main branch pushes
- ✅ Uploads build artifacts
- ✅ Validates builds and provides detailed reports

يتضمن المشروع خط إنتاج CI/CD شامل يقوم بـ:
- ✅ بناء APK باستخدام طرق متعددة
- ✅ إنشاء إصدارات تلقائيًا عند الدفع للفرع الرئيسي
- ✅ رفع ملفات البناء
- ✅ التحقق من صحة البناء وتوفير تقارير مفصلة

## 📦 APK Installation / تثبيت APK

### Download / التحميل
- **Latest Release**: Check [Releases](../../releases) page
- **Development Builds**: Available in [Actions](../../actions) artifacts

### Installation Steps / خطوات التثبيت
1. Enable "Unknown Sources" in Android settings / فعّل "مصادر غير معروفة" في إعدادات أندرويد
2. Download the APK file / حمّل ملف APK
3. Install using one of these methods / ثبّت باستخدام إحدى هذه الطرق:
   ```bash
   # Via ADB
   adb install app-debug.apk
   
   # Or tap the APK file on your device
   # أو انقر على ملف APK في جهازك
   ```

## 🛠️ Project Structure / هيكل المشروع

```
my-app-test/
├── .github/workflows/          # GitHub Actions CI/CD
│   └── build-apk.yml          # Main build workflow
├── app/                       # Android app module
│   ├── src/main/
│   │   ├── java/              # Java source files
│   │   ├── res/               # Android resources
│   │   └── AndroidManifest.xml
│   └── build.gradle           # App-level build config
├── gradle/wrapper/            # Gradle wrapper files
├── build.gradle               # Project-level build config
├── settings.gradle            # Gradle settings
├── gradle.properties          # Gradle properties
├── build-local.sh            # Local development build script
├── build_apk.sh              # Custom APK build script
└── README.md                 # This file
```

## 🔧 Build Methods / طرق البناء

### 1. Gradle Build (Primary)
- Standard Android Gradle Plugin build
- Handles dependencies automatically
- Generates optimized APK
- Full Android toolchain integration

### 2. Custom Script Build (Fallback)
- Uses AAPT2 and build tools directly  
- Works without Gradle in CI environments
- Simplified dependency handling
- Offline-capable build process

### 3. Local Development Script
- Intelligent build method detection
- Environment validation
- Colored output and progress reporting
- Multiple build options

## 🎯 Features / المميزات

### Current Implementation / التطبيق الحالي
- ✅ Basic UI with Arabic support / واجهة أساسية بدعم العربية
- ✅ Service management buttons / أزرار إدارة الخدمة
- ✅ System status display / عرض حالة النظام
- ✅ Clean material design / تصميم ماتيريال نظيف

### Simplified Components / المكونات المبسطة
- `ModelDownloadManager`: Mock implementation / تطبيق وهمي
- `TensorFlowManager`: Simplified AI logic / منطق ذكي مبسط
- `OpenCVManager`: Basic image processing / معالجة صور أساسية

## 🏗️ Development / التطوير

### Local Development Setup / إعداد التطوير المحلي
```bash
# Clone repository
git clone https://github.com/ahmadox1/my-app-test.git
cd my-app-test

# Check environment
./build-local.sh check

# Build project
./build-local.sh

# Clean build
./build-local.sh clean
```

### Adding Features / إضافة مميزات
1. Modify source files in `app/src/main/java/`
2. Update resources in `app/src/main/res/`  
3. Test builds locally with `./build-local.sh`
4. Commit and push to trigger CI/CD

### Testing / الاختبار
```bash
# Run unit tests
./gradlew test

# Run instrumentation tests
./gradlew connectedAndroidTest
```

## 📊 Build Status / حالة البناء

[![Android APK Build & Release](../../actions/workflows/build-apk.yml/badge.svg)](../../actions/workflows/build-apk.yml)

## 🤝 Contributing / المساهمة

1. Fork the repository / انسخ المشروع
2. Create feature branch / أنشئ فرع مميزة
3. Make changes / اعمل تغييرات
4. Test locally / اختبر محليًا
5. Submit pull request / أرسل طلب دمج

## 📄 License / الترخيص

This project is open source. Please check the license file for details.

هذا المشروع مفتوح المصدر. يرجى مراجعة ملف الترخيص للتفاصيل.

---

**Build Info:**
- Min SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)  
- Java Version: 17
- Gradle Version: 8.4
- Android Gradle Plugin: 8.1.4

**معلومات البناء:**
- أقل إصدار: 24 (أندرويد 7.0)
- الإصدار المستهدف: 34 (أندرويد 14)
- إصدار جافا: 17
- إصدار جرادل: 8.4
- إضافة أندرويد جرادل: 8.1.4