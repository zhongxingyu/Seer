 /**
  * 
  */
 package de.jail.schemas;
 
import de.jail.MathLib;
 import de.jail.interfaces.IDefaultOperator;
 
 /**
  * This class represents a complex number.  A complex number is a number 
  * consisting of a real and imaginary part. It can be written in the 
  * form <code>a + bi</code>, where <code>a</code> and <code>b</code> are 
  * real numbers, and <code>i</code> is the standard imaginary unit with the 
  * property <code>i� = -1</code>.
  * 
  * @author Christian Vogel
  */
 public class ComplexNumber implements IDefaultOperator<ComplexNumber> {
 	
 	private double re;
 	private double im;
 	
 	/**
 	 * Initializes a new zero complex number.
 	 */
 	public ComplexNumber() {}
 	
 	/**
 	 * Initializes a new complex number initially equal to re + im i
 	 */
 	public ComplexNumber(double re, double im) {
 		setRealNumber(re);
 		setImaginaryNumber(im);
 	}
 	
 
 	/**
 	 * Sets the real part of the number.
 	 * 
 	 * @param re the real part of the number to set
 	 */
 	public void setRealNumber(double re) {
 		this.re = re;
 	}
 
 	/**
 	 * Gets the real part of the number.
 	 * 
 	 * @return the real part of the number
 	 */
 	public double getRealNumber() {
 		return re;
 	}
 
 	/**
 	 * Sets the imaginary part of the number.
 	 * 
 	 * @param im the imaginary part of the number to set
 	 */
 	public void setImaginaryNumber(double im) {
 		this.im = im;
 	}
 
 	/**
 	 * Gets the imaginary part of the number.
 	 * 
 	 * @return the imaginary part of the number
 	 */
 	public double getImaginaryNumber() {
 		return im;
 	}
 
 	/* (non-Javadoc)
 	 * @see de.mathlib.interfaces.IOperator#add(java.lang.Object)
 	 */
 	@Override
 	public ComplexNumber add(ComplexNumber arg) {
 		if(arg == null) {
 			throw new NullPointerException("arg cannot be null");
 		}
 		
 		double resultRe = this.re + arg.re;
 		double resultIm = this.im + arg.im;
 		
 		return new ComplexNumber(resultRe, resultIm);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.mathlib.interfaces.IOperator#mul(java.lang.Object)
 	 */
 	@Override
 	public ComplexNumber mul(ComplexNumber arg) {
 		if(arg == null) {
 			throw new NullPointerException("arg cannot be null");
 		}
 		
 		double resultRe = this.re * arg.re - this.im * arg.im;
 		double resultIm = this.re * arg.im + arg.re * this.im;
 		
 		return new ComplexNumber(resultRe, resultIm);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.mathlib.interfaces.IOperator#div(java.lang.Object)
 	 */
 	@Override
 	public ComplexNumber div(ComplexNumber arg) {
 		if(arg == null) {
 			throw new NullPointerException("arg cannot be null");
 		}
 		
		double resultRe = (this.re * arg.re + this.im * arg.im) / (MathLib.square(arg.re) + MathLib.square(arg.im));
		double resultIm = (this.im * arg.re - this.re * arg.im) / (MathLib.square(arg.re) + MathLib.square(arg.im));
 		
 		return new ComplexNumber(resultRe, resultIm);
 	}
 
 	/* (non-Javadoc)
 	 * @see de.mathlib.interfaces.IOperator#sub(java.lang.Object)
 	 */
 	@Override
 	public ComplexNumber sub(ComplexNumber arg) {
 		if(arg == null) {
 			throw new NullPointerException("arg cannot be null");
 		}
 		
 		double resultRe = this.re - arg.re;
 		double resultIm = this.im - arg.im;
 		
 		return new ComplexNumber(resultRe, resultIm);
 	}
 
 	@Override
 	public String toString() {
 		String rePart = Double.toString(getRealNumber());
 		String imPart = Double.toString(getImaginaryNumber()) + "i";
 		
 		String complexNumber = "";
 		
 		if(imPart.contains("-")) {
 			complexNumber += rePart + " - " + imPart.replace("-", "");
 		} else {
 			complexNumber += rePart + " + " + imPart;
 		}
 		
 		return  complexNumber;
 	}
 }
