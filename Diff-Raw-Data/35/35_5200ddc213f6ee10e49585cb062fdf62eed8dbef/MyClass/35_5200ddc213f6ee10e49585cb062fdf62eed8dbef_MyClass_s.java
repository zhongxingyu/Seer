 package org.my;
 
 class MyClass
 {
  boolean flip = false;
   boolean myMethod(int value)
   {
    boolean result = myOddMethod(2 * value + (flip ? 1 : 0));
     return result;
   }
 
   boolean myOddMethod(int oddOrEven)
   {
       return ((oddOrEven & 1) == 1);
   }
 }
