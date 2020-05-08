 /*
  * This file is part of AMEE.
  *
  * Copyright (c) 2007, 2008, 2009 AMEE UK LIMITED (help@amee.com).
  *
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 package com.amee.domain.data;
 
 import java.util.*;
 
 /**
  * A Map of {@link ItemValue} instances.
  *
  * The keys will be the {@link ItemValue} paths. The entries will be a Set of {@link ItemValue} instances. The Set will
  * consist of a single entry for single-valued {@link ItemValue} histories.
  *
  */
 @SuppressWarnings("unchecked")
 public class ItemValueMap extends HashMap {
 
     /**
      * Get the head {@link ItemValue} in the historical sequence.
      *
      * @param path - the {@link ItemValue} path.
      * @return the head {@link ItemValue} in the historical sequence.
      */
     public ItemValue get(String path) {
         ItemValue itemValue = null;
         TreeSet<ItemValue> series = (TreeSet<ItemValue>) super.get(path);
         if (series != null) {
             itemValue = series.first();
         }
         return itemValue;
     }
 
     /**
      * Get the list of active {@link ItemValue}s at the passed start Date.
      *
      * @param startDate - the active {@link ItemValue} will be that starting immediately prior-to or on this date.
      * @return the set of active {@link ItemValue}s at the passed start Date.
      */
     public List<ItemValue> getAll(Date startDate) {
         List<ItemValue> itemValues = new ArrayList();
         for(Object path : super.keySet()) {
             itemValues.add(get((String)path, startDate));
         }
         return itemValues;
     }
 
     /**
      * Get all instances of {@link ItemValue} with the passed path.
      *
      * @param path - the {@link ItemValue} path.
      * @return the List of {@link ItemValue}. Will be empty is there exists no {@link ItemValue}s with this path.
      */
     public List<ItemValue> getAll(String path) {
         return new ArrayList((TreeSet<ItemValue>) super.get(path));
     }
 
     /**
      * Get the active {@link ItemValue} at the passed start Date.
      *
      * @param path - the {@link ItemValue} path.
      * @param startDate - the active {@link ItemValue} will be that starting immediately prior-to or on this date.
      * @return the active {@link ItemValue} at the passed start Date.
      */
     public ItemValue get(String path, Date startDate) {
         ItemValue itemValue = null;
         Set<ItemValue> series = (TreeSet<ItemValue>) super.get(path);
         if (series != null) {
             itemValue = find(series, startDate);
         }
         return itemValue;
     }
 
     public void put(String path, ItemValue itemValue) {
         if (!containsKey(path)) {
             super.put(path, new TreeSet<ItemValue>(new Comparator<ItemValue>() {
                 @Override
                 public int compare(ItemValue iv1, ItemValue iv2) {
                     return iv2.getStartDate().compareTo(iv1.getStartDate());
                 }
 
             }));
         }
 
         Set<ItemValue> itemValues = (Set<ItemValue>) super.get(path);
         itemValues.add(itemValue);
     }
 
     /**
     * Return the number of entries stored for a given {@link ItemValueDefinition} path.
      *
     * @param path - the {@link ItemValueDefinition} path.
      *
      * @return the number of entries stored.
      */
     public int size(String path) {
         return getAll(path).size();
     }
 
     // Find the active ItemValue at startDate.
     // The active ItemValue is the one occuring at or immediately before startDate.
     private ItemValue find(Set<ItemValue> itemValues, Date startDate) {
         ItemValue selected = null;
         for (ItemValue itemValue : itemValues) {
             if (!itemValue.getStartDate().after(startDate)) {
                 selected = itemValue;
                 break;
             }
         }
         return selected;
     }
 
 }
