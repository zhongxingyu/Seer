 package ro.finsiel.eunis.search;
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Random;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import ro.finsiel.eunis.WebContentManagement;
 import ro.finsiel.eunis.jrfTables.Chm62edtBiogeoregionPersist;
 import ro.finsiel.eunis.jrfTables.Chm62edtConservationStatusDomain;
 import ro.finsiel.eunis.jrfTables.Chm62edtConservationStatusPersist;
 import ro.finsiel.eunis.jrfTables.Chm62edtCountryDomain;
 import ro.finsiel.eunis.jrfTables.Chm62edtCountryPersist;
 import ro.finsiel.eunis.jrfTables.Chm62edtGroupspeciesDomain;
 import ro.finsiel.eunis.jrfTables.Chm62edtGroupspeciesPersist;
 import ro.finsiel.eunis.jrfTables.Chm62edtSpeciesDomain;
 import ro.finsiel.eunis.jrfTables.Chm62edtSpeciesPersist;
 import ro.finsiel.eunis.jrfTables.habitats.habitatsByReferences.RefDomain;
 import ro.finsiel.eunis.jrfTables.sites.statistics.CountrySitesFactsheetPersist;
 import ro.finsiel.eunis.jrfTables.species.glossary.Chm62edtGlossaryDomain;
 import ro.finsiel.eunis.jrfTables.species.habitats.CommonRecordsDomain;
 import ro.finsiel.eunis.jrfTables.species.habitats.CommonRecordsPersist;
 import ro.finsiel.eunis.jrfTables.species.habitats.HabitatNatureObjectGeoscopeDomain;
 import ro.finsiel.eunis.jrfTables.species.habitats.HabitatNatureObjectGeoscopePersist;
 import ro.finsiel.eunis.jrfTables.species.habitats.HabitatNatureObjectReportTypeDomain;
 import ro.finsiel.eunis.jrfTables.species.habitats.HabitatNatureObjectReportTypePersist;
 import ro.finsiel.eunis.jrfTables.species.references.ReferencesJoinDomain;
 import ro.finsiel.eunis.jrfTables.species.references.ReferencesJoinPersist;
 import ro.finsiel.eunis.search.species.taxcode.TaxonomyDTO;
 import ro.finsiel.eunis.utilities.SQLUtilities;
 import ro.finsiel.eunis.utilities.TableColumns;
 import eionet.eunis.util.Constants;
 
 
 /**
  * This class contains global utilities used throughout all the application.
  * Put here your new utility methods and explain what they do.
  *
  * @author finsiel
  */
 public final class Utilities {
 
     /**
      * Defines an EUNIS habitat.
      */
     public static final Integer EUNIS_HABITAT = new Integer(0);
 
     /**
      * Defines an ANNEX I habitat.
      */
     public static final Integer ANNEX_I_HABITAT = new Integer(1);
 
     /**
      * Operator IS (meaning something *IS* ...).
      */
     public static final Integer OPERATOR_IS = new Integer(2);
 
     /**
      * Operator STARTS (meaning something *STARTS WITH* a given string ...).
      */
     public static final Integer OPERATOR_STARTS = new Integer(3);
 
     /**
      * Operator CONTAINS (meaning something *CONTAINS* a given string ...).
      */
     public static final Integer OPERATOR_CONTAINS = new Integer(4);
 
     /**
      * Operator IS (meaning something *IS NOT* ...).
      */
     public static final Integer OPERATOR_IS_NOT = new Integer(5);
 
     /**
      * Operator BETWEEN (meaning some value is between some limits).
      */
     public static final Integer OPERATOR_BETWEEN = new Integer(6);
 
     /**
      * Operator GREATER (meaning some value is greater than..).
      */
     public static final Integer OPERATOR_GREATER_OR_EQUAL = new Integer(7);
 
     /**
      * Operator SMALLER (meaning some value is smaller than..).
      */
     public static final Integer OPERATOR_SMALLER_OR_EQUAL = new Integer(8);
 
     /**
      * The maximum number of results which are displayed within a popup.
      */
     public static final int MAX_POPUP_RESULTS = 100;
 
     /**
      * Define relation as LOGIC AND.
      */
     public static final Integer LOGICAL_AND = new Integer(9);
 
     /**
      * Define relation as LOGIC oR.
      */
     public static final Integer LOGICAL_OR = new Integer(10);
 
     /**
      * This is the Log4J logger used for message logging.
      */
     // private static Logger logger = null;
     private static Logger logger = Logger.getLogger(Utilities.class);
 
     /**
      * The root folder where images are located.
      */
     private static final String IMG_SORT_BASE = "images/";
 
     /**
      * Image used to resemble ascending sorting.
      */
     private static final String IMG_SORT_ASCENDING = "sort_ascending.gif";
 
     /**
      * Image used to resemble descending sorting.
      */
     private static final String IMG_SORT_DESCENDING = "sort_descending.gif";
 
     /**
      * Image used to resemble unsorted results column.
      */
     private static final String IMG_SORT_UNSORTED = "sort_unsorted.gif";
 
     /**
      * A timer used to do timing performance tests for an object calling method.
      */
     private static Date timer = null;
 
     /**
      * This method is used to display value taken from database, which may be null so displaying indirectly through
      * this method, assures that never will appear in web page, the infamous 'null' string.
      *
      * @param object Object to be interpreted
      * @return String representation of that object (.toString()) if non-null.
      */
     public static String formatString(Object object) {
         return formatString(object, "");
     }
 
     /**
      * This method is used to display value taken from database, which may be null so displaying indirectly through
      * this method, assures that never will appear in web page, the infamous 'null' string.
      *
      * @param object       Object to be interpreted
      * @param defaultValue Default value.
      * @return String representation of that object (.toString()) if non-null.
      */
     public static String formatString(Object object, String defaultValue) {
         if (null == object) {
             return defaultValue;
         }
         if (object.toString().equalsIgnoreCase("null")) {
             return defaultValue;
         }
         if (object.toString().length() == 0) {
             return defaultValue;
         }
         return object.toString();
     }
 
     /**
      * Transforms an String object into an int, safely (without throwing exceptions).
      *
      * @param s            String to be transformed
      * @param defaultValue Default value returned if s cannot be parsed
      * @return The integer representation of s or defaultValue if error occurred.
      */
     public static int checkedStringToInt(String s, int defaultValue) {
         if (null == s) {
             return defaultValue;
         }
         try {
             return Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Transforms an String object into an long, safely (without throwing exceptions).
      *
      * @param s            String to be transformed
      * @param defaultValue Default value returned if s cannot be parsed
      * @return The long representation of s or defaultValue if error occurred.
      */
     public static long checkedStringToLong(String s, int defaultValue) {
         if (null == s) {
             return defaultValue;
         }
         try {
             return Long.parseLong(s);
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Transforms an String object into an Integer, safely (without throwing exceptions).
      *
      * @param s            String to be transformed
      * @param defaultValue Default value returned if s cannot be parsed
      * @return The Integer representation of s
      */
     public static Integer checkedStringToInt(String s, Integer defaultValue) {
         if (null == s) {
             return defaultValue;
         }
         try {
             return new Integer(s);
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Transforms an String object into an float, safely (without throwing exceptions).
      *
      * @param s            String to be transformed
      * @param defaultValue Default value returned if s cannot be parsed
      * @return The float representation of s or defaultValue if error occurred.
      */
     public static float checkedStringToFloat(String s, float defaultValue) {
         if (null == s) {
             return defaultValue;
         }
         try {
             return Float.parseFloat(s);
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Transforms an String object into an Integer, safely (without throwing exceptions).
      *
      * @param s            String to be transformed
      * @param defaultValue Default value returned if s cannot be parsed
      * @return The Integer representation of s
      */
     public static Float checkedStringToFloat(String s, Float defaultValue) {
         if (null == s) {
             return defaultValue;
         }
         try {
             return new Float(s);
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Transforms an String object into an boolean, safely (without throwing exceptions).
      *
      * @param s            String to be transformed.<br />
      *                     Possible values are:
      *                     <UL>
      *                     <LI> "true"  - boolean true
      *                     <LI> "false" - boolean false
      *                     <LI> "1" - boolean true
      *                     <LI> "0" - boolean false
      *                     </UL>
      * @param defaultValue Default value if s cannot be parsed
      * @return The boolean representation of s
      */
     public static boolean checkedStringToBoolean(String s, boolean defaultValue) {
         boolean ret = defaultValue;
 
         if (null != s) {
             ret = Boolean.valueOf(s).booleanValue();
             if (s.equalsIgnoreCase("1")) {
                 ret = true;
             }
             if (s.equalsIgnoreCase("0")) {
                 ret = false;
             }
         }
         return ret;
     }
 
     /**
      * Transforms an String object into an boolean, safely (without throwing exceptions).
      *
      * @param s            String to be transformed.<br />
      *                     Possible values are:
      *                     <UL>
      *                     <LI> "true"  - boolean true
      *                     <LI> "false" - boolean false
      *                     <LI> "1" - boolean true
      *                     <LI> "0" - boolean false
      *                     </UL>
      * @param defaultValue Default value if s cannot be parsed
      * @return The boolean representation of s
      */
     public static boolean checkedStringToBoolean(String s, Boolean defaultValue) {
         boolean ret = false;
 
         if (null != defaultValue) {
             ret = defaultValue.booleanValue();
         }
         return checkedStringToBoolean(s, ret);
     }
 
     /**
      * This method is used along with stopTimer to make performance tests. One should call
      * the startTimer at the beginning of code block and stopTimer at the end.
      */
     public static synchronized void startTimer() {
         timer = new Date();
     }
 
     /**
      * This is other behalf method used for performance counting. This should be called
      * at the end of the block, after
      *
      * @return The number of milliseconds since timer was started. Of course you should substract 1 millisecond  (maybe
      *         even less than 1ms. ) for the overhead of calling startTimer() / stopTimer() pair of methods.
      */
     public static synchronized long stopTimer() {
         long ret;
 
         if (null == timer) {
             return -1;
         }
         ret = new Date().getTime() - timer.getTime();
         timer = null;
         return ret;
     }
 
     /**
      * This method computes the HTML tag IMG appended in each column sorted. for example:
      * &ltIMG border='0' align='absmiddle' src='images/sort_unsorted.gif'&gt
      *
      * @param sortCriteria The criteria upon which imgage src attribute is computed.
      * @return The HTML IMG tag.
      */
     public static StringBuffer getSortImageTag(AbstractSortCriteria sortCriteria) {
         StringBuffer imgTag = new StringBuffer(30);
 
         imgTag.append("<img border=\"0\" align=\"middle\" src=\"");
         imgTag.append(IMG_SORT_BASE);
         if (null != sortCriteria) {
             if (0
                     == sortCriteria.getAscendency().compareTo(
                             AbstractSortCriteria.ASCENDENCY_ASC)) {
                 imgTag.append(IMG_SORT_ASCENDING);
                 imgTag.append(
                         "\" title=\"Results are sorted ascending by this column\" "
                         + "alt=\"Results are sorted ascending by this column\" ");
             } else if (0
                     == sortCriteria.getAscendency().compareTo(
                             AbstractSortCriteria.ASCENDENCY_DESC)) {
                 imgTag.append(IMG_SORT_DESCENDING);
                 imgTag.append(
                         "\" alt=\"Results are sorted descending by this column\" "
                         + "title=\"Results are sorted descending by this column\" ");
             } else {
                 imgTag.append(IMG_SORT_UNSORTED);
                 imgTag.append(
                         "\" alt=\"Results are not sorted by this column\" "
                         + "title=\"Results are not sorted by this column\" ");
             }
         } else {
             imgTag.append(IMG_SORT_UNSORTED);
             imgTag.append(
                     "\" alt=\"Results are not sorted by this column\" "
                         + "title=\"Results are not sorted by this column\" ");
         }
         imgTag.append(" />");
         return imgTag;
     }
 
     /**
      * This method computes the HTML tag IMG appended in each column sorted. for example:
      * &lt;IMG border='0' align='absmiddle' src='images/sort_unsorted.gif'&gt;
      *
      * @param ascendency What type of image to return
      * @return The HTML IMG tag.
      */
     public static StringBuffer getSortImageTag(Integer ascendency) {
         if (null == ascendency) {
             return new StringBuffer();
         }
         StringBuffer imgTag = new StringBuffer(30);
 
         imgTag.append("<img title=\"Sort\" border=\"0\" align=\"middle\" src=\"");
         imgTag.append(IMG_SORT_BASE);
         if (0 == ascendency.compareTo(AbstractSortCriteria.ASCENDENCY_ASC)) {
             imgTag.append(IMG_SORT_ASCENDING);
             imgTag.append(
                     "\" title=\"Results are sorted ascending by this column\" alt=\"Results are sorted ascending by this column\" ");
         } else if (0
                 == ascendency.compareTo(AbstractSortCriteria.ASCENDENCY_DESC)) {
             imgTag.append(IMG_SORT_DESCENDING);
             imgTag.append(
                     "\" alt=\"Results are sorted descending by this column\" title=\"Results are sorted descending by this column\" ");
         } else {
             imgTag.append(IMG_SORT_UNSORTED);
             imgTag.append(
                     "\" alt=\"Results are not sorted by this column\" title=\"Results are not sorted by this column\" ");
         }
         imgTag.append(" />");
         return imgTag;
     }
 
     /**
      * Print to standard output the request parameters.
      *
      * @param request Request object
      */
     public static void dumpRequestParams(HttpServletRequest request) {
         Enumeration en = request.getParameterNames();
 
         System.out.println("======= Dumping request parameters =============");
         while (en.hasMoreElements()) {
             String param = (String) en.nextElement();
 
             System.out.println(param + " == " + request.getParameter(param));
         }
         System.out.println("=================== Done =======================");
     }
 
     /**
      * This method translates the REQUEST parameters in FORM's hidden parameters.
      *
      * @param request the request
      * @return The string containing the request parameters as hidden form fields
      */
     public static String requestToFormParam(HttpServletRequest request) {
         if (null == request) {
             return "";
         }
         StringBuffer ret = new StringBuffer();
         Enumeration en = request.getParameterNames();
 
         while (en.hasMoreElements()) {
             String param = (String) en.nextElement();
 
             ret.append(writeFormParameter(param, request.getParameter(param)));
         }
         return ret.toString();
     }
 
     /**
      * Splits an string which contains delimiters.
      *
      * @param string    String to be parsed
      * @param delimiter Element used to separate strings
      * @return Vector containing the seperated strings (order is preserved).
      */
     public static Vector tokenizeString(String string, String delimiter) {
         StringTokenizer st = new StringTokenizer(string, delimiter);
         Vector v = new Vector();
 
         while (st.hasMoreElements()) {
             v.addElement(st.nextElement());
         }
         return v;
     }
 
     /**
      * This method will write the value of a parameters, as an URL representable string, in another words:
      * "&paramName=paramValue".
      *
      * @param paramName  Parameters name
      * @param paramValue Value of the parameter
      * @return The representation of the paramName/Value pair
      */
     public static StringBuffer writeURLParameter(String paramName, String paramValue) {
         StringBuffer urlString = new StringBuffer();
 
         if (null == paramName || null == paramValue) {
             return urlString;
         }
         urlString.append("&amp;");
         urlString.append(paramName);
         urlString.append("=");
         urlString.append(paramValue);
         return urlString;
     }
 
     /**
      * This method return a string with checked fields list like : "&showName=true&showDesignationYear=true...".
      *
      * @param v vector with checked fields
      * @return string with checked fields
      */
     public static StringBuffer writeURLCriteriaSave(Vector v) {
         if (v != null) {
             StringBuffer write = new StringBuffer();
 
             for (int i = 0; i < v.size(); i++) {
                 write.append(writeURLParameter((String) v.get(i), "true"));
             }
             return write;
         }
         return new StringBuffer("");
     }
 
     /**
      * This method will write the value of a parameters, as an URL representable string, in other words:
      * "&paramName=paramValue".
      *
      * @param paramName  Parameters name
      * @param paramValue Value of the parameter
      * @return The representation of the paramName/Value pair
      */
     public static StringBuffer writeURLParameter(String paramName, Integer paramValue) {
         StringBuffer urlString = new StringBuffer();
 
         if (null == paramName || null == paramValue) {
             return urlString;
         }
         urlString.append("&amp;");
         urlString.append(paramName);
         urlString.append("=");
         urlString.append(paramValue);
         return urlString;
     }
 
     /**
      * This method will write the value of a parameters, as an URL representable string, in other words:
      * "&paramName=paramValue".
      *
      * @param paramName  Parameters name
      * @param paramValue Value of the parameter
      * @return The representation of the paramName/Value pair
      */
     public static StringBuffer writeURLParameter(String paramName, int paramValue) {
         StringBuffer urlString = new StringBuffer();
 
         if (null == paramName) {
             return urlString;
         }
         urlString.append("&amp;");
         urlString.append(paramName);
         urlString.append("=");
         urlString.append(paramValue);
         return urlString;
     }
 
     /**
      * This method will write the value of a parameters, as an URL representable string, in another words:
      * "&paramName=paramValue".
      *
      * @param paramName  Parameters name
      * @param paramValue Value of the parameter
      * @return The representation of the paramName/Value pair
      */
     public static StringBuffer writeURLParameter(String paramName, float paramValue) {
         StringBuffer urlString = new StringBuffer();
 
         if (null == paramName) {
             return urlString;
         }
         urlString.append("&amp;");
         urlString.append(paramName);
         urlString.append("=");
         urlString.append(paramValue);
         return urlString;
     }
 
     /**
      * This method will write the value of a parameters, as an URL representable string, in another words:
      * "&paramName=paramValue".
      *
      * @param paramName  Parameters name
      * @param paramValue Value of the parameter
      * @return The representation of the paramName/Value pair
      */
     public static StringBuffer writeURLParameter(String paramName, Boolean paramValue) {
         StringBuffer urlString = new StringBuffer();
 
         if (null == paramName || null == paramValue) {
             return urlString;
         }
         urlString.append("&amp;");
         urlString.append(paramName);
         urlString.append("=");
         urlString.append(paramValue);
         return urlString;
     }
 
     /**
      * This method return a URL compatible representation of a paramName, having multiple values. For example
      * "&paramName=val1&paramName=val2&paramName=val3 ..."
      *
      * @param paramName   Name of the parameter
      * @param paramValues Values of the parameter
      * @return An URL representation of that object
      */
     public static StringBuffer writeURLParameter(String paramName, String[] paramValues) {
         StringBuffer urlString = new StringBuffer();
 
         if (null == paramName || null == paramValues) {
             return urlString;
         }
         for (int i = 0; i < paramValues.length; i++) {
             urlString.append(writeURLParameter(paramName, paramValues[ i ]));
         }
         return urlString;
     }
 
     /**
      * This method is used to create a representation in a form of a param<->value pair, i.e., having param1,value1
      * after calling this method will return the string: "<input type="hidden" name="param1" value="value1">\n".
      *
      * @param paramName  Name of the parameter (in form terms, the name property of the field)
      * @param paramValue Value of the parameter (in form terms, the value property of the field)
      * @return A hidden param in a form type of representation for the given name=value pair.
      *         Note: Because of JSPC (Jasper compiler) which throws exception if paramValue is Object, I had to
      *         overload this method to accept String, Integers and Booleans as well
      */
     public static StringBuffer writeFormParameter(String paramName, String paramValue) {
         StringBuffer formString = new StringBuffer();
 
         if (null == paramName || null == paramValue) {
             return formString;
         }
         paramValue = treatURLSpecialCharacters(paramValue);
         formString.append("<input type=\"hidden\" name=\"");
         formString.append(paramName);
         formString.append("\"");
         formString.append(" value=\"");
         formString.append(paramValue);
         formString.append("\" />");
         return formString;
     }
 
     /**
      * This method is used to create a representation in a form of a param<->value pair, i.e., having param1,value1
      * after calling this method will return the string: "<input type="hidden" name="param1" value="value1">\n".
      *
      * @param paramName  Name of the parameter (in form terms, the name property of the field)
      * @param paramValue Value of the parameter (in form terms, the value property of the field)
      * @return A hidden param in a form type of representation for the given name=value pair.
      *         Note: Because of JSPC (Jasper compiler) which throws exception if paramValue is Object, I had to
      *         overload this method to accept String, Integers and Booleans as well
      */
     public static StringBuffer writeFormParameter(String paramName, Integer paramValue) {
         StringBuffer formString = new StringBuffer();
 
         if (null == paramName || null == paramValue) {
             return formString;
         }
         formString.append("<input type=\"hidden\" name=\"");
         formString.append(paramName);
         formString.append("\"");
         formString.append(" value=\"");
         formString.append(paramValue);
         formString.append("\" />");
         return formString;
     }
 
     /**
      * This method is used to create a representation in a form of a param<->value pair, i.e., having param1,value1
      * after calling this method will return the string: "<input type="hidden" name="param1" value="value1">\n".
      *
      * @param paramName  Name of the parameter (in form terms, the name property of the field)
      * @param paramValue Value of the parameter (in form terms, the value property of the field)
      * @return A hidden param in a form type of representation for the given name=value pair.
      *         Note: Because of JSPC (Jasper compiler) which throws exception if paramValue is Object, I had to
      *         overload this method to accept String, Integers and Booleans as well
      */
     public static StringBuffer writeFormParameter(String paramName, int paramValue) {
         StringBuffer formString = new StringBuffer();
 
         if (null == paramName) {
             return formString;
         }
         formString.append("<input type=\"hidden\" name=\"");
         formString.append(paramName);
         formString.append("\"");
         formString.append(" value=\"");
         formString.append(paramValue);
         formString.append("\" />");
         return formString;
     }
 
     /**
      * This method is used to create a representation in a form of a param<->value pair, i.e., having param1,value1
      * after calling this method will return the string: "<input type="hidden" name="param1" value="value1">\n".
      *
      * @param paramName  Name of the parameter (in form terms, the name property of the field)
      * @param paramValue Value of the parameter (in form terms, the value property of the field)
      * @return A hidden param in a form type of representation for the given name=value pair.
      *         Note: Because of JSPC (Jasper compiler) which throws exception if paramValue is Object, I had to
      *         overload this method to accept String, Integers and Booleans as well
      */
     public static StringBuffer writeFormParameter(String paramName, float paramValue) {
         StringBuffer formString = new StringBuffer();
 
         if (null == paramName) {
             return formString;
         }
         formString.append("<input type=\"hidden\" name=\"");
         formString.append(paramName);
         formString.append("\"");
         formString.append(" value=\"");
         formString.append(paramValue);
         formString.append("\" />");
         return formString;
     }
 
     /**
      * This method is used to create a representation in a form of a param<->value pair, i.e., having param1,value1
      * after calling this method will return the string: "<input type="hidden" name="param1" value="value1">\n".
      *
      * @param paramName  Name of the parameter (in form terms, the name property of the field)
      * @param paramValue Value of the parameter (in form terms, the value property of the field)
      * @return A hidden param in a form type of representation for the given name=value pair.
      *         Note: Because of JSPC (Jasper compiler) which throws exception if paramValue is Object, I had to
      *         overload this method to accept String, Integers and Booleans as well
      */
     public static StringBuffer writeFormParameter(String paramName, Boolean paramValue) {
         StringBuffer formString = new StringBuffer();
 
         if (null == paramName || null == paramValue) {
             return formString;
         }
         formString.append("<input type=\"hidden\" name=\"");
         formString.append(paramName);
         formString.append("\"");
         formString.append(" value=\"");
         formString.append(paramValue);
         formString.append("\" />");
         return formString;
     }
 
     /**
      * This method is used to create a representation in a form of a param<->value pair, i.e., having param1,value1
      * after calling this method will return the string: "<input type="hidden" name="param1" value="value1">\n".
      * This method is used to write a single parameter with multiple values.
      *
      * @param paramName   Name of the parameters
      * @param paramValues Values of that parameter
      * @return Form param compatible type of representation.
      */
     public static StringBuffer writeFormParameter(String paramName, String[] paramValues) {
         StringBuffer formString = new StringBuffer();
 
         if (null == paramName || null == paramValues) {
             return formString;
         }
         for (int i = 0; i < paramValues.length; i++) {
             formString.append(writeFormParameter(paramName, paramValues[ i ]));
         }
         return formString;
     }
 
     /**
      * Format a given timestamp within a displayable date, but only the year part of the whole date.
      *
      * @param date Date to be parsed
      * @return The year representation of this date
      */
     public static String formatReferencesDate(Date date) {
         String dt = "";
 
         if (null == date) {
             return "";
         }
         try {
             if (null != date) {
                 dt = new SimpleDateFormat("yyyy").format(date);
                 if (dt.equalsIgnoreCase("0001")) {
                     dt = "";
                 }
             }
         } catch (IllegalArgumentException iagex) {
             logger.warn(
                     "formatReferencesDate(): An IllegalArgumetnException occurred during the parse of the date. Returning ''");
             return "";
         }
         return dt;
     }
     /**
      * Format a given year value within a displayable date, but only the year part of the whole date.
      *
      * @param date Date to be parsed
      * @return The year representation of this date
      */
     public static String formatReferencesYear(String date) {
         String dt = "";
 
         if (null == date) {
             return "";
         }
         dt = (date.length() > 4) ? date.substring(0, 4) : date;
         if (dt.equals("0001")) {
             dt = "";
         }
         return dt;
     }
 
     /**
      * This method transform the given parameters into SQL statement as follows:
      * if oper = OPERATOR_IS: "paramName='paramValue'"
      * if oper = OPERATOR_STARTS: "paramName LIKE '%paramValue'"
      * if oper = OPERATOR_CONTAINS: "paramName LIKE '%paramValue%'"
      * if oper = OPERATOR_GREATER_OR_EQUAL: "paramName >= paramValue"
      * if oper = OPERATOR_SMALLER_OR_EQUAL: "paramName <= paramValue".
      *
      * @param paramName  Name of the SQL parameter, see above
      * @param paramValue Value of the SQL parameter, see above
      * @param oper       Type of operation, see above
      * @return An SQL representation, see above.
      */
     public static StringBuffer prepareSQLOperator(String paramName, String paramValue, Integer oper) {
         StringBuffer sql = new StringBuffer();
 
         // Check param validity and log errors
         if (null == paramName || null == paramValue || null == oper) {
             logger.warn(
                     "prepareSQLOperator('" + paramName + "', '" + paramValue
                     + "', " + oper
                     + "): null parameters. Returning EMPTY STRING!");
             logger.warn("debugOperator returned: " + debugOperator(oper));
             return sql;
         }
         paramValue = paramValue.replaceAll("'", "''");
         // ///
         sql.append(paramName);
         boolean branch = false;
 
         if (0 == oper.compareTo(OPERATOR_IS)) {
             sql.append("='" + paramValue + "' ");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_STARTS)) {
             sql.append(" LIKE '" + paramValue + "%'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_CONTAINS)) {
             sql.append(" LIKE '%" + paramValue + "%'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_GREATER_OR_EQUAL)) {
             sql.append(" >=" + paramValue);
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_SMALLER_OR_EQUAL)) {
             sql.append(" <=" + paramValue);
             branch = true;
         }
         if (!branch) {
             logger.warn(
                     "::prepareSQLOperator(" + paramName + ", " + paramValue
                     + ", " + oper + "): No branch hit!");
         }
         return sql;
     }
 
     /**
      * This method transform the given triplet parameters into SQL statement as in following example
      * if oper = OPERATOR_BETWEEN: "paramName  >=paramValueMin AND paramName <=paramValueMax".
      *
      * @param paramName     Name of the SQL parameter, see above
      * @param paramValue    Value of the SQL parameter
      * @param paramValueMin Value of the SQL parameter (min)
      * @param paramValueMax Value of the SQL parameter (max)
      * @param oper          Type of operation, see above
      * @return An SQL representation, see above.
      */
     public static StringBuffer prepareSQLOperator(String paramName, String paramValue, String paramValueMin,
                                                   String paramValueMax, Integer oper) {
         StringBuffer sql = new StringBuffer();
 
         if (null == paramName
                 || (null == paramValue
                 && (null == paramValueMin && null == paramValueMax))
                 || null == oper) {
             logger.warn(
                     "prepareSQLOperator(String, String, String, String, Integer): one of the parameters was null. Returning EMPTY STRING!");
             return sql;
         }
         if (null == paramValue) {
             paramValue = "";
         }
         if (null == paramValueMin) {
             paramValueMin = "";
         }
         if (null == paramValueMax) {
             paramValueMax = "";
         }
 
         paramValue = paramValue.replaceAll("'", "''");
         paramValueMin = paramValueMin.replaceAll("'", "''");
         paramValueMax = paramValueMax.replaceAll("'", "''");
         sql.append(paramName);
         boolean branch = false;
 
         if (0 == oper.compareTo(OPERATOR_IS)) {
             sql.append("='").append(paramValue).append("' ");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_STARTS)) {
             sql.append(" LIKE '").append(paramValue).append("%'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_CONTAINS)) {
             sql.append(" LIKE '%").append(paramValue).append("%'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_GREATER_OR_EQUAL)) {
             sql.append(" >=").append(paramValue);
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_SMALLER_OR_EQUAL)) {
             sql.append(" <=").append(paramValue);
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_BETWEEN)) {
             sql.append(" >=").append(paramValueMin);
             sql.append(" AND ");
             sql.append(paramName).append(" <=").append(paramValueMax);
             branch = true;
         }
         if (!branch) {
             logger.warn(
                     "Warning: prepareSQLOperator(" + paramName + ", "
                             + paramValue + ", " + paramValueMin + ", " + paramValueMax
                             + ", " + oper + "): No branch hit!");
         }
         return sql;
     }
 
     /**
      * This method transforms an triplet of (parameter, operator, parameter value) within a human readable language
      * for example prepareHumanString("height", OPERATOR_IS, "176") will return the string:
      * "height is 176".
      *
      * @param paramName  Parameter name
      * @param paramValue Parameter value.
      * @param oper       the operator. Possible values are: ro.finsiel.eunis.OPERATOR_XXXX / but NOT OPERATOR_BETWEEN
      * @return An human readable representation of the parameters given.
      */
     public static StringBuffer prepareHumanString(String paramName, String paramValue, Integer oper) {
         StringBuffer humanStr = new StringBuffer();
 
         humanStr.append(paramName);
         boolean branch = false;
 
         if (0 == oper.compareTo(OPERATOR_IS)) {
             humanStr.append(" is ").append(paramValue).append("");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_BETWEEN)) {
             humanStr.append(" between ").append(paramValue).append("");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_STARTS)) {
             humanStr.append(" starts with '").append(paramValue).append("'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_CONTAINS)) {
             humanStr.append(" contains '").append(paramValue).append("'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_GREATER_OR_EQUAL)) {
             humanStr.append(" is greater or equal than ").append(paramValue);
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_SMALLER_OR_EQUAL)) {
             humanStr.append(" is smaller or equal than ").append(paramValue);
             branch = true;
         }
         if (!branch) {
             logger.warn(
                     "::prepareHumanString(" + paramName + ", " + paramValue
                     + ", " + oper + "): No branch hit!");
             logger.warn("::debugOperator returned: " + debugOperator(oper));
         }
         return humanStr;
     }
 
     /**
      * This method transforms an triplet of (parameter, operator, parameter value) within a human readable language
      * for example prepareHumanString("height", null, "176", "185", OPERATOR_BETWEEN) will return the string:
      * "height between (including) 176 and (including) 185"...
      *
      * @param paramName     Parameter name
      * @param paramValue    Parameter value. If oper is OPERATOR_BETWEEN, this value can be null
      * @param paramValueMin Parameter value minimal. If operator is NOT OPERATOR_BETWEEN, this can be null
      * @param paramValueMax Parameter value maximum. If operator is NOT OPERATOR_BETWEEN, this can be null
      * @param oper          the operator. Possible values are: ro.finsiel.eunis.OPERATOR_XXXX
      * @return An human readable representation of the parameters given.
      */
     public static StringBuffer prepareHumanString(String paramName, String paramValue, String paramValueMin,
                                                   String paramValueMax, Integer oper) {
         StringBuffer humanStr = new StringBuffer();
 
         humanStr.append(paramName);
         boolean branch = false;
 
         if (0 == oper.compareTo(OPERATOR_IS)) {
             humanStr.append(" is " + paramValue + "");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_STARTS)) {
             humanStr.append(" starts with '" + paramValue + "'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_CONTAINS)) {
             humanStr.append(" contains '" + paramValue + "'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_GREATER_OR_EQUAL)) {
             humanStr.append(" greater or equal than " + paramValue);
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_SMALLER_OR_EQUAL)) {
             humanStr.append(" smaller or equal than " + paramValue);
             branch = true;
         }
 
         if (0 == oper.compareTo(OPERATOR_BETWEEN)) {
             humanStr.append(" between (including) " + paramValueMin);
             humanStr.append(" and ");
             humanStr.append(" (including) " + paramValueMax);
             branch = true;
         }
         if (!branch) {
             logger.warn(
                     "::prepareHumanString(" + paramName + ", " + paramValue
                     + ", " + paramValueMin + ", " + paramValueMax + ", " + oper
                     + "): No branch hit!");
             logger.warn("::debugOperator returned: " + debugOperator(oper));
         }
         return humanStr;
     }
 
     /**
      * This method transforms an triplet of (parameter, operator, parameter value) within a human readable language
      * for example prepareHumanString("height", null, "176", "185", OPERATOR_BETWEEN) will return the string:
      * "height between (including) 176 and (including) 185"...
      *
      * @param paramName     Parameter name
      * @param paramValue    Parameter value. If oper is OPERATOR_BETWEEN, this value can be null
      * @param paramValueMin Parameter value minimal. If operator is NOT OPERATOR_BETWEEN, this can be null
      * @param paramValueMax Parameter value maximum. If operator is NOT OPERATOR_BETWEEN, this can be null
      * @param oper          the operator. Possible values are: ro.finsiel.eunis.OPERATOR_XXXX
      * @return An human readable representation of the parameters given.
      */
     public static StringBuffer prepareHumanStringForPlural(String paramName, String paramValue, String paramValueMin, String paramValueMax, Integer oper) {
         StringBuffer humanStr = new StringBuffer();
 
         humanStr.append(paramName);
         boolean branch = false;
 
         if (0 == oper.compareTo(OPERATOR_IS)) {
             humanStr.append(" are " + paramValue + "");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_STARTS)) {
             humanStr.append(" starts with '" + paramValue + "'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_CONTAINS)) {
             humanStr.append(" contains '" + paramValue + "'");
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_GREATER_OR_EQUAL)) {
             humanStr.append(" greater or equal than " + paramValue);
             branch = true;
         }
         if (0 == oper.compareTo(OPERATOR_SMALLER_OR_EQUAL)) {
             humanStr.append(" smaller or equal than " + paramValue);
             branch = true;
         }
 
         if (0 == oper.compareTo(OPERATOR_BETWEEN)) {
             humanStr.append(" between (including) " + paramValueMin);
             humanStr.append(" and ");
             humanStr.append(" (including) " + paramValueMax);
             branch = true;
         }
         if (!branch) {
             logger.warn(
                     "Warning: prepareHumanStringForPlural(" + paramName + ", "
                             + paramValue + ", " + paramValueMin + ", " + paramValueMax
                             + ", " + oper + "): No branch hit!");
             logger.warn("debugOperator returned: " + debugOperator(oper));
         }
         return humanStr;
     }
 
     /**
      * Map the operators to human readable string.
      *
      * @param rel The operator to be translated (ro.finsiel.eunis.search.Utilities.OPERATOR_XXXX)
      * @return The human readable string corresponding to that operator or " contains " if no match was found.
      */
     public static String ReturnStringRelatioOp(Integer rel) {
         if (0 == rel.compareTo(OPERATOR_CONTAINS)) {
             return " contains ";
         }
         if (0 == rel.compareTo(OPERATOR_IS)) {
             return " is ";
         }
         if (0 == rel.compareTo(OPERATOR_IS_NOT)) {
             return " is not ";
         }
         if (0 == rel.compareTo(OPERATOR_STARTS)) {
             return " starts with ";
         }
         return " contains ";
     }
 
     /**
      * Map operators to human readable string.
      *
      * @param rel The operator to be translated (ro.finsiel.eunis.search.Utilities.LOGICAL_AND / OR)
      * @return The human readable string corresponding to that operator or "" if no match was found.
      */
     public static String ReturnStringBoolean(Integer rel) {
         if (0 == rel.compareTo(Utilities.LOGICAL_AND)) {
             return " and ";
         }
         if (0 == rel.compareTo(Utilities.LOGICAL_OR)) {
             return " or ";
         }
         return "";
     }
 
     /**
      * This method finds the COMMON_NAME field from CHM62EDT_GROUP_SPECIES, ginving its ID_GROUP_SPECIES.
      *
      * @param idGroupSpecies the ID_GROUP_SPECIES field
      * @return The COMMON_NAME associated with that ID_GROUP_SPECIES from the CHM62EDT_GROUP_SPECIES table
      */
     public static String getGroupName(String idGroupSpecies) {
         String ret = null;
         List list = new Chm62edtGroupspeciesDomain().findWhere(
                 "ID_GROUP_SPECIES ='" + idGroupSpecies + "'");
 
         if (list != null && !list.isEmpty()) {
             ret = ((Chm62edtGroupspeciesPersist) list.get(0)).getCommonName();
         }
         return ret;
     }
 
     public static String getIdGroupSpeciesByGroupName(String groupName) {
         String ret = null;
         List list = new Chm62edtGroupspeciesDomain().findWhere(
                 "COMMON_NAME ='" + groupName + "'");
 
         if (list != null && !list.isEmpty()) {
             ret = ((Chm62edtGroupspeciesPersist) list.get(0)).getIdGroupspecies().toString();
         }
         return ret;
     }
 
     /**
      * This method gets the NAME field from CHM62EDT_CONSERVATION_STATUS, giving the ID_CONSERVATION_STATUS
      * field.
      *
      * @param id the ID_CONSERVATION_STATUS field
      * @return the NAME field corresponding to that ID
      */
     public static String getConservationName(String id) {
         String s = null;
         List l1 = new Chm62edtConservationStatusDomain().findWhere(
                 "ID_CONSERVATION_STATUS ='" + id + "'");
 
         if (l1 != null && l1.size() > 0) {
             s = ((Chm62edtConservationStatusPersist) l1.get(0)).getName();
         }
         return s;
     }
 
     /**
      * Find a string within a string and highlight it using HTML's <B>bolding</B> tag.
      *
      * @param data       String to be searched
      * @param searchTerm Searched string which will be highlighted within text data...
      * @return The original, formatted string (data formatted)
      */
     public static String highlightTerm(String data, String searchTerm) {
         String result = data;
 
         try {
             Pattern pattern = Pattern.compile(searchTerm);
             Matcher matcher = pattern.matcher(data);
 
             result = matcher.replaceAll("<strong>" + searchTerm + "</strong>");
         } catch (Exception exVal) {
             exVal.printStackTrace(System.err);
         } finally {
             if (null == result) {
                 result = data;
             }
         }
         return result;
     }
 
     /**
      * This methods finds if an habitat is EUNIS or ANNEX I.
      *
      * @param codeAnnexI This is the Code 2000 for the habitat
      * @return EUNIS_HABITAT or ANNEX_I_HABITAT.
      */
     public static Integer getHabitatType(String codeAnnexI) {
         Integer ret;
 
         if (null == codeAnnexI) {
             ret = EUNIS_HABITAT;
         } else {
             if (codeAnnexI.trim().equals("")) {
                 ret = EUNIS_HABITAT;
             } else {
                 ret = ANNEX_I_HABITAT;
             }
         }
         return ret;
     }
 
     /**
      * Map source to human readable string.
      *
      * @param source value like RefDomain.OTHER_INFO or RefDomain.SOURCE
      * @return 'Other reference' or 'Source'.
      */
     public static String returnSourceValueReferences(Short source) {
         if (source == null) {
             return "";
         } else {
             if (source.intValue() == RefDomain.OTHER_INFO.intValue()) {
                 return "Other reference";
             } else {
                 return "Source";
             }
         }
     }
 
     /**
      * This method checks if a string is null and if it is it returns an empty string ("") instead.
      *
      * @param s String to be checked
      * @return The s string or empty string if s was null
      * @see #formatString
      * @deprecated by formatString(Object object)
      */
     @Deprecated
     public static String IsContainsNull(String s) {
         return (s == null ? "" : (s.equalsIgnoreCase("null") ? "" : s));
     }
 
     /**
      * This method finds the references for a term used within a specific module from web site.
      *
      * @param searchString searched string.
      * @param operand      relation operator.
      * @param useTerm      search in terms realm.
      * @param useDef       search in definition realm.
      * @param module       search module (species, habitats, sites).
      * @return List of Chm62edtGlossaryPersist objects.
      */
     public static List findGlossaryTerms(String searchString,
             Integer operand,
             boolean useTerm,
             boolean useDef,
             String module) {
         List list = new Vector();
 
         if (useTerm || useDef) {
             String sql = " 1 = 1 ";
 
             if (module != null) {
                 sql += " AND TERM_DOMAIN LIKE '%" + module + "%' ";
             }
             sql += "AND ( ";
             if (useTerm) {
                 sql += Utilities.prepareSQLOperator(" TERM ", searchString,
                         operand);
                 sql += (useDef) ? " OR " : " ) ";
             }
             if (useDef) {
                 sql += Utilities.prepareSQLOperator(" DEFINITION ", searchString,
                         operand);
                 sql += " ) ";
             }
             try {
                 list = new Chm62edtGlossaryDomain().findWhere(
                         sql + " ORDER BY TERM");
             } catch (Exception exVal) {
                 exVal.printStackTrace(System.err);
             } finally {
                 if (null == list) {
                     list = new Vector();
                 }
             }
         }
         return list;
     }
 
     /**
      * This method parse if the first string begin, contains or is equal with the second string depend to relationOp value.
      *
      * @param s1         first string
      * @param s2         second string
      * @param relationOp relation between first string and second string
      * @return a boolean value.
      */
     public static boolean ifStringCorespund(String s1, String s2, Integer relationOp) {
         boolean corespund = false;
 
         if (s1 != null && s2 != null) {
             if (s2.equalsIgnoreCase("%")) {
                 return true;
             }
             if (relationOp.compareTo(OPERATOR_CONTAINS) == 0) {
                 corespund = true;
                 if (s1.toLowerCase().indexOf(s2.toLowerCase()) == -1) {
                     corespund = false;
                 }
             }
 
             if (relationOp.compareTo(OPERATOR_IS) == 0) {
                 corespund = s1.equalsIgnoreCase(s2);
             }
 
             if (relationOp.compareTo(OPERATOR_STARTS) == 0) {
                 corespund = s1.toLowerCase().startsWith(s2.toLowerCase());
             }
         }
         return corespund;
     }
 
     /**
      * This method trims the spaces from an array of strings.
      *
      * @param array The array of strings which are to be trimmed.
      * @return The trimmed array.
      */
     public static String[] trimArray(String[] array) {
         String[] result = null;
 
         if (null != array) {
             result = new String[array.length];
             for (int i = array.length - 1; i >= 0; i--) {
                 result[ i ] = (null != array[ i ])
                         ? array[ i ].trim()
                                 : array[ i ];
             }
         }
         return result;
     }
 
     /**
      * Translate the SOURCE_DB field from CHM62EDT_SITES in SQL compatible language.
      *
      * @param sourceDB The source database in human language
      * @return The string identifying the DB within database (ie. CDDA National corresponds to CDDA_NATIONAL).
      * @see ro.finsiel.eunis.search.sites.SitesSearchUtility#translateSourceDBInvert(String)
      * @deprecated by ro.finsiel.eunis.search.sites.SitesSearchUtilities#translateSourceDBInvert;
      */
     @Deprecated
     public static String SourceDBSite(String sourceDB) {
         if (sourceDB.equalsIgnoreCase("CDDA National")) {
             return "CDDA_NATIONAL";
         }
         if (sourceDB.equalsIgnoreCase("CDDA International")) {
             return "CDDA_INTERNATIONAL";
         }
         if (sourceDB.equalsIgnoreCase("Natura 2000")) {
             return "NATURA2000";
         }
         if (sourceDB.equalsIgnoreCase("Corine biotopes")) {
             return "CORINE";
         }
         if (sourceDB.equalsIgnoreCase("Diploma sites")) {
             return "DIPLOMA";
         }
         if (sourceDB.equalsIgnoreCase("Biogenetic reserve")) {
             return "BIOGENETIC";
         }
         if (sourceDB.equalsIgnoreCase("NatureNet")) {
             return "NATURENET";
         }
         if (sourceDB.equalsIgnoreCase("Emerald")) {
             return "EMERALD";
         }
         return sourceDB;
     }
 
     /**
      * Retrieve habitat which contains species.
      *
      * @param search   Species scientific name.
      * @param op       relation operator.
      * @param useLimit limit the output.
      * @return Vector of String objects with scientific names.
      */
     public static Vector getHabitatsWithSpecies(String search, String op, boolean useLimit) {
         Vector v = new Vector();
 
         String dom = (Utilities.prepareSQLOperator("H.SCIENTIFIC_NAME", search, Utilities.checkedStringToInt(op, Utilities.OPERATOR_CONTAINS))).toString();
 
         List l1 = new HabitatNatureObjectReportTypeDomain().findWhere(
                 "H.ID_HABITAT<>'-1' AND H.ID_HABITAT<>'10000' AND B.ID_NATURE_OBJECT_LINK IS NULL AND "
                         + dom + " GROUP BY H.ID_HABITAT");
         List l2 = new HabitatNatureObjectGeoscopeDomain().findWhere(
                 "H.ID_HABITAT<>'-1' AND H.ID_HABITAT<>'10000' AND B.ID_NATURE_OBJECT_LINK IS NULL AND "
                         + dom + " GROUP BY H.ID_HABITAT");
         List l3 = new CommonRecordsDomain().findWhere(
                 "H.ID_HABITAT<>'-1' AND H.ID_HABITAT<>'10000' AND " + dom
                 + " GROUP BY H.ID_HABITAT");
 
         if (l1 != null) {
             for (int i = 0; i < l1.size(); i++) {
                 HabitatNatureObjectReportTypePersist s1 = (HabitatNatureObjectReportTypePersist) l1.get(
                         i);
 
                 if (useLimit) {
                     if (v.size() < Utilities.MAX_POPUP_RESULTS) {
                         v.addElement(s1.getScientificName());
                     }
                 } else {
                     v.addElement(s1.getScientificName());
                 }
             }
         }
 
         if (l2 != null) {
             for (int i = 0; i < l2.size(); i++) {
                 HabitatNatureObjectGeoscopePersist s2 = (HabitatNatureObjectGeoscopePersist) l2.get(
                         i);
 
                 if (useLimit) {
                     if (v.size() < Utilities.MAX_POPUP_RESULTS) {
                         v.add(s2.getScientificName());
                     }
                 } else {
                     v.add(s2.getScientificName());
                 }
             }
         }
 
         if (l3 != null) {
             for (int i = 0; i < l3.size(); i++) {
                 CommonRecordsPersist s3 = (CommonRecordsPersist) l3.get(i);
 
                 if (useLimit) {
                     if (v.size() < Utilities.MAX_POPUP_RESULTS) {
                         v.add(s3.getScientificName());
                     }
                 } else {
                     v.add(s3.getScientificName());
                 }
             }
         }
 
         SortListString sorter = new SortListString();
 
         v = sorter.sort(v, false);
 
         return v;
 
     }
 
     /**
      * This method formats the area field from the sites module which is displayed within HTML result pages.
      * The purpose is to align numbers, therefore the Courier font.
      *
      * @param input the input string
      * @param left  how many spaces to fill on the left side
      * @param right how many spaces to fill on the right side
      * @param blank which is the blank character to fill empty spaces (ie. in HTML is used &nbsp;)
      * @return The formatted string
      */
     public static String formatArea(String input, int left, int right, String blank) {
         String result = "<span style=\"font-family: 'Courier New', courier; font-size: 95%\">";
 
         result += formatArea(input, left, right, blank, null);
         result += "</span>";
         return result;
     }
 
     /**
      * This method formats the area field from the sites module which is displayed within HTML result pages.
      *
      * @param input    the input string
      * @param left     how many spaces to fill on the left side
      * @param right    how many spaces to fill on the right side
      * @param blank    which is the blank character to fill empty spaces (ie. in HTML is used &nbsp;)
      * @param cssStyle CSS Style applied to the string. If null, no CSS applied. Result will look like:<br />
      *                 &lt;FONT style="cssStyle"&gt; area &lt;/FONT&gt;
      * @return The formatted string
      */
     public static String formatArea(String input, int left, int right, String blank, String cssStyle) {
         String result;
 
         if (null != input && !input.equalsIgnoreCase("-1")) {
             input = input.replaceAll(",", ".");
             int pos;
             int leftLen;
             int rightLen;
 
             pos = input.lastIndexOf(".");
             if (-1 != pos) {
                 leftLen = (input.substring(0, pos)).length();
                 rightLen = (input.substring(pos + 1)).length();
 
                 String leftString = spaces(left - leftLen, blank)
                         + input.substring(0, pos);
                 // String rightString = (input.substring(pos  + 1) + spaces(right - rightLen, blank)).substring(0, 3);
                 String rightString;
 
                 if (right > rightLen) {
                     rightString = input.substring(pos + 1).substring(0, rightLen)
                             + spaces(right - rightLen, blank);
                 } else {
                     rightString = input.substring(pos + 1).substring(0, right);
                 }
                 result = leftString + "." + rightString;
             } else {
                 leftLen = input.length();
                 String leftString = spaces(left - leftLen, blank) + input;
 
                 result = leftString + spaces(right + 1, blank);
             }
             if (null != cssStyle) {
                 result = "<span style=\"" + cssStyle + "\">" + result;
                 result += "</span>";
             }
         } else {
             return "&nbsp;";
         }
         return result;
     }
 
     /**
      * This method formats the area field from the sites module which is displayed within PDF.
      *
      * @param input the input string
      * @param left  how much spaces to left on the left side
      * @param right how much spaces to let on the right side
      * @param blank which is the blank character to fill empty spaces (ie. in HTML is used &nbsp;)
      * @return The formatted string
      */
     public static String formatAreaPDF(String input, int left, int right, String blank) {
 
         String result = " ";
 
         if (blank == null) {
             blank = " ";
         }
 
         if (null != input && !input.equalsIgnoreCase("-1")) {
             input = input.replace(",", ".");
             int pos;
             int leftLen;
             int rightLen;
 
             pos = input.lastIndexOf(".");
             if (-1 != pos) {
                 leftLen = (input.substring(0, pos)).length();
                 rightLen = (input.substring(pos + 1)).length();
 
                 String leftString = spaces(left - leftLen, blank) + input.substring(0, pos);
                 String rightString = (input.substring(pos + 1) + spaces(right - rightLen, blank)).substring(0, 3);
 
                 result = leftString + "." + rightString;
             } else {
                 leftLen = input.length();
                 String leftString = spaces(left - leftLen, blank) + input;
 
                 result = leftString + spaces(right + 1, blank);
             }
         } else {
             return result;
         }
         result = result.replace(".", ",");
         return result;
     }
 
     /**
      * Find a country name, searching by its ISO code. This method searches in CHM62EDT_COUNTRY and matches
      * iso with ISO_3L, returning the AREA_NAME_ENGLISH field
      *
      * @param iso ISO_3L for the country (3-letters ISO code)
      * @return Country name or empty string if country not found.
      */
     public static String findCountryByISO(String iso) {
         if (iso != null && !iso.equalsIgnoreCase("")) {
             List countries = new Chm62edtCountryDomain().findWhere(
                     "ISO_3L IS NOT NULL AND TRIM(ISO_3L) <>'' AND ISO_3L ='"
                             + iso + "'");
 
             if (countries != null && countries.size() > 0) {
                 return ((Chm62edtCountryPersist) countries.get(0)).getAreaNameEnglish();
             } else {
                 return "";
             }
         } else {
             return "";
         }
     }
 
     /**
      * Find a country name, searching by its ID_GEOSCOPE code. This method searches in CHM62EDT_COUNTRY and matches
      * iso with ID_GEOSCOPE, returning the AREA_NAME_ENGLISH field
      *
      * @param idGeoscope ID_GEOSCOPE for the country
      * @return Country name or empty string if country not found.
      */
     public static String findCountryByIdGeoscope(String idGeoscope) {
         if (idGeoscope != null && !idGeoscope.equalsIgnoreCase("")) {
             List countries = new Chm62edtCountryDomain().findWhere(
                     "ID_GEOSCOPE IS NOT NULL AND ID_GEOSCOPE =" + idGeoscope);
 
             if (countries != null && countries.size() > 0) {
                 return ((Chm62edtCountryPersist) countries.get(0)).getAreaNameEnglish();
             } else {
                 return "";
             }
         } else {
             return "";
         }
     }
 
     /**
      * Find a country name, searching by its ID_COUNTRY. This method searches in CHM62EDT_COUNTRY and matches
      * idCountry with ID_COUNTRY, returning the AREA_NAME_ENGLISH field
      *
      * @param idCountry ID_COUNTRY for the country
      * @return Country name or empty string if country not found.
      */
     public static String getCountryName(String idCountry) {
         if (idCountry != null && idCountry.trim().length() > 0) {
             List countries = new Chm62edtCountryDomain().findWhere(
                     "ID_COUNTRY = " + idCountry);
 
             if (countries != null && countries.size() > 0) {
                 return ((Chm62edtCountryPersist) countries.get(0)).getAreaNameEnglish();
             } else {
                 return "";
             }
         } else {
             return "";
         }
     }
 
     /**
      * Return a where condition string like '... and (A.SOURCE_DB='corine' or A.SOURCE_DB='CDDA_NATIONAL' OR A.SOURCE.DB='CDDA_INTERNATIONAL')'.
      *
      * @param sql       is other part of sql where condition of query for witch on construct this where condition string
      * @param sourceDb is a boolean vector witch indicate whitch source database has been selected
      * @param db        is a vector whitch contains all source database values from database
      * @param alias     Table alias.
      * @return where condition.
      */
     public static StringBuffer getConditionForSourceDB(StringBuffer sql, boolean[] sourceDb, String[] db, String alias) {
         StringBuffer filterSQL = sql;
         boolean exist = false;
 
         for (int i = 0; i < sourceDb.length; i++) {
             if (sourceDb[ i ]) {
                 exist = true;
             }
         }
 
         if (exist) {
             if (filterSQL.length() > 0) {
                 filterSQL.append(" AND  ");
             }
             filterSQL.append(" ( ");
 
             boolean putOR = false;
 
             for (int i = 0; i < sourceDb.length; i++) {
                 if (sourceDb[ i ]) {
                     if (putOR) {
                         filterSQL.append(
                                 " or " + alias + ".SOURCE_DB = '" + db[ i ]
                                         + "' ");
                     } else {
                         filterSQL.append(
                                 " " + alias + ".SOURCE_DB = '" + db[ i ] + "' ");
                     }
                     putOR = true;
                 }
             }
             filterSQL.append(" ) ");
         }
 
         if (!exist) {
             if (filterSQL.length() > 0) {
                 filterSQL.append(" AND ");
             }
             filterSQL.append(" ( ");
             for (int i = 0; i < sourceDb.length; i++) {
                 if (i > 0) {
                     filterSQL.append(
                             " and " + alias + ".SOURCE_DB <> '" + db[ i ] + "' ");
                 } else {
                     filterSQL.append(
                             " " + alias + ".SOURCE_DB <> '" + db[ i ] + "' ");
                 }
             }
 
             filterSQL.append(" ) ");
         }
 
         return filterSQL;
     }
 
     /**
      * Return if a string appear or not in a vector.
      *
      * @param v is a vector
      * @param s is a string
      * @return false if s or v are null or if s not appear in v
      */
     public static boolean ifStringAppear(Vector v, String s) {
         if (null == s) {
             return false;
         }
         if (null == v) {
             return false;
         }
         boolean ret = false;
 
         for (int i = 0; i < v.size(); i++) {
             String element = (String) v.get(i);
 
             if (null != element && element.equalsIgnoreCase(s)) {
                 return true;
             }
         }
         return ret;
     }
 
     /**
      * Used to generate a string of n characters.
      *
      * @param n         - Number of characters which repeats in string
      * @param character - The character being repeated
      * @return A string of characters. For example 'spaces(3, "x")' will return "xxx" string.
      *         Note: now it is optimized to use StringBuffer instead of plain string concatenation.
      */
     private static String spaces(int n, String character) {
         StringBuffer result = new StringBuffer();
 
         for (int i = 0; i < n; i++) {
             result.append(character);
         }
         return result.toString();
     }
 
     /**
      * @param str - string to split
      */
     public static String SplitString(String str) {
         String result = "";
         String low;
         String up;
 
         for (int i = 0; i < str.length(); i++) {
             low = str.substring(i, i + 1);
             up = str.substring(i, i + 1).toUpperCase();
             if (low.equals(up) && i > 0) {
                 result += " ";
             }
             result += str.substring(i, i + 1);
         }
         return result;
     }
 
     /**
      * Formats a name like "SOURCE_DATABASE" to "Source database".
      *
      * @param str - field name to format
      */
     public static String FormatDatabaseFieldName(String str) {
         if (str == null) {
             return "";
         }
         String result = "";
         String s;
 
         for (int i = 0; i < str.length(); i++) {
             s = str.substring(i, i + 1);
             if (s.equals("_") && i > 0) {
                 result += " ";
             } else {
                 if (i == 0) {
                     result += str.substring(i, i + 1).toUpperCase();
                 } else {
                     result += str.substring(i, i + 1).toLowerCase();
                 }
             }
         }
         return result;
     }
 
     /**
      * Clear the database tables which were filled during a session. This method is called by the SessionListener
      * when the session is destroyed.
      *
      * @param IdSession ID of the session being expired
      * @param SQL_DRV   SQL Driver used to do the db connection (from web.xml)
      * @param SQL_URL   SQL URL used to do the db connection (from web.xml)
      * @param SQL_USR   SQL user used to do the db connection (from web.xml)
      * @param SQL_PWD   SQL password used to do the db connection (from web.xml)
      */
     public static void ClearSessionData(String IdSession, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
         try {
             ro.finsiel.eunis.search.CombinedSearch tcs = new ro.finsiel.eunis.search.CombinedSearch();
 
             tcs.Init(SQL_DRV, SQL_URL, SQL_USR, SQL_PWD);
             tcs.DeleteSessionData(IdSession);
 
             ro.finsiel.eunis.search.AdvancedSearch tas = new ro.finsiel.eunis.search.AdvancedSearch();
 
             tas.Init(SQL_DRV, SQL_URL, SQL_USR, SQL_PWD);
             tas.DeleteSessionData(IdSession);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Clear all the temporary tables.
      *
      * @param SQL_DRV JDBC driver.
      * @param SQL_URL JDBC url.
      * @param SQL_USR JDBC user.
      * @param SQL_PWD JDBC password.
      * @return true if operation succeed.
      */
     public static boolean ClearAllSessionsData(String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
         boolean result = false;
 
         try {
             ro.finsiel.eunis.search.CombinedSearch tcs = new ro.finsiel.eunis.search.CombinedSearch();
 
             tcs.Init(SQL_DRV, SQL_URL, SQL_USR, SQL_PWD);
             tcs.DeleteAllTemporaryData();
 
             ro.finsiel.eunis.search.AdvancedSearch tas = new ro.finsiel.eunis.search.AdvancedSearch();
 
             tas.Init(SQL_DRV, SQL_URL, SQL_USR, SQL_PWD);
             tas.DeleteAllTemporaryData();
 
             result = true;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return result;
     }
 
     /**
      * Find a reference by an idDc and return a vector with two elements , first element contains author of that
      * reference and second element contains url of reference.
      *
      * @param idDc idDC of reference
      * @return {author, url} vector with these two objects.
      */
     public static Vector getAuthorAndUrlByIdDc(String idDc) {
         String author = "";
         String url = "";
         Vector result = new Vector();
 
         try {
             List references = new ReferencesJoinDomain().findWhere("ID_DC = " + idDc);
 
             if (references != null && references.size() > 0) {
                 author = (((ReferencesJoinPersist) references.get(0)).getSource()
                         == null
                         ? ""
                                 : ((ReferencesJoinPersist) references.get(0)).getSource());
                 url = (((ReferencesJoinPersist) references.get(0)).getUrl()
                         == null
                         ? ""
                                 : ((ReferencesJoinPersist) references.get(0)).getUrl());
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         result.addElement(author);
         result.addElement(url);
         return result;
     }
 
     /**
      * Translate a numeric value of database source variable in habitats searches into a string.
      *
      * @param source      value of the database variable from a habitats search
      * @param annex_value value of the SEARCH_ANNEX1 constant by the corresponded Search Criteria class
      * @param both_value  value of the SEARCH_BOTH constant by the corresponded Search Criteria class
      * @return String.
      */
     public static String getSourceHabitat(Integer source, int annex_value, int both_value) {
         String res = " EUNIS ";
 
         if (source.intValue() == annex_value) {
             res = " ANNEX I ";
         }
         if (source.intValue() == both_value) {
             res = " EUNIS and ANNEX I ";
         }
         return res;
     }
 
     /**
      * Find a reference by her idDc.
      *
      * @param idDc idDC of reference
      * @return Return references tooltip for an nature object, giving its ID_DC.
      */
     public static String getReferencesByIdDc(String idDc) {
         List references = new ReferencesJoinDomain().findWhere("ID_DC = '" + idDc + "'");
         String result = "";
 
         if (references != null && references.size() > 0) {
             result = "<ul>";
             result += "<li>Author : "
                     + (((ReferencesJoinPersist) references.get(0)).getSource()
                             == null
                             ? ""
                                     : treatURLSpecialCharacters(
                                             removeQuotes(
                                                     ((ReferencesJoinPersist) references.get(0)).getSource())));
             result += "</li>";
             result += " <li>Title : "
                     + (((ReferencesJoinPersist) references.get(0)).getTitle()
                             == null
                             ? ""
                                     : treatURLSpecialCharacters(
                                             removeQuotes(
                                                     ((ReferencesJoinPersist) references.get(0)).getTitle())));
             result += "</li>";
             result += " <li>Editor : "
                     + (((ReferencesJoinPersist) references.get(0)).getEditor()
                             == null
                             ? ""
                                     : treatURLSpecialCharacters(
                                             removeQuotes(
                                                     ((ReferencesJoinPersist) references.get(0)).getEditor())));
             result += "</li>";
             result += " <li>Publisher : "
                     + (((ReferencesJoinPersist) references.get(0)).getPublisher()
                             == null
                             ? ""
                                     : treatURLSpecialCharacters(
                                             removeQuotes(
                                                     ((ReferencesJoinPersist) references.get(0)).getPublisher())));
             result += "</li>";
             String date = "";
 
             if (((ReferencesJoinPersist) references.get(0)).getCreated() != null) {
                 date = new SimpleDateFormat("yyyy").format(new Date(((ReferencesJoinPersist) references.get(0)).getCreated().getTime())).toString();
             }
             result += " <li>Date : " + date;
             result += "</li>";
             result += " <li>Url : "
                     + (((ReferencesJoinPersist) references.get(0)).getUrl()
                             == null
                             ? ""
                                     : treatURLSpecialCharacters(
                                             ((ReferencesJoinPersist) references.get(0)).getUrl()));
             result += "</li>";
             result += "</ul>";
         }
 
         result = result.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
                 ">", "&gt;");
         return result;
     }
 
     /**
      * Return current system time, formatted correctly (dd MMMM yyyy).
      *
      * @return System date and time.
      */
     public static String getCurrentDate() {
         String currentDate = new Date().toString();
 
         try {
             currentDate = new SimpleDateFormat("dd MMMM yyyy").format(new Date()).toString();
         } catch (Exception exVal) {
             exVal.printStackTrace(System.err);
         }
         return currentDate;
     }
 
     /**
      * Return current system time, formatted correctly (dd MMMM yyyy HH:mm:ss).
      *
      * @return System date and time.
      */
     public static String getCurrentDateTime() {
         String currentDate = new Date().toString();
 
         try {
             currentDate = new SimpleDateFormat("dd MMMM yyyy HH:mm:ss").format(new Date()).toString();
         } catch (Exception exVal) {
             exVal.printStackTrace(System.err);
         }
         return currentDate;
     }
 
     /**
      * Replace occurrences of a string within another string.
      *
      * @param s       String to be processed.
      * @param find    Searched string.
      * @param replace Replacing string.
      * @return Modified string.
      */
     public static String replace(String s, String find, String replace) {
         int findLength;
         // the next statement has the side effect of throwing a null pointer
         // exception if s is null.
         int stringLength = s.length();
 
         if (find == null || (findLength = find.length()) == 0) {
             // If there is nothing to find, we won't try and find it.
             return s;
         }
         if (replace == null) {
             // a null string and an empty string are the same
             // for replacement purposes.
             replace = "";
         }
         int replaceLength = replace.length();
 
         // We need to figure out how long our resulting string will be.
         // This is required because without it, the possible resizing
         // and copying of memory structures could lead to an unacceptable runtime.
         // In the worst case it would have to be resized n times with each
         // resize having a O(n) copy leading to an O(n^2) algorithm.
         int length;
 
         if (findLength == replaceLength) {
             // special case in which we don't need to count the replacements
             // because the count falls out of the length formula.
             length = stringLength;
         } else {
             int count;
             int start;
             int end;
 
             // Scan s and count the number of times we find our target.
             count = 0;
             start = 0;
             while ((end = s.indexOf(find, start)) != -1) {
                 count++;
                 start = end + findLength;
             }
             if (count == 0) {
                 // special case in which on first pass, we find there is nothing
                 // to be replaced.  No need to do a second pass or create a string buffer.
                 return s;
             }
             length = stringLength - (count * (findLength - replaceLength));
         }
 
         int start = 0;
         int end = s.indexOf(find, start);
 
         if (end == -1) {
             // nothing was found in the string to replace.
             // we can get this if the find and replace strings
             // are the same length because we didn't check before.
             // in this case, we will return the original string
             return s;
         }
         // it looks like we actually have something to replace
         // *sigh* allocate memory for it.
         StringBuffer sb = new StringBuffer(length);
 
         // Scan s and do the replacements
         while (end != -1) {
             sb.append(s.substring(start, end));
             sb.append(replace);
             start = end + findLength;
             end = s.indexOf(find, start);
         }
         end = stringLength;
         sb.append(s.substring(start, end));
 
         return (sb.toString());
     }
 
     /**
      * Find ID_SPECIES for a species, giving its ID_NATURE_OBJECT.
      *
      * @param idNO ID_NATURE_OBJECT.
      * @return ID_SPECIES.
      */
     public static Integer getIdSpeciesByIdNatureObject(String idNO) {
         Integer id = new Integer(-1);
         List species = new Chm62edtSpeciesDomain().findWhere(
                 "ID_NATURE_OBJECT='" + idNO + "'");
 
         if (species != null && species.size() > 0) {
             id = ((Chm62edtSpeciesPersist) species.get(0)).getIdSpecies();
         }
         return id;
     }
 
     /**
      * Transform OPERATOR_##### into displayable string, for example debugOperator(OPERATOR_BETWEEN)
      * returns the "OPERATOR_BETWEEN" string.
      *
      * @param operator Operator value as Integer
      * @return Operator name as string
      */
     public static String debugOperator(Integer operator) {
         String result;
 
         if (null == operator) {
             result = "Operator is null.";
         } else {
             if (operator.intValue() == OPERATOR_BETWEEN.intValue()) {
                 result = "OPERATOR_BETWEEN";
             } else if (operator.intValue() == OPERATOR_CONTAINS.intValue()) {
                 result = "OPERATOR_CONTAINS";
             } else if (operator.intValue()
                     == OPERATOR_GREATER_OR_EQUAL.intValue()) {
                 result = "OPERATOR_GREATER_OR_EQUAL";
             } else if (operator.intValue() == OPERATOR_IS.intValue()) {
                 result = "OPERATOR_IS";
             } else if (operator.intValue() == OPERATOR_IS_NOT.intValue()) {
                 result = "OPERATOR_IS_NOT";
             } else if (operator.intValue()
                     == OPERATOR_SMALLER_OR_EQUAL.intValue()) {
                 result = "OPERATOR_SMALLER_OR_EQUAL";
             } else if (operator.intValue() == OPERATOR_STARTS.intValue()) {
                 result = "OPERATOR_STARTS";
             } else {
                 result = "Operator '" + operator + "'"
                         + " is not defined as valid operator.";
             }
         }
         return result;
     }
 
     /**
      * Set/Remove the filter for displaying invalidated species in results.
      *
      * @param tableColumn Colunmn of the table to be filtered.
      * @param show        true if display.
      * @return SQL compatible condition.
      */
     public static String showEUNISInvalidatedSpecies(String tableColumn, boolean show) {
         String sql = "";
 
         if (show) { // No filter condition necesary
         } else {
             sql = " " + tableColumn + " > 0 ";
         }
         return sql;
     }
 
     /**
     * Replace strange characters (', ",`, �, \u0027) from a string with empty space.
      *
      * @param str String to be processed.
      * @return Processed string.
      */
     public static String removeQuotes(String str) {
         return removeQuotes(str, " ");
     }
 
     public static String removeQuotes(String str, String replacement) {
         if (str == null) {
             return "";
         }
         char ch = (char) 39;
         char chspace = (char) 32;
 
        str = str.replaceAll("\"", replacement).replaceAll("'", replacement).replaceAll("\"", replacement).replaceAll("�", replacement).replaceAll("`", replacement).replaceAll("�", replacement).replaceAll("'", replacement).replaceAll(
                "\u0027", replacement);
         str = str.replace(ch, chspace);
 
         return str;
     }
 
     public static String removeSpaces(String str) {
         if (str == null) {
             return "";
         }
         return str.replaceAll(" ", "");
     }
 
     public static String cleanString(String str) {
         str = removeQuotes(str, "");
         str = removeSpaces(str);
         str = str.replaceAll(",", "");
         return str;
     }
 
     /**
      * Referse the content of a string.
      *
      * @param str String to be reversed.
      * @return String.
      */
     public static String reverseString(String str) {
         if (str == null) {
             return null;
         }
         int lgn = str.length();
         String yy = "";
 
         for (int i = 1; i < lgn; yy += str.substring(lgn - i, lgn - (i++))) {
             ;
         }
         return yy;
     }
 
     /**
      * Warning string displayed in popups (helpers).
      *
      * @return HTML string content.
      */
     public static String getTextWarningForPopup(int size) {
         String result = ""
                 + "<strong>Warning: Database might not contains data for all values!</strong> "
                 + "<br /> " + " ";
 
         // if (size > 100)
         // result += "<strong>Note:</strong> Only the first " + Utilities.MAX_POPUP_RESULTS + " values were displayed. Please refine the search criteria. " +
         // "<br /><br /> ";
         // else result +="<br />";
         return result;
     }
 
     public static String getTextWarningForPopup(WebContentManagement cm, int size) {
         String result = "" + "<strong>"
                 + cm.cmsPhrase(
                         "Warning: Database might not contain data for all values!")
                         + "</strong> "
                         + "<br /> "
                         + " ";
 
         // if (size > 100)
         // result += "<strong>Note:</strong> Only the first " + Utilities.MAX_POPUP_RESULTS + " values were displayed. Please refine the search criteria. " +
         // "<br /><br /> ";
         // else result +="<br />";
         return result;
     }
 
     /**
      * Warning string displayed in popups (helpers).
      *
      * @return HTML string content.
      */
     public static String getAdvancedTextWarningForPopup(int size) {
         String result = ""
                 + "<strong>Warning: Database might not contains data for all values!</strong> "
                 + "<br /> " + " ";
 
         if (size > 100) {
             result += "<strong>Note:</strong> Only the first "
                     + Utilities.MAX_POPUP_RESULTS
                     + " values are displayed.<br /><br />";
         } else {
             result += "<br />";
         }
         return result;
     }
 
     /**
      * Replace characters having special meaning inside HTML tags
      * with their escaped equivalents, using character entities.
      *
      * @param str String to be parsed
      * @return Processed string.
      */
     public static String treatURLSpecialCharacters(String str) {
         if (str == null) {
             return "";
         }
         String result = str;
 
         result = result.replaceAll("&", "&amp;");
         result = result.replaceAll("<", "&lt;");
         result = result.replaceAll(">", "&gt;");
         result = result.replaceAll("\"", "&quot;");
         result = result.replaceAll("'", "&#039;");
         result = result.replaceAll("\\\\", "&#092;");
         result = result.replaceAll("%", "&#037;");
 
         return result;
     }
 
     public static String treatURLAmp(String str) {
         if (str == null) {
             return "";
         }
         String result = str;
 
         result = result.replaceAll("&", "&amp;");
 
         return result;
     }
 
     /**
      * Limit string displayed in popups (helpers).
      *
      * @return HTML string content.
      */
     public static String getTextMaxLimitForPopup(int size) {
         String result = (size >= Utilities.MAX_POPUP_RESULTS
                 ? "" + "<br /> " + "<strong>Note:</strong> Only the first "
                 + Utilities.MAX_POPUP_RESULTS
                 + " values were displayed. Please refine the search criteria. "
                 + "<br /><br /> "
                 : "<br />");
 
         return result;
     }
 
     public static String getTextMaxLimitForPopup(WebContentManagement cm, int size) {
         String result = (size >= Utilities.MAX_POPUP_RESULTS
                 ? "<br /> " + cm.cmsPhrase("<strong>Note:</strong> Only first")
                         + " " + Utilities.MAX_POPUP_RESULTS + " "
                         + cm.cmsPhrase(
                                 " values were displayed. Please refine the search criteria.")
                                 + "<br /><br /> "
                                 : "<br />");
 
         return result;
     }
 
     public static java.sql.Timestamp stringToTimeStamp(String dataIn, String formatData) {
 
         if (dataIn == null || dataIn.trim().length() <= 0 || formatData == null
                 || formatData.trim().length() <= 0) {
             return null;
         }
         SimpleDateFormat sdf = new SimpleDateFormat(formatData);
 
         try {
             java.util.Date d = sdf.parse(dataIn);
 
             return (new java.sql.Timestamp(d.getTime()));
         } catch (java.text.ParseException e) {
             e.printStackTrace();
         }
         return null;
     }
 
     public static String formatDate(Date dataIn, String formatData) {
         if (dataIn == null || dataIn.toString().trim().length() <= 0
                 || formatData == null || formatData.trim().length() <= 0) {
             return "";
         }
         try {
             DateFormat formatter = new SimpleDateFormat(formatData);
 
             return formatter.format(dataIn);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return "";
     }
 
     public static String formatDate(String dataIn) {
         String ret = "";
 
         if (dataIn == null || dataIn.toString().trim().length() <= 4) {
             return ret;
         }
         try {
             ret = dataIn.substring(0, 4);
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ret;
     }
 
     /**
      * Test method.
      *
      * @param args Command line args
      */
     public static void main(String[] args) { // BasicConfigurator.configure();
         // Test checkedStringToInt
         // Test trimArray
         // String testString = "-123.3435242";
         // logger.info(">>>" + formatArea(testString, 9, 5, "X").replaceAll(" ", "*") + "<<<");
         // System.out.println(formatArea(testString, 9, 1, "X"));
         // System.out.println(Utilities.getCurrentDate());
         // String[] testArray = {" 1 ", "2 ", null, "4" };
         // testArray = trimArray(testArray);
         // for (int i = 0;  i  < testArray.length; i++) {
         // logger.info(">>" + testArray[i] + "<<");
         // }
         //
         // // Test trimSpaces
         // logger.info(checkedStringToInt("1234.5", 123));
         // logger.info("'  abcd   ' =>" + new String("  abcd   ").trim() + "<");
         //
         // String test = "my test for replacement. replaces patterns within text";
         // test = Utilities.highlightTerm(test, "replace");
         // logger.info(test);
         // String str = "121323xfddfd";
         // logger.info(str.replaceAll("[a-z]", ""));
     }
 
     public static String formatDecimal(Object val, int decimals) {
         String val2 = "";
 
         decimals++;
         try {
             if (val != null) {
                 val2 = val.toString();
                 int pos = val2.indexOf(".");
 
                 if (pos > 0 && pos + decimals <= val2.length()) {
                     val2 = val2.substring(0, pos + decimals);
                 }
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return val2;
     }
 
     public static String getSitesCountryFactsheetInTable(List factList, WebContentManagement contentManagement) {
         if (factList == null || factList.size() <= 0) {
             return "<!-- factlist size 0 -->";
         }
         String result = "";
         boolean isGood = false;
 
         result += "<tr><th scope=\"row\">"
                 + contentManagement.cmsPhrase("No. of sites") + "</th>"
                 + "<tr><th scope=\"row\">"
                 + contentManagement.cmsPhrase("No. of species") + "</th>"
                 + "<tr><th scope=\"row\">"
                 + contentManagement.cmsPhrase("No. of habitat types") + "</th>"
                 + "<tr><th scope=\"row\">"
                 + contentManagement.cmsPhrase("No. of sites/km2") + "</th>"
                 + "<tr><th scope=\"row\">"
                 + contentManagement.cmsPhrase(
                         "Percent number of sites with surface data available")
                         + "</th>"
                         + "<tr><th scope=\"row\">"
                         + contentManagement.cmsPhrase("Total area (ha)")
                         + "</th>"
                         + "<tr><th scope=\"row\">"
                         + contentManagement.cmsPhrase("Average area(ha)")
                         + "</th>"
                         + "<tr><th scope=\"row\">"
                         + contentManagement.cmsPhrase(
                                 "Standard deviation for sites area")
                                 + "</th>";
 
         for (int i = 0; i < factList.size(); i++) {
             CountrySitesFactsheetPersist site = (CountrySitesFactsheetPersist) factList.get(
                     i);
 
             String[] splitResult = result.split("<tr>");
 
             if (splitResult != null && splitResult.length == 9) {
                 splitResult[ 1 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getNumberOfSites(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
                 splitResult[ 2 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getNumberOfSpecies(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
                 splitResult[ 3 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getNumberOfHabitats(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
                 splitResult[ 4 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getPerSquare(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
                 splitResult[ 5 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getSurfaceAvailable(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
                 splitResult[ 6 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getTotalSize(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
                 splitResult[ 7 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getAvgSize(), 0, 2, "&nbsp;")
                         + "</td>";
                 splitResult[ 8 ] += "<td class=\"number\">"
                         + Utilities.formatArea(site.getDeviation(), 0, 2,
                                 "&nbsp;")
                                 + "</td>";
 
                 if (i == factList.size() - 1) {
                     result = "<tr class=\"zebraodd\">" + splitResult[ 1 ]
                             + "</tr>" + "<tr class=\"zebraeven\">"
                             + splitResult[ 2 ] + "</tr>"
                             + "<tr class=\"zebraodd\">" + splitResult[ 3 ]
                                     + "</tr>" + "<tr class=\"zebraeven\">"
                                     + splitResult[ 4 ] + "</tr>"
                                     + "<tr class=\"zebraodd\">" + splitResult[ 5 ]
                                             + "</tr>" + "<tr class=\"zebraeven\">"
                                             + splitResult[ 6 ] + "</tr>"
                                             + "<tr class=\"zebraodd\">" + splitResult[ 7 ]
                                                     + "</tr>" + "<tr class=\"zebraeven\">"
                                                     + splitResult[ 8 ] + "</tr>";
                     isGood = true;
                 } else {
                     result = "<tr>" + splitResult[ 1 ] + "<tr>"
                             + splitResult[ 2 ] + "<tr>" + splitResult[ 3 ]
                                     + "<tr>" + splitResult[ 4 ] + "<tr>"
                                     + splitResult[ 5 ] + "<tr>" + splitResult[ 6 ]
                                             + "<tr>" + splitResult[ 7 ] + "<tr>"
                                             + splitResult[ 8 ];
                 }
             }
         }
         if (!isGood) { // result = "";
         }
         return result;
     }
 
     public static String getCountryListString() {
         String result = "";
         List allCountries = CountryUtil.findAllCountries();
 
         for (int j = 0; j < allCountries.size(); j++) {
             Chm62edtCountryPersist country = (Chm62edtCountryPersist) allCountries.get(
                     j);
 
             if (result.trim().length() > 0) {
                 result += "|";
             }
             result += country.getAreaNameEnglish();
         }
         return result;
     }
 
     public static String getRegionListString() {
         String result = "";
         List allRegions = CountryUtil.findAllRegions();
 
         for (int j = 0; j < allRegions.size(); j++) {
             Chm62edtBiogeoregionPersist region = (Chm62edtBiogeoregionPersist) allRegions.get(
                     j);
 
             if (result.trim().length() > 0) {
                 result += "|";
             }
             result += region.getBiogeoregionName();
         }
         return result;
     }
 
     public static Date getTimer() {
         return timer;
     }
 
     public static void setTimer(Date timer) {
         Utilities.timer = timer;
     }
 
     public static String getTaxonomicNameForASpecies(String idSpecies, String what, String SQL_DRV, String SQL_URL, String SQL_USR, String SQL_PWD) {
         if (idSpecies == null || idSpecies.trim().length() <= 0 || what == null
                 || what.trim().length() <= 0) {
             return "";
         }
         String result = "";
 
         SQLUtilities sqlc = new SQLUtilities();
 
         sqlc.Init(SQL_DRV, SQL_URL, SQL_USR, SQL_PWD);
 
         List rez = sqlc.ExecuteSQLReturnList(
                 "SELECT LEVEL,NAME,TAXONOMY_TREE FROM CHM62EDT_SPECIES AS A INNER JOIN CHM62EDT_TAXONOMY AS B ON A.ID_TAXONOMY = B.ID_TAXONOMY WHERE A.ID_SPECIES = "
                         + idSpecies,
                         3);
 
         if (rez != null && rez.size() > 0) {
             TableColumns columns = (TableColumns) rez.get(0);
 
             if (columns != null && columns.getColumnsValues() != null
                     && columns.getColumnsValues().size() == 3) {
                 result = getTaxonomicName(
                         (String) columns.getColumnsValues().get(0),
                         (String) columns.getColumnsValues().get(1),
                         (String) columns.getColumnsValues().get(2), what);
             }
         }
         return result;
     }
 
     public static String getTaxonomicName(String taxonomyLevel, String taxonomyName, String taxonomyTree, String what) {
         String level = taxonomyLevel;
 
         if (level != null && level.equalsIgnoreCase(what)) {
             return taxonomyName;
         } else {
             String result = "";
             String str = taxonomyTree;
 
             StringTokenizer st = new StringTokenizer(str, ",");
 
             while (st.hasMoreTokens()) {
                 StringTokenizer sts = new StringTokenizer(st.nextToken(), "*");
                 // String classificationId = sts.nextToken();
                 String classificationLevel = sts.nextToken();
                 String classificationName = sts.nextToken();
 
                 if (classificationLevel != null
                         && classificationLevel.equalsIgnoreCase(what)) {
                     result = classificationName;
                     break;
                 }
             }
 
             return result;
         }
     }
 
     public static String getIntroImage(ServletContext application) {
         String ret = "1.jpg";
 
         try {
             String appHome = application.getInitParameter(Constants.APP_HOME_INIT_PARAM);
             File dir = new File(appHome, "/images/intros");
             if (dir.exists() && dir.isDirectory()) {
                 java.io.File[] files = dir.listFiles();
 
                 if (files.length > 0) {
                     Random rnd = new Random(new Date().getTime());
                     int iRnd;
 
                     iRnd = rnd.nextInt(files.length);
                     if (iRnd == 0) {
                         iRnd = 1;
                     }
                     ret = files[ iRnd ].getName();
                 }
             } else {
                 throw new Exception("Could not find intro images directory: " + dir);
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return ret;
     }
 
     public static boolean isCountry(String countryName) {
         try {
             if (countryName != null && countryName.trim().length() > 0) {
                 List countries = new Chm62edtCountryDomain().findWhere(
                         "ISO_2L<>'' AND ISO_2L<>'null' AND ISO_2L IS NOT NULL AND SELECTION <> 0 and AREA_NAME_EN ='"
                                 + countryName + "'");
 
                 if (countries != null && countries.size() > 0) {
                     return true;
                 } else {
                     return false;
                 }
             } else {
                 return false;
             }
         } catch (Exception ex) {
             return false;
         }
     }
 
     public static String readContentFromURL(String strURL) {
         String ret = "";
 
         if (strURL != null) {
             BufferedReader in = null;
 
             try {
                 URL url = new URL(strURL);
 
                 in = new BufferedReader(
                         new InputStreamReader(url.openStream(), "UTF-8"));
                 String line;
 
                 while (((line = in.readLine()) != null)) {
                     ret += line + "\n";
                 }
             } catch (UnsupportedEncodingException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 if (in != null) {
                     try {
                         in.close();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
         return ret;
     }
 
     // Utility methods for habitats-eunis-browser, species-taxonomic-browser.jsp, habitats-annex1-browser.jsp START
 
     public static String removeFromExpanded(String expand, String idCurrent) {
         StringBuffer ret = new StringBuffer();
         String[] st = expand.split(",");
 
         for (int i = 0; i < st.length; i++) {
             String id = st[i];
 
             if (!id.startsWith(idCurrent)) {
                 ret.append(id);
                 ret.append(",");
             }
         }
         if (ret.length() > 0) {
             int index = ret.lastIndexOf(",");
 
             ret.deleteCharAt(index);
         }
         return ret.toString();
     }
 
     public static String removeSpecieFromExpanded(String expand, String idCurrent) {
         StringBuffer ret = new StringBuffer();
         String[] st = expand.split(",");
 
         for (int i = 0; i < st.length; i++) {
             String id = st[i];
 
             if (!id.equals(idCurrent)) {
                 ret.append(id);
                 ret.append(",");
             }
         }
         if (ret.length() > 0) {
             int index = ret.lastIndexOf(",");
 
             ret.deleteCharAt(index);
         }
         return ret.toString();
     }
 
     public static String addToExpanded(String expand, String idCurrent) {
         StringBuffer ret = new StringBuffer(expand);
 
         if (expand.length() > 0) {
             ret.append(",");
         }
         ret.append(idCurrent);
         return ret.toString();
     }
 
     public static boolean expandContains(String expand, String idCurrent) {
         boolean ret = false;
         String[] st = expand.split(",");
 
         for (int i = 0; i < st.length; i++) {
             String id = st[i];
 
             if (id.equals(idCurrent)) {
                 ret = true;
             }
         }
         return ret;
     }
 
     // Method that generates tree for species-taxonomic-browser.jsp
     public static String generateSpeciesTaxonomicTree(String id, String expand, boolean isRoot, Connection con, SQLUtilities sqlc, WebContentManagement cm) {
 
         String ret = "";
         String strSQL = "";
         String newLine = "\n";
 
         PreparedStatement ps = null;
         ResultSet rs = null;
 
         try {
 
             // we display root nodes
             strSQL = "SELECT CONCAT(NAME,' ','(',LEVEL,')') AS TITLE, ID_TAXONOMY AS ID";
             strSQL = strSQL + " FROM CHM62EDT_TAXONOMY";
             if (isRoot) {
                 strSQL = strSQL
                         + " WHERE ID_TAXONOMY=ID_TAXONOMY_PARENT";
             } else {
                 strSQL = strSQL + " WHERE ID_TAXONOMY_PARENT=" + id
                         + " AND ID_TAXONOMY != " + id;
             }
 
             ps = con.prepareStatement(strSQL);
             rs = ps.executeQuery();
             String hide = cm.cmsPhrase("Hide sublevels");
             String show = cm.cmsPhrase("Show sublevels");
 
             List<TaxonomyDTO> list = new ArrayList<TaxonomyDTO>();
 
             while (rs.next()) {
                 TaxonomyDTO dto = new TaxonomyDTO(rs.getString("TITLE"),
                         rs.getInt("ID"));
 
                 list.add(dto);
             }
             Collections.sort(list);
 
             ret += "<ul class=\"eunistree\">" + newLine;
             for (TaxonomyDTO tax : list) {
 
                 String taxTitle = tax.getTitle();
                 String taxId = new Integer(tax.getId()).toString();
 
                 ret += "<li>" + newLine;
                 boolean hasChilds = sqlc.SpeciesHasChildTaxonomies(taxId);
                 boolean hasChildSpecies = false;
 
                 if (!hasChilds) {
                     hasChildSpecies = sqlc.SpeciesHasChildSpecies(taxId);
                 }
                 if (hasChilds || hasChildSpecies) {
                     if (expandContains(expand, taxId)) {
                         ret += "<a title=\"" + hide + "\" id=\"level_" + taxId
                                 + "\" href=\"species-taxonomic-browser.jsp?expand="
                                 + removeSpecieFromExpanded(expand, taxId)
                                 + "#level_" + taxId
                                 + "\"><img src=\"images/img_minus.gif\" alt=\""
                                 + hide + "\"/></a>" + newLine;
                     } else {
                         ret += "<a title=\"" + show + "\" id=\"level_" + taxId
                                 + "\" href=\"species-taxonomic-browser.jsp?expand="
                                 + addToExpanded(expand, taxId) + "#level_"
                                 + taxId
                                 + "\"><img src=\"images/img_plus.gif\" alt=\""
                                 + show + "\"/></a>" + newLine;
                     }
                     ret += "&nbsp;" + taxTitle + newLine;
                 } else {
                     ret += "<img src=\"images/img_bullet.gif\" alt=\""
                             + taxTitle + "\"/>&nbsp;&nbsp;" + taxTitle + newLine;
                 }
                 if (expand.length() > 0 && expandContains(expand, taxId)) {
 
                     ret += generateSpeciesTaxonomicTree(taxId, expand, false, con, sqlc, cm);
 
                     ArrayList SpeciesList = sqlc.SQL2Array(
                             "SELECT CONCAT('<a href=\"species/',ID_SPECIES,'\">',SCIENTIFIC_NAME,'</a>') FROM chm62edt_species"
                                     + " WHERE ID_TAXONOMY=" + taxId + " ORDER BY SCIENTIFIC_NAME");
 
                     if (SpeciesList.size() > 0) {
                         ret += "<ul class=\"eunistree\">" + newLine;
                         for (int i = 0; i < SpeciesList.size(); i++) {
                             ret += "<li>" + newLine;
                             ret += "<img src=\"images/img_bullet.gif\">&nbsp;&nbsp;"
                                     + SpeciesList.get(i) + newLine;
                             ret += "</li>" + newLine;
                         }
                         ret += "</ul>" + newLine;
                     }
                 }
                 ret += "</li>" + newLine;
             }
 
             ret += "</ul>" + newLine;
 
             rs.close();
             ps.close();
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return ret;
     }
 
     // Utility methods for habitats-eunis-browser, species-taxonomic-browser.jsp, habitats-annex1-browser.jsp END
 }
