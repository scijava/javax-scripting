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
 * XSLTScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.xslt;

import javax.script.*;
import java.io.*;
import java.net.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

public class XSLTScriptEngine extends AbstractScriptEngine 
        implements Compilable { 

    // pre-defined variable names
    public static final String SOURCE = "com.sun.script.xslt.source";
    public static final String RESULT = "com.sun.script.xslt.result";
    public static final String URI_RESOLVER = "com.sun.script.xslt.URIResolver";
    public static final String SYSTEM_ID = "com.sun.script.xslt.systemId";

    // my factory, may be null
    private ScriptEngineFactory factory;

    // my implementation for CompiledScript
    private class XSLTCompiledScript extends CompiledScript {
        // my compiled code
        private Templates templates;

        XSLTCompiledScript (Templates templates) {
            this.templates = templates;
        }

        public ScriptEngine getEngine() {
            return XSLTScriptEngine.this;
        }

        public synchronized Object eval(ScriptContext ctx) throws ScriptException {
            ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);           
            ctx.setAttribute("engine", this, ScriptContext.ENGINE_SCOPE);
            Transformer trans;
            try {
                trans = templates.newTransformer();
            } catch (Exception exp) {
                throw new ScriptException(exp);
            }
            return evalTransform(trans, ctx);
        }
    }

    // Compilable methods
    public CompiledScript compile(String script) 
                                  throws ScriptException {  
        return compile(new StringReader(script));
    }

    public CompiledScript compile (Reader reader) 
                                  throws ScriptException {  
        Templates templates = compileTransform(reader, context);
        return new XSLTCompiledScript(templates);
    }

    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	        
        return eval(new StringReader(str), ctx);
    }

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        return evalTransform(reader, ctx);
    }

    public ScriptEngineFactory getFactory() {
	synchronized (this) {
	    if (factory == null) {
	    	factory = new XSLTScriptEngineFactory();
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

    private static class ContextErrorListener implements ErrorListener {
        private PrintWriter err;                    

        ContextErrorListener(ScriptContext ctx) {
            err = new PrintWriter(ctx.getErrorWriter());
        }

        public void error(TransformerException exception)
                          throws TransformerException {
            err.println("ERROR: " + exception.getMessage());
            throw exception;
        }

        public void fatalError(TransformerException exception)
                               throws TransformerException {
            err.println("FATAL ERROR: " + exception.getMessage());
            throw exception;
        }

        public void warning(TransformerException exception)
                            throws TransformerException {
            err.println("WARNING: " + exception.getMessage());
            throw exception;
        }
    }

    private Templates compileTransform(Reader reader, final ScriptContext ctx) 
                                          throws ScriptException {
        try {
            ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
            ctx.setAttribute("engine", this, ScriptContext.ENGINE_SCOPE);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            transFactory.setErrorListener(new ContextErrorListener(ctx));
            transFactory.setURIResolver(getURIResolver(ctx));
            return transFactory.newTemplates(new StreamSource(reader));           
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
    }

    private Object evalTransform(Reader reader, ScriptContext ctx)
                                 throws ScriptException {
        Transformer trans;
        try {
            ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);           
            ctx.setAttribute("engine", this, ScriptContext.ENGINE_SCOPE);
            TransformerFactory transFactory = TransformerFactory.newInstance();
            transFactory.setErrorListener(new ContextErrorListener(ctx));
            transFactory.setURIResolver(getURIResolver(ctx));
            trans = transFactory.newTransformer(new StreamSource(reader));
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
        return evalTransform(trans, ctx); 
    }

    private Object evalTransform(Transformer transformer, ScriptContext ctx)
                                 throws ScriptException {
        try {
            transformer.clearParameters();
            transformer.setParameter("context", ctx);
            transformer.setErrorListener(new ContextErrorListener(ctx));
            transformer.setURIResolver(getURIResolver(ctx));
            transformer.transform(getSource(ctx), getResult(ctx));
            return null;
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }        
    }

    private static URIResolver getURIResolver(final ScriptContext ctx) {
        Object uriResolver = ctx.getAttribute(URI_RESOLVER);
        if (uriResolver instanceof URIResolver) {
            return (URIResolver) uriResolver;
        } else {
            return new URIResolver() {
                public Source resolve(String href, String base)
                                      throws TransformerException {
                    try {
                        URL url = new URL(href);
                        URLConnection conn = url.openConnection();
                        return new StreamSource(conn.getInputStream());
                    } catch(Exception exp) {
                    }

                    // try relative URL
                    try {
                        URL context = new URL(base);
                        URL url = new URL(context, href);
                        URLConnection conn = url.openConnection();
                        return new StreamSource(conn.getInputStream());
                    } catch (Exception exp) {
                    }

                    // let the engine resolve it...
                    return null;
                }
            };
        }
    }

    private static Source getSource(ScriptContext ctx) throws ScriptException {
        Object obj = ctx.getAttribute(SOURCE);
        Source src;
        if (obj instanceof Source) {
            src = (Source) obj;
        } else if (obj instanceof String) {
            src = new StreamSource((String)obj);
            src.setSystemId((String)obj);
        } else if (obj instanceof File) {
            String sysId = ((File)obj).toURI().toString();
            src = new StreamSource((File)obj);
            src.setSystemId(sysId);
        } else if (obj instanceof InputStream) {
            src = new StreamSource((InputStream)obj);
        } else if (obj instanceof Reader) {
            src = new StreamSource((Reader)obj);
        } else {
            src = new StreamSource(ctx.getReader());
        }
        
        Object systemId = ctx.getAttribute(SYSTEM_ID);
        if (systemId instanceof String) {
            src.setSystemId((String)systemId);
        }
        return src;
    }

    private static Result getResult(ScriptContext ctx) {
        Object result = ctx.getAttribute(RESULT);
        if (result instanceof Result) {
            return (Result) result;
        } else if (result instanceof String) {
            return new StreamResult((String)result);
        } else if (result instanceof File) {
            return new StreamResult((File)result);        
        } else if (result instanceof OutputStream) {
            return new StreamResult((OutputStream)result);
        } else if (result instanceof Writer) {
            return new StreamResult((Writer)result);
        } else {
            return new StreamResult(ctx.getWriter());
        }
    }

}
