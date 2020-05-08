 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IService;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.events.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.events.UpdateViewEventType;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkTypes;
 import org.amanzi.neo.core.enums.NodeTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.ActionUtil.RunnableWithResult;
 import org.amanzi.neo.core.utils.CSVParser;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.index.MultiPropertyIndex;
 import org.amanzi.neo.loader.NetworkLoader.CRS;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.preferences.CommonCRSPreferencePage;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.preference.IPreferenceNode;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.preference.PreferenceManager;
 import org.eclipse.jface.preference.PreferenceNode;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Traverser.Order;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 public abstract class AbstractLoader {
     private static final Logger LOGGER = Logger.getLogger(AbstractLoader.class);
     // private static final Logger LOGGER = Logger.getLogger(AbstractLoader.class);
 
     /** AbstractLoader DEFAULT_DIRRECTORY_LOADER field */
     public static final String DEFAULT_DIRRECTORY_LOADER = "DEFAULT_DIRRECTORY_LOADER";
 
     // protected HashMap<Integer, HeaderMaps> headersMap = new HashMap<Integer, HeaderMaps>();
     // protected HashMap<Integer, Pair<Long, Long>> timeStamp = new HashMap<Integer, Pair<Long,
     // Long>>();
     protected HashMap<Integer, StoringProperty> storingProperties = new HashMap<Integer, StoringProperty>();
     protected String typeName = "CSV";
     protected GraphDatabaseService neo;
     private NeoServiceProvider neoProvider;
     protected HashMap<String, GisProperties> gisNodes = new HashMap<String, GisProperties>();
     protected String filename = null;
     protected String basename = null;
     protected Display display;
     private String fieldSepRegex;
     protected String[] possibleFieldSepRegexes = new String[] {"\t", ",", ";"};
     protected int lineNumber = 0;
     private int limit = 0;
     private long savedData = 0;
     private long started = System.currentTimeMillis();
     protected boolean headerWasParced;
 
     // private ArrayList<MultiPropertyIndex<?>> indexes = new
     // ArrayList<MultiPropertyIndex<?>>();
     private final LinkedHashMap<String, ArrayList<MultiPropertyIndex< ? >>> indexes = new LinkedHashMap<String, ArrayList<MultiPropertyIndex< ? >>>();
     private final LinkedHashMap<String, LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>>> mappedIndexes = new LinkedHashMap<String, LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>>>();
 
     @SuppressWarnings("unchecked")
     public static final Class[] NUMERIC_PROPERTY_TYPES = new Class[] {Integer.class, Long.class, Float.class, Double.class};
     @SuppressWarnings("unchecked")
     public static final Class[] KNOWN_PROPERTY_TYPES = new Class[] {Integer.class, Long.class, Float.class, Double.class, String.class};
     private boolean indexesInitialized = false;
     private boolean taskSetted;
 
     protected CSVParser parser;
 
     public class Header {
         private static final int MAX_PROPERTY_VALUE_COUNT = 100; // discard
         // calculate spread after this number of data points
         int index;
         String key;
         String name;
         HashMap<Class< ? extends Object>, Integer> parseTypes = new HashMap<Class< ? extends Object>, Integer>();
         Double min = Double.POSITIVE_INFINITY;
         Double max = Double.NEGATIVE_INFINITY;
         HashMap<Object, Integer> values = new HashMap<Object, Integer>();
         boolean isIdentityHeader=false;
         int parseCount = 0;
         int countALL = 0;
 
         Header(String name, String key, int index) {
             this.index = index;
             this.name = name;
             this.key = key;
             for (Class< ? extends Object> klass : KNOWN_PROPERTY_TYPES) {
                 parseTypes.put(klass, 0);
             }
         }
 
         Header(Header old) {
             this(old.name, old.key, old.index);
             this.parseCount = old.parseCount;
             this.values = old.values;
             this.min = old.min;
             this.max = old.max;
             this.countALL = old.countALL;
             this.isIdentityHeader=old.isIdentityHeader;
         }
 
         protected boolean invalid(String field) {
             return field == null || field.length() < 1 || field.equals("?");
         }
 
         Object parse(String field) {
             if (invalid(field))
                 return null;
             parseCount++;
             try {
                 int value = Integer.parseInt(field);
                 incValue(value);
                 incType(Integer.class);
                 return value;
             } catch (Exception e) {
                 try {
                     float value = Float.parseFloat(field);
                     incValue(value);
                     incType(Float.class);
                     return value;
                 } catch (Exception e2) {
                     incValue(field);
                     incType(String.class);
                     return field;
                 }
             }
         }
 
         protected void incType(Class< ? extends Object> klass) {
             parseTypes.put(klass, parseTypes.get(klass) + 1);
         }
 
         protected Object incValue(Object value) {
             if (value != null) {
                 countALL++;
                 if (value instanceof Number) {
                     double doubleValue = ((Number)value).doubleValue();
                     min = Math.min(min, doubleValue);
                     max = Math.max(max, doubleValue);
                 }
             }
             if (values != null) {
                 Integer count = values.get(value);
                 if (count == null) {
                     count = 0;
                 }
                 boolean discard = false;
                 if (count == 0) {
                     // We have a new value, so adding it will increase the size
                     // of the map
                     // We should perform threshold tests to decide whether to
                     // drop the map or not
                     if (values.size() >= MAX_PROPERTY_VALUE_COUNT) {
                         // Exceeded absolute threashold, drop map
                         LOGGER.debug("Property values exceeded maximum count, no longer tracking value set: " + this.key);
                         discard = true;
                     }
                     // TODO if we do not use parse method this check will be
                     // wrong
                     // else if (values.size() >=
                     // MIN_PROPERTY_VALUE_SPREAD_COUNT) {
                     // // Exceeded minor threshold, test spread and then decide
                     // float spread = (float)values.size() / (float)parseCount;
                     // if (spread > MAX_PROPERTY_VALUE_SPREAD) {
                     // // Exceeded maximum spread, too much property variety,
                     // drop map
                     // LOGGER.debug("Property shows excessive variation, no longer tracking value set: "
                     // + this.key);
                     // discard = true;
                     // }
                     // }
                 }
                 //do not drop statistics for identity properties
                 if (discard && !isIdentityHeader) {
                     // Detected too much variety in property values, stop
                     // counting
                     dropStats();
                 } else {
                     values.put(value, count + 1);
                 }
             }
             return value;
         }
 
         boolean shouldConvert() {
             return parseCount > 10;
         }
         
        
         Class< ? extends Object> knownType() {
             Class< ? extends Object> best = String.class;
             int maxCount = 0;
             int countFound = 0;
             for (Class< ? extends Object> klass : parseTypes.keySet()) {
                 int count = parseTypes.get(klass);
                 // Bias towards Strings
                 if (klass == String.class)
                     count *= 2;
                 if (maxCount < parseTypes.get(klass)) {
                     maxCount = count;
                     best = klass;
                 }
                 if (count > 0) {
                     countFound++;
                 }
             }
             if (countFound > 1) {
                 AbstractLoader.this.notify("Header " + key + " had multiple type matches: ");
                 for (Class< ? extends Object> klass : parseTypes.keySet()) {
                     int count = parseTypes.get(klass);
                     if (count > 0) {
                         AbstractLoader.this.notify("\t" + count + ": " + klass + " => " + key);
                     }
                 }
             }
             return best;
         }
 
         /**
          * Disable statistics collection for this header. This is useful if the property is
          * undesirable in some later statistical analysis, either because it is too diverse, or it
          * is a property we can 'grouped by' during the load. Examples of excessive diversity would
          * be element names, ids, timestamps, locations. Examples of grouping by would be site
          * properties, timestamps and locations.
          */
         public void dropStats() {
             values = null;
         }
     }
 
     protected class IntegerHeader extends Header {
         IntegerHeader(Header old) {
             super(old);
         }
 
         @Override
         Integer parse(String field) {
             if (invalid(field))
                 return null;
             parseCount++;
             return (Integer)incValue(Integer.parseInt(field));
         }
 
         @Override
         boolean shouldConvert() {
             return false;
         }
 
         @Override
         Class<Integer> knownType() {
             return Integer.class;
         }
     }
 
     protected class LongHeader extends Header {
         LongHeader(Header old) {
             super(old);
         }
 
         @Override
         Long parse(String field) {
             if (invalid(field))
                 return null;
             parseCount++;
             return (Long)incValue(Long.parseLong(field));
         }
 
         @Override
         boolean shouldConvert() {
             return false;
         }
 
         @Override
         Class<Long> knownType() {
             return Long.class;
         }
     }
 
     protected class FloatHeader extends Header {
         FloatHeader(Header old) {
             super(old);
         }
 
         @Override
         Float parse(String field) {
             if (invalid(field))
                 return null;
             parseCount++;
             return (Float)incValue(Float.parseFloat(field));
         }
 
         @Override
         boolean shouldConvert() {
             return false;
         }
 
         @Override
         Class<Float> knownType() {
             return Float.class;
         }
     }
 
     protected class StringHeader extends Header {
         StringHeader(Header old) {
             super(old);
         }
 
         @Override
         String parse(String field) {
             if (invalid(field))
                 return null;
             parseCount++;
             return (String)incValue(field);
         }
 
         @Override
         boolean shouldConvert() {
             return false;
         }
 
         @Override
         Class<String> knownType() {
             return String.class;
         }
     }
 
     protected interface PropertyMapper {
         public Object mapValue(String originalValue);
     }
 
     protected class MappedHeaderRule {
         private final String name;
         protected String key;
         private final PropertyMapper mapper;
 
         MappedHeaderRule(String name, String key, PropertyMapper mapper) {
             this.key = key;
             this.name = name;
             this.mapper = mapper;
         }
     }
 
     /**
      * This class allows for either replacing of duplicating properties. See addMappedHeader for
      * details.
      * 
      * @author craig
      * @since 1.0.0
      */
     protected class MappedHeader extends Header {
         protected PropertyMapper mapper;
         Class< ? extends Object> knownClass = null;
 
         MappedHeader(Header old, MappedHeaderRule mapRule) {
             super(old);
             this.key = mapRule.key;
             if (mapRule.name != null) {
                 // We only replace the name if the new one is valid, otherwise
                 // inherit from the old
                 // header
                 // This allows for support of header replacing rules, as well as
                 // duplicating rules
                 this.name = mapRule.name;
             }
             this.mapper = mapRule.mapper;
             this.values = new HashMap<Object, Integer>(); // need to make a new
             // values list,
             // otherwise we share the same data as
             // the original
         }
 
         @Override
         Object parse(String field) {
             if (invalid(field))
                 return null;
             Object result = mapper.mapValue(field);
             parseCount++;
             if (knownClass == null && result != null) {
                 // Determine converted class from very first conversion
                 knownClass = result.getClass();
             }
             return incValue(result);
         }
 
         @Override
         boolean shouldConvert() {
             return false;
         }
 
         @Override
         Class< ? extends Object> knownType() {
             return knownClass;
         }
     }
 
     /**
      * Convenience implementation of a property mapper that understands date and time formats.
      * Construct with a date-time pattern understood by java.text.SimpleDateFormat. If you pass null
      * of an invalid format, then the default of "HH:mm:ss" will be used.
      * 
      * @author craig
      * @since 1.0.0
      */
     protected class DateTimeMapper extends DateMapper {
 
         /**
          * @param format
          */
         protected DateTimeMapper(String format) {
             super(format);
         }
 
         @Override
         public Object mapValue(String time) {
             Date datetime = (Date)super.mapValue(time);
             return datetime == null ? 0L : datetime.getTime();
         }
     }
 
     /**
      * Convenience implementation of a property mapper that understands date formats. Construct with
      * a date-time pattern understood by java.text.SimpleDateFormat. If you pass null of an invalid
      * format, then the default of "HH:mm:ss" will be used.
      */
     protected class DateMapper implements PropertyMapper {
         private SimpleDateFormat format;
 
         protected DateMapper(String format) {
             try {
                 this.format = new SimpleDateFormat(format);
             } catch (Exception e) {
                 this.format = new SimpleDateFormat("HH:mm:ss");
             }
         }
 
         @Override
         public Object mapValue(String time) {
             Date datetime;
             try {
                 datetime = format.parse(time);
             } catch (ParseException e) {
                 error(e.getLocalizedMessage());
                 return null;
             }
             return datetime;
         }
     }
 
     /**
      * Convenience implementation of a property mapper that assumes the object is a String. This is
      * useful for overriding the default behavior of detecting field formats, and simply keeping the
      * original strings. For example if the site name happens to contain only numbers, but we still
      * want to see it as a string because it is the name.
      * 
      * @author craig
      * @since 1.0.0
      */
     protected class StringMapper implements PropertyMapper {
 
         @Override
         public Object mapValue(String value) {
             return value;
         }
     }
 
     /**
      * Initialize Loader with a specified set of parameters
      * 
      * @param type defaults to 'CSV' if empty
      * @param neoService defaults to looking up from Neoclipse if null
      * @param fileName name of file to load
      * @param display Display to use for scheduling plugin lookups and message boxes, or null
      */
     protected void initialize(String typeString, GraphDatabaseService neoService, String filenameString, Display display) {
         if (typeString != null && !typeString.isEmpty()) {
             this.typeName = typeString;
         }
         initializeNeo(neoService, display);
         this.display = display;
         this.filename = filenameString;
         this.basename = (new File(filename)).getName();
     }
 
     protected void initializeNeo(GraphDatabaseService neoService, Display display) {
         if (neoService == null) {
             // if Display is given than start Neo using syncExec
             if (display != null) {
                 display.syncExec(new Runnable() {
                     public void run() {
                         initializeNeo();
                     }
                 });
             }
             // if Display is not given than initialize Neo as usual
             else {
                 initializeNeo();
             }
         } else {
             this.neo = neoService;
         }
     }
 
     private void initializeNeo() {
         if (this.neoProvider == null)
             this.neoProvider = NeoServiceProvider.getProvider();
         if (this.neo == null)
             this.neo = this.neoProvider.getService();
     }
 
     protected void determineFieldSepRegex(String line) {
         int maxMatch = 0;
         for (String regex : possibleFieldSepRegexes) {
             String[] fields = line.split(regex);
             if (fields.length > maxMatch) {
                 maxMatch = fields.length;
                 fieldSepRegex = regex;
             }
         }
         parser = new CSVParser(fieldSepRegex.charAt(0));
     }
 
     protected List<String> splitLine(String line) {
         return parser.parse(line);
     }
 
     /**
      * Converts to lower case and replaces all illegal characters with '_' and removes trailing '_'.
      * This is useful for creating a version of a header or property name that can be used as a
      * variable or method name in programming code, notably in Ruby DSL code.
      * 
      * @param original header String
      * @return edited String
      */
     protected final static String cleanHeader(String header) {
         return header.replaceAll("[\\s\\-\\[\\]\\(\\)\\/\\.\\\\\\:\\#]+", "_").replaceAll("[^\\w]+", "_").replaceAll("_+", "_").replaceAll("\\_$", "").toLowerCase();
     }
 
     /**
      * Add a property name and regular expression for a known header. This is used if we want the
      * property name in the database to be some specific text, not the header text in the file. The
      * regular expression is used to find the header in the file to associate with the new property
      * name. Note that the original property will not be saved using its original name. It will be
      * saved with the specified name provided. For example, if you want the first field found that
      * starts with 'lat' to be saved in a property called 'y', then you would call this using:
      * 
      * <pre>
      * addKnownHeader(&quot;y&quot;, &quot;lat.*&quot;);
      * </pre>
      * @param key the name to use for the property
      * @param regex a regular expression to use to find the property
      * @param isIdentityHeader true if the property is an identity property
      */
     protected void addKnownHeader(Integer headerId, String key, String regex, boolean isIdentityHeader) {
         addKnownHeader(headerId, key, new String[] {regex}, isIdentityHeader);
     }
 
     /**
      * Add a property name and list of regular expressions for a single known header. This is used
      * if we want the property name in the database to be some specific text, not the header text in
      * the file. The regular expressions are used to find the header in the file to associate with
      * the new property name. Note that the original property will not be saved using its original
      * name. It will be saved with the specified name provided. For example, if you want the first
      * field found that starts with either 'lat' or 'y_wert' to be saved in a property called 'y',
      * then you would call this using:
      * 
      * <pre>
      * addKnownHeader(&quot;y&quot;, new String[] {&quot;lat.*&quot;, &quot;y_wert.*&quot;}, true);
      * </pre>
      * @param key the name to use for the property
      * @param isIdentityHeader true if the property is an identity property
      * @param array of regular expressions to use to find the single property
      */
     protected void addKnownHeader(Integer headerId, String key, String[] regexes, boolean isIdentityHeader) {
         HeaderMaps header = getHeaderMap(headerId);
         if (header.knownHeaders.containsKey(key)) {
             List<String> value = header.knownHeaders.get(key);
             value.addAll(Arrays.asList(regexes));
             header.knownHeaders.put(key, value);
         } else {
             header.knownHeaders.put(key, Arrays.asList(regexes));
         }
         if (isIdentityHeader) {
             header.identityHeaders.add(key);
         }
     }
 
     /**
      * Add a number of regular expression strings to use as filters for deciding which properties to
      * save. If this method is never used, and the filters are empty, then all properties are
      * processed. Since the saving code is done in the specific loader, not using this method can
      * cause a lot more parsing of data than is necessary, so it is advised to use this. Note also
      * that the filter regular expressions are applied to the cleaned headers, not the original ones
      * found in the file.
      * 
      * @param filters
      */
     protected void addHeaderFilters(Integer headerMapId, String[] filters) {
         HeaderMaps header = getHeaderMap(headerMapId);
         for (String filter : filters) {
             header.headerFilters.add(Pattern.compile(filter));
         }
     }
 
     /**
      * gets header map
      * 
      * @param headerId
      * @return
      */
     protected HeaderMaps getHeaderMap(Integer index) {
         StoringProperty sProp = storingProperties.get(index);
         if (sProp == null) {
             sProp = new StoringProperty(getStoringNode(index));
             storingProperties.put(index, sProp);
         }
         HeaderMaps header = sProp.getHeaders();
         if (header == null) {
             header = new HeaderMaps();
             sProp.setHeaders(header);
         }
         return header;
     }
 
     /**
      * Add a special header that creates a new property based on the existence of another property.
      * This includes a mapper that modifies the contents of the value interpreted. For example, if
      * you want to create a new property called 'active' that contains only 'yes/no' values and is
      * based on finding the text 'on air' inside another property, use this:
      * 
      * <pre>
      * addMappedHeader(&quot;status&quot;, &quot;Active&quot;, &quot;active&quot;, new PropertyMapper() {
      *     public String mapValue(String originalValue) {
      *         return originalValue.toLowerCase().contains(&quot;on air&quot;) ? &quot;yes&quot; : &quot;no&quot;;
      *     }
      * });
      * </pre>
      * 
      * @param original header key to base new header on
      * @param name of new header, or null to use the old header (and replace it)
      * @param key of new header
      * @param mapper the mapper required to convert values from the old to the new
      */
     protected final void addMappedHeader(Integer headerMapId, String original, String name, String key, PropertyMapper mapper) {
         HeaderMaps header = getHeaderMap(headerMapId);
         header.mappedHeaders.put(original, new MappedHeaderRule(name, key, mapper));
     }
 
     /**
      * This uses the same PropertyMapper mechanism as the addMappedHeader() method, but does not
      * create a new property, instead it replaces the original property. Internally it uses the same
      * key for original and new property and also sets the new name to null to signal the system to
      * do replacement. This is especially useful if you want to override the default header parsing
      * logic with your own custom logic. For example, to keep string values for a property:
      * 
      * <pre>
      * useMapper(&quot;site&quot;, new StringMapper());
      * </pre>
      * 
      * @param key of header/property
      * @param mapper the mapper required to convert values
      */
     protected final void useMapper(Integer headerMapId, String key, PropertyMapper mapper) {
         HeaderMaps headerMap = getHeaderMap(headerMapId);
         headerMap.mappedHeaders.put(key, new MappedHeaderRule(null, key, mapper));
     }
 
     protected final void dropHeaderStats(Integer headerMapId, String[] keys) {
         HeaderMaps headerMap = getHeaderMap(headerMapId);
         headerMap.dropStatsHeaders.addAll(Arrays.asList(keys));
     }
 
     protected final void addNonDataHeaders(Integer headerMapId, Collection<String> keys) {
         HeaderMaps headerMap = getHeaderMap(headerMapId);
         headerMap.dropStatsHeaders.addAll(keys);
         headerMap.nonDataHeaders.addAll(keys);
     }
     protected final void addIdentityHeaders(Integer headerMapId, Collection<String> keys) {
         HeaderMaps headerMap = getHeaderMap(headerMapId);
         headerMap.dropStatsHeaders.addAll(keys);
         headerMap.nonDataHeaders.addAll(keys);
         headerMap.identityHeaders.addAll(keys);
     }
 
     /**
      * Parse possible header lines and build a set of header objects to be used to parse all data
      * lines later. This allows us to deal with several requirements:
      * <ul>
      * <li>Know when we have passed the header and are in the data body of the file</li>
      * <li>Have objects that automatically learn the type of the data as the data is parsed</li>
      * <li>Support mapping headers to known specific names</li>
      * <li>Support mapping values to different values using pre-defined mapper code</li>
      * </ul>
      * 
      * @param line to parse as the header line
      */
     protected final void parseHeader(String line) {
         debug(line);
         determineFieldSepRegex(line);
         List<String> fields = splitLine(line);
         if (fields.size() < 2)
             return;
         int index = 0;
         for (String headerName : fields) {
             String header = cleanHeader(headerName);
             for (StoringProperty sProp : storingProperties.values()) {
                 HeaderMaps headerMap = sProp.getHeaders();
                 if (headerMap.headerAllowed(header)) {
                     boolean added = false;
                     debug("Added header[" + index + "] = " + header);
                     KNOWN: for (String key : headerMap.knownHeaders.keySet()) {
                         if (!headerMap.headers.containsKey(key)) {
                             for (String regex : headerMap.knownHeaders.get(key)) {
                                 for (String testString : new String[] {header, headerName}) {
                                     if (testString.toLowerCase().matches(regex.toLowerCase())) {
                                         debug("Added known header[" + index + "] = " + key);
                                         if (!headerMap.identityHeaders.contains(key)){
                                             headerMap.headers.put(key, new Header(headerName, key, index));
                                         }else{
                                             final Header identityHeader = new Header(headerName, key, index);
                                             identityHeader.isIdentityHeader=true;
                                             headerMap.headers.put(key, identityHeader);
                                         }
                                         added = true;
                                         break KNOWN;
                                     }
                                 }
                             }
                         }
                     }
                     if (!added/* !headers.containsKey(header) */) {
                         if (!headerMap.identityHeaders.contains(header)){
                             headerMap.headers.put(header, new Header(headerName, header, index));
                         }else{
                             final Header identityHeader = new Header(headerName, header, index);
                             identityHeader.isIdentityHeader=true;
                             headerMap.headers.put(header, identityHeader);
                         }
                     }
                 }
                 headerWasParced = headerWasParced || !headerMap.headers.isEmpty();
             }
             index++;
         }
         // Now add any new properties created from other existing properties
         // using mapping rules
         for (StoringProperty sProp : storingProperties.values()) {
             HeaderMaps headerMap = sProp.getHeaders();
             for (String key : headerMap.mappedHeaders.keySet()) {
                 if (headerMap.headers.containsKey(key)) {
                     MappedHeaderRule mapRule = headerMap.mappedHeaders.get(key);
                     if (headerMap.headers.containsKey(mapRule.key)) {
                         // We only allow replacement if the user passed null for
                         // the name
                         if (mapRule.name == null) {
                             headerMap.headers.put(mapRule.key, new MappedHeader(headerMap.headers.get(key), mapRule));
                         } else {
                             notify("Cannot add mapped header with key '" + mapRule.key + "': header with that name already exists");
                         }
                     } else {
                         headerMap.headers.put(mapRule.key, new MappedHeader(headerMap.headers.get(key), mapRule));
                     }
                 } else {
                     notify("No original header found matching mapped header key: " + key);
                 }
             }
             for (String key : headerMap.dropStatsHeaders) {
                 Header header = headerMap.headers.get(key);
                 //do not drop stats for identity headers
                 if (header != null && !header.isIdentityHeader) {
                     header.dropStats();
                 }
             }
         }
     }
 
     protected Transaction mainTx;
     protected int commitSize = 5000;
 
     // protected List<String> getNumericProperties() {
     // ArrayList<String> results = new ArrayList<String>();
     // for (Class< ? extends Object> klass : NUMERIC_PROPERTY_TYPES) {
     // results.addAll(getProperties(klass));
     // }
     // return results;
     // }
 
     // protected List<String> getDataProperties() {
     // ArrayList<String> results = new ArrayList<String>();
     // results.addAll(getNumericProperties());
     // for (String key : getProperties(String.class)) {
     // if (headers.get(key).parseCount > 0) {
     // results.add(key);
     // }
     // }
     // return results;
     // }
 
     protected final LinkedHashMap<String, Object> makeDataMap(List<String> fields) {
         LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
         for (StoringProperty sProp : storingProperties.values()) {
             HeaderMaps headerMap = sProp.getHeaders();
             for (String key : headerMap.headers.keySet()) {
                 try {
                     Header header = headerMap.headers.get(key);
                     String field = fields.get(header.index);
                     if (field == null || field.length() < 1 || field.equals("?")) {
                         continue;
                     }
                     Object value = header.parse(field);
                     map.put(key, value);
                     // TODO: Decide if we should actually use the name here
                     // Now speed up parsing once we are certain of the column types
                     if (header.shouldConvert()) {
                         Class< ? extends Object> klass = header.knownType();
                         if (klass == Integer.class) {
                             headerMap.headers.put(key, new IntegerHeader(header));
                         } else if (klass == Float.class) {
                             headerMap.headers.put(key, new FloatHeader(header));
                         } else {
                             headerMap.headers.put(key, new StringHeader(header));
                         }
                     }
                 } catch (Exception e) {
                     // TODO Handle Exception
                 }
             }
         }
         return map;
     }
 
     private Display currentDisplay = null;
 
     protected final void debug(final String line) {
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.debug(typeName + ":" + basename + ":" + status() + ": " + line);
             }
         });
     }
 
     protected final void info(final String line) {
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.notify(typeName + ":" + basename + ":" + status() + ": " + line);
             }
         });
     }
 
     protected final void notify(final String line) {
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.notify(typeName + ":" + basename + ":" + status() + ": " + line);
             }
         });
     }
 
     protected final void error(final String line) {
         runInDisplay(new Runnable() {
             public void run() {
                 NeoLoaderPlugin.notify(typeName + ":" + basename + ":" + status() + ": " + line);
             }
         });
     }
 
     private final void runInDisplay(Runnable runnable) {
         if (display != null) {
             if (currentDisplay == null) {
                 currentDisplay = PlatformUI.getWorkbench().getDisplay();
             }
             currentDisplay.asyncExec(runnable);
         } else {
             runnable.run();
         }
     }
 
     protected final String status() {
         if (started <= 0)
             started = System.currentTimeMillis();
         return (lineNumber > 0 ? "line:" + lineNumber : "" + ((System.currentTimeMillis() - started) / 1000.0) + "s");
     }
 
     public void setLimit(int value) {
         this.limit = value;
     }
 
     protected boolean isOverLimit() {
         return limit > 0 && savedData > limit;
     }
 
     protected boolean setNewIndexProperty(Map<String, Header> headers, Node eventNode, String key, Object parsedValue) {
         if (eventNode.hasProperty(key)) {
             return false;
         }
         setIndexProperty(headers, eventNode, key, parsedValue);
         return true;
     }
 
     /**
      * Sets index property
      * 
      * @param headers index header
      * @param eventNode node
      * @param key property key
      * @param parsedValue parsed value
      */
     protected void setIndexProperty(Map<String, Header> headers, Node eventNode, String key, Object parsedValue) {
         if (parsedValue == null) {
             return;
         }
         eventNode.setProperty(key, parsedValue);
         Header header = headers.get(key);
         if (header == null) {
             header = new Header(key, key, 1);
 
             headers.put(key, header);
         }
         header.parseCount++;
         header.incValue(parsedValue);
         header.incType(parsedValue.getClass());
     }
 
     /**
      * Sets index property
      * 
      * @param headers index header
      * @param eventNode node
      * @param key property key
      * @param nonParsedValue parsed value
      */
     protected void setIndexPropertyNotParcedValue(LinkedHashMap<String, Header> headers, Node eventNode, String key, String nonParsedValue) {
         if (StringUtils.isEmpty(nonParsedValue)) {
             return;
         }
         Header header = headers.get(key);
         if (header == null) {
             header = new Header(key, key, 1);
 
             headers.put(key, header);
         }
         Object value = header.parse(nonParsedValue);
         eventNode.setProperty(key, value);
     }
 
     private void incSaved() {
         savedData++;
     }
 
     /**
      * This is the main method of the class. It opens the file, iterates over the contents and calls
      * parseLine(String) on each line. The subclass needs to implement parseLine(String) to
      * interpret the data and save it to the database.
      * 
      * @param monitor
      * @throws IOException
      */
     public void run(IProgressMonitor monitor) throws IOException {
         if (monitor != null && !taskSetted){
             monitor.beginTask(basename, 100);
         }
         CountingFileInputStream is = new CountingFileInputStream(new File(filename));
         String characterSet = NeoLoaderPlugin.getDefault().getCharacterSet();
         BufferedReader reader = new BufferedReader(new InputStreamReader(is, characterSet));
         mainTx = neo.beginTx();
         NeoUtils.addTransactionLog(mainTx, Thread.currentThread(), "AbstractLoader");
         try {
             initializeIndexes();
             int perc = is.percentage();
             int prevPerc = 0;
             int prevLineNumber = 0;
             String line;
             headerWasParced = !needParceHeaders();
             while ((line = reader.readLine()) != null) {
                 lineNumber++;
 
                 if (!headerWasParced) {
                     parseHeader(line);
                 } else {
                     parseLine(line);
                 }
                 if (monitor != null) {
                     if (monitor.isCanceled())
                         break;
                     perc = is.percentage();
                     if (perc > prevPerc) {
                         monitor.subTask(basename + ":" + lineNumber + " (" + perc + "%)");
                         monitor.worked(perc - prevPerc);
                         prevPerc = perc;
                     }
                 }
                 if (lineNumber > prevLineNumber + commitSize) {
                     commit(true);
                     prevLineNumber = lineNumber;
                 }
                 if (isOverLimit())
                     break;
 
             }
             commit(true);
             reader.close();
             saveProperties();
             finishUpIndexes();
             finishUp();
         } finally {
             commit(false);
         }
     }
 
     /**
      * check necessity of parsing headers
      * 
      * @return
      */
     protected abstract boolean needParceHeaders();
 
     // TODO add thread safe???
 
     protected void addIndex(String nodeType, MultiPropertyIndex< ? > index) {
         ArrayList<MultiPropertyIndex< ? >> indList = indexes.get(nodeType);
         if (indList == null) {
             indList = new ArrayList<MultiPropertyIndex< ? >>();
             indexes.put(nodeType, indList);
         }
         if (!indList.contains(index)) {
             indList.add(index);
         }
     }
 
     protected void addMappedIndex(String key, String nodeType, MultiPropertyIndex< ? > index) {
         LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>> mappIndex = mappedIndexes.get(key);
         if (mappIndex == null) {
             mappIndex = new LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>>();
             mappedIndexes.put(key, mappIndex);
         }
         HashSet<MultiPropertyIndex< ? >> indSet = mappIndex.get(nodeType);
         if (indSet == null) {
             indSet = new HashSet<MultiPropertyIndex< ? >>();
             mappIndex.put(nodeType, indSet);
         }
         indSet.add(index);
     }
 
     protected void removeIndex(String nodeType, MultiPropertyIndex< ? > index) {
         ArrayList<MultiPropertyIndex< ? >> indList = indexes.get(nodeType);
         if (indList != null) {
             indList.remove(index);
         }
 
     }
 
     /**
      *remove mapped index
      * 
      * @param key map key
      * @param nodeType - node type
      * @param index - index
      */
     private void removeMappedIndex(String key, String nodeType, MultiPropertyIndex< ? > index) {
         LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>> mapIn = mappedIndexes.get(key);
         if (mapIn != null) {
             HashSet<MultiPropertyIndex< ? >> indList = mapIn.get(nodeType);
             if (indList != null) {
                 indList.remove(index);
             }
         }
     }
 
     protected void index(Node node) {
         String nodeType = NeoUtils.getNodeType(node, "");
         ArrayList<MultiPropertyIndex< ? >> indList = indexes.get(nodeType);
         if (indList == null) {
             return;
         }
         for (MultiPropertyIndex< ? > index : indList) {
             try {
                 index.add(node);
             } catch (IOException e) {
                 // TODO:Log error
                 removeIndex(nodeType, index);
             }
         }
     }
 
     /**
      * Indexes mapped
      * 
      * @param key - index key
      * @param node - node
      */
     protected void index(String key, Node node) {
         String nodeType = NeoUtils.getNodeType(node, "");
         LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>> indMap = mappedIndexes.get(key);
         if (indMap == null) {
             return;
         }
         HashSet<MultiPropertyIndex< ? >> indList = indMap.get(nodeType);
         if (indList == null) {
             return;
         }
         for (MultiPropertyIndex< ? > index : indList) {
             try {
                 index.add(node);
             } catch (IOException e) {
                 NeoLoaderPlugin.error(e.getLocalizedMessage());
                 removeMappedIndex(key, nodeType, index);
             }
         }
     }
 
     protected void flushIndexes() {
         for (Entry<String, ArrayList<MultiPropertyIndex< ? >>> entry : indexes.entrySet()) {
 
             for (MultiPropertyIndex< ? > index : entry.getValue()) {
                 try {
                     index.flush();
                 } catch (IOException e) {
                     // TODO:Log error
                     removeIndex(entry.getKey(), index);
                 }
             }
         }
         for (Entry<String, LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>>> entryInd : mappedIndexes.entrySet()) {
             if (entryInd.getValue() != null) {
                 for (Entry<String, HashSet<MultiPropertyIndex< ? >>> entry : entryInd.getValue().entrySet()) {
                     for (MultiPropertyIndex< ? > index : entry.getValue()) {
                         try {
                             index.flush();
                         } catch (IOException e) {
                             // TODO:Log error
                             removeMappedIndex(entryInd.getKey(), entry.getKey(), index);
                         }
                     }
                 }
             }
         }
     }
 
     protected void initializeIndexes() {
         if (indexesInitialized) {
             return;
         }
         for (Entry<String, ArrayList<MultiPropertyIndex< ? >>> entry : indexes.entrySet()) {
             for (MultiPropertyIndex< ? > index : entry.getValue()) {
                 try {
                     index.initialize(this.neo, null);
                 } catch (IOException e) {
                     // TODO:Log error
                     removeIndex(entry.getKey(), index);
                 }
             }
         }
         for (Entry<String, LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>>> entryInd : mappedIndexes.entrySet()) {
             if (entryInd.getValue() != null) {
                 for (Entry<String, HashSet<MultiPropertyIndex< ? >>> entry : entryInd.getValue().entrySet()) {
                     for (MultiPropertyIndex< ? > index : entry.getValue()) {
                         try {
                             index.initialize(this.neo, null);
                         } catch (IOException e) {
                             NeoLoaderPlugin.error(e.getLocalizedMessage());
                             removeMappedIndex(entryInd.getKey(), entry.getKey(), index);
                         }
                     }
                 }
             }
         }
         indexesInitialized = true;
     }
 
     protected void finishUpIndexes() {
         for (Entry<String, ArrayList<MultiPropertyIndex< ? >>> entry : indexes.entrySet()) {
             for (MultiPropertyIndex< ? > index : entry.getValue()) {
                 index.finishUp();
             }
         }
         for (Entry<String, LinkedHashMap<String, HashSet<MultiPropertyIndex< ? >>>> entryInd : mappedIndexes.entrySet()) {
             if (entryInd.getValue() != null) {
                 for (Entry<String, HashSet<MultiPropertyIndex< ? >>> entry : entryInd.getValue().entrySet()) {
                     for (MultiPropertyIndex< ? > index : entry.getValue()) {
                         index.finishUp();
                     }
                 }
             }
         }
     }
 
     protected void commit(boolean restart) {
         if (mainTx != null) {
             flushIndexes();
             mainTx.success();
             mainTx.finish();
             // LOGGER.debug("Commit: Memory: "+(Runtime.getRuntime().totalMemory()
             // -
             // Runtime.getRuntime().freeMemory()));
             if (restart) {
                 mainTx = neo.beginTx();
             } else {
                 mainTx = null;
             }
         }
     }
 
     /**
      * This method must be implemented by all readers to parse the data lines. It might save data
      * directly to the database, or it might keep it in a cache for saving later, in the finishUp
      * method. A common pattern is to block data into chunks, saving these to the database at
      * reasonable points, and then using finishUp() to save any remaining data.
      * 
      * @param line
      */
     protected abstract void parseLine(String line);
 
     /**
      * After all lines have been parsed, this method is called, allowing the implementing class the
      * opportunity to save any cached information, or write any final statistics. It is not abstract
      * because it is possible, or even probable, to write an importer that does not need it.
      */
     protected void finishUp() {
         if (!isTest()) {
             addRootToProject();
         }
         commit(true);
 
         for (Map.Entry<Integer, StoringProperty> entry : storingProperties.entrySet()) {
             Node storeNode = getStoringNode(entry.getKey());
             entry.getValue().storeTimeStamp(storeNode);
             if (storeNode != null)
                 storeNode.setProperty(INeoConstants.COUNT_TYPE_NAME, entry.getValue().getDataCounter());
         }
 
     }
 
     /**
      * Adds the root to project.
      */
     protected void addRootToProject() {
         for (Node root : getRootNodes()) {
             String aweProjectName = LoaderUtils.getAweProjectName();
             if (root != null) {
                 NeoCorePlugin.getDefault().getProjectService().addDataNodeToProject(aweProjectName, root);
             }
         }
     }
 
     /**
      * Search the database for the 'gis' node for this dataset. If none found it created an
      * appropriate node. The search is done for 'gis' nodes that reference the specified main node.
      * If a node needs to be created it is linked to the main node so future searches will return
      * it.
      * 
      * @param mainNode main network or drive data node
      * @return gis node for mainNode
      */
     protected final Node findOrCreateGISNode(String gisName, String gisType, NetworkTypes fileType) {
         GisProperties gisProperties = gisNodes.get(gisName);
 
         if (gisProperties == null) {
             Transaction transaction = neo.beginTx();
             try {
                 Node reference = neo.getReferenceNode();
 
                 Node gis = NeoUtils.findGisNode(gisName, neo);
                 if (gis == null) {
                     gis = NeoUtils.createGISNode(reference, gisName, gisType, neo);
                     fileType.setTypeToNode(gis, neo);
                 }
                 gisProperties = new GisProperties(gis);
                 gisNodes.put(gisName, gisProperties);
                 transaction.success();
             } finally {
                 transaction.finish();
             }
         }
         // TODO add check on correct type!
         return gisProperties.getGis();
     }
 
     protected void deleteTree(Node root) {
         if (root != null) {
             for (Relationship relationship : root.getRelationships(NetworkRelationshipTypes.CHILD, Direction.OUTGOING)) {
                 Node node = relationship.getEndNode();
                 deleteTree(node);
                 debug("Deleting node " + node + ": " + (node.hasProperty("name") ? node.getProperty("name") : ""));
                 deleteNode(node);
             }
         }
     }
 
     protected void deleteNode(Node node) {
         if (node != null) {
             for (Relationship relationship : node.getRelationships()) {
                 relationship.delete();
             }
             node.delete();
         }
     }
 
     protected abstract String getPrymaryType(Integer key);
 
     protected void saveProperties() {
         for (Map.Entry<Integer, StoringProperty> spEntry : storingProperties.entrySet()) {
             HeaderMaps headers = spEntry.getValue().getHeaders();
             Node storingRootNode = getStoringNode(spEntry.getKey());
             if (storingRootNode != null && headers != null) {
                 Transaction transaction = neo.beginTx();
                 try {
                     String primaryType = getPrymaryType(spEntry.getKey());
                     if (StringUtils.isNotEmpty(primaryType)) {
                         NeoUtils.setPrimaryType(storingRootNode, primaryType, neo);
                     }
                     Node propNode;
                     Relationship propRel = storingRootNode.getSingleRelationship(GeoNeoRelationshipTypes.PROPERTIES, Direction.OUTGOING);
                     if (propRel == null) {
                         propNode = neo.createNode();
                         propNode.setProperty(INeoConstants.PROPERTY_NAME_NAME, NeoUtils.getNodeName(storingRootNode, neo));
                         propNode.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.GIS_PROPERTIES.getId());
                         storingRootNode.createRelationshipTo(propNode, GeoNeoRelationshipTypes.PROPERTIES);
                     } else {
                         propNode = propRel.getEndNode();
                     }
                     HashMap<String, Node> propTypeNodes = new HashMap<String, Node>();
                     for (Node node : propNode.traverse(Order.BREADTH_FIRST, StopEvaluator.END_OF_GRAPH, ReturnableEvaluator.ALL_BUT_START_NODE,
                             GeoNeoRelationshipTypes.CHILD, Direction.OUTGOING)) {
                         propTypeNodes.put(node.getProperty("name").toString(), node);
                     }
                     for (Class< ? extends Object> klass : KNOWN_PROPERTY_TYPES) {
                         String typeName = makePropertyTypeName(klass);
                         List<String> properties = headers.getProperties(klass);
                         if (properties != null && properties.size() > 0) {
                             Node propTypeNode = propTypeNodes.get(typeName);
                             if (propTypeNode == null) {
                                 propTypeNode = neo.createNode();
                                 propTypeNode.setProperty(INeoConstants.PROPERTY_NAME_NAME, typeName);
                                 propTypeNode.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NodeTypes.GIS_PROPERTY.getId());
                                 savePropertiesToNode(headers, propTypeNode, properties);
                                 propNode.createRelationshipTo(propTypeNode, GeoNeoRelationshipTypes.CHILD);
                             } else {
                                 TreeSet<String> combinedProperties = new TreeSet<String>();
                                 String[] previousProperties = (String[])propTypeNode.getProperty(INeoConstants.NODE_TYPE_PROPERTIES, null);
                                 if (previousProperties != null)
                                     combinedProperties.addAll(Arrays.asList(previousProperties));
                                 combinedProperties.addAll(properties);
                                 savePropertiesToNode(headers, propTypeNode, combinedProperties);
                             }
                         }
                     }
                     transaction.success();
                 } finally {
                     transaction.finish();
                 }
             }
         }
     }
 
     /**
      * get root for statistic node
      * 
      * @param key - statistic id
      * @return Node or null
      */
     protected abstract Node getStoringNode(Integer key);
 
     private void savePropertiesToNode(HeaderMaps headerMaps, Node propTypeNode, Collection<String> properties) {
 
         propTypeNode.setProperty("properties", properties.toArray(new String[properties.size()]));
         HashMap<String, Node> valueNodes = new HashMap<String, Node>();
         ArrayList<String> noStatsProperties = new ArrayList<String>();
         ArrayList<String> dataProperties = new ArrayList<String>();
         ArrayList<String> identityProperties= new ArrayList<String>();
         for (Relationship relation : propTypeNode.getRelationships(GeoNeoRelationshipTypes.PROPERTIES,GeoNeoRelationshipTypes.IDENTITY_PROPERTIES)) {
             Node valueNode = relation.getEndNode();
             String property = relation.getProperty("property", "").toString();
             valueNodes.put(property, valueNode);
         }
         for (String property : properties) {
             if (!headerMaps.nonDataHeaders.contains(property)) {
                 dataProperties.add(property);
             }
             Node valueNode = valueNodes.get(property);
             Header header = headerMaps.headers.get(property);
            GeoNeoRelationshipTypes relType = !header.isIdentityHeader ? GeoNeoRelationshipTypes.PROPERTIES
                    : GeoNeoRelationshipTypes.IDENTITY_PROPERTIES;
             // if current headers do not contain information about property - we
             // do not handling
             // this property
             if (header == null) {
                 continue;
             }
             HashMap<Object, Integer> values = header.values;
             Relationship valueRelation = null;
             if (values == null) {
                 if (valueNode != null) {
                     for (Relationship relation : valueNode.getRelationships()) {
                         relation.delete();
                     }
                     valueNode.delete();
                     valueNode = null;
                 }
                 noStatsProperties.add(property);
             } else {
                 if (header.isIdentityHeader){
                     noStatsProperties.add(property);
                     identityProperties.add(property);
                 }
                 if (valueNode == null) {
                     valueNode = neo.createNode();
                     valueRelation = propTypeNode.createRelationshipTo(valueNode, relType);
                     valueRelation.setProperty("property", property);
                 } else {
                     valueRelation = valueNode.getSingleRelationship(relType, Direction.INCOMING);
                     for (Object key : valueNode.getPropertyKeys()) {
                         Integer oldCount = (Integer)valueNode.getProperty(key.toString(), null);
                         if (oldCount == null) {
                             oldCount = 0;
                         }
                         Integer newCount = values.get(key);
                         if (newCount == null) {
                             newCount = 0;
                         }
                         values.put(key, oldCount + newCount);
                     }
                 }
                 int total = 0;
                 for (Object key : values.keySet()) {
                     valueNode.setProperty(key.toString(), values.get(key));
                     total += values.get(key);
                 }
                 if (valueRelation != null) {
                     valueRelation.setProperty("count", total);
                 }
             }
 
             if (valueNode == null) {
                 valueNode = neo.createNode();
                 valueRelation = propTypeNode.createRelationshipTo(valueNode, relType);
                 valueRelation.setProperty("property", property);
                 valueRelation.setProperty(INeoConstants.COUNT_TYPE_NAME, header.countALL);
             }
             if (!header.min.equals(Double.POSITIVE_INFINITY)) {
                 valueRelation.setProperty(INeoConstants.MIN_VALUE, header.min);
                 valueRelation.setProperty(INeoConstants.MAX_VALUE, header.max);
 
             }
         }
         ArrayList<String> statsProperties = new ArrayList<String>(properties);
         for (String noStat : noStatsProperties) {
             statsProperties.remove(noStat);
         }
         propTypeNode.setProperty("data_properties", dataProperties.toArray(new String[0]));
         propTypeNode.setProperty("stats_properties", statsProperties.toArray(new String[0]));
         propTypeNode.setProperty("no_stats_properties", noStatsProperties.toArray(new String[0]));
         propTypeNode.setProperty("identity_properties", identityProperties.toArray(new String[0]));
     }
 
     public static String makePropertyTypeName(Class< ? extends Object> klass) {
         return klass.getName().replaceAll("java.lang.", "").toLowerCase();
     }
 
     /**
      * This method adds the loaded data to the GIS catalog. This is achieved by
      * <ul>
      * <li>Cleaning the gis node of any old statistics, and then updating the basic statistics</li>
      * <li>Then the data is added to the current AWE project</li>
      * <li>The catalog for Neo data is created or updated</li>
      * </ul>
      * 
      * @throws MalformedURLException
      */
     public static final void finishUpGis() throws MalformedURLException {
         NeoServiceProvider neoProvider = NeoServiceProvider.getProvider();
         if (neoProvider != null) {
             addDataToCatalog();
         }
     }
 
     public abstract Node[] getRootNodes();
 
     /**
      * Is this a test case running outside AWE application
      * 
      * @return true if we have no NeoProvider and so are not running inside AWE
      */
     protected final boolean isTest() {
         return neoProvider == null;
     }
 
     public void clearCaches() {
         for (StoringProperty sProp : storingProperties.values()) {
             sProp.getHeaders().clearCaches();
         }
     }
 
     /**
      * This method adds the loaded data to the GIS catalog. The neo-catalog entry is created or
      * updated.
      * 
      * @throws MalformedURLException
      */
     public static void addDataToCatalog() throws MalformedURLException {
         // TODO: Lagutko, 17.12.2009, can be run as a Job
         NeoServiceProvider neoProvider = NeoServiceProvider.getProvider();
         if (neoProvider != null) {
             String databaseLocation = neoProvider.getDefaultDatabaseLocation();
             sendUpdateEvent(UpdateViewEventType.GIS);
             ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
             URL url = new URL("file://" + databaseLocation);
             List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(url);
             for (IService service : services) {
                 if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                     catalog.replace(service.getIdentifier(), service);
                 } else {
                     catalog.add(service);
                 }
             }
             neoProvider.commit();
         }
     }
 
     public static void sendUpdateEvent(UpdateViewEventType aType) {
         NeoCorePlugin.getDefault().getUpdateViewManager().fireUpdateView(new UpdateDatabaseEvent(aType));
     }
 
     /**
      * Clean all gis nodes of any old statistics, and then update the basic statistics
      */
     protected final void cleanupGisNode() {
         for (GisProperties gisProperties : gisNodes.values()) {
             cleanupGisNode(gisProperties);
         }
     }
 
     /**
      * Clean the gis node of any old statistics, and then update the basic statistics
      * 
      * @param mainNode to use to connect to the AWE project
      * @throws MalformedURLException
      */
     private final void cleanupGisNode(GisProperties gisProperties) {
         if (gisProperties != null) {
             Transaction transaction = neo.beginTx();
             try {
                 Node gis = gisProperties.getGis();
                 if (gisProperties.getBbox() != null) {
                     gis.setProperty(INeoConstants.PROPERTY_BBOX_NAME, gisProperties.getBbox());
                 }
                 gis.setProperty(INeoConstants.COUNT_TYPE_NAME, gisProperties.savedData);
                 HashSet<Node> nodeToDelete = new HashSet<Node>();
                 for (Relationship relation : gis.getRelationships(NetworkRelationshipTypes.AGGREGATION, Direction.OUTGOING)) {
                     nodeToDelete.add(relation.getEndNode());
                 }
                 for (Node node : nodeToDelete) {
                     NeoCorePlugin.getDefault().getProjectService().deleteNode(node);
                 }
                 transaction.success();
             } finally {
                 transaction.finish();
             }
         }
     }
 
     /**
      * Collects a list of GIS nodes that should be added to map
      * 
      * @return list of GIS nodes
      */
     protected ArrayList<Node> getGisNodes() {
         ArrayList<Node> result = new ArrayList<Node>();
         for (GisProperties gisPr : gisNodes.values()) {
             result.add(gisPr.getGis());
         }
         return result;
     }
 
     /**
      * adds gis to active map
      * 
      * @param gis node
      */
     public void addLayersToMap() {
         LoaderUtils.addGisNodeToMap(getDataName(), getGisNodes().toArray(new Node[0]));
     }
     
     protected String getDataName(){
         return filename;
     }
 
     /**
      * @return Time in milliseconds since this loader started running
      */
     protected long timeTaken() {
         return System.currentTimeMillis() - started;
     }
 
     private void printHeaderStats() {
         notify("Determined Columns:");
         for (StoringProperty sProp : storingProperties.values()) {
             HeaderMaps hm = sProp.getHeaders();
             for (String key : hm.headers.keySet()) {
                 Header header = hm.headers.get(key);
                 if (header.parseCount > 0) {
                     notify("\t" + header.knownType() + " loaded: " + header.parseCount + " => " + key);
                 }
             }
         }
     }
 
     public void printStats(boolean verbose) {
         printHeaderStats();
         long taken = timeTaken();
         notify("Finished loading " + basename + " data in " + (taken / 1000.0) + " seconds");
     }
 
     public void setCommitSize(int commitSize) {
         this.commitSize = commitSize;
     }
 
     /**
      * @return Returns the commitSize.
      */
     public int getCommitSize() {
         return commitSize;
     }
 
     /**
      * This code finds the specified network node in the database, creating its own transaction for
      * that.
      * 
      * @param gis gis node
      */
     protected Node findOrCreateNetworkNode(Node gisNode) {
         return NeoUtils.findOrCreateNetworkNode(gisNode, basename, filename, neo);
     }
 
     /**
      * get gisProperties by gis name
      * 
      * @param name gis name
      * @return GisProperties
      */
     protected GisProperties getGisProperties(String name) {
         return gisNodes.get(name);
     }
 
     public class HeaderMaps {
         protected HashMap<Class< ? extends Object>, List<String>> typedProperties = null;
         protected ArrayList<Pattern> headerFilters = new ArrayList<Pattern>();
         protected LinkedHashMap<String, List<String>> knownHeaders = new LinkedHashMap<String, List<String>>();
         protected LinkedHashMap<String, MappedHeaderRule> mappedHeaders = new LinkedHashMap<String, MappedHeaderRule>();
         public LinkedHashMap<String, Header> headers = new LinkedHashMap<String, Header>();
         protected TreeSet<String> dropStatsHeaders = new TreeSet<String>();
         protected TreeSet<String> nonDataHeaders = new TreeSet<String>();
         protected TreeSet<String> identityHeaders = new TreeSet<String>();
 
         /**
          * @return true if we have parsed the header line and know the properties to load
          */
         protected boolean haveHeaders() {
             return headers.size() > 0;
         }
 
         /**
          * Get the header name for the specified key, if it exists
          * 
          * @param key
          * @return
          */
         protected String headerName(String key) {
             Header header = headers.get(key);
             return header == null ? null : header.name;
         }
 
         public void clearCaches() {
             this.headers.clear();
             this.knownHeaders.clear();
         }
 
         protected boolean headerAllowed(String header) {
             if (headerFilters == null || headerFilters.size() < 1) {
                 return true;
             }
             for (Pattern filter : headerFilters) {
                 if (filter.matcher(header).matches()) {
                     return true;
                 }
             }
             return false;
         }
 
         protected List<String> getProperties(Class< ? extends Object> klass) {
             if (typedProperties == null) {
                 makeTypedProperties();
             }
             return typedProperties.get(klass);
         }
 
         private void makeTypedProperties() {
             this.typedProperties = new HashMap<Class< ? extends Object>, List<String>>();
             for (Class< ? extends Object> klass : KNOWN_PROPERTY_TYPES) {
                 this.typedProperties.put(klass, new ArrayList<String>());
             }
             for (String key : headers.keySet()) {
                 Header header = headers.get(key);
                 if (header.parseCount > 0) {
                     for (Class< ? extends Object> klass : KNOWN_PROPERTY_TYPES) {
                         if (header.knownType() == klass) {
                             this.typedProperties.get(klass).add(header.key);
                         }
                     }
                 }
             }
         }
     }
 
     public class StoringProperty {
         // private Node storingNode;
         private long dataCounter;
         private Long timeStampMin;
         private Long timeStampMax;
         private HeaderMaps headers;
 
         public StoringProperty(Node storingNode) {
             // this.storingNode = storingNode;
             if (storingNode != null)
                 dataCounter = (Long)storingNode.getProperty(INeoConstants.COUNT_TYPE_NAME, 0L);
         }
 
         /**
          * @param storeNode node for store
          */
         public void storeTimeStamp(Node storeNode) {
             if (storeNode != null) {
                 if (timeStampMin != null) {
                     storeNode.setProperty(INeoConstants.MIN_TIMESTAMP, timeStampMin);
                 }
                 if (timeStampMax != null) {
                     storeNode.setProperty(INeoConstants.MAX_TIMESTAMP, timeStampMax);
                 }
             }
         }
 
         /**
          *inc saved;
          */
         public void incSaved() {
             dataCounter++;
         }
 
         /**
          * @return Returns the dataCountre.
          */
         public long getDataCounter() {
             return dataCounter;
         }
 
         /**
          * @return Returns the timeStampMin.
          */
         public Long getTimeStampMin() {
             return timeStampMin;
         }
 
         /**
          * @param timeStampMin The timeStampMin to set.
          */
         public void setTimeStampMin(Long timeStampMin) {
             this.timeStampMin = timeStampMin;
         }
 
         /**
          * @return Returns the timeStampMax.
          */
         public Long getTimeStampMax() {
             return timeStampMax;
         }
 
         /**
          * @param timeStampMax The timeStampMax to set.
          */
         public void setTimeStampMax(Long timeStampMax) {
             this.timeStampMax = timeStampMax;
         }
 
         /**
          * @return Returns the headers.
          */
         public HeaderMaps getHeaders() {
             return headers;
         }
 
         /**
          * @param headers The headers to set.
          */
         public void setHeaders(HeaderMaps headers) {
             this.headers = headers;
         }
 
         /**
          * @param dataCounter The dataCounter to set.
          */
         public void setDataCounter(long dataCounter) {
             this.dataCounter = dataCounter;
         }
 
     }
 
     public static class GisProperties {
         private final Node gis;
         private CRS crs;
         private double[] bbox;
         private long savedData;
 
         public GisProperties(Node gis) {
             this.gis = gis;
             bbox = (double[])gis.getProperty(INeoConstants.PROPERTY_BBOX_NAME, null);
             savedData = (Long)gis.getProperty(INeoConstants.COUNT_TYPE_NAME, 0L);
         }
 
         /**
          *inc saved;
          */
         public void incSaved() {
             savedData++;
         }
 
         protected final void checkCRS(float lat, float lon, String hint) {
             if (crs == null) {
                 // TODO move CRS class and update CRS in amanzi.neo.core
                 crs = CRS.fromLocation(lat, lon, hint);
                 saveCRS();
             }
         }
 
         /**
          * initCRS
          */
         public void initCRS() {
             if (gis.hasProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME) && gis.hasProperty(INeoConstants.PROPERTY_CRS_NAME)) {
                 crs = CRS.fromCRS((String)gis.getProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME), (String)gis.getProperty(INeoConstants.PROPERTY_CRS_NAME));
             }
         }
 
         /**
          * ubdate bbox
          * 
          * @param lat - latitude
          * @param lon - longitude
          */
         public final void updateBBox(double lat, double lon) {
             if (bbox == null) {
                 bbox = new double[] {lon, lon, lat, lat};
             } else {
                 if (bbox[0] > lon)
                     bbox[0] = lon;
                 if (bbox[1] < lon)
                     bbox[1] = lon;
                 if (bbox[2] > lat)
                     bbox[2] = lat;
                 if (bbox[3] < lat)
                     bbox[3] = lat;
             }
         }
 
         /**
          * @return Returns the gis.
          */
         public Node getGis() {
             return gis;
         }
 
         /**
          * @return Returns the bbox.
          */
         public double[] getBbox() {
             return bbox;
         }
 
         /**
          * @param crs The crs to set.
          */
         public void setCrs(CRS crs) {
             this.crs = crs;
         }
 
         /**
          * @return
          */
         public CRS getCrs() {
             return crs;
         }
 
         /**
          *save bbox to gis node
          */
         public void saveBBox() {
             if (getBbox() != null) {
                 gis.setProperty(INeoConstants.PROPERTY_BBOX_NAME, getBbox());
             }
         }
 
         /**
          *save CRS to gis node
          */
         public void saveCRS() {
             if (getCrs() != null) {
                 if (crs.getWkt() != null) {
                     gis.setProperty(INeoConstants.PROPERTY_WKT_CRS, crs.getWkt());
                 }
                 gis.setProperty(INeoConstants.PROPERTY_CRS_TYPE_NAME, crs.getType());// TODO remove?
                 // - not used
                 // in GeoNeo
                 gis.setProperty(INeoConstants.PROPERTY_CRS_NAME, crs.toString());
             }
         }
 
         /**
          *save CRS
          * 
          * @param crs -CoordinateReferenceSystem
          */
         public void setCrs(CoordinateReferenceSystem crs) {
             setCrs(CRS.fromCRS(crs));
         }
 
         /**
          * @param bbox The bbox to set.
          */
         public void setBbox(double[] bbox) {
             this.bbox = bbox;
         }
     }
 
     /**
      * @param key -key of value from preference store
      * @return array of possible headers
      */
     protected String[] getPossibleHeaders(String key) {
         String text = NeoLoaderPlugin.getDefault().getPreferenceStore().getString(key);
         String[] array = text.split(",");
         List<String> result = new ArrayList<String>();
         for (String string : array) {
             String value = string.trim();
             if (!value.isEmpty()) {
                 result.add(value);
             }
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * Updates Min and Max timestamp values for this gis
      * 
      * @param timestamp
      */
     protected void updateTimestampMinMax(Integer key, final long timestamp) {
         StoringProperty sProp = storingProperties.get(key);
         if (sProp == null) {
             sProp = new StoringProperty(getStoringNode(key));
             storingProperties.put(key, sProp);
         }
         Long minTimeStamp = sProp.getTimeStampMin() == null ? timestamp : Math.min(sProp.getTimeStampMin(), timestamp);
         Long maxTimeStamp = sProp.getTimeStampMax() == null ? timestamp : Math.max(sProp.getTimeStampMax(), timestamp);
         sProp.setTimeStampMin(minTimeStamp);
         sProp.setTimeStampMax(maxTimeStamp);
     }
 
     /**
      * Sets property to node (if value!=null)
      * 
      * @param node - node
      * @param key - property key
      * @param value - value
      */
     protected void setProperty(PropertyContainer node, String key, Object value) {
         if (value != null) {
             node.setProperty(key, value);
         }
     }
 
     /**
      * @param gisProperties
      * @return
      */
     public static CoordinateReferenceSystem askCRSChoise(final GisProperties gisProperties) {
         CoordinateReferenceSystem result = ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult<CoordinateReferenceSystem>() {
 
             private CoordinateReferenceSystem result;
 
             @Override
             public CoordinateReferenceSystem getValue() {
                 return result;
             }
 
             @Override
             public void run() {
                 result = null;
                 CommonCRSPreferencePage page = new CommonCRSPreferencePage();
                 try {
                     LOGGER.debug(gisProperties.getCrs().epsg);
                     page.setSelectedCRS(org.geotools.referencing.CRS.decode(gisProperties.getCrs().epsg));
                 } catch (NoSuchAuthorityCodeException e) {
                     NeoLoaderPlugin.exception(e);
                     result = null;
                     return;
                 }
                 page.setTitle("Select Coordinate Reference System");
                 page.setSubTitle("Select the coordinate reference system from the list of commonly used CRS's, or add a new one with the Add button");
                 page.init(PlatformUI.getWorkbench());
                 PreferenceManager mgr = new PreferenceManager();
                 IPreferenceNode node = new PreferenceNode("1", page); //$NON-NLS-1$
                 mgr.addToRoot(node);
                 Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
                 PreferenceDialog pdialog = new PreferenceDialog(shell, mgr);;
                 if (pdialog.open() == PreferenceDialog.OK) {
                     page.performOk();
                     result = page.getCRS();
                 }
 
             }
         });
         return result;
     }
 
     /**
      * Calculates list of files to import
      * 
      * @param directoryName directory to import
      * @param extension extension of File (can be null)
      * @return list of files to import
      */
     protected ArrayList<File> getAllLogFilePathes(String directoryName, String extension) {
         File directory = new File(directoryName);
         ArrayList<File> result = new ArrayList<File>();
 
         for (File childFile : directory.listFiles()) {
             if (childFile.isDirectory()) {
                 result.addAll(getAllLogFilePathes(childFile.getAbsolutePath(), extension));
             } else if (childFile.isFile() && ((extension == null) || childFile.getName().endsWith(extension))) {
                 result.add(childFile);
             }
         }
         return result;
 
     }
 
     /**
      * @param taskSetted The taskSetted to set.
      */
     public void setTaskSetted(boolean taskSetted) {
         this.taskSetted = taskSetted;
     }
     
 }
