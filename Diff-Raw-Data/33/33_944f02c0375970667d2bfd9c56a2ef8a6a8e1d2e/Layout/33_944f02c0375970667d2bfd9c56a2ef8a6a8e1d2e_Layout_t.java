 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.scope;
 
 import java.io.Serializable;
 
 import org.joe_e.Powerless;
 import org.joe_e.Selfless;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.ref_send.Record;
 import org.ref_send.deserializer;
 import org.ref_send.name;
 
 /**
  * A description of a {@linkplain Scope record}.
  * @param <T> soft type
  */
 public final class
 Layout<T> implements Powerless, Record, Selfless, Serializable {
     static private final long serialVersionUID = 1L;
     
     /**
      * each member name
      * <p>
      * A member name MUST NOT be either <code>null</code> or <code>"@"</code>,
      * and MUST be unique within the layout.
      * </p>
      */
     public final PowerlessArray<String> names;
     
     /**
      * Constructs an instance.
      * @param names {@link #names}
      */
     public @deserializer
     Layout(@name("names") final PowerlessArray<String> names) {
         for (int i = names.length(); 0 != i--;) {
             final String name = names.get(i); 
             if (name.equals("@")) { throw new RuntimeException(); }
             for (int j = i; 0 != j--;) {
                 if (name.equals(names.get(j))) { throw new RuntimeException(); }
             }
         }
 
         this.names = names;
     }
     
     /**
      * Defines a new layout.
      * @param <T> soft type
     * @param names {@link #names}
      */
     static public <T> Layout<T>
     define(final String... names) {
        return new Layout<T>(PowerlessArray.array(names));
     }
     
     // java.lang.Object interface
     
     /**
      * Is the given object the same?
      * @param o compared to object
      * @return <code>true</code> if the same, else <code>false</code>
      */
     public boolean
     equals(final Object o) {
         return null != o && Layout.class == o.getClass() &&
                names.equals(((Layout<?>)o).names);
     }
     
     /**
      * Calculates the hash code.
      */
     public int
     hashCode() { return 0x4EF2A3E5 + names.hashCode(); }
     
     // org.ref_send.scope.Layout interface
     
     /**
      * Constructs a scope.
      * @param values    {@link Scope#values}
      */
     public Scope<T>
     make(final Object... values) {
         return new Scope<T>(this, ConstArray.array(values));
     }
     
     /**
      * Finds the index of the named member.
      * @param name  searched for member name
      * @return found index, or <code>-1</code> if not found
      */
     public int
     find(final String name) {
         int i = names.length();
         while (0 != i-- && !names.get(i).equals(name)) { /**/ }
         return i;
     }
 }
