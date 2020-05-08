 //
 // SLIMCurveFitter.java
 //
 
 /*
 Curve Fitter library for fitting exponential decay curves.
 
 Copyright (c) 2010, UW-Madison LOCI
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
     * Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.
     * Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.
     * Neither the name of the UW-Madison LOCI nor the
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
 
 package loci.curvefitter;
 
 //TODO old style:
 import com.sun.jna.Library;
 import com.sun.jna.Native;
 import com.sun.jna.ptr.DoubleByReference;
 //TODO
 
 import ij.IJ;
 
 import imagej.nativelibrary.NativeLibraryUtil;
 
 /**
  * This class is a Java wrapper around the SLIMCurve fitting C code.
  *
  * <dl><dt><b>Source code:</b></dt>
  * <dd><a href="http://dev.loci.wisc.edu/trac/software/browser/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/SLIMCurveFitter.java">Trac</a>,
  * <a href="http://dev.loci.wisc.edu/svn/software/trunk/projects/curve-fitter/src/main/java/loci/curvefitter/SLIMCurveFitter.java">SVN</a></dd></dl>
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 public class SLIMCurveFitter extends AbstractCurveFitter {
     private static Object s_synchObject = new Object();
     private static volatile boolean s_libraryLoaded = false;
     private static boolean s_libraryOnPath = false;
     private static CLibrary s_library;
 
     /**
      * This interface supports loading the library using JNA.
      */
     public interface CLibrary extends Library {
 
         //TODO I'm omitting noise, s/b Poisson or Gaussian with lots of photons???
         //TODO I'm omitting residuals, see below also, same thing...
 
         public int RLD_fit(double xInc,
                            double y[],
                            int fitStart,
                            int fitEnd,
                            double instr[],
                            int nInstr,
                            double sig[],
                            DoubleByReference z,
                            DoubleByReference a,
                            DoubleByReference tau,
                            double fitted[],
                            DoubleByReference chiSquare,
                            double chiSquareTarget
                            );
 
         //TODO I'm omitted noise, see above and restrainType and fitType, for now
         //TODO also covar, alpha, errAxes and chiSqPercent
         //TODO I'm omitting residuals[] aren't residuals = y 0 yFitted??? is there some weighting I'm missing that is time-consuming/impossible to recreate?
 
         public int LMA_fit(double xInc,
                            double y[],
                            int fitStart,
                            int fitEnd,
                            double instr[],
                            int n_instr,
                            double sig[],
                            double param[],
                            int paramFree[],
                            int nParam,
                            double fitted[],
                            DoubleByReference chiSquare,
                            double chiSquareTarget
                            );
     }
 
 
     /**
      * This supports calling the libray using JNI.
      *
      * @param xInc
      * @param y
      * @param fitStart
      * @param fitEnd
      * @param instr
      * @param nInstr
      * @param sig
      * @param z
      * @param a
      * @param tau
      * @param fitted
      * @param chiSquare
      * @param chiSquareTarget
      * @return
      */
 
 
     //TODO I'm omitting noise, s/b Poisson or Gaussian with lots of photons???
    //TODO I'm omitting residuals, see below also, same thing...
 
     private native int RLD_fit(double xInc,
                            double y[],
                            int fitStart,
                            int fitEnd,
                            double instr[],
                            int nInstr,
                            double sig[],
                            double z[],
                            double a[],
                            double tau[],
                            double fitted[],
                            double chiSquare[],
                            double chiSquareTarget
                            );
 
     /**
      * This supports calling the library using JNI.
      *
      * @param xInc
      * @param y
      * @param fitStart
      * @param fitEnd
      * @param instr
      * @param n_instr
      * @param sig
      * @param param
      * @param paramFree
      * @param nParam
      * @param fitted
      * @param chiSquare
      * @param chiSquareTarget
      * @return
      */
     //TODO I'm omitted noise, see above and restrainType and fitType, for now
     //TODO also covar, alpha, errAxes and chiSqPercent
     //TODO I'm omitting residuals[] aren't residuals = y 0 yFitted??? is there some weighting I'm missing that is time-consuming/impossible to recreate?
 
     private native int LMA_fit(double xInc,
                            double y[],
                            int fitStart,
                            int fitEnd,
                            double instr[],
                            int n_instr,
                            double sig[],
                            double param[],
                            int paramFree[],
                            int nParam,
                            double fitted[],
                            double chiSquare[],
                            double chiSquareTarget
                            );
 
     @Override
     public int fitData(ICurveFitData[] dataArray, int start, int stop) {
         int returnValue = 0;
 
         // load the native library, if not already loaded
         if (!s_libraryLoaded) {
 
             synchronized (s_synchObject) {
 
                 // check again to see if some other thread loaded it
                 if (!s_libraryLoaded) {
                     
                     // look for library on path
                     try {
                         IJ.log("Using JNA");
                         s_library = (CLibrary) Native.loadLibrary("slim-curve-1.0-SNAPSHOT", CLibrary.class);
                         s_libraryLoaded = true;
                         s_libraryOnPath = true;
                     }
                     catch (UnsatisfiedLinkError e) {
                         System.out.println("Library not on path " + e.getMessage());
                         IJ.log("Library not on path " + e.getMessage());
                     } 
                 }
 
 
                 if (!s_libraryLoaded) {
                     // look for library in jar, using JNI
                     IJ.log("Using JNI");
                     s_libraryLoaded = NativeLibraryUtil.loadNativeLibrary(this.getClass(), "slim-curve");
                 }
             }
         }
        if (s_libraryLoaded) {
            IJ.log("Library loaded");
        }
        else {
             IJ.log("Unable to do fit.");
             return 0;
         }
 
         //TODO ARG 9/3/10 these issues still need to be addressed:
 
         //TODO ARG since initial x = fit_start * xincr we have to supply the unused portion of y[] before fit_start.
         // if this data were already premassaged it might be better to get rid of fit_start & _end, just give the
         // portion to be fitted and specify an initial x.
         //TODO ARG August use initial X of 0.
 
         boolean[] free = m_free.clone();
         if (FitAlgorithm.RLD.equals(m_fitAlgorithm)) {
             // pure RLD (versus RLD followed by LMA) has no way to fix
             // parameters
             for (int i = 0; i < free.length; ++i) {
                 free[i] = true;
             }
         }
         
         if (s_libraryOnPath) {
             // JNA version
             DoubleByReference chiSquare = new DoubleByReference();
             double chiSquareTarget = 1.0; //TODO s/b specified incoming
             
             if (FitAlgorithm.RLD.equals(m_fitAlgorithm) || FitAlgorithm.RLD_LMA.equals(m_fitAlgorithm)) {
                 // RLD or triple integral fit
                 DoubleByReference z = new DoubleByReference();
                 DoubleByReference a = new DoubleByReference();
                 DoubleByReference tau = new DoubleByReference();
 
                 for (ICurveFitData data: dataArray) {
                     // grab incoming parameters
                     a.setValue(  data.getParams()[2]);
                     tau.setValue(data.getParams()[3]);
                     z.setValue(  data.getParams()[1]);
 
                     // get IRF curve, if any
                     double[] instrumentResponse = getInstrumentResponse(data.getPixels());
                     int nInstrumentResponse = 0;
                     if (null != instrumentResponse) {
                         nInstrumentResponse = instrumentResponse.length;
                     }
 
                     returnValue = s_library.RLD_fit(
                             m_xInc,
                             data.getYCount(),
                             start,
                             stop,
                             instrumentResponse,
                             nInstrumentResponse,
                             data.getSig(),
                             z,
                             a,
                             tau,
                             data.getYFitted(),
                             chiSquare,
                             chiSquareTarget
                             );
                     // set outgoing parameters, unless they are fixed
                     data.getParams()[0] = chiSquare.getValue();
                     if (free[0]) {
                         data.getParams()[1] = z.getValue();
                     }
                     if (free[1]) {
                         data.getParams()[2] = a.getValue();
                     }
                     if (free[2]) {
                         data.getParams()[3] = tau.getValue();
                     }
                 }
             }
             
             if (FitAlgorithm.LMA.equals(m_fitAlgorithm) || FitAlgorithm.RLD_LMA.equals(m_fitAlgorithm)) {
                 // LMA fit
                 for (ICurveFitData data: dataArray) {
                     int nInstrumentResponse = 0;
                     if (null != m_instrumentResponse) {
                         nInstrumentResponse = m_instrumentResponse.length;
                     }
                     returnValue = s_library.LMA_fit(
                             m_xInc,
                             data.getYCount(),
                             start,
                             stop,
                             m_instrumentResponse,
                             nInstrumentResponse,
                             data.getSig(),
                             data.getParams(),
                             toIntArray(m_free),
                             data.getParams().length - 1,
                             data.getYFitted(),
                             chiSquare,
                             chiSquareTarget
                             );
                 }
             }
         }
         else {
             // JNI version
 
             // use array to pass double by reference
             double[] chiSquare = new double[1];
             double chiSquareTarget = 1.0; //TODO s/b specified incoming
             
             if (FitAlgorithm.RLD.equals(m_fitAlgorithm) || FitAlgorithm.RLD_LMA.equals(m_fitAlgorithm)) {
                 // RLD or triple integral fit
 
                 // use arrays to pass double by reference
                 double[] z = new double[1];
                 double[] a = new double[1];
                 double[] tau = new double[1];
 
                 for (ICurveFitData data: dataArray) {
                     // grab incoming parameters
                     a[0] = data.getParams()[2];
                     tau[0] = data.getParams()[3];
                     z[0] = data.getParams()[1];
 
                     // get IRF curve, if any
                     double[] instrumentResponse = getInstrumentResponse(data.getPixels());
                     int nInstrumentResponse = 0;
                     if (null != instrumentResponse) {
                         nInstrumentResponse = instrumentResponse.length;
                     }
 
                     returnValue = RLD_fit(m_xInc,
                             data.getYCount(),
                             start,
                             stop,
                             instrumentResponse,
                             nInstrumentResponse,
                             data.getSig(),
                             z,
                             a,
                             tau,
                             data.getYFitted(),
                             chiSquare,
                             chiSquareTarget
                             );
 
                     // set outgoing parameters, unless they are fixed
                     data.getParams()[0] = chiSquare[0];
                     if (free[0]) {
                         data.getParams()[1] = z[0];
                     }
                     if (free[1]) {
                         data.getParams()[2] = a[0];
                     }
                     if (free[2]) {
                         data.getParams()[3] = tau[0];
                     }
                 }
             }
 
             if (FitAlgorithm.LMA.equals(m_fitAlgorithm) || FitAlgorithm.RLD_LMA.equals(m_fitAlgorithm)) {
                 // LMA fit
                 for (ICurveFitData data: dataArray) {
                     int nInstrumentResponse = 0;
                     if (null != m_instrumentResponse) {
                         nInstrumentResponse = m_instrumentResponse.length;
                     }
                     returnValue = LMA_fit(
                             m_xInc,
                             data.getYCount(),
                             start,
                             stop,
                             m_instrumentResponse,
                             nInstrumentResponse,
                             data.getSig(),
                             data.getParams(),
                             toIntArray(m_free),
                             data.getParams().length - 1,
                             data.getYFitted(),
                             chiSquare,
                             chiSquareTarget
                             );
                 }
             }
         }
 
         //TODO error return deserves much more thought!!  Just returning the last value here!!
         return returnValue;
 
     }
 
     int[] toIntArray(boolean[] booleanArray) {
         int intArray[] = new int[booleanArray.length];
         for (int i = 0; i < booleanArray.length; ++i) {
             intArray[i] = (booleanArray[i] ? 1 : 0);
         }
         return intArray;
     }
 
     double[] floatToDouble(float[] f) {
         double d[] = new double[f.length];
         for (int i = 0; i < f.length; ++i) {
             d[i] = f[i];
         }
         return d;
     }
 
     float[] doubleToFloat(double[] d) {
         float f[] = new float[d.length];
         for (int i = 0; i < d.length; ++i) {
             f[i] = (float) d[i];
         }
         return f;
     }
 
 }
