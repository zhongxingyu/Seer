 /**
  * EFTEMj - Processing of Energy Filtering TEM images with ImageJ
  * 
  * Copyright (c) 2013, Michael Epping
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package elemental_map;
 
 import ij.IJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.gui.GenericDialog;
 import ij.process.Blitter;
 import ij.process.ByteProcessor;
 import ij.process.FloatProcessor;
 
 import java.util.Arrays;
 import java.util.Vector;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 /**
  * This class will calculate the elemental maps. It is separated from the plugin for reason of clarity.
  * 
  * @author Michael Epping <michael.epping@uni-muenster.de>
  * 
  */
 public class ElementalMapping {
 
     /**
      * All fit methods that are available are listed at the {@link Enum}. Each method contains a full name that can be
      * used to be displayed at the GUI.
      * 
      * @author Michael Entrup <michael.entrup@uni-muenster.de>
      */
     public static enum AVAILABLE_METHODS {
 	LSE("Least squares estimation"), MLE("Maximum-likelihood estimation"), WLSE("Weighted least squares estimation");
 
 	/**
 	 * Full name of the method. Display this {@link String} at the GUI.
 	 */
 	private String fullName;
 
 	/**
 	 * @param fullName
 	 *            The full name that identifies the method.
 	 */
 	private AVAILABLE_METHODS(String fullName) {
 	    this.fullName = fullName;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Enum#toString()
 	 */
 	public String toString() {
 	    return fullName;
 	}
 
 	/**
 	 * @return A {@link String} array that can be used to create a choice at a {@link GenericDialog}.
 	 */
 	public static String[] toStringArray() {
 	    String[] array = new String[values().length];
 	    for (int i = 0; i < array.length; i++) {
 		array[i] = values()[i].toString();
 	    }
 	    return array;
 	}
     }
 
     /**
      * This {@link Enum} lists all accuracy values (break conditions) that are available to the background fit.
      * 
      * @author Michael Entrup <michael.entrup@uni-muenster.de>
      */
     public static enum AVAILABLE_EPSILONS {
 	VERY_LOW(1.0E-03), LOW(1.0E-06), MID(1.0E-09), HIGH(1.0E-12), VERRY_HIGH(1.0E-15);
 
 	/**
 	 * A double value that represents the accuracy of the background fit.
 	 */
 	private double epsilon;
 
 	/**
 	 * @param epsilon
 	 *            A double value that represents the accuracy (break conditions).
 	 */
 	private AVAILABLE_EPSILONS(double epsilon) {
 	    this.epsilon = epsilon;
 	}
 
 	/**
 	 * @return The accuracy (break condition).
 	 */
 	public double getValue() {
 	    return epsilon;
 	}
 
 	public String toString() {
 	    return Double.toString(epsilon);
 	}
 
 	/**
 	 * @return A {@link String} array that can be used to create a choice at a {@link GenericDialog}.
 	 */
 	public static String[] toStringArray() {
 	    String[] array = new String[values().length];
 	    for (int i = 0; i < array.length; i++) {
 		array[i] = values()[i].toString();
 	    }
 	    return array;
 	}
     }
 
     /**
      * All energy losses that are lower than the selected edge energy loss.<br />
      * The images with this energy losses will be used to fit the power law background signal.
      */
     private float[] preEdgeEnergyLosses;
     /**
      * All energy losses that are higher than the selected edge energy loss.<br />
      * The images with this energy losses will be used to extract the elemental signal.
      */
     private float[] postEdgeEnergyLosses;
     /**
      * The images at the stack will not be sorted. This indices are used to identify the pre-edge images.<br />
      * The indices start at 1 to match the indexing of {@link ImageStack}s.
      */
     private int[] preEdgeIndices;
     /**
      * The images at the stack will not be sorted. This indices are used to identify the post-edge images.<br />
      * The indices start at 1 to match the indexing of {@link ImageStack}s.
      */
     private int[] postEdgeIndices;
     /**
      * This {@link String} represents the power law fit method that has been choosen.
      */
     private AVAILABLE_METHODS method;
     /**
      * This is the break condition for iterative power law fit methods.<br />
      * If the results of two iteration differ by less then this value the iteration will be stopped.
      */
     private float epsilon;
     /**
      * The {@link ImagePlus} that is used for elemental mapping.
      */
     private ImagePlus impStack;
     /**
      * A map of the parameter <strong>r</strong> ( power law: I(E) = a&sdot;E<sup>-r</sup> ).
      */
     private FloatProcessor rMap;
     /**
      * A map of the parameter <strong>a</strong> ( power law: I(E) = a&sdot;E<sup>-r</sup> ).
      */
     private FloatProcessor aMap;
     /**
      * A map that shows all errors that occurred at the power law fit.
      */
     private ByteProcessor errorMap;
     /**
      * The extracted elemental signal of all post-edge images.
      */
     private FloatProcessor[] elementalMaps;
     /**
      * This field indicates the progress. A static method is used to increase the value by 1. It is necessary to use
      * volatile because different {@link Thread}s call the related method.
      */
     private static volatile int progress;
     /**
      * Number of steps until the drift detection is finished.
      */
     private static int progressSteps;
 
     /**
      * @param energyLossArray
      *            The energy losses of all images at the given stack.
      * @param stack
      *            An {@link ImagePlus} that contains the stack to process.
      * @param edgeEnergyLoss
      *            The onset energy of the considered ionisation edge.
      * @param epsilon
      *            The accuracy of the power low fit (this is the break condition for the used fit method).
      * @param method
      *            The method used for fitting the power law function.
      */
     public ElementalMapping(float[] energyLossArray, ImagePlus stack, float edgeEnergyLoss, float epsilon,
 	    AVAILABLE_METHODS method) {
 	this.method = method;
 	this.epsilon = epsilon;
 	this.impStack = stack;
 	splitEnergyLosses(energyLossArray, edgeEnergyLoss);
 	rMap = new FloatProcessor(stack.getWidth(), stack.getHeight());
 	aMap = new FloatProcessor(stack.getWidth(), stack.getHeight());
 	errorMap = new ByteProcessor(stack.getWidth(), stack.getHeight());
 	elementalMaps = new FloatProcessor[postEdgeIndices.length];
 	for (int i = 0; i < elementalMaps.length; i++) {
 	    elementalMaps[i] = new FloatProcessor(stack.getWidth(), stack.getHeight());
 	    float[] pixels = (float[]) elementalMaps[i].getPixels();
 	    Arrays.fill(pixels, Float.NaN);
 	}
 	progressSteps = stack.getHeight();
     }
 
     /**
      * {@link NormCrossCorrelationTask} will use this method to update the process.
      */
     private static void updateProgress() {
 	progress++;
 	IJ.showProgress(progress, progressSteps);
     }
 
     /**
      * Starts the calculation with parallel {@link Thread}s.
      */
     public void startCalculation() {
 	ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
 	for (int j = 0; j < impStack.getHeight(); j++) {
 	    executorService.execute(new ElementalMappingTask(j));
 	}
 	executorService.shutdown();
 	try {
 	    executorService.awaitTermination(5, TimeUnit.MINUTES);
 	} catch (InterruptedException e) {
 	    e.printStackTrace();
 	}
     }
 
     /**
      * Shows an {@link ImagePlus}with the map of the parameter <strong>r</strong>.<br />
      * All values are 0 if no calculation has been done before.
      */
     public void showRMap() {
 	ImagePlus impRMap = new ImagePlus("Map of parameter r", rMap);
 	impRMap.show();
     }
 
     /**
      * Shows an {@link ImagePlus} with the map of the parameter <strong>ln(a)</strong>. Using the logarithm makes it
      * easier to review the map.<br />
      * All values are 0 if no calculation has been done before.
      */
     public void showLnAMap() {
 	FloatProcessor lnAMap = new FloatProcessor(aMap.getWidth(), aMap.getHeight());
 	lnAMap.copyBits(aMap, 0, 0, Blitter.COPY);
 	lnAMap.log();
 	ImagePlus impLnAMap = new ImagePlus("Map of parameter ln(a)", lnAMap);
 	impLnAMap.show();
     }
 
     /**
      * Shows an {@link ImagePlus} with the map of the all errors that occurred at the power law fit.<br />
      * All values are 0 if no calculation has been done before, or no error occurred.
      */
     public void showErrorMap() {
 	ImagePlus impErrorMap = new ImagePlus("Map errors", errorMap);
 	impErrorMap.show();
     }
 
     /**
      * Shows one or more {@link ImagePlus} with the elemental map.<br />
      * All values are 0 if no calculation has been done before.
      */
     public void showElementalMap() {
 	for (int z = 0; z < elementalMaps.length; z++) {
	    ImagePlus impElementalMap = new ImagePlus("Elemental map " + postEdgeEnergyLosses[z] + "eV",
		    elementalMaps[z]);
 	    impElementalMap.show();
 	}
     }
 
     /**
      * This method has to be called at the constructor. It will fill the arrays of pre-edge and post-edge energy losses.
      * 
      * @param allEnergyLosses
      *            An array of energy losses.
      * @param edgeEnergyLoss
      *            All energy losses lower than this one are pre-edge energy losses.
      */
     private void splitEnergyLosses(float[] allEnergyLosses, float edgeEnergyLoss) {
 	Vector<Float> preEdgeVector = new Vector<>();
 	Vector<Float> postEdgeVector = new Vector<>();
 	Vector<Integer> preEdgeIndexVector = new Vector<>();
 	Vector<Integer> postEdgeIndexVector = new Vector<>();
 	for (int i = 0; i < allEnergyLosses.length; i++) {
 	    if (allEnergyLosses[i] < edgeEnergyLoss) {
 		preEdgeVector.add(allEnergyLosses[i]);
 		// use an index starting at 1
 		preEdgeIndexVector.add(i + 1);
 	    } else {
 		postEdgeVector.add(allEnergyLosses[i]);
 		// use an index starting at 1
 		postEdgeIndexVector.add(i + 1);
 	    }
 	}
 	preEdgeIndices = new int[preEdgeIndexVector.size()];
 	preEdgeEnergyLosses = new float[preEdgeIndexVector.size()];
 	for (int i = 0; i < preEdgeIndices.length; i++) {
 	    preEdgeIndices[i] = preEdgeIndexVector.get(i);
 	    preEdgeEnergyLosses[i] = preEdgeVector.get(i);
 	}
 	postEdgeIndices = new int[postEdgeIndexVector.size()];
 	postEdgeEnergyLosses = new float[postEdgeIndexVector.size()];
 	for (int i = 0; i < postEdgeIndices.length; i++) {
 	    postEdgeIndices[i] = postEdgeIndexVector.get(i);
 	    postEdgeEnergyLosses[i] = postEdgeVector.get(i);
 	}
     }
 
     /**
      * This Class implements {@link Runnable} to allow parallel calculation of power low fit functions. This is
      * possible, because the calculation is independent for each pixel of the stack. To reduce the administrative
      * workload, one task is created for each image row instead of one task for each pixel.
      * 
      * @author Michael Epping <michael.epping@uni-muenster.de>
      * 
      */
     private class ElementalMappingTask implements Runnable {
 
 	/**
 	 * The image row to process.
 	 */
 	private int y;
 
 	/**
 	 * A default constructor that only sets one field.
 	 * 
 	 * @param y
 	 *            The image row to process.
 	 */
 	public ElementalMappingTask(int y) {
 	    super();
 	    this.y = y;
 	}
 
 	@Override
 	public void run() {
 	    float[] counts = new float[preEdgeIndices.length];
 	    // A power low fit method has to extend PowerLawFit.
 	    PowerLawFit fitMethod;
 	    switch (method) {
 	    case MLE:
 		fitMethod = new PowerLawFit_MLE(preEdgeEnergyLosses, counts, epsilon);
 		break;
 	    default:
 		break;
 	    }
 	    for (int x = 0; x < impStack.getWidth(); x++) {
 		float r;
 		float a;
 		for (int z = 0; z < preEdgeIndices.length; z++) {
 		    counts[z] = impStack.getStack().getProcessor(preEdgeIndices[z]).getf(x, y);
 		}
 		fitMethod = new PowerLawFit_MLE(preEdgeEnergyLosses, counts, epsilon);
 		fitMethod.doFit();
 		if (fitMethod.getErrorCode() == PowerLawFit_MLE.ERROR_NONE) {
 		    r = (float) fitMethod.getR();
 		    if (Float.isInfinite(r)) {
 			errorMap.set(x, y, PowerLawFit_MLE.ERROR_R_INFINITE);
 			rMap.setf(x, y, Float.NaN);
 			aMap.setf(x, y, Float.NaN);
 		    } else {
 			rMap.setf(x, y, r);
 			a = (float) fitMethod.getA();
 			if (Float.isInfinite(a)) {
 			    errorMap.set(x, y, PowerLawFit_MLE.ERROR_A_INFINITE);
 			    rMap.setf(x, y, Float.NaN);
 			    aMap.setf(x, y, Float.NaN);
 			} else {
 			    errorMap.set(x, y, PowerLawFit_MLE.ERROR_NONE);
 			    aMap.setf(x, y, a);
 			    for (int z = 0; z < postEdgeIndices.length; z++) {
 				float value = impStack.getStack().getProcessor(postEdgeIndices[z]).getf(x, y);
 				float bg = (float) (a * Math.pow(postEdgeEnergyLosses[z], -r));
 				elementalMaps[z].setf(x, y, value - bg);
 			    }
 			}
 		    }
 		} else {
 		    errorMap.set(x, y, fitMethod.getErrorCode());
 		    rMap.setf(x, y, Float.NaN);
 		    aMap.setf(x, y, Float.NaN);
 		}
 	    }
 	    ElementalMapping.updateProgress();
 	}
 
     }
 }
