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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

/**
 * EvalSample.java
 * @author Yoko Harada
 */
public class EvalSample {

    EvalSample() throws UnsupportedEncodingException, ScriptException, FileNotFoundException {
        String basedir = System.getProperty("basedir");
        ScriptEngineManager manager = new ScriptEngineManager();
        //JRubyScriptEngineManager manager = new JRubyScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jruby");

        List<String> people = new ArrayList<String>();

        //give global varibales to runime by context in the super class
        engine.put("who", new String("Ichiro"));
        people.add("Julia");
        people.add("Johnny");
        engine.put("people", people);
        engine.eval(new FileReader(basedir + "/src/main/ruby/greetings.rb"));

        //give global variables to runtime by Bindings
        Bindings bindings = engine.createBindings();
        bindings.put("who", new String("Harry"));
        people.add("Keira");
        people.add("Keith");
        bindings.put("people", people);
        engine.eval(new FileReader(basedir + "/src/main/ruby/greetings.rb"), bindings);

        //give global varibales to runtime by context created
        SimpleScriptContext context = new SimpleScriptContext();
        context.setAttribute("who", new String("George"), ScriptContext.ENGINE_SCOPE);
        people.add("Luke");
        people.add("Lindsay");
        context.setAttribute("people", people, ScriptContext.ENGINE_SCOPE);
        engine.eval(new FileReader(basedir + "/src/main/ruby/greetings.rb"), context);

        String script = "puts \"Good evening, #{$who}.\";" +
                "print \"Hello\", $people;" +
                "print \"#{$people.size + 1} in total.\"";

        //give global varibales to runime by context in the super class
        engine.put("who", new String("Fred"));
        people.add("Matt");
        people.add("Mariah");
        engine.eval(new String(script));

        //give global variables to runtime by Bindings
        bindings.put("who", new String("Elliott"));
        people.add("Nicole");
        people.add("Nicolas");
        engine.eval(new String(script), bindings);

        //give global varibales to runtime by context created
        context.setAttribute("who", new String("Drew"), ScriptContext.ENGINE_SCOPE);
        people.add("Olivia");
        people.add("Orlando");
        engine.eval(new String(script), context);
    }

    public static void main(String[] args)
        throws FileNotFoundException, ScriptException, UnsupportedEncodingException {
        new EvalSample();
    }
}
