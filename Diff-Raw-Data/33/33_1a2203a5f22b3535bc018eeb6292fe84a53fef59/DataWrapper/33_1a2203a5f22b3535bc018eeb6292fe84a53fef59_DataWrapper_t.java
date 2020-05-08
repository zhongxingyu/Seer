 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg & Dynabyte AB.
  * All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.core.js;
 
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 
 /**
  * A JavaScript data object wrapper. This class encapsulates a
  * generic data object and forwards all reads and modifications to
  * the data object.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 class DataWrapper implements Scriptable {
 
     // TODO: Split this object into one DictWrapper and one ArrayWrapper...
 
     /**
      * The encapsulated object.
      */
     private Object data;
 
     /**
      * The object prototype.
      */
     private Scriptable prototype;
 
     /**
      * The object parent scope.
      */
     private Scriptable parentScope;
 
     /**
      * Creates a new JavaScript data object wrapper.
      *
      * @param data           the data object
      * @param parentScope    the object parent scope
      */
     public DataWrapper(Object data, Scriptable parentScope) {
         this.data = data;
         if (data instanceof Array) {
             this.prototype = ScriptableObject.getClassPrototype(parentScope, "Array");
         } else {
             this.prototype = ScriptableObject.getObjectPrototype(parentScope);
         }
         this.parentScope = parentScope;
     }
 
     /**
      * Returns the encapsulated data object.
      *
      * @return the encapsulated data object
      */
     public Object getData() {
         return this.data;
     }
 
     /**
      * Returns the class name.
      *
      * @return the class name
      */
     public String getClassName() {
         return "Data";
     }
 
     /**
      * Returns a named property from this object.
      *
      * @param name           the name of the property
      * @param start          the object in which the lookup began
      *
      * @return the value of the property, or
      *         NOT_FOUND if not found
      */
     public Object get(String name, Scriptable start) {
         if (data instanceof Array) {
             if (name.equals("length")) {
                 return new Integer(((Array) data).size());
             }
         } else if (data instanceof Dict) {
             if (((Dict) data).containsKey(name)) {
                 return JsSerializer.wrap(((Dict) data).get(name), this);
             }
         }
         return NOT_FOUND;
     }
 
     /**
      * Returns an indexed property from this object.
      *
      * @param index          the index of the property
      * @param start          the object in which the lookup began
      *
      * @return the value of the property, or
      *         NOT_FOUND if not found
      */
     public Object get(int index, Scriptable start) {
         if (data instanceof Array) {
             if (((Array) data).containsIndex(index)) {
                 return JsSerializer.wrap(((Array) data).get(index), this);
             }
         }
         return NOT_FOUND;
     }
 
     /**
      * Checks if a property is defined in this object.
      *
      * @param name           the name of the property
      * @param start          the object in which the lookup began
      *
      * @return true if the property is defined, or
      *         false otherwise
      */
     public boolean has(String name, Scriptable start) {
         if (data instanceof Array) {
             if (name.equals("length")) {
                 return true;
             }
         } else if (data instanceof Dict) {
             return (((Dict) data).containsKey(name));
         }
         return false;
     }
 
     /**
      * Checks if an index is defined in this object.
      *
      * @param index          the index of the property
      * @param start          the object in which the lookup began
      *
      * @return true if the index is defined, or
      *         false otherwise
      */
     public boolean has(int index, Scriptable start) {
         return data instanceof Array && (((Array) data).containsIndex(index));
     }
 
     /**
      * Sets a property in this object.
      *
      * @param name           the name of the property
      * @param start          the object in which the lookup began
      * @param value          the value to set
      */
     public void put(String name, Scriptable start, Object value) {
         if (data instanceof Array) {
             if (name.equals("length") && value instanceof Number) {
                 int len = ((Number) value).intValue();
                 while (((Array) data).size() < len) {
                     ((Array) data).add(null);
                 }
                 while (((Array) data).size() > len) {
                     ((Array) data).remove(((Array) data).size() - 1);
                 }
             }
         } else if (data instanceof Dict) {
            ((Dict) data).set(name, value);
         }
     }
 
     /**
      * Sets an indexed property in this object.
      *
      * @param index          the index of the property
      * @param start          the object in which the lookup began
      * @param value          the value to set
      */
     public void put(int index, Scriptable start, Object value) {
         if (data instanceof Array) {
            ((Array) data).set(index, value);
         }
     }
 
     /**
      * Removes a property from this object.
      *
      * @param name           the name of the property
      */
     public void delete(String name) {
         if (data instanceof Dict) {
             ((Dict) data).remove(name);
         }
     }
 
     /**
      * Removes an indexed property from this object.
      *
      * @param index          the index of the property
      */
     public void delete(int index) {
         if (data instanceof Array) {
             // Emulates JS semantics by not renumbering array
             ((Array) data).set(index, null);
         }
     }
 
     /**
      * Returns the prototype of the object.
      *
      * @return the prototype of the object
      */
     public Scriptable getPrototype() {
         return this.prototype;
     }
 
     /**
      * Sets the prototype of the object.
      *
      * @param prototype      the prototype object
      */
     public void setPrototype(Scriptable prototype) {
         this.prototype = prototype;
     }
 
     /**
      * Returns the parent (enclosing) scope of the object.
      *
      * @return the parent (enclosing) scope of the object
      */
     public Scriptable getParentScope() {
         return this.parentScope;
     }
 
     /**
      * Sets the parent (enclosing) scope of the object.
      *
      * @param parentScope    the parent scope of the object
      */
     public void setParentScope(Scriptable parentScope) {
         this.parentScope = parentScope;
     }
 
     /**
      * Returns an array of defined property keys.
      *
      * @return an array of defined property keys
      */
     public Object[] getIds() {
         if (data instanceof Array) {
             return new String[] { "length" };
         } else if (data instanceof Dict) {
             return ((Dict) data).keys();
         } else {
             return new Object[0];
         }
     }
 
     /**
      * Returns the default value of this object.
      *
      * @param typeHint       type type hint class
      *
      * @return the default value of this object
      */
     public Object getDefaultValue(Class typeHint) {
         return ScriptableObject.getDefaultValue(this, typeHint);
     }
 
     /**
      * Checks for JavaScript instance objects (always returns false).
      *
      * @param instance       the object to check
      *
      * @return always returns false as this is not a class
      */
     public boolean hasInstance(Scriptable instance) {
         return false;
     }
 }
