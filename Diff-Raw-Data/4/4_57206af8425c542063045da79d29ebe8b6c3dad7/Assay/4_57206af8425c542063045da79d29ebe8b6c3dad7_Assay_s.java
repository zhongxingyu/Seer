 /*
  * Copyright 2006-2010 Gregor N. Purdy, Sr.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.exoprax.assay;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * <p>
  * To perform a data assay, you create an instance of this class and push attribute definitions
  * at it and observations of attribute values. It builds up a data structure representing the
  * unique values observed over all records for each attribute, and for each value infers a data
  * type.
  * </p>
  * 
  * <p>
  * You can access the results programmatically via the Map returned by getTableAssay(), or you
  * can use an instance of a class that implements the AssayOutput interface to format a report
  * for you.
  * </p>
  * 
  * @see Assayer for a class that coordinates an AssayInput, AssayOutput and Assay
  * @see AssayerRunner for a class that runs an Assayer in a separate thread
  * 
  * @author Gregor N. Purdy, Sr.
  */
 public class Assay {
 
     public final static Pattern singleDigitPattern = Pattern.compile("[0-9]");
 
     public final static Pattern integerPattern = Pattern.compile("^\\s*[-+]?\\s*0*([0-9]+)\\.?\\s*$");
 
     public final static Pattern decimalPattern = Pattern.compile("^\\s*[-+]?\\s*([0-9]*)\\.([0-9]+)\\s*$");
 
     public final static Pattern zonedPattern = Pattern.compile("^\\s*([0-9 /(){}\\[\\]:.-]*[0-9][0-9 /(){}\\[\\]:.-]*?)\\s*$");
 
     /**
      * TODO: Lots of other formats, including ones that aren't all numeric
      */
     public final static Pattern datePattern1 = Pattern.compile("^\\s*([0-9]{4}-[0-9]{2}-[0-9]{2})\\s*$");
     
     public final static Pattern datePattern2 = Pattern.compile("^\\s*([0-9]{2}-[0-9]{2}-[0-9]{4})\\s*$");
     
     public final static Pattern datePattern3 = Pattern.compile("^\\s*([0-9]{4}/[0-9]{2}/[0-9]{2})\\s*$");
     
     public final static Pattern datePattern4 = Pattern.compile("^\\s*([0-9]{2}/[0-9]{2}/[0-9]{4})\\s*$");
     
     public final static Pattern datePattern5 = Pattern.compile("^\\s*([a-z]{3} [0-9]{2} [0-9]{4})\\s*$", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
 
     /**
      * TODO: Lots of other formats, including ones that aren't all numeric (including time zones, too)
      */
     public final static Pattern timestampPattern = Pattern
             .compile("^\\s*[0-9]{4}-[0-9]{2}-[0-9]{2}\\s+[0-9]{2}:[0-9]{2}:[0-9]{2}[.][0-9]{3}\\s*$");
 
     public final static Pattern ssnPattern = Pattern.compile("^\\s*([0-9]{3}-[0-9]{2}-[0-9]{4})\\s*$");
 
     public final static Pattern einPattern = Pattern.compile("^\\s*([0-9]{2}-[0-9]{7})\\s*$");
 
     public final static Pattern telephonePattern = Pattern.compile("^\\s*([0-9]{3}[.-][0-9]{3}[.-][0-9]{4})\\s*$");
 
     public final static Pattern zipPattern = Pattern.compile("^\\s*([0-9]{5}-[0-9]{4})\\s*$");
 
     public final static Pattern telephone2Pattern = Pattern.compile("^\\s*([(][0-9]{3}[)]\\s*[0-9]{3}[.-][0-9]{4})\\s*$");
 
     public final static Pattern truePattern = Pattern.compile("^(true|t|yes|y)$", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
     
     public final static Pattern falsePattern = Pattern.compile("^(false|f|no|n)$", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
     
     public final static Pattern smallStringPattern = Pattern.compile("^\\p{Print}{1,3}$");
 
     public final static Pattern fillPattern = Pattern.compile("^(.)\\1{2,}$");
 
     public final static Pattern alphaPattern = Pattern.compile("^\\p{Alpha}+$");
 
     public final static Pattern alnumPattern = Pattern.compile("^\\p{Alnum}+$");
 
     public final static Pattern printPattern = Pattern.compile("^\\p{Print}+$");
 
     public final static Pattern newlinePattern = Pattern.compile("(\\n|\\r|\\t)");
 
     /**
      * TODO: This is not RFC822 compliant, probably not even if you leave out the quoting and real name stuff that standard allows
      */
     public final static Pattern emailPattern = Pattern.compile("^\\s*([a-zA-Z0-9_.+-]+@([a-zA-Z0-9_-]+\\.)+[a-zA-Z0-9_-]+)\\s*$");
 
     /**
      * TODO: What about multiline? Do our patterns work right? Should we have a "TEXT()" type for cases where the value is
      * multiline, but otherwise all printable characters and normal whitespace?
      */
     public static String classify(final String value) {
         if (value == null) {
             return "NULL";
         }
 
         if (value.equals("")) {
             return "EMPTY";
         }
 
         final Matcher integerMatcher = integerPattern.matcher(value);
 
         if (integerMatcher.matches()) {
             final int precision = integerMatcher.group(1).length();
 
             return String.format("INTEGER(%2d)", precision);
         }
 
         final Matcher decimalMatcher = decimalPattern.matcher(value);
 
         if (decimalMatcher.matches()) {
             final int precision = decimalMatcher.group(1).length() + decimalMatcher.group(2).length();
             final int scale = decimalMatcher.group(2).length();
 
             return String.format("DECIMAL(%2d, %2d)", precision, scale);
         }
         
         final Matcher dateMatcher5 = datePattern5.matcher(value);
 
         if (dateMatcher5.matches()) {
             return "DATE('MMM dd yyyy')";
         }
 
         final Matcher zonedMatcher = zonedPattern.matcher(value);
 
         if (zonedMatcher.matches()) {
             final Matcher dateMatcher1 = datePattern1.matcher(value);
 
             if (dateMatcher1.matches()) {
                 return "DATE('yyyy-??-??')";
             }
             
             final Matcher dateMatcher2 = datePattern2.matcher(value);
 
             if (dateMatcher2.matches()) {
                 return "DATE('??-??-yyyy')";
             }
             
             final Matcher dateMatcher3 = datePattern3.matcher(value);
 
             if (dateMatcher3.matches()) {
                 return "DATE('yyyy/??/??')";
             }
             
             final Matcher dateMatcher4 = datePattern4.matcher(value);
 
             if (dateMatcher4.matches()) {
                 return "DATE('??/??/yyyy')";
             }
 
             final Matcher timestampMatcher = timestampPattern.matcher(value);
 
             if (timestampMatcher.matches()) {
                 return "TIMESTAMP('yyyy-MM-dd hh:mm:ss.SSS')"; // TODO: Get the format right, once we accept more than one format
             }
 
             final Matcher ssnMatcher = ssnPattern.matcher(value);
 
             if (ssnMatcher.matches()) {
                 return "SSN('999-99-9999')";
             }
 
             final Matcher einMatcher = einPattern.matcher(value);
 
             if (einMatcher.matches()) {
                 return "EIN('99-9999999')";
             }
 
             final Matcher telephoneMatcher = telephonePattern.matcher(value);
 
             if (telephoneMatcher.matches()) {
                 final String temp = zonedMatcher.group(1);
                 final String format = singleDigitPattern.matcher(temp).replaceAll("9");
                 
                 return "TELEPHONE('" + format + "')";
             }
 
             final Matcher telephone2Matcher = telephone2Pattern.matcher(value);
 
             if (telephone2Matcher.matches()) {
                 final String temp = zonedMatcher.group(1);
                 final String format = singleDigitPattern.matcher(temp).replaceAll("9");
                 
                 return "TELEPHONE('" + format + "')";
             }
 
             final Matcher zipMatcher = zipPattern.matcher(value);
 
             if (zipMatcher.matches()) {
                 return "ZIP('99999-9999')";
             }
 
             final String temp = zonedMatcher.group(1);
 
             final String format = singleDigitPattern.matcher(temp).replaceAll("9");
 
             return "ZONED('" + format + "')";
         }
 
         final Matcher trueMatcher = truePattern.matcher(value);
         
         if (trueMatcher.matches()) {
             return "BOOLEAN:TRUE";
         }
 
         final Matcher falseMatcher = falsePattern.matcher(value);
         
         if (falseMatcher.matches()) {
             return "BOOLEAN:FALSE";
         }
         
         final Matcher smallStringMatcher = smallStringPattern.matcher(value);
 
         if (smallStringMatcher.matches()) {
             return String.format("CHAR(%4d)", value.length());
         }
 
         final Matcher fillMatcher = fillPattern.matcher(value);
 
         if (fillMatcher.matches()) {
             return String.format("FILL(%2d, '%s')", value.length(), fillMatcher.group(1));
         }
 
         final Matcher alphaMatcher = alphaPattern.matcher(value);
 
         if (alphaMatcher.matches()) {
             return String.format("ALPHA(%2d)", value.length());
         }
 
         final Matcher alnumMatcher = alnumPattern.matcher(value);
 
         if (alnumMatcher.matches()) {
             return String.format("ALNUM(%2d)", value.length());
         }
 
         final Matcher printMatcher = printPattern.matcher(value);
 
         if (printMatcher.matches()) {
             final Matcher emailMatcher = emailPattern.matcher(value);
 
             if (emailMatcher.matches()) {
                 return "EMAIL";
             }
 
             final Matcher newlineMatcher = newlinePattern.matcher(value);
 
             if (newlineMatcher.matches()) {
                 return String.format("TEXT(%4d)", value.length());
             }
 
             return String.format("CHAR(%4d)", value.length());
         }
 
         return String.format("DATA(%4d)", value.length());
     }
 
     private String title = "Untitled Assay";
 
     private final Map<String, String> columnTypes = new HashMap<String, String>();
 
     private final Map<String, Map<String, Map<String, Long>>> tableAssay = new HashMap<String, Map<String, Map<String, Long>>>();
 
     public Assay() {
         super();
     }
 
     public void addAttribute(final String name, final String type) {
         this.columnTypes.put(name, type);
     }
 
     public void addAttributeValue(final String name, final String value) {
         final String typeSpec = Assay.classify(value);
 
         Map<String, Map<String, Long>> columnAssay = this.tableAssay.get(name);
 
         if (columnAssay == null) {
             columnAssay = new HashMap<String, Map<String, Long>>();
             this.tableAssay.put(name, columnAssay);
         }
 
         Map<String, Long> valueAssay = columnAssay.get(typeSpec);
 
         if (valueAssay == null) {
             valueAssay = new HashMap<String, Long>();
             columnAssay.put(typeSpec, valueAssay);
         }
 
         final Long valueCount = valueAssay.get(value);
         final long temp = valueCount == null ? 0 : valueCount.longValue();
 
         valueAssay.put(value, Long.valueOf(temp + 1));
     }
 
     public Map<String, String> getColumnTypes() {
         return this.columnTypes;
     }
 
     public Map<String, Map<String, Map<String, Long>>> getTableAssay() {
         return this.tableAssay;
     }
 
     public String getTitle() {
         return this.title;
     }
 
     public void setTitle(final String title) {
         this.title = title;
     }
 
 }
