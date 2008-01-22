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

import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * InvokeMethodTest.java
 * @author Yoko Harada
 */
public class InvokeMethodTest {
    public static void main(String[] args) throws FileNotFoundException, ScriptException, NoSuchMethodException {
        String basedir = System.getProperty("base.dir");
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("jruby");
        
        engine.eval(new FileReader(basedir+"/src/main/ruby/flowers.rb"));
        //get two instances as a global variable
        Object red = engine.get("red");
        Object white = engine.get("white");
        
        //invoke methods defined in the Flower class
        Invocable invocable = (Invocable) engine;
        invocable.invokeMethod(red, "comment", new Object[]{});
        invocable.invokeMethod(white, "comment", new Object[]{});
        invocable.invokeMethod(red, "others", new Object[]{1});
        invocable.invokeMethod(white, "others", new Object[]{2});
    }
}
