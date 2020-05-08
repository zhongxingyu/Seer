 /*
  * Copyright (C) 2010 Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fr.jamgotchian.abcd.core.util;
 
 import java.util.Collection;
 
 /**
  *
  * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at gmail.com>
  */
 public class Collections3 {
 
     private Collections3() {
     }
 
     public static <E> boolean equals(Collection<E> coll1, Collection<E> coll2) {
         if (coll1.size() != coll2.size()) {
             return false;
         }
         return coll1.containsAll(coll2);
     }
 
     public static <E extends Integer> int[] toIntArray(Collection<E> coll) {
         int[] array = new int[coll.size()];
         int i = 0;
         for (E e : coll) {
             array[i++] = e.intValue();
         }
         return array;
     }
 }
