 package com.pholser.types;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.security.PrivateKey;
 
 import static com.pholser.types.Primitives.*;
 
 public class Types {
     private Types() {
         throw new UnsupportedOperationException();
     }
 
     public static boolean areConvertible(Type to, Type from) {
         if (to.equals(from))
             return true;
 
         Type toEquivalent = Primitives.equivalentOf(to);
         Type fromEquivalent = Primitives.equivalentOf(from);
         if (isPrimitive(toEquivalent))
             return canBeWidened(toEquivalent, fromEquivalent);
         if (to instanceof ParameterizedType)
             return areConvertible((ParameterizedType) to, from);
         return ((Class<?>) to).isAssignableFrom((Class<?>) from);
     }
 
     private static boolean areConvertible(ParameterizedType to, Type from) {
        Type toRawType = to.getRawType();
        return toRawType instanceof Class
                && from instanceof Class
                && ( (Class) toRawType).isAssignableFrom( (Class) from);
     }
 }
