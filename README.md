# ScreenTalk

تطبيق أندرويد يعتمد على Kotlin و Jetpack Compose لتقديم مساعد ذكي يعمل بالكامل على الجهاز. يوفر فقاعة عائمة تلتقط الشاشة، تحلل النص وعناصر الواجهة، ثم تجيب على أسئلة المستخدم صوتيًا ونصيًا بدون أي اتصال خارجي.

## Features / المميزات
- 🫧 Floating chat head overlay with draggable bubble and Compose chat panel.
- 📸 Foreground MediaProjection service throttled for battery-friendly screen snapshots.
- 🔤 On-device OCR using ML Kit مع خيار Tesseract للغة العربية.
- ♿️ Optional accessibility service to قراءة عناصر الواجهة الحالية.
- 🧠 Local LLM abstraction with llama.cpp bridge + Echo fallback for offline responses.
- 🎙 Offline Vosk speech-to-text (Arabic + English) and built-in TextToSpeech playback.
- 📥 Resumable downloads for LLM, STT, and tessdata models using WorkManager.
- 🔐 Privacy-first defaults: المعالجة داخل الجهاز، بدون رفع بيانات أو حفظ لقطات شاشة إلا باختيار المستخدم.

## Project structure
```
app/        – Main Compose UI, permissions flow, navigation.
overlay/    – SYSTEM_ALERT_WINDOW chat head + Compose chat panel.
screen/     – MediaProjection capture, OCR engines, accessibility service.
core-ml/    – LLM/STT/TTS abstractions, JNI bridge for llama.cpp.
common/     – Shared utilities, download helpers, logging.
data/       – JSON catalogs for LLM/STT/Tesseract downloads.
assets/     – (empty) reserved for future bundled assets.
```

## Build requirements
- Android Studio Ladybug (AGP 8.7+) أو أحدث.
- Android SDK Platform 35 مع أدوات البناء الأحدث.
- تمكين دعم Kotlin 2.0.21 و Java 17 toolchain.
- NDK r26+ إذا رغبت ببناء مكتبة `llama_bridge` (الاعتماد على CMake موجود).

## Getting started
1. افتح المشروع في Android Studio ودع Gradle يقوم بالمزامنة.
2. أول تشغيل سيبني وحدة `core-ml` ويجهّز ربط JNI. يمكنك بناء Debug APK من `app` مباشرة.
3. ثبّت التطبيق على جهاز فعلي (يتطلب MediaProjection و SYSTEM_ALERT_WINDOW).
4. من التطبيق:
   - امنح صلاحية الفقاعة العائمة (overlay).
   - فعّل التقاط الشاشة عند الطلب، ثم سهّل تمكين Accessibility (اختياري).
   - حمّل نموذج الذكاء الاصطناعي الافتراضي (TinyLlama Q4) عبر شاشة الإعدادات > Downloader.
   - حمّل نموذج Vosk (ar/en) وملف tessdata العربي عند الحاجة.

## Permissions
- `SYSTEM_ALERT_WINDOW` لعرض الفقاعة العائمة فوق التطبيقات.
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MEDIA_PROJECTION` للتقاط الشاشة في الخلفية.
- `RECORD_AUDIO` لسماع أسئلة المستخدم بالصوت.
- `POST_NOTIFICATIONS` لإظهار حالة تنزيل النماذج والخدمات الدائمة.
- `WAKE_LOCK` للحفاظ على المعالجة أثناء الجلسات الطويلة.
- `BIND_ACCESSIBILITY_SERVICE` (اختياري) لقراءة بنية واجهة المستخدم.

## Usage flow
1. Start the chat bubble from the home screen. إذا لم تكن صلاحية overlay ممنوحة سيتم توجيهك إلى الإعدادات المناسبة.
2. ابدأ التقاط الشاشة. سيطلب النظام موافقة MediaProjection، ثم يبدأ استخراج النص كل ~1.5 ثانية.
3. اضغط على الفقاعة لفتح لوحة الدردشة. يمكنك الكتابة أو الضغط على زر الميكروفون لتحويل صوتك إلى نص محليًا.
4. سيُبنَى سياق الشاشة (OCR + Accessibility) ويُمرَّر إلى الـ LLM ليولّد إجابة في الزمن الحقيقي، مع خيار نطقها عبر TTS.
5. يمكن إيقاف التقاط الشاشة أو إغلاق الفقاعة في أي وقت، وكل المعالجة تبقى على الجهاز.

## Troubleshooting
- **النموذج بطيء؟** استخدم نموذج TinyLlama الصغير، وقلّل فاصل الالتقاط من شاشة الإعدادات.
- **الـ OCR لا يلتقط العربية جيدًا؟** فعّل Tesseract وحمّل ملف `ara.traineddata`.
- **استهلاك البطارية مرتفع؟** زِد الفاصل الزمني للالتقاط أو عطّل خدمة Accessibility.
- **لا يعمل الصوت؟** تأكد من منح صلاحية الميكروفون وتشغيل نموذج Vosk قبل بدء التسجيل.

## Offline & privacy
- جميع عمليات OCR/STT/LLM تتم محليًا بعد تنزيل النماذج لأول مرة.
- لا يتم حفظ لقطات الشاشة بشكل افتراضي، ويتم مسح البتات من الذاكرة بعد المعالجة.
- لا يتم إرسال أي بيانات إلى خوادم خارجية.

## Licenses
- [llama.cpp](https://github.com/ggerganov/llama.cpp) — MIT License.
- [Vosk](https://github.com/alphacep/vosk-api) — Apache 2.0 License.
- [ML Kit Text Recognition](https://developers.google.com/ml-kit) — Google Play Services Terms.
- [tess-two](https://github.com/adaptech-cz/Tesseract4Android) — Apache 2.0 License.

> ملاحظة: قيم SHA256 في ملفات `data/*.json`:
> - `tessdata.json`: تم التحقق من القيم وهي صحيحة
> - `models.json` و `stt_models.json`: تحتاج للتحديث بالقيم الفعلية قبل الإصدار
