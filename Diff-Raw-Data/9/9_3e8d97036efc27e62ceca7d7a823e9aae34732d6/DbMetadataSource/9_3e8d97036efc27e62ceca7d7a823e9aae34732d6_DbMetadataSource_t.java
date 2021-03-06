 /*
  * This file is part of rasdaman community.
  *
  * Rasdaman community is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser  General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Rasdaman community is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright 2003 - 2010 Peter Baumann / rasdaman GmbH.
  *
  * For more information please see <http://www.rasdaman.org>
  * or contact Peter Baumann via <baumann@rasdaman.com>.
  */
 package petascope.core;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Savepoint;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.hsqldb.lib.StringUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import petascope.ConfigManager;
 import petascope.exceptions.ExceptionCode;
 import petascope.exceptions.PetascopeException;
 import petascope.exceptions.RasdamanException;
 import petascope.exceptions.SecoreException;
 import petascope.util.CrsUtil;
 import petascope.util.Pair;
 import petascope.util.Vectors;
 import petascope.util.XMLSymbols;
 import petascope.util.ras.RasQueryResult;
 import petascope.util.ras.RasUtil;
 import petascope.wcps.server.core.*;
 import petascope.wcs2.parsers.GetCoverageRequest;
 import petascope.wcs2.templates.Templates;
 
 /**
  * The DbMetadataSource is a IMetadataSource that uses a relational database. It
  * keeps a global connection which is reused on future requests, as well as between
  * threads. Before each read, the connection is verified to be valid, and
  * recreated if necessary. This IMetadataSource is not particularly efficient,
  * because it accesses the database at least once for every read. To increase
  * efficiency, wrap a CachedMetadataSource around this one.
  *
  */
 public class DbMetadataSource implements IMetadataSource {
     
     private static Logger log = LoggerFactory.getLogger(DbMetadataSource.class);
     
     /*
      *  Petascopedb Tables and Fields
      */
     
     public static final String TABLES_PREFIX = "ps9_";
 
     /* Dictionary tables (coverage-independent) */
     // TABLE_EXTRAMETADATA_TYPE : types of extra-metadata for a coverage (eg GMLCOV, OWS, etc.)
     public static final String TABLE_EXTRAMETADATA_TYPE = TABLES_PREFIX + "extra_metadata_type";
     public static final String EXTRAMETADATA_TYPE_ID    = "id";
     public static final String EXTRAMETADATA_TYPE_TYPE  = "type";
     // TABLE_CRS : list of /single/ Coordinate Reference Systems (no compound CRS)
     public static final String TABLE_CRS                = TABLES_PREFIX + "crs";
     public static final String CRS_ID                   = "id";
     public static final String CRS_URI                  = "uri";
     // TABLE_FORMAT : list of formats abbreviations
     public static final String TABLE_FORMAT             = TABLES_PREFIX + "format";
     public static final String FORMAT_ID                = "id";
     public static final String FORMAT_NAME              = "name";
     public static final String FORMAT_MIME_ID           = "mime_type_id";
     public static final String FORMAT_GDAL_ID           = "gdal_id";
     // TABLE_GDAL_FORMAT : list of GDAL-understandable formats (see $ gdal_translate --formats)
     public static final String TABLE_GDAL_FORMAT        = TABLES_PREFIX + "gdal_format";
     public static final String GDAL_FORMAT_ID           = "id";
     public static final String GDAL_FORMAT_GDAL_ID      = "gdal_id";
     public static final String GDAL_FORMAT_DESCRIPTION  = "description";
     // TABLE_MIME_TYPE : list of MIME types (WCS requests use this)
     public static final String TABLE_MIME_TYPE          = TABLES_PREFIX + "mime_type";
     public static final String MIME_TYPE_ID             = "id";
     public static final String MIME_TYPE_MIME           = "mime_type";
     // TABLE_GML_SUBTYPE : list of GML coverage types (eg AbstractCoverage, RectifiedGridCoverage, MultiPointCoverage, etc)
     public static final String TABLE_GML_SUBTYPE        = TABLES_PREFIX + "gml_subtype";
     public static final String GML_SUBTYPE_ID           = "id";
     public static final String GML_SUBTYPE_SUBTYPE      = "subtype";
     public static final String GML_SUBTYPE_PARENT       = "subtype_parent";
     // TABLE_INTERVAL : to determine allowed values in a coverage attribute space (range-type)
     public static final String TABLE_INTERVAL           = TABLES_PREFIX + "interval";
     public static final String INTERVAL_ID              = "id";
     public static final String INTERVAL_MIN             = "min";
     public static final String INTERVAL_MAX             = "max";
     // TABLE_UOM : Units of Measure (UoM)
     public static final String TABLE_UOM                = TABLES_PREFIX + "uom";
     public static final String UOM_ID                   = "id";
     public static final String UOM_CODE                 = "code";
     // TABLE_QUANTITY : quantities (see OGC SWE)
     public static final String TABLE_QUANTITY           = TABLES_PREFIX + "quantity";
     public static final String QUANTITY_ID              = "id";
     public static final String QUANTITY_UOM_ID          = "uom_id";
     public static final String QUANTITY_LABEL           = "label";
     public static final String QUANTITY_DESCRIPTION     = "description";
     public static final String QUANTITY_DEFINITION      = "definition_uri";
     public static final String QUANTITY_SIGNIFICANT_FIGURES = "significant_figures";    
     // TABLE_INTERVAL_QUANTITY : association table between TABLE_INTERVAL and TABLE_QUANTITY
     public static final String TABLE_INTERVAL_QUANTITY  = TABLES_PREFIX + "interval_quantity";
     public static final String INTERVAL_QUANTITY_IID    = "quantity_id";
     public static final String INTERVAL_QUANTITY_QID    = "interval_id";
     // TABLE_RANGE_DATATYPE : WCPS range types [OGC 08-068r2, Tab.2]
     public static final String TABLE_RANGE_DATATYPE     = TABLES_PREFIX + "range_data_type";
     public static final String RANGE_DATATYPE_ID        = "id";
     public static final String RANGE_DATATYPE_NAME      = "name";
     public static final String RANGE_DATATYPE_MEANING   = "meaning";
     
     /* WCS Service-related tables */
     // TABLE_SERVICE_IDENTIFICATION : metadata for the WCS service
     public static final String TABLE_SERVICE_IDENTIFICATION         = TABLES_PREFIX + "service_identification";
     public static final String SERVICE_IDENTIFICATION_ID            = "id";
     public static final String SERVICE_IDENTIFICATION_TITLE         = "title";
     public static final String SERVICE_IDENTIFICATION_ABSTRACT      = "abstract";
     // TABLE_KEYWORD : keywords for the WCS service identification
     public static final String TABLE_KEYWORD                        = TABLES_PREFIX + "keyword";
     public static final String KEYWORD_ID                           = "id";
     public static final String KEYWORD_VALUE                        = "value";
     public static final String KEYWORD_TYPE                         = "type";
     public static final String KEYWORD_TYPE_CODESPACE               = "type_codespace";
     // TABLE_SERVICE_KEYWORD : association table between TABLE_SERVICE_IDENTIFICATION and TABLE_KEYWORD
     public static final String TABLE_SERVICE_KEYWORD                = TABLES_PREFIX + "service_keyword";
     public static final String SERVICE_KEYWORD_SID                  = "service_id";
     public static final String SERVICE_KEYWORD_KID                  = "keyword_id";
     // TABLE_SERVICE_PROVIDER : metadata for the WCS service provider
     public static final String TABLE_SERVICE_PROVIDER               = TABLES_PREFIX + "service_provider";
     public static final String SERVICE_PROVIDER_ID                  = "id";
     public static final String SERVICE_PROVIDER_NAME                = "name";
     public static final String SERVICE_PROVIDER_SITE                = "site";
     public static final String SERVICE_PROVIDER_CONTACT_NAME        = "contact_individual_name";
     public static final String SERVICE_PROVIDER_CONTACT_POSITION    = "contact_position_name";
     public static final String SERVICE_PROVIDER_CONTACT_PHONE       = "contact_phone";
     public static final String SERVICE_PROVIDER_CONTACT_DELIVERY    = "contact_delivery_point";
     public static final String SERVICE_PROVIDER_CONTACT_CITY        = "contact_city";
     public static final String SERVICE_PROVIDER_CONTACT_AREA        = "contact_administrative_area";
     public static final String SERVICE_PROVIDER_CONTACT_PCODE       = "contact_postal_code";
     public static final String SERVICE_PROVIDER_CONTACT_COUNTRY     = "contact_country";
     public static final String SERVICE_PROVIDER_CONTACT_EMAIL       = "contact_email_address";
     public static final String SERVICE_PROVIDER_CONTACT_HOURS       = "contact_hours_of_service";
     public static final String SERVICE_PROVIDER_CONTACT_INSTRUCTIONS = "contact_instructions";
     public static final String SERVICE_PROVIDER_CONTACT_ROLE        = "contact_role";
     
     /* Coverage-related tables */
     // TABLE_COVERAGE : root table of a gml:*Coverage
     public static final String TABLE_COVERAGE                   = TABLES_PREFIX + "coverage";
     public static final String COVERAGE_ID                      = "id";
     public static final String COVERAGE_NAME                    = "name";
     public static final String COVERAGE_GML_TYPE_ID             = "gml_type_id";
     public static final String COVERAGE_NATIVE_FORMAT_ID        = "native_format_id";
     // TABLE_EXTRAMETADATA : descriptive metadata 
     public static final String TABLE_EXTRAMETADATA              = TABLES_PREFIX + "extra_metadata";
     public static final String EXTRAMETADATA_ID                 = "id";
     public static final String EXTRAMETADATA_COVERAGE_ID        = "coverage_id";
     public static final String EXTRAMETADATA_METADATA_TYPE_ID   = "metadata_type_id";
     public static final String EXTRAMETADATA_VALUE              = "value";
     // Domain-set //
     // TABLE_DOMAINSET : common geometric information for any type of coverage
     public static final String TABLE_DOMAINSET                  = TABLES_PREFIX + "domain_set";
     public static final String DOMAINSET_COVERAGE_ID            = "coverage_id";
     public static final String DOMAINSET_NATIVE_CRS_ID          = "native_crs_id";
     // TABLE_GRIDDED_DOMAINSET : geometry information specific to gridded coverages
     public static final String TABLE_GRIDDED_DOMAINSET          = TABLES_PREFIX + "gridded_domain_set";
     public static final String GRIDDED_DOMAINSET_COVERAGE_ID    = "coverage_id";  
     public static final String GRIDDED_DOMAINSET_ORIGIN         = "grid_origin"; 
     // TABLE_GRID_AXIS : axis-specific information, regardless of its regularity and rectilinearity
     public static final String TABLE_GRID_AXIS                  = TABLES_PREFIX + "grid_axis";
     public static final String GRID_AXIS_ID                     = "id"; 
     public static final String GRID_AXIS_COVERAGE_ID            = "gridded_coverage_id"; 
     public static final String GRID_AXIS_RASDAMAN_ORDER         = "rasdaman_order";
     // TABLE_RECTILINEAR_AXIS : if an axis is rectilinear, then we can define it with offset vectors
     public static final String TABLE_RECTILINEAR_AXIS           = TABLES_PREFIX + "rectilinear_axis";
     public static final String RECTILINEAR_AXIS_ID              = "grid_axis_id";
     public static final String RECTILINEAR_AXIS_OFFSET_VECTOR   = "offset_vector";
     // TABLE_VECTOR_COEFFICIENTS : /long/ table, storing the coefficients (c*offset_vector) of an irregularly-spaced rectilinear axis
     public static final String TABLE_VECTOR_COEFFICIENTS        = TABLES_PREFIX + "vector_coefficients";
     public static final String VECTOR_COEFFICIENTS_AXIS_ID      = "grid_axis_id";
     public static final String VECTOR_COEFFICIENTS_COEFFICIENT  = "coefficient";
     public static final String VECTOR_COEFFICIENTS_ORDER        = "coefficient_order";    
     // Range-set //
     // TABLE_RANGE_SET : hub for different range-set options for a coverage: rasdaman (-> TABLE_RASDAMAN_COLLECTION), PostGIS, etc.
     public static final String TABLE_RANGESET          = TABLES_PREFIX + "range_set";
     public static final String RANGESET_ID             = "id";
     public static final String RANGESET_COVERAGE_ID    = "coverage_id";
     public static final String RANGESET_STORAGE_TABLE  = "storage_table";
     public static final String RANGESET_STORAGE_ID     = "storage_id";
     // TABLE_RASDAMAN_COLLECTION : list of rasdaman collections (1 coverage = 1 MDD // 1 collection = 1+ MDDs)
     public static final String TABLE_RASDAMAN_COLLECTION = TABLES_PREFIX + "rasdaman_collection";
     public static final String RASDAMAN_COLLECTION_ID   = "id";
     public static final String RASDAMAN_COLLECTION_NAME = "name";
     public static final String RASDAMAN_COLLECTION_OID  = "oid";
     public static final String RASDAMAN_COLLECTION_BASE_TYPE = "base_type";
 
     // Range-type //
     // TABLE_RANGETYPE_COMPONENT : components (aka bands, or channels) of a coverage
     public static final String TABLE_RANGETYPE_COMPONENT        = TABLES_PREFIX + "range_type_component";
     public static final String RANGETYPE_COMPONENT_ID           = "id";
     public static final String RANGETYPE_COMPONENT_COVERAGE_ID  = "coverage_id";
     public static final String RANGETYPE_COMPONENT_NAME         = "name";
     public static final String RANGETYPE_COMPONENT_TYPE_ID      = "data_type_id";
     public static final String RANGETYPE_COMPONENT_ORDER        = "component_order";
     public static final String RANGETYPE_COMPONENT_FIELD_TABLE  = "field_table";
     public static final String RANGETYPE_COMPONENT_FIELD_ID     = "field_id";    
     /* ~end TABLES */
     
     /* Status variables */
     private boolean initializing;
     private boolean checkAtInit;
     
     /* Contents of (static) dictionary-tables */
     // TODO: map DB tables to dedicated classes instead of Map objects
     private Map<Integer, String> crss;
     private Map<Integer, String> extraMetadataTypes;
     private Map<Integer, String> gmlSubTypes;
     private Map<String,  String> gmlChildParent; // GML type -> parent type    
     private Map<Integer, String> mimeTypes;
     private Map<String,  String> supportedFormats; // format -> MIME type    
     private Map<String,  String> gdalFormatsIds; // GDAL code -> format name    
     private Map<Integer, String> quantities; // id -> quantity's UoM
     private Map<Integer, Pair<BigDecimal,BigDecimal>> intervals; // id -> (min,max)
     private Set<Pair<Integer, Integer>> quantityInterval; // A quantity can be constrained by 1+ intervals of allowed values
     private Map<Integer, String> rangeDataTypes;
 
     /* Contents of (static) dictionary-tables (reversed) */
     private Map<String, Integer> revCrss;    
     private Map<String, Integer> revExtraMetadataTypes;
     private Map<String, Integer> revGmlSubTypes;
     private Map<String, Integer> revMimeTypes;
     private Map<String, String>  revSupportedFormats; // MIME type -> format
     private Map<String, String>  revGdalFormatsIds; // format name -> GDAL code 
     private Map<Pair<BigDecimal,BigDecimal>,  Integer> revIntervals; // (min,max) -> id
     private Map<String, Integer> revRangeDataTypes;
 
     /* Database access info */
     private static final String pgDriver = "org.postgresql.Driver";
     private String driver;
     private String pass;
     private String url;
     private String user;
     
     /* Global database key variables */
     private Connection conn;
     private Savepoint savepoint;
     private String query;
     
     /* Cache */
     private Map<String, CoverageMetadata> cache = new HashMap<String, CoverageMetadata>();
     
     /*------------------------------------------------*/
     
     public DbMetadataSource(String driver, String url, String user, String pass) throws PetascopeException, SecoreException {
         this(driver, url, user, pass, true);
     }
     
     public DbMetadataSource(String driver, String url, String user, String pass, boolean checkAtInit) throws PetascopeException, SecoreException {
         try {
             this.driver = driver;
             Class.forName(driver).newInstance();
         } catch (ClassNotFoundException e) {
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error: Could not find JDBC driver: " + driver, e);
         } catch (InstantiationException e) {
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error: Could not instantiate JDBC driver: " + driver, e);
         } catch (IllegalAccessException e) {
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error: Access denied to JDBC driver: " + driver, e);
         }
         
         this.driver = driver;
         this.url = url;
         this.user = user;
         this.pass = pass;
         Statement s = null;
         
         try {
             
             /*
              *  Read contents of static metadata tables
              */
             
             ensureConnection();
             s = conn.createStatement();
             String sqlQuery; // buffer for SQL queries
             
             /* TABLE_EXTRAMETADATA_TYPE */
             extraMetadataTypes    = new HashMap<Integer, String>();
             revExtraMetadataTypes = new HashMap<String, Integer>();
             sqlQuery = 
                     " SELECT " + EXTRAMETADATA_TYPE_ID   + ", " 
                                + EXTRAMETADATA_TYPE_TYPE + 
                     " FROM "   + TABLE_EXTRAMETADATA_TYPE
                     ;
             log.debug("SQL query: " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             while (r.next()) {
                 extraMetadataTypes.put(   r.getInt(EXTRAMETADATA_TYPE_ID),      r.getString(EXTRAMETADATA_TYPE_TYPE));
                 revExtraMetadataTypes.put(r.getString(EXTRAMETADATA_TYPE_TYPE), r.getInt(EXTRAMETADATA_TYPE_ID));
             }
            
             /* TABLE_CRS */
             crss    = new HashMap<Integer, String>();
             revCrss = new HashMap<String, Integer>();
             sqlQuery =
                     " SELECT " + CRS_ID  + ", " 
                                + CRS_URI + 
                     " FROM "   + TABLE_CRS
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                 crss.put(   r.getInt(CRS_ID),     r.getString(CRS_URI));
                 revCrss.put(r.getString(CRS_URI), r.getInt(CRS_ID));
             }
     
             /* TABLE_GML_SUBTYPE */
             gmlSubTypes    = new HashMap<Integer, String>();
             revGmlSubTypes = new HashMap<String, Integer>();
             gmlChildParent = new HashMap<String, String>();
             sqlQuery =
                     " SELECT a." + GML_SUBTYPE_ID      +  ", "  +
                            " a." + GML_SUBTYPE_SUBTYPE +  ", "  + 
                            " b." + GML_SUBTYPE_SUBTYPE + " AS " + GML_SUBTYPE_PARENT +
                                " FROM " + TABLE_GML_SUBTYPE + " AS a " +
                     " LEFT OUTER JOIN " + TABLE_GML_SUBTYPE + " AS b " +
                     " ON a." + GML_SUBTYPE_PARENT + " = b." + GML_SUBTYPE_ID
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                    gmlSubTypes.put(r.getInt(GML_SUBTYPE_ID), r.getString(GML_SUBTYPE_SUBTYPE));                
                 revGmlSubTypes.put(r.getString(GML_SUBTYPE_SUBTYPE), r.getInt(GML_SUBTYPE_ID));
                 gmlChildParent.put(r.getString(GML_SUBTYPE_SUBTYPE), 
                            null == r.getString(GML_SUBTYPE_PARENT) ? "" : r.getString(GML_SUBTYPE_PARENT));
             }
                             
             /* FORMAT <-> GDAL <-> MIME */
             mimeTypes           = new HashMap<Integer, String>();
             revMimeTypes        = new HashMap<String, Integer>();
             gdalFormatsIds      = new HashMap<String, String>();
             revGdalFormatsIds   = new HashMap<String, String>();
             supportedFormats    = new HashMap<String, String>();
             revSupportedFormats = new HashMap<String, String>();
             // Aliases (more robust against fields of same name over different tables)
             String formatNameAlias = "f" + FORMAT_NAME;
             String mimeIdAlias     = "m" + MIME_TYPE_ID;
             String mimeTypeAlias   = "m" + MIME_TYPE_MIME;
             String gdalIdAlias     = "g" + GDAL_FORMAT_GDAL_ID;
             sqlQuery =
                     " SELECT " + TABLE_FORMAT      + "." + FORMAT_NAME         + " AS " + formatNameAlias + ","
                                + TABLE_MIME_TYPE   + "." + MIME_TYPE_ID        + " AS " + mimeIdAlias + ","
                                + TABLE_MIME_TYPE   + "." + MIME_TYPE_MIME      + " AS " + mimeTypeAlias + ","
                                + TABLE_GDAL_FORMAT + "." + GDAL_FORMAT_GDAL_ID + " AS " + gdalIdAlias +
                     " FROM "   + TABLE_MIME_TYPE   +
                     " LEFT OUTER JOIN " + TABLE_FORMAT   + " ON "
                                + TABLE_MIME_TYPE   + "." + MIME_TYPE_ID   + "="
                                + TABLE_FORMAT      + "." + FORMAT_MIME_ID +
                     " LEFT OUTER JOIN " + TABLE_GDAL_FORMAT + " ON "
                                + TABLE_GDAL_FORMAT + "." + GDAL_FORMAT_ID + "="
                                + TABLE_FORMAT      + "." + FORMAT_GDAL_ID
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                           mimeTypes.put(r.getInt(mimeIdAlias),      r.getString(mimeTypeAlias));
                        revMimeTypes.put(r.getString(mimeTypeAlias), r.getInt(mimeIdAlias));
                    supportedFormats.put(r.getString(formatNameAlias), r.getString(mimeTypeAlias));
                 revSupportedFormats.put(r.getString(mimeTypeAlias),   r.getString(formatNameAlias));
                      gdalFormatsIds.put(r.getString(gdalIdAlias),     r.getString(formatNameAlias));
                   revGdalFormatsIds.put(r.getString(formatNameAlias), r.getString(gdalIdAlias));
             }
             
             /* (SWE) QUANTITIES */
             // Intervals (allowed values)
             // NOTE: PostgreSQL Numeric <-> BigDecimal 
             // (see "Mapping SQL and Java Types, §8.9.1" - http://docs.oracle.com/javase/1.3/docs/guide/jdbc/getstart/mapping.html)
             intervals    = new HashMap<Integer, Pair<BigDecimal,BigDecimal>>();
             revIntervals = new HashMap<Pair<BigDecimal,BigDecimal>, Integer>();
             sqlQuery =
                     "SELECT " + INTERVAL_ID  + "," 
                               + INTERVAL_MIN + ", "
                               + INTERVAL_MAX +
                     " FROM "  + TABLE_INTERVAL
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                 Pair minMax = Pair.of(r.getBigDecimal(INTERVAL_MIN), r.getBigDecimal(INTERVAL_MAX));
                    intervals.put(r.getInt(INTERVAL_ID), minMax);
                 revIntervals.put(minMax, r.getInt(INTERVAL_ID));
             }
             // Quantities (UoMs now, then all metadata when table-classes are created)
             quantities    = new HashMap<Integer, String>();
             sqlQuery =
                     " SELECT " + TABLE_QUANTITY + "." + QUANTITY_ID + ", "
                                + TABLE_UOM      + "." + UOM_CODE    +
                     " FROM "   + TABLE_QUANTITY + ", " 
                                + TABLE_UOM      +
                     " WHERE "  + TABLE_UOM      + "." + UOM_ID      + "="
                                + TABLE_QUANTITY + "." + QUANTITY_UOM_ID
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                    quantities.put(r.getInt(QUANTITY_ID), r.getString(UOM_CODE));
             }
             // Quantity <-> Intervals of allowed values
             quantityInterval = new HashSet<Pair<Integer, Integer>>();
             sqlQuery =
                     "SELECT " + INTERVAL_QUANTITY_QID + "," 
                               + INTERVAL_QUANTITY_IID +
                     " FROM "  + TABLE_INTERVAL_QUANTITY
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                 quantityInterval.add(Pair.of(r.getInt(INTERVAL_QUANTITY_QID), r.getInt(INTERVAL_QUANTITY_IID)));
             }
             
             // Range data types (WCPS rangeType())
             rangeDataTypes    = new HashMap<Integer, String>();
             revRangeDataTypes = new HashMap<String, Integer>();
             sqlQuery =
                     "SELECT " + RANGE_DATATYPE_ID   + "," 
                               + RANGE_DATATYPE_NAME +
                     " FROM "  + TABLE_RANGE_DATATYPE
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             while (r.next()) {
                    rangeDataTypes.put(r.getInt(RANGE_DATATYPE_ID), r.getString(RANGE_DATATYPE_NAME));
                 revRangeDataTypes.put(r.getString(RANGE_DATATYPE_NAME), r.getInt(RANGE_DATATYPE_ID));
             }
 
             /*
              *  Read WCS _service_ metadata
              */
             // TODO (as soon as table-classes are created)
             // ...
             
             // End of session
             s.close();
             
             /* Check CoverageMetadata consistency at startup, if needed */
             this.checkAtInit = checkAtInit;
             
             if (checkAtInit) {
                 initializing = true;
                 boolean coveragesOk = false;
                 
                 while (!coveragesOk) {
                     Iterator<String> coverages = coverages().iterator();
                     while (coverages.hasNext()) {
                         read(coverages.next());
                     }
                     coveragesOk = true;
                 }
                 initializing = false;
             }
             
         } catch (SecoreException sEx) {
             throw sEx;
         } catch (PetascopeException pEx) {
             throw pEx;
         } catch (SQLException sqle) {
             if (s != null) {
                 try {
                     s.close();
                 } catch (SQLException e) {
                 }
             }
             
             close();
             
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error", sqle);
         }        
     }
     
     public void close() {
         if (conn != null) {
             try {
                 conn.close();
             } catch (SQLException sqle) {
             }
             conn = null;
         }
     }    
     
     /**
      * Returns a list of all available coverages in petascopedb
      * @return Unordered list of coverage names
      * @throws PetascopeException
      */
     @Override
     public Set<String> coverages() throws PetascopeException {
         Statement s = null;
         Set<String> coverages;
         
         try {
             ensureConnection();
             s = conn.createStatement();
             
             String sqlQuery =
                     " SELECT "   + COVERAGE_NAME  + 
                     " FROM "     + TABLE_COVERAGE +
                     " ORDER BY " + COVERAGE_NAME                    
                     ;
             log.debug("SQL query: " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             coverages = new HashSet<String>(r.getFetchSize());
             while (r.next()) {
                 coverages.add(r.getString(COVERAGE_NAME));
             }
             s.close();
             
             return coverages;
         } catch (SQLException sqle) {
             if (s != null) {
                 try {
                     s.close();
                 } catch (SQLException f) {
                 }
             }
             
             close();
             
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error", sqle);
         }
     }
     
     /**
      * Given a coverage id; returns the corresponding name
      * @param coverageId
      * @return The name of the coverage
      * @throws PetascopeException
      */
     public String coverageType(String coverageId) throws PetascopeException {
         Statement s = null;
         try {
             ensureConnection();
             s = conn.createStatement();
             String sqlQuery =
                     " SELECT " + COVERAGE_GML_TYPE_ID +
                     " FROM "   + TABLE_COVERAGE       +
                     " WHERE "  + COVERAGE_ID    + "=" + coverageId
                     ;
             log.debug("SQL query: " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             if (r.next()) {
                 return gmlSubTypes.get(r.getInt(COVERAGE_GML_TYPE_ID));
             }
             throw new PetascopeException(ExceptionCode.NoSuchCoverage.locator(coverageId),
                     "Error getting coverage type.");
         } catch (SQLException sqle) {
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Error retrieving type for coverage " + coverageId, sqle);
         } finally {
             if (s != null) {
                 try {
                     s.close();
                 } catch (SQLException f) {
                 }
             }
             close();
         }
     }
     
     /**
      * Given a coverage name; returns the corresponding id
      * @param coverageName
      * @return The ID (TABLE_COVERAGE) of this coverage
      * @throws PetascopeException
      */
     public Integer coverageID(String coverageName) throws PetascopeException {
         Statement s = null;
         try {
             ensureConnection();
             s = conn.createStatement();
             String sqlQuery =
                     " SELECT " + COVERAGE_ID    +
                     " FROM "   + TABLE_COVERAGE +
                     " WHERE "  + COVERAGE_NAME  + "='" + coverageName + "'"
                     ;
             log.debug("SQL query: " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             if (r.next()) {
                 return r.getInt(COVERAGE_ID);
             }
             throw new PetascopeException(ExceptionCode.NoSuchCoverage.locator(coverageName),
                     "Error getting coverageID.");
         } catch (SQLException sqle) {
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Error retrieving ID for coverage " + coverageName, sqle);
         } finally {
             if (s != null) {
                 try {
                     s.close();
                 } catch (SQLException f) {
                 }
             }
             close();
         }
     }
     
     /**
      * Given a format name; returns the corresponding MIME type
      * @param format
      * @return The associated MIME type
      */
     @Override
     public String formatToMimetype(String format) {
         return supportedFormats.get(format);
     }
     
     /**
      * Given a coverage name; scans its metadata information thoroughly
      * (range, domain, extra) and stores it in a dedicated class instance.
      * @param coverageName
      * @return The CoverageMetadata object for this coverage
      * @throws PetascopeException
      */
     @Override
     public CoverageMetadata read(String coverageName) throws PetascopeException, SecoreException {
         
         log.debug("Reading metadata for coverage '{}'", coverageName);
         
         if ((coverageName == null) || coverageName.equals("")) {
             throw new PetascopeException(ExceptionCode.InvalidRequest,
                     "Cannot retrieve coverage with null or empty name");
         }
         
         // If coverage has been read already, no need to do it again
         if (cache.containsKey(coverageName)) {
             log.trace("Returning cached coverage metadata.");
             return cache.get(coverageName);
         }
         
         // Init
         Statement s = null;
         String sqlQuery; // buffer for SQL queries
         
         try {
             ensureConnection();
             s = conn.createStatement();
             
             /* TABLE_COVERAGE */
             sqlQuery = 
                     " SELECT " + COVERAGE_ID               + ", " 
                                + COVERAGE_NAME             + ", "
                                + COVERAGE_GML_TYPE_ID      + ", "
                                + COVERAGE_NATIVE_FORMAT_ID +
                     " FROM "   + TABLE_COVERAGE +
                     " WHERE "  + COVERAGE_NAME  + "='" + coverageName + "'"
                     ;
             log.debug("SQL query: " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             if (!r.next()) {
                 throw new PetascopeException(ExceptionCode.InvalidRequest,
                         "Coverage '" + coverageName + "' is not served by this server");
             }
             // Store fetched data
             int coverageId              = r.getInt(COVERAGE_ID);
             String coverageType         = gmlSubTypes.get(r.getInt(COVERAGE_GML_TYPE_ID));
             String coverageNativeFormat = mimeTypes.get(r.getInt(COVERAGE_NATIVE_FORMAT_ID));
             
             /* EXTRA METADATA */
             // Each coverage can have 1+ additional descriptive metadata (1+ OWS:Metadata, 1+ GMLCOV:Metadata, etc)
             sqlQuery =
                     " SELECT " + EXTRAMETADATA_METADATA_TYPE_ID    + ", "
                                + EXTRAMETADATA_VALUE               +
                     " FROM "   + TABLE_EXTRAMETADATA        +
                     " WHERE "  + EXTRAMETADATA_COVERAGE_ID  + "=" + coverageId
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             // Store fetched data
             Set<Pair<String,String>> extraMetadata = new HashSet<Pair<String,String>>();
             while (r.next()) {
                 Pair<String,String> typeValue = Pair.of(
                         r.getString(EXTRAMETADATA_METADATA_TYPE_ID), 
                         r.getString(EXTRAMETADATA_VALUE)
                         );
                 extraMetadata.add(typeValue);
             }
             
             // TABLE_DOMAINSET : read the CRS, as array of single-CRS URIs
             CellDomainElement cell;
             DomainElement     dom;
             List<CellDomainElement> cellDomainElements = new ArrayList<CellDomainElement>(r.getFetchSize());
             sqlQuery =
                     " SELECT " + DOMAINSET_NATIVE_CRS_ID +
                     " FROM "   + TABLE_DOMAINSET         +
                     " WHERE "  + DOMAINSET_COVERAGE_ID   + "=" + coverageId
                     ;
             log.debug("SQL query: " + sqlQuery);
             r = s.executeQuery(sqlQuery);
             if (!r.next()) {
                 throw new PetascopeException(ExceptionCode.InvalidRequest,
                         "Coverage '" + coverageName + "' is missing the domain-set information.");
             }
             // Store fetched data
             List<Pair<CrsDefinition.Axis,String>> crsAxes = new ArrayList<Pair<CrsDefinition.Axis,String>>();
             // The result-set of a SQL Array is a set of results, each of which is an array of 2 columns {id,attribute}, 
             // of indexes 1 and 2 respectively:
             ResultSet rs = r.getArray(DOMAINSET_NATIVE_CRS_ID).getResultSet();
             while (rs.next()) {
                 log.debug("CRS id: " + rs.getInt(2)); 
                 String uri = crss.get(rs.getInt(2));
                 if (null == uri) {
                     log.error("No CRS found in " + TABLE_CRS + " with id " + rs.getInt(2) + ".");
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             "No CRS found in " + TABLE_CRS + " with id " + rs.getInt(2) + ".");
                 }
                 log.info("Decoding " + uri + " ...");
                 // If not cached, parse the SECORE-resolved definition ot this CRS
                 CrsDefinition crsDef = CrsUtil.getGmlDefinition(uri);
                 for (CrsDefinition.Axis axis : crsDef.getAxes()) {
                     crsAxes.add(Pair.of(axis, uri));
                 }
             }
            log.trace("Coverage " + coverageName + " CRS decoded: it has " + crsAxes.size() + (crsAxes.size()>1?" axes":" axis") + ".");
             // Check CRS
             if (crsAxes.isEmpty()) {
                 throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                         "Coverage '" + coverageName + "' has no external (native) CRS.");
             }            
             
             // Now read the coverage-type-specific domain-set information
             if (coverageType.matches(".*" + XMLSymbols.LABEL_GRID_COVERAGE)) {
                 
                 // Variables for metadata object creation of gridded coverage
                 LinkedHashMap<List<BigDecimal>,BigDecimal> gridAxes;    // Offset-vector -> greatest-coefficient (null if no coeffs) 
                 List<BigDecimal>         gridOrigin;  // each BD is a coordinate's component                
                 Pair<BigInteger, String> rasdamanColl;// collName -> OID
         
                 // This is a gridded coverage //
                                 
                 /* RANGE-SET : coverage values (aka features, attributes, etc.) */
                 // TABLE_RANGESET
                 sqlQuery =
                         " SELECT " + RANGESET_STORAGE_TABLE + ", "
                                    + RANGESET_STORAGE_ID    +
                         " FROM "   + TABLE_RANGESET         +
                         " WHERE "  + RANGESET_COVERAGE_ID   + "=" + coverageId
                         ;
                 log.debug("SQL query: " + sqlQuery);
                 r = s.executeQuery(sqlQuery);
                 // Check that range-set is not missing
                 if (!r.next()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             "Missing range-set for coverage '" + coverageName + "'");
                 }
                 // Currently only coverages stored in `rasdaman' are supported
                 String storageTable = r.getString(RANGESET_STORAGE_TABLE);
                 if (!storageTable.equals(TABLE_RASDAMAN_COLLECTION)) {
                     throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                             "Storage table '" + storageTable + "' is not valid." +
                             "Only coverages stored in `rasdaman' are currently supported.");
                 } else {
                     // TABLE_RASDAMAN_COLLECTION
                     // Read the rasdaman collection:OID which stores the values of this coverage
                     int storageId = r.getInt(RANGESET_STORAGE_ID);
                     sqlQuery =
                             " SELECT " + RASDAMAN_COLLECTION_NAME  + ", "
                                        + RASDAMAN_COLLECTION_OID   +
                             " FROM "   + TABLE_RASDAMAN_COLLECTION +
                             " WHERE "  + RASDAMAN_COLLECTION_ID    + "=" + storageId
                             ;
                     log.debug("SQL query: " + sqlQuery);
                     r = s.executeQuery(sqlQuery);
                     if (!r.next()) {
                         throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                                 "Coverage '" + coverageName + "' is missing the range-set metadata.");
                     }
                     // Store fetched data: OID -> collName
                     rasdamanColl = Pair.of(
                             r.getBigDecimal(RASDAMAN_COLLECTION_OID).toBigInteger(), 
                             r.getString(RASDAMAN_COLLECTION_NAME)
                             );
             
                     log.trace("Coverage '" + coverageName + "' has range-set data in " +
                             r.getString(RASDAMAN_COLLECTION_NAME) + ":" + r.getBigDecimal(RASDAMAN_COLLECTION_OID) + ".");
                 }
                 
                 /* RANGE-TYPE : coverage feature-space description (SWE-based) */
                 // TABLE_RANGETYPE_COMPONENT
                 // Ordered mapping of band name and its UoM
                 List<RangeElement> rangeElements = new ArrayList<RangeElement>();
                 sqlQuery =
                         " SELECT "   + RANGETYPE_COMPONENT_NAME        + ", "
                                      + RANGETYPE_COMPONENT_TYPE_ID     + ", "
                                      + RANGETYPE_COMPONENT_FIELD_TABLE + ", "
                                      + RANGETYPE_COMPONENT_FIELD_ID    +
                         " FROM "     + TABLE_RANGETYPE_COMPONENT       +
                         " WHERE "    + RANGETYPE_COMPONENT_COVERAGE_ID + "=" + coverageId +
                         " ORDER BY " + RANGETYPE_COMPONENT_ORDER       + " ASC "
                         ;
                 log.debug("SQL query: " + sqlQuery);
                 r = s.executeQuery(sqlQuery);
                 if (!r.next()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             "Coverage '" + coverageName + "' is missing the range-type metadata.");
                 }
                 do {
                     // Check if it is a SWE:quantity (currently the only supported SWE field)
                     if (!r.getString(RANGETYPE_COMPONENT_FIELD_TABLE).equals(TABLE_QUANTITY)) {
                         throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                                 "Band " + r.getString(RANGETYPE_COMPONENT_NAME) + " of coverage '" +
                                 coverageName + "' is not a continuous quantity.");
                     }
                     
                     // Now read the intervals:
                     List<Pair<BigDecimal,BigDecimal>> allowedIntervals = new ArrayList<Pair<BigDecimal,BigDecimal>>();
                     for (Pair qI : quantityInterval) {
                         if (qI.fst.equals(r.getInt(RANGETYPE_COMPONENT_FIELD_ID))) {
                             // Fetch the associated interval
                             allowedIntervals.add(intervals.get((Integer)qI.snd));
                         }
                     }
     
                     // Create the RangeElement (WCPS): {name, type, UoM}
                     rangeElements.add(new RangeElement(
                             r.getString(RANGETYPE_COMPONENT_NAME),
                             rangeDataTypes.get(r.getInt(RANGETYPE_COMPONENT_TYPE_ID)),
                             quantities.get(r.getInt(RANGETYPE_COMPONENT_FIELD_ID)),
                             allowedIntervals)
                             ); 
                     log.debug("Added range element: " + rangeElements.get(rangeElements.size()-1));
                     
                 } while (r.next());
                 
                 /* DOMAIN-SET : geometry, origin, axes... */
                 sqlQuery =
                     " SELECT " + GRIDDED_DOMAINSET_ORIGIN       +
                     " FROM "   + TABLE_GRIDDED_DOMAINSET        +
                     " WHERE "  + GRIDDED_DOMAINSET_COVERAGE_ID  + "=" + coverageId
                     ;
                 log.debug("SQL query: " + sqlQuery);
                 r = s.executeQuery(sqlQuery);
                 if (!r.next()) {
                     throw new PetascopeException(ExceptionCode.InvalidRequest,
                             "Gridded coverage '" + coverageName + "' is missing the origin.");
                 }
                 // Store fetched data
                 gridOrigin = new ArrayList<BigDecimal>();
                 // The result-set of a SQL Array is a set of results, each of which is an array of 2 columns {id,attribute}, 
                 // of indexes 1 and 2 respectively:
                 rs = r.getArray(GRIDDED_DOMAINSET_ORIGIN).getResultSet();
                 while (rs.next()) {
                     log.debug("Grid origin component: " + rs.getBigDecimal(2));
                     gridOrigin.add(rs.getBigDecimal(2));
                 }
                 log.trace("Gridded coverage '{}' has origin '{}'", coverageName, gridOrigin);
                 // Check origin is not empty
                 if (gridOrigin.isEmpty()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             "Gridded coverage '" + coverageName + "' has an empty origin.");
                 }
                 // check if origin is compatible with the native CRS
                 // (then offset-vectors are checked against origin)
                 if (gridOrigin.size() != crsAxes.size()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             "Native CRS of coverage '" + coverageName + " is not compatible with given origin: " +
                             crsAxes.size() + " CRS dimensions against " + gridOrigin.size() + " origin components.");
                 }
                 
                 // Read axis-specific information, independently of its nature (rectilinear, regularly-spaced, etc.)
                 // Axis id -> {offset-vector, isIrregular}
                 // NOTE: use LinkedHashMap to preserve insertion order.
                 gridAxes = new LinkedHashMap<List<BigDecimal>,BigDecimal>();
                 Pair<String, String> cellDimensions;
                 sqlQuery =
                     " SELECT "   + GRID_AXIS_ID             + ", "
                                  + GRID_AXIS_RASDAMAN_ORDER +
                     " FROM "     + TABLE_GRID_AXIS          +
                     " WHERE "    + GRID_AXIS_COVERAGE_ID    + "=" + coverageId +
                     " ORDER BY " + GRID_AXIS_RASDAMAN_ORDER
                     ;
                 log.debug("SQL query: " + sqlQuery);
                 r = s.executeQuery(sqlQuery);
                 if (!r.next()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             "Gridded coverage '" + coverageName + "' has no axes." );
                 }
                 // Cannot put r.next() in a loop since an other SQL query is sent and "r" gets closed.
                 // Store my data a priori then run the loop
                 List<Integer> axisIds = new ArrayList<Integer>(r.getFetchSize());
                 do {
                     // Store fetched data
                     axisIds.add(r.getInt(GRID_AXIS_ID));
                 } while (r.next());
                 
                 // Proceed with axis examination
                 for (int axisId : axisIds) {
                     log.debug("Found axis with id {}", axisId);
                     // Read the offset vector
                     sqlQuery =
                             " SELECT " + RECTILINEAR_AXIS_OFFSET_VECTOR +
                             " FROM "   + TABLE_RECTILINEAR_AXIS         +
                             " WHERE "  + RECTILINEAR_AXIS_ID      + "=" + axisId
                             ;
                     log.debug("SQL query: " + sqlQuery);
                     ResultSet rAxis = s.executeQuery(sqlQuery);
                     if (rAxis.next()) {
                         // Store fetched data
                         List<BigDecimal> offsetVector = new ArrayList<BigDecimal>();
                         // The result-set of a SQL Array is a set of results, each of which is an array of 2 columns {id,attribute}, 
                         // of indexes 1 and 2 respectively:
                         rs = rAxis.getArray(RECTILINEAR_AXIS_OFFSET_VECTOR).getResultSet();
                         while (rs.next()) {
                             log.debug("Offset-vector " + axisId + " component: " + rs.getBigDecimal(2));
                             offsetVector.add(rs.getBigDecimal(2));
                         }
                         log.trace("Axis {} has offset-vector {}", axisId, offsetVector);
                         // Check offset vector is not empty
                         if (offsetVector.isEmpty()) {
                             throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                                     "Axis " + axisId + " of '" + coverageName + "' has an empty offset vector.");
                         }
                         
                         // Check if offset vector is aligned with a CRS axis
                         if (Vectors.nonZeroComponentsIndices(offsetVector.toArray(new BigDecimal[offsetVector.size()])).size() > 1) {
                             throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                                     "Offset vector " + offsetVector + " of coverage '" + coverageName + " is forbidden." +
                                     "Only aligned offset vectors are currently allowed (1 non-zero component).");
                         }
                         
                         // Check consistency origin/offset-vector
                         if (offsetVector.size() != gridOrigin.size()) {
                             throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                                     "Incompatible dimensionality of grid origin ("   + gridOrigin.size() +
                                     ") and offset vector of axis " + gridAxes.size() + " (" + offsetVector.size() +
                                     ") for coverage '" + coverageName + "'.");
                         }
                                        
                         // Check if it has coefficients -> is irregular 
                         // At the same time check that the number of coefficients is consistent with the `sdom' of the collection:
                         cellDimensions = getIndexDomain(rasdamanColl.snd, rasdamanColl.fst, gridAxes.size());
                         sqlQuery =
                                 " SELECT COUNT(*), MAX(" + VECTOR_COEFFICIENTS_COEFFICIENT + ") " +
                                                  "FROM " + TABLE_VECTOR_COEFFICIENTS       +
                                                " WHERE " + VECTOR_COEFFICIENTS_AXIS_ID     + "="  + axisId
                                 ;
                         log.debug("SQL query: " + sqlQuery);
                         rAxis = s.executeQuery(sqlQuery);
                         BigDecimal maxCoeff = null;
                         boolean isIrregular = false;
                         if (rAxis.next()) {
                             int coeffNumber = rAxis.getInt(1);
                             if (coeffNumber > 0) {
                                 // this axis has coefficients
                                 isIrregular = true;
                                 maxCoeff = rAxis.getBigDecimal(2);
                                 // check consistency with `sdom'
                                 int sdomCount   = new Integer(petascope.util.StringUtil.getCount(cellDimensions.fst, cellDimensions.snd));
                                 if (coeffNumber != sdomCount) {
                                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                                             "Coverage '" + coverageName + " has a wrong number of coefficients for axis " + axisId
                                             + " (" + coeffNumber + ") and is not consistent with its rasdaman `sdom' (" + sdomCount + ").");
                                 }
                             }
                         }
                         log.trace("Axis " + gridAxes.size() + " of coverage '" + coverageName + 
                                   "' is " + (isIrregular ? "" : "not ") + "irregular.");
                         
                         // Build up the axis map
                         gridAxes.put(offsetVector, maxCoeff);
                         
                         /* Create CellDomainElement and DomainElement for this axis: */
                         // CellDomain
                         cell = new CellDomainElement(
                                 BigInteger.valueOf(Integer.parseInt(cellDimensions.fst)),
                                 BigInteger.valueOf(Integer.parseInt(cellDimensions.snd)),
                                 gridAxes.size()-1
                                 );
                         cellDomainElements.add(cell);
                         log.debug("Added WCPS `cellDomain' element: " + cell);
                         
                     } else {
                         throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                                 "Axis " + axisId + " of '" + coverageName + "' has no offset vector.");
                     }
                 } // ~end read grid axes
                 
                 // Check if the coverage can fit into the CRS (dimensionality)
                 if (gridAxes.size() > crsAxes.size()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             gridAxes.size() + "D coverage '" + coverageName + "' cannot fit into its " +
                             crsAxes.size() + "D native CRS.");
                 }
                 // Currently accept only N-D coverages in N-D CRS
                 if (gridAxes.size() != crsAxes.size()) {
                     throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                             gridAxes.size() + "D coverage '" + coverageName + "' must have the same dimensions as its native CRS.");
                 }
                 
                 // Check if offset-vectors are orthogonal
                 if (!Vectors.areOrthogonal(new ArrayList(gridAxes.keySet()))) {
                     throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                             "Offset-vectors for coverage '" + coverageName + "' are not orthogonal.");
                 }
                 
                 // Check if rasdaman domain has the same dimensions
                 if (gridAxes.keySet().size() != cellDomainElements.size()) {
                     throw new PetascopeException(ExceptionCode.InvalidCoverageConfiguration,
                             gridAxes.keySet().size() + "D coverage '" + coverageName + "' is not compatible with " +
                             cellDomainElements.size() + " range-set.");
                 }
                 
                 /* Done with SQL statements */
                 s.close();
                 
                 /* Build the complete metadata object */
                 // NOTE: create a Bbox object with list of axis/extents or just let CoverageMetadata have a getBbox method?
                 // `domain' object has the required metadata to deduce the Bbox of the coverage.
                 // TODO : general constructor for *Coverages, then overloads in the CoverageMetadata.java
                 CoverageMetadata covMeta = new CoverageMetadata(
                         coverageName,
                         coverageType,
                         coverageNativeFormat,
                         extraMetadata,
                         crsAxes,
                         cellDomainElements,
                         gridOrigin,
                         gridAxes,
                         rasdamanColl,
                         rangeElements
                         );
                 // non-dynamic coverage:
                 covMeta.setCoverageId(coverageId);
                 
                 log.trace("Caching coverage metadata..");
                 cache.put(coverageName, covMeta);
                 return covMeta;
                 
             } else {
                 // TODO manage Multi*Coverage alternatives
                 throw new PetascopeException(ExceptionCode.UnsupportedCoverageConfiguration,
                         "Coverages of type '" + coverageType + "' are not supported.");
             }
             
         } catch (SecoreException sEx) {
             log.error("Error while parsing the CRS definitions to SECORE (" + ConfigManager.SECORE_URL + ").");
             throw sEx;
         } catch (PetascopeException ime) {
             log.error("Failed reading metadata", ime);
             if (checkAtInit && !initializing) {
                 throw new PetascopeException(ExceptionCode.ResourceError,
                         "Previously valid metadata is now invalid. The metadata for coverage '" + coverageName + "' has been modified incorrectly.", ime);
             } else {
                 throw new PetascopeException(ExceptionCode.InvalidRequest,
                         "Coverage '" + coverageName + "' has invalid metadata", ime);
             }
         } catch (SQLException sqle) {
             log.error("Failed reading metadata", sqle);
             if (s != null) {
                 try {
                     s.close();
                 } catch (SQLException f) {
                 }
             }
             close();
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error", sqle);
         }
     }
     
     /** Write a coverage's metadata to the database. This function can be used
      * for both inserting new coverages and updating coverage metadata.
      *
      * @param meta CoverageMetadata container for the information to be stored in the metadata database
      * @param commit Boolean value, specifying if we want to commit immediately or not
      */
     // TODO (WCS-T)
     /*private void write(CoverageMetadata meta, boolean commit) throws PetascopeException {
         String coverageName = meta.getCoverageName();
         if (existsCoverageName(coverageName)) {
             updateCoverageMetadata(meta, commit);
         } else {
             insertNewCoverageMetadata(meta, commit);
         }
     }*/
     
     public void delete(CoverageMetadata meta, boolean commit) throws PetascopeException {
         String coverageName = meta.getCoverageName();
         if (existsCoverageName(coverageName) == false) {
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Cannot delete inexistent coverage: " + coverageName);
         }
         
         /* Delete main coverage entry from "PS_Coverage". Auxiliary metadata are
          * automatically deleted by the DB (via CASCADING) on
          * deletion of the main entry in ps_coverage */
         Statement s = null;
         try {
             s = conn.createStatement();
             String sqlQuery =
                     "DELETE FROM " + TABLE_COVERAGE + 
                     " WHERE "      + COVERAGE_NAME  + "='" + coverageName + "'"
                     ;
             log.debug("SQL query : " + sqlQuery);
             setQuery(sqlQuery);
             int count = s.executeUpdate(query);
             log.trace("Affected rows: " + count);
             s.close();
             
             if (commit) {
                 commitAndClose();
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try {
                 s.close();
             } catch (Exception e) {
             }
         }
     }
     
     /**
      * Check if there is metadata available for a given coverage name
      * @param name coverage name
      * @return true is coverage already exists
      */
     public boolean existsCoverageName(String name) {
         boolean result = false;
         Statement s = null;
         try {
             ensureConnection();
             s = conn.createStatement();
             String sqlQuery =
                     "SELECT * FROM " + TABLE_COVERAGE + 
                     " WHERE " + COVERAGE_NAME + "='" + name + "'"
                     ;
             log.debug("SQL query : " + sqlQuery);
             setQuery(sqlQuery);
             ResultSet r = s.executeQuery(query);
             if (r.next()) {
                 result = true;
             } else {
                 result = false;
             }
             s.close();
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             try {
                 s.close();
             } catch (Exception e) {
             }
         }
         return result;
     }
     
     /**
      * Check if there exists a coverage with a given ID in the metadata database.
      * @param id coverage id
      * @return true is coverage already exists
      */
     private boolean existsCoverageId(int id) {
         boolean result = false;
         Statement s = null;
         try {
             s = conn.createStatement();
             String sqlQuery =
                     "SELECT * FROM " + TABLE_COVERAGE + 
                            " WHERE " + COVERAGE_ID    + "='" + id + "'"
                     ;
             log.debug("SQL query : " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             if (r.next()) {
                 result = true;
             } else {
                 result = false;
             }
             s.close();
         } catch (SQLException e) {
             log.error(e.getMessage());
             e.printStackTrace();
         } finally {
             try {
                 s.close();
             } catch (Exception e) {
                 log.error(e.getMessage());
             }
         }
         log.trace("Coverage with ID " + id + (result == false ? " does not " : "") + " exists.");
         return result;
     }
     
     /** Update metadata for an existing coverage. All information may change (including
      * name), but the ID of the tuple in PS_Coverage will stay the same.
      *
      * @param meta CoverageMetadata object, container of the new information.
      * @param commit True if we want to commit immediately, false to delay commit indefinitely
      */
     // TODO (WCS-T)
     //public void updateCoverageMetadata(CoverageMetadata meta, boolean commit) throws PetascopeException {
     // ...
     //}
     
     /** Insert metadata for a new coverage.
      *
      * @param meta CoverageMetadata object, container of information
      * @param commit Boolean value, specifying if we want to commit immediately or not
      */
     // TODO (WCS-T)
     //public void insertNewCoverageMetadata(CoverageMetadata meta, boolean commit) throws PetascopeException {
     // ...
     //}
     
     public void ensureConnection() throws SQLException {
         synchronized (this) {
             //          if( connection == null || !connection.isValid( CONNECTION_TIMEOUT ) ) { // Not implemented by PostgreSQL yet.
             if ((conn == null) || conn.isClosed()) {
                 //                log.trace("*** Opening new DB connection...");
                 close();
                 openConnection();
                 //                log.trace("*** ok.");
             }
         }
     }
     
     public void openConnection() throws SQLException {
         conn = DriverManager.getConnection(url, user, pass);
         conn.setAutoCommit(false);
         savepoint = conn.setSavepoint();
     }
     
     public void abortAndClose() throws SQLException {
         if (conn != null) {
             conn.rollback(savepoint);
             conn.close();
             conn = null;
         }
     }
     
     public void commitAndClose() throws SQLException {
         if (conn != null) {
             conn.commit();
             conn.close();
             conn = null;
         }
     }
     
     /* Logging function for SQL queries. */
     private void setQuery(String q) {
         this.query = q;
         log.trace("SQL Query: {}", q);
     }
     
     /* Returns the available formatToMimetype formats, as stored in the metadata database */
     public String[] getMimetypesList() {
         return supportedFormats.values().toArray(new String[1]);
     }
     
     /* Translate a mime-type to a format name, if known to rasdaman. */
     @Override
     public String mimetypeToFormat(String mime) {
         return revSupportedFormats.get(mime);
     }
 
     @Override
     public String formatToGdalid(String format) {
         return revGdalFormatsIds.get(format);
     }
 
     @Override
     public String gdalidToFormat(String gdalid) {
         return gdalFormatsIds.get(gdalid);
     }
     
     /**
      * Clear the internal cache of coverage metadata objects.
      */
     public void clearCache() {
         cache.clear();
     }
 
     /**
      * @return the range data types
      */
     public Collection<String> getDataTypes() {
         return rangeDataTypes.values();
     }
 
         /**
      * Fetches Coverage Data from Non-Raster Coverage created in PetascopeDB
      * @param schemaName
      * @param coverageID
      * @param coverageName
      * @param cellDomainList
      * @return
      * @throws PetascopeException
      */
     public String coverageData(String schemaName, String coverageID, String coverageName, 
                                List<CellDomainElement> cellDomainList) throws PetascopeException {
         
         Statement s = null;
         int pointCount = 0;
 
         try {
             ensureConnection();
             s = conn.createStatement();
 
             String genQuery = "SELECT * FROM " + schemaName + " WHERE coverage=" + coverageID;
 
             ResultSet r = s.executeQuery(genQuery);
             String pointMembers = "";
 
             while (r.next()) {
                 
                 String tuple = "";
                 boolean isInSubset = true;
                 int count = 0;
 
                 for (CellDomainElement cell : cellDomainList){
 
                     double columnValue = r.getDouble(count+3);
                     Double low = Double.parseDouble(cell.getLo().toString());
                     Double high = Double.parseDouble(cell.getHi().toString());
                     GetCoverageRequest.DimensionSubset subset = cell.getSubsetElement();
 
                     if(columnValue >= low && columnValue <= high){
 
                         /* ignore the dimension for the slicePoint if subsetting operation is slicing */
                         if(subset instanceof GetCoverageRequest.DimensionTrim || low.compareTo(high) != 0){
 
                             if((count+1) != cellDomainList.size()) {
                                 tuple += Double.toString(columnValue) + " ";
                             } else {
                                 tuple += Double.toString(columnValue);
                             }
                         }
                     }else{
                         isInSubset = false;
                     }
 
                     count++;
                 }
                 if(isInSubset){
 
                     String rowID = "p" + (++pointCount) + "_" + coverageName;
 
                     if(pointMembers!=null) {
                       pointMembers += Templates.getTemplate(Templates.MULTIPOINT_POINTMEMBERS,
                       Pair.of("\\{gmlPointId\\}", rowID),
                       Pair.of("\\{gmlPos\\}", tuple));                    
                     } else {
                       pointMembers = Templates.getTemplate(Templates.MULTIPOINT_POINTMEMBERS,
                       Pair.of("\\{gmlPointId\\}", rowID),
                       Pair.of("\\{gmlPos\\}", tuple));
                     }
 
                     pointMembers += "\n";
                 }               
             }
 
             s.close();
             return pointMembers;
 
         } catch (SQLException sqle) {
             if (s != null) {
                 try {
                     s.close();
                 } catch (SQLException f) {
                 }
             }
 
             close();
 
             throw new PetascopeException(ExceptionCode.ResourceError,
                     "Metadata database error", sqle);
         }
     }
     
     /**
      * Method to retrieve subset indexes of an irregularly spaced axis, subject to specified bounds.
      * Bounds must be numeric (timestamps have already been indexed before function call)
      * @param covName  Coverage human-readable name
      * @param iOrder   Order of the axis in the CRS: to indentify it in case the coverage has 2+ irregular axes.
      * @param lo       The lower index of the subset
      * @param hi       The upper index of the subset
      * @param min      The lower bound
      * @param max      The upper bound
      * @return Index of coefficients which enclose the interval.
      * @throws PetascopeException 
      */
     public long[] getIndexesFromIrregularRectilinearAxis(String covName, int iOrder, BigDecimal lo, BigDecimal hi, long min, long max)
             throws PetascopeException {
         
         long[] outCells = new long[2];
         Statement s = null;
         
         try {
             ensureConnection();
             s = conn.createStatement();
             
             String sqlQuery =
                     " SELECT COALESCE(MIN(" + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_ORDER + ")," + min + "), " +
                            " COALESCE(MAX(" + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_ORDER + ")," + max + ") "  + 
                           " FROM " + TABLE_VECTOR_COEFFICIENTS + ", " 
                                    + TABLE_GRID_AXIS           + ", "
                                    + TABLE_COVERAGE            + 
                          " WHERE " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_AXIS_ID +
                              " = " + TABLE_GRID_AXIS           + "." + GRID_AXIS_ID +
                            " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_COVERAGE_ID +
                              " = " + TABLE_COVERAGE  + "." + COVERAGE_ID +
                            " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_RASDAMAN_ORDER  + "="  + iOrder  +
                            " AND " + TABLE_COVERAGE  + "." + COVERAGE_NAME             + "='" + covName + "'" +
                            " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT 
                                    + " >= " + lo +
                            " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                                 // + " < "  + stringHi  // [a,b) subsets
                                    + " <= " + hi  // [a,b] subsets
                     ;
             log.debug("SQL query : " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             if (r.next()) {
                 outCells[0] = r.getInt(1);
                 outCells[1] = r.getInt(2);
                 if (lo.compareTo(hi) == 0 && outCells[0] == min && outCells[1] == max) {
                     // Aggregators have been coalesced to boundary values but because there is no intersection on this slice
                     throw new PetascopeException(ExceptionCode.InvalidRequest,
                             covName + " does not intersect with slicing point '" + lo + "'."
                             );
                 }
             } else {
                 throw new PetascopeException(ExceptionCode.InternalComponentError,
                         "No tuples returned from " + TABLE_VECTOR_COEFFICIENTS + ": check the metadata.");
             }
                
             s.close();
             return outCells;
             
         } catch (SQLException sqle) {
             /* Abort this transaction */
             try {
                 if (s != null) {
                     s.close();
                 }
                 abortAndClose();
             } catch (SQLException f) {
                 log.warn(f.getMessage());
             }
             throw new PetascopeException(ExceptionCode.InvalidRequest,
                     "Metadata database error", sqle);
         }
     }
     
     /**
      * Retrieves the coefficients of an irregular axis of a certain interval
      * @param covName  Coverage human-readable name
      * @param iOrder   Order of the axis in the CRS: to indentify it in case the coverage has 2+ irregular axes.
      * @param lo       The lower index of the subset
      * @param hi       The upper index of the subset
      * @return The ordered list of coefficients of the grid points inside the interval.
      * @throws PetascopeException 
      */    
     public List<BigDecimal> getCoefficientsOfInterval(String covName, int iOrder, BigDecimal lo, BigDecimal hi)
             throws PetascopeException {
 
         List<BigDecimal> coefficients = new ArrayList<BigDecimal>();
         Statement s = null;
         
         try {
             ensureConnection();
             s = conn.createStatement();
             
             String sqlQuery =
                     " SELECT ARRAY_AGG(" + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT + ") " + 
                           " FROM " + TABLE_VECTOR_COEFFICIENTS + ", " 
                                    + TABLE_GRID_AXIS           + ", "
                                    + TABLE_COVERAGE            + 
                          " WHERE " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_AXIS_ID +
                              " = " + TABLE_GRID_AXIS           + "." + GRID_AXIS_ID +
                            " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_COVERAGE_ID +
                              " = " + TABLE_COVERAGE  + "." + COVERAGE_ID +
                            " AND " + TABLE_GRID_AXIS + "." + GRID_AXIS_RASDAMAN_ORDER  + "="  + iOrder  +
                            " AND " + TABLE_COVERAGE  + "." + COVERAGE_NAME             + "='" + covName + "'" +
                            " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT 
                                    + " >= " + lo +
                            " AND " + TABLE_VECTOR_COEFFICIENTS + "." + VECTOR_COEFFICIENTS_COEFFICIENT
                                 // + " < "  + stringHi  // [a,b) subsets
                                    + " <= " + hi        // [a,b] subsets
                     ;
             log.debug("SQL query : " + sqlQuery);
             ResultSet r = s.executeQuery(sqlQuery);
             
             if (r.next()) {
                 // The result-set of a SQL Array is a set of results, each of which is an array of 2 columns {id,attribute},
                 // of indexes 1 and 2 respectively:
                 ResultSet rs = r.getArray(1).getResultSet();
                 while (rs.next()) {
                     coefficients.add(rs.getBigDecimal(2));
                 }
                 
                 s.close();
                 Arrays.sort(coefficients.toArray());
             }
             return coefficients;
             
         } catch (SQLException sqle) {
             /* Abort this transaction */
             try {
                 if (s != null) {
                     s.close();
                 }
                 abortAndClose();
             } catch (SQLException f) {
                 log.warn(f.getMessage());
             }
             throw new PetascopeException(ExceptionCode.InvalidRequest,
                     "Metadata database error", sqle);
         }
     }
     
     /**
      * Get the lower and upper bound of the specified coverage's dimension in pixel coordinates.
      * PURPOSE: remove redundant pixel-domain dimensions info in the petascopedb.
      * @param collName     The coverage name.
      * @param dimType      The dimension of collName of which the extent.
      * @return             The minimum and maximum pixel values of the array.
      * @throws PetascopeException 
      */
     public Pair<String, String> getIndexDomain(String collName, BigInteger collOid, int rasdamanAxisOrder) throws PetascopeException {
                 
         // Run RasQL query
         Object obj = null;
         String rasQuery = 
                 "SELECT sdom(c)["   + rasdamanAxisOrder + "]" + 
                 " FROM " + collName + " AS c WHERE oid(c) = " + collOid
                 ;
         log.debug("RasQL query : " + rasQuery);
         try {
             obj = RasUtil.executeRasqlQuery(rasQuery);
         } catch (RasdamanException ex) {
             throw new PetascopeException(ExceptionCode.InternalComponentError, "Error while executing RasQL query", ex);
         }
         
         // Parse the result        
         if (obj != null) {
             RasQueryResult res = new RasQueryResult(obj);
             if (!res.getScalars().isEmpty()) {
                 // TODO: can be done better with Minterval instead of sdom2bounds
                 Pair<String, String> bounds = Pair.of(
                         StringUtil.split(res.getScalars().get(0), ":")[0],
                         StringUtil.split(res.getScalars().get(0), ":")[1]);
                 return bounds;
             }
         }
         
         return null;
     }
 }
