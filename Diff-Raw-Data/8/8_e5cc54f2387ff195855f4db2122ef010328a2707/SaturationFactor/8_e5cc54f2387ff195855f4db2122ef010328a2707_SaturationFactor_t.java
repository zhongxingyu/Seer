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
 
 import es.csic.iiia.maxsum.MaxOperator;
 import es.csic.iiia.maxsum.util.BestValuesTracker;
 import java.util.Map;
 
 /**
  * Factor that selects the best active neighbor but doesn't constraint others. This is very similar
  * to the {@link SelectorFactor}, but in this case other neighbors are allowed to be active
  * at the same time (without an increase in utility).
  * <p/>
  * Formally, this factor is defined over a sequence of neighbors <em>X = {x_1, ..., x_n}</em> and a
  * sequence of corresponding weights <em>B = {b_1, ..., b_n}, b_i > 0</ëm>. Then, the factor is
  * defined as
  * <pre>
  *      f(X,B) = max_{i=1..n | x_i=1} b_i*x_i
  * </pre>
  *
  * Messages are computed in O(n) time by using the derivation:
  * <pre>
  *      v_{f->x_i} = max(b_i, b*_i) - max(-v*_i, b*_i)
  * </pre>
  * which is a simplification of the message
  * <pre>
  *      \mu_{f->x_i}(0) = max( 0, v*_i + b*_i )
  *      \mu_{f->x_i}(1) = v*_i + max( b_i, b*_i )
  * </pre>
  * where
  * <pre>
  *      b*   = max_i[ b_i + min(v_i, 0) ] ,
  *      i*   = argmax_i[ b_i + min(v_i, 0) ] ,
  *      b**  = max_{i != i*}[ b_i + min(v_i, 0) ] ,
  *      b*_i = b** if i=i*, b* otherwise ,
  *      v*   = \sum max(v_i, 0) ,
  *      v*_i = v* - max(v_i, 0) ,
  * </pre>
  *
  * Intuitively,
  * <ul>
  * <li>{@code b*} is the max b value that can be achieved (penalized if the
  * corresponding  message is negative)</li>
  * <li>{@code i*} is the index of that best b value</li>
  * <li>{@code b**} is the second best b value (penalized if needed)</li>
  * <li>{@code b*_i} is the best (penalized) b value excluding neighbor {@code i}</li>
  * <li>{@code v*} is the sum of all positive messages
  * <li>{@code v*_i} is the sum of all positive messages excluding the one from neighbor {@code i}</li>
  * </ul>
  *
  * @param <T> Type of the factor's identity.
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class SaturationFactor<T> extends IndependentFactor<T> {
 
     /** Tracks the maximum gain from the max(b_i) part */
     private BestValuesTracker<T> max_b;
 
     @Override
     public void setMaxOperator(MaxOperator maxOperator) {
         super.setMaxOperator(maxOperator);
         max_b = new BestValuesTracker<T>(maxOperator);
     }
 
     @Override
     protected double eval(Map<T, Boolean> values) {
         BestValuesTracker<T> chosen = new BestValuesTracker<T>(getMaxOperator());
        chosen.reset();
        boolean empty = true;

         for (T neighbor : getNeighbors()) {
             if (values.get(neighbor)) {
                empty = false;
                 chosen.track(neighbor, getPotential(neighbor));
             }
         }
 
        return empty ? 0 : chosen.getBestValue();
     }
 
     @Override
     protected long iter() {
         final MaxOperator max = getMaxOperator();
 
         double v_positive = 0;
         max_b.reset();
         for (T neighbor : getNeighbors()) {
             final double v_i = getMessage(neighbor);
             final double b_i = getPotential(neighbor);
 
             v_positive += max.max(v_i, 0);
             if (max.compare(v_i, 0) >= 0) {
                 max_b.track(neighbor, b_i);
             } else {
                 max_b.track(neighbor, b_i + v_i);
             }
         }
 
         for (T neighbor : getNeighbors()) {
             final double v_i = getMessage(neighbor);
             final double b_i = getPotential(neighbor);
 
             final double max_b_i = max_b.getComplementary(neighbor);
             final double v_positive_i = v_positive - max.max(v_i, 0);
 
             final double value = max.max(b_i, max_b_i) - max.max(-v_positive_i, max_b_i);
             send(value, neighbor);
         }
 
         return 2*getNeighbors().size();
     }
 
 }
