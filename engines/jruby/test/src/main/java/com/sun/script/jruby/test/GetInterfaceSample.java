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
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * GetInterfaceSample.java
 * @author Yoko Harada
 */
public class GetInterfaceSample {
    GetInterfaceSample() throws ScriptException, FileNotFoundException {
        String basedir = System.getProperty("basedir");
        ScriptEngineManager manager = new ScriptEngineManager();
        //JRubyScriptEngineManager manager = new JRubyScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("rb");
        Invocable invocable = (Invocable) engine;

        //invoke ruby's top-level methods implementing Java interface
        engine.eval(new FileReader(basedir+"/src/main/ruby/temperature.rb"));
        TempConversion convertor = invocable.getInterface(TempConversion.class);
        double f = 32.0;
        double c = 0.0;
        System.out.println(f + " Fahrenheit = " +
                convertor.get_c_from_f(f) + " Celsius");
        System.out.println(c + " Celsius = " +
                convertor.get_f_from_c(c) + " Fahrenheit");

        //invoke ruby's instance methods implementing Java interface
        Object obj = engine.eval(new FileReader(basedir+"/src/main/ruby/distance.rb"));
        DistConversion convertor2 =
                invocable.getInterface(obj, DistConversion.class);
        double k = 45.0;
        double m = 45.0;
        System.out.println(k + " km = " +
                convertor2.get_mi_from_km(k) + " mi");
        System.out.println(m + " mi = " +
                convertor2.get_km_from_mi(m) + " km");
    }

    public static void main(String[] args) throws FileNotFoundException, ScriptException {
        new GetInterfaceSample();
    }
}
