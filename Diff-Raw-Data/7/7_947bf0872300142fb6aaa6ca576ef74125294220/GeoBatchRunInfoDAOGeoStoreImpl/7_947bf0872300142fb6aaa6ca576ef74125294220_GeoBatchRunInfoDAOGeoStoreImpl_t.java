 /*
  *  OpenSDI Manager
  *  Copyright (C) 2013 GeoSolutions S.A.S.
  *  http://www.geo-solutions.it
  *
  *  GPLv3 + Classpath exception
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package it.geosolutions.opensdi.dao.impl;
 
 import it.geosolutions.geobatch.services.rest.GeoBatchRESTClient;
 import it.geosolutions.geobatch.services.rest.RESTFlowService;
 import it.geosolutions.geobatch.services.rest.model.RESTConsumerStatus;
 import it.geosolutions.geobatch.services.rest.model.RESTRunInfo;
 import it.geosolutions.geostore.core.model.Category;
 import it.geosolutions.geostore.core.model.Resource;
 import it.geosolutions.geostore.services.dto.search.AndFilter;
 import it.geosolutions.geostore.services.dto.search.BaseField;
 import it.geosolutions.geostore.services.dto.search.CategoryFilter;
 import it.geosolutions.geostore.services.dto.search.FieldFilter;
 import it.geosolutions.geostore.services.dto.search.SearchFilter;
 import it.geosolutions.geostore.services.dto.search.SearchOperator;
 import it.geosolutions.geostore.services.rest.GeoStoreClient;
 import it.geosolutions.geostore.services.rest.model.RESTCategory;
 import it.geosolutions.geostore.services.rest.model.RESTResource;
 import it.geosolutions.geostore.services.rest.model.ResourceList;
 import it.geosolutions.opensdi.dao.GeoBatchRunInfoDAO;
 import it.geosolutions.opensdi.dto.GeoBatchRunInfoComparator;
 import it.geosolutions.opensdi.dto.GeobatchRunInfo;
 import it.geosolutions.opensdi.model.FileUpload;
 import it.geosolutions.opensdi.operations.GeoBatchOperation;
 import it.geosolutions.opensdi.operations.LocalOperation;
 import it.geosolutions.opensdi.operations.RemoteOperation;
 import it.geosolutions.opensdi.service.GeoBatchClientConfiguration;
 import it.geosolutions.opensdi.service.impl.GeoBatchClientImpl;
 import it.geosolutions.opensdi.utils.GeoBatchRunInfoUtils;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.Cacheable;
 
 /**
  * GeoBatch run information DAO saving on GeoStore via REST API
  * 
  * @author adiaz
  */
 public class GeoBatchRunInfoDAOGeoStoreImpl implements GeoBatchRunInfoDAO,
         Serializable {
 
 /** serialVersionUID */
 private static final long serialVersionUID = 2157928966616236857L;
 
 @Autowired
 GeoBatchClientConfiguration geobatchClientConfiguration;
 
 /**
  * Category to save on GeoStore
  */
 protected RESTCategory geobatchExecutionCategory = null;
 
 /**
  * GeoStore client autowired
  */
 @Autowired
 protected GeoStoreClient geostoreClient;
 
 /**
  * Default status for a GeoBatch action
  */
 public static final String RUNNING = "RUNNING";
 
 /**
  * Name for the geostore category for the geobatch runs
  */
 private String CATEGORY_NAME = "GEOBATCH_RUN";
 
 /**
  * @return name for the geostore category for the geobatch runs
  */
 public String getCATEGORY_NAME() {
     return CATEGORY_NAME;
 }
 
 /**
  * @param CATEGORY_NAME for the geostore category for the geobatch runs
  */
 public void setCATEGORY_NAME(String name) {
     CATEGORY_NAME = name;
 }
 
 /**
  * @return the geobatchExecutionCategory
  */
 private RESTCategory getGeobatchExecutionCategory() {
     if (geobatchExecutionCategory == null) {
         geobatchExecutionCategory = initCategory();
     }
     return geobatchExecutionCategory;
 }
 
 /**
  * Initialize GeoBatch category for GeoStore. The category is identified by
  * {@link GeoBatchRunInfoDAOGeoStoreImpl#CATEGORY_NAME}.
  */
 private RESTCategory initCategory() {
     // Read all categories looking for CATEGORY_NAME
     Long geobatchExecutionCategoryId = null;
     for (Category category : this.geostoreClient.getCategories().getList()) {
         if (CATEGORY_NAME.equals(category.getName())) {
             geobatchExecutionCategoryId = category.getId();
             break;
         }
     }
     // Obtain geobatch execution category
     geobatchExecutionCategory = new RESTCategory();
     geobatchExecutionCategory.setName(CATEGORY_NAME);
     if (geobatchExecutionCategoryId == null) {
         geobatchExecutionCategory.setId(this.geostoreClient
                 .insert(geobatchExecutionCategory));
     } else {
         geobatchExecutionCategory.setId(geobatchExecutionCategoryId);
     }
     return geobatchExecutionCategory;
 }
 
 /**
  * Obtain run information for a file identified by compositeId
  * 
  * @param compositeId identifier of the file (to concatenate)
  * @return List of runs for a composite id
  */
 public List<GeobatchRunInfo> getRunInfo(String... compositeId) {
     return search(compositeId != null && compositeId.length > 0 ? generateDecription(compositeId) : null);
 }
 
 /**
  * Obtain last run for a compositeId
  * 
  * @param compositeId unique ID for a file
  * @return last execution for this file or null if not found
  */
 public GeobatchRunInfo getLastRunInfo(String... compositeId) {
     return getLastRunInfoCache(generateDecription(compositeId));
 }
 
 /**
  * Obtain last run updated or not for a compositeId
  * 
  * @param updateStatus flag to check status of the run or not
  * @param compositeId unique ID for a file in admin project
  * @return last execution for this file or null if not found
  */
 public GeobatchRunInfo getLastRunInfo(Boolean updateStatus,
         String... compositeId) {
     if (updateStatus == null || !updateStatus) {
         return getLastRunInfo(compositeId);
     } else {
         GeobatchRunInfo runInfo = getLastRunInfo(compositeId);
         if (runInfo != null) {
             runInfo = updateRunInfoStatus(runInfo);
         }
         return runInfo;
     }
 }
 
 /**
  * Obtain run information for a file identified by compositeId
  * 
  * @param updateStatus flag to check status of the run or not
  * @param compositeId identifier of the file (to concatenate)
  * 
  * @return List of runs for a composite id
  */
 public List<GeobatchRunInfo> getRunInfo(Boolean updateStatus, String... compositeId){
     if (updateStatus == null || !updateStatus) {
         return getRunInfo(compositeId);
     } else {
         List<GeobatchRunInfo> runInfoList = new LinkedList<GeobatchRunInfo>();
         List<GeobatchRunInfo> searchResult = getRunInfo(compositeId);
         if (searchResult != null && !searchResult.isEmpty()) {
             for(GeobatchRunInfo runInfo: searchResult){
                 runInfoList.add(updateRunInfoStatus(runInfo));
             }
         }
         return runInfoList;
     }
 }
 
 /**
  * Obtain a execution for a known runUid and file
  * 
  * @param runUid
  * @param compositeId unique ID for a file
  * @return run information or null if not found
  */
 public GeobatchRunInfo getRunInfoByUid(String runUid, String... compositeId) {
     return searchUnique(runUid, generateDecription(compositeId));
 }
 
 /**
  * Obtain a execution for a known runUid and file
  * 
  * @param runUid
  * @param fileId unique ID for a file
  * @return run information or null if not found
  */
 public GeobatchRunInfo searchUnique(String runUid, String description) {
     SearchFilter filter = new AndFilter(new FieldFilter(BaseField.DESCRIPTION,
             description, SearchOperator.EQUAL_TO), new FieldFilter(
             BaseField.METADATA, runUid, SearchOperator.EQUAL_TO),
             new CategoryFilter(CATEGORY_NAME, SearchOperator.EQUAL_TO));
     ResourceList list = geostoreClient.searchResources(filter, 0,
             Integer.MAX_VALUE, true, false);
 
     List<GeobatchRunInfo> resources = new LinkedList<GeobatchRunInfo>();
     if (list.getList() != null && !list.getList().isEmpty()) {
         for (Resource resource : list.getList()) {
             resources.add(getRunStatus(resource));
         }
     }
     
     // sort by last execution
     Collections.sort(resources, new GeoBatchRunInfoComparator());
     
     return resources != null && !resources.isEmpty() ? resources.get(0) : null;
 }
 
 /**
  * Obtain all executions for a known file
  * 
  * @param fileId unique ID for a file
  * @return all runs or null if not found
  */
 public List<GeobatchRunInfo> search(String fileId) {
     SearchFilter filter;
     if(fileId != null){
         filter = new AndFilter(new FieldFilter(BaseField.DESCRIPTION,
                 fileId, SearchOperator.EQUAL_TO), new CategoryFilter(CATEGORY_NAME,
                 SearchOperator.EQUAL_TO));
     }else{
         filter = new CategoryFilter(CATEGORY_NAME,
                 SearchOperator.EQUAL_TO);
     }
     ResourceList list = geostoreClient.searchResources(filter, 0,
             Integer.MAX_VALUE, true, false);
     List<GeobatchRunInfo> resources = new LinkedList<GeobatchRunInfo>();
     if (list.getList() != null && !list.getList().isEmpty()) {
         for (Resource resource : list.getList()) {
             resources.add(getRunStatus(resource));
         }
     }
     
     // sort by last execution
     Collections.sort(resources, new GeoBatchRunInfoComparator());
     
     return resources;
 }
 
 /**
  * Update execution status
  * 
  * @param runUid to be updated
  * @param status new status
  * @return updated run info
  */
 public GeobatchRunInfo updateRunInfo(String runUid, String status) {
     GeobatchRunInfo result = searchUnique(runUid);
     if (result != null) {
         result.setFlowStatus(status);
         result = updateRunInfo(result);
     } else {
         // FIXME: Something lost
     }
     return result;
 }
 
 /**
  * Update run information
  * 
  * @param runInfo
  * @return run information updated
  */
 public GeobatchRunInfo updateRunInfo(GeobatchRunInfo runInfo) {
     RESTResource resource = getRestResource(runInfo);
     geostoreClient.updateResource(resource.getId(), resource);
     return searchUnique(resource.getName());
 }
 
 /**
  * Save run information for a GeoBatch operation
  * 
  * @param obj parameters for the operation
  * @param operation executed
  * @return GeobatchRunInfo stored
  */
 public GeobatchRunInfo saveRunInfo(Object[] parameters,
         GeoBatchOperation operation) {
     // TODO: Implement if needed for other operations
     return null;
 }
 
 /**
  * Save run information for a local operation
  * 
  * @param runUid
  * @param operation
  * @param runInfo
  * @return run information saved
  */
 public GeobatchRunInfo saveRunInfo(String runUid, LocalOperation operation,
         RESTRunInfo runInfo) {
     return saveRunInfo(runUid,
             operation.getVirtualPath(runInfo.getFileList().get(0)),
             operation.getName());
 }
 
 /**
  * Save run information for a remote operation
  * 
  * @param runUid
  * @param operation
  * @param uploadFile
  * @return run information saved
  */
 public GeobatchRunInfo saveRunInfo(String runUid, RemoteOperation operation,
         FileUpload uploadFile) {
     // TODO: improve it, now using RESTPATH as virtual path of the file
     return saveRunInfo(runUid, operation.getRESTPath(), uploadFile.getFiles()
             .get(0).getName(), operation.getName());
 }
 
 /**
  * Clean all run information for a compositeId
  * 
  * @param compositeId unique ID for a file in admin project
  * 
  * @return last execution for this file or null if not found
  */
 public GeobatchRunInfo cleanRunInformation(String... compositeId) {
     GeobatchRunInfo last = null;
     List<GeobatchRunInfo> list = search(generateDecription(compositeId));
     if(list != null
             && !list.isEmpty()){
         // obtain last
         last = list.get(0);
         // Delete all resources
         for(GeobatchRunInfo runInfo: list){
             RESTResource resource = getRestResource(runInfo);
             geostoreClient.deleteResource(resource.getId());
         }
     }
     return last;
 }
 
 /**
  * Save default run information for an execution
  * 
  * @param runUid
  * @param compositeId
  * @return run information with 'running'
  */
 private GeobatchRunInfo saveRunInfo(String runUid, String... compositeId) {
     // Mapping og run status
     RESTResource resource = new RESTResource();
     // Category it's our category for geobatch execution
     resource.setCategory(getGeobatchExecutionCategory());
     // Name it's the run UID for the execution
     resource.setName(runUid);
     // Description it's the composite id
     resource.setDescription(generateDecription(compositeId));
     // Metadata it's the status of the execution
     resource.setMetadata(RUNNING);
     resource.setId(geostoreClient.insert(resource));
     return getRunStatus(resource);
 }
 
 /**
  * Cached method for each file
  * 
  * @param id
  * @return Last execution cached
  */
 @Cacheable("lastRunInfo")
 private GeobatchRunInfo getLastRunInfoCache(String id) {
     List<GeobatchRunInfo> search = search(id);
     
     return search != null && !search.isEmpty() ? search.get(0) : null;
 }
 
 /**
  * Generate description for a compositeId
  * 
  * @param compositeId String array with parameters to be concatenated in one
  *        String separated by {@link GeoBatchClientImpl#SEPARATOR}
  * @return concatenated String
  */
 private String generateDecription(String... compositeId) {
     return GeoBatchRunInfoUtils.generateDecription(compositeId);
 }
 
 /**
  * Obtain a RESTResource from a run information data
  * 
  * @param runInfo
  * @return
  */
 private RESTResource getRestResource(GeobatchRunInfo runInfo) {
     // Mapping og run status
     RESTResource resource = new RESTResource();
     // Category it's our category for geobatch execution
     resource.setCategory(getGeobatchExecutionCategory());
     // Name it's the run UID for the execution
     resource.setName(runInfo.getFlowUid());
     // Description it's the composite id
     resource.setDescription(runInfo.getInternalUid());
     // Metadata it's the status of the execution
     resource.setMetadata(runInfo.getFlowStatus());
     resource.setId(runInfo.getId());
     return resource;
 }
 
 /**
  * Obtain a GeobatchRunInfo from a resource
  * 
  * @param resource of geostore to obtain
  * @return GeobatchRunInfo
  */
 private GeobatchRunInfo getRunStatus(Resource lazyResource) {
     GeobatchRunInfo runStatus = new GeobatchRunInfo();
     Resource resource = geostoreClient.getResource(lazyResource.getId(), true);
     runStatus.setId(resource.getId());
     runStatus.setExternalUid(resource.getName());
     runStatus.setInternalUid(resource.getDescription());
     runStatus.setFlowUid(resource.getName());
     runStatus.setFlowStatus(resource.getMetadata());
     runStatus.setLastExecution(resource.getCreation());
     runStatus.setLastCheck(resource.getLastUpdate());
     return runStatus;
 
 }
 
 /**
  * Obtain a GeobatchRunInfo from a resource
  * 
  * @param resource of geostore to obtain
  * @return GeobatchRunInfo
  */
 private GeobatchRunInfo getRunStatus(RESTResource resource) {
     GeobatchRunInfo runStatus = new GeobatchRunInfo();
     runStatus.setId(resource.getId());
     runStatus.setExternalUid(resource.getName());
     runStatus.setInternalUid(resource.getDescription());
     runStatus.setFlowUid(resource.getName());
     runStatus.setFlowStatus(resource.getMetadata());
     runStatus.setLastExecution(resource.getCreation());
     runStatus.setLastCheck(resource.getLastUpdate());
     return runStatus;
 }
 
 /**
  * @return RESTFlowService
  */
 public RESTFlowService getFlowService() {
     GeoBatchRESTClient client = new GeoBatchRESTClient();
     client.setGeostoreRestUrl(geobatchClientConfiguration.getGeobatchRestUrl());
     client.setUsername(geobatchClientConfiguration.getGeobatchUsername());
     client.setPassword(geobatchClientConfiguration.getGeobatchPassword());
     return client.getFlowService();
 }
 
 /**
  * Search unique geobatch run by Id
  * 
  * @param runUid
  * @return geobatch run information or null if not found
  */
 private GeobatchRunInfo searchUnique(String runUid) {
     GeobatchRunInfo result = null;
     SearchFilter filter = new AndFilter(new FieldFilter(BaseField.NAME, runUid,
             SearchOperator.EQUAL_TO), new CategoryFilter(CATEGORY_NAME,
             SearchOperator.EQUAL_TO));
     ResourceList list = geostoreClient.searchResources(filter, 0,
             Integer.MAX_VALUE, true, false);
     if (list.getList() != null && !list.getList().isEmpty()) {
         result = getRunStatus(list.getList().get(0));
     }
     return result;
 }
 
 /**
  * Obtain run information status and update it on stored data
  * 
  * @param runInfo
  * 
  * @return run information updated
  */
 private GeobatchRunInfo updateRunInfoStatus(GeobatchRunInfo runInfo) {
     RESTConsumerStatus status = null;
     try {
         status = getFlowService().getConsumerStatus(
                 runInfo.getFlowUid());
     } catch (Exception e) {
         // status it's FAIL
     }
    if(status != null) {
    	runInfo.setFlowStatus(status.getStatus().name());
    	runInfo = updateRunInfo(runInfo);
    }
     return runInfo;
 }
 
 }
