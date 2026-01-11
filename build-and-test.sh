#!/bin/bash

# Build and Test Script for Trash Piles
# Runs tests then builds debug APK

echo "🧪 Building and Testing Trash Piles"
echo "======================================"

# Navigate to project directory
cd "$(dirname "$0")"

# Run unit tests
echo ""
echo "🔬 Running unit tests..."
./gradlew test --info

# Check if tests passed
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ All tests passed!"
else
    echo ""
    echo "❌ Tests failed!"
    exit 1
fi

# Build debug APK
echo ""
echo "📦 Building debug APK..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build successful!"
    echo ""
    echo "📱 APK Location:"
    ls -lh app/build/outputs/apk/debug/app-debug.apk
    echo ""
    echo "🚀 To install on device:"
    echo "   adb install -r app/build/outputs/apk/debug/app-debug.apk"
else
    echo ""
    echo "❌ Build failed!"
    exit 1
fi