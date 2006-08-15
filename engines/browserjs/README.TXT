This is JSR-223 script engine for the native JavaScript engine 
embedded in web browsers. Unlike other script engines, this
script engine is *not* expected to work on all settings. This engine
works only inside the browsers - which effectively means only
Java applets can make use of this script engine.

This jsr-223 script engine is just a jsr-223 wrapper around LiveConnect
Java API. LiveConnect is Java-to-JavaScript (browser's native
JavaScript) communication API. This API is documented here:

http://java.sun.com/j2se/1.5.0/docs/guide/plugin/developer_guide/java_js.html

Because this engine uses LiveConnect, the applet's APPLET tag has to 
include mayscript="true" attribute so that browser's window object can 
be obtained. Also, user has to set Applet object as a variable named
"applet" in the current ScriptContext.