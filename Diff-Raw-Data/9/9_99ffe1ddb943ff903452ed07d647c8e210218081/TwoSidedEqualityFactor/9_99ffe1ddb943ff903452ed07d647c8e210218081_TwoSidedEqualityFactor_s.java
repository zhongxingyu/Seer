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
  * Max-sum "Two-sided equality" factor.
  *
  * Given two sets of neighbors A = {a_1, ..., a_p} and B = {b_1, ..., b_q}, this
  * factor tries to ensure that there are the same number of neighbors from the
  * first set chosen than from the second one. That is, f(a_1, ..., a_p, b_1,
  * ..., b_q) = 0 if (\sum_i a_i) = (\sum_j b_j) or (-)\infty otherwise.
  * <p/>
  * Outgoing messages are computed in <em>O(n log(n))</em> time. Where <em>n</em>
  * is the number of elements in the biggest set. That is, n = max(p, q).
  *
  * @param <T> Type of the factor's identity.
  * @author Toni Penya-Alba <tonipenya@iiia.csic.es>
  */
 public class TwoSidedEqualityFactor<T> extends AbstractTwoSidedFactor<T> {
 
     @Override
     public long run() {
         final MaxOperator op = getMaxOperator();
         final int nNeighbors = getNeighbors().size();
         final int nElementsB = nNeighbors - nElementsA;
         constraintChecks = 0;
 
         if (nElementsA == 0 || nElementsB == 0) {
             for (T neigh : getNeighbors()) {
                 send(op.getWorstValue(), neigh);
             }
 
             return nNeighbors;
         }
 
         final List<NeighborValue<T>> setAPairs = getSortedSetAPairs();
         final List<NeighborValue<T>> setBPairs = getSortedSetBPairs();
 
         final int eta = getEta(setAPairs, setBPairs);
 
         final double nuAEta = (eta == 0) ? -op.getWorstValue() : setAPairs.get(eta-1).value;
         final double nuAEtaPlusOne = (nElementsA > eta) ? setAPairs.get(eta).value : op.getWorstValue();
         final double nuBEta = (eta == 0) ? -op.getWorstValue() : setBPairs.get(eta-1).value;
         final double nuBEtaPlusOne = (nElementsB > eta) ? setBPairs.get(eta).value : op.getWorstValue();
 
         final double tauPlus = -op.max(-nuBEta, nuAEtaPlusOne);
         final double tauMinus = op.max(-nuAEta, nuBEtaPlusOne);
 
         constraintChecks += 6;
 
         // active sellers
         for (int i = 0; i < eta; i++) {
             send(tauPlus, setAPairs.get(i).neighbor);
         }
 
         // inactive sellers
         for (int i = eta; i < nElementsA; i++) {
             send(tauMinus, setAPairs.get(i).neighbor);
         }
 
         // active buyers
         for (int i = 0; i < eta; i++) {
             send(-tauMinus, setBPairs.get(i).neighbor);
         }
 
         // inactive buyers
         for (int i = eta; i < nElementsB; i++) {
             send(-tauPlus, setBPairs.get(i).neighbor);
         }
 
         constraintChecks += nNeighbors;
 
         return constraintChecks;
     }
 
     @Override
    public double eval(Map<T, Boolean> values) {
         final int reserve = getReserve(values);
 
         return (reserve == 0) ? 0 : getMaxOperator().getWorstValue();
     }
 
     private int getEta(List<NeighborValue<T>> setAPairs, List<NeighborValue<T>> setBPairs) {
         final MaxOperator op = getMaxOperator();
         final int nElementsB = setBPairs.size();
         final int n = Math.min(nElementsA, nElementsB);
 
         int eta = 0;
         while (eta < n
                 && op.compare(
                         setBPairs.get(eta).value + setAPairs.get(eta).value, 0) >= 0) {
             eta++;
         }
 
         constraintChecks += eta * 3;
 
         return eta;
     }
 }
