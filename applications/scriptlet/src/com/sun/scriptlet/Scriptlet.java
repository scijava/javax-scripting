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
package com.sun.scriptlet;

import java.awt.Graphics;
import java.io.*;
import java.net.URL;
import javax.script.*;

/**
 * A Scriplet is an applet written in JavaScript. HTML/JavaScript authors can 
 * use this applet in the page and define applet methods such as init, start
 * etc. as JavaScript functions or methods of a script object.
 *
 * @author A. Sundararajan
 */
public class Scriptlet extends javax.swing.JApplet {
    private ScriptContext mycontext;
    // script object for this applet
    private Object appletObj;
    
    private static final String SCRIPT = "script";
    private static final String SCRIPTSRC = "scriptsrc";
    private static final String ID = "id";
    private static final String SHAREDCONTEXT = "sharedcontext";

    @Override public void init() {
        boolean sharedContext = Boolean.parseBoolean(getParameter(SHAREDCONTEXT)); 
        mycontext = sharedContext ? jsengine.getContext()
                                  : new SimpleScriptContext();
        mycontext.setAttribute("engine", jsengine, ScriptContext.ENGINE_SCOPE);
        evalScript();
        invoke("init");
    }
    
    @Override public void start() {
        invoke("start");
    }    
    
    @Override public void stop() {
        invoke("stop");
    }
    
    @Override public void destroy() {
        invoke("destroy");
    }
    
    @Override public void paint(Graphics g) {
        invoke("paint", new Object[] { this, g });
    }
    
    @Override public String getAppletInfo() {
        Object res = invoke("getAppletInfo");
        return (res != null)? res.toString() : null;
    }
    
    @Override public String[][] getParameterInfo() {
        Object res = invoke("getParameterInfo");
        if (res instanceof String[][]) {
            return (String[][]) res;
        } else {
            return null;
        }
    }
    
    // - Internals only below this point
    
    private static ScriptEngine jsengine;
    static {
        ScriptEngineManager manager = new ScriptEngineManager();
        jsengine = manager.getEngineByName("JavaScript");        
    }  
    
    private void evalScript() {        
        try {
            String script = getParameter(SCRIPT);
            loadInitScript();
            if (script != null) {                
                jsengine.eval(script, mycontext);
            } else {
                String scriptSrc = getParameter(SCRIPTSRC);
                if (scriptSrc != null) {
                    URL u = new URL(getCodeBase(), scriptSrc);                   
                    InputStream is = u.openStream();
                    Reader r = new InputStreamReader(is);
                    jsengine.eval(r, mycontext);                            
                }
            }          
            String appletId = getParameter(ID);
            if (appletId != null) {
                appletObj = mycontext.getAttribute(appletId);
            } else {
                appletObj = jsengine.eval("this", mycontext);
            }
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }
    
    private Object invoke(String name) {
        return invoke(name, new Object[] { this });
    }
    
    private Object invoke(String name, Object[] args) {
        try {
            Invocable invocable = (Invocable)jsengine;
            return invocable.invokeMethod(appletObj, name, args);
        } catch (ScriptException sexp) {
            throw new RuntimeException(sexp);
        } catch (NoSuchMethodException nexp) {
            return null;
        }
    }

    private static final String INIT_SRC = "/resources/scriptlet.init.js";

    private void loadInitScript() 
        throws ScriptException, NoSuchMethodException {
        InputStream is = Scriptlet.class.getResourceAsStream(INIT_SRC);  
        jsengine.eval(new InputStreamReader(is), mycontext);
        Object contextThis = jsengine.eval("this", mycontext);
        Object res = ((Invocable)jsengine).invokeMethod(contextThis, 
                          "wrapJSWindow", 
                          new Object[] { this });
        mycontext.setAttribute("window", res, ScriptContext.ENGINE_SCOPE);
    }
}
