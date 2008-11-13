This directory contains samples how to use JSR 223 API with JRuby engine.
These samples get run by mvn command since this directory is a maven project. However, you need to do steps below before you try samples.

Step1. Install JSR 223 API in your local maven repository.

  This project is built and tested by JDK 5, which doesn't have JSR 223 API. Thus you need to install jar archive of the API in your local maven repository. Even though you use JDK 6, this step is necessary because the archive of jruby-engine in java.net repository depends on it.

  Download sjp-1_0-fr-ri.zip, from http://www.jcp.org/en/jsr/detail?id=223, and unzip it. Then try following command to add script-api.jar to the local repo.

   mvn install:install-file -DgroupId=javax.script -DartifactId=script-api -Dversion=1.0 -Dpackaging=jar -Dfile=/home/yoko/tools/jsr223-api/script-api.jar


Step2. Edit jruby.home System property in pom.xml

  LoadPathSample uses jruby.home System property. This property's value must be actual path to your jruby's top directory. Currently, "/home/yoko/tools/jruby-1.1.5" is set in the value tag. You need JRuby's source distribution to get LoadPathSample run.


Step3. Build and test all

  mvn clean install

  or

  mvn test


All samples are executed by only one command. If you open this direcory by using maven compilent IDE, you can test each sample one by one or do anything that your IDE supports. 
