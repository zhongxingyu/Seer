 package com.kinnack.nthings.model.level.pushup;
 
 import android.util.Log;
 
 import com.kinnack.nthings.model.level.EasyLevel;
 
 public class InitialEasyLevel extends EasyLevel {
 
     @Override
     public boolean checkLevel(int count_) {
        Log.d("InitialEasyLevel", "Check if "+count_+" < 5 "+(count_<5));
        return count_ < 5;
     }
 
     @Override
     public int getStartWeek() {
         return 1;
     }
 
 }
