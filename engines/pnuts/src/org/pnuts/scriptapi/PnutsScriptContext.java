/*
 * @(#)PnutsScriptContext.java 1.5 05/06/14
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import pnuts.lang.Context;
import pnuts.lang.Package;
import pnuts.lang.NamedValue;
import pnuts.compiler.CompilerPnutsImpl;
import java.io.InputStreamReader;
import java.io.Writer;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import javax.script.ScriptContext;
import javax.script.Bindings;
import javax.script.ScriptException;

public class PnutsScriptContext implements ScriptContext {
    private Reader reader;
    protected Context context;
    Bindings globalBindings;
    PnutsBindings engineBindings;
    BindingsPackage bindingsPackage;

    /**
     * Constructor
     */
    public PnutsScriptContext(){
	this(new Context(), new PnutsBindings(), new PnutsBindings());
	initializeContext(context);
    }

    /**
     * Constructor
     *
     * @param engineBindings the engine's bindings
     * @param globalBindings the global bindings
     */
    public PnutsScriptContext(Bindings engineBindings, Bindings globalBindings){
	this(new Context(), engineBindings, globalBindings);
	initializeContext(context);
    }

    /**
     * Constructor
     *
     * @param scriptContext a ScriptContext
     */
    public PnutsScriptContext(ScriptContext scriptContext){
	this(new Context(),
	     scriptContext.getBindings(ENGINE_SCOPE),
	     scriptContext.getBindings(GLOBAL_SCOPE));
	initializeContext(context);
    }

    protected void initializeContext(Context context){
	List modules = PnutsScriptEngine.startupModules;
	if (modules != null){
	    for (Iterator it = modules.iterator(); it.hasNext();){
		context.usePackage((String)it.next());
	    }
	}
    }

    /**
     * Constructor
     *
     * @param context Pnuts' context
     * @param engineBindings the engine's bindings
     * @param globalBindings the global bindings
     */
    public PnutsScriptContext(Context context, Bindings engineBindings, Bindings globalBindings){
	this.context = context;
	this.reader = new InputStreamReader(System.in);
	if (engineBindings instanceof PnutsBindings){
	    this.engineBindings = (PnutsBindings)engineBindings;
	} else {
	    this.engineBindings = new PnutsBindings(engineBindings);
	}
	this.globalBindings = globalBindings;
	this.bindingsPackage = new BindingsPackage();
	if (globalBindings != null){
	    bindingsPackage.setBindings(globalBindings, ScriptContext.GLOBAL_SCOPE);
	}
	if (engineBindings != null){
	    bindingsPackage.setBindings(engineBindings, ScriptContext.ENGINE_SCOPE);
	}
	context.setCurrentPackage(bindingsPackage);
    }

    /**
     * Gets Pnuts' context associated with this ScriptContext
     */
    public Context getPnutsContext(){
	return this.context;
    }

    /**
     * Associates a Bindings instance with a particular scope in
     * this ScriptContext. Calls to the getAttribute and
     * setAttribute methods must map to the put and get methods of
     * the Bindings for the specified scope.
     *
     * @param bindings The Bindings to associate with the given scope
     * @param scope The scope
     * @exception IllegalArgumentException If no Bindings is defined
     * for the specified scope value in ScriptContexts of this type.
     * @exception NullPointerException if value of scope is ENGINE_SCOPE
     * and the specified Bindings is null.
     */
    public void setBindings(Bindings bindings, int scope){
	if (scope == GLOBAL_SCOPE){
	    this.globalBindings = bindings;
	} else if (scope == ENGINE_SCOPE){
	    if (bindings instanceof PnutsBindings){
		this.engineBindings = (PnutsBindings)bindings;
	    } else if (bindings != null){
		this.engineBindings = new PnutsBindings(bindings);
	    }
	} else {
	    throw new IllegalArgumentException("Illegal scope value.");
	}
	if (bindings != null){
	    bindingsPackage.setBindings(bindings, scope);
	    context.setCurrentPackage(bindingsPackage);
	}
    }

    /**
     * Gets the Bindings associated with the given scope in this
     * ScriptContext.
     *
     * @return The associated Bindings. Returns null if it has not
     * been set.
     * @exception java.lang.IllegalArgumentException If no Bindings is
     * defined for the specified scope value in ScriptContext
     * of this type.
     */
    public Bindings getBindings(int scope){
	if (scope == GLOBAL_SCOPE){
	    return this.globalBindings;
	} else if (scope == ENGINE_SCOPE){
	    return this.engineBindings;
	} else {
	    throw new IllegalArgumentException("Illegal scope value.");
	}
    }

    /**
     * Sets the value of an attribute in a given scope.
     * 
     * @param name The name of the attribute to set.
     * @param scope The scope in which to set the attribute
     * @exception IllegalArgumentException if the scope is invalid.
     * @exception NullPointerException if the name is null.
     * @exception ClassCastException if the name is not a String.
     */
    public void setAttribute(String attr, Object value, int scope){
	getBindings(scope).put(attr, value);
    }

    /**
     * Gets the value of an attribute in a given scope.
     * 
     * @param name The name of the attribute to retrieve.
     * @param scope The scope in which to retrieve the attribute.
     * @return The value of the attribute. Returns null is the name
     * does not exist in the given scope.
     * 
     * @exception IllegalArgumentException if the value of scope is
     * invalid.
     * @exception NullPointerException if the name is null.
     */
    public Object getAttribute(String attr, int scope){
	return getBindings(scope).get(attr);
    }

    /**
     * Remove an attribute in a given scope.
     *
     * @param name The name of the attribute to remove
     * @param scope The scope in which to remove the attribute
     * @return The removed value.
     * @exception IllegalArgumentException if the scope is invalid.
     * @exception NullPointerException if the name is null.
     */
    public Object removeAttribute(String attr, int scope){
	return getBindings(scope).remove((Object)attr);
    }

    /**
     * Retrieves the value of the attribute with the given name in
     * the scope occurring earliest in the search order. The order
     * is determined by the numeric value of the scope parameter
     * (lowest scope values first.)
     * 
     * @param name The name of the the attribute to retrieve.
     * @return The value of the attribute in the lowest scope for which
     * an attribute with the given name is defined. Returns
     * null if no attribute with the name exists in any scope.
     * @exception NullPointerException  if the name is null.
     */
    public Object getAttribute(String name){
	Bindings ns;
	String symbol = name.intern();
	NamedValue value;

	ns = getBindings(ENGINE_SCOPE);
	if (ns instanceof PnutsBindings){
	    NamedValue binding = ((PnutsBindings)ns).lookup(symbol);
	    if (binding != null){
		return binding.get();
	    }
	} else {
	    if (ns.containsKey(symbol)){
		return ns.get(symbol);
	    }
	}
	ns = getBindings(GLOBAL_SCOPE);
	return ns.get(symbol);
    }

    /**
     * Get the lowest scope in which an attribute is defined.
     *
     * @param name  Name of the attribute.
     * @return The lowest scope. Returns -1 if no attribute with the
     * given name is defined in any scope.
     */
    public int getAttributesScope(String name){
	Bindings ns;

	ns = getBindings(ENGINE_SCOPE);
	if (ns.containsKey(name)){
	    return ENGINE_SCOPE;
	}
	ns = getBindings(GLOBAL_SCOPE);
	if (ns.containsKey(name)){
	    return GLOBAL_SCOPE;
	}
	return -1;
    }

    /**
     * Returns the Writer for scripts to use when displaying output.
     *
     * @return The Writer.
     */
    public Writer getWriter(){
	return context.getWriter();
    }

    /**
     * Returns the Writer used to display error output.
     * 
     * @return The Writer
     */
    public Writer getErrorWriter(){
	return context.getErrorWriter();
    }

    /**
     * Sets the Writer for scripts to use when displaying output.
     *
     * @param writer The new Writer.
     */
    public void setWriter(Writer writer){
	context.setWriter(writer);
    }

    /**
     * Sets the Writer used to display error output.
     *
     * @param writer The Writer.
     */
    public void setErrorWriter(Writer w){
	context.setErrorWriter(w);
    }

    /**
     * Returns a Reader to be used by the script to read input.
     *
     * @return The Reader.
     */
    public Reader getReader(){
	return reader;
    }

    /**
     * Sets the Reader for scripts to read input .
     * 
     * @param reader The new Reader.
     */
    public void setReader(Reader reader){
	this.reader = reader;
    }

	public List<Integer> getScopes(){
		return Arrays.asList(new Integer[]{ENGINE_SCOPE,GLOBAL_SCOPE});
	}	
}
