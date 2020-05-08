 /*
  * IntegralTypeToken.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 enum IntegralTypeToken implements TypeToken {
     SIGNED_SHORT(15), UNSIGNED_SHORT(16), SIGNED_INT(31), UNSIGNED_INT(32), SIGNED_LONG(63), UNSIGNED_LONG(64);
 
     private final int width;
 
     private IntegralTypeToken(int width) {
         this.width = width;
     }
 
     @Override
     public boolean isIntegral() {
         return true;
     }
 
     @Override
     public boolean isAssignableTo(TypeToken targetType) {
         if (!isIntegral()) {
             return false;
         } else {
             IntegralTypeToken integralTarget = (IntegralTypeToken) targetType;
             return (integralTarget.width >= this.width);
         }
     }

    @Override
    public String toString() {
        return name().toLowerCase().replace('_', ' ');
    }
 }
