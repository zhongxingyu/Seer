 package com.photon.phresco.service.converters;
 
 import org.springframework.data.document.mongodb.MongoOperations;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.util.ServiceConstants;
 
 public class ApplicationInfoConverter implements Converter<ApplicationInfoDAO, ApplicationInfo>, ServiceConstants{
 
 	@Override
 	public ApplicationInfo convertDAOToObject(ApplicationInfoDAO dao,
 			MongoOperations mongoOperation) throws PhrescoException {
 		ApplicationInfo applicationInfo = new ApplicationInfo();
 		applicationInfo.setCode(dao.getCode());
 		applicationInfo.setVersion(dao.getVersion());
 		applicationInfo.setCustomerIds(dao.getCustomerIds());
 		applicationInfo.setDescription(dao.getDescription());
 		applicationInfo.setEmailSupported(dao.isEmailSupported());
 		applicationInfo.setId(dao.getId());
 		applicationInfo.setName(dao.getName());
 		applicationInfo.setPilotInfo(dao.getPilotInfo());
		applicationInfo.setSelectedComponents(dao.getSelectedComponents());
		applicationInfo.setSelectedDatabases(dao.getSelectedDatabases());
 		applicationInfo.setSelectedFrameworks(dao.getSelectedFrameworks());
 		applicationInfo.setSelectedJSLibs(dao.getSelectedJSLibs());
 		applicationInfo.setSelectedModules(dao.getSelectedModules());
 		applicationInfo.setSelectedServers(dao.getSelectedServers());
 		applicationInfo.setSelectedWebservices(dao.getSelectedWebservices());
 		applicationInfo.setTechInfo(dao.getTechInfo());
 		if(dao.getArtifactGroupId() != null) {
 			applicationInfo.setPilotContent(createPilotContent(dao.getArtifactGroupId(), mongoOperation));
 		}
 		applicationInfo.setPhoneEnabled(dao.isPhoneEnabled());
 		applicationInfo.setTabletEnabled(dao.isTabletEnabled());
 		applicationInfo.setPilot(dao.getPilot());
 		applicationInfo.setSystem(dao.isSystem());
 		return applicationInfo;
 	}
 
 	@Override
 	public ApplicationInfoDAO convertObjectToDAO(ApplicationInfo applicationInfo)
 			throws PhrescoException {
 		ApplicationInfoDAO applicationInfoDAO = new ApplicationInfoDAO();
 		applicationInfoDAO.setId(applicationInfo.getId());
 		applicationInfoDAO.setCode(applicationInfo.getId());
 		applicationInfoDAO.setDescription(applicationInfo.getDescription());
 		applicationInfoDAO.setEmailSupported(applicationInfo.isEmailSupported());
 		applicationInfoDAO.setName(applicationInfo.getName());
 		applicationInfoDAO.setPilotInfo(applicationInfo.getPilotInfo());
 		applicationInfoDAO.setSelectedComponents(applicationInfo.getSelectedComponents());
 		applicationInfoDAO.setSelectedDatabases(applicationInfo.getSelectedDatabases());
 		applicationInfoDAO.setSelectedFrameworks(applicationInfo.getSelectedFrameworks());
 		applicationInfoDAO.setSelectedJSLibs(applicationInfo.getSelectedJSLibs());
 		applicationInfoDAO.setSelectedModules(applicationInfo.getSelectedModules());
 		applicationInfoDAO.setSelectedServers(applicationInfo.getSelectedServers());
 		applicationInfoDAO.setSelectedWebservices(applicationInfo.getSelectedWebservices());
 		applicationInfoDAO.setSystem(applicationInfo.isSystem());
 		applicationInfoDAO.setTechInfo(applicationInfo.getTechInfo());
 		applicationInfoDAO.setCustomerIds(applicationInfo.getCustomerIds());
 		applicationInfoDAO.setVersion(applicationInfo.getVersion());
 		if(applicationInfo.getPilotContent() != null) {
 			applicationInfoDAO.setArtifactGroupId(applicationInfo.getPilotContent().getId());
 		}
 		applicationInfoDAO.setPhoneEnabled(applicationInfo.isPhoneEnabled());
 		applicationInfoDAO.setTabletEnabled(applicationInfo.isTabletEnabled());
 		applicationInfoDAO.setPilot(applicationInfo.isPilot());
 		return applicationInfoDAO;
 	}
 	
 	private ArtifactGroup createPilotContent(String artifactGroupId, MongoOperations mongoOperation) throws PhrescoException {
 		ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(artifactGroupId)), ArtifactGroupDAO.class);
 		Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
 			(Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		return converter.convertDAOToObject(artifactGroupDAO, mongoOperation);
 	}
 
 }
