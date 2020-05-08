 package net.meisen.general.genmisc.types;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 
 /**
  * Helper methods to deal with numbers.
  */
 public class Numbers {
 
 	/**
 	 * Maps the specified value to the number specified by the {@code clazz}.
 	 * 
 	 * @param value
 	 *            the value to be mapped
 	 * @param clazz
 	 *            the class to map the specified value to
 	 * 
 	 * @return the mapped value or {@code null} if it cannot be mapped to the
 	 *         number
 	 */
 	@SuppressWarnings("unchecked")
 	public static <D> D mapToDataType(final Object value, final Class<D> clazz) {
 
 		if (value == null) {
 			return null;
 		}
 
 		if (clazz.equals(value.getClass())) {
 			return (D) value;
 		} else if (Number.class.isAssignableFrom(clazz)) {
 			final Class<? extends Number> numClazz = (Class<? extends Number>) clazz;
 			final Number number = (Number) value;
 			final Number result = Numbers.castToNumber(number, numClazz);
 
 			// check the result
 			if (result != null) {
 				final Class<?> srcClazz = number.getClass();
 				final Number cmpNumber = Numbers.castToNumber(result,
 						number.getClass());
 
 				if (cmpNumber.equals(number)) {
 					return (D) result;
 				}
 				/*
 				 * There is a problem with the BigDecimal the equality depends
 				 * on how it is created, i.e. using new BigDecimal(...) or
 				 * BigDecimal.valueOf(...). The castToNumber method uses the
 				 * valueOf, therefore here we check the constructor.
 				 */
 				else if (BigDecimal.class.equals(srcClazz)
 						&& new BigDecimal(result.doubleValue()).equals(number)) {
 					return (D) result;
 				}
 			}
 		}
 
 		// if we came so far there is no hope
 		return null;
 	}
 
 	/**
 	 * Method which maps a {@code number} to the specified {@code clazz}. The
 	 * specified {@code clazz} is another {@code Number}.
 	 * 
 	 * @param number
 	 *            the number to be casted
 	 * @param clazz
 	 *            the {@code Number}-class to cast the {@code number} to
 	 * 
 	 * @return the casted {@code number} or {@code null} if a cast wasn't
 	 *         possible
 	 */
 	public static Number castToNumber(final Number number,
 			final Class<? extends Number> clazz) {
 		final Number result;
 
 		if (number == null) {
 			return null;
 		} else if (number.getClass().equals(clazz)) {
 			return number;
 		} else if (Byte.class.equals(clazz)) {
 			result = number.byteValue();
 		} else if (Short.class.equals(clazz)) {
 			result = number.shortValue();
 		} else if (Integer.class.equals(clazz)) {
 			result = number.intValue();
 		} else if (Long.class.equals(clazz)) {
 			result = number.longValue();
 		} else if (Float.class.equals(clazz)) {
 			result = number.floatValue();
 		} else if (Double.class.equals(clazz)) {
 			result = number.doubleValue();
 		} else if (BigInteger.class.equals(clazz)) {
 			result = BigInteger.valueOf(number.longValue());
 		} else if (BigDecimal.class.equals(clazz)) {
 			result = BigDecimal.valueOf(number.doubleValue());
 		} else {
 			return null;
 		}
 
 		return result;
 	}
 
 	/**
 	 * Cast the value to an integer.
 	 * 
 	 * @param b
 	 *            the value to be casted
 	 * @return the result
 	 */
 	public static int castToInt(final byte b) {
 		return (int) b;
 	}
 
 	/**
 	 * Cast the value to an integer.
 	 * 
 	 * @param s
 	 *            the value to be casted
 	 * @return the result
 	 */
 	public static int castToInt(final short s) {
 		return (short) s;
 	}
 
 	/**
 	 * Cast the value to an integer.
 	 * 
 	 * @param l
 	 *            the value to be casted
 	 * @return the result
 	 * 
 	 * @throws ArithmeticException
 	 *             if the value doesn't fit into an integer
 	 */
 	public static int castToInt(final long l) {
 		if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
 			throw new ArithmeticException("Cannot convert the long value '" + l
 					+ "' to an integer.");
 		} else {
 			return (int) l;
 		}
 	}
 
 	/**
 	 * Cast the value to a short.
 	 * 
 	 * @param b
 	 *            the value to be casted
 	 * @return the result
 	 */
 	public static short castToShort(final byte b) {
 		return (short) b;
 	}
 
 	/**
 	 * Cast the value to a short.
 	 * 
 	 * @param i
 	 *            the value to be casted
 	 * @return the result
 	 * 
 	 * @throws ArithmeticException
 	 *             if the value doesn't fit into a short
 	 */
 	public static short castToShort(final int i) {
 		if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
 			throw new ArithmeticException("Cannot convert the integer value '"
 					+ i + "' to a short.");
 		} else {
 			return (short) i;
 		}
 	}
 
 	/**
 	 * Cast the value to a short.
 	 * 
 	 * @param l
 	 *            the value to be casted
 	 * @return the result
 	 * 
 	 * @throws ArithmeticException
 	 *             if the value doesn't fit into a short
 	 */
 	public static short castToShort(final long l) {
 		if (l < Short.MIN_VALUE || l > Short.MAX_VALUE) {
 			throw new ArithmeticException("Cannot convert the long value '" + l
 					+ "' to a short.");
 		} else {
 			return (short) l;
 		}
 	}
 
 	/**
 	 * Cast the value to a byte.
 	 * 
 	 * @param s
 	 *            the value to be casted
 	 * @return the result
 	 * 
 	 * @throws ArithmeticException
 	 *             if the value doesn't fit into a byte
 	 */
 	public static byte castToByte(final short s) {
 		if (s < Byte.MIN_VALUE || s > Byte.MAX_VALUE) {
 			throw new ArithmeticException("Cannot convert the short value '"
 					+ s + "' to a byte.");
 		} else {
 			return (byte) s;
 		}
 	}
 
 	/**
 	 * Cast the value to a byte.
 	 * 
 	 * @param i
 	 *            the value to be casted
 	 * @return the result
 	 * 
 	 * @throws ArithmeticException
 	 *             if the value doesn't fit into a byte
 	 */
 	public static byte castToByte(final int i) {
		if (i < Short.MIN_VALUE || i > Short.MAX_VALUE) {
 			throw new ArithmeticException("Cannot convert the integer value '"
 					+ i + "' to a byte.");
 		} else {
 			return (byte) i;
 		}
 	}
 
 	/**
 	 * Cast the value to a byte.
 	 * 
 	 * @param l
 	 *            the value to be casted
 	 * @return the result
 	 * 
 	 * @throws ArithmeticException
 	 *             if the value doesn't fit into a byte
 	 */
 	public static byte castToByte(final long l) {
		if (l < Short.MIN_VALUE || l > Short.MAX_VALUE) {
 			throw new ArithmeticException("Cannot convert the long value '" + l
 					+ "' to a byte.");
 		} else {
 			return (byte) l;
 		}
 	}
 }
