 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.fitting;
 
 import edu.stanford.cfuller.imageanalysistools.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.RealVector;
 import org.apache.commons.math.optimization.OptimizationException;
 
 import java.util.Vector;
 
 /**
  * An ImageObject that fits to a three-dimensional gaussian.
  * 
  * @author Colin J. Fuller
  */
 public class GaussianImageObject extends ImageObject {
 
 	static final long serialVersionUID =2L;
 	
     /**
      * Creates an empty GaussianImageObject.
      */
     public GaussianImageObject() {
         init();
     }
 
     /**
      * Creates a GaussianImageObject from the specified masked region in an Image.
      * @param label     The greylevel of the object in the Image mask.
      * @param mask      The mask of objects in the Image, with a unique greylevel assigned to each object.
      * @param parent    The Image that the object occurs in and that is masked by mask.
      * @param p         The parameters associated with this analysis.
      */
     public GaussianImageObject(int label, Image mask, Image parent, ParameterDictionary p) {
         init(label, mask, parent, p);
 
     }
 
 
     /**
      * Fits this object to a 3-dimensional gaussian, and estimates error and goodness of fit.
      * @param p     The parameters for the current analysis.
      * @throws FunctionEvaluationException if there is an error evaluating the gaussian function or the likelihood function used to fit the gaussian.
      * @throws OptimizationException        if the optimizer used to compute the fit raises an exception.
      */
     public void fitPosition(ParameterDictionary p) throws FunctionEvaluationException, OptimizationException {
 
         if (this.sizeInPixels == 0) {
             this.nullifyImages();
             return;
         }
 
         this.fitParametersByChannel = new Vector<RealVector>();
         this.fitR2ByChannel = new Vector<Double>();
         this.fitErrorByChannel = new Vector<Double>();
         this.nPhotonsByChannel = new Vector<Double>();
 
         GaussianFitter3D gf = new GaussianFitter3D();
 
         //System.out.println(this.parent.getDimensionSizes().getZ());
 
         int numChannels = 0;
 
         if (p.hasKey("num_wavelengths")) {
             numChannels = p.getIntValueForKey("num_wavelengths");
         } else {
             numChannels = this.parent.getDimensionSizes().get(ImageCoordinate.C);
         }
 
         //for (int channelIndex = 0; channelIndex < this.parent.getDimensionSizes().getC(); channelIndex++) {
         for (int channelIndex = 0; channelIndex < numChannels; channelIndex++) {
 
             RealVector fitParameters = new ArrayRealVector(7, 0.0);
 
             double ppg = p.getDoubleValueForKey("photons_per_greylevel");
 
             this.parentBoxMin.set(ImageCoordinate.C,channelIndex);
             this.parentBoxMax.set(ImageCoordinate.C,channelIndex + 1);
 
             this.boxImages();
 
             java.util.Vector<Double> x = new java.util.Vector<Double>();
             java.util.Vector<Double> y = new java.util.Vector<Double>();
             java.util.Vector<Double> z = new java.util.Vector<Double>();
             java.util.Vector<Double> f = new java.util.Vector<Double>();
 
 
             for (ImageCoordinate ic : this.parent) {
                 x.add((double) ic.get(ImageCoordinate.X));
                 y.add((double) ic.get(ImageCoordinate.Y));
                 z.add((double) ic.get(ImageCoordinate.Z));
                 f.add(parent.getValue(ic));
             }
 
             xValues = new double[x.size()];
             yValues = new double[y.size()];
             zValues = new double[z.size()];
             functionValues = new double[f.size()];
 
             double xCentroid = 0;
             double yCentroid = 0;
             double zCentroid = 0;
             double totalCounts = 0;
 
 
             
 
             for (int i = 0; i < x.size(); i++) {
                 xValues[i] = x.get(i);
                 yValues[i] = y.get(i);
                 zValues[i] = z.get(i);
                 functionValues[i] = f.get(i)*ppg;
                 xCentroid += xValues[i] * functionValues[i];
                 yCentroid += yValues[i] * functionValues[i];
                 zCentroid += zValues[i] * functionValues[i];
                 totalCounts += functionValues[i];
             }
 
 
             xCentroid /= totalCounts;
             yCentroid /= totalCounts;
             zCentroid /= totalCounts;
 
             //z sometimes seems to be a bit off... trying (20110415) to go back to max value pixel at x,y centroid
 
             int xRound = (int) Math.round(xCentroid);
             int yRound = (int) Math.round(yCentroid);
 
             double maxVal = 0;
             int maxInd = 0;
 
             double minZ = Double.MAX_VALUE;
             double maxZ = 0;
 
             for (int i =0; i < x.size(); i++) {
 
                 if (zValues[i] < minZ) minZ = zValues[i];
                 if (zValues[i] > maxZ) maxZ = zValues[i];
 
                 if (xValues[i] == xRound && yValues[i] == yRound) {
                     if (functionValues[i] > maxVal) {
                         maxVal = functionValues[i];
                         maxInd = (int) zValues[i];
                     }
                 }
             }

             zCentroid = maxInd;  

             //parameter ordering: amplitude, var x-y, var z, x/y/z coords, background
 
             //amplitude: find the max value; background: find the min value
 
 
             double maxValue = 0;
 
             double minValue = Double.MAX_VALUE;
 
 
             for (ImageCoordinate ic : this.parent) {
 
                 if (parent.getValue(ic) > maxValue) maxValue = parent.getValue(ic);
                 if (parent.getValue(ic) < minValue) minValue = parent.getValue(ic);
 
             }
 
 
             fitParameters.setEntry(0, (maxValue-minValue)*0.95);
             fitParameters.setEntry(6, minValue+0.05*(maxValue - minValue));
 
             //positions
 
             fitParameters.setEntry(3, xCentroid);
             fitParameters.setEntry(4, yCentroid);
             fitParameters.setEntry(5, zCentroid);
 
             //variances
 
             final double limitedWidthxy = 200;
             final double limitedWidthz = 500;
 
             double sizex = limitedWidthxy / p.getDoubleValueForKey("pixelsize_nm");
             double sizez = limitedWidthz / p.getDoubleValueForKey("z_sectionsize_nm");
 
             fitParameters.setEntry(1, Math.pow(sizex, 2.0)/2);
             fitParameters.setEntry(2, Math.pow(sizez, 2.0)/2);
 
             //amplitude and background are in arbitrary intensity units; convert to photon counts
 
             fitParameters.setEntry(0, fitParameters.getEntry(0)*ppg);
             fitParameters.setEntry(6, fitParameters.getEntry(6)*ppg);
             
             //do the fit
 
             fitParameters = gf.fit(this, fitParameters, ppg);
 
 
             fitParametersByChannel.add(fitParameters);
             
 //            System.out.println(fitParameters);
 
             //calculate R2
 
             double residualSumSquared = 0;
             double mean = 0;
             double variance = 0;
             double R2 = 0;
 
             double n_photons = 0;
 
             for (int i =0; i < this.xValues.length; i++) {
 
                 residualSumSquared += Math.pow(GaussianFitter3D.fitResidual(functionValues[i], xValues[i], yValues[i], zValues[i], fitParameters), 2);
 
                 mean += functionValues[i];
 
                 n_photons += functionValues[i] - fitParameters.getEntry(6);
 
             }
 
             mean /= functionValues.length;
 
             for (int i =0; i < this.xValues.length; i++) {
                 variance += Math.pow(functionValues[i] - mean, 2);
             }
 
             R2 = 1 - (residualSumSquared/variance);
 
             this.fitR2ByChannel.add(R2);
 
             this.unboxImages();
 
             //calculate fit error
 
             double s_xy = fitParameters.getEntry(1) * Math.pow(p.getDoubleValueForKey("pixelsize_nm"), 2);
             double s_z = fitParameters.getEntry(2) * Math.pow(p.getDoubleValueForKey("z_sectionsize_nm"), 2);
 
             double error = Math.sqrt(2*(2*s_xy + s_z)/(n_photons - 1));
  
             this.fitErrorByChannel.add(error);
             
             this.positionsByChannel.add(fitParameters.getSubVector(3, 3));
             
             this.nPhotonsByChannel.add(n_photons);

         }
 
         this.hadFittingError = false;
         this.nullifyImages();
     }
 
 }
