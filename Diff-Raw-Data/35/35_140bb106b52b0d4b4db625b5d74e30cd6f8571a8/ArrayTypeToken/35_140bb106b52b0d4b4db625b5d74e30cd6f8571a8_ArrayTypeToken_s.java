 /*
  * ArrayTypeToken.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 import static com.google.common.base.Preconditions.*;
 
 final class ArrayTypeToken implements TypeToken {
     private final TypeToken elementType;
 
     ArrayTypeToken(TypeToken elementType) {
         checkNotNull(elementType);
         this.elementType = elementType;
     }
 
     public TypeToken getElementType() {
         return elementType;
     }
 
     @Override
     public boolean isIntegral() {
         return false;
     }
 
     @Override
     public boolean isAssignableTo(TypeToken targetType) {
         return equals(targetType);
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         ArrayTypeToken that = (ArrayTypeToken) o;
 
         if (!elementType.equals(that.elementType)) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         return elementType.hashCode();
     }
 }
