@echo off

if "%BATIK_HOME%" == "" goto noBatikHome
java -Djava.security.policy=unsafe.policy -cp ../../../engines/groovy/lib/groovy-all-1.0.jar;../../../engines/groovy/build/groovy-engine.jar;../build/svg-jsr223.jar;%BATIK_HOME%/batik.jar com.sun.svg.script.Main %*
goto end   
:noBatikHome
  echo Please set BATIK_HOME before running this script
:end

