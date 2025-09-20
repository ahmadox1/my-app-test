# Smart Coach - مساعد الألعاب الذكي 🎮🤖

[![Android CI](https://github.com/ahmadox1/my-app-test/actions/workflows/android.yml/badge.svg)](https://github.com/ahmadox1/my-app-test/actions/workflows/android.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)

مساعد ذكي للألعاب يستخدم الذكاء الاصطناعي المحلي لتحليل الألعاب وتقديم اقتراحات استراتيجية في الوقت الفعلي.

## ✨ المميزات

### 🔍 تحليل ذكي للألعاب
- **خدمة الوصول**: مراقبة التطبيق الأمامي وقراءة عناصر الشاشة
- **التقاط الشاشة**: استخدام MediaProjection لالتقاط محتوى اللعبة
- **فقاعة تفاعلية**: عرض الاقتراحات عبر واجهة قابلة للسحب فوق الألعاب

### 🧠 محرك رؤية محلي
- **ML Kit Text Recognition**: استخراج النصوص والعناصر الأساسية
- **تحليل الألوان والأنماط**: كشف أحداث اللعبة باستخدام OpenCV-Android
- **حالة اللعبة المنظمة**: تحويل المخرجات إلى `GameState` منظم

### ⚡ محرك الاستراتيجيات
- **قواعد واضحة ومفسرة**: اقتراحات "هجوم/دفاع/انتظار" بناء على قواعد محددة
- **تحليل متقدم**: مراعاة مستوى الإكسير ونوعية البطاقات ووضع الوحدات
- **ثقة متغيرة**: تقييم مستوى الثقة في كل اقتراح

### 🤖 ذكاء اصطناعي محلي
- **نموذج مفتوح المصدر**: دمج llama.cpp لتشغيل نماذج GGUF محلياً
- **تحميل تلقائي**: تنزيل النموذج عند أول تشغيل مع التحقق من checksum
- **خيارات الجودة**: اختيار بين جودة عالية/متوسطة/منخفضة حسب إمكانيات الجهاز
- **تكوين مرن**: إعدادات قابلة للتحديث عبر `remote_config.json`

## 🛡️ الخصوصية والأمان

- ✅ **معالجة محلية بالكامل**: جميع البيانات تُعالج على جهازك
- ✅ **لا توجد بيانات خارجية**: عدم إرسال أي معلومات للإنترنت
- ✅ **موافقة المستخدم**: كل شيء اختياري ويتطلب موافقة صريحة
- ✅ **إيقاف فوري**: إمكانية إيقاف الخدمة من الإشعار مباشرة
- ✅ **شفافية كاملة**: كود مفتوح المصدر وقابل للمراجعة

## 📱 التشغيل

### متطلبات النظام
- Android 7.0 (API level 24) أو أحدث
- 2GB RAM أو أكثر (4GB مُنصح به للنماذج عالية الجودة)
- مساحة تخزين: 500MB - 8GB (حسب جودة النموذج المختارة)

### الصلاحيات المطلوبة
1. **BIND_ACCESSIBILITY_SERVICE**: قراءة محتوى الألعاب
2. **SYSTEM_ALERT_WINDOW**: عرض الفقاعة التفاعلية
3. **POST_NOTIFICATIONS**: إشعارات الاقتراحات
4. **إذن التقاط الشاشة**: تحليل محتوى اللعبة (يُطلب عند الحاجة)

### التثبيت

#### من Releases (مُنصح به)
1. اذهب إلى صفحة [Releases](https://github.com/ahmadox1/my-app-test/releases)
2. حمّل أحدث ملف `app-debug.apk`
3. فعّل "مصادر غير معروفة" في إعدادات الجهاز
4. ثبّت التطبيق

#### من GitHub Actions
1. اذهب إلى [Actions](https://github.com/ahmadox1/my-app-test/actions)
2. اختر أحدث build ناجح
3. حمّل `smart-coach-debug-apk` من Artifacts

### الإعداد الأولي
1. **اقبل إشعار الخصوصية**: اقرأ واقبل شروط الاستخدام
2. **امنح الصلاحيات**: 
   - الذهاب للإعدادات → خدمات الوصول → تفعيل Smart Coach
   - السماح بالعرض فوق التطبيقات
3. **حمّل النماذج**: اختر جودة النموذج وانتظر التحميل
4. **ابدأ الخدمة**: اضغط "بدء الخدمة" واختر اللعبة المدعومة

## 🎮 الألعاب المدعومة

### Clash Royale 👑
- تحليل مستوى الإكسير للاعبين
- كشف البطاقات في اليد
- مراقبة الوحدات على الساحة
- اقتراحات الهجوم والدفاع بناء على الوضع الحالي

### Clash of Clans 🏰
- تحليل الموارد المتاحة
- كشف وضع القرية والجيش
- اقتراحات الهجوم حسب نوع القاعدة المستهدفة

## 🏗️ هندسة المشروع

```
SmartCoach/
├── app/                          # التطبيق الرئيسي
├── core-vision/                  # وحدة الرؤية والتحليل البصري
├── core-llm/                     # وحدة الذكاء الاصطناعي المحلي
├── core-strategy/                # محرك الاستراتيجيات
├── core-permissions/             # إدارة الصلاحيات
├── sample_screens/               # شاشات اختبارية
└── remote_config.json           # تكوين النماذج عن بُعد
```

### تقنيات مستخدمة
- **Kotlin + Jetpack Compose**: واجهة المستخدم العصرية
- **Hilt**: Dependency Injection
- **ML Kit**: تحليل النصوص والصور
- **OpenCV-Android**: معالجة الصور المتقدمة
- **OkHttp**: تحميل النماذج
- **Coroutines**: المعالجة غير المتزامنة

## 🔧 البناء والتطوير

### البناء المحلي
```bash
git clone https://github.com/ahmadox1/my-app-test.git
cd my-app-test
./gradlew assembleDebug
```

### تشغيل الاختبارات
```bash
# اختبار محرك الاستراتيجيات
./gradlew :core-strategy:test

# اختبار وحدة الرؤية  
./gradlew :core-vision:test

# جميع الاختبارات
./gradlew test
```

### إضافة توقيع للإنتاج

1. **إنشاء keystore**:
```bash
keytool -genkey -v -keystore smart-coach-key.keystore -alias smart_coach -keyalg RSA -keysize 2048 -validity 10000
```

2. **إعداد gradle.properties**:
```properties
SMART_COACH_STORE_FILE=../smart-coach-key.keystore
SMART_COACH_STORE_PASSWORD=your_store_password
SMART_COACH_KEY_ALIAS=smart_coach
SMART_COACH_KEY_PASSWORD=your_key_password
```

3. **تحديث build.gradle للتطبيق**:
```gradle
android {
    signingConfigs {
        release {
            storeFile file(SMART_COACH_STORE_FILE)
            storePassword SMART_COACH_STORE_PASSWORD
            keyAlias SMART_COACH_KEY_ALIAS
            keyPassword SMART_COACH_KEY_PASSWORD
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            // ... باقي إعدادات Release
        }
    }
}
```

4. **بناء النسخة الموقعة**:
```bash
./gradlew assembleRelease
```

## 🤝 المساهمة

نرحب بالمساهمات! يمكنك المساعدة في:

- **تحسين دقة التحليل البصري**: إضافة حالات جديدة لكشف العناصر
- **توسيع الألعاب المدعومة**: إضافة ألعاب جديدة
- **تطوير الاستراتيجيات**: تحسين قواعد محرك الاستراتيجيات
- **تحسين الواجهة**: تطوير تجربة المستخدم
- **الترجمة**: إضافة لغات جديدة

### خطوات المساهمة
1. Fork المشروع
2. أنشئ branch جديد (`git checkout -b feature/amazing-feature`)
3. اعمل commit لتغييراتك (`git commit -m 'Add amazing feature'`)
4. ادفع للـ branch (`git push origin feature/amazing-feature`)
5. افتح Pull Request

## 📊 خارطة الطريق

- [ ] **الإصدار 1.1**: 
  - دعم ألعاب إضافية
  - تحسين دقة التحليل البصري
  - واجهة إعدادات متقدمة

- [ ] **الإصدار 1.2**:
  - نماذج AI محلية أصغر وأسرع  
  - تحليل تاريخي للمباريات
  - إحصائيات تفصيلية للأداء

- [ ] **الإصدار 2.0**:
  - دعم الألعاب ثلاثية الأبعاد
  - تحليل متقدم بالتعلم العميق
  - ميزات التعلم التكيفي

## 🐛 الإبلاغ عن المشاكل

إذا واجهت أي مشكلة، يرجى:
1. البحث في [Issues الموجودة](https://github.com/ahmadox1/my-app-test/issues)
2. إنشاء issue جديد مع:
   - وصف مفصل للمشكلة
   - خطوات إعادة الإنتاج
   - لقطات شاشة (إن أمكن)
   - معلومات الجهاز ونسخة Android

## 📄 الترخيص

هذا المشروع مُرخص تحت [MIT License](LICENSE) - راجع ملف LICENSE للتفاصيل.

## ⭐ دعم المشروع

إذا أعجبك المشروع:
- ⭐ اعط نجمة للمشروع
- 🐛 أبلغ عن المشاكل
- 💡 اقترح ميزات جديدة
- 🤝 ساهم في التطوير
- 📢 شارك المشروع مع الآخرين

---

**ملاحظة**: هذا المشروع تعليمي ولأغراض البحث. يرجى الالتزام بشروط خدمة الألعاب المدعومة واللعب النزيه.