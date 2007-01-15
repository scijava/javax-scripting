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

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import javax.script.*;
import org.apache.batik.script.*;

/**
 * This is Batik interpreter implemenation that wraps a jsr-223
 * ScriptEngine for actual script evaluation.
 *
 * @author A. Sundararajan
 */
public class JSR223Interpreter implements Interpreter {
    private ScriptEngine engine;

    public JSR223Interpreter(ScriptEngine engine) {
        this.engine = engine;
    }

    @Override
    public Object evaluate(Reader reader, String description)
        throws InterpreterException, IOException {
        engine.put(ScriptEngine.FILENAME, description);
        return evaluate(reader);
    }

    @Override
    public Object evaluate(Reader reader)
        throws InterpreterException, IOException {
        try {
            return engine.eval(reader);
        } catch (ScriptException sexp) {
            throw newInterpreterException(sexp);
        }
    }

    @Override
    public Object evaluate(String script)
        throws InterpreterException {
        try {
            return engine.eval(script);
        } catch (ScriptException sexp) {
            throw newInterpreterException(sexp);
        }
    }

    @Override
    public void bindObject(String name, Object object) {
        engine.put(name, object);
    }

    @Override
    public void setOut(Writer writer) {
        engine.getContext().setWriter(writer);
    }

    @Override
    public void dispose() {
        engine = null;
    }

    @Override
    public void setLocale(Locale l) {
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public String formatMessage(String key, Object[] args) {
        return null;
    }

    private InterpreterException newInterpreterException(ScriptException sexp) {
        InterpreterException iexp = new InterpreterException(sexp.toString(),
            sexp.getLineNumber(), -1);
        iexp.initCause(sexp);
        return iexp;
    }
}
