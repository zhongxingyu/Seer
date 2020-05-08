 package aufgabe1_1;
 /**
  * Complex.<p>
  * 
  * Represents a complex number Z = a + i*b with real part
  * Re(Z) = a and imaginary part Im(Z) = b.
  *
  * @author Jendrik Baggendorf, Daniel Dahmer
 * @version 2013-11-27
  */
 public class Complex {
 
 	private double real;	// requirement 1
 	private double imag;	// requirement 1
 	private double abs;	// requirement 6
 	private double phase;	// requirement 7
 	
 	// requirement 2
 	public Complex(){
 		real = 0.0;
 		imag = 0.0;		
 	}
 	
 	// requirement 3
 	public Complex(double real, double imag) {
 		this();
 		this.real = real;
 		this.imag = imag;
 	}
 	
 	// requirement 4
 	/**
 	 * getReal.<p>
 	 * 
 	 * Get the real part of a complex number
 	 * 
 	 * @return real the actual value of the real part of Z
 	 */
 	public double getReal() {
 		return real;
 	}
 	
 	// requirement 4
 	/**
 	 * getImag.<p>
 	 * 
 	 * Get the imaginary part of a complex number 
 	 * 
 	 * @return imag the actual value of the imaginary part of Z
 	 */
 	public double getImag() {
 		return imag;
 	}
 	
 	// requirement 6
 	/**
 	 * getAbs.<p>
 	 * 
 	 * Get the amount of a complex number
 	 * 
 	 * @return abs the amount of Z
 	 */
 	public double getAbs() {
 		abs = Math.sqrt(Math.pow(real, 2) + Math.pow(imag, 2));
 		return abs;
 	}
 	
 	// requirement 7
 	/**
 	 * getPhase.<p>
 	 * 
 	 * Get the phase of a complex number
 	 * 
 	 * @return phase the phase of Z
 	 */
 	public double getPhase() {
 		phase = Math.atan2(imag, real);
 		return phase;
 	}
 
 	// requirement 5
 	/**
 	 * Addition with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 + Z2 = (a + i*b) + (c + i*d) = (a + c) + i * (b + d)
 	 * 
 	 * @param z Complex number to add with
 	 * @return reference of this object after addition
 	 */
 	public Complex add(Complex z){
 		this.real = (this.real + z.real);
 		this.imag = (this.imag + z.imag);
 		return this;
 	}
 	
 	// requirement 5
 	/**
 	 * Subtraction with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 - Z2 = (a + i*b) - (c + i*d) = (a - c) + i * (b - d)
 	 * 
 	 * @param z Complex number to subtract with
 	 * @return reference of this object after subtraction
 	 */
 	public Complex sub(Complex z){
 		this.real = (this.real - z.real);
 		this.imag = (this.imag - z.imag);
 		return this;
 	}
 	
 	// requirement 5
 	/**
 	 * Multiply with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 * Z2 = (a + i*b) * (c + i*d) = (ac - bd) + i * (ad + bc)
 	 * 
 	 * @param z Complex number to multiply with
 	 * @return reference of this object after multiplication
 	 */
 	public Complex mul(Complex z){
 		double re;
 		re = ((this.real * z.real) - (this.imag * z.imag));
 		this.imag = ((this.real * z.imag) + (this.imag * z.real));
 		this.real = re;
 		return this;
 	}
 	
 	// requirement 5
 	/**
 	 * Division with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 / Z2 = [(ac + bd) / (c + d)] + i * [(cb - ad) / (c + d)]
 	 * 
 	 * @param z Complex number to divide with
 	 * @return reference of this object after division
 	 */	
 	public Complex div(Complex z){
 		double re;
 		re = (((this.real * z.real) + (this.imag * z.imag)) / ((Math.pow(z.real, 2)) + (Math.pow(z.imag, 2))));
 		this.imag = (((z.real * this.imag) - (this.real * z.imag)) / ((Math.pow(z.real, 2)) + (Math.pow(z.imag, 2))));
 		this.real = re;
 		return this;
 	}
 	
 	// requirement 8
 	/**
 	 * Addition with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 + Z2 = (a + i*b) + (c + i*d) = (a + c) + i * (b + d)
 	 * 
 	 * @param z1 Complex number to add with
 	 * @param z2 Complex number to add with
 	 * @return Z after addition
 	 */
 	public static Complex add(Complex z1, Complex z2){
 		Complex Z = new Complex();
 		Z.real = (z1.real + z2.real);
 		Z.imag = (z1.imag + z2.imag);
 		return Z;
 	}
 	
 	// requirement 8
 	/**
 	 * Subtraction with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 - Z2 = (a + i*b) - (c + i*d) = (a - c) + i * (b - d)
 	 * 
 	 * @param z1 Complex number to subtract with
 	 * @param z2 Complex number to subtract with
 	 * @return Z after subtraction
 	 */
 	public static Complex sub(Complex z1, Complex z2){
 		Complex Z = new Complex();
 		Z.real = (z1.real - z2.real);
 		Z.imag = (z1.imag - z2.imag);
 		return Z;
 	}
 
 	// requirement 8
 	/**
 	 * Multiply with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 * Z2 = (a + i*b) * (c + i*d) = (ac - bd) + i * (ad + bc)
 	 * 
 	 * @param z1 Complex number to multiply with
 	 * @param z2 Complex number to multiply with
 	 * @return Z after multiplication
 	 */
 	public static Complex mul(Complex z1, Complex z2){
 		Complex Z = new Complex();
 		Z.real = ((z1.real * z2.real) - (z1.imag * z2.imag));
 		Z.imag = ((z1.real * z2.imag) + (z1.imag * z2.real));
 		return Z;
 	}
 	
 	// requirement 8
 	/**
 	 * Division with a complex number.<p>
 	 * 
 	 * For Z1 = a + i*b and Z2 = c + i*d:<br>
 	 * Z1 / Z2 = [(ac + bd) / (c + d)] + i * [(cb - ad) / (c + d)]
 	 * 
 	 * @param z1 Complex number to divide with
 	 * @param z2 Complex number to devide with
 	 * @return Z after division
 	 */	
 	public static Complex div(Complex z1, Complex z2){
 		Complex Z = new Complex();
 		Z.real = ((z1.real * z2.real) + (z1.imag * z2.imag)) / (Math.pow(z2.real, 2) + Math.pow(z2.imag, 2));
 		Z.imag = ((z2.real * z1.imag) - (z1.real * z2.imag)) / (Math.pow(z2.real, 2) + Math.pow(z2.imag, 2));
 		return Z;
 	}
 
 	// requirement 9
 	/** 
 	 * toString.<p>
 	 * 
 	 * Checks the sign of the imaginary part and change the format to a string.<br> 
 	 * (real part + imaginary part * j) respectively (real part - imaginary part * j)
 	 * 
 	 * @return String in cartesian form
 	 */
 	@Override
 	public String toString() {
		double im = imag * -1;
 		if (imag >= 0){
 			return "(" + real + " + " + imag + " * j)";
 		} else {
			return "(" + real + " - " + im + " * j)"; 
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		long temp;
 		temp = Double.doubleToLongBits(imag);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		temp = Double.doubleToLongBits(real);
 		result = prime * result + (int) (temp ^ (temp >>> 32));
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	// requirement 10
 	@Override
 	public boolean equals(Object obj) {
 		Complex other = (Complex) obj;
 		if ( ( this.getReal() == other.getReal() ) && ( this.getImag() == other.getImag() ) ){
 			return true;
 		}
 		else
 			return false;
 	}
 	
 }
