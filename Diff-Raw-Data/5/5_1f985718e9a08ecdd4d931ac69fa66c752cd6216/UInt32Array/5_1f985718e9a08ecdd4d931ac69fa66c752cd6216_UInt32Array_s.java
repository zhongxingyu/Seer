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
 package org.utgenome.weaver.align.sais;
 
 import java.util.Iterator;
 
 import org.utgenome.weaver.align.LSeq;
 
 /**
  * Array capable to store at most 4G (4 x 1024 x 1024 x 1024) entries
  * 
  * @author leo
  * 
  */
 public class UInt32Array implements LSeq, Iterable<Long>
 {
     public final static long MAX_VALUE = 0xFFFFFFFFL;
     public final static long MAX_INDEX = 4 * 1024 * 1024 * 1024;
 
     private final long       size;
 
     private long[]           rawArray;
 
     public UInt32Array(long size) {
         this.size = size;
 
         int rawArraySize = (int) ((size + 1) / 2);
         rawArray = new long[rawArraySize];
     }
 
     public long textSize() {
         return size;
     }
 
     public long lookup(long index) {
        int pos = (int) index >> 1;
         int offset = (int) (index & 0x01);
         long v = (rawArray[pos] >>> ((1 - offset) * 32)) & 0xFFFFFFFFL;
         return v;
     }
 
     public void set(long index, long value) {
 
        int pos = (int) index >> 1;
         int offset = (int) (index & 0x01);
         rawArray[pos] &= 0xFFFFFFFFL << (offset * 32);
         rawArray[pos] |= value << ((1 - offset) * 32);
     }
 
     @Override
     public long increment(long index, long val) {
         long next = lookup(index) + val;
         set(index, next);
         return next;
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
 
     public long[] toArray() {
         long[] r = new long[(int) textSize()];
         for (long i = 0; i < textSize(); ++i) {
             r[(int) i] = lookup(i);
         }
         return r;
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
                 return lookup(cursor++);
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException("remove");
             }
         };
     }
 
 }
