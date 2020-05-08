 package de.embl.cmci.radial;
 /** 
  	based on plugin http://imagej.nih.gov/ij/plugins/radial-profile.html
 	@author Kota Miura (miura@embl.de) April. 18-21, 2011
 	modification purpose
 	(1) enhance usability from javascript, without dialog and plot show up 
 	(2) to use it as a library. 
 	(3) profile data accessible by Javascript
 
 	Modified: (SCRIPT FRIENDLY VERSION)
 	- use arguments for setup("whatever ", imp).
 	- doRadialDistribution(ImageProcessor ip) method was changed, 
 		so that statistics outside oval ROI will not be	included in the measurement value.
 	- added a field value "Radius", just to check if the sampled radius are OK.
 	- added plotter method.
 	- librarization done
 
 	20111012 update: skips NaN pixels (with Thomas and Dirk)
 	 
    Kota Miura (miura@embl.de)
 */
 
 import ij.*;
 import ij.process.*;
 import ij.gui.*;
 import ij.measure.Calibration;
 import java.awt.*;
 
 //import java.util.*;
 
 public class RadialProfile {
 
 	ImagePlus imp;
 	boolean canceled=false;
	double X0 = 0.0;
	double Y0 = 0.0;
 	double mR;
 	Rectangle rct;
 	int nBins=100;
 	//static boolean doNormalize = true;
 	boolean useCalibration = false;
 	boolean isScript = false;
 	boolean doPlot = true;
 	float[][] Accumulator;
 	double [][] Radius;
 	String[] paraA;
 	
 	public float[][] getAccumulator() {
 		return Accumulator;
 	}
 
 	public double[][] getRadius() {
 		return Radius;
 	}
 	//for setting parameters externally.
 	public void setVars(double x0, double y0, double mr, boolean usecalib, boolean doplot) {
 		X0 = x0;
 		Y0 = y0;
 		mR = mr;
 		useCalibration = usecalib;
 		doPlot = doplot;
    isScript = true;
 	}
 	//Depreciated
 	public void ArgumentParser(String arg){
 		String[] argA = arg.split(" ");
 		for (int i = 0 ; i < argA.length; i++){
 			if (argA[i].startsWith("x=")) X0 =  Double.parseDouble(argA[i].split("=")[1]);
 			else if (argA[i].startsWith("y=")) Y0 =  Double.parseDouble(argA[i].split("=")[1]);
 			else if (argA[i].startsWith("radius=")) mR =  Double.parseDouble(argA[i].split("=")[1]);
 			else if (argA[i].startsWith("use")) useCalibration = true;
 			else if (argA[i].startsWith("noplot")) doPlot = false;
 			paraA = argA[i].split("=");
 		}		
 	}
 
 	public void ArgumentParserV2(String arg){
 			X0 =  Double.parseDouble(Macro.getValue(arg, "x", "0"));
 			Y0 = Double.parseDouble(Macro.getValue(arg, "y", "0"));
 			mR = Double.parseDouble(Macro.getValue(arg, "radius", "0"));
 			useCalibration = Boolean.parseBoolean(Macro.getValue(arg, "use", "false"));
 			doPlot = Boolean.parseBoolean(Macro.getValue(arg, "noplot", "false"));
 	}
 	
 	public void doit(ImagePlus imp, String arg) {
 		this.imp = imp;
 		ImageProcessor ip = imp.getProcessor();
 		if ((arg != "") && (arg != null)) {
 			ArgumentParserV2(arg);
 			isScript = true;
 			if (useCalibration)IJ.log("calbrate data");
 			else IJ.log("no calbration");
 		}
 		
 		if (!isScript) {
 			setXYcenter();
 		} else {
 			if ((X0 > imp.getWidth()) || (X0 <0) || (Y0 > imp.getHeight()) ||  (Y0 <0)){
 			
 				setXYcenter();
 				IJ.log("since assigned X0, Y0 was out of image dimension, center was set to default value.");
 			}
 		}
 		double mRX = Math.min(Math.abs(X0 - 0.0), Math.abs(X0 - imp.getWidth()));
 		double mRY = Math.min(Math.abs(Y0 - 0.0), Math.abs(Y0 - imp.getHeight()));
 		double minR = Math.min(mRX, mRY);
 		if (mR > minR) {
 			mR = minR;
 			IJ.log("Radius adjusted since assigned value was larger than image dimension.");
 		}
 		IJ.makeOval((int)(X0-mR), (int)(Y0-mR), (int)(2*mR), (int)(2*mR));
 		if (!isScript) {
 			doDialog();
 			IJ.makeOval((int)(X0-mR), (int)(Y0-mR), (int)(2*mR), (int)(2*mR));
 		}
 		imp.startTiming();
 		if (canceled) return;
 		doRadialDistribution(ip);
 	}
 	
 	private void setXYcenter() {
 		if (imp.getRoi() == null)
 			imp.setRoi(0, 0, imp.getWidth(), imp.getHeight());
 		rct = imp.getRoi().getBounds();
 		X0 = (double)rct.x+(double)rct.width/2;
 		Y0 =  (double)rct.y+(double)rct.height/2;
 		mR =  Math.min(rct.width/2, rct.height/2);
 	}
 
 	//Kota Look like this should be fixed.
 	//TODO
 	private void doRadialDistribution(ImageProcessor ip) {
 		//K nBins = (int) (3*mR/4);
 		nBins = (int) (Math.floor(mR));
 		int thisBin;
 		Accumulator = new float[2][nBins];	
 		double R;
 		int xmin = (int) Math.floor(X0-mR);
 		int xmax = (int) Math.floor(X0+mR);
 		int ymin = (int) Math.floor(Y0-mR);
 		int ymax = (int) Math.floor(Y0+mR);
 		Radius = new double[(int) (xmax - xmin + 1.0)][(int) (ymax - ymin + 1.0)];
 		for (int i = xmin; i < xmax; i++) {
 			for (int j = ymin; j < ymax; j++) {
 				R = Math.sqrt((i-X0)*(i-X0)+(j-Y0)*(j-Y0));
 				if (R==0)
 					thisBin = 0;
 				else {
 					//thisBin = (int) Math.floor((R/mR)*(double)nBins);
 					thisBin = (int) Math.floor(R);
 					//if (thisBin == 0) thisBin = 1;	//
 				}
 				//if (thisBin==0)
 				//	if ((i != X0) && (i != Y0)) 
 				//		thisBin=1;
 				//thisBin=thisBin-1;
 				//if (thisBin>nBins-1) thisBin=nBins-1;
 				if (thisBin<nBins) {
 					if (!Double.isNaN(imp.getProcessor().getPixelValue((int)i,(int)j))) {
 						Accumulator[0][thisBin]=Accumulator[0][thisBin]+1;
 						Accumulator[1][thisBin]=Accumulator[1][thisBin]+ip.getPixelValue((int)i,(int)j);
 						Radius[(int) (i-xmin)][(int) (j-ymin)] = thisBin;
 					}
 				}
  			}
 		}
 		
 		Calibration cal = imp.getCalibration();
 		if (cal.getUnit() == "pixel") useCalibration=false;
 		Plot plot = null;
 		if (useCalibration) {
 			for (int i=0; i<nBins;i++) {
 				Accumulator[1][i] =  Accumulator[1][i] / Accumulator[0][i];
 				//Accumulator[0][i] = (float)(cal.pixelWidth*mR*((double)(i+1)/nBins));
 				Accumulator[0][i] = (float)(cal.pixelWidth*i);
 			}
 			//if (doPlot) plot = new Plot("Radial Profile Plot", "Radius ["+cal.getUnits()+"]", "Normalized Integrated Intensity",  Accumulator[0], Accumulator[1]);
 			if (doPlot) plot = new Plot("Radial Profile Plot", "Radius ["+cal.getUnits()+"]", "Mean Intensity",  Accumulator[0], Accumulator[1]);
 
 		} else {
 			for (int i=0; i<nBins;i++) {
 				Accumulator[1][i] = Accumulator[1][i] / Accumulator[0][i];
 				//Accumulator[0][i] = (float)(mR*((double)(i+1)/nBins));
 				Accumulator[0][i] = i;
 			}
 			//if (doPlot) plot = new Plot("Radial Profile Plot", "Radius [pixels]", "Normalized Integrated Intensity",  Accumulator[0], Accumulator[1]);
 			if (doPlot) plot = new Plot("Radial Profile Plot", "Radius [pixels]", "Mean Intensity",  Accumulator[0], Accumulator[1]);
 		}
 		if (doPlot) plot.show();
 	}
 	public void plotter(){
 		if (Accumulator == null)return;
 		Calibration cal = imp.getCalibration();
 		if (cal.getUnit() == "pixel") useCalibration=false;
 		Plot plot = null;
 		if (useCalibration) {
 			plot = new Plot("Radial Profile Plot", "Radius ["+cal.getUnits()+"]", "Mean Intensity",  Accumulator[0], Accumulator[1]);
 		} else {
 			plot = new Plot("Radial Profile Plot", "Radius [pixels]", "Mean Intensity",  Accumulator[0], Accumulator[1]);
 		}
 		plot.show();
 	}
 
 	private void doDialog() {
 		canceled=false;
 		GenericDialog gd = new GenericDialog("Radial Distribution...", IJ.getInstance());
 		gd.addNumericField("X center (pixels):",X0,2);
 		gd.addNumericField("Y center (pixels):", Y0,2);
 		gd.addNumericField("Radius (pixels):", mR,2);
 		//gd.addCheckbox("Normalize", doNormalize);
 		gd.addCheckbox("Use Spatial Calibration", useCalibration);
 		gd.showDialog();
 		if (gd.wasCanceled()) {
 			canceled = true;
 			return;
 		}
 		X0=gd.getNextNumber();
 		Y0=gd.getNextNumber();
 		mR=gd.getNextNumber();
 		//doNormalize = gd.getNextBoolean();
 		useCalibration = gd.getNextBoolean();
 		if(gd.invalidNumber()) {
 			IJ.showMessage("Error", "Invalid input Number");
 			canceled=true;
 			return;
 		}
 	}
 }
 
 
