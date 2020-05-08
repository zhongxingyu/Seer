 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of FifthElement.
  * 
  * FifthElement is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * FifthElement is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with FifthElement.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.FifthElement.data.filter;
 
 import de.minestar.FifthElement.data.Warp;
 
 public class NameFilter implements WarpFilter {
 
     private final String name;
 
     public NameFilter(String name) {
         this.name = name.toLowerCase();
     }
 
     @Override
     public boolean accept(Warp warp) {
         return warp.getName().toLowerCase().contains(name);
     }

    @Override
    public String toString() {
        return "Suchwort: " + name;
    }
 }
