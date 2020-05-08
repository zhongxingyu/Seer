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
 
 import es.csic.iiia.maxsum.factors.cardinality.CardinalityFunction;
 import es.csic.iiia.maxsum.MaxOperator;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Maxsum cardinality factor.
  * <p/>
  * Outgoing messages are computed in <em>O(n*log(n))</em> time, where <em>n</em>
  * is the number of variables connected to this factor.
  *
  * @param <T> Type of the factor's identity.
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class CardinalityFactor<T> extends AbstractFactor<T> {
     private static final Logger LOG = Logger.getLogger(CardinalityFactor.class.getName());
 
     private CardinalityFunction function;
 
     private long constraintChecks;
 
     /**
      * Set the workload function f that returns the cost depending on the
      * number of active variables.
      *
      * @param f function to use
      */
     public void setFunction(CardinalityFunction f) {
         function = f;
     }
 
     @Override
     protected double eval(Map<T, Boolean> values) {
         int nActive = 0;
         for (T neighbor : getNeighbors()) {
             if (values.get(neighbor)) {
                 nActive++;
             }
         }
         return function.getCost(nActive);
     }
 
     @Override
     public long run() {
         constraintChecks = 0;
         final MaxOperator operator = getMaxOperator();
         final int size = getNeighbors().size();
 
         // Group incoming message data for sorting
         int i = 0;
         List<Triplet> vals_and_indices = new ArrayList<Triplet>(size);
         for (T f : getNeighbors()) {
             vals_and_indices.add(new Triplet(f, getMessage(f), i++));
         }
         constraintChecks += size;
 
         // Sort them from best to worst
         Collections.sort(vals_and_indices, Collections.reverseOrder());
 
         // Prepare the reverse index
         int[] ridx = new int[size];
         for (i=0; i<size; i++) {
             ridx[vals_and_indices.get(i).idx] = i;
         }
 
         // Cumulative sums
         double[] cum_ws    = new double[size+1];
         double[] cum_w_s_1 = new double[size+1];
         double[] cum_w_s0  = new double[size+1];
         double[] cum_w_s1  = new double[size+1];
         for (i=0; i<=size; i++) {
 
             if (i==0) {
                 cum_ws[i] = 0;
             } else {
                 cum_ws[i] = vals_and_indices.get(i-1).cost + cum_ws[i-1];
             }
 
             cum_w_s0[i] = cum_ws[i] + function.getCost(i);
 
             if (i>0) {
                 cum_w_s_1[i] = cum_ws[i] + function.getCost(i-1);
             } else {
                 cum_w_s_1[i] = operator.getWorstValue();
             }
 
             if (i<size) {
                 cum_w_s1[i] = cum_ws[i] + function.getCost(i+1);
             } else {
                 cum_w_s1[i] = operator.getWorstValue();
             }
         }
         constraintChecks += size*4;
 
         // Cumulative maxes
         double[] m_1 = new double[size+1];
         double[] m0R = new double[size+1];
         double[] m0L = new double[size+1];
         double[] m1  = new double[size+1];
         for (i=0; i<=size; i++) {
 
             if (i==0) {
                 m1[i]       = cum_w_s1[i];
                 m0L[i]      = cum_w_s0[i];
                 m0R[size-i] = cum_w_s0[size-i];
                 m_1[size-i] = cum_w_s_1[size-i];
             } else {
                 m1[i]       = operator.max(cum_w_s1[i], m1[i-1]);
                 m0L[i]      = operator.max(cum_w_s0[i], m0L[i-1]);
                 m0R[size-i] = operator.max(cum_w_s0[size-i], m0R[size-i+1]);
                 m_1[size-i] = operator.max(cum_w_s_1[size-i], m_1[size-i+1]);
             }
 
         }
         constraintChecks += size*4;
 
         int pos;
         double msg0;
         double msg1;
         for (i=0; i<size; i++) {
 
             pos  = ridx[i];
             msg0 = operator.getWorstValue();
             msg1 = operator.getWorstValue();
 
             if (pos > 0) {
                 msg0 = operator.max(msg0, m0L[pos-1]);
                 msg1 = operator.max(msg1, m1[pos-1]);
             }
 
             msg0 = operator.max(msg0, m_1[pos+1] - vals_and_indices.get(pos).cost);
             msg1 = operator.max(msg1, m0R[pos+1] - vals_and_indices.get(pos).cost);
 
             final T f = vals_and_indices.get(pos).factor;
             LOG.log(Level.FINE, "Msg1: {0}, Msg0:{1}", new Object[]{msg1, msg0});
            send(msg1 - msg0, f);
         }
         constraintChecks += size*3;
 
         return constraintChecks;
     }
 
     private class Triplet implements Comparable<Triplet> {
         public final T factor;
         public final Double cost;
         public final int idx;
         public Triplet(T factor, Double cost, int idx) {
             this.factor = factor;
             this.cost = cost;
             this.idx = idx;
         }
 
         @Override
         public String toString() {
             return "[" + idx + "]" + factor + ":" + cost;
         }
 
         @Override
         public int compareTo(Triplet t) {
             constraintChecks++;
 
             return getMaxOperator().compare(cost, t.cost);
         }
     }
 
 }
