#!/bin/sh
#
# Copyright © 2015-2021 the original authors.
# Gradle start up script for POSIX compatible shells.

APP_NAME="Gradle"
APP_BASE_NAME=$(basename "$0")
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)

DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

die() {
    echo
    echo "ERROR: $*"
    echo
    exit 1
}

# OS specific support
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  Darwin*  ) darwin=true ;;
  MSYS* | MINGW* ) msys=true ;;
  NONSTOP* ) nonstop=true ;;
esac

CLASSPATH=$(JAVACP=""; find "$APP_HOME/gradle/wrapper" -name "gradle-wrapper.jar" -exec echo -n ":$APP_HOME/gradle/wrapper/gradle-wrapper.jar" \;)

if [ -z "$JAVA_HOME" ] ; then
  if [ -x "/usr/libexec/java_home" ] ; then
    JAVA_HOME=$(/usr/libexec/java_home) ; export JAVA_HOME
  fi
fi

JAVACMD="java"
if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
fi

eval exec \
  '"$JAVACMD"' \
  $DEFAULT_JVM_OPTS \
  $JAVA_OPTS \
  $GRADLE_OPTS \
  '-classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"' \
  org.gradle.wrapper.GradleWrapperMain \
  '"$@"'
