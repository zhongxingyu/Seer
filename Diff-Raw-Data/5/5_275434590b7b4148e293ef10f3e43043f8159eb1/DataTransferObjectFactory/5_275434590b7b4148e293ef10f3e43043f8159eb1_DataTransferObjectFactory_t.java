 /* Copyright (c) 2004 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global.dto;
 
 import java.util.Collections;
 
 import org.geotools.feature.AttributeType;
 import org.geotools.feature.FeatureType;
 import org.vfny.geoserver.global.xml.GMLUtils;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * Generate Data Transfer Objects from "real" objects in the system.
  * <p>
  * This class is used to isolate the DTO from the details of
  * generating them. This allows DTO objects to be safely used as
  * a wire protocol with out unrequired dependencies on such things
  * as AttributeType and FeatureType.
  * </p>
  * <p>
  * This class may choose to opperate as a facade on the services of
  * global.xml?
  * </p>
  * 
  * @author jgarnett, Refractions Research, Inc.
  * @author $Author: dmzwiers $ (last modification)
 * @version $Id: DataTransferObjectFactory.java,v 1.4 2004/01/18 00:37:23 dmzwiers Exp $
  */
 public class DataTransferObjectFactory {
     /**
      * Construct DTO based on provided attributeType.
      * <p>
      * GMLUtils is used to provide the mapping from 
      * attributeType.getName/attributeType.getType() to an XML
      * type/fragement.
      * </p>
      * @param attributeType Real geotools2 AttributeType
      * @return Data Transfer Object for provided attributeType
      */
     public static AttributeTypeInfoDTO create( AttributeType attributeType){
         AttributeTypeInfoDTO dto = new AttributeTypeInfoDTO();
 
         dto.setName( attributeType.getName() );
        dto.setMinOccurs( 0 );
         dto.setMaxOccurs( 1 );
         
         
         GMLUtils.Mapping mapping =
             GMLUtils.schema( attributeType.getName(), attributeType.getType() );
             
         if( mapping == null ){
             dto.setComplex(false);
             dto.setType( GMLUtils.STRING.toString() );                        
         }
         else {
             dto.setComplex(false);
             dto.setType( mapping.toString() );                        
         }
         return dto;
     }
     /**
      * Construct DTO based on provided schema.
      * <p>
      * GMLUtils is used to provide the mapping  
      * to an XML type/fragement for each attribute
      * </p> 
      * @param dataStoreId Used as a backpointer to locate dataStore
      * @param schema Real geotools2 FeatureType 
      * @return Data Transfer Object for provided schema
      */
     public static FeatureTypeInfoDTO create( String dataStoreId, FeatureType schema){
         FeatureTypeInfoDTO dto = new FeatureTypeInfoDTO();
         dto.setAbstract(null);
         dto.setDataStoreId(dataStoreId);
         dto.setDefaultStyle("styles/normal.sld"); 
         dto.setDefinitionQuery( null ); // no extra restrictions
         dto.setDirName( dataStoreId+"_"+schema.getTypeName() );
         dto.setKeywords( Collections.EMPTY_LIST );
         dto.setLatLongBBox( new Envelope() );
         dto.setName( schema.getTypeName() );
         dto.setNumDecimals( 8 );
         dto.setSchema( generateAttributes( schema ));
         dto.setSchemaBase( GMLUtils.ABSTRACTFEATURETYPE.toString() );
         dto.setSchemaName( dataStoreId.toUpperCase()+"_"+schema.getTypeName().toUpperCase()+"_TYPE" );
         dto.setSRS( schema.getDefaultGeometry().getGeometryFactory().getSRID() );
         dto.setTitle( schema.getNamespace() + " "+schema.getTypeName() );
         
         return dto;
     }
     public static List generateAttributes( FeatureType schema ){
     	AttributeType attributes[] = schema.getAttributeTypes();
     	List list = new ArrayList( attributes.length );
         for( int i=0; i<attributes.length;i++){
             list.add( create( attributes[i] ) );
         }
         return list;
     }
 }
