/*
 * @(#)PnutsBindings.java 1.7 05/06/14
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import javax.script.Bindings;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Collection;
import java.util.ArrayList;
import pnuts.lang.Package;
import pnuts.lang.NamedValue;

public class PnutsBindings implements Bindings {
	private Package pkg;

	public PnutsBindings(){
		this(new Package(null, null));
	}

	public PnutsBindings(Package pkg){
		this.pkg = pkg;
	}

	public PnutsBindings(Bindings ns){
		this(new MapPackage(ns));
		if (ns == null){
			new RuntimeException().printStackTrace();
		}
	}

	public Package getPackage(){
		return this.pkg;
	}

	public int size(){
		return pkg.size();
	}

	public boolean isEmpty(){
		return size() == 0;
	}

	static String intern(String key){
		return key.intern();
	}

	public boolean containsKey(Object key){
		if (key instanceof String){
			return pkg.lookup(intern((String)key)) != null;
		} else {
			return false;
		}
	}

	public boolean containsValue(Object value){
		return values().contains(value);
	}

	public Object get(Object key){
		NamedValue value = pkg.lookup(intern((String)key));
		if (value != null){
			return value.get();
		} else {
			return null;
		}
	}

	public Object remove(Object key){
		if (key instanceof String){
			Object oldValue = get(key);
			pkg.clear(intern((String)key), null);
			return oldValue;
		}
		return null;
	}

	public void clear(){
		for (Enumeration e = pkg.keys(); e.hasMoreElements();){
			pkg.clear((String)e.nextElement(), null);
		}
	}

	public Set<String> keySet(){
		HashSet<String> keySet = new HashSet<String>();
		for (Enumeration e = pkg.keys(); e.hasMoreElements();){
			keySet.add((String)e.nextElement());
		}
		return keySet;
	}

	public Collection<Object> values(){
		ArrayList<Object> values = new ArrayList<Object>();
		for (Enumeration e = pkg.values(); e.hasMoreElements();){
			values.add(e.nextElement());
		}
		return values;
	}

	public Set<java.util.Map.Entry<java.lang.String,java.lang.Object>> entrySet(){
		HashSet<Map.Entry<java.lang.String,java.lang.Object>> entrySet =
		    new HashSet<Map.Entry<java.lang.String,java.lang.Object>>();
		for (Enumeration e = pkg.bindings(); e.hasMoreElements();){
			NamedValue value = (NamedValue)e.nextElement();
			entrySet.add(new NamedValueEntry(value));
		}
		return entrySet;
	}

	static class NamedValueEntry implements Map.Entry<String,Object> {
		private NamedValue value;

		NamedValueEntry(NamedValue value){
			this.value = value;
		}

		public String getKey(){
			return value.getName();
		}

		public Object getValue(){
			return value.get();
		}

		public Object setValue(Object obj){
			Object oldValue = getValue();
			value.set(obj);
			return oldValue;
		}
	}

	public void remove(String interned){
		pkg.clear(interned, null);
	}

	public NamedValue lookup(String interned){
		return pkg.lookup(interned);
	}

	/**
	 * Set a named value.
	 *
	 * @param name The name associated with the value.
	 * @param value The value associated with the name.
	 *
	 * @return The value previously associated with the given name.
	 * Returns null if no value was previously associated with the name.
	 *
	 * @throws <code>NullPointerException</code> if the name is null.
	 * @throws <code>IllegalArgumentException</code> if the name is empty String.
	 */
	public Object put(String name, Object value){
		String symbol = intern(name);
		NamedValue oldValue = pkg.lookup(symbol);
		pkg.set(symbol, value);
		if (oldValue == null){
			return null;
		} else {
			return oldValue.get();
		}
	}

	/**
	 * Adds all the mappings in a given <code>Map</code> to this <code>Bindings</code.
	 * @param toMerge The <code>Map</code> to merge with this one.
	 *
	 * @throws <code>NullPointerException</code> if some key in the map is null.
	 * @throws <code>IllegalArgumentException</code> if some key in the map is an empty String.
	 */
	public void putAll(Map toMerge){
		Package pkg = this.pkg;
		for (Iterator it = toMerge.entrySet().iterator(); 
		     it.hasNext();){
			Map.Entry entry = (Map.Entry)it.next();
			String symbol = ((String)entry.getKey()).intern();
			pkg.set(symbol, entry.getValue());
		}
	}
}
