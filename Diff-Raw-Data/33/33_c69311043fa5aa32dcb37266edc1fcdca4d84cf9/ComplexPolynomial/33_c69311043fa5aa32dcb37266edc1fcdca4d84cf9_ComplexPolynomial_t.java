 package com.codemelon.polynomial;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.codemelon.math.Complex;
 
 /**
  * Polynomials with complex coefficients
  * 
  * @author Marshall Farrier
  * @my.created Sep 16, 2013
  * @my.edited Sep 16, 2013
  */
 public class ComplexPolynomial {
 	
 	private static final int FOURIER_MULTIPLY_DEGREE_THRESHOLD = 2;
 
 	private ArrayList<Complex> coefficients;
 	
 	public ComplexPolynomial(List<Complex> coefficients) {
 		if (coefficients == null || coefficients.size() == 0) {
 			throw new IllegalArgumentException("Polynomial must have at least 1 coefficient");
 		}
 		this.coefficients = new ArrayList<Complex>(coefficients);
 		trimZeroCoefficients(this.coefficients);
 		validateCoefficients();
 	}
 	
 	public ComplexPolynomial(Complex[] coefficients) {
 		this(Arrays.asList(coefficients));
 	}
 	
 	/**
 	 * Construct a polynomial of the given degree where every coefficient is set
 	 * to 1
 	 * The 0 polynomial is taken to have degree -1
 	 * @param degree
 	 */
 	public static ComplexPolynomial fromDegree(int degree) {
 		ComplexPolynomial result = new ComplexPolynomial(degree, Complex.ONE);
 		if (degree < 0) {
 			result.coefficients.add(Complex.ZERO);
 		}
 		return result;
 	}
 	
 	public int degree() {
 		if (coefficients.size() > 1 || !coefficients.get(0).equalWithinDelta(Complex.ZERO)) {
 			return coefficients.size() - 1;
 		}
 		return -1;
 	}
 	
 	public Complex coefficient(int i) {
 		if (i < 0) {
 			throw new IllegalArgumentException("Coefficient index must be non-negative");
 		}
 		if (i >= coefficients.size()) {
 			return Complex.ZERO;
 		}
 		return coefficients.get(i);
 	}
 	
 	public ComplexPolynomial plus(ComplexPolynomial p) {
 		if (this.degree() <= p.degree()) {
 			return add(p, this);
 		}
 		return add(this, p);
 	}
 	
 	public ComplexPolynomial minus(ComplexPolynomial p) {
 		return subtract(this.coefficients, p.coefficients);
 	}
 	
 	public ComplexPolynomial times(ComplexPolynomial p) {
 		if (this.degree() < 0 || p.degree() < 0) {
 			return fromDegree(-1);
 		}
 		if (this.degree() < FOURIER_MULTIPLY_DEGREE_THRESHOLD || 
 				p.degree() < FOURIER_MULTIPLY_DEGREE_THRESHOLD || 
 				this.degree() < Math.log(p.degree()) || 
 				p.degree() < Math.log(this.degree())) {
 			return schoolMultiply(this, p);
 		}
 		return fourierMultiply(this, p);
 	}
 	
 	/**
 	 * Horner's rule
 	 * Cf. CLRS, p. 900
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public Complex evaluate(Complex c) {
 		if (coefficients.size() == 1 || c.equalWithinDelta(Complex.ZERO)) {
 			return coefficients.get(0);
 		}
 		Complex result = coefficients.get(coefficients.size() - 1);
 		for (int i = this.coefficients.size() - 2; i >= 0; i--) {
 			result = this.coefficients.get(i).plus(c.times(result));
 		}
 		return result;
 	}
 	
 	/**
 	 * For internal use only: Creates a polynomial with the given degree for which
 	 * all coefficients are set to the given value (Complex.ZERO is also allowed)
 	 * @param degree
 	 * @param coefficient
 	 */
 	private ComplexPolynomial(int degree, Complex coefficient) {
 		this(degree);
 		for (int i = 0; i <= degree; i++) {
 			this.coefficients.add(coefficient);
 		}
 	}
 	
 	/**
 	 * Internal use only: Creates a polynomial with an empty coefficient list
 	 * with capacity for the given coefficients
 	 * @param degree
 	 */
 	private ComplexPolynomial(int degree) {
 		this.coefficients = new ArrayList<Complex>(degree + 1);
 	}
 	
 	/**
 	 * The first input polynomial must have degree <= the degree of the second
 	 * polynomial
 	 * @param smaller
 	 * @param larger
 	 * @return
 	 */
 	private static ComplexPolynomial add(ComplexPolynomial larger, ComplexPolynomial smaller) {
 		ComplexPolynomial result = new ComplexPolynomial(larger.coefficients);
 		for (int i = 0; i < smaller.coefficients.size(); i++) {
 			result.coefficients.set(i, smaller.coefficients.get(i).plus(larger.coefficients.get(i)));
 		}
 		trimZeroCoefficients(result.coefficients);
 		return result;
 	}
 	
 	private static ComplexPolynomial subtract(ArrayList<Complex> first, ArrayList<Complex> second) {
 		ArrayList<Complex> resultCoefficients = new ArrayList<Complex>(first);
 		// pad first with 0s to make length at least the size of second
 		while (resultCoefficients.size() < second.size()) {
 			resultCoefficients.add(Complex.ZERO);
 		}
 		for (int i = 0; i < second.size(); i++) {
 			resultCoefficients.set(i, resultCoefficients.get(i).minus(second.get(i)));
 		}
 		return new ComplexPolynomial(resultCoefficients);
 	}
 	
 	/**
 	 * Neither input polynomial can be 0 (degree -1)
 	 * @param p1
 	 * @param p2
 	 * @return
 	 */
 	private static ComplexPolynomial schoolMultiply(ComplexPolynomial p1, ComplexPolynomial p2) {
 		int resultDegree = p1.degree() + p2.degree();
 		ComplexPolynomial result = new ComplexPolynomial(resultDegree, Complex.ZERO);
 		for (int i = 0; i < p1.coefficients.size(); i++) {
 			for (int j = 0; j < p2.coefficients.size(); j++) {
 				result.coefficients.set(i + j, result.coefficients.get(i + j) 
 						.plus(p1.coefficients.get(i).times(p2.coefficients.get(j))));
 			}
 		}
 		trimZeroCoefficients(result.coefficients);
 		return result;
 	}
 	
 	/**
 	 * Neither input can be 0
 	 * @param p1
 	 * @param p2
 	 * @return
 	 */
 	private static ComplexPolynomial fourierMultiply(ComplexPolynomial p1, ComplexPolynomial p2) {
 		int p1CoefficientsSize = p1.coefficients.size();
 		int p2CoefficientsSize = p2.coefficients.size();
 		int productArraySize = p1.degree() + p2.degree() + 1;
 		int powerOfTwo = 1;
 		while (powerOfTwo < productArraySize) {
 			powerOfTwo *= 2;
 		}
 		productArraySize = powerOfTwo;
 		Complex[] p1PaddedCoefficients = new Complex[productArraySize];
 		Complex[] p2PaddedCoefficients = new Complex[productArraySize];
 		for (int i = 0; i < productArraySize; i++) {
 			if (i < p1CoefficientsSize) {
 				p1PaddedCoefficients[i] = p1.coefficients.get(i);
 			}
 			else {
 				p1PaddedCoefficients[i] = Complex.ZERO;
 			}
 			if (i < p2CoefficientsSize) {
 				p2PaddedCoefficients[i] = p2.coefficients.get(i);
 			}
 			else {
 				p2PaddedCoefficients[i] = Complex.ZERO;
 			}
 		}
 		Complex[] p1Values = recursiveFFT(p1PaddedCoefficients);
 		Complex[] p2Values = recursiveFFT(p2PaddedCoefficients);
 		Complex[] productValues = new Complex[productArraySize];
 		for (int i = 0; i < productArraySize; i++) {
 			productValues[i] = p1Values[i].times(p2Values[i]);
 		}
 		return new ComplexPolynomial(recursiveFFTInverse(productValues));
 	}
 
 	//The most significant coefficient cannot be 0 unless it is the 0 polynomial
 	private static void trimZeroCoefficients(ArrayList<Complex> _coefficients) {
 		while(_coefficients.get(_coefficients.size() - 1).equalWithinDelta(Complex.ZERO) &&
 				_coefficients.size() > 1) {
 			_coefficients.remove(_coefficients.size() - 1);
 		}
 	}
 	
 	private void validateCoefficients() {
 		for (Complex coefficient : coefficients) {
 			if (coefficient == null) {
 				throw new NullPointerException("Coefficient cannot be null");
 			}
 		}
 	}
 	
 	/**
 	 * Implementation following to CLRS, p. 911
 	 * 
 	 * @param a array representing a complex polynomial (often padded with 0s
 	 * to guarantee a power of 2
 	 * @return
 	 */
 	static Complex[] recursiveFFT(Complex[] a) {
 		int n = a.length;
 		if (n == 1) {
 			return a;
 		}
 		int nOverTwo = n / 2;
 		Complex omegaN = Complex.fromPolar(1.0, 2.0 * Math.PI / n);
 		Complex omega = Complex.ONE;
 		Complex[] a0 = new Complex[nOverTwo];
 		Complex[] a1 = new Complex[nOverTwo];
 		for (int i = 0; i < nOverTwo; i++) {
 			a0[i] = a[2 * i];
 			a1[i] = a[2 * i + 1];
 		}
 		Complex[] y0 = recursiveFFT(a0);
 		Complex[] y1 = recursiveFFT(a1);
 		Complex[] y = new Complex[n];
 		for (int i = 0; i < nOverTwo; i++) {
 			y[i] = y0[i].plus(omega.times(y1[i]));
 			y[i + nOverTwo] = y0[i].minus(omega.times(y1[i]));
 			omega = omega.times(omegaN);
 		}
 		return y;
 	}
 	
 	/**
 	 * Implementation following the sketch from CLRS, pp. 912ff.
 	 * 
 	 * @param a array representing a complex polynomial (often padded with 0s
 	 * to guarantee a power of 2
 	 * @return
 	 */
 	static Complex[] recursiveFFTInverse(Complex[] y) {
		Complex[] result = recursiveFFTInverseInternal(y);
		Complex oneOverN = new Complex(1.0 / y.length, 0.0);
		for (int i = 0; i < y.length; i++) {
			result[i] = result[i].times(oneOverN);
		}
		return result;
	}
	
	private static Complex[] recursiveFFTInverseInternal(Complex[] y) {
 		int n = y.length;
 		if (n == 1) {
 			return y;
 		}
 		int nOverTwo = n / 2;
		Complex omegaNReciprocal = Complex.fromPolar(1.0, -2.0 * Math.PI / n);
 		Complex omega = Complex.ONE;
 		Complex[] y0 = new Complex[nOverTwo];
 		Complex[] y1 = new Complex[nOverTwo];
 		for (int i = 0; i < nOverTwo; i++) {
 			y0[i] = y[2 * i];
 			y1[i] = y[2 * i + 1];
 		}
		Complex[] a0 = recursiveFFTInverseInternal(y0);
		Complex[] a1 = recursiveFFTInverseInternal(y1);
 		Complex[] a = new Complex[n];
 		for (int i = 0; i < nOverTwo; i++) {
			a[i] = a0[i].plus(omega.times(a1[i]));
			a[i + nOverTwo] = a0[i].minus(omega.times(a1[i]));
 			omega = omega.times(omegaNReciprocal);
 		}
 		return a;
 	}
 }
