 package com.codingstory.polaris.parser;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.ImmutableSet;
 
 import java.util.Set;
 
 public class ResolvedTypeReference implements TypeReference {
 
     public static final ResolvedTypeReference BYTE = initPrimitive("byte");
     public static final ResolvedTypeReference SHORT = initPrimitive("short");
     public static final ResolvedTypeReference INTEGER = initPrimitive("int");
     public static final ResolvedTypeReference LONG = initPrimitive("long");
     public static final ResolvedTypeReference FLOAT = initPrimitive("float");
     public static final ResolvedTypeReference DOUBLE = initPrimitive("double");
     public static final ResolvedTypeReference CHAR = initPrimitive("char");
     public static final ResolvedTypeReference BOOLEAN = initPrimitive("boolean");
     public static final Set<ResolvedTypeReference> PRIMITIVES = ImmutableSet.of(
            BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, CHAR, BOOLEAN);
 
     private final FullyQualifiedTypeName name;
 
     public ResolvedTypeReference(FullyQualifiedTypeName name) {
         this.name = name;
     }
 
     public FullyQualifiedTypeName getName() {
         return name;
     }
 
     private static ResolvedTypeReference initPrimitive(String name) {
         return new ResolvedTypeReference(FullyQualifiedTypeName.of(name));
     }
 
     @Override
     public String getUnqualifiedName() {
         return name.getTypeName();
     }
 
     @Override
     public boolean isResoleved() {
         return true;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (!(o instanceof ResolvedTypeReference)) {
             return false;
         }
 
         ResolvedTypeReference that = (ResolvedTypeReference) o;
         return Objects.equal(this.name, that.name);
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(name);
     }
 
     @Override
     public String toString() {
         return Objects.toStringHelper(ResolvedTypeReference.class)
                 .add("name", name)
                 .toString();
     }
 }
