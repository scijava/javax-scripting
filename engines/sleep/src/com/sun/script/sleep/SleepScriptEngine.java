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
 * SleepScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.sleep;

import javax.script.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;

import sleep.bridges.*;
import sleep.engine.*;
import sleep.interfaces.*;
import sleep.runtime.*;

public class SleepScriptEngine extends AbstractScriptEngine 
        implements Compilable, Invocable { 

    // my factory, may be null
    private ScriptEngineFactory factory;
    // my ScriptLoader
    private ScriptLoader loader;
    // my env shared by all ScriptInstances created by me
    private Hashtable scriptEnv;

    public SleepScriptEngine() {
        loader = new ScriptLoader();
        scriptEnv = new Hashtable();
    }

    // my implementation for CompiledScript
    private class SleepCompiledScript extends CompiledScript {
        // my compiled code
        private ScriptInstance script;

        SleepCompiledScript (ScriptInstance script) {
            this.script = script;
        }

        public ScriptEngine getEngine() {
            return SleepScriptEngine.this;
        }
       
        public synchronized Object eval(ScriptContext ctx) throws ScriptException {
            return evalScript(script, ctx);
        }
    }

    // Compilable methods
    public CompiledScript compile(String code) 
                                  throws ScriptException {  
        ScriptInstance script = compileScript(code, context);
        return new SleepCompiledScript(script);
    }

    public CompiledScript compile (Reader reader) 
                                  throws ScriptException {  
        return compile(readFully(reader));
    }

    // Invocable methods
    public Object invoke(String name, Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        return invoke(null, name, args);
    }

    public Object invoke(Object obj, String name, Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        if (name == null) {
            throw new NullPointerException("method name is null");
        }
        return invokeMethod(obj, name, args, Object.class);
    }

    public <T> T getInterface(Object obj, Class<T> clazz) {
        final Object thiz = obj;         

        return (T) Proxy.newProxyInstance(
              clazz.getClassLoader(),
              new Class[] { clazz },
              new InvocationHandler() {
                  public Object invoke(Object proxy, Method m, Object[] args)
                                       throws Throwable {
                      return invokeMethod(thiz, m.getName(), args, m.getReturnType());
                  }
              });
    }

    public <T> T getInterface(Class<T> clazz) {
        return getInterface(null, clazz);
    }

    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        ScriptInstance script = compileScript(str, ctx);
        return evalScript(script, ctx);
    }

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        ScriptInstance script = compileScript(reader, ctx);
        return evalScript(script, ctx);
    }

    public ScriptEngineFactory getFactory() {
	synchronized (this) {
	    if (factory == null) {
	    	factory = new SleepScriptEngineFactory();
	    }
        }
	return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    // package-private methods
    void setFactory(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    // internals only below this point    
    private Object sleepToJava(Object value, ScriptInstance script) {
        return sleepToJava(value, Object.class, script);
    }

    private Object sleepToJava(Object value, Class type, ScriptInstance script) {
        if (value instanceof Scalar) {
            return ObjectUtilities.buildArgument(type, (Scalar)value, script);
        } else {
            return value;
        }            
    }

    private Scalar javaToSleep(Object value) {
        if (value instanceof Scalar) {
            return (Scalar) value;
        }
        // seems like a bug in Sleep interpreter - not handling
        // Short type 
        if (value.getClass() == Short.class) {
            return SleepUtils.getScalar((int)(((Short)value).shortValue()));
        } else {            
            return ObjectUtilities.BuildScalar(true, value);
        }
    }       

    private ScriptInstance compileScript(String code, ScriptContext ctx) 
                                 throws ScriptException {    
        String fileName = (String) ctx.getAttribute(ScriptEngine.FILENAME);
        if (fileName == null) {
            fileName = "<unknown>";
        }
        // Compile step is nothing by loading script and saving
        // ScriptInstance from that...
        return loader.loadScript(fileName, code, scriptEnv);    
    }

    private ScriptInstance compileScript(Reader reader, ScriptContext ctx) 
                                 throws ScriptException {
        return compileScript(readFully(reader), ctx);
    }
    
    private Object evalScript(final ScriptInstance script, final ScriptContext ctx) 
                            throws ScriptException {
       ctx.setAttribute("$context", ctx, ScriptContext.ENGINE_SCOPE);
       // We create a Variable bridge that will resolve variables from
       // our ScriptContext
       ScriptVariables variables = new ScriptVariables(new DefaultVariable() {
                        public synchronized boolean scalarExists(String key) {
                            synchronized (ctx) {
                                return ctx.getAttributesScope(key) != -1;
                            }
                        }

                        public synchronized Scalar getScalar(String key) {
                            synchronized (ctx) {
                                int scope = ctx.getAttributesScope(key);  
                                Object value = null;
                                if (scope != -1) {           
                                    value = ctx.getAttribute(key, scope);                                
                                }
                                Scalar scalar = javaToSleep(value);
                                // not to store Scalar in the ScriptContext or else
                                // we'll loose modifications to it. Note that Scalar
                                // has one further level of indirection via ScalarType.
                                ctx.setAttribute(key, scalar, scope);
                                return scalar;
                            }
                        }

                        public synchronized Scalar putScalar(String key, Scalar value) {
                            synchronized (ctx) {
                                int scope = ctx.getAttributesScope(key);
                                Scalar oldValue = null;
                                if (scope == -1) {
                                    scope = ScriptContext.ENGINE_SCOPE;  
                                } else {
                                    oldValue = javaToSleep(ctx.getAttribute(key));
                                } 
                                ctx.setAttribute(key, value, scope);
                                return oldValue;
                            }
                        }

                        public synchronized void removeScalar(String key) {
                            synchronized (ctx) {
                                int scope = ctx.getAttributesScope(key);
                                if (scope != -1) {
                                    ctx.removeAttribute(key, scope);
                                }
                            }
                        }
 
                    });
        
        script.setScriptVariables(variables);
        try {
            // run the script and convert return value
            return sleepToJava(script.runScript(), script);
        } catch (Exception e) {
            throw new ScriptException(e);
        }
    }    

    private Object invokeMethod(Object thiz, String name, 
                      Object[] args, Class returnType) 
                      throws ScriptException, NoSuchMethodException {
        // Sleep routine names start with a '&'
        if (!name.startsWith("&")) {
            name = "&" + name;
        }
        ScriptInstance script = new ScriptInstance(scriptEnv);
        Object func = null;
        try {
            // Object orientation by Closures - Sleep's "objects" are
            // Closures with first parameter being the name of the message.
            if (thiz != null) { 
                // do we have a Function Scalar as 'thiz' script object?
                if (thiz instanceof Scalar && 
                    SleepUtils.isFunctionScalar((Scalar)thiz)) {
                    thiz = SleepUtils.getFunctionFromScalar((Scalar)thiz, script);
                }
                if (thiz instanceof Function) {
                    func = thiz;
                } 
            } else {
                // no thiz. Look for global subroutine using environment
                func = scriptEnv.get(name);
            }
        } catch (Exception e) {
            throw new ScriptException(e);
        }
  
        if (func instanceof Function) {
            Stack sArgs = new Stack();
            // Sleep arguments have to be pushed in reverse order
            for (int i = args.length - 1; i >= 0; i--) {
                sArgs.push(javaToSleep(args[i]));
            }
            try {
                // run script function
                return SleepUtils.runCode((Function)func, name.substring(1), 
                                 script, sArgs);
            } catch (Exception e) {
                throw new ScriptException(e);
            }
        } else {
            throw new NoSuchMethodException(name.substring(1));
        }
    }

    private String readFully(Reader reader) throws ScriptException { 
        BufferedReader in;
        if (! (reader instanceof BufferedReader)) {
            in = new BufferedReader(reader);
        } else {
            in = (BufferedReader) reader;
        }
        StringBuffer buf = new StringBuffer();
        try {
            String s = in.readLine();
            while (s != null) {
                buf.append("\n");
                buf.append(s);
                s = in.readLine();
            }
            return buf.toString();            
        } catch (IOException exp) {
            throw new ScriptException(exp);
        } 
    }
}
