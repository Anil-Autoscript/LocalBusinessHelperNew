#!/bin/bash
# ============================================================
# setup_wrapper.sh
# Run this once after cloning to initialize Gradle wrapper
# Requires: Java 17+ installed
# ============================================================

set -e

echo "Setting up Gradle wrapper..."

# Check Java is available
if ! command -v java &> /dev/null; then
  echo "❌ Java not found. Please install JDK 17:"
  echo "   https://adoptium.net/"
  exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1 | sed 's/.*version "\([0-9]*\).*/\1/')
echo "Java version: $JAVA_VERSION"

# Check gradle CLI if available
if command -v gradle &> /dev/null; then
  gradle wrapper --gradle-version=8.4
  echo "✅ Gradle wrapper generated via gradle CLI"
elif command -v ./gradlew &> /dev/null && [ -f gradle/wrapper/gradle-wrapper.jar ]; then
  echo "✅ Gradle wrapper already set up"
else
  # Download wrapper jar from Maven Central distribution
  GRADLE_VERSION="8.4"
  JAR_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-wrapper.jar"
  echo "Downloading gradle-wrapper.jar from Gradle.org..."
  curl -L -o gradle/wrapper/gradle-wrapper.jar "$JAR_URL" || \
  wget -O gradle/wrapper/gradle-wrapper.jar "$JAR_URL"
  echo "✅ gradle-wrapper.jar downloaded"
fi

chmod +x gradlew
echo "✅ Setup complete! Run: ./gradlew assembleDebug"
