 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.meiste.greg.ptw;
 
 import android.content.Context;
 import android.content.res.Resources;
 
 import com.google.gson.Gson;
 
 public final class Driver {
     private int mNumber;
     private String mName;
 
     public static Driver newInstance(Context context, int id) {
         String json = context.getResources().getStringArray(R.array.drivers)[id];
         return new Gson().fromJson(json, Driver.class);
     }
 
     public static Driver newInstance(Resources res, int num) {
         String[] drivers = res.getStringArray(R.array.drivers);
         Gson gson = new Gson();
 
         for (String json : drivers) {
             Driver driver = gson.fromJson(json, Driver.class);
             if (driver.getNumber() == num)
                 return driver;
         }
 
        // Now check the inactive drivers array
        drivers = res.getStringArray(R.array.drivers_inactive);
        for (String json : drivers) {
            Driver driver = gson.fromJson(json, Driver.class);
            if (driver.getNumber() == num)
                return driver;
        }

         return null;
     }
 
     public static int getNumDrivers(Context context) {
         return context.getResources().getStringArray(R.array.drivers).length;
     }
 
     public int getNumber() {
         return mNumber;
     }
 
     public String getName() {
         return mName;
     }
 
     @Override
     public String toString() {
         return getName();
     }
 }
