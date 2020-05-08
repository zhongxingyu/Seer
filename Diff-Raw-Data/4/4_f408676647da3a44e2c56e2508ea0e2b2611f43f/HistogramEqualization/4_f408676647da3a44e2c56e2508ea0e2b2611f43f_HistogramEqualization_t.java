 /*
  * Copyright 2010, 2011 Institut Pasteur.
  * 
  * This file is part of ICY.
  * 
  * ICY is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ICY is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ICY. If not, see <http://www.gnu.org/licenses/>.
  */
 package plugins.tlecomte.histogram;
 
 import java.util.Arrays;
 import java.lang.Double; // for NEGATIVE_INFINITY
 
 import icy.math.MathUtil;
 import plugins.adufour.ezplug.*;
 import icy.sequence.Sequence;
 import icy.type.collection.array.Array1DUtil;
 import icy.gui.dialog.MessageDialog;
 
 /**
  * 
  * @author Timothee Lecomte
  *
  * This plugin equalizes the histogram (this operation is also called "histogram
  * flattening"). It operates on the selected channel of each image of a sequence.
  * Pixel intensities are transformed so that they are uniformly distributed
  * over the gray-scale range.  
  *
  * TODO:
  * Luminance equalization on RGB data
  * histogram shaping (sqrt, log) in addition to flattening
  * local enhancement ("adaptive equalization")
  * 
  */
 
 public class HistogramEqualization extends EzPlug
 {
 	public EzVarSequence inputSelector = new EzVarSequence("Input");
 	public EzVarInteger	channelSelector	= new EzVarInteger("Channel");
 	public EzVarBoolean inPlaceSelector	= new EzVarBoolean("In-place", false);
 	
 	@Override
 	protected void initialize()
 	{
 		addEzComponent(inputSelector);
 		addEzComponent(channelSelector);
 		addEzComponent(inPlaceSelector);
 		
 		inputSelector.addVarChangeListener(new EzVarListener<Sequence>()
 		{
 			@Override
 			public void variableChanged(EzVar<Sequence> source, Sequence newSequence)
 			{
 				channelSelector.setValue(0);
 				if (newSequence == null)
 				{
 					channelSelector.setEnabled(false);
 				}
 				else
 				{
 					int sizeC = newSequence.getSizeC();
 					channelSelector.setMaxValue(sizeC - 1);
 					channelSelector.setEnabled(sizeC == 1 ? false : true);
 				}
 			}
 		});
 	}
 	
 	@Override
 	protected void execute()
 	{
 		// main plugin code goes here, and runs in a separate thread
 		
 		super.getUI().setProgressBarMessage("Waiting...");
 		
         Sequence inputSequence = inputSelector.getValue();
         int channel = channelSelector.getValue();
         
         // Check if sequence exists.
         if ( inputSequence == null )
         {
                    MessageDialog.showDialog("Please open a sequence to use this plugin.", MessageDialog.WARNING_MESSAGE );
                    return;
         }
         
         // data range, is also the histogram size
         boolean sampleSignedType = inputSequence.isSignedDataType();
         double[] bounds = inputSequence.getImage(0, 0).getIcyColorModel().getDefaultComponentBounds();
         // length of the histogram equals the number of gray levels
        int len = (int) (bounds[1] - bounds[0] + 1);
        boolean continuous = inputSequence.isFloatDataType();
 
         Sequence outputSequence;
     	if (inPlaceSelector.getValue()) {
     		outputSequence = inputSequence;
     	} else {
     		outputSequence = inputSequence.getCopy();
     	}
     	
 		for (int t = 0; t < inputSequence.getSizeT(); t++)
 		{
 			for (int z = 0; z < inputSequence.getSizeZ(); z++)
 			{
 		        // Get the data of the image for the chosen channel as a linear buffer, regardless of the type.
 				// FIXME global luminance equalization for RGB images
 		        Object inputImageData = inputSequence.getDataXY(t, z, channel);
 		        Object outputImageData = outputSequence.getDataXY(t, z, channel);
 		        
 				// integer data
 				if (!continuous) {
 					// Get a copy of the data in integers.
 					int[] dataBuffer = Array1DUtil.arrayToIntArray(inputImageData, sampleSignedType);
 					int[] outputDataBuffer;
 
 					if (inPlaceSelector.getValue()) {
 						outputDataBuffer = dataBuffer;
 					} else {
 						outputDataBuffer = Array1DUtil.arrayToIntArray(outputImageData, sampleSignedType);
 					}
 
 					float[] HistoData = new float[len];
 					Arrays.fill(HistoData, 0, len - 1, 0f);
 
 					final double absLeftIn = bounds[0];
 
 					int offset;
 					for ( int i = 0 ; i < dataBuffer.length ; i++ )
 					{
 						offset = (int) (dataBuffer[i] - absLeftIn);
 
 						if ((offset >= 0) && (offset < len))
 							HistoData[offset]++;
 					}
 
 					// Normalize the histogram by the number of pixels
 					MathUtil.divide(HistoData, inputSequence.getSizeX()*inputSequence.getSizeY());
 
 					// Compute the cumulative histogram
 					float[] CumulativeHistoData = new float[len];
 					CumulativeHistoData[0] = HistoData[0];
 					for (int i = 1; i<len; i++) {
 						CumulativeHistoData[i] = HistoData[i] + CumulativeHistoData[i-1];
 					}
 
 					// Transform every pixel       
 					for ( int i = 0 ; i < dataBuffer.length ; i++ )
 					{
 						outputDataBuffer[i] = (int) (CumulativeHistoData[dataBuffer[i]] * (len-1));
 					}
 
 					// Put the data in the output image.
 					Array1DUtil.intArrayToArray( outputDataBuffer, outputImageData, sampleSignedType );
 				}
 				else
 				{ // continuous case
 
 					// Get a copy of the data as doubles.
 					double[][] dataBuffer = new double[2][];
 					dataBuffer[0] = Array1DUtil.arrayToDoubleArray( inputImageData , sampleSignedType );
 					dataBuffer[1] = Array1DUtil.arrayToDoubleArray( inputImageData , sampleSignedType );
 					double[][] outputDataBuffer = new double[2][];
 
 					// put the pixels indices in dataBuffer second line
 					for (int i = 0; i<dataBuffer[0].length; i++) {
 						dataBuffer[1][i] = i;
 					}
 
 					// sort the pixels by their intensities (while keeping a trace of the
 					// permutation in the second line of outputDataBuffer)
 					outputDataBuffer = mergeSort(dataBuffer);
 
 					// now we will walk the array, computing the cumulative distribution
 					// function, and using it along the way to assign new pixel intensities
 
 					// two doubles for intensity comparisons
 					double previous_intensity = Double.NEGATIVE_INFINITY;
 					double intensity;
 					// (un-normalized) cumulative distribution function
 					int CDF = 0; 
 					// m keeps track of successive pixels with the same intensities
 					int m = 0;
 
 					for (int i = 0; i<outputDataBuffer[0].length; i++) {
 						// save before swapping
 						intensity = outputDataBuffer[0][i];
 
 						// first line of outputDataBuffer will be indices to sort the array back
 						outputDataBuffer[0][i] = outputDataBuffer[1][i];
 
 						// new pixel intensities based on the cumulative distribution function
 						outputDataBuffer[1][i] = CDF;
 						// update the CDF
 						if (intensity > previous_intensity) {
 							// handle the case with several pixels having the same intensity
 							CDF += m;
 							m = 1;
 							previous_intensity = intensity;
 						} else {
 							m++;
 						}
 					}
 
 					// sort the array back in its original order
 					outputDataBuffer = mergeSort(outputDataBuffer);
 
 					// Normalize the data to [0,1]
 					MathUtil.divide(outputDataBuffer[1], inputSequence.getSizeX()*inputSequence.getSizeY());
 
 					// Put the data in the output image.
 					Array1DUtil.doubleArrayToArray( outputDataBuffer[1], outputImageData);
 				}
 			} // end z
 		} // end t
 
 	    if (!inPlaceSelector.getValue()) {
 			// Add a viewer for the new sequence
 			addSequence(outputSequence);
 		}
 		
 		// notify ICY the data has changed.
 		outputSequence.dataChanged();
 	}
 	
 	@Override
 	public void clean()
 	{
 		// use this method to clean local variables or input streams (if any) to avoid memory leaks
 	}
 	
 	public double[][] mergeSort(double array[][])
 	//pre: array is full, all elements are valid integers (not null)
 	//post: array is sorted in ascending order (lowest to highest)
 	{
 		// if the array has more than 1 element, we need to split it and merge the sorted halves
 		if(array[0].length > 1)
 		{
 			// number of elements in sub-array 1
 			// if odd, sub-array 1 has the smaller half of the elements
 			// e.g. if 7 elements total, sub-array 1 will have 3, and sub-array 2 will have 4
 			int elementsInA1 = array[0].length/2;
 			// since we want an even split, we initialize the length of sub-array 2 to
 			// equal the length of sub-array 1
 			int elementsInA2 = elementsInA1;
 			// if the array has an odd number of elements, let the second half take the extra one
 			// see note (1)
 			if((array[0].length % 2) == 1)
 				elementsInA2 += 1;
 			// declare and initialize the two arrays once we've determined their sizes
 			double arr1[][] = new double[2][elementsInA1];
 			
 			double arr2[][] = new double[2][elementsInA2];
 			
 			// copy the first part of 'array' into 'arr1', causing arr1 to become full
 			for(int i = 0; i < elementsInA1; i++) {
 				arr1[0][i] = array[0][i];
 				arr1[1][i] = array[1][i];
 			}
 			// copy the remaining elements of 'array' into 'arr2', causing arr2 to become full
 			for(int i = elementsInA1; i < elementsInA1 + elementsInA2; i++) {
 				arr2[0][i - elementsInA1] = array[0][i];
 				arr2[1][i - elementsInA1] = array[1][i];
 			}
 			// recursively call mergeSort on each of the two sub-arrays that we've just created
 			// note: when mergeSort returns, arr1 and arr2 will both be sorted!
 			// it's not magic, the merging is done below, that's how mergesort works :)
 			arr1 = mergeSort(arr1);
 			arr2 = mergeSort(arr2);
 			
 			// the three variables below are indexes that we'll need for merging
 			// [i] stores the index of the main array. it will be used to let us
 			// know where to place the smallest element from the two sub-arrays.
 			// [j] stores the index of which element from arr1 is currently being compared
 			// [k] stores the index of which element from arr2 is currently being compared
 			int i = 0, j = 0, k = 0;
 			// the below loop will run until one of the sub-arrays becomes empty
 			// in my implementation, it means until the index equals the length of the sub-array
 			while(arr1[0].length != j && arr2[0].length != k)
 			{
 				// if the current element of arr1 is less than current element of arr2
 				if(arr1[0][j] < arr2[0][k])
 				{
 					// copy the current element of arr1 into the final array
 					array[0][i] = arr1[0][j];
 					array[1][i] = arr1[1][j];
 					// increase the index of the final array to avoid replacing the element
 					// which we've just added
 					i++;
 					// increase the index of arr1 to avoid comparing the element
 					// which we've just added
 					j++;
 				}
 				// if the current element of arr2 is less than current element of arr1
 				else
 				{
 					// copy the current element of arr1 into the final array
 					array[0][i] = arr2[0][k];
 					array[1][i] = arr2[1][k];
 					// increase the index of the final array to avoid replacing the element
 					// which we've just added
 					i++;
 					// increase the index of arr2 to avoid comparing the element
 					// which we've just added
 					k++;
 				}
 			}
 			// at this point, one of the sub-arrays has been exhausted and there are no more
 			// elements in it to compare. this means that all the elements in the remaining
 			// array are the highest (and sorted), so it's safe to copy them all into the
 			// final array.
 			while(arr1[0].length != j)
 			{
 				array[0][i] = arr1[0][j];
 				array[1][i] = arr1[1][j];
 				i++;
 				j++;
 			}
 			while(arr2[0].length != k)
 			{
 				array[0][i] = arr2[0][k];
 				array[1][i] = arr2[1][k];
 				i++;
 				k++;
 			}
 		}
 		// return the sorted array to the caller of the function
 		return array;
 	}
 }
 
