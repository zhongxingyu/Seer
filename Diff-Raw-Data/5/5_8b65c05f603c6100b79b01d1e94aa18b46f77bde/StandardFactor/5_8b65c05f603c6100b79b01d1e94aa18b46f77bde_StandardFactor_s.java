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
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Implementation of a standard (binary) max-sum factor.
  * <p/>
  * The potential of this factor is represented as an array of costs/utilites, where the list
  * array a cost/utility for each combination of values in the list of neighbors.
  *
  * For example, an array [0, 2, 3, 6, 3, 4, 5, 8] represents the cost/utility table defined below,
  * where <em>x</em>, <em>y</em>, and <em>z</em> are the three neighbors of this factor:
  * <pre>
  * | x | y | z | Cost
  * ---------------------
  * | 0 | 0 | 0 |  0
  * | 0 | 0 | 1 |  2
  * | 0 | 1 | 0 |  3
  * | 0 | 1 | 1 |  6
  * | 1 | 0 | 0 |  3
  * | 1 | 0 | 1 |  4
  * | 1 | 1 | 0 |  5
  * | 1 | 1 | 1 |  8
  * </pre>
  *
  * You must ensure that there are exactly <em>2**len(neighbors)</em> costs/utilities in the
  * potential array.
  *
  * @param <T> Type of the factor's identity.
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class StandardFactor<T> extends AbstractFactor<T> {
 
     private TabularPotential potential;
 
     /**
      * Get the potential of this factor.
      *
      * @see StandardFactor
      * @return array of cost/utilities that define the potential
      */
     public double[] getPotential() {
         return potential.values;
     }
 
     /**
      * Set the potential of this factor.
      *
      * @see StandardFactor
      * @param values array of cost/utilities that define the potential
      */
     public void setPotential(double[] values) {
         this.potential = new TabularPotential(values);
     }
 
     @Override
     protected double eval(Map<T, Boolean> values) {
         final List<T> neighbors = getNeighbors();
         final int nNeighbors = getNeighbors().size();
 
         int index = 0;
        for (int i=nNeighbors-1; i>=0; i--) {
             if (values.get(neighbors.get(i))) {
                index |= 1 << i;
             }
         }
 
         return potential.values[index];
     }
 
     @Override
     protected long iter() {
         final List<T> neighbors = getNeighbors();
         final int nNeighbors = neighbors.size();
 
         // Fetch the list of (ordered) messages
         double[] messages = new double[nNeighbors];
         for (int i=0; i<nNeighbors; i++) {
             messages[i] = getMessage(neighbors.get(i));
         }
 
         for (T neighbor : getNeighbors()) {
             final double m_0 = computeMu(neighbor, false, neighbors, messages);
             final double m_1 = computeMu(neighbor, true, neighbors, messages);
             final double message = m_1 - m_0;
             send(message, neighbor);
         }
 
         return nNeighbors*nNeighbors;
     }
 
     private double computeMu(T neighbor,  boolean value, List<T> neighbors, double[] messages) {
         Iterator<Integer> idxs = potential.getIterator(neighbor, value);
         double max = getMaxOperator().getWorstValue();
         while (idxs.hasNext()) {
             final int idx = idxs.next();
             final double util = computeUtil(idx, neighbor, neighbors, messages);
             max = getMaxOperator().max(util, max);
         }
         return max;
     }
 
     private double computeUtil(int idx, T neighbor, List<T> neighbors, double[] messages) {
         int nVar = neighbors.size()-1, nVarIdx = 1;
         double value = potential.values[idx];
         while (nVar >= 0) {
             final T n = neighbors.get(nVar);
             if (!n.equals(neighbor) && (idx & nVarIdx) != 0) {
                 value += messages[nVar];
             }
 
             nVar--;
             nVarIdx = nVarIdx << 1;
         }
 
         return value;
     }
 
     /**
      * Represents a potential as a table of costs/utilities for each combination of values of
      * a list of neighbors.
      * <p/>
      * For example, a TabularPotential([x, y, z], [0, 2, 3, 6, 3, 4, 5, 8]) represents the table
      * <pre>
      * | x | y | z | Cost
      * ---------------------
      * | 0 | 0 | 0 |  0
      * | 0 | 0 | 1 |  2
      * | 0 | 1 | 0 |  3
      * | 0 | 1 | 1 |  6
      * | 1 | 0 | 0 |  3
      * | 1 | 0 | 1 |  4
      * | 1 | 1 | 0 |  5
      * | 1 | 1 | 1 |  8
      * </pre>
      *
      * You must ensure that there are exactly <pre>2^len(neighbors)</pre> costs/utilities in the
      * values array.
      */
     private class TabularPotential {
         private double[] values;
 
         public TabularPotential(double[] values) {
             this.values = values;
             int size = 1 << getNeighbors().size();
 
             if (size != values.length) {
                 throw new IllegalArgumentException("Expected 2^" + getNeighbors().size() + " values (" +
                         size + "), but the values array contains " + values.length + " values.");
             }
         }
 
         public NeighborIterator getIterator(T neighbor, boolean value) {
             return new NeighborIterator(neighbor, value);
         }
 
         private class NeighborIterator implements Iterator<Integer> {
             int increment;
             int count;
             int index;
 
             public NeighborIterator(T neighbor, boolean value) {
                 // Invert the index so that the first neighbor gets the highest index
                 int neighborNumber = getNeighbors().size() - 1 - getNeighbors().indexOf(neighbor);
                 increment = 1 << neighborNumber;
                 count = increment;
 
                 if (value) {
                     index += increment;
                 }
             }
 
             @Override
             public boolean hasNext() {
                 return index < values.length;
             }
 
             @Override
             public Integer next() {
                 int result = index;
 
                 // Increase the index to the next matching configuration
                 count--; index++;
                 if (count == 0) {
                     count = increment;
                     index += increment;
                 }
 
                 return result;
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException("Not supported yet.");
             }
 
         }
 
     }
 
 }
