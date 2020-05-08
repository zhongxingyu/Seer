 package org.bh.data.types;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Class for economic calculation with mixed types. Especially Interval
  * arithmetic is relevant.
  * 
  * @author Sebastian
  * @author Norman
  * @author Robert
  * @author Alex
  * 
  * @version 0.1, 21.11.2009, Sebastian
  * @version 0.2, unknown, Robert
  * @version 0.3, 21.12.2009, Norman
  * @version 0.4, 22.12.2009, Alex
  * @version 0.5, 22.12.2009, Robert
  */
 public abstract class Calculable implements IValue {
 
 	/**
 	 * Pattern for a double value. It matches zero or one minus sign, followed
 	 * by zero or more digits (0-9), followed by a dot and one or more digits.
 	 * Leading or trailing spaces will be ignored.
 	 * 
 	 * <p>
 	 * Examples for values are:
 	 * <ul>
 	 * <li>1.2
 	 * <li>12.3
 	 * <li>12.34
 	 * <li>.1
 	 * <li>1.
 	 * <li>12
 	 * <li>-1.2
 	 * <li>-.1
 	 * <li>-1
 	 * <li>&nbsp;1.2&nbsp; (spaces before and/or after the value)
 	 * </ul>
 	 * 
	 * <p>
 	 * However, these values do not match:
 	 * <ul>
 	 * <li>(empty string)
 	 * <li>1,2
 	 * <li>- 1.2
 	 * <li>1
 	 * <li>abc
 	 * <li>a 1 b
 	 * </ul>
 	 */
 	public static final Pattern DOUBLE_PATTERN = Pattern.compile("^\\s*"
 			+ DoubleValue.REGEX + "\\s*$");
 
 	/**
 	 * Pattern for an integer value. It matches zero or one minus sign, followed
 	 * by one or more digits. Leading or trailing spaces will be ignored.
 	 * 
 	 * <p>
 	 * Examples for values are:
 	 * <ul>
 	 * <li>1
 	 * <li>12
 	 * <li>-1
 	 * <li>&nbsp;1&nbsp; (spaces before and/or after the value)
 	 * </ul>
 	 * 
	 * <p>
 	 * However, these values do not match:
 	 * <ul>
 	 * <li>(empty string)
 	 * <li>- 1
 	 * <li>1.
 	 * <li>.1
 	 * <li>1.2
 	 * <li>abc
 	 * <li>a 1 b
 	 * </ul>
 	 */
 	public static final Pattern INTEGER_PATTERN = Pattern.compile("^\\s*"
 			+ IntegerValue.REGEX + "\\s*$");
 
 	/**
 	 * Pattern for an interval. It matches two double or integer values, divided
 	 * by a semicolon (;) and enclosed by squared brackets. Spaces are allowed,
 	 * but not necessary, between the brackets and the digits, between the
 	 * digits and the semicolon as well as before and after the interval.
 	 * 
 	 * <p>
 	 * Examples for values are:
 	 * <ul>
 	 * <li>[1.2;3.4]
 	 * <li>[-1.2;3.4]
 	 * <li>[ 1.2;3.4]
 	 * <li>[1.2;3.4 ]
 	 * <li>[ 1.2 ; 3.4 ]
 	 * <li>[ 1 ; 2 ]
 	 * <li>[ 1.2 ;3 ]
 	 * <li>[&nbsp;&nbsp;1.2;3.4] (two spaces between bracket and digits)
 	 * <li>&nbsp;[ 1 ; 2 ]&nbsp; (spaces before and/or after the interval)
 	 * </ul>
 	 * 
	 * <p>
 	 * However, these values do not match:
 	 * <ul>
 	 * <li>(empty string)
 	 * <li>1.
 	 * <li>.1
 	 * <li>1.
 	 * <li>1.2
 	 * <li>abc
 	 * <li>a 1 b
 	 * <li>[;]
 	 * <li>[1.2;]
 	 * </ul>
 	 * 
 	 * @see DoubleValue#REGEX
 	 * @see IntegerValue#REGEX
 	 */
 	public static final Pattern INTERVAL_PATTERN = Pattern
 			.compile("^\\s*\\[\\s*(" + DoubleValue.REGEX + "|"
 					+ IntegerValue.REGEX + ")\\s*;\\s*(" + DoubleValue.REGEX
 					+ "|" + IntegerValue.REGEX + ")\\s*\\]\\s*$");
 
 	/**
 	 * Adds summand to the current Calculable.
 	 * 
 	 * @param summand
 	 *            the summand
 	 * 
 	 * @return the result as a new calculable instance
 	 */
 	public abstract Calculable add(Calculable summand);
 
 	/**
 	 * Subtracts the subtrahend from the current Calculable.
 	 * 
 	 * @param subtrahend
 	 *            the subtrahend
 	 * 
 	 * @return the result as a new calculable instance
 	 */
 	public abstract Calculable sub(Calculable subtrahend);
 
 	/**
 	 * Divides the current Calculable through the divisor.
 	 * 
 	 * @param divisor
 	 *            the divisor
 	 * 
 	 * @return the result as a new Calculable instance
 	 */
 	public abstract Calculable div(Calculable divisor);
 
 	/**
 	 * Multiplies the current Calculable with the multiplicand.
 	 * 
 	 * @param multiplicand
 	 *            the multiplicand
 	 * 
 	 * @return the result as a new Calculable instance
 	 */
 	public abstract Calculable mul(Calculable multiplicand);
 
 	/**
 	 * Returns the square root of the current Calculable.
 	 * 
 	 * @return the result as a new Calculable instance
 	 */
 	public abstract Calculable sqrt();
 
 	/**
 	 * Returns the power of the exponent of the current Calculable.
 	 * 
 	 * @param exponent
 	 *            the exponent
 	 * 
 	 * @return the result as a new Calculable instance
 	 */
 	public abstract Calculable pow(Calculable exponent);
 
 	/* Specified by interface/super class. */
 	@Override
 	public abstract Calculable clone();
 
 	/**
 	 * Parses a given String to create a new Calculable instance
 	 * 
 	 * @param s
 	 *            the String
 	 * 
 	 * @return the Calculable
 	 */
 	public static Calculable parseCalculable(String s) {
 		if (INTEGER_PATTERN.matcher(s).matches()) {
 			return new IntegerValue(java.lang.Integer.parseInt(s));
 		} else if (DOUBLE_PATTERN.matcher(s).matches()) {
 			return new DoubleValue(java.lang.Double.parseDouble(s));
 		} else {
 			Matcher intervalMatcher = INTERVAL_PATTERN.matcher(s);
 			if (intervalMatcher.matches()) {
 				String min = intervalMatcher.group(1);
 				String max = intervalMatcher.group(2);
 				return new IntervalValue(Double.parseDouble(min), Double
 						.parseDouble(max));
 			}
 		}
 
 		throw new UnsupportedOperationException(
 				"unknown type for calculable,  has not been implemented yet");
 	}
 
 	/**
 	 * Calculates the sum of all parameters.
 	 * 
 	 * @param summands
 	 *            the summands
 	 * 
 	 * @return Sum of all parameters as new Calculable
 	 */
 	public Calculable add(Calculable... summands) {
 		Calculable result = this;
 		for (Calculable summand : summands) {
 			result = result.add(summand);
 		}
 		return result;
 	}
 
 	/**
 	 * Calculates the product of all parameters.
 	 * 
 	 * @param factors
 	 *            the factors
 	 * 
 	 * @return Product of all parameters as new Calculable
 	 */
 	public Calculable mul(Calculable... factors) {
 		Calculable result = this;
 		for (Calculable factor : factors) {
 			result = result.mul(factor);
 		}
 		return result;
 	}
 
 	/* Specified by interface/super class. */
 	@Override
 	public abstract String toString();
 }
