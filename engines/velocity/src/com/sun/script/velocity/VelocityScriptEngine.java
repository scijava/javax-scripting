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
 * VelocityScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.velocity;

import javax.script.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.Properties;
import org.apache.velocity.*;
import org.apache.velocity.app.*;

public class VelocityScriptEngine extends AbstractScriptEngine {

    public static final String STRING_OUTPUT_MODE = "com.sun.script.velocity.stringOutput";

    // my factory, may be null
    private ScriptEngineFactory factory;
    private VelocityEngine vengine;

    public VelocityScriptEngine(ScriptEngineFactory factory, Properties props) {
        this.factory = factory;
        vengine = new VelocityEngine();
        try {
            if (props != null) {
                vengine.init(props);
            } else {
                vengine.init();
            }
        } catch (RuntimeException rexp) {
            throw rexp;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }   

    public VelocityScriptEngine(Properties props) {
        this(null, props);
    }

    public VelocityScriptEngine(ScriptEngineFactory factory) {
        this(factory, null);
    }

    public VelocityScriptEngine() {
        this(null, null);
    }

    public VelocityEngine getVelocityEngine() {
        return vengine;
    }
	
    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        return eval(new StringReader(str), ctx);
    }

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        String fileName = getFilename(ctx);
        VelocityContext vctx = getVelocityContext(ctx);
        boolean outputAsString = isStringOutputMode(ctx);
        Writer out;
        if (outputAsString) {
            out = new StringWriter();
        } else {
            out = ctx.getWriter();
        }
        try {
            vengine.evaluate(vctx, out, fileName, reader);
            out.flush();
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
        return outputAsString? out.toString() : null;
    }

    public ScriptEngineFactory getFactory() {
	  synchronized (this) {
	      if (factory == null) {
	          factory = new VelocityScriptEngineFactory();
	      }
        }
	  return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    // internals only below this point
    private VelocityContext getVelocityContext(ScriptContext ctx) {
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        ctx.setAttribute("vengine", vengine, ScriptContext.ENGINE_SCOPE);
        Bindings globalScope = ctx.getBindings(ScriptContext.GLOBAL_SCOPE);        
        Bindings engineScope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        if (globalScope != null) {
            return new VelocityContext(engineScope, new VelocityContext(globalScope));
        } else {
            return new VelocityContext(engineScope);
        }
    }

    private String getFilename(ScriptContext ctx) {
        Object fileName = ctx.getAttribute(ScriptEngine.FILENAME);
        return fileName != null? fileName.toString() : "<unknown>";
    }

    private boolean isStringOutputMode(ScriptContext ctx) {
        return ctx.getAttribute(STRING_OUTPUT_MODE) != null;
    }
}
