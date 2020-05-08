 import ij.*;
 import ij.gui.*;
 
 import java.awt.image.IndexColorModel;
 import java.io.File;
 import java.io.IOException;
 import ij.plugin.LutLoader;
 import ij.plugin.filter.PlugInFilter;
 import ij.process.*;
 import ij.ImagePlus;
 
 public class Create_NDVI_FromImage implements PlugInFilter {
 	public int setup(String arg, ImagePlus imp) {
 		return DOES_RGB+NO_CHANGES;
 	}
 	
 	public void run(ImageProcessor ip) {
 		String[] outputImageTypes = {"tiff", "jpeg", "gif", "zip", "raw", "avi", "bmp", "fits", "png", "pgm"};
 		String[] ndviBands = {"red", "green", "blue"};	
 		// Get list of LUTs
 		String lutLocation = IJ.getDirectory("luts");
 		File lutDirectory = new File(lutLocation);
 		String[] lutNames = lutDirectory.list();
 		
 		ImagePlus inImagePlus = null;
 		ImagePlus ndviImage = null;
 		String outFileBase = "";
 		int redBand, irBand;
 		Boolean saveParameters = true;
 		Boolean useDefaults = false;
 		
 		// Initialize variables from IJ.Prefs file
 		Boolean displayNDVIColor = Prefs.get("pm.fromSBImage.createNDVIColor", true);
 		Boolean displayNDVIFloat = Prefs.get("pm.fromSBImage.createNDVIFloat", true);
 		Boolean stretchVisible = Prefs.get("pm.fromSBImage.stretchVisible", true);
 		Boolean stretchIR = Prefs.get("pm.fromSBImage.stretchIR", true);
 		double saturatedPixels = Prefs.get("pm.fromSBImage.saturatedPixels", 2.0);
 		double maxColorScale = Prefs.get("pm.fromSBImage.maxColorScale", 1.0);
 		double minColorScale = Prefs.get("pm.fromSBImage.minColorScale", -1.0);
 		String lutName = Prefs.get("pm.fromSBImage.lutName", lutNames[0]);
 		int redBandIndex = (int)Prefs.get("pm.fromSBImage.redBandIndex", 2); 
 		int irBandIndex = (int)Prefs.get("pm.fromSBImage.irBandIndex", 0);
 		saturatedPixels = Prefs.get("pm.fromSBImage.saturatedPixels", 2.0);
 		
 		// Create dialog window
 		GenericDialog dialog = new GenericDialog("Enter variables");
 		dialog.addCheckbox("Load default parameters (click OK below to reload)", false);
 		dialog.addCheckbox("Display Color NDVI image?", displayNDVIColor);
 		dialog.addNumericField("Minimum NDVI value for scaling color NDVI image", minColorScale, 1);
 		dialog.addNumericField("Maximum NDVI value for scaling color NDVI image", maxColorScale, 1);
 		dialog.addCheckbox("Display floating point NDVI image?", displayNDVIFloat);
 		dialog.addCheckbox("Stretch the visible band before creating NDVI?", stretchVisible);
 		dialog.addCheckbox("Stretch the NIR band before creating NDVI?", stretchIR);
 		dialog.addNumericField("Saturation value for stretch", saturatedPixels, 1);
 		dialog.addChoice("Channel for Red band to create NDVI", ndviBands, ndviBands[redBandIndex]);
 		dialog.addChoice("Channel for IR band to create NDVI", ndviBands, ndviBands[irBandIndex]);
 		dialog.addChoice("Select output color table for color NDVI image", lutNames, lutName);
 		dialog.addCheckbox("Save parameters for next session", true);
 		dialog.showDialog();
 		if (dialog.wasCanceled()) {
 			return;
 		}
 		
 		useDefaults = dialog.getNextBoolean();
 		if (useDefaults) {
 			dialog = null;
 			// Create dialog window with default values
 			dialog = new GenericDialog("Enter variables");
 			dialog.addCheckbox("Load default parameters (click OK below to reload)", false);
 			dialog.addCheckbox("Output Color NDVI image?", true);
 			dialog.addNumericField("Enter the minimum NDVI value for scaling color NDVI image", -1.0, 1);
 			dialog.addNumericField("Enter the maximum NDVI value for scaling color NDVI image", 1.0, 1);
 			dialog.addCheckbox("Display floating point NDVI image?", true);
 			dialog.addCheckbox("Stretch the visible band before creating NDVI?", true);
 			dialog.addCheckbox("Stretch the NIR band before creating NDVI?", true);
 			dialog.addNumericField("Enter the saturation value for stretch", 2.0, 1);
 			dialog.addChoice("Channel for Red band to create NDVI", ndviBands, ndviBands[2]);
 			dialog.addChoice("Channel for IR band to create NDVI", ndviBands, ndviBands[0]);
 			dialog.addChoice("Select output color table for color NDVI image", lutNames, lutNames[0]);
 			dialog.addCheckbox("Save parameters for next session", false);
 			dialog.showDialog();
 			if (dialog.wasCanceled()) {
 				return;
 			}
 		}
 		
 		// Get variables from dialog
 		if (useDefaults) { 
 			dialog.getNextBoolean();
 		}
		dialog.getNextBoolean();
 		displayNDVIColor = dialog.getNextBoolean();
 		minColorScale = dialog.getNextNumber();
 		maxColorScale = dialog.getNextNumber();
 		displayNDVIFloat = dialog.getNextBoolean();
 		stretchVisible = dialog.getNextBoolean();
 		stretchIR = dialog.getNextBoolean();
 		saturatedPixels = dialog.getNextNumber();
 		redBand = dialog.getNextChoiceIndex() + 1;
 		irBand = dialog.getNextChoiceIndex() + 1;
		lutName  = dialog.getNextChoice();	
 
 		if (saveParameters) {
 			// Set preferences to IJ.Prefs file
 			Prefs.set("pm.fromSBImage.createNDVIColor", displayNDVIColor);
 			Prefs.set("pm.fromSBImage.createNDVIFloat", displayNDVIFloat);
 			Prefs.set("pm.fromSBImage.stretchVisible", stretchVisible);
 			Prefs.set("pm.fromSBImage.stretchIR", stretchIR);
 			Prefs.set("pm.fromSBImage.saturatedPixels", saturatedPixels);
 			Prefs.set("pm.fromSBImage.maxColorScale", maxColorScale);
 			Prefs.set("pm.fromSBImage.minColorScale", minColorScale);
 			Prefs.set("pm.fromSBImage.lutName", lutName);
 			Prefs.set("pm.fromSBImage.redBandIndex", redBand - 1);
 			Prefs.set("pm.fromSBImage.irBandIndex", irBand - 1);
 			Prefs.set("pm.fromSBImage.saturatedPixels", saturatedPixels);
 		
 			// Save preferences to IJ.Prefs file
 			Prefs.savePreferences();
 		}
 
 		ImagePlus imagePlus = new ImagePlus("image", ip);
 		RegImagePair imagePair = new RegImagePair(imagePlus, imagePlus);
 		ndviImage = imagePair.calcNDVI(irBand, redBand, stretchVisible, stretchIR, saturatedPixels);
 		if (displayNDVIFloat) {
 			ndviImage.show();
 		}
 		
 		if (displayNDVIColor) {
 			IndexColorModel cm = null;
 			LUT lut;
 			//Uncomment next line to use default float-to-byte conversion
 			//ImageProcessor colorNDVI = ndviImage.getProcessor().convertToByte(true);
 			ImagePlus colorNDVI;
 			colorNDVI = NewImage.createByteImage("Color NDVI", ndviImage.getWidth(), ndviImage.getHeight(), 1, NewImage.FILL_BLACK);
 			
 			float[] pixels = (float[])ndviImage.getProcessor().getPixels();
 			for (int y=0; y<ndviImage.getHeight(); y++) {
 	            int offset = y*ndviImage.getWidth();
 				for (int x=0; x<ndviImage.getWidth(); x++) {
 					int pos = offset+x;
 					colorNDVI.getProcessor().putPixelValue(x, y, Math.round((pixels[pos] - minColorScale)/((maxColorScale - minColorScale) / 255.0)));
 				}	    						    				
 			}
 			// Get the LUT
 			try {
 			cm = LutLoader.open(lutLocation+lutName);
 			} catch (IOException e) {
 			IJ.error(""+e);
 			}
 		
 			lut = new LUT(cm, 255.0, 0.0);
 			colorNDVI.getProcessor().setLut(lut);
 			colorNDVI.show();
 		}
 	}
 }
