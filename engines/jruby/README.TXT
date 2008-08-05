This is JSR-223 script engine for JRuby - Java implementation of Ruby language.
JRuby is available for download at http://jruby.codehaus.org/. We have built and tested with JRuby version 1.1RC3, 1.1, 1.1.1, 1.1.2 and 1.1.3.

Tips for JDK 1.5 users:
JDK 1.5 users need an archive of scripting API since it has been included in JDK 1.6. Download the archive, sjp-1_0-fr-ri.zip, from http://www.jcp.org/en/jsr/detail?id=223, and unzip it. Then, add script-api.jar in your classpath.
If you got java.lang.UnsupportedClassVersionError when you ran your code, give com.sun.script.jruby.JRubyScriptEngineManager a try instead of javax.script.ScriptEngineManager to get script engine's instance. The class, com.sun.script.jruby.JRubyScriptEngineManager, works in the same way as javax.script.SciprtEngineManager.
