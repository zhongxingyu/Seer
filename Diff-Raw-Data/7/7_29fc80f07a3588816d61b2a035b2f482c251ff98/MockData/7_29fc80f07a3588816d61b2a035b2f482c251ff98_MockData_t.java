 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.data.test;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.geoserver.data.CatalogWriter;
 import org.geoserver.data.util.CoverageStoreUtils;
 import org.geoserver.data.util.CoverageUtils;
 import org.geotools.coverage.Category;
 import org.geotools.coverage.GridSampleDimension;
 import org.geotools.coverage.grid.GridGeometry2D;
 import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
 import org.geotools.coverage.grid.io.AbstractGridFormat;
 import org.geotools.coverage.grid.io.GridFormatFinder;
 import org.geotools.data.property.PropertyDataStoreFactory;
 import org.geotools.geometry.GeneralEnvelope;
 import org.geotools.referencing.CRS;
 import org.opengis.coverage.grid.GridGeometry;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.cs.CoordinateSystem;
 
 
 /**
  * Class used to build a mock GeoServer data directory.
  * <p>
  * Data is based off the wms and wfs "cite" datasets.
  * </p>
  *
  * @author Justin Deoliveira, The Open Planning Project
  *
  */
 public class MockData {
     // //// WMS 1.1.1
     /**
      * WMS 1.1.1 cite namespace + uri
      */
     public static String CITE_PREFIX = "cite";
     public static String CITE_URI = "http://www.opengis.net/cite";
 
     /** featuretype name for WMS 1.1.1 CITE BasicPolygons features */
     public static QName BASIC_POLYGONS = new QName(CITE_URI, "BasicPolygons", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Bridges features */
     public static QName BRIDGES = new QName(CITE_URI, "Bridges", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Buildings features */
     public static QName BUILDINGS = new QName(CITE_URI, "Buildings", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Divided Routes features */
     public static QName DIVIDED_ROUTES = new QName(CITE_URI, "DividedRoutes", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Forests features */
     public static QName FORESTS = new QName(CITE_URI, "Forests", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Lakes features */
     public static QName LAKES = new QName(CITE_URI, "Lakes", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Map Neatliine features */
     public static QName MAP_NEATLINE = new QName(CITE_URI, "MapNeatline", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Named Places features */
     public static QName NAMED_PLACES = new QName(CITE_URI, "NamedPlaces", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Ponds features */
     public static QName PONDS = new QName(CITE_URI, "Ponds", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Road Segments features */
     public static QName ROAD_SEGMENTS = new QName(CITE_URI, "RoadSegments", CITE_PREFIX);
 
     /** featuretype name for WMS 1.1.1 CITE Streams features */
     public static QName STREAMS = new QName(CITE_URI, "Streams", CITE_PREFIX);
 
     // /// WFS 1.0
     /**
      * WFS 1.0 cdf namespace + uri
      */
     public static String CDF_PREFIX = "cdf";
     public static String CDF_URI = "http://www.opengis.net/cite/data";
 
     /** featuretype name for WFS 1.0 CITE Deletes features */
     public static QName DELETES = new QName(CDF_URI, "Deletes", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Fifteen features */
     public static QName FIFTEEN = new QName(CDF_URI, "Fifteen", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Inserts features */
     public static QName INSERTS = new QName(CDF_URI, "Inserts", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Inserts features */
     public static QName LOCKS = new QName(CDF_URI, "Locks", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Nulls features */
     public static QName NULLS = new QName(CDF_URI, "Nulls", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Other features */
     public static QName OTHER = new QName(CDF_URI, "Other", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Nulls features */
     public static QName SEVEN = new QName(CDF_URI, "Seven", CDF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Updates features */
     public static QName UPDATES = new QName(CDF_URI, "Updates", CDF_PREFIX);
 
     /**
      * cgf namespace + uri
      */
     public static String CGF_PREFIX = "cgf";
     public static String CGF_URI = "http://www.opengis.net/cite/geometry";
 
     /** featuretype name for WFS 1.0 CITE Lines features */
     public static QName LINES = new QName(CGF_URI, "Lines", CGF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE MLines features */
     public static QName MLINES = new QName(CGF_URI, "MLines", CGF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE MPoints features */
     public static QName MPOINTS = new QName(CGF_URI, "MPoints", CGF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE MPolygons features */
     public static QName MPOLYGONS = new QName(CGF_URI, "MPolygons", CGF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Points features */
     public static QName POINTS = new QName(CGF_URI, "Points", CGF_PREFIX);
 
     /** featuretype name for WFS 1.0 CITE Polygons features */
     public static QName POLYGONS = new QName(CGF_URI, "Polygons", CGF_PREFIX);
 
     // //// WFS 1.1
     /**
      * sf namespace + uri
      */
     public static String SF_PREFIX = "sf";
     public static String SF_URI = "http://cite.opengeospatial.org/gmlsf";
     public static QName PRIMITIVEGEOFEATURE = new QName(SF_URI, "PrimitiveGeoFeature", SF_PREFIX);
     public static QName AGGREGATEGEOFEATURE = new QName(SF_URI, "AggregateGeoFeature", SF_PREFIX);
     public static QName GENERICENTITY = new QName(SF_URI, "GenericEntity", SF_PREFIX);
 
     // DEFAULT
     public static String DEFAULT_PREFIX = "gs";
     public static String DEFAULT_URI = "http://geoserver.org";
 
     // public static QName ENTIT\u00C9G\u00C9N\u00C9RIQUE = new QName( SF_URI,
     // "Entit\u00E9G\u00E9n\u00E9rique", SF_PREFIX );
     
     // Extra types
     public static QName GEOMETRYLESS = new QName(CITE_URI, "Geometryless", CITE_PREFIX);
     
 
     /**
      * List of all cite types names
      */
     public static QName[] TYPENAMES = new QName[] {
             // WMS 1.1.1
             BASIC_POLYGONS, BRIDGES, BUILDINGS, DIVIDED_ROUTES, FORESTS, LAKES, MAP_NEATLINE,
             NAMED_PLACES, PONDS, ROAD_SEGMENTS, STREAMS, // WFS 1.0
             DELETES, FIFTEEN, INSERTS, LOCKS, NULLS, OTHER, SEVEN, UPDATES, LINES, MLINES, MPOINTS,
             MPOLYGONS, POINTS, POLYGONS, // WFS 1.1
             PRIMITIVEGEOFEATURE, AGGREGATEGEOFEATURE, GENERICENTITY, GEOMETRYLESS /* ENTIT\u00C9G\u00C9N\u00C9RIQUE */
         };
 
     /**
      * List of wms type names.
      */
     public static QName[] WMS_TYPENAMES = new QName[] {
             BASIC_POLYGONS, BRIDGES, BUILDINGS, DIVIDED_ROUTES, FORESTS, LAKES, MAP_NEATLINE,
             NAMED_PLACES, PONDS, ROAD_SEGMENTS, STREAMS
         };
 
     /**
      * List of wfs 1.0 type names.
      */
     public static QName[] WFS10_TYPENAMES = new QName[] {
             DELETES, FIFTEEN, INSERTS, LOCKS, NULLS, OTHER, SEVEN, UPDATES, LINES, MLINES, MPOINTS,
             MPOLYGONS, POINTS, POLYGONS
         };
 
     /**
      * List of wfs 1.1 type names.
      */
     public static QName[] WFS11_TYPENAMES = new QName[] {
             PRIMITIVEGEOFEATURE, AGGREGATEGEOFEATURE, GENERICENTITY /* ENTIT\u00C9G\u00C9N\u00C9RIQUE */
         };
 
     /** the base of the data directory */
     File data;
 
     /** the 'featureTypes' directory, under 'data' */
     File featureTypes;
     
     /** the 'coverages' directory, under 'data' */
     File coverages;
 
     /** the 'styles' directory, under 'data' */
     File styles;
 
     /** the 'plugIns' directory under 'data */
     File plugIns;
 
     /** the 'validation' directory under 'data */
     File validation;
 
     /** the 'templates' director under 'data' */
     File templates;
     
     /** The datastore definition map */
     HashMap dataStores = new HashMap();
     
     /** The datastore to namespace map */
     private HashMap dataStoreNamepaces = new HashMap();
     
     /** The namespaces map */
     private HashMap namespaces = new HashMap();
     
     /** The styles map */
     private HashMap layerStyles = new HashMap();
     
     /** The coverage store map */
     private HashMap coverageStores = new HashMap();
     
     /** The coverage store id to namespace map */
     private HashMap coverageStoresNamespaces = new HashMap();
 
     /**
      * @param base
      *            Base of the GeoServer data directory.
      *
      * @throws IOException
      */
     public MockData() throws IOException {
         // setup the root
         data = File.createTempFile("mock", "data", new File("./target"));
         data.delete();
         data.mkdir();
 
         // create a featureTypes directory
         featureTypes = new File(data, "featureTypes");
         featureTypes.mkdir();
         
         // create a coverages directory
         coverages = new File(data, "coverages");
         coverages.mkdir();
 
         // create the styles directory
         styles = new File(data, "styles");
         styles.mkdir();
         //copy over the minimal style
         copy(MockData.class.getResourceAsStream("Default.sld"), new File(styles, "Default.sld"));
 
         //plugins
         plugIns = new File(data, "plugIns");
         plugIns.mkdir();
 
         //validation
         validation = new File(data, "validation");
         validation.mkdir();
 
         //templates
         templates = new File(data, "templates");
         templates.mkdir();
         
         // setup basic map information
         namespaces.put(DEFAULT_PREFIX, DEFAULT_URI);
         namespaces.put("", DEFAULT_URI);
         layerStyles.put("Default", "Default.sld");
     }
 
     /**
      * @return The root of the data directory.
      */
     public File getDataDirectoryRoot() {
         return data;
     }
 
     /**
      * @return the "featureTypes" directory under the root
      */
     public File getFeatureTypesDirectory() {
         return featureTypes;
     }
     
     /**
      * @return the "coverages" directory under the root
      */
     public File getCoveragesirectory() {
         return coverages;
     }
     
     /**
      * Copies some content to a file under the base of the data directory.
      * <p>
      * The <code>location</code> is considred to be a path relative to the
      * data directory root.
      * </p>
      * <p>
      * Note that the resulting file will be deleted when {@link #tearDown()}
      * is called.
      * </p>
      * @param input The content to copy.
      * @param location A relative path
      */
     public void copyTo(InputStream input, String location)
         throws IOException {
         copy(input, new File(getDataDirectoryRoot(), location));
     }
     
     /**
      * Copies some content to a file udner a specific feature type directory 
      * of the data directory.
      * Example:
      * <p>
      *  <code>
      *    dd.copyToFeautreTypeDirectory(input,MockData.PrimitiveGeoFeature,"info.xml");
      *  </code>
      * </p>
      * @param input The content to copy.
      * @param featureTypeName The name of the feature type.
      * @param location The resulting location to copy to relative to the 
      *  feautre type directory.
      */
     public void copyToFeatureTypeDirectory(InputStream input, QName featureTypeName, String location )
         throws IOException {
         
         copyTo(input, "featureTypes" + File.separator + featureTypeName.getPrefix() 
                 + "_" + featureTypeName.getLocalPart() + File.separator + location );
     }
     
     /**
      * Adds the list of well known types to the data directory. Well known types
      * are listed as constants in the MockData class header, and are organized
      * as arrays based on the cite test they do come from
      * 
      * @param names
      * @throws IOException
      */
     public void addWellKnownTypes(QName[] names) throws IOException {
         for (int i = 0; i < names.length; i++) {
             QName name = names[i];
             
             URL properties = MockData.class.getResource(name.getLocalPart() + ".properties");
             URL style = MockData.class.getResource(name.getLocalPart() + ".sld");
             String styleName = null;
             if(style != null) {
                 styleName = name.getLocalPart();
                 addStyle(styleName, style);
             }
             addPropertiesType(name, properties, styleName);
         }
     }
     
     /**
      * Adds the specified style to the data directory
      * @param styleId the style id
      * @param style an URL pointing to an SLD file to be copied into the data directory
      * @throws IOException
      */
     public void addStyle(String styleId, URL style) throws IOException {
         layerStyles.put(styleId, styleId + ".sld");
         InputStream styleContents = style.openStream();
         File to = new File(styles, styleId + ".sld");
         copy(styleContents, to);
     }
 
     /**
      * Adds a property file as a feature type in a property datastore.
      * 
      * @param name
      *            the fully qualified name of the feature type. The prefix and
      *            namespace URI will be used to create a namespace, the prefix
      *            will be used as the datastore name, the local name will become
      *            the feature type name
      * @param properties
      *            a URL to the property file backing the feature type. If null,
      *            an emtpy property file will be used
      * @param style
      *            a URL to the style that will be associated to the feature
      *            type, or null to use a default one
      * @throws IOException
      */
     public void addPropertiesType(QName name, URL properties, String styleName) throws IOException {
         // setup the type directory if needed
         File directory = new File(data, name.getPrefix());
         if ( !directory.exists() ) {
             directory.mkdir();    
         }
         
         // create the properties file
         File f = new File(directory, name.getLocalPart() + ".properties");
         
         // copy over the contents
         InputStream propertiesContents;
         if(properties == null)
             propertiesContents = new ByteArrayInputStream( "-=".getBytes() );
         else
             propertiesContents = properties.openStream();
         copy( propertiesContents, f );
         
         // write the info file
         info(name, styleName);
         
         // setup the meta information to be written in the catalog 
         namespaces.put(name.getPrefix(), name.getNamespaceURI());
         dataStoreNamepaces.put(name.getPrefix(), name.getPrefix());
         Map params = new HashMap();
         params.put(PropertyDataStoreFactory.DIRECTORY.key, directory);
         params.put(PropertyDataStoreFactory.NAMESPACE.key, name.getNamespaceURI());
         dataStores.put(name.getPrefix(), params);
     }
     
     /**
      * Adds a new coverage.
      *<p>
      * Note that callers of this code should call <code>applicationContext.refresh()</code>
      * in order to force the catalog to reload.
      * </p>
      * <p>
      * The <tt>coverage</tt> parameter is an input stream containing a single uncompressed
      * file that's supposed to be a coverage (e.g., a GeoTiff).
      * </p>
 
      * @param name
      * @param coverage
      */
     public void addCoverage(QName name, URL coverage, String extension, String styleName) throws Exception {
         File directory = new File(data, name.getPrefix());
         if ( !directory.exists() ) {
             directory.mkdir();    
         }
         
        // create the coverage file
         File f = new File(directory, name.getLocalPart() + "." + extension);
         
         // copy over the contents
         copy( coverage.openStream(), f );
         coverageInfo(name, f, styleName);
         
         // setup the meta information to be written in the catalog 
         AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder.findFormat(f);
         namespaces.put(name.getPrefix(), name.getNamespaceURI());
         coverageStoresNamespaces.put(name.getLocalPart(), name.getPrefix());
         Map params = new HashMap();
        params.put(CatalogWriter.COVERAGE_TYPE_KEY, format.getName());
        params.put(CatalogWriter.COVERAGE_URL_KEY, "file:" + name.getPrefix() + "/" + name.getLocalPart() + "." + extension);
         coverageStores.put(name.getLocalPart(), params);
     }
     
     /**
      * Sets up the data directory, creating all the necessary files.
      *
      * @throws IOException
      */
     public void setUpCatalog() throws IOException {
         // create the catalog.xml
         CatalogWriter writer = new CatalogWriter();
         writer.dataStores(dataStores, dataStoreNamepaces);
         writer.coverageStores(coverageStores, coverageStoresNamespaces);
         writer.namespaces(namespaces);
         writer.styles(layerStyles);
         writer.write(new File(data, "catalog.xml"));
     }
 
     void properties(QName name) throws IOException {
         // copy over the properties file
         InputStream from = MockData.class.getResourceAsStream(name.getLocalPart() + ".properties");
         
         File directory = new File(data, name.getPrefix());
         directory.mkdir();
 
         File to = new File(directory, name.getLocalPart() + ".properties");
         copy(from, to);     
     }
 
     void copy(InputStream from, File to) throws IOException {
         OutputStream out = new FileOutputStream(to);
 
         byte[] buffer = new byte[4096];
         int bytes = 0;
         while ((bytes = from.read(buffer)) != -1)
             out.write(buffer, 0, bytes);
 
         from.close();
         out.flush();
         out.close();
     }
 
     void info(QName name, String styleName) throws IOException {
         String type = name.getLocalPart();
         String prefix = name.getPrefix();
 
         File featureTypeDir = new File(featureTypes, prefix + "_" + type);
         featureTypeDir.mkdir();
 
         File info = new File(featureTypeDir, "info.xml");
         info.createNewFile();
 
         FileWriter writer = new FileWriter(info);
         writer.write("<featureType datastore=\"" + prefix + "\">");
         writer.write("<name>" + type + "</name>");
         writer.write("<SRS>4326</SRS>");
         // this mock type may have wrong SRS compared to the actual one in the property files...
         // let's configure SRS handling not to alter the original one, and have 4326 used only
         // for capabilities
         writer.write("<SRSHandling>2</SRSHandling>");
         writer.write("<title>" + type + "</title>");
         writer.write("<abstract>abstract about " + type + "</abstract>");
         writer.write("<numDecimals value=\"8\"/>");
         writer.write("<keywords>" + type + "</keywords>");
         writer.write(
             "<latLonBoundingBox dynamic=\"false\" minx=\"-180\" miny=\"-90\" maxx=\"180\" maxy=\"90\"/>");
 
         if(styleName == null)
             styleName = "Default";
         writer.write("<styles default=\"" + styleName + "\"/>");
 
         writer.write("</featureType>");
 
         writer.flush();
         writer.close();
     }
 
     
     void coverageInfo(QName name, File coverageFile, String styleName) throws Exception {
         String coverage = name.getLocalPart();
 
         File coverageDir = new File(coverages, coverage);
         coverageDir.mkdir();
 
         File info = new File(coverageDir, "info.xml");
         info.createNewFile();
         
         // let's grab the necessary metadata
         AbstractGridFormat format = (AbstractGridFormat) GridFormatFinder.findFormat(coverageFile);
         AbstractGridCoverage2DReader reader = (AbstractGridCoverage2DReader) format.getReader(coverageFile);
 
         // basic info
         FileWriter writer = new FileWriter(info);
         writer.write("<coverage format=\"" + coverage + "\">");
         writer.write("<name>" + coverage + "</name>");
         writer.write("<label>" + coverage + "</label>");
         writer.write("<description>" + coverage + " description</description>");
         // TODO: need to add metadata links?
         writer.write("<keywords>WCS," + coverage + " </keywords>");
         if(styleName == null)
             styleName = "raster";
         writer.write("<styles default=\"" + styleName + "\"/>");
         
         // envelope
         CoordinateReferenceSystem crs = reader.getCrs();
         GeneralEnvelope envelope = reader.getOriginalEnvelope();
         GeneralEnvelope wgs84envelope = CoverageStoreUtils.getWGS84LonLatEnvelope(envelope);
         final String nativeCrsName = CRS.lookupIdentifier(crs, false);
         writer.write("<envelope crs=\"" + crs.toString().replaceAll("\"", "'") + "\" srsName=\"" + nativeCrsName + "\">");
         writer.write("<pos>" + wgs84envelope.getMinimum(0) + " " + wgs84envelope.getMinimum(1) + "</pos>");
         writer.write("<pos>" + wgs84envelope.getMaximum(0) + " " + wgs84envelope.getMaximum(1) + "</pos>");
         writer.write("</envelope>");
         
         // grid geometry
         GridGeometry geometry = new GridGeometry2D(reader.getOriginalGridRange(), reader.getOriginalEnvelope());
         final int dimensions = geometry.getGridRange().getDimension();
         String lower = "";
         String upper = "";
         for(int i = 0; i < dimensions; i++) {
             lower = lower + geometry.getGridRange().getLower(i) + " "; 
             upper = upper + geometry.getGridRange().getUpper(i) + " ";
         }
         writer.write("<grid dimension = \"" + dimensions + "\">");
         writer.write("<low>" + lower + "</low>");
         writer.write("<high>" + upper + "</high>");
         final CoordinateSystem cs = crs.getCoordinateSystem();
         for (int i=0; i < cs.getDimension(); i++) {
             writer.write("<axisName>" + cs.getAxis(i).getName().getCode() + "</axisName>");
         }
         writer.write("</grid>");
         
         // coverage dimensions
         GridSampleDimension[] sampleDimensions = CoverageUtils.getCoverageDimensions(reader);
         for (int i = 0; i < sampleDimensions.length; i++) {
             writer.write("<CoverageDimension>");
             writer.write("<name>" + sampleDimensions[i].getDescription().toString() + "</name>");
             writer.write("<interval>");
             writer.write("<min>" + sampleDimensions[i].getMinimumValue() + "</min>");
             writer.write("<max>" + sampleDimensions[i].getMinimumValue() + "</max>");
             writer.write("</interval>");
             writer.write("<nullValues>");
             for (Iterator it = sampleDimensions[i].getCategories().iterator(); it.hasNext();) {
                 Category cat = (Category) it.next();
 
                 if ((cat != null) && cat.getName().toString().equalsIgnoreCase("no data")) {
                     double min = cat.getRange().getMinimum();
                     double max = cat.getRange().getMaximum();
                     writer.write("<value>" + min + "</value>");
                     if(min != max)
                         writer.write("<value>" + max + "</value>");
                 }
             }
             writer.write("</nullValues>");
             writer.write("</CoverageDimension>");
         }
         
         // supported crs
         writer.write("<supportedCRSs>");
         writer.write("<requestCRSs>" + nativeCrsName + "</requestCRSs>");
         writer.write("<responseCRSs>" + nativeCrsName + "</responseCRSs>");
         writer.write("</supportedCRSs>");
         
         // supported formats
         writer.write("<supportedFormats nativeFormat = \"" + format.getName() + "\">");
         writer.write("<formats>ARCGRID,ARCGRID-GZIP,GEOTIFF,PNG,GIF,TIFF</formats>");
         writer.write("</supportedFormats>");
         
         // supported interpolations
         writer.write("<supportedInterpolations default = \"nearest neighbor\">");
         writer.write("<interpolationMethods>nearest neighbor</interpolationMethods>");
         writer.write("</supportedInterpolations>");
         
         // the end
         writer.write("</coverage>");
         writer.flush();
         writer.close();
     }
 
     /**
      * Kills the data directory, deleting all the files.
      *
      * @throws IOException
      */
     public void tearDown() throws IOException {
         delete(templates);
         delete(validation);
         delete(plugIns);
         delete(styles);
         delete(featureTypes);
         delete(coverages);
         delete(data);
 
         styles = null;
         featureTypes = null;
         data = null;
     }
 
     void delete(File dir) throws IOException {
         File[] files = dir.listFiles();
 
         for (int i = 0; i < files.length; i++) {
             if (files[i].isDirectory()) {
                 delete(files[i]);
             } else {
                 if(!files[i].delete())
                     System.out.println("Could not delete " + files[i].getAbsolutePath());
             }
         }
 
         dir.delete();
     }
 }
