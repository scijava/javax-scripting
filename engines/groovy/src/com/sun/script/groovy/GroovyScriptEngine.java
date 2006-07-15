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
 * GroovyScriptEngine.java
 * @author Mike Grogan
 * @author A. Sundararajan
 */
package com.sun.script.groovy;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.Reader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import javax.script.*;
import groovy.lang.*;
import org.codehaus.groovy.syntax.SyntaxException;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationFailedException;
import java.lang.reflect.*;

public class GroovyScriptEngine 
    extends AbstractScriptEngine implements Compilable, Invocable {
    
    
    private Map classMap;
    private GroovyClassLoader loader;
    private Class currentClass;
    private Script currentScript;
    public static int counter;
    private GroovyScriptEngineFactory factory;
    private Script scriptObject;
 
    static {
        
        MetaClass.setUseReflection(true);
        counter = 0;
    }
    
    public GroovyScriptEngine() {    
        
        currentClass = null;
        classMap = new HashMap();
        loader = new GroovyClassLoader(Thread.currentThread().getContextClassLoader(),
                                        new CompilerConfiguration());
        factory = null; 
    }
    
   
    
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        StringWriter writer = new StringWriter();
        try {
            int c = 0;
            while (-1 != (c = reader.read())) {
                writer.write(c);
            }    
            return eval(writer.toString(), context);
        } catch (IOException e) {
            throw new ScriptException("Could not read the script source.");
        }
    }
    
    public Object eval(String script, ScriptContext context) throws ScriptException {
        try {
            return eval(getScriptClass(script), context);
        } catch (SyntaxException e) {
            throw new ScriptException(e.getMessage(), e.getSourceLocator(), e.getLine());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ScriptException(e);
        }
            
    }
    
    public Object eval(Class scriptClass, ScriptContext context) throws ScriptException {
        
        //store the class for possible use by Invocable methods
        currentClass = scriptClass;
                
        try {
            
            //add context to bindings
            context.setAttribute("context", context, ScriptContext.ENGINE_SCOPE);
        
            //direct output to context.getWriter
            Writer writer = context.getWriter();
            
            context.setAttribute("out", (writer instanceof PrintWriter) ? 
                                    writer :
                                    new PrintWriter(writer),
                                    ScriptContext.ENGINE_SCOPE);

            final ScriptContext ctx = context;
            Binding binding = new Binding(context.getBindings(ScriptContext.ENGINE_SCOPE)) {
                                  public Object getVariable(String name) {
                                      int scope = ctx.getAttributesScope(name);
                                      if (scope != -1) {
                                          return ctx.getAttribute(name, scope);
                                      } else {
                                          return super.getVariable(name);
                                      }
                                  }
                                  public void setVariable(String name, Object value) {
                                      int scope = ctx.getAttributesScope(name);
                                      if (scope != -1) {
                                          ctx.setAttribute(name, value, scope);
                                      } else {
                                          super.setVariable(name, value);
                                      }
                                  }
                              };

            scriptObject = InvokerHelper.createScript(scriptClass, binding);
            return scriptObject.run();
       
        } catch (Exception e) {
            throw new ScriptException(e);
        }                      
    }
    
    
    public Bindings createBindings() {
        return new SimpleBindings();
    }
    
    public ScriptEngineFactory getFactory() {
        if (factory == null) {
            factory = new GroovyScriptEngineFactory();
        }
        
        return factory;
    }
    
   protected synchronized String generateScriptName() {
        return "Script" + (++counter) + ".groovy";
   }
    
   public Class getScriptClass(String script) throws SyntaxException, 
                                                    CompilationFailedException, 
                                                    IOException {
        Object obj = classMap.get(script);
        if (obj != null) {
            return (Class)obj;
        }
        
        Class clasz = loader.parseClass(new ByteArrayInputStream(script.getBytes()), 
                                                        generateScriptName());
        
        classMap.put(script, clasz);
        
        return clasz;
    }
   
 
   public CompiledScript compile(String scriptSource) throws ScriptException {
       try {
            return new GroovyCompiledScript(this, 
                                    getScriptClass(scriptSource));
       } catch (SyntaxException e) {
            throw new ScriptException(e.getMessage(), e.getSourceLocator(), e.getLine());
       } catch (IOException e) {
           throw new ScriptException(e);
       } catch (CompilationFailedException ee) {
           throw new ScriptException(ee);
       }
   }   
    
   public CompiledScript compile(Reader reader) throws ScriptException {
       StringWriter writer = new StringWriter();
        try {
            int c = 0;
            while (-1 != (c = reader.read())) {
                writer.write(c);
            }    
            return new GroovyCompiledScript(this, getScriptClass(writer.toString()));
        } catch (IOException e) {
            throw new ScriptException("Could not read the script source.");
        } catch (SyntaxException e) {
            throw new ScriptException(e.getMessage(), e.getSourceLocator(), e.getLine());
        } catch (CompilationFailedException ee) {
            throw new ScriptException(ee);
        }
   }
   
   /* Invocable methods.
    */
   public Object invokeFunction(String name, Object... args) 
            throws ScriptException, NoSuchMethodException  {
       return invokeImpl(null, name, args);
   }
   
   public Object invokeMethod(Object thiz, String name, Object... args) 
            throws ScriptException, NoSuchMethodException  {
       if (thiz == null) {
           throw new IllegalArgumentException("script object is null");
       }
       return invokeImpl(thiz, name, args);
   }
	        
   private Object invokeImpl(Object thiz, String name, Object... args) 
            throws ScriptException, NoSuchMethodException  {
       if (name == null) {
           throw new NullPointerException("method name is null");
       }
       //use reflection as with any java object.. If a this
       //reference is passed, use it.  Otherwise, use the current
       //GroovyClass.    
       int len =  0;
       Class[] types = null;
       if (args != null) {
		len = args.length;
       		types = new Class[len];
       		for (int i = 0; i < len; i++) {
           		types[i] = java.lang.Object.class;
		}
      	} else {
		types = new Class[]{};
	}
       
       Class clasz = null;
       
       if (thiz != null) {
           clasz = thiz.getClass();
       } else {
           clasz = currentClass;
	   if (scriptObject == null) {
		throw new ScriptException("Script has not been compiled.");
	   }
	   thiz = scriptObject;
       }
       
       
       Method method = clasz.getMethod(name, types);
       if (thiz == null && !Modifier.isStatic(method.getModifiers())) {
           throw new NoSuchMethodException();
       }
       if (thiz != null) {
           try {
                return method.invoke(thiz,  args);
           } catch (IllegalAccessException e) {
               //won't get here
               return null;
           } catch (InvocationTargetException e) {
               throw new ScriptException(e);
           }
       } else {
           //use InvokerHelper
           try {
               return InvokerHelper.invokeMethod(currentScript, name, args);
           } catch (Exception e) {
               throw new ScriptException(e);
           }
       }
               
   } 
   
   public <T> T getInterface(Class<T> clasz) {
       return makeInterface(null, clasz);
   }

   public <T> T getInterface(Object thiz, Class<T> clasz) {
       if (thiz == null) {
           throw new IllegalArgumentException("script object is null");
       }
       return makeInterface(thiz, clasz);
   }

   public <T> T makeInterface(Object obj, Class<T> clazz) {
       final Object thiz = obj;
       if (clazz == null || !clazz.isInterface()) {
           throw new IllegalArgumentException("interface Class expected");
       }
       return (T) Proxy.newProxyInstance(
             clazz.getClassLoader(),
             new Class[] { clazz },
             new InvocationHandler() {
                 public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
                     return invokeImpl(thiz, m.getName(), args);
                 }
             });
   }
}
