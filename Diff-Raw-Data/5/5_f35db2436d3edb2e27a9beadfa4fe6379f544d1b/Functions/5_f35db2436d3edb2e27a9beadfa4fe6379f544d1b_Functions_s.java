 package org.agmip.common;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.math.BigInteger;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Functions {
 
     private static final Logger log = LoggerFactory.getLogger(Functions.class);
     private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyyMMdd");
 
     /**
      * Cannot instantiate this class.
      */
     private Functions() {
     }
 
     /**
      * Converts a numeric string to a {@code BigInteger}.
      *
      * This function first converts to a {@code BigDecimal} to make sure the
      * base number being used is accurate. By default, this method uses the
      * {@code ROUND_HALF_UP} rounding method from BigDecimal. If the string
      * cannot be converted, this method returns {@code null}
      *
      * @param numeric A numeric string (with or without decimals).
      *
      * @return {@code BigInteger} representation of the string or {@code null}.
      *
      * @see BigDecimal
      */
     public static BigInteger numericStringToBigInteger(String numeric) {
         return numericStringToBigInteger(numeric, true);
     }
 
     /**
      * Converts a numeric string to a {@code BigInteger}.
      *
      * This function first converts to a {@code BigDecimal} to make sure the
      * base number being used is accurate. If {@code round} is set to
      * <strong>true</strong> this method uses the {@code ROUND_HALF_UP} rounding
      * method from {@code BigDecimal}. Otherwise the decimal part is dropped. If
      * the string cannot be converted, this method returns {@code null}
      *
      * @param numeric A numeric string (with or without decimals).
      * @param round Use {@link BigDecimal#ROUND_HALF_UP} method.
      *
      * @return {@code BigInteger} representation of the string or {@code null}
      *
      * @see BigDecimal
      */
     public static BigInteger numericStringToBigInteger(String numeric, boolean round) {
         BigDecimal decimal;
 
         try {
             decimal = new BigDecimal(numeric);
         } catch (Exception ex) {
             return null;
         }
 
         if (round) {
             decimal = decimal.setScale(0, BigDecimal.ROUND_HALF_UP);
         }
         BigInteger integer = decimal.toBigInteger();
         return integer;
     }
 
     /**
      * Convert from AgMIP standard date string (YYYYMMDD) to a {@code Date}
      *
      * @param agmipDate AgMIP standard date string
      *
      * @return {@code Date} represented by the AgMIP date string or {@code null}
      */
     public static Date convertFromAgmipDateString(String agmipDate) {
         try {
             return dateFormatter.parse(agmipDate);
        } catch (ParseException ex) {
             return null;
         }
     }
 
     /**
      * Convert from {@code Date} to AgMIP standard date string (YYYYMMDD)
      *
      * @param date {@link Date} object
      *
      * @return an AgMIP standard date string representation of {@code date}.
      */
     public static String convertToAgmipDateString(Date date) {
         if (date != null) {
             return dateFormatter.format(date);
         } else {
             return null;
         }
     }
 
     /**
      * Convert from AgMIP standard date string (YYMMDD) to a custom date string
      *
      * @param agmipDate AgMIP standard date string
      * @param format Destination format
      *
      * @return a formatted date string or {@code null}
      */
     public static String formatAgmipDateString(String agmipDate, String format) {
         try {
             SimpleDateFormat fmt = new SimpleDateFormat(format);
             Date d = dateFormatter.parse(agmipDate);
             return fmt.format(d);
        } catch (ParseException ex) {
             return null;
         }
     }
 
     /**
      * Offset an AgMIP standard date string (YYYYMMDD) by a set number of days.
      *
      * @param initial AgMIP standard date string
      * @param offset number of days to offset (can be positive or negative
      * integer)
      *
      * @return AgMIP standard date string of <code>initial + offset</code>
      */
     public static String dateOffset(String initial, String offset) {
         Date date = convertFromAgmipDateString(initial);
         BigInteger iOffset;
         if (date == null) {
             // Invalid date
             return null;
         }
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(date);
 
         try {
             iOffset = new BigInteger(offset);
             cal.add(GregorianCalendar.DAY_OF_MONTH, iOffset.intValue());
         } catch (Exception ex) {
             return null;
         }
         return convertToAgmipDateString(cal.getTime());
     }
 
     /**
      * Offset an AgMIP standard date string (YYYYMMDD) by a set number of years.
      *
      * @param initial AgMIP standard date string
      * @param offset number of years to offset (can be positive or negative
      * integer)
      *
      * @return AgMIP standard date string of <code>initial + offset</code>
      */
     public static String yearOffset(String initial, String offset) {
         Date date = convertFromAgmipDateString(initial);
         BigInteger iOffset;
         if (date == null) {
             // Invalid date
             return null;
         }
         GregorianCalendar cal = new GregorianCalendar();
         cal.setTime(date);
 
         try {
             iOffset = new BigInteger(offset);
             cal.add(GregorianCalendar.YEAR, iOffset.intValue());
         } catch (Exception ex) {
             return null;
         }
         return convertToAgmipDateString(cal.getTime());
     }
 
     /**
      * Offset a numeric string by another numeric string.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param initial A valid number string
      * @param offset A valid number string
      *
      * @return a number string with the precision matching the highest precision
      * argument.
      *
      * @see BigDecimal
      */
     public static String numericOffset(String initial, String offset) {
         BigDecimal number;
         BigDecimal dOffset;
 
         try {
             number = new BigDecimal(initial);
             dOffset = new BigDecimal(offset);
         } catch (Exception ex) {
             return null;
         }
         return number.add(dOffset).toString();
     }
 
     /**
      * Multiply two numbers together
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param f1 A valid number string
      * @param f2 A valid number string
      *
      * @return <code>f1*f2</code>
      *
      * @see BigDecimal
      */
     public static String multiply(String f1, String f2) {
         BigDecimal factor1;
         BigDecimal factor2;
 
         try {
             factor1 = new BigDecimal(f1);
             factor2 = new BigDecimal(f2);
         } catch (Exception ex) {
             return null;
         }
 
         return factor1.multiply(factor2).toString();
     }
 
     /**
      * Get the sum of all input numbers
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param addends one or more valid number strings
      *
      * @return <code>addends[0] + addends[1] + ...</code>
      *
      * @see BigDecimal
      */
     public static String sum(String... addends) {
 
         if (addends == null || addends.length == 0) {
             return null;
         }
 
         BigDecimal sum;
         try {
             sum = new BigDecimal(addends[0]);
             for (int i = 1; i < addends.length; i++) {
                 sum = sum.add(new BigDecimal(addends[i]));
             }
             return sum.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Get the difference of minuend and all subtrahends
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param minuend A valid number string
      * @param subtrahends one or more valid number strings
      *
      * @return <code>minuend - subtrahends[0] - subtrahends[1] - ...</code>
      *
      * @see BigDecimal
      */
     public static String substract(String minuend, String... subtrahends) {
 
         if (subtrahends == null) {
             return minuend;
         }
 
         BigDecimal difference;
         try {
             difference = new BigDecimal(minuend);
             for (int i = 0; i < subtrahends.length; i++) {
                 difference = difference.subtract(new BigDecimal(subtrahends[i]));
             }
             return difference.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Get the product of all input numbers
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param factors one or more valid number strings
      *
      * @return <code>factors[0] * factors[1] * ...</code>
      *
      * @see BigDecimal
      */
     public static String product(String... factors) {
 
         if (factors == null || factors.length == 0) {
             return null;
         }
 
         BigDecimal prodcut;
         try {
             prodcut = new BigDecimal(factors[0]);
             for (int i = 1; i < factors.length; i++) {
                 prodcut = prodcut.multiply(new BigDecimal(factors[i]));
             }
             return prodcut.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Get the result of dividend divided by divisor. When the result is
      * indivisible, the scale will depends on the scale of dividend and divisor.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param dividend A valid number string
      * @param divisor A valid number strings
      *
      * @return <code>dividend / divisor</code>
      *
      * @see BigDecimal
      */
     public static String divide(String dividend, String divisor) {
         try {
             BigDecimal bdDividend = new BigDecimal(dividend);
             BigDecimal bdDivisor = new BigDecimal(divisor);
             try {
                 return bdDividend.divide(bdDivisor).toString();
             } catch (ArithmeticException ae) {
                 int scale = Math.max(bdDividend.scale(), bdDivisor.scale()) + 1;
                 return divide(dividend, divisor, scale);
             }
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Get the result of dividend divided by divisor with given scale.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param dividend A valid number string
      * @param divisor A valid number strings
      * @param scale scale of the {@code BigDecimal} quotient to be returned.
      *
      * @return <code>dividend / divisor</code>
      *
      * @see BigDecimal
      */
     public static String divide(String dividend, String divisor, int scale) {
         BigDecimal bdDividend;
         BigDecimal bdDivisor;
 
         try {
             bdDividend = new BigDecimal(dividend);
             bdDivisor = new BigDecimal(divisor);
             return bdDividend.divide(bdDivisor, scale, RoundingMode.HALF_UP).toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Get the average of all input numbers. When the result is indivisible, the
      * scale will depends on the scale of all input numbers
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param values one or more valid number strings
      *
      * @return <code>(values[0] + values[1] + ...) / values.length</code>
      *
      * @see BigDecimal
      */
     public static String average(String... values) {
         if (values != null) {
             return divide(sum(values), values.length + "");
         }
         return null;
     }
 
     /**
      * Get the average of all input numbers with given scale.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param scale of the {@code BigDecimal} quotient to be returned.
      * @param values one or more valid number strings
      *
      * @return <code>(values[0] + values[1] + ...) / values.length</code>
      *
      * @see BigDecimal
      */
     public static String average(int scale, String... values) {
         if (values != null) {
             return divide(sum(values), values.length + "", scale);
         }
         return null;
     }
 
     /**
      * Returns Euler's number <i>e</i> raised to the power of a {@code double}
      * value.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param exponent A valid number string
      *
      * @return the value <i>e</i><sup>{@code value}</sup>, where <i>e</i> is the
      * base of the natural logarithms.
      *
      * @see BigDecimal
      */
     public static String exp(String exponent) {
         BigDecimal bd;
         try {
             bd = new BigDecimal(exponent);
             return Math.exp(bd.doubleValue()) + "";
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the natural logarithm (base <i>e</i>) of a {@code double} value.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param val A valid number string
      *
      * @return the value ln&nbsp;{@code a}, the natural logarithm of {@code a}.
      *
      * @see BigDecimal
      */
     public static String log(String val) {
         BigDecimal bd;
         try {
             bd = new BigDecimal(val);
             return Math.log(bd.doubleValue()) + "";
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the minimum number from a group of input value.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param values One of more valid number strings
      *
      * @return the minimum number.
      *
      * @see BigDecimal
      */
     public static String min(String... values) {
         BigDecimal bd;
         BigDecimal bd2;
         try {
             int start = 0;
             while (values[start] == null) {
                 start++;
             }
             bd = new BigDecimal(values[start]);
             for (int i = start + 1; i < values.length; i++) {
                 if (values[i] == null) {
                     continue;
                 }
                 bd2 = new BigDecimal(values[i]);
                 if (bd.compareTo(bd2) > 0) {
                     bd = bd2;
                 }
             }
             return bd.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the maximum number from a group of input value.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param values One of more valid number strings
      *
      * @return the maximum number.
      *
      * @see BigDecimal
      */
     public static String max(String... values) {
         BigDecimal bd;
         BigDecimal bd2;
         try {
             int start = 0;
             while (values[start] == null) {
                 start++;
             }
             bd = new BigDecimal(values[start]);
             for (int i = start + 1; i < values.length; i++) {
                 if (values[i] == null) {
                     continue;
                 }
                 bd2 = new BigDecimal(values[i]);
                 if (bd.compareTo(bd2) < 0) {
                     bd = bd2;
                 }
             }
             return bd.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Returns the value of the first argument raised to the power of the second
      * argument.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param base the base.
      * @param exponent the exponent.
      * @return the value {@code base}<sup>{@code exponent}</sup>.
      */
     public static String pow(String base, String exponent) {
         try {
             BigDecimal bdBase = new BigDecimal(base);
             BigDecimal bdExp = new BigDecimal(exponent);
             return Math.pow(bdBase.doubleValue(), bdExp.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the correctly rounded positive square root of a {@code double}
      * value.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number
      * @return the positive square root of {@code value}.
      */
     public static String sqrt(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.sqrt(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the trigonometric cosine of an angle.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number for an angle, in radians
      * @return the cosine of the argument
      */
     public static String cos(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.cos(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the trigonometric sine of an angle.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number for an angle, in radians
      * @return the sine of the argument
      */
     public static String sin(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.sin(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the trigonometric tangent of an angle.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number for an angle, in radians
      * @return the tangent of the argument
      */
     public static String tan(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.tan(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the arc cosine of a value; the returned angle is in the range 0.0
      * through <i>pi</i>.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number for an angle, in radians
      * @return the arc cosine of the argument.
      */
     public static String acos(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.acos(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the arc sine of a value; the returned angle is in the range
      * -<i>pi</i>/2 through <i>pi</i>/2.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number for an angle, in radians
      * @return the arc sine of the argument.
      */
     public static String asin(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.asin(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the arc tangent of a value; the returned angle is in the range
      * -<i>pi</i>/2 through <i>pi</i>/2.
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number for an angle, in radians
      * @return the arc tangent of the argument.
      */
     public static String atan(String value) {
         try {
             BigDecimal bd = new BigDecimal(value);
             return Math.atan(bd.doubleValue()) + "";
         } catch (Exception ex) {
             return null;
         }
     }
 
     /**
      * Returns the closest {@code decimal} to the argument, with given scale,
      * using HALF_UP mode
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param value A valid number string
      *
      * @return the rounded number.
      *
      * @see BigDecimal
      */
     public static String round(String value, int scale) {
         BigDecimal bd;
         try {
             bd = new BigDecimal(value);
             bd = bd.setScale(scale, RoundingMode.HALF_UP);
             return bd.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * compare mode
      */
     public enum CompareMode {
 
         LESS, NOTLESS, GREATER, NOTGREATER, EQUAL
     }
 
     /**
      * Compare the input number by given mode The scale will depends on the
      * scale of input number when the result is indivisible
      *
      * Any numeric string recognized by {@code BigDecimal} is supported.
      *
      * @param v1 A valid number strings
      * @param v2 A valid number strings
      * @param mode The compare mode
      *
      * @return the boolean compare result based on given mode; invalid mode or
      * value will return false
      *
      * @see BigDecimal
      */
     public static boolean compare(String v1, String v2, CompareMode mode) {
         BigDecimal bd1;
         BigDecimal bd2;
 
         try {
             bd1 = new BigDecimal(v1);
             bd2 = new BigDecimal(v2);
             int ret = bd1.compareTo(bd2);
             switch (mode) {
                 case LESS:
                     return ret < 0;
                 case NOTLESS:
                     return ret >= 0;
                 case GREATER:
                     return ret > 0;
                 case NOTGREATER:
                     return ret <= 0;
                 case EQUAL:
                     return ret == 0;
                 default:
                     return false;
             }
         } catch (Exception e) {
             return false;
         }
     }
 
     /**
      * Gathering the messages from a {@code Throwable} instance and its back
      * trace
      *
      * @param aThrowable
      *
      * @return The trace messages
      */
     public static String getStackTrace(Throwable aThrowable) {
         final Writer result = new StringWriter();
         final PrintWriter printWriter = new PrintWriter(result);
         aThrowable.printStackTrace(printWriter);
         return result.toString();
     }
 
     /**
      * Remove the null value in the input String array
      *
      * @param in The input String values
      * @return The input String without null value
      */
     public static String[] removeNull(String[] in) {
         if (in == null) {
             return new String[0];
         }
         ArrayList<String> arr = new ArrayList();
         for (int i = 0; i < in.length; i++) {
             if (in[i] != null) {
                 arr.add(in[i]);
             }
         }
         if (in.length == arr.size()) {
             return in;
         } else {
             return arr.toArray(new String[0]);
         }
     }
 }
