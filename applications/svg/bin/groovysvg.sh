#! /bin/sh

if [ "${BATIK_HOME}" != "" ]; then
java -Djava.security.policy=unsafe.policy -cp ../../../engines/groovy/lib/groovy-all-1.0.jar:../../../engines/groovy/build/groovy-engine.jar:../build/svg-jsr223.jar:${BATIK_HOME}/batik.jar com.sun.svg.script.Main $*
else
  echo "Please set BATIK_HOME before running this script"
fi
