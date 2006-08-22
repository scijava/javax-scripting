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
 * for JavaScript Templates (JST) language implemented by
 * TrimPath. Please visit http://www.trimpath.com/ and 
 * look for "TrimPath Templates". JST is a PHP/ASP/JSP-like
 * templating language for JavaScript. To use this script
 * engine, you need to have
 *
 *  a) JavaScript engine :-) [which is part of JDK 6]
 *  b) TrimPath's template.js
 *
 * Simple Example:
 *
 *     var jstEngine = jstScriptEngine();
 *     jstEngine.put("greeting", "hello, world");
 *     println(jstEngine.eval("<html>${greeting}</html>"));
 *
 * If you want to refer to all globals of your JS engine,
 * then you can call
 *
 *     jstEngine.eval("<html>${greeting}</html>", context); 
 *
 * where "context" is current context of the JS engine.
 *
 * @author A. Sundararajan
 */

/*
 * This function creates javax.script.ScriptEngineFactory
 * for JST (JavaScript Templates) language.
 */
function jstScriptEngineFactory() {

    // file extensions for JST
    var exts = java.util.Arrays.asList(["jst"]);
    exts = java.util.Collections.unmodifiableList(exts);

    // no MIME types (yet)
    var emptyList = java.util.Arrays.asList([]);
    emptyList = java.util.Collections.unmodifiableList(emptyList);

    var factory = new Packages.javax.script.ScriptEngineFactory() {
        getEngineName: function() { 
            return "TrimPath JavaScript Templates"; 
        },
        getEngineVersion: function() { 
            return "1.0.38"; 
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
            return "jst"; 
        },
        getLanguageVersion: function() {
            return "1.0.38" 
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

        getMethodCallSyntax: function() {
            throw "not implemented";
        },
        getOutputStatement: function() {
            throw "not implemented";
        },
        getProgram: function() {
            throw "not implemented";
        },
        getScriptEngine: function() {
            return jstScriptEngine(factory);
        },
        toString: function() {
            return "jst script engine factory";
        }
    }
    return factory;
}

/*
 * This function creates javax.script.ScriptEngine
 * for JST (JavaScript Templates) language.
 *
 * @param factory javax.script.ScriptEngineFactory
 *                instance that created this engine [optional]
 * @return new javax.script.ScriptEngine instance
 *
 */
function jstScriptEngine(factory) {

    // my default script context
    var mycontext = new Packages.javax.script.SimpleScriptContext();

    // whether "eval" just compiles or evaluates JST code!
    /*const*/ var JST_COMPILE_MODE = "jst.compile.mode";

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

    // call given callback after setting given
    // ScriptContext as thread local context
    function callWithThreadContext(ctx, func) {
        var oldCtx = jstScriptEngine.threadContext.get();
        try {
            jstScriptEngine.threadContext.set(ctx);
            return func();
        } finally {
            jstScriptEngine.threadContext.set(oldCtx);
        }
    }

    /*
     * Compile the given JST script and return a
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
        var template = TrimPath.parseTemplate(src, name);
        var name = ctx.getAttribute(Packages.javax.script.ScriptEngine.FILENAME);
        if (name == null) { 
            name = "<unknown>"; 
        }
        return function(newCtx) {
            if (newCtx == null) {
                newCtx = ctx;
            }
            return callWithThreadContext(newCtx,
                function() {
                    newCtx.setAttribute("context", newCtx, newCtx.ENGINE_SCOPE);
                    var myscope = newCtx.getBindings(newCtx.ENGINE_SCOPE);
                    return template.process(scope(myscope));
                });
        }
    }

    // Evaluates given JST script with given script context
    function evalTemplate(ctx, src) {
        ctx.setAttribute("context", ctx, ctx.ENGINE_SCOPE);
        var template = TrimPath.parseTemplate(src, name);
        var name = ctx.getAttribute(Packages.javax.script.ScriptEngine.FILENAME);
        if (name == null) { 
            name = "<unknown>"; 
        }
        var myscope = ctx.getBindings(ctx.ENGINE_SCOPE);
        return template.process(scope(myscope));
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

            /*
             * Whether we are "compiling" or running a JST
             * script is controlled by JST_COMPILE_MODE "flag".
             * In "compile mode", we return a function that can
             * be called multipled times (avoiding repeated JST
             * parsing. With "eval" mode, we return a string that
             * is result of evaluating the given JST script. As said
             * earlier, this is a workaround for not having proper
             * javax.script.Compilable implementation.
             */
             
            if (ctx.getAttribute(JST_COMPILE_MODE)) {
                return callWithThreadContext(ctx,
                           function() { return compileTemplate(ctx, src) });
            } else {
                return callWithThreadContext(ctx, 
                           function() { return evalTemplate(ctx, src) });
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
                factory = jstScriptEngineFactory();
            }
            return factory;
        }),

        toString: function() { return "jst engine"; },
        equals: function (obj) { return obj === engineImpl; },
    };
}

/*
 * This thread local and associated initialization function
 * below is used to ensure that "eval" calls from TrimPath
 * use the correct ScriptContext - so that correct "scope"
 * will be used.
 */
jstScriptEngine.threadContext = new java.lang.ThreadLocal();
(function() {
    if (this.engine && (typeof(engine.eval)=='function')) {
        /*
         * FIXME: abstract breaking! TrimPath evaluates
         * JS code by calling evalEx method. We "override" 
         * that to execute the script with the correct 
         * ScriptContext. Note that this depends on the
         * "engine" varible being defined in top level.
         */
        var thrCtx = jstScriptEngine.threadContext;
        if (this.TrimPath == null) {
            TrimPath = {};
        }

        TrimPath.evalEx = function (str) {
            var ctx = thrCtx.get();
            if (ctx != null) {
                return engine.eval(str, thrCtx.get());
            } else {
                return eval(str);
            }
        }
    }
})();