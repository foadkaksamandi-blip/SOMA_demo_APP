#!/usr/bin/env sh
# ------------------------------------------------------------
# Gradle startup script for Unix-based systems
# ------------------------------------------------------------

APP_HOME=$(cd "$(dirname "$0")" && pwd)
DEFAULT_JVM_OPTS="-Xmx64m -Xms64m"
GRADLE_WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$GRADLE_WRAPPER_JAR" ]; then
  echo "Gradle wrapper JAR not found at $GRADLE_WRAPPER_JAR"
  exit 1
fi

exec java $DEFAULT_JVM_OPTS -jar "$GRADLE_WRAPPER_JAR" "$@"
