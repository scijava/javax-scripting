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
 * SchemeScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.scheme;

import java.lang.reflect.*;
import java.math.*;
import java.io.*;
import java.util.*;
import javax.script.*;

import sisc.data.*;
import sisc.nativefun.*;
import sisc.interpreter.*;
import sisc.io.*;
import sisc.modules.s2j.*;
import sisc.ser.*;
import sisc.reader.SourceReader;

public class SchemeScriptEngine extends AbstractScriptEngine 
        implements Invocable { 

    // my factory, may be null
    private ScriptEngineFactory factory;

    // my AppContext
    private AppContext appContext;
  
    public SchemeScriptEngine(Properties props) {
        appContext = new AppContext(props);  
        Context.register(uniqueName(), appContext);
        initApp();
    }

    public SchemeScriptEngine() {
        this(System.getProperties());
    }

    private static class ContextProc extends FixableProcedure {
        private ScriptContext ctx;

        ContextProc(ScriptContext ctx) {
            this.ctx = ctx;
        }

        public synchronized Value apply() throws ContinuationException {
            synchronized (ctx) {
                // default variable is 'context' itself
                return java2scheme(ctx.getAttribute("context", 
                                   ScriptContext.ENGINE_SCOPE));
            }
        }
            
        public synchronized Value apply(Value v1) throws ContinuationException {
            String name = Util.symval(v1);
            synchronized (ctx) {
                int scope = ctx.getAttributesScope(name);
                if (scope == -1) {
                    return VOID;
                } else {
                    return java2scheme(ctx.getAttribute(name, scope));
                }
            }
        }

        public synchronized Value apply(Value v1, Value v2) throws ContinuationException {
            String name = Util.symval(v1);
            synchronized (ctx) {
                int scope = ctx.getAttributesScope(name);
                if (scope == -1) {
                    scope = ScriptContext.ENGINE_SCOPE;
                }               
                ctx.setAttribute(name, scheme2java(v2), scope);
            }
            return v2;
        }
    }
   
    // Invocable methods
    public Object invokeFunction(String name, Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        return invokeImpl(null, name, args);
    }

    public Object invokeMethod(Object obj, String name, Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        if (obj == null) {
            throw new IllegalArgumentException("script object is null");
        }
        return invokeImpl(obj, name, args);
    }
        
    private Object invokeImpl(Object obj, final String name,
                              final Object... args) 
                         throws ScriptException, NoSuchMethodException {       
        if (name == null) {
            throw new NullPointerException("method name is null");
        }
        Value tmp = null;
        if (obj != null) {
            if (obj instanceof Value) {
                tmp = (Value) obj;
            } else {
                tmp = java2scheme(obj);
            }
        }

        final Value thiz = tmp;
        try {
            return Context.execute(appContext, new SchemeCaller() {
                public Object execute(Interpreter interp) {
                    try {
                        return invoke(interp, thiz, name, args);
                    } catch (ScriptException exp) {
                        throw new RuntimeException(exp);
                    } catch (NoSuchMethodException exp) {
                        throw new RuntimeException(exp);
                    }
                }
            });
        } catch (SchemeException se) {
            throw new ScriptException(se);
        } catch (RuntimeException re) {
            handleRuntimeException2(re);
            // should not reach here..
            return null;
        }
    }    

    public <T> T getInterface(Object obj, Class<T> clazz) {
        if (obj == null) {
            throw new IllegalArgumentException("script object is null");
        }
        return makeInterface(obj, clazz);
    }

    public <T> T getInterface(Class<T> clazz) {
        return makeInterface(null, clazz);
    }

    private <T> T makeInterface(Object obj, Class<T> clazz) {
        if (clazz == null || !clazz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
        final Object thiz = obj;
        return (T) Proxy.newProxyInstance(
              clazz.getClassLoader(),
              new Class[] { clazz },
              new InvocationHandler() {
                  public Object invoke(Object proxy, Method m, Object[] args)
                                       throws Throwable {
                      return invokeImpl(thiz, m.getName(), args);
                  }
              });
    }


    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        return eval(new StringReader(str), ctx);
    }

    public Object eval(final Reader reader, final ScriptContext ctx)
                       throws ScriptException { 
        try {
            return Context.execute(appContext, new SchemeCaller() {
                public Object execute(Interpreter interp) {
                    try {
                        return eval(interp, reader, ctx);
                    } catch (ScriptException exp) {
                        throw new RuntimeException(exp);
                    }
                }
            });
        } catch (SchemeException se) {
            throw new ScriptException(se);
        } catch (RuntimeException re) {
            handleRuntimeException(re);
            // should not reach here..
            return null;
        }
    
    }    

    public ScriptEngineFactory getFactory() {
	synchronized (this) {
	    if (factory == null) {
	    	factory = new SchemeScriptEngineFactory();
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

    static Object scheme2java(Value value) {
        if (value instanceof JavaObject) {
            return ((JavaObject)value).get();
        } else {
            if (value instanceof SchemeVoid) {
                return null;
            } else if (value instanceof SchemeBoolean) {
                return value.equals(SchemeBoolean.TRUE)? 
                       Boolean.TRUE: Boolean.FALSE;
            } else if (value instanceof SchemeCharacter) {
                return new Character(((SchemeCharacter)value).c);
            } else if (value instanceof SchemeString) {
                return ((SchemeString)value).asString();
            } else {
                return value;
            }
        }
    }

    static Value java2scheme(Object jobj) {
        if (jobj instanceof Value) {
            return (Value) jobj;
        } else if (jobj instanceof Boolean) {
            return (jobj.equals(Boolean.TRUE))? 
                   SchemeBoolean.TRUE : SchemeBoolean.FALSE;
        } else if (jobj instanceof Character) {
            return new SchemeCharacter(((Character)jobj).charValue());
        } else if (jobj instanceof String) {
            return new SchemeString((String)jobj);
        } else if (jobj instanceof Number) {
            if (jobj instanceof Long) {
                return Quantity.valueOf(((Long)jobj).longValue());
            } else if (jobj instanceof BigInteger) {
                return Quantity.valueOf((BigInteger)jobj);
            } else if (jobj instanceof BigDecimal) {
                return Quantity.valueOf((BigDecimal)jobj);
            } else {
                return Quantity.valueOf(((Number)jobj).doubleValue());
            }
        } else {
            Class jclass = (jobj == null)? Object.class : jobj.getClass();
            return Util.makeJObj(jobj, jclass);
        }
    }

    static Value[] wrapArguments(Object[] args) {
        if (args == null) {
            return new Value[0];
        }

        Value[] res = new Value[args.length];
        for (int i = 0; i < args.length; i++) {
            res[i] = java2scheme(args[i]);
        }
        return res;
    }

    private Object eval(Interpreter interp, Reader reader, ScriptContext ctx) 
                        throws ScriptException {
        try {
            initContext(interp, ctx);
            Value res = interp.evalInput(new SourceReader(
                           new BufferedReader(reader), getFileName(ctx)));
            return scheme2java(res);
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
    }

    private Object invoke(Interpreter interp, Value thiz, 
                          String name, Object[] args) 
                          throws ScriptException, NoSuchMethodException {
        try {
            Symbol procName = interp.getSymbol(name);
            Expression expr = interp.lookup(procName, Util.TOPLEVEL);
            if (expr instanceof Procedure) {
                if (thiz != null) {
                    // if thiz is present, pass that as first argument.
                    args = (args == null)? new Object[0] : args;                    
                    Object[] tmp = new Object[args.length + 1];
                    System.arraycopy(args, 0, tmp, 1, args.length);
                    tmp[0] = thiz;
                    args = tmp;
                }
                Value[] arguments = wrapArguments(args);
                initContext(interp, context);
                Value res = interp.eval((Procedure)expr, arguments);
                return scheme2java(res);
            } else {
                throw new NoSuchMethodException(name);
            }
        } catch (SchemeException exp) {
            throw new ScriptException(exp);
        }
    }
   
    private void initContext(Interpreter interp, ScriptContext ctx) {
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        Procedure ctxProc = new ContextProc(ctx);
        initContext(interp, ctxProc);
    }

    private void initContext(Interpreter interp, Value ctxProc) {        
        interp.define(Symbol.get("context"), ctxProc, Util.TOPLEVEL);        
        // alias for "context" procedure
        interp.define(Symbol.get("var"), ctxProc, Util.TOPLEVEL);
    }

    private void handleRuntimeException(RuntimeException re) 
                                 throws ScriptException {
        Throwable cause = re.getCause();
        if (cause instanceof ScriptException) {
            throw (ScriptException) cause;
        } else {
            throw re;
        }
    }

    private void handleRuntimeException2(RuntimeException re) 
                       throws ScriptException, NoSuchMethodException {
        Throwable cause = re.getCause();
        if (cause instanceof ScriptException) {
            throw (ScriptException) cause;
        } else if (cause instanceof NoSuchMethodException) {
            throw (NoSuchMethodException) cause;
        } else {
            throw re;
        }
    }

    private static final String SCHEME_HEAP_FILE = "sisc.shp";

    private SeekableInputStream findHeap() {
        try {
           InputStream heapIS = SchemeScriptEngine.class
                   .getResourceAsStream("/" + SCHEME_HEAP_FILE);
           if (heapIS == null) {
                String home = System.getProperty("sisc.home");
                if (home == null) {
                    return null;
                }
                String location = home + File.separator + SCHEME_HEAP_FILE;                
                return new BufferedRandomAccessInputStream(
                                       location, "r", 1, 8192);
           } else {
                return new MemoryRandomAccessInputStream(heapIS);
           }
        } catch (IOException exp) {
            return null;
        }
    }

    private void initApp() {
        try {
            SeekableInputStream his = findHeap();                        
            SeekableDataInputStream in = new SeekableDataInputStream(his);                 
            appContext.loadEnv(in);
        } catch (IOException ie) {
            throw new RuntimeException(ie);
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe);
        }
    }

    private static long sequence = 0L;
    private synchronized static String uniqueName() {
        return "com.sun.script.scheme.AppContext@" + Long.toString(sequence++);
    }    

    private static String getFileName(ScriptContext ctx) {
        Object name = ctx.getAttribute(ScriptEngine.FILENAME);
        if (name instanceof String) {
            return name.toString();
        } else {
            return "<unknown>";
        }
    }
}
