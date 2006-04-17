@if "%DEBUG%" == "" @echo off
if "%PNUTS_JAVA_COMMAND%" == "" goto implicit_java_command

set JAVA_EXE=%PNUTS_JAVA_COMMAND%
goto next

:implicit_java_command
if "%JAVA_HOME%" == ""  goto no_JAVA_HOME
set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
goto next

:no_JAVA_HOME
set JAVA_EXE=java.exe

:next

if "%PNUTS_HOME%" == "" goto implicit_home
set TOP="%PNUTS_HOME%"
goto module

:implicit_home
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.\
set TOP="%DIRNAME%/.."

:module
if "%PNUTS_MODULE%" == "" goto implicit_module
set MODULE=%PNUTS_MODULE%
goto invocation

:implicit_module
set MODULE=pnuts.tools

set ARGS="-Xbootclasspath/a:%TOP%/lib/pnuts.jar" "-Djava.ext.dirs=%TOP%/modules" "-Dpnuts.home=%TOP%" pnuts.tools.Main -m %MODULE% %1 %2 %3 %4 %5 %6 %7 %8 %9

if "%HTTP_PROXY_HOST%" == "" goto invocation
if "%HTTP_PROXY_PORT%" == "" goto invocation
set ARGS=-Dhttp.proxyHost=%HTTP_PROXY_HOST% -Dhttp.proxyPort=%HTTP_PROXY_PORT% %ARGS% 

:invocation

%JAVA_EXE% %ARGS%

:end

set JAVA_EXE=
set DIRNAME=
set MODULE=
set TOP=
