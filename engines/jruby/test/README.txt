This directory contains examples using jruby engine with scripting API.

When you want to get these examples run, try steps below.

step1. Install jruby-engine.jar in your local maven repository.

   mvn install:install-file -Dfile=../lib/jruby-engine.jar -DgroupId=com.sun.script.jruby -DartifactId=jruby-engine -Dversion=1.1.2 -Dpackaging=jar 

step2. Compile examples

  mvn compile

step3. Generate ant build files

  mvn ant:ant


step4. Run examples

  ant EvalTest
  ant -Djruby.home=[path to your JRuby 1.1RC3] LoadPathTest
  ant InvokeFunctionTest
  ant InvokeMethodTest
  ant GetInterfaceTest
