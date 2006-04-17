#!/bin/sh

CLASSPATH=../lib/forehead-1.0-beta-5.jar

# This is ugly. I am using jrunscript main class name. 
# I have to include too many jar files in classpath :-(

$JAVA_HOME/bin/java -classpath ${CLASSPATH} \
  -Dforehead.conf.file=./forehead.conf \
  -Dtools.jar=${JAVA_HOME}/lib/tools.jar \
  com.werken.forehead.Forehead -l jelly "$@"

