 package util.type;
 
import util.MethodInstance;
 import util.SymbolTable;
 
 public interface Type {
 
     /**
      * Return an array of this type.
      */
     Type arrayOf();
 
     /**
      * Return a <code>dims</code>-array of this type.
      */
     Type arrayOf(int dims);
 
     /**
      * Returns true iff the type t can be coerced to a String in the given
      * Context. If a type can be coerced to a String then it can be
      * concatenated with Strings, e.g. if o is of type T, then the code snippet
      *         "" + o
      * would be allowed.
      */
     boolean canCoerceToString(Type t, SymbolTable c);
     
     /**
      * Return true if this type is equivalent to t.
      * Usually this is the same as equalsImpl(TypeObject), but that
      * method should return true only if the types are
      * <i>structurally equal</i>.
      * @param t Type to compare to
      * @param context TODO
      * @return True if this type is equivalent to t.
      */
     boolean typeEquals(Type t, SymbolTable context);
 
     /**
      * Returns true iff <code>t</code> has the method <code>mi</code>.
      */
     boolean hasMethod(Type t, MethodInstance mi, SymbolTable context);
 
     /**
      * Returns true iff <code>t</code> has a method with name <code>name</code>
      * either defined in <code>t</code> or inherited into it.
      */
    boolean hasMethodNamed(Type t, String name);
     
     /**
      * Returns true iff <code>m1</code> is the same method as <code>m2</code>.
      * @param context TODO
      */
     boolean isSameMethod(MethodInstance m1, MethodInstance m2, SymbolTable context);
     
     
     /**
      * Return true if this type is a subtype of <code>ancestor</code>.
      * @param context TODO
      */
     boolean isSubtype(Type ancestor, SymbolTable context);
     
     
     /**
      * Return true if this type can be cast to <code>toType</code>.
      * @param context TODO
      */
     boolean isCastValid(Type toType, SymbolTable context);
 
     /**
      * Return true if a value of this type can be assigned to a variable of
      * type <code>toType</code>.
      * @param context TODO
      */
     boolean isImplicitCastValid(Type toType, SymbolTable context);
     
     
     
     /**
      * Return true a literal <code>value</code> can be converted to this type.
      * @param context TODO
      */
     boolean numericConversionValid(Object value, SymbolTable context);
 
     /**
      * Return true a literal <code>value</code> can be converted to this type.
      * @param context TODO
      */
     boolean numericConversionValid(long value, SymbolTable context);
 
     /**
      * Return true if a primitive type.
      */
     boolean isJavaPrimitive();
 
     /**
      * Return true if void.
      */
     boolean isVoid();
 
     /**
      * Return true if boolean.
      */
     boolean isBoolean();
 
     /**
      * Return true if char.
      */
     boolean isChar();
 
     /**
      * Return true if byte.
      */
     boolean isByte();
 
     /**
      * Return true if short.
      */
     boolean isShort();
 
     /**
      * Return true if int.
      */
     boolean isInt();
 
     /**
      * Return true if long.
      */
     boolean isLong();
 
     /**
      * Return true if float.
      */
     boolean isFloat();
 
     /**
      * Return true if double.
      */
     boolean isDouble();
 
     /**
      * Return true if UByte
      */
     boolean isUByte();
 
     /**
      * Return true if UShort
      */
     boolean isUShort();
 
     /**
      * Return true if UInt
      */
     boolean isUInt();
 
     /**
      * Return true if ULong
      */
     boolean isULong();
 
     /**
      * Return true if int, short, byte, or char.
      */
     boolean isIntOrLess();
 
     /**
      * Return true if long, int, short, byte, or char.
      */
     boolean isLongOrLess();
 
     /**
      * Return true if double, float, long, int, short, byte, or char.
      */
     boolean isNumeric();
 
     /**
      * Return true if long, int, short, or byte.
      */
     boolean isSignedNumeric();
 
     /**
      * Return true if ulong, uint, ushort, or ubyte.
      */
     boolean isUnsignedNumeric();
 
     /**
      * Return true if a reference type.
      */
     boolean isReference();
 
     /**
      * Return true if a null type.
      */
     boolean isNull();
 
     /**
      * Return true if an array type.
      */
     boolean isArray();
 
     /**
      * Return true if a class type.
      */
     boolean isClass();
 
     /**
      * Return true if a subclass of Throwable.
      */
     boolean isThrowable();
 
     /**
      * Return true if an unchecked exception.
      */
     boolean isUncheckedException();
 
     /**
      * Return true if the types can be compared; that is, if they have
      * the same type system.
      */
     boolean isComparable(Type t);
     
     /**
      * Return true if the type is Any
      */
     boolean isAny();
     
     /**
      * Return true if the type is a type parameter
      */
     boolean isParameterType();
     
     /**
      * Return true if the type is Object
      */
     boolean isObject();
     
     /**
      * Return true if the type is String
      */
     boolean isString();
     
     /**
      * Return true if the type is IndexedMemoryChunk
      */
     boolean isIndexedMemoryChunk();
     
     /**
      * Return true if the type is Runtime
      */
     boolean isRuntime();
 
     /**
      * Return the primitive with the given name.
      */
     
    Type primitiveForName(String name);
     
     /**
      * Yields a string representing this type.  The string
      * should be consistent with equality.  That is,
      * if this.equals(anotherType), then it should be
      * that this.toString().equals(anotherType.toString()).
      *
      * The string does not have to be a legal Java identifier.
      * It is suggested, but not required, that it be an
      * easily human readable representation, and thus useful
      * in error messages and generated output.
      */
     String toString();
 
 }
