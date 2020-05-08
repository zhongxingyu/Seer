 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2013 Marc Pujol <mpujol@iiia.csic.es>.
  *
  * Redistribution and use of this software in source and binary forms, with or
  * without modification, are permitted provided that the following conditions
  * are met:
  *
  *   Redistributions of source code must retain the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer.
  *
  *   Redistributions in binary form must reproduce the above
  *   copyright notice, this list of conditions and the
  *   following disclaimer in the documentation and/or other
  *   materials provided with the distribution.
  *
  *   Neither the name of IIIA-CSIC, Artificial Intelligence Research Institute
  *   nor the names of its contributors may be used to
  *   endorse or promote products derived from this
  *   software without specific prior written permission of
  *   IIIA-CSIC, Artificial Intelligence Research Institute
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package es.csic.iiia.maxsum.factors;
 
 import java.util.List;
 import java.util.Map;
 
 import es.csic.iiia.maxsum.MaxOperator;
 import es.csic.iiia.maxsum.util.NeighborValue;
 
 /**
  * Max-sum "Two-sided reserve" factor.
  *
  * Given two sets of neighbors A = {a_1, ..., a_p} and B = {b_1, ..., b_q}, this
  * factor tries to ensure that there are at least the same number of neighbors
  * from the first set chosen than from the second one. That is, f(a_1, ..., a_p,
  * b_1, ..., b_q) = 0 if (\sum_i a_i) >= (\sum_j b_j) or (-)\infty otherwise.
  * <p/>
  * Outgoing messages are computed in <em>O(n log(n))</em> time. Where <em>n</em>
  * is the number of elements in the biggest set. That is, n = max(p, q).
  *
  * @param <T> Type of the factor's identity.
  * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
  */
 public class TwoSidedReserveFactor<T> extends AbstractTwoSidedFactor<T> {
 
     @Override
     public long run() {
         final MaxOperator op = getMaxOperator();
         final int nNeighbors = getNeighbors().size();
         final int nElementsB = nNeighbors - nElementsA;
         constraintChecks = 0;
 
         if (nElementsA == 0) {
             for (T neigh : getNeighbors()) {
                 send(op.getWorstValue(), neigh);
             }
 
             return nNeighbors;
         }
 
         // Optimization. If there are no elements in set B, messages for the
         // members in set A is always 0.
         if (nElementsB == 0) {
             for (T neigh : getNeighbors()) {
                 send(0.0, neigh);
             }
 
             return nNeighbors;
         }
 
         final List<NeighborValue<T>> setAPairs = getSortedSetAPairs();
         final List<NeighborValue<T>> setBPairs = getSortedSetBPairs();
 
         final int theta = getTheta(setAPairs, setBPairs);
 
         final double nuAtheta = (theta == 0) ? -op.getWorstValue() : setAPairs
                 .get(theta - 1).value;
         final double nuAthetaPlus = (nElementsA > theta) ? setAPairs.get(theta).value
                 : op.getWorstValue();
         final double nuBtheta = (theta == 0) ? -op.getWorstValue() : setBPairs
                 .get(theta - 1).value;
         final double nuBTthetaPlus = (nElementsB > theta) ? setBPairs
                 .get(theta).value : op.getWorstValue();
 
         final double A = op.max(nuAthetaPlus, -nuBtheta);
         final double B = op.max(0, op.max(nuBTthetaPlus, -nuAtheta));
 
         constraintChecks += 6;
 
         final int nPositiveA = getNPositive(setAPairs);
         if (nPositiveA > theta) {
             for (T neighbor : getNeighbors()) {
                 send(0, neighbor);
             }
         } else {
             final int nActiveA = Math.max(theta, nPositiveA);
             // Send -A to active 'a's
             for (int i = 0; i < nActiveA; i++) {
                 send(-A, setAPairs.get(i).neighbor);
             }
 
             // Send B to inactive 'a's
             for (int i = nActiveA; i < nElementsA; i++) {
                 send(B, setAPairs.get(i).neighbor);
             }
 
             // Send -B to active 'b's
             for (int i = 0; i < theta; i++) {
                 send(-B, setBPairs.get(i).neighbor);
             }
 
             // Send A to inactive 'b's
             for (int i = theta; i < nElementsB; i++) {
                 send(A, setBPairs.get(i).neighbor);
             }
         }
         constraintChecks += nNeighbors;
 
         return constraintChecks;
     }
 
     @Override
    public double eval(Map<T, Boolean> values) {
         final int reserve = getReserve(values);
 
         return (reserve >= 0) ? 0 : getMaxOperator().getWorstValue();
     }
 
     private int getTheta(List<NeighborValue<T>> setAPairs, List<NeighborValue<T>> setBPairs) {
         final MaxOperator op = getMaxOperator();
         final int nElementsB = setBPairs.size();
         final int n = Math.min(nElementsA, nElementsB);
 
         int theta = 0;
         while (theta < n
                 && op.compare(setBPairs.get(theta).value, 0) > 0
                 && op.compare(setBPairs.get(theta).value
                         + setAPairs.get(theta).value, 0) >= 0) {
             theta++;
         }
 
         constraintChecks += theta * 3;
 
         return theta;
     }
 
     private int getNPositive(List<NeighborValue<T>> pairs) {
         final int pairsLength = pairs.size();
         final MaxOperator op = getMaxOperator();
 
         int nPositive = 0;
         while (nPositive < pairsLength
                 && op.compare(pairs.get(nPositive).value, 0) >= 0) {
             nPositive++;
         }
 
         constraintChecks += nPositive;
 
         return nPositive;
     }
 
 }
