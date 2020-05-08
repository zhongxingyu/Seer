 /* ***** BEGIN LICENSE BLOCK *****
  * 
  * Copyright (c) 2012 Colin J. Fuller
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the Software), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  * 
  * ***** END LICENSE BLOCK ***** */
 
 package edu.stanford.cfuller.imageanalysistools.fitting;
 
 import java.util.Map;
 
 /**
  * A class that holds the parameters that are the result of a fit of an {@link ImageObject}.
  * 
  * @author Colin J. Fuller
  */
 public class FitParameters implements java.io.Serializable {
 	
 	final static long serialVersionUID = 1L;
 	
 	Map<Integer, Double> positionParameters;
 	Map<Integer, Double> sizeParameters;
 	double amplitude;
 	double background;
 	
 	Map<String, Double> otherParameters;
 	
 	/**
 	 * Creates a new fit parameters object with no parameter values.
 	 */
 	public FitParameters() {
 		
 		this.positionParameters = new java.util.HashMap<Integer, Double>();
 		this.sizeParameters = new java.util.HashMap<Integer, Double>();
 		this.amplitude = 0;
 		this.background = 0;
 		this.otherParameters = new java.util.HashMap<String, Double>();
 		
 	}
 	
 	/**
 	 * Gets the position of the object associated with these parameters in the supplied dimension.
 	 * 
 	 * @param dim an int that specifies the dimension; this should be one of the dimension constants from {@link edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate ImageCoordinate} or a user-defined dimension.
 	 * @return the position in that dimension.
 	 */
 	public double getPosition(int dim) {
 		return this.positionParameters.get(dim);
 	}
 	
 	/**
 	 * Sets the position of the object associated with these parameters in the supplied dimension.
 	 * 
 	 * @param dim an int that specifies the dimension; this should be one of the dimension constants from {@link edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate ImageCoordinate} or a user-defined dimension.
 	 * @param value the position in the supplied dimension
 	 */
 	public void setPosition(int dim, double value) {
 		this.positionParameters.put(dim, value);
 	}
 	
 	/**
 	 * Gets the size of the object associated with these parameters in the supplied dimension.
 	 * 
 	 * @param dim an int that specifies the dimension; this should be one of the dimension constants from {@link edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate ImageCoordinate} or a user-defined dimension.
 	 * @return the size in that dimension.
 	 */
 	public double getSize(int dim) {
 		return this.sizeParameters.get(dim);
 	}
 	
 	/**
 	 * Sets the size of the object associated with these parameters in the supplied dimension.
 	 * 
 	 * @param dim an int that specifies the dimension; this should be one of the dimension constants from {@link edu.stanford.cfuller.imageanalysistools.image.ImageCoordinate ImageCoordinate} or a user-defined dimension.
 	 * @param value the size in the supplied dimension
 	 */
 	public void setSize(int dim, double value) {
 		this.sizeParameters.put(dim, value);
 	}
 	
 	/**
 	 * Gets the amplitude of the object associated with these parameters.
 	 * 
 	 * @return the amplitude
 	 */
 	public double getAmplitude() {
 		return this.amplitude;
 	}
 	
 	/**
 	 * Sets the amplitude of the object associated with these parameters.
 	 * 
 	 * @param a the amplitude
 	 */
 	public void setAmplitude(double a) {
 		this.amplitude = a;
 	}
 	
 	/**
 	 * Gets the background of the object associated with these parameters.
 	 * 
 	 * @return the background
 	 */
 	public double getBackground() {
 		return this.background;
 	}
 	
 	/**
 	 * Sets the background of the object associated with these parameters.
 	 * 
 	 * @param b the background
 	 */
 	public void setBackground(double b) {
 		this.background = b;
 	}
 	
 	/**
 	 * Gets the value of a named parameter associated with the fit object.
 	 * @param parameter a String naming the parameter
 	 * @return the value of the named parameter as a double
 	 */
 	public double getOtherParameters(String parameter) {
 		return this.otherParameters.get(parameter);
 	}
 	
 	/**
 	 * Sets the value of a named parameter associated with the fit object.
 	 * @param parameter a String naming the parameter
 	 * @param value the value of the named parameter as a double
 	 */
 	public void setOtherParameter(String parameter, double value) {
 		this.otherParameters.put(parameter, value);
 	}
 	
 	/**
 	* Creates a String representation of the parameters.
 	* Format is amplitude, background, pos in each channel, size in each channel, other parameters.
 	* @return a String containing the parameter information.
 	*/
 	public String toString() {
 		String s = "amp=" + this.getAmplitude();
 		s+= ";bkg="+this.getBackground();
 		for (Integer i : this.positionParameters.keySet()) {
 			s+= ";pos" + i + "=" + this.positionParameters.get(i);
 		}
 		for (Integer i : this.sizeParameters.keySet()) {
 			s+= ";size" + i + "=" + this.sizeParameters.get(i);
 		}
		for (String s : this.otherParameters.keySet()) {
			s+= ";" + s + "=" + this.otherParameters.get(s); 
 		}
 	}
 	
 }
 
 
 
