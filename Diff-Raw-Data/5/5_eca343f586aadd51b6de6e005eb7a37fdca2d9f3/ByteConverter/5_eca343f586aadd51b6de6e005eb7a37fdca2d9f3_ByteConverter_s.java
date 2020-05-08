 package com.gunnarhoffman.converters;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.security.InvalidParameterException;
 
 /**
  * This class aides in conversion between all the various byte and bit
  * increments. The highest understood value for this class is a Terabyte. While
  * it supports values well into the range of Petabytes all sizes above a
  * Terabyte will be reported as a number Terabytes. Negative values are not
  * supported except for use with the add* methods assuming it does not produce a
  * completely negative value for the current byte sum.
  * 
  * @author Gunnar Hoffman
  * 
  */
 public class ByteConverter {
 
 	// Constants
 
 	private static final BigDecimal tenTwentyFourToTheFourth = new BigDecimal(
 			"1099511627776");
 	private static final BigDecimal tenTwentyFourToTheThird = new BigDecimal(
 			"1073741824");
 	private static final BigDecimal tenTwentyFourToTheSecond = new BigDecimal(
 			"1048576");
 	private static final BigDecimal tenTwentyFourToTheFirst = new BigDecimal(
 			"1024");
 	private static final BigDecimal eight = new BigDecimal("8");
 
 	// Instance fields
 
 	private BigDecimal bytes;
 
 	// Constructors
 
 	private ByteConverter(BigDecimal bytes) {
 		this.setBytes(bytes);
 	}
 
 	/**
 	 * Sets the raw byte value in the underlying data source for this class.
 	 * 
 	 * This method is final to protect the constructor from undefined behavior
 	 * should this class be extended.
 	 * 
 	 * In the event a negative number is passed into this function a runtime
 	 * exception of the type InvalidParameterException will be thrown. All
	 * static methods on this object indirectly call this method and therfor can
 	 * also throw this exception.
 	 * 
 	 * @param number
	 *            The number of bytes to opperate over.
 	 * @return An instance of this class containing the byte number.
 	 */
 	public final ByteConverter setBytes(BigDecimal number) {
 		if (number.signum() == -1) {
 			throw new InvalidParameterException(
 					"negative bytes makes no sense!");
 		}
 		this.bytes = number;
 		return this;
 	}
 
 	// Byte initializers
 
 	public static ByteConverter fromTerabytes(double terabytes) {
 
 		return ByteConverter.fromTerabytes(new BigDecimal(
 				Double.toString(terabytes)));
 	}
 
 	public static ByteConverter fromTerabytes(BigDecimal terabytes) {
 
 		BigDecimal calculator = new BigDecimal(terabytes.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheFourth);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromGigabytes(double gigabytes) {
 
 		return ByteConverter.fromGigabytes(new BigDecimal(
 				Double.toString(gigabytes)));
 	}
 
 	public static ByteConverter fromGigabytes(BigDecimal gigabytes) {
 
 		BigDecimal calculator = new BigDecimal(gigabytes.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheThird);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromMegabytes(double megabytes) {
 
 		return ByteConverter.fromMegabytes(new BigDecimal(
 				Double.toString(megabytes)));
 	}
 
 	public static ByteConverter fromMegabytes(BigDecimal megabytes) {
 
 		BigDecimal calculator = new BigDecimal(megabytes.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheSecond);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromKilobytes(double kilobytes) {
 
 		return ByteConverter.fromKilobytes(new BigDecimal(
 				Double.toString(kilobytes)));
 	}
 
 	public static ByteConverter fromKilobytes(BigDecimal kilobytes) {
 
 		BigDecimal calculator = new BigDecimal(kilobytes.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheFirst);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromBytes(long bytes) {
 		return ByteConverter.fromBytes(new BigDecimal(Long.toString(bytes)));
 	}
 
 	public static ByteConverter fromBytes(BigDecimal bytes) {
 		return new ByteConverter(new BigDecimal(bytes.toString()));
 	}
 
 	// Bit initializers
 
 	public static ByteConverter fromTerabits(double terabits) {
 
 		return ByteConverter.fromTerabits(new BigDecimal(
 				Double.toString(terabits)));
 	}
 
 	public static ByteConverter fromTerabits(BigDecimal terabits) {
 
 		BigDecimal calculator = new BigDecimal(terabits.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheFourth);
 		calculator = calculator.divide(ByteConverter.eight);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromGigabits(double gigabits) {
 
 		return ByteConverter.fromGigabits(new BigDecimal(
 				Double.toString(gigabits)));
 	}
 
 	public static ByteConverter fromGigabits(BigDecimal gigabits) {
 
 		BigDecimal calculator = new BigDecimal(gigabits.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheThird);
 		calculator = calculator.divide(ByteConverter.eight);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromMegabits(double megabits) {
 
 		return ByteConverter.fromMegabits(new BigDecimal(
 				Double.toString(megabits)));
 	}
 
 	public static ByteConverter fromMegabits(BigDecimal megabits) {
 
 		BigDecimal calculator = new BigDecimal(megabits.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheSecond);
 		calculator = calculator.divide(ByteConverter.eight);
 		return new ByteConverter(calculator);
 	}
 
 	public static ByteConverter fromKilobits(double kilobits) {
 		return ByteConverter.fromKilobits(new BigDecimal(
 				Double.toString(kilobits)));
 	}
 
 	public static ByteConverter fromKilobits(BigDecimal kilobits) {
 
 		BigDecimal calculator = new BigDecimal(kilobits.toString());
 		calculator = calculator.multiply(ByteConverter.tenTwentyFourToTheFirst);
 		calculator = calculator.divide(ByteConverter.eight);
 		return new ByteConverter(calculator);
 	}
 
 	// Addition and (via the use of negative numbers) subtraction
 
 	public ByteConverter addBytes(long bytes) {
 		this.setBytes(this.bytes.add(new BigDecimal(bytes)));
 		return this;
 	}
 
 	public ByteConverter addKilobytes(double kilobytes) {
 		this.setBytes(this.bytes.add(new BigDecimal(kilobytes).multiply(ByteConverter.tenTwentyFourToTheFirst)));
 		return this;
 	}
 
 	public ByteConverter addMegabytes(double megabytes) {
 		this.setBytes(this.bytes.add(new BigDecimal(megabytes).multiply(ByteConverter.tenTwentyFourToTheSecond)));
 		return this;
 	}
 
 	public ByteConverter addGigabytes(double gigabytes) {
 		this.setBytes(this.bytes.add(new BigDecimal(gigabytes).multiply(ByteConverter.tenTwentyFourToTheThird)));
 		return this;
 	}
 
 	public ByteConverter addTerabytes(double terabytes) {
 		this.setBytes(this.bytes.add(new BigDecimal(terabytes).multiply(ByteConverter.tenTwentyFourToTheFourth)));
 		return this;
 	}
 
 	// Output as bytes
 
 	public BigInteger toBytes() {
 		return this.bytes.toBigInteger();
 	}
 
 	public BigDecimal toKilobytes() {
 		return this.bytes.divide(ByteConverter.tenTwentyFourToTheFirst);
 	}
 
 	public BigDecimal toMegabytes() {
 		return this.bytes.divide(ByteConverter.tenTwentyFourToTheSecond);
 	}
 
 	public BigDecimal toGigabytes() {
 		return this.bytes.divide(ByteConverter.tenTwentyFourToTheThird);
 	}
 
 	public BigDecimal toTerabytes() {
 		return this.bytes.divide(ByteConverter.tenTwentyFourToTheFourth);
 	}
 
 	// Output as bits
 
 	public BigDecimal toKilobits() {
 		return this.toKilobytes().divide(ByteConverter.eight);
 	}
 
 	public BigDecimal toMegabits() {
 		return this.toMegabytes().divide(ByteConverter.eight);
 	}
 
 	public BigDecimal toGigabits() {
 		return this.toGigabytes().divide(ByteConverter.eight);
 	}
 
 	public BigDecimal toTerabits() {
 		return this.toTerabytes().divide(ByteConverter.eight);
 	}
 
 	// Output as a String
 
 	@Override
 	public String toString() {
 
 		if (this.bytes.compareTo(ByteConverter.tenTwentyFourToTheFourth) >= 0) {
 			return String.format("%.2f TB", this.toTerabytes().doubleValue());
 
 		} else if (this.bytes.compareTo(ByteConverter.tenTwentyFourToTheThird) >= 0) {
 			return String.format("%.2f GB", this.toGigabytes().doubleValue());
 
 		} else if (this.bytes.compareTo(ByteConverter.tenTwentyFourToTheSecond) >= 0) {
 			return String.format("%.2f MB", this.toMegabytes().doubleValue());
 
 		} else if (this.bytes.compareTo(ByteConverter.tenTwentyFourToTheFirst) >= 0) {
 			return String.format("%.2f KB", this.toKilobytes().doubleValue());
 
 		} else {
 			return String.format("%d B", this.bytes.intValue());
 		}
 	}
 }
