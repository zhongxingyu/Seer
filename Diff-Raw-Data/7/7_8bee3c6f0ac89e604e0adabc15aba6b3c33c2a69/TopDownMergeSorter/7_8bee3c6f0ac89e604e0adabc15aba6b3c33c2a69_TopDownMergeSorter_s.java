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
 import com.ilyagubarev.algorithms.adt.arrays.ArrayModelFactory;
 import com.ilyagubarev.algorithms.adt.nodes.NodeModelFactory;
 import com.ilyagubarev.algorithms.adt.utils.Registry;
 
 /**
  * Top-down merge method sorting algorithm implementation.
  *
  * @see MergeSorter
  *
  * @version 1.04, 20 September 2013
  * @since 11 September 2013
  * @author Ilya Gubarev
  */
 public final class TopDownMergeSorter extends MergeSorter {
 
     @Override
     public String getInfo() {
         return "Top-down merge method";
     }
 
     @Override
     public <T> void sort(ArrayModel<T> target, Comparator<T> comparator,
             ArrayModelFactory arrayFactory, NodeModelFactory nodeFactory,
             Registry recursions) {
         ArrayModel aux = arrayFactory.create(target.getSize());
         sort(target, comparator, 0, target.getSize() - 1, aux, recursions);
     }
 
     private <T> void sort(ArrayModel<T> target, Comparator<T> comparator,
             int leftFirst, int rightLast, ArrayModel<T> aux,
             Registry recursions) {
         if (rightLast <= leftFirst) {
             return;
         }
         int leftLast = leftFirst + (rightLast - leftFirst) / 2;
         registerRecursiveCall(recursions);
         sort(target, comparator, leftFirst, leftLast, aux, recursions);
         registerRecursiveReturn(recursions);
         registerRecursiveCall(recursions);
         sort(target, comparator, leftLast + 1, rightLast, aux, recursions);
         registerRecursiveReturn(recursions);
         merge(target, comparator, leftFirst, leftLast, rightLast, aux);
     }
 }
