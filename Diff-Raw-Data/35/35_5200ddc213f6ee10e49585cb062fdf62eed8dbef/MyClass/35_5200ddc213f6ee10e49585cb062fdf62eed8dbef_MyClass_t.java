 package org.my;
 
 class MyClass
 {
  boolean flip = true;
   boolean myMethod(int value)
   {
    boolean result = myOddMethod(2 * value + (flip ? 0 : 1));
     return result;
   }
 
   boolean myOddMethod(int oddOrEven)
   {
       return ((oddOrEven & 1) == 1);
   }
 }
