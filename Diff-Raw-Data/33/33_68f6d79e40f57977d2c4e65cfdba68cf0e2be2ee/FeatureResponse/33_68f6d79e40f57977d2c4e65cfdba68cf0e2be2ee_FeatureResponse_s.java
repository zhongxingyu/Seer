 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.responses;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Logger;
 import java.sql.Connection;
 import com.vividsolutions.jts.geom.Geometry;
 import org.geotools.feature.Feature;
 import org.geotools.feature.FeatureCollection;
 import org.geotools.feature.FeatureType;
 import org.geotools.feature.FeatureTypeFactory;
 import org.geotools.feature.FeatureTypeFlat;
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.SchemaException;
 import org.geotools.data.DataSource;
 import org.geotools.data.DataSourceException;
 import org.geotools.data.postgis.PostgisConnectionFactory;
 import org.geotools.data.postgis.PostgisDataSource;
 import org.vfny.geoserver.requests.FeatureRequest;
 import org.vfny.geoserver.requests.Query;
 import org.vfny.geoserver.config.TypeInfo;
 import org.vfny.geoserver.config.TypeRepository;
 
 /**
  * Handles a Get Feature request and creates a Get Feature response GML string.
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
  * @version $VERSION$
  */
 public class FeatureResponse {
 
     /** Standard logging instance for class */
     private static final Logger LOG = 
         Logger.getLogger("org.vfny.geoserver.responses");
     
     /** Constructor, which is required to take a request object. */ 
     private FeatureResponse () {}
 
     /**
      * Parses the GetFeature reqeust and returns a contentHandler.
      * @return XML response to send to client
      */ 
     public static String getXmlResponse(FeatureRequest request) 
         throws WfsException {
 	LOG.finest("get xml response called request is: " + request);
 
         TypeRepository repository = TypeRepository.getInstance();
         StringBuffer result = new StringBuffer();
 	int maxFeatures = request.getMaxFeatures();
 
 	GMLBuilder gml = new GMLBuilder(true);
 	gml.startFeatureCollection(null);
 	//HACK: different srids can go in same collection.  Doesn't matter now
 	//since we don't use the bbox for our feature collections.
         for(int i = 0, n = request.getQueryCount(); i < n; i++) {            
 	    Query curQuery = request.getQuery(i);
 	    TypeInfo meta = repository.getType(curQuery.getTypeName());
 	    if (meta == null) {
 		throw new WfsException("Could not find Feature Type named: " +
 				       curQuery.getTypeName() + ", feature information"
 				       + " is not in the data folder.", 
 				       getLocator(curQuery));
 	    }
 	    String srid = meta.getSrs();
 	    Feature[] curFeatures = getQuery(curQuery, meta, maxFeatures);
 	    addFeatures(curFeatures, gml, meta);
             LOG.finest("ended feature");
         }        
 	gml.endFeatureCollection();
 
 	LOG.finest("GML is: " + gml.getGML());
 	
         // return final string
         return gml.getGML();//result.toString();
     }
 
 
     /**
      * Adds an array of features to the gml to be returned.  All should
      * be of the same featureType (same schemas).
      *
      * @param features the features to add.
      * @param gml the object building the GML to return.
      * @param meta contains information about the features.
      * @tasks TODO: check to make sure schemas of each feature match.
      */
     private static void addFeatures(Feature[] features, GMLBuilder gml, TypeInfo meta){
 	if (features.length > 0) {
 	FeatureType schema = features[0].getSchema();
 	String typeName = meta.getName();
         AttributeType[] attributeTypes = schema.getAttributeTypes();
         Object[] attributes;
 	AttributeType geometryAttr = schema.getDefaultGeometry();
 	int geometryPosition = -1;
 	if (geometryAttr != null) {
 	    geometryPosition = geometryAttr.getPosition();
 	}
         LOG.finest("about to create gml");
         LOG.finest("initializing..." + attributeTypes[schema.attributeTotal() - 1].getClass().toString());
 	gml.initializeFeatureType(typeName);
 	if (geometryAttr != null) {
 	    gml.initializeGeometry(attributeTypes[geometryPosition].
                                getType(), 
                                typeName, 
 			       meta.getSrs(),  
                                attributeTypes[geometryPosition].
                                getName());
 	}
         for(int i = 0, m = features.length; i < m; i++) {
 
             LOG.finest("fid: " + features[i].getId());
             gml.startFeature(features[i].getId());
             attributes = features[i].getAttributes();
             LOG.finest("feature: " + features[i].toString());
             LOG.finest("att total: " + schema.attributeTotal());
             for(int j = 0, n = schema.attributeTotal(); j < n; j++) {
                LOG.finest("sent attribute: " + attributes[j].toString());
 		if (j == geometryPosition) {
		    LOG.finest("geometry att: " + attributes[j].getClass());
		    LOG.finest("att name: " + attributeTypes[j].getName());
             gml.addGeometry((Geometry) attributes[j], 
 			    attributeTypes[j].getName());
 		LOG.finest("added geometry: " + ((Geometry) attributes[j]).toString());
 		} else {
                gml.addAttribute(attributeTypes[j].getName(), 
                                 attributes[j].toString());
 		}
 	    }
             
             
             gml.endFeature();
 	}
 	}
     }
 
     /**
      * Convenience method to get the handle information
      * from a query, if it exists.
      * @param query the query to get the handle from.
      */
     private static String getLocator(Query query){
 	String locator = query.getHandle();
 	if (locator == null  || locator.equals("")) {
 	    locator = "Class FeatureResponse, in method getQuery";
 	}
 	return locator;
     }
 
     /**
      * Parses the GetFeature reqeust and returns a contentHandler.
      * @return XML response to send to client
      */ 
     private static Feature[] getQuery(Query query, TypeInfo meta, 
 				   int maxFeatures) throws WfsException {
         
 	LOG.finest("about to get query: " + query);
       
         PostgisConnectionFactory db =
 	    new PostgisConnectionFactory (meta.getHost(), meta.getPort(),
 					  meta.getDatabaseName()); 
 	LOG.finest("connecting to db " + meta.getHost() + " is host, port "
 		      + meta.getPort() + " name: " + meta.getDatabaseName());
        db.setLogin(meta.getUser(), meta.getPassword());
 	LOG.finest("setting user and pass " + meta.getUser() + meta.getPassword());

 	FeatureCollection collection = null;
 	FeatureType schema = null;
 	String tableName = meta.getName();
 	try {
	    Connection dbConnection = db.getConnection();
 	schema = PostgisDataSource.makeSchema(tableName, dbConnection);
 	if (!query.allRequested()) {
 	    List propertyNames = query.getPropertyNames();
 	    AttributeType[] properties = new AttributeType[propertyNames.size()];
 	    try {
 	    for(int i = 0; i < propertyNames.size(); i++) {
 		properties[i] = 
 		    schema.getAttributeType(propertyNames.get(i).toString());
 		
             }
 	    //HACK: we should not rely on schema being a featureTypeFlat.  And
 	    //get and set srid methods will go away soon (which should make it
 	    //so the datasource works without these hacks)
 	    int srid = ((FeatureTypeFlat)schema).getSRID();
 	    schema = FeatureTypeFactory.create(properties);	    
 	    ((FeatureTypeFlat)schema).setSRID(srid);
 	    } catch (SchemaException e) {
 		throw new WfsException(e, "While trying to create schema for" +
 				       "return feature", getLocator(query));
 	    }
 	}
 
 	    DataSource data = 
 		new PostgisDataSource(dbConnection, tableName, 
 				      schema, maxFeatures);
 	    LOG.finest("filter is " + query.getFilter());
 	    
             collection = data.getFeatures(query.getFilter());
         } catch(DataSourceException e) {
             throw new WfsException(e, "While getting features from datasource",
 				   getLocator(query));
         } catch(java.sql.SQLException e) {
 	    throw new WfsException(e, "While attempting to connect to" 
 				   + " datasource", getLocator(query));
 	}
 
         LOG.finest("successfully retrieved collection");
 
         Feature[] features = collection.getFeatures();
 	return features;
     }
     
 }
