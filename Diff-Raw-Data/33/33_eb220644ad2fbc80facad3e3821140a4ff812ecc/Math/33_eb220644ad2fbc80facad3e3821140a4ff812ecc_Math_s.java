 /*
  * VLCMath.java
  *
  *
  */
 package org.smartly.packages.velocity.impl.vtools;
 
 import org.smartly.commons.util.ConversionUtils;
 import org.smartly.commons.util.MathUtils;
 import org.smartly.commons.util.RandomUtils;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Tool utility for basic arithmetic operations.
  */
 public class Math
         implements IVLCTool {
 
     public static final String NAME = "math";
 
     /**
      * Creates a new instance of VLCMath
      */
     public Math() {
 
     }
 
     @Override
     public String getName() {
         return NAME;
     }
 
     public Number inc(final Object num){
        return this.sum(num, 1);
     }
 
     public Number dec(final Object num){
         return this.sub(num,1);
     }
 
     /**
      * Sum a list of numbers in different formats (String, integer, BigDecimal, Decimal, etc..)
      *
      * @param numbers List of numbers to sum
      * @return BigDecimal
      */
     public Number sum(List<Object> numbers) {
         BigDecimal result = BigDecimal.ZERO;
         for (Object number : numbers) {
             BigDecimal value;
             if (null != number) {
                 value = ConversionUtils.toBigDecimal(number);
             } else {
                 value = BigDecimal.ZERO;
             }
 
             result = result.add(value);
         }
 
        return result;
     }
 
    public BigDecimal sum(final Object... numbers) {
        BigDecimal result = BigDecimal.ZERO;
        for (Object number : numbers) {
            BigDecimal value;
            if (null != number) {
                value = ConversionUtils.toBigDecimal(number);
            } else {
                value = BigDecimal.ZERO;
            }

            result = result.add(value);
        }
        return result;
     }
 
    public double divide(final Object dividend, final Object divisor) {
         final BigDecimal num1 = ConversionUtils.toBigDecimal(dividend);
         final BigDecimal num2 = ConversionUtils.toBigDecimal(divisor);
         final double result = num1.doubleValue() / num2.doubleValue();
 
         return result;
     }
 
    public int divideIntValue(final Object dividend, final Object divisor) {
         final double result = this.divide(dividend, divisor);
         return this.intValue(result);
     }
 
    public int divideRoundCeil(final Object dividend, final Object divisor) {
         final double result = this.divide(dividend, divisor);
         return this.intValue(this.roundCeil(result));
     }
 
    public BigDecimal multiply(final Object dividend, final Object divisor) {
         final BigDecimal num1 = ConversionUtils.toBigDecimal(dividend);
         final BigDecimal num2 = ConversionUtils.toBigDecimal(divisor);
        return num1.multiply(num2);
     }
 
     // --------------------------------------------------------------------
     //               R A N D O M
     // --------------------------------------------------------------------
 
     public Integer randomInt(final int digits) {
         return RandomUtils.getTimeBasedRandomInteger(digits);
     }
 
     /**
      * <p>Creates a random string whose length is the number of characters
      * specified.</p>
      * <p/>
      * <p>Characters will be chosen from the set of alpha-numeric
      * characters as indicated by the arguments.</p>
      *
      * @param digits the length of random string to create
      * @return the random string
      */
     public String randomString(final int digits) {
         return RandomUtils.random(digits, true, true);
     }
 
     /**
      * <p>Creates a random string whose length is the number of characters
      * specified.</p>
      * <p/>
      * <p>Characters will be chosen from the set of characters whose
      * ASCII value is between <code>32</code> and <code>126</code> (inclusive).</p>
      *
      * @param digits the length of random string to create
      * @return the random string
      */
     public String randomAscii(final int digits) {
         return RandomUtils.randomAscii(digits);
     }
 
     /**
      * Return a random number with a limit range.
      *
      * @param min
      * @param max
      * @return
      */
     public double random(final double min, final double max) {
         return RandomUtils.rnd(min, max);
     }
 
     /**
      * @return a pseudo-random {@link Double} greater
      *         than or equal to 0.0 and less than 1.0
      * @see java.lang.Math#random()
      */
     public Double random() {
         return new Double(java.lang.Math.random());
     }
 
 
     /**
      * This returns a random {@link Number} within the
      * specified range.  The returned value will be
      * greater than or equal to the first number
      * and less than the second number.  If both arguments
      * are whole numbers then the returned number will
      * also be, otherwise a {@link Double} will
      * be returned.
      *
      * @param num1 the first number
      * @param num2 the second number
      * @return a pseudo-random {@link Number} greater than
      *         or equal to the first number and less than
      *         the second
      * @see java.lang.Math#random()
      */
     public Number random(Object num1, Object num2) {
         return RandomUtils.rnd(num1, num2);
     }
 
     // --------------------------------------------------------------------
     //               R O U N D
     // --------------------------------------------------------------------
 
     /**
      * Round a number mantainig desired number of decimal digits.<br>
      * i.e. round(1.234, 2) = 1.23<br>
      * i.e. round(1.236, 2) = 1.24
      *
      * @param value
      * @param decimals
      * @return
      */
     public double round(final double value, final int decimals) {
         return MathUtils.round(value, decimals);
     }
 
     /**
      * Round ceil a number, mantaining desired decimals.
      * i.e. roundCeil(1.121, 2) = 1.13
      *
      * @param value    the value
      * @param decimals number of decimals digits
      * @return rounded value
      */
     public double roundCeil(final double value, final int decimals) {
         return MathUtils.roundCeil(value, decimals);
     }
 
     /**
      * Round ceil a number, mantaining desired decimals.
      * i.e. roundCeil(1.121, 2) = 1.13
      *
      * @param value    the value
      * @param decimals number of decimals digits
      * @return rounded value
      */
     public double roundCeil(final Object value, final int decimals) {
         final BigDecimal number = ConversionUtils.toBigDecimal(value);
         return this.roundCeil(number.doubleValue(), decimals);
     }
 
     public double roundCeil(final Object value) {
         final BigDecimal number = ConversionUtils.toBigDecimal(value);
         return this.roundCeil(number.doubleValue(), 0);
     }
 
     /**
      * Round floor a number, mantaining desired decimals.
      * i.e. roundFloor(1.129, 2) = 1.12
      *
      * @param value    the value
      * @param decimals number of decimals digits
      * @return rounded value
      */
     public double roundFloor(final double value, final int decimals) {
         return MathUtils.roundFloor(value, decimals);
     }
 
     /**
      * Return integer part of a number
      *
      * @param value
      * @return
      */
     public int intValue(final Object value) {
         final BigDecimal number = ConversionUtils.toBigDecimal(value);
         return number.intValue();
     }
 
     /**
      * @param nums the numbers to be added
      * @return the sum of the numbers or
      *         <code>null</code> if they're invalid
      * @see MathUtils#add(Object...)
      */
     public Number add(Object... nums) {
         return MathUtils.add(nums);
     }
 
 
     /**
      * @param nums the numbers to be subtracted
      * @return the difference of the numbers (subtracted in order) or
      *         <code>null</code> if they're invalid
      * @see MathUtils#sub(Object...)
      */
     public Number sub(Object... nums) {
         return MathUtils.sub(nums);
     }
 
 
     /**
      * @param nums the numbers to be multiplied
      * @return the product of the numbers or
      *         <code>null</code> if they're invalid
      * @see MathUtils#mul(Object...)
      */
     public Number mul(Object... nums) {
         return MathUtils.mul(nums);
     }
 
 
     /**
      * @param nums the numbers to be divided
      * @return the quotient of the numbers or
      *         <code>null</code> if they're invalid
      *         or if any denominator equals zero
      * @see MathUtils#div(Object...)
      */
     public Number div(Object... nums) {
         return MathUtils.div(nums);
     }
 
 
     /**
      * @param num1 the first number
      * @param num2 the second number
      * @return the first number raised to the power of the
      *         second or <code>null</code> if they're invalid
      * @see MathUtils#pow(Object, Object)
      */
     public Number pow(Object num1, Object num2) {
         return MathUtils.pow(num1, num2);
     }
 
 
     /**
      * Does integer division on the int values of the specified numbers.
      * <p/>
      * <p>So, $math.idiv('5.1',3) will return '1',
      * and $math.idiv(6,'3.9') will return '2'.</p>
      *
      * @param num1 the first number
      * @param num2 the second number
      * @return the result of performing integer division
      *         on the operands.
      * @see MathUtils#idiv(Object, Object)
      */
     public Integer idiv(Object num1, Object num2) {
         return MathUtils.idiv(num1, num2);
     }
 
 
     /**
      * Does integer modulus on the int values of the specified numbers.
      * <p/>
      * <p>So, $math.mod('5.1',3) will return '2',
      * and $math.mod(6,'3.9') will return '0'.</p>
      *
      * @param num1 the first number
      * @param num2 the second number
      * @return the result of performing integer modulus
      *         on the operands.
      * @see MathUtils#mod(Object, Object)
      */
     public Integer mod(Object num1, Object num2) {
         return MathUtils.mod(num1, num2);
     }
 
 
     /**
      * @param nums the numbers to be searched
      * @return the largest of the numbers or
      *         <code>null</code> if they're invalid
      * @see MathUtils#max(int...)
      */
     public Number max(Object... nums) {
         return MathUtils.max(nums);
     }
 
 
     /**
      * @param nums the numbers to be searched
      * @return the smallest of the numbers or
      *         <code>null</code> if they're invalid
      * @see MathUtils#min(Object...)
      */
     public Number min(Object... nums) {
         return MathUtils.min(nums);
     }
 
 
     /**
      * @param num the number
      * @return the absolute value of the number or
      *         <code>null</code> if it's invalid
      * @see MathUtils#abs(Object)
      */
     public Number abs(Object num) {
         return MathUtils.abs(num);
     }
 
     /**
      * @param num the number
      * @return the smallest integer that is not
      *         less than the given number
      * @see MathUtils#ceil(Object)
      */
     public Integer ceil(Object num) {
         return MathUtils.ceil(num);
     }
 
 
     /**
      * @param num the number
      * @return the integer portion of the number
      * @see MathUtils#floor(Object)
      */
     public Integer floor(final Object num) {
         return MathUtils.floor(num);
     }
 
 
     /**
      * Rounds a number to the nearest whole Integer
      *
      * @param num the number to round
      * @return the number rounded to the nearest whole Integer
      *         or <code>null</code> if it's invalid
      * @see MathUtils#round(Object)
      */
     public Integer round(final Object num) {
         return MathUtils.round(num);
     }
 
     /**
      * Rounds a number to the specified number of decimal places.
      * This is particulary useful for simple display formatting.
      * If you want to round an number to the nearest integer, it
      * is better to use {@link #round}, as that will return
      * an {@link Integer} rather than a {@link Double}.
      *
      * @param decimals the number of decimal places
      * @param num      the number to round
      * @return the value rounded to the specified number of
      *         decimal places or <code>null</code> if it's invalid
      * @see MathUtils#roundTo(Object, Object)
      */
     public Double roundTo(Object num, Object decimals) {
         return MathUtils.roundTo(num, decimals);
     }
 
     // ------------------------- Aggregation methods ------------------
 
     /**
      * Get the sum of the values from a list
      *
      * @param collection A collection containing Java beans
      * @param field      A Java Bean field for the objects in <i>collection</i> that
      *                   will return a number.
      * @return The sum of the values in <i>collection</i>.
      */
     public Number getTotal(Collection collection, String field) {
         return MathUtils.getTotal(collection, field);
     }
 
     /**
      * Get the average of the values from a list
      *
      * @param collection A collection containing Java beans
      * @param field      A Java Bean field for the objects in <i>collection</i> that
      *                   will return a number.
      * @return The average of the values in <i>collection</i>.
      */
     public Number getAverage(Collection collection, String field) {
         return MathUtils.getAverage(collection, field);
     }
 
     /**
      * Get the sum of the values from a list
      *
      * @param array An array containing Java beans
      * @param field A Java Bean field for the objects in <i>array</i> that
      *              will return a number.
      * @return The sum of the values in <i>array</i>.
      */
     public Number getTotal(Object[] array, String field) {
         return MathUtils.getTotal(array, field);
     }
 
     /**
      * Get the sum of the values from a list
      *
      * @param array A collection containing Java beans
      * @param field A Java Bean field for the objects in <i>array</i> that
      *              will return a number.
      * @return The sum of the values in <i>array</i>.
      */
     public Number getAverage(Object[] array, String field) {
         return MathUtils.getAverage(array, field);
     }
 
     /**
      * Get the sum of the values
      *
      * @param collection A collection containing numeric values
      * @return The sum of the values in <i>collection</i>.
      */
     public Number getTotal(Collection collection) {
         return MathUtils.getTotal(collection);
     }
 
     /**
      * Get the average of the values
      *
      * @param collection A collection containing number values
      * @return The average of the values in <i>collection</i>.
      */
     public Number getAverage(Collection collection) {
         return MathUtils.getAverage(collection);
     }
 
     /**
      * Get the sum of the values
      *
      * @param array An array containing number values
      * @return The sum of the values in <i>array</i>.
      */
     public Number getTotal(Object... array) {
         return MathUtils.getTotal(array);
     }
 
     /**
      * Get the average of the values
      *
      * @param array An array containing number values
      * @return The sum of the values in <i>array</i>.
      */
     public Number getAverage(Object... array) {
         return MathUtils.getAverage(array);
     }
 
     /**
      * Get the sum of the values
      *
      * @param values The list of double values to add up.
      * @return The sum of the arrays
      */
     public Number getTotal(double... values) {
         return MathUtils.getTotal(values);
     }
 
     /**
      * Get the average of the values in an array of double values
      *
      * @param values The list of double values
      * @return The average of the array of values
      */
     public Number getAverage(double... values) {
         return MathUtils.getAverage(values);
     }
 
     /**
      * Get the sum of the values
      *
      * @param values The list of long values to add up.
      * @return The sum of the arrays
      */
     public Number getTotal(long... values) {
         return MathUtils.getTotal(values);
     }
 
     /**
      * Get the average of the values in an array of long values
      *
      * @param values The list of long values
      * @return The average of the array of values
      */
     public Number getAverage(long... values) {
         return MathUtils.getAverage(values);
     }
 
     // --------------- public type conversion methods ---------
 
     /**
      * Converts an object with a numeric value into an Integer
      * Valid formats are {@link Number} or a {@link String}
      * representation of a number
      *
      * @param num the number to be converted
      * @return a {@link Integer} representation of the number
      *         or <code>null</code> if it's invalid
      */
     public Integer toInteger(Object num) {
         Number n = ConversionUtils.toNumber(num);
         if (n == null) {
             return null;
         }
         return Integer.valueOf(n.intValue());
     }
 
 
     /**
      * Converts an object with a numeric value into a Double
      * Valid formats are {@link Number} or a {@link String}
      * representation of a number
      *
      * @param num the number to be converted
      * @return a {@link Double} representation of the number
      *         or <code>null</code> if it's invalid
      */
     public Double toDouble(Object num) {
         Number n = ConversionUtils.toNumber(num);
         if (n == null) {
             return null;
         }
         return new Double(n.doubleValue());
     }
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
 
 
 }
