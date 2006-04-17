@echo off

if "%JAVA_HOME%"=="" goto err
goto run

:err
echo JAVA_HOME must be specified
goto end

:run

REM This is ugly. I am using jrunscript main class name. 
REM I have to include too many jar files in classpath :-(

"%JAVA_HOME%"\bin\java -classpath "..\lib\forehead-1.0-beta-5.jar" "-Dforehead.conf.file=forehead.conf" "-Dtools.jar=%JAVA_HOME%\lib\tools.jar"  com.werken.forehead.Forehead -l jelly  %* 

:end
