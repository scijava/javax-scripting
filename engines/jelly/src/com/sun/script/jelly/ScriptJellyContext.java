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
 * ScriptJellyContext.java
 * @author A. Sundararajan
 */

package com.sun.script.jelly;

import java.io.*;
import java.net.*; 
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import org.apache.commons.jelly.JellyContext;

public class ScriptJellyContext extends JellyContext {
    private ScriptContext ctx;
    private ScriptEngine engine;

    ScriptJellyContext(JellyContext parent, ScriptContext ctx) {
        setScriptContext(ctx);
        setParent(parent);
        setInherit(false);
        setExportLibraries(false);
    }

    public synchronized ScriptContext getScriptContext() {
        return ctx;
    }

    public synchronized void setScriptContext(ScriptContext ctx) {
        this.ctx = ctx;
        setContextURLs();
    }

    public synchronized ScriptEngine getScriptEngine() {
        return engine;
    }

    public synchronized void setScriptEngine(ScriptEngine engine) {
        this.engine = engine;
    }

    private void setContextURLs() {
        URL rootURL = null, currentURL = null;
        Object root = ctx.getAttribute(JellyScriptEngine.ROOT_URL, ScriptContext.ENGINE_SCOPE);
        if (root instanceof URL) {
            rootURL = (URL) root;
        } else {
            String filename = (String) ctx.getAttribute(JellyScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
            if (filename != null) {
                try {
                    rootURL = new File(filename).toURL();
                } catch (MalformedURLException mue) {}
            }    
        }
        Object current = ctx.getAttribute(JellyScriptEngine.CURRENT_URL, ScriptContext.ENGINE_SCOPE);
        if (current instanceof URL) {
            currentURL = (URL) current;
        } else {
            currentURL = rootURL;
        }
        if (rootURL != null) {
            setRootURL(rootURL);
            setCurrentURL(currentURL);
        }
    }

    public Object getVariable(String name) {
        synchronized (ctx) {
            int scope = ctx.getAttributesScope(name);
            if (scope != -1) {
                return ctx.getAttribute(name, scope);
            }
        }
        return super.getVariable(name);                   
    }

    // FIXME: what is the difference b/w getVariable & this method?
    public Object findVariable(String name) {
        synchronized (ctx) {
            int scope = ctx.getAttributesScope(name);
            if (scope != -1) {
                return ctx.getAttribute(name, scope);
            }
        }
        return super.findVariable(name);
    }

    public void setVariable(String name, Object value) {
        synchronized (ctx) {
            int scope = ctx.getAttributesScope(name);
            if (scope == -1) {
                scope = ScriptContext.ENGINE_SCOPE;
            }
            ctx.setAttribute(name, value, scope);
        }
    }

    public void removeVariable(String name) {
        synchronized (ctx) {
            int scope = ctx.getAttributesScope(name);
            if (scope != -1) {
                ctx.removeAttribute(name, scope);
                return;
            }
        }
        super.removeVariable(name);
    }
}
