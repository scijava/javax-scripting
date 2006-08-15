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
 * BrowserJSScriptEngine.java
 * @author A. Sundararajan
 */
package com.sun.script.browserjs;
import java.io.*;
import java.util.*;
import javax.script.*;
import java.lang.reflect.*;
import netscape.javascript.*;
import java.applet.Applet;

public class BrowserJSScriptEngine 
    extends AbstractScriptEngine implements Invocable {

    // lazily initialized factory
    private volatile ScriptEngineFactory factory;

    public BrowserJSScriptEngine() {    
        this(null);
    }

    BrowserJSScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    public Object eval(Reader reader, ScriptContext ctx) 
                       throws ScriptException {
        return eval(readFully(reader), ctx);
    }
    
    public Object eval(String script, ScriptContext ctx) 
                       throws ScriptException {
        JSObject window = getBrowserWindow(ctx);
        try {
            ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
            synchronized (window) {
                Bindings scope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
                for (String key: scope.keySet()) {
                    window.setMember(key, scope.get(key));
                }
            }

            Object result = window.eval(script);

            synchronized (window) {
                Bindings scope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
                for (String key: scope.keySet()) {
                    scope.put(key, window.getMember(key));
                }
            }
            return result;
        } catch (JSException exp) {
            throw new ScriptException(exp);
        }
    }
    
    public Bindings createBindings() {
        return new SimpleBindings();
    }
    
    public ScriptEngineFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = new BrowserJSScriptEngineFactory();
                }
            }
        }
        return factory;
    }
   
    // javax.script.Invocable methods.
    public Object invokeFunction(String name, Object... args) 
             throws ScriptException, NoSuchMethodException  {
        return invokeImpl(null, name, args);
    }
   
    public Object invokeMethod(Object thiz, String name, Object... args) 
             throws ScriptException, NoSuchMethodException  {
        if (!(thiz instanceof JSObject)) {
            throw new IllegalArgumentException();
        }
        return invokeImpl((JSObject)thiz, name, args);
    }
	        
    public <T> T getInterface(Class<T> clazz) {
        return makeInterface(null, clazz);
    }

    public <T> T getInterface(Object thiz, Class<T> clazz) {
        if (!(thiz instanceof JSObject)) {
            throw new IllegalArgumentException();
        }
        return makeInterface((JSObject)thiz, clazz);
    }

    //-- Internals only below this point

    // invokes the specified method/function on the given object.
    private Object invokeImpl(JSObject thiz, String name, Object... args) 
             throws ScriptException, NoSuchMethodException  {
        if (name == null) {
            throw new NullPointerException("method name is null");
        }

        JSObject target;   
        if (thiz != null) {
            target = thiz;
        } else {
            target = getBrowserWindow(context);
        }

        Object res = target.eval("typeof(" + name + ") == 'function'");
        if (! res.equals(Boolean.TRUE)) {
            throw new NoSuchMethodException(name);
        }        

        try {
            return target.call(name, args);
        } catch (JSException exp) {
            throw new ScriptException(exp);
        }
    }

    private <T> T makeInterface(JSObject obj, Class<T> clazz) {
        final JSObject thiz = obj;
        if (clazz == null || !clazz.isInterface()) {
            throw new IllegalArgumentException("interface Class expected");
        }
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

    private String readFully(Reader reader) throws ScriptException {
        char[] arr = new char[8*1024]; // 8K at a time
        StringBuffer buf = new StringBuffer();
        int numChars;
        try {
            while ((numChars = reader.read(arr, 0, arr.length)) > 0) {
                buf.append(arr, 0, numChars);
            }
        } catch (IOException exp) {
            throw new ScriptException(exp);
        }
        return buf.toString();
    }

    public JSObject getBrowserWindow(ScriptContext ctx) throws ScriptException {
        Object tmp = ctx.getAttribute("applet");
        if (tmp instanceof Applet) {
            return JSObject.getWindow((Applet)tmp);
        } else {
            throw new ScriptException("The current applet is not set");
        }
    }
}
 
