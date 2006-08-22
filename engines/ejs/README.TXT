This is JSR-223 script engine for the Embedded JavaScript language. 
Embedded JavaScript is JSP-like templating language for JavaScript.

Unlike other JSR-223 script engines, this engine is fully implemented in
JavaScript. So, you need a JavaScript engine to use this. But, this 
engine or it's factory can be passed to Java code - because this does 
implement javax.script.* interfaces. Because this is implemented in 
JavaScript, jar services based script engine discovery won't work for 
this script engine. But, you can register factory of this engine with 
your ScriptEngineManager instance and do extension (.ejs) based lookup 
and so on.  Or from JavaScript code, you can directly call the necessary
function to create script engine factory.

EJS script engine JavaScript API:

1. Function: ejsScriptEngineFactory()
 - returns an instance of javax.script.ScriptEngineFactory for EJS

2. Function: ejsScriptEngine()
 - returns an instance of javax.script.ScriptEngine for EJS. Actually
   you do not need to call this directly. You can call the
   getScriptEngine() method on factory instance.

EJS supports

<%= expression %>
<% code-snippet %>



