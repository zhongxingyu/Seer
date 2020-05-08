 import ij.*;
 import ij.io.*;
 import ij.gui.*;
 import ij.plugin.*;
 import ij.process.*;
 import ij.gui.DialogListener;
 import java.io.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.Choice;
 import java.awt.image.*;
 import java.awt.AWTEvent;
 
 public class Register_Images implements PlugIn, DialogListener {
 	static final int ADD=1, SUBTRACT=2, MULTIPLY=3, DIVIDE=4;
 	public void run(String arg) {
 		ImagePlus rawSourceImage = null;
 		ImagePlus rawTargetImage = null;
 		ImagePlus regSource = null;
 		RegImagePair regImages = null;
 		ImagePlus ndviImage = null;
 		String primaryRegMethod;
 		String secondaryRegMethod;
 		String transformation;
 		String preprocessingMethod;
 		Boolean useSecondaryMethod;
 		int numSiftTries = 1;
 		Boolean createNGR;
 		Boolean createNDVIColor;
 		Boolean createNDVIFloat;
 		Boolean outputClipTwo;
 		String fileType = "";
 		String lutName = "";
 		String logName = "log.txt";
 		String outFileBase = "";
 		boolean continueProcessing = true;
 		int redBand, irBand;
 		//RoiPair rois = null;
 		RoiPair lastGoodRois = null;
 		
 		// Dialog variables
 		String[] primaryRegMethodTypes = {"SIFT/Landmark correspondences", "SIFT/Landmark correspondences using reference points from first valid image pair", "bUnwarpJ"};
 		String[] secondaryRegMethodTypes = {"SIFT/Landmark correspondences", "SIFT/Landmark correspondences using last valid set of points", "bUnwarpJ"};
 		String[] transformationTypes = {"Rigid", "Affine"};
 		String[] preprocessingType = {"nir (g+b) vis (g-b)", "none"}; 
 		String[] ndviBands = {"red", "green", "blue"};
 		String[] outputImageTypes = {"tiff", "jpeg", "gif", "zip", "raw", "avi", "bmp", "fits", "png", "pgm"};
 		// Get list of LUTs
 		String lutLocation = IJ.getDirectory("luts");
 		File lutDirectory = new File(lutLocation);
 		String[] lutNames = lutDirectory.list();
 		
 		// Create dialog window
 		GenericDialog dialog = new GenericDialog("Enter variables");
 		dialog.addMessage("Image-to-image registration options:");
 		dialog.addCheckbox("Use backup registration method if primary fails?", true);
 		dialog.addChoice("Select primary registration method", primaryRegMethodTypes, primaryRegMethodTypes[0]);
 		dialog.addChoice("Select secondary registration method", secondaryRegMethodTypes, secondaryRegMethodTypes[1]);
 		dialog.addChoice("Select transformation type if using SIFT", transformationTypes, transformationTypes[0]);
 		dialog.addNumericField("Number of tries for SIFT to find correspondence points", 3, 0);
 		dialog.addChoice("Method to improve SIFT point selection", preprocessingType, preprocessingType[0]);
 		dialog.addMessage("Output image options:");
 		dialog.addChoice("Output image type", outputImageTypes, outputImageTypes[0]);
 		dialog.addCheckbox("Create NGR image?", true);
 		dialog.addCheckbox("Output clipped visible image?", true);
 		dialog.addCheckbox("Create Color NDVI image?", true);
 		dialog.addCheckbox("Create floating point NDVI image?", true);	
		dialog.addChoice("Channel from visible image to use for Red band to create NDVI", ndviBands, ndviBands[0]);
 		dialog.addChoice("Channel from IR image to use for IR band to create NDVI", ndviBands, ndviBands[2]);
 		dialog.addChoice("Select output color table for color NDVI image", lutNames, lutNames[0]);
 		dialog.addDialogListener(this);
 		dialog.showDialog();
 		if (dialog.wasCanceled()) {
 			return;
 		}
 		
 		// Get variables from dialog
 		useSecondaryMethod = dialog.getNextBoolean();
 		primaryRegMethod = dialog.getNextChoice();
 		secondaryRegMethod = dialog.getNextChoice();
 		transformation = dialog.getNextChoice();
 		numSiftTries = (int)dialog.getNextNumber();
 		preprocessingMethod = dialog.getNextChoice();
 		fileType = dialog.getNextChoice();
 		createNGR = dialog.getNextBoolean();
 		outputClipTwo = dialog.getNextBoolean();
 		createNDVIColor = dialog.getNextBoolean();
 		createNDVIFloat = dialog.getNextBoolean();
 		redBand = dialog.getNextChoiceIndex() + 1;
 		irBand = dialog.getNextChoiceIndex() + 1;
 		lutName  = dialog.getNextChoice();
 		
 		
 		// Dialog for photo pair list file
 		OpenDialog od = new OpenDialog("Photo pair file", arg);
 	    String pairDirectory = od.getDirectory();
 	    String pairFileName = od.getFileName();
 	    if (pairFileName==null) {
 	    	IJ.error("No file was selected");
 	    	return;
 	    }
 		
 		// Dialog for output photos directory and log file name
 		SaveDialog sd = new SaveDialog("Output directory", "log", ".txt");
 	    String outDirectory = sd.getDirectory();
 	    logName = sd.getFileName();
 	    if (logName==null){
 	    	IJ.error("No directory was selected");
 	    	return;
 	    }
 	    
 	    // Get photoPairs
 	    FilePairList photoPairs = new FilePairList(pairDirectory, pairFileName);
 	    
 	    // Start processing one image pair at a time
 	    try {
 	    	BufferedWriter bufWriter = new BufferedWriter(new FileWriter(outDirectory+logName));
 	    
 	    	for (FilePair filePair : photoPairs) {
 	    		// Open image pairs
 	    		continueProcessing = false;
 	    	
 	    		rawSourceImage = new ImagePlus(filePair.getFirst().trim());
 	    		rawTargetImage = new ImagePlus(filePair.getSecond().trim());
 	    		outFileBase = rawTargetImage.getTitle().replaceFirst("[.][^.]+$", "");
 	    	
 	    		// Make sure images are RGB
 	    		if (rawSourceImage.getType() != ImagePlus.COLOR_RGB | rawTargetImage.getType() != ImagePlus.COLOR_RGB) {
 	    			IJ.error("Images must be Color RGB");
 	    			return;  
 	    		}
 	    	
 	    		rawSourceImage.show();
 	    		rawTargetImage.show();
 	
 	    		if (primaryRegMethod == primaryRegMethodTypes[0] ||  (primaryRegMethod == primaryRegMethodTypes[1]) && lastGoodRois == null) {
 	    			RoiPair rois = doSift(rawSourceImage, rawTargetImage, numSiftTries, transformation, preprocessingMethod);
 	    			if (rois != null) {
 	    				lastGoodRois = new RoiPair(rois.getSourceRoi(), rois.getTargetRoi());
 	    				rawSourceImage.setRoi(rois.getSourceRoi(), false);
 	    				rawTargetImage.setRoi(rois.getTargetRoi(), false);
 	    				regSource = doLandmark(rawSourceImage, rawTargetImage, transformation);
 	    				bufWriter.write("Images "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+" " +
 	    					"registered using SIFT and landmark correspondences with "+transformation+" transformation\n");
 	    				continueProcessing = true;
 	    			} 
 	    			else {
 	    				continueProcessing = false;
 	    			}
 	    		}
 	    		else if (primaryRegMethod == primaryRegMethodTypes[1] && lastGoodRois != null) {
 		    		rawSourceImage.setRoi(lastGoodRois.getSourceRoi(), false);
 	    			rawTargetImage.setRoi(lastGoodRois.getTargetRoi(), false);
 	    			regSource = doLandmark(rawSourceImage, rawTargetImage, transformation);
 	    			if (regSource != null) {
 	    				bufWriter.write("Images "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+" " +
 	    						"registered using previous SIFT points and landmark correspondences with "+transformation+" transformation\n");
 	    				continueProcessing = true;
 	    			}
 	    			else {
 	    				continueProcessing = false;
 	    			}
 	    		} else if (primaryRegMethod == primaryRegMethodTypes[2]) {
 	    			regSource = dobUnwarpJ(rawSourceImage, rawTargetImage, transformation);
 	    			if (regSource != null) {
 	    				bufWriter.write("Images "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+
     							" registered using bUnwarpJ\n");
 	    				continueProcessing = true;
 	    			}
 	    			else {
 	    				continueProcessing = false;
 	    			}
 	    		}
 	    		
 	    		if (!continueProcessing && useSecondaryMethod) {
 		    		if (secondaryRegMethod == secondaryRegMethodTypes[0] ||  (secondaryRegMethod == secondaryRegMethodTypes[1]) && lastGoodRois == null) {
 		    			RoiPair rois = doSift(rawSourceImage, rawTargetImage, numSiftTries, transformation, preprocessingMethod);
 		    			if (rois != null) {
 		    				lastGoodRois = new RoiPair(rois.getSourceRoi(), rois.getTargetRoi());
 		    				rawSourceImage.setRoi(rois.getSourceRoi(), false);
 		    				rawTargetImage.setRoi(rois.getTargetRoi(), false);
 		    				regSource = doLandmark(rawSourceImage, rawTargetImage, transformation);
 		    				bufWriter.write("Images "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+" " +
 		    					"registered using SIFT and landmark correspondences with "+transformation+" transformation\n");
 		    				continueProcessing = true;
 		    			} 
 		    			else {
 		    				continueProcessing = false;
 		    			}
 		    		}
 		    		else if (secondaryRegMethod == secondaryRegMethodTypes[1] && lastGoodRois != null) {
 			    		rawSourceImage.setRoi(lastGoodRois.getSourceRoi(), false);
 		    			rawTargetImage.setRoi(lastGoodRois.getTargetRoi(), false);
 		    			regSource = doLandmark(rawSourceImage, rawTargetImage, transformation);
 		    			if (regSource != null) {
 		    				bufWriter.write("Images "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+" " +
 		    						"registered using previous SIFT points and landmark correspondences with "+transformation+" transformation\n");
 		    				continueProcessing = true;
 		    			}
 		    			else {
 		    				continueProcessing = false;
 		    			}
 		    		} else if (secondaryRegMethod == secondaryRegMethodTypes[2]) {
 		    			regSource = dobUnwarpJ(rawSourceImage, rawTargetImage, transformation);
 		    			if (regSource != null) {
 		    				bufWriter.write("Images "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+
 	    							" registered using bUnwarpJ\n");
 		    				continueProcessing = true;
 		    			}
 		    			else {
 		    				continueProcessing = false;
 		    			}
 		    		}
 		    	}
 	    		bufWriter.flush();
 	    
 	    		if (continueProcessing) {
 	    			regImages = clipPair(regSource, rawTargetImage);
 	    			if (createNDVIFloat | createNDVIColor) {
 	    			ndviImage = regImages.calcNDVI(redBand, irBand);
 	    			}
 	    			
 	    			if (createNDVIFloat) {
 	    				IJ.save(ndviImage, outDirectory+outFileBase+"_NDVI_Float."+fileType);
 	    			}
 	    			if (createNDVIColor) {
 	    				IndexColorModel cm = null;
 	    				LUT lut; 
 	    				ImageProcessor colorNDVI = ndviImage.getProcessor().convertToByte(true);
 	    				// Get the LUT
 	    				try {
 	    				cm = LutLoader.open(lutLocation+lutName);
 	    				} catch (IOException e) {
 	    				IJ.error(""+e);
 	    				}
 	    			
 	    				lut = new LUT(cm, 0.0, 255.0);
 	    				colorNDVI.setLut(lut);
 	    				ImagePlus colorNDVI_Image = new ImagePlus("Color NDVI", colorNDVI);
 	    				IJ.save(colorNDVI_Image, outDirectory+outFileBase+"_NDVI_Color."+fileType);
 	    			}
 	    	
 	    			if (outputClipTwo) {
 	    				IJ.save(regImages.getSecond(), outDirectory+outFileBase+"_Clipped."+fileType);
 	    			}
 	    	
 	    			if (createNGR) {
 	    				ColorProcessor firstCP = (ColorProcessor)regImages.getFirst().getProcessor();
 	    				ColorProcessor secondCP = (ColorProcessor)regImages.getSecond().getProcessor();
 	    				ColorProcessor colorNGR = new ColorProcessor(regImages.getFirst().getWidth(), regImages.getSecond().getHeight());
 	    				colorNGR.setChannel(1, firstCP.getChannel(1, null));
 	    				colorNGR.setChannel(2, secondCP.getChannel(1, null));
 	    				colorNGR.setChannel(3, secondCP.getChannel(2, null));
 	    				ImagePlus ngrImage = new ImagePlus("NGR Image", colorNGR);
 	    				IJ.save(ngrImage, outDirectory+outFileBase+"_NGR."+fileType);
 	    			}
 	    	
 	    		}
 	    		IJ.run("Close All");
 	    	}
 	    	bufWriter.close();
 	    } catch (Exception e) {
 	    	IJ.error("Error writing log file", e.getMessage());
 	    	return;
 	    }
 	}
 	
 	// Method to update dialog based on user selections
 	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
 		Checkbox ndviColorCheckbox = (Checkbox)gd.getCheckboxes().get(3);
 		Checkbox ndviFloatCheckbox = (Checkbox)gd.getCheckboxes().get(4);
 		Vector<?> choices = gd.getChoices();
 		if (ndviColorCheckbox.getState() | ndviFloatCheckbox.getState()) {
 			((Choice)choices.get(5)).setEnabled(true);
 			((Choice)choices.get(6)).setEnabled(true);
 			((Choice)choices.get(7)).setEnabled(true);
 		} 
 		else {
 			((Choice)choices.get(5)).setEnabled(false);
 			((Choice)choices.get(6)).setEnabled(false);
 			((Choice)choices.get(7)).setEnabled(false);
 		}
 		Checkbox useSecondaryMethodsCheckbox = (Checkbox)gd.getCheckboxes().get(0);
 		if (useSecondaryMethodsCheckbox.getState()) {
 			((Choice)choices.get(1)).setEnabled(true);
 		}
 		else {
 			((Choice)choices.get(1)).setEnabled(false);
 		}
 		
 		int primaryChoice = gd.getNextChoiceIndex();
 		int secondaryChoice = gd.getNextChoiceIndex();
 		Vector<?> numTriesVector = gd.getNumericFields();
 		if (primaryChoice==0 || primaryChoice==1 || (secondaryChoice==0 && useSecondaryMethodsCheckbox.getState()) || 
 				(secondaryChoice == 1 && useSecondaryMethodsCheckbox.getState())) {
 			((Choice)choices.get(2)).setEnabled(true);
 			((Choice)choices.get(3)).setEnabled(true);
 			((TextField)numTriesVector.get(0)).setEnabled(true);
 		}
 		else {
 			((Choice)choices.get(2)).setEnabled(false);
 			((Choice)choices.get(3)).setEnabled(false);
 			((TextField)numTriesVector.get(0)).setEnabled(false);
 		}
 		
 		if (ndviColorCheckbox.getState()) {
 			((Choice)choices.get(7)).setEnabled(true);
 		} 
 		else {
 			((Choice)choices.get(7)).setEnabled(false);
 		}
 			
 		return true;
 	}
 	
 	// Do image-to-image registration using SIFT/Landmark correspondences
 	public RoiPair doSift(ImagePlus rawSourceImage, ImagePlus rawTargetImage, int numSiftTries, String transformation, String preprocessingMethod) {
 		IJ.log("Geting SIFT correspondence points from "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle());
 		ImagePlus processSourceImage = null;
 		ImagePlus processTargetImage = null;
 		int trys = 1;
 		boolean noPoints = true;
 		
 		// Do pre-processing if requested
 		if (preprocessingMethod == "nir (g+b) vis (g-b)") {
 			processSourceImage = channelMath((ColorProcessor)rawSourceImage.getProcessor(), 2, 3, ADD);
 			processTargetImage = channelMath((ColorProcessor)rawTargetImage.getProcessor(), 2, 3, SUBTRACT);				
 		} 
 		else if (preprocessingMethod == "none") {
 			processSourceImage = new ImagePlus("Source", rawSourceImage.getProcessor());
 			processTargetImage = new ImagePlus("Target", rawTargetImage.getProcessor());
 		}
 		processSourceImage.show();
 		processTargetImage.show();
 
 		processSourceImage.setTitle("sourceProcessed");
 		processTargetImage.setTitle("targetProcessed");
 
 		// Try 5 times to get a set of match points for the transformation
 		while (trys <= numSiftTries && noPoints) {
 			IJ.log("Number of times trying SIFT: "+trys);
 			// Get match points using SIFT
 			IJ.run("Extract SIFT Correspondences", "source_image="+processTargetImage.getTitle()+" target_image="+
 					processSourceImage.getTitle()+" initial_gaussian_blur=1.60    " +
 							"steps_per_scale_octave=3 minimum_image_size=64 maximum_image_size=1024 " +
 							"feature_descriptor_size=4 feature_descriptor_orientation_bins=8 closest/   " +
 							"next_closest_ratio=0.92 filter maximal_alignment_error=25 " +
 							"minimal_inlier_ratio=0.05 minimal_number_of_inliers=7 expected_transformation="+
 							transformation);
 			if (processSourceImage.getRoi() != null) {
 				if (processTargetImage.getRoi().getType() == Roi.POINT) {
 					noPoints = false;
 				}
 			}
 			trys = trys + 1;
 		}
 		if (!noPoints) {
 			
 			RoiPair rois = new RoiPair(processSourceImage.getRoi(), processTargetImage.getRoi());
 			processSourceImage.close();
 			processTargetImage.close();
 			return rois;
 		}
 		else {
 			return null;
 		}
 	}
 	
 	// Method to run Landmark correspondence
 	public ImagePlus doLandmark(ImagePlus rawSourceImage, ImagePlus rawTargetImage, String transformation) {
 		IJ.log("Registering "+rawTargetImage.getTitle()+" and "+rawSourceImage.getTitle()+ " using Landmark correspondences");
 		IJ.run("Landmark Correspondences", "source_image="+rawSourceImage.getTitle()+" template_image="+
 				rawTargetImage.getTitle()+" transformation_method=[Moving Least Squares (non-linear)] " +
 				"alpha=1 mesh_resolution=32 transformation_class="+transformation+
 				" interpolate");
 			ImagePlus regSource = WindowManager.getImage("Transformed"+rawSourceImage.getTitle());
 			return regSource;
 	}
 	
 	// Method to run bUnwarpJ
 	public ImagePlus dobUnwarpJ(ImagePlus rawSourceImage, ImagePlus rawTargetImage, String transformation) {
 		IJ.log("Processing "+rawSourceImage+" and "+rawTargetImage+" using bUnwarpJ");
 		IJ.run("bUnwarpJ", "source_image="+rawSourceImage.getTitle()+" target_image="+rawTargetImage.getTitle()+
 				" registration=Fast image_subsample_factor=0 initial_deformation=[Very Coarse] " +
 				"inal_deformation=Fine divergence_weight=0 curl_weight=0 landmark_weight=0 image_weight=1 " +
 				"consistency_weight=10 stop_threshold=0.01");
 		ImagePlus regSource = new ImagePlus("newSourceImage", WindowManager.getImage("Registered Source Image").getStack().getProcessor(1));
 		return regSource;
 	}
 	
 	
  
 	// Method to perform channel math
 	public ImagePlus channelMath (ColorProcessor image, int chanA, int chanB, int operator) {
 		int width = image.getWidth();
 		int height = image.getHeight();
 		ImagePlus newImage;
 		newImage = NewImage.createFloatImage("resultImage", width, height, 1, NewImage.FILL_BLACK);
 		byte[] array1 = image.getChannel(chanA);
 		byte[] array2 = image.getChannel(chanB);
 		double outPixel = 0.0;
 
 		for (int y=0; y<height; y++) {
             int offset = y*width;
 			for (int x=0; x<width; x++) {
 				int pos = offset+x;
 				double pixel1 = array1[pos] & 0xff;
 				double pixel2 = array2[pos] & 0xff;
                 switch (operator) {
                 	case SUBTRACT: outPixel = pixel1 - pixel2; break;
                 	case ADD: outPixel = pixel1 + pixel2; break;
                 	case MULTIPLY: outPixel = pixel1 * pixel2; break;
                 	case DIVIDE: outPixel = pixel2!=0.0?pixel1/pixel2:0.0; break;
                 }
                 	newImage.getProcessor().putPixelValue(x, y, outPixel);
             }
 		}
 		return newImage;
 	}
 	
 	//Method to clip image pair
 	public RegImagePair clipPair (ImagePlus clipSourceImage, ImagePlus otherImage) {
 		if ((clipSourceImage.getHeight() != otherImage.getHeight()) | (clipSourceImage.getWidth() != otherImage.getWidth())) {
 			return null;
 		}
 		ImageProcessor byteImage = clipSourceImage.getProcessor().convertToByte(true);
 		ImagePlus croppedSource = null;
 		ImagePlus croppedOther = null;
 		int height = clipSourceImage.getHeight();
 		int width = clipSourceImage.getWidth();
 		int xmin = 0;
 		int ymin = 0;
 		int xmax = width - 1;
 		int ymax = height - 1;
 		boolean topHasNoData = true;
 		boolean rightHasNoData = true;
 		boolean bottomHasNoData = true;
 		boolean leftHasNoData = true;
 	    int sidesOK = 0;
 		
 	    while (sidesOK < 4) {
 	    	double proportionNoData = 0.0;
 	    	String moveSide = "";
 	    	int numNoData = 0;
 	    	
 	    	// For each side count the number of no-data pixel in the line or column then calculate percent no-data
 	         if (topHasNoData) {
 	            numNoData = 0;
 	            for (int x=xmin; x<=xmax; x++) {
 	               if (byteImage.getPixel(x, ymin) == 0) {
 	                   numNoData++;
 	               }
 	            }
 	            if ((numNoData/(double)(xmax+1.0)) > proportionNoData) {
 	               proportionNoData = numNoData/(double)(xmax+1.0);
 	               moveSide = "top";
 	            } else if (numNoData == 0) {
 	              topHasNoData = false;
 	              sidesOK = sidesOK + 1;
 	            }
 	         }
 	         if (rightHasNoData) {
 	            numNoData = 0;
 	           for (int y=ymin; y<=ymax; y++) {
 	               if (byteImage.getPixel(xmax, y) == 0) {
 	                   numNoData++;
 	               }
 	            }
 	            if ((numNoData/(double)(ymax+1.0)) > proportionNoData) {
 	               proportionNoData = numNoData/(double)(ymax+1.0);
 	               moveSide = "right";
 	            } else if (numNoData == 0) {
 	              rightHasNoData = false;
 	              sidesOK = sidesOK + 1;
 	            }
 	         }
 	         if (bottomHasNoData) {
 	            numNoData = 0;
 	            for (int x=xmin; x<=xmax; x++) {
 	               if (byteImage.getPixel(x, ymax) == 0) {
 	                   numNoData++;
 	               }
 	            }
 	            if ((numNoData/(double)(xmax+1.0)) > proportionNoData) {
 	               proportionNoData = numNoData/(double)(xmax+1.0);
 	               moveSide = "bottom";
 	            } else if (numNoData == 0) {
 	              bottomHasNoData = false;
 	              sidesOK = sidesOK + 1;
 	            }
 	         }
 	         if (leftHasNoData) {
 	            numNoData = 0;
 	            for (int y=ymin; y<=ymax; y++) {
 	               if (byteImage.getPixel(xmin, y) == 0) {
 	                   numNoData++;
 	               }
 	            }
 	            if ((numNoData/(double)(ymax+1.0)) > proportionNoData) {
 	               proportionNoData = numNoData/(double)(ymax+1.0);
 	               moveSide = "left";
 	            } else if (numNoData == 0) {
 	              rightHasNoData = false;
 	              sidesOK = sidesOK + 1;
 	            }
 	         }
 	         // Move the side that has the highest proportion of no-data pixels
 	         if (moveSide == "top") {
 	            ymin = ymin + 1;
 	         }
 	         if (moveSide == "right") {
 	            xmax = xmax - 1;
 	         }
 	         if (moveSide == "bottom") {
 	            ymax = ymax - 1;
 	         }
 	         if (moveSide == "left") {
 	            xmin = xmin + 1;
 	         }
 	    }
 	    // Calculate selection rectangle
 	    clipSourceImage.setRoi(xmin,ymin, xmax - xmin + 1, ymax - ymin + 1);
 	    otherImage.setRoi(xmin,ymin, xmax - xmin + 1, ymax - ymin + 1);
 	    clipSourceImage.show();
 	    otherImage.show();
 	    croppedSource = new ImagePlus("croppedFirst", clipSourceImage.getProcessor().crop());
 	    croppedOther = new ImagePlus("croppedSecond", otherImage.getProcessor().crop());
 	    RegImagePair outPair = new RegImagePair(croppedSource, croppedOther);
 	    return outPair;
 	}
 }
 
 
