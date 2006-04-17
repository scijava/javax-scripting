/*
 * @(#)PnutsCompiledScript.java 1.5 05/06/14
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import javax.script.ScriptEngine;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.Bindings;
import pnuts.lang.Executable;

public class PnutsCompiledScript extends CompiledScript {

	private PnutsScriptEngine engine;
	private Executable executable;

	/**
	 * Constructor
	 *
	 * @param engine a Pnuts engine
	 * @param executable compiled or parsed Pnuts script
	 */
	public PnutsCompiledScript(PnutsScriptEngine engine, Executable executable){
		this.engine = engine;
		this.executable = executable;
	}

	/**
	 * Executes the program stored in this CompiledScript object.
	 * 
	 * @param context A ScriptContext that is used in the same way
	 * as the ScriptContext passed to the eval methods of ScriptEngine.
	 * @return The value returned by the script execution, if any.
	 * Should return null if no value is returned by the script
	 * execution.
	 * @exception ScriptException if an error occurs.
	 */
	public Object eval(ScriptContext context) throws ScriptException {
		PnutsScriptContext psc;
		if (context instanceof PnutsScriptContext){
			psc = (PnutsScriptContext)context;
		} else {
			psc = new PnutsScriptContext(context);
		}
		return executable.run(psc.getPnutsContext());
	}

	/**
	 * Executes the program stored in the CompiledScript object
	 * using the supplied Bindings of attributes as the
	 * ENGINE_SCOPE of the associated ScriptEngine during script
	 * execution.
	 * <ul>
	 * <li>The GLOBAL_SCOPE Bindings, Reader and Writer associated
	 * with the defaule ScriptContext of the associated ScriptEngine
	 * are used.
	 * </ul>
	 * @param bindings The Bindings of attributes used for the
	 * ENGINE_SCOPE.
	 * @return The return value from the script execution
	 * @exception ScriptException if an error occurs.
	 */
	public Object eval(Bindings bindings) throws ScriptException {
		return eval(engine.getScriptContext(bindings));
	}

	/**
	 * Returns the ScriptEngine wbose compile method created this
	 * CompiledScript. The CompiledScript will execute in this
	 * engine.
	 * @return The ScriptEngine that created this CompiledScript
	 */
	public ScriptEngine getEngine(){
		return engine;
	}
}
