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
package com.sun.script.jruby;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * Alternative class to javax.script.ScriptEngineManager to make scripting API work on
 * JDK 1.5. Usage is exactly the same as the one of javax.script.ScriptEngineMaager.
 * 
 * @author Yoko Harada
 */
public class JRubyScriptEngineManager {

    private final ClassLoader loader;
    private final String serviceName = "META-INF/services/javax.script.ScriptEngineFactory";
    private HashSet<ScriptEngineFactory> lookupedFactories;
    private final HashMap<String, ScriptEngine> nameMap =
            new HashMap<String, ScriptEngine>();
    private final HashMap<String, ScriptEngine> extensionMap =
            new HashMap<String, ScriptEngine>();
    private final HashMap<String, ScriptEngine> mimeTypeMap =
            new HashMap<String, ScriptEngine>();
    private Bindings globalScope = new SimpleBindings();

    public JRubyScriptEngineManager() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public JRubyScriptEngineManager(final ClassLoader loader) {
        this.loader = loader;
        init();
    }

    private void init() {
        lookupedFactories = ScriptEngineFactoryLookup.lookup(loader, serviceName);
        for (ScriptEngineFactory factory : lookupedFactories) {
            registerEngineNames(factory);
            registerEngineExtenstions(factory);
            registerEngineMimeTypes(factory);
        }
    }

    private void registerEngineExtenstions(ScriptEngineFactory factory) {
        List<String> extensions = factory.getExtensions();
        for (String extension : extensions) {
            extensionMap.put(extension, factory.getScriptEngine());
        }
    }

    private void registerEngineMimeTypes(ScriptEngineFactory factory) {
        List<String> mimeTypes = factory.getMimeTypes();
        for (String mimeType : mimeTypes) {
            mimeTypeMap.put(mimeType, factory.getScriptEngine());
        }
    }

    private void registerEngineNames(ScriptEngineFactory factory) {
        List<String> names = factory.getNames();
        for (String name : names) {
            nameMap.put(name, factory.getScriptEngine());
        }
    }

    public void setBindings(Bindings bindings) {
        globalScope = bindings;
    }

    public Bindings getBindings() {
        return globalScope;
    }

    public void put(String key, Object value) {
        globalScope.put(key, value);
    }

    public Object get(String key) {
        return globalScope.get(key);
    }

    public ScriptEngine getEngineByName(String shortName) {
        ScriptEngine engine = nameMap.get(shortName);
        if (engine != null) {
            return engine;
        } else {
            throw new IllegalArgumentException("no engine registered for: " + shortName);
        }
    }

    public ScriptEngine getEngineByExtension(String extension) {
        ScriptEngine engine = extensionMap.get(extension);
        if (engine != null) {
            return engine;
        } else {
            throw new IllegalArgumentException("no engine registered for: " + extension);
        }
    }

    public ScriptEngine getEngineByMimeType(String mimeType) {
        ScriptEngine engine = mimeTypeMap.get(mimeType);
        if (engine != null) {
            return engine;
        } else {
            throw new IllegalArgumentException("no engine registered for: " + mimeType);
        }
    }

    public List<ScriptEngineFactory> getEngineFactories() {
        return (List<ScriptEngineFactory>) new ArrayList(lookupedFactories);
    }

    public void resigterEngineName(String name, ScriptEngineFactory factory) {
        if ((name != null) && (factory != null)) {
            nameMap.put(name, factory.getScriptEngine());
        }
    }

    public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
        if ((type != null) && (factory != null)) {
            mimeTypeMap.put(type, factory.getScriptEngine());
        }
    }

    public void registerEngineExtension(String extension,
            ScriptEngineFactory factory) {
        if ((extension != null) && (factory != null)) {
            extensionMap.put(extension, factory.getScriptEngine());
        }
    }
}

class ScriptEngineFactoryLookup {

    private static final boolean DEBUG = false;

    static HashSet<ScriptEngineFactory> lookup(ClassLoader loader, String name) {
        HashSet<ScriptEngineFactory> factories = new HashSet<ScriptEngineFactory>();
        try {
            Enumeration<URL> urls = loader.getResources(name);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    if ((line = trim(line)) != null) {
                        try {
                            Class<ScriptEngineFactory> clazz =
                                    (Class<ScriptEngineFactory>) Class.forName(line, true, loader);
                            ScriptEngineFactory factory = clazz.newInstance();
                            factories.add(factory);
                        } catch (java.lang.UnsupportedClassVersionError error) {
                            if (DEBUG) {
                                System.err.println(line + ": version mismatch - ignore");
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new ScriptException(ex);
        } finally {
            return factories;
        }
    }

    private static String trim(String line) {
        if (line.startsWith("#")) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(line, "#");
        return ((String) st.nextElement()).trim();
    }
}

