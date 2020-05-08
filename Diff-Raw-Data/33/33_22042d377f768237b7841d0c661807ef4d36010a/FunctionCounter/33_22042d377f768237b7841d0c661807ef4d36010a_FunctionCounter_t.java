 /*
  * Software License Agreement (BSD License)
  * 
  * Copyright (c) 2010, IIIA-CSIC, Artificial Intelligence Research Institute
  * All rights reserved.
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
 
 package es.csic.iiia.dcop.util;
 
 import es.csic.iiia.dcop.CostFunction;
 import es.csic.iiia.dcop.HypercubeCostFunction;
 import es.csic.iiia.dcop.MapCostFunction;
 import es.csic.iiia.dcop.SparseCostFunction;
 
 /**
  *
  * @author Marc Pujol <mpujol at iiia.csic.es>
  */
 public class FunctionCounter {
     private static long nSparse;
     private static long nDense;
     private static long nTuples;
     private static long nNaNTuples;
 
     public static void incSparse() {
         nSparse++;
     }
 
     public static void incDense() {
         nDense++;
     }
 
     public static void countFunction(CostFunction f) {
        nTuples += f.getSize();
        nNaNTuples += f.getNumberOfNoGoods();
         if (f instanceof HypercubeCostFunction) {
             nDense++;
         } else if (f instanceof SparseCostFunction) {
             nSparse++;
         } else if (f instanceof MapCostFunction) {
             nSparse++;
         }
     }
 
     public static double getRatio() {
         System.out.println(
                 "nDense: " + nDense + ", nSparse: " + nSparse +
                ", filteredTuples: " + (nNaNTuples/(double)nTuples)
         );
         double res = nSparse/(double)(nSparse+nDense);
         nSparse = 0;
         nDense = 0;
         nTuples = 0;
         nNaNTuples = 0;
         return res;
     }
 
 }
