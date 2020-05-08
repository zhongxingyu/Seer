 package org.agmip.common;
 
 import java.math.BigInteger;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
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
                sum.add(new BigDecimal(addends[i]));
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
                difference.subtract(new BigDecimal(subtrahends[i]));
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
                prodcut.multiply(new BigDecimal(factors[i]));
             }
             return prodcut.toString();
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Get the result of dividend divided by divisor The scale will depends on
      * the scale of dividend when the result is indivisible
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
         BigDecimal bdDividend;
         BigDecimal bdDivisor;
 
         try {
             bdDividend = new BigDecimal(dividend);
             bdDivisor = new BigDecimal(divisor);
             if (bdDivisor.doubleValue() == 0) {
                 log.warn("Try to let a number be devided by 0");
                 return null;
             }
         } catch (Exception ex) {
             return null;
         }
 
         try {
             return bdDividend.divide(bdDivisor).toString();
         } catch (ArithmeticException ae) {
             try {
                 return bdDividend.divide(bdDivisor, bdDividend.scale(), RoundingMode.HALF_UP).toString();
             } catch (Exception e) {
                 return null;
             }
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * Get the average of all input numbers The scale will depends on the scale
      * of input number when the result is indivisible
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
         return divide(sum(values), values.length + "");
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
             bd = new BigDecimal(values[0]);
             for (int i = 1; i < values.length; i++) {
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
             bd = new BigDecimal(values[0]);
             for (int i = 1; i < values.length; i++) {
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
      * Returns the closest {@code decimal} to the argument, with given scale, using HALF_UP mode
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
         Math.round(234);
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
 
         LESS, NOTLESS, GREATER, NOTGREATER, EQUALL
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
                 case EQUALL:
                     return ret == 0;
                 default:
                     return false;
             }
         } catch (Exception e) {
             return false;
         }
     }
 }
