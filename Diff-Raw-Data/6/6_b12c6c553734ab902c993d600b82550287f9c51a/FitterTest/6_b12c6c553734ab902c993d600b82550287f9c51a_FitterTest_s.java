 /*
  * Copyright 2011 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.fitting;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 
 public class FitterTest {
 	
 	@Test
 	public void testGaussianFit() {
 		double pos = 2.0;
 		double fwhm = 3.0;
 		double area = 4.0;
 		Gaussian gauss = new Gaussian(pos,fwhm,area);
 		DoubleDataset xAxis = DoubleDataset.arange(-10.0,10.0,0.1);
 		DoubleDataset ds = gauss.makeDataset(xAxis);
 		
 		AFunction result = Fitter.GaussianFit(ds, xAxis);
 		
 		Assert.assertEquals(pos, result.getParameterValue(0), 0.1);
 		Assert.assertEquals(fwhm, result.getParameterValue(1), 0.1);
 		Assert.assertEquals(area, result.getParameterValue(2), 0.1);
 	}
 	
 	@Test
 	public void testNDGaussianFit1D() {
 		double pos = 2.0;
 		double fwhm = 3.0;
 		double area = 4.0;
 		Gaussian gauss = new Gaussian(pos,fwhm,area);
 		DoubleDataset xAxis = DoubleDataset.arange(-10.0,10.0,0.1);
 		DoubleDataset ds = gauss.makeDataset(xAxis);
 		
 		NDGaussianFitResult result = Fitter.NDGaussianSimpleFit(ds, xAxis);
 		
 		Assert.assertEquals(pos, result.getPos()[0], 0.1);
 		Assert.assertEquals(fwhm, result.getFwhm()[0], 0.1);
 		Assert.assertEquals(area, result.getArea()[0], 0.1);
 	}
 	
 	@Test
 	public void testNDGaussianFit2D() {
 		double pos1 = 2.0;
 		double fwhm1 = 3.0;
 		double area1 = 4.0;
 		double pos2 = 1.0;
 		double fwhm2 = 1.5;
 		double area2 = 2.0;
 		double xAxisStep = 0.1;
 		double yAxisStep = 0.2;
 		
 		Gaussian gauss1 = new Gaussian(pos1,fwhm1,area1);
 		DoubleDataset xAxis = DoubleDataset.arange(-10.0,10.0,xAxisStep);
 		DoubleDataset ds1 = gauss1.makeDataset(xAxis);
 		
 		Gaussian gauss2 = new Gaussian(pos2,fwhm2,area2);
 		DoubleDataset yAxis = DoubleDataset.arange(-20.0,20.0,yAxisStep);
 		DoubleDataset ds2 = gauss2.makeDataset(yAxis);
 		
 		DoubleDataset ds = (DoubleDataset) AbstractDataset.zeros(new int[] {xAxis.getShape()[0],xAxis.getShape()[0]} , AbstractDataset.FLOAT64);
 		
 		IndexIterator iter = ds.getIterator(true);
 		int[] pos;
 		while(iter.hasNext()) {
 			pos = iter.getPos();
 			ds.set((ds1.getDouble(pos[0])*ds2.getDouble(pos[1])), pos);
 		}
 		
 		NDGaussianFitResult result = Fitter.NDGaussianSimpleFit(ds, xAxis,yAxis);
 		
 		double area = (Double) ds.sum();
 		
 		Assert.assertEquals(pos1, result.getPos()[0], 0.1);
 		Assert.assertEquals(pos2, result.getPos()[1], 0.1);
 		Assert.assertEquals(fwhm1, result.getFwhm()[0], 0.1);
 		Assert.assertEquals(fwhm2, result.getFwhm()[1], 0.1);
 		Assert.assertEquals(area*xAxisStep, result.getArea()[0], 0.1);
 		Assert.assertEquals(area*yAxisStep, result.getArea()[1], 0.1);
 	}
 
 }
