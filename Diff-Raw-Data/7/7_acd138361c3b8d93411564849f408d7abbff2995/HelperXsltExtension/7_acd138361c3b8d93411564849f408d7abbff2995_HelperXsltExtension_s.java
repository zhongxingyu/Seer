 package uk.ac.ebi.ae15;
 
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Element;
 import org.apache.commons.logging.Log;
 import org.apache.xalan.extensions.XSLProcessorContext;
 import org.apache.xalan.templates.ElemExtensionCall;
 
 import javax.xml.transform.TransformerException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public abstract class HelperXsltExtension {
 
     private static final Log log = org.apache.commons.logging.LogFactory.getLog(HelperXsltExtension.class);
 
     public static String concatAll( NodeList nl )
     {
 		StringBuilder buf = new StringBuilder();
 
         try {
             for (int i = 0; i < nl.getLength(); i++) {
                 Node elt = nl.item(i);
 
                 if ( null != elt.getNodeValue() )
                     buf.append( elt.getNodeValue() ).append(' ');
 
                 if ( elt.hasAttributes() ) {
                     NamedNodeMap attrs = elt.getAttributes();
                     for ( int j = 0; j < attrs.getLength(); j++) {
                         buf.append( attrs.item(j).getNodeValue() ).append(' ');
                     }
                 }
 
                 if ( elt.hasChildNodes() )
                     buf.append( concatAll( elt.getChildNodes() )).append(' ');
             }
         } catch ( Throwable t ) {
             log.debug("Caught an exception:", t);
         }
 
         return buf.toString();
 	}
 
     public static String toUpperCase( String str )
     {
         return str.toUpperCase();
     }
 
     public static String toLowerCase( String str )
     {
         return str.toLowerCase();
     }
 
     public static boolean isFileAvailableForDownload( String fileName )
     {
         return true;
     }
 
     public static String getFileDownloadUrl( String fileName )
     {
         return "http://www.ebi.ac.uk/microarray-as/ae/download/" + fileName;
     }
 
     public static boolean testRegexp( NodeList nl, String pattern, String flags )
     {
         return testRegexp( concatAll(nl), pattern, flags );
     }
 
     public static boolean testRegexp( String input, String pattern, String flags )
     {
         boolean result = false;
         try {
             int patternFlags = ( flags.indexOf("i") >= 0 ? Pattern.CASE_INSENSITIVE : 0 );
 
             String inputStr = ( input == null ? "" : input );
             String patternStr = ( pattern == null ? "" : pattern );
 
             Pattern p = Pattern.compile(patternStr, patternFlags);
             Matcher matcher = p.matcher(inputStr);
             result = matcher.find();
         } catch ( Throwable t ) {
             log.debug("Caught an exception:", t);
         }
 
         return result;
     }
 
     public static String replaceRegexp( String input, String pattern, String flags, String replace )
 	{
         int patternFlags = ( flags.indexOf("i") >= 0 ? Pattern.CASE_INSENSITIVE : 0 );
 
 		String inputStr = ( input == null ? "" : input );
  		String patternStr = ( pattern == null ? "" : pattern );
  		String replaceStr = ( replace == null ? "" : replace );
 
 		Pattern p = Pattern.compile(patternStr, patternFlags);
 		Matcher matcher = p.matcher(inputStr);
 		return ( flags.indexOf("g") >= 0 ? matcher.replaceAll(replaceStr) : matcher.replaceFirst(replaceStr) );
 	}
 
     private static String keywordToPattern( String keyword, boolean wholeWord ) {
         return ( wholeWord ? "\\b\\Q" + keyword + "\\E\\b" : "\\Q" + keyword + "\\E" );
     }
     public static boolean testSpecies( NodeList nl, String species )
     {
         return testSpecies( concatAll(nl), species );
     }
 
     public static boolean testSpecies( String input, String species )
     {
         return testRegexp( input, keywordToPattern( species, true ), "i" );
     }
 
     public static boolean testKeywords( NodeList nl, String keywords, boolean wholeWords )
     {
         return testKeywords( concatAll(nl), keywords, wholeWords );
     }
 
     public static boolean testKeywords( String input, String keywords, boolean wholeWords )
     {
         if ( null != keywords && 0 < keywords.length() ) {
             String[] kwdArray = keywords.split("\\s");
             for ( String keyword : kwdArray ) {
                 if ( !testRegexp( input, keywordToPattern( keyword, wholeWords ), "i" ) )
                     return false;
             }
             return true;
         }
 
         return false;
     }
 
     public static String markKeywords( String input, String keywords, boolean wholeWords )
     {
         String result = input;
         if ( null != keywords && 0 < keywords.length() ) {
             String[] kwdArray = keywords.split("\\s");
             for ( String keyword : kwdArray ) {
                 result = replaceRegexp( result, "(" +  keywordToPattern( keyword, wholeWords ) + ")", "ig", "|*$1*|" );
             }
         }
         return result;
     }
 
     public static void logInfo( XSLProcessorContext c, ElemExtensionCall extElt )
     {
         try {
             log.info(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
         } catch (TransformerException e) {
             log.debug( "Caught an exception:", e );
         }
     }
 
     public static void logDebug( XSLProcessorContext c, ElemExtensionCall extElt )
     {
         try {
             log.debug(extElt.getAttribute("select", c.getContextNode(), c.getTransformer()));
         } catch (TransformerException e) {
             log.debug( "Caught an exception:", e );
         }
     }
 }
