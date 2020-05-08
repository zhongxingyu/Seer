 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2011 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.analysistoolsinterface;
 
 import edu.stanford.cfuller.imageanalysistools.frontend.AnalysisController;
 import edu.stanford.cfuller.imageanalysistools.frontend.DataSummary;
 import edu.stanford.cfuller.imageanalysistools.image.Image;
 import edu.stanford.cfuller.imageanalysistools.image.WritableImage;
 import edu.stanford.cfuller.imageanalysistools.image.ImageFactory;
 import edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate;
 import edu.stanford.cfuller.imageanalysistools.image.ImageSet;
 import edu.stanford.cfuller.imageanalysistools.image.io.ImageReader;
 import edu.stanford.cfuller.imageanalysistools.metric.Measurement;
 import edu.stanford.cfuller.imageanalysistools.metric.Quantification;
 import edu.stanford.cfuller.imageanalysistools.meta.parameters.ParameterDictionary;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadata;
 import edu.stanford.cfuller.imageanalysistools.meta.AnalysisMetadataParserFactory;
 import ij.ImagePlus;
 import ij.gui.ImageWindow;
 import ij.gui.Roi;
 import ij.process.ImageConverter;
 import ij.process.ImageProcessor;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.PrintWriter;
 import java.util.HashSet;
 import java.util.prefs.Preferences;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 
 /**
  *
  * @author cfuller
  */
 public class DeschmutzerizerController extends TaskController {
 
 
 	public static final String PARAMETERS_DIR = "parameters";
 	public static final String OUTPUT_DIR = "deschmutzed";
 
 	DeschmutzerizerWindow dw;
 	DeschmutzerizerInputEventHandler dieh;
 
 	ImageWindow originalImageWindow;
 	ImageWindow maskWindow;
 
 	Image currentOriginalImage;
 	Image currentMaskImage;
 	Image currentLabeledMaskImage;
 
 	HashSet<Integer> selectedRegions;
 
 	ImagePlus colorCodedMaskDisplay;
 	ImagePlus originalImageDisplay;
 
 	java.util.List<String> filenamesToProcess;
 
 	String currentMaskFilename;
 	String currentDataFilename;
 	String currentLabeledMaskFilename;
 	String currentParametersFilename;
 	String lastParametersProcessed;
 	String lastDataFilenameProcessed;
 
 	ParameterDictionary currentParameters;
 
 	public void shouldDrawMouseDragBox(boolean shouldDraw) {
 
 		//        if (shouldDraw) {
 			//            this.originalImageDisplay.drawSelectionBox(this.dieh.getMouseDownPoint(), this.dieh.getMouseDragEndPoint());
 		//            this.maskImageDisplay.drawSelectionBox(this.dieh.getMouseDownPoint(), this.dieh.getMouseDragEndPoint());
 		//
 		//        } else {
 		//            this.originalImageDisplay.clearSelectionBox();
 		//            this.maskImageDisplay.clearSelectionBox();
 		//
 		//        }
 
 	}
 
 	public void processBox(Point start, Point end) {
 
 		Image regionNumberCheck = null;
 
 		if (this.dw.groupSelectionSelected()) {
 			regionNumberCheck = this.currentMaskImage;
 		} else {
 			regionNumberCheck = this.currentLabeledMaskImage;
 		}
 
 		int xLower = (int) (start.getX() < end.getX() ? start.getX() : end.getX());
 		int yLower = (int) (start.getY() < end.getY() ? start.getY() : end.getY());
 
 		int width = (int) Math.abs(start.getX() - end.getX());
 		int height = (int) Math.abs(start.getY() - end.getY());
 
 		if (xLower < 0) {
 			width += xLower;
 			if (width < 0) width = 0;
 			xLower = 0;
 		}
 
 		if (yLower < 0) {
 			height += yLower;
 			if (height < 0) height = 0;
 			yLower = 0;
 		}
 
 		if (xLower + width > regionNumberCheck.getDimensionSizes().get(ImageCoordinate.X)) {
 			width = regionNumberCheck.getDimensionSizes().get(ImageCoordinate.X) - xLower;
 		}
 
 		if (yLower + height > regionNumberCheck.getDimensionSizes().get(ImageCoordinate.Y)) {
 			height = regionNumberCheck.getDimensionSizes().get(ImageCoordinate.Y) - yLower;
 		}
 
 
 		int[] posCZT = this.colorCodedMaskDisplay.convertIndexToPosition(this.colorCodedMaskDisplay.getSlice());
 
 		ImageCoordinate startCoord = ImageCoordinate.createCoordXYZCT(xLower, yLower, posCZT[1]-1,posCZT[0]-1,posCZT[2]-1);
 		ImageCoordinate endCoord = ImageCoordinate.createCoordXYZCT(xLower + width+1, yLower + height +1, posCZT[1],posCZT[0],posCZT[2]);
 
 		regionNumberCheck.setBoxOfInterest(startCoord, endCoord);
 
 		java.util.HashSet<Integer> tempSelectedRegions = new java.util.HashSet<Integer>();
 
 		for (ImageCoordinate ic : regionNumberCheck) {
 			if (regionNumberCheck.getValue(ic) > 0) {
 				tempSelectedRegions.add((int) regionNumberCheck.getValue(ic));
 			}
 		}
 
 
 		startCoord.recycle();
 		endCoord.recycle();
 
 		regionNumberCheck.clearBoxOfInterest();
 
 		int x0 = regionNumberCheck.getDimensionSizes().get(ImageCoordinate.X);
 		int xf = 0;
 		int y0 = regionNumberCheck.getDimensionSizes().get(ImageCoordinate.Y);
 		int yf = 0;
 
 		java.util.HashSet<Integer> nextSelectedRegions = new java.util.HashSet<Integer>();
 
 		nextSelectedRegions.addAll(this.selectedRegions);
 
 		final int[] red = {255,0,0};
 		final int[] white = {255,255,255};
 		
 		int currentSlice = this.colorCodedMaskDisplay.getSlice();
 
 		for (ImageCoordinate ic : regionNumberCheck) {
 
 			int x = ic.get(ImageCoordinate.X);
 			int y = ic.get(ImageCoordinate.Y);
 			int z = ic.get(ImageCoordinate.Z);
 			int c = ic.get(ImageCoordinate.C);
 			int t = ic.get(ImageCoordinate.T);
 
 			if (tempSelectedRegions.contains((int) regionNumberCheck.getValue(ic))  && ! this.selectedRegions.contains((int) this.currentLabeledMaskImage.getValue(ic))) {
 
 				this.colorCodedMaskDisplay.setPositionWithoutUpdate(c+1,z+1,t+1);
 
 				ImageProcessor ip = this.colorCodedMaskDisplay.getProcessor();
 
 				ip.putPixel(x,y,red);
 
 				nextSelectedRegions.add((int) this.currentLabeledMaskImage.getValue(ic));
 
 				if (x < x0) x0 = x;
 				if (x >= xf) xf = x+1;
 				if (y < y0) y0 = y;
 				if (y >= yf) yf = y+1;
 
 
 			} else if (tempSelectedRegions.contains((int) regionNumberCheck.getValue(ic))  && this.selectedRegions.contains((int) this.currentLabeledMaskImage.getValue(ic))) {
 
 				this.colorCodedMaskDisplay.setPositionWithoutUpdate(c+1,z+1,t+1);
 
 				ImageProcessor ip = this.colorCodedMaskDisplay.getProcessor();
 
 				ip.putPixel(x,y,white);
 
 				nextSelectedRegions.remove((int) this.currentLabeledMaskImage.getValue(ic));
 
 				if (x < x0) x0 = x;
 				if (x >= xf) xf = x+1;
 				if (y < y0) y0 = y;
 				if (y >= yf) yf = y+1;
 
 			}
 			
 
 		}
 		
 		this.colorCodedMaskDisplay.setSlice(currentSlice);
 		
 		this.colorCodedMaskDisplay.updateAndRepaintWindow();
 
 
 		this.selectedRegions = nextSelectedRegions;
 
 
 	}
 
 	public void processSelectedBox() {
 		
 		
 		Roi currentRoi = this.colorCodedMaskDisplay.getRoi();
 		
 			
 		if (currentRoi == null) {
 		
 			currentRoi = this.originalImageDisplay.getRoi();
 			
 		}
 				
 		if (currentRoi == null) return;
 		
 		Point secondCorner = new Point(currentRoi.getBounds().getLocation());
 		secondCorner.translate((int) currentRoi.getBounds().getWidth(), (int) currentRoi.getBounds().getHeight());
 
 		
 		this.processBox(currentRoi.getBounds().getLocation(), secondCorner);
 		//this.processBox(this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseDownPoint()), this.maskImageDisplay.translateDisplayPointToImagePoint(this.dieh.getMouseUpPoint()));
 
 		this.colorCodedMaskDisplay.killRoi();
 		this.originalImageDisplay.killRoi();
 
 	}
 
 	public void processSelectedPoint() {
 		this.processBox(this.colorCodedMaskDisplay.getCanvas().getCursorLoc(), this.colorCodedMaskDisplay.getCanvas().getCursorLoc());
 	}
 
 	@Override
 	public void startTask() {
 
 		this.currentParameters = null;
 		this.lastDataFilenameProcessed = null;
 		this.lastParametersProcessed = null;
 
 		this.selectedRegions = new java.util.HashSet<Integer>();
 
 		this.dw = new DeschmutzerizerWindow(this);
 
 		this.dw.addWindowListener(this);
 
 		this.dieh = new DeschmutzerizerInputEventHandler(this);
 
 		this.dw.setImageFilename(Preferences.userNodeForPackage(this.getClass()).get("deschmutzerizer_filename", this.dw.getImageFilename()));
 
 		this.dw.setVisible(true);
 
 		this.originalImageWindow = null;
 		this.maskWindow = null;
 
 
 	}
 
 	private void finish(){
 
 		this.dw.setDone(true);
 		this.dw.setStartButtonEnabled(true);
 		if (this.lastParametersProcessed != null) {
 			try {
 				DataSummary.SummarizeData((new File(this.getOutputFilename(this.lastDataFilenameProcessed))).getParent(), (new File(this.lastParametersProcessed)).getParent());
 			} catch (java.io.IOException e ) {
 				LoggingUtilities.severe("encountered exception while making data summary");
 			}
 		}
 	}
 
 	private void createOutputDirectory(String outputFilename) {
 		File dirFile = (new File(outputFilename)).getParentFile();
 		if (!dirFile.exists()) {
 			dirFile.mkdirs();
 		}
 	}
 
 	private void finishCurrentImage() {
 
 
 		this.originalImageWindow.setVisible(false);
 		this.maskWindow.setVisible(false);
 
 		
 		WritableImage outputImage = ImageFactory.createWritable(this.currentMaskImage);
 		WritableImage outputImageLabeled = ImageFactory.createWritable(this.currentLabeledMaskImage);
 		String outputImageFilename = this.getOutputFilename(this.currentMaskFilename);
 		String outputImageLabeledFilename = this.getOutputFilename(this.currentLabeledMaskFilename);
 		this.createOutputDirectory(outputImageFilename);
 
 		for (ImageCoordinate ic : outputImage) {
 			if (this.selectedRegions.contains((int) this.currentLabeledMaskImage.getValue(ic))) {
 				outputImage.setValue(ic, 0);
 				outputImageLabeled.setValue(ic, 0);
 			}
 		}
 
 
 		outputImage.writeToFile(outputImageFilename);
 		outputImageLabeled.writeToFile(outputImageLabeledFilename);
 
 		String outputDataFilename = this.getOutputFilename(this.currentDataFilename);
 		this.createOutputDirectory(outputDataFilename);
 

 		try {
 			BufferedReader input = new BufferedReader(new FileReader(this.currentDataFilename));
 
 			PrintWriter output = new PrintWriter(new FileWriter(outputDataFilename));
 
 			String currentLine = input.readLine();
 
 			while (currentLine != null) {
 
 				String[] split = currentLine.split(" ");
 				
 				int regionNumber = 0;
 				
 				try {
 
 					regionNumber =(int)  Double.parseDouble(split[0]);
 
 				} catch (NumberFormatException e) {
 					currentLine = input.readLine();
 
 					continue;
 				}
 					
 				if (! this.selectedRegions.contains(regionNumber)) {
 					output.println(currentLine);
 				}
 
 				currentLine = input.readLine();
 
 			}
 
 			output.close();
 			
 			File data = new File(this.currentDataFilename);
			
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(data.getParent() + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX + File.separator + data.getName()));
 			
 			Quantification q = (Quantification) ois.readObject();
 			
 			Quantification outputQ = new Quantification();
 			
 			if (q != null) {
 			
 				for (Measurement m : q.getAllMeasurements()) {
 					if (! this.selectedRegions.contains((int) m.getFeatureID())) {
 						outputQ.addMeasurement(m);
 					}
 				}
 			
 			}
 				
 			File outputData = new File(outputDataFilename);
 			
 			File outputDir = new File(outputData.getParent() + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX);
 			
 			if (! outputDir.exists()) {
 				outputDir.mkdir();
 			}
 			
 			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(outputData.getParent() + File.separator + AnalysisController.SERIALIZED_DATA_SUFFIX + File.separator + outputData.getName()));
 
 			oos.writeObject(outputQ);
 			
 			oos.close();
 			ois.close();
 			
 		} catch (java.io.IOException e) {
 			LoggingUtilities.warning("encountered IO exception while writing deschmutzed data");
 		} catch (ClassNotFoundException e) {
 			LoggingUtilities.warning("unable to read quantification while writing deschmutzed data");
 		}
 
 
 	}
 
 	private void readCurrentMaskAndDataFilenames(String nextImageToProcess) {
 
 		File nextToProcessFile = new File(nextImageToProcess);
 
 		// System.out.println(nextImageToProcess);
 
 		String parametersFilename = null;
 
 		String parametersDir = nextToProcessFile.getParent() + File.separator + PARAMETERS_DIR;
 
 		File parametersDirFile = new File(parametersDir);
 
 		for (File f : parametersDirFile.listFiles()) {
 			if (f.getName().matches(".*" + nextToProcessFile.getName() + ".*")) {
 				parametersFilename = f.getAbsolutePath();
 				break;
 			}
 		}
 
 		if (parametersFilename == null) {
 			this.currentParameters = null;
 			this.currentParametersFilename = null;
 			this.currentMaskFilename = null;
 			this.currentLabeledMaskFilename = null;
 			this.currentDataFilename = null;
 			return;
 		}
 		
 
 		AnalysisMetadata am = AnalysisMetadataParserFactory.createParserForFile(parametersFilename).parseFileToAnalysisMetadata(parametersFilename);
 		
 
 		ParameterDictionary p = am.getOutputParameters();
 
 		this.currentParameters = p;
 		this.currentParametersFilename = parametersFilename;
 
 		ImageSet outputImages = am.getOutputImages();
 
 		String maskFilename = outputImages.getImageNameForIndex(0);
 
 		this.currentMaskFilename = maskFilename;
 		this.currentLabeledMaskFilename = maskFilename;
 		if (outputImages.size() > 1) {
 			this.currentLabeledMaskFilename = outputImages.getImageNameForIndex(outputImages.size() - 1);
 		}
		this.currentDataFilename = nextToProcessFile.getParent() + File.separator + p.getValueForKey("data_output_filename");
 
 		this.lastParametersProcessed = parametersFilename;
 		this.lastDataFilenameProcessed = this.currentDataFilename;
 		
 	}
 
 	private String getOutputFilename(String inputFilename) {
 
 		File inputFile = new File(inputFilename);
 
 		String outputFilename = inputFile.getParent() + File.separator + OUTPUT_DIR + File.separator + inputFile.getName();
 		return outputFilename;
 
 	}
 
 	private void processNextImage() {
 
 		this.selectedRegions.clear();
 
 		if (this.filenamesToProcess.isEmpty()) {this.finish(); return;}
 
 		String nextToProcess = this.filenamesToProcess.remove(0);
 
 		this.readCurrentMaskAndDataFilenames(nextToProcess);
 
 		String maskFilename = this.currentMaskFilename;
 		String labeledMaskFilename = this.currentLabeledMaskFilename;
 
 		ImageReader ir = new ImageReader();
 
 		Image currentImage = null;
 		Image currentMask = null;
 		Image currentLabeledMask = null;
 
 		Image existingMask = null;
 
 		try {
 
 			if (maskFilename == null || labeledMaskFilename == null) {
 				throw new java.io.IOException("could not locate mask");
 			}
 			
 			currentImage = ir.read(nextToProcess);
 			currentMask = ir.read(maskFilename);
 			currentLabeledMask = ir.read(labeledMaskFilename);
 
 			if (this.dw.useExistingMask()) {
 				try {
 					existingMask = ir.read(this.getOutputFilename(maskFilename));
 				} catch (Exception e) {
 					LoggingUtilities.warning("encountered exception while attempting to load deschmutzerized mask, continuing without");
 					existingMask = null;
 				}
 			}
 			
 		} catch (java.io.IOException e) {
 			LoggingUtilities.warning("encountered exception while reading " + nextToProcess + ".  Continuing. " + e.getMessage());
 			this.processNextImage();
 			return;
 		} catch (IllegalArgumentException e) {
 			LoggingUtilities.warning("encountered exception while reading " + nextToProcess + ".  Continuing. " + e.getMessage());
 			this.processNextImage();
 			return;
 		}
 
 
 		this.currentOriginalImage = currentImage;
 		this.currentMaskImage = currentMask;
 		this.currentLabeledMaskImage = currentLabeledMask;
 
 
 		int channel = 0;
 
 		if(this.currentParameters != null && this.currentParameters.hasKey("marker_channel_index")) {
 			channel = this.currentParameters.getIntValueForKey("marker_channel_index");
 		}
 
 		WritableImage display = ImageFactory.createWritable(this.currentMaskImage);
 
 		for (ImageCoordinate ic : display) {
 			if (display.getValue(ic) > 0) {
 				display.setValue(ic, Float.MAX_VALUE);
 			}
 		}
 		
 		ImagePlus origMask = (ImageFactory.createWritable(this.currentMaskImage)).toImagePlus();
 		
 		this.colorCodedMaskDisplay = origMask.createHyperStack("mask", this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.C), this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.Z), this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.T), 24); //24 = RGB
 
 		for (int i = 0; i < this.colorCodedMaskDisplay.getImageStackSize(); i++) {
 			this.colorCodedMaskDisplay.setSlice(i+1);
 			origMask.setSlice(i+1);
 			this.colorCodedMaskDisplay.setProcessor(origMask.getProcessor().duplicate().convertToRGB());
 		}
 
 
 		if (existingMask != null) {
 			for (ImageCoordinate ic : existingMask) {
 				if (this.currentLabeledMaskImage.getValue(ic) > 0 && existingMask.getValue(ic) == 0) {
 
 					this.colorCodedMaskDisplay.setPosition(ic.get(ImageCoordinate.C)+1, ic.get(ImageCoordinate.Z)+1, ic.get(ImageCoordinate.T)+1);
 					ImageProcessor ip = this.colorCodedMaskDisplay.getProcessor();
 					int[] red = {255,0,0};
 					ip.putPixel(ic.get(ImageCoordinate.X), ic.get(ImageCoordinate.Y), red);
 					this.selectedRegions.add((int) this.currentLabeledMaskImage.getValue(ic));
 				}
 			}
 		}
 
 		ImagePlus orig = this.currentOriginalImage.toImagePlus();
 		this.originalImageDisplay = orig;
 		
 		this.originalImageDisplay.setPosition(channel+1, 1, 1);
 
 		if (this.maskWindow == null || this.originalImageWindow == null) {
 			this.colorCodedMaskDisplay.show();
 			maskWindow = this.colorCodedMaskDisplay.getWindow();
 
 			orig.show();
 			originalImageWindow = orig.getWindow();
 
 			this.originalImageWindow.setLocationRelativeTo(this.dw);
 			this.originalImageWindow.setLocationAndSize((int) (this.originalImageWindow.getLocation().getX()), (int) (this.originalImageWindow.getLocation().getY() + this.dw.getHeight()), 500, (int) (500.0*this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.Y)/this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.X)));
 
 			this.maskWindow.setLocationRelativeTo(this.dw);
 			this.maskWindow.setLocationAndSize((int) (this.maskWindow.getLocation().getX()+this.dw.getWidth()*1.1), (int) (this.maskWindow.getLocation().getY()+ this.dw.getHeight()/2), 500, (int) (500.0*this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.Y)/this.currentMaskImage.getDimensionSizes().get(ImageCoordinate.X)));
 
 			this.maskWindow.addMouseListener(this.dieh);
 			this.maskWindow.getCanvas().addMouseListener(this.dieh);
 			
 			this.originalImageWindow.addMouseListener(this.dieh);
 			this.originalImageWindow.getCanvas().addMouseListener(this.dieh);
 
 		} else {
 
 			this.maskWindow.setImage(this.colorCodedMaskDisplay);
 			this.originalImageWindow.setImage(orig);
 		}
 
 		this.maskWindow.setVisible(true);
 		this.originalImageWindow.setVisible(true);
 
 		orig.resetDisplayRange();
 		this.colorCodedMaskDisplay.resetDisplayRange();
 		orig.updateAndDraw();
 		this.originalImageDisplay.updateAndRepaintWindow();
 
 	}
 
 	public void startButtonPressed() {
 
 		Preferences.userNodeForPackage(this.getClass()).put("deschmutzerizer_filename", this.dw.getImageFilename());
 
 		this.filenamesToProcess = new java.util.LinkedList<String>();
 
 		File toProcess = new File(this.dw.getImageFilename());
 
 		if (toProcess.isDirectory()) {
 			for (File f : toProcess.listFiles()) {
 				this.filenamesToProcess.add(f.getAbsolutePath());
 			}
 		} else {
 			this.filenamesToProcess.add(toProcess.getAbsolutePath());
 		}
 
 		this.dw.setStartButtonEnabled(false);
 
 		this.processNextImage();
 
 
 	}
 
 	public void browseButtonPressed() {
 		String path = this.dw.getImageFilename();
 		JFileChooser fc = null;
 		if ((new File(path)).exists()) {
 			fc = new JFileChooser(path);
 		} else {
 			fc = new JFileChooser();
 		}
 		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
 		int retVal = fc.showOpenDialog(this.dw);
 
 		if (retVal == JFileChooser.APPROVE_OPTION) {
 
 			String selected = fc.getSelectedFile().getAbsolutePath();
 
 
 			dw.setImageFilename(selected);
 		}
 	}
 
 	public void continueButtonPressed() {
 		this.finishCurrentImage();
 		this.processNextImage();
 	}
 
 	public void selectAllButtonPressed() {
 
 		this.processBox(new Point(0,0), new Point(this.currentOriginalImage.getDimensionSizes().get(ImageCoordinate.X), this.currentOriginalImage.getDimensionSizes().get(ImageCoordinate.Y)));
 
 	}
 
 
 
 }
