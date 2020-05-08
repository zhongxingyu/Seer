 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.form.data;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.apache.struts.Globals;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.util.MessageResources;
 import org.geotools.data.DataStore;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.GeometryAttributeType;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.vfny.geoserver.action.HTMLEncoder;
 import org.vfny.geoserver.config.AttributeTypeInfoConfig;
 import org.vfny.geoserver.config.ConfigRequests;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.config.DataStoreConfig;
 import org.vfny.geoserver.config.FeatureTypeConfig;
 import org.vfny.geoserver.config.StyleConfig;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.MetaDataLink;
 import org.vfny.geoserver.global.UserContainer;
 import org.vfny.geoserver.global.dto.AttributeTypeInfoDTO;
 import org.vfny.geoserver.global.dto.DataTransferObjectFactory;
 import org.vfny.geoserver.util.Requests;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 
 /**
  * Form used to work with FeatureType information.
  *
  * @author jgarnett, Refractions Research, Inc.
  * @author $Author: cholmesny $ (last modification)
  * @version $Id$
  */
 public class TypesEditorForm extends ActionForm {
     static final List schemaBases;
     static final List allMetadataURLTypes;
 
     static {
         List bases = new ArrayList();
         bases.add("--");
         bases.addAll(DataTransferObjectFactory.schemaBaseMap.keySet());
         schemaBases = Collections.unmodifiableList(bases);
         allMetadataURLTypes = Arrays.asList(new String[] { "FGDC", "TC211" });
     }
 
     /** Identiy DataStore responsible for this FeatureType */
     private String dataStoreId;
 
     /** Identify Style used to render this feature type */
     private String styleId;
 
     /** Sorted Set of available styles */
     private SortedSet panelStyleIds;
     private SortedSet typeStyles;
     private String[] otherSelectedStyles;
 
     /**
      * Name of featureType.
      *
      * <p>
      * An exact match for typeName provided by a DataStore.
      * </p>
      */
     private String typeName;
 
     /**
          *
          */
     private String wmsPath;
 
     /**
      * Representation of the Spatial Reference System.
      *
      * <p>
      * Empty represents unknown, usually assumed to be Cartisian Coordinates.
      * </p>
     */
     private String SRS;
 
     /**
      *  WKT representation of the SRS
      *  This is read-only since it gets generated from the SRS id.
      *  Everytime SRS is updates (#setSRS()), this will also be re-set.
      *  If there's a problem with the SRS, this will try to give some info about the error.
      */
     private String SRSWKT;
     
     /**
      * WKT representation of the native SRS
      */
     private String nativeSRSWKT;
     
     private List allSrsHandling;
     private int srsHandling = FeatureTypeInfo.FORCE;
 
     /** Title of this FeatureType */
     private String title;
 
     /** Representation of bounds info as parseable by Double */
     private String minX;
 
     /** Representation of bounds info as parseable by Double */
     private String minY;
 
     /** Representation of bounds info as parseable by Double */
     private String maxX;
 
     /** Representation of bounds info as parseable by Double */
     private String maxY;
 
     /** List of keywords, often grouped with brackets */
     private String keywords;
 
     /** Metadata URL
      *  This is a quick hack, the user interface and configuration code is really too broken
      *  to waste time on it...
      **/
     private MetaDataLink[] metadataLinks;
 
     /** Metadata URL types **/
     private String[] metadataURLTypes;
 
     /** FeatureType abstract */
     private String description;
 
     /** The amount of time to use for the CacheControl: max-age parameter in maps generated from this featuretype **/
     private String cacheMaxAge;
 
     /** Should we add the CacheControl: max-age header to maps generated from this featureType? **/
     private boolean cachingEnabled;
     private boolean cachingEnabledChecked = false;
 
     /**
      * One of a select list - simplest is AbstractBaseClass.
      *
      * <p>
      * The value "--" will be used to indicate default schema completly
      * generated from FeatureType information at runtime.
      * </p>
      *
      * <p>
      * When generated the schema will make use a schemaBase of
      * "AbstractFeatureType".
      * </p>
      */
     private String schemaBase;
 
     /**
      * The name of the complex element of type schemaBase.
      *
      * <p>
      * We only need this for non generated schemaBase.
      * </p>
      */
     private String schemaName;
 
     /** List of AttributeDisplay and AttributeForm */
     private List attributes;
 
     /** List of attributes available for addition */
     private List addList;
 
     /** Action requested by user */
     private String action;
 
     /** Sorted Set of available styles */
     private SortedSet styles;
 
     /** A hidden field to enable autogeneration of extents (for SRS and BoundingBox values) **/
     private String autoGenerateExtent;
 
     /** Stores the name of the new attribute they wish to create */
     private String newAttribute;
 
     /** these store the bounding box of DATASET - in it coordinate system.
          *  normally, you'll have these set to "" or null.
          *  They're only for information purposes (presentation), they are never persisted or used in any calculations.
          */
     private String dataMinX;
     private String dataMinY;
     private String dataMaxX;
     private String dataMaxY;
     private CoordinateReferenceSystem declaredCRS;
     private CoordinateReferenceSystem nativeCRS;
 
     /**
      * Set up FeatureTypeEditor from from Web Container.
      *
      * <p>
      * The key DataConfig.SELECTED_FEATURE_TYPE is used to look up the selected
      * from the web container.
      * </p>
      *
      * @param mapping
      * @param request
      */
     public void reset(ActionMapping mapping, HttpServletRequest request) {
         super.reset(mapping, request);
 
         action = "";
 
         ServletContext servletContext = getServlet().getServletContext();
         ServletContext context = servletContext;
 
         DataConfig config = ConfigRequests.getDataConfig(request);
         UserContainer user = Requests.getUserContainer(request);
 
         FeatureTypeConfig type = user.getFeatureTypeConfig();
 
         if (type == null) {
             System.out.println("Type is not there");
 
             // Not sure what to do, user must have bookmarked?
             return; // Action should redirect to Select screen?
         }
 
         this.dataStoreId = type.getDataStoreId();
         this.styleId = type.getDefaultStyle();
 
         description = type.getAbstract();
 
         this.cacheMaxAge = type.getCacheMaxAge();
         this.cachingEnabled = type.isCachingEnabled();
         cachingEnabledChecked = false;
 
         Envelope bounds = type.getLatLongBBox();
 
         if ((bounds == null) || bounds.isNull()) {
             minX = "";
             minY = "";
             maxY = "";
             maxX = "";
         } else {
             minX = Double.toString(bounds.getMinX());
             minY = Double.toString(bounds.getMinY());
             maxX = Double.toString(bounds.getMaxX());
             maxY = Double.toString(bounds.getMaxY());
         }
 
         Envelope nativeBounds = type.getNativeBBox();
 
         if ((nativeBounds == null) || nativeBounds.isNull()) {
             dataMinX = "";
             dataMinY = "";
             dataMaxX = "";
             dataMaxY = "";
         } else {
             dataMinX = Double.toString(nativeBounds.getMinX());
             dataMinY = Double.toString(nativeBounds.getMinY());
             dataMaxX = Double.toString(nativeBounds.getMaxX());
             dataMaxY = Double.toString(nativeBounds.getMaxY());
         }
 
         typeName = type.getName();
         setSRS(Integer.toString(type.getSRS())); // doing it this way also sets SRSWKT
         srsHandling = type.getSRSHandling();
         
         nativeSRSWKT = "-";
         try {
             DataConfig dataConfig =  (DataConfig) servletContext.getAttribute(DataConfig.CONFIG_KEY);
             DataStoreConfig dsConfig = dataConfig.getDataStore(type.getDataStoreId());
             DataStore dataStore = dsConfig.findDataStore(servletContext);
             FeatureType featureType = dataStore.getSchema(type.getName());
             GeometryAttributeType dg = featureType.getPrimaryGeometry();
             if(dg != null && dg.getCoordinateSystem() != null) {
                 nativeCRS = dg.getCoordinateSystem();
                 nativeSRSWKT = dg.getCoordinateSystem().toString();
             }
         } catch(Exception e) {
             // never mind
         }
         
         // load localized
         MessageResources resources = ((MessageResources) request.getAttribute(Globals.MESSAGES_KEY));
         if(nativeSRSWKT == "-")
             allSrsHandling =  Arrays.asList(new String[] {resources.getMessage("label.type.forceSRS")});
         else
             allSrsHandling =  Arrays.asList(new String[] {resources.getMessage("label.type.forceSRS"),
                 resources.getMessage("label.type.reprojectSRS"), resources.getMessage("label.type.leaveSRS")});
         
 
         title = type.getTitle();
         wmsPath = type.getWmsPath();
 
         System.out.println("rest based on schemaBase: " + type.getSchemaBase());
 
         // Generate ReadOnly list of Attributes
         //
         DataStoreConfig dataStoreConfig = config.getDataStore(dataStoreId);
         FeatureType featureType = null;
 
         try {
             DataStore dataStore = dataStoreConfig.findDataStore(servletContext);
             featureType = dataStore.getSchema(typeName);
         } catch (IOException e) {
             // DataStore unavailable!
         }
 
         if (((type.getSchemaBase() == null) || "--".equals(type.getSchemaBase()))
                 || (type.getSchemaAttributes() == null)) {
             //We are using the generated attributes
             this.schemaBase = "--";
             this.schemaName = typeName + "_Type";
             this.attributes = new LinkedList();
 
             // Generate ReadOnly list of Attributes
             //
             List generated = DataTransferObjectFactory.generateAttributes(featureType);
             this.attributes = attributesDisplayList(generated);
             addList = Collections.EMPTY_LIST;
         } else {
             this.schemaBase = type.getSchemaBase();
             this.schemaName = type.getSchemaName();
             this.attributes = new LinkedList();
 
             //
             // Need to add read only AttributeDisplay for each required attribute
             // defined by schemaBase
             //
             List schemaAttributes = DataTransferObjectFactory.generateRequiredAttributes(schemaBase);
             attributes.addAll(attributesDisplayList(schemaAttributes));
             attributes.addAll(attributesFormList(type.getSchemaAttributes(), featureType));
             addList = new ArrayList(featureType.getAttributeCount());
 
             for (int i = 0; i < featureType.getAttributeCount(); i++) {
                 String attributeName = featureType.getAttributeType(i).getName();
 
                 if (lookUpAttribute(attributeName) == null) {
                     addList.add(attributeName);
                 }
             }
         }
 
         StringBuffer buf = new StringBuffer();
 
         for (Iterator i = type.getKeywords().iterator(); i.hasNext();) {
             String keyword = (String) i.next();
             buf.append(keyword);
 
             if (i.hasNext()) {
                 buf.append(" ");
             }
         }
 
         this.keywords = buf.toString();
 
         metadataLinks = new MetaDataLink[2];
         metadataLinks[0] = new MetaDataLink();
         metadataLinks[0].setType("text/plain");
         metadataLinks[1] = new MetaDataLink();
         metadataLinks[1].setType("text/plain");
 
         if ((type.getMetadataLinks() != null) && (type.getMetadataLinks().size() > 0)) {
             List links = new ArrayList(type.getMetadataLinks());
             MetaDataLink link = (MetaDataLink) links.get(0);
             metadataLinks[0] = new MetaDataLink(link);
 
             if (links.size() > 1) {
                 link = (MetaDataLink) links.get(1);
                 metadataLinks[1] = new MetaDataLink(link);
             }
         }
 
         styles = new TreeSet();
 
         for (Iterator i = config.getStyles().values().iterator(); i.hasNext();) {
             StyleConfig sc = (StyleConfig) i.next();
             styles.add(sc.getId());
 
             if (sc.isDefault()) {
                 if ((styleId == null) || styleId.equals("")) {
                     styleId.equals(sc.getId());
                 }
             }
         }
 
         typeStyles = new TreeSet();
 
         for (Iterator i = type.getStyles().iterator(); i.hasNext();) {
             String styleName = (String) i.next();
             typeStyles.add(styleName);
         }
 
         Object attribute = styles;
 
         if (attribute instanceof org.vfny.geoserver.form.data.AttributeDisplay) {
             ;
         }
     }
 
     private Object lookUpAttribute(String name) {
         for (Iterator i = attributes.iterator(); i.hasNext();) {
             Object attribute = i.next();
 
             if (attribute instanceof AttributeDisplay
                     && name.equals(((AttributeDisplay) attribute).getName())) {
                 return attribute;
             }
 
             if (attribute instanceof AttributeForm
                     && name.equals(((AttributeForm) attribute).getName())) {
                 return attribute;
             }
         }
 
         return null;
     }
 
     /**
      * Create a List of AttributeDisplay based on AttributeTypeInfoDTO.
      *
      * @param dtoList
      *
      * @return
      */
     private List attributesDisplayList(List dtoList) {
         List list = new ArrayList();
         int index = 0;
 
         for (Iterator i = dtoList.iterator(); i.hasNext(); index++) {
             Object next = i.next();
             //System.out.println(index + " attribute: " + next);
             list.add(new AttributeDisplay(new AttributeTypeInfoConfig((AttributeTypeInfoDTO) next)));
         }
 
         return list;
     }
 
     /**
      * Create a List of AttributeForm based on AttributeTypeInfoDTO.
      *
      * @param dtoList
      * @param schema DOCUMENT ME!
      *
      * @return
      */
     private List attributesFormList(List dtoList, FeatureType schema) {
         List list = new ArrayList();
 
         for (Iterator i = dtoList.iterator(); i.hasNext();) {
             AttributeTypeInfoConfig config = (AttributeTypeInfoConfig) i.next();
             list.add(new AttributeForm(config, schema.getAttributeType(config.getName())));
         }
 
         return list;
     }
 
     /**
      * Generate DTO attributes List for the TypesEditorAction.
      *
      * <p>
      * This list only includes entries defined by the user, not those generated
      * by the schemaBase.
      * </p>
      *
      * <p>
      * If the user has chosen -- then this list will be <code>null</code>.
      * </p>
      *
      * @return List of user supplied AttributeTypeInfoConfig, or
      *         <code>null</code>
      */
     public List toSchemaAttributes() {
         if ((schemaBase == null) || schemaBase.equals("--")) {
             return null;
         }
 
         List list = new ArrayList();
 
         for (Iterator i = attributes.iterator(); i.hasNext();) {
             Object obj = i.next();
 
             if (obj instanceof AttributeForm) {
                 AttributeForm form = (AttributeForm) obj;
                 list.add(form.toConfig());
             }
         }
 
         return list;
     }
 
     public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
         ActionErrors errors = new ActionErrors();
 
         Locale locale = (Locale) request.getLocale();
 
         //MessageResources messages = servlet.getResources();
         //TODO: not sure about this, changed for struts 1.2.8 upgrade
         MessageResources messages = (MessageResources) request.getAttribute(Globals.MESSAGES_KEY);
         final String BBOX = HTMLEncoder.decode(messages.getMessage(locale,
                     "config.data.calculateBoundingBox.label"));
         final String SLDWIZARD = HTMLEncoder.decode(messages.getMessage(locale,
                     "config.data.sldWizard.label"));
         final String LOOKUP_SRS = HTMLEncoder.decode(messages.getMessage(locale,
                     "config.data.lookupSRS.label"));
 
         // If autoGenerateExtent flag is not set, don't break.
         if (autoGenerateExtent == null) {
             autoGenerateExtent = "false";
         }
 
         // Pass Attribute Management Actions through without
         // much validation.
         if (action.startsWith("Up") || action.startsWith("Down") || action.startsWith("Remove")
                 || action.equals(BBOX) || action.equals(SLDWIZARD) || action.equals(LOOKUP_SRS)) {
             return errors;
         }
 
         // Check selected style exists
         DataConfig data = ConfigRequests.getDataConfig(request);
 
         if (!(data.getStyles().containsKey(styleId) || "".equals(styleId))) {
             errors.add("styleId", new ActionError("error.styleId.notFound", styleId));
         }
 
         // check name exists in current DataStore?
         if (!autoGenerateExtent.equals("true")) {
             if (("".equals(minX) || "".equals(minY) || "".equals(maxX) || "".equals(maxY))) {
                 errors.add("latlongBoundingBox", new ActionError("error.latLonBoundingBox.required"));
             } else {
                 try {
                     Double.parseDouble(minX);
                     Double.parseDouble(minY);
                     Double.parseDouble(maxX);
                     Double.parseDouble(maxY);
                 } catch (NumberFormatException badNumber) {
                     errors.add("latlongBoundingBox",
                         new ActionError("error.latLonBoundingBox.invalid", badNumber));
                 }
             }
         }
 
         if (isCachingEnabled()) {
             try {
                 Integer.parseInt(cacheMaxAge);
             } catch (NumberFormatException nfe) {
                 errors.add("cacheMaxAge", new ActionError("error.cacheMaxAge.malformed", nfe));
             } catch (Throwable t) {
                 errors.add("cacheMaxAge", new ActionError("error.cacheMaxAge.error", t));
             }
         }
 
         return errors;
     }
 
     /**
      * Are belong to us.
      *
      * <p>
      * What can I say it is near a deadline! Easy access for
      * <code>Editor.jsp</code>.
      * </p>
      *
      * @return Possible schemaBase options
      */
     public List getAllYourBase() {
         return schemaBases;
     }
 
     public List getAllMetadataURLTypes() {
         return allMetadataURLTypes;
     }
 
     //
     // Generated Accessors for Editor.jsp
     //
 
     /**
      * Access attributes property.
      *
      * @return Returns the attributes.
      */
     public List getAttributes() {
         return attributes;
     }
 
     /**
      * Set attributes to attributes.
      *
      * @param attributes The attributes to set.
      */
     public void setAttributes(List attributes) {
         this.attributes = attributes;
     }
 
     /**
      * Access dataStoreId property.
      *
      * @return Returns the dataStoreId.
      */
     public String getDataStoreId() {
         return dataStoreId;
     }
 
     /**
      * Set dataStoreId to dataStoreId.
      *
      * @param dataStoreId The dataStoreId to set.
      */
     public void setDataStoreId(String dataStoreId) {
         this.dataStoreId = dataStoreId;
     }
 
     /**
      * Access abstact (or description) property.
      *
      * @return Returns the description.
      */
     public String getAbstract() {
         return description;
     }
 
     /**
      * Set abstact (or description) to description.
      *
      * @param description The description to set.
      */
     public void setAbstract(String description) {
         this.description = description;
     }
 
     /**
      * Access keywords property.
      *
      * @return Returns the keywords.
      */
     public String getKeywords() {
         return keywords;
     }
 
     /**
      * Set keywords to keywords.
      *
      * @param keywords The keywords to set.
      */
     public void setKeywords(String keywords) {
         this.keywords = keywords;
     }
 
     public MetaDataLink getMetadataLink(int index) {
         return metadataLinks[index];
     }
 
     /**
      * Access name property.
      *
      * @return Returns the name.
      */
     public String getTypeName() {
         return typeName;
     }
 
     /**
      * Set name to name.
      *
      * @param name The name to set.
      */
     public void setTypeName(String name) {
         this.typeName = name;
     }
 
     /**
      * Access schemaBase property.
      *
      * @return Returns the schemaBase.
      */
     public String getSchemaBase() {
         return schemaBase;
     }
 
     /**
      * Set schemaBase to schemaBase.
      *
      * @param schemaBase The schemaBase to set.
      */
     public void setSchemaBase(String schemaBase) {
         this.schemaBase = schemaBase;
     }
 
     /**
      * Access sRS property.
      *
      * @return Returns the sRS.
      */
     public String getSRS() {
         return SRS;
     }
 
     /**
      * Access SRSWKT property.  There is no setSRSWKT() because its derived from the SRS id.
      *
      * @return Returns the sRS.
      */
     public String getSRSWKT() {
         return SRSWKT;
     }
     
     /**
      * Access SRSWKT property.  There is no setSRSWKT() because its derived from the SRS id.
      *
      * @return Returns the sRS.
      */
     public String getNativeSRSWKT() {
         return nativeSRSWKT;
     }
 
     /**
      * Set sRS to srs.
      *
      *  Also sets WKTSRS.
      *  srs should be an Integer (in string form) - according to FeatureTypeConfig
      *
      * @param srs The sRS to set.
      */
     public void setSRS(String srs) {
         SRS = srs;
 
         try {
             // srs should be an Integer - according to FeatureTypeConfig
             // TODO: make everything consistent for SRS - either its an int or a
             //       string.
             String newSrs = srs;
 
             if (newSrs.indexOf(':') == -1) {
                 newSrs = "EPSG:" + srs;
             }
 
             declaredCRS = CRS.decode(newSrs);
             SRSWKT = declaredCRS.toString();
         } catch (FactoryException e) // couldnt decode their code
          {
             // DJB:
             // dont know how to internationize this inside a set() method!!!
             // I think I need the request to get the local, then I can get MessageResources
             // from the servlet and call an appropriate method.  
             // Unforutunately, I dont know how to get the local!  
             SRSWKT = "Could not find a definition for: EPSG:" + srs;
         }
     }
 
     /**
      * Access title property.
      *
      * @return Returns the title.
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * Set title to title.
      *
      * @param title The title to set.
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the action.
      */
     public String getAction() {
         return action;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param action The action to set.
      */
     public void setAction(String action) {
         this.action = action;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the maxX.
      */
     public String getMaxX() {
         return maxX;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param maxX The maxX to set.
      */
     public void setMaxX(String maxX) {
         this.maxX = maxX;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the maxY.
      */
     public String getMaxY() {
         return maxY;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param maxY The maxY to set.
      */
     public void setMaxY(String maxY) {
         this.maxY = maxY;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the minX.
      */
     public String getMinX() {
         return minX;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param minX The minX to set.
      */
     public void setMinX(String minX) {
         this.minX = minX;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the minY.
      */
     public String getMinY() {
         return minY;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param minY The minY to set.
      */
     public void setMinY(String minY) {
         this.minY = minY;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the styleId.
      */
     public String getStyleId() {
         return styleId;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param styleId The styleId to set.
      */
     public void setStyleId(String styleId) {
         this.styleId = styleId;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the styles.
      */
     public SortedSet getStyles() {
         return styles;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param styles The styles to set.
      */
     public void setStyles(SortedSet styles) {
         this.styles = styles;
     }
 
     public Object getAttribute(int index) {
         return attributes.get(index);
     }
 
     public void setAttribute(int index, Object attribute) {
         attributes.set(index, attribute);
     }
 
     /**
      * Access newAttribute property.
      *
      * @return Returns the newAttribute.
      */
     public String getNewAttribute() {
         return newAttribute;
     }
 
     /**
      * Set newAttribute to newAttribute.
      *
      * @param newAttribute The newAttribute to set.
      */
     public void setNewAttribute(String newAttribute) {
         this.newAttribute = newAttribute;
     }
 
     /**
      * Access the autoGenerateExtent attribute.
      *
      */
     public String getAutoGenerateExtent() {
         if (this.autoGenerateExtent == null) {
             this.autoGenerateExtent = "false";
         }
 
         return this.autoGenerateExtent;
     }
 
     /**
      * Set autoGenerateExtent to autoGenerateExtent.
      * @param autoGenerateExtent The autoGenerateExtent to set.
      */
     public void setAutoGenerateExtent(String autoGenerateExtent) {
         this.autoGenerateExtent = autoGenerateExtent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return List of attributes available for addition
      */
     public List getCreateableAttributes() {
         return addList;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return Returns the schemaName.
      */
     public String getSchemaName() {
         return schemaName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param schemaName The schemaName to set.
      */
     public void setSchemaName(String schemaName) {
         this.schemaName = schemaName;
     }
 
     public void setDataMinX(String x) {
         dataMinX = x;
     }
 
     public void setDataMinY(String x) {
         dataMinY = x;
     }
 
     public void setDataMaxX(String x) {
         dataMaxX = x;
     }
 
     public void setDataMaxY(String x) {
         dataMaxY = x;
     }
 
     public String getDataMinX() {
         return dataMinX;
     }
 
     public String getDataMinY() {
         return dataMinY;
     }
 
     public String getDataMaxX() {
         return dataMaxX;
     }
 
     public String getDataMaxY() {
         return dataMaxY;
     }
 
     public String getWmsPath() {
         return wmsPath;
     }
 
     public void setWmsPath(String wmsPath) {
         this.wmsPath = wmsPath;
     }
 
     public String getCacheMaxAge() {
         return cacheMaxAge;
     }
 
     public void setCacheMaxAge(String cacheMaxAge) {
         this.cacheMaxAge = cacheMaxAge;
     }
 
     public boolean isCachingEnabled() {
         return cachingEnabled;
     }
 
     public void setCachingEnabled(boolean cachingEnabled) {
         cachingEnabledChecked = true;
         this.cachingEnabled = cachingEnabled;
     }
 
     public boolean isCachingEnabledChecked() {
         return cachingEnabledChecked;
     }
 
     public String[] getOtherSelectedStyles() {
         return otherSelectedStyles;
     }
 
     public void setOtherSelectedStyles(String[] otherSelectedStyles) {
         this.otherSelectedStyles = otherSelectedStyles;
     }
 
     public SortedSet getPanelStyleIds() {
         return panelStyleIds;
     }
 
     public SortedSet getTypeStyles() {
         return typeStyles;
     }
     
     public List getAllSrsHandling() {
         return allSrsHandling;
     }
     
     /**
      * This methods are used by the form, where the "leave" option cannot be offered, if
      * they are equal the drop down list won't be shown, that's all
      * @return
      */
     public String getSrsHandling() {
        if(srsHandling == 0 || srsHandling == 1)
             return (String) allSrsHandling.get(srsHandling);
         else
            return "";
     }
     
     public void setSrsHandling(String handling) {
         srsHandling = allSrsHandling.indexOf(handling);
         if(srsHandling == -1)
             srsHandling = FeatureTypeInfo.FORCE;
     }
     
     public int getSrsHandlingCode() {
         return srsHandling;
     }
     
     public void setSrsHandlingCode(int code) {
         this.srsHandling = code;
     }
     
     public boolean isDeclaredCRSDifferent() {
         return nativeCRS == null || (declaredCRS != null && !CRS.equalsIgnoreMetadata(declaredCRS, nativeCRS));
     }
     
 }
