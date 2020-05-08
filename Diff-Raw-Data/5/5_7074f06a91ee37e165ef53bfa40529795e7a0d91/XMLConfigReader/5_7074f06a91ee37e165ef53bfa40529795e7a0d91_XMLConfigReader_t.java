 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global.xml;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.apache.xml.serialize.LineSeparator;
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.geotools.filter.FilterDOMParser;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.Log4JFormatter;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.ContactDTO;
 import org.vfny.geoserver.global.dto.DataDTO;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 import org.vfny.geoserver.global.dto.FeatureTypeInfoDTO;
 import org.vfny.geoserver.global.dto.GeoServerDTO;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 import org.vfny.geoserver.global.dto.ServiceDTO;
 import org.vfny.geoserver.global.dto.StyleDTO;
 import org.vfny.geoserver.global.dto.WFSDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * XMLConfigReader purpose.
  * 
  * <p>
  * Description of XMLConfigReader  Static class to load a configuration
  * org.vfny.geoserver.global.dto
  * </p>
  * 
  * <p>
  * Example Use:
  * <pre><code>
  * ModelConfig m = XMLConfigReader.load(new File("/conf/"));
  * </code></pre>
  * </p>
  *
  * @author dzwiers, Refractions Research, Inc.
 * @version $Id: XMLConfigReader.java,v 1.27 2004/02/03 00:13:46 emperorkefka Exp $
  */
 public class XMLConfigReader {
     /** Used internally to create log information to detect errors. */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.global");
 
     /** The root directory from which the configuration is loaded. */
     private File root;
 
     /** Is set to true after the model is loaded into memory. */
     private boolean initialized = false;
     private WMSDTO wms;
     private WFSDTO wfs;
     private GeoServerDTO geoServer;
     private DataDTO data;
 
     /**
      * XMLConfigReader constructor.
      * 
      * <p>
      * Should never be called.
      * </p>
      */
     protected XMLConfigReader() {
         wms = new WMSDTO();
         wfs = new WFSDTO();
         geoServer = new GeoServerDTO();
         data = new DataDTO();
         root = new File(".");
     }
 
     /**
      * <p>
      * This method loads the config files from the  specified directory into a
      * ModelConfig. If the path is incorrect,  or the directory is formed
      * correctly, a ConfigException  will be thrown and/or null returned. <br>
      * <br>
      * The config directory is as follows:<br>
      * 
      * <ul>
      * <li>
      * ./WEB-INF/catalog.xml
      * </li>
      * <li>
      * ./WEB-INF/services.xml
      * </li>
      * <li>
      * ./data/featuretypes/  /info.xml
      * </li>
      * <li>
      * ./data/featuretypes/  /schema.xml
      * </li>
      * </ul>
      * </p>
      *
      * @param root A directory which contains the config files.
      *
      * @throws ConfigurationException When an error occurs.
      */
     public XMLConfigReader(File root) throws ConfigurationException {
         this.root = root;
         wms = new WMSDTO();
         wfs = new WFSDTO();
         geoServer = new GeoServerDTO();
         data = new DataDTO();
         load();
         initialized = true;
     }
 
     public boolean isInitialized() {
         return initialized;
     }
 
     /**
      * load purpose.
      * 
      * <p>
      * Main load routine, sets up file handles for various other portions of
      * the load procedure.
      * </p>
      *
      * @throws ConfigurationException
      */
     protected void load() throws ConfigurationException {
         root = ReaderUtils.checkFile(root, true);
 
         File configDir = ReaderUtils.checkFile(new File(root, "WEB-INF/"), true);
         File configFile = ReaderUtils.checkFile(new File(configDir,
                     "services.xml"), false);
 
         loadServices(configFile);
 
         File catalogFile = ReaderUtils.checkFile(new File(configDir,
                     "catalog.xml"), false);
         File dataDir = ReaderUtils.checkFile(new File(root, "data/"), true);
         File featureTypeDir = ReaderUtils.checkFile(new File(dataDir,
                     "featureTypes/"), true);
         loadCatalog(catalogFile, featureTypeDir);
 
         // Future additions
         // validationDir = ReaderUtils.initFile(new File(dataDir,"validation/"),true);
         // loadValidation(validationDir);	
     }
 
     /**
      * loadServices purpose.
      * 
      * <p>
      * loads services.xml into memory with the assistance of other class
      * methods.
      * </p>
      *
      * @param configFile services.xml
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected void loadServices(File configFile) throws ConfigurationException {
         LOGGER.fine("loading config file: " + configFile);
 
         Element configElem = null;
 
         try {
         	FileReader fr = new FileReader(configFile);
             configElem = ReaderUtils.loadConfig(fr);
             fr.close();
         } catch (FileNotFoundException e) {
             throw new ConfigurationException(e);
         } catch (IOException e) {
         	throw new ConfigurationException(e);
         }
 
         LOGGER.fine("parsing configuration documents");
 
         Element elem = (Element) configElem.getElementsByTagName("global").item(0);
         loadGlobal(elem);
 
         NodeList configuredServices = configElem.getElementsByTagName("service");
         int nServices = configuredServices.getLength();
 
         for (int i = 0; i < nServices; i++) {
             elem = (Element) configuredServices.item(i);
 
             String serviceType = elem.getAttribute("type");
 
             if ("WFS".equalsIgnoreCase(serviceType)) {
                 loadWFS(elem);
             } else if ("WMS".equalsIgnoreCase(serviceType)) {
                 loadWMS(elem);
             } else if ("Z39.50".equalsIgnoreCase(serviceType)) {
                 //...
             } else {
                 throw new ConfigurationException("Unknown service type: "
                     + serviceType);
             }
         }
     }
 
     /**
      * loadCatalog purpose.
      * 
      * <p>
      * loads catalog.xml into memory with the assistance of other class
      * methods.
      * </p>
      *
      * @param catalogFile catalog.xml
      * @param featureTypeDir the directory containing the info.xml files for
      *        the featuretypes.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected void loadCatalog(File catalogFile, File featureTypeDir)
         throws ConfigurationException {
         LOGGER.fine("loading catalog file: " + catalogFile);
 
         Element catalogElem = null;
 
         try {
         	FileReader fr = new FileReader(catalogFile);
             catalogElem = ReaderUtils.loadConfig(fr);
             fr.close();
         } catch (FileNotFoundException e) {
             throw new ConfigurationException(e);
         } catch (IOException e) {
         	throw new ConfigurationException(e);
         }
 
         LOGGER.info("loading catalog configuration");
         data.setNameSpaces(loadNameSpaces(ReaderUtils.getChildElement(
                     catalogElem, "namespaces", true)));
         setDefaultNS();
         data.setDataStores(loadDataStores(ReaderUtils.getChildElement(
                     catalogElem, "datastores", true)));
         data.setStyles(loadStyles(ReaderUtils.getChildElement(catalogElem,
                     "styles", false),
                 new File(featureTypeDir.getParentFile(), "styles")));
 
         // must be last
         data.setFeaturesTypes(loadFeatureTypes(featureTypeDir));
     }
 
     /**
      * setDefaultNS purpose.
      * 
      * <p>
      * Finds and sets the default namespace. The namespaces in catalog must
      * already be loaded.
      * </p>
      */
     protected void setDefaultNS() {
         Iterator i = data.getNameSpaces().values().iterator();
 
         while (i.hasNext()) {
             NameSpaceInfoDTO ns = (NameSpaceInfoDTO) i.next();
 
             if (ns.isDefault()) {
                 data.setDefaultNameSpacePrefix(ns.getPrefix());
 
                 return;
             }
         }
     }
 
     /**
      * getLoggingLevel purpose.
      * 
      * <p>
      * Parses the LoggingLevel from a DOM tree and converts the level into a
      * Level Object.
      * </p>
      *
      * @param globalConfigElem
      *
      * @return The logging Level
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected Level getLoggingLevel(Element globalConfigElem)
         throws ConfigurationException {
         Level level = Logger.getLogger("org.vfny.geoserver").getLevel();
         Element levelElem = ReaderUtils.getChildElement(globalConfigElem,
                 "loggingLevel");
 
         if (levelElem != null) {
             String levelName = levelElem.getFirstChild().getNodeValue();
 
             try {
                 level = Level.parse(levelName);
             } catch (IllegalArgumentException ex) {
                 LOGGER.warning("illegal loggingLevel name: " + levelName);
             }
         } else {
             LOGGER.config("No loggingLevel found, using default "
                 + "logging.properties setting");
         }
 
         return level;
     }
 
     /**
      * loadGlobal purpose.
      * 
      * <p>
      * Converts a DOM tree into a GlobalData configuration.
      * </p>
      *
      * @param globalElem A DOM tree representing a complete global
      *        configuration.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected void loadGlobal(Element globalElem) throws ConfigurationException {
         geoServer = new GeoServerDTO();
         LOGGER.fine("parsing global configuration parameters");
 
         Level loggingLevel = getLoggingLevel(globalElem);
 
         //init this now so the rest of the config has correct log levels.
         Log4JFormatter.init("org.geotools", loggingLevel);
         Log4JFormatter.init("org.vfny.geoserver", loggingLevel);
         LOGGER.config("logging level is " + loggingLevel);
         geoServer.setLoggingLevel(loggingLevel);
 
         Element elem = null;
         elem = ReaderUtils.getChildElement(globalElem, "ContactInformation");
         geoServer.setContact(loadContact(elem));
 
         elem = ReaderUtils.getChildElement(globalElem, "verbose", false);
 
         if (elem != null) {
             geoServer.setVerbose(ReaderUtils.getBooleanAttribute(elem, "value",
                     false));
         }
 
         elem = ReaderUtils.getChildElement(globalElem, "maxFeatures");
 
         if (elem != null) {
             //if the element is pressent, it's "value" attribute is mandatory
             geoServer.setMaxFeatures(ReaderUtils.getIntAttribute(elem, "value",
                     true, geoServer.getMaxFeatures()));
         }
 
         LOGGER.config("maxFeatures is " + geoServer.getMaxFeatures());
         elem = ReaderUtils.getChildElement(globalElem, "numDecimals");
 
         if (elem != null) {
             geoServer.setNumDecimals(ReaderUtils.getIntAttribute(elem, "value",
                     true, geoServer.getNumDecimals()));
         }
 
         LOGGER.config("numDecimals returning is " + geoServer.getNumDecimals());
         elem = ReaderUtils.getChildElement(globalElem, "charSet");
 
         if (elem != null) {
             String chSet = ReaderUtils.getAttribute(elem, "value", true);
 
             try {
                 Charset cs = Charset.forName(chSet);
                 geoServer.setCharSet(cs);
                 LOGGER.finer("charSet: " + cs.displayName());
             } catch (Exception ex) {
                 LOGGER.info(ex.getMessage());
             }
         }
 
         LOGGER.config("charSet is " + geoServer.getCharSet());
 
         //geoServer.setBaseUrl(ReaderUtils.getChildText(globalElem, "URL", true));
         String schemaBaseUrl = ReaderUtils.getChildText(globalElem,
                 "SchemaBaseUrl");
 
         if (schemaBaseUrl != null) {
             geoServer.setSchemaBaseUrl(schemaBaseUrl);
         } else {
             geoServer.setSchemaBaseUrl(root.toString() + "/data/capabilities/");
         }
     }
 
     /**
      * loadContact purpose.
      * 
      * <p>
      * Converts a DOM tree into a ContactConfig
      * </p>
      *
      * @param contactInfoElement a DOM tree to convert into a ContactConfig.
      *
      * @return The resulting ContactConfig object from the DOM tree.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected ContactDTO loadContact(Element contactInfoElement)
         throws ConfigurationException {
         ContactDTO c = new ContactDTO();
 
         if (contactInfoElement == null) {
             return c;
         }
 
         Element elem;
         NodeList nodeList;
         elem = ReaderUtils.getChildElement(contactInfoElement,
                 "ContactPersonPrimary");
 
         if (elem != null) {
             c.setContactPerson(ReaderUtils.getChildText(elem, "ContactPerson"));
             c.setContactOrganization(ReaderUtils.getChildText(elem,
                     "ContactOrganization"));
         }
 
         c.setContactPosition(ReaderUtils.getChildText(contactInfoElement,
                 "ContactPosition"));
         elem = ReaderUtils.getChildElement(contactInfoElement, "ContactAddress");
 
         if (elem != null) {
             c.setAddressType(ReaderUtils.getChildText(elem, "AddressType"));
             c.setAddress(ReaderUtils.getChildText(elem, "Address"));
             c.setAddressCity(ReaderUtils.getChildText(elem, "City"));
             c.setAddressState(ReaderUtils.getChildText(elem, "StateOrProvince"));
             c.setAddressPostalCode(ReaderUtils.getChildText(elem, "PostCode"));
             c.setAddressCountry(ReaderUtils.getChildText(elem, "Country"));
         }
 
         c.setContactVoice(ReaderUtils.getChildText(contactInfoElement,
                 "ContactVoiceTelephone"));
         c.setContactFacsimile(ReaderUtils.getChildText(contactInfoElement,
                 "ContactFacsimileTelephone"));
         c.setContactEmail(ReaderUtils.getChildText(contactInfoElement,
                 "ContactElectronicMailAddress"));
 
         return c;
     }
 
     /**
      * loadWFS purpose.
      * 
      * <p>
      * Converts a DOM tree into a WFS object.
      * </p>
      *
      * @param wfsElement a DOM tree to convert into a WFS object.
      *
      * @throws ConfigurationException When an error occurs.
      *
      * @see GlobalData#getBaseUrl()
      */
     protected void loadWFS(Element wfsElement) throws ConfigurationException {
         wfs = new WFSDTO();
 
         try {
             wfs.setGmlPrefixing(ReaderUtils.getBooleanAttribute(
                     ReaderUtils.getChildElement(wfsElement, "gmlPrefixing"),
                     "value", false));
         } catch (Exception e) {
         }
 
         ServiceDTO s = loadService(wfsElement);
         wfs.setService(s);
     }
 
     /**
      * loadWMS purpose.
      * 
      * <p>
      * Converts a DOM tree into a WMS object.
      * </p>
      *
      * @param wmsElement a DOM tree to convert into a WMS object.
      *
      * @throws ConfigurationException When an error occurs.
      *
      * @see GlobalData#getBaseUrl()
      */
     protected void loadWMS(Element wmsElement) throws ConfigurationException {
         wms = new WMSDTO();
         wms.setService(loadService(wmsElement));
     }
 
     /**
      * loadService purpose.
      * 
      * <p>
      * Converts a DOM tree into a ServiceDTO object.
      * </p>
      *
      * @param serviceRoot a DOM tree to convert into a ServiceDTO object.
      *
      * @return A complete ServiceDTO object loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected ServiceDTO loadService(Element serviceRoot)
         throws ConfigurationException {
         ServiceDTO s = new ServiceDTO();
 
         s.setName(ReaderUtils.getChildText(serviceRoot, "name", true));
         s.setTitle(ReaderUtils.getChildText(serviceRoot, "title", false));
         s.setAbstract(ReaderUtils.getChildText(serviceRoot, "abstract"));
         s.setKeywords(ReaderUtils.getKeyWords(ReaderUtils.getChildElement(
                     serviceRoot, "keywords")));
         s.setFees(ReaderUtils.getChildText(serviceRoot, "fees"));
         s.setAccessConstraints(ReaderUtils.getChildText(serviceRoot,
                 "accessConstraints"));
         s.setMaintainer(ReaderUtils.getChildText(serviceRoot, "maintainer"));
         s.setEnabled(ReaderUtils.getBooleanAttribute(serviceRoot, "enabled",
                 false));
 
         try {
             s.setOnlineResource(new URL(ReaderUtils.getChildText(serviceRoot,
                         "onlineResource", true)));
         } catch (MalformedURLException e) {
             throw new ConfigurationException(e);
         }
 
         return s;
     }
 
     /**
      * loadNameSpaces purpose.
      * 
      * <p>
      * Converts a DOM tree into a Map of NameSpaces.
      * </p>
      *
      * @param nsRoot a DOM tree to convert into a Map of NameSpaces.
      *
      * @return A complete Map of NameSpaces loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected Map loadNameSpaces(Element nsRoot) throws ConfigurationException {
         NodeList nsList = nsRoot.getElementsByTagName("namespace");
         Element elem;
         int nsCount = nsList.getLength();
         Map nameSpaces = new HashMap(nsCount);
 
         for (int i = 0; i < nsCount; i++) {
             elem = (Element) nsList.item(i);
 
             NameSpaceInfoDTO ns = new NameSpaceInfoDTO();
             ns.setUri(ReaderUtils.getAttribute(elem, "uri", true));
             ns.setPrefix(ReaderUtils.getAttribute(elem, "prefix", true));
             ns.setDefault(ReaderUtils.getBooleanAttribute(elem, "default", false)
                 || (nsCount == 1));
             LOGGER.config("added namespace " + ns);
             nameSpaces.put(ns.getPrefix(), ns);
         }
 
         return nameSpaces;
     }
 
     /**
      * loadStyles purpose.
      * 
      * <p>
      * Converts a DOM tree into a Map of Styles.
      * </p>
      *
      * @param stylesElem a DOM tree to convert into a Map of Styles.
      * @param baseDir DOCUMENT ME!
      *
      * @return A complete Map of Styles loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected Map loadStyles(Element stylesElem, File baseDir)
         throws ConfigurationException {
         Map styles = new HashMap();
 
         NodeList stylesList = null;
 
         if (stylesElem != null) {
             stylesList = stylesElem.getElementsByTagName("style");
         }
 
         if ((stylesList == null) || (stylesList.getLength() == 0)) {
             //no styles where defined, just add a default one
             StyleDTO s = new StyleDTO();
             s.setId("normal");
             s.setFilename(new File(baseDir, "normal.sld"));
             s.setDefault(true);
             styles.put("normal", s);
         }
 
         int styleCount = stylesList.getLength();
         Element styleElem;
 
         for (int i = 0; i < styleCount; i++) {
             styleElem = (Element) stylesList.item(i);
 
             StyleDTO s = new StyleDTO();
             s.setId(ReaderUtils.getAttribute(styleElem, "id", true));
             s.setFilename(new File(baseDir,
                     ReaderUtils.getAttribute(styleElem, "filename", true)));
             s.setDefault(ReaderUtils.getBooleanAttribute(styleElem, "default",
                     false));
             styles.put(s.getId(), s);
         }
 
         return styles;
     }
 
     /**
      * loadDataStores purpose.
      * 
      * <p>
      * Converts a DOM tree into a Map of DataStores.
      * </p>
      *
      * @param dsRoot a DOM tree to convert into a Map of DataStores.
      *
      * @return A complete Map of DataStores loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected Map loadDataStores(Element dsRoot) throws ConfigurationException {
         Map dataStores = new HashMap();
 
         NodeList dsElements = dsRoot.getElementsByTagName("datastore");
         int dsCnt = dsElements.getLength();
         DataStoreInfoDTO dsConfig;
         Element dsElem;
 
         for (int i = 0; i < dsCnt; i++) {
             dsElem = (Element) dsElements.item(i);
             dsConfig = loadDataStore(dsElem);
 
             if (dataStores.containsKey(dsConfig.getId())) {
                 throw new ConfigurationException("duplicated datastore id: "
                     + data.getNameSpaces().get(dsConfig.getNameSpaceId()));
             }
 
             dataStores.put(dsConfig.getId(), dsConfig);
         }
 
         return dataStores;
     }
 
     /**
      * loadDataStore purpose.
      * 
      * <p>
      * Converts a DOM tree into a DataStoreInfo object.
      * </p>
      *
      * @param dsElem a DOM tree to convert into a DataStoreInfo object.
      *
      * @return A complete DataStoreInfo object loaded from the DOM tree
      *         provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected DataStoreInfoDTO loadDataStore(Element dsElem)
         throws ConfigurationException {
         DataStoreInfoDTO ds = new DataStoreInfoDTO();
 
         LOGGER.finer("creating a new DataStoreDTO configuration");
         ds.setId(ReaderUtils.getAttribute(dsElem, "id", true));
 
         String namespacePrefix = ReaderUtils.getAttribute(dsElem, "namespace",
                 true);
 
         if (data.getNameSpaces().containsKey(namespacePrefix)) {
             ds.setNameSpaceId(namespacePrefix);
         } else {
             String msg = "there is no namespace defined for datatasore '"
                 + namespacePrefix + "'";
             throw new ConfigurationException(msg);
         }
 
         ds.setEnabled(ReaderUtils.getBooleanAttribute(dsElem, "enabled", false));
         ds.setTitle(ReaderUtils.getChildText(dsElem, "title", false));
        ds.setAbstract(ReaderUtils.getChildText(dsElem, "description", false));
         LOGGER.fine("loading connection parameters for DataStoreDTO "
             + ds.getNameSpaceId());
         ds.setConnectionParams(loadConnectionParams(ReaderUtils.getChildElement(
                     dsElem, "connectionParams", true)));
 
         return ds;
     }
 
     /**
      * loadConnectionParams purpose.
      * 
      * <p>
      * Converts a DOM tree into a Map of Strings which represent connection
      * parameters.
      * </p>
      *
      * @param connElem a DOM tree to convert into a Map of Strings which
      *        represent connection parameters.
      *
      * @return A complete Map of Strings which represent connection parameters
      *         loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected Map loadConnectionParams(Element connElem)
         throws ConfigurationException {
         Map connectionParams = new HashMap();
 
         NodeList paramElems = connElem.getElementsByTagName("parameter");
         int pCount = paramElems.getLength();
         Element param;
         String paramKey;
         String paramValue;
 
         for (int i = 0; i < pCount; i++) {
             param = (Element) paramElems.item(i);
             paramKey = ReaderUtils.getAttribute(param, "name", true);
             paramValue = ReaderUtils.getAttribute(param, "value", true);
             connectionParams.put(paramKey, paramValue);
             LOGGER.finer("added parameter " + paramKey + ": '" + paramValue
                 + "'");
         }
 
         return connectionParams;
     }
 
     /**
      * Load map of FeatureTypeDTO instances from a directory.
      * 
      * <p>
      * Expected directory structure:
      * </p>
      * 
      * <ul>
      * <li>
      * rootDir/
      * </li>
      * <li>
      * rootDir/featureType1/info.xml - required
      * </li>
      * <li>
      * rootDir/featureType1/schema.xml - optional
      * </li>
      * <li>
      * rootDir/featureType2/info.xml - required
      * </li>
      * <li>
      * rootDir/featureType2/schema.xml - optional
      * </li>
      * </ul>
      * 
      * <p>
      * If a schema.xml file is not used, the information may be generated from
      * a FeatureType using DataTransferObjectFactory.
      * </p>
      *
      * @param featureTypeRoot Root FeatureType directory
      *
      * @return Map of FeatureTypeInfoDTO by <code>dataStoreId:typeName</code>
      *
      * @throws ConfigurationException When an error occurs.
      * @throws IllegalArgumentException DOCUMENT ME!
      */
     protected Map loadFeatureTypes(File featureTypeRoot)
         throws ConfigurationException {
         LOGGER.finest("examining: " + featureTypeRoot.getAbsolutePath());
         LOGGER.finest("is dir: " + featureTypeRoot.isDirectory());
 
         if (!featureTypeRoot.isDirectory()) {
             throw new IllegalArgumentException(
                 "featureTypeRoot must be a directoy");
         }
 
         File[] directories = featureTypeRoot.listFiles(new FileFilter() {
                     public boolean accept(File pathname) {
                         return pathname.isDirectory();
                     }
                 });
 
         Map map = new HashMap();
 
         for (int i = 0, n = directories.length; i < n; i++) {
             File info = new File(directories[i], "info.xml");
 
             if (info.exists() && info.isFile()) {
                 LOGGER.fine("Info dir:" + info);
 
                 FeatureTypeInfoDTO dto = loadFeature(info);
                 map.put(dto.getKey(), dto);
             }
         }
 
         return map;
     }
 
     /**
      * Load FeatureTypeInfoDTO from a directory.
      * 
      * <p>
      * Expected directory structure:
      * </p>
      * 
      * <ul>
      * <li>
      * info.xml - required
      * </li>
      * <li>
      * schema.xml - optional
      * </li>
      * </ul>
      * 
      * <p>
      * If a schema.xml file is not used, the information may be generated from
      * a FeatureType using DataTransferObjectFactory.
      * </p>
      *
      * @param infoFile a File to convert into a FeatureTypeInfo object.
      *        (info.xml)
      *
      * @return A complete FeatureTypeInfo object loaded from the File handle
      *         provided.
      *
      * @throws ConfigurationException When an error occurs.
      * @throws IllegalArgumentException DOCUMENT ME!
      *
      * @see loadFeaturePt2(Element)
      */
     protected FeatureTypeInfoDTO loadFeature(File infoFile)
         throws ConfigurationException {
         if (!infoFile.exists()) {
             throw new IllegalArgumentException("Info File not found:"
                 + infoFile);
         }
 
         if (!infoFile.isFile()) {
             throw new IllegalArgumentException("Info file is the wrong type:"
                 + infoFile);
         }
 
         if (!isInfoFile(infoFile)) {
             throw new IllegalArgumentException("Info File not valid:"
                 + infoFile);
         }
 
         Element featureElem = null;
 
 
         try {
         	Reader reader = null;
         	reader = new FileReader(infoFile);
             featureElem = ReaderUtils.loadConfig(reader);
             reader.close();
         } catch (FileNotFoundException fileNotFound) {
         	throw new ConfigurationException("Could not read info file:"
         			+ infoFile, fileNotFound);
         } catch (Exception erk) {
             throw new ConfigurationException("Could not parse info file:"
                 + infoFile, erk);
         }
 
         FeatureTypeInfoDTO dto = loadFeaturePt2(featureElem);
 
         File parentDir = infoFile.getParentFile();
         dto.setDirName(parentDir.getName());
 
         List attributeList;
 
         File schemaFile = new File(parentDir, "schema.xml");
 
         if (schemaFile.exists() && schemaFile.isFile()) {
             // attempt to load optional schema information
             //
             LOGGER.finest("process schema file " + infoFile);
 
             try {
                 loadSchema(schemaFile, dto);
             } catch (Exception badDog) {
                 badDog.printStackTrace();
                 attributeList = Collections.EMPTY_LIST;
             }
         } else {
             dto.setSchemaAttributes(Collections.EMPTY_LIST);
         }
 
         LOGGER.finer("added featureType " + dto.getName());
 
         return dto;
     }
 
     /**
      * loadFeaturePt2 purpose.
      * 
      * <p>
      * Converts a DOM tree into a FeatureTypeInfo object.
      * </p>
      *
      * @param fTypeRoot a DOM tree to convert into a FeatureTypeInfo object.
      *
      * @return A complete FeatureTypeInfo object loaded from the DOM tree
      *         provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected FeatureTypeInfoDTO loadFeaturePt2(Element fTypeRoot)
         throws ConfigurationException {
         FeatureTypeInfoDTO ft = new FeatureTypeInfoDTO();
 
         ft.setName(ReaderUtils.getChildText(fTypeRoot, "name", true));
         ft.setTitle(ReaderUtils.getChildText(fTypeRoot, "title", true));
         ft.setAbstract(ReaderUtils.getChildText(fTypeRoot, "abstract"));
 
         String keywords = ReaderUtils.getChildText(fTypeRoot, "keywords");
         List l = new LinkedList();
         String[] ss = keywords.split(",");
 
         for (int i = 0; i < ss.length; i++)
             l.add(ss[i].trim());
 
         ft.setKeywords(l);
         ft.setDataStoreId(ReaderUtils.getAttribute(fTypeRoot, "datastore", true));
         ft.setSRS(Integer.parseInt(ReaderUtils.getChildText(fTypeRoot, "SRS",
                     true)));
 
         Element tmp = ReaderUtils.getChildElement(fTypeRoot, "styles");
 
         if (tmp != null) {
             ft.setDefaultStyle(ReaderUtils.getAttribute(tmp, "default", false));
         }
 
         ft.setLatLongBBox(loadLatLongBBox(ReaderUtils.getChildElement(
                     fTypeRoot, "latLonBoundingBox")));
 
         Element numDecimalsElem = ReaderUtils.getChildElement(fTypeRoot,
                 "numDecimals", false);
 
         if (numDecimalsElem != null) {
             ft.setNumDecimals(ReaderUtils.getIntAttribute(numDecimalsElem,
                     "value", false, 8));
         }
 
         ft.setDefinitionQuery(loadDefinitionQuery(fTypeRoot));
 
         return ft;
     }
 
     /**
      * getKeyWords purpose.
      * 
      * <p>
      * Converts a DOM tree into a List of Strings representing keywords.
      * </p>
      *
      * @param keywordsElem a DOM tree to convert into a List of Strings
      *        representing keywords.
      *
      * @return A complete List of Strings representing keywords loaded from the
      *         DOM tree provided.
      */
     protected List getKeyWords(Element keywordsElem) {
         NodeList klist = keywordsElem.getElementsByTagName("keyword");
         int kCount = klist.getLength();
         List keywords = new LinkedList();
         String kword;
         Element kelem;
 
         for (int i = 0; i < kCount; i++) {
             kelem = (Element) klist.item(i);
             kword = ReaderUtils.getElementText(kelem);
 
             if (kword != null) {
                 keywords.add(kword);
             }
         }
 
         return keywords;
     }
 
     /**
      * loadLatLongBBox purpose.
      * 
      * <p>
      * Converts a DOM tree into a Envelope object.
      * </p>
      *
      * @param bboxElem a DOM tree to convert into a Envelope object.
      *
      * @return A complete Envelope object loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected Envelope loadLatLongBBox(Element bboxElem)
         throws ConfigurationException {
         boolean dynamic = ReaderUtils.getBooleanAttribute(bboxElem, "dynamic",
                 false);
 
         if (!dynamic) {
             double minx = ReaderUtils.getDoubleAttribute(bboxElem, "minx", true);
             double miny = ReaderUtils.getDoubleAttribute(bboxElem, "miny", true);
             double maxx = ReaderUtils.getDoubleAttribute(bboxElem, "maxx", true);
             double maxy = ReaderUtils.getDoubleAttribute(bboxElem, "maxy", true);
 
             return new Envelope(minx, maxx, miny, maxy);
         }
 
         return new Envelope();
     }
 
     /**
      * loadDefinitionQuery purpose.
      * 
      * <p>
      * Converts a DOM tree into a Filter object.
      * </p>
      *
      * @param typeRoot a DOM tree to convert into a Filter object.
      *
      * @return A complete Filter object loaded from the DOM tree provided.
      *
      * @throws ConfigurationException When an error occurs.
      */
     protected org.geotools.filter.Filter loadDefinitionQuery(Element typeRoot)
         throws ConfigurationException {
         Element defQNode = ReaderUtils.getChildElement(typeRoot,
                 "definitionQuery", false);
         org.geotools.filter.Filter filter = null;
 
         if (defQNode != null) {
             LOGGER.fine("definitionQuery element found, looking for Filter");
 
             Element filterNode = ReaderUtils.getChildElement(defQNode,
                     "Filter", false);
 
             if ((filterNode != null)
                     && ((filterNode = ReaderUtils.getFirstChildElement(
                             filterNode)) != null)) {
                 filter = FilterDOMParser.parseFilter(filterNode);
 
                 return filter;
             }
 
             LOGGER.fine("No Filter definition query found");
         }
 
         return filter;
     }
 
     /**
      * isInfoFile purpose.
      * 
      * <p>
      * Used to perform safety checks on info.xml file handles.
      * </p>
      *
      * @param testFile The file to test.
      *
      * @return true if the file is an info.xml file.
      */
     protected static boolean isInfoFile(File testFile) {
         String testName = testFile.getAbsolutePath();
 
         int start = testName.length() - "info.xml".length();
         int end = testName.length();
 
         return testName.substring(start, end).equals("info.xml");
     }
 
     /**
      * Process schema File for a list of AttributeTypeInfoDTO.
      * 
      * <p>
      * The provided FeatureTypeInfoDTO will be updated with the schemaBase.
      * </p>
      *
      * @param schemaFile File containing schema definition
      * @param dto Schema DOM element
      *
      * @throws ConfigurationException
      */
     protected void loadSchema(File schemaFile, FeatureTypeInfoDTO dto)
         throws ConfigurationException {
         schemaFile = ReaderUtils.checkFile(schemaFile, false);
 
         Element elem = null;
         if(schemaFile == null || (!schemaFile.exists() || !schemaFile.canRead())){
         	System.err.println("File does not exist for schema for "+dto.getName());
         	return;
         }
         try {
         	Reader reader;
         	reader = new FileReader(schemaFile);
             elem = ReaderUtils.loadConfig(reader);
             reader.close();
         } catch (FileNotFoundException e) {
         	LOGGER.log(Level.FINEST, e.getMessage(), e);
         	throw new ConfigurationException("Could not open schmea file:"
         			+ schemaFile, e);
         } catch (Exception erk) {
             throw new ConfigurationException("Could not parse schema file:"
                 + schemaFile, erk);
         }
         try{
         	processSchema(elem, dto);
         }catch(ConfigurationException e){
         	throw new ConfigurationException("Error occured in "+schemaFile+"\n"+e.getMessage(),e);
         }
     }
 
     /**
      * Process schema DOM for a list of AttributeTypeInfoDTO.
      * 
      * <p>
      * The provided FeatureTypeInfoDTO will be updated with the schemaBase.
      * </p>
      *
      * @param elem Schema DOM element
      * @param featureTypeInfoDTO
      *
      * @throws ConfigurationException
      */
     public static void processSchema(Element elem,
         FeatureTypeInfoDTO featureTypeInfoDTO) throws ConfigurationException {
         ArrayList list = new ArrayList();
 
         featureTypeInfoDTO.setSchemaName(ReaderUtils.getAttribute(elem, "name",
                 true));
 
         elem = ReaderUtils.getChildElement(elem, "xs:complexContent");
         elem = ReaderUtils.getChildElement(elem, "xs:extension");
         featureTypeInfoDTO.setSchemaBase(ReaderUtils.getAttribute(elem, "base",
                 true));
         elem = ReaderUtils.getChildElement(elem, "xs:sequence");
 
         NodeList nl = elem.getElementsByTagName("xs:element");
 
         for (int i = 0; i < nl.getLength(); i++) {
             // one element now
             elem = (Element) nl.item(i);
 
             AttributeTypeInfoDTO ati = new AttributeTypeInfoDTO();
             String name = ReaderUtils.getAttribute(elem, "name", false);
             String ref = ReaderUtils.getAttribute(elem, "ref", false);
             String type = ReaderUtils.getAttribute(elem, "type", false);
 
             if ((ref != null) && (ref != "")) {
                 ati.setComplex(false);
 
                 String tmp = GMLUtils.type(ref).reference;
                 tmp = Character.toLowerCase(tmp.charAt(0)) + tmp.substring(1);
                 ati.setType(tmp);
                 tmp = GMLUtils.type(ref).name;
                 tmp = Character.toLowerCase(tmp.charAt(0)) + tmp.substring(1);
                 ati.setName(tmp);
             } else {
                 ati.setName(name);
 
                 if ((type != null) && (type != "")) {
                     int t = type.indexOf(":");
 
                     if (t != -1) {
                         type = type.substring(t + 1);
                     }
 
                     ati.setType(type);
                     ati.setComplex(false);
                 } else {
                     Element tmp = ReaderUtils.getFirstChildElement(elem);
                     OutputFormat format = new OutputFormat(tmp.getOwnerDocument());
                     format.setLineSeparator(LineSeparator.Windows);
                     format.setIndenting(true);
                     format.setLineWidth(0);
                     format.setPreserveSpace(true);
 
                     StringWriter sw = new StringWriter();
                     XMLSerializer serializer = new XMLSerializer(sw, format);
 
                     try {
                         serializer.asDOMSerializer();
                         serializer.serialize(tmp);
                     } catch (IOException e) {
                         throw new ConfigurationException(e);
                     }
 
                     ati.setType(elem.toString());
                     ati.setComplex(true);
                 }
             }
 
             ati.setNillable(ReaderUtils.getBooleanAttribute(elem, "nillable",
                     false));
             ati.setMaxOccurs(ReaderUtils.getIntAttribute(elem, "maxOccurs",
                     false, 1));
             ati.setMinOccurs(ReaderUtils.getIntAttribute(elem, "minOccurs",
                     false, 0));
             list.add(ati);
         }
 
         featureTypeInfoDTO.setSchemaAttributes(list);
     }
 
     /**
      * getData purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public DataDTO getData() {
         return data;
     }
 
     /**
      * getGeoServer purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public GeoServerDTO getGeoServer() {
         return geoServer;
     }
 
     /**
      * getWfs purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public WFSDTO getWfs() {
         return wfs;
     }
 
     /**
      * getWms purpose.
      * 
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public WMSDTO getWms() {
         return wms;
     }
 }
