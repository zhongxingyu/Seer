 package com.wealdtech.utils;
 
 import java.util.Collection;
 
 public class GuavaUtils
 {
  public static <T> Collection<T> emptyToNull(final Collection<T> cl)
   {
     if (cl == null || cl.isEmpty())
     {
       return null;
     }
     return cl;
   }
 }
