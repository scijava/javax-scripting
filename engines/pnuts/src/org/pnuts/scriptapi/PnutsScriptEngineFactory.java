/*
 * @(#)PnutsScriptEngineFactory.java 1.4 05/06/14
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import java.util.List;
import java.util.Arrays;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngine;
import pnuts.lang.Pnuts;

public class PnutsScriptEngineFactory implements ScriptEngineFactory {

    public PnutsScriptEngineFactory(){
    }

    /**
     * Returns an array of filename extensions, which generally
     * identify scripts written in the language supported by this
     * ScriptEngine. The array is used by the ScriptEngineManager to
     * implement its getEngineByExtension method.
     *
     * @return The array of extensions.
     */
    public List<String> getExtensions(){
	return Arrays.asList(new String[]{"pnut"});
    }

    /**
     * Returns an array of mimetypes, associated with scripts that
     * can be executed by the engine. The array is used by the
     * ScriptEngineManager class to implement its
     * getEngineByMimetype method.
     *
     * @return The array of mime types.
     */
    public List<String> getMimeTypes(){
	return Arrays.asList(new String[0]);
    }

    /**
     * Returns an array of short names for the ScriptEngine, which
     * may be used to identify the ScriptEngine by the
     * ScriptEngineManager. For instance, an implementation based on
     * the Mozilla Rhino Javascript engine might return
     * {"javascript", "rhino"}.
     */
    public List<String> getNames(){
	return Arrays.asList(new String[]{"pnuts"});
    }


	public String getEngineName(){
		return "Pnuts";
	}

	public String getEngineVersion(){
		return PnutsScriptEngine.class.getPackage().getSpecificationVersion();
	}

	public String getLanguageVersion(){
		return Pnuts.class.getPackage().getSpecificationVersion();
	}

	public String getLanguageName(){
		return "Pnuts";
	}


    public Object getParameter(String s){
	if (s.equals(ScriptEngine.NAME)){
	    return getEngineVersion();
	} else if (s.equals(ScriptEngine.ENGINE)){
	    return getEngineName();
	} else if (s.equals(ScriptEngine.ENGINE_VERSION)){
	    return getEngineVersion();
	} else if (s.equals(ScriptEngine.LANGUAGE)){
	    return getLanguageName();
	} else if (s.equals(ScriptEngine.LANGUAGE_VERSION)){
	    return getLanguageVersion();
	} else if (s.equals("THREADING")){
	    return "MULTITHREADED";
	} else {
	    throw new IllegalArgumentException("Invalid key");
	}
    }

    /**
     * Returns an instance of the ScriptEngine associated with this
     * ScriptEngineFactory. Properties of this ScriptEngine class
     * are described by the ScriptEngineInfo members of this
     * ScriptEngineFactory.
     *
     * @return A new ScriptEngine instance.
     */
    public ScriptEngine getScriptEngine(){
	return new PnutsScriptEngine();
    }

    /**
     * Returns a String which can be used to invoke a method of a
     * Java object using the syntax of the supported scripting
     * language. For instance, an implementaton for a Javascript
     * engine might be;
     * <pre>
     * public String getMethodCallSyntax(String obj, String method, String[] args) {
     *     int ret = obj;
     *     obj += "." + method + "(";
     *     for (int i = 0; i < args.length; i++) {
     *       return += args[i];
     *       if (i == args.length - 1) {
     *         obj += ")";
     *       } else {
     *         obj += ",");
     *       }
     *     }
     *     return ret;
     * }
     * </pre>
     * @param obj The name representing the object whose method is
     * to be invoked. The name is the one used to create
     * bindings using the put method of ScriptEngine, the put
     * method of an ENGINE_SCOPE bindings,or the setAttribute
     * method of ScriptContext. The identifier used in scripts
     * may be a decorated form of the specified one.
     * @param m The name of the method to invoke.
     * @param args An array whose members are the names of the
     * arguments in the method call.
     * @return The String used to invoke the method in the syntax of
     * the scripting language.
     */
	public String getMethodCallSyntax(String obj, String m, String... args){
		StringBuilder sbuf = new StringBuilder();
		sbuf.append(obj);
		sbuf.append(".");
		sbuf.append(m);
		sbuf.append("(");
		int len = args.length;
		sbuf.append(args[0]);
		for (int i = 1; i < len; i++){
			sbuf.append(",");
			sbuf.append(args[i]);
		}
		sbuf.append(")");
		return sbuf.toString();
    }

    /**
     * Returns a String that can be used as a statement to display
     * the specified String using the syntax of the supported
     * scripting language. For instance, the implementaton for a
     * Perl engine might be;
     * <pre>
     * public String getOutputStatement(String toDisplay) {
     *    return "print(" + toDisplay + ")";
     * }
     * </pre>
     * @param toDisplay  The String to be displayed by the returned
     * statement.
     * @return The string used to display
     */
    public String getOutputStatement(String toDisplay) {
	StringBuilder sbuf = new StringBuilder();
	sbuf.append("println(\"");
	int len = toDisplay.length();
	for (int i = 0; i < len; i++){
	    char ch = toDisplay.charAt(i);
	    switch (ch){
	    case '"':
		sbuf.append("\\\"");
		break;
	    case '\\':
		sbuf.append("\\\\");
		break;
	    default:
		sbuf.append(ch);
	    }
	}
	sbuf.append("\")");
	return sbuf.toString();
    }

    /**
     * Returns A valid scripting language executable progam whose
     * statements are the elements of the array passed as an
     * argument. For instance an implementation for a PHP engine
     * might be:
     * <pre>
     * public String getProgram(String[] statements) {
     *   $retval = "<?\n";
     *   int len = statements.length;
     *   for (int i = 0; i < len; i++) {
     *     $retval += statements[i] + ";\n";
     *   }
     *   $retval += "?>";
     * }
     * @param statements The statements to be executed. May be
     * return values of calls to the getMethodCallSyntax and
     * getOutputStatement methods.
     * @return The Program
     */
    public String getProgram(String... statements){
	StringBuilder sbuf = new StringBuilder();
	for (int i = 0; i < statements.length; i++){
	    sbuf.append(statements[i]);
	    sbuf.append("\n");
	}
	return sbuf.toString();
    }
}
