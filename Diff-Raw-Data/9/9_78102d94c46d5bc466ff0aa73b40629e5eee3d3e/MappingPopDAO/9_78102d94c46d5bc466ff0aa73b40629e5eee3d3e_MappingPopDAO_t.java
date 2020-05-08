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
 package org.generationcp.middleware.dao.gdms;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.generationcp.middleware.dao.GenericDAO;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.pojos.gdms.MappingPop;
 import org.generationcp.middleware.pojos.gdms.MappingValueElement;
 import org.generationcp.middleware.pojos.gdms.ParentElement;
 import org.hibernate.HibernateException;
 import org.hibernate.SQLQuery;
 
 /**
  * The Class MappingPopDAO.
  * 
  * @author Joyce Avestro
  * 
  */
 public class MappingPopDAO extends GenericDAO<MappingPop, Integer>{
     
     @SuppressWarnings("rawtypes")
     public List<ParentElement> getParentsByDatasetId(Integer datasetId) throws QueryException{
         SQLQuery query = getSession().createSQLQuery(MappingPop.GET_PARENTS_BY_DATASET_ID); 
         query.setParameter("datasetId", datasetId);
         
         
         List<ParentElement> dataValues = new ArrayList<ParentElement>();
         try{
             List results = query.list();
         
             for (Object o : results) {
                 Object[] result = (Object[]) o;
                 if (result != null) {
                     Integer parentAGId = (Integer) result[0];
                     Integer parentBGId = (Integer) result[1];
                     String mappingType = (String) result[2];
                     ParentElement parentElement = new ParentElement(parentAGId, parentBGId, mappingType);
                     dataValues.add(parentElement);
                 }
             }
             return dataValues;        
         } catch (HibernateException ex) {
             throw new QueryException("Error with get parents by dataset id: " + ex.getMessage());
         }
     }
     
     @SuppressWarnings("rawtypes")
     public List<MappingValueElement> getMappingValuesByGidAndMarkerIds(List<Integer> gids, List<Integer> markerIds) throws QueryException {
         SQLQuery query = getSession().createSQLQuery(MappingPop.GET_MAPPING_VALUES_BY_GIDS_AND_MARKER_IDS);
         query.setParameterList("markerIdList", markerIds);
         query.setParameterList("gidList", gids);
         
         List<MappingValueElement> mappingValues = new ArrayList<MappingValueElement>();
         try{
             List results = query.list();
         
             for (Object o : results) {
                 Object[] result = (Object[]) o;
                 if (result != null) {
                     Integer datasetId = (Integer) result[0];
                     String mappingType = (String) result[1];
                     Integer parentAGid = (Integer) result[2];
                     Integer parentBGid = (Integer) result[3];
                    String markerType = (String) result[4];
                     MappingValueElement mappingValueElement = new MappingValueElement(datasetId, mappingType, parentAGid, parentBGid, markerType);
                     mappingValues.add(mappingValueElement);
                 }
             }
             return mappingValues;        
         } catch (HibernateException ex) {
             throw new QueryException("Error with get mapping values by GIDs and Marker Ids: " + ex.getMessage());
         }
     }
 
 }
