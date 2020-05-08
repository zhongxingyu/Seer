 package veeju.runtime;
 
 import java.util.Map;
 import veeju.forms.AttributeName;
 import veeju.forms.Symbol;
 
 public class String extends Object {
     /**
      * The inner value of the string.
      *
      * @see #getValue()
      */
     protected java.lang.String value;
 
     public String(final Class class_) {
         this(class_, null, null);
     }
 
     public String(final Class class_, final java.lang.String value) {
         this(class_, null, value);
     }
 
    public String(final Class class_, Map<Symbol, Object> attributes) {
         this(class_, attributes, null);
     }
 
    public String(final Class class_, final Map<Symbol, Object> attributes,
                   final java.lang.String value) {
         super(class_, attributes);
         this.value = value;
     }
 
     /**
      * The method for getting {@link #value}.
      *
      * @return the inner value of the string.
      * @see #value
      */
     public java.lang.String getValue() {
         return value == null ? "" : value;
     }
 
     public Object getAttribute(final AttributeName name) {
         if (Symbol.create("size").equals(name)) {
             if (value == null) return new String(class_, "0");
             return new String(class_, java.lang.String.valueOf(value.length()));
         }
         return super.getAttribute(name);
     }
 
     public boolean equals(final java.lang.Object obj) {
         if (!(obj instanceof String)) return false;
         final String o = (String) obj;
         if (value != null && !value.isEmpty()) return value.equals(o.value);
         return o.value == null || o.value.isEmpty();
     }
 
     public int hashCode() {
         return getValue().hashCode();
     }
 
     public java.lang.String toString() {
         return getValue();
     }
 }
 
