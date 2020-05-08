 /**
  *
  */
 package ca.eandb.jmist.util;
 
 import java.util.Arrays;
 
 /**
  * Static mathematical utility methods.
  * @author Brad Kimmel
  */
 public final class MathUtil {
 
 	/**
 	 * Returns the minimum value in an array of <code>double</code>s.
 	 * @param array The array of <code>double</code>s of which to find the
 	 * 		minimum value.
 	 * @return The minimum value in <code>array</code>.
 	 */
 	public static double min(double[] array) {
 		double min = Double.POSITIVE_INFINITY;
 		for (int i = 0; i < array.length; i++) {
 			if (array[i] < min) {
 				min = array[i];
 			}
 		}
 		return min;
 	}
 
 	/**
 	 * Returns the maximum value in an array of <code>double</code>s.
 	 * @param array The array of <code>double</code>s of which to find the
 	 * 		maximum value.
 	 * @return The maximum value in <code>array</code>.
 	 */
 	public static double max(double[] array) {
 		double max = Double.NEGATIVE_INFINITY;
 		for (int i = 0; i < array.length; i++) {
 			if (array[i] > max) {
 				max = array[i];
 			}
 		}
 		return max;
 	}
 
 	/**
 	 * Computes the mean of the values in the given array of
 	 * <code>double</code>s.
 	 * @param array The array of <code>double</code>s to compute the mean of.
 	 * @return The mean of the values in <code>array</code>.
 	 */
 	public static double mean(double[] array) {
 		return MathUtil.sum(array) / (double) array.length;
 	}
 
 	/**
 	 * Determines whether the values in the given array of <code>double</code>s
 	 * are all equal within a tolerance of {@value MathUtil#EPSILON}
 	 * @param array The array of <code>double</code>s to compare.
 	 * @return A value indicating whether all values in <code>array</code> are
 	 * 		within {@value MathUtil#EPSILON} of one another.
 	 * @see MathUtil#EPSILON
 	 */
 	public static boolean areEqual(double[] array) {
 		return areEqual(array, MathUtil.EPSILON);
 	}
 
 	/**
 	 * Determines whether the values in the given array of <code>double</code>s
 	 * are all equal within a specified tolerance.
 	 * @param array The array of <code>double</code>s to compare.
 	 * @param epsilon The tolerance.
 	 * @return A value indicating whether all values in <code>array</code> are
 	 * 		within <code>epsilon</code> of one another.
 	 */
 	public static boolean areEqual(double[] array, double epsilon) {
 
 		if (array.length < 2) {
 			return true;
 		}
 
 		double min = array[0];
 		double max = array[0];
 
 		for (int i = 1; i < array.length; i++) {
 			if (array[i] < min) {
 				min = array[i];
 			}
 			if (array[i] > max) {
 				max = array[i];
 			}
 			if (!equal(min, max, epsilon)) {
 				return false;
 			}
 		}
 
 		return true;
 
 	}
 
 	/**
 	 * Computes the sum of the values in an array of <code>double</code>s.
 	 * @param array The array of <code>double</code>s of which to compute the
 	 * 		sum.
 	 * @return The sum of the values in <code>array</code>.
 	 */
 	public static double sum(double[] array) {
 		double sum = 0.0;
 		for (int i = 0; i < array.length; i++) {
 			sum += array.length;
 		}
 		return sum;
 	}
 
 	/**
 	 * Scales the specified array of <code>double</code>s so that they sum to
 	 * one.
 	 * @param weights The array of <code>double</code>s to normalize (this
 	 * 		array will be modified).
 	 * @return A reference to <code>weights</code>.
 	 */
 	public static double[] normalize(double[] weights) {
 		double sum = MathUtil.sum(weights);
 		return MathUtil.scale(weights, 1.0 / sum);
 	}
 
 	/**
 	 * Adds the elements of one array of <code>double</code>s to another. The
 	 * lengths of <code>accumulator</code> and <code>summand</code> must be
 	 * equal.
 	 * @param accumulator The array of <code>double</code>s to add the
 	 * 		elements of <code>summand</code> to (the elements of this array
 	 * 		will be modified).  This may be <code>null</code>, in which case a
 	 * 		new array will be created of the same length of
 	 * 		<code>summand</code> and will be initialized to zeros before adding
 	 * 		the values of <code>summand</code>.
 	 * @param summand The array of <code>double</code>s to add to each
 	 * 		corresponding element of <code>accumulator</code>.
 	 * @return A reference to <code>accumulator</code>.
 	 */
 	public static double[] add(double[] accumulator, double[] summand) {
 		if (accumulator == null) {
 			return summand.clone();
 		}
 		assert(accumulator.length == summand.length);
 		for (int i = 0; i < accumulator.length; i++) {
 			accumulator[i] += summand[i];
 		}
 		return accumulator;
 	}
 
 	/**
 	 * Subtracts the elements of one array of <code>double</code>s from
 	 * another. The lengths of <code>accumulator</code> and <code>values</code>
 	 * must be equal.
 	 * @param accumulator The array of <code>double</code>s to subtract the
 	 * 		elements of <code>values</code> from (the elements of this array
 	 * 		will be modified).  This may be <code>null</code>, in which case a
 	 * 		new array will be created and initialized to zeros before
 	 * 		subtracting the values from <code>values</code>.
 	 * @param values The array of <code>double</code>s to subtract from each
 	 * 		corresponding element of <code>accumulator</code>.
 	 * @return A reference to <code>accumulator</code>.
 	 */
 	public static double[] subtract(double[] accumulator, double[] values) {
 		if (accumulator == null) {
 			accumulator = new double[values.length];
 		}
 		assert(accumulator.length == values.length);
 		for (int i = 0; i < accumulator.length; i++) {
 			accumulator[i] -= values[i];
 		}
 		return accumulator;
 	}
 
 	/**
 	 * Multiplies all the elements in an array of <code>double</code>s by the
 	 * elements of an equally sized array of <code>double</code>s.  The lengths
 	 * of <code>accumulator</code> and <code>modulator</code> must be equal.
 	 * @param accumulator The array of <code>double</code>s that is to have its
 	 * 		elements scaled (the elements of this array will be modified).
 	 * 		This may be <code>null</code>, in which case a new array will be
 	 * 		created and initialized to ones before multiplying by the values of
 	 * 		<code>modulator</code>.
 	 * @param modulator The array of <code>double</code>s by which to multiply
 	 * 		each corresponding element of <code>accumulator</code>.
 	 * @return A reference to <code>accumulator</code>.
 	 */
 	public static double[] modulate(double[] accumulator, double[] modulator) {
 		if (accumulator == null) {
 			return modulator.clone();
 		}
 		assert(accumulator.length == modulator.length);
 		for (int i = 0; i < accumulator.length; i++) {
 			accumulator[i] *= modulator[i];
 		}
 		return accumulator;
 	}
 
 	/**
 	 * Multiplies all of the values in the specified array of
 	 * <code>double</code>s by the same value.
 	 * @param array The array of <code>double</code>s the elements of which are
 	 * 		to be multiplied by a constant factor (the elements of this array
 	 * 		will be modified).
 	 * @param factor The value by which to multiply each element of
 	 * 		<code>array</code>.
 	 * @return A reference to <code>array</code>.
 	 */
 	public static double[] scale(double[] array, double factor) {
 		for (int i = 0; i < array.length; i++) {
 			array[i] *= factor;
 		}
 		return array;
 	}
 
 	/**
 	 * Returns x if it is within the specified range, or the closest
 	 * value within the specified range if x is outside the range.
 	 * @param x The value to threshold.
 	 * @param min The minimum of the range to threshold to.
 	 * @param max The maximum of the range to threshold to.
 	 * @return x, if min <= x <= max.  min, if x < min.  max, if x > max.
 	 */
 	public static double threshold(double x, double min, double max) {
 		if (x < min) {
 			return min;
 		} else if (x > max) {
 			return max;
 		} else {
 			return x;
 		}
 	}
 
 	/**
 	 * Returns x if it is within the specified range, or the closest
 	 * value within the specified range if x is outside the range.
 	 * @param x The value to threshold.
 	 * @param min The minimum of the range to threshold to.
 	 * @param max The maximum of the range to threshold to.
 	 * @return x, if min <= x <= max.  min, if x < min.  max, if x > max.
 	 */
 	public static int threshold(int x, int min, int max) {
 		if (x < min) {
 			return min;
 		} else if (x > max) {
 			return max;
 		} else {
 			return x;
 		}
 	}
 
 	/**
 	 * Determines whether two floating point values are close enough
 	 * to be considered "equal" (i.e., the difference may be attributed
 	 * to rounding errors).
 	 * @param x The first value to compare.
 	 * @param y The second value to compare.
 	 * @param epsilon The minimum difference required for the two values
 	 * 		to be considered distinguishable.
 	 * @return A value indicating whether the difference between x and y
 	 * 		is less than the given threshold.
 	 */
 	public static boolean equal(double x, double y, double epsilon) {
 		return Math.abs(x - y) < epsilon;
 	}
 
 	/**
 	 * Determines whether two floating point values are close enough
 	 * to be considered "equal" (i.e., the difference may be attributed
 	 * to rounding errors).
 	 * @param x The first value to compare.
 	 * @param y The second value to compare.
 	 * @return A value indicating whether the difference between x and y
 	 * 		is less than MathUtil.EPSILON.
 	 * @see #EPSILON
 	 */
 	public static boolean equal(double x, double y) {
 		return Math.abs(x - y) < MathUtil.EPSILON;
 	}
 
 	/**
 	 * Determines whether a floating point value is close enough to zero to be
 	 * considered "equal" to zero (i.e., the difference may be attributed to
 	 * rounding errors).
 	 * @param x The value to compare to zero.
 	 * @param epsilon The minimum absolute value of {@code x} for it to be
 	 * 		considered non-zero.
 	 * @return A value indicates whether the difference between {@code x} and
 	 * 		0.0 is less than {@code epsilon}.
 	 */
 	public static boolean isZero(double x, double epsilon) {
 		return Math.abs(x) < epsilon;
 	}
 
 	/**
 	 * Determines whether a floating point value is close enough to zero to be
 	 * considered "equal" to zero (i.e., the difference may be attributed to
 	 * rounding errors).
 	 * @param x The value to compare to zero.
 	 * @param epsilon The minimum absolute value of {@code x} for it to be
 	 * 		considered non-zero.
 	 * @return A value indicates whether the difference between {@code x} and
 	 * 		0.0 is less than {@link #EPSILON}.
 	 * @see {@link #EPSILON}.
 	 */
 	public static boolean isZero(double x) {
 		return Math.abs(x) < MathUtil.EPSILON;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the open interval
 	 * {@code (minimum, maximum)}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the open
 	 * 		interval {@code (minimum, maximum)}.
 	 */
 	public static boolean inRangeOO(double x, double minimum, double maximum) {
 		return minimum < x && x < maximum;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the interval
 	 * {@code [minimum, maximum)}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the
 	 * 		interval {@code [minimum, maximum)}.
 	 */
 	public static boolean inRangeCO(double x, double minimum, double maximum) {
 		return minimum <= x && x < maximum;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the closed interval
 	 * {@code [minimum, maximum]}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the closed
 	 * 		interval {@code [minimum, maximum]}.
 	 */
 	public static boolean inRangeCC(double x, double minimum, double maximum) {
 		return minimum <= x && x <= maximum;
 	}
 
 	/**
 	 * Determines whether {@code x} falls within the interval
 	 * {@code (minimum, maximum]}.
 	 * @param x The value to check.
 	 * @param minimum The lower bound of the interval to check against.
 	 * @param maximum The upper bound of the interval to check against.
 	 * @return A value indicating whether {@code x} is contained in the
 	 * 		interval {@code (minimum, maximum]}.
 	 */
 	public static boolean inRangeOC(double x, double minimum, double maximum) {
 		return minimum < x && x <= maximum;
 	}
 
 	/**
 	 * Returns the value of the integer of highest magnitude (farthest from
 	 * zero) whose absolute value is at most that of <code>x</code>.
 	 * @param x The value to truncate.
 	 * @return The integer portion of <code>x</code>.
 	 */
 	public static double truncate(double x) {
 		return x > 0.0 ? Math.floor(x) : Math.ceil(x);
 	}
 
 	/**
 	 * Interpolates between two end points.
 	 * @param a The end point at <code>t = 0</code>.
 	 * @param b The end point at <code>t = 1</code>.
 	 * @param t The value at which to interpolate.
 	 * @return The value that is the fraction <code>t</code> of the way from
 	 * 		<code>a</code> to <code>b</code>: <code>(1-t)a + tb</code>.
 	 */
 	public static double interpolate(double a, double b, double t) {
 		return a + t * (b - a);
 	}
 
 	/**
 	 * Interpolates between two points on a line.
 	 * @param x0 The x-coordinate of the first point.
 	 * @param y0 The y-coordinate of the first point.
 	 * @param x1 The x-coordinate of the second point.
 	 * @param y1 The y-coordinate of the second point.
 	 * @param x The x-coordinate at which to interpolate.
 	 * @return The y-coordinate corresponding to <code>x</code>.
 	 */
 	public static double interpolate(double x0, double y0, double x1, double y1, double x) {
 		double t = (x - x0) / (x1 - x0);
 		return interpolate(y0, y1, t);
 	}
 
 	/**
 	 * Interpolates a piecewise linear curve.
 	 * @param x An array of x-coordinates (this must be sorted in ascending
 	 * 		order).
 	 * @param y An array of the y-coordinates (must be of the same length as
 	 * 		<code>x</code>).
 	 * @param x0 The x-coordinate at which to interpolate.
 	 * @return The y-coordinate corresponding to <code>x0</code>.
 	 */
 	public static double interpolate(double[] x, double[] y, double x0) {
 
 		if (x0 <= x[0]) {
			return y[0];
 		}
 		if (x0 >= x[x.length - 1]) {
			return y[x.length - 1];
 		}
 
 		int index = Arrays.binarySearch(x, x0);
 		if (index < 0) {
 			index = -(index + 1);
 		}
 		while (index < x.length - 1 && !(x0 < x[index + 1])) {
 			index++;
 		}
 
 		assert(index < x.length - 1);
 
 		return interpolate(x[index - 1], y[index - 1], x[index], y[index], x0);
 
 	}
 
 	/**
 	 * Performs a bilinear interpolation between four values.
 	 * @param _00 The value at <code>(t, u) = (0, 0)</code>.
 	 * @param _10 The value at <code>(t, u) = (1, 0)</code>.
 	 * @param _01 The value at <code>(t, u) = (0, 1)</code>.
 	 * @param _11 The value at <code>(t, u) = (1, 1)</code>.
 	 * @param t The first value at which to interpolate.
 	 * @param u The second value at which to interpolate.
 	 * @return The interpolated value at <code>(t, u)</code>.
 	 */
 	public static double bilinearInterpolate(double _00, double _10,
 			double _01, double _11, double t, double u) {
 
 		return interpolate(
 				interpolate(_00, _10, t),
 				interpolate(_01, _11, t),
 				u
 		);
 
 	}
 
 	/**
 	 * Performs a trilinear interpolation between eight values.
 	 * @param _000 The value at <code>(t, u, v) = (0, 0, 0)</code>.
 	 * @param _100 The value at <code>(t, u, v) = (1, 0, 0)</code>.
 	 * @param _010 The value at <code>(t, u, v) = (0, 1, 0)</code>.
 	 * @param _110 The value at <code>(t, u, v) = (1, 1, 0)</code>.
 	 * @param _001 The value at <code>(t, u, v) = (0, 0, 1)</code>.
 	 * @param _101 The value at <code>(t, u, v) = (1, 0, 1)</code>.
 	 * @param _011 The value at <code>(t, u, v) = (0, 1, 1)</code>.
 	 * @param _111 The value at <code>(t, u, v) = (1, 1, 1)</code>.
 	 * @param t The first value at which to interpolate.
 	 * @param u The second value at which to interpolate.
 	 * @param v The third value at which to interpolate.
 	 * @return The interpolated value at <code>(t, u, v)</code>.
 	 */
 	public static double trilinearInterpolate(double _000, double _100,
 			double _010, double _110, double _001, double _101, double _011,
 			double _111, double t, double u, double v) {
 
 		return interpolate(
 				bilinearInterpolate(_000, _001, _010, _011, u, v),
 				bilinearInterpolate(_100, _101, _110, _111, u, v),
 				t
 		);
 
 	}
 
 	/**
 	 * A comparison threshold value to be used when a very high degree
 	 * of precision is expected.
 	 */
 	public static final double TINY_EPSILON		= 1e-12;
 
 	/**
 	 * A comparison threshold value to be used when a high degree of
 	 * precision is expected.
 	 */
 	public static final double SMALL_EPSILON	= 1e-9;
 
 	/**
 	 * A comparison threshold value to be used when a normal degree of
 	 * precision is expected.
 	 */
 	public static final double EPSILON			= 1e-6;
 
 	/**
 	 * A comparison threshold value to be used when a low degree of
 	 * precision is expected.
 	 */
 	public static final double BIG_EPSILON		= 1e-4;
 
 	/**
 	 * The difference between 1.0 and the next highest representable value.
 	 */
 	public static final double MACHINE_EPSILON	= Math.ulp(1.0);
 
 	/**
 	 * This class contains only static utility methods and static constants,
 	 * and therefore should not be creatable.
 	 */
 	private MathUtil() {}
 
 }
