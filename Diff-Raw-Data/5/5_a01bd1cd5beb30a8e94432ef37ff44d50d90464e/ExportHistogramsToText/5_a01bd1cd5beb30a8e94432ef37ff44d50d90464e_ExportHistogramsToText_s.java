 //
 // ExportHistogramsToText.java
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
 
 package loci.slim.analysis.plugins;
 
 import ij.IJ;
 import ij.gui.GenericDialog;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.util.Arrays;
 import java.util.prefs.*;
 import loci.curvefitter.ICurveFitter.FitFunction;
 import loci.curvefitter.ICurveFitter.FitRegion;
 import loci.slim.analysis.ISLIMAnalyzer;
 import loci.slim.analysis.SLIMAnalyzer;
 import mpicbg.imglib.cursor.LocalizableByDimCursor;
 import mpicbg.imglib.image.Image;
 import mpicbg.imglib.type.numeric.real.DoubleType;
 
 /**
  * Exports histogram values as text for further analysis of SLIMPlugin results.
  * 
  * @author Aivar Grislis
  */
 @SLIMAnalyzer(name="Export Histograms to Text")
 public class ExportHistogramsToText implements ISLIMAnalyzer {
 	private static final int BINS = 256;
 	private static final long MIN_COUNT = 3;
 	private static final String NAN = "NaN";
     private static final String FILE_KEY = "export_histograms_to_text/file";
 	private static final String APPEND_KEY = "export_histograms_to_text/append";
     private static final int X_INDEX = 0;
     private static final int Y_INDEX = 1;
     private static final int C_INDEX = 2;
     private static final int P_INDEX = 3;
     private static final char TAB = '\t';
     private static final char EOL = '\n';
 	private String fileName;
 	private boolean append;
     private FileWriter fileWriter = null;
     private MathContext context = new MathContext(4, RoundingMode.FLOOR);
 
     public void analyze(Image<DoubleType> image, FitRegion region, FitFunction function) {
 		// need entire fitted image
 		if (FitRegion.EACH == region) {
 			boolean export = showFileDialog(getFileFromPreferences(), getAppendFromPreferences());
 			if (export && null != fileName) {
 				saveFileInPreferences(fileName);
 				saveAppendInPreferences(append);
 				export(fileName, append, image, function);
 			}
 		}
     }
 
     public void export(String fileName, boolean append, Image<DoubleType> image,
 			FitFunction function)
 	{
         try {
             fileWriter = new FileWriter(fileName, append);
         } catch (IOException e) {
             IJ.log("exception opening file " + fileName);
             IJ.handleException(e);
         }
 
         if (null != fileWriter) {
 			
 			// look at image dimensions
 			int[] dimensions = image.getDimensions();
             int width    = dimensions[X_INDEX];
             int height   = dimensions[Y_INDEX];
             int channels = dimensions[C_INDEX];
 
 			// get titles of parameters in parameter order
 			String[] titles = null;
 			switch (function) {
 				case SINGLE_EXPONENTIAL:
 					titles = new String[] { "X2", "Z", "A", "T" };
 					break;
 				case DOUBLE_EXPONENTIAL:
 					titles = new String[] { "X2", "Z", "A1", "T1", "A2", "T2" };
 					break;
 				case TRIPLE_EXPONENTIAL:
 					titles = new String[] { "X2", "Z", "A1", "T1", "A2", "T2", "A3", "T3" };
 					break;
 				case STRETCHED_EXPONENTIAL:
 					titles = new String[] { "X2", "Z", "A", "T", "H" };
 					break;
 			}
 			
 			try {
 				// title this export
 				fileWriter.write("Export Histograms " + image.getName() + EOL + EOL);
 				
 				for (int channel = 0; channel < channels; ++channel) {
 					if (channels > 1) {
 					    fileWriter.write("Channel" + TAB + channel + EOL + EOL);
 					}
 					
 					for (int i = 0; i < titles.length; ++i) {
 						// first statistical pass through the image
 						Statistics1 statistics1 = getStatistics1(image, channel, i);
 						
 						if (statistics1.count < MIN_COUNT) {
 							fileWriter.write("Count" + TAB + statistics1.count + EOL);
 							fileWriter.write("Too few pixels for histograms" + EOL + EOL);
 							
 							// don't process any more parameters; all will have same count
 							break;
 						}
 						else {
 							fileWriter.write("Parameter" + TAB + titles[i] + EOL);
 							
 							// second statistical pass through the image
 							Statistics2 statistics2 = getStatistics2(image, channel, i, statistics1.mean, statistics1.range, BINS);
 													
 							// put out statistics
 							fileWriter.write("Min" + TAB + showParameter(statistics1.min) + EOL);
 							fileWriter.write("Max" + TAB + showParameter(statistics1.max) + EOL);
 							fileWriter.write("Count" + TAB + statistics1.count + EOL);
 							fileWriter.write("Mean" + TAB + showParameter(statistics1.mean) + EOL);
 							fileWriter.write("Standard Deviation" + TAB + showParameter(statistics2.standardDeviation) + EOL);
 							fileWriter.write("1st Quartile" + TAB + showParameter(statistics1.quartile[0]) + EOL);
 							fileWriter.write("Median" + TAB + showParameter(statistics1.quartile[1]) + EOL);
 							fileWriter.write("2nd Quartile" + TAB + showParameter(statistics1.quartile[2]) + EOL);
 
 							// put out histogram
 							fileWriter.write("Histogram" + EOL);
 							fileWriter.write("Bins" + TAB + BINS + EOL);
 							fileWriter.write("Min" + TAB + showParameter(statistics1.range[0]) + EOL);
 							fileWriter.write("Max" + TAB + showParameter(statistics1.range[1]) + EOL);
 							fileWriter.write("Count" + TAB + statistics2.histogramCount + EOL);
 
 							double[] values = Binning.centerValuesPerBin(BINS, statistics1.range[0], statistics1.range[1]);
 							for (int j = 0; j < statistics2.histogram.length; ++j) {
 								fileWriter.write(showParameter(values[j]) + TAB + statistics2.histogram[j] + EOL);
 							}
 						}
 					
 						fileWriter.write(EOL);
 					}
 				}
 				fileWriter.close();
 			}
 			catch (IOException e) {
 				IJ.log("exception writing file " + e.getMessage());
 				IJ.handleException(e);
 			}
 		}
     }
 
 	/**
 	 * First pass through the image, gathering statistics.
 	 * 
 	 * @param image
 	 * @param channel
 	 * @param parameter
 	 * @return container of various statistics
 	 */
 	private Statistics1 getStatistics1(Image<DoubleType> image, int channel, int parameter) {
 		long count = 0;
 		double min = Double.MAX_VALUE;
 		double max = Double.MIN_VALUE;
 		double sum = 0.0;
 		double[] quartile = null;
 		double[] range = null;
 		int[] dimensions = image.getDimensions();
 		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();
 
 		// collect & sort non-NaN values
 		double[] values = new double[dimensions[0] * dimensions[1]];
 		int index = 0;
 		int[] position = new int[dimensions.length];
 		for (int y = 0; y < dimensions[1]; ++y) {
 			for (int x = 0; x < dimensions[0]; ++x) {
 				// set position
 				position[0] = x;
 				position[1] = y;
 				position[2] = channel;
 				position[3] = parameter;
 				cursor.setPosition(position);
 
 				// account for value
 				double value = cursor.getType().getRealDouble();
 				if (!Double.isNaN(value)) {
 					values[index++] = value;
 					if (value < min) {
 						min = value;
 					}
 					if (value > max) {
 						max = value;
 					}
 					sum += value;
 					++count;
 				}
 			}
 		}
 		// sort values to read off quartiles
 		Arrays.sort(values, 0, index);
 		
		if (count > MIN_COUNT) {
 			// read off the quartiles
 			quartile = new double[3];
 			int lowerTopHalfIndex, upperBottomHalfIndex;
 			if (index % 2 != 0) {
 				// odd array size
 
 				// take the middle value
 				lowerTopHalfIndex = upperBottomHalfIndex = index / 2;
 				quartile[1] = values[lowerTopHalfIndex];
 			}
 			else {
 				// even array size
 
 				// take the mean of middle two values
 				lowerTopHalfIndex = index / 2;
 				upperBottomHalfIndex = lowerTopHalfIndex - 1;
 				quartile[1] = (values[lowerTopHalfIndex] + values[upperBottomHalfIndex]) / 2;
 			}
 
 			if (upperBottomHalfIndex % 2 == 0) {
 				// even index means odd half sizes
 
 				// take the middle values
 				index = upperBottomHalfIndex / 2;
 				quartile[0] = values[index];
 				index += lowerTopHalfIndex;
 				quartile[2] = values[index];
 			}
 			else {
 				// even half sizes
 
 				// take the mean of middle two values
 				index = upperBottomHalfIndex / 2;
 				quartile[0] = (values[index] + values[index + 1]) / 2;
 
 				index += lowerTopHalfIndex;
 				quartile[2] = (values[index] + values[index + 1]) / 2;
 			}
 
 			// calculate range
 			range = new double[2];
 			double iqr = quartile[2] - quartile[0];
 			range[0] = quartile[1] - 1.5 * iqr;
 			range[1] = quartile[1] + 1.5 * iqr;
 		}
 		else if (0 == count) {
 			// avoid reporting spurious values
 			min = max = Double.NaN;
 		}
 			
 		Statistics1 statistics = new Statistics1();
 		statistics.count = count;
 		statistics.min = min;
 		statistics.max = max;
 		statistics.mean = sum / count;
 		statistics.quartile = quartile;
 		statistics.range = range;
 		return statistics;
 	}
 
 	/**
 	 * Second pass through the image, gathering statistics.
 	 * 
 	 * @param image
 	 * @param channel
 	 * @param parameter
 	 * @param mean
 	 * @param range
 	 * @param bins
 	 * @return container of various statistics
 	 */
 	private Statistics2 getStatistics2(Image<DoubleType> image, int channel, int parameter, double mean, double[] range, int bins) {
 		double diffSquaredSum = 0.0;
 		long count = 0;
 		long histogramCount = 0;
 		long[] histogram = new long[bins];
 		
 		int[] dimensions = image.getDimensions();
 		LocalizableByDimCursor<DoubleType> cursor = image.createLocalizableByDimCursor();
 
 		// collect & sort non-NaN values
 		double[] values = new double[dimensions[0] * dimensions[1]];
 		int index = 0;
 		int[] position = new int[dimensions.length];
 		for (int y = 0; y < dimensions[1]; ++y) {
 			for (int x = 0; x < dimensions[0]; ++x) {
 				// set position
 				position[0] = x;
 				position[1] = y;
 				position[2] = channel;
 				position[3] = parameter;
 				cursor.setPosition(position);
 
 				// account for value
 				double value = cursor.getType().getRealDouble();
 				if (!Double.isNaN(value)) {
 					// compute standard deviation from mean
 					double diff = mean - value;
 					diffSquaredSum += diff * diff;
 					++count;

 					int bin = Binning.exclusiveValueToBin(bins, range[0], range[1], value);
 					if (0 <= bin && bin < bins) {
 						++histogram[bin];
 						++histogramCount;
 					}
 				}
 			}
 		}
 		
 		Statistics2 statistics = new Statistics2();
 		statistics.standardDeviation = Math.sqrt(diffSquaredSum / count);
 		statistics.histogramCount = histogramCount;
 		statistics.histogram = histogram;
 		return statistics;
 	}
 
 	/**
 	 * Container for first batch of statistics.
 	 */
 	private class Statistics1 {
 		public long count;
 		public double min;
 		public double max;
 		public double mean;
 		public double[] quartile;
 		public double[] range;
 	}
 
 	/**
 	 * Container for second batch of statistics.
 	 */
 	private class Statistics2 {
 		public double standardDeviation;
 		public long histogramCount;
 		public long[] histogram;
 	}
 
     private String getFileFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        return prefs.get(FILE_KEY, fileName);
     }
 
     private void saveFileInPreferences(String fileName) {
         Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         prefs.put(FILE_KEY, fileName);
     }
 	
 	private boolean getAppendFromPreferences() {
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 		return prefs.getBoolean(APPEND_KEY, append);
 	}
 	
 	private void saveAppendInPreferences(boolean append) {
 		Preferences prefs = Preferences.userNodeForPackage(this.getClass());
 		prefs.putBoolean(APPEND_KEY, append);
 	}
 
     private boolean showFileDialog(String defaultFile, boolean defaultAppend) {
         GenericDialog dialog = new GenericDialog("Export Histograms to Text");
         dialog.addStringField("Save As:", defaultFile, 24);
 		dialog.addCheckbox("Append", defaultAppend);
         dialog.showDialog();
         if (dialog.wasCanceled()) {
             return false;
         }
 		fileName = dialog.getNextString();
 		append   = dialog.getNextBoolean();
 		return true;
     }
 
     private String showParameter(double parameter) {
 		String returnValue = NAN;
 		if (!Double.isNaN(parameter)) {
 			returnValue = BigDecimal.valueOf(parameter).round(context).toEngineeringString();
 		}
         return returnValue;
 	}
 }
