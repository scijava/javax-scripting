package com.sun.svg.script;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import javax.script.*;
import org.apache.batik.script.*;

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