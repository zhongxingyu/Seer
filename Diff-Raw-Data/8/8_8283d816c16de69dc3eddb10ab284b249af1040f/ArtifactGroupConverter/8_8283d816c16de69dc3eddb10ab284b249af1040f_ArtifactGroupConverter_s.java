 /**
  * Phresco Service Implemenation
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.service.converters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Order;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.RepoInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.CustomerDAO;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.ServiceConstants;
 
 public class ArtifactGroupConverter implements Converter<ArtifactGroupDAO, ArtifactGroup>, ServiceConstants {
 	
 	private MongoOperations mongoOperation  = null;
 	private static final SplunkLogger LOGGER = SplunkLogger.getSplunkLogger(ArtifactGroupConverter.class.getName());
 	private static Boolean isDebugEnabled = LOGGER.isDebugEnabled();
 	
 	@Override
     public ArtifactGroup convertDAOToObject(ArtifactGroupDAO artifactGroupDAO,
             MongoOperations mongoOperation) throws PhrescoException {
 		if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.convertDAOToObject:Entry");
 		}
 		this.mongoOperation = mongoOperation;
         ArtifactGroup artifactGroup = new ArtifactGroup();
         artifactGroup.setArtifactId(artifactGroupDAO.getArtifactId());
         artifactGroup.setClassifier(artifactGroupDAO.getClassifier());
         artifactGroup.setCustomerIds(artifactGroupDAO.getCustomerIds());
         artifactGroup.setGroupId(artifactGroupDAO.getGroupId());
         artifactGroup.setId(artifactGroupDAO.getId());
         artifactGroup.setImageURL(artifactGroupDAO.getImageURL());
         artifactGroup.setName(artifactGroupDAO.getName());
         artifactGroup.setPackaging(artifactGroupDAO.getPackaging());
         artifactGroup.setType(artifactGroupDAO.getType());
         artifactGroup.setUsed(artifactGroupDAO.isUsed());
         artifactGroup.setDisplayName(artifactGroupDAO.getDisplayName());
         artifactGroup.setAppliesTo(artifactGroupDAO.getAppliesTo());
         Query query = new Query(Criteria.where(DB_COLUMN_ARTIFACT_GROUP_ID).is(artifactGroupDAO.getId()));
         query.limit(NUMBER_SEVEN).sort().on(DB_COLUMN_CREATIONDATE, Order.DESCENDING);
         List<ArtifactInfo> versions = mongoOperation.find(ARTIFACT_INFO_COLLECTION_NAME, 
         		query , ArtifactInfo.class);
         artifactGroup.setVersions(versions);
         artifactGroup.setLicenseId(artifactGroupDAO.getLicenseId());
         createArticatGroupURL(artifactGroup);
         if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.convertDAOToObject:Exit");
 		}
         return artifactGroup;
     }
 
     @Override
     public ArtifactGroupDAO convertObjectToDAO(ArtifactGroup artifactGroup)
             throws PhrescoException {
     	if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.convertObjectToDAO:Entry");
 		}
         ArtifactGroupDAO artifactGroupDAO = new ArtifactGroupDAO();
         artifactGroupDAO.setId(artifactGroup.getId());
         artifactGroupDAO.setArtifactId(artifactGroup.getArtifactId());
         artifactGroupDAO.setClassifier(artifactGroup.getClassifier());
         artifactGroupDAO.setCustomerIds(artifactGroup.getCustomerIds());
         artifactGroupDAO.setDescription(artifactGroup.getDescription());
         artifactGroupDAO.setGroupId(artifactGroup.getGroupId());
         artifactGroupDAO.setImageURL(artifactGroup.getImageURL());
         artifactGroupDAO.setName(artifactGroup.getName());
         artifactGroupDAO.setPackaging(artifactGroup.getPackaging());
         artifactGroupDAO.setSystem(artifactGroup.isSystem());
         artifactGroupDAO.setType(artifactGroup.getType());
         artifactGroupDAO.setUsed(artifactGroup.isUsed());
         artifactGroupDAO.setAppliesTo(artifactGroup.getAppliesTo());
         artifactGroupDAO.setHelpText(artifactGroup.getHelpText());
         artifactGroupDAO.setLicenseId(artifactGroup.getLicenseId());
         artifactGroupDAO.setDisplayName(artifactGroup.getDisplayName());
         artifactGroupDAO.setAppliesTo(artifactGroup.getAppliesTo());
         if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.convertObjectToDAO:Exit");
 		}
         return artifactGroupDAO;
     }
     
     private ArtifactGroup createArticatGroupURL(ArtifactGroup artifactGroup) {
     	if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.createArticatGroupURL:Entry");
 		}
 		List<ArtifactInfo> newVersions = new ArrayList<ArtifactInfo>();
 		if(CollectionUtils.isEmpty(artifactGroup.getVersions())) {
 			LOGGER.warn("ArtifactGroupConverter.createArticatGroupURL", "versions="+artifactGroup.getVersions());
 			return artifactGroup;
 		}
 		List<ArtifactInfo> actualVersions = artifactGroup.getVersions();
 		String customerId = artifactGroup.getCustomerIds().get(0);
 		for (ArtifactInfo artifactInfo : actualVersions) {
			String downloadURL = createDownloadURL(artifactGroup.getGroupId(), artifactGroup.getArtifactId(), 
					artifactGroup.getPackaging(), artifactInfo.getVersion(), customerId);
			artifactInfo.setDownloadURL(downloadURL);
 			newVersions.add(artifactInfo);
 		}
 		artifactGroup.setVersions(newVersions);
 		if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.createArticatGroupURL:Exit");
 		}
 		return artifactGroup;
 	}
     
 	private String createDownloadURL(String groupId, String artifactId, String packaging, String version, String customerId) {		
 		if (isDebugEnabled) {
 			LOGGER.debug("ArtifactGroupConverter.createDownloadURL:Entry");
 		}
 		if(StringUtils.isNotEmpty(groupId) && StringUtils.isNotEmpty(artifactId) && StringUtils.isNotEmpty(version)) {
 			CustomerDAO customer = mongoOperation.findOne(CUSTOMERS_COLLECTION_NAME, new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);			
 			if(customer != null) {
 				RepoInfo repoInfo = mongoOperation.findOne(REPOINFO_COLLECTION_NAME, 
 						new Query(Criteria.whereId().is(customer.getRepoInfoId())), RepoInfo.class);				
 				if(StringUtils.isNotEmpty(repoInfo.getGroupRepoURL())) {
 					return repoInfo.getGroupRepoURL() + "/" + ServerUtil.createContentURL(groupId, artifactId, version, packaging);
 				}
 				if (isDebugEnabled) {
 					LOGGER.debug("ArtifactGroupConverter.createDownloadURL:Exit");
 				}
 			}
 		}
 		return null;
 	}
 
 }
