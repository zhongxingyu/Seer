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
  * Bottom-up merge method sorting algorithm implementation.
  *
  * @see MergeSorter
  *
  * @version 1.02, 20 September 2013
  * @since 15 September 2013
  * @author Ilya Gubarev
  */
 public final class BottomUpMergeSorter extends MergeSorter {
 
     @Override
     public String getInfo() {
         return "Bottom-up merge method";
     }
 
     @Override
     public <T> void sort(ArrayModel<T> target, Comparator<T> comparator,
             ArrayModelFactory arrayFactory, NodeModelFactory nodeFactory,
             Registry recursions) {
         ArrayModel aux = arrayFactory.create(target.getSize());
         for (int sub = 1; sub < target.getSize(); sub += sub) {
             for (int i = 0; i < target.getSize() - sub; i += 2 * sub) {
                 int rightLast = Math.min(i + 2 * sub, target.getSize());
                 merge(target, comparator, i, i + sub - 1, rightLast - 1, aux);
             }
         }
     }
 }
