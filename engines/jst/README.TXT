This is JSR-223 script engine for the JavaScript templates (JST) language. 
JavaScript Templates is available for download at http://www.trimpath.com.
We have built and tested with TrimPath JavaScript Templates version 1.0.38.

Unlike other JSR-223 script engines, this engine is fully implemented in
JavaScript. So, you need a JavaScript engine to use this. But, this 
engine or it's factory can be passed to Java code - because this does 
implement javax.script.* interfaces. Because this is implemented in 
JavaScript, jar services based script engine discovery won't work for 
this script engine. But, you can register factory of this engine with 
your ScriptEngineManager instance and do extension (.jst) based lookup 
and so on.  Or from JavaScript code, you can directly call the necessary
function to create script engine factory.

JST script engine JavaScript API:

1. Function: jstScriptEngineFactory()
 - returns an instance of javax.script.ScriptEngineFactory for JST

2. Function: jstScriptEngine()
 - returns an instance of javax.script.ScriptEngine for JST. Actually
   you do not need to call this directly. You can call the
   getScriptEngine() method on factory instance.



