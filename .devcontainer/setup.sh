#!/usr/bin/env bash
set -euo pipefail

# Ensure Android CLI tools are on PATH
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

echo "Accepting Android SDK licenses..."
yes | sdkmanager --licenses >/dev/null

echo "Installing Android SDK components (platform-tools, platform 34, build-tools 34.0.0)..."
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null

# Install Gradle via SDKMAN (to generate wrapper)
echo "Installing SDKMAN + Gradle 8.2.1..."
curl -s "https://get.sdkman.io" | bash
# shellcheck disable=SC1091
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install gradle 8.2.1

# Generate Gradle wrapper if not present
if [ ! -f "./gradlew" ]; then
  echo "Generating Gradle wrapper..."
  gradle wrapper --gradle-version 8.2.1
fi

echo "Done. You can now run: ./gradlew assembleDebug"
