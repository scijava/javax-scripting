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
 * ScriptTagLibrary.java
 * @author A. Sundararajan
 */

package com.sun.script.jelly;

import java.util.Map;
import java.util.HashMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.apache.commons.jelly.JellyException;
import org.apache.commons.jelly.Tag;
import org.apache.commons.jelly.expression.ExpressionFactory;
import org.apache.commons.jelly.impl.TagFactory;
import org.apache.commons.jelly.tags.core.CoreTagLibrary;
import org.xml.sax.Attributes;

public class ScriptTagLibrary extends CoreTagLibrary {
    private static final String DEFAULT_LANGUAGE = "JavaScript";
    private ScriptExpressionFactory expressionFactory;
    private ScriptEngineManager manager;
    private Map<String, ScriptEngine> engines;

    public ScriptTagLibrary() {
        manager = new ScriptEngineManager();
        expressionFactory = new ScriptExpressionFactory(this);     
        engines = new HashMap<String, ScriptEngine>();
        registerTagFactory(
            "script",
            new TagFactory() {
                public Tag createTag(String name, Attributes attributes)
                    throws JellyException {
                    return createScriptTag(name, attributes);
                }
            });
    }   

    ScriptEngine getDefaultScriptEngine() {
        return getScriptEngine(DEFAULT_LANGUAGE);
    }

    private synchronized ScriptEngine getScriptEngine(String lang) {
        ScriptEngine e = engines.get(lang);
        if (e == null) {
            e = manager.getEngineByName(lang);
            engines.put(lang, e);
        }
        return e;
    }

    protected ExpressionFactory getExpressionFactory() {
        return expressionFactory;
    }

    // Factory method to create a new ScriptTag with a ScriptEngine     
    protected Tag createScriptTag(String name, Attributes attributes) throws JellyException {
        String lang = attributes.getValue("", "language");
        if (lang == null || lang.equals("")) {
            lang = DEFAULT_LANGUAGE;
        }
        ScriptEngine engine = getScriptEngine(lang);
        if (engine == null) {
            throw new JellyException("language " + lang + " not yet supported");
        }
        return new ScriptTag(engine);
    }
}
