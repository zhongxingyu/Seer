 /*
  * Copyright 2013 Ilya Gubarev.
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
 package com.ilyagubarev.algorithms.sorting.methods;
 
import java.util.Comparator;

 import com.ilyagubarev.algorithms.adt.arrays.ArrayModel;
 import com.ilyagubarev.algorithms.adt.utils.Registry;
 import com.ilyagubarev.algorithms.utils.CommonHelper;
 
 /**
  * Sorting algorithm common implementation.
  *
  * @see Sorter
  *
  * @version 1.06, 20 September 2013
  * @since 07 September 2013
  * @author Ilya Gubarev
  */
 public abstract class AbstractSorter implements Sorter {
 
     @Override
     public void prepare(int n) {
         
     }
 
     /**
      * Checks if the first item is less than the second.
      *
      * @param target target array model.
      * @param comparator an item comparator.
      * @param first an index of the first item.
      * @param second an index of the second item.
      * @return true if the first item is less than the second.
      *
      * @see ArrayModel
      * @see Comparator
      */
     protected final <T> boolean less(ArrayModel<T> target,
             Comparator<T> comparator, int first, int second) {
         T item1 = target.read(first);
         T item2 = target.read(second);
         return CommonHelper.compare(comparator, item1, item2) < 0;
     }
 
     /**
      * Exchanges items of specified indeces with each other.
      *
      * @param target target array model.
      * @param first an index of the first item.
      * @param second an index of the second item.
      *
      * @see ArrayModel
      */
     protected final <T> void swap(ArrayModel<T> target, int first, int second) {
         T buffer = target.read(first);
         target.write(first, target.read(second));
         target.write(second, buffer);
     }
 
     /**
      * Exchanges items with each other if the first is less than the second.
      *
      * @param target target array model.
      * @param comparator an item comparator.
      * @param first an index of the first item.
      * @param second an index of the second item.
      * @return true is specified items are swapped.
      *
      * @see ArrayModel
      * @see Comparator
      */
     protected final <T> boolean swapIfLess(ArrayModel<T> target,
             Comparator<T> comparator, int first, int second) {
         T item1 = target.read(first);
         T item2 = target.read(second);
         if (CommonHelper.compare(comparator, item1, item2) < 0) {
             target.write(first, item2);
             target.write(second, item1);
             return true;
         }
         return false;
     }
 
     /**
      * Registers a recursive call.
      *
      * @param recursions a registry of recursive calls.
      *
      * @see Registry
      */
     protected final void registerRecursiveCall(Registry recursions) {
         recursions.register(1);
     }
 
     /**
      * Registers a recursive return.
      *
      * @param recursions a registry of recursive calls.
      *
      * @see Registry
      */
     protected final void registerRecursiveReturn(Registry recursions) {
         recursions.register(-1);
     }
 }
