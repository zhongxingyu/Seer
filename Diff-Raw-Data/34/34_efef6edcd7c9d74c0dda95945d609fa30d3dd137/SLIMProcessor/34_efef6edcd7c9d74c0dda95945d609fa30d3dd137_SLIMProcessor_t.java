 //
 // SLIMProcessor.java
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
 
 package loci.slim;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.gui.GenericDialog;
 import ij.gui.Roi;
 import ij.plugin.frame.RoiManager;
 import ij.process.ColorProcessor;
 import ij.process.ImageProcessor;
 
 import imagej.imglib.process.ImageUtils;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 
 import javax.swing.JFrame;
 
 import loci.slim.colorizer.DataColorizer;
 import loci.slim.ui.IStartStopListener;
 import loci.slim.ui.IUserInterfacePanel;
 import loci.slim.ui.IUserInterfacePanel.FitAlgorithm;
 import loci.slim.ui.IUserInterfacePanel.FitFunction;
 import loci.slim.ui.IUserInterfacePanel.FitRegion;
 import loci.slim.ui.IUserInterfacePanelListener;
 import loci.slim.ui.UserInterfacePanel;
 import loci.curvefitter.CurveFitData;
 import loci.curvefitter.GrayCurveFitter;
 import loci.curvefitter.GrayNRCurveFitter;
 import loci.curvefitter.ICurveFitData;
 import loci.curvefitter.ICurveFitter;
 import loci.curvefitter.JaolhoCurveFitter;
 import loci.curvefitter.MarkwardtCurveFitter;
 import loci.curvefitter.SLIMCurveFitter;
 import loci.formats.FormatTools;
 import loci.formats.IFormatReader;
 import loci.slim.analysis.SLIMAnalysis;
 import loci.slim.binning.SLIMBinning;
 
 import mpicbg.imglib.container.planar.PlanarContainerFactory;
 import mpicbg.imglib.cursor.Cursor;
 import mpicbg.imglib.cursor.LocalizableByDimCursor;
 import mpicbg.imglib.image.Image;
 import mpicbg.imglib.image.ImageFactory;
 import mpicbg.imglib.io.ImageOpener;
 import mpicbg.imglib.type.numeric.RealType;
 import mpicbg.imglib.type.numeric.real.DoubleType;
 
 
 //TODO tidy up SLIMProcessor
 /**
  * SLIMProcessor is the main class of the SLIM Plugin.  It was originally just thrown
  * together to get something working, with some code/techniques borrowed from SLIM Plotter.
  * Parts of this code are ugly & experimental.
  *
  * @author Aivar Grislis grislis at wisc.edu
  */
 public class SLIMProcessor <T extends RealType<T>> {
     private static final String X = "X";
     private static final String Y = "Y";
     private static final String LIFETIME = "Lifetime";
     private static final String CHANNELS = "Channels";
     private static final boolean USE_TAU = true;
     private static final boolean USE_LAMBDA = false;
 
     // this affects how lifetimes are colorized: //TODO get rid of this
     private static final double MAXIMUM_LIFETIME = 0.075; // for fitting fake with Jaolho // for fitting brian with barber triple integral 100.0f X tau vs lambda issue here
 
     // this affects how many pixels we process at once
     private static final int PIXEL_COUNT = 128; //32;//16;
 
     // Unicode special characters
     private static final Character CHI    = '\u03c7';
     private static final Character SQUARE = '\u00b2';
     private static final Character TAU    = '\u03c4';
     private static final Character LAMBDA = '\u03bb';
     private static final Character SIGMA  = '\u03c3';
     private static final Character SUB_1  = '\u2081';
     private static final Character SUB_2  = '\u2082';
     private static final Character SUB_3  = '\u2083';
 
     private static final double[] DEFAULT_SINGLE_EXP_PARAMS  = { 0.0, 0.5, 100.0, 0.5 };                      // 0 C A T
     private static final double[] DEFAULT_DOUBLE_EXP_PARAMS  = { 0.0, 0.5, 50.0, 0.5, 50, 0.25 };             // 0 C A1 T1 A2 T2
     private static final double[] DEFAULT_TRIPLE_EXP_PARAMS  = { 0.0, 0.5, 40.0, 0.5, 30.0, 0.25, 30, 0.10 }; // 0 C A1 T1 A2 T2 A3 T3
     private static final double[] DEFAULT_STRETCH_EXP_PARAMS = { 0.0, 0.5, 100.0, 0.5, 0.5 };                 // 0 C A T H
 
     private Object m_synchFit = new Object();
     private volatile boolean m_quit;
     private volatile boolean m_cancel;
     private volatile boolean m_fitInProgress;
     private volatile boolean m_fitted;
 
     //TODO total kludge; just to get started
     private boolean m_fakeData = false;
 
     private static final String FILE_KEY = "file";
     private String m_file;
 
     IFormatReader m_reader;
 
     private Image<T> m_image;
     private LocalizableByDimCursor<T> m_cursor;
 
     // Actual data values, dimensioned [channel][row][column][bin] //TODO DEPRECATE
     protected int[][][][] Xm_data;
 
     private ImageProcessor m_grayscaleImageProcessor;
     private Canvas m_grayscaleCanvas;
 
     private Image<DoubleType> m_fittedImage = null;
     private int m_fittedParameterCount = 0;
     boolean m_visibleFit = true;
 
     // data parameters
     private boolean m_hasChannels;
     private int m_channels;
     private int m_width;
     private int m_height;
     private int[] m_cLengths;
     private int m_timeBins;
     private int m_lifetimeIndex;
     private int m_spectraIndex;
 
     private boolean m_little;
     private int m_pixelType;
     private int m_bpp;
     private boolean m_floating;
     private float m_timeRange;
     private int m_minWave, m_waveStep; //, m_maxWave;
 
     private FitRegion m_region;
     private FitAlgorithm m_algorithm;
     private FitFunction m_function;
 
     private SLIMAnalysis m_analysis;
     private SLIMBinning m_binning;
 
     private IGrayScaleImage m_grayScaleImage;
     // user sets these from the grayScalePanel
     private int m_channel;
     private boolean m_fitAllChannels;
 
     // current channel, x, y
     private int m_xchannel; // channel to fit; -1 means fit all channels
     private int m_x;
     private int m_y;
     private int m_xvisibleChannel; // channel being displayed; -1 means none
 
     private double[] m_param = new double[7];
     private boolean[] m_free = { true, true, true, true, true, true, true };
 
     private int m_startBin;
     private int m_stopBin;
     private int m_startX;
     private int m_threshold;
     private float m_chiSqTarget;
 
     private int m_debug = 0;
 
     public SLIMProcessor() {
         m_analysis = new SLIMAnalysis();
         m_binning = new SLIMBinning();
         m_quit = false;
         m_cancel = false;
         m_fitInProgress = false;
         m_fitted = false;
     }
 
     public void processImage(Image<T> image) {
         boolean success = false;
 
         m_image = image;
         if (getImageInfo(image)) {
             // show the UI; do fits
             doFits();
         }
     }
 
     /**
      * Run method for the plugin.  Throws up a file dialog.
      *
      * @param arg
      */
     public void process(String arg) {
         boolean success = false;
         if (showFileDialog(getFileFromPreferences())) {
             if (m_fakeData) {
                 fakeData();
                 success = true;
             }
             else {
                 m_image = loadImage(m_file);
                 if (getImageInfo(m_image)) {
                     saveFileInPreferences(m_file);
                     success = true;
                 }
             }
         }
         
         if (success) {
             // show the UI; do fits
             doFits();
         }
     }
 
     /**
      * Creates a user interface panel.  Shows a grayscale
      * version of the image.
      *
      * Loops until quitting time and handles fit requests.
      * Fitting is driven by a button on the UI panel which
      * sets the global m_fitInProgress.
      *
      * @param uiPanel
      */
     private void doFits() {
         // show the UI; do fits
         final IUserInterfacePanel uiPanel = new UserInterfacePanel(USE_TAU, m_analysis.getChoices(), m_binning.getChoices());
         uiPanel.setX(0);
         uiPanel.setY(0);
         uiPanel.setStart(m_timeBins / 2); //TODO hokey
         uiPanel.setStop(m_timeBins - 1);
         uiPanel.setThreshold(100);
         uiPanel.setFunctionParameters(0, DEFAULT_SINGLE_EXP_PARAMS);
         uiPanel.setFunctionParameters(1, DEFAULT_DOUBLE_EXP_PARAMS);
         uiPanel.setFunctionParameters(2, DEFAULT_TRIPLE_EXP_PARAMS);
         uiPanel.setFunctionParameters(3, DEFAULT_STRETCH_EXP_PARAMS);
         uiPanel.setListener(
             new IUserInterfacePanelListener() {
                 public void doFit() {
                     m_cancel = false;
                     m_fitInProgress = true;
                 }
 
                 public void cancelFit() {
                     m_cancel = true;
                 }
 
                 public void quit() {
                     m_quit = true;
                 }
 
             }
         );
         uiPanel.getFrame().setLocationRelativeTo(null);
         uiPanel.getFrame().setVisible(true);
 
         // create a grayscale image from the data
         m_grayScaleImage = new GrayScaleImage("TITLE", m_image);
         m_grayScaleImage.setListener(
             new ISelectListener() {
                 public void selected(int channel, int x, int y) {
                     // just ignore clicks during a fit
                     if (!m_fitInProgress) {
                         synchronized (m_synchFit) {
                             uiPanel.setX(x);
                             uiPanel.setY(y);
                             // fit on the pixel clicked
                             fitPixel(uiPanel, x, y);
                         }
                     }
                 }
             }
         );
 
         // processing loop; waits for UI panel input
         while (!m_quit) {
             while (!m_fitInProgress) {
                 try {
                     Thread.sleep(1000);
                 }
                 catch (InterruptedException e) {
 
                 }
                 if (m_quit) {
                     return;
                 }
             }
 
             //uiPanel.enable(false); //TODO this might be better to be same as grayScalePanel
             m_grayScaleImage.enable(false);
 
             // get settings of requested fit
             getFitSettings(m_grayScaleImage, uiPanel);
 
             // do the fit
             fitData(uiPanel);
 
             m_fitInProgress = false;
             //uiPanel.enable(true);
             m_grayScaleImage.enable(true);
             uiPanel.reset();
         }
     }
 
     private void getFitSettings(IGrayScaleImage grayScalePanel, IUserInterfacePanel uiPanel) {
         m_channel        = grayScalePanel.getChannel();
 
         m_region         = uiPanel.getRegion();
         m_algorithm      = uiPanel.getAlgorithm();
         m_function       = uiPanel.getFunction();
         m_fitAllChannels = uiPanel.getFitAllChannels();
 
         m_x              = uiPanel.getX();
         m_y              = uiPanel.getY();
         m_startBin       = uiPanel.getStart();
         m_stopBin        = uiPanel.getStop();
         m_threshold      = uiPanel.getThreshold();
 
         m_param          = uiPanel.getParameters();
         m_free           = uiPanel.getFree();
     }
 
     /**
      * Prompts for a .sdt file.
      *
      * @param defaultFile
      * @return
      */
     private boolean showFileDialog(String defaultFile) {
         //TODO shouldn't UI be in separate class?
         //TODO need to include fiji-lib.jar in repository:
         //GenericDialogPlus dialog = new GenericDialogPlus("Load Data");
         GenericDialog dialog = new GenericDialog("Load Data");
         //TODO works with GenericDialogPlus, dialog.addFileField("File:", defaultFile, 24);
         dialog.addStringField("File", defaultFile);
         dialog.addCheckbox("Fake data", m_fakeData);
         dialog.showDialog();
         if (dialog.wasCanceled()) {
             return false;
         }
 
         m_file = dialog.getNextString();
         m_fakeData = dialog.getNextBoolean();
 
         return true;
     }
 
     private Image<T> loadImage(String file) {
         ImageOpener imageOpener = new ImageOpener();
         Image<T> image = null;
         try {
             image = imageOpener.openImage(file);
         }
         catch (Exception e) {
             System.out.println("Error " + e.getMessage());
         }
         return image;
     }
    
     private boolean getImageInfo(Image<T> image) {
         System.out.println("Image is " + image);
         int[] dimensions = image.getDimensions();
         System.out.println("dimensions size is " + dimensions.length);
         Integer xIndex, yIndex, lifetimeIndex, channelIndex;
         m_width = ImageUtils.getWidth(image);
         m_height = ImageUtils.getHeight(image);
         m_channels = ImageUtils.getNChannels(image);
         //TODO this is broken; returns 1 when there are 16 channels; corrected below
         System.out.println("ImageUtils.getNChannels returns " + m_channels);
         m_hasChannels = false;
         if (dimensions.length > 3) {
             m_hasChannels = true;
             m_channels = dimensions[3];
         }
         System.out.println("corrected to " + m_channels);
         m_timeBins = ImageUtils.getDimSize(image, FormatTools.LIFETIME);
         System.out.println("width " + m_width + " height " + m_height + " timeBins " + m_timeBins + " channels " + m_channels);
         m_cursor = image.createLocalizableByDimCursor();
         /*
         int index = 0;
         xIndex = index++;
         yIndex = index++;
         lifetimeIndex = index++;
         if (m_channels > 1) {
             channelIndex = index;
         }
         else {
             channelIndex = null;
         }
 
 
         m_data = new int[m_channels][m_height][m_width][m_timeBins];
         final LocalizableByDimCursor<T> cursor = image.createLocalizableByDimCursor();
         int x, y, bin, channel;
         for (channel = 0; channel < m_channels; ++channel) {
             if (null != channelIndex) {
                 dimensions[channelIndex] = channel;
             }
             for (y = 0; y < m_height; ++y) {
                 dimensions[yIndex] = y;
                 for (x = 0; x < m_width; ++x) {
                     dimensions[xIndex] = x;
                     for (bin = 0; bin < m_timeBins; ++bin) {
                         dimensions[lifetimeIndex] = bin;
                         cursor.moveTo(dimensions);
                         m_data[channel][y][x][bin] = (int) cursor.getType().getRealFloat();
                         //TODO don't do this, screws up low photon count images...  m_data[channel][y][x][bin] /= 10.0f; //TODO in accordance with TRI2; HOLY COW!!!  ALSO int vs float??? why?
                     }
                 }
             }
         }
         cursor.close();*/
         // print out some useful information about the image
         //System.out.println(image);
         //final Cursor<T> cursor = image.createCursor();
         //cursor.fwd();
         //System.out.println("\tType = " + cursor.getType().getClass().getName());
         //cursor.close();
 
         //TODO from a former version:
      //TODO won't compile with my version of the jar: Number timeBase = (Number) m_reader.getGlobalMetadata().get("time base");
      //TODO fix:
          //   Number timeBase = null;
          //   m_timeRange = timeBase == null ? Float.NaN : timeBase.floatValue();
          ////   if (m_timeRange != m_timeRange) m_timeRange = 10.0f;
          //   m_minWave = 400;
          //   m_waveStep = 10;
             //m_binRadius = 3;
 
 
         // patch things up
         m_timeRange = 10.0f / 64.0f; //TODO ARG this patches things up in accord with TRI2 for brian/gpl1.sdt; very odd value here NOTE this was with photon counts all divided by 10.0f above! might have cancelled out.
                    //TODO the patch above worked when I was also dividing the photon count by 10.0f!!  should be 1/64?
         m_minWave = 400;
         m_waveStep = 10;
 
         return true;
     }
 
     private boolean fakeData() {
         return true;
     }
     /**
      * This routine creates an artificial set of data that is useful to test fitting.
      *
      * @return whether successful
      */
    /* private boolean fakeData() {
         m_width = 50;
         m_height = 50;
         m_timeBins = 20;
         m_channels = 1;
         m_timeRange = 10.0f;
         m_minWave = 400;
         m_waveStep = 10;
 
         double A;
         double lambda;
         double b = 1.0;
 
         // show colorized lifetimes
         DataColorizer dataColorizer = new DataColorizer(m_width, m_height, "Fake Data");
         //ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
         //ImagePlus imagePlus = new ImagePlus("Fake Data", imageProcessor);
 
         m_data = new int[m_channels][m_height][m_width][m_timeBins];
         for (int y = 0; y < m_height; ++y) {
             A = 1000.0 + y  * 10000.0; // was 1000.0; bumped up Tuesday July 27 trying to get Barber LMA to work - didn't help.
             for (int x = 0; x < m_width; ++x) {
                 double tmpX = x;
                 lambda = 0.05 + x * 0.0005d; //0.0001 + x * .001; //0.5 + x * 0.01; // .002500 + x * .01;
                 //System.out.println("lambda " + lambda + " color " + lambdaColorMap(MAXIMUM_LAMBDA, lambda));
                 dataColorizer.setData(true, x, y, lambda);
                 //imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lambda));
                 //imageProcessor.drawPixel(x, y);
                 for (int t = 0; t < m_timeBins; ++t) {
                     m_data[0][y][x][t] = (int)(A * Math.exp(-lambda * m_timeRange * t) + b);
                 }
                 //System.out.print(" " + m_data[0][y][x][0]);
                 if (5 == x && 5 == y) System.out.println("at (5, 5) A is " + A + " lambda " + lambda + " b " + b);
                 if (10 == x && 10 == y) System.out.println("at (10, 10) A is " + A + " lambda " + lambda + " b " + b);
                 if (49 == x && 49 == y) System.out.println("at (49, 49) A is " + A + " lambda " + lambda + " b " + b);
             }
             //System.out.println();
         }
         dataColorizer.update();
         return true;
     }
     */
 
     /**
      * Restores file name from Java Preferences.
      *
      * @return file name String
      */
     private String getFileFromPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(this.getClass());
        return prefs.get(FILE_KEY, "");
     }
 
     /**
      * Saves the file name to Java Preferences.
      *
      * @param file
      */
     private void saveFileInPreferences(String file) {
         Preferences prefs = Preferences.userNodeForPackage(this.getClass());
         prefs.put(FILE_KEY, file);
     }
 
     /*
      * Fits the data as requested by UI.
      */
     private void fitData(IUserInterfacePanel uiPanel) {
         // only one fit at a time
         synchronized (m_synchFit) {
             switch (m_region) {
                 case SUMMED:
                     // sum all pixels
                     fitSummed(uiPanel);
                     break;
                 case ROI:
                     // fit summed ROIs
                     fitROIs(uiPanel);
                     break;
                 case POINT:
                     // fit single pixel
                     fitPixel(uiPanel, m_x, m_y);
                     break;
                 case EACH:
                     // fit every pixel
                     fitEachPixel(uiPanel);
                     break;
             }
         }
         m_analysis.doAnalysis(uiPanel.getAnalysis(), m_fittedImage, uiPanel.getRegion(), uiPanel.getFunction()); //TODO get from uiPanel or get from global?  re-evaluate approach here
     }
 
     /*
      * Sums all pixels and fits the result.
      */
     private void fitSummed(IUserInterfacePanel uiPanel) {
         double params[] = uiPanel.getParameters(); //TODO go cumulative
         
         // build the data
         ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
         ICurveFitData curveFitData;
         double yCount[];
         double yFitted[];
         
         // sum up all the photons
         curveFitData = new CurveFitData();
         curveFitData.setParams(params);
         yCount = new double[m_timeBins];
         for (int b = 0; b < m_timeBins; ++b) {
             yCount[b] = 0.0;
         }
         int photons = 0;
         
         if (-1 == m_channel) {
             // sum all of the channels
             for (int channel = 0; channel < m_channels; ++channel) {
                 for (int y = 0; y < m_height; ++y) {
                     for (int x = 0; x < m_width; ++x) {
                         for (int b = 0; b < m_timeBins; ++b) {
                             double count = getData(m_cursor, channel, x, y, b);
                             yCount[b] += count;
                             photons += (int) count;
                         }
                     }
                 }
             }
         }
         else {
             // sum selected channel
             for (int y = 0; y < m_height; ++y) {
                 for (int x = 0; x < m_width; ++x) {
                     for (int b = 0; b < m_timeBins; ++b) {
                         double count = getData(m_cursor, m_channel, x, y, b);
                         yCount[b] += count;
                         photons += (int) count;
                     }
                 }
             }    
         }
         System.out.println("Summed photons " + photons);
 
         curveFitData.setYCount(yCount);
         yFitted = new double[m_timeBins];
         curveFitData.setYFitted(yFitted);
         curveFitDataList.add(curveFitData);
 
         // do the fit
         ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
         getCurveFitter(uiPanel).fitData(dataArray, m_startBin, m_stopBin);
 
         // show decay and update UI parameters
         showDecayGraph(uiPanel, dataArray);
         uiPanel.setParameters(dataArray[0].getParams());
     }
 
     /*
      * Sums and fits each ROI.
      */
     private void fitROIs(IUserInterfacePanel uiPanel) {
         double params[] = uiPanel.getParameters();
         
         // build the data
         ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
         ICurveFitData curveFitData;
         double yCount[];
         double yFitted[];
         
         int roiNumber = 0;
         for (Roi roi: getRois()) {
             ++roiNumber;
             curveFitData = new CurveFitData();
             curveFitData.setParams(params.clone());
             yCount = new double[m_timeBins];
             for (int b = 0; b < m_timeBins; ++b) {
                 yCount[b] = 0.0;
             }
             Rectangle bounds = roi.getBounds();
             for (int x = 0; x < bounds.width; ++x) {
                 for (int y = 0; y < bounds.height; ++y) {
                     if (roi.contains(bounds.x + x, bounds.y + y)) {
                         System.out.println("roi " + roiNumber + " x " + x + " Y " + y);
                         for (int b = 0; b < m_timeBins; ++b) {
                             yCount[b] += getData(m_cursor, m_channel, x, y, b);
                         }
                     }
                 }
             }
             curveFitData.setYCount(yCount);
             yFitted = new double[m_timeBins];
             curveFitData.setYFitted(yFitted);
             curveFitDataList.add(curveFitData);
         }
         
         // do the fit
         ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
         getCurveFitter(uiPanel).fitData(dataArray, m_startBin, m_stopBin);
         
         showDecayGraph(uiPanel, dataArray);
         
         // show colorized lifetimes
         ImageProcessor imageProcessor = new ColorProcessor(m_width, m_height);
         ImagePlus imagePlus = new ImagePlus("Fitted Lifetimes", imageProcessor);
         int i = 0;
         for (Roi roi: getRois()) {
             double lifetime = dataArray[i++].getParams()[1];
             imageProcessor.setColor(lifetimeColorMap(MAXIMUM_LIFETIME, lifetime));
 
             Rectangle bounds = roi.getBounds();
             for (int x = 0; x < bounds.width; ++x) {
                 for (int y = 0; y < bounds.height; ++y) {
                     if (roi.contains(bounds.x + x, bounds.y + y)) {
                         imageProcessor.drawPixel(bounds.x + x, bounds.y + y);
                     }
                 }
             }
         }
         imagePlus.show();  
 
         // update UI parameters
         uiPanel.setParameters(dataArray[0].getParams()); //TODO, just picked first ROI here!
     }
 
     /*
      * Fits a given pixel.
      * 
      * @param x
      * @param y
      */
     private void fitPixel(IUserInterfacePanel uiPanel, int x, int y) {
         double params[] = uiPanel.getParameters();
         
         // build the data
         ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
         ICurveFitData curveFitData;
         double yCount[];
         double yFitted[];
         
         curveFitData = new CurveFitData();
         curveFitData.setParams(params);
         yCount = new double[m_timeBins];
         for (int b = 0; b < m_timeBins; ++b) {
             yCount[b] = getData(m_cursor, m_channel, x, m_height - y - 1, b);
         }
         curveFitData.setYCount(yCount);
         yFitted = new double[m_timeBins];
         curveFitData.setYFitted(yFitted);
         curveFitDataList.add(curveFitData);
         
         // do the fit
         ICurveFitData dataArray[] = curveFitDataList.toArray(new ICurveFitData[0]);
         getCurveFitter(uiPanel).fitData(dataArray, m_startBin, m_stopBin);
         
         showDecayGraph(uiPanel, dataArray);
 
         // update UI parameters
         uiPanel.setParameters(dataArray[0].getParams());
     }
  
     /*
      * Fits each and every pixel.  This is the most complicated fit.
      *
      * If a channel is visible it is fit first and drawn incrementally.
      *
      * Results of the fit go to VisAD for analysis.
      */
     private void fitEachPixel(IUserInterfacePanel uiPanel) {
         long start = System.nanoTime();
         int pixelCount = 0;
         int totalPixelCount = totalPixelCount(m_width, m_height, m_channels, m_fitAllChannels);
         int pixelsToProcessCount = 0;
 
         Image<T> workImage = m_image;
         if (!SLIMBinning.NONE.equals(uiPanel.getBinning())) {
             workImage = m_binning.doBinning(uiPanel.getBinning(),  m_image);
         }
         LocalizableByDimCursor<T> pixelCursor = workImage.createLocalizableByDimCursor();
 
         ICurveFitter curveFitter = getCurveFitter(uiPanel);     
         double params[] = uiPanel.getParameters();
 
         boolean useFittedParams;
         LocalizableByDimCursor<DoubleType> resultsCursor = null;
         if (null == m_fittedImage || uiPanel.getParameterCount() != m_fittedParameterCount) {
             // can't use previous results
             useFittedParams = false;
             int channels = m_channels;
             m_fittedParameterCount = uiPanel.getParameterCount();
             m_fittedImage = makeImage(channels, m_width, m_height, m_fittedParameterCount);
         }
         else {
             // ask UI whether to use previous results
             useFittedParams = uiPanel.refineFit();
         }
         resultsCursor = m_fittedImage.createLocalizableByDimCursor();
         
         // build the data
         ArrayList<ICurveFitData> curveFitDataList = new ArrayList<ICurveFitData>();
         ArrayList<ChunkyPixel> pixelList = new ArrayList<ChunkyPixel>();
         ICurveFitData curveFitData;
         double yCount[];
         double yFitted[];
 
         // special handling for visible channel
         if (m_visibleFit) {
             // show colorized image
             DataColorizer dataColorizer = new DataColorizer(m_width, m_height, m_algorithm + " Fitted Lifetimes");
 
             ChunkyPixelEffectIterator pixelIterator = new ChunkyPixelEffectIterator(new ChunkyPixelTableImpl(), m_width, m_height);
 
             while (!m_cancel && pixelIterator.hasNext()) {
                 if (m_cancel) {
                     IJ.showProgress(0, 0); //TODO kludgy to have this here and also below; get rid of this but make the dataColorizer go away regardless
                     dataColorizer.quit();
                     cancelImageFit();
                     return;
                 }
                 IJ.showProgress(++pixelCount, totalPixelCount);
                 ChunkyPixel pixel = pixelIterator.next();
                 if (wantFitted(m_channel, pixel.getX(), pixel.getY())) {
                     curveFitData = new CurveFitData();
                     curveFitData.setChannel(m_channel);
                     curveFitData.setX(pixel.getX());
                     curveFitData.setY(pixel.getY());
                     curveFitData.setParams(
                             useFittedParams ?
                                 getFittedParams(resultsCursor, m_channel, pixel.getX(), pixel.getY(), m_fittedParameterCount) :
                                 params.clone());
                     yCount = new double[m_timeBins];
                     for (int b = 0; b < m_timeBins; ++b) {
                         yCount[b] = getData(pixelCursor, m_channel, pixel.getX(), pixel.getY(), b); //binnedData[m_channel][pixel.getY()][pixel.getX()][b];
                     }
                     curveFitData.setYCount(yCount);
                     yFitted = new double[m_timeBins];
                     curveFitData.setYFitted(yFitted);
                     curveFitDataList.add(curveFitData);
                     pixelList.add(pixel);
 
                     // process the pixels
                     if (++pixelsToProcessCount >= PIXEL_COUNT) {
                         pixelsToProcessCount = 0;
                         ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                         curveFitDataList.clear();
                         curveFitter.fitData(data, m_startBin, m_stopBin);
                         setFittedParamsFromData(resultsCursor, data);
                         colorizePixels(dataColorizer, m_height, m_channel, data, pixelList.toArray(new ChunkyPixel[0]));
                         pixelList.clear();
                     }
                 }
             }
             // handle any leftover pixels
             if (!m_cancel && pixelsToProcessCount > 0) {
                 pixelsToProcessCount = 0;
                 ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                 curveFitDataList.clear();
                 curveFitter.fitData(data, m_startBin, m_stopBin);
                 setFittedParamsFromData(resultsCursor, data);
                 colorizePixels(dataColorizer, m_height, m_channel, data, pixelList.toArray(new ChunkyPixel[0]));
             }
         }
         if (m_cancel) {
             IJ.showProgress(0, 0); //TODO the code below s/b showing progress also
            // dataColorizer.quit(); //TODO no longer visible in this code
             cancelImageFit();
             return;
         }
  
         // any channels still to be fitted?
         for (int channel : channelIndexArray(m_channel, m_channels, m_visibleFit, m_fitAllChannels)) {
             for (int y = 0; y < m_height; ++y) {
                 for (int x = 0; x < m_width; ++x) {
                     if (m_visibleFit) {
                         IJ.showProgress(++pixelCount, totalPixelCount);
                     }
  
                     if (wantFitted(channel, x, y)) {
                          ++pixelsToProcessCount;
                          curveFitData = new CurveFitData();
                          curveFitData.setChannel(channel);
                          curveFitData.setX(x);
                          curveFitData.setY(y);
                          curveFitData.setParams(
                              useFittedParams ?
                                  getFittedParams(resultsCursor, channel, x, y, m_fittedParameterCount) :
                                  params.clone());
                          yCount = new double[m_timeBins];
                          for (int b = 0; b < m_timeBins; ++b) {
                              yCount[b] = getData(pixelCursor, channel, x, y, b); //binnedData[channel][y][x][b];
                          }
                          curveFitData.setYCount(yCount);
                          yFitted = new double[m_timeBins];
                          curveFitData.setYFitted(yFitted);
                          curveFitDataList.add(curveFitData);
                     }
                     
                     if (m_cancel) {
                         cancelImageFit();
                         return;
                     }
                 }
                 // every row, process pixels as needed
                 if (pixelsToProcessCount >= PIXEL_COUNT) {
                     pixelsToProcessCount = 0;
                     ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
                     curveFitDataList.clear();
                     curveFitter.fitData(data, m_startBin, m_stopBin);
                     setFittedParamsFromData(resultsCursor, data);
                 }
             }
         }
         // handle any leftover pixels
         if (pixelsToProcessCount > 0) {
             ICurveFitData[] data = curveFitDataList.toArray(new ICurveFitData[0]);
             curveFitter.fitData(data, m_startBin, m_stopBin);
             setFittedParamsFromData(resultsCursor, data);
         }
 
 
         uiPanel.setFittedParameterCount(m_fittedParameterCount); //TODO kind of strange since I got that info from uiPanel earlier...  This s/b reset(true) or something Also, it doesn't really do anything in uiPanel
 
         long elapsed = System.nanoTime() - start;
         System.out.println("nanoseconds " + elapsed);
     }
 
     /**
      * Calculates the total number of pixels to fit.  Used for
      * progress bar.
      *
      * @param channels
      * @param fitAll
      * @return
      */
     private int totalPixelCount(int x, int y, int channels, boolean fitAll) {
         int count = x * y;
         if (fitAll) {
             count *= channels;
         }
         return count;
     }
 
     /**
      * Calculates an array of channel indices to iterate over.
      *
      * @param channel
      * @param channels
      * @param visibleFit
      * @param fitAll
      * @return
      */
     private int[] channelIndexArray(int channel, int channels, boolean visibleFit, boolean fitAll) {
         int returnValue[] = { };
         if (fitAll) {
             returnValue = new int[visibleFit ? channels - 1 : channels];
             int i = 0;
             for (int c = 0; c < channels; ++c) {
                 // skip visible; already processed
                 if (c != channel || !visibleFit) {
                     returnValue[i++] = c;
                 }
             }
         }
         else if (!visibleFit) {
             // single channel, not processed yet
             returnValue = new int[1];
             returnValue[0] = channel;
         }
         return returnValue;
     }
 
     private double getData(LocalizableByDimCursor<T> cursor, int channel, int x, int y, int bin) {
         int dim[];
         if (m_hasChannels) {
             dim = new int[] { x, y, bin, channel };
         }
         else {
             dim = new int[] { x, y, bin };
         }
         cursor.moveTo(dim);
         return cursor.getType().getRealFloat();
     }
 
     /**
      * Helper routine to create imglib.Image to store fitted results.
      *
      * @param width
      * @param height
      * @param components
      * @return
      */
     private Image<DoubleType> makeImage(int channels, int width, int height, int parameters) {
         Image<DoubleType> image = null;
 
         // create image object
         int dim[] = { width, height, channels, parameters }; //TODO when we keep chi square in image  ++parameters };
         image = new ImageFactory<DoubleType>(new DoubleType(), new PlanarContainerFactory()).createImage(dim, "Fitted");
 
         // initialize image
         Cursor<DoubleType> cursor = image.createCursor();
         while (cursor.hasNext()) {
             cursor.fwd();
             cursor.getType().set(Double.NaN);
         }
 
         return image;
     }
 
     private double[] getFittedParams(LocalizableByDimCursor<DoubleType> cursor, int channel, int x, int y, int count) {
         double params[] = new double[count];
         int position[] = new int[4];
         position[0] = x;
         position[1] = y;
         position[2] = channel;
         for (int i = 0; i < count; ++i) {
             position[3] = i;
             cursor.setPosition(position);
             params[i] = cursor.getType().getRealDouble();
         }
         return params;
     }
 
     private void setFittedParamsFromData(LocalizableByDimCursor<DoubleType> cursor, ICurveFitData dataArray[]) {
         int x, y;
         double[] params;
         for (ICurveFitData data : dataArray) {
             setFittedParams(cursor, data.getChannel(), data.getX(), data.getY(), data.getParams());
         }
     }
 
     private void setFittedParams(LocalizableByDimCursor<DoubleType> cursor, int channel, int x, int y, double[] params) {
         int position[] = new int[4];
         position[0] = x;
         position[1] = y;
         position[2] = channel;
         for (int i = 0; i < params.length; ++i) {
             position[3] = i;
             cursor.setPosition(position);
             cursor.getType().set(params[i]);
         }
     }
 
     private void cancelImageFit() {
         m_fittedImage = null;
         m_fittedParameterCount = 0;
     }
 
     /**
      * Visibly processes a batch of pixels.
      *
      * @param dataColorizer automatically sets colorization range and updates colorized image
      * @param height passed in to fix a vertical orientation problem
      * @channel current channel
      * @param data list of data corresponding to pixels to be fitted
      * @param pixels parallel list of rectangles with which to draw the fitted pixel
      */
     void colorizePixels(DataColorizer dataColorizer, int height, int channel, ICurveFitData data[], ChunkyPixel pixels[]) {
 
         // draw as you go; 'chunky' pixels get smaller as the overall fit progresses
         for (int i = 0; i < pixels.length; ++i) {
             ChunkyPixel pixel = pixels[i];
             double lifetime = data[i].getParams()[1];
 
            //TODO quick fix
            if (lifetime < 0.0) {
                System.out.println("negative lifetime " + lifetime + " at " + pixel.getX() + " " + pixel.getY());
                return;
            }

             //TODO debugging:
             //if (lifetime > 2 * m_param[1]) {
             //    System.out.println("BAD FIT??? x " + pixel.getX() + " y " + pixel.getY() + " fitted lifetime " + lifetime);
             //}
 
             //TODO BUG:
             // With the table as is, you can get
             //   x   y   w   h
             //   12  15  2   1
             //   14  15  2   1
             // all within the same drawing cycle.
             // So it looks like a 4x1 slice gets drawn (it
             // is composed of two adjacent 2x1 slices with
             // potentially two different colors).
             //if (pixel.getWidth() == 2) {
             //    System.out.println("x " + pixel.getX() + " y " + pixel.getY() + " w " + pixel.getWidth() + " h " + pixel.getHeight());
             //}
             //System.out.println("w " + pixel.getWidth() + " h " + pixel.getHeight());
             //System.out.println("lifetime is " + lifetime);
             //Color color = lifetimeColorMap(MAXIMUM_LIFETIME, lifetime);
             //imageProcessor.setColor(color);
             boolean firstTime = true;
             for (int x = pixel.getX(); x < pixel.getX() + pixel.getWidth(); ++x) {
                 for (int y = pixel.getY(); y < pixel.getY() + pixel.getHeight(); ++y) {
                     if (wantFitted(channel, x, y)) {
                         // (flip vertically)
                         dataColorizer.setData(firstTime, x, height - y - 1 , lifetime);
                         firstTime = false;
                     }
                 }
             }
         }
         dataColorizer.update();
     }
 
     /**
      * Checks criterion for whether this pixel needs to get fitted or drawn.
      *
      * @param channel
      * @param x
      * @param y
      * @return whether to include or ignore this pixel
      */
     boolean wantFitted(int channel, int x, int y) {
         return (aboveThreshold(channel, x, y) & isInROIs(x, y));
     }
 
     /**
      * Checks whether a given pixel is above threshold photon count value.
      *
      * @param channel
      * @param x
      * @param y
      * @return whether above threshold
      */
     boolean aboveThreshold(int channel, int x, int y) {
         return (m_threshold <= m_grayScaleImage.getPixel(channel, x, m_height - y - 1));
     }
 
     /**
      * Checks whether a given pixel is included in ROIs.  If no ROIs are
      * selected then all pixels are included.
      *
      * @param x
      * @param y
      * @return whether or not included in ROIs
      */
     boolean isInROIs(int x, int y) {
         Roi[] rois = getRois();
         if (0 < rois.length) {
             for (Roi roi: rois) {
                 if (roi.contains(x, y)) {
                     return true;
                 }
             }
             return false;
         }
         else {
             return true;
         }
     }
 
     /**
      * Gets a list of ROIs (may be empty).
      *
      * @return array of ROIs.
      */
     private Roi[] getRois() {
         Roi[] rois = {};
         RoiManager manager = RoiManager.getInstance();
         if (null != manager) {
             rois = manager.getRoisAsArray();
         }
         return rois;
     }
 
     /**
      * Colorizes a given lifetime value.
      *
      * Note this is much cruder than the DataColorizer that is
      * used in fitEachPixel.
      *
      * @param max
      * @param lifetime
      * @return
      */
     //TODO make consistent with fitEachPixel's DataColorizer
      private Color lifetimeColorMap(double max, double lifetime) {
         Color returnColor = Color.BLACK;
         if (lifetime > 0.0) {
             if (lifetime < max/2.0) {
                 returnColor = interpolateColor(Color.BLUE, Color.GREEN, 2.0 * lifetime / max);
             }
             else if (lifetime < max) {
                 returnColor = interpolateColor(Color.GREEN, Color.RED, 2.0 * (lifetime - max / 2.0) / max);
             }
             else returnColor = Color.RED;
         }
         return returnColor;
     }
 
      /**
       * Interpolates between two colors based on a blend factor.
       *
       * @param start color
       * @param end color
       * @param blend factor
       * @return interpolated color
       */
     private Color interpolateColor(Color start, Color end, double blend) {
         int startRed   = start.getRed();
         int startGreen = start.getGreen();
         int startBlue  = start.getBlue();
         int endRed   = end.getRed();
         int endGreen = end.getGreen();
         int endBlue  = end.getBlue();
         int red   = interpolateColorComponent(startRed, endRed, blend);
         int green = interpolateColorComponent(startGreen, endGreen, blend);
         int blue  = interpolateColorComponent(startBlue, endBlue, blend);
         return new Color(red, green, blue);
     }
 
     /**
      * Interpolates a single RGB component between two values based on
      * a blend factor.
      *
      * @param start component value
      * @param end component value
      * @param blend factor
      * @return interpolated component value
      */
     private int interpolateColorComponent(int start, int end, double blend) {
         return (int)(blend * (end - start) + start);
     }
 
     /*
      * Gets the appropriate curve fitter for the current fit.
      *
      * @param uiPanel has curve fitter selection
      */
     private ICurveFitter getCurveFitter(IUserInterfacePanel uiPanel) {
         ICurveFitter curveFitter = null;
         switch (uiPanel.getAlgorithm()) {
             case JAOLHO:
                 curveFitter = new JaolhoCurveFitter();
                 break;
            /* case AKUTAN:
                 curveFitter = new AkutanCurveFitter();
                 break; */
             case BARBER_RLD:
                 curveFitter = new GrayCurveFitter(0);
                 break;
             case BARBER_LMA:
                 curveFitter = new GrayCurveFitter(1);
                 break;
             case MARKWARDT:
                 curveFitter = new MarkwardtCurveFitter();
                 break;
             case BARBER2_RLD:
                 curveFitter = new GrayNRCurveFitter(0);
                 break;
             case BARBER2_LMA:
                 curveFitter = new GrayNRCurveFitter(1);
                 break;
             case SLIMCURVE_RLD:
                 curveFitter = new SLIMCurveFitter(SLIMCurveFitter.AlgorithmType.RLD);
                 break;
             case SLIMCURVE_LMA:
                 curveFitter = new SLIMCurveFitter(SLIMCurveFitter.AlgorithmType.LMA);
                 break;
         }
         ICurveFitter.FitFunction fitFunction = null;
         switch (uiPanel.getFunction()) {
             case SINGLE_EXPONENTIAL:
                 fitFunction = ICurveFitter.FitFunction.SINGLE_EXPONENTIAL;
                 break;
             case DOUBLE_EXPONENTIAL:
                 fitFunction = ICurveFitter.FitFunction.DOUBLE_EXPONENTIAL;
                 break;
             case TRIPLE_EXPONENTIAL:
                 fitFunction = ICurveFitter.FitFunction.TRIPLE_EXPONENTIAL;
                 break;
             case STRETCHED_EXPONENTIAL:
                 fitFunction = ICurveFitter.FitFunction.STRETCHED_EXPONENTIAL;
                 break;
         }
         curveFitter.setFitFunction(fitFunction);
         curveFitter.setXInc(m_timeRange);
         curveFitter.setFree(uiPanel.getFree());
         return curveFitter;
     }
 
     /*
      * Helper function for the fit.  Shows the decay curve.
      * 
      * @param dataArray array of fitted data
      */
     private void showDecayGraph(final IUserInterfacePanel uiPanel, ICurveFitData dataArray[]) {
         if (0 < dataArray.length) {
             //TODO need to be able to examine any fitted pixel; for now just show the first.  // was last
             DecayGraph decayGraph = new DecayGraph(m_startBin, m_stopBin, m_timeBins, m_timeRange, dataArray[0]); //dataArray.length - 1]);
             decayGraph.setStartStopListener(
                 new IStartStopListener() {
                     public void setStartStop(int start, int stop) {
                         uiPanel.setStart(start);
                         uiPanel.setStop(stop);
                     }
                 }
             );
             JFrame frame = decayGraph.getFrame();
             frame.setLocationRelativeTo(uiPanel.getFrame());
             frame.setVisible(true);
         }        
     }
 }
