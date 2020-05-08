 ///////////////////////////////////////////////////////////////////////////////
 // Title:            Find_Particle_Areas
 // Description:		 ImageJ plugin to isolate cell particles in images and image
 //					 stacks. Intended to be used in realtime and offline analysis
 //					 of flow cytometry experiments. 
 //
 // 					 Three main functions: determining sum pixel area of cell or
 //					 particle in brightfield images, determining sum pixel 
 //					 intensity count in intensity images, and calculating ratio
 //					 of particle intensity per particle area.
 //
 // Author:           Ajeetesh Vivekanandan, UW-Madison LOCI
 // Contact:			 ajeet.vivekanandan@gmail.com
 // Web:				 loci.wisc.edu
 //
 ///////////////////////////////////////////////////////////////////////////////
 /**
  * @author Ajeet Vivekanandan 
  * @author UW-Madison LOCI
  */
 
 package loci.apps.flow;
 
 import java.awt.image.ColorModel;
 import java.awt.image.IndexColorModel;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import ij.IJ;
 import ij.ImageJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.WindowManager;
 import ij.gui.GenericDialog;
 import ij.gui.Roi;
 import ij.macro.Interpreter;
 import ij.measure.Measurements;
 import ij.measure.ResultsTable;
 import ij.plugin.Duplicator;
 import ij.plugin.ImageCalculator;
 import ij.plugin.filter.GaussianBlur;
 import ij.plugin.filter.ParticleAnalyzer;
 import ij.plugin.filter.PlugInFilter;
 import ij.plugin.filter.ThresholdToSelection;
 import ij.process.ByteProcessor;
 import ij.process.ColorProcessor;
 import ij.process.ImageProcessor;
 import ij.process.ImageStatistics;
 import ij.text.TextWindow;
 
 
 public class Find_Particle_Areas implements PlugInFilter {
 	private ImagePlus imp, bfImpOrig, intImpOrig, bfImp, intImp;
 	private String myMethod;
 	private double thresholdMin, gaussianSigma;
 	private boolean excludeOnEdge, checkIndividualParticles, doFullStack, multipleImagesAvail, doRatio, inPlugInMode;
 	private int sizeMin, currSlice, stackSize;
 	private Duplicator duplicator;
 	private TextWindow twindow;
 	private ParticleAnalyzer particleAnalyzer;
 	private ResultsTable rt;
 	private ImageCalculator ic;
 	private GaussianBlur gb;
 
 	public Find_Particle_Areas(){
 	}
 
 	public Find_Particle_Areas(ImagePlus image, ImagePlus brightfieldImage, ImagePlus intensityImage, String method, double minThresh, double sigma, int minSize, boolean excludeEdge, boolean ratioMode){
 		imp = image;
 		bfImpOrig = brightfieldImage;
 		intImpOrig = intensityImage;
 		bfImp = bfImpOrig.duplicate();
 		intImp = intImpOrig.duplicate();
 		myMethod = method;
 		thresholdMin = minThresh;
 		gaussianSigma = sigma;
 		sizeMin = minSize;
 		excludeOnEdge = excludeEdge;
 		doRatio = ratioMode;
 		checkIndividualParticles = !ratioMode;
 		doFullStack=false;
 		inPlugInMode = false;
 		multipleImagesAvail = (bfImp==null||intImp==null)? false:true;
 	}
 
 	public void setFullStackOption(boolean option){
 		doFullStack = option;
 	}
 
 	public static void main(String[] args){
 		new ImageJ();
 		new IJ();
 		ImagePlus bfImage = IJ.openImage("C:/Users/Ajeet/Desktop/sampleBf.tif");
 		ImagePlus intImage = IJ.openImage("C:/Users/Ajeet/Desktop/sampleInt.tif");
 		bfImage.show();
 		intImage.show();
 		Find_Particle_Areas fpa = new Find_Particle_Areas();
 		fpa.run(null);
 	}
 
 	/**
 	 * Original setup method called by ImageJ when plugin is run.
 	 *
 	 * @param arg String arg0
 	 * @param image ImagePlus image to run calculation on
 	 */
 	public int setup(String arg, ImagePlus image) {
 		imp = image;
 		return DOES_8G | NO_CHANGES;
 	}
 
 	public void run(ImageProcessor arg0) {
 		if(!createDialog()){
 			//	IJ.showMessage("Please enter or choose correct parameters");
 			return;
 		}
 		if(doRatio){
 			createRatioMask();
 			//clean up
 			bfImp.close();
 			intImp.close();
 		} else if (doFullStack){
 			analyzeSingleStack();
 		} else if (checkIndividualParticles){
 			IJ.log(analyzeIndividualParticles()[0] + "");
 		}
 
 		Interpreter.batchMode=false;
 	}
 
 	private boolean createDialog(){
 		try{
 			GenericDialog gd = new GenericDialog("Calculate Particle Areas");
 			String [] methods={"Brightfield", "Intensity"};
 			gd.addChoice("Channel:", methods, methods[0]);
 			gd.addMessage ("Special paramters; (thresholdMax = 255, SizeMax=Infinity)");
 			gd.addNumericField ("Threshold_Minimum",  30, 0);
 			gd.addNumericField ("Size_Minimum",  100, 0);
 			gd.addNumericField ("Gaussian_Sigma",  2.2, 0);
 			gd.addCheckbox("Exclude_Particles_on_Edge",true);
 			gd.addCheckbox("Check_Individual_Particles",false);
 			gd.addCheckbox("Run_Plugin_Over_Entire_Stack", false);
 
 			//below is basically: if there area more than one image open, allow the
 			//	"calculate ratio between" option and populate option boxes
 			int[] wList = WindowManager.getIDList();
 			if(wList!=null && wList.length>=2){
 				String[] availImages = new String[wList.length];
 				for (int i=0; i<wList.length; i++) {
 					imp = WindowManager.getImage(wList[i]);
 					availImages[i] = imp!=null?imp.getTitle():"";
 				}
 				gd.addMessage ("Check box below to calculate intensity/area ratio for two images.");
 				gd.addCheckbox("Calculate Ratio between:", false);
 				gd.addChoice("BRIGHTFIELD image:", availImages, availImages[0]);
 				gd.addChoice("INTENSITY image:", availImages, availImages[1]);
 				multipleImagesAvail = true;
 			} else 
 				multipleImagesAvail = false;
 
 			gd.showDialog();
 			if (gd.wasCanceled()) return false;
 
 			myMethod = gd.getNextChoice();
 			thresholdMin= gd.getNextNumber();
 			sizeMin= (int) gd.getNextNumber();
 			gaussianSigma=gd.getNextNumber();
 			excludeOnEdge=gd.getNextBoolean(); 
 			checkIndividualParticles= gd.getNextBoolean();
 			doFullStack = gd.getNextBoolean();
 			doRatio = multipleImagesAvail? gd.getNextBoolean():false;
 			//set up all required objects 
 			if(doRatio){
 				int index = gd.getNextChoiceIndex();
 				bfImpOrig = WindowManager.getImage(wList[index]);
 				bfImp = bfImpOrig.duplicate();
 				index = gd.getNextChoiceIndex();
 				intImpOrig = WindowManager.getImage(wList[index]);
 				intImp = intImpOrig.duplicate();
 			} else if(doFullStack){
 				stackSize = imp.getStackSize();
 				currSlice = imp.getCurrentSlice();
 			} 
 			rt = new ResultsTable();
 			inPlugInMode = true;
 			Interpreter.batchMode=true;
 			return true;
 		}catch(Throwable e){
 			IJ.log("Error encountered while parsing dialog box.");
 			IJ.log(e.getStackTrace().toString());
 			Interpreter.batchMode=false;
 		}
 		return false;
 	}
 
 	/**
 	 * Creates a ratio mask stack for both brightfield and intensity images. Intensity image masks contain only
 	 * the pixels above threshold INSIDE the brightfield image's cell outline (if there is a cell). 
 	 * 
 	 * @return ImagePlus[] array of 2 ImagePlus objects, array[0] = brightfield Image, array[1] = intensity Image
 	 */
 	public ImagePlus[] createRatioMask(){
 		try{
 		//If we're using this plugin as a class for some other class/main instead of through ImageJ, assume 
 		//	that class/main will handle how to display the data, just return the masks as an array...
 		//	Otherwise create and display info in a TextWindow below
 
 		double ratio=0, bfAreas=0, intAreas=0;
 		long[] meanIntensities = null;
 		boolean particleDetected = false;
 		boolean prevParticleDetected = false;
 		int numParticlesDetected = 0;
 		ImagePlus tempInt = null, tempBF = null, 
 				bfMask = new ImagePlus("Cell Outlines"), intMask = new ImagePlus("Cell Intensity inside Outlines"),
 				duplicatedBF = null, duplicatedInt = null;
 		ImageStack intMaskStack = null, bfMaskStack = null;
 		ImageProcessor tempIP;
 		ImageStatistics stats;
 		duplicator = new Duplicator();
 		ic = new ImageCalculator();
 		gb = new GaussianBlur();
 		Roi tempRoi = null;
 		ThresholdToSelection tts = new ThresholdToSelection();
 		ArrayList<Float> resultsBF = new ArrayList<Float>();
 		ArrayList<Float> resultsIN = new ArrayList<Float>();
 		ArrayList<Float> resultsRATIO = new ArrayList<Float>();
 		ArrayList<Float> resultsMeanIN = new ArrayList<Float>();
 		ArrayList<Float> resultsTotalIN = new ArrayList<Float>();
 
 		//only needed if this method is executed through ImageJ
 		if(inPlugInMode){
 			twindow = new TextWindow("RATIO of Found Particles", "Slice \t Brightfield Area \t Intensity Area \t RATIO \t Mean Intensity above Threshold \t Total Intensity", "", 800, 300);
 			byte[] r = new byte[256];
 			for(int ii=0 ; ii<256 ; ii++)
 				r[ii]=(byte)ii;
 			ColorModel theCM = new IndexColorModel(8, 256, r,r,r);
 			intMaskStack = new ImageStack(intImp.getWidth(), intImp.getHeight(), theCM);
 			bfMaskStack = new ImageStack(bfImp.getWidth(), bfImp.getHeight(), theCM);
 		}
 
 		for (int i=bfImp.getCurrentSlice(); i<=bfImp.getStackSize(); i++){
 			try{
 				//have the user be able to observe which image the plugin is at
 				bfImpOrig.setSlice(i);
 				intImpOrig.setSlice(i);
 
 				//ALWAYS create duplicates for findParticles(...), otherwise ImagePlus objects get creates that are never closed
 				//	Using Duplicator() to get single slice, whereas ImagePlus.duplicate() duplicates entire stack.
 				duplicatedBF = duplicator.run(bfImp, i, i);
 
 				tempBF = findParticles(duplicatedBF, false, false);
 
 				duplicatedInt = duplicator.run(intImp, i, i);
 				
 				if (inPlugInMode) meanIntensities = duplicatedInt.getStatistics().getHistogram();
 
 				tempInt = findParticles(duplicatedInt, true, false);
 
 				ic.run("AND", tempInt, tempBF);
 
 				//clean up
 				duplicatedBF.close();
 				duplicatedInt.close();
 
 				//once again, assume if this method is not called by ImageJ, then the caller will handle the data
 				if(inPlugInMode){
 					ratio=0; bfAreas=0; intAreas=0;
 
 					//GET total brightfield particles' area
 					tempIP = tempBF.getProcessor();
 					//this step is necessary to reset the processor's threshold for the ThresholdToSelection below
 					tempIP.setThreshold(1, 255, ImageProcessor.BLACK_AND_WHITE_LUT);
 					tempRoi = tts.convert(tempIP);
 					if(tempRoi!=null) {
 						tempIP.setRoi(tempRoi);
 						stats = ImageStatistics.getStatistics(tempIP, Measurements.AREA, tempBF.getCalibration());
 						bfAreas = stats.area;
 					}
 
 					addImageToStack(bfMask, tempIP, bfMaskStack);
 
 					//GET total intensity particles' area
 					tempIP = tempInt.getProcessor();
 					tempIP.setThreshold(1, 255, ImageProcessor.BLACK_AND_WHITE_LUT);
 					tempRoi = tts.convert(tempIP);
 					if(tempRoi!=null) {
 						tempIP.setRoi(tempRoi);
 						stats = ImageStatistics.getStatistics(tempIP, Measurements.AREA, tempBF.getCalibration());
 						intAreas = stats.area;
 					}
 
 					addImageToStack(intMask, tempIP, intMaskStack);
 
 					ratio = bfAreas==0? 0:intAreas/bfAreas;
 					if (ratio!=0){
 						particleDetected = true;
 						if (!prevParticleDetected){
 							numParticlesDetected++;
 							prevParticleDetected=true;
 						}
 						resultsBF.add((float) bfAreas);
 						resultsIN.add((float) intAreas);
 						resultsRATIO.add((float) ratio);
 						
 						float intensityPixelCount=0;
 						float totalIntensity=0;
 						for(int j=(int)thresholdMin;j<meanIntensities.length;j++){
 							intensityPixelCount+=meanIntensities[j];
 							totalIntensity+=(j*meanIntensities[j]);
 						}
 						float avgIntensity = totalIntensity/intensityPixelCount;
 						resultsMeanIN.add(avgIntensity);
 						resultsTotalIN.add((float) (avgIntensity*ratio));
 						
 						twindow.append(i + "\t" + bfAreas + "\t" + intAreas + "\t" + ratio + "\t" + avgIntensity + "\t" + avgIntensity*ratio);
 					} else{
 						particleDetected = false;
 						prevParticleDetected = false; 
 					}
 					Interpreter.batchMode=false;
 					bfMask.show();
 					intMask.show();
 					Interpreter.batchMode=true;
 
 				}
 				if(!doFullStack) i = bfImp.getStackSize()+1;
 			}catch(NullPointerException e){
 				IJ.log("Null value encountered in slice " + i);
 			}catch(Throwable e){
 				IJ.log("Error creating accurate mask on slice " + i);
 			}
 		}
 
 		ImagePlus[] returnImages = new ImagePlus[2];
 		returnImages[0] = tempBF;
 		returnImages[1] = tempInt;
 
 		if(inPlugInMode){
 
 			Collections.sort(resultsBF);
 			Collections.sort(resultsIN);
 			Collections.sort(resultsRATIO);
 			Collections.sort(resultsMeanIN);
 			Collections.sort(resultsTotalIN);
 
 			float sumBF = 0;
 			for (float s : resultsBF) sumBF += s;
 			float sumIN = 0;
 			for (float s : resultsIN) sumIN += s;
 			float sumRatio = 0;
 			for (float s : resultsRATIO) sumRatio += s;
 			float sumMeanIn = 0;
 			for (float s : resultsMeanIN) sumMeanIn += s;
 			float sumTotalIn = 0;
 			for (float s : resultsTotalIN) sumTotalIn += s;
 
 			twindow.append("Avg" + "\t" + sumBF/resultsBF.size() + "\t" + sumIN/resultsIN.size() + "\t" + sumRatio/resultsRATIO.size() + "\t" + sumMeanIn/resultsMeanIN.size() + "\t" + sumTotalIn/resultsTotalIN.size());
 			twindow.append("Min" + "\t" + resultsBF.get(0) + "\t" + resultsIN.get(0) + "\t" + resultsRATIO.get(0) + "\t" + resultsMeanIN.get(0) + "\t" + resultsTotalIN.get(0));
 			twindow.append("Max" + "\t" + resultsBF.get(resultsBF.size()-1) + "\t" + resultsIN.get(resultsIN.size()-1) + "\t" + resultsRATIO.get(resultsRATIO.size()-1) + "\t" + resultsMeanIN.get(resultsMeanIN.size()-1) + "\t" + resultsTotalIN.get(resultsTotalIN.size()-1));
 			twindow.append("Med" + "\t" + resultsBF.get(resultsBF.size()/2) + "\t" + resultsIN.get(resultsIN.size()/2) + "\t" + resultsRATIO.get(resultsRATIO.size()/2) + "\t" + resultsMeanIN.get(resultsMeanIN.size()/2) + "\t" + resultsTotalIN.get(resultsTotalIN.size()/2));
 			twindow.append("Total Particle count: " + numParticlesDetected);
 		}
 		else Interpreter.batchMode=false;
 		intMaskStack=null;
 		bfMaskStack=null;
 		tempInt = null;
 		tempBF = null;
 		return returnImages;
 		} catch(Throwable e){
 			IJ.log("Error with creating ratio masks in plugin");
 			IJ.log(e.getLocalizedMessage().toString());
 		}
 		return null;
 	}
 
 	public ImagePlus findParticles(ImagePlus imageToAnalyze, boolean intensityImage, boolean showResults){
 		try{
 			if(intensityImage){
 				initParticleAnalyzer(true, showResults);
 				imageToAnalyze.getProcessor().setThreshold(thresholdMin, 255, ImageProcessor.BLACK_AND_WHITE_LUT);
 				particleAnalyzer.analyze(imageToAnalyze);
 				return particleAnalyzer.getOutputImage();
 			} else{
 				initParticleAnalyzer(false, showResults);
 				IJ.run(imageToAnalyze, "Find Edges", null);
 				double accuracy = (imageToAnalyze.getProcessor() instanceof ByteProcessor || imageToAnalyze.getProcessor() instanceof ColorProcessor) ?
 						0.002 : 0.0002;
 				gb.blurGaussian(imageToAnalyze.getProcessor(), gaussianSigma, gaussianSigma, accuracy);
 				imageToAnalyze.getProcessor().setAutoThreshold("Minimum", true, ImageProcessor.BLACK_AND_WHITE_LUT);
 				particleAnalyzer.analyze(imageToAnalyze);
 				imageToAnalyze = particleAnalyzer.getOutputImage();
 				//copy and paste below code once more if an underestimation is desired
 				((ByteProcessor)(imageToAnalyze.getProcessor())).erode(1, 0);
 			}
 			return imageToAnalyze;
 		} catch(Throwable e){
 			IJ.log("Error encountered while finding particles");
 			IJ.log(e.getStackTrace().toString());
 			IJ.log(e.getMessage().toString());
 		}
 		return null;
 	}
 
 	private void initParticleAnalyzer(boolean intensityImage, boolean showResults){
 		int options = 0;
 		if (excludeOnEdge) options |= ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES;
 		if (showResults) options |= ParticleAnalyzer.SHOW_RESULTS;
 		options |= ParticleAnalyzer.CLEAR_WORKSHEET;
 		options |= ParticleAnalyzer.SHOW_MASKS;
 
 		if(intensityImage){
 			particleAnalyzer = new ParticleAnalyzer(options, Measurements.AREA, rt, 1, Double.POSITIVE_INFINITY);
 		} else{
 			options |= ParticleAnalyzer.INCLUDE_HOLES|ParticleAnalyzer.CLEAR_WORKSHEET|ParticleAnalyzer.SHOW_MASKS;
 			particleAnalyzer = new ParticleAnalyzer(options, Measurements.AREA, rt, sizeMin, Double.POSITIVE_INFINITY);
 		}		
 	}
 
 	public void addImageToStack(ImagePlus mainImage, ImageProcessor imageToAdd, ImageStack correspondingStack){
 		//very useful to add any single ImagePlus to an existing stack
 		correspondingStack.addSlice("", imageToAdd);
 		mainImage.setStack(correspondingStack);
 		mainImage.setSlice(mainImage.getStackSize());
 		mainImage.unlock();		
 	}
 
 	public void analyzeSingleStack(){
 		twindow = new TextWindow("Found Particles", " \t Slice \t \tPixel Area", "", 800, 300);
 		duplicator = new Duplicator();
 		ImagePlus currentImage, tempDuplicate;
 		ImageProcessor tempIP;
 		ImageStatistics stats;
 		Roi tempRoi;
 		ThresholdToSelection tts = new ThresholdToSelection();
 		double area = 0;
 
 		try{
 			currSlice=imp.getCurrentSlice(); stackSize=imp.getStackSize();
 			if(myMethod.equalsIgnoreCase("intensity")){
 				while(currSlice <= stackSize){
 					imp.setSlice(currSlice);
 					tempDuplicate = duplicator.run(imp, currSlice, currSlice);
 					currentImage = findParticles(tempDuplicate, true, false);
 					tempDuplicate.close();
 					tempIP = currentImage.getProcessor();
 					tempIP.setThreshold(1, 255, ImageProcessor.BLACK_AND_WHITE_LUT);
 					tempRoi = tts.convert(tempIP);
 					if(tempRoi!=null) {
 						tempIP.setRoi(tempRoi);
 						stats = ImageStatistics.getStatistics(tempIP, Measurements.AREA, currentImage.getCalibration());					//originally Measurements.MEAN
 						area = stats.area;
 					}
 
 					if(area!=0){
 						twindow.append("Particle(s) found in slice    \t"+ currSlice+ "\t    with a total pixel area of    \t"+area);
 					}
 					currSlice++;
 				}
 			} else{
 				while(currSlice <= stackSize){
 					imp.setSlice(currSlice);
 					tempDuplicate = duplicator.run(imp, currSlice, currSlice);
 					currentImage = findParticles(tempDuplicate, false, false);
 					tempDuplicate.close();
 					tempIP = currentImage.getProcessor();
 					tempIP.setThreshold(1, 255, ImageProcessor.BLACK_AND_WHITE_LUT);
 					tempRoi = tts.convert(tempIP);
 					if(tempRoi!=null) {
 						tempIP.setRoi(tempRoi);
 						stats = ImageStatistics.getStatistics(tempIP, Measurements.AREA, currentImage.getCalibration());					//originally Measurements.MEAN
 						area = stats.area;
 					}
 
 					if(area!=0){
 						twindow.append("Particle(s) found in slice    \t"+ currSlice+ "\t    with a total pixel area of    \t"+area);
 					}
 					currSlice++;
 				}
 			}
 		}catch(Throwable e){
 			IJ.log("Error encountered while analyzing full stack.");
 			IJ.log(e.getStackTrace().toString());
 		}

 	}
 
 	public float[] analyzeIndividualParticles(){
 		rt = new ResultsTable();
 		duplicator = new Duplicator();
 		try{
 			ImagePlus duplicateImage = duplicator.run(imp, imp.getCurrentSlice(), imp.getCurrentSlice());
 			if(myMethod.equalsIgnoreCase("intensity")) 
 				findParticles(duplicateImage, true, true);
			else findParticles(duplicateImage, false, false);
 			duplicateImage.close();
 			if (rt.getCounter()>0)
 				IJ.log("Counter: " + rt.getCounter());
 			return rt.getColumn(rt.getColumnIndex("Area"));
 		}catch(Throwable e){
 			IJ.log("Error encountered while analyzing individual particles.");
 			IJ.log(e.getStackTrace().toString());
 		}
 		return new float[1];
 	}
 }
