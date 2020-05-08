 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxConversionException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.value.*;
 import org.apache.commons.lang.StringUtils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Formatter;
 import java.util.List;
 import java.util.Locale;
 
 /**
  * Miscellaneous formatting utility functions.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class FxFormatUtils {
     public final static String DEFAULT_COLOR = "#000000";
     public final static String UNIVERSAL_TIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
 
     /**
      * Private constructor to avoid instantiation
      */
     private FxFormatUtils() {
     }
 
     /**
      * Returns true if the char is a valid RGB value.
      *
      * @param ch the character to check
      * @return true if the char is a valid RGB value
      */
     private static boolean isValidRGBChar(final char ch) {
         return (Character.isDigit(ch) || ch == 'A' || ch == 'B' || ch == 'C' || ch == 'D'
                 || ch == 'E' || ch == 'F');
     }
 
     /**
      * Checks if a value is a RGB color code.
      * <p/>
      * The RGB color code my start with a '#', but is also recognized without it.
      *
      * @param value the string to check
      * @return true if the value is a valid RGB color code
      */
     public static boolean isRGBCode(String value) {
         if (value == null || value.length() < 6) return false;
         String rgbValue = value.trim();
         rgbValue = rgbValue.toUpperCase();
         if (rgbValue.charAt(0) == '#' && rgbValue.length() != 7) return false;
         if (rgbValue.charAt(0) != '#' && rgbValue.length() != 6) return false;
         // Cut away the leading'#'
         if ((rgbValue.charAt(0)) == '#') {
             rgbValue = rgbValue.substring(1);
         }
         // Check all digits
         for (int i = 0; i < rgbValue.length(); i++) {
             if (!isValidRGBChar(rgbValue.charAt(i))) return false;
         }
         // Passed all tests
         return true;
     }
 
     /**
      * Converts "#FF0000" or "FF0000" rgb values to style="color:#FF0000" and all other style values
      * to class="XXX".
      *
      * @param code the color code
      * @return the style or class value
      */
     public static String colorCodeToStyle(String code) {
         if (code == null) return "";
         String colorCode = code.trim();
         if (colorCode != null && colorCode.length() > 0) {
             if (isRGBCode(colorCode)) {
                 if (colorCode.charAt(0) != '#') colorCode = '#' + colorCode;
                 colorCode = "style=\"color:" + colorCode + "\" ";
             } else {
                 colorCode = "class=\"" + colorCode + "\" ";
             }
         }
         return colorCode;
     }
 
     /**
      * Checks the email. Also allows local email addresses without a domain (e.g. 'root@localhost').
      *
      * @param email the email to check
      * @return the email without whitespaces
      * @throws com.flexive.shared.exceptions.FxInvalidParameterException
      *          if the email is invalid
      */
     public static String checkEmail(String email) throws FxInvalidParameterException {
         final boolean valid = email != null && email.indexOf('@') > 0
                 && (StringUtils.countMatches(email, "@") == 1)
                 && (email.indexOf('@') < email.length() - 1);
         if (!valid) {
             throw new FxInvalidParameterException("EMAIL", "ex.account.email.invalid", email);
         }
         return email.trim();
     }
 
 
     /**
      * Checks the password and encodes it.
      *
      * @param accountId the account ID (needed for computing the hash)
      * @param password  unencoded the password
      * @return the encoded password
      * @throws FxInvalidParameterException if the password is invalid (too short, too simple, ..)
      */
     public static String encodePassword(long accountId, final String password) throws FxInvalidParameterException {
         if (password.length() < 6)
             throw new FxInvalidParameterException("PASSWORD", "ex.account.password.tooShort");
         return FxSharedUtils.hashPassword(accountId, password);
     }
 
     /**
      * Check if a color value is valid.
      * <p/>
      * The color may be a RGB value (recognized by a starting '#') or a css class name.<br>
      * The function returns the default color if value is empty.<br>
      * If the value defines an invalid RGB value a FxInvalidParameterException is thrown.<br>
      *
      * @param paramName the name of the parameter that is used when a FxInvalidParameterException is thrown
      * @param value     the color alue
      * @return the (corrected) color
      * @throws FxInvalidParameterException if the color is invalid
      */
     public static String processColorString(String paramName, String value)
             throws FxInvalidParameterException {
         if (value == null || value.length() == 0) {
             return DEFAULT_COLOR;
         }
         if (value.charAt(0) == '#') {
             if (value.length() != 7) {
                 throw new FxInvalidParameterException("Invalid color for property [" + paramName + "]", paramName);
             }
             for (int i = 1; i < 7; i++) {
                 char aChar = value.charAt(i);
                 if (!Character.isDigit(aChar) && (aChar != 'A') && (aChar != 'B') && (aChar != 'C')
                         && (aChar != 'D') && (aChar != 'E') && (aChar != 'F')) {
                     throw new FxInvalidParameterException("Invalid color for property [" + paramName + "]", paramName);
                 }
             }
         }
         return value;
     }
 
     /**
      * Escape a path to allow only a-zA-Z0-9/ and replace all other letters with underscores (_)
      *
      * @param path the path to escape
      * @return escaped path
      */
     public static String escapeTreePath(final String path) {
         StringBuilder sb = new StringBuilder(path.length());
         char c;
         boolean inTag = false;
         for (int i = 0; i < path.length(); i++) {
             c = path.charAt(i);
             if (c == '<' && !inTag) {
                 inTag = true;
                 continue;
             }
             if (c == '>' && inTag) {
                 inTag = false;
                 continue;
             }
             if (inTag)
                 continue;
             if ((c >= '0' && c <= '9') || (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '/')
                 sb.append(c);
             else {
                 if ((i > 0 && path.charAt(i - 1) != '_') || i == 0)
                     sb.append('_');
             }
         }
         if (sb.length() == 0)
             sb.append('_');
         return sb.toString();
     }
 
     public static String toString(Date date) {
         return getDateFormat().format(date);
     }
 
     private static SimpleDateFormat getGermanDateFormat() {
         return new SimpleDateFormat("dd.MM.yyyy", Locale.ENGLISH);
     }
 
     /**
      * Is the value quoted by ' or " ?
      *
      * @param value value to check
      * @return if quoted by ' or " ?
      */
     public static boolean isQuoted(String value) {
         return !StringUtils.isEmpty(value) && ((value.endsWith("'") && value.startsWith("'")) || (value.endsWith("\"") && value.startsWith("\"")));
     }
 
     /**
      * Unquote a quoted value
      *
      * @param value value to unquote
      * @return unquoted calue
      */
     public static String unquote(String value) {
         if (!isQuoted(value))
             return value;
         return value.substring(1, value.length() - 1);
     }
 
     /**
      * Turns an array of bytes into a String representing each byte as an unsigned
      * hex number.
      *
      * @param bytes an array of bytes to convert to a hex-string
      * @return generated hex string
      */
     public static String encodeHex(byte[] bytes) {
         StringBuffer buf = new StringBuffer(bytes.length * 2);
         int i;
         for (i = 0; i < bytes.length; i++) {
             if (((int) bytes[i] & 0xff) < 0x10) {
                 buf.append("0");
             }
             buf.append(Long.toString((int) bytes[i] & 0xff, 16));
         }
         return buf.toString();
     }
 
     /**
      * Format a resource, replacing all {x} by the appropriate value in values.
      * If values is an instance of FxString the given localeId is used
      *
      * @param resource   String containing {0}..{n} placeholders
      * @param languageId used locale if values contain FxString instances
      * @param values     either FxString, FxException or any Object using toString()
      * @return formatted resource
      */
     public static String formatResource(String resource, long languageId, Object... values) {
         if (resource == null)
             return "";
         StringBuffer msg = new StringBuffer((int) (resource.length() * 1.5));
         int pos = 0;
         boolean inRep = false;
         int index;
         StringBuffer sbIdx = new StringBuffer(5);
         while (pos < resource.length()) {
             if (resource.charAt(pos) == '{') {
                 inRep = true;
                 sbIdx.setLength(0);
             } else if (resource.charAt(pos) == '}' && inRep) {
                 inRep = false;
                 try {
                     index = Integer.parseInt(sbIdx.toString());
                 } catch (NumberFormatException e) {
                     index = -1;
                 }
                 if (index >= 0 && index < values.length) {
                     final Object value = values[index];
                     if (value instanceof FxString) {
                         if (((FxString) value).translationExists(languageId))
                             msg.append(((FxString) value).getTranslation(languageId));
                         else
                             msg.append(((FxString) value).getDefaultTranslation());
                     } else if (value instanceof FxApplicationException) {
                         msg.append(((FxApplicationException) value).getMessage(languageId));
                     } else if (value instanceof FxRuntimeException) {
                         msg.append(((FxRuntimeException) value).getMessage(languageId));
                     } else
                         msg.append(String.valueOf(value));
                 } else {
                     // append unset parameter
                     msg.append("{").append(index).append("}");
                 }
             } else if (!inRep)
                 msg.append(resource.charAt(pos));
             if (inRep && resource.charAt(pos) >= '0' && resource.charAt(pos) <= '9')
                 sbIdx.append(resource.charAt(pos));
             pos++;
         }
         return msg.toString();
     }
 
     /**
      * Escape text content for output in a javascript function call parameter,
      * based on the Struts TagUtils#filter function.
      *
      * @param value           the text content
      * @param replaceNewlines replaces linebreaks with html <br> tags if set to true,
      *                        or simply removes them (replaces them with spaces) if set to false.
      * @param filterHtml      filter html?
      * @return the escaped text
      */
     public static String escapeForJavaScript(String value, boolean replaceNewlines, boolean filterHtml) {
         if (value != null) {
             // based on org.apache.struts.util.ResponseUtils.filter, custom handling of HTML identifiers
             StringBuffer result = null;
             String filtered;
             for (int i = 0; i < value.length(); i++) {
                 char lookAhead = i < value.length() - 1 ? value.charAt(i + 1) : '\0';
                 filtered = null;
                 switch (value.charAt(i)) {
                     case '<':
                         if (filterHtml)
                             filtered = "&lt;";
                         break;
                     case '>':
                         if (filterHtml)
                             filtered = "&gt;";
                         break;
                     case '&':
                         if (filterHtml && lookAhead != '#')
                             filtered = "&amp;";
                         break;
                     case '"':
                         filtered = "&quot;";
                         break;
                     case '\'':
                         filtered = "&#39;";
                         break;
                     case '\r':
                         filtered = (replaceNewlines && lookAhead != '\n') ? "<br/>" : " ";
                         break;
                     case '\n':
                         filtered = replaceNewlines ? "<br/>" : " ";
                         break;
                 }
 
                 if (result == null) {
                     if (filtered != null) {
                         result = new StringBuffer(value.length() + 50);
                         if (i > 0) {
                             result.append(value.substring(0, i));
                         }
                         result.append(filtered);
                     }
                 } else {
                     if (filtered == null) {
                         result.append(value.charAt(i));
                     } else {
                         result.append(filtered);
                     }
                 }
             }
             return result == null ? value : result.toString();
         } else {
             return "";
         }
     }
 
     /**
      * Escape text content for output in a javascript function call parameter.
      *
      * @param value           the text content
      * @param replaceNewlines replaces linebreaks with html <br> tags if set to true,
      *                        or simply removes them (replaces them with spaces) if set to false.
      * @return the escaped text
      */
     public static String escapeForJavaScript(String value, boolean replaceNewlines) {
         return escapeForJavaScript(value, replaceNewlines, false);
     }
 
     /**
      * Escape text content for output in a javascript function call parameter.
      *
      * @param value the text content
      * @return the escaped text
      */
     public static String escapeForJavaScript(String value) {
         return escapeForJavaScript(value, false, false);
     }
 
     /**
      * Generic SQL escape method.
      *
      * @param value the value to be formatted
      * @return the formatted value
      */
     public static String escapeForSql(Object value) {
         if (value instanceof FxValue) {
             return ((FxValue) value).getSqlValue();
         } else if (value instanceof String) {
             return "'" + StringUtils.replace((String) value, "'", "''") + "'";
         } else if (value instanceof Date) {
             return "'" + new Formatter().format("%tF", (Date) value) + "'";
         } else if (value instanceof FxSelectListItem) {
             return String.valueOf(((FxSelectListItem) value).getId());
         } else if (value instanceof SelectMany) {
             final SelectMany selectMany = (SelectMany) value;
             final List<Long> selectedIds = selectMany.getSelectedIds();
             if (selectedIds.size() > 1) {
                 return "(" + StringUtils.join(selectedIds.iterator(), ',') + ")";
             } else if (selectedIds.size() == 1) {
                 return String.valueOf(selectedIds.get(0));
             } else {
                 return "-1";
             }
         } else if (value != null) {
             return value.toString();
         } else {
             return "null";
         }
     }
 
     /**
      * Returns a basic date/time format that is readable but not localized.
      *
      * @return a basic date/time format that is readable but not localized.
      */
     public static SimpleDateFormat getDateTimeFormat() {
         return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
     }
 
     /**
      * Returns a formatter for the universal date/time format
      *
      * @return formatter for the universal date/time format
      */
     public static SimpleDateFormat getUniversalDateTimeFormat() {
         return new SimpleDateFormat(UNIVERSAL_TIMEFORMAT);
     }
 
     /**
      * Returns a basic date format that is readable but not localized.
      *
      * @return a basic date format that is readable but not localized.
      */
     public static SimpleDateFormat getDateFormat() {
         return new SimpleDateFormat("yyyy-MM-dd");
     }
 
     /**
      * Convert a String to Boolean
      *
      * @param value value to convert
      * @return Boolean
      */
     public static Boolean toBoolean(String value) {
         try {
             value = unquote(value);
             return Boolean.parseBoolean(value) || "1".equals(value);
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxBoolean.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to Integer
      *
      * @param value value to convert
      * @return Integer
      */
     public static Integer toInteger(String value) {
         try {
             return Integer.parseInt(unquote(value));
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxNumber.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to Long
      *
      * @param value value to convert
      * @return Long
      */
     public static Long toLong(String value) {
         try {
             return Long.parseLong(unquote(value));
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxLargeNumber.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to Double
      *
      * @param value value to convert
      * @return Double
      */
     public static Double toDouble(String value) {
         try {
             return Double.parseDouble(unquote(value).replace(',', '.'));
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxDouble.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to Float
      *
      * @param value value to convert
      * @return Float
      */
     public static Float toFloat(String value) {
         try {
             return Float.parseFloat(unquote(value).replace(',', '.'));
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxFloat.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to Date
      *
      * @param value value to convert
      * @return Date
      */
     public static Date toDate(String value) {
         try {
             //TODO: use a better date parser
             try {
                 return getDateFormat().parse(unquote(value));
             } catch (ParseException e) {
                 //fallback to universal format if "short" format is no match
                 return new SimpleDateFormat(UNIVERSAL_TIMEFORMAT).parse(unquote(value));
             }
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxDate.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Convert a String to DateTime
      *
      * @param value value to convert
      * @return Date
      */
     public static Date toDateTime(String value) {
         try {
             //TODO: use a better date parser
             try {
                 return getDateTimeFormat().parse(unquote(value));
             } catch (ParseException e) {
                 try {
                    return getDateFormat().parse(unquote(value));
                } catch (ParseException e2) {
                     //fallback to universal format if "short" format is no match
                     return new SimpleDateFormat(UNIVERSAL_TIMEFORMAT).parse(unquote(value));
                 }
             }
         } catch (Exception e) {
             throw new FxConversionException(e, "ex.conversion.value.error", FxDate.class.getCanonicalName(), value,
                     e.getMessage()).asRuntimeException();
         }
     }
 }
