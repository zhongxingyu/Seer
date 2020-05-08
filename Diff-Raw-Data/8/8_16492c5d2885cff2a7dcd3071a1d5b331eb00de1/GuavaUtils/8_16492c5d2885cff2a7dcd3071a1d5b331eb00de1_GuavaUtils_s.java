 package com.wealdtech.utils;
 
 import java.util.Collection;
 
 public class GuavaUtils
 {
  public static Collection<?> emptyToNull(final Collection<?> cl)
   {
     if (cl == null || cl.isEmpty())
     {
       return null;
     }
     return cl;
   }
 }
