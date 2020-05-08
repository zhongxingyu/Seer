 /*
  * Copyright (c) 2010, Regents of the University of Colorado
  * 
  * All rights reserved. Redistribution and use in source and binary forms, 
  * with or without modification, are permitted provided that the following 
  * conditions are met:
  * 
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution. 
  * Neither the name of the University of Colorado nor the names of its
  * contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
  * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
  * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package lasp.tss.variable;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.TimeZone;
 
 import org.apache.log4j.Logger;
 
 import lasp.tss.TSSException;
 import lasp.tss.TSSPublicException;
 import lasp.tss.util.JulianDate;
 import lasp.tss.util.RegEx;
 
 import ucar.ma2.Array;
 import ucar.ma2.DataType;
 import ucar.nc2.Attribute;
 import ucar.nc2.Variable;
 import ucar.nc2.units.DateUnit;
 
 /**
  * Variable to represent the time variable.
  * 
  * @author Doug Lindholm
  */
 public class TimeVariable extends IndependentVariable {
 
     // Initialize a logger.
     private static final Logger _logger = Logger.getLogger(TimeVariable.class);
     
     /**
      * The units from the native data.
      */
     private String _origUnits;
     
     private String _origFormat;
     
     /**
      * Keep a cached copy of the DateUnit so we don't have to remake it
      */
     private DateUnit _dateUnit; 
     
     /**
      * The format for the string representation of the time, if defined.
      * If time unit is "formatted", the data will be stored as doubles
      * in Java/Unix time: milliseconds since 1970-01-01 00:00 GMT.
      */
     private String _format;
     
     /**
      * Is the time in units of Julian Date.
      * Hack since DateUnit doesn't support Julian Date.
      */
     private boolean _isJulian;
 
     /**
      * Keep a collection of DateFormat-s mapped to by its String representation.
      * For performance purposes.
      */
     private HashMap<String,DateFormat> _dateFormatMap;
 
     public static String DEFAULT_TIME_UNIT = "milliseconds since 1970-01-01T00:00";
     
     /**
      * Construct a TimeVariable based on its nc2.Variable.
      */
     public TimeVariable(CompositeVariable parent, Variable ncVariable) {
         super(parent, ncVariable);
         
         _dateFormatMap = new HashMap<String,DateFormat>();
         parseTimeUnits();
     }
 
     /**
      * Does this variable have a non-numeric representation.
      */
     private boolean isFormatted() {
         return (_format != null);
     }
     
     /**
      * Is the time in units of Julian Date.
      */
     private boolean isJulian() {
         return _isJulian;
     }
     
     /**
      * Make sense of the time units.
      * Three cases: Julian Date, formatted, duration since epoch
      */
     private void parseTimeUnits() {
         String timeUnit = getUnits(); 
         if (timeUnit == null) timeUnit = DEFAULT_TIME_UNIT;  //default to Unix/Java time, 
         
         //Keep the original units since the "units" attribute might change
         //to express the units to be served.
         _origUnits = timeUnit;
         
         if (timeUnit.contains("since")) {
             //numeric unit, manage units with NetCDF DateUnit
             try {
                 _dateUnit = new DateUnit("0 "+timeUnit);
             } catch (Exception e) {
                 String msg = "Failed to parse time unit: " + timeUnit;
                 _logger.error(msg, e);
                 throw new TSSException(msg, e);
             }
         } else if (timeUnit.toLowerCase().startsWith("julian")) {
             _isJulian = true;
         } else {
             //formatted time stamp
             _format = timeUnit;
             _origFormat = timeUnit;
         }
     }
     
     /**
      * Allow the format to be set (e.g. by format_time filter).
      * The underlying data won't be modified.
      * Formatting will happen as data are requested via getStringValues().
      */
     public void setFormat(String format) {
         //TODO: validate
         _format = format;
         
         //change units attribute
         Variable ncvar = getNetcdfVariable();
         Attribute att = new Attribute("units", format);
         ncvar.addAttribute(att);
         //change variable type to String
         ncvar.setDataType(DataType.STRING);
     }
     
     /**
      * Return the format string for this Time variable.
      * It should be the same as the units attribute if it is formatted,
      * otherwise it will return null.
      */
     public String getFormat() {
         return _format;
     }
     
     /**
      * Return the time represented by the given string as a double.
      * May involve converting an ISO format to the defined time unit.
      */
     public double parseValue(String s) {
         double d = Double.NaN;
         
         if (s.matches(RegEx.TIME)) {
             d = convertIsoTime(s);
         } else if (_origFormat != null) {
             //native formatted time, return default time
             try {
                 DateFormat dateFormat = getDateFormat(_origFormat);
                 Date date = dateFormat.parse(s);
                 d = date.getTime();
             } catch (ParseException e) {
                 String msg = "Unable to parse \""+s+"\" using the native time format: " + _origFormat;
                 _logger.error(msg, e);
                 throw new TSSPublicException(msg, e);
             }
         } else d = Double.parseDouble(s);
         
         return d;
     }
     
     /**
      * Return the given value as a java.util.Date, making use of the units.
      */
     public Date getValueAsDate(double time) {
         Date date = null;
         
         if (isJulian()) {
             JulianDate jd = new JulianDate(time);
             date = jd.getDate();
         } else if (_dateUnit != null) {
             date = _dateUnit.makeDate(time);
         } else {
             date = new Date((long) time); //assume Unix/Java time
         }
             
         return date;
     }
     
     /**
      * Override to support formatted times.
      */
     public String[] getStringValues(int timeIndex) {
         String[] ss = new String[1]; //only one time value per time index
        
         if (_origFormat == _format) {
             //format unchanged, or both null if numbers
             Array array = getTimeSample(timeIndex);
             ss[0] = array.getObject(0).toString();
         }
         else {
             double[] times = getValues(timeIndex);
             double time = times[0];
             
             if (isFormatted()) {
                 ss[0] = format(_format, time);
             } else {
                 ss[0] = ""+time;
             }
         }
         
         return ss;
     }
     
     /**
      * Override to deal with formatted native time.
      */
     public double[] getValues(int timeIndex) {
         double[] dd = new double[1];
         
         //If the native data are formatted, convert to Java time
         if (_origFormat != null) {
             try {
                 Array array = getTimeSample(timeIndex);
                String s = (String) array.getObject(0);
                 //String s = array.next().toString();
                 DateFormat dateFormat = getDateFormat(_origFormat);
                 Date date = dateFormat.parse(s);
                 dd[0] = date.getTime();
             } catch (ParseException e) {
                String msg = "Unable to parse native time format: " + _origFormat;
                 _logger.warn(msg);
                 //throw new TSSException(msg, e);
                 dd = null;
             }
         } else {
             dd = super.getValues(timeIndex);
         }
         
         return dd;
     }
     
     /**
      * Override to deal with formatted time strings.
      * Return as doubles.
      */
     public double[] getValues() {
         double[] d = null;
         
         Array array = read();
         if (array == null) return null;
         
         if (isFormatted()) {
             //parse formatted times only once
             //String format = getNetcdfVariable().findAttribute("format").getStringValue();
             DateFormat dateFormat = getDateFormat(_format);
             
             int n = (int) array.getSize();
             d = new double[n];
             
             int i = 0;
             while(array.hasNext()) {
                 String s = array.next().toString(); //could be string or number
                 try {
                     Date date = dateFormat.parse(s);
                     d[i] = date.getTime();
                 } catch (ParseException e) {
                     String msg = "Failed to parse time value: " + s;
                     _logger.warn(msg);
                     //throw new TSSException(msg, e);
                     d[i] = Double.NaN;
                 }
                 i++;
             }
 
         } else d = super.getValues();
         
         return d;
     }
 
     /**
      * Convert the ISO 8601 formatted time to a double in the predefined units.
      */
     private double convertIsoTime(String isoTime) {
         double value = Double.NaN;
         
         Date date = DateUnit.getStandardOrISO(isoTime);
 
         //special handling for JuianDate since DateUnit does not support it.
         if (isJulian()) {
             JulianDate jd = new JulianDate(date);
             value = jd.getJulianDate();
         } else if (isFormatted()) {
             value = date.getTime(); //Unix/Java time
         } else {
             value = _dateUnit.makeValue(date);
         }
 
         return value;
     }
     
     /**
      * Apply the format to the time value (in predefined units) and return as a String.
      */
     public String format(String format, double time) {
         Date date = getValueAsDate(time);
         
         DateFormat df = getDateFormat(format);
         String formattedTime = df.format(date);
 
         return formattedTime;
     }
     
     /**
      * Get the DateFormat object for the given format string.
      */
     private DateFormat getDateFormat(String format) {
         //see if we have already made one
         DateFormat dateFormat = _dateFormatMap.get(format);
         
         try {
             if (dateFormat == null) {
                 dateFormat = new SimpleDateFormat(format);
                 dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); //assumes GMT time zone
                 _dateFormatMap.put(format, dateFormat);
             }
         } catch (Exception e) {
             String msg = "Unable to parse the time format: " + format;
             _logger.error(msg, e);
             throw new TSSPublicException(msg, e);
         }
 
         return dateFormat;
     }
     
     /**
      * Return the data type for the DDS.
      */
     public String getDodsType() {
         String type = null;
         
         if (isFormatted()) type = "String";
         else type = super.getDodsType();
         
         return type;
     }
     
 }
