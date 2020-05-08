 package edu.grinnell.csc207.nguyenti.utils;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 /**
  * implementing fractions
  * 
  * @author Tiffany Nguyen
  * @author Earnest Wheeler
  * @author Matt Dole
  * @author Samuel A. Rebelsky
  * @author CSC207 2013F
  */
 
 public class Fraction {
 
     // +------------------+---------------------------------------------
     // | Design Decisions |
     // +------------------+
 
     /*
      * Fractions are immutable. Once you've created one, it stays that way.
      * 
      * Fractions are arbitrary precision.
      * 
      * Denominators are always positive. Therefore, negative fractions are
      * represented with a negative numerator. Similarly, if a fraction has a
      * negative numerator, it is negative.
      * 
      * Fractions are stored in simplified form.
      */
 
     private static BigInteger NEGATIVE_ONE = BigInteger.valueOf(-1);
 
     // +--------+-------------------------------------------------------
     // | Fields |
     // +--------+
 
     /** The numerator of the fraction. Can be positive, zero or negative. */
     BigInteger numerator;
 
     /** The denominator of the fraction. Must be positive. */
     BigInteger denominator;
 
     // +--------------+-------------------------------------------------
     // | Constructors |
     // +--------------+
 
     /**
      * Create a new fraction equivalent to numerator/denominator.
      */
     public Fraction(BigInteger numerator, BigInteger denominator)
 	    throws Exception {
 	if (denominator.intValue() == 0) {
 	    throw new Exception("Zero is an invalid denominator");
 	}
 	this.numerator = numerator;
 	this.denominator = denominator;
 	this.cleanup();
     } // Fraction(BigInteger, BigInteger)
 
     public Fraction(BigInteger numerator) {
 	this.numerator = numerator;
 	this.denominator = BigInteger.valueOf(1);
     } // Fraction(BigInteger)
 
     public Fraction(int numerator, int denominator) throws Exception {
 	if (denominator == 0) {
 	    throw new Exception("Zero is an invalid denominator");
 	}
 	this.numerator = BigInteger.valueOf(numerator);
 	this.denominator = BigInteger.valueOf(denominator);
 	this.cleanup();
 
     } // Fraction(int, int)
 
     public Fraction(int numerator) {
 	this.numerator = BigInteger.valueOf(numerator);
 	this.denominator = BigInteger.valueOf(1);
     } // Fraction(int)
 
     public Fraction(String fract) throws Exception {
 	String[] parts = fract.split("/");
 	if (parts.length > 2) {
 	    throw new Exception("Too many values");
	} else if ((Long.parseLong(parts[1]) == 0) && (parts.length != 1)) {
 	    throw new Exception("Zero is an invalid denominator");
 	}
 	try {
 	    this.numerator = BigInteger.valueOf(Long.parseLong(parts[0]));
 	    if (parts.length == 1) {
 		this.denominator = BigInteger.valueOf(1);
 	    } else {
 		this.denominator = BigInteger.valueOf(Long.parseLong(parts[1]));
 	    }
 	} catch (Exception e) {
 	    throw new Exception("Problem with " + fract + ": not a fraction");
 	}
 	this.cleanup();
     } // Fraction(String)
 
     public Fraction(long numerator, long denominator) throws Exception {
 	if (denominator == 0) {
 	    throw new Exception("Zero is an invalid denominator");
 	}
 	this.numerator = BigInteger.valueOf(numerator);
 	this.denominator = BigInteger.valueOf(denominator);
 	this.cleanup();
 
     } // Fraction(long, long)
 
     public Fraction(double num) {
 	double mantissa = (num % 1);
 	if (mantissa == 0) {
 	    this.numerator = BigInteger.valueOf((long) num);
 	    this.denominator = BigInteger.valueOf(1);
 	} else {
 	    long multiplier = (long) (1 / mantissa);
 	    long nearInt = Math.round(multiplier * num);
 	    this.numerator = BigInteger.valueOf(nearInt);
 	    this.denominator = BigInteger.valueOf(multiplier);
 	}
     }
 
     // +-------------------------+--------------------------------------
     // | Standard Object Methods |
     // +-------------------------+
 
     /**
      * Convert this fraction to a string for ease of printing.
      */
     public String toString() {
 	return this.numerator + "/" + this.denominator;
     } // toString()
 
     public int hashCode() {
 	return this.numerator.hashCode() * this.denominator.hashCode();
     }
 
     /**
      * If clone is called on something that isn't a fraction, it will return new
      * Fraction(-1)
      */
 
     public Fraction clone() {
 	try {
 	    return new Fraction(this.numerator, this.denominator);
 	} catch (Exception e) {
 	    return new Fraction(-1);
 	}
     }
 
     public int compareTo(Fraction other) {
 	return this.numerator.multiply(other.denominator).compareTo(
 		this.denominator.multiply(other.numerator));
     }
 
     public boolean equals(Fraction other) {
 	return (this.numerator.equals(other.numerator))
 		&& (this.denominator.equals(other.denominator));
     }
 
     public boolean equals(Object other) {
 	return (other instanceof Fraction) && (this.equals((Fraction) other));
     }
 
     // +-----------------+----------------------------------------------
     // | Private Methods |
     // +-----------------+
 
     /**
      * If the fraction is negative, make sure the numerator is negative and the
      * denominator is positive. If both the denominator and numerator are
      * negative, then make them both positive. The function then converts the
      * fraction to its simplest possible form
      */
 
     private void cleanup() {
 	if (this.denominator.signum() < 0) {
 	    this.denominator = this.denominator.abs();
 	    if (this.numerator.signum() > 0) {
 		this.numerator = this.numerator.multiply(NEGATIVE_ONE);
 	    } else {
 		this.numerator = this.numerator.abs();
 	    }
 	}
 	this.simplify();
     }
 
     private void simplify() {
 	BigInteger gcd = this.numerator.gcd(this.denominator);
 	this.numerator = this.numerator.divide(gcd);
 	this.denominator = this.denominator.divide(gcd);
     } // simplify()
 
     // +---------+------------------------------------------------------
     // | Methods |
     // +---------+
 
     /**
      * Add another fraction to this fraction.
      */
     public Fraction add(Fraction addend) throws Exception {
 	BigInteger resultNumerator;
 	BigInteger resultDenominator;
 
 	// The denominator of the result is the
 	// product of this object's denominator
 	// and addMe's denominator
 	resultDenominator = this.denominator.multiply(addend.denominator);
 	// The numerator is more complicated
 	resultNumerator = (this.numerator.multiply(addend.denominator))
 		.add(addend.numerator.multiply(this.denominator));
 
 	// Return the computed value
 	Fraction resultFrac = new Fraction(resultNumerator, resultDenominator);
 	resultFrac.simplify();
 	return resultFrac;
 
     } // add(Fraction)
 
     public Fraction subtract(Fraction subtractor) throws Exception {
 	subtractor.numerator = subtractor.numerator.multiply(NEGATIVE_ONE);
 	return this.add(subtractor);
     }
 
     /**
      * Approximate this fraction as a double.
      */
     public double toDouble() {
 	return this.numerator.doubleValue() / this.denominator.doubleValue();
     } // toDouble()
 
     /**
      * Approximate this fraction as a BigDouble
      */
 
     public BigDecimal toBigDecimal() {
 	BigDecimal bigNumerator = new BigDecimal(this.numerator);
 	BigDecimal bigDenominator = new BigDecimal(this.denominator);
 	return bigNumerator.divide(bigDenominator);
     }
 
     /**
      * Multiply another fraction by this fraction
      */
     public Fraction multiplyBy(Fraction multiplier) {
 	BigInteger resultNumerator;
 	BigInteger resultDenominator;
 
 	resultNumerator = this.numerator.multiply(multiplier.numerator);
 	resultDenominator = this.denominator.multiply(multiplier.denominator);
 
 	try {
 	    Fraction resultFrac = new Fraction(resultNumerator,
 		    resultDenominator);
 	    return resultFrac;
 	} catch (Exception e1) {
 	    return new Fraction(-1);
 	}
 
     }
 
     /**
      * Divide this fraction by another fraction
      * 
      * @param divisor
      * @return
      * @throws Exception
      */
 
     public Fraction divideBy(Fraction divisor) throws Exception {
 	BigInteger resultNumerator;
 	BigInteger resultDenominator;
 
 	resultNumerator = this.numerator.multiply(divisor.denominator);
 	resultDenominator = this.denominator.multiply(divisor.numerator);
 
 	Fraction resultFrac = new Fraction(resultNumerator, resultDenominator);
 	resultFrac.simplify();
 	return resultFrac;
     }
 
     /**
      * Find the fraction to the power of an integer -A fraction to a negative
      * power is its reciprocal to the absolute value of the power
      * 
      * @param exponent
      * @return
      */
 
     public Fraction pow(int exponent) throws Exception {
 	if (exponent == 0) {
 	    return new Fraction(1);
 	} else {
 	    BigInteger resultNumerator;
 	    BigInteger resultDenominator;
 	    int sign = this.compareTo(new Fraction(0));
 	    int power = Math.abs(exponent);
 
 	    resultNumerator = this.numerator.pow(power);
 	    resultDenominator = this.denominator.pow(power);
 
 	    Fraction resultFrac = new Fraction(resultNumerator,
 		    resultDenominator);
 	    resultFrac.simplify();
 	    if (sign >= 0) {
 		return resultFrac;
 	    } else {
 		return resultFrac.reciprocal();
 	    }
 	}
     }
 
     /**
      * Return the reciprocal of a fraction
      * 
      * @return
      */
     public Fraction reciprocal() throws Exception {
 	if (this.numerator == BigInteger.valueOf(0)) {
 	    throw new Exception("0 does not have a reciprocal");
 	}
 	BigInteger resultNumerator = this.denominator;
 	BigInteger resultDenominator = this.numerator;
 	Fraction reciprocal = new Fraction(resultNumerator, resultDenominator);
 	return reciprocal;
     }
 
     /**
      * Return the fraction multiplied by -1
      * 
      * @return
      */
     public Fraction negate() throws Exception {
 	BigInteger newmerator = this.numerator.multiply(NEGATIVE_ONE);
 	return new Fraction(newmerator, this.denominator);
     }
 
     /**
      * Return the numerator of a fraction
      */
     public BigInteger numerator() {
 	return this.numerator;
     }
 
     /**
      * Return the denominator of a fraction
      */
     public BigInteger denominator() {
 	return this.denominator;
     }
 
     /**
      * Returns the proper fraction portion of a given fraction. For a whole
      * number, n/1, it will return 0
      */
     public Fraction fractionalPart() throws Exception {
 	BigInteger resultNumerator = this.numerator.remainder(this.denominator);
 	return new Fraction(resultNumerator, this.denominator);
     }
 
     public BigInteger wholePart() {
 	BigInteger resultNum = this.numerator.subtract(
 		this.numerator.remainder(this.denominator)).divide(
 		this.denominator);
 	return resultNum;
     }
 
     public static void main(String[] args) {
 	System.out.print(new Fraction(6.0).toString());
     }
 } // class Fraction
