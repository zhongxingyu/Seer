 
 package org.webmacro;
 
 import java.util.*;
 import org.webmacro.util.*;
 
 public class Context {
 
    final private Hashtable _locals;
    final private Hashtable _tools;
 
    final private Broker _broker;
    final private Hashtable _toolbox;
    final private Object _bean;
 
    /**
      * Create an empty context--no bean, no tools, just local variables
      * and a broker.
      */
    public Context(final Broker broker) {
       _broker = broker; 
       _bean = null;
       _tools = null;
       _toolbox = null;
       _locals = new Hashtable();
    }
 
    /**
      * Create a new context working from the specified broker with the 
      * tools available in the supplied toolbox Hashtable. If a bean 
      * is specified (bean != null) then it will be used for property
      * introspection, otherwise property introspection will work with 
      * the local variables stored in this context.
      */
    public Context(final Broker broker, final Hashtable toolbox, 
          final Object bean)
    {
       _broker = broker;
       _bean = bean;
       _toolbox = toolbox;
       _tools = new Hashtable();
       _locals = new Hashtable();
    }
 
    private Context(final Broker broker, final Hashtable toolbox, 
          final Object bean, final Hashtable tools)
    {
       _broker = broker;
       _toolbox = toolbox;
       _bean = bean;
       _tools = tools;
       _locals = new Hashtable();
    }
 
    /**
      * Create a context which shares the tools and broker that this 
      * context does, but uses the named bean as the top level object
      * for introspection, and has its own local variables. If the 
      * supplied bean is null, then local variables will be used as 
      * the root of introspection.
      */
    final public Context subContext(Object bean) {
       return new Context(_broker, _toolbox, bean, _tools);
    }
 
    /**
      * Return the root of introspection, the top level bean for this 
      * context which properties reference into. If this returns null, 
      * then properties reference local variables.
      */
    final public Object getBean() {
       return _bean;
    }
 
    /**
      * Retrieve a local value from this Context
      */
    final public Object get(Object name) {
       return _locals.get(name);
    }
 
    /**
      * Set a local value in this Context
      */
    final public void put(Object name, Object value) {
       _locals.put(name,value);
    }
 
    /**
      * Get the local variables as a Hashtable
      */
    final public Hashtable getLocalVariables() {
       return _locals;
    }
 
    /**
      * Return the tool corresponding to the specified tool name, or 
      * null if there isn't one
      */
    final public ContextTool getTool(String name) 
       throws InvalidContextException
    {
       if (_toolbox == null) {
          throw new InvalidContextException("No tools in this context.");
       }
       try {
          ContextTool t = null;
          t = (ContextTool) _tools.get(name);
          if (t == null) {
             t = (ContextTool) _toolbox.get(name);
             if (t != null) {
                _tools.put(name,t.init(this));
             }
          }
          return t;
       } catch (ClassCastException ce) {
          throw new InvalidContextException("Tool" + name  
                + " does not implement the ContextTool interface!");
       }
    }
 
    /**
      * Get the broker that it is in effect for this context
      */
    final public Broker getBroker() {
       return _broker;
    }
 
    /**
      * Get the named property via introspection 
      */
    public final Object getProperty(final Object[] names) 
       throws PropertyException, InvalidContextException
    {
       if (names.length == 0) {
          return null;
       } else if (_bean == null) {
          return PropertyOperator.getProperty(this,get(names[0]),names,1);
       } else {
          return PropertyOperator.getProperty(this,_bean,names);
       }
    }
 
    /**
      * Set the named property via introspection 
      */
    final public boolean setProperty(final Object[] names, final Object value) 
       throws PropertyException, InvalidContextException
    {
       if (names.length == 0) {
          return false;
       } else if (_bean == null) {
          return PropertyOperator.setProperty(this,get(names[0]),names,1,value);
       } else {
          return PropertyOperator.setProperty(this,_bean,names,value);      
       }
    }
 
 }
 
