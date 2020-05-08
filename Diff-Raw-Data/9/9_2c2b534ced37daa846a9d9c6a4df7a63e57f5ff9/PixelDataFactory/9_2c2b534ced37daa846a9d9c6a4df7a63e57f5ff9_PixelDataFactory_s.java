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
 
 package edu.stanford.cfuller.imageanalysistools.image;
 
 import ij.ImagePlus;
 import net.imglib2.img.ImgPlus;
 import net.imglib2.type.numeric.real.FloatType;
 
 
 /**
  * A factory to construct PixelData objects; the choice of implementation will be made based on the
  * properties of the data.
  * 
  * @author Colin J. Fuller
  *
  */
 public class PixelDataFactory {
 
 	//TODO: reimplement to handle images other than 5D.
 	
     /**
      * Constructs a new default PixelDataFactory.
      */
     public PixelDataFactory(){
     }
 
     /**
      * Constructs a new pixeldata object using an {@link ImageCoordinate} to specify the size of the pixeldata.
      * @param sizes     An ImageCoordinate that specifies the size of the pixeldata in all 5 (XYZCT) dimensions.
      * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
      * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
      * @return              A new PixelData with the specified options.
      */
 
     public static WritablePixelData createPixelData(ImageCoordinate sizes, int data_type, String dimensionOrder) {
 
         return createPixelData(sizes.get(ImageCoordinate.X), sizes.get(ImageCoordinate.Y), sizes.get(ImageCoordinate.Z), sizes.get(ImageCoordinate.C), sizes.get(ImageCoordinate.T), data_type, dimensionOrder);
 
     }
 
     /**
      * Convenience constructor for creating a PixelData object with individual dimension sizes instead of the sizes lumped into an ImageCoordinate.
      * @param size_x    Size of the pixel data in the X-dimension.
      * @param size_y    Size of the pixel data in the Y-dimension.
      * @param size_z    Size of the pixel data in the Z-dimension.
      * @param size_c    Size of the pixel data in the C-dimension.
      * @param size_t    Size of the pixel data in the T-dimension.
      * @param data_type     An integer that specifies the numeric type of the on-disk representation of this data; valid values are from {@link loci.formats.FormatTools}
      * @param dimensionOrder    A string made up of the characters "XYZCT" in any order that specifies the order of the dimensions in the on-disk representation.
      * @return          A new PixelData with the specified options.
      */
     public static WritablePixelData createPixelData(int size_x, int size_y, int size_z, int size_c, int size_t, int data_type, String dimensionOrder) {
 
         return new ImgLibPixelData(size_x, size_y, size_z, size_c, size_t, dimensionOrder);        
         
     }
 
 	/**
 	 * Creates a new ImagePlusPixelData from an existing ImagePlus.
 	 * 
 	 * @param imPl	The ImagePlus to use.  This will not be copied, but used and potentially modified in place.
 	 * @return          A new PixelData with the specified options.
 	 */
 	public static WritablePixelData createPixelData(ImagePlus imPl) {
 		return new ImagePlusPixelData(imPl);
 	}
 	
 	/**
 	 * Creates a new PixelData from an existing ImgLib2 ImgPlus and a specified dimension order.
 	 * 
	 * @param imPl	The ImgPlus to use.  This may not be copied, but used and potentially modified in place.
 	 * @param dimensionOrder	a String containing the five characters XYZCT in the order they are in the image (if some dimensions are not present, the can be specified in any order)
 	 * @return          A new PixelData with the specified options.
 	 */
 	public static WritablePixelData createPixelData(ImgPlus<FloatType> imgpl, String dimensionOrder){
 		return new ImgLibPixelData(imgpl, dimensionOrder);
 	}
 
 }
