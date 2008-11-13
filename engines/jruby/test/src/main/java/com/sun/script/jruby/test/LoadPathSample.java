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
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * LoadPathSample.java
 * @author Yoko Harada
 */
public class LoadPathSample {

    public static void main(String[] args) throws FileNotFoundException, ScriptException {
        String jrubyhome = System.getProperty("jruby.home");
        String separator = System.getProperty("path.separator");
        String classpath = System.getProperty("java.class.path");
        classpath = classpath + separator +
                jrubyhome + separator +
                jrubyhome + "/lib/ruby/1.8";
        System.setProperty("java.class.path", classpath);
        ScriptEngineManager manager = new ScriptEngineManager();
        //JRubyScriptEngineManager manager = new JRubyScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("rb");
        
        /*
         * When ScriptEngine.FILENAME property is set,
         * org.jruby.Ruby#parseFile method compiles the specified file.
         * If not, org.jruby.Ruby#parseEval does after reading a file.
         * A big ruby file had better to have this property.
         */
        String testname = jrubyhome + "/test/testString.rb";
        engine.put(ScriptEngine.FILENAME, testname);
        engine.eval(new FileReader(testname));

        testname = jrubyhome + "/test/testCornerCases.rb";
        engine.put(ScriptEngine.FILENAME, testname);
        engine.eval(new FileReader(testname));

        String script = "require 'pstore'\n" +
                    "db = PStore.new('/tmp/foo')\n" +
                    "db.transaction do\n" +
                    "  array = db['foo'] = ['a', 'b', 'c', 'd'];" +
                    "  array[1] = ['ab', 'bc'];" +
                    "end\n" +
                    "db.transaction(true) do\n" +
                    "  p db['foo'];" +
                    "end";
        engine.eval(script);
    }
}
