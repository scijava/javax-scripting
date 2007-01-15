#! /bin/sh

if [ "${BATIK_HOME}" != "" ]; then
java -Djava.security.policy=unsafe.policy -cp ../../../engines/jruby/lib/jruby.jar:../../../engines/jruby/build/jruby-engine.jar:../build/svg-jsr223.jar:${BATIK_HOME}/batik.jar com.sun.svg.script.Main $*
else
  echo "Please set BATIK_HOME before running this script"
fi