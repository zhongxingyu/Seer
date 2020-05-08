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
 
 package edu.stanford.cfuller.imageanalysistools.filter;
 
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
 import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
 import edu.stanford.cfuller.imageanalysistools.filter.MaximumSeparabilityThresholdingFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.LabelFilter;
 import edu.stanford.cfuller.imageanalysistools.filter.MaskFilter;
 import edu.stanford.cfuller.imageanalysistools.image.Histogram;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 
 
 /**
  * A Filter that recursively applies a {@link MaximumSeparabilityThresholdingFilter} to each region in an Image mask (and the
  * regions resulting from that thresholding, and so on), until every region in the Image falls withing a specified range of sizes
  * or has been removed due to being smaller than the smallest acceptable size.
  * <p>
  * Part of the implementation of the recursive Otsu thresholding method described in Xiong et al. (DOI: 10.1109/ICIP.2006.312365).
  * <p>
  * The reference Image for the Filter should be set to the original Image (not a mask); its values will be used to set
  * the threshold in the MaximumSeparabilityThresholdingFilter for each region.  This Image will not be changed.
  * <p>
  * The argument to the apply method should be the mask whose regions are to be individually thresholded.  This mask will contain
  * the mask with thresholded regions after this Filter has been implies; no particular labeling of regions is guaranteed.
  *
  *
  * @author Colin J. Fuller
  */
 public class RecursiveMaximumSeparabilityFilter extends Filter {
 
 	final int MAX_RECURSIONS = 1;
 	int numRecs;
 	
 	public RecursiveMaximumSeparabilityFilter() {
 		this.numRecs = 0;
 	}
 
     /**
      * Applies the Filter to an Image mask, replacing its values by the mask that is the result of thresholding each
      * region in the mask.
      * @param im    The Image mask to process; will be overwritten by the result.
      */
 	@Override
 	public void apply(WritableImage im) {
 
         Image originalImageReference = this.referenceImage;
 
         this.referenceImage = ImageFactory.createWritable(this.referenceImage); // to save having to allocate an Image at every step, this
                                                                 // overwrites the reference Image, so make a copy
 		boolean doRecursion = true;
 		
 		WritableImage maskBuffer = ImageFactory.createWritable(im.getDimensionSizes(), 0.0f);
 		
 		WritableImage imageBuffer = null;
 		
 		MaximumSeparabilityThresholdingFilter mstf = new MaximumSeparabilityThresholdingFilter();
 		MaskFilter MF = new MaskFilter();
 
 		
 		mstf.setReferenceImage(imageBuffer);
 		mstf.setParameters(this.params);
 		
 		LabelFilter lf = new LabelFilter();
 		
 		int areaMin = -1;
 		int areaMax = -1;
 		
 		if (this.params != null) {
 			areaMin = Integer.parseInt(this.params.getValueForKey("min_size"));
 			areaMax = Integer.parseInt(this.params.getValueForKey("max_size"));
 		}
 		
 		if (areaMin < 0) {
 			areaMin = 25; //orig 25, 5000 for HeLa cells, 5 for centromeres
 			areaMax = 1000; //orig 1000, 100000 for HeLa cells, 50 for centromeres
 		}
 		
 		
 		
 		while(doRecursion && this.numRecs < MAX_RECURSIONS) {
 			
 			doRecursion = false;
 			
 			Histogram h = new Histogram(im);
 			
 			boolean[] flags = new boolean[h.getMaxValue() + 1];
 			boolean[] remove = new boolean[h.getMaxValue() + 1];
 			
 			flags[0] = false;
 			remove[0] = false;
 			
 			for (int i = 1; i <= h.getMaxValue(); i++) {
 				flags[i] = h.getCounts(i) > areaMax;
 				remove[i] = h.getCounts(i) < areaMin;
 			}
 			
 			for (ImageCoordinate c : im) {
 				if (remove[(int) im.getValue(c)]) {
 					im.setValue(c, 0);
 				}
 			}
 			
 			boolean divided = false;
 			
 			//set up points lists
 			
 			java.util.Hashtable<Integer, java.util.List<Integer> > xList = new java.util.Hashtable<Integer, java.util.List<Integer> >();
 			java.util.Hashtable<Integer, java.util.List<Integer> > yList = new java.util.Hashtable<Integer, java.util.List<Integer> >();
 
 			for (ImageCoordinate c : im) {
 				
 				int value = (int) im.getValue(c);
 				
 				if (value == 0) continue;
 				
 				if(! xList.containsKey(value)) {
 					xList.put(value, new java.util.Vector<Integer>());
 					yList.put(value, new java.util.Vector<Integer>());
 				}
 				
 				xList.get(value).add(c.get(ImageCoordinate.X));
 				yList.get(value).add(c.get(ImageCoordinate.Y));
 				
 			}
 			
 			int lastK = 0;
 			
 			ImageCoordinate ic = ImageCoordinate.createCoordXYZCT(0, 0, 0, 0, 0);
 			
 			for (int k = 0; k < h.getMaxValue() + 1; k++) {
 				
 				if (!flags[k]) continue;
 				
 				
 				if (lastK > 0) {
 					java.util.List<Integer> xKList = xList.get(lastK);
 					java.util.List<Integer> yKList = yList.get(lastK);
 					for (int i =0; i < xKList.size(); i++) {
 						ic.set(ImageCoordinate.X,xKList.get(i));
 						ic.set(ImageCoordinate.Y,yKList.get(i));
 						maskBuffer.setValue(ic, 0);
 					}
 				}
 				
 				//imageBuffer.copy(this.referenceImage);
 				
 				divided = true;
 				
 				int lower_width = im.getDimensionSizes().get(ImageCoordinate.X);
 				int lower_height = im.getDimensionSizes().get(ImageCoordinate.Y);
 				int upper_width = 0;
 				int upper_height = 0;
 				
 				java.util.List<Integer> xKList = xList.get(k);
 				java.util.List<Integer> yKList = yList.get(k);
 				
 				for (int i =0; i < xList.get(k).size(); i++) {
 					ic.set(ImageCoordinate.X,xKList.get(i));
 					ic.set(ImageCoordinate.Y,yKList.get(i));
 					maskBuffer.setValue(ic, k);
 					
 					if (ic.get(ImageCoordinate.X) < lower_width) lower_width = ic.get(ImageCoordinate.X);
 					if (ic.get(ImageCoordinate.X) > upper_width - 1) upper_width = ic.get(ImageCoordinate.X);
 					if (ic.get(ImageCoordinate.Y) < lower_height) lower_height = ic.get(ImageCoordinate.Y);
 					if (ic.get(ImageCoordinate.Y) > upper_height - 1) upper_height = ic.get(ImageCoordinate.Y);
 				}
 				
 				ImageCoordinate lowerBound = ImageCoordinate.createCoordXYZCT(lower_width, lower_height, 0, 0, 0);
 				ImageCoordinate upperBound = ImageCoordinate.createCoordXYZCT(upper_width + 1, upper_height + 1, 1, 1, 1);
 
 				
                 maskBuffer.setBoxOfInterest(lowerBound, upperBound);
                 imageBuffer = this.referenceImage.getWritableInstance(); // This is set to be a writable copy above, so not clobbering the reference image.
                 imageBuffer.setBoxOfInterest(lowerBound, upperBound);
 
 				//Image smallMaskBuffer = maskBuffer.subImage(sizes, lowerBound);
 				//imageBuffer = this.referenceImage.subImage(sizes, lowerBound);
 				
 				lastK = k;
 
 				MF.setReferenceImage(maskBuffer);
 				
 				MF.apply(imageBuffer);
 				
 				mstf.apply(imageBuffer);
 				
 				MF.setReferenceImage(imageBuffer);
 				
 				MF.apply(maskBuffer);
 
 				for (int i =0; i < xList.get(k).size(); i++) {
 					ic.set(ImageCoordinate.X,xKList.get(i));
 					ic.set(ImageCoordinate.Y,yKList.get(i));
 				
 					if (maskBuffer.getValue(ic) == 0) {
 						im.setValue(ic, 0);
 					}
 				
 				}
 
                 maskBuffer.clearBoxOfInterest();
                 for (ImageCoordinate ic_restore : imageBuffer) {
                     imageBuffer.setValue(ic_restore, originalImageReference.getValue(ic_restore));
                 }
                 imageBuffer.clearBoxOfInterest();
                 lowerBound.recycle();
                 upperBound.recycle();
 								
 			}
 			
 			ic.recycle();
 			
 			this.numRecs += 1;
 			
 			if (divided && this.numRecs < MAX_RECURSIONS) {
 				doRecursion = true;
 			}

			lf.apply(im);
 			
 		}
 		
 		this.numRecs = 0;
 
         this.referenceImage = originalImageReference;
 
 
 		return;
 		
 	}
 
 }
