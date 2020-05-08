 package be.vrt.web.restdc.domain;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Preconditions;
 
 import java.util.Arrays;
 import java.util.Objects;
 
 /**
  * A type holds type information on return and parameter values in a REST API.
  *
  * @author Mike Seghers
  */
 public final class Type {
     public static final int HASH_PRIME = 31;
     private String typeName;
     private String[] genericTypeNames;
 
     private Type(final String typeName, final String[] genericTypeNames) {
         this.typeName = typeName;
        this.genericTypeNames = Arrays.copyOf(genericTypeNames, genericTypeNames.length); ;
     }
 
     public String getTypeName() {
         return typeName;
     }
 
     public String[] getGenericTypeNames() {
         return Arrays.copyOf(genericTypeNames, genericTypeNames.length);
     }
 
     @Override
     public String toString() {
         return com.google.common.base.Objects.toStringHelper(this).add("typeName", typeName).add("genericTypeNames", "[" + Joiner.on(',').skipNulls().join(genericTypeNames) + "]").toString();
     }
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
         if (o == null || getClass() != o.getClass()) {
             return false;
         }
 
         Type type = (Type) o;
         return Arrays.equals(genericTypeNames, type.genericTypeNames) && Objects.equals(typeName, type.typeName);
     }
 
     @Override
     public int hashCode() {
         return Objects.hash(typeName) * HASH_PRIME + Arrays.hashCode(genericTypeNames);
     }
 
     /**
      * {@link Type} builder.
      */
     public static class TypeBuilder {
         private String typeName;
         private String[] genericTypeNames;
 
         /**
          * Construct a new builder, adding required properties.
          *
          * @param typeName the name of the type
          */
         public TypeBuilder(final String typeName) {
             Preconditions.checkArgument(typeName != null && typeName.trim().length() != 0, "You should provide a valid (non-null, non-blank) type name to the TypeBuilder");
             this.typeName = typeName;
         }
 
         /**
          * Adds generic type names to the builder.
          *
          * @param genericTypeNames the generuc type names
          * @return this builder
          */
         public TypeBuilder withGenericTypeNames(final String... genericTypeNames) {
             this.genericTypeNames = genericTypeNames;
             return this;
         }
 
         /**
          * Builds the {@link Type}
          *
          * @return the build type
          */
         public Type build() {
             return new Type(typeName, genericTypeNames == null ? new String[0] : genericTypeNames);
         }
     }
 }
