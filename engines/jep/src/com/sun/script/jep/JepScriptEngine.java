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
 * JepScriptEngine.java
 *
 * This class is script engine for Java Expression Parser (JEP)
 *
 * @author A. Sundararajan
 */
package com.sun.script.jep;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Observer;
import java.util.Observable;
import java.util.Stack;
import javax.script.*;

import org.nfunk.jep.*;
import org.nfunk.jep.function.*;
import org.nfunk.jep.type.*;

import org.lsmp.djep.groupJep.GroupJep;
import org.lsmp.djep.groupJep.groups.*;
import org.lsmp.djep.vectorJep.VectorJep;

public class JepScriptEngine 
    extends AbstractScriptEngine implements Compilable {

    /*
     * Current mode - determines basic data type of JEP
     * Refer to getContextJep() method.
     */
    public static final String JEP_MODE = "jep.mode";

    // lazily initialized factory
    private volatile ScriptEngineFactory factory;

    public JepScriptEngine() {
        this(null); 
    }

    JepScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }

    public Object eval(Reader reader, ScriptContext ctx) 
                       throws ScriptException {
        return eval(readFully(reader), context);
    }
    
    public Object eval(String script, ScriptContext ctx) 
                       throws ScriptException {
        JEP jep = getContextJep(ctx);
        Node node = parse(jep, script);
        return evaluate(jep, node);
    }
    
    public Bindings createBindings() {
        return new SimpleBindings();
    }
    
    public ScriptEngineFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
                if (factory == null) {
                    factory = new JepScriptEngineFactory();
                }
            }
        }
        return factory;
    }
   
    // javax.script.Compilable methods 
    private class JepCompiledScript extends CompiledScript {
        private Node node;

        JepCompiledScript(Node node) {
            this.node = node;
        }

        public Object eval(ScriptContext ctx) throws ScriptException {
            JEP jep = getContextJep(ctx);
            return evaluate(jep, node);
        }
    
        public ScriptEngine getEngine() {
            return JepScriptEngine.this;
        }
    }

    public CompiledScript compile(String script) throws ScriptException {
        JEP jep = getContextJep(context);
        Node node = parse(jep, script);
        return new JepCompiledScript(node);  
    }   
    
    public CompiledScript compile(Reader reader) throws ScriptException {
        return compile(readFully(reader));
    }

    // -- Internals only below this point
    private Node parse(JEP jep, String str)
                       throws ScriptException {
        try {
            return jep.parse(str);
        } catch (ParseException pe) {
            throw new ScriptException(pe);
        }
    }

    private Object evaluate(JEP jep, Node node) 
                            throws ScriptException {
        try {
            return jep.evaluate(node);
        } catch (ParseException pe) {
            throw new ScriptException(pe);
        }
    }

    // This command class is used to expose "mode" function to JEP
    private class ModeCommand extends PostfixMathCommand {
        private ScriptContext ctx;

        public ModeCommand(ScriptContext ctx) {
            this.ctx = ctx;
            numberOfParameters = 1;
        }

        public void run(Stack s) throws ParseException {
            checkStack(s);            
            String mode = s.pop().toString();
            ctx.setAttribute(JEP_MODE, mode, ScriptContext.ENGINE_SCOPE);
            getContextJep(ctx);            
            s.push(null);
        }
    }

    private JEP getContextJep(final ScriptContext ctx) {
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);      
        Object tmp  = ctx.getAttribute(JEP_MODE);
         
        JEP jep;
        if (tmp instanceof JEP) {
            jep = (JEP) tmp;
            importEngineBindings(jep, ctx);
            return jep;
        } else if (tmp instanceof String) {
            String mode = (String)tmp; 
        
            /*
             * We support a handful of "modes" - mode is nothing
             * but different JEP implementations in jep, djep
             * packages. I've not added all modes possible. In
             * particular, I've left differential ones.
             */

            if (mode.equals("complex")) {
                jep = new JEP();
                jep.addComplex();
            } else if (mode.equals("integer")) {
                jep = new GroupJep(new Integers());
            } else if (mode.equals("real")) {
                jep = new GroupJep(new Reals());
            } else if (mode.equals("bigreal")) {
                jep = new GroupJep(new BigReals(1));
            } else if (mode.equals("rational")) {
                jep = new GroupJep(new Rationals());
            } else if (mode.equals("quarternion")) {
                jep = new GroupJep(new Quaternions());            
            } else if (mode.equals("vector")) {
                jep = new VectorJep();
            } else if (mode.startsWith("z") || 
                       mode.startsWith("Z")) {
                String mod = mode.substring(1);
                jep = new GroupJep(new Zn(new BigInteger(mod))); 
            } else {
                ctx.setAttribute(JEP_MODE, null, ScriptContext.ENGINE_SCOPE);
                throw new RuntimeException("unsupported mode: " + mode);
            }
        } else {
            jep = new JEP();
        }

        // allow assignments
        jep.setAllowAssignment(true);
        // allow undeclared variables (with zero values)
        jep.setAllowUndeclared(true);
        // add standard functions and constants
        jep.addStandardFunctions();
        jep.addStandardConstants();

        // add "mode" function to switch the current JEP mode
        jep.addFunction("mode", new ModeCommand(ctx));

        // We set this observer to sync variables from JEP
        // to the current ScriptContext object
        Observer observer = new Observer() {
            public void update(Observable arg0, Object arg1) {
                Variable var;
                if (arg0 instanceof Variable) {
                    var = (Variable)arg0;                 
                } else if (arg0 instanceof SymbolTable.StObservable) {
                    var = (Variable)arg1;
                    var.addObserver(this);
                } else {
                    // should not happen!
                    return;
                }

                assert var != null : "huh! null Variable?";
                if (var.isConstant()) { return; }
                String name = var.getName();
                synchronized (ctx) {
                    int scope = ctx.getAttributesScope(name);
                    if (scope == -1) {
                        scope = ScriptContext.ENGINE_SCOPE;
                    }
                    ctx.setAttribute(name, var.getValue(), scope);
                }                      
            }
        };
        jep.getSymbolTable().addObserverToExistingVariables(observer);
        jep.getSymbolTable().addObserver(observer);

        // import current script context's engine bindings 
        // as variables/functions to JEP
        importEngineBindings(jep, ctx);
        // save JEP object in this script context for future 
        ctx.setAttribute(JEP_MODE, jep, ScriptContext.ENGINE_SCOPE);

        return jep;     
    }

    private void importEngineBindings(JEP jep, ScriptContext ctx) {
        Bindings bindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        for (String key : bindings.keySet()) {
            Object value = bindings.get(key);
            /*
             * If the value is a PostfixMathCommandI or Method or
             * Constructor or Invocable, we expose the same as JEP
             * function. Everything else is exposed as variable.
             */

            if (value instanceof PostfixMathCommandI) {
                jep.addFunction(key, (PostfixMathCommandI)value);
            } else if (value instanceof Method) {
                Method m = (Method) value;
                jep.addFunction(key, new MethodCommand(m));
            } else if (value instanceof Constructor) {
                Constructor c = (Constructor) value;
                jep.addFunction(key, new ConstructorCommand(c));
            } else if (value instanceof Invocable) {
                jep.addFunction(key, new InvocableCommand((Invocable)value, key));
            } else {
                Variable var = jep.getVar(key);
                if (var == null || !var.isConstant()) {
                    jep.addVariable(key, bindings.get(key));
                }
            }
        }      
    }

    // reads all contents of a given Reader
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
} 
