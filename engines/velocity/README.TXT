This is JSR-223 script engine for Velocity general purpose template
engine in Java. More information and download URL for Velocity is 
available from http://jakarta.apache.org/velocity. We have built and 
tested this engine with Velocity version 1.5.

Special variables reserved by this engine:

com.sun.script.velocity.properties

  - java.util.Properties to configure Velocity. By default,
    Properties loaded from the following resource is used:
    "org/apache/velocity/runtime/defaults/velocity.properties"
    This variable is read from the context when the first 'eval' 
    call is made.


com.sun.script.velocity.stringOutput

  - by default, velocity script engine output goes to the
    context's output writer. But, if you want 'eval' to return
    a String result instead, then set this variable to true in
    the context.
