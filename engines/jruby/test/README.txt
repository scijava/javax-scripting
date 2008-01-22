This directory contains examples using jruby engine with scripting API.

When you want to get these examples run, try steps below.

step1. Check out entire set of jruby enigne.

    cvs -d :pserver:username@cvs.dev.java.net:/cvs checkout scripting/engines/jruby

step2. Build jruby engine.

    cd scripting/engines/jruby
    ant -f make/build.xml

step3. Install jruby-engine.jar in your local maven repository.

   mvn install:install-file -Dfile=build/jruby-engine.jar -DgroupId=com.sun.script.jruby -DartifactId=jruby-engine -Dversion=1.1.0 -Dpackaging=jar 

step4. Compile examples

  cd test
  mvn compile

step5. Generate ant build files

  mvn ant:ant


step6. Run examples

  ant EvalTest
  ant -Djruby.home=[path to your JRuby 1.1RC1] LoadPathTest
  ant InvokeFunctionTest
  ant InvokeMethodTest
  ant GetInterfaceTest
