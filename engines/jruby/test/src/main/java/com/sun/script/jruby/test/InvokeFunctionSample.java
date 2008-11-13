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

package com.sun.script.jruby.test;

import com.sun.script.jruby.JRubyScriptEngineManager;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * InvoleFunctionSample.java
 * @author Yoko Harada
 */
public class InvokeFunctionSample {

    public static void main(String[] args) throws ScriptException, NoSuchMethodException, FileNotFoundException {
        String basedir = System.getProperty("basedir");
        ScriptEngineManager manager = new ScriptEngineManager();
        //JRubyScriptEngineManager manager = new JRubyScriptEngineManager();
        ScriptEngine engine = null;
        for (ScriptEngineFactory factory : manager.getEngineFactories()) {
            if ("ruby".equals(factory.getLanguageName())) {
                engine = factory.getScriptEngine();
                break;
            }
        }

        engine.eval("def hello(to) print 'Hello, ', to, '\n' end");
        Invocable invocable = (Invocable) engine;
        
        //Methods compiled("parsed" in ruby) previously can be invoked more than once.
        invocable.invokeFunction("hello", new Object[]{"Santa Claus"});
        invocable.invokeFunction("hello", new Object[]{"St. Valentine"});
        invocable.invokeFunction("hello", new Object[]{"St. Patrick"});
        
        //If a method to be invoked doesn't have any argument,
        //give an empty array of Object.
        engine.eval("def say() puts \"Don\'t drink too much!\" end");
        invocable.invokeFunction("say", new Object[]{});
        
        //give global variables to a method by context
        Object[] list = {"Scarecrow", "Pumpkin", "Witch"};
        engine.put("list", list);
        engine.eval(new FileReader(basedir+"/src/main/ruby/say2all.rb"));
        invocable.invokeFunction("say_to_all", new Object[]{});
        
        //give a global variable and an arugment to be used in the method
        engine.put("message", new String("trick or treat"));
        engine.eval(new FileReader(basedir+"/src/main/ruby/manytimes.rb"));
        invocable.invokeFunction("many_times", new Object[]{4});
    }
}
