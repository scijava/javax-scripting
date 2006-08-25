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

package com.sun.script.jep;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Stack;
import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

/*
 * MethodCommand.java
 *
 * This class wraps a Java Method as a function for JEP.
 *
 * @author A. Sundararajan
 */
public class MethodCommand extends PostfixMathCommand {
    private Method method;
    private boolean isStatic;

    public MethodCommand(Method method) {
        this.method = method;
        numberOfParameters = method.getParameterTypes().length;
        isStatic = Modifier.isStatic(method.getModifiers());
        if (! isStatic) {
            numberOfParameters++;
        }
    }

    public void run(Stack s) throws ParseException {
        checkStack(s);            
        Object thiz = isStatic? null : s.pop();
        int numArgs = isStatic ? numberOfParameters 
                               : (numberOfParameters-1);
        Object[] args = new Object[numArgs];
        for (int i=0; i < args.length; i++) {
            args[i] = s.pop();
        }
        try {
            Object res = method.invoke(thiz, args);
            s.push(res);
        } catch (Exception exp) {
            throw new ParseException(exp.getMessage());
        }
    }
}
