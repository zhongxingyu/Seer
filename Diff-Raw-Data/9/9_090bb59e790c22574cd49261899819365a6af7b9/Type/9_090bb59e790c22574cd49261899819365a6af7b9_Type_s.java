 package xhl.core.validator;
 
 import xhl.core.elements.*;
 
 public class Type {
     public static final Type AnyType = new Type("AnyType");
     public static final Type Null = new Type("Null");
     public static final Type Element = new Type("Element");
     public static final Type Boolean = new Type("Boolean");
     public static final Type Number = new Type("Number");
     public static final Type String = new Type("String");
     public static final Type Symbol = new Type("Symbol");
     public static final Type List = new Type("List");
     public static final Type Map = new Type("Map");
     public static final Type Block = new Type("Block");
     public static final Type Combination = new Type("Combination");
 
     private final Symbol name;
 
     public Type(Symbol name) {
         this.name = name;
     }
 
     public Type(String name) {
         this(new Symbol(name));
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Type)
             return name.equals(((Type) obj).name);
         else
             return false;
     }
 
     @Override
     public java.lang.String toString() {
         return name.toString();
     }
 
     /**
      * Check is a type is compatible with <code>that</code> type.
      * @param that
      * @return
      */
     public boolean is(Type that) {
        if (this == AnyType || that == AnyType)
             return true;
         else
             return this.equals(that);
     }
 
     public boolean isNamed(String n) {
         return name.isNamed(n);
     }
 
     public static Type typeOfElement(Expression ex) {
         if (ex instanceof Symbol)
             return Symbol;
         if (ex instanceof SString)
             return String;
         if (ex instanceof SNumber)
             return Number;
         if (ex instanceof SBoolean)
             return Boolean;
         if (ex instanceof SList)
             return List;
         if (ex instanceof SMap)
             return Map;
         if (ex instanceof Combination)
             return Combination;
         if (ex instanceof Block)
             return Block;
         return AnyType;
     }
 }
