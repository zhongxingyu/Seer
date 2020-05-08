 /*
  * Copyright 2002-2004 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.cscott.sdr.util;
 
 import java.io.Serializable;
 import java.math.BigInteger;
 
 /**
  * <p><code>Fraction</code> is a <code>Number</code> which implements
  * exact arithmetic on fractions.  All computations which do not
  * overflow can be done without any accumulated rounding errors.</p>
  *
  * <p>The implementation of the <code>Fraction</code> class is based on
  * the algorithms described in Donald E. Knuth's <i>The Art of Computer
  * Programming, volume 2</i>, sections 4.5 through 4.5.2.  In particular,
  * fractions are represented as a pair of integers <code>(n/d)</code>
  * where <code>n</code> and <code>d</code> are relatively prime to
  * each other and <code>d>0</code>.  The number zero is represented
  * as <code>(0/1)</code>.  Fractions are always maintained in "simplest
  * form" and numerically-equal fractions always return true from the
  * <code>equals()</code> method.</p>
  *
  * <p>(Note however that there is a "backwards-compatibility" mode enabled
  * if you use the deprecated <code>getFraction()</code> constructor; in this
  * case it will appear that unsimplified fractions are maintained, and
  * <code>!getFraction(1,2).equals(getFraction(2,4))</code>.  Use of
  * <code>getFraction()</code> is not recommended.)
  * </p>
  *
  * <p>This class is immutable, and interoperable with most methods that accept
  * a <code>Number</code>.</p>
  *
  * @author C. Scott Ananian
  * @author Travis Reeder
  * @author Stephen Colebourne
  * @author Tim O'Brien
  * @author Pete Gieser
  * @since 2.0
 * @version $Id: Fraction.java,v 1.7 2006-10-25 20:50:53 cananian Exp $
  */
 public class Fraction extends Number implements Serializable, Comparable<Fraction> {
 
     /** Serialization lock, Lang version 2.0 */
     private static final long serialVersionUID = 65382027393090L;
     /** Ensure serialization is backwards-compatible by
      * reconstructing fractions as they are read in.
      * @return a current representation of the deserialized
      *         <code>Fraction</code>
      * @throws java.io.ObjectStreamException if the deserialized fraction
      *   is invalid or corrupt (for example, the denominator is zero)
      */
     private Object readResolve() throws java.io.ObjectStreamException {
         try {
             return getFraction(numerator, denominator);
         } catch (ArithmeticException e) {
             throw new java.io.InvalidObjectException(e.toString());
         }
     }
 
     /**
      * <code>Fraction</code> representation of 0.
      */
     public static final Fraction ZERO = new Fraction(0, 1);
     /**
      * <code>Fraction</code> representation of 1.
      */
     public static final Fraction ONE = new Fraction(1, 1);
     /**
      * <code>Fraction</code> representation of 1/2.
      */
     public static final Fraction ONE_HALF = new Fraction(1, 2);
     /**
      * <code>Fraction</code> representation of 1/3.
      */
     public static final Fraction ONE_THIRD = new Fraction(1, 3);
     /**
      * <code>Fraction</code> representation of 2/3.
      */
     public static final Fraction TWO_THIRDS = new Fraction(2, 3);
     /**
      * <code>Fraction</code> representation of 1/4.
      */
     public static final Fraction ONE_QUARTER = new Fraction(1, 4);
     /**
      * <code>Fraction</code> representation of 2/4.
      * @deprecated This is an unreduced fraction for backwards-compatibility,
      *   and is prone to overflow during arithmetic.  Use ONE_HALF instead.
      */
     public static final Fraction TWO_QUARTERS = getFraction(2, 4);
     /**
      * <code>Fraction</code> representation of 3/4.
      */
     public static final Fraction THREE_QUARTERS = new Fraction(3, 4);
     /**
      * <code>Fraction</code> representation of 1/5.
      */
     public static final Fraction ONE_FIFTH = new Fraction(1, 5);
     /**
      * <code>Fraction</code> representation of 2/5.
      */
     public static final Fraction TWO_FIFTHS = new Fraction(2, 5);
     /**
      * <code>Fraction</code> representation of 3/5.
      */
     public static final Fraction THREE_FIFTHS = new Fraction(3, 5);
     /**
      * <code>Fraction</code> representation of 4/5.
      */
     public static final Fraction FOUR_FIFTHS = new Fraction(4, 5);
     /**
      * <code>Fraction</code> representation of 1/8.
      */
     public static final Fraction ONE_EIGHTH = new Fraction(1, 8);
     /**
      * <code>Fraction</code> representation of 3/8.
      */
     public static final Fraction THREE_EIGHTHS = new Fraction(3, 8);
     /**
      * <code>Fraction</code> representation of 5/8.
      */
     public static final Fraction FIVE_EIGHTHS = new Fraction(5, 8);
     /**
      * <code>Fraction</code> representation of 7/8.
      */
     public static final Fraction SEVEN_EIGHTHS = new Fraction(7, 8);
 
 
     /**
      * The numerator number part of the fraction (the three in three sevenths).
      * Numerator and denominator are always relatively prime.
      */
     final int numerator;
     /**
      * The denominator number part of the fraction (the seven in three sevenths).
      * Numerator and denominator are always relatively prime.
      * The denominator is always greater than zero.
      */
     final int denominator;
 
     /**
      * Cached output hashCode (class is immutable).
      */
     private transient int hashCode = 0;
     /**
      * Cached output toString (class is immutable).
      */
     private transient String toString = null;
     /**
      * Cached output toProperString (class is immutable).
      */
     private transient String toProperString = null;
 
     /**
      * <p>Constructs a <code>Fraction</code> instance with the 2 parts
      * of a fraction Y/Z.</p>
      *
      * @param numerator  the numerator, for example the three in 'three sevenths'
      * @param denominator  the denominator, for example the seven in 'three sevenths'
      */
     private Fraction(int numerator, int denominator) {
         this(numerator, denominator, true/*use no-check constructor*/);
         /* Now do checks. */
         assert denominator > 0 :
            "denominator must be greater than zero";
         assert numerator==0 ? denominator==1 :
             greatestCommonDivisor(numerator, denominator)==1 :
             "numerator and denominator ought to be relatively prime and "+
             "zero should be represented as 0/1";
     }
     /** Private constructor which bypasses numerator/denominator
      *  validity checks.  ONLY FOR USE by standard private constructor
      *  and within BadFraction.writeReplace().
      * @param numerator  the numerator
      * @param denominator  the denominator
      * @param _ignore_ an ignored parameter used only to select the constructor
      */
     private Fraction(int numerator, int denominator, boolean _ignore_) {
         super();
         this.numerator = numerator;
         this.denominator = denominator;
     }
 
     /**
      * <p>Creates a <code>Fraction</code> instance with the 2 parts
      * of a fraction Y/Z.</p>
      *
      * <p>Any negative signs are resolved to be on the numerator.
      * Creates UNREDUCED fractions for backwards-compatibility.</p>
      *
      * @param numerator  the numerator, for example the three in 'three sevenths'
      * @param denominator  the denominator, for example the seven in 'three sevenths'
      * @return a new fraction instance
      * @throws ArithmeticException if the denominator is <code>zero</code>
      * @deprecated For reasons of backwards-compatibility, this method
      *   does not simplify fractions.  The Fraction objects returned are
      *   thus subject to overflow.  It should not be used in new
      *   code.  Use valueOf() instead.
      */
     public static Fraction getFraction(final int numerator,
                                        final int denominator) {
         Fraction f = valueOf(numerator, denominator);
         if (f.denominator!=Math.abs(denominator)) {
             // fraction was not simplified, ugh.
             // make a backwards-compatibility thunk.
             return new BadFraction(f, Math.abs(denominator));
         }
         return f;
     }
     /**
      * <p>Creates a <code>Fraction</code> instance with the 2 parts
      * of a fraction Y/Z.</p>
      *
      * <p>Any negative signs are resolved to be on the numerator, zeros
      * are normalized to (0/1), and all fractions are simplified.</p>
      *
      * @param numerator  the numerator, for example the three in 'three sevenths'
      * @param denominator  the denominator, for example the seven in 'three sevenths'
      * @return a new fraction instance
      * @throws ArithmeticException if the denominator is <code>zero</code>
      */
     public static Fraction valueOf(int numerator, int denominator) {
         if (denominator == 0) {
             throw new ArithmeticException("The denominator must not be zero");
         }
         if (numerator==0) {
             return ZERO; // normalize zero.
         }
         // allow 2^k/-2^31 as a valid fraction (where k>0)
         if (denominator==Integer.MIN_VALUE && (numerator&1)==0) {
             numerator/=2; denominator/=2;
         }
         if (denominator < 0) {
             if (numerator==Integer.MIN_VALUE ||
                 denominator==Integer.MIN_VALUE) {
                 throw new ArithmeticException("overflow: can't negate");
             }
             numerator = -numerator;
             denominator = -denominator;
         }
         // simplify fraction.
         int gcd = greatestCommonDivisor(numerator, denominator);
         numerator /= gcd;
         denominator /= gcd;
         return new Fraction(numerator, denominator);
     }
     /**
      * <p>Creates a <code>Fraction</code> instance from a <code>int</code> value.</p>
      * @param value  the integer value to convert
      * @return a new fraction instance that is equal to the value
      */
     public static Fraction valueOf(int value) {
         return new Fraction(value,1);
     }
 
     /**
      * <p>Creates a <code>Fraction</code> instance with the 3 parts
      * of a fraction X Y/Z.
      * Creates UNREDUCED fractions for backwards-compatibility.</p>
      *
      * <p>The negative sign must be passed in on the whole number part.</p>
      *
      * @param whole  the whole number, for example the one in 'one and three sevenths'
      * @param numerator  the numerator, for example the three in 'one and three sevenths'
      * @param denominator  the denominator, for example the seven in 'one and three sevenths'
      * @return a new fraction instance
      * @throws ArithmeticException if the denominator is <code>zero</code>
      * @throws ArithmeticException if the denominator is negative
      * @throws ArithmeticException if the numerator is negative
      * @throws ArithmeticException if the resulting numerator exceeds 
      *  <code>Integer.MAX_VALUE</code>
      * @deprecated For reasons of backwards-compatibility, this method
      *   does not simplify fractions.  The Fraction objects returned are
      *   thus subject to overflow.  It should not be used in new
      *   code.  Use valueOf() instead.
      */
     public static Fraction getFraction(int whole, int numerator, int denominator) {
         Fraction f = valueOf(whole, numerator, denominator);
         if (f.denominator!=Math.abs(denominator)) {
             // fraction was not simplified, ugh.
             // make a backwards-compatibility thunk.
             return new BadFraction(f, denominator);
         }
         return f;
     }
     /**
      * <p>Creates a <code>Fraction</code> instance with the 3 parts
      * of a fraction X Y/Z.</p>
      *
      * <p>The negative sign must be passed in on the whole number part.</p>
      *
      * @param whole  the whole number, for example the one in 'one and three sevenths'
      * @param numerator  the numerator, for example the three in 'one and three sevenths'
      * @param denominator  the denominator, for example the seven in 'one and three sevenths'
      * @return a new fraction instance
      * @throws ArithmeticException if the denominator is <code>zero</code>
      * @throws ArithmeticException if the denominator is negative
      * @throws ArithmeticException if the numerator is negative
      * @throws ArithmeticException if the resulting numerator exceeds 
      *  <code>Integer.MAX_VALUE</code>
      */
     public static Fraction valueOf(int whole, int numerator, int denominator) {
         if (denominator == 0) {
             throw new ArithmeticException("The denominator must not be zero");
         }
         if (denominator < 0) {
             throw new ArithmeticException("The denominator must not be negative");
         }
         if (numerator < 0) {
             throw new ArithmeticException("The numerator must not be negative");
         }
         long numeratorValue;
         if (whole < 0) {
             numeratorValue = whole * (long)denominator - numerator;
         } else {
             numeratorValue = whole * (long)denominator + numerator;
         }
         if (numeratorValue < Integer.MIN_VALUE ||
             numeratorValue > Integer.MAX_VALUE) {
             throw new ArithmeticException("Numerator too large to represent as an Integer.");
         }
         return valueOf((int)numeratorValue, denominator);
     }
 
     /**
      * <p>Creates a <code>Fraction</code> instance with the 2 parts
      * of a fraction Y/Z.</p>
      *
      * <p>Any negative signs are resolved to be on the numerator.</p>
      *
      * @param numerator  the numerator, for example the three in 'three sevenths'
      * @param denominator  the denominator, for example the seven in 'three sevenths'
      * @return a new fraction instance, with the numerator and denominator reduced
      * @throws ArithmeticException if the denominator is <code>zero</code>
      * @deprecated All fractions are now stored as reduced fractions;
      *             use the valueOf() method instead.
      */
     public static Fraction getReducedFraction(int numerator, int denominator) {
         return valueOf(numerator, denominator);
     }
 
     /**
      * <p>Creates a <code>Fraction</code> instance from a <code>double</code> value.</p>
      *
      * <p>This method uses the <a href="http://archives.math.utk.edu/articles/atuyl/confrac/">
      *  continued fraction algorithm</a>, computing a maximum of
      *  25 convergents and bounding the denominator by 10,000.</p>
      *
      * @param value  the double value to convert
      * @return a new fraction instance that is close to the value
      * @throws ArithmeticException if <code>|value| > Integer.MAX_VALUE</code> 
      *  or <code>value = NaN</code>
      * @throws ArithmeticException if the calculated denominator is <code>zero</code>
      * @throws ArithmeticException if the the algorithm does not converge
      * @deprecated Renamed this method to valueOf() for consistency.
      */
     public static Fraction getFraction(double value) {
         return valueOf(value);
     }
     /**
      * <p>Creates a <code>Fraction</code> instance from a <code>double</code> value.</p>
      *
      * <p>This method uses the <a href="http://archives.math.utk.edu/articles/atuyl/confrac/">
      *  continued fraction algorithm</a>, computing a maximum of
      *  25 convergents and bounding the denominator by 10,000.</p>
      *
      * @param value  the double value to convert
      * @return a new fraction instance that is close to the value
      * @throws ArithmeticException if <code>|value| > Integer.MAX_VALUE</code> 
      *  or <code>value = NaN</code>
      * @throws ArithmeticException if the calculated denominator is <code>zero</code>
      * @throws ArithmeticException if the the algorithm does not converge
      */
     public static Fraction valueOf(double value) {
         int sign = (value < 0 ? -1 : 1);
         value = Math.abs(value);
         if (value  > Integer.MAX_VALUE || Double.isNaN(value)) {
             throw new ArithmeticException
                 ("The value must not be greater than Integer.MAX_VALUE or NaN");
         }
         int wholeNumber = (int) value;
         value -= wholeNumber;
         
         int numer0 = 0;  // the pre-previous
         int denom0 = 1;  // the pre-previous
         int numer1 = 1;  // the previous
         int denom1 = 0;  // the previous
         int numer2 = 0;  // the current, setup in calculation
         int denom2 = 0;  // the current, setup in calculation
         int a1 = (int) value;
         int a2 = 0;
         double x1 = 1;
         double x2 = 0;
         double y1 = value - a1;
         double y2 = 0;
         double delta1, delta2 = Double.MAX_VALUE;
         double fraction;
         int i = 1;
 //        System.out.println("---");
         do {
             delta1 = delta2;
             a2 = (int) (x1 / y1);
             x2 = y1;
             y2 = x1 - a2 * y1;
             numer2 = a1 * numer1 + numer0;
             denom2 = a1 * denom1 + denom0;
             fraction = (double) numer2 / (double) denom2;
             delta2 = Math.abs(value - fraction);
 //            System.out.println(numer2 + " " + denom2 + " " + fraction + " " + delta2 + " " + y1);
             a1 = a2;
             x1 = x2;
             y1 = y2;
             numer0 = numer1;
             denom0 = denom1;
             numer1 = numer2;
             denom1 = denom2;
             i++;
 //            System.out.println(">>" + delta1 +" "+ delta2+" "+(delta1 > delta2)+" "+i+" "+denom2);
         } while ((delta1 > delta2) && (denom2 <= 10000) && (denom2 > 0) && (i < 25));
         if (i == 25) {
             throw new ArithmeticException("Unable to convert double to fraction");
         }
         return valueOf((numer0 + wholeNumber * denom0) * sign, denom0);
     }
 
     /**
      * <p>Creates a Fraction from a <code>String</code>.</p>
      *
      * <p>The formats accepted are:</p>
      *
      * <ol>
      *  <li><code>double</code> String containing a dot</li>
      *  <li>'X Y/Z'</li>
      *  <li>'Y/Z'</li>
      *  <li>'X' (a simple whole number)</li>
      * </ol>
      *
      * @param str  the string to parse, must not be <code>null</code>
      * @return the new <code>Fraction</code> instance
      * @throws IllegalArgumentException if the string is <code>null</code>
      * @throws NumberFormatException if the number format is invalid
      */
     public static Fraction valueOf(String str) {
         return valueOf(str, false/* not backwards-compatible */);
     }
     /**
      * <p>Creates a Fraction from a <code>String</code>.
      * Creates UNREDUCED fractions for backwards-compatibility.</p>
      *
      * <p>The formats accepted are:</p>
      *
      * <ol>
      *  <li><code>double</code> String containing a dot</li>
      *  <li>'X Y/Z'</li>
      *  <li>'Y/Z'</li>
      *  <li>'X' (a simple whole number)</li>
      * </ol>
      *
      * @param str  the string to parse, must not be <code>null</code>
      * @return the new <code>Fraction</code> instance
      * @throws IllegalArgumentException if the string is <code>null</code>
      * @throws NumberFormatException if the number format is invalid
      * @deprecated For reasons of backwards-compatibility, this method
      *   does not simplify fractions.  The Fraction objects returned are
      *   thus subject to overflow.  It should not be used in new
      *   code.  Use valueOf() instead.
      */
     public static Fraction getFraction(String str) {
         return valueOf(str, true/* backwards-compatible */);
     }
     /**
      * <p>Creates a Fraction from a <code>String</code>.</p>
      *
      * <p>The formats accepted are:</p>
      *
      * <ol>
      *  <li><code>double</code> String containing a dot</li>
      *  <li>'X Y/Z'</li>
      *  <li>'Y/Z'</li>
      *  <li>'X' (a simple whole number)</li>
      * </ol>
      *
      * @param str  the string to parse, must not be <code>null</code>
      * @param isBackwards true iff unreduced fractions should be returned.
      * @return the new <code>Fraction</code> instance
      * @throws IllegalArgumentException if the string is <code>null</code>
      * @throws NumberFormatException if the number format is invalid
      */
     private static Fraction valueOf(String str, boolean isBackwards) {
         if (str == null) {
             throw new IllegalArgumentException("The string must not be null");
         }
         // parse double format
         int pos = str.indexOf('.');
         if (pos >= 0) {
             return valueOf(Double.parseDouble(str));
         }
 
         // parse X Y/Z format
         pos = str.indexOf(' ');
         if (pos > 0) {
             int whole = Integer.parseInt(str.substring(0, pos));
             str = str.substring(pos + 1);
             pos = str.indexOf('/');
             if (pos < 0) {
                 throw new NumberFormatException("The fraction could not be parsed as the format X Y/Z");
             } else {
                 int numer = Integer.parseInt(str.substring(0, pos));
                 int denom = Integer.parseInt(str.substring(pos + 1));
                 return isBackwards ?
                     getFraction(whole, numer, denom) :
                     valueOf(whole, numer, denom);
             }
         }
 
         // parse Y/Z format
         pos = str.indexOf('/');
         if (pos < 0) {
             // simple whole number
             return valueOf(Integer.parseInt(str), 1);
         } else {
             int numer = Integer.parseInt(str.substring(0, pos));
             int denom = Integer.parseInt(str.substring(pos + 1));
             return isBackwards ?
                 getFraction(numer, denom) :
                 valueOf(numer, denom);
         }
     }
 
     // Accessors
     //-------------------------------------------------------------------
 
     /**
      * <p>Gets the numerator part of the fraction.</p>
      *
      * <p>This method may return a value greater than the denominator, an
      * improper fraction, such as the seven in 7/4.</p>
      *
      * <p>The numerator and denominator will always be relatively prime
      * unless deprecated methods are used.</p>
      * @return the numerator fraction part
      */
     public int getNumerator() {
         return numerator;
     }
 
     /**
      * <p>Gets the denominator part of the fraction.</p>
      *
      * <p>The numerator and denominator will always be relatively prime
      * unless deprecated methods are used.
      * The denominator will always be greater than zero. </p>
      * @return the denominator fraction part
      */
     public int getDenominator() {
         return denominator;
     }
 
     /**
      * <p>Gets the proper numerator, always positive.</p>
      *
      * <p>An improper fraction 7/4 can be resolved into a proper one, 1 3/4.
      * This method returns the 3 from the proper fraction.</p>
      *
      * <p>If the fraction is negative such as -7/4, it can be resolved into
      * -1 3/4, so this method returns the positive proper numerator, 3.</p>
      *
      * <p>The proper numerator will always be relatively prime to the
      * denominator, unless deprecated methods are used.</p>
      * @return the numerator fraction part of a proper fraction, always positive
      */
     public int getProperNumerator() {
         return Math.abs(numerator % denominator);
     }
 
     /**
      * <p>Gets the proper whole part of the fraction.</p>
      *
      * <p>An improper fraction 7/4 can be resolved into a proper one, 1 3/4.
      * This method returns the 1 from the proper fraction.</p>
      *
      * <p>If the fraction is negative such as -7/4, it can be resolved into
      * -1 3/4, so this method returns the positive whole part -1.</p>
      *
      * @return the whole fraction part of a proper fraction, that includes the sign
      */
     public int getProperWhole() {
         return numerator / denominator;
     }
 
     // Number methods
     //-------------------------------------------------------------------
 
     /**
      * <p>Gets the fraction as an <code>int</code>. This returns the whole number
      * part of the fraction.</p>
      *
      * @return the whole number fraction part
      */
     public int intValue() {
         return numerator / denominator;
     }
 
     /**
      * <p>Gets the fraction as a <code>long</code>. This returns the whole number
      * part of the fraction.</p>
      *
      * @return the whole number fraction part
      */
     public long longValue() {
         return (long) numerator / denominator;
     }
 
     /**
      * <p>Gets the fraction as a <code>float</code>. This calculates the fraction
      * as the numerator divided by denominator.</p>
      *
      * @return the fraction as a <code>float</code>
      */
     public float floatValue() {
         return ((float) numerator) / ((float) denominator);
     }
 
     /**
      * <p>Gets the fraction as a <code>double</code>. This calculates the fraction
      * as the numerator divided by denominator.</p>
      *
      * @return the fraction as a <code>double</code>
      */
     public double doubleValue() {
         return ((double) numerator) / ((double) denominator);
     }
 
     // Calculations
     //-------------------------------------------------------------------
 
     /**
      * <p>Reduce the fraction to the smallest values for the numerator and
      * denominator, returning the result.</p>
      *
      * @return a new reduce fraction instance, or this if no simplification possible
      * @deprecated Unless you are using deprecated methods
      *   this operation is a no-op.
      */
     public Fraction reduce() { return this; /* do nothing */ }
 
     /**
      * <p>Gets a fraction that is the inverse (1/fraction) of this one.</p>
      *
      * @return a new fraction instance with the numerator and denominator
      *         inverted.
      * @throws ArithmeticException if the fraction represents zero.
      */
     public Fraction invert() {
         if (numerator == 0) {
             throw new ArithmeticException("Unable to invert zero.");
         }
         if (numerator==Integer.MIN_VALUE) {
             throw new ArithmeticException("overflow: can't negate numerator");
         }
         if (numerator<0) {
             return new Fraction(-denominator, -numerator);
         } else {
             return new Fraction(denominator, numerator);
         }
     }
 
     /**
      * <p>Gets a fraction that is the negative (-fraction) of this one.</p>
      *
      * @return a new fraction instance with the opposite signed numerator
      */
     public Fraction negate() {
         // the positive range is one smaller than the negative range of an int.
         if (numerator==Integer.MIN_VALUE) {
             throw new ArithmeticException("overflow: too large to negate");
         }
         return new Fraction(-numerator, denominator);
     }
 
     /**
      * <p>Gets a fraction that is the positive equivalent of this one.</p>
      * <p>More precisely: <code>(fraction >= 0 ? this : -fraction)</code></p>
      *
      * @return <code>this</code> if it is positive, or a new positive fraction
      *  instance with the opposite signed numerator
      */
     public Fraction abs() {
         if (numerator >= 0) {
             return this;
         }
         return negate();
     }
 
     /**
      * <p>Gets a fraction that is raised to the passed in power.</p>
      *
      * @param power  the power to raise the fraction to
      * @return <code>this</code> if the power is one, <code>ONE</code> if the power
      * is zero (even if the fraction equals ZERO) or a new fraction instance 
      * raised to the appropriate power
      * @throws ArithmeticException if the resulting numerator or denominator exceeds
      *  <code>Integer.MAX_VALUE</code>
      */
     public Fraction pow(int power) {
         if (power == 1) {
             return this;
         } else if (power == 0) {
             return ONE;
         } else if (power < 0) {
             if (power==Integer.MIN_VALUE) { // MIN_VALUE can't be negated.
                 return this.invert().pow(2).pow(-(power/2));
             }
             return this.invert().pow(-power);
         } else {
             Fraction f = this.multiply(this);
             if ((power % 2) == 0) { // if even...
                 return f.pow(power/2);
             } else { // if odd...
                 return f.pow(power/2).multiply(this);
             }
         }
     }
 
     /**
      * <p>Gets the greatest common divisor of the absolute value of
      * two numbers, using the "binary gcd" method which avoids
      * division and modulo operations.  See Knuth 4.5.2 algorithm B.
      * This algorithm is due to Josef Stein (1961).</p>
      *
      * @param u  a non-zero number
      * @param v  a non-zero number
      * @return the greatest common divisor, never zero
      */
     private static int greatestCommonDivisor(int u, int v) {
         // keep u and v negative, as negative integers range down to
         // -2^31, while positive numbers can only be as large as 2^31-1
         // (i.e. we can't necessarily negate a negative number without
         // overflow)
         assert u!=0 && v!=0;
         if (u>0) { u=-u; } // make u negative
         if (v>0) { v=-v; } // make v negative
         // B1. [Find power of 2]
         int k=0;
         while ((u&1)==0 && (v&1)==0) { // while u and v are both even...
             u/=2; v/=2; k++; // cast out twos.
         }
         if (k>=31) {
             throw new ArithmeticException("overflow: gcd is 2^31");
         }
         // B2. Initialize: u and v have been divided by 2^k and at least
         //     one is odd.
         int t = ((u&1)==1) ? v : -(u/2)/*B3*/;
         // t negative: u was odd, v may be even (t replaces v)
         // t positive: u was even, v is odd (t replaces u)
         do {
             assert u<0 && v<0;
             // B4/B3: cast out twos from t.
             while ((t&1)==0) { // while t is even..
                 t/=2; // cast out twos
             }
             // B5 [reset max(u,v)]
             if (t>0) {
                 u = -t;
             } else {
                 v = t;
             }
             // B6/B3. at this point both u and v should be odd.
             t = (v - u)/2;
             // |u| larger: t positive (replace u)
             // |v| larger: t negative (replace v)
         } while (t!=0);
         return -u*(1<<k); // gcd is u*2^k
     }
 
     // Arithmetic
     //-------------------------------------------------------------------
 
     /** Multiply two integers, checking for overflow.
      * @param x a factor
      * @param y a factor
      * @return the product <code>x*y</code>
      * @throws ArithmeticException if the result can not be represented as
      *                             an int
      */
     private static int mulAndCheck(int x, int y) {
         long m = ((long)x)*((long)y);
         if (m < Integer.MIN_VALUE ||
             m > Integer.MAX_VALUE) {
             throw new ArithmeticException("overflow: mul");
         }
         return (int)m;
     }
     /** Multiply two non-negative integers, checking for overflow.
      * @param x a non-negative factor
      * @param y a non-negative factor
      * @return the product <code>x*y</code>
      * @throws ArithmeticException if the result can not be represented as
      *                             an int
      */
     private static int mulPosAndCheck(int x, int y) {
         assert x>=0 && y>=0;
         long m = ((long)x)*((long)y);
         if (m > Integer.MAX_VALUE) {
             throw new ArithmeticException("overflow: mulPos");
         }
         return (int)m;
     }
     /** Add two integers, checking for overflow.
      * @param x an addend
      * @param y an addend
      * @return the sum <code>x+y</code>
      * @throws ArithmeticException if the result can not be represented as
      *                             an int
      */
     private static int addAndCheck(int x, int y) {
         long s = (long)x+(long)y;
         if (s < Integer.MIN_VALUE ||
             s > Integer.MAX_VALUE) {
             throw new ArithmeticException("overflow: add");
         }
         return (int)s;
     }
     /** Subtract two integers, checking for overflow.
      * @param x the minuend
      * @param y the subtrahend
      * @return the difference <code>x-y</code>
      * @throws ArithmeticException if the result can not be represented as
      *                             an int
      */
     private static int subAndCheck(int x, int y) {
         long s = (long)x-(long)y;
         if (s < Integer.MIN_VALUE ||
             s > Integer.MAX_VALUE) {
             throw new ArithmeticException("overflow: add");
         }
         return (int)s;
     }
     /**
      * <p>Adds the value of this fraction to another.
      * The algorithm follows Knuth, 4.5.1.</p>
      *
      * @param fraction  the fraction to add, must not be <code>null</code>
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the resulting numerator or denominator exceeds
      *  <code>Integer.MAX_VALUE</code>
      */
     public Fraction add(Fraction fraction) {
         return addSub(fraction, true /* add */);
     }
 
     /**
      * <p>Subtracts the value of another fraction from the value of this one.
      * </p>
      *
      * @param fraction  the fraction to subtract, must not be <code>null</code>
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the resulting numerator or denominator
      *   cannot be represented in an <code>int</code>.
      */
     public Fraction subtract(Fraction fraction) {
         return addSub(fraction, false /* subtract */);
     }
 
     /** Implement add and subtract using algorithm described in Knuth 4.5.1.
      * @param fraction the fraction to subtract, must not be <code>null</code>
      * @param isAdd true to add, false to subtract
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the resulting numerator or denominator
      *   cannot be represented in an <code>int</code>.
      */
     private Fraction addSub(Fraction fraction, boolean isAdd) {
         if (fraction == null) {
             throw new IllegalArgumentException("The fraction must not be null");
         }
         // zero is identity for addition.
         if (numerator == 0) {
             return isAdd ? fraction : fraction.negate();
         }
         if (fraction.numerator == 0) {
             return this;
         }     
         // if denominators are randomly distributed, d1 will be 1 about 61%
         // of the time.
         int d1 = greatestCommonDivisor(denominator, fraction.denominator);
         if (d1==1) {
             // result is ( (u*v' +/- u'v) / u'v')
             int uvp = mulAndCheck(numerator, fraction.denominator);
             int upv = mulAndCheck(fraction.numerator, denominator);
             return new Fraction
                 (isAdd ? addAndCheck(uvp, upv) : subAndCheck(uvp, upv),
                  mulPosAndCheck(denominator, fraction.denominator));
         }
         // the quantity 't' requires 65 bits of precision; see knuth 4.5.1
         // exercise 7.  we're going to use a BigInteger.
         // t = u(v'/d1) +/- v(u'/d1)
         BigInteger uvp = BigInteger.valueOf(numerator)
             .multiply(BigInteger.valueOf(fraction.denominator/d1));
         BigInteger upv = BigInteger.valueOf(fraction.numerator)
             .multiply(BigInteger.valueOf(denominator/d1));
         BigInteger t = isAdd ? uvp.add(upv) : uvp.subtract(upv);
         // but d2 doesn't need extra precision because
         // d2 = gcd(t,d1) = gcd(t mod d1, d1)
         int tmodd1 = t.mod(BigInteger.valueOf(d1)).intValue();
         int d2 = (tmodd1==0)?d1:greatestCommonDivisor(tmodd1, d1);
 
         // result is (t/d2) / (u'/d1)(v'/d2)
         BigInteger w = t.divide(BigInteger.valueOf(d2));
         if (w.bitLength() > 31) {
             throw new ArithmeticException
                 ("overflow: numerator too large after multiply");
         }
         return new Fraction
             (w.intValue(),
              mulPosAndCheck(denominator/d1, fraction.denominator/d2));
     }
 
     /**
      * <p>Multiplies the value of this fraction by another.</p>
      *
      * @param fraction  the fraction to multiply by, must not be <code>null</code>
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the resulting numerator or denominator exceeds
      *  <code>Integer.MAX_VALUE</code>
      * @deprecated Renamed to multiply()
      */
     public Fraction multiplyBy(Fraction fraction) {
         return multiply(fraction);
     }
     /**
      * <p>Multiplies the value of this fraction by another.</p>
      *
      * @param fraction  the fraction to multiply by, must not be <code>null</code>
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the resulting numerator or denominator exceeds
      *  <code>Integer.MAX_VALUE</code>
      */
     public Fraction multiply(Fraction fraction) {
         if (fraction == null) {
             throw new IllegalArgumentException("The fraction must not be null");
         }
         if (numerator == 0 || fraction.numerator == 0) {
             return ZERO;
         }
         // knuth 4.5.1
         // make sure we don't overflow unless the result *must* overflow.
         int d1 = greatestCommonDivisor(numerator, fraction.denominator);
         int d2 = greatestCommonDivisor(fraction.numerator, denominator);
         return new Fraction
             (mulAndCheck(numerator/d1, fraction.numerator/d2),
              mulPosAndCheck(denominator/d2, fraction.denominator/d1));
     }
 
     /**
      * <p>Divide the value of this fraction by another.</p>
      *
      * @param fraction  the fraction to divide by, must not be <code>null</code>
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the fraction to divide by is zero
      * @throws ArithmeticException if the resulting numerator or denominator exceeds
      *  <code>Integer.MAX_VALUE</code>
      * @deprecated Renamed to divide()
      */
     public Fraction divideBy(Fraction fraction) {
         return divide(fraction);
     }
     /**
      * <p>Divide the value of this fraction by another.</p>
      *
      * @param fraction  the fraction to divide by, must not be <code>null</code>
      * @return a <code>Fraction</code> instance with the resulting values
      * @throws IllegalArgumentException if the fraction is <code>null</code>
      * @throws ArithmeticException if the fraction to divide by is zero
      * @throws ArithmeticException if the resulting numerator or denominator exceeds
      *  <code>Integer.MAX_VALUE</code>
      */
     public Fraction divide(Fraction fraction) {
         if (fraction == null) {
             throw new IllegalArgumentException("The fraction must not be null");
         }
         if (fraction.numerator == 0) {
             throw new ArithmeticException("The fraction to divide by must not be zero");
         }
         return multiply(fraction.invert());
     }
 
     // Basics
     //-------------------------------------------------------------------
 
     /**
      * <p>Compares this fraction to another object to test if they are equal.</p>.
      *
      * <p>Note that 2/4 is equal to 1/2 (unless you are using
      * deprecated methods).</p>
      *
      * @param obj the reference object with which to compare
      * @return <code>true</code> if this object is equal
      */
     public boolean equals(Object obj) {
         if (obj == this) {
             return true;
         }
         if (obj instanceof Fraction == false) {
             return false;
         }
         Fraction other = (Fraction) obj;
         return (getNumerator() == other.getNumerator() &&
                 getDenominator() == other.getDenominator());
     }
 
     /**
      * <p>Gets a hashCode for the fraction.</p>
      *
      * @return a hash code value for this object
      */
     public int hashCode() {
         if (hashCode == 0) {
             // hashcode update should be atomic.
             hashCode = 37 * (37 * 17 + getNumerator()) + getDenominator();
         }
         return hashCode;
     }
 
     /**
      * <p>Compares this object to another based on size.</p>
      *
     * @param other  the object to compare to
      * @return -1 if this is less, 0 if equal, +1 if greater
      * @throws ClassCastException if the object is not a <code>Fraction</code>
      * @throws NullPointerException if the object is <code>null</code>
      */
     public int compareTo(Fraction other) {
         if (this==other) {
             return 0;
         }
         if (numerator == other.numerator && denominator == other.denominator) {
             return 0;
         }
 
         // otherwise see which is less
         long first = (long) numerator * (long) other.denominator;
         long second = (long) other.numerator * (long) denominator;
         if (first == second) {
             return 0;
         } else if (first < second) {
             return -1;
         } else {
             return 1;
         }
     }
 
     /**
      * <p>Gets the fraction as a <code>String</code>.</p>
      *
      * <p>The format used is '<i>numerator</i>/<i>denominator</i>' always.
      *
      * @return a <code>String</code> form of the fraction
      */
     public String toString() {
         if (toString == null) {
             toString = new StringBuffer(32)
                 .append(getNumerator())
                 .append('/')
                 .append(getDenominator()).toString();
         }
         return toString;
     }
 
     /**
      * <p>Gets the fraction as a proper <code>String</code> in the format X Y/Z.</p>
      *
      * <p>The format used in '<i>wholeNumber</i> <i>numerator</i>/<i>denominator</i>'.
      * If the whole number is zero it will be ommitted. If the numerator is zero,
      * only the whole number is returned.</p>
      *
      * @return a <code>String</code> form of the fraction
      */
     public String toProperString() {
         if (toProperString == null) {
             if (numerator == 0) {
                 toProperString = "0";
             } else if (numerator == denominator) {
                 toProperString = "1";
             } else if ((numerator>0?-numerator:numerator) <= -denominator) {
                 // note that we do the magnitude comparison test above with
                 // NEGATIVE (not positive) numbers, since negative numbers
                 // have a larger range.  otherwise numerator==Integer.MIN_VALUE
                 // is handled incorrectly.
                 int properNumerator = getProperNumerator();
                 if (properNumerator == 0) {
                     toProperString = Integer.toString(getProperWhole());
                 } else {
                     toProperString = new StringBuffer(32)
                         .append(getProperWhole()).append(' ')
                         .append(properNumerator).append('/')
                         .append(getDenominator()).toString();
                 }
             } else {
                 toProperString = new StringBuffer(32)
                     .append(getNumerator()).append('/')
                     .append(getDenominator()).toString();
             }
         }
         return toProperString;
     }
 
     /** Backwards-compatible extension of Fraction.  Makes it appear that
      *  fractions are stored unsimplified. */
     private static class BadFraction extends Fraction {
 		private static final long serialVersionUID = 4001349100163830901L;
 		/** The unreduced denominator to use for this backwards-compatible
          *  fraction. */
         final int badDenom;
         /**
          * Creates a backwards-compatible Fraction representing an
          * unsimplified fraction.
          * @param f the simplified fraction whose numerical value we are
          *        to take
          * @param badDenom the unsimplified denominator whose absolute
          *        value we should use
          * @throws ArithmeticException if badDenom is Integer.MIN_VALUE
          *   (in which case we can't represent its absolute value).
          */
         BadFraction(Fraction f, int badDenom) {
             super(f.numerator, f.denominator);
             if (badDenom==Integer.MIN_VALUE) {
                 throw new ArithmeticException
                     ("overflow: can't negate denominator");
             }
             this.badDenom = Math.abs(badDenom);
         }
         /** Fake an unsimplified numerator.
          * @return an unreduced numerator for this fraction. */
         public int getNumerator() {
             return numerator * (badDenom/denominator);
         }
         /** Fake an unsimplified denominator.
          * @return an unreduced denominator for this fraction. */
         public int getDenominator() {
             return badDenom;
         }
         /** Fake an unsimplified proper numerator.
          * @return an unreduced 'proper' numerator for this fraction. */
         public int getProperNumerator() {
             return super.getProperNumerator() * (badDenom/denominator);
         }
         /** Return the conventional simplified fraction corresponding to
          * this backwards-compatible fraction.
          * @return a simplified fraction.
          */
         public Fraction reduce() {
             return new Fraction(this.numerator, this.denominator);
         }
         /** Invert this fraction, keeping it unsimplified.
          * @return an unreduced fraction with denominator equal to the
          *   unreduced numerator of this.
          */
         public Fraction invert() {
             return new BadFraction(super.invert(), getNumerator());
         }
         /** Negate this fraction, keeping it unsimplified.
          * @return an unreduced fraction with the same denominator as this.
          */
         public Fraction negate() {
             return new BadFraction(super.negate(), badDenom);
         }
         /** Take the absolute value of this fraction, keeping it
          *  unsimplified.
          * @return an unreduced fraction with the same denominator as this.
          */
         public Fraction abs() {
             return new BadFraction(super.abs(), badDenom);
         }
         /** Private helper function to compute an integer raised to
          *  an integer power, while checking for overflow.
          * @param n the base
          * @param pow the exponent
          * @return the value <code>n^pow</code>
          */
         private static int intpow(int n, int pow) {
             assert n>0;
             if (pow<0) {
                 return 0;
             } else if (pow==0) {
                 return 1;
             } else if (pow==1) {
                 return n;
             } else if ((pow%2)==0) {
                 return intpow(mulPosAndCheck(n,n), pow/2);
             } else {
                 return mulPosAndCheck(intpow(mulPosAndCheck(n,n), pow/2),n);
             }
         }
         /** Compute fraction raised to a power; keep fraction unsimplified.
          * @param power the exponent
          * @return an unsimplified fraction with the given unreduced
          *    denominator raised to the given power.
          */
         public Fraction pow(int power) {
             if (power<1) {
                 return super.pow(power);
             }
             return new BadFraction(super.pow(power), intpow(badDenom, power));
         }
         /** Ensure that we serialize BadFraction in a compatible manner.
          *  @return a special instance of the Fraction superclass that
          *  will serialize in a compatible manner (ie as an unsimplified
          *  fraction).
          */
         private Object writeReplace() {
             return new Fraction(getNumerator(), getDenominator(),
                                 true/*use no-check constructor*/);
         }
     }
 }
