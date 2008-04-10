This is JSR-223 script engine for JRuby - Java implementation of Ruby language.
JRuby is available for download at http://jruby.codehaus.org/. We have built and tested with JRuby version 1.1RC3 and 1.1.

For JDK 1.5 users:
JDK 1.5 users need an archive of scripting API. Download the archive, sjp-1_0-fr-ri.zip, from http://www.jcp.org/en/jsr/detail?id=223, and unzip it. Then, add script-api.jar in your classpath.
Use com.sun.script.jruby.JRubyScriptEngineManager instead of javax.script.ScriptEngineManager to get script engine's instance.
