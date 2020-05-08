 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.requests;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.LinearRing;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Polygon;
 import com.vividsolutions.jts.geom.PrecisionModel;
 import org.geotools.filter.FilterFactory;
 import org.geotools.filter.AbstractFilter;
 import org.geotools.filter.Filter;
 import org.geotools.filter.FidFilter;
 import org.geotools.filter.FidFilterImpl;
 import org.geotools.filter.GeometryFilter;
 import org.geotools.filter.AttributeExpression;
 import org.geotools.filter.LiteralExpression;
 import org.geotools.filter.IllegalFilterException;
 import org.vfny.geoserver.responses.WfsException;
 
 /**
  * Base class for all KVP readers, with some generalized convenience methods.
  * 
  * <p>If you pass this utility a KVP request (everything after the '?' in the 
  * GET request URI), it will translate this into a list of key-word value 
  * pairs.These pairs represent every element in the KVP GET request, legal or 
  * otherwise.  This class may then be subclassed and  used by request-specific
  * classes.  Because there is no error checking for the KVPs in this class, 
  * subclasses must  check for validity of their KVPs before passing the their 
  * requests along, but - in return - this parent class is quite flexible.  For
  * example, native KVPs may be easily parsed in its subclasses, since they
  * are simply read and stored (without analysis) in the constructer in this
  * class.  Note that all keys are translated to upper case to avoid case 
  * conflicts.</p>
  * 
  * @author Rob Hranac, TOPP
  * @version $version$
  */
 abstract public class RequestKvpReader {
 
     /** Class logger */
     private static Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.requests");
 
     /** Delimeter for KVPs in the raw string */
     protected static final String KEYWORD_DELIMITER = "&";
     
     /** Delimeter that seperates keywords from values */
     protected static final String VALUE_DELIMITER = "=";
        
     /** Delimeter for outer value lists in the KVPs */
     protected static final String OUTER_DELIMETER = "()";
 
     /** Delimeter for inner value lists in the KVPs */
     protected static final String INNER_DELIMETER = ",";
 
     /** KVP pair listing; stores all data from the KVP request */
     protected Map kvpPairs = new HashMap();
     
     /** Holds mappings between HTTP and ASCII encodings */
     private static Map translator = new HashMap();
 
     /** Holds mappings between HTTP and ASCII encodings */
     private static FilterFactory factory = FilterFactory.createFilterFactory();
 
     // sets up some of the HTML encoding translations
     static {
         translator.put("%3C","<");
         translator.put("%3E",">");
         translator.put("%22","'");
         translator.put("%20"," ");
         translator.put("%27","'");
     }
 
     /**
      * Constructor with raw request string.  This constructor  parses the
      * entire request string into a KVP Map for quick access by
      * sub-classes.
      * @param rawRequest The raw request string from the client.
      */
     public RequestKvpReader (String rawRequest) {
 
         // uses the request cleaner to remove HTTP junk
         String cleanRequest = clean(rawRequest);
         
         // parses initial request sream into KVPs
         StringTokenizer requestKeywords = 
             new StringTokenizer(cleanRequest.trim(), KEYWORD_DELIMITER);
         
         // parses KVPs into values and keywords and puts them in a HashTable
         while(requestKeywords.hasMoreTokens()) {
             String kvpPair = requestKeywords.nextToken();
             String key;
             String value;
 
             // a bit of a horrible hack for filters, which handles problems of
             //  delimeters, which may appear in XML (such as '=' for 
             //  attributes.  unavoidable and illustrates the problems with
             //  mixing nasty KVP Get syntax and pure XML syntax!
             if(kvpPair.startsWith("FILTER")) {
                 kvpPairs.put( "FILTER", kvpPair.substring(7));
 
             } else {
                 // handles all other standard cases by looking for the correct
                 //  delimeter and then sticking the KVPs into the hash table 
                 StringTokenizer requestValues = 
                     new StringTokenizer(kvpPair, VALUE_DELIMITER);
                 // make sure that there is a key token
                 if( requestValues.hasMoreTokens()) {
                     // assign key as uppercase to eliminate case conflict
                     key = requestValues.nextToken().toUpperCase();
                     // make sure that there is a value token
                     if( requestValues.hasMoreTokens() ) {
                         // assign value and store in hash with key
                         value = requestValues.nextToken();
                         kvpPairs.put(key, value);
                     }
                 }
             }            
         }
     }
 
 
     /*************************************************************************
      * STATIC GENERALIZED METHODS FOR CHILDREN                               *
      *************************************************************************
      * XML GetFeature parsing tests.  Each test reads from a specific XML    *
      * file and compares it to the base request defined in the test itself.  *
      * Tests are run via the static methods in this suite.  The tests        *
      * themselves are quite generic, so documentation is minimal.            *
      *************************************************************************/
     /**
      * Cleans an HTTP string and returns pure ASCII as a string.
      * @param dirtyRequest The HTTP-encoded string.
      */ 
     public static String clean(String raw) {        
         LOGGER.finest("raw request: " + raw);
         Set keys = translator.keySet();
         Iterator i = keys.iterator();
         while(i.hasNext()) {
             String encoding = (String) i.next();
	    if (raw != null) {
             raw = raw.replaceAll(encoding, (String) translator.get(encoding));
	    } else {
		return "";
	    }
         }
         LOGGER.finest("cleaned request: " + raw);
         return raw;
     }
 
     /**
      * Reads a tokenized string and turns it into a list.  In this method, the
      * tokenizer is quite flexible.  Note that if the list is unspecified (ie.
      * is null) or is unconstrained (ie. is '*'), then the method returns an
      * empty list.
      *
      * @param rawList The tokenized string.
      * @param delimeter The delimeter for the string tokens.
      */ 
     protected static List readFlat(String rawList, String delimeter) {
         // handles implicit unconstrained case
         if(rawList == null) {
             return new ArrayList(0);
         // handles explicit unconstrained case
         } else if(rawList.equals("*")) {
             return new ArrayList(0);
         // handles explicit, constrained element lists
         } else {
             StringTokenizer kvps = new StringTokenizer(rawList, delimeter);
             List kvpList = new ArrayList(kvps.countTokens());
             while(kvps.hasMoreTokens()) {
                 LOGGER.finest("adding simple element");
                 kvpList.add(kvps.nextToken());
             }
             return kvpList;
         }
     }
 
     /**
      * Reads a nested tokenized string and turns it into a list.  This method
      * is much more specific to the KVP get request syntax than the more 
      * general readFlat method.  In this case, the outer tokenizer '()' and
      * inner tokenizer ',' are both from the specification.  Returns a list of
      * lists.
      * @param rawList The tokenized string.
      * @return A list of lists, containing outer and inner elements.
      * @throws WfsException When the string structure cannot be read.
      */ 
     protected static List readNested(String rawList)
         throws WfsException {
 
         LOGGER.finest("reading nested: " + rawList);
         List kvpList = new ArrayList();
 
         // handles implicit unconstrained case
         if(rawList == null) {
             LOGGER.finest("found implicit all requested");
             return kvpList;
 
         // handles explicit unconstrained case
         } else if(rawList.equals("*")) {
             LOGGER.finest("found explicit all requested");
             return kvpList;
 
         // handles explicit, constrained element lists
         } else {
             LOGGER.finest("found explicit requested");
 
             // handles multiple elements list case
             if( rawList.startsWith("(")) {
                 LOGGER.finest("reading complex list");
                 List outerList = readFlat(rawList, OUTER_DELIMETER);
                 Iterator i = outerList.listIterator();
                 while(i.hasNext()) {
                     kvpList.add(readFlat((String) i.next(), INNER_DELIMETER));
                 }
 
             // handles single element list case
             } else {
                 LOGGER.finest("reading simple list");
                 kvpList.add(readFlat(rawList, INNER_DELIMETER));
             }
             return kvpList;
         }
     }
 
     /**
      * Reads in three strings, representing some sort of feature constraints,
      * and translates them into filters.  If no filters exist, it returns an
      * empty list.
      * @param fid A group of feature IDs, as a String.
      * @param filter A group of filters, as a String.
      * @param bbox A group of boxes, as a String.
      * @return A list filters.
      * @throws WfsException When the string structure cannot be read.
      */ 
     protected static List readFilters(String fid, String filter, String bbox)
         throws WfsException {
 
         List unparsed = new ArrayList();
         List filters = new ArrayList();
         ListIterator i;
 
         // handles feature id(es) case
         if( (fid != null) &&
             (filter == null) &&
             (bbox == null)) {            
             LOGGER.finest("reading fid filter: " + fid);
             unparsed = readNested(fid);
             i = unparsed.listIterator();
             while(i.hasNext()) {
                 List ids = (List) i.next();
                 ListIterator innerIterator = ids.listIterator();
                 FidFilter fidFilter = factory.createFidFilter();
                 while(innerIterator.hasNext()) {
                     fidFilter.addFid((String) innerIterator.next());
                 }
                 filters.add(fidFilter);
                 LOGGER.finest("added fid filter: " + fidFilter);
             }
             return filters;
 
         // handles filter(s) case
         } else if( (filter != null) &&
                    (fid == null) &&
                    (bbox == null)) {            
             LOGGER.finest("reading filter: " + filter);
             unparsed = readFlat(filter, OUTER_DELIMETER);
             i = unparsed.listIterator();
             while(i.hasNext()) {
                 Reader filterReader = new StringReader((String) i.next());
                 filters.add(XmlRequestReader.readFilter(filterReader));
             }
             return filters;
             
         // handles bounding box(s) case
         } else if( (bbox != null) &&
                    (fid == null) &&
                    (filter == null)) {
             LOGGER.finest("bbox filter: " + bbox);
             double[] rawCoords = new double[4]; 
             unparsed = readFlat(bbox, INNER_DELIMETER);
             i = unparsed.listIterator();
 
             // check to make sure that the bounding box has 4 coordinates
             if(unparsed.size() != 4) {
                 throw new WfsException("Requested bounding box contains wrong"
                                        + "number of coordinates (should have "
                                        + "4): " + unparsed.size());
 
             // if it does, store them in an array of doubles
             } else {
                 int j = 0;
                 while(i.hasNext()) {
                     try {
                         rawCoords[j] = Double.parseDouble((String) i.next());
                         j++;
                     } catch(NumberFormatException e) {
                         throw new WfsException("Bounding box coordinate " + j +
                                                " is not parsable:" + 
                                                unparsed.get(j));
                     }
                 }
             }
 
             // turn the array of doubles into an appropriate geometry filter
             // TODO 2:
             //  hack alert: because we do not yet know the schema, we have
             //  used the '@' symbol for the attribute expression, to be
             //  replaced later by the appropriate attribute.  I would argue
             //  that this is a failure in the specification because there
             //  should always be explicit designation of geometry attibutes
             //  within the filter.  The BBOX element is ambiguous, since
             //  features may contain multiple geometries.  For now, we will
             //  parse it and keep a record of a 'primary geometry' in the 
             //  server.
             try {
                 GeometryFilter finalFilter = factory.
                     createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
                 //leave as null and postgisDatSource will use default geom.
 		//AttributeExpression leftExpression = 
                 //    factory.createAttributeExpression(null);
 		//leftExpression.setAttributePath("@");
                 // Creates coordinates for the linear ring
                 Coordinate[] coords = new Coordinate[5];
                 coords[0] = new Coordinate(rawCoords[0], rawCoords[1]);
                 coords[1] = new Coordinate(rawCoords[0], rawCoords[3]);
                 coords[2] = new Coordinate(rawCoords[2], rawCoords[3]);
                 coords[3] = new Coordinate(rawCoords[2], rawCoords[1]);
                 coords[4] = new Coordinate(rawCoords[0], rawCoords[1]);
                 LinearRing outerShell = 
                     new LinearRing(coords, new PrecisionModel(),0);
                 Geometry polygon = 
                     new Polygon(outerShell, new PrecisionModel(), 0);
                 LiteralExpression rightExpression = 
                     factory.createLiteralExpression(polygon); 
                 //finalFilter.addLeftGeometry(leftExpression);
                 finalFilter.addRightGeometry(rightExpression);
                 filters.add(finalFilter);
                 return filters;
 
             } catch(IllegalFilterException e) {
                 throw new WfsException("Filter creation problem: " + 
                                        e.getMessage());
             }
 
             // handles unconstrained case
         } else if( (bbox == null) &&
                    (fid == null) &&
                    (filter == null)) {            
             return new ArrayList();
             
             // handles error when more than one filter specified
         } else {
             throw new WfsException("GetFeature KVP request contained " + 
                                    "conflicting filters.  Filter: " + filter +
                                    ", fid: " + fid + ", bbox:" + bbox);
         }   
     }    
 }
