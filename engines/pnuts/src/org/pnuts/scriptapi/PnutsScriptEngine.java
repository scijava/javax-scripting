/*
 * @(#)PnutsScriptEngine.java 1.5 05/06/14
 *
 * Copyright (c) 2005 Sun Microsystems, Inc. All Rights Reserved.
 *
 * See the file "LICENSE.txt" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.
 */
package org.pnuts.scriptapi;

import javax.script.ScriptEngine;
import javax.script.ScriptContext;
import javax.script.ScriptException;
import javax.script.CompiledScript;
import javax.script.Bindings;
import javax.script.ScriptEngineFactory;
import javax.script.Compilable;
import javax.script.Invocable;
import pnuts.lang.Pnuts;
import pnuts.lang.Context;
import pnuts.lang.Package;
import pnuts.lang.Callable;
import pnuts.lang.PnutsFunction;
import pnuts.lang.Executable;
import pnuts.lang.ParseException;
import pnuts.lang.PnutsException;
import pnuts.compiler.Compiler;
import org.pnuts.lang.SubtypeGenerator;
import org.pnuts.util.MemoryCache;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class PnutsScriptEngine implements ScriptEngine, Compilable, Invocable {

	private final static String CONTEXT_KEY = "context";

	protected PnutsScriptContext defaultContext;
	private PnutsScriptEngineFactory factory;
	private Compiler compiler = new Compiler();
	private MemoryCache cache = new MemoryCache();
	static ArrayList startupModules = null;
	private final static String PROPERTY_FILE = "/org/pnuts/scriptapi/pnuts.properties";
	private final static String PROPERTY_MODULES = "pnuts.modules";

	static {
		try {
			AccessController.doPrivileged(new PrivilegedAction<Object>(){
					public Object run(){
						InputStream in = PnutsScriptEngine.class.getResourceAsStream(PROPERTY_FILE);
						if (in != null){
							Properties prop = new Properties();
							try{
								prop.load(in);
								Pnuts.setDefaults(prop);
								String module_property = prop.getProperty(PROPERTY_MODULES);
								ArrayList<String> modules = new ArrayList<String>();
								StringTokenizer stoken = new StringTokenizer(module_property, ",");
								while (stoken.hasMoreTokens()){
									modules.add(stoken.nextToken());
								}
								startupModules = modules;
							} catch (IOException ioe){
								// ignore
							}
						}
						return null;
					}
				});
		} catch (Exception e){
			// ignore
		}
	}

	/**
	 * Constructor
	 */
	public PnutsScriptEngine(){
		this.defaultContext = new PnutsScriptContext();
	}

	/**
	 * Causes the immediate execution of the script whose source is
	 * the String passed as the first argument. The script may be
	 * reparsed or recompiled before execution. State left in the
	 * engine from previous executions, including variable values
	 * and compiled procedures may be visible during this execution.
	 *
	 * @param script The script to be executed by the script engine.
	 * @param context A ScriptContext exposing sets of attributes in
	 * different scopes. The meanings of the scopes ScriptContext.GLOBAL_SCOPE, and
	 * ScriptContext.ENGINE_SCOPE are defined in the specification.
	 * The ENGINE_SCOPE Bindings of the ScriptContext contains
	 * the bindings of scripting variables to application
	 * objects to be used during this script execution.
	 * @return The value returned from the execution of the script.
	 * @exception ScriptException if an error occurrs. ScriptEngines
	 * should create and throw ScriptException wrappers for
	 * checked Exceptions thrown by underlying scripting implementations.
	 * @exception java.lang.NullPointerException if either argument is null.
	 */
	public Object eval(String script, ScriptContext context) throws ScriptException {
		PnutsScriptContext psc;
		if (context instanceof PnutsScriptContext){
			psc = (PnutsScriptContext)context;
		} else {
			psc = new PnutsScriptContext(context);
		}
		Context ctx = psc.getPnutsContext();
		psc.engineBindings.put(CONTEXT_KEY, context);
		Executable executable = null;
		try {
			executable = (Executable)cache.get(script);
			if (executable == null){
				Pnuts parsed = Pnuts.parse(script);
				executable = parsed;
				try {
					executable = compiler.compile(parsed, ctx);
				} catch (ClassFormatError e){
					// skip
				}
			}
			return executable.run(ctx);
		} catch (PnutsException e1){
			Throwable t = e1.getThrowable();
			if (t instanceof Exception){
				throw new ScriptException((Exception)t);
			} else {
				throw (Error)t;
			}
		} catch (Exception e2){
			throw new ScriptException(e2);
		} finally {
			psc.engineBindings.remove(CONTEXT_KEY);
			cache.put(script, executable);
			Writer w = psc.getWriter();
			if (w != null){
				try {
					w.flush();
				} catch (IOException ioe){
					// skip
				}
			}
		}
	}

	/**
	 * Same as eval(String, ScriptContext) where the source of the
	 * script is read from a Reader.
	 *
	 * @param reader The source of the script to be executed by the script engine.
	 * @param context The ScriptContext passed to the script engine.
	 * @return The value returned from the execution of the script.
	 * @exception ScriptException if an error occurrs.
	 * @exception java.lang.NullPointerException if either argument is null.
	 */
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		PnutsScriptContext psc;
		if (context instanceof PnutsScriptContext){
			psc = (PnutsScriptContext)context;
		} else {
			psc = new PnutsScriptContext(context);
		}
		try {
			psc.engineBindings.put(CONTEXT_KEY, context);
			return Pnuts.load(reader, psc.getPnutsContext());
		} catch (PnutsException e1){
			Throwable t = e1.getThrowable();
			if (t instanceof Exception){
				throw new ScriptException((Exception)t);
			} else {
				throw (Error)t;
			}
		} catch (Exception e2){
			throw new ScriptException(e2);
		} finally {
			psc.engineBindings.remove(CONTEXT_KEY);
			Writer w = psc.getWriter();
			if (w != null){
				try {
					w.flush();
				} catch (IOException ioe){
					// skip
				}
			}
		}
	}

	/**
	 * Executes the specified script. The default ScriptContext for
	 * the ScriptEngine is used.
	 *
	 * @param script The script language source to be executed.
	 * @return The value returned from the execution of the script.
	 * @exception ScriptException if error occurrs.
	 * @exception java.lang.NullPointerException if the argument is null.
	 */
	public Object eval(String script) throws ScriptException {
		return eval(script, defaultContext);
	}

	/**
	 * Same as eval(String) except that the source of the script is
	 * provided as a Reader
	 * 
	 * @param reader The source of the script.
	 * @return The value returned by the script.
	 * @exception ScriptExcepion if an error occurrs.
	 * @exception java.lang.NullPointerException if the argument is null.
	 */
	public Object eval(Reader reader) throws ScriptException {
		return eval(reader, defaultContext);
	}

	/**
	 * Executes the script using the Bindings argument as the
	 * ENGINE_SCOPE Bindings of the ScriptEngine during the script
	 * execution. The Reader, Writer and non-ENGINE_SCOPE Bindingss
	 * of the default ScriptContext are used. The ENGINE_SCOPE
	 * Bindings of the ScriptEngine is not changed, and its
	 * mappings are unaltered by the script execution.
	 *
	 * @param script The source for the script.
	 * @param n The Bindings of attributes to be used for script execution.
	 * @return The value returned by the script.
	 * @exception ScriptException if an error occurrs.
	 * @exception java.lang.NullPointerException if either argument is null.
	 */
	public Object eval(String script, Bindings n) throws ScriptException {
		return eval(script, getScriptContext(n));
	}

	/**
	 * Same as eval(String, Bindings) except that the source of the
	 * script is provided as a Reader.
	 *
	 * @param reader The source of the script.
	 * @param n The Bindings of attributes.
	 * @return The value returned by the script.
	 * @exception ScriptException if an error occurrs.
	 * @exception java.lang.NullPointerException  if either argument is null.
	 */
	public Object eval(Reader reader, Bindings n) throws ScriptException {
		return eval(reader, getScriptContext(n));
	}

	/**
	 * Sets a key/value pair in the state of the ScriptEngine that
	 * may either create a Java Language Binding to be used in the
	 * execution of scripts or be used in some other way, depending
	 * on whether the key is reserved. Must have the same effect as
	 * getBindings(ScriptContext.ENGINE_SCOPE).put.
	 *
	 * @param key The name of named value to add
	 * @param value The value of named value to add.
	 * @exception java.lang.IllegalArgumentException if key is null or
	 * not a String.
	 */
	public void put(String key, Object value){
		getBindings(ScriptContext.ENGINE_SCOPE).put(key, value);
	}

	/**
	 * Retrieves a value set in the state of this engine. The value
	 * might be one which was set using setValue or some other value
	 * in the state of the ScriptEngine, depending on the
	 * implementation. Must have the same effect as getBindings
	 * (ScriptContext.ENGINE_SCOPE).get
	 *
	 * @param key The key whose value is to be returned
	 * @return the value for the given key
	 */
	public Object get(String key){
		return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
	}

	/**
	 * Returns a scope of named values. The possible scopes are:
	 * <ul>
	 * <li>criptContext.GLOBAL_SCOPE - A set of named values
	 * shared by all ScriptEngines in the process. If the
	 * ScriptEngine is created by a ScriptEngineManager, a
	 * reference to the global scope stored by the
	 * ScriptEngineManager should be returned. May return null
	 * if no global scope is associated with this ScriptEngine
	 * <li>ScriptContext.ENGINE_SCOPE - The set of named values
	 * representing the state of this ScriptEngine. The values
	 * are generally visible in scripts using the associated
	 * keys as variable names.
	 * <li>Any other value of scope defined in the default
	 * ScriptContext of the ScriptEngine.
	 * </ul>
	 * The Bindings instances that are returned must be identical
	 * to those returned by the getBindings method of ScriptContext
	 * called with corresponding arguments on the default
	 * ScriptContext of the ScriptEngie.
	 *
	 * @param scope  Either ScriptContext.ENGINE_SCOPE or
	 * ScriptContext.GLOBAL_SCOPE which specifies the Bindings
	 * to return. Implementations of ScriptContext may define
	 * additional scopes. If the default ScriptContext of the
	 * ScriptEngine defines additional scopes, any of them can
	 * be passed to get the corresponding Bindings.
	 * @return The Bindings with the specified scope.
	 * @exception java.lang.IllegalArgumentException if specified scope is invalid
	 */
	public Bindings getBindings(int scope) {
		if (scope == ScriptContext.GLOBAL_SCOPE){
			return defaultContext.getBindings(ScriptContext.GLOBAL_SCOPE);
		} else if (scope == ScriptContext.ENGINE_SCOPE){
			return defaultContext.getBindings(ScriptContext.ENGINE_SCOPE);
		} else {
			throw new IllegalArgumentException("Invalid scope value.");
		}
	}

	/**
	 * Sets a scope of named values to be used by scripts. The
	 * possible scopes are:
	 * <ul>
	 * <li>ScriptContext.ENGINE_SCOPE - The specified Bindings
	 * replaces the engine scope of the ScriptEngine.
	 * <li>ScriptContext.GLOBAL_SCOPE - The specified Bindings
	 * must be visible as the GLOBAL_SCOPE in every
	 * ScriptContext in the process.
	 * <li>Any other value of scope defined in the default
	 * ScriptContext of the ScriptEngine.
	 * </ul>
	 * The method must have the same effect as calling the
	 * setBindings method of ScriptContext with the corresponding
	 * value of scope on the default ScriptContext of the
	 * ScriptEngine.
	 * 
	 * @param bindings The Bindings for the specified scope.
	 * @param scope The specified scope. Either
	 * ScriptContext.ENGINE_SCOPE, ScriptContext.GLOBAL_SCOPE,
	 * or any other valid value of scope.
	 * @exception java.lang.IllegalArgumentException if the scope is invalid
	 */
	public void setBindings(Bindings bindings, int scope){
		if (scope == ScriptContext.GLOBAL_SCOPE){
			defaultContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
		} else if (scope == ScriptContext.ENGINE_SCOPE){
			defaultContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
		} else {
			throw new IllegalArgumentException("Invalid scope value.");
		}
	}

	/**
	 * Returns an uninitialized Bindings.
	 * 
	 * @return A Bindings that can be used to replace the state of
	 * this ScriptEngine.
	 */
	public Bindings createBindings(){
		return new PnutsBindings(new Package(null, null));
	}

	/**
	 * Returns the default ScriptContext of the ScriptEngine whose
	 * Bindingss, Reader and Writers are used for script executions
	 * when no ScriptContext is specified.
	 *
	 * @return The default ScriptContext of the ScriptEngine.
	 */
	public ScriptContext getContext(){
		return this.defaultContext;
	}

	/**
	 * Sets the default code>ScriptContext of the ScriptEngine whose
	 * Bindingss, Reader and Writers are used for script executions
	 * when no ScriptContext is specified.
	 * 
	 * @param context A ScriptContext that will replace the
	 * default ScriptContext in the ScriptEngine.
	 */
	public void setContext(ScriptContext context){
		if (context instanceof PnutsScriptContext){
			this.defaultContext = (PnutsScriptContext)context;
		} else {
			this.defaultContext = new PnutsScriptContext(context);
		}
	}

	/**
	 * Returns a ScriptEngineFactory for the class to which this
	 * ScriptEngine belongs. The returned ScriptEngineFactory
	 * implements ScriptEngineInfo, which describes attributes of
	 * this ScriptEngine implementation.
	 * 
	 * @return The ScriptEngineFactory
	 */
	public synchronized ScriptEngineFactory getFactory(){
		if (factory == null){
			return factory = new PnutsScriptEngineFactory();
		} else {
			return factory;
		}
	}

	/**
	 * Compiles the script (source represented as a String) for
	 * later execution.
	 * 
	 * @param script The source of the script, represented as a String.
	 * @return An subclass of CompiledScript to be executed later using
	 * one of the eval methods of CompiledScript.
	 * @exception ScriptException if compilation fails.
	 * @exception NullPointerException if the argument is null.
	 */
	public CompiledScript compile(String s) throws ScriptException {
		try {
			Executable executable = (Executable)cache.get(s);
			if (executable == null){
				Context context = new Context();
				Pnuts parsedScript = Pnuts.parse(s);
				executable = parsedScript;
				try {
					executable = compiler.compile(parsedScript, context);
				} catch (ClassFormatError e){
					// skip
				}
				cache.put(s, executable);
			}
			return new PnutsCompiledScript(this, executable);
		} catch (ParseException e1){
			throw new ScriptException(e1.getMessage(),
						  null,
						  e1.getErrorLine(),
						  e1.getErrorColumn());
		} catch (PnutsException e2){
			String file = null;
			Object scriptSource = e2.getScriptSource();
			if (scriptSource != null){
				file = scriptSource.toString();
			}
			throw new ScriptException(e2.getMessage(), file, e2.getLine());
		} catch (Exception e3){
			throw new ScriptException(e3);
		}
	}

	/**
	 * Compiles the script (source read from Reader) for later
	 * execution. Functionality is identical to compile(String)
	 * other than the way in which the source is passed.
	 * 
	 * @param script The reader from which the script source is
	 * obtained.
	 * @return An implementation of CompiledScript to be executed later
	 * using one of its eval methods of CompiledScript.
	 * @exception ScriptException if compilation fails.
	 * @exception NullPointerException if argument is null.
	 */
	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			Pnuts parsedScript = Pnuts.parse(script);
			Executable executable = parsedScript;
			try {
				executable = compiler.compile(parsedScript, new Context());
			} catch (ClassFormatError e){
				// skip
			}
			return new PnutsCompiledScript(this, executable);
		} catch (Exception e){
			throw new ScriptException(e);
		}
	}

	/**
	 * Calls a procedure compiled during a previous script execution, which is retained in 
	 * the state of the <code>ScriptEngine<code>.
	 *
	 * @param name The name of the procedure to be called.
	 *
	 * @param thiz If the procedure is a member  of a class
	 * defined in the script and thiz is an instance of that class
	 * returned by a previous execution or invocation, the named method is 
	 * called through that instance.
	 * If classes are not supported in the scripting language or 
	 * if the procedure is not a member function of any class, the argument must be 
	 * <code>null</code>.
	 *
	 * @param args Arguments to pass to the procedure.  The rules for converting
	 * the arguments to scripting variables are implementation-specific.
	 *
	 * @return The value returned by the procedure.  The rules for converting the scripting variable returned by the procedure to a Java Object are implementation-specific.
	 *
	 * @throws ScriptException if an error occurrs during invocation of the method.
	 * @throws NoSuchMethodException if method with given name or matching argument types cannot be found.
	 * @throws NullPointerException if method name is null.
	 */
	public Object invoke(Object thiz, String name, Object... args)
		throws ScriptException, NoSuchMethodException
	{
		try {
			if (thiz != null){
				Class cls = thiz.getClass();
				Method[] methods = cls.getMethods();
				for (int i = 0; i < methods.length; i++){
					Method m = methods[i];
					if (m.getName().equals(name)){
						return m.invoke(thiz, args);
					}
				}
			}
		} catch (Exception e){
			throw new ScriptException(e);
		}
		Object value = defaultContext.getAttribute(name);
	
		if (value instanceof Callable){
			try {
				return ((Callable)value).call(args, defaultContext.getPnutsContext());
			} catch (Exception e){
				throw new ScriptException(e);
			}
		} else {
			throw new NoSuchMethodException();
		}
	}

	/**
	 * Same as invoke(Object, String, Object...) with <code>null</code> as the first
	 * argument.  Used to call top-level procedures defined in scripts.
	 *
	 * @param args Arguments to pass to the procedure
	 * @return The value returned by the procedure
	 *
	 * @throws ScriptException if an error occurrs during invocation of the method.
	 * @throws NoSuchMethodException if method with given name or matching argument types cannot be found.
	 * @throws NullPointerException if method name is null.
	 */
	public Object invoke(String name, Object... args)
		throws ScriptException, NoSuchMethodException 
	{
		return invoke(null, name, args);
	}

	/**
	 * Returns an implementation of an interface using procedures compiled in 
	 * the interpreter. The methods of the interface 
	 * may be implemented using the <code>invoke</code> method.
	 *
	 * @param clasz The <code>Class</code> object of the interface to return.
	 *
	 * @return An instance of requested interface - null if the requested interface is unavailable, 
	 * i. e. if compiled methods in the <code>ScriptEngine</code> cannot be found matching 
	 * the ones in the requested interface.
	 *
	 * @throws IllegalArgumentException if the specified <code>Class</code> object
	 * does not exist or is not an interface. 
	 */
	public <T> T getInterface(Class<T> clasz){
		return getInterface(clasz, clasz);
	}

	/**
	 * Returns an implementation of an interface using member functions of
	 * a scripting object compiled in the interpreter. The methods of the
	 * interface may be implemented using invoke(Object, String, Object...)
	 * method.
	 *
	 * @param thiz The scripting object whose member functions are used to implement the methods of the interface.
	 * @param clasz The <code>Class</code> object of the interface to return.
	 *
	 * @return An instance of requested interface - null if the requested interface is unavailable, 
	 * i. e. if compiled methods in the <code>ScriptEngine</code> cannot be found matching 
	 * the ones in the requested interface.
	 *
	 * @throws IllegalArgumentException if the specified <code>Class</code> object
	 * does not exist or is not an interface, or if the specified Object is 
	 * null or does not represent a scripting object.
	 */
	public <T> T getInterface(Object thiz, Class<T> clasz){
		return getInterface(thiz.getClass(), clasz);
	}

	<T> T getInterface(Class c, Class<T> clasz){
		if (clasz == null || !clasz.isInterface()){
			throw new IllegalArgumentException("Not an interface.");
		}
		Method[] methods = c.getMethods();
		Package methodMap = new Package(null, null);
		for (int i = 0; i < methods.length; i++){
			Method m = methods[i];
			String name = m.getName();
			Object value = defaultContext.getAttribute(name);
			if (!(value instanceof PnutsFunction)){
				return null;
			}
			methodMap.set(name.intern(), value);
		}
		try {
			Context context = defaultContext.getPnutsContext();
			Class<T> generatedClass = SubtypeGenerator.generateSubclass(Object.class,
										 new Class[]{clasz},
										 methodMap, 
										 context, 0);
			return (T)generatedClass.newInstance();
		} catch (Exception e){
			return null;
		}
	}
	
	protected PnutsScriptContext getScriptContext(Bindings ns){
		Bindings global = getBindings(ScriptContext.GLOBAL_SCOPE);
		PnutsScriptContext c = new PnutsScriptContext(ns, global);
		if (global != null){
			c.setBindings(global, ScriptContext.GLOBAL_SCOPE);
		}
		if (ns != null){
			c.setBindings(ns, ScriptContext.ENGINE_SCOPE);
		} else {
			throw new NullPointerException();
		}
		c.setReader(defaultContext.getReader());
		c.setWriter(defaultContext.getWriter());
		c.setErrorWriter(defaultContext.getErrorWriter());
		return c;
	}
}
