This is JSR-223 script engine for Freemarker general purpose template
engine in Java. More information and download URL for Freemarker is 
available from http://freemarker.sourceforge.net. We have built and 
tested this engine with Freemarker version 2.3.8.

Special variables reserved by this engine:

com.sun.script.freemarker.properties

  - java.util.Properties to configure Freemarker. This variable 
    is read from the context when the first 'eval' call is made.

com.sun.script.freemarker.template.dir
  - The directory from which templates will be loaded. By default,
    it is the current working directory.

com.sun.script.freemarker.stringOutput

  - by default, freemarker script engine output goes to the
    context's output writer. But, if you want 'eval' to return
    a String result instead, then set this variable to true in
    the context.
