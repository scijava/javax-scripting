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
 * JellyScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.jelly;

import javax.script.*;
import java.lang.reflect.*;
import java.io.*;
import java.net.*;
import org.xml.sax.*;

import org.apache.commons.jelly.*;
import org.apache.commons.jelly.parser.*;
import org.apache.commons.jelly.expression.ExpressionFactory;

public class JellyScriptEngine extends AbstractScriptEngine 
        implements Compilable { 

    public static final String JELLY_OUTPUT = "com.sun.script.jelly.output";

    // my factory, may be null
    private ScriptEngineFactory factory;   
    private ScriptTagLibrary scriptTagLibrary;

    public JellyScriptEngine() {
        scriptTagLibrary = new ScriptTagLibrary();
    }

    // my implementation for CompiledScript
    private class JellyCompiledScript extends CompiledScript {
        // my compiled code
        private Script script;

        // my jelly context
        private JellyContext jctx;

        JellyCompiledScript (Script script, JellyContext jctx) {
            this.script = script;
            this.jctx = jctx;
        }

        Script getScript() {
            return script;
        }

        JellyContext getJellyContext() {
            return jctx;
        }

        public ScriptEngine getEngine() {
            return JellyScriptEngine.this;
        }

        public Object eval(ScriptContext ctx) throws ScriptException {
            return evalScript(this, ctx);
        }
    }

    // Compilable methods
    public CompiledScript compile (String str) 
                                  throws ScriptException {  
        return compile(new StringReader(str));
    }

    public CompiledScript compile (Reader reader) 
                                  throws ScriptException {  
        return compileJelly(reader, context);              
    }

    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx)
                       throws ScriptException { 
        return eval(new StringReader(str), ctx);
    }    

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        return evalScript(compileJelly(reader, ctx), ctx);
    }

    public ScriptEngineFactory getFactory() {
	synchronized (this) {
	    if (factory == null) {
	    	factory = new JellyScriptEngineFactory();
	    }
        }
	return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    public static final String ROOT_URL = "com.sun.script.jelly.root_url";
    public static final String CURRENT_URL = "com.sun.script.jelly.current_url";

    // package-private methods
    void setFactory(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    // internals only below this point      
    private JellyCompiledScript compileJelly(Reader reader, ScriptContext ctx) 
                                 throws ScriptException {
        // JSR-223 requirement
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        ctx.setAttribute("engine", this, ScriptContext.ENGINE_SCOPE);
        ScriptJellyContext jctx = new ScriptJellyContext(new JellyContext(), ctx);
        // expose JellyContext as "jcontext" variable
        ctx.setAttribute("jcontext", jctx, ScriptContext.ENGINE_SCOPE);
        jctx.registerTagLibrary("jelly:core", scriptTagLibrary);
        XMLParser parser = new XMLParser();
        parser.setExpressionFactory(scriptTagLibrary.getExpressionFactory());
        parser.setContext(jctx);
        try {
            Script script = parser.parse(reader);
            script = script.compile();
            return new JellyCompiledScript(script, jctx);
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
    }

    
    private Object evalScript(JellyCompiledScript jcs, ScriptContext ctx) 
                            throws ScriptException {
        // JSR-223 requirement
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        ctx.setAttribute("engine", this, ScriptContext.ENGINE_SCOPE);
        try {
            XMLOutput output = null;
            int scope = ctx.getAttributesScope(JELLY_OUTPUT);
            if (scope != -1) {
                Object o = ctx.getAttribute(JELLY_OUTPUT, scope);
                if (o instanceof OutputStream) {
                    output = XMLOutput.createXMLOutput((OutputStream)o, false);
                } else if (o instanceof Writer) {
                    output = XMLOutput.createXMLOutput((Writer)o, false);
                } else if (o instanceof XMLReader) {
                    output = XMLOutput.createXMLOutput((XMLReader)o);
                }
            }
            
            // no output configured, make use of context's output writer
            if (output == null) {
                output = XMLOutput.createXMLOutput(ctx.getWriter(), false);
            }

            ScriptJellyContext newJctx = new ScriptJellyContext(jcs.getJellyContext(), ctx);
            ctx.setAttribute("jcontext", newJctx, ScriptContext.ENGINE_SCOPE);
            newJctx.registerTagLibrary("jelly:core", scriptTagLibrary);
            Script script = jcs.getScript();
            script.run(newJctx, output);
            output.flush();
            return null;
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }     
    }
}
