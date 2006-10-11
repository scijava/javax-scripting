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
 * OgnlScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.juel;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.script.*;
import java.io.*;
import javax.el.*;
import de.odysseus.el.util.SimpleResolver;

public class JuelScriptEngine extends AbstractScriptEngine 
        implements Compilable {

    // my factory, may be null
    private ScriptEngineFactory factory;
    private ExpressionFactory exprFactory;

    public JuelScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
        this.exprFactory = ExpressionFactory.newInstance();
    }

    public JuelScriptEngine() {
        this(null);
    }

    // my implementation for CompiledScript
    private class JuelCompiledScript extends CompiledScript {
        private ValueExpression expr;

        JuelCompiledScript (ValueExpression expr) {
            this.expr = expr;
        }

        public ScriptEngine getEngine() {
            return JuelScriptEngine.this;
        }

        public Object eval(ScriptContext ctx) throws ScriptException {
            return evalExpr(expr, ctx);
        }
    }

    public CompiledScript compile (String script) throws ScriptException {
        ValueExpression expr = parse(script, context);
        return new JuelCompiledScript(expr);
    }

    public CompiledScript compile (Reader reader) throws ScriptException {
        return compile(readFully(reader));
    }

    public Object eval(String script, ScriptContext ctx) 
                       throws ScriptException {	
        ValueExpression expr = parse(script, ctx);
        return evalExpr(expr, ctx);
    }

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException {
        return eval(readFully(reader), ctx);
    }

    public ScriptEngineFactory getFactory() {
        synchronized (this) {
	      if (factory == null) {
	    	    factory = new JuelScriptEngineFactory();
	      }
        }
	  return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    //-- Internals only below this point

    private class ScriptContextVariableMapper extends VariableMapper {
        private ScriptContext ctx;

        ScriptContextVariableMapper(ScriptContext ctx) {
            this.ctx = ctx;
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            int scope = ctx.getAttributesScope(variable); 
            if (scope != -1) {
                Object value = ctx.getAttribute(variable, scope);
                if (value instanceof ValueExpression) {
                    return (ValueExpression) value;
                } else {
                    return exprFactory.createValueExpression(
                        value, Object.class);
                }
            }
            return null;
        }

        @Override
        public ValueExpression setVariable(String variable, 
                                     ValueExpression value) {
            ValueExpression oldValue = resolveVariable(variable);
            ctx.setAttribute(variable, value, ScriptContext.ENGINE_SCOPE);
            return oldValue;
        }
    }

    private class ScriptContextFunctionMapper extends FunctionMapper {
        private ScriptContext ctx;
        ScriptContextFunctionMapper(ScriptContext ctx) {
            this.ctx = ctx;
        }

        private String getFullName(String prefix, String localName) {
            return prefix + ":" + localName;
        }

        @Override
        public Method resolveFunction(String prefix, String localName) {
            String fullName = getFullName(prefix, localName);
            int scope = ctx.getAttributesScope(fullName);
            if (scope != -1) {
                Object tmp = ctx.getAttribute(fullName);
                return (tmp instanceof Method)? (Method)tmp : null;
            } else {
                return null;
            }
        }
    }

    private ELContext toELContext(final ScriptContext ctx) {
        Object tmp = ctx.getAttribute("elcontext");
        if (tmp instanceof ELContext) {
            return (ELContext)tmp;
        }

        ctx.setAttribute("context", ctx, 
                         ScriptContext.ENGINE_SCOPE);

        // define built-in functions        
        ctx.setAttribute("out:print", getPrintMethod(),
                         ScriptContext.ENGINE_SCOPE);

        SecurityManager manager = System.getSecurityManager();
        if (manager == null) {
            ctx.setAttribute("lang:import", getImportMethod(),
                         ScriptContext.ENGINE_SCOPE);
        }

        ELContext elContext = new ELContext() {
            ELResolver resolver = new SimpleResolver();
            VariableMapper varMapper = new ScriptContextVariableMapper(ctx);
            FunctionMapper funcMapper = new ScriptContextFunctionMapper(ctx);

            @Override
            public ELResolver getELResolver() {
                return resolver;
            }

            @Override
            public VariableMapper getVariableMapper() {
                return varMapper;	
		}

            @Override
            public FunctionMapper getFunctionMapper() {
                return funcMapper;
            }
        };
        ctx.setAttribute("elcontext", elContext, 
                         ScriptContext.ENGINE_SCOPE);
        return elContext;
    }

    private ValueExpression parse(String script, 
                                  ScriptContext ctx) 
                                  throws ScriptException {
        try {
            return exprFactory.createValueExpression(
                   toELContext(ctx), script, Object.class);
        } catch (ELException elexp) {
            throw new ScriptException(elexp);
        }
    }

    private Object evalExpr(ValueExpression expr, 
                            ScriptContext ctx) 
                            throws ScriptException {
        try {
            return expr.getValue(toELContext(ctx));
        } catch (ELException elexp) {
            throw new ScriptException(elexp);
        }
    }

    private String readFully(Reader reader) 
                            throws ScriptException { 
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

    private static Method getPrintMethod() {
        try {
            Class myClass = JuelScriptEngine.class;
            Method method = myClass.getMethod(
                "print", new Class[] { Object.class });
            return method;
        } catch (Exception exp) {
            // should not happen
            return null;
        }
    }

    public static void print(Object obj) {
        System.out.print(obj);
    }

    private static Method getImportMethod() {
        try {
            Class myClass = JuelScriptEngine.class;
            Method method = myClass.getMethod(
                "importFunctions", 
                new Class[] { ScriptContext.class,
                              String.class, 
                              Object.class 
                            });
            return method;
        } catch (Exception exp) {
            // should not happen
            return null;
        }
    }
    
    public static void importFunctions(ScriptContext ctx,
                            String namespace,
                            Object obj) {
        Class clazz = null;
        if (obj instanceof Class) {
            clazz = (Class) obj;
        } else if (obj instanceof String) {
            try {
                clazz = Class.forName((String) obj);
            } catch (ClassNotFoundException cnfe) {
                throw new ELException(cnfe);
            }
        } else {
            throw new ELException("Class or class name is missing");
        }
        Method[] methods = clazz.getMethods();
        for (Method m : methods) {
            int mod = m.getModifiers();
            if (Modifier.isStatic(mod) &&
                Modifier.isPublic(mod)) {
                String name = namespace + ":" + m.getName();
                ctx.setAttribute(name, m, 
                            ScriptContext.ENGINE_SCOPE);
            }
        }
    }
}