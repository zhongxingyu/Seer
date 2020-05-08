 package com.kinnack.nthings.model.level;
 
 
 public abstract class MidLevel extends Level {
 
     @Override
     public int getIndex() {
         return 1;
     }
     
     @Override
     public String toString() {
        return "MID on or after week "+getStartWeek();
     }
     
     @Override
     public String getLabel() {
        return "MID";
     }
 
     
 }
