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
 * FreeMarkerScriptEngine.java
 * @author A. Sundararajan
 */

package com.sun.script.freemarker;

import javax.script.*;
import java.io.*;
import java.util.Properties;
import java.util.Set;
import freemarker.template.*;

public class FreeMarkerScriptEngine extends AbstractScriptEngine {

    public static final String STRING_OUTPUT_MODE = "com.sun.script.freemarker.stringOut";
    public static final String FREEMARKER_CONFIG = "com.sun.script.freemarker.config";
    public static final String FREEMARKER_PROPERTIES = "com.sun.script.freemarker.properties";
    public static final String FREEMARKER_TEMPLATE_DIR = "com.sun.script.freemarker.template.dir";

    // my factory, may be null
    private volatile ScriptEngineFactory factory;
    private volatile Configuration conf;

    public FreeMarkerScriptEngine(ScriptEngineFactory factory) {
        this.factory = factory;
    }   

    public FreeMarkerScriptEngine() {
        this(null);
    }
	
    // ScriptEngine methods
    public Object eval(String str, ScriptContext ctx) 
                       throws ScriptException {	
        return eval(new StringReader(str), ctx);
    }

    public Object eval(Reader reader, ScriptContext ctx)
                       throws ScriptException { 
        ctx.setAttribute("context", ctx, ScriptContext.ENGINE_SCOPE);
        initFreeMarkerConfiguration(ctx);
        String fileName = getFilename(ctx);
        boolean outputAsString = isStringOutputMode(ctx);
        Writer out;
        if (outputAsString) {
            out = new StringWriter();
        } else {
            out = ctx.getWriter();
        }
        Bindings engineScope = ctx.getBindings(ScriptContext.ENGINE_SCOPE);

        try {
            Template template = new Template(fileName, reader, conf);
            template.process(engineScope, out);
            out.flush();
        } catch (Exception exp) {
            throw new ScriptException(exp);
        }
        return outputAsString? out.toString() : null;
    }

    public ScriptEngineFactory getFactory() {
        if (factory == null) {
            synchronized (this) {
	          if (factory == null) {
	              factory = new FreeMarkerScriptEngineFactory();
	          }
            }
        }
	  return factory;
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    // internals only below this point  
    private static String getFilename(ScriptContext ctx) {
        Object fileName = ctx.getAttribute(ScriptEngine.FILENAME);
        return fileName != null? fileName.toString() : "<unknown>";
    }

    private static boolean isStringOutputMode(ScriptContext ctx) {
        Object flag = ctx.getAttribute(STRING_OUTPUT_MODE);
        if (flag != null) {
            return flag.equals(Boolean.TRUE);
        } else {
            return false;
        }
    }

    private void initFreeMarkerConfiguration(ScriptContext ctx) {
        if (conf == null) {
            synchronized (this) {
                if (conf != null) {
                    return;
                }
                Object cfg = ctx.getAttribute(FREEMARKER_CONFIG);
                if (cfg instanceof Configuration) {
                    conf = (Configuration) cfg;
                    return;
                }

                Configuration tmpConf = new Configuration();
                try {
                    initConfProps(tmpConf, ctx);
                    initTemplateDir(tmpConf, ctx);
                } catch (RuntimeException rexp) {
                    throw rexp;
                } catch (Exception exp) {
                    throw new RuntimeException(exp);
                }
                conf = tmpConf;
            }
        }
    }    

    private static void initConfProps(Configuration conf, ScriptContext ctx) {         
        try {
            Properties props = null;
            Object tmp = ctx.getAttribute(FREEMARKER_PROPERTIES);
            if (props instanceof Properties) {
                props = (Properties) tmp;
            } else {
                String propsName = System.getProperty(FREEMARKER_PROPERTIES);
                if (propsName != null) {                    
                    File propsFile = new File(propsName);
                    if (propsFile.exists() && propsFile.canRead()) {
                        props = new Properties();
                        props.load(new FileReader(propsFile));
                    }               
                }
            }
            if (props != null) {
                Set<String> keys = props.stringPropertyNames();
                for (String key : keys) {
                    try {
                        conf.setSetting(key, props.get(key).toString());
                    } catch (TemplateException te) {
                        // ignore
                    }
                }
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception exp) {
            throw new RuntimeException(exp);
        }
    }

    private static void initTemplateDir(Configuration conf, ScriptContext ctx) {
        try {
            Object tmp = ctx.getAttribute(FREEMARKER_TEMPLATE_DIR);
            String dirName;
            if (tmp != null) {
                dirName = tmp.toString();
            } else {
                tmp = System.getProperty(FREEMARKER_TEMPLATE_DIR);
                dirName = (tmp == null)? "." : tmp.toString();
            }
            File dir = new File(dirName);
            if (dir.exists() && dir.isDirectory()) {
                conf.setDirectoryForTemplateLoading(dir);
            }
        } catch (IOException exp) {
            throw new RuntimeException(exp);
        }
    }
}
