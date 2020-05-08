 /*
  * Software License Agreement (BSD License)
  *
  * Copyright 2014 Marc Pujol <mpujol@iiia.csic.es>.
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
 package es.csic.iiia.maxsum.util;
 
 import es.csic.iiia.maxsum.MaxOperator;
 import java.util.Comparator;
 
 /**
  * Comparator that compares {@link NeighborValue} entries according a {@link MaxOperator}.
  *
  * @param <T> Type of the factor's identity.
  * @author Marc Pujol <mpujol@iiia.csic.es>
  */
 public class NeighborComparator<T> implements Comparator<NeighborValue<T>> {
 
     private final MaxOperator operator;
 
     private int constraintChecks;
 
     /**
      * Builds a new comparator of {@link NeighborValue}s.
      * @param operator maximization operator whose order should be used.
      */
     public NeighborComparator(MaxOperator operator) {
         this.operator = operator;
     }
 
     /**
      * Returns the number of comparisons performed by this comparator.
      *
      * @return number of operations performed by this comparator.
      */
     public int getConstraintChecks() {
         return constraintChecks;
     }
 
     @Override
     public int compare(NeighborValue<T> o1, NeighborValue<T> o2) {
         constraintChecks++;
         final int result = operator.compare(o1.value, o2.value);
         // We use the hashcode in this comparator because this comoparator may be used to sort
         // Tree{Map/Set}s. In that case, two NeighborValues with the same value but different
         // neighbor must *not* be treated as equal, which is what we achieve through the hashcode.
         if (result == 0) {
            return Integer.compare(o1.hashCode(), o2.hashCode());
         }
         return result;
     }
 }
