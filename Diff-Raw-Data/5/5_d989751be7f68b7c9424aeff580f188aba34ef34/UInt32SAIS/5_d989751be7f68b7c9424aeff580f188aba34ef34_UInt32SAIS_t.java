 /*--------------------------------------------------------------------------
  *  Copyright 2011 utgenome.org
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
 // genome-weaver Project
 //
 // Uint32SAIS.java
 // Since: 2011/02/17
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align.sais;
 
 import java.util.Arrays;
 
 import org.utgenome.weaver.align.LSeq;
 import org.xerial.util.BitVector;
 
 /**
  * Suffix-array (SA) construction algorithm based on Induced Sorting (IS)
  * 
  * @author leo
  * 
  */
 public class UInt32SAIS
 {
     private final LSeq           T;
     private final long           N;
     private final int            K;
     private final int[]          bucketEnd;
     private LSType               LS;
 
     private final static boolean LType = false;
     private final static boolean SType = true;
 
     public static class LSType
     {
         private final static int BIT_LENGTH = 32;
         private int[]            bitVector;
         private long             size;
 
         public LSType(long n) {
             int blockSize = (int) ((n + BIT_LENGTH - 1) / BIT_LENGTH);
             bitVector = new int[blockSize];
             this.size = n;
         }
 
         public void set(long index, boolean flag) {
             int pos = (int) (index / BIT_LENGTH);
             int offset = (int) (index % BIT_LENGTH);
             int mask = 0x01 << (offset - 1);
 
             if (flag)
                 bitVector[pos] |= mask;
             else
                 bitVector[pos] &= ~mask;
         }
 
         public boolean get(long index) {
             int pos = (int) (index / BIT_LENGTH);
             int offset = (int) (index % BIT_LENGTH);
             int mask = 0x01 << (offset - 1);
             return (bitVector[pos] & mask) != 0;
         }
 
         @Override
         public boolean equals(Object obj) {
             if (!BitVector.class.isInstance(obj))
                 return false;
 
             LSType other = LSType.class.cast(obj);
 
             // compare the size
             if (size() != other.size())
                 return false;
 
             // compare each byte
             int byteLength = byteLength();
 
             for (int i = 0; i < byteLength; i++) {
                 if (this.bitVector[i] != other.bitVector[i])
                     return false;
             }
 
             return true;
         }
 
         private int byteLength() {
             return (int) ((size + BIT_LENGTH - 1) / BIT_LENGTH);
         }
 
         @Override
         public int hashCode() {
             int hashValue = 3;
 
             int byteLength = byteLength();
             for (int i = 0; i < byteLength; i++) {
                 hashValue += hashValue * 137 + bitVector[i];
             }
 
             return hashValue % 1987;
         }
 
         public long size() {
             return size;
         }
 
         public void clear() {
             bitVector = null;
             size = 0;
         }
 
         @Override
         public String toString() {
             StringBuilder buf = new StringBuilder();
             for (int i = 0; i < size; i++)
                 buf.append(get(i) ? "1" : "0");
             return buf.toString();
         }
 
     }
 
     static class ArrayWrap implements LSeq
     {
 
         private final LSeq seq;
         private final long offset;
         private final long length;
 
         public ArrayWrap(LSeq original, long offset) {
             this.seq = original;
             this.offset = offset;
             this.length = original.textSize() - offset;
         }
 
         public ArrayWrap(LSeq original, long offset, long length) {
             this.seq = original;
             this.offset = offset;
             this.length = length;
         }
 
         @Override
         public long lookup(long index) {
             return seq.lookup(index + offset);
         }
 
         @Override
         public long textSize() {
             return length;
         }
 
         @Override
         public void set(long index, long value) {
             seq.set(index + offset, value);
         }
 
         @Override
         public long update(long index, long value) {
             return seq.update(index + offset, value);
         }
 
         @Override
         public String toString() {
             StringBuilder w = new StringBuilder();
             w.append("[");
             for (long i = 0; i < length; ++i) {
                 if (i != 0)
                     w.append(", ");
                 w.append(lookup(i));
             }
             w.append("]");
             return w.toString();
         }
     }
 
     public UInt32SAIS(LSeq T, int K) {
         this.T = T;
         this.N = T.textSize();
         this.K = K;
         this.bucketEnd = new int[K];
         LS = new LSType(N);
     }
 
     public static UInt32Array SAIS(LSeq T, int K) {
         UInt32Array SA = new UInt32Array(T.textSize());
         SAIS(T, SA, K);
         return SA;
     }
 
     public static LSeq SAIS(LSeq T, LSeq SA, int K) {
         UInt32SAIS sais = new UInt32SAIS(T, K);
         sais.SAIS(SA);
         return SA;
     }
 
     public void SAIS(LSeq SA) {
 
         // initialize the suffix array
         for (long i = 0; i < N; ++i)
             SA.set(i, 0);
 
         LS.set(N - 1, SType); // the sentinel 
         LS.set(N - 2, LType);
 
         // set the type of each character
         for (long i = N - 2; i > 0; --i) {
             long x = T.lookup(i);
             long y = T.lookup(i - 1);
             if (x < y)
                 LS.set(i - 1, LType);
             else if (x > y)
                 LS.set(i - 1, SType);
             else
                 LS.set(i - 1, LS.get(i));
         }
 
         // Initialize the buckets. 
         // A bucket is a container of the suffixes sharing the same first character
         Arrays.fill(bucketEnd, 0);
         // Compute the size of each bucket
        for (long i = 0; i < N; ++i) {
             ++bucketEnd[(int) T.lookup(i)];
         }
         // Accumulate the character counts. The bucketStart holds the pointers to beginning of the buckets in SA
         for (int i = 1; i < bucketEnd.length; ++i) {
             bucketEnd[i] += bucketEnd[i - 1];
         }
 
         // Step 1: reduce the problem by at least 1/2 
         // Sort all the S-substrings
 
         // Find LMS characters
         int[] cursorInBucket = Arrays.copyOf(bucketEnd, bucketEnd.length);
        for (long i = 1; i < N; ++i) {
             if (isLMS(i))
                 SA.set(--cursorInBucket[(int) T.lookup(i)], i);
         }
 
         // Induced sorting LMS prefixes
         induceSA(SA);
 
         int numLMS = 0;
         // Compact all the sorted substrings into the first M items of SA
         // 2*M must be not larger than N 
 
         // 
         for (long i = 0; i < N; ++i) {
             long si = SA.lookup(i);
             if (si > 0 && isLMS(si))
                 SA.set(numLMS++, si);
         }
 
         // Initialize the name array buffer
         for (long i = numLMS; i < N; ++i)
             SA.set(i, 0);
 
         // Find the lexicographic names of the LMS substrings
         int name = 1;
         SA.set(numLMS + (SA.lookup(0) / 2), name++);
         for (long i = 1; i < numLMS; ++i) {
             final long prev = SA.lookup(i - 1);
             final long current = SA.lookup(i);
             if (!isEqualLMSSubstring(prev, current)) {
                 name++;
             }
             SA.set(numLMS + (current / 2), name - 1);
         }
 
         for (long i = N - 1, j = N - 1; i >= numLMS; --i) {
             if (SA.lookup(i) != 0)
                 SA.set(j--, SA.lookup(i) - 1);
         }
 
         // Step 2: solve the reduced problem
         // Create SA1, a view of SA[0, numLMS-1]
         LSeq SA1 = new ArrayWrap(SA, 0, numLMS);
         LSeq T1 = new ArrayWrap(SA, N - numLMS, numLMS);
         if (name - 1 != numLMS) {
             new UInt32SAIS(T1, name - 1).SAIS(SA1);
         }
         else {
             // When all LMS substrings have unique names
             for (long i = 0; i < numLMS; i++)
                 SA1.set(T1.lookup(i), i);
         }
 
         // Step 3: Induce SA from SA1
         // Construct P1 using T1 buffer
         for (long i = 1, j = 0; i < N; ++i) {
             if (isLMS(i))
                 T1.set(j++, i); // 
         }
         // Translate short name into the original index at T
         // SA1 now holds the LMS-substring indexes
         for (long i = 0; i < numLMS; ++i) {
             SA1.set(i, T1.lookup(SA1.lookup(i)));
         }
 
         // Step 3-1: Put all the items in SA1 into corresponding S-type buckets of SA
 
         // Clear SA[N1 .. N-1]
         for (long i = numLMS; i < N; ++i) {
             SA.set(i, 0);
         }
         // Put SA1 contents into S-type buckets of SA 
         System.arraycopy(bucketEnd, 0, cursorInBucket, 0, bucketEnd.length);
         for (int i = numLMS - 1; i >= 0; --i) {
             long si = SA1.lookup(i);
             SA.set(i, 0);
             SA.set(--cursorInBucket[(int) T.lookup(si)], si);
         }
         SA.set(0, T.textSize() - 1);
 
         // Step 3-2, 3-3
         induceSA(SA);
 
     }
 
     boolean isLMS(long pos) {
         return LS.get(pos) == SType && LS.get(pos - 1) == LType;
     }
 
     private void induceSA(LSeq SA) {
         int[] cursorInBucket = Arrays.copyOf(bucketEnd, bucketEnd.length);
 
         // induce left
         for (long i = 0; i < N; ++i) {
             long si = SA.lookup(i);
             if (si == 0)
                 continue;
             if (LS.get(si - 1) == LType)
                 SA.set(cursorInBucket[(int) T.lookup(si - 1) - 1]++, si - 1);
         }
 
         // induce right
         System.arraycopy(bucketEnd, 0, cursorInBucket, 0, bucketEnd.length);
         for (long i = N - 1; i >= 0; --i) {
             long si = SA.lookup(i);
             if (si == 0)
                 continue;
 
             if (LS.get(si - 1) == SType)
                 SA.set(--cursorInBucket[(int) T.lookup(si - 1)], si - 1);
         }
     }
 
     boolean isEqualLMSSubstring(long pos1, long pos2) {
         boolean prevLS = SType;
         for (; pos1 < N && pos2 < N; ++pos1, ++pos2) {
             if (T.lookup(pos1) == T.lookup(pos2) && LS.get(pos1) == LS.get(pos2)) {
                 if (prevLS == LType && LS.get(pos1) == SType)
                     return true; // equal LMS substring
                 prevLS = LS.get(pos1);
                 continue;
             }
             else
                 return false;
         }
         return false;
     }
 
 }
