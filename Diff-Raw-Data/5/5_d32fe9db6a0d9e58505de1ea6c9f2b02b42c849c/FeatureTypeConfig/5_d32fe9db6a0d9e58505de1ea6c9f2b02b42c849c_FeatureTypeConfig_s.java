 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.config;
 
 import com.vividsolutions.jts.geom.*;
 import org.geotools.data.*;
 import org.geotools.factory.*;
 import org.geotools.feature.*;
 import org.geotools.filter.*;
 import org.geotools.styling.Style;
 import org.vfny.geoserver.WmsException;
 import org.w3c.dom.*;
 import java.io.*;
 import java.util.*;
 
 
 /**
  * Represents a FeatureType, its user config and autodefined information.
  *
  * @author Gabriel Roldn
  * @author Chris Holmes
 * @version $Id: FeatureTypeConfig.java,v 1.6 2004/01/05 23:12:06 cholmesny Exp $
  */
 public class FeatureTypeConfig extends BasicConfig {
     /** DOCUMENT ME! */
     private static final int DEFAULT_NUM_DECIMALS = 8;
 
     /** DOCUMENT ME! */
     private DataStoreConfig dataStore;
 
     /** DOCUMENT ME! */
     private Envelope bbox;
 
     /** DOCUMENT ME! */
     private Envelope latLongBBox;
 
     /** DOCUMENT ME! */
     private String SRS;
 
     /** DOCUMENT ME! */
     private Filter definitionQuery = Filter.NONE;
 
     /** DOCUMENT ME! */
     private FeatureType schema;
 
     /** DOCUMENT ME! */
     private Map styles;
     private CatalogConfig catalog;
     private List mandatoryAtts;
 
     /**
      * defaultStyle is not currently written to, and there are not any
      * subclasses. 12/17/03 dz
      */
     private String defaultStyle;
 
     /** DOCUMENT ME! */
     private String pathToSchemaFile;
     private String prefix;
     private int numDecimals = DEFAULT_NUM_DECIMALS;
 
     /**
      * GT2 based configuration, Config map supplies extra info.
      * 
      * <p>
      * We need to make an GeometryAttributeType that knows about SRID
      * </p>
      * 
      * <ul>
      * <li>
      * datastore.featuretype.srid: int (default 0)
      * </li>
      * <li>
      * datastore.featuretype.numDecimals: int (default 8)
      * </li>
      * <li>
      * datastore.featuretype.bbxo: Envelope (default calcuated)
      * </li>
      * </ul>
      * 
      *
      * @param config
      * @param type
      * @param dataStoreConfig
      */
     public FeatureTypeConfig(Map config, FeatureType type,
         DataStoreConfig dataStoreConfig) {
         super(config);
 
         String key = type.getNamespace() + "." + type.getTypeName();
         SRS = String.valueOf(get(config, key + ".srid", 0));
         dataStore = dataStoreConfig;
         numDecimals = get(config, key + ".numDecimals", 8);
         schema = type;
         styles = new HashMap(0);
 
         if (type.getDefaultGeometry() == null) {
             latLongBBox = new Envelope();
         } else if (config.containsKey(key + ".bbox")) {
             latLongBBox = (Envelope) config.get(key + ".bbox");
         } else {
             try {
                 FeatureSource access = dataStore.getDataStore()
                                                 .getFeatureSource(type
                         .getTypeName());
                 latLongBBox = access.getBounds();
 
                 if (latLongBBox == null) {
                     latLongBBox = access.getFeatures().getBounds();
                 }
             } catch (IOException io) {
                 latLongBBox = new Envelope();
             }
         }
     }
 
     /**
      * Creates a new FeatureTypeConfig object.
      *
      * @param catalog DOCUMENT ME!
      * @param fTypeRoot DOCUMENT ME!
      *
      * @throws ConfigurationException DOCUMENT ME!
      */
     public FeatureTypeConfig(CatalogConfig catalog, Element fTypeRoot)
         throws ConfigurationException {
         super(fTypeRoot);
 
         String msg = null;
         String dataStoreId = getAttribute(fTypeRoot, "datastore", true);
         this.dataStore = catalog.getDataStore(dataStoreId);
 
         if (dataStore == null) {
             msg = "FeatureType " + getName(true)
                 + " is congfigured from a datastore named " + dataStoreId
                 + " wich was not found. Check your config files.";
             throw new ConfigurationException(msg);
         }
 
         this.SRS = getChildText(fTypeRoot, "SRS", true);
 
         if (dataStore.isEnabled()) {
             try {
                 this.schema = getSchema(getChildElement(fTypeRoot, "attributes"));
             } catch (Exception ex) {
                 throw new ConfigurationException("Error obtaining schema for "
                     + getName() + ": " + ex.getMessage(), ex);
             }
 
             this.mandatoryAtts = getMandatoryAtts(getChildElement(fTypeRoot,
                         "attributes"));
             loadStyles(getChildElement(fTypeRoot, "styles"), catalog);
             loadLatLongBBox(getChildElement(fTypeRoot, "latLonBoundingBox"));
         } else {
             LOGGER.info("featureType " + getName() + " is not enabled");
         }
 
         Element numDecimalsElem = getChildElement(fTypeRoot, "numDecimals",
                 false);
 
         if (numDecimalsElem != null) {
             this.numDecimals = getIntAttribute(numDecimalsElem, "value", false,
                     DEFAULT_NUM_DECIMALS);
         }
 
         loadDefinitionQuery(fTypeRoot);
     }
 
     private void loadDefinitionQuery(Element typeRoot)
         throws ConfigurationException {
         Element defQNode = getChildElement(typeRoot, "definitionQuery", false);
         Filter filter = null;
 
         if (defQNode != null) {
             LOGGER.fine("definitionQuery element found, looking for Filter");
 
             Element filterNode = getChildElement(defQNode, "Filter", false);
 
             if ((filterNode != null)
                     && ((filterNode = getFirstChildElement(filterNode)) != null)) {
                 this.definitionQuery = FilterDOMParser.parseFilter(filterNode);
 
                 return;
             }
 
             LOGGER.fine("No Filter definition query found");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public int getNumDecimals() {
         return numDecimals;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public FeatureType getSchema() {
         return schema;
     }
 
     /**
      * gets the string of the path to the schema file.  This is set during
      * feature reading, the schema file should be in the same folder as the
      * feature type info, with the name schema.xml.  This function does not
      * guarantee that the schema file actually exists, it just gives the
      * location where it _should_ be located.
      *
      * @return The path to the schema file.
      */
     public String getSchemaFile() {
         return pathToSchemaFile;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param pathToSchema DOCUMENT ME!
      */
     public void setSchemaFile(String pathToSchema) {
         this.pathToSchemaFile = pathToSchema;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public DataStoreConfig getDataStore() {
         return this.dataStore;
     }
 
     /**
      * Indicates if this FeatureType is enabled.  For now just gets whether the
      * backing datastore is enabled.
      *
      * @return <tt>true</tt> if this FeatureTypeConfig is enabled.
      *
      * @task REVISIT: Consider adding more fine grained control to config
      *       files, so users can indicate specifically if they want the
      *       featureTypes enabled, instead of just relying on if the datastore
      *       is.
      */
     public boolean isEnabled() {
         return (this.dataStore != null) && (this.dataStore.isEnabled());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getPrefix() {
         if (this.prefix == null) {
             this.prefix = getDataStore().getNameSpace().getPrefix();
         }
 
         return prefix;
     }
 
     /**
      * Gets the namespace for this featureType.  This isn't _really_ necessary,
      * but I'm putting it in in case we change namespaces,  letting
      * FeatureTypes set their own namespaces instead of being dependant on
      * datasources.  This method will allow us to make that change more easily
      * in the future.
      *
      * @return DOCUMENT ME!
      *
      * @throws IllegalStateException DOCUMENT ME!
      */
     public NameSpace getNameSpace() {
         if (!isEnabled()) {
             throw new IllegalStateException("This featureType is not "
                 + "enabled");
         }
 
         return getDataStore().getNameSpace();
     }
 
     /**
      * overrides getName to return full type name with namespace prefix
      *
      * @return DOCUMENT ME!
      */
     public String getName() {
         //getDataStore().getNameSpace().getPrefix() is causing too many null
         //pointers on unitialized stuff.  figure out a more elegant way to
         //handle this.
         return new StringBuffer(getPrefix()).append(NameSpace.PREFIX_DELIMITER)
                                             .append(super.getName()).toString();
     }
 
     /**
      * Convenience method for those who just want to report the name of the
      * featureType instead of requiring the full name for look up.  If
      * allowShort is true then just the localName, with no prefix, will be
      * returned if the dataStore is not enabled.  If allow short is false then
      * a full getName will be returned, with potentially bad results.
      *
      * @param allowShort DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getName(boolean allowShort) {
         if (allowShort && (!isEnabled() || (getDataStore() == null))) {
             return getShortName();
         } else {
             return getName();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getShortName() {
         return super.getName();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public FeatureSource getFeatureSource() throws IOException {
         if (!isEnabled() || (dataStore.getDataStore() == null)) {
             throw new IOException("featureType: " + getName(true)
                 + " does not have a properly configured " + "datastore");
         }
 
         FeatureSource realSource = getRealFeatureSource();
         FeatureSource mappedSource = new DEFQueryFeatureLocking(realSource,
                 getSchema(), this.definitionQuery, this.mandatoryAtts);
 
         return mappedSource;
     }
 
     private FeatureSource getRealFeatureSource()
         throws NoSuchElementException, IllegalStateException, IOException {
         FeatureSource realSource = dataStore.getDataStore().getFeatureSource(super
                 .getName());
 
         return realSource;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public Envelope getBoundingBox() throws IOException {
         if (bbox == null) {
             loadBoundingBoxes();
         }
 
         return bbox;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public Filter getDefinitionQuery() {
         return this.definitionQuery;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      */
     public Envelope getLatLongBoundingBox() throws IOException {
         if (latLongBBox == null) {
             loadBoundingBoxes();
         }
 
         return latLongBBox;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getSRS() {
         return SRS;
     }
 
     /**
      **/
     public String getOperations() {
         //get this from the datasource?
         return null;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public Map getStyles() {
         return this.styles;
     }
 
     //HACK: should not have access to all catalog styles, but first we need
     //to figure out the loading of styles.
     public Style getStyle(String styleName) throws WmsException {
         Style style = (Style) styles.get(styleName);
         LOGGER.info("got style " + style + " from " + styles);
 
         if (style == null) {
             throw new WmsException("style named " + styleName + " not found");
         }
 
         return style;
     }
 
     /**
      * defaultStyle is not currently written to, and there are not any
      * subclasses. 12/17/03 dz
      *
      * @return String defaultStyle
      */
     public String getDefaultStyle() {
         return this.defaultStyle;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws IOException DOCUMENT ME!
      * @throws IllegalStateException DOCUMENT ME!
      */
     private void loadBoundingBoxes() throws IOException {
         if (!isEnabled()) {
             throw new IllegalStateException("This featureType is not "
                 + "enabled");
         }
 
         FeatureSource source = getRealFeatureSource();
         this.bbox = source.getBounds();
 
         if (this.latLongBBox == null) {
             this.latLongBBox = getLatLongBBox(getSRS(), bbox);
         }
     }
 
     /**
      * creates a FeatureType schema from the list of defined exposed
      * attributes, or the full schema if no exposed attributes were defined
      *
      * @param attsElem DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      *
      * @throws ConfigurationException DOCUMENT ME!
      * @throws IOException DOCUMENT ME!
      *
      * @task TODO: if the default geometry attribute was not declared as
      *       exposed should we expose it anyway? I think yes.
      */
     private FeatureType getSchema(Element attsElem)
         throws ConfigurationException, IOException {
         NodeList exposedAttributes = null;
         FeatureType schema = getRealFeatureSource().getSchema();
         FeatureType filteredSchema = null;
 
         if (attsElem != null) {
             exposedAttributes = attsElem.getElementsByTagName("attribute");
         }
 
         if ((exposedAttributes == null) || (exposedAttributes.getLength() == 0)) {
             return schema;
         }
 
         int attCount = exposedAttributes.getLength();
         AttributeType[] attributes = new AttributeType[attCount];
         Element attElem;
         String attName;
 
         for (int i = 0; i < attCount; i++) {
             attElem = (Element) exposedAttributes.item(i);
             attName = getAttribute(attElem, "name", true);
             attributes[i] = schema.getAttributeType(attName);
 
             if (attributes[i] == null) {
                 throw new ConfigurationException("the FeatureType " + getName()
                     + " does not contains the configured attribute " + attName
                     + ". Check your catalog configuration");
             }
         }
 
         try {
             filteredSchema = FeatureTypeFactory.newFeatureType(attributes,
                     getName());
         } catch (SchemaException ex) {
         } catch (FactoryConfigurationError ex) {
         }
 
         return filteredSchema;
     }
 
     /**
      * This method extracts the mandatory attributes that should always be
      * returned.  This is a hack, it should at least be part of the geotools
      * featureType, and extracted in the getSchema method.  Ideally it should
      * be read from the datasource or customized with specific gml schema.
      *
      * @param attsElem The attributes element, with attribute child elements.
      *
      * @return A list of the mandatory attributes, an empty list if none of the
      *         attributes are mandatory if attsElem has no child attribute
      *         elements.
      *
      * @throws ConfigurationException DOCUMENT ME!
      */
     private List getMandatoryAtts(Element attsElem)
         throws ConfigurationException {
         List mandAtts = new ArrayList();
 
         NodeList exposedAttributes = null;
 
         if (attsElem != null) {
             exposedAttributes = attsElem.getElementsByTagName("attribute");
         }
 
         if ((exposedAttributes == null) || (exposedAttributes.getLength() == 0)) {
             return mandAtts;
         }
 
         int attCount = exposedAttributes.getLength();
         Element attElem;
         String attName;
 
         for (int i = 0; i < attCount; i++) {
             attElem = (Element) exposedAttributes.item(i);
             attName = getAttribute(attElem, "name", true);
             LOGGER.finer("attribute name is " + attName + ", from " + attElem);
 
             String mandatory = notNull(getAttribute(attElem, "mandatory", false));
 
             if (mandatory.equalsIgnoreCase("true")) {
                 mandAtts.add(attName);
             }
         }
 
         LOGGER.fine("returning mandatory atts " + mandAtts + ", from "
             + attsElem);
 
         return mandAtts;
     }
 
     /**
      * here we must make the transformation. Crhis: do you know how to do it? I
      * don't know.  Ask martin or geotools devel.  This will be better when
      * our geometries actually have their srs objects.  And I think that we
      * may need some MS Access database, not sure, but I saw some stuff about
      * that on the list.  Hopefully they'll do it all in java soon.  I'm sorta
      * tempted to just have users define for now.
      *
      * @param fromSrId DOCUMENT ME!
      * @param bbox DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private static Envelope getLatLongBBox(String fromSrId, Envelope bbox) {
         //Envelope latLongBBox = null;
         //return latLongBBox;
         return bbox;
     }
 
     private void loadLatLongBBox(Element bboxElem)
         throws ConfigurationException {
         boolean dynamic = getBooleanAttribute(bboxElem, "dynamic", false);
 
         if (!dynamic) {
             double minx = getDoubleAttribute(bboxElem, "minx", true);
             double miny = getDoubleAttribute(bboxElem, "miny", true);
             double maxx = getDoubleAttribute(bboxElem, "maxx", true);
             double maxy = getDoubleAttribute(bboxElem, "maxy", true);
            this.latLongBBox = new Envelope(minx, miny, maxx, maxy);
         }
     }
 
     /**
      * DOCUMENT ME!  does not appear to have any affect except to create an
      * empty hashmap (dz)
      *
      * @param styles DOCUMENT ME!
      * @param catalog DOCUMENT ME!
      *
      * @throws ConfigurationException DOCUMENT ME!
      *
      * @task TODO: I'm not sure this class is necessary, or if it's doing what
      *       we want.  Right now it reads in the style elements in an info.xml
      *       file (or rather doesn't, as I haven't got it working yet), and
      *       then limits the styles to those.  I think that instead what it
      *       should do is analyze the styles of catalog, see if they match
      *       this FeatureType, and if they do then load the styles.
      */
     private void loadStyles(Element styles, CatalogConfig catalog)
         throws ConfigurationException {
         NodeList stylesList = null;
         int numStyles = 0;
         LOGGER.info("loading styles " + styles + ", " + this.styles);
 
         //HACK: we need to shake out catalog and config and whatnot.
         this.styles = catalog.getStyles();
 
         /*if (styles != null) {
            stylesList = styles.getElementsByTagName("style");
            numStyles = stylesList.getLength();
            this.styles = new HashMap(numStyles);
            for (int i = 0; i < numStyles; i++) {
                Node node = stylesList.item(i);
                if (node instanceof Element) {
                    Element elem = (Element) node;
                    String id = getElementText(elem, true);
                    Style style = catalog.getStyle(id);
                    if (style == null) {
                        LOGGER.warning("Problem loading styles for " + getName(true) +
                                       ", requested id " + id + " is not available in loaded "
                                       + "styles: " + catalog.getStyles());
                    } else {
                        LOGGER.config("featureType " + getName(true) + " added style " + id);
                        this.styles.put(id, style);
                        String defaultVal = getAttribute(elem, "default", false);
                        if (defaultVal != null && defaultVal.equals("true")) {
                            this.defaultStyle = id;
                        }
                    }
                }
            }
         
            }
         
            //TODO: programatically provide a good default.
            if (numStyles == 0) {
                this.styles = new HashMap(numStyles + 1);
                }*/
     }
 }
