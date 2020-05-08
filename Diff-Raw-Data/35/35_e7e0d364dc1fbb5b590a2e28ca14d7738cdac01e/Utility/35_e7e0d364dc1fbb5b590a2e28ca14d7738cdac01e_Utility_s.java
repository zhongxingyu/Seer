 /** SVN FILE: $Id: Utility.java 8005 2013-09-11 12:47:10Z jal55 $ */
 package edu.psu.iam.cpr.core.util;
 
 import edu.psu.iam.cpr.core.database.beans.MessageConsumer;
 import edu.psu.iam.cpr.core.database.types.CprPropertyName;
 import edu.psu.iam.cpr.core.database.types.GenderType;
 import edu.psu.iam.cpr.core.error.ReturnType;
 import org.apache.commons.httpclient.HttpStatus;
 
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Generic utility class.
  * <p/>
  * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivs 3.0 United States License. To
  * view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/us/ or send a letter to Creative
  * Commons, 444 Castro Street, Suite 900, Mountain View, California, 94041, USA.
  *
  * @author $Author: jal55 $
  * @version $Rev: 8005 $
  * @package edu.psu.iam.cpr.core.util
  * @lastrevision $Date: 2013-09-11 08:47:10 -0400 (Wed, 11 Sep 2013) $
  */
 public final class Utility {
 
     /**
      * Contains the value for a forward slash character
      */
     private static final String FORWARD_SLASH = "/";
 
     /**
      * Constant fifty nine
      */
     private static final int FIFTY_NINE = 59;
 
     /**
      * Constant twenty three
      */
     private static final int TWENTY_THREE = 23;
 
     /**
      * Constant zero
      */
     private static final int ZERO = 0;
 
     private static final int MAX_NUMBER_OF_GROUPS = 8;
     private static final int MAX_GROUP_LENGTH = 4;
 
     /**
      * Pattern for pretty-formatting an int/long
      */
     private static final String INTEGER_PATTERN = "###,###,###,##0";
 
     /**
      * Contains the formatter string to convert a date/time to ISO 8601 format
      */
     private static final String ISO8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
 
     /**
      * Formatter object that translates a date to a ISO 8601 compliant format
      */
     private static final DateFormat ISO8601_FORMATTER = new SimpleDateFormat(ISO8601_DATE_FORMAT);
 
     /**
      * Contains the value for the UTC timezone string
      */
     private static final String UTC_TIMEZONE = "UTC";
 
     /**
      * Constructor
      */
     private Utility() {
 
     }
 
     /**
      * This routine is used to convert a java.util.Date to a String formatted based on the value of a property.
      *
      * @param d contains the date to be converted.
      * @return will return a string representation of the date.
      */
     public static String convertDateToString(final Date d) {
         if (d == null) {
             return null;
         } else {
             final SimpleDateFormat dateFormat = new SimpleDateFormat(CprProperties.INSTANCE.getProperties().getProperty(
                     CprPropertyName.CPR_FORMAT_DATE.toString()));
             return dateFormat.format(d);
         }
     }
 
     /**
      * This routine is used to convert a java.util.Date (timestamp) to a String formatted based on the value of a property.
      *
      * @param d contains the timestamp to be converted.
      * @return will return a string representation of the timestamp.
      */
     public static String convertTimestampToString(final Date d) {
         if (d == null) {
             return null;
         } else {
             final SimpleDateFormat dateFormat = new SimpleDateFormat(CprProperties.INSTANCE.getProperties().getProperty(
                     CprPropertyName.CPR_FORMAT_TIMESTAMP.toString()));
             return dateFormat.format(d);
         }
     }
 
     /**
      * This routine is used to convert a string to an enumerated type.
      *
      * @param enumClass   Contains the enumerated type's class.
      * @param stringValue Contains the string that will be used to convert to an enum.
      * @return will return the enumerated value if successful.
      */
     public static <T extends Enum<T>> T getEnumFromString(final Class<T> enumClass, final String stringValue) {
         if (enumClass != null && stringValue != null) {
             try {
                 return Enum.valueOf(enumClass, stringValue.trim().toUpperCase());
             } catch (final IllegalArgumentException ex) { // $codepro.audit.disable emptyCatchClause, logExceptions
             }
         }
         return null;
     }
 
     /**
      * This routine is a helper to convert a long value to a string, it handles the case where a null pointer is passed in.
      *
      * @param value contains the value to be converted.
      * @return will return a null value if one was specified, otherwise it will return the string representation of the long value.
      */
     public static String safeConvertLongToString(final Long value) {
         if (value == null) {
             return null;
         } else {
             return Long.toString(value);
         }
     }
 
     /**
      * This routine is used to safely convert a string value to a Long.
      *
      * @param value contains the string value to be converted.
      * @return will return a long if successful.
      */
     public static Long safeConvertStringToLong(final String value) {
         if (value == null) {
             return -1L;
         } else {
             return Long.valueOf(value);
         }
     }
 
     /**
      * check all fields supplied to see if they have data
      *
      * @param names a variable argument list of fields
      * @return 'true' if all fields contain data, else return 'false'
      */
     public static boolean fieldIsPresent(String... names) {
       /* false means at least one field is null or empty
        * true  means all fields have real data 
        */
         if (names == null) {
             return false;
         }
 
         boolean isPresent = true;
 
         // All fields must be present
         for (String name : names) {
             if (name == null || name.trim().length() == 0) {
                 isPresent = false;
             }
         }
 
         return isPresent;
     }
 
     /**
      * Check multiple fields to see if they are empty or null
      *
      * @param names variable parameter list of names
      * @return true if all any field is null or empty, else false
      */
     public static boolean fieldIsNotPresent(String... names) {
         boolean fieldIsNotPresent = true;
         if (fieldIsPresent(names)) {
             fieldIsNotPresent = false;
         }
 
         return fieldIsNotPresent;
     }
 
     /**
      * Check significant characters for 'yes' and return if yes is indicated or not
      *
      * @param field
      * @return true if option is 'yes'
      */
     public static boolean isOptionYes(final String field) {
         if (field == null) {
             return false;
         }
         final int length = Math.min("yes".length(), field.length());
         return "yes".substring(0, length).equalsIgnoreCase(field);
     }
 
     /**
      * Check significant characters for 'no' and return if no is indicated or not
      *
      * @param field
      * @return true if option is 'no'
      */
     public static boolean isOptionNo(final String field) {
         if (field == null) {
             return false;
         }
         final int length = Math.min("no".length(), field.length());
         return "no".substring(0, length).equalsIgnoreCase(field);
     }
 
     /**
      * Return a List of name tokens
      *
      * @param names is a comma separated String
      * @return will a List of Strings
      */
     public static List<String> getNameTokens(final String names) {
         List<String> listNames = new ArrayList<String>();
         if (fieldIsPresent(names)) {
            String[] tokens = names.split(",");

            for (String token : tokens) {
                if (token.trim().length() > 0) {
                    listNames.add(token);
                 }
             }
         }
 
         return listNames;
     }
 
     /**
      * Convert a String to a primitive 'int'
      *
      * @param strNumber - the String to convert
      * @return return an 'int' number representing the string, or 0 if the number is not valid
      */
     public static int convertStringToInt(final String strNumber) {
         int numericNumber = 0;
         try {
             numericNumber = Integer.parseInt(strNumber);
         }
         // Null, and Empty Strings yield an 'int' of 0
         catch (NumberFormatException nfe) {
         }
 
         return numericNumber;
 
     }
 
     /**
      * Format a 'int' number with default pattern
      *
      * @param number
      * @return contains the string representation.
      */
     public static String formatInt(final int number) {
         String result = null;
         DecimalFormat df = new DecimalFormat(INTEGER_PATTERN);
 
         try {
             result = df.format(number);
         } catch (ArithmeticException ae) {
         }
 
         return result;
     }
 
     /**
      * Format a 'long' number with default pattern
      *
      * @param number
      * @return contains the string representation.
      */
     public static String formatLong(final long number) {
         String result = null;
         DecimalFormat df = new DecimalFormat(INTEGER_PATTERN);
 
         try {
             result = df.format(number);
         } catch (ArithmeticException ae) {
         }
 
         return result;
     }
 
     /**
      * This routines returns either the parsed numeric of a string, or the default value
      *
      * @param property     is a String object with a numeric value
      * @param defaultValue is the default return value if the property is missing, or null, or non-numeric
      * @return contains the result.
      */
     public static int integerOrDefault(final String property, final int defaultValue) {
         int integerOrDefault = defaultValue;
 
         try {
             integerOrDefault = Integer.parseInt(property);
         } catch (NumberFormatException nfe) {
         }
 
         return integerOrDefault;
     }
 
     /**
      * Convert a List to a Map for Easier Handline
      *
      * @param consumerList is a list of MessageConsumers returned from a query
      * @return a Map of the List
      */
     @SuppressWarnings("unchecked")
     public static Map<Long, MessageConsumer> convertConsumerListToMap(final List<?> consumerList) {
         HashMap<Long, MessageConsumer> resultMap = new HashMap<Long, MessageConsumer>();
 
         // Convert consumer list to Map
         for (MessageConsumer messageConsumer : (List<MessageConsumer>) consumerList) {
             resultMap.put(messageConsumer.getMessageConsumerKey(), messageConsumer);
         }
 
         // Return a list of all Message Consumers
         return resultMap;
     }
 
     /**
      * This method is use to create the start date time for the current date.
      *
      * @param d contains the date to have its time zero'd out.
      * @return will return the starting time for a date.
      */
     public static Date makeStartDate(final Date d) {
 
         final Calendar cal = Calendar.getInstance();
         cal.setTime(d);
         cal.set(Calendar.HOUR_OF_DAY, ZERO);
         cal.set(Calendar.MINUTE, ZERO);
         cal.set(Calendar.SECOND, ZERO);
         cal.set(Calendar.MILLISECOND, ZERO);
 
         return cal.getTime();
     }
 
     /**
      * This method is use to create the end date time for the current date.
      *
      * @param d contains the date to have its time set to 11:59 PM
      * @return will return the end time for a date.
      */
     public static Date makeEndDate(final Date d) {
 
         final Calendar cal = Calendar.getInstance();
         cal.setTime(d);
         cal.set(Calendar.HOUR_OF_DAY, TWENTY_THREE);
         cal.set(Calendar.MINUTE, FIFTY_NINE);
         cal.set(Calendar.SECOND, FIFTY_NINE);
         cal.set(Calendar.MILLISECOND, ZERO);
 
         return cal.getTime();
     }
 
     /**
      * This routine is used to determine if a database field already equals another field.
      *
      * @param s1 contains the first field to test.
      * @param s2 contains the second field to test.
      * @return will return true if the field are equal, otherwise it will return false.
      */
     public static boolean areStringFieldsEqual(final String s1, final String s2) {
 
         try {
             final String tempString1 = s1 == null || s1.trim().length() == 0 ? "NULL" : s1.trim();
             final String tempString2 = s2 == null || s2.trim().length() == 0 ? "NULL" : s2.trim();
 
             return tempString1.equals(tempString2);
         } catch (final Exception e) { // $codepro.audit.disable logExceptions
             return false;
         }
     }
 
     /**
      * This routine is used to determine if a database field already equals another field - ignore case.
      *
      * @param s1 contains the first field to test.
      * @param s2 contains the second field to test.
      * @return will return true if the field are equal, otherwise it will return false.
      */
     public static boolean areStringFieldsEqualIgnoreCase(final String s1, final String s2) {
 
         try {
             final String tempString1 = s1 == null || s1.trim().length() == 0 ? "NULL" : s1.trim();
             final String tempString2 = s2 == null || s2.trim().length() == 0 ? "NULL" : s2.trim();
 
             return tempString1.equalsIgnoreCase(tempString2);
         } catch (final Exception e) { // $codepro.audit.disable logExceptions
             return false;
         }
     }
 
     /**
      * This routine is used to determine if a database field already equals another field.
      *
      * @param parm1 contains the first field to test.
      * @param parm2 contains the second field to test.
      * @return will return true if the field are equal, otherwise it will return false.
      */
     public static boolean areLongFieldsEqual(final Long parm1, final Long parm2) {
 
         try {
             if (parm1 == null && parm2 == null) {
                 return true;
             } else if (parm1 == null && parm2 != null ||
                     parm1 != null && parm2 == null) {
                 return false;
             } else if (parm1 != null && parm2 == null) {
                 return false;
             } else {
                 return parm1.equals(parm2);
             }
         } catch (final Exception e) { // $codepro.audit.disable logExceptions
             return false;
         }
     }
 
     /**
      * This method is used to compare dates for equality.
      *
      * @param parm1 contains the first date to be compared.
      * @param parm2 contains the second date to be compared.
      * @return will return true if the dates are equal, otherwise it will return false.
      */
     public static boolean areDateFieldsEqual(final Date parm1, final Date parm2) {
 
         try {
             if (parm1 == null && parm2 == null) {
                 return true;
             } else if (parm1 == null && parm2 != null ||
                     parm1 != null && parm2 == null) {
                 return false;
             } else if (parm1 != null && parm2 == null) {
                 return false;
             } else {
                 return parm1.equals(parm2);
             }
         } catch (final Exception e) { // $codepro.audit.disable logExceptions
             return false;
         }
     }
 
     /**
      * <p>Convert IPv6 address into RFC 5952 form.
      * E.g. 2001:db8:0:1:0:0:0:1 -> 2001:db8:0:1::1</p>
      * <p/>
      * <p>Method is null safe, and if IPv4 address or host name is passed to the
      * method it is returned wihout any processing.</p>
      * <p/>
      * <p>Method also supports IPv4 in IPv6 (e.g. 0:0:0:0:0:ffff:192.0.2.1 ->
      * ::ffff:192.0.2.1), and zone ID (e.g. fe80:0:0:0:f0f0:c0c0:1919:1234%4
      * -> fe80::f0f0:c0c0:1919:1234%4).</p>
      *
      * @param ipv6Address String representing valid IPv6 address.
      * @return String representing IPv6 in canonical form.
      * @throws IllegalArgumentException if IPv6 format is unacceptable.
      */
     public static String canonicalizeAddress(final String ipv6Address)
             throws IllegalArgumentException {
 
         if (ipv6Address == null) {
             return null;
         }
 
         // Definitely not an IPv6, return untouched input.
         if (!mayBeIPv6Address(ipv6Address)) {
             return ipv6Address;
         }
 
         // Length without zone ID (%zone) or IPv4 address
         int ipv6AddressLength = ipv6Address.length();
         if (ipv6Address.contains(":") && ipv6Address.contains(".")) {
             // IPv4 in IPv6
             // e.g. 0:0:0:0:0:FFFF:127.0.0.1
             final int lastColonPos = ipv6Address.lastIndexOf(':');
             final int lastColonsPos = ipv6Address.lastIndexOf("::");
             if (lastColonsPos >= 0 && lastColonPos == lastColonsPos + 1) {
                 /*
                  *  IPv6 part ends with two consecutive colons,
                  *  last colon is part of IPv6 format.
                  *  e.g. ::127.0.0.1
                  */
                 ipv6AddressLength = lastColonPos + 1;
             } else {
                 /*
                  *  IPv6 part ends with only one colon,
                  *  last colon is not part of IPv6 format.
                  *  e.g. ::FFFF:127.0.0.1
                  */
                 ipv6AddressLength = lastColonPos;
             }
         } else if (ipv6Address.contains(":") && ipv6Address.contains("%")) {
             // Zone ID
             // e.g. fe80:0:0:0:f0f0:c0c0:1919:1234%4
             ipv6AddressLength = ipv6Address.lastIndexOf('%');
         }
 
         final StringBuilder result = new StringBuilder();
         final char[][] groups = new char[MAX_NUMBER_OF_GROUPS][MAX_GROUP_LENGTH];
         int groupCounter = 0;
         int charInGroupCounter = 0;
 
         // Index of the current zeroGroup, -1 means not found.
         int zeroGroupIndex = -1;
         int zeroGroupLength = 0;
 
         // maximum length zero group, if there is more then one, then first one
         int maxZeroGroupIndex = -1;
         int maxZeroGroupLength = 0;
 
         boolean isZero = true;
         boolean groupStart = true;
 
         /*
          *  Two consecutive colons, initial expansion.
          *  e.g. 2001:db8:0:0:1::1 -> 2001:db8:0:0:1:0:0:1
          */
 
         final StringBuilder expanded = new StringBuilder(ipv6Address);
         final int colonsPos = ipv6Address.indexOf("::");
         int length = ipv6AddressLength;
         int change = 0;
 
         if (colonsPos >= 0 && colonsPos < ipv6AddressLength - 2) {
             int colonCounter = 0;
             for (int i = 0; i < ipv6AddressLength; i++) {
                 if (ipv6Address.charAt(i) == ':') {
                     colonCounter++;
                 }
             }
 
             if (colonsPos == 0) {
                 expanded.insert(0, "0");
                 change = change + 1;
             }
 
             for (int i = 0; i < MAX_NUMBER_OF_GROUPS - colonCounter; i++) {
                 expanded.insert(colonsPos + 1, "0:");
                 change = change + 2;
             }
 
 
             if (colonsPos == ipv6AddressLength - 2) {
                 expanded.setCharAt(colonsPos + change + 1, '0');
             } else {
                 expanded.deleteCharAt(colonsPos + change + 1);
                 change = change - 1;
             }
             length = length + change;
         }
 
 
         // Processing one char at the time
         for (int charCounter = 0; charCounter < length; charCounter++) {
             char c = expanded.charAt(charCounter);
             if (c >= 'A' && c <= 'F') {
                 c = (char) (c + 32);
             }
             if (c != ':') {
                 groups[groupCounter][charInGroupCounter] = c;
                 if (!(groupStart && c == '0')) {
                     ++charInGroupCounter;
                     groupStart = false;
                 }
                 if (c != '0') {
                     isZero = false;
                 }
             }
             if (c == ':' || charCounter == length - 1) {
                 // We reached end of current group
                 if (isZero) {
                     ++zeroGroupLength;
                     if (zeroGroupIndex == -1) {
                         zeroGroupIndex = groupCounter;
                     }
                 }
 
                 if (!isZero || charCounter == length - 1) {
                     // We reached end of zero group
                     if (zeroGroupLength > maxZeroGroupLength) {
                         maxZeroGroupLength = zeroGroupLength;
                         maxZeroGroupIndex = zeroGroupIndex;
                     }
                     zeroGroupLength = 0;
                     zeroGroupIndex = -1;
                 }
                 ++groupCounter;
                 charInGroupCounter = 0;
                 isZero = true;
                 groupStart = true;
             }
         }
 
         final int numberOfGroups = groupCounter;
 
         // Output results
         for (groupCounter = 0; groupCounter < numberOfGroups; groupCounter++) {
             if (maxZeroGroupLength <= 1 || groupCounter < maxZeroGroupIndex
                     || groupCounter >= maxZeroGroupIndex + maxZeroGroupLength) {
                 for (int j = 0; j < MAX_GROUP_LENGTH; j++) {
                     if (groups[groupCounter][j] != 0) {
                         result.append(groups[groupCounter][j]);
                     }
                 }
                 if (groupCounter < numberOfGroups - 1
                         && (groupCounter != maxZeroGroupIndex - 1
                         || maxZeroGroupLength <= 1)) {
                     result.append(':');
                 }
             } else if (groupCounter == maxZeroGroupIndex) {
                 result.append("::");
             }
         }
 
         // Solve problem with three colons in IPv4 in IPv6 format
         // e.g. 0:0:0:0:0:0:127.0.0.1 -> :::127.0.0.1 -> ::127.0.0.1
         final int resultLength = result.length();
         if (result.charAt(resultLength - 1) == ':' && ipv6AddressLength < ipv6Address.length()
                 && ipv6Address.charAt(ipv6AddressLength) == ':') {
             result.delete(resultLength - 1, resultLength);
         }
 
         /*
          * Append IPv4 from IPv4-in-IPv6 format or Zone ID
          */
         for (int i = ipv6AddressLength; i < ipv6Address.length(); i++) {
             result.append(ipv6Address.charAt(i));
         }
 
         return result.toString();
     }
 
     /**
      * Heuristic check if string might be an IPv6 address.
      *
      * @param address Any string or null
      * @return true, if input string contains only hex digits and at least two colons, before '.' or '%' charachter
      */
     public static boolean mayBeIPv6Address(final String address) {
         if (address == null) {
             return false;
         }
 
         boolean result = false;
         int colonsCounter = 0;
         final int length = address.length();
         for (int i = 0; i < length; i++) {
             final char c = address.charAt(i);
             if (c == '.' || c == '%') {
                 // IPv4 in IPv6 or Zone ID detected, end of checking.
                 break;
             }
             if (!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f'
                     || c >= 'A' && c <= 'F' || c == ':')) {
                 return false;
             } else if (c == ':') {
                 colonsCounter++;
             }
         }
         if (colonsCounter >= 2) {
             result = true;
         }
         return result;
     }
 
     /**
      * This method is used to update gender record with a new gender if one differs from what's stored in the database.
      *
      * @param gender contains the incoming gender ('M', 'F', or other).
      */
     public static GenderType genderStringToType(final String gender) {
         final GenderType genderType;
 
         if (gender == null) {
             return null;
         }
 
         final String genderTrim = gender.trim().toUpperCase();
 
         if (genderTrim.equals("M")) {
             genderType = GenderType.GENDER_MALE;
         } else if (genderTrim.equals("F")) {
             genderType = GenderType.GENDER_FEMALE;
         } else {
             genderType = GenderType.GENDER_OTHER;
         }
 
         return genderType;
     }
 
     /**
      * This method is used to convert an CPR status code to an analogous HTTP status code.
      *
      * @param statusCode contains the CPR status code.
      * @return will return the HTTP status code.
      */
     public static int convertCprReturnToHttpStatus(final int statusCode) {
 
         final ReturnType returnType = ReturnType.get(statusCode);
         int httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
 
         switch (returnType) {
             case SUCCESS:
                 httpStatus = HttpStatus.SC_OK;
                 break;
             case ADD_FAILED_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case ALREADY_DELETED_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case RECORD_NOT_FOUND_EXCEPTION:
                 httpStatus = HttpStatus.SC_NOT_FOUND;
                 break;
             case ARCHIVE_FAILED_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case NOT_SPECIFIED_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case TYPE_NOT_FOUND_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case INVALID_PARAMETERS_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case YN_PARAMETERS_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case GENERAL_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case PARAMETER_LENGTH_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case MESSAGE_CREATION_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case MESSAGE_INITIALIZATION_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case MESSAGE_SEND_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case NOT_AUTHORIZED_EXCEPTION:
                 httpStatus = HttpStatus.SC_UNAUTHORIZED;
                 break;
             case PERSON_NOT_ACTIVE_EXCEPTION:
                 httpStatus = HttpStatus.SC_BAD_REQUEST;
                 break;
             case PERSON_NOT_FOUND_EXCEPTION:
                 httpStatus = HttpStatus.SC_NOT_FOUND;
                 break;
             case PSUID_NOT_FOUND_EXCEPTION:
                 httpStatus = HttpStatus.SC_NOT_FOUND;
                 break;
             case SERVICE_AUTHENTICATION_EXCEPTION:
                 httpStatus = HttpStatus.SC_UNAUTHORIZED;
                 break;
             case SET_PRIMARY_FAILED_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case UPDATE_FAILED_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case WEB_SERVICE_NOT_FOUND_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case GI_FAILURE:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case DB_CONNECTION_FAILURE:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case GENERAL_DATABASE_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case UNARCHIVE_FAILED_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case DATA_CHANGE_EXCEPTION:
                 httpStatus = HttpStatus.SC_UNAUTHORIZED;
                 break;
             case SECURITY_OPERATION_EXCEPTION:
                 httpStatus = HttpStatus.SC_UNAUTHORIZED;
                 break;
             case AFFILIATION_USE_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case IAP_USE_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case RECORD_ALREADY_EXISTS:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case EXACT_MATCH_EXCEPTION:
                 httpStatus = HttpStatus.SC_MULTIPLE_CHOICES;
                 break;
             case NEAR_MATCH_EXCEPTION:
                 httpStatus = HttpStatus.SC_MULTIPLE_CHOICES;
                 break;
             case MESSAGE_RECEIVE_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case DIRECTORY_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case JSON_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case JMS_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             case NOT_IMPLEMENTED_EXCEPTION:
                 httpStatus = HttpStatus.SC_INTERNAL_SERVER_ERROR;
                 break;
             default:
                 break;
         }
         return httpStatus;
     }
 
     /**
      * This method is used to convert a date/time to an ISO 8601 compliant format.
      *
      * @param d contains the date value.
      * @return will a string representation of the date in ISO 8601 format.
      */
     public static String formatDateToISO8601(final Date d) {
         if (d == null) {
             return null;
         }
         final TimeZone tz = TimeZone.getTimeZone(UTC_TIMEZONE);
         ISO8601_FORMATTER.setTimeZone(tz);
         return ISO8601_FORMATTER.format(d);
     }
 
     /**
      * This method is used to convert a time_t to an ISO 8601 compliant format.
      *
      * @param t contains the time_t value (in milliseconds).
      * @return will return a string representation of the date in ISO 8601 format.
      */
     public static String formatTimeMsToISO8601(final long t) {
         final TimeZone tz = TimeZone.getTimeZone(UTC_TIMEZONE);
         ISO8601_FORMATTER.setTimeZone(tz);
         return ISO8601_FORMATTER.format(t);
     }
 
     /**
      * This method is used by the RESTful services to build the URIs for a return.
      *
      * @param path contains the path for the service.
      * @param key  contains the key.
      * @return will return the formatted URI.
      */
     public static String constructUri(final String path, final String key) {
         final StringBuilder sb = new StringBuilder(128);
         sb.append(FORWARD_SLASH);
         sb.append(path);
         sb.append(FORWARD_SLASH);
         sb.append(key);
         return sb.toString();
     }
 }
