 
 package org.webmacro.engine;
 
 import org.webmacro.*;
 
 /**
   * A predicate is a term passed to a Directive. It consists of a string 
   * word representing the action to beformed, and a target object on which 
   * to perform the action. The target object can be any kind of WebMacro
   * term. 
   */
public final class Argument
 {
 
    private String _name;
    private Object _value;
 
    /**
      * Create a new predicate
      */
    public Argument(String name, Object value) {
       _name = name;
       _value = value;
    }
 
    /**
      * Return the action code for this predicate
      */
    final public String getName() { return _name; }
 
    /**
      * Return the object on which this predicate operates
      */
    final public Object getValue() { return _value; }
 
    /**
      * Make sure that _value is not a builder! Not a public method,
      * used by Directivebuilder and other things that build Argument
      * lists.
      */
    final void build(BuildContext bc) throws BuildException
    {
       if (_value instanceof Builder) {
          _value = ((Builder) _value).build(bc);
       }
    }
 
 }
