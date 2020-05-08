 //
 // FitterEstimator.java
 //
 
 /*
 SLIMPlugin for combined spectral-lifetime image analysis.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the names of the ImageJDev.org developers nor the
       names of its contributors may be used to endorse or promote products
       derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package loci.slim.fitting.cursor;
 
 import loci.curvefitter.ICurveFitter.FitFunction;
 import loci.curvefitter.IFitterEstimator;
 
 /**
  * This is a class used when doing a fit with an RLD estimate fit combined
  * with a LMA fit.  It allows for some peculiarities of TRI2 to ensure matching
  * results.
  *
  * @author Aivar Grislis grislis at wisc dot edu
  */
 public class FitterEstimator implements IFitterEstimator {
     
     @Override
     public boolean usePrompt() {
         // no prompt for RLD estimate fit
         return false;
     }
 
     @Override
     public int getEstimateStartIndex(double[] yCount, int start, int stop) {
         //System.out.println("FitterEstimator.getEstimateStartIndex " + yCount.length + " " + start + " " + stop);
         if (start < 0) {
             start = 0;
         } //TODO ARG patch for an exception
         // start index changes for RLD estimate fit
         int transEstimateStartIndex = findMax(yCount, start, stop);
         return transEstimateStartIndex;
     }
 
     @Override
     public double getEstimateAValue(double A, double[] yCount, int start, int stop) {
         //System.out.println("FitterEstimator.getEstimateA " + yCount.length + " " + start + " " + stop);
         // A parameter estimate changes for RLD estimate fit
         int transEstimateStartIndex = findMax(yCount, start, stop);
         return yCount[transEstimateStartIndex];
     }
 
     /**
      * Adjusts the mono exponential triple integral fit estimate for further
      * mono, bi, tri, and stretched exponential fitting.
      * 
      * Based on TRfitting.c from TRI2.  In TRI2 this is all table-driven, using
      * "fitType[i].preEstimateFactors[0]" etc.  Comments below give those table
      * entry values.
      * 
      * @param params
      * @param fitFunction
      * @param A
      * @param tau
      * @param Z 
      */
     @Override
     public void adjustEstimatedParams(double[] params, FitFunction fitFunction,
             double A, double tau, double Z) {
         switch (fitFunction) {
             case SINGLE_EXPONENTIAL:
                 params[1] = Z;                // 1.0
                 params[2] = A;                // 1.0
                 params[3] = tau;              // 1.0
                 break;
             case DOUBLE_EXPONENTIAL:
                 params[1] = Z;                // 1.0
                 params[2] = 0.75 * A;         // 0.75
                 params[3] = tau;              // 1.0
                 params[4] = 0.25 * A;         // 0.25
                 params[5] = 0.6666667 * tau;  // 0.6666667
                 break;
             case TRIPLE_EXPONENTIAL:
                 params[1] = Z;                // 1.0
                 params[2] = 0.75 * A;         // 0.75
                 params[3] = tau;              // 1.0
                 params[4] = 1.0 / 6.0 * A;    // 1.0 / 6.0
                 params[5] = 0.6666667 * tau;  // 0.6666667
                 params[6] = 1.0 / 6.0 * A;    // 1.0 / 6.0
                 params[7] = 0.3333333 * tau;  // 0.3333333
                 break;
             case STRETCHED_EXPONENTIAL:
                 params[1] = Z;                // 1.0
                 params[2] = A;                // 1.0
                 params[3] = tau;              // 1.0
                 params[4] = 1.5;              // -1.5
                 break;
         }
     }
     
     public int findMax(double[] value, int start, int stop) {
         int index = start;
         double max = value[start];
         for (int i = start; i < stop; ++i) {
             if (value[i] > max) {
                 max = value[i];
                 index = i;
             }
         }
         return index;
     }
 }
