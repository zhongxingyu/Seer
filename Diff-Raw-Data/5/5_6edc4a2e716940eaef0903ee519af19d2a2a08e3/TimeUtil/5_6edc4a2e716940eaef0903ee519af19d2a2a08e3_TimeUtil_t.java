 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.classes;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class TimeUtil {
     private final TimeUnit unit;
     private final int amount;
 
     enum TimeUnit {
         SECOND(new String[]{"seconds", "sec", "secs", "s"}, 1, 20),
         MINUTE(new String[]{"minutes", "min", "mins", "m"}, 60, 1200),
         HOUR(new String[]{"hours", "hr", "hrs", "h"}, 3600, 72000),
         DAY(new String[]{"days", "d"}, 86400, 1728000);
 
         private String[] aliases;
         private int seconds;
         private int ticks;
         TimeUnit(String[] aliases, int seconds, int ticks) {
             this.aliases = aliases;
             this.seconds = seconds;
             this.ticks = ticks;
         }
 
         public List<String> getAliases() {
             return Arrays.asList(this.aliases);
         }
 
         public String getName() {
             return this.name().toLowerCase();
         }
 
         public String getPlural() {
             return getAliases().get(0).toLowerCase();
         }
 
         public int getSeconds() {
             return this.seconds;
         }
 
         public int getTicks() {
             return this.ticks;
         }
 
         public static TimeUnit getUnit(String name) {
             for (TimeUnit unit : TimeUnit.values()) {
                 if (unit.getAliases().contains(name.toLowerCase())) {
                     return unit;
                 }
             }
             return null;
         }
     }
 
     public TimeUtil(int seconds) {
         this.unit = TimeUnit.SECOND;
         this.amount = seconds;
     }
 
     public TimeUtil(String timeString) {
         String[] split = timeString.split(" ");
        this.amount = Integer.parseInt(split[0]);
        this.unit = TimeUnit.getUnit(split[1].toLowerCase());
     }
 
     public TimeUtil(int amount, TimeUnit unit) {
         this.amount = amount;
         this.unit = unit;
     }
 
     public TimeUtil(int amount, String unit) {
         this.amount = amount;
         this.unit = TimeUnit.getUnit(unit.toLowerCase());
     }
 
     public TimeUnit getUnit() {
         return this.unit;
     }
 
     public int getAmount() {
         return this.amount;
     }
 
     public int getTicks() {
         return getUnit().getTicks() * getAmount();
     }
 
     public int getSeconds() {
         return getUnit().getSeconds() * getAmount();
     }
 
     @Override
     public String toString() {
         if (getUnit().equals(TimeUnit.SECOND) && getAmount() > 60) {
             /*TODO: Create a seconds to string converter*/
             return null;
         } else if (getAmount() > 1) {
             return getAmount() + " " + getUnit().getPlural();
         } else {
             return getAmount() + " " + getUnit().getName();
         }
     }
 }
