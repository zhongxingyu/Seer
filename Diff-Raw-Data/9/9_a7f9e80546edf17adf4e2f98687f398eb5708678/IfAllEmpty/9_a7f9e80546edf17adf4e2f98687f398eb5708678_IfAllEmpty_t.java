 package com.thefind.sisyphus.test;
 
 import java.util.*;
 
 /**
  * @author Eric Gaudet
  */
 public class IfAllEmpty
 extends TestColumns
 {
   public IfAllEmpty(String... columns)
   { super(columns); }
 
   @Override
   public boolean eval(String[] that)
   throws EvalException
   {
     for (int i=0 ; i<that.length ; ++i) {
       if (that[i]!=null && !"".equals(that[i])) {
         return false;
       }
     }
     return true;
   }
 
   @Override
   public String toStringWhich()
   { return "==[null]"; }
 }
 
