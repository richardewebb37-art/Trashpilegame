#!/bin/bash

# Install Debug APK Script for Trash Piles
# Builds and installs the app on connected Android device

echo "📲 Installing Trash Piles on Device"
echo "======================================"

# Navigate to project directory
cd "$(dirname "$0")"

# Check if device is connected
echo ""
echo "🔍 Checking for connected devices..."
adb devices

# Build if APK doesn't exist
if [ ! -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "📦 Building debug APK..."
    ./gradlew assembleDebug
    
    if [ $? -ne 0 ]; then
        echo "❌ Build failed!"
        exit 1
    fi
fi

# Install on device
echo ""
echo "🚀 Installing on device..."
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Check if installation was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Installation successful!"
    echo ""
    echo "🎮 Launch the app from your device home screen"
else
    echo ""
    echo "❌ Installation failed!"
    echo ""
    echo "💡 Tips:"
    echo "   - Make sure USB debugging is enabled"
    echo "   - Make sure device is unlocked"
    echo "   - Check adb devices output above"
    exit 1
fi