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
 // SuffixFilter.java
 // Since: 2011/07/27
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.weaver.align.strategy;
 
 import java.util.PriorityQueue;
 
 import org.utgenome.weaver.align.ACGT;
 import org.utgenome.weaver.align.ACGTSequence;
 import org.utgenome.weaver.align.BitVector;
 import org.utgenome.weaver.align.FMIndexOnGenome;
 import org.utgenome.weaver.align.Strand;
 import org.utgenome.weaver.align.SuffixInterval;
 import org.xerial.util.ObjectHandler;
 
 public class SuffixFilter
 {
     private final int             k;
     private final int             m;
     private final FMIndexOnGenome fmIndex;
     private final ACGTSequence    query;
     private final Strand          strand;
 
     private byte[]                chunkStart;
     private byte[]                chunkLen;
 
     // automaton
     private BitVector[]           patternMask;
     private BitVector[][]         automaton;
     private BitVector[]           stairMask;
 
     public static class Candidate
     {
         public final SuffixInterval si;
         public final int            k;
         public final int            offset;
 
         public Candidate(SuffixInterval si, int k, int offset) {
             this.si = si;
             this.k = k;
             this.offset = offset;
         }
     }
 
     public SuffixFilter(int k, FMIndexOnGenome fmIndex, ACGTSequence query, Strand strand) {
         this.k = k;
         this.fmIndex = fmIndex;
         this.query = query;
         this.strand = strand;
 
         this.m = (int) query.textSize();
         int lastChunkSize = (m - k >= 6) ? m * 2 / (k + 2) : m - k;
 
         // prefix length of each chunk of the filter
         chunkStart = new byte[k + 2];
         byte rest = (byte) (m - lastChunkSize);
         if (k == 0)
             chunkStart[0] = rest;
         else {
             for (int i = 0; i <= k; ++i) {
                 chunkStart[i] = (byte) (rest * i / k);
             }
         }
         chunkStart[k + 1] = (byte) m;
 
         chunkLen = new byte[k + 1];
         for (int i = 0; i <= k; ++i) {
             chunkLen[i] = (byte) (chunkStart[i + 1] - chunkStart[i]);
         }
 
         patternMask = new BitVector[ACGT.values().length];
         for (int i = 0; i < patternMask.length; ++i)
             patternMask[i] = new BitVector(m);
 
         // Set bit flag where the character appears.
         for (BitVector each : patternMask) {
             each.clear();
         }
         for (int i = 0; i < m; ++i) {
             ACGT ch = query.getACGT(i);
             patternMask[ch.code].set(i);
         }
 
     }
 
     public SuffixInterval exactMatch(int start, int end) {
         SuffixInterval si = fmIndex.wholeSARange();
         for (int i = start; i < end; ++i) {
             ACGT ch = query.getACGT(i);
             si = fmIndex.forwardSearch(strand, ch, si);
             if (si.isEmpty())
                 break;
         }
         return si;
     }
 
     /**
      * <pre>
      *   *---*---*---*
      *   | \ | \ | \ |
      *   *---*---*---*
      *   | \ | \ | \ |
      *   *---*---*---*
      * 
      * </pre>
      * 
      * 
      * 
      * @param strand
      * @param out
      * @throws Exception
      */
     public void match(ObjectHandler<Candidate> out) throws Exception {
 
         // row-wise simulation of NFA 
         for (int row = 0; row <= k; ++row) {
             if (row == k) {
                 // No more difference is allowed, so try to find a strong match
                 SuffixInterval si = exactMatch(chunkStart[row], chunkStart[row + 1]);
                 if (si.isEmpty())
                     continue;
                 out.handle(new Candidate(si, k, chunkStart[row + 1]));
             }
             else {
                 //rewind = chunkStart[row] + k;
                 simulateNFA(row, out);
             }
         }
 
     }
 
     private void simulateNFA(int row, ObjectHandler<Candidate> out) throws Exception {
 
         // Starts with the chunk position  
         final int startChunk = row;
         // First chunk must have an exact match  
         SuffixInterval si = exactMatch(chunkStart[startChunk], chunkStart[startChunk + 1]);
         if (si.isEmpty()) {
             return;
         }
 
         // Init the automaton
         int maxStep = m - chunkStart[startChunk + 1] + 1;
         int height = k - row + 1; // when k=2 and row=0, the automaton height is 3
         automaton = new BitVector[maxStep][height];
         for (int step = 0; step < maxStep; ++step)
             for (int i = 0; i < height; ++i)
                 automaton[step][i] = new BitVector(m);
 
         // Prepare staircase filter
         stairMask = new BitVector[height];
         for (int i = 0; i < stairMask.length; ++i) {
             stairMask[i] = new BitVector(m)._not()._rshift(chunkStart[startChunk + i]);
         }
 
         // Init the diagonal of the automaton
         byte q = chunkStart[startChunk + 1];
         automaton[0][0].set(q);
         for (int i = 1; i < height; ++i) {
             if (stairMask[i].get(q + 1)) {
                 q++;
                 automaton[0][i].set(q);
             }
             else
                 break;
         }
 
         // Continue the search 
         final int stepOffset = chunkStart[startChunk + 1];
         PriorityQueue<Cursor> searchQueue = new PriorityQueue<Cursor>();
         searchQueue.add(new Cursor(chunkStart[startChunk + 1], si));
         while (!searchQueue.isEmpty()) {
             Cursor current = searchQueue.poll();
            if (current.pos >= m)
                continue;
             for (ACGT ch : ACGT.exceptN) {
                 SuffixInterval nextSi = fmIndex.forwardSearch(strand, ch, current.si);
                 if (nextSi.isEmpty())
                     continue;
 
                 DFSState state = activateNext(current.pos - stepOffset, ch);
                 switch (state.type) {
                 case Finished:
                     out.handle(new Candidate(nextSi, row + state.matchRow, current.pos + 1));
                     break;
                 case NoMatch:
                     break;
                 case Match:
                    searchQueue.add(new Cursor(current.pos + 1, nextSi));
                     break;
                 }
             }
         }
     }
 
     private DFSState activateNext(int step, ACGT ch) {
 
         final int height = automaton[0].length;
 
         final BitVector[] prev = automaton[step];
         final BitVector[] next = automaton[step + 1];
 
         int nm = 0;
 
         // R'_0 = (R_0 << 1) | P[ch]
         BitVector next0 = prev[nm].rshift(1)._and(patternMask[ch.code]);
        if (next0.get(m - 1)) {
             // Found a full match
             return new DFSState(State.Finished, 0);
         }
         if (next0.isZero())
             ++nm;
         next[nm]._or(next0);
 
         for (int i = 1; i < height; ++i) {
             BitVector next_i;
             // R'_{i+1} = ((R_{i+1} << 1) &  P[ch]) | R_i | (R_i << 1) | (R'_i << 1)   
             next_i = prev[i].rshift(1)._and(patternMask[ch.code]);
             next_i._or(prev[i - 1]);
             next_i._or(prev[i - 1].rshift(1));
             next_i._or(next[i - 1].rshift(1));
             // Apply a suffix filter (staircase mask)
             next_i._and(stairMask[i]);
 
             // Found a match
            if (next_i.get(m - 1))
                 return new DFSState(State.Finished, i);
 
             next[i]._or(next_i);
 
             if (nm == i && next_i.isZero())
                 ++nm;
         }
 
         if (nm >= height) {
             return new DFSState(State.NoMatch, -1);
         }
         else {
            return new DFSState(State.Match, nm);
         }
 
     }
 
     private static class Cursor implements Comparable<Cursor>
     {
         public final int            pos;
         public final SuffixInterval si;
 
         public Cursor(int pos, SuffixInterval si) {
             this.pos = pos;
             this.si = si;
         }
 
         @Override
         public int compareTo(Cursor o) {
             int diff = this.pos - o.pos;
             if (diff != 0)
                 return diff;
 
             long rDiff = this.si.range() - o.si.range();
             if (rDiff < 0L)
                 return -1;
             else if (rDiff == 0L)
                 return 0;
             else
                 return 1;
 
         }
 
         @Override
         public String toString() {
             return String.format("pos:%d, si:%s", pos, si);
         }
     }
 
     private enum State {
         Finished, NoMatch, Match
     }
 
     private static class DFSState
     {
         public final State type;
         public final int   matchRow;
 
         public DFSState(State state, int matchRow) {
             this.type = state;
             this.matchRow = matchRow;
         }

        @Override
        public String toString() {
            return String.format("type:%s, match row:%d", type, matchRow);
        }
     }
 
 }
