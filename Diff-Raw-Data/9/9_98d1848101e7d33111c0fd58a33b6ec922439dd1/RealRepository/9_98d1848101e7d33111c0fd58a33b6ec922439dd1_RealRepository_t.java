 package com.id.git;
 
 import com.id.util.Util;
 
 public class RealRepository implements Repository {
   @Override
   public String getHead() {
     return "HEAD";
   }
 
   @Override
   public Diff getDiffTo(String rev) {
    return Diff.fromLines(Util.exec("git diff " + getHead()));
   }
 }
