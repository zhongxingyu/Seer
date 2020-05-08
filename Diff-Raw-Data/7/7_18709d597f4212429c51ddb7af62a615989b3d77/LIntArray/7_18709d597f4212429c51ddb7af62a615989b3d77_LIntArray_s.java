 /*--------------------------------------------------------------------------
  *  Copyright 2010 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // UInt32Array.java
 // Since: 2010/11/05
 //
 //--------------------------------------
 package org.utgenome.weaver.align;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.utgenome.weaver.align.LSAIS.LArray;
 
 /**
  * Array capable to store more than 2G (2 x 1024 x 1024 x 1024) entries
  * 
  * @author leo
  * 
  */
 public class LIntArray implements LArray, Iterable<Long>
 {
 
     private final long       size;
 
     private ArrayList<int[]> array;
     private BitVector        flag;
 
     public LIntArray(long size) {
         this.size = size;
         flag = new BitVector(size);
 
         // (flag)|---(array pos)------|---------------------------|
         // (flag)|------(32 bit)-------|------(index: 30bit)------|
         int container = pos(size);
         int remainder = offset(size);
 
         array = new ArrayList<int[]>(container + 1);
         for (int i = 0; i < container; i++) {
             array.add(new int[0x40000000]);
         }
         if (remainder > 0)
             array.add(new int[remainder]);
 
     }
 
     private static int pos(long index) {
         return (int) (index >>> 30);
     }
 
     private static int offset(long index) {
         return (int) (index & 0x3FFFFFFFL);
     }
 
     public long size() {
         return size;
     }
 
     public long get(long index) {
         long v = array.get(pos(index))[offset(index)] & 0xFFFFFFFFL;
         return flag.get(index) ? -v : v;
     }
 
     public void set(long index, long value) {
         flag.setBit(value < 0, index);
         array.get(pos(index))[offset(index)] = (int) Math.abs(value);
     }
 
     @Override
     public long update(long index, long val) {
        if (flag.get(index))
            return array.get(pos(index))[offset(index)] -= (int) (val & 0xFFFFFFFFL);
        else
            return array.get(pos(index))[offset(index)] += (int) (val & 0xFFFFFFFFL);
     }
 
     @Override
     public String toString() {
         StringBuilder b = new StringBuilder();
         int i = 0;
         b.append("[");
         for (long each : this) {
             if (i++ > 0)
                 b.append(", ");
             b.append(each);
         }
         b.append("]");
         return b.toString();
     }
 
     @Override
     public Iterator<Long> iterator() {
         return new Iterator<Long>() {
 
             private long cursor = 0;
 
             @Override
             public boolean hasNext() {
                 return cursor < size;
             }
 
             @Override
             public Long next() {
                 return get(cursor++);
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException("remove");
             }
         };
     }
 
 }
