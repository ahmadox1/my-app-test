#!/bin/bash

# Script to help set up and build the AR Memory App
# استخدام: ./setup.sh

echo "================================"
echo "إعداد تطبيق ذكريات AR"
echo "AR Memory App Setup"
echo "================================"
echo ""

# Check for Flutter
if ! command -v flutter &> /dev/null
then
    echo "❌ Flutter غير مثبت / Flutter is not installed"
    echo "📥 يرجى تثبيت Flutter من: https://docs.flutter.dev/get-started/install"
    echo "   Please install Flutter from: https://docs.flutter.dev/get-started/install"
    exit 1
fi

echo "✅ Flutter مثبت / Flutter is installed"
flutter --version
echo ""

# Check Flutter doctor
echo "🔍 فحص بيئة Flutter / Checking Flutter environment..."
flutter doctor
echo ""

# Check for Google Maps API Key
if grep -q "YOUR_GOOGLE_MAPS_API_KEY_HERE" android/app/src/main/AndroidManifest.xml; then
    echo "⚠️  تحذير: لم يتم إعداد مفتاح Google Maps API"
    echo "   Warning: Google Maps API Key not configured"
    echo ""
    echo "📝 يرجى إضافة مفتاح API في:"
    echo "   Please add your API key in:"
    echo "   android/app/src/main/AndroidManifest.xml"
    echo ""
    echo "   احصل على مفتاح من / Get a key from:"
    echo "   https://console.cloud.google.com/"
    echo ""
    read -p "هل تريد المتابعة؟ / Continue anyway? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]
    then
        exit 1
    fi
fi

# Clean and get dependencies
echo "🧹 تنظيف المشروع / Cleaning project..."
flutter clean

echo "📦 تحميل المكتبات / Getting dependencies..."
flutter pub get

# Analyze code
echo "🔍 تحليل الكود / Analyzing code..."
flutter analyze

# Ask what to do
echo ""
echo "ماذا تريد أن تفعل؟ / What would you like to do?"
echo "1) تشغيل التطبيق (Run app)"
echo "2) بناء APK (Build APK)"
echo "3) بناء App Bundle (Build App Bundle)"
echo "4) إنهاء (Exit)"
echo ""
read -p "اختر رقماً / Choose a number (1-4): " choice

case $choice in
  1)
    echo "🚀 تشغيل التطبيق / Running app..."
    flutter run
    ;;
  2)
    echo "🔨 بناء APK / Building APK..."
    flutter build apk --release
    echo ""
    echo "✅ تم! APK موجود في / Done! APK is at:"
    echo "   build/app/outputs/flutter-apk/app-release.apk"
    ;;
  3)
    echo "🔨 بناء App Bundle / Building App Bundle..."
    flutter build appbundle --release
    echo ""
    echo "✅ تم! App Bundle موجود في / Done! App Bundle is at:"
    echo "   build/app/outputs/bundle/release/app-release.aab"
    ;;
  4)
    echo "👋 وداعاً / Goodbye!"
    exit 0
    ;;
  *)
    echo "❌ اختيار غير صحيح / Invalid choice"
    exit 1
    ;;
esac

echo ""
echo "================================"
echo "✅ تمت العملية بنجاح / Process completed"
echo "================================"
