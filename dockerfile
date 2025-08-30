# Base Ubuntu devcontainer
FROM mcr.microsoft.com/devcontainers/base:ubuntu-22.04

# Install basics + JDK 17 + tools
USER root
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
      wget curl unzip zip ca-certificates git openssh-client \
      openjdk-17-jdk && \
    rm -rf /var/lib/apt/lists/*

# Android SDK (command line tools)
ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O /tmp/clt.zip && \
    unzip -q /tmp/clt.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/clt.zip

# Precreate licenses dir so we can accept licenses later
RUN mkdir -p ${ANDROID_HOME}/licenses
