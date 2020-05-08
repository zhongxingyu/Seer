 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.middleware.pojos.gdms;
 
 import java.io.Serializable;
 
 import javax.persistence.Basic;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 
 /**
  * POJO for mapping_pop table.
  *
  * @author Joyce Avestro
  */
 @Entity
 @Table(name = "mapping_pop")
 public class MappingPop implements Serializable{
 
     /** The Constant serialVersionUID. */
     private static final long serialVersionUID = 1L;
     
     public static final String GET_PARENTS_BY_DATASET_ID =
             "select parent_a_gid, parent_b_gid, mapping_type from mapping_pop where dataset_id = :datasetId";
     
     public static final String GET_MAPPING_VALUES_BY_GIDS_AND_MARKER_IDS =
             "select distinct" +
                 " mapping_pop_values.dataset_id" +
                 ", mapping_pop.mapping_type" +
                 ", mapping_pop.parent_a_gid" +
                 ", mapping_pop.parent_b_gid" +
                ", concat(marker.marker_type, '')" +
             " from mapping_pop_values" +
                 ", mapping_pop" +
                 ", marker" +
             " where mapping_pop_values.dataset_id = mapping_pop.dataset_id" +
                 " and mapping_pop_values.marker_id = marker.marker_id" +
                 " and mapping_pop_values.marker_id in (:markerIdList)" +
                 " and mapping_pop_values.gid in (:gidList)" +
             " order by" +
                 " mapping_pop_values.gid desc" +
                 ", marker.marker_name";
     
     /** The dataset id. */
     @Id
     @Basic(optional = false)
     @Column(name = "dataset_id")
     private Integer datasetId;
 
     private ParentElement parent;
 
     /** The population size. */
     @Column(name = "population_size")
     private Integer populationSize;
 
     /** The population type. */
     @Column(name = "population_type")
     private String populationType;
 
     /** The map data description. */
     @Column(name = "mapdata_desc")
     private String mapDataDescription;
     
     /** The scoring scheme. */
     @Column(name = "scoring_scheme")
     private String scoringScheme;
     
     /** The map id. */
     @Column(name = "map_id")
     private Integer mapId;
 
     /**
      * Instantiates a new mapping pop.
      */
     public MappingPop() {
     }
 
     /**
      * Instantiates a new mapping pop.
      *
      * @param datasetId the dataset id
      * @param mappingType the mapping type
      * @param parentAGId the parent ag id
      * @param parentBGId the parent bg id
      * @param populationSize the population size
      * @param populationType the population type
      * @param mapDataDescription the map data description
      * @param scoringScheme the scoring scheme
      * @param mapId the map id
      */
     public MappingPop(Integer datasetId,
                     String mappingType,
                     Integer parentAGId,
                     Integer parentBGId,
                     Integer populationSize,
                     String populationType,
                     String mapDataDescription,
                     String scoringScheme,
                     Integer mapId) {
         this.datasetId = datasetId;
         this.parent = new ParentElement(parentAGId, parentBGId, mappingType);
         this.populationSize = populationSize;
         this.populationType = populationType;
         this.mapDataDescription = mapDataDescription;
         this.scoringScheme = scoringScheme;
         this.mapId = mapId;    
     }
 
     /**
      * Gets the dataset id.
      *
      * @return the dataset id
      */
     public Integer getDatasetId() {
         return datasetId;
     }
     
     /**
      * Sets the dataset id.
      *
      * @param datasetId the new dataset id
      */
     public void setDatasetId(Integer datasetId) {
         this.datasetId = datasetId;
     }
     
     public ParentElement getParent() {
         return parent;
     }
     
     public void setParent(ParentElement parent) {
         this.parent = parent;
     }
 
     /**
      * Gets the mapping type.
      *
      * @return the mapping type
      */
     public String getMappingType() {
         return parent.getMappingType();
     }
     
     /**
      * Sets the mapping type.
      *
      * @param mappingType the new mapping type
      */
     public void setMappingType(String mappingType) {
         parent.setMappingType(mappingType);
     }
     
     /**
      * Gets the parent ag id.
      *
      * @return the parent ag id
      */
     public Integer getParentAGId() {
         return parent.getParentAGId();
     }
     
     /**
      * Sets the parent ag id.
      *
      * @param parentAGId the new parent ag id
      */
     public void setParentAGId(Integer parentAGId) {
         parent.setParentAGId(parentAGId);
     }
     
     /**
      * Gets the parent bg id.
      *
      * @return the parent bg id
      */
     public Integer getParentBGId() {
         return parent.getParentBGId();
    }
     
     /**
      * Sets the parent bg id.
      *
      * @param parentBGId the new parent bg id
      */
     public void setParentBGId(Integer parentBGId) {
         parent.setParentBGId(parentBGId);
     }
     
     /**
      * Gets the population size.
      *
      * @return the population size
      */
     public Integer getPopulationSize() {
         return populationSize;
     }
     
     /**
      * Sets the population size.
      *
      * @param populationSize the new population size
      */
     public void setPopulationSize(Integer populationSize) {
         this.populationSize = populationSize;
     }
     
     /**
      * Gets the population type.
      *
      * @return the population type
      */
     public String getPopulationType() {
         return populationType;
     }
     
     /**
      * Sets the population type.
      *
      * @param populationType the new population type
      */
     public void setPopulationType(String populationType) {
         this.populationType = populationType;
     }
     
     /**
      * Gets the map data description.
      *
      * @return the map data description
      */
     public String getMapDataDescription() {
         return mapDataDescription;
     }
     
     /**
      * Sets the map data description.
      *
      * @param mapDataDescription the new map data description
      */
     public void setMapDataDescription(String mapDataDescription) {
         this.mapDataDescription = mapDataDescription;
     }
     
     /**
      * Gets the scoring scheme.
      *
      * @return the scoring scheme
      */
     public String getScoringScheme() {
         return scoringScheme;
     }
     
     /**
      * Sets the scoring scheme.
      *
      * @param scoringScheme the new scoring scheme
      */
     public void setScoringScheme(String scoringScheme) {
         this.scoringScheme = scoringScheme;
     }
     
     /**
      * Gets the map id.
      *
      * @return the map id
      */
     public Integer getMapId() {
         return mapId;
     }
     
     /**
      * Sets the map id.
      *
      * @param mapId the new map id
      */
     public void setMapId(Integer mapId) {
         this.mapId = mapId;
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (obj == this) {
             return true;
         }
         if (!(obj instanceof MappingPop)) {
             return false;
         }
 
         MappingPop rhs = (MappingPop) obj;
         return new EqualsBuilder().appendSuper(super.equals(obj))
                                     .append(datasetId, rhs.datasetId)
                                     .append(parent, rhs.parent)
                                     .append(populationSize, rhs.populationSize)
                                     .append(populationType, rhs.populationType)
                                     .append(mapDataDescription, rhs.mapDataDescription)
                                     .append(scoringScheme, rhs.scoringScheme)
                                     .append(mapId, rhs.mapId).isEquals();    
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return new HashCodeBuilder(97, 311).append(datasetId)
                                         .append(parent)
                                         .append(populationSize)
                                         .append(populationType)
                                         .append(mapDataDescription)
                                         .append(scoringScheme)
                                         .append(mapId).toHashCode();   
     }
 
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return "MappingPop [datasetId=" + datasetId + 
                         ", mappingType=" + parent.getMappingType() +
                         ", parentAGId=" + parent.getParentAGId() +
                         ", parentBGId=" + parent.getParentBGId() +
                         ", populationSize=" + populationSize +
                         ", populationType=" + populationType +
                         ", mapDataDescription=" + mapDataDescription +
                         ", scoringScheme=" + scoringScheme +
                         ", mapId=" + mapId + "]";    
     }
 
 }
