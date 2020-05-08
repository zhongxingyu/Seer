 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global.dto;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.opengis.filter.Filter;
 import org.vfny.geoserver.config.DataConfig;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * Data Transfer Object used for GeoServer FeatureTypeInfo information.
  *
  * <p>
  * FeatureTypeInfo is used because FeatureType is already used to represent
  * schema information in GeoTools2.
  * </p>
  *
  * <p>
  * Data Transfer object are used to communicate between the GeoServer
  * application and its configuration and persistent layers. As such the class
  * is final - to allow for its future use as an on-the-wire message.
  * </p>
  * <pre>Example:<code>
  * FeatureTypeInfoDTO ftiDto = new FeatureTypeInfoDTO();
  * ftiDto.setName("My Feature Type");
  * ftiDto.setTitle("The Best Feature Type");
  * ftiDto.setSRS(23769);
  * </code></pre>
  *
  * @author dzwiers, Refractions Research, Inc.
  * @version $Id$
  */
 public final class FeatureTypeInfoDTO implements DataTransferObject {
     /** The Id of the datastore which should be used to get this featuretype. */
     private String dataStoreId;
 
     /** A bounding box in EPSG:4326 for this featuretype */
     private Envelope latLongBBox;
 
     /** A bounding box in native's CRS for this featuretype */
     private Envelope nativeBBox;
 
     /** native wich EPGS code for the FeatureTypeInfo */
     private int SRS;
     
     /** either reproject or force, see {@link FeatureTypeInfo} */
     private int SRSHandling;
 
     /** Copy of the featuretype schema as a string. */
     private List schema;
 
     /** The schema name. */
     private String schemaName;
 
     /**
      * The schemaBase name.
      *
      * <p>
      * Example NullType, or PointPropertyType.
      * </p>
      */
     private String schemaBase;
 
     /**
      * The featuretype name.
      *
      * <p>
      * Often related to the title - like bc_roads_Type
      * </p>
      */
     private String name;
     private String wmsPath;
 
     /**
      * The featuretype directory name. This is used to write to, and is  stored
      * because it may be longer than the name, as this often includes
      * information about the source of the featuretype.
      */
     private String dirName;
 
     /** The featuretype title */
     private String title;
 
     /** The feature type abstract, short explanation of this featuretype. */
     private String _abstract;
 
     /** A list of keywords to associate with this featuretype. */
     private List keywords;
 
     /** A list of metadataURLs to associate with this featuretype. */
     private List metadataLinks;
 
     /** Used to limit the number of decimals used in GML representations. */
     private int numDecimals;
 
     /**
      * the list of exposed attributes. If the list is empty or not present at
      * all, all the FeatureTypeInfo's attributes are exposed, if is present,
      * only those oattributes in this list will be exposed by the services
      */
     private Filter definitionQuery = null;
 
     /** The default style name. */
     private String defaultStyle;
 
     /** Other Style Names. */
     private ArrayList styles = new ArrayList();
 
     // Modif C. Kolbowicz - 06/10/2004 
 
     /** The legend icon description. */
     private LegendURLDTO legendURL;
 
     //-- Modif C. Kolbowicz - 06/10/2004 
 
     /** Holds the location of the file that contains schema information.*/
     private File schemaFile;
     
     /**
      * This value is added the headers of generated maps, marking them as being both
      * "cache-able" and designating the time for which they are to remain valid.
      *  The specific header added is "Cache-Control: max-age="
      */
     private String cacheMaxAge;
 
     /**
      * Should we be adding the CacheControl: max-age header to outgoing maps which include this layer?
      */
     private boolean cachingEnabled;
     
     /**
      * The maximum number of features to be served for this feature type (it's understood
      * it's less than the global maxFeatures). 0 is used as the "no limit" flag
      */
      private int maxFeatures = 0;
 
     /**
      * FeatureTypeInfo constructor.
      *
      * <p>
      * does nothing
      * </p>
      */
     public FeatureTypeInfoDTO() {
     }
 
 
     /**
      * FeatureTypeInfo constructor.
      *
      * <p>
      * Creates a copy of the FeatureTypeInfo provided. If the FeatureTypeInfo
      * provided  is null then default values are used. All the data structures
      * are cloned.
      * </p>
      *
      * @param dto The featuretype to copy.
      *
      * @throws NullPointerException DOCUMENT ME!
      */
     public FeatureTypeInfoDTO(FeatureTypeInfoDTO dto) {
         if (dto == null) {
             throw new NullPointerException("Non null FeatureTypeInfoDTO required");
         }
 
         dataStoreId = dto.getDataStoreId();
         latLongBBox = CloneLibrary.clone(dto.getLatLongBBox());
         nativeBBox = CloneLibrary.clone(dto.getNativeBBox());
         SRS = dto.getSRS();
         SRSHandling = dto.getSRSHandling();
         schema = dto.getSchemaAttributes();
         name = dto.getName();
         wmsPath = dto.getWmsPath();
         title = dto.getTitle();
         _abstract = dto.getAbstract();
         numDecimals = dto.getNumDecimals();
         definitionQuery = dto.getDefinitionQuery();
 
         // Modif C. Kolbowicz - 06/10/2004
         legendURL = dto.getLegendURL();
 
         //-- Modif C. Kolbowicz - 06/10/2004 
         try {
             keywords = CloneLibrary.clone(dto.getKeywords()); //clone?
         } catch (Exception e) {
             keywords = new LinkedList();
         }
 
         try {
             metadataLinks = CloneLibrary.clone(dto.getMetadataLinks()); //clone?
         } catch (Exception e) {
             metadataLinks = new LinkedList();
         }
 
         defaultStyle = dto.getDefaultStyle();
         styles = dto.getStyles();
 
         dirName = dto.getDirName();
         schemaName = dto.getSchemaName();
         schemaBase = dto.getSchemaBase();
 
         cachingEnabled = dto.isCachingEnabled();
         cacheMaxAge = dto.getCacheMaxAge();
         
         maxFeatures = dto.getMaxFeatures();
     }
 
     /**
      * Implement clone as a deep copy.
      *
      * @return A copy of this FeatureTypeInfo
      *
      * @see java.lang.Object#clone()
      */
     public Object clone() {
         return new FeatureTypeInfoDTO(this);
     }
 
     /**
      * Implement equals.
      *
      * <p>
      * recursively tests to determine if the object passed in is a copy of this
      * object.
      * </p>
      *
      * @param obj The FeatureTypeInfo object to test.
      *
      * @return true when the object passed is the same as this object.
      *
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object obj) {
         if ((obj == null) || !(obj instanceof FeatureTypeInfoDTO)) {
             return false;
         }
 
         FeatureTypeInfoDTO f = (FeatureTypeInfoDTO) obj;
         boolean r = true;
         r = r && (dataStoreId == f.getDataStoreId());
 
         if (latLongBBox != null) {
             r = r && latLongBBox.equals(f.getLatLongBBox());
         } else if (f.getLatLongBBox() != null) {
             return false;
         }
 
         if (nativeBBox != null) {
             r = r && nativeBBox.equals(f.getNativeBBox());
         } else if (f.getNativeBBox() != null) {
             return false;
         }
 
         r = r && (SRS == f.getSRS());
         
         r = r && (SRSHandling == f.getSRSHandling());
 
         if (schema != null) {
             r = r && schema.equals(f.getSchemaAttributes());
         } else if (f.getSchemaAttributes() != null) {
             return false;
         }
 
         // Modif C. Kolbowicz - 06/10/2004
         if (legendURL != null) {
             r = r && schema.equals(f.getLegendURL());
         } else if (f.getLegendURL() != null) {
             return false;
         }
 
         //-- Modif C. Kolbowicz - 06/10/2004 
         r = r && (defaultStyle == f.getDefaultStyle());
         r = r && (styles == f.getStyles());
         r = r && (name == f.getName());
         r = r && (wmsPath == f.getWmsPath());
         r = r && (title == f.getTitle());
         r = r && (_abstract == f.getAbstract());
         r = r && (numDecimals == f.getNumDecimals());
        r = r && (maxFeatures == f.getMaxFeatures());
 
         if (definitionQuery != null) {
             r = r && definitionQuery.equals(f.getDefinitionQuery());
         } else if (f.getDefinitionQuery() != null) {
             return false;
         }
 
         if (keywords != null) {
             r = r && EqualsLibrary.equals(keywords, f.getKeywords());
         } else if (f.getKeywords() != null) {
             return false;
         }
 
         if (metadataLinks != null) {
             r = r && EqualsLibrary.equals(metadataLinks, f.getMetadataLinks());
         } else if (f.getMetadataLinks() != null) {
             return false;
         }
 
         r = r && (dirName == f.getDirName());
         r = r && (schemaName == f.getSchemaName());
         r = r && (schemaBase == f.getSchemaBase());
 
         r = r && (isCachingEnabled() == f.isCachingEnabled());
         r = r && ((getCacheMaxAge() != null) && getCacheMaxAge().equals(f.getCacheMaxAge()));
 
         return r;
     }
 
     /**
      * Implement hashCode.
      *
      * @return Service hashcode or 0
      *
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
         int r = 1;
 
         if (name != null) {
             r *= name.hashCode();
         }
 
         if (dataStoreId != null) {
             r *= dataStoreId.hashCode();
         }
 
         // Modif C. Kolbowicz - 06/10/2004
         if (legendURL != null) {
             r *= legendURL.hashCode();
         }
 
         //-- Modif C. Kolbowicz - 06/10/2004 
         if (title != null) {
             r *= title.hashCode();
         }
 
         if (SRS != 0) {
             r = SRS % r;
         }
         
         r += SRSHandling;
         
         if (cacheMaxAge != null) {
             r *= cacheMaxAge.hashCode();
         }
 
         if (cachingEnabled) {
             r += 1;
         }
        
        r += maxFeatures;
 
         return r;
     }
 
     /**
      * Short description of FeatureType.
      *
      * @return Description of FeatureType
      */
     public String getAbstract() {
         return _abstract;
     }
 
     /**
      * Identifier of DataStore used to create FeatureType.
      *
      * @return DataStore identifier
      */
     public String getDataStoreId() {
         return dataStoreId;
     }
 
     /**
      * List of keywords (limitied to text).
      *
      * @return List of Keywords about this FeatureType
      */
     public List getKeywords() {
         return keywords;
     }
 
     /**
      * List of metadataURLs (limited to text).
      *
      * @return List of metadataURLs about this FeatureType
      */
     public List getMetadataLinks() {
         return metadataLinks;
     }
 
     /**
      * Convience method for dataStoreId.typeName.
      *
      * <p>
      * This key may be used to store this FeatureType in a Map for later.
      * </p>
      *
      * @return dataStoreId.typeName
      */
     public String getKey() {
         return getDataStoreId() + DataConfig.SEPARATOR + getName();
     }
 
     /**
      * The extent of this FeatureType.
      *
      * <p>
      * Extent is measured against the tranditional LatLong coordinate system.
      * </p>
      *
      * @return Envelope of FeatureType
      */
     public Envelope getLatLongBBox() {
         return latLongBBox;
     }
 
     /**
      * Sets the feature type's envelope in its
      * native CRS for cached storage.
      *
      * @param envelope
      */
     public void setNativeBBox(Envelope envelope) {
         nativeBBox = envelope;
     }
 
     /**
      * The extent of this FeatureType.<p>Extent is measured against the
      * FeatureType's native coordinate system.</p>
      *
      * @return Envelope of FeatureType
      */
     public Envelope getNativeBBox() {
         return nativeBBox;
     }
 
     /**
      * Name of featureType, must match typeName provided by DataStore.
      *
      * @return typeName of FeatureType
      */
     public String getName() {
         return name;
     }
 
     /**
      * Spatial Reference System for FeatureType.
      *
      * <p>
      * Makes use of the standard EPSG codes?
      * </p>
      *
      * @return WPSG Spatial Reference System for FeatureType
      */
     public int getSRS() {
         return SRS;
     }
     
     public int getSRSHandling() {
         return SRSHandling;
     }
 
     /**
      * Title used to identify FeatureType to user.
      *
      * @return FeatureType title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * setAbstract purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setAbstract(String string) {
         _abstract = string;
     }
 
     /**
      * setDataStore purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param store
      */
     public void setDataStoreId(String store) {
         dataStoreId = store;
     }
 
     /**
      * setKeywords purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param list
      */
     public void setKeywords(List list) {
         keywords = list;
     }
 
     /**
      * Sets the MetadataURL list for this feature type
      *
      * @param list
      */
     public void setMetadataLinks(List list) {
         metadataLinks = list;
     }
 
     /**
      * setKeywords purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      *
      * @return boolean true when added.
      */
     public boolean addKeyword(String key) {
         if (keywords == null) {
             keywords = new LinkedList();
         }
 
         return keywords.add(key);
     }
 
     /**
      * setKeywords purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param key
      *
      * @return true whwn removed
      */
     public boolean removeKeyword(String key) {
         return keywords.remove(key);
     }
 
     /**
      * setLatLongBBox purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param envelope
      */
     public void setLatLongBBox(Envelope envelope) {
         latLongBBox = envelope;
     }
 
     /**
      * setName purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setName(String string) {
         name = string;
     }
 
     /**
      * setSRS purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param i
      */
     public void setSRS(int i) {
         SRS = i;
     }
     
     public void setSRSHandling(int i) {
         SRSHandling = i;
     }
 
     /**
      * setTitle purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setTitle(String string) {
         title = string;
     }
 
     /**
      * getNumDecimals purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public int getNumDecimals() {
         return numDecimals;
     }
 
     /**
      * setNumDecimals purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param i
      */
     public void setNumDecimals(int i) {
         numDecimals = i;
     }
 
     /**
      * getDefinitionQuery purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public Filter getDefinitionQuery() {
         return definitionQuery;
     }
 
     /**
      * setDefinitionQuery purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param filter
      */
     public void setDefinitionQuery(Filter filter) {
         definitionQuery = filter;
     }
 
     /**
      * getDefaultStyle purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getDefaultStyle() {
         //HACK: So our UI doesn't seem to allow the setting of styles or 
         //default styles or anything, despite the fact that shit chokes when none
         //is present.  This is making it so the beta release can not have any data
         //stores added to it.  This is a hacky ass way to get around it, just 
         //write out a normal style if it is null.  This can obviously be done 
         //better, and I have no idea why this default style shit is required - wfs
         //does not care about a style.  Should be able to seamlessly at least do
         //something for wms.
         if ((defaultStyle == null) || defaultStyle.equals("")) {
             defaultStyle = "normal";
         }
 
         return defaultStyle;
     }
 
     /**
      * setDefaultStyle purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setDefaultStyle(String string) {
         defaultStyle = string;
     }
 
     /**
      * getSchema purpose.
      *
      * <p>
      * Returns An ordered list of AttributeTypeInfoDTOs
      * </p>
      *
      * @return An ordered list of AttributeTypeInfoDTOs
      */
     public List getSchemaAttributes() {
         return schema;
     }
 
     /**
      * setSchema purpose.
      *
      * <p>
      * Stores a list of AttributeTypeInfoDTOs.
      * </p>
      *
      * @param schemaElements An ordered list of AttributeTypeInfoDTOs
      */
     public void setSchemaAttributes(List schemaElements) {
         this.schema = schemaElements;
     }
 
     /**
      * getDirName purpose.
      *
      * <p>
      * Returns the featuretype directory name.
      * </p>
      *
      * @return the featuretype directory name
      */
     public String getDirName() {
         return dirName;
     }
 
     /**
      * setDirName purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setDirName(String string) {
         dirName = string;
     }
 
     /**
      * getSchemaName purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @return
      */
     public String getSchemaName() {
         return schemaName;
     }
 
     /**
      * setSchemaName purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setSchemaName(String string) {
         schemaName = string;
     }
 
     /**
      * getSchemaBase purpose.
      *
      * <p>
      * Usually generated as: getName + "_Type"
      * </p>
      *
      * @return
      */
     public String getSchemaBase() {
         return schemaBase;
     }
 
     /**
      * setSchemaBase purpose.
      *
      * <p>
      * Description ...
      * </p>
      *
      * @param string
      */
     public void setSchemaBase(String string) {
         schemaBase = string;
     }
 
     // Modif C. Kolbowicz - 06/10/2004
 
     /**
      * Gets a reference to an optional legend icon.
      *
      * @return Value of property legendURL.
      */
     public LegendURLDTO getLegendURL() {
         return this.legendURL;
     }
 
     //-- Modif C. Kolbowicz - 06/10/2004
     // Modif C. Kolbowicz - 06/10/2004
 
     /**
      * Returns a reference to an optional legend icon.
      *
      * @param legendURL New value of property legendURL.
      */
     public void setLegendURL(LegendURLDTO legendURL) {
         this.legendURL = legendURL;
     }
 
     /**
      * Gets the schema.xml file associated with this FeatureType.  This is set
      * during the reading of configuration, it is not persisted as an element
      * of the FeatureTypeInfoDTO, since it is just whether the schema.xml file
      * was persisted, and its location.  If there is no schema.xml file then
      * this method will return a File object with the location where the schema
      * file would be located, but the file will return false for exists().
      */
     public File getSchemaFile() {
         return this.schemaFile;
     }
 
     /**
      * Sets the schema file.  Note that a non-exisiting file can be set here,
      * to indicate that no schema.xml file is present.
      */
     public void setSchemaFile(File schemaFile) {
         this.schemaFile = schemaFile;
     }
 
     //-- Modif C. Kolbowicz - 06/10/2004
     public String toString() {
         return "[FeatureTypeInfoDTO: " + name + ", datastoreId: " + dataStoreId + ", latLongBBOX: "
         + latLongBBox + "\n  SRS: " + SRS + ", schema:" + schema + ", schemaName: " + schemaName
         + ", dirName: " + dirName + ", title: " + title + "\n  definitionQuery: " + definitionQuery
         + ", defaultStyle: " + defaultStyle + ", legend icon: " + legendURL + ", caching?: "
        + cachingEnabled + ", max-age: " + cacheMaxAge + ", maxFeatures: " + maxFeatures;
     }
 
     public String getWmsPath() {
         return wmsPath;
     }
 
     public void setWmsPath(String wmsPath) {
         this.wmsPath = wmsPath;
     }
 
     public boolean isCachingEnabled() {
         return cachingEnabled;
     }
 
     public void setCachingEnabled(boolean cachingEnabled) {
         this.cachingEnabled = cachingEnabled;
     }
 
     public String getCacheMaxAge() {
         return cacheMaxAge;
     }
 
     public void setCacheMaxAge(String cacheMaxAge) {
         this.cacheMaxAge = cacheMaxAge;
     }
 
     public ArrayList getStyles() {
         return styles;
     }
 
     public void addStyle(String styleName) {
         if (!styles.contains(styleName)) {
             styles.add(styleName);
         }
     }
 
     public void setStyles(ArrayList styles) {
         this.styles = styles;
     }
     
     public int getMaxFeatures() {
         return maxFeatures;
     }
 
 
     public void setMaxFeatures(int maxFeatures) {
         this.maxFeatures = maxFeatures;
     }
 }
