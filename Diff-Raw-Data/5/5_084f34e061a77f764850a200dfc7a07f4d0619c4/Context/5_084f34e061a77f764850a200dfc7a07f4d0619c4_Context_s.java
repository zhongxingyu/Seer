 /*
  * Copyright (C) 1998-2000 Semiotek Inc.  All Rights Reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted under the terms of either of the following
  * Open Source licenses:
  *
  * The GNU General Public License, version 2, or any later version, as
  * published by the Free Software Foundation
  * (http://www.fsf.org/copyleft/gpl.html);
  *
  *  or
  *
  * The Semiotek Public License (http://webmacro.org/LICENSE.)
  *
  * This software is provided "as is", with NO WARRANTY, not even the
  * implied warranties of fitness to purpose, or merchantability. You
  * assume all risks and liabilities associated with its use.
  *
  * See www.webmacro.org for more information on the WebMacro project.
  */
 
 
 package org.webmacro;
 
 import java.util.*;
 import java.lang.reflect.*;
 import org.webmacro.util.*;
 import org.webmacro.profile.*;
 import org.webmacro.engine.*;
 
 /**
  * A Context contains state. The idea is to put all of the data you
  * wish to render into the Context and then merge it with a Template
  * via the Template.write() or Template.evaluate() methods. Actually
  * you can render any Macro object by passing a Context to its
  * write() or evaluat() method, not just templates.
  * <p>
  * A Context is a per-thread data structure. It should not be shared
  * between threads since it is not thread safe. The idea is to put all
  * of the state for a single request into the context and then execute
  * it, with each request having its own separate context. In this
  * thread-per-request worldview there is no reason to synchronize
  * the Context objects as they are not shared bewteen threads.
  * <p>
  * Ordinarily you acquire a Context object from the WebMacro
  * interface, use it for awhile, and then recycle() it. But you
  * can implement your own Context objects and pass it to the
  * evaluate() and write() method of any Template or other Macro.
  */
 public class Context implements Map, Cloneable {
    private Broker _broker;
    private HashMap _tools = new HashMap();
    private HashMap _funcs = new HashMap();
    private Log _log;
    
    private HashMap _initializedTools = new HashMap();
    private EvaluationExceptionHandler _eeHandler;
    
    private Map _variables = new HashMap();
    private Pool _contextPool = null;
    
    private TemplateEvaluationContext _teContext
    = new TemplateEvaluationContext();
    
    final static private org.webmacro.engine.UndefinedMacro UNDEF
    = org.webmacro.engine.UndefinedMacro.getInstance();
    /**
     * Create a new Context relative to the supplied broker
     */
    public Context(Broker broker) {
       _prof = broker.newProfile();
       if (_prof != null) { startTiming("Context life"); }
       if (_prof != null) { startTiming("Context init"); }
       _broker = broker;
       _log = broker.getLog("context", "property and evaluation errors");
       loadTools("ContextTools");
       if (_prof != null) { stopTiming(); }
    }
    
    public static final class TemplateEvaluationContext {
       public Block _curBlock;
       public int _curIndex;
    }
    
    private class SettingHandler extends Settings.ListSettingHandler {
       public void processSetting(String settingKey, String settingValue) {
          try {
             addTool(settingKey, settingValue, "Tool");
          } catch (Exception e) {
             _log.error("Provider (" + settingValue + ") failed to load", e);
          }
       }
    }
    
    /**
     * Load the context tools listed in the supplied string. See
     * the ComponentMap class for a description of the format of
     * this string.
     */
    final protected void loadTools(String keyName) {
       _broker.getSettings().processListSetting(keyName, new SettingHandler());
    }
    
    /**
     * See cloneContext(). Subclasses should override cloneContext()
     * rather than the clone() method.
     */
    final public Object clone() {
       return cloneContext();
    }
    
    /**
     * Create a copy of this context. The underlying storage will
     * be copied and the local variables reset.
     */
    public Context cloneContext() {
       if (_prof != null) { startTiming("cloneContext"); }
       Context c;
       try {
          c = (Context) super.clone();
       } catch (CloneNotSupportedException e) {
          e.printStackTrace();
          return null; // never going to happen
       }
       c._prof = _broker.newProfile();
       c.startTiming("Context life"); // stops in clear()
       c._initializedTools = (HashMap) _initializedTools.clone();
       c._teContext = new TemplateEvaluationContext();
       if (_variables instanceof HashMap) {
          c._variables = (Map) ((HashMap) _variables).clone();
       } else {
          c._variables = new HashMap(_variables);
       }
       if (_prof != null) { stopTiming(); }
       return c;
    }
    
    /**
     * Clear the context so that it can be used for another request.
     * This does not meant hat the context is completely empty: it
     * may have been configured with some initial state, such as
     * a collection of tools, that are to be re-used. But all local
     * variables and other local structures will be cleared.
     * <p>
     * Subclasses may override the clear method and add functionality
     * but they must call super.clear() if they do so.
     */
    public void clear() {
       _variables.clear();
       Iterator i = _initializedTools.entrySet().iterator();
       while (i.hasNext()) {
          Map.Entry m = (Map.Entry) i.next();
          ContextTool ct = (ContextTool) m.getKey();
          ct.destroy(m.getValue());
       }
       _initializedTools.clear();
       _eeHandler = null;
       if (_prof != null) {
          stopTiming();
          _prof.destroy();
       }
    }
    
    
    /**
     * Get the instance of the Broker for this request
     */
    final public Broker getBroker() {
       return _broker;
    }
    
    final public TemplateEvaluationContext getTemplateEvaluationContext() {
       return _teContext;
    }
    
    final public String getCurrentLocation() {
       Block b = _teContext._curBlock;
       if (b == null)
          return "(unknown)";
       else
          return b.getTemplateName() + ":"
          + Integer.toString(b.getLineNo(_teContext._curIndex))
          + "." + Integer.toString(b.getColNo(_teContext._curIndex));
    }
    
    /**
     * Get a log instance that can be used to write log messages
     * into the log under the supplied log type.
     */
    final public Log getLog(String type, String description) {
       return _broker.getLog(type, description);
    }
    
    /**
     * Get a log instance that can be used to write log messages
     * into the log under the supplied log type. The type will
     * be used as the description.
     */
    final public Log getLog(String type) {
       return _broker.getLog(type, type);
    }
    
    /**
     * Get the EvaluationExceptionHandler
     */
    public EvaluationExceptionHandler getEvaluationExceptionHandler() {
       if (_eeHandler != null)
          return _eeHandler;
       else
          return _broker.getEvaluationExceptionHandler();
    }
    
    
    /**
     * Set a new EvaluationExceptionHandler
     */
    public void setEvaluationExceptionHandler(EvaluationExceptionHandler eeh) {
       _eeHandler = eeh;
    }
    
    
    /**
     * Get the named object/property from the Context. If the Object
     * does not exist and there is a tool of the same name then the
     * Object will be instantiated and managed by the tool.
     * If there's no such variable, it throws.
     */
    protected Object internalGet(Object name)
    throws PropertyException {
       Object ret = _variables.get(name);
       if (ret == null && !_variables.containsKey(name)) {
          Object tool = _tools.get(name);
          if(tool != null) {
             try {
                ContextTool ct = (ContextTool) tool;
                ret = ct.init(this);
                put(name,ret);
                _initializedTools.put(ct,ret);
             } catch (PropertyException e) {
                _log.error("Unable to initialize ContextTool: " + name, e);
             }
          }
          else if (name instanceof FunctionCall){
             FunctionCall fc = (FunctionCall)name;
             String fname = fc.getName();
             MethodWrapper func = (MethodWrapper)_funcs.get(fname);
             if (func == null){
                func = _broker.getFunction(fname);
             }
             if (func != null){
                Object[] args = fc.getArguments(this);
                ret = func.invoke(args);
             }
             else {
                _log.error("Function " + fname + " was not loaded!");
             }
          }
          else // changed by Keats 30-Nov-01
             return UNDEF;
          //throw new
          // PropertyException.NoSuchVariableException(name.toString());
       }
       return ret;
    }
    
    /**
     * Get the named object/property from the Context; returns null if
     * not found.
     */
    final public Object get(Object name) {
       try {
          //return internalGet(name);
          Object o =  internalGet(name);
          if (o == UNDEF)
             return null;
          return o;
       }
       catch (PropertyException e) {
          // NOTE: I don't think we get here anymore!  -Keats
          return null;
       }
    }
    
    /**
     * Convenience method for putting static classes into the context, wraps the
     * class instance in a wrapper
     */
    final public Object put(Object name, Class c) {
       if (c == null)
          return _variables.put(name, null);
       else
          return _variables.put(name,new org.webmacro.engine.StaticClassWrapper(c));
    }
    
    //   final public Object put(Object name, MethodWrapper mw){
    //       System.out.println("Adding function " + name);
    //       return _funcs.put(name, mw);
    //   }
    
    final public void putFunction(String name, Object instance, String methodName){
       MethodWrapper func = wrapMethod(instance, methodName);
       _funcs.put(name, func);
    }
    
    final public void putGlobalFunction(String name, Object instance, String methodName){
       MethodWrapper func = wrapMethod(instance, methodName);
       _broker.putFunction(name, func);
    }
    
    final private MethodWrapper wrapMethod(Object instance, String methodName){
       MethodWrapper func = null;
       try {
          func = new MethodWrapper(instance, methodName);
       }
       catch (Exception e){
          String className = null;
          if (instance instanceof Class){
             className = ((Class)instance).getName();
          }
          else if (instance != null){
             className = instance.getClass().getName();
          }
          _log.error("Unable to construct function from method: "
          + methodName + " of class " + className);
       }
       return func;
    }
    
    
    /**
     * Add an object to the context returning the object that was
     * there previously under the same name, if any.
     */
    final public Object put(Object name, Object value) {
       return _variables.put(name,value);
    }
    
    /**
     * Get the named object from the Context. The name is a list
     * of property names. The first name is the name of an object
     * in the context. The subsequent names are properties of
     * that object which will be searched using introspection.
     */
    protected Object internalGet(Object[] names)
    throws PropertyException {
       Object instance;
       try {
          instance = internalGet(names[0]);
       } catch (ArrayIndexOutOfBoundsException e) {
          throw new PropertyException(
          "Attempt to access property with a zero length name array");
       }
       if (names.length == 1)
          return instance;
       else if (instance == null)
          throw new PropertyException.NullValueException(names[0].toString());
       else
          return _broker._propertyOperators.getProperty(this,instance,names,1);
    }
    
    /**
     * Set the named property in the Context. The first name is
     * the name of an object in the context. The subsequent names
     * are properties of that object which will be searched using
     * introspection.
    * @returns whether or not the set was successful
     */
    final public boolean set(Object names[], Object value)
    throws PropertyException {
       if (names.length == 1) {
          put(names[0], value);
          return true;
       } else {
          Object instance;
          try {
             instance = internalGet(names[0]);
          } catch (ArrayIndexOutOfBoundsException e) {
             return false;
          }
          return _broker._propertyOperators
          .setProperty(this,instance,names,1,value);
       }
    }
    
    /**
     * Same as get(name) but can be overridden by subclasses to do
     * something different
     */
    public Object getProperty(Object name) throws PropertyException {
       return internalGet(name);
    }
    
    /**
     * Same as put(name,value) but can be overridden by subclasses to do
     * something different
     */
    public boolean setProperty(Object name, Object value)
    throws PropertyException {
       put(name,value);
       return true;
    }
    
    /**
     * Same as get(Object names[]) but can be overridden by subclasses
     * to behave differently
     */
    public Object getProperty(Object names[]) throws PropertyException {
       return internalGet(names);
    }
    
    /**
     * Same as set(Object names[], Object value) but can be overridden
     * by subclasses to behave differently
    * @returns whether or not the set was successful
     */
    public boolean setProperty(Object names[],Object value)
    throws PropertyException {
       return set(names,value);
    }
    
    static private String makeName(Object[] names) {
       StringBuffer buf = new StringBuffer();
       buf.append("$(");
       for (int i = 0; i < names.length; i++) {
          if (i != 0) buf.append(".");
          buf.append( (names[i] != null) ? names[i] : "NULL");
       }
       buf.append(")");
       return buf.toString();
    }
    
    /**
     * Assign the object pool that this context should return to
     * when its recycle() method is called.
     */
    final public void setPool(Pool contextPool) {
       _contextPool = contextPool;
    }
    
    final public Pool getPool(){
       return _contextPool;
    }
    
    /**
     * Return the context to the object pool assigned via setPool(),
     * if any. This method implicitly calls clear().
     */
    final public void recycle() {
       clear();
       if (_contextPool != null) {
          _contextPool.put(this);
       }
    }
    
    
    /**
     * Set the underlying Map object. The supplied Map will subsequently
     * be used to resolve local variables.
     */
    final public void setMap(Map m) {
       _variables = m;
    }
    
    /**
     * Get the underlying Map object.
     */
    final public Map getMap() {
       return _variables;
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    public boolean containsKey(Object key) {
       return _variables.containsKey(key);
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public boolean containsValue(Object value) {
       return _variables.containsValue(value);
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public Set entrySet() {
       return _variables.entrySet();
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public boolean isEmpty() {
       return _variables.isEmpty();
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public Set keySet() {
       return _variables.keySet();
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public void putAll(Map t) {
       _variables.putAll(t);
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public Object remove(Object key) {
       return _variables.remove(key);
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public int size() {
       return _variables.size();
    }
    
    /**
     * Method from Map interface, operates on underlying Map
     */
    final public Collection values() {
       return _variables.values();
    }
    
    // Adding new tools to the context
    private static final Class[] _ctorArgs1 = {
       java.lang.String.class,
       org.webmacro.util.Settings.class
    };
    private static final Class[] _ctorArgs2 = { java.lang.String.class };
    
    /**
     * Attempts to instantiate the tool using three different constructors
     * until one succeeds, in the following order:
     * <ul>
     * <li>new MyContextTool(String key, Settings settings)</li>
     * <li>new MyTool(String key)</li>
     * <li>new MyTool()</li>
     * </ul>
     * The key is generally the unqualified class name of the tool minus the
     * "Tool" suffix, e.g., "My" in the example above
     * The settings are any configured settings for this tool, i.e, settings
     * prefixed with the tool's key.
     * <br>
     * NOTE: keats - 25 May 2002, no tools are known to use the settings mechanism.
     * We should create an example of this and test it, or abolish this capability!
     */
    private void addTool(String key, String className, String suffix) {
       
       Class c;
       try {
          c = _broker.classForName(className);
       } catch (ClassNotFoundException e) {
          _log.warning("Context: Could not locate class for context tool "
          + className);
          return;
       }
       if (key == null || key.equals("")) {
          key = className;
          int start = 0;
          int end = key.length();
          int lastDot = key.lastIndexOf('.');
          if (lastDot != -1) {
             start = lastDot + 1;
          }
          if (key.endsWith(suffix)) {
             end -= suffix.length();
          }
          key = key.substring(start, end);
       }
       
       Object instance = null;
       StringBuffer log = null;
       try {
          Constructor ctor = c.getConstructor(_ctorArgs1);
          Object[] args = new Object[2];
          args[0] = key;
          args[1] = new SubSettings(_broker.getSettings(), key);
          instance = ctor.newInstance(args);
       } catch (Exception e) {
          log = new StringBuffer();
          log.append("Error loading component key=");
          log.append(key);
          log.append(" class=");
          log.append(c.toString());
          log.append("\n");
          log.append("Trying 2-argument constructor: ");
          log.append(e.toString());
          log.append("\n");
       }
       
       if (instance == null) {
          try {
             Constructor ctor = c.getConstructor(_ctorArgs2);
             Object[] args = new Object[1];
             args[0] = key;
             instance = ctor.newInstance(args);
          } catch (Exception e) {
             log.append("Trying 1-argument constructor: ");
             log.append(e.toString());
             log.append("\n");
          }
       }
       
       if (instance == null) {
          try {
             instance = c.newInstance();
          } catch (Exception e) {
             log.append("Trying 0-argument constructor: ");
             log.append(e.toString());
             log.append("\n");
             _log.warning(log.toString());
             return;
          }
       }
       _tools.put(key, instance);
       _log.info("Registered ContextTool " + key);
    }
    
    
    
    //////////////////////////////////////////////////////////////
    
    private org.webmacro.profile.Profile _prof = null;
    
    /**
     * Return true if the Context contains an active profiler, and
     * calls to startTiming/stopTiming will be counted.
     */
    public final boolean isTiming() {
       return (_prof != null);
    }
    
    /**
     * Mark the start of an event for profiling. Note that you MUST
     * call stop() or the results of profiling will be invalid.
     */
    final public void startTiming(String name) {
       if (_prof == null) return;
       _prof.startEvent(name);
    }
    
    /**
     * Same as startTiming(name1 + "(" + arg + ")") but the concatenation
     * of strings and the call to arg.toString() occurs only if profiling
     * is enabled.
     */
    final public void startTiming(String name1, Object arg) {
       if (_prof == null) return;
       _prof.startEvent(name1 + "(" + arg + ")");
    }
    
    /**
     * Same as startTiming(name1 + "(" + arg1 + "," + arg2 + ")") but the
     * concatenation of strings and the call to arg.toString() occurs only
     * if profiling * is enabled.
     */
    final public void startTiming(String name1, Object arg1, Object arg2) {
       if (_prof == null) return;
       _prof.startEvent(name1 + "(" + arg1 + ", " + arg2 + ")");
    }
    
    /**
     * Same as startTiming(name1 + "(" + arg + ")") but the
     * concatenation of strings and the call to toString() occurs only
     * if profiling is enabled.
     */
    final public void startTiming(String name, int arg) {
       if (_prof == null) return;
       _prof.startEvent(name + "(" + arg + ")");
    }
    
    /**
     * Same as startTiming(name1 + "(" + arg + ")") but the
     * concatenation of strings and the call to toString() occurs only
     * if profiling is enabled.
     */
    final public void startTiming(String name, boolean arg) {
       if (_prof == null) return;
       _prof.startEvent(name + "(" + arg + ")");
    }
    
    /**
     * Mark the end of an event for profiling. Note that you MUST
     * HAVE CALLED start() first or the results of profiling will
     * be invalid.
     */
    final public void stopTiming() {
       if (_prof == null) return;
       _prof.stopEvent();
    }
    
    /* Convenience methods for primitive types */
    
    final public void put(Object o, int i)     { put(o, new Integer(i)); }
    final public void put(Object o, byte b)    { put(o, new Byte(b)); }
    final public void put(Object o, short s)   { put(o, new Short(s)); }
    final public void put(Object o, long l)    { put(o, new Long(l)); }
    final public void put(Object o, char c)    { put(o, new Character(c)); }
    final public void put(Object o, float f)   { put(o, new Float(f)); }
    final public void put(Object o, double d)  { put(o, new Double(d)); }
    final public void put(Object o, boolean b) { put(o, new Boolean(b)); }
 }
