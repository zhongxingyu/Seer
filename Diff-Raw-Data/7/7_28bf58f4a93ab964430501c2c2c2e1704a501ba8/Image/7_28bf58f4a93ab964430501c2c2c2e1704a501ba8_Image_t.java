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
 
 package uk.ac.diamond.scisoft.analysis.dataset;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.function.MapToRotatedCartesian;
 import uk.ac.diamond.scisoft.analysis.delaunay_triangulation.Delaunay_Triangulation;
 import uk.ac.diamond.scisoft.analysis.delaunay_triangulation.Point_dt;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Image processing package
  */
 public class Image {
 
 	/**
 	 * Setup the logging facilities
 	 */
 	protected static final Logger logger = LoggerFactory.getLogger(Image.class);
 
 	/**
 	 * Find translation shift between two 2D datasets
 	 * @param a
 	 * @param b
 	 * @param r rectangular region of interest to use for alignment
 	 * @return a vector containing the translation needed to be applied to dataset b to align with dataset a
 	 */
 	public static double[] findTranslation2D(AbstractDataset a, AbstractDataset b, RectangularROI r) {
 		if (a.getRank() != 2 || b.getRank() != 2) {
 			logger.error("Both datasets should be two-dimensional");
 			throw new IllegalArgumentException("Both datasets should be two-dimensional");
 		}
 		AbstractDataset f, g;
 		if (r == null) {
 			f = a;
 			g = b;
 		} else {
 			MapToRotatedCartesian rcmap = new MapToRotatedCartesian(r);
 			f = rcmap.value(a).get(0);
 			g = rcmap.value(b).get(0);
 		}
 
 //		logger.info("f {} {}", f.shape, f.getElementDoubleAbs(0));
 //		logger.info("g {} {}", g.shape, g.getElementDoubleAbs(0));
 
		// subtract mean before correlating
		List<AbstractDataset> corrs = Signal.phaseCorrelate(Maths.subtract(f, f.mean()), Maths.subtract(g, g.mean()), null, true);
 		AbstractDataset pcorr = corrs.get(0);
 		int[] maxpos = pcorr.maxPos(); // peak pos
 		int[] xshape = pcorr.shape;
 		double mvalue = pcorr.max().doubleValue();
 
 		logger.info("Max at {} is {}", maxpos, mvalue);
 		double[] shift = new double[2];
 
 		// Refine shift using inverse of cross-power spectrum
 		// Foroosh et al, "Extension of Phase Correlation to Subpixel Registration",
 		// IEEE Trans. Image Processing, v11n3, 188-200 (2002)
 		AbstractDataset xcorr = corrs.get(1);
 
 		double c0 = xcorr.getDouble(maxpos);
 
 		for (int i = 0; i < 2; i++) {
 			maxpos[i]++;
 			if (maxpos[i] < xshape[i]) {
 				final double c1 = xcorr.getDouble(maxpos);
 				shift[i] = c1/(c1-c0);
 				if (Math.abs(shift[i]) > 1)
 					shift[i] = c1/(c1+c0);
 			} 
 			maxpos[i]--;
 		}
 		logger.info("Partial shift is {}", shift);
 
 		for (int i = 0; i < 2; i++) {
 			shift[i] += maxpos[i];
 			if (shift[i] > xshape[i]/2) {
 				shift[i] -= xshape[i];
 				logger.info("  Unwrapped position to {}", shift[i] );
 			}
 		}
 		logger.info("Shift is {}", shift);
 
 		return shift;
 	}
 	
 	public static AbstractDataset regrid_delaunay(
 			AbstractDataset data, 
 			AbstractDataset x, 
 			AbstractDataset y, 
 			AbstractDataset gridX, 
 			AbstractDataset gridY) {
 		
 		// create a list of all the points
 		ArrayList<Point_dt> points = new ArrayList<Point_dt>();
 		IndexIterator it = data.getIterator();
 		while(it.hasNext()){
 			
 			Point_dt point_dt = new Point_dt(
 					x.getElementDoubleAbs(it.index)*1000000, 
 					y.getElementDoubleAbs(it.index)*1000000,
 					data.getElementDoubleAbs(it.index));
 			points.add(point_dt);
 		}
 		
 		Point_dt[] pointArray = points.toArray(new Point_dt[0]);
 		
 		
 		// create the Delauney_triangulation_Mesh
 		Delaunay_Triangulation dt = new Delaunay_Triangulation(pointArray);
 		
 		IndexIterator itx = gridX.getIterator();
 		DoubleDataset result = new DoubleDataset(gridX.shape[0], gridY.shape[0]);
 		while(itx.hasNext()){
 			int xindex = itx.index;
 			double xpos = gridX.getDouble(xindex);
 			IndexIterator ity = gridY.getIterator();
 			
 			while(ity.hasNext()){
 				int yindex = ity.index;
 				double ypos = gridX.getDouble(yindex);
 				if(dt.contains(xpos, ypos)){
 					result.set(dt.z(xpos, ypos), xindex,yindex);
 				} else {
 					result.set(Double.NaN, xindex,yindex);
 				}
 				
 			}
 		}
 		
 		return result;
 	}
 	
 	public static AbstractDataset regrid_kabsch(
 			AbstractDataset data, 
 			AbstractDataset x, 
 			AbstractDataset y, 
 			AbstractDataset gridX, 
 			AbstractDataset gridY) {
 		
 		// create the output array
 		DoubleDataset result = new DoubleDataset(gridY.shape[0]+1, gridX.shape[0]+1);
 		IntegerDataset count = new IntegerDataset(gridY.shape[0]+1, gridX.shape[0]+1);
 
 		IndexIterator it = data.getIterator();
 		while(it.hasNext()){
 			double xpos = x.getElementDoubleAbs(it.index);
 			double ypos = y.getElementDoubleAbs(it.index);
 			double dvalue = data.getElementDoubleAbs(it.index);
 			int xind = getLowerIndex(xpos,gridX);
 			int yind = getLowerIndex(ypos,gridY);
 			
 			if (xind >= 0 && xind < gridX.getShape()[0]-1 && yind >= 0 && yind < gridY.getShape()[0]-1) {
 			
 				double x1 = gridX.getDouble(xind+1);
 				double x0 = gridX.getDouble(xind);
 				double dx = Math.abs(x1 - x0);
 				double y1 = gridY.getDouble(yind+1);
 				double y0 = gridY.getDouble(yind);
 				double dy = Math.abs(y1 - y0);
 				
 				// now work out the 4 weightings
 				double ux0 = Math.abs(dx - Math.abs(xpos-x0));
 				double uy0 = Math.abs(dy - Math.abs(ypos-y0));
 				double ux1 = Math.abs(dx - Math.abs(xpos-x1));
 				double uy1 = Math.abs(dy - Math.abs(ypos-y1));
 				
 				double area = dx*dy;
 				
 				double w00 = ((ux0*uy0)/area);
 				double w01 = ((ux0*uy1)/area);
 				double w10 = ((ux1*uy0)/area);
 				double w11 = ((ux1*uy1)/area);
 				
 				if (Math.abs(w00+w10+w01+w11 -1.0) > 0.000001) {
 					System.out.println(w00+w10+w01+w11);
 				}
 				
 				double new00 = result.getDouble(yind,xind)+(w00*dvalue);
 				result.set(new00, yind, xind);
 				count.set(count.get(yind, xind)+1, yind, xind);
 				double new01 = result.getDouble(yind,xind+1)+(w01*dvalue);
 				result.set(new01, yind, xind+1);
 				count.set(count.get(yind, xind+1)+1, yind, xind+1);
 				double new10 = result.getDouble(yind+1,xind)+(w10*dvalue);
 				result.set(new10, yind+1, xind);
 				count.set(count.get(yind+1, xind)+1, yind+1, xind);
 				double new11 = result.getDouble(yind+1,xind+1)+(w11*dvalue);
 				result.set(new11, yind+1, xind+1);
 				count.set(count.get(yind+1, xind+1)+1, yind+1, xind+1);
 			}
 		}
 		
 		return result;
 	}
 	
 	private static int getLowerIndex(double point, AbstractDataset axis) {
 		AbstractDataset mins = Maths.abs(Maths.subtract(axis, point));
 		return mins.minPos()[0];
 		
 	}
 	
 	public static AbstractDataset regrid(
 			AbstractDataset data, 
 			AbstractDataset x, 
 			AbstractDataset y, 
 			AbstractDataset gridX, 
 			AbstractDataset gridY) {
 		
 		//return regrid_kabsch(data, x, y, gridX, gridY);
 		
 		try {
 			return InterpolatorUtils.regrid(data, x, y, gridX, gridY);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			logger.error("TODO put description of error here", e);
 		}
 		return null;
 	}
 	
 	public static AbstractDataset medianFilter(AbstractDataset input, int[] kernel) {
 		// check to see if the kernel shape in the correct dimensionality.
 		if (kernel.length != input.getShape().length)
 			throw new IllegalArgumentException("Kernel shape must be the same shape as the input dataset");
 
 		AbstractDataset result = input.clone();
 		int[] offset = kernel.clone();
 		for (int i = 0; i < offset.length; i++) {
 			offset[i] = -kernel[i]/2;
 		}
 		IndexIterator iter = input.getIterator(true);
 		while (iter.hasNext()) {
 			int[] pos = iter.getPos();
 			int[] start = pos.clone();
 			int[] stop = pos.clone();
 			for (int i = 0; i < pos.length; i++) {
 				start[i] = pos[i] + offset[i];
 				stop[i] = start[i] + kernel[i];
 				if (start[i] < 0) start[i] = 0;
 				if (stop[i] >= input.getShape()[i]) stop[i] = input.getShape()[i];
 			}
 			AbstractDataset slice = input.getSlice(start, stop, null);
 			result.set(Stats.median(slice), pos);
 		}
 
 		return result;
 	}
 	
 	public static AbstractDataset convolutionFilter(AbstractDataset input, AbstractDataset kernel) {
 		// check to see if the kernel shape in the correct dimensionality.
 		if (kernel.getShape().length != input.getShape().length)
 			throw new IllegalArgumentException("Kernel shape must be the same shape as the input dataset");
 
 		AbstractDataset result = input.clone();
 		int[] kShape = kernel.getShape();
 		int[] offset = kShape.clone();
 		for (int i = 0; i < offset.length; i++) {
 			offset[i] = -kShape[i] / 2;
 		}
 		IndexIterator iter = input.getIterator(true);
 		while (iter.hasNext()) {
 			int[] pos = iter.getPos();
 			int[] start = pos.clone();
 			int[] stop = pos.clone();
 			int[] kStart = kShape.clone();
 			int[] kStop = kShape.clone();
 			boolean kClipped = false;
 			for (int i = 0; i < pos.length; i++) {
 				start[i] = pos[i] + offset[i];
 				stop[i] = start[i] + kShape[i];
 				kStart[i] = 0;
 				if (start[i] < 0) {
 					kStart[i] = kStart[i] - start[i];
 					start[i] = 0;
 					kClipped = true;
 				}
 				if (stop[i] >= input.getShape()[i]) {
 					kStop[i] = kStop[i] - (stop[i] - input.getShape()[i]);
 					stop[i] = input.getShape()[i];
 					kClipped = true;
 				}
 			}
 			AbstractDataset tempKernel = kernel;
 			if (kClipped)
 				tempKernel = kernel.getSlice(kStart, kStop, null);
 			AbstractDataset slice = input.getSlice(start, stop, null);
 			slice.imultiply(tempKernel);
 			result.set(slice.sum(), pos);
 		}
 
 		return result;
 	}
 	
 	public static AbstractDataset sobelFilter(AbstractDataset input) {
 		//TODO should be extended for Nd but 2D is all that is required for now.
 		if(input.getShape().length != 2) throw new IllegalArgumentException("The sobel filter only works on 2D datasets");
 		DoubleDataset kernel = new DoubleDataset(new double[] {-1,0,1,-2,0,2,-1,0,1}, 3 ,3);
 		AbstractDataset result = convolutionFilter(input, kernel);
 		kernel = new DoubleDataset(new double[] {-1,-2,-1,0,0,0,1,2,1}, 3 ,3);
 		result.iadd(convolutionFilter(input, kernel));
 		return result;
 		
 	}
 
 }
