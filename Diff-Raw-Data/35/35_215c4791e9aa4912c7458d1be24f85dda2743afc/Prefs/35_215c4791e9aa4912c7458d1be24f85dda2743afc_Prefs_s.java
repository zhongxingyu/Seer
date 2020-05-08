 //FILE:          Prefs.java
 //PROJECT:       Octane
 //-----------------------------------------------------------------------------
 //
 // AUTHOR:       Ji Yu, jyu@uchc.edu, 2/16/08
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
 //               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES./**
 //
 package edu.uchc.octane;
 
 import java.util.prefs.Preferences;
 
 /**
  * The Prefences.
  */
 public class Prefs {
 
 	final static String LAST_WORKING_DIR_KEY = "LastWorkingDir";
 	final static String VIRTUAL_STACK_KEY = "VirtualStack";
 	final static String DIC_XOFFSET_KEY = "DicXOffset";
 	final static String DIC_YOFFSET_KEY = "DicYOffset";
 	final static String BALL_RADIUS_KEY = "BallRadius";
 	final static String POINT_INDICATOR_KEY = "PointIndicator";
 	final static String SHOW_OVERLAY_KEY = "ShowOverlay";
 	final static String OMIT_SINGLE_FRAME_TRAJS = "OmitSingleFrame";
 	final static String PALM_SCALE_FACTOR = "PalmRatio";
 	final static String MAX_PEAK_AREA = "MaxPeakArea";
 	final static String PALM_PSD_WIDTH = "PalmPSDWidth";
 	final static String TRACKER_MAX_BLINKING = "trackerMaxBlinking";
 	final static String TRACKER_MAX_DISPLACEMENT = "trackerMaxDsp";
 	final static String PALM_THRESHOLD = "palmThreshold";
 	final static String REFINE_PEAK_KEY = "refinePeak";
 	final static String REFINER_KEY = "refiner";
 	final static String HISTOGRAMBINS_KEY = "histogramBins";
 	final static String RESIDUETHRESHOLD_KEY = "histogramBins";
 	final static String SLOPPYNESS_KEY = "sloppyness";
 	final static String KERNELSIZE_KEY = "kernelSize";
 	final static String SIGMA_KEY = "sigma";
 	final static String IFS_SCALE_FACTOR = "IFSRatio";
 	//static String lastWorkingDir;
 
 	static boolean initialized = false;
 	static boolean virtualStack_ = true;
 	static int dicXOffset_ = -9;
 	static int dicYOffset_ = 0;
 	static int ballRadius_ = 50;
 	static boolean pointIndicator_ = true;
 	static boolean showOverlay_ = true;
 	static boolean omitSingleFrameTrajs_ = false;
 	static double palmScaleFactor_ = 10.0;
 	static double palmPSDWidth_ = 0.1875;
 	static int maxPeakArea_ = 300;
	static double trackerMaxDsp_ = 6;
 	static int trackerMaxBlinking_ = 0;
 	static double palmThreshold_ = 100; 
 	static boolean refinePeak_ = true;
 	static int refiner_ = 1;
 	static int histogramBins_ = 20;
 	static double residueThreshold_ = 100;
 	static int sloppyness_ = 1;
 	static int kernelSize_ = 2;
 	static double sigma_ = 0.94;
 	static int IFSScaleFactor_ = 3;
 	static double trackerLowerBound_ = 1.0;
 	
 	static Preferences pref_;
 
 	/**
 	 * Load preferences.
 	 */
 	public static void loadPrefs() {
 		if (initialized)
 			return;
 
 		Preferences pref = Preferences.userNodeForPackage(Preferences.class);
 		pref = pref.node(pref.absolutePath());
 		//lastWorkingDir = pref.get(LAST_WORKING_DIR_KEY, null);
 
 		virtualStack_ = pref.getBoolean(VIRTUAL_STACK_KEY, virtualStack_);
 		ballRadius_ = pref.getInt(BALL_RADIUS_KEY, ballRadius_);
 		pointIndicator_ = pref.getBoolean(POINT_INDICATOR_KEY, pointIndicator_);
 		showOverlay_ = pref.getBoolean(SHOW_OVERLAY_KEY, showOverlay_);
 		omitSingleFrameTrajs_ = pref.getBoolean(OMIT_SINGLE_FRAME_TRAJS, omitSingleFrameTrajs_);
 		palmScaleFactor_ = pref.getDouble(PALM_SCALE_FACTOR, palmScaleFactor_);
 		palmPSDWidth_ = pref.getDouble(PALM_PSD_WIDTH, palmPSDWidth_);
 		maxPeakArea_ = pref.getInt(MAX_PEAK_AREA, maxPeakArea_);
 		trackerMaxBlinking_ = pref.getInt(TRACKER_MAX_BLINKING, trackerMaxBlinking_ );
 		trackerMaxDsp_ = pref.getDouble(TRACKER_MAX_DISPLACEMENT, trackerMaxDsp_ );
 		palmThreshold_ = pref.getDouble(PALM_THRESHOLD, palmThreshold_);
 		refinePeak_ = pref.getBoolean(REFINE_PEAK_KEY, refinePeak_);
 		refiner_ = pref.getInt(REFINER_KEY, refiner_);
 		histogramBins_ = pref.getInt(HISTOGRAMBINS_KEY, histogramBins_);
 		residueThreshold_ = pref.getDouble(RESIDUETHRESHOLD_KEY, residueThreshold_);
 		sloppyness_ = pref.getInt(SLOPPYNESS_KEY, sloppyness_);
 		kernelSize_ = pref.getInt(KERNELSIZE_KEY, kernelSize_);
 		sigma_ = pref.getDouble(SIGMA_KEY, sigma_);
 		IFSScaleFactor_ = pref.getInt(IFS_SCALE_FACTOR, IFSScaleFactor_);
 		pref_ = pref;
 		initialized = true;
 	}
 
 	/**
 	 * Save preferences.
 	 */
 	public static void savePrefs() {
 		//pref_.put(LAST_WORKING_DIR_KEY, lastWorkingDir);
 
 		if (! initialized )
 			return;
 
 		pref_.putBoolean(VIRTUAL_STACK_KEY, virtualStack_);
 		pref_.putInt(DIC_XOFFSET_KEY, dicXOffset_);
 		pref_.putInt(DIC_YOFFSET_KEY, dicYOffset_);
 		pref_.putInt(BALL_RADIUS_KEY, ballRadius_);
 		pref_.putBoolean(POINT_INDICATOR_KEY, pointIndicator_);
 		pref_.putBoolean(SHOW_OVERLAY_KEY, showOverlay_);
 		pref_.putBoolean(OMIT_SINGLE_FRAME_TRAJS, omitSingleFrameTrajs_);
 		pref_.putDouble(PALM_SCALE_FACTOR, palmScaleFactor_);
 		pref_.putDouble(PALM_PSD_WIDTH, palmPSDWidth_);
 		pref_.putInt(MAX_PEAK_AREA, maxPeakArea_);
 		pref_.putInt(TRACKER_MAX_BLINKING,trackerMaxBlinking_);
 		pref_.putDouble(TRACKER_MAX_DISPLACEMENT, trackerMaxDsp_);
 		pref_.putDouble(PALM_THRESHOLD, palmThreshold_);
 		pref_.putBoolean(REFINE_PEAK_KEY, refinePeak_);
 		pref_.putInt(REFINER_KEY, refiner_);
 		pref_.putInt(HISTOGRAMBINS_KEY, histogramBins_);
 		pref_.putDouble(RESIDUETHRESHOLD_KEY , residueThreshold_);
 		pref_.putInt(SLOPPYNESS_KEY, sloppyness_);
 		pref_.putInt(KERNELSIZE_KEY, kernelSize_);
 		pref_.putDouble(SIGMA_KEY, sigma_);
 		pref_.putInt(IFS_SCALE_FACTOR, IFSScaleFactor_);		
 	}
 
 }
