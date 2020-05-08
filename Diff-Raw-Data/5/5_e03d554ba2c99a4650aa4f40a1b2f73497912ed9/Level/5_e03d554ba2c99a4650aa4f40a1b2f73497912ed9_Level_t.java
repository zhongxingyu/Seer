 /*
 This file is part of Legends.
 
     Legends is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Legends is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Legends.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.dawnfirerealms.legends.library.level;
 
 /**
  * @author YoshiGenius
  */
 public class Level extends Exp {
     private int level;
 
     public Level(int level) {
         this.level = level;
     }
 
     @Override
     public double getExp() {
         return super.getExp();
     }
     
     public int getLevel() {
         return this.level;
     }
     
     public static Level max(Level lvl1, Level lvl2) {
         return new Level(Math.max(lvl1.level, lvl2.level));
     }
     
     public static Level min(Level lvl1, Level lvl2) {
         return new Level(Math.min(lvl1.level, lvl2.level));
     }
     
     public static boolean areEqual(Level lvl1, Level lvl2) {
         return (lvl1.level == lvl2.level);
     }
 }
