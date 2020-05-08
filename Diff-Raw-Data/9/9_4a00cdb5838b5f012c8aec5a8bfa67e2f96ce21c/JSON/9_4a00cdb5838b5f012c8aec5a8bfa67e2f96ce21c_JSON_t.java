 // Copyright 2002-2007 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.syntax.json;
 
 import java.lang.reflect.Modifier;
 import java.lang.reflect.Proxy;
 
 import org.joe_e.Powerless;
 import org.joe_e.Struct;
 import org.joe_e.array.PowerlessArray;
 import org.joe_e.reflect.Reflection;
 import org.ref_send.brand.Brand;
 import org.ref_send.promise.Promise;
 
 /**
  * Java &lt;=&gt; JSON naming conventions.
  */
 /* package */ final class
 JSON {
     private JSON() {}
 
     /**
      * Enumerate an inheritance chain from [ bottom, top ).
      * @param bottom    bottom of the inheritance chain
      * @param top       top of the inheritance chain
      */
     static public PowerlessArray<String>
     upto(final Class<?> bottom, final Class<?> top) {
         if (bottom == top ||                            // fast path
             Promise.class.isAssignableFrom(bottom) ||   // hide implementation
             Brand.class.isAssignableFrom(bottom)) {     // hide implementation
             return PowerlessArray.array();
         }
 
         // simplify the knot at the top of the world
         final Class<?> limit = Struct.class.isAssignableFrom(bottom) ?
                 Struct.class :
             Proxy.class.isAssignableFrom(bottom) ?
                 Proxy.class :
             RuntimeException.class.isAssignableFrom(bottom) ?
                 (Exception.class.isAssignableFrom(top) ?
                         RuntimeException.class : Exception.class) :
             Exception.class.isAssignableFrom(bottom) ?
                 Throwable.class :
             Object.class;
         final PowerlessArray.Builder<String> r = PowerlessArray.builder(4);
         for (Class<?> i = bottom; limit != i && top != i; i=i.getSuperclass()) {
             ifaces(top, i, r);
         }
         return r.snapshot();
     }
 
     /**
      * List a class and all its implemented interfaces.
      */
     static private void
     ifaces(final Class<?> top, final Class<?> type,
            final PowerlessArray.Builder<String> r) {
         if (Modifier.isPublic(type.getModifiers())) {
             try {
                 if (org.joe_e.Selfless.class != type &&
                     org.joe_e.Equatable.class != type &&
                     org.joe_e.Powerless.class != type &&
                     org.joe_e.Immutable.class != type &&
                     org.ref_send.Record.class != type &&
                     java.lang.Comparable.class != type &&
                     java.io.Serializable.class != type) {r.append(name(type));}
             } catch (final Exception e) {
                 // Skip any type that does not have a deterministic name.
             }
         }
         for (final Class<?> i : type.getInterfaces()) {
            if (!i.isAssignableFrom(top)) {
                 ifaces(top, i, r);
             }
         }
     }
 
     static private final class
     Alias extends Struct implements Powerless {
         final Class<?> type;
         final String name;
 
         Alias(final Class<?> type, final String name) {
             this.type = type;
             this.name = name;
         }
     }
 
     /**
      * custom typenames
      */
     static private final PowerlessArray<Alias> custom = PowerlessArray.array(
         new Alias(Object.class, "object"),
         new Alias(String.class, "string"),
         new Alias(Number.class, "number"),
         new Alias(RuntimeException.class, "Error"),
         new Alias(Exception.class, "Error"),
         new Alias(java.lang.reflect.Method.class, "function"),
         new Alias(Class.class, "class"),
         new Alias(ClassCastException.class, "Mismatch"),
         new Alias(NullPointerException.class, "NaO"),
         new Alias(ArithmeticException.class, "NaN"),
         new Alias(org.joe_e.array.ConstArray.class, "array")
     );
 
     static protected String
     name(final Class<?> type) throws IllegalArgumentException {
         for (final Alias a : custom) {
             if (type == a.type) { return a.name; }
         }
         return Reflection.getName(type).replace('$', '-');
     }
 
     static protected Class<?>
     load(final ClassLoader code,
          final String name) throws ClassNotFoundException {
         for (final Alias a : custom) {
             if (a.name.equals(name)) { return a.type; }
         }
         return "boolean".equals(name)
             ? boolean.class
         : "byte".equals(name)
             ? byte.class
         : "char".equals(name)
             ? char.class
         : "double".equals(name)
             ? double.class
         : "float".equals(name)
             ? float.class
         : "int".equals(name)
             ? int.class
         : "long".equals(name)
             ? long.class
         : "short".equals(name)
             ? short.class
         : "void".equals(name)
             ? void.class
         : code.loadClass(name.replace('-', '$'));
     }
 }
