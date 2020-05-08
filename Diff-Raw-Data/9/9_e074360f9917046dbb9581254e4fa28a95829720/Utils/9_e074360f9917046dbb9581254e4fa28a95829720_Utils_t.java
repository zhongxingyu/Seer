 package cz.cuni.mff.odcleanstore.shared;
 
 import com.hp.hpl.jena.graph.Node;
 
 import java.text.Normalizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 
 /**
  * Various utility methods.
  *
  * @author Jan Michelfeit
  */
 public final class Utils {
     /* Simplified patterns for IRIs and prefixed names  based on
      * specification at http://www.w3.org/TR/rdf-sparql-query/#QSynIRI
      */
     private static final String PN_CHARS_BASE = "A-Za-z\\xC0-\\xFF";
     private static final String PN_CHARS_U = PN_CHARS_BASE + "_";
     private static final String PN_CHARS = PN_CHARS_U + "\\-0-9\\xB7";
     private static final String PN_PREFIX_PATTERN =
             "[" + PN_CHARS_BASE + "](?:[" + PN_CHARS + ".]*[" + PN_CHARS + "])?";
     private static final String PN_LOCAL_PATTERN =
             "[" + PN_CHARS_U + "0-9](?:[" + PN_CHARS + ".]*[" + PN_CHARS + "])?";
 
     private static final Pattern IRI_PATTERN = Pattern.compile("^[^<>\"{}|^`\\x00-\\x20']*$");
     private static final Pattern PREFIXED_NAME_PATTERN =
             Pattern.compile("^(" + PN_PREFIX_PATTERN + ")?:(" + PN_LOCAL_PATTERN + ")?$");
     
     private static final Pattern UUID_PATTERN = 
             Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
 
     /** Milliseconds in a second. */
     public static final long MILLISECONDS = 1000;
     
     /** Time unit 60. */
     public static final long TIME_UNIT_60 = 60;
     
     /** Number of hours in a day. */
     public static final long DAY_HOURS = 24;
     
     /** Jdbc driver class. */
     public static final String JDBC_DRIVER = "virtuoso.jdbc3.Driver";
     
     /** Default encoding - UTF-8. */
     public static final String DEFAULT_ENCODING = "UTF-8";
     
     /** Pattern matching characters to be removed from a literal when being escaped for a SPARQL query. */ 
     public static final Pattern ESCAPE_LITERAL_CHARS_TO_REMOVE = Pattern.compile("[\\x00-\\x09\\x0E-\\x1F]");
     
     /** Pattern matching characters to be escaped in a literal when being escaped for a SPARQL query. */
     public static final Pattern ESCAPE_LITERAL_CHARS_TO_ESCAPE = Pattern.compile("([\"'`\\\\])");
 
     /**
      * Compare two values which may be null. Null is considered less than all non-null values.
      * @param o1 first compared value or null
      * @param o2 second compared value or null
      * @param <T> type of compared values
      * @return a negative integer, zero, or a positive integer as o1 is less than, equal to, or greater than o2
      */
     public static <T> int nullProofCompare(Comparable<T> o1, T o2) {
         if (o1 != null && o2 != null) {
             return o1.compareTo(o2);
         } else if (o1 != null) {
             return 1;
         } else if (o2 != null) {
             return -1;
         } else {
             assert o1 == null && o2 == null;
             return 0;
         }
     }
     
     /**
      * Compare two values which may be null for equality.
      * @param o1 first compared value or null
      * @param o2 second compared value or null
      * @return return true iff both values are null or if they satisfy equals()
      */
     public static boolean nullProofEquals(Object o1, Object o2) {
         if (o1 == null) {
             return o1 == o2;
         } else {
             return o1.equals(o2);
         }
     }
 
     /**
      * Checks whether the given URI is a valid IRI.
      * See http://www.w3.org/TR/rdf-sparql-query/#QSynIRI
      * @param uri the string to check
      * @return true iff the given string is a valid IRI
      */
     public static boolean isValidIRI(String uri) {
         return !uri.isEmpty() && IRI_PATTERN.matcher(uri).matches();
     }
 
     /**
      * Checks whether the given URI is a prefixed name.
      * See http://www.w3.org/TR/rdf-sparql-query/#QSynIRI.
      * @param uri the string to check
      * @return true iff the given string is a valid IRI
      */
     public static boolean isPrefixedName(String uri) {
         return !uri.isEmpty() && PREFIXED_NAME_PATTERN.matcher(uri).matches();
     }
     
     /**
      * Checks if a string is null or an empty string.
      * @param s tested string
      * @return true iff s is null or an empty string
      */
     public static boolean isNullOrEmpty(String s) {
         return s == null || s.length() == 0;
     }
     
     /**
      * Extracts the UUID part from a data named graph URI.
      * @param namedGraphURI URI of a payload/metadata/provenanceMetadata named graph
      * @return the UUID part or null if it the named graph doesn't have the correct format
      */
     public static String extractUUID(String namedGraphURI) {
         if (Utils.isNullOrEmpty(namedGraphURI)) {
             return null;
         }
         Matcher matcher = UUID_PATTERN.matcher(namedGraphURI);
         return matcher.find() ? matcher.group() : null;
     }
     
     /**
      * Convert the given string to ASCII characters, removing diacritical marks.
      * @param str string to convert
      * @return string containing only ASCII characters
      */
     public static String toAscii(CharSequence str) {
         final int asciiSize = 128;
         String decomposed = Normalizer.normalize(str, Normalizer.Form.NFKD);
         /* Build a new String with only ASCII characters. */
         StringBuilder buf = new StringBuilder(str.length());
         for (int idx = 0; idx < decomposed.length(); ++idx) {
             char ch = decomposed.charAt(idx);
             if (ch < asciiSize) {
                 buf.append(ch);
             }
         }
         return buf.toString();
     }
     
     /**
      * Escapes a literal for use in a SPARQL query.
      * @param literalValue value to be escaped
      * @return escaped value
      */
     public static String escapeSPARQLLiteral(String literalValue) {
         if (literalValue == null) {
             return "";
         }
         
         String escapedValue = literalValue;
         escapedValue = ESCAPE_LITERAL_CHARS_TO_REMOVE.matcher(escapedValue).replaceAll("");
         escapedValue = ESCAPE_LITERAL_CHARS_TO_ESCAPE.matcher(escapedValue).replaceAll("\\\\$1");
         
         return escapedValue;
     }
     
     /**
      * Return the URI identifying a blank node in Virtuoso.
      * @param bNode blank node
      * @return URI identifying bNode in Virtuoso
      * @throws UnsupportedOperationException bNode is not a blank node
      */
     public static String getVirtuosoURIForBlankNode(Node bNode) {
         return "nodeID://" + bNode.getBlankNodeLabel();
     }
    
    /**
     * Converts an object or null refrence to a string (null is converted to the empty string).
     * @param obj object to stringify
     * @return string representation of obj
     */
    public static String toStringNullProof(Object obj) {
        return obj == null ? "" : obj.toString();
    }
         
     /** Disable constructor for a utility class. */
     private Utils() {
     }
 }
