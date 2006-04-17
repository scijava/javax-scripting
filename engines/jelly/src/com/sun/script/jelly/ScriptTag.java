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
 * ScriptTag.java
 * @author A. Sundararajan
 */

package com.sun.script.jelly;

import org.apache.commons.jelly.JellyTagException;
import org.apache.commons.jelly.LocationAware;
import org.apache.commons.jelly.MissingAttributeException;
import org.apache.commons.jelly.TagSupport;
import org.apache.commons.jelly.XMLOutput;

import java.io.*;
import javax.script.*;

/**
 * A tag which evaluates its body using the current scripting language
 */
public class ScriptTag extends TagSupport implements LocationAware {
    private ScriptEngine engine;
    private String src;
    private String language;

    private String elementName;
    private String fileName;
    private int columnNumber;
    private int lineNumber;

    public ScriptTag(ScriptEngine engine) {
        this.engine = engine;
    }

    // Tag interface
    public void doTag(XMLOutput output) throws MissingAttributeException, JellyTagException {
        String code = null;
        Reader reader = null;

        if (src != null && !src.equals("")) {
            reader = new InputStreamReader(context.getResourceAsStream(src));
        } else {
            code = getBodyText();
            if (code.equals("")) return;
        }

        ScriptJellyContext sjc = (ScriptJellyContext)context;
        ScriptContext ctx = sjc.getScriptContext();
        sjc.setScriptEngine(engine);
        if (fileName != null) {
            ctx.setAttribute(ScriptEngine.FILENAME, fileName, 
                             ScriptContext.ENGINE_SCOPE);
        }      
        try {
            if (code != null) {
                engine.eval(code, ctx);
            } else {
                engine.eval(reader, ctx);
            }
        } catch (ScriptException e) {
           throw new JellyTagException("Error occurred with script: " + e, e);    
        }
    }       
   
    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public String getElementName() {
        return elementName;
    }
    
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
}
