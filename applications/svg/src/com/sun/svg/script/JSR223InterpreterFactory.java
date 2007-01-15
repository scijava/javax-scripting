/*
 * Copyright (C) 2007 Sun Microsystems, Inc. All rights reserved. 
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
package com.sun.svg.script;

import java.net.URL;
import java.util.List;
import javax.script.*;
import org.apache.batik.script.*;

/**
 * This is Batik interpreter factory implementation that wraps
 * a jsr-223 ScriptEngineFactory.
 *
 * @author A. Sundararajan
 */
public class JSR223InterpreterFactory implements InterpreterFactory {
    private static final String MIME_PREFIX = "x-script/";
    private static final String JAVASCRIPT = MIME_PREFIX + "javascript";
    private ScriptEngineFactory factory;
    private String mimeType = JAVASCRIPT;

    private static class MyPool extends InterpreterPool {
        private static ScriptEngineFactory jsFactory;
        static {
            ScriptEngineManager manager = new ScriptEngineManager();
            List<ScriptEngineFactory> factories = manager.getEngineFactories();       
            for (ScriptEngineFactory fac : factories) {
                for (String mimeType: fac.getMimeTypes()) {
                    InterpreterFactory interpFac = new JSR223InterpreterFactory(fac, mimeType);
                    defaultFactories.put(mimeType, interpFac);
                }
                String langName = fac.getLanguageName();
                String mime = MIME_PREFIX + langName;
                if (langName.equals("ECMAScript") ||
                    langName.equals("ecmascript") ||                    
                    langName.equals("JavaScript") ||
                    langName.equals("javascript")) {
                    jsFactory = fac;
                }
                InterpreterFactory interpFac = new JSR223InterpreterFactory(fac, mime);
                defaultFactories.put(mime, interpFac);
            }
        }
        static ScriptEngineFactory getJSFactory() {
            return jsFactory;
        }
    }

    public JSR223InterpreterFactory() {
        mimeType = JAVASCRIPT; 
        factory = MyPool.getJSFactory();
    }

    public JSR223InterpreterFactory(ScriptEngineFactory factory,
        String mimeType) {
        this.factory = factory;
        this.mimeType = mimeType;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public Interpreter createInterpreter(URL documentURL) {
        ScriptEngine engine = factory.getScriptEngine();
        engine.put("documentURL", documentURL);
        Interpreter res = new JSR223Interpreter(engine);
        return res;
    }
}
