 /*
  * $Id$
  */
 package org.xins.types.standard;
 
 import org.xins.types.Type;
 import org.xins.types.TypeValueException;
 import org.xins.util.MandatoryArgumentChecker;
 import org.xins.util.text.FastStringBuffer;
 
 /**
  * Standard type <em>_date</em>.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.166
  */
 public class Date extends Type {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * The only instance of this class. This field is never <code>null</code>.
     */
    public final static Date SINGLETON = new Date();
 
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a <code>Date.Value</code> from the specified string
     * which is guaranteed to be non-<code>null</code>.
     *
     * @param string
     *    the string to convert, cannot be <code>null</code>.
     *
     * @return
     *    the {@link Value} object, never <code>null</code>.
     *
     * @throws IllegalArgumentException
     *    if <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Value fromStringForRequired(String string)
    throws IllegalArgumentException, TypeValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("string", string);
 
       return (Value) SINGLETON.fromString(string);
    }
 
    /**
     * Constructs a <code>Date.Value</code> from the specified string.
     *
     * @param string
     *    the string to convert, can be <code>null</code>.
     *
     * @return
     *    the {@link Value}, or <code>null</code> if
     *    <code>string == null</code>.
     *
     * @throws TypeValueException
     *    if the specified string does not represent a valid value for this
     *    type.
     */
    public static Value fromStringForOptional(String string)
    throws TypeValueException {
       return (Value) SINGLETON.fromString(string);
    }
 
    /**
     * Converts the specified <code>Date.Value</code> to a string.
     *
     * @param value
     *    the value to convert, can be <code>null</code>.
     *
     * @return
     *    the textual representation of the value, or <code>null</code> if and
     *    only if <code>value == null</code>.
     */
    public static String toString(Value value) {
 
       // Short-circuit if the argument is null
       if (value == null) {
          return null;
       }
 
       return toString(value.getYear(),
                       value.getMonthOfYear(),
                       value.getDayOfMonth());
    }
 
    /**
     * Converts the specified combination of a year, month and day to a string.
     *
     * @param year
     *    the year, must be &gt;=0 and &lt;= 9999.
     *
     * @param month
     *    the month of the year, must be &gt;= 1 and &lt;= 12.
     *
     * @param day
     *    the day of the month, must be &gt;= 1 and &lt;= 31.
     *
     * @return
     *    the textual representation of the value, never <code>null</code>.
     */
    private static String toString(int year, int month, int day) {
 
      // Short-circuit if the argument is null
      if (value == null) {
         return null;
      }

       // Use a buffer to create the string
       FastStringBuffer buffer = new FastStringBuffer(8);
 
       // Append the year
       if (year < 10) {
          buffer.append("000");
       } else if (year < 100) {
          buffer.append("00");
       } else if (year < 1000) {
          buffer.append('0');
       }
       buffer.append(year);
 
       // Append the month
       if (month < 10) {
          buffer.append('0');
       }
       buffer.append(month);
 
       // Append the day
       if (day < 10) {
          buffer.append('0');
       }
       buffer.append(day);
 
       return buffer.toString();
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>Date</code> instance.
     * This constructor is private, the field {@link #SINGLETON} should be
     * used.
     */
    private Date() {
       super("date", Value.class);
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    protected final boolean isValidValueImpl(String value) {
 
       // First check the length
       if (value.length() != 8) {
          return false;
       }
 
       // Convert all 3 components of the string to integers
       int y, m, d;
       try {
          y = Integer.parseInt(value.substring(0, 4));
          m = Integer.parseInt(value.substring(4, 2));
          d = Integer.parseInt(value.substring(6, 2));
       } catch (NumberFormatException nfe) {
          return false;
       }
 
       // Check that the values are in the correct range
       return (y >= 0) && (m >= 1) && (m <= 12) && (d >= 1) && (d <= 31);
    }
 
    protected final Object fromStringImpl(String string)
    throws TypeValueException {
 
       // Convert all 3 components of the string to integers
       int y, m, d;
       try {
          y = Integer.parseInt(string.substring(0, 4));
          m = Integer.parseInt(string.substring(4, 2));
          d = Integer.parseInt(string.substring(6, 2));
       } catch (NumberFormatException nfe) {
 
          // Should never happen, since isValidValueImpl(String) will have been
          // called
          throw new TypeValueException(this, string);
       }
 
       // Check that the values are in the correct range
       return new Value(y, m, d);
    }
 
    public final String toString(Object value)
    throws IllegalArgumentException, ClassCastException, TypeValueException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("value", value);
 
       // The argument must be a PropertyReader
       return toString((Value) value);
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Date value, composed of a year, month and a day.
     *
     * @version $Revision$ $Date$
     * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
     *
     * @since XINS 0.166
     */
    public static final class Value {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new date value. The values will not be checked.
        *
        * @param year
        *    the year, e.g. <code>2004</code>.
        *
        * @param month
        *    the month of the year, e.g. <code>11</code> for November.
        *
        * @param day
        *    the day of the month, e.g. <code>1</code> for the first day of the
        *    month.
        */
       Value(int year, int month, int day) {
          _year  = year;
          _month = month;
          _day   = day;
 
          _asString = toString(year, month, day);
       }
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The year. E.g. <code>2004</code>.
        */
       private final int _year;
 
       /**
        * The month of the year. E.g. <code>11</code> for November.
        */
       private final int _month;
 
       /**
        * The day of the month. E.g. <code>1</code> for the first day of the
        * month.
        */
       private final int _day;
 
       /**
        * Textual representation of this date. Composed of the year (YYYY),
        * month (MM) and day (DD) in the format: <em>YYYYMMDD</em>.
        */
       private final String _asString;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Returns the year.
        *
        * @return
        *    the year, between 0 and 9999 (inclusive).
        */
       public int getYear() {
          return _year;
       }
 
       /**
        * Returns the month of the year.
        *
        * @return
        *    the month of the year, between 1 and 12 (inclusive).
        */
       public int getMonthOfYear() {
          return _month;
       }
 
       /**
        * Returns the day of the month.
        *
        * @return
        *    the day of the month, between 1 and 31 (inclusive).
        */
       public int getDayOfMonth() {
          return _day;
       }
 
       public String toString() {
          return _asString;
       }
    }
 }
