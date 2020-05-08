 /*
  * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Mateusz Parzonka - initial API and implementation
  */
 package prm4jeval;
 
 import java.io.Serializable;
 
 import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
 
 public class SteadyStateInvocation implements Serializable {
 
     private static final long serialVersionUID = -5341292262089739659L;
 
     /**
      * When the coefficient of standard deviation falls below this value, we have entered steady state.
      */
     protected final double covThreshold;
     protected final int window;
     protected int iteration;
     private final DescriptiveStatistics measurements;
 
     public SteadyStateInvocation(int window, double covThreshold) {
 	this.window = window;
 	this.covThreshold = covThreshold;
 	measurements = new DescriptiveStatistics(window);
     }
 
     public SteadyStateInvocation() {
 	window = Integer.parseInt(getSystemProperty("prm4jeval.window", "5"));
 	covThreshold = Double.parseDouble(getSystemProperty("prm4jeval.covThreshold", "0.02"));
 	measurements = new DescriptiveStatistics(window);
     }
 
     /**
      *
      * @param time
      * @return <code>true</code> if more measurements are needed
      */
     public boolean addMeasurement(long time) {
	iteration++;
 	final double value = new Long(time).doubleValue();
 	measurements.addValue(value);
 	return isThresholdReached();
     }
 
     public boolean isThresholdReached() {
 	if (measurements.getN() < window) {
 	    return false;
 	}
 	double cov = getCoefficientOfStandardDeviation();
 	if (cov < covThreshold) {
 	    System.out.println("Reached cov-threshold: " + cov);
 	    return true;
 	}
	if (iteration >= 25) {
 	    System.out.println("Performed 25 iterations, proceeding with cov=" + cov + " and mean of last " + window
 		    + " measurements: " + measurements.getMean());
 	    return true;
 	}
 	return false;
     }
 
     public double getCoefficientOfStandardDeviation() {
 	double standardDeviation = measurements.getStandardDeviation();
 	double mean = measurements.getMean();
 	return standardDeviation / mean;
     }
 
     /**
      * @return the mean of the measurements
      */
     public double getMean() {
 	return measurements.getMean();
     }
 
     /**
      * @return the measurementCount
      */
     public long getMeasurementCount() {
 	return measurements.getN();
     }
 
     static String getSystemProperty(String key, String defaultValue) {
 	final String value = System.getProperty(key);
 	return value != null ? value : defaultValue;
     }
 
 }
