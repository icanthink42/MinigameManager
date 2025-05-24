#!/bin/bash

# Exit on any error
set -e

echo "Building MinigameManager..."

# Clean and build the plugin
./gradlew clean build

# Check if build was successful
if [ $? -eq 0 ]; then
    echo "Build successful! Plugin JAR created at: build/libs/MinigameManager-1.0-SNAPSHOT.jar"

    # Update symlink to server plugins directory
    ln -sf "$(pwd)/build/libs/MinigameManager-1.0-SNAPSHOT.jar" "/home/jack/programming/plugin_mc_server/plugins/MinigameManager-1.0-SNAPSHOT.jar"
    echo "Symlink updated in server plugins directory"
else
    echo "Build failed!"
    exit 1
fi