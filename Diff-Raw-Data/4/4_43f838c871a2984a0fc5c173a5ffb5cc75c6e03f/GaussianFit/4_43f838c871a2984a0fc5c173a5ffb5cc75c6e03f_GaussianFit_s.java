 //FILE:          GaussianFit.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 3/20/13
 //
 // LICENSE:      This file is distributed under the BSD license.
 //               License text is included with the source distribution.
 //
 //               This file is distributed in the hope that it will be useful,
 //               but WITHOUT ANY WARRANTY; without even the implied warranty
 //               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 //
 //               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 //               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
 //
 package edu.uchc.octane;
 
 import org.apache.commons.math3.util.FastMath;
 import org.apache.commons.math3.analysis.MultivariateFunction;
 import org.apache.commons.math3.exception.TooManyEvaluationsException;
 import org.apache.commons.math3.optim.InitialGuess;
 import org.apache.commons.math3.optim.MaxEval;
 import org.apache.commons.math3.optim.PointValuePair;
 import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
 import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
 import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
 
 /**
  * A simple 2D Gaussian fitting module
  * @author Ji-Yu
  *
  */
 public class GaussianFit extends BaseGaussianFit {
 
 	//double [] parameters_;
 	boolean floatingSigma_ = false;
 	
 	double sigma2_;
 
 //	public void setZeroBackground(boolean b) {
 //		bZeroBg_ = b;
 //	}
 
 	/**
	 * Specify whether the image has zero background
	 * @param b True if the image has zero background
 	 */
 	public void setFloatingSigma(boolean b) {
 		floatingSigma_ = b;
 	}
 	
 	/**
 	 * Set initial value for sigma value
 	 * @param sigma The sigma value of the Gaussian function
 	 */
 	public void setPreferredSigmaValue(double sigma) {
 		sigma2_ = sigma * sigma * 2;
 	}
 	
 	/* (non-Javadoc)
 	 * @see edu.uchc.octane.BaseGaussianFit#fit()
 	 */
 	@Override
 	public double [] fit() {
 		
 		double [] initParameters;
 		
 		if (floatingSigma_) {
 			if (bZeroBg_) {
 				initParameters = new double[] {0, 0, pixelValue(0, 0) - bg_, sigma2_};
 			} else {
 				initParameters = new double[] {0, 0, pixelValue(0, 0) - bg_, bg_, sigma2_};
 			}			
 		} else {
 			if (bZeroBg_) {
 				initParameters = new double[] {0, 0, pixelValue(0, 0) - bg_};
 			} else {
 				initParameters = new double[] {0, 0, pixelValue(0, 0) - bg_, bg_};
 			}
 		}
 
 		PowellOptimizer optimizer = new PowellOptimizer(1e-4, 1e-1);
 		
 		MultivariateFunction func = new MultivariateFunction() {
 			@Override
 			public double value(double[] point) {
 				
 				//initParameters = point;
 				double bg = bZeroBg_ ? 0 : point[3]; 
 					
 				double v = 0;
 				
 				for (int xi = - windowSize_; xi < windowSize_; xi ++) {
 					for (int yi = - windowSize_; yi < windowSize_; yi ++) {
 						double delta = getValueExcludingBackground(xi, yi, point) + bg - pixelValue(xi, yi);
 						v += delta * delta;
 					}
 				}
 				return v;
 			}
 		};
 
 		PointValuePair pvp;
 		try {
 			pvp = optimizer.optimize(
 					new ObjectiveFunction(func),
 					new InitialGuess(initParameters),
 					new MaxEval(10000),
 					GoalType.MINIMIZE);
 		} catch (TooManyEvaluationsException e) {
 			return null;
 		}
 		
 		pvp_ = pvp;
 
 		return pvp.getPoint();
 	}
 
 	/* (non-Javadoc)
 	 * @see edu.uchc.octane.BaseGaussianFit#getValueExcludingBackground(int, int, double[])
 	 */
 	@Override
 	public double getValueExcludingBackground(int xi, int yi, double [] p) {
 		double x = ( - p[0] + xi);
 		double y = ( - p[1] + yi);
 		
 		return  FastMath.exp(- (x*x + y*y) / (floatingSigma_ ? p[p.length - 1]:sigma2_)) * p[2];
 	}
 }
