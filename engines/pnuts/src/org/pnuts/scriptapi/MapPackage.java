/*
 * @(#)MapPackage.java 1.5 05/02/17
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import pnuts.lang.Package;
import pnuts.lang.Context;
import pnuts.lang.NamedValue;
import java.util.*;

public class MapPackage extends Package {

    private Map<String,Object> map;

    /*
     * Constructor
     *
     * @param map the map
     */
    public MapPackage(Map<String,Object> map){
	this.map = map;
    }

    public Object get(String symbol){
	return map.get(symbol);
    }

    public Object get(String symbol, Context context){
	return map.get(symbol);
    }

    public void set(String symbol, Object value){
	map.put(symbol, value);
    }

    public boolean defined(String name, Context context){
	return map.containsKey(name);
    }

    public Enumeration keys(){
	return Collections.enumeration(map.keySet());
    }

    public Enumeration values(){
	return Collections.enumeration(map.values());
    }

    public int size(){
	return map.size();
    }

    public NamedValue lookup(final String symbol){
	if (map.containsKey(symbol)){
	    final Object value = map.get(symbol);
	    return new NamedValue(){
		    public String getName(){
			return symbol;
		    }
		    public Object get(){
			return value;
		    }
		    public void set(Object value){
			map.put(symbol, value);
		    }
		};
	} else {
	    return null;
	}
    }

    public NamedValue lookup(final String symbol, Context context){
	return lookup(symbol);
    }
}
