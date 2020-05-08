 /*
  * $Id$
  *
  * Copyright 2003-2005 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.common.collections;
 
 import java.util.AbstractMap;
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
 * This class provides an Map that stores the key/value pair in the order
  * that they were added to the Map.
 * If an entry already exists, the key/pair entry will be inserted at the
 * position of the old one.
  *
  * @version $Revision$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  */
 public class ChainedMap extends AbstractMap {
    
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
    
    //-------------------------------------------------------------------------
    // Constructor
    //-------------------------------------------------------------------------
    
    /**
    * Creates a new instance of ChainedMap
     */
    public ChainedMap() {
    }
    
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
    
    /**
     * The keys of the Map.
     */
    private List _keys = new ArrayList();
    
    /**
    * The key/pair entry of the Map.
     */
    private List _entries = new ArrayList();
    
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public Set entrySet() {
       return new ChainedSet(_entries);
    }
    
    public Collection values() {
       List values = new ArrayList();
       Iterator itEntries = _entries.iterator();
       while (itEntries.hasNext()) {
          EntryMap entry = (EntryMap) itEntries.next();
          values.add(entry.getValue());
       }
       return values;
    }
    
    public Object put(Object key, Object value) {
       int oldKeyPos = _keys.indexOf(key);
       if (oldKeyPos == -1) {
          _keys.add(key);
          _entries.add(new EntryMap(key, value));
          return null;
       } else {
          Object oldValue = ((Map.Entry) _entries.get(oldKeyPos)).getValue();
          _entries.set(oldKeyPos, new EntryMap(key, value));
          return oldValue;
       }
    }
    
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * The <code>Map.Entry</code> for this <code>ChainedMap</code>.
     *
     * @version $Revision$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     */
    private class EntryMap implements Map.Entry {
 
       //-------------------------------------------------------------------------
       // Constructor
       //-------------------------------------------------------------------------
 
       /**
       * Creates a new instance of EntryMap
        */
       public EntryMap(Object key, Object value) {
          _key = key;
          _value = value;
       }
 
       //-------------------------------------------------------------------------
       // Fields
       //-------------------------------------------------------------------------
 
       private Object _key;
 
       private Object _value;
       
       //-------------------------------------------------------------------------
       // Methods
       //-------------------------------------------------------------------------
    
        public Object getKey() {
           return _key;
        }
 
        public Object getValue() {
           return _value;
        }
 
        public Object setValue(Object value) {
           Object oldValue = _value;
           _value = value;
           return oldValue;
        }
 
        public int hashCode() {
           return (_key == null ? 0 : _key.hashCode()) ^
                 (_value == null ? 0 : _value.hashCode());
        }
 
        public boolean equals(Object o) {
           if (!(o instanceof Map.Entry)) {
              return false;
           }
           Map.Entry e2 = (Map.Entry)o;
           return (_key.equals(e2.getKey()))  &&
                  (_value == null ? e2.getValue() == null : _value.equals(e2.getValue()));
        }
    }
    
    /**
     * The <code>ChainedSet</code> used for the {@link #entrySet} method of this ChainedMap.
     *
     * @version $Revision$
     * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
     */
    private class ChainedSet extends AbstractSet {
       
       //-------------------------------------------------------------------------
       // Constructor
       //-------------------------------------------------------------------------
 
       /**
       * Creates a new instance of ChainedSet.
        */
       public ChainedSet() {
       }
 
       /**
       * Creates a new instance of ChainedSet.
        *
       * @param c
       *    the collection that contain the values of the set, never <code>null</code>.
        */
       public ChainedSet(Collection collection) {
          Iterator itCollection = collection.iterator();
          while (itCollection.hasNext()) {
             _values.add(itCollection.next());
          }
       }
       
       
       //-------------------------------------------------------------------------
       // Fields
       //-------------------------------------------------------------------------
 
       /**
        * The values of the set.
        */
       private List _values = new ArrayList();
 
       //-------------------------------------------------------------------------
       // Methods
       //-------------------------------------------------------------------------
    
       public int size() {
          return _values.size();
       }
       
       public Iterator iterator() {
          return _values.iterator();
       }
    }
 }
