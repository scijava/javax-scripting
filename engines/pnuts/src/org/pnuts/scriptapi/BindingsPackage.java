/*
 * @(#)BindingsPackage.java 1.5 05/06/14
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import javax.script.Bindings;
import javax.script.ScriptContext;
import pnuts.lang.Package;
import pnuts.lang.Context;
import pnuts.lang.NamedValue;
import java.util.Enumeration;

public class BindingsPackage extends Package {

    private Package globalPackage;
    private Package enginePackage;

    public BindingsPackage(){
    }

    public void setBindings(Bindings ns, int scope){
	if (scope == ScriptContext.GLOBAL_SCOPE){
	    globalPackage = bindingsToPackage(ns);
	} else if (scope == ScriptContext.ENGINE_SCOPE){
	    enginePackage = bindingsToPackage(ns);
	} else {
	    throw new IllegalArgumentException("Illegal scope value.");
	}
    }

    static Package bindingsToPackage(Bindings ns){
	PnutsBindings pns;
	if (ns instanceof PnutsBindings){
	    pns = (PnutsBindings)ns;
	} else {
	    pns = new PnutsBindings(ns);
	}
	return pns.getPackage();
    }

    public Object get(String symbol){
	NamedValue value = enginePackage.lookup(symbol);
	if (value != null){
	    return value.get();
	}
	if (globalPackage != null){
	    return globalPackage.get(symbol);
	} else {
	    return null;
	}
    }

    public Object get(String symbol, Context context){
	return get(symbol);
    }

    public void set(String symbol, Object value){
	enginePackage.set(symbol, value);
    }

    public boolean defined(String name, Context context){
	return enginePackage.lookup(name) != null || 
	    (globalPackage != null) && (globalPackage.lookup(name) != null);
    }

    public Enumeration keys(){
	throw new RuntimeException();
    }

    public Enumeration values(){
	throw new RuntimeException();
    }

    public int size(){
	int sz = 0;
	if (enginePackage != null){
	    sz += enginePackage.size();
	}
	if (globalPackage != null){
	    sz += globalPackage.size();
	}
	return sz;
    }

    public NamedValue lookup(String symbol){
	NamedValue value = enginePackage.lookup(symbol);
	if (value != null){
	    return value;
	}
	if (globalPackage != null){
	    return globalPackage.lookup(symbol);
	} else {
	    return null;
	}
    }

    public NamedValue lookup(String symbol, Context context){
	return lookup(symbol);
    }

}
