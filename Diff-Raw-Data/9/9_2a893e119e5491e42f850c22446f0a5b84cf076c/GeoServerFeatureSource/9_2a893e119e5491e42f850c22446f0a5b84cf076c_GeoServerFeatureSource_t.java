 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.geotools.data.DataSourceException;
 import org.geotools.data.DataStore;
 import org.geotools.data.DefaultQuery;
 import org.geotools.data.FeatureListener;
 import org.geotools.data.FeatureLocking;
 import org.geotools.data.FeatureResults;
 import org.geotools.data.FeatureSource;
 import org.geotools.data.FeatureStore;
 import org.geotools.data.Query;
 import org.geotools.feature.FeatureType;
 import org.geotools.filter.AbstractFilter;
 import org.geotools.filter.Filter;
 import org.geotools.filter.FilterFactory;
 import org.geotools.filter.LogicFilter;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 
 /**
  * GeoServer wrapper for backend Geotools2 DataStore.
  * 
  * <p>
  * Support FeatureSource decorator for FeatureTypeInfo that takes care of
  * mapping the FeatureTypeInfo's FeatureSource with the schema and definition
  * query configured for it.
  * </p>
  * 
  * <p>
  * Because GeoServer requires that attributes always be returned in the same
  * order we need a way to smoothly inforce this. Could we use this class to do
  * so?
  * </p>
  *
  * @author Gabriel Roldn
 * @version $Id: GeoServerFeatureSource.java,v 1.3 2004/01/18 02:15:37 cholmesny Exp $
  */
 public class GeoServerFeatureSource implements FeatureSource {
     /** Shared package logger */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.global");
 
     /** FeatureSource being served up */
     protected FeatureSource source;
 
     /**
      * GeoTools2 Schema information
      * 
      * <p>
      * Is this the same as source.getSchema() or is it used supply the order
      * that GeoServer requires attributes to be returned in?
      * </p>
      */
     private FeatureType schema;
 
     /** Used to constrain the Feature made available to GeoServer. */
    private Filter definitionQuery = Filter.NONE;
 
     /**
      * Factory that make the correct decorator for the provided featureSource.
      * <p>
      * This factory method is public and will be used to create all required
      * subclasses. By comparison the constructors for this class have package
      * visibiliy.
      * </p>
      * @param featureSource
      * @return
      */
     public static GeoServerFeatureSource create( FeatureSource featureSource, FeatureType schema, Filter definitionQuery  ){
         if( featureSource instanceof FeatureLocking ){
             return new GeoServerFeatureLocking( (FeatureLocking) featureSource, schema, definitionQuery );            
         }
         else if ( featureSource instanceof FeatureStore ){
             return new GeoServerFeatureStore( (FeatureStore) featureSource, schema, definitionQuery );
         }
         return new GeoServerFeatureSource( featureSource, schema, definitionQuery );
     }
     
     /**
      * Creates a new GeoServerFeatureSource object.
      *
      * @param source GeoTools2 FeatureSource
      * @param schema FeatureType returned by this FeatureSource
      * @param definitionQuery Filter used to limit results
      */
     GeoServerFeatureSource(FeatureSource source, FeatureType schema,
         Filter definitionQuery) {
         this.source = source;
         this.schema = schema;
         this.definitionQuery = definitionQuery;
	if (this.definitionQuery == null) {
	    this.definitionQuery = Filter.NONE;
	}
     }
 
     /**
      * Takes a query and adapts it to match re definitionQuery filter
      * configured for a feature type.
      *
      * @param query Query against this DataStore
      *
      * @return Query restricted to the limits of definitionQuery
      *
      * @throws IOException See DataSourceException
      * @throws DataSourceException If query could not meet the restrictions of definitionQuery
      */
     protected Query makeDefinitionQuery(Query query) throws IOException {
         if ((query == Query.ALL) || query.equals(Query.ALL)) {
             return query;
         }
 
         try {
             String handle = query.getHandle();
             int maxFeatures = query.getMaxFeatures();
             String typeName = query.getTypeName();
             String[] propNames = extractAllowedAttributes(query);
             Filter filter = query.getFilter();
             filter = makeDefinitionFilter(filter);
 
             return new DefaultQuery(typeName, filter, maxFeatures, propNames,
                 handle);
         } catch (Exception ex) {
             throw new DataSourceException(
                 "Could not restrict the query to the definition criteria: "
                 + ex.getMessage(), ex);
         }
     }
 
     /**
      * List of allowed attributes.
      * 
      * <p>
      * Creates a list of FeatureTypeInfo's attribute names based on the
      * attributes requested by <code>query</code> and making sure they not
      * contain any non exposed attribute.
      * </p>
      * 
      * <p>
      * Exposed attributes are those configured in the "attributes" element of
      * the FeatureTypeInfo's configuration
      * </p>
      *
      * @param query User's origional query
      *
      * @return List of allowed attribute types
      */
     private String[] extractAllowedAttributes(Query query) {
         String[] propNames = null;
 
         if (query.retrieveAllProperties()) {
             propNames = new String[schema.getAttributeCount()];
 
             for (int i = 0; i < schema.getAttributeCount(); i++) {
                 propNames[i] = schema.getAttributeType(i).getName();
             }
         } else {
             String[] queriedAtts = query.getPropertyNames();
             int queriedAttCount = queriedAtts.length;
             List allowedAtts = new LinkedList();
 
             for (int i = 0; i < queriedAttCount; i++) {
                 if (schema.getAttributeType(queriedAtts[i]) != null) {
                     allowedAtts.add(queriedAtts[i]);
                 } else {
                     LOGGER.info("queried a not allowed property: "
                         + queriedAtts[i] + ". Ommitting it from query");
                 }
             }
 
             propNames = (String[]) allowedAtts.toArray(new String[allowedAtts
                     .size()]);
         }
 
         return propNames;
     }
 
     /**
      * If a definition query has been configured for the FeatureTypeInfo, makes
      * and return a new Filter that contains both the query's filter and the
      * layer's definition one, by logic AND'ing them.
      *
      * @param filter Origional user supplied Filter
      *
      * @return Filter adjusted to the limitations of definitionQuery
      *
      * @throws DataSourceException If the filter could not meet the limitations of definitionQuery
      */
     protected Filter makeDefinitionFilter(Filter filter)
         throws DataSourceException {
         Filter newFilter = filter;
 
         try {
             if (definitionQuery != Filter.NONE) {
                 FilterFactory ff = FilterFactory.createFilterFactory();
                 newFilter = ff.createLogicFilter(AbstractFilter.LOGIC_AND);
                 ((LogicFilter) newFilter).addFilter(definitionQuery);
                 ((LogicFilter) newFilter).addFilter(filter);
             }
         } catch (Exception ex) {
             throw new DataSourceException("Can't create the definition filter",
                 ex);
         }
 
         return newFilter;
     }
 
     /**
      * Implement getDataStore.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#getDataStore()
      * 
      * @return
      */
     public DataStore getDataStore() {
         return source.getDataStore();
     }
 
     /**
      * Implement addFeatureListener.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#addFeatureListener(org.geotools.data.FeatureListener)
      * 
      * @param listener
      */
     public void addFeatureListener(FeatureListener listener) {
         source.addFeatureListener(listener);
     }
 
     /**
      * 
      * Implement removeFeatureListener.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#removeFeatureListener(org.geotools.data.FeatureListener)
      * 
      * @param listener
      */
     public void removeFeatureListener(FeatureListener listener) {
         source.removeFeatureListener(listener);
     }
 
     /**
      * Implement getFeatures.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.data.Query)
      * 
      * @param query
      * @return
      * @throws IOException
      */
     public FeatureResults getFeatures(Query query) throws IOException {
         query = makeDefinitionQuery(query);
 
         return source.getFeatures(query);
     }
     
     /**
      * Implement getFeatures.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#getFeatures(org.geotools.filter.Filter)
      * 
      * @param filter
      * @return
      * @throws IOException
      */
     public FeatureResults getFeatures(Filter filter) throws IOException {
         filter = makeDefinitionFilter(filter);
 
         return source.getFeatures(filter);
     }
 
     /**
      * Implement getFeatures.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#getFeatures()
      * 
      * @return
      * @throws IOException
      */
     public FeatureResults getFeatures() throws IOException {
         if (definitionQuery == Filter.NONE) {
             return source.getFeatures();
         } else {
             return source.getFeatures(definitionQuery);
         }
     }
 
     /**
      * Implement getSchema.
      * <p>
      * Description ...
      * </p>
      * @see org.geotools.data.FeatureSource#getSchema()
      * 
      * @return
      */
     public FeatureType getSchema() {
         return schema;
     }
 
     /**
      * Retrieves the total extent of this FeatureSource.
      * <p>
      * Please note this extent will reflect the provided definitionQuery.
      * </p>
      * @return Extent of this FeatureSource, or <code>null</code> if no optimizations exist.
      *
      * @throws IOException If bounds of definitionQuery
      */
     public Envelope getBounds() throws IOException {
         if (definitionQuery == Filter.NONE) {
             return source.getBounds();
         } else {
             Query query = new DefaultQuery(definitionQuery);
             return source.getBounds( query );
         }
     }
 
     /**
      * Retrive the extent of the Query.
      * 
      * <p>
      * This method provides access to an optimized getBounds opperation. If no
      * optimized opperation is available <code>null</code> will be returned.
      * </p>
      * 
      * <p>
      * You may still make use of getFeatures( Query ).getCount() which will
      * return the correct answer (even if it has to itterate through all the
      * results to do so.
      * </p>
      *
      * @param query User's query
      *
      * @return Extend of Query or <code>null</code> if no optimization is
      *         available
      *
      * @throws IOException If a problem is encountered with source
      */
     public Envelope getBounds(Query query) throws IOException {
         try {
             query = makeDefinitionQuery(query);
         } catch (IOException ex) {
             return null;
         }
 
         return source.getBounds(query);
     }
 
     /**
      * Adjust query and forward to source.
      * 
      * <p>
      * This method provides access to an optimized getCount opperation. If no
      * optimized opperation is available <code>-1</code> will be returned.
      * </p>
      * 
      * <p>
      * You may still make use of getFeatures( Query ).getCount() which will
      * return the correct answer (even if it has to itterate through all the
      * results to do so).
      * </p>
      *
      * @param query User's query.
      *
      * @return Number of Features for Query, or -1 if no optimization is
      *         available.
      */
     public int getCount(Query query) {
         try {
             query = makeDefinitionQuery(query);
         } catch (IOException ex) {
             return -1;
         }
 
         return source.getCount(query);
     }
 }
