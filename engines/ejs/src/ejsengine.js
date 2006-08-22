/*
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: Redistributions of source code
 * must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution. Neither the name of the Sun Microsystems nor the names of
 * is contributors may be used to endorse or promote products derived from this software
 * without specific prior written permission.

 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

/*
 * This file implements JSR-223 compliant script engine
 * for Extended JavaScript (ejs) language implemented.
 * ejs is a JSP-like templating language for JavaScript.
 * Usual <% code-block %>, <%= expr %> are supported.
 *
 * Simple Example:
 *
 *     var ejsEngine = ejsScriptEngine();
 *     ejsEngine.put("greeting", "hello, world");
 *     ejsEngine.eval("<html><%=greeting%></html>"));
 *
 * If you want to refer to all globals of your JS engine,
 * then you can call
 *
 *     ejsEngine.eval("<html><%=greeting%></html>", context); 
 *
 * where "context" is current context of the JS engine. When
 * evaulating, the output goes to context's writer which by
 * default is standard output.
 *
 * @author A. Sundararajan
 * @author Mike Grogan [DeTagifier was "ported" from his Java code]
 */

/*
 * This function creates javax.script.ScriptEngineFactory
 * for ejs (Embedded JavaScript) language.
 */
function ejsScriptEngineFactory() {

    // file extensions for ejs
    var exts = java.util.Arrays.asList(["ejs"]);
    exts = java.util.Collections.unmodifiableList(exts);

    // no MIME types (yet)
    var emptyList = java.util.Arrays.asList([]);
    emptyList = java.util.Collections.unmodifiableList(emptyList);

    var factory = new Packages.javax.script.ScriptEngineFactory() {
        getEngineName: function() { 
            return "Embedded JavaScript"; 
        },
        getEngineVersion: function() { 
            return "1.0"; 
        },
        getExtensions: function() { 
            return exts; 
        },
        getMimeTypes: function() {
            return emptyList;
        },
        getNames : function() { 
            return exts; 
        },
        getLanguageName: function() { 
            return "ejs"; 
        },
        getLanguageVersion: function() {
            return "1.0";
        },
        getParameter: function(name) {
            var e = Packages.javax.script.ScriptEngine;
            if (e.ENGINE.equals(name)) {
                return this.getEngineName();
            } else if (e.ENGINE_VERSION.equals(name)) {
                return this.getEngineVersion();
            } else if (e.LANGUAGE.equals(name)) {
                return this.getLanguageName();
            } else if (e.LANGUAGE_VERSION.equals(name)) {
                return this.getLanguageVersion();
            } else if ("THREADING".equals(name)) {
                return "MULTITHREADED";
            } else {
                return null;
            }
        },
        getMethodCallSyntax: function(obj, method, args) {            
            throw "not implemented";
        },
        getOutputStatement: function(toDisplay) {
            throw "not implemented";
        },
        getProgram: function(stats) {
            throw "not implemented";
        },
        getScriptEngine: function() {
            return ejsScriptEngine(factory);
        },
        toString: function() {
            return "ejs script engine factory";
        }
    }
    return factory;
}

/*
 * This function creates javax.script.ScriptEngine
 * for ejs (Embedded JavaScript) language.
 *
 * @param factory javax.script.ScriptEngineFactory
 *                instance that created this engine [optional]
 * @return new javax.script.ScriptEngine instance
 *
 */
function ejsScriptEngine(factory) {

    function DeTagifier(outputStart, outputEnd, exprStart, exprEnd) {
        var START = 0;
        var IN_CODEBLOCK = 1;
        var IN_OUTPUTBLOCK = 2;
        var IN_EXPRBLOCK = 3;
        var INSIDE_START_TAG = 4;
        var INSIDE_INITIAL_START_TAG = 5;
        var INSIDE_END_TAG = 6;
        var INSIDE_CODE_EXPR_BLOCK = 7;
        var INSIDE_EXPR_END_TAG = 8;
        var INVALID_STATE = -1;

        this.outputStart = outputStart;
        this.outputEnd = outputEnd;
        this.exprStart = exprStart;
        this.exprEnd = exprEnd;
        
        this.parse = function(str) {            
            var charHolder = new java.lang.StringBuffer();
            var state = START;
            for (var i = 0; i < str.length; i++) {
                var ch = str.charAt(i);
                //ignore control-M
                if (ch == '\r') {
                    continue;
                }
            
                state = this.processChar(state, ch, charHolder);
                if (state == INVALID_STATE) {
                    return null;
                }
            }
            if (state == IN_OUTPUTBLOCK) {
                charHolder.append(outputEnd);
            }
      
            return charHolder.toString();
        }
    
        this.processChar = function(state, c, charHolder) {
            switch (state) {
            case START:
                if (c == '<') {
                    return INSIDE_INITIAL_START_TAG;
                } else {
                    charHolder.append(outputStart);
                    charHolder.append(c);
                    return IN_OUTPUTBLOCK;
                }
            case IN_CODEBLOCK:
                if (c == '%') {
                    return INSIDE_END_TAG;
                } else {
                    charHolder.append(c);
                    return IN_CODEBLOCK;
                }
            case IN_OUTPUTBLOCK:
                if (c == '<') {
                    return INSIDE_START_TAG;
                } else if (c == '\n') {
                    charHolder.append('\\');
                    charHolder.append('n');
                    charHolder.append(outputEnd);
                    charHolder.append(outputStart);
                    return IN_OUTPUTBLOCK;
                } else if (c == '"') {
                    charHolder.append('\\');
                    charHolder.append(c);
                    return IN_OUTPUTBLOCK;
                } else {
                    charHolder.append(c);
                    return IN_OUTPUTBLOCK;
                }
            case IN_EXPRBLOCK:
                if (c == '%') {
                    //charHolder.append("+ ''");
                    return INSIDE_EXPR_END_TAG;
                } else {                                        
                    charHolder.append(c);
                    return IN_EXPRBLOCK;
                }
            case INSIDE_INITIAL_START_TAG:
            case INSIDE_START_TAG:
                if (c == '%') {
                    if (state == INSIDE_START_TAG) {
                        charHolder.append(outputEnd);
                    }
                    return INSIDE_CODE_EXPR_BLOCK;
                } else {
                    if (state == INSIDE_INITIAL_START_TAG) {
                        charHolder.append(outputStart);
                    }
                    charHolder.append('<');
                    charHolder.append(c);
                    return IN_OUTPUTBLOCK;
                }
             case INSIDE_END_TAG:
                if (c == '>') {
                    charHolder.append(outputStart);
                    return IN_OUTPUTBLOCK;
                } else {
                    charHolder.append('%');
                    charHolder.append(c);
                    return IN_CODEBLOCK;
                }
             case INSIDE_CODE_EXPR_BLOCK:
                if (c == '=') {
                    charHolder.append(exprStart);
                    return IN_EXPRBLOCK;
                } else {
                    charHolder.append(c);
                    return IN_CODEBLOCK;
                }             
             case INSIDE_EXPR_END_TAG:
                if (c == '>') {
                    charHolder.append(exprEnd);
                    charHolder.append(outputStart);
                    return IN_OUTPUTBLOCK;
                } else {
                    charHolder.append('%');
                    charHolder.append(c);
                    return IN_EXPRBLOCK;
                }
             }
             println("Invalid State: " + state);
             return INVALID_STATE;  
         }
         
    }

    // my default script context
    var mycontext = new Packages.javax.script.SimpleScriptContext();

    // whether "eval" just compiles or evaluates EJS code!
    /*const*/ var EJS_COMPILE_MODE = "ejs.compile.mode";

    // clone mycontext and set new bindings 
    // as ENGINE_SCOPE bindings for it
    function newScriptContext(binds) {
        var ctxt = new Packages.javax.script.SimpleScriptContext();
        var gs = mycontext.getBindings(ctxt.GLOBAL_SCOPE);
        
        if (gs != null) {
            ctxt.setBindings(gs, ctxt.GLOBAL_SCOPE);
        }
        
        if (bindings != null) {
            ctxt.setBindings(binds, ctxt.ENGINE_SCOPE);
        } else {
            throw "Engine scope Bindings may not be null.";
        }
        ctxt.setReader(mycontext.getReader());
        ctxt.setWriter(mycontext.getWriter());
        ctxt.setErrorWriter(mycontext.getErrorWriter());      
        return ctxt;
    }

    // returns full content from a java.io.Reader
    function readFully(reader) {
        var arr = java.lang.reflect.Array.newInstance(
                java.lang.Character.TYPE, 8*1024); // 8K at a time
        var buf = new java.lang.StringBuffer();
        var numChars;
        while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
            buf.append(arr, 0, numChars);
        }
        return String(buf.toString());
    }

    function checkNull(obj) {
        if (obj == null) {
            throw "null pointer exception";
        }
    }

    function createDeTagifier() {
        return new DeTagifier("context.getWriter()['write(java.lang.String)'](\"",
                              "\");\n",
                              "context.getWriter()['write(java.lang.String)'](",
                              ");\n");
    }

    if (this.engine && (typeof(engine.eval) == 'function')) {
        function evalJS(str, ctx) {
            return engine.eval(str, ctx);    
        }
    } else {
        function evalJS(str, ctx) {
            return eval(str);
        }
    }

    function translateEjs(src) {
        var detagifier = createDeTagifier();
        var src = detagifier.parse(src);
        return src + "context.writer.flush();\n";
    }
 
    /*
     * Compile the given ejs script and return a
     * function as a result. The returned function
     * can be called later -- by passing new script
     * context optionally. Ideally, we would like 
     * to implement javax.script.Compilable interface,
     * we can not :-( 
     *
     * This is because javax.script.CompiledScript is 
     * an abstract class and we can not extend an abstract
     * class in JavaScript - atleast not in the JDK 6 
     * impl. of the same (JavaAdapter to extend class is 
     * not (yet) supported.
     */
    function compileTemplate(ctx, src) {
        var translated = translateEjs(src);
        return function(newCtx) {
            if (newCtx == null) {
                newCtx = ctx;
            }
            newCtx.setAttribute("context", newCtx, newCtx.ENGINE_SCOPE);
            return evalJS(translated, newCtx);
        }
    }

    // Evaluates given ejs script with given script context
    function evalTemplate(ctx, src) {
       ctx.setAttribute("context", ctx, ctx.ENGINE_SCOPE);
       var translated = translateEjs(src);
       return evalJS(translated, ctx);
    }

    return new Packages.javax.script.ScriptEngine() {
        eval: function (strOrReader, ctxOrBindings) {
            checkNull(strOrReader);
            var src;
            if (strOrReader instanceof java.lang.String) {
                src = String(strOrReader);
            } else if (strOrReader instanceof java.io.Reader) {
                src = readFully(strOrReader);
            } else if (typeof(strOrReader) == 'string') {
                src = strOrReader;
            } else {
                throw "illegal argument exception";
            }

            var ctx;
            if (ctxOrBindings instanceof Packages.javax.script.ScriptContext) {
                ctx = ctxOrBindings;
            } else if (ctxOrBindings instanceof Packages.javax.script.Bindings) {
                ctx = newScriptContext(ctxOrBindings);
            } else {
                ctx = mycontext;
            }

            ctx.setAttribute("context", ctx, ctx.ENGINE_SCOPE);

            /*
             * Whether we are "compiling" or running a ejs
             * script is controlled by EJS_COMPILE_MODE "flag".
             * In "compile mode", we return a function that can
             * be called multipled times (avoiding repeated ejs
             * parsing. With "eval" mode, we return a string that
             * is result of evaluating the given ejs script. As said
             * earlier, this is a workaround for not having proper
             * javax.script.Compilable implementation.
             */
             
            if (ctx.getAttribute(EJS_COMPILE_MODE)) {   
                return compileTemplate(ctx, src);
            } else {
                return evalTemplate(ctx, src);
            }
        },

        put: function(name, value) {
            checkNull(name);
            this.getBindings(mycontext.ENGINE_SCOPE).put(name, value);
        },

        get: function(name) {           
           checkNull(name);
           return this.getBindings(mycontext.ENGINE_SCOPE).get(name);
        },

        setBindings: function(binds, scope) {
            checkNull(name);
            if (scope == mycontext.ENGINE_SCOPE ||
                scope == mycontext.GLOBAL_SCOPE) {
                mycontext.setBindings(binds, scope);
            } else {
                throw "illegal argument exception";
            }
        },

        getBindings: function(scope) {
            if (scope == mycontext.GLOBAL_SCOPE) {
                return mycontext.getBindings(mycontext.GLOBAL_SCOPE);
            } else if (scope == mycontext.ENGINE_SCOPE) {
                return mycontext.getBindings(mycontext.ENGINE_SCOPE);
            } else {
                throw "Invalid scope value";
            }
        },

        createBindings: function() {
            return new Packages.javax.script.SimpleBindings();
        },

        getContext: function() {
            return mycontext;
        },

        setContext: function(ctx) {
            checkNull(ctx);
            mycontext = ctx;
        },

        getFactory: sync(function () {
            if (factory == null) {
                factory = ejsScriptEngineFactory();
            }
            return factory;
        }),

        toString: function() { return "ejs engine"; },
        equals: function (obj) { return obj === engineImpl; },
    };
}
