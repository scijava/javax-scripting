package com.sun.svg.script;

import java.net.URL;
import java.util.List;
import javax.script.*;
import org.apache.batik.script.*;

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