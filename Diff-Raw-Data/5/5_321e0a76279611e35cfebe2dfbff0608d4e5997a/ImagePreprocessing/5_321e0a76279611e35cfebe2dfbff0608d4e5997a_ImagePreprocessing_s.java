 /*-
  * Copyright (c) 2014 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package org.dawnsci.boofcv.stitching;
 
 import java.util.List;
 
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
 import org.eclipse.dawnsci.analysis.dataset.roi.EllipticalROI;
 import org.eclipse.dawnsci.analysis.dataset.roi.RectangularROI;
 
 import boofcv.struct.image.ImageBase;
 
 /**
  * Static methods used to pre-process the images
  * 
  * @authors Baha El-Kassaby
  *
  */
 public class ImagePreprocessing {
 
 	/**
 	 * Image cropped is a rectangle inside of the given ellipse
 	 * 
 	 * @param image
 	 * @param ellipse
 	 * @return
 	 */
 	public static <T extends ImageBase<?>> T maxRectangleFromEllipticalImage(T image, EllipticalROI ellipse) {
 		// TODO make it work for ellipses with non-null angle
 		RectangularROI boundingBox = ellipse.getBounds();
 		double width = boundingBox.getLength(0);
 		double height = boundingBox.getLength(1);
 		int[] center = ellipse.getIntPoint();
 		double majorSemiAxis = ellipse.getSemiAxis(0);
 		double minorSemiAxis = ellipse.getSemiAxis(1);
 		int x = center[0];
 		int y = center[1];
 		if (width >= height) {
 			return maxRectangleFromEllipticalImage(image, majorSemiAxis * 2, minorSemiAxis *2, (int)(x - majorSemiAxis), (int)(y - minorSemiAxis));
 		} else {
 			return maxRectangleFromEllipticalImage(image, minorSemiAxis * 2, majorSemiAxis * 2, (int)(x - minorSemiAxis), (int)(y - majorSemiAxis));
 		}
 	}
 
 	/**
 	 * Image cropped is a rectangle inside of the circular image
 	 * 
 	 * if centre of ellipse is (0, 0) then (x, y) = (a/sqrt(2), b/sqrt(2)) where a = xdiameter/2 and b = ydiameter/2<br>
 	 * With origin of image being top left of the image, (x, y) the top left corner of the rectangle becomes
 	 * (buffer + (a * (sqrt(2)-1)/sqrt(2) , buffer + (b * (sqrt(2)-1)/sqrt(2))
 	 * and width = 2*a/sqrt(2) and height = 2*b/sqrt(2)
 	 * 
 	 * @param image
 	 * @param xdiameter
 	 * @param ydiameter
 	 * @param xbuffer
 	 * @param ybuffer
 	 * @return
 	 */
 	public static <T extends ImageBase<?>> T maxRectangleFromEllipticalImage(T image, double xdiameter, double ydiameter, int xbuffer, int ybuffer) {
 		// maximum rectangle dimension
 		double a = xdiameter / 2;
 		double b = ydiameter / 2;
 		int width = (int) (xdiameter / Math.sqrt(2));
 		int height = (int) (ydiameter / Math.sqrt(2));
 
 		// find the top left corner of the largest square within the circle
 		int cornerx = (int) (xbuffer + (a * (Math.sqrt(2)-1)/Math.sqrt(2)));
 		int cornery = (int) (ybuffer + (b * (Math.sqrt(2)-1)/Math.sqrt(2)));
 
 		T cropped = (T) image.subimage(cornerx, cornery, cornerx + width, cornery + height, null);
 		T result = (T) cropped.clone();
 		return result;
 	}
 
 	/**
 	 * 
 	 * @param input
 	 * @param rows
 	 * @param columns
 	 * @return Ordered array of Dataset
 	 * @throws Exception
 	 */
 	public static IDataset[][] listToArray(List<IDataset> input, int rows, int columns) {
 		IDataset[][] images = new Dataset[rows][columns];
 		for (int i = 0; i < rows; i++) {
 			for(int j = 0; j < columns; j++) {
 				images[i][j] = input.get((i * columns) + j);
 				//	images[i][j].setMetadata(getUniqueMetadata(i+1, j+1));
 			}
 		}
 		return images;
 	}
 
 	/**
 	 * 
 	 * @param input
 	 * @param rows
 	 * @param columns
 	 * @return Ordered array of Dataset
 	 * @throws Exception
 	 */
 	public static double[][][] transToArraysInMicrons(List<double[]> input, int rows, int columns) {
 		double[][][] images = new double[rows][columns][2];
 		for (int i = 0; i < rows; i++) {
 			for(int j = 0; j < columns; j++) {
				images[i][j] = input.get((i * columns) + j);
 				images[i][j][0] *= 10;
 				images[i][j][1] *= 10;
 				//	images[i][j].setMetadata(getUniqueMetadata(i+1, j+1));
 			}
 		}
 		return images;
 	}
 }
