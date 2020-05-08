 /*
  * BooleanTypeToken.java
  * Copyright (C) 2012 Pre-Alpha Software
  * All rights reserved.
  */
 
 package com.prealpha.diamond.compiler;
 
 enum BooleanTypeToken implements TypeToken {
     INSTANCE;
 
     @Override
     public boolean isIntegral() {
         return false;
     }
 
     @Override
     public boolean isAssignableTo(TypeToken targetType) {
         return (targetType == INSTANCE);
     }
 }
