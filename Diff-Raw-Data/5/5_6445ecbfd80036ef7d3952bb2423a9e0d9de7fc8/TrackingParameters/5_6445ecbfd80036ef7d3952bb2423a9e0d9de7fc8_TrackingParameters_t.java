 //FILE:          TrackingPrefDialog.java
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
 
 import ij.gui.GenericDialog;
 
 /**
  * The Preferences dialog .
  */
 public class TrackingParameters {
 
 	final static String MAX_BLINKING_KEY = "trackerMaxBlinking";
 	final static String MAX_DISPLACEMENT_KEY = "trackerMaxDsp";
 	final static String ERROR_THRESHOLD_KEY = "errorThreshold";
 	final static String LOWER_BOUND_KEY = "lowerBound";
 	
 	private static Preferences prefs_ = GlobalPrefs.getRoot().node(TrackingParameters.class.getName());
 
 	public static double trackerMaxDsp_ = prefs_.getDouble(MAX_DISPLACEMENT_KEY, 2.0);
 	public static int trackerMaxBlinking_ = prefs_.getInt(MAX_BLINKING_KEY, 0);
 	public static double errorThreshold_ = prefs_.getDouble(ERROR_THRESHOLD_KEY, -1.0);
 	public static double trackerLowerBound_ = prefs_.getDouble(LOWER_BOUND_KEY, 0.1);
 	
 	/**
 	 * Open dialog.
 	 */
 	static public boolean openDialog() {
 	
 		GenericDialog dlg = new GenericDialog("Tracking Options");
 		dlg.addMessage("- Tracking -");
		dlg.addNumericField("Max Displacement (pixels)", trackerMaxDsp_, 1);
 		dlg.addNumericField("Max Blinking", (double)trackerMaxBlinking_, 0);
		dlg.addNumericField("Resolution (pixels)", (double)trackerLowerBound_, 3);
 		dlg.addNumericField("Confidence Threshold", errorThreshold_, 0);
 
 		dlg.showDialog();
 		if (dlg.wasCanceled())
 			return false;
 		
 		trackerMaxDsp_ = dlg.getNextNumber();
 		trackerMaxBlinking_ = (int) dlg.getNextNumber();
 		errorThreshold_ = dlg.getNextNumber();
 		
 		prefs_.putInt(MAX_BLINKING_KEY,trackerMaxBlinking_);
 		prefs_.putDouble(MAX_DISPLACEMENT_KEY, trackerMaxDsp_);
 		prefs_.putDouble(ERROR_THRESHOLD_KEY , errorThreshold_);
 		prefs_.putDouble(LOWER_BOUND_KEY, trackerLowerBound_);
 		return true;
 	}
 }
