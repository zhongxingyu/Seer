 /**
  * Service Web Archive
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
 package com.photon.phresco.service.rest.api;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.core.io.ByteArrayResource;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.RequestPart;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ApplicationType;
 import com.photon.phresco.commons.model.ArtifactElement;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.ArtifactGroup.Type;
 import com.photon.phresco.commons.model.ArtifactInfo;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Element;
 import com.photon.phresco.commons.model.FunctionalFramework;
 import com.photon.phresco.commons.model.FunctionalFrameworkProperties;
 import com.photon.phresco.commons.model.License;
 import com.photon.phresco.commons.model.PlatformType;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.commons.model.Property;
 import com.photon.phresco.commons.model.SettingsTemplate;
 import com.photon.phresco.commons.model.Technology;
 import com.photon.phresco.commons.model.TechnologyGroup;
 import com.photon.phresco.commons.model.TechnologyInfo;
 import com.photon.phresco.commons.model.TechnologyOptions;
 import com.photon.phresco.commons.model.WebService;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ApplicationTypeDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.CustomerDAO;
 import com.photon.phresco.service.dao.DownloadsDAO;
 import com.photon.phresco.service.dao.ProjectInfoDAO;
 import com.photon.phresco.service.dao.TechnologyDAO;
 import com.photon.phresco.service.impl.DbService;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ServiceConstants;
 import com.phresco.pom.site.Reports;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.BodyPartEntity;
 import com.sun.jersey.multipart.MultiPart;
 import com.wordnik.swagger.annotations.ApiError;
 import com.wordnik.swagger.annotations.ApiErrors;
 import com.wordnik.swagger.annotations.ApiOperation;
 import com.wordnik.swagger.annotations.ApiParam;
 
 @Controller
 @RequestMapping(value = ServiceConstants.REST_API_COMPONENT)
 public class ComponentService extends DbService {
 	
 	private static final SplunkLogger LOGGER = SplunkLogger.getSplunkLogger("SplunkLogger");
 	private static Boolean isDebugEnabled = LOGGER.isDebugEnabled();
 	private static RepositoryManager repositoryManager;
 	
 	private static String exceptionString ="PhrescoException Is";
 	
 	public ComponentService() throws PhrescoException {
 		super();
 		PhrescoServerFactory.initialize();
 		repositoryManager = PhrescoServerFactory.getRepositoryManager();
     }
 	
 	/**
 	 * Returns the list of apptypes
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Retrives application types based on customerId")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Applicationtype Not Found"), @ApiError(code=500, reason = "Failed")})
     @RequestMapping(value= REST_API_APPTYPES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<ApplicationType> findAppTypes(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(value = "CustomerId to retrive apptypes",name = "customerId")@QueryParam(REST_QUERY_CUSTOMERID) String customerId) 
 			throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findAppTypes : Entry");
 	        LOGGER.debug("ComponentService.findAppTypes ", "remoteAddress=" + request.getRemoteAddr() , "customer" + getCustomerNameById(customerId),
 	        		"endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"));
 	    }
 		try {
 			List<ApplicationType> applicationTypes = new ArrayList<ApplicationType>();
 			List<ApplicationType> applicationTypeDAOs = DbService.getMongoOperation().getCollection(APPTYPES_COLLECTION_NAME, ApplicationType.class);
 			
 			for (ApplicationType applicationType : applicationTypeDAOs) {
 				List<TechnologyGroup> techGroupss = new ArrayList<TechnologyGroup>();
 				Query query = createCustomerIdQuery(customerId);
 				Criteria appCriteria = Criteria.where("appTypeId").is(applicationType.getId());
 				query.addCriteria(appCriteria);
 				List<TechnologyGroup> techGroups = DbService.getMongoOperation().find(TECH_GROUP_COLLECTION_NAME, query, TechnologyGroup.class);
 				for (TechnologyGroup techGroup : techGroups) {
 					query = createCustomerIdQuery(customerId);
 					Criteria criteria = Criteria.where("techGroupId").is(techGroup.getId());
 					query.addCriteria(criteria);
 					List<TechnologyInfo> techInfos = DbService.getMongoOperation().find("techInfos", query, TechnologyInfo.class);
 					techGroup.setTechInfos(techInfos);
 					techGroupss.add(techGroup);
 				}
 				applicationType.setTechGroups(techGroupss);
 				applicationTypes.add(applicationType);
 			}
 			if(CollectionUtils.isEmpty(applicationTypes)) {
 				if (isDebugEnabled) {			        LOGGER.debug("ComponentService.findAppTypes", "status=\"Not Found\"", "remoteAddress=" + request.getRemoteAddr() , 
 			        		"customer" + getCustomerNameById(customerId), "endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"));
 			    }
 				response.setStatus(204);
 				return applicationTypes;
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findAppTypes : Exit");
 		    }
 	        return applicationTypes;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.findAppTypes", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of apptypes
 	 * @param appTypes
 	 * @return 
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Creates list of apptypes")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable To Create")})
     @RequestMapping(value= REST_API_APPTYPES, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createAppTypes(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(value = "List of apptypes to create",	name = "appTypes")@RequestBody List<ApplicationType> appTypes) 
 			throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createAppTypes : Entry");
 	        LOGGER.debug("ComponentService.createAppTypes" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() + 
 	        		"user=" + request.getParameter("userId") , "appType=" + appTypes.get(0).getId());
         }
 		try {
 			for (ApplicationType applicationType : appTypes) {
 				if(validate(applicationType)) {
 					DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , applicationType);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.createAppTypes" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , 
 		        		"endpoint=" + request.getRequestURI(), "user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
 	        }
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createAppTypes : Exit");
         }
 	}
 	
 	/**
 	 * Updates the list of apptypes
 	 * @param appTypes
 	 * @return
 	 */
 	@ApiOperation(value = " Updates list of apptypes")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable To Update")})
     @RequestMapping(value= REST_API_APPTYPES, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateAppTypes(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(value = "List of apptypes to update", name = "appTypes")@RequestBody List<ApplicationType> appTypes) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppTypes : Entry");
 	        LOGGER.debug("ComponentService.updateAppTypes" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "appType=" + appTypes.get(0).getId());
 	    }
 	    try {
 	        for (ApplicationType applicationType : appTypes) {
 	            DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , applicationType);
             }
 	        response.setStatus(200);
         } catch (Exception e) {
         	response.setStatus(500);
         	LOGGER.error("ComponentService.createAppTypes" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
             throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
         }
         if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppTypes : Exit");
 	    }
 	}
 	
 	/**
 	 * Deletes the list of apptypes
 	 * @param appTypes
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Deletes list of apptypes")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Operation Not Supported")})
     @RequestMapping(value= REST_API_APPTYPES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteAppTypes(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(value = "List of apptypes to delete", name = "appTypes")@RequestBody List<ApplicationType> appTypes) 
 			throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteAppTypes : Entry");
 	        LOGGER.debug("ComponentService.deleteAppTypes" , "remoteAddress=" + request.getRemoteAddr() ,"endpoint=" + request.getRequestURI() , 
     		"user=" + request.getParameter("userId"));
         }
 		response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error("ComponentService.deleteAppTypes" , "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the apptype by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = " Retrive apptype based on their id ")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Applicationtype Not Found")})
     @RequestMapping(value= REST_API_APPTYPES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody ApplicationType getApptype(@Context HttpServletRequest request,HttpServletResponse response, 
 			@ApiParam(name = "id" , required = true, value = "The id of the apptype that needs to be retrieved") 
 			@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getApptype : Entry");
 	        LOGGER.debug("ComponentService.getApptype", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 	    ApplicationType appType = null;
 		try {
 			appType = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationType.class);
 			if(appType != null) {
 				response.setStatus(200);
 				return appType;
 			} else {
 				if (isDebugEnabled) {
 			        LOGGER.warn("ComponentService.getApptype", "status=\"Bad Request\"" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "id=" + id);
 			    }
 			}
 			response.setStatus(204);
 		} catch (Exception e) {
 			if (isDebugEnabled) {
 				LOGGER.error("ComponentService.getApptype" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getApptype : Exit");
 	    }
 		return appType;
 	}
 	
 	/**
 	 * Updates the list of apptypes
 	 * @param appTypes
 	 * @return
 	 */
 	@ApiOperation(value = " Update apptype based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to update")})
     @RequestMapping(value= REST_API_APPTYPES + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateAppType(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "id" ,	required = true, value = "The id of the apptype that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "appType" ,required = true, value = "The apptype that needs to be update")@RequestBody ApplicationType appType) 
 	throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppType : Entry");
 	        LOGGER.debug("ComponentService.updateAppType" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 	        DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME, appType);
 	        response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if (isDebugEnabled) {
 				LOGGER.error("ComponentService.updateAppType" , "status=\"Failure\"", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00005, APPTYPES_COLLECTION_NAME);
         }
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppType : Exit");
 	    }
 	}
 	
 	/**
 	 * Deletes the apptype by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Delete apptype based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to delete")})
     @RequestMapping(value= REST_API_APPTYPES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteAppType(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "id" , required = true, value = "The id of the apptype that needs to be delete")
 			@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteAppType : Entry");
 	        LOGGER.debug("ComponentService.deleteAppType", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		try {
 			ApplicationTypeDAO appType = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(id)), ApplicationTypeDAO.class);
 			if(appType != null) {
 				List<String> techGroups = appType.getTechGroupIds();
 				if(CollectionUtils.isNotEmpty(techGroups)) {
 					for (String techGroupId : techGroups) {
 						deleteTechGroup(techGroupId, response);
 					}
 				}
 				DbService.getMongoOperation().remove(APPTYPES_COLLECTION_NAME, 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationTypeDAO.class);
 				response.setStatus(200);
 			} else {
 				if (isDebugEnabled) {
 			        LOGGER.warn("ComponentService.deleteAppType", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "status=\"Bad Request\"", "id=" + id);
 			    }
 			}
 		} catch (Exception e) {
 			response.setStatus(500);
 			if (isDebugEnabled) {
 				LOGGER.error("ComponentService.updateAppType" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId") ,"status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteAppType : Exit");
 	    }
 	}
 	
 	private void deleteTechGroup(String id, HttpServletResponse response) {
 		TechnologyGroup techGroup = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 		List<TechnologyInfo> techInfos = techGroup.getTechInfos();
 		for (TechnologyInfo technologyInfo : techInfos) {
 			deleteTechnologyObject(technologyInfo.getId(), response);
 		}
 		DbService.getMongoOperation().remove(TECH_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 	}
 	
 	private void deleteTechnologyObject(String id, HttpServletResponse response) {
 		Query query = new Query(Criteria.whereId().is(id));
 		TechnologyDAO technologyDAO = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 		if(isDebugEnabled) {
 			LOGGER.warn("ComponentService.deleteTechnologyObject", "id=" + id , "status=\"Bad Request\"", 
 					"message=" + "Technology Not Found");
 		}
 		if(technologyDAO != null) {
 			String archetypeGroupDAOId = technologyDAO.getArchetypeGroupDAOId();
 			deleteAttifact(archetypeGroupDAOId, response);
 			List<String> pluginIds = technologyDAO.getPluginIds();
 			if (CollectionUtils.isNotEmpty(pluginIds)) {
 				for (String pluginId : pluginIds) {
 					deleteAttifact(pluginId, response);
 				}
 			}
 			DbService.getMongoOperation().remove(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 		}
 	}
 	
 	/**
 	 * Returns the list of technologies
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives technologies ")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Technology not found"), @ApiError(code=500, reason = "Failed error caused")})
     @RequestMapping(value= REST_API_TECHNOLOGIES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<Technology> findTechnologies(@Context HttpServletRequest request,  HttpServletResponse response, 
 			@ApiParam(name = "customerId" ,	required = true, value = "The customerid to retrive technologies")@QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
 			@ApiParam(name = "appTypeId" ,required = true, value = "The apptypeid to retrive technologies")
 			@QueryParam(REST_QUERY_APPTYPEID) String appTypeId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findTechnologies : Entry");
 	        LOGGER.debug("ComponentService.findTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId), "appTypeId=" + appTypeId);
 	    }
 	    List<TechnologyDAO> techDAOList = new ArrayList<TechnologyDAO>();
 	    try {
 			Query query = createCustomerIdQuery(customerId);
 		   
 			if(StringUtils.isNotEmpty(appTypeId)) {
 			    query.addCriteria(Criteria.where(REST_QUERY_APPTYPEID).is(appTypeId));
 		    } 
 			
 			techDAOList = DbService.getMongoOperation().find(TECHNOLOGIES_COLLECTION_NAME, query, TechnologyDAO.class);
 			if(StringUtils.isEmpty(appTypeId) && !StringUtils.equals(customerId, DEFAULT_CUSTOMER_NAME)) {
 				CustomerDAO customerDAO = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
 						new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
 				List<String> applicableTechnologies = customerDAO.getApplicableTechnologies();
 				if(CollectionUtils.isNotEmpty(applicableTechnologies)) {
 					List<TechnologyDAO> customerTechs = DbService.getMongoOperation().find(TECHNOLOGIES_COLLECTION_NAME, 
 							new Query(Criteria.whereId().in(applicableTechnologies.toArray())), TechnologyDAO.class);
 					techDAOList.addAll(customerTechs);
 				}
 			}
 			
 		    List<Technology> techList = new ArrayList<Technology>();
 			Converter<TechnologyDAO, Technology> technologyConverter = 
 		          (Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
 
 			for (TechnologyDAO technologyDAO : techDAOList) {
 				Technology technology = technologyConverter.convertDAOToObject(technologyDAO, DbService.getMongoOperation());
 				techList.add(technology);
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 		    }
 			if (CollectionUtils.isEmpty(techList)) {
 				response.setStatus(204);
 				if (isDebugEnabled) {
 			        LOGGER.warn("ComponentService.findTechnologies", "remoteAddress=" + request.getRemoteAddr() , 
 			        		"endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"), "status=\"Not Found\"");
 			    }
 			}
 			
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findTechnologies : Exit");
 		    }
 			return techList;
 		} catch (Exception e) {
 			e.printStackTrace();
 			response.setStatus(500);
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findTechnologies ", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECHNOLOGIES_COLLECTION_NAME);
 		}
     	 
 	}
 	
 	/**
 	 * Creates the list of technologies
 	 * @param technologies
 	 * @throws IOException 
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Creates technology ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to create technology")})
     @RequestMapping(value= REST_API_TECHNOLOGIES, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createTechnologies(MultipartHttpServletRequest request,HttpServletResponse response,
 			@RequestParam("technology")byte[] techJson) throws PhrescoException, IOException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createTechnologies");
 	        LOGGER.debug("ComponentService.createTechnologies" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 	    createOrUpdateTechnology(request, techJson);
 	}
 
 	private void createOrUpdateTechnology(MultipartHttpServletRequest request,
 			byte[] techJson) throws IOException, PhrescoException {
 		byte[] archetypeJar = null;
 	    Map<String, byte[]> pluginMap = new HashMap<String, byte[]>();
 	    Map<String, ArtifactGroup> pluginInfoMap = new HashMap<String, ArtifactGroup>();
 	    
 	    Technology technology = new Gson().fromJson(new String(techJson), Technology.class);
 	    List<ArtifactGroup> pluginsInfo = technology.getPlugins();
 	    Map<String, MultipartFile> fileMap = request.getFileMap();
 	    Set<String> keySet = fileMap.keySet();
 	    for (String key : keySet) {
 	    	if(key.equals(technology.getName())) {
 	    		archetypeJar = fileMap.get(key).getBytes();
 	    	} else {
 	    		for (ArtifactGroup plugin : pluginsInfo) {
 					if(plugin.getName().equals(key)) {
 						pluginMap.put(plugin.getName(), fileMap.get(key).getBytes());
 						pluginInfoMap.put(plugin.getName(), plugin);
 						break;
 					}
 				}
 	    	}
 		}
 	    // Save archetype jar
 	    if(archetypeJar != null) {
 	    	boolean saveArchetype = saveArtifactFile(technology.getArchetypeInfo(), archetypeJar);
 	    	if(!saveArchetype) {
 	    		throw new PhrescoException("Archetype Creation Failed...");
 	    	}
 	    }
 	    //To save plugin jars
 	    if(pluginMap != null) {
 	    	Set<String> pluginNames = pluginMap.keySet();
 	    	for (String name : pluginNames) {
 	    		boolean savePlugin = saveArtifactFile(pluginInfoMap.get(name), pluginMap.get(name));
 	    		if(!savePlugin) {
 	    			throw new PhrescoException("Plugin Creation Failed...");
 	    		}
 			}
 	    }
 	    saveTechnology(technology);
 	}
 	
 	
 	private void saveOrUpdateTechnology(MultiPart multiPart, HttpServletResponse response) throws PhrescoException {
 		Technology technology = null;
 		List<BodyPart> entities = new ArrayList<BodyPart>();
 		Map<ArtifactGroup, BodyPart> archetypeMap = new HashMap<ArtifactGroup, BodyPart>();
 		Map<ArtifactGroup, BodyPart> pluginMap = new HashMap<ArtifactGroup, BodyPart>();
 
 		// To separete the object and binary file
 		List<BodyPart> bodyParts = multiPart.getBodyParts();
 		for (BodyPart bodyPart : bodyParts) {
 			if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_VALUE)) {
 				technology = bodyPart.getEntityAs(Technology.class);
 			} else {
 				entities.add(bodyPart);
 			}
 		}
 
 		if (technology == null) {
 			if(isDebugEnabled) {
 				LOGGER.debug("ComponentService.saveOrUpdateTechnology", "status=\"Bad Request\"" ,"message=" + "Technology Is Null");
 			}
 			response.setStatus(500);
 		}
 		if(isDebugEnabled) {
 			LOGGER.debug("ComponentService.saveOrUpdateTechnology", "customer" + getCustomerNameById(technology.getCustomerIds().get(0)), 
 					"techId=" + technology.getId());
 		}
 		for (BodyPart bodyPart : entities) {
 			if (bodyPart.getContentDisposition().getFileName()
 					.equals(technology.getName())) {
 				archetypeMap.put(technology.getArchetypeInfo(), bodyPart);
 			} else {
 				List<ArtifactGroup> plugins = technology.getPlugins();
 				for (ArtifactGroup artifactGroup : plugins) {
 					if (artifactGroup.getName().equals(
 							bodyPart.getContentDisposition().getFileName())) {
 						pluginMap.put(artifactGroup, bodyPart);
 					}
 				}
 			}
 		}
 
 		Set<ArtifactGroup> archetypeSet = archetypeMap.keySet();
 		for (ArtifactGroup artifactGroup : archetypeSet) {
 			createArtifacts(artifactGroup, archetypeMap.get(artifactGroup));
 		}
 
 		Set<ArtifactGroup> pluginSet = pluginMap.keySet();
 		for (ArtifactGroup artifactGroup : pluginSet) {
 			createArtifacts(artifactGroup, pluginMap.get(artifactGroup));
 		}
 		saveTechnology(technology);
 		if(isDebugEnabled) {
 			LOGGER.debug("ComponentService.saveOrUpdateTechnology : Exit");
 		} 
 	}
 
 	private void createArtifacts(ArtifactGroup artifactGroup, BodyPart bodyPart) throws PhrescoException {
 		BodyPartEntity bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
 		File artifactFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null, 
 				artifactGroup.getPackaging(), artifactGroup.getName());
 		if(!artifactFile.exists()) {
 			if(isDebugEnabled) {
 				LOGGER.info("ComponentService.createArtifacts", "customer" + getCustomerNameById(artifactGroup.getCustomerIds().get(0)), 
 						"artifactId=" + artifactGroup.getId() + "message=" + "Artifact File Not Found");
 			}
 		}
 		uploadBinary(artifactGroup, artifactFile);
 	}
 
 	private void saveTechnology(Technology technology) throws PhrescoException {
 		if(!validate(technology)) {
 			return;
 		}
     	Converter<TechnologyDAO, Technology> techConverter = 
     		(Converter<TechnologyDAO, Technology>) ConvertersFactory.getConverter(TechnologyDAO.class);
     	TechnologyDAO technologyDAO = techConverter.convertObjectToDAO(technology);
     	String archetypeId = createArchetypeId(technology);
     	technologyDAO.setArchetypeGroupDAOId(archetypeId);
     	ArtifactGroup archetypeInfo = technology.getArchetypeInfo();
     	archetypeInfo.setId(archetypeId);
     	saveModuleGroup(archetypeInfo);
     	if(CollectionUtils.isNotEmpty(technology.getPlugins())) {
     		List<String> pluginIds = createPluginIds(technology);
         	technologyDAO.setPluginIds(pluginIds);
         	for (ArtifactGroup plugin : technology.getPlugins()) {
         		ArtifactGroupDAO agDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
         				new Query(Criteria.where("artifactId").is(plugin.getArtifactId())), ArtifactGroupDAO.class);
         		if(agDAO != null) {
         			plugin.setId(agDAO.getId());
         		}
 				saveModuleGroup(plugin);
 			}
     	}
     	
     	addTechnologyToGroup(technologyDAO);
     	DbService.getMongoOperation().save(TECHNOLOGIES_COLLECTION_NAME, technologyDAO);
     	DbService.getMongoOperation().save("techInfos", createTechInfo(technologyDAO));
 	}
 	
 	private TechnologyInfo createTechInfo(TechnologyDAO dao) {
 		TechnologyInfo info = new TechnologyInfo();
 		info.setId(dao.getId());
 		info.setAppTypeId(dao.getAppTypeId());
 		info.setName(dao.getName());
 		info.setCustomerIds(dao.getCustomerIds());
 		info.setTechGroupId(dao.getTechGroupId());
 		return info;
 	}
 	
 	private void addTechnologyToGroup(TechnologyDAO technologyDAO) {
 		TechnologyGroup technologyGroup = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(technologyDAO.getTechGroupId())), TechnologyGroup.class);
 		if(technologyGroup != null) {
 			List<TechnologyInfo> foundTechInfos = technologyGroup.getTechInfos();
 			TechnologyInfo info = new TechnologyInfo();
 			info.setId(technologyDAO.getId());
 			info.setAppTypeId(technologyDAO.getAppTypeId());
 			info.setName(technologyDAO.getName());
 			info.setCreationDate(technologyDAO.getCreationDate());			
 			if(CollectionUtils.isEmpty(foundTechInfos)) {
 				foundTechInfos = new ArrayList<TechnologyInfo>();
 				foundTechInfos.add(info);
 			} else {
 				foundTechInfos.add(info);    
 			}
 			technologyGroup.setTechInfos(foundTechInfos);
 			DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, technologyGroup);
 		}
 	}
 
 	private List<String> createPluginIds(Technology technology) {
 		List<String> pluginIds = new ArrayList<String>();
 		TechnologyDAO techDAO = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(technology.getId())), TechnologyDAO.class);
 		if(techDAO == null) {
 			for (ArtifactGroup plugin : technology.getPlugins()) {
 				pluginIds.add(plugin.getId());
 			}
 			return pluginIds;
 		}
 		pluginIds = techDAO.getPluginIds();
 		List<ArtifactGroup> plugins = technology.getPlugins();
 		for (ArtifactGroup artifactGroup : plugins) {
 			if(CollectionUtils.isNotEmpty(pluginIds)) {
 				boolean pluginAvailable = checkPluginAvailable(pluginIds, artifactGroup.getArtifactId());
 				if(pluginAvailable) {
 					pluginIds.add(artifactGroup.getId());
 				} else {
 					pluginIds.add(artifactGroup.getId());
 				}
 			}
 		}
 		return pluginIds;
 	}
 
 	private boolean checkPluginAvailable(List<String> pluginIds, String artifactId) {
 		for (String pluginId : pluginIds) {
 			ArtifactGroupDAO pluginFound = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(pluginId)), ArtifactGroupDAO.class);
 			if(pluginFound != null) {
 				if(pluginFound.getArtifactId().equals(artifactId)) {
 					return false;
 				} else {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private String createArchetypeId(Technology technology) {
 		TechnologyDAO techDAO = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(technology.getId())), TechnologyDAO.class);
 		if(techDAO == null) {
 			return technology.getArchetypeInfo().getId();
 		} else {
 			return techDAO.getArchetypeGroupDAOId();
 		}
 	}
 
 	/**
 	 * Updates the list of technologies
 	 * @param technologies
 	 * @return
      * @throws PhrescoException 
 	 * @throws IOException 
 	 */
 	@ApiOperation(value = " Updates technology ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to update technology")})
     @RequestMapping(value= REST_API_TECHNOLOGIES, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public void updateTechnologies(MultipartHttpServletRequest request,HttpServletResponse response,
 			@RequestParam("technology")byte[] techJson) throws PhrescoException, IOException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateTechnologies : Entry");
 	        LOGGER.debug("ComponentService.createTechnologies" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 		createOrUpdateTechnology(request, techJson);
 	}
 	
 	/**
 	 * Deletes the list of technologies
 	 * @param technologies
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Deletes list of technologies ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Operation not supported")})
     @RequestMapping(value= REST_API_TECHNOLOGIES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteTechnologies(@Context HttpServletRequest request, HttpServletResponse response , 
 			@ApiParam(name = "technologies" , required = true, value = "List of technologies ")@RequestBody List<Technology> technologies) 
 			throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteTechnologies : Entry");
 	        LOGGER.debug("ComponentService.deleteTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error("ComponentService.deleteTechnologies", "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		throw phrescoException;
 	}
 	
 	
 	/**
 	 * Get the technology by id for the given parameter
 	 * @param id
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Retrives technology base on their id ")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Technology not found")})
     @RequestMapping(value= REST_API_TECHNOLOGIES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, 
     		method = RequestMethod.GET)
 	public @ResponseBody Technology getTechnology(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "id" , required = true, value = "The id of the technology that needs to be retrieved")
 			@PathVariable(REST_API_PATH_PARAM_ID) String id) throws PhrescoException {
 	    if (isDebugEnabled) {
 	    	LOGGER.debug("ComponentService.getTechnology : Entry");
 	    	LOGGER.debug("ComponentService.deleteTechnologies", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		Technology technology = getTechnologyById(id);
 		if(technology == null) {
 			response.setStatus(204);
 			LOGGER.warn("ComponentService.deleteTechnologies", "remoteAddress=" + request.getRemoteAddr() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id , "status=\"Not Found\"");
 		}
 		response.setStatus(200);
 		if (isDebugEnabled) {
 	    	LOGGER.debug("ComponentService.getTechnology : Exit");
 	    }
 		return technology;
 	}
 	
 	/**
 	 * Updates the technology given by the parameter
 	 * @param id
 	 * @param technology
 	 * @return
 	 */
 	@ApiOperation(value = " Updates technology based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to update technology")})
     @RequestMapping(value= REST_API_TECHNOLOGIES + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateTechnology(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "id" , required = true, value = "The id of the technology that needs to be update")
 			@PathVariable(REST_API_PATH_PARAM_ID) String id , @ApiParam(name = "technology" , required = true, 
 					value = "The technology that needs to be update")Technology technology) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateTechnology");
 	        LOGGER.debug("ComponentService.updateTechnology" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "techId=" + technology.getId());
 	    }
 		try {
 			if (id.equals(technology.getId())) {
 				DbService.getMongoOperation().save(TECHNOLOGIES_COLLECTION_NAME, technology);
 				response.setStatus(200);
 			} 
 		} catch (Exception e) {
 			response.setStatus(500);
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.updateTechnology" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateTechnology : Exit");
 	    }
 	}
 	
 	/**
 	 * Deletes the server by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Deletes technology based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to update technology")})
     @RequestMapping(value= REST_API_TECHNOLOGIES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, 
     		method = RequestMethod.DELETE)
 	public @ResponseBody void deleteTechnology(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , required = true, value = "The id of the technology that needs to be delete")
 			@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteTechnology : Entry");
 	        LOGGER.debug("ComponentService.deleteTechnology" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			deleteTechnologyObject(id, response);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		LOGGER.debug("ComponentService.deleteTechnology : Exit");
 	}
     
 	/**
 	 * Returns the list of settings
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives settings ")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Settings not found"), @ApiError(code=204, reason = "Failed")})
     @RequestMapping(value= REST_API_SETTINGS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<SettingsTemplate> findSettings(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "customerId" , value = "Customerid to retrive settings")@QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
 			@ApiParam(name = "techId" , value = "Techid to retrive settings")@QueryParam(REST_QUERY_TECHID) String techId, 
 			@ApiParam(name = "type" , value = "Type to retrive settings")@QueryParam(REST_QUERY_TYPE) String type) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findSettings : Entry");
 	        LOGGER.debug("ComponentService.findSettings" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId) , "techId=" + techId, "type=" + type);
 	    }
 	    List<SettingsTemplate> settings = new ArrayList<SettingsTemplate>();
 		try {
 			Query query = new Query();
 			if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
 			if(StringUtils.isNotEmpty(techId)) {
 			    query.addCriteria(Criteria.where("appliesToTechs._id").is(techId));
 			} else {
 				Criteria customerCri = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(Arrays.asList(DEFAULT_CUSTOMER_NAME, customerId).toArray());
 				query.addCriteria(customerCri);
 			}
 			if(StringUtils.isNotEmpty(type)) {
 				query.addCriteria(Criteria.where(REST_API_NAME).is(type));
				SettingsTemplate setting = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME, 
						query, SettingsTemplate.class);
				response.setStatus(200);
				return settings;
 			}
 			List<SettingsTemplate> settingsList = DbService.getMongoOperation().find(SETTINGS_COLLECTION_NAME, query, SettingsTemplate.class);
 			for (SettingsTemplate settingsTemplate : settingsList) {
 				List<Element> types = getTypes(settingsTemplate.getName(), customerId);
 				settingsTemplate.setPossibleTypes(types);
 				settings.add(settingsTemplate);
 			}
 			if(CollectionUtils.isEmpty(settings)) {
 				if(isDebugEnabled) {
 					LOGGER.debug("ComponentService.findSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "status=\"Not Found\"", "message=\"" + "Settings Not Found" + "\"");
 				}
 				response.setStatus(204);
 				return settings;
 			}
 			if(isDebugEnabled) {
 				LOGGER.debug("ComponentService.findSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 				LOGGER.debug("ComponentService.findSettings : Exit");
 			}
 			return settings;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 	}
 	
 	private List<Element> getTypes(String type, String customerId) {
 		List<Element> types = new ArrayList<Element>();
 		Query query = createCustomerIdQuery(customerId);
 		Criteria typeCriteria = Criteria.where(CATEGORY).is(type.toUpperCase());
 		query.addCriteria(typeCriteria);
 		List<DownloadsDAO> downloads = DbService.getMongoOperation().find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 		for (DownloadsDAO downloadsDAO : downloads) {
 			Element element = new Element();
 			element.setId(downloadsDAO.getId());
 			element.setName(downloadsDAO.getName());
 			types.add(element);
 		}
 		return types;
 	}
 	
 	/**
 	 * Creates the list of settings
 	 * @param settings
 	 * @return 
 	 */
 	@ApiOperation(value = " Create list of settings ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to create setting")})
     @RequestMapping(value= REST_API_SETTINGS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createSettings(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "settings" , value = "List of settings to create ")@RequestBody List<SettingsTemplate> settings) {
 		if (isDebugEnabled) {
 		    LOGGER.debug("ComponentService.createSettings : Entry");
 		    LOGGER.debug("ComponentService.createSettings" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(settings.get(0).getCustomerIds().get(0)));
 		}
 		try {
 			for (SettingsTemplate settingsTemplate : settings) {
 				if(validate(settingsTemplate)) {
 					DbService.getMongoOperation().save(SETTINGS_COLLECTION_NAME, settingsTemplate);
 				}
 			}
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.createSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		if (isDebugEnabled) {
 		    LOGGER.debug("ComponentService.createSettings : Exit");
 		}
 	}
 	
 	/**
 	 * Updates the list of settings
 	 * @param settings
 	 * @return
 	 */
 	@ApiOperation(value = " Updates list of settings ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to update setting")})
     @RequestMapping(value= REST_API_SETTINGS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateSettings(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "settings" , value = "List of settings to update ")@RequestBody List<SettingsTemplate> settings) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateSettings : Entry");
 	        LOGGER.debug("ComponentService.updateSettings" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(settings.get(0).getCustomerIds().get(0)));
 	    }
 		
 		try {
 			for (SettingsTemplate settingTemplate : settings) {
 				SettingsTemplate settingTemplateInfo = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(settingTemplate.getId())), SettingsTemplate.class);
 				if (settingTemplateInfo != null) {
 					DbService.getMongoOperation().save(SETTINGS_COLLECTION_NAME, settingTemplate);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.createSettings", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 		    LOGGER.debug("ComponentService.updateSettings : Exit");
 		}
 	}
 	
 	/**
 	 * Deletes the list of settings
 	 * @param settings
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Deletes list of settings ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Operation not supported")})
     @RequestMapping(value= REST_API_SETTINGS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteSettings(HttpServletResponse response, @ApiParam(name = "settings" , 
 			value = "List of settings to Delete ")@RequestBody List<SettingsTemplate> settings) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteSettings : Entry");
 	    }
 		response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if(isDebugEnabled) {
 			LOGGER.error("ComponentService.deleteSettings", "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		}
 		throw phrescoException;
 		
 	}
 	
 	/**
 	 * Get the settingTemplate by id 
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = " Retrive setting based on their id ")
 	@ApiErrors(value = {@ApiError(code=204, reason = "Setting not found"),@ApiError(code=500, reason = "Failed")})
     @RequestMapping(value= REST_API_SETTINGS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody SettingsTemplate getSettingsTemplate(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of settings to retrive")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getSettingsTemplate : Entry");
 	        LOGGER.debug("ComponentService.getSettingsTemplate" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 	    SettingsTemplate settingTemplate = null;
 		try {
 			settingTemplate = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class); 
 			if (settingTemplate == null) {
 				response.setStatus(204);
 				return settingTemplate;
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getSettingsTemplate", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getSettingsTemplate : Exit");
 	    }
 		return settingTemplate;
 	}
 	
 	/**
 	 * Updates the list of setting
 	 * @param setting
 	 * @return
 	 */
 	@ApiOperation(value = " Update setting based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to update setting")})
     @RequestMapping(value= REST_API_SETTINGS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateSetting(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of settings to update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "setting" , value = "Setting data thats needs to update")@RequestBody SettingsTemplate setting) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateAppType : Entry");
 	        LOGGER.debug("ComponentService.getSettingsTemplate" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id , "customer" + getCustomerNameById(setting.getCustomerIds().get(0)));
 	    }
 		try {
 			SettingsTemplate fromDb = DbService.getMongoOperation().findOne(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class);
 			List<String> customerIds = fromDb.getCustomerIds();
 			if(!customerIds.contains(setting.getCustomerIds().get(0))) {
 				customerIds.add(setting.getCustomerIds().get(0));
 			}
 			List<Element> appliesToTechs = fromDb.getAppliesToTechs();
 			List<Element> newAppliesToTechs = setting.getAppliesToTechs();
 			for (Element element : newAppliesToTechs) {
 				if(! checkPreviouslyAvail(appliesToTechs, element)) {
 					appliesToTechs.add(element);
 				}
 			}
 			setting.setAppliesToTechs(appliesToTechs);
 			setting.setCustomerIds(customerIds);
 			DbService.getMongoOperation().save(SETTINGS_COLLECTION_NAME, setting);
 			response.setStatus(200);
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.updateAppType : Exit");
 		    }
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateAppType", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	private boolean checkPreviouslyAvail(List<Element> appliesToTechsInDb, Element newTech) {
 		for (Element element : appliesToTechsInDb) {
 			if(element.getId().equals(newTech.getId())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Deletes the settingsTemplate by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Delete settings based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unable to delete setting")})
     @RequestMapping(value= REST_API_SETTINGS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteSettingsTemplate(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of settings to delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteSettingsTemplate : Entry");
 	        LOGGER.debug("ComponentService.deleteSettingsTemplate" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			DbService.getMongoOperation().remove(SETTINGS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), SettingsTemplate.class);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.deleteSettingsTemplate", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteSettingsTemplate : Exit");
 	    }
 	}
 	
 	/**
 	 * Returns the list of modules
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives features ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed"), @ApiError(code=204, reason = "Features not found")})
     @RequestMapping(value= REST_API_MODULES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<ArtifactGroup> findModules(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "type" , value = "Feature type to retrive")@QueryParam(REST_QUERY_TYPE) String type, 
 			@ApiParam(name = "customerId" , value = "Customerid to retrive")@QueryParam(REST_QUERY_CUSTOMERID) String customerId,
 			@ApiParam(name = "techId" , value = "Techid to retrive")@QueryParam(REST_QUERY_TECHID) String techId, 
 			@ApiParam(name = "count" , value = "Limit value")@QueryParam(REST_LIMIT_VALUE) String count,
 			@ApiParam(name = "start" , value = "Start value")@QueryParam(REST_SKIP_VALUE) String start) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findModules : Entry");
 	        LOGGER.debug("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId), "techId=" + techId);
 	    }
 	    List<ArtifactGroup> modules = null;
 		try {
 			Query query = new Query();
 			if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
 			Criteria typeQuery = Criteria.where(DB_COLUMN_ARTIFACT_GROUP_TYPE).is(type);
 			List<String> technologies = new ArrayList<String>();
 			technologies.add(techId);
 			if(StringUtils.isNotEmpty(techId)) {
 				Technology techInDB = getTechnologyById(techId);
 				if(CollectionUtils.isNotEmpty(techInDB.getArchetypeFeatures())) {
 					technologies.addAll(techInDB.getArchetypeFeatures());
 				}
 			}
 			Criteria techIdQuery = Criteria.where(DB_COLUMN_APPLIESTOTECHID).in(technologies.toArray());
 			query = query.addCriteria(typeQuery);
 			query = query.addCriteria(techIdQuery);
 			if(StringUtils.isNotEmpty(count)){
 				query.skip(Integer.parseInt(start)).limit(Integer.parseInt(count));
 			}
 			List<ArtifactGroupDAO> artifactGroupDAOs = DbService.getMongoOperation().find(ARTIFACT_GROUP_COLLECTION_NAME,
 					query, ArtifactGroupDAO.class);
 			if(CollectionUtils.isEmpty(artifactGroupDAOs)) {
 				if(isDebugEnabled) {
 					LOGGER.warn("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "status=\"Not Found\"", "message=" + "Not Found");
 				}
 				response.setStatus(204);
 		    	return modules;
 		    }
 			if(isDebugEnabled) {
 				LOGGER.info("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 			}
 		    modules = convertDAOToModule(artifactGroupDAOs);
 		    if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findModules : Exit");
 		    }
 			return modules;
 		    
 		} catch(Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.deleteSettingsTemplate", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_GROUP_COLLECTION_NAME);
 		}
 
 	}
 	
     private List<ArtifactGroup> convertDAOToModule(List<ArtifactGroupDAO> moduleDAOs) throws PhrescoException {
 		try {
 			Converter<ArtifactGroupDAO, ArtifactGroup> artifactConverter = 
 			    (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 			List<ArtifactGroup> modules = new ArrayList<ArtifactGroup>();
 			for (ArtifactGroupDAO artifactGroupDAO : moduleDAOs) {
 				ArtifactGroup artifactGroup = artifactConverter.convertDAOToObject(artifactGroupDAO, DbService.getMongoOperation());
 				ArtifactElement artifactElement = DbService.getMongoOperation().findOne(ARTIFACT_ELEMENT_COLLECTION_NAME, 
 						new Query(Criteria.whereId().is(artifactGroupDAO.getId())), ArtifactElement.class);
 			    if(artifactElement != null) {
 			    	artifactGroup.setDescription(artifactElement.getDescription());
 			    	artifactGroup.setHelpText(artifactElement.getHelpText());
 			    	artifactGroup.setSystem(artifactElement.isSystem());
 			    	artifactGroup.setLicenseId(artifactElement.getLicenseId());
 			    	artifactGroup.setCreationDate(artifactElement.getCreationDate());
 			    }
 				modules.add(artifactGroup);
 			}
 			return modules;
 		} catch (PhrescoException e) {
 			throw new PhrescoException(e);
 		}
     }
 
     /**
      * Creates the list of modules
      * @param modules
      * @return 
      * @return 
      * @throws PhrescoException 
      * @throws IOException 
      */
     @ApiOperation(value = " Creates new features ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Feature creation failed")})
     @RequestMapping(value= REST_API_MODULES, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
     public @ResponseBody ArtifactGroup createModules(HttpServletResponse response, MultipartHttpServletRequest request, 
     		@RequestPart(value = "feature", required = false) ByteArrayResource moduleFile, 
     		@RequestPart(value = "icon", required = false) ByteArrayResource iconFile,
     		@RequestParam("moduleGroup") byte[] artifactGroupData)
     		throws PhrescoException, IOException {
         if (isDebugEnabled) {
         	LOGGER.debug("ComponentService.createModules : Entry");
         	LOGGER.debug("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
         }
         String string = new String(artifactGroupData);
         ArtifactGroup artifactGroup = new Gson().fromJson(string, ArtifactGroup.class);
         if(moduleFile != null) {
         	boolean saveArtifactFile = saveArtifactFile(artifactGroup, moduleFile.getByteArray());
         	if(!saveArtifactFile) {
         		throw new PhrescoException("Unable to create artifact");
         	}
         }
         if(iconFile != null) {
         	artifactGroup.setPackaging(ICON_EXT);
         	boolean saveArtifactFile = saveArtifactFile(artifactGroup, iconFile.getByteArray());
         	if(!saveArtifactFile) {
         		throw new PhrescoException("Unable to create artifact");
         	}
         }
         saveModuleGroup(artifactGroup);
         return artifactGroup;
     }
     
     private boolean saveArtifactFile(ArtifactGroup artifactGroup, byte[] artifactFile) throws PhrescoException {
     	File artifcatFile = new File(ServerUtil.getTempFolderPath() + "/"
                 + artifactGroup.getName() + "." + artifactGroup.getPackaging());
 		boolean createIcon = false;
 		if(ServerUtil.convertByteArrayToFile(artifcatFile, artifactFile)) {
 			createIcon = uploadBinary(artifactGroup, artifcatFile);
 			FileUtil.delete(artifcatFile);
 		}
 		return createIcon;
     }
     
 	private boolean saveArtifactIcon(ArtifactGroup artifactGroup, byte[] iconByte) throws PhrescoException {
 		File iconFile = new File(ServerUtil.getTempFolderPath() + "/"
                 + artifactGroup.getName() + "." + ICON_EXT);
 		artifactGroup.setPackaging(ICON_EXT);
 		boolean createIcon = false;
 		if(ServerUtil.convertByteArrayToFile(iconFile, iconByte)) {
 			createIcon = uploadBinary(artifactGroup, iconFile);
 			FileUtil.delete(iconFile);
 		}
 		return createIcon;
 	}
 	
     private ArtifactGroup createOrUpdateFeatures(MultiPart moduleInfo, HttpServletResponse response) throws PhrescoException {
     	ArtifactGroup moduleGroup = null;
         File moduleFile = null;
         List<BodyPart> bodyParts = moduleInfo.getBodyParts();
         Map<String, BodyPartEntity> bodyPartEntityMap = new HashMap<String, BodyPartEntity>();
         moduleGroup = createBodyPart(moduleGroup, bodyParts, bodyPartEntityMap);
         if (isDebugEnabled) {
         	LOGGER.debug("ComponentService.createOrUpdateFeatures " , "customer" + getCustomerNameById(moduleGroup.getCustomerIds().get(0)), 
         			"id" + moduleGroup.getId());
         }
         if(bodyPartEntityMap.isEmpty()) {
         	saveModuleGroup(moduleGroup);
         }
         if (!bodyPartEntityMap.isEmpty()) {
         	BodyPartEntity bodyPartEntity = bodyPartEntityMap.get(Type.ARCHETYPE.name());
         	if (bodyPartEntity != null) {
         		if(moduleGroup.getType().name().equals(FEATURE_TYPE_JS)) {
 					moduleGroup.setGroupId(JS_GROUP_ID);
 					moduleGroup.setArtifactId(moduleGroup.getName().toLowerCase());
 				}
         		moduleFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null, 
         				moduleGroup.getPackaging(), moduleGroup.getName());
         		boolean uploadBinary = uploadBinary(moduleGroup, moduleFile);
                 if (uploadBinary) {
                 	saveModuleGroup(moduleGroup);
                 }
                 FileUtil.delete(moduleFile);
 			}
         	if(bodyPartEntityMap.get(Type.ICON.name()) != null) {
         		BodyPartEntity iconEntity = bodyPartEntityMap.get(Type.ICON.name());
             	File iconFile = ServerUtil.writeFileFromStream(iconEntity.getInputStream(), null, ICON_EXT, moduleGroup.getName());
             	moduleGroup.setPackaging(ICON_EXT);
         		boolean uploadBinary = uploadBinary(moduleGroup, iconFile);
         		FileUtil.delete(iconFile);
         		if(!uploadBinary) {
         			throw new PhrescoException("Module Icon Uploading Failed...");
         		}
         	}
         }
         response.setStatus(200);
         bodyPartEntityMap.clear();
         if (isDebugEnabled) {
         	LOGGER.debug("ComponentService.createOrUpdateFeatures : Exit");
         }
         return moduleGroup;
 	}
 
 	public ArtifactGroup createBodyPart(ArtifactGroup moduleGroup, List<BodyPart> bodyParts,
 			Map<String, BodyPartEntity> bodyPartEntityMap) {
 		if (CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE)) {
                     moduleGroup = bodyPart.getEntityAs(ArtifactGroup.class);
                 } else {
                 	bodyPartEntityMap.put(bodyPart.getContentDisposition().getFileName(), (BodyPartEntity) bodyPart.getEntity());
                 }
             }
         }
 		return moduleGroup;
 	}
 	
 	private void saveModuleGroup(ArtifactGroup moduleGroup) throws PhrescoException {
 		if(!validate(moduleGroup)) {
 			return;
 		}
         Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
         ArtifactGroupDAO moduleGroupDAO = converter.convertObjectToDAO(moduleGroup);
         
         List<String> versionIds = new ArrayList<String>();
         
         ArtifactGroupDAO moduleDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 		        new Query(Criteria.whereId().is(moduleGroupDAO.getId())), ArtifactGroupDAO.class);
         String newCustomerId = moduleGroup.getCustomerIds().get(0);
         List<String> customerIds = new ArrayList<String>();
         if(moduleDAO != null) {
         	 customerIds = moduleDAO.getCustomerIds();
              if(!moduleDAO.getCustomerIds().contains(newCustomerId)) {
              	customerIds.add(newCustomerId);
              	moduleGroupDAO.setCustomerIds(customerIds);
              }
         }
         com.photon.phresco.commons.model.ArtifactInfo newVersion = moduleGroup.getVersions().get(0);
         if(moduleDAO != null) {
         	moduleGroupDAO.setId(moduleDAO.getId());
         	versionIds.addAll(moduleDAO.getVersionIds());
         	List<com.photon.phresco.commons.model.ArtifactInfo> info = DbService.getMongoOperation().find(ARTIFACT_INFO_COLLECTION_NAME, 
         			new Query(Criteria.where(DB_COLUMN_ARTIFACT_GROUP_ID).is(moduleDAO.getId())), 
         			com.photon.phresco.commons.model.ArtifactInfo.class);
         	
         	List<com.photon.phresco.commons.model.ArtifactInfo> versions = new ArrayList<com.photon.phresco.commons.model.ArtifactInfo>();
         	newVersion.setArtifactGroupId(moduleDAO.getId());
         	versions.add(newVersion);
         	info.addAll(versions);
         	
         	String id = checkVersionAvailable(info, newVersion.getVersion());
         	if(id == newVersion.getId()) {
         		versionIds.add(newVersion.getId());
         	}
 			newVersion.setId(id);
 			DbService.getMongoOperation().save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }  else if(moduleDAO == null){
         	
         		versionIds.add(newVersion.getId());
         		newVersion.setArtifactGroupId(moduleGroupDAO.getId());
         		DbService.getMongoOperation().save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }
         moduleGroupDAO.setVersionIds(versionIds);
         DbService.getMongoOperation().save("artifactElement", createArtifactElement(moduleGroup));
         DbService.getMongoOperation().save(ARTIFACT_GROUP_COLLECTION_NAME, moduleGroupDAO);
     }
 	
 	private ArtifactElement createArtifactElement(ArtifactGroup artifactGroup) {
 		ArtifactElement artifactElement = new ArtifactElement();
 		artifactElement.setId(artifactGroup.getId());
 		artifactElement.setArtifactGroupId(artifactGroup.getId());
 		artifactElement.setHelpText(artifactGroup.getHelpText());
 		artifactElement.setDescription(artifactGroup.getDescription());
 		artifactElement.setCreationDate(artifactGroup.getCreationDate());
 		artifactElement.setSystem(artifactGroup.isSystem());
 		artifactElement.setLicenseId(artifactGroup.getLicenseId());
 		return artifactElement;
 	}
 	
 	private String checkVersionAvailable(List<com.photon.phresco.commons.model.ArtifactInfo> info, String version) {
 		for (com.photon.phresco.commons.model.ArtifactInfo artifactInfo : info) {
 			if(artifactInfo.getVersion().equals(version)) {
 				return artifactInfo.getId();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Updates the list of modules
 	 * @param modules
 	 * @return 
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Updates features ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Feature updation failed")})
     @RequestMapping(value= REST_API_MODULES, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody ArtifactGroup updateModules(HttpServletResponse response, MultipartHttpServletRequest request, 
     		@RequestPart(value = "feature", required = false ) ByteArrayResource moduleFile, 
     		@RequestPart(value = "icon", required = false) ByteArrayResource iconFile,
     		@RequestParam("data") byte[] artifactGroupData) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateModules :  Entry");
 	        LOGGER.debug("ComponentService.findModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 	    String string = new String(artifactGroupData);
         ArtifactGroup artifactGroup = new Gson().fromJson(string, ArtifactGroup.class);
         if(moduleFile != null) {
         	boolean saveArtifactFile = saveArtifactFile(artifactGroup, moduleFile.getByteArray());
         	if(!saveArtifactFile) {
         		throw new PhrescoException("Unable to create artifact");
         	}
         }
         if(iconFile != null) {
         	artifactGroup.setPackaging(ICON_EXT);
         	boolean saveArtifactFile = saveArtifactFile(artifactGroup, iconFile.getByteArray());
         	if(!saveArtifactFile) {
         		throw new PhrescoException("Unable to create artifact");
         	}
         }
         saveModuleGroup(artifactGroup);
         return artifactGroup;
 	}
 	
 	/**
 	 * Deletes the list of modules
 	 * @param features
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Delete list of features ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Operation not supported")})
     @RequestMapping(value= REST_API_MODULES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteModules(HttpServletResponse response, 
 		@ApiParam(name = "features" , value = "List of features") @RequestBody List<ArtifactGroup> features) 
 		throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules : Entry");
 	    }
 		response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules" ,"status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 	    }
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the module by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives feature based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed"), @ApiError(code=204, reason = "Feature not found")})
     @RequestMapping(value= REST_API_MODULES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody ArtifactGroup getModule(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of feature that needs to be retrive") @PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getModule : Entry");
 	        LOGGER.debug("ComponentService.getModule" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 	    ArtifactGroup moduleGroup = null;
 		try {
 			ArtifactGroupDAO moduleDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ArtifactGroupDAO.class);
 			if (moduleDAO != null) {
 		        Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
 		            (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
 		        moduleGroup = converter.convertDAOToObject(moduleDAO, DbService.getMongoOperation());
 		        ArtifactElement artifactElement = DbService.getMongoOperation().findOne("artifactElement", 
 						new Query(Criteria.whereId().is(moduleGroup.getId())), ArtifactElement.class);
 			    if(artifactElement != null) {
 			    	moduleGroup.setDescription(artifactElement.getDescription());
 			    	moduleGroup.setHelpText(artifactElement.getHelpText());
 			    	moduleGroup.setSystem(artifactElement.isSystem());
 			    	moduleGroup.setLicenseId(artifactElement.getLicenseId());
 			    	moduleGroup.setCreationDate(artifactElement.getCreationDate());
 			    } 
 			}
 			response.setStatus(200);
 			return  moduleGroup;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getModule", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_GROUP_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the module given by the parameter
 	 * @param id
 	 * @param feature
 	 * @return
 	 */
 	@ApiOperation(value = " Updates feature based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_MODULES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, 
     		consumes = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updatemodule(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of feature that needs to be update") @PathVariable(REST_API_PATH_PARAM_ID) String id, 
 			@ApiParam(name = "feature" , value = "Feature that needs to be update") @RequestBody ArtifactGroup feature) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updatemodule : Entry");
 	        LOGGER.debug("ComponentService.updatemodule" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id , "customer" + getCustomerNameById(feature.getCustomerIds().get(0)));
 	    }
 		try {
 			if (id.equals(feature.getId())) {
 				response.setStatus(200);
 				saveModuleGroup(feature);
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.updatemodule : Exit");
 			    }
 			}
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updatemodule", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the module by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Deletes feature based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_MODULES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteModules(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of feature that needs to be delete") @PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules : Entry");
 	        LOGGER.debug("ComponentService.deleteModules" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		deleteAttifact(id, response);
 	}
 	
 	private void deleteAttifact(String id, HttpServletResponse response) {
 		try {
 			Query query = new Query(Criteria.whereId().is(id));
 			ArtifactGroupDAO artifactGroupDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 					query, ArtifactGroupDAO.class);
 			if(artifactGroupDAO != null) {
 				DbService.getMongoOperation().remove(ARTIFACT_GROUP_COLLECTION_NAME, query, ArtifactGroupDAO.class);
 				List<String> versionIds = artifactGroupDAO.getVersionIds();
 				DbService.getMongoOperation().remove(ARTIFACT_INFO_COLLECTION_NAME, new Query(Criteria.whereId().in(versionIds.toArray())), ArtifactInfo.class);
 			} else {
 				ArtifactInfo artifactInfo = DbService.getMongoOperation().findOne(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 				if(artifactInfo != null) {
 					ArtifactInfo info = DbService.getMongoOperation().findOne(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 					artifactGroupDAO = DbService.getMongoOperation().findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 							new Query(Criteria.whereId().is(info.getArtifactGroupId())), ArtifactGroupDAO.class);
 					List<String> versionIds = artifactGroupDAO.getVersionIds();
 					versionIds.remove(id);
 					artifactGroupDAO.setVersionIds(versionIds);
 					DbService.getMongoOperation().save(ARTIFACT_GROUP_COLLECTION_NAME, artifactGroupDAO);
 					DbService.getMongoOperation().remove(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 				}
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.deleteAttifact " , "query=" + query.getQueryObject().toString());
 		    }
 		} catch (Exception e) {
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.deleteAttifact", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteModules : Exit");
 	    }
 	}
 	
 	/**
 	 * Returns the artifactInfo
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives feature version based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"),@ApiError(code=204, reason = "Artifact not found")})
     @RequestMapping(value= REST_API_ARTIFACTINFO, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody ArtifactInfo getArtifactInfo(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of feature that needs to be retrive") @QueryParam(REST_QUERY_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getArtifactInfo : Entry");
 	        LOGGER.debug("ComponentService.getArtifactInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "id=" + id);
 	    }
 		try {
 			Query query = new Query(Criteria.whereId().is(id));
 			ArtifactInfo info = DbService.getMongoOperation().findOne(ARTIFACT_INFO_COLLECTION_NAME, query, ArtifactInfo.class);
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.getArtifactInfo : Exit");
 			}
 			if(info == null) {
 				response.setStatus(204);
 				return info;
 			}
 			response.setStatus(200);
 			return  info;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getArtifactInfo", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ARTIFACT_INFO_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Returns the list of pilots
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives pilots ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"),@ApiError(code=204, reason = "Pilots not found")})
     @RequestMapping(value= REST_API_PILOTS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<ApplicationInfo> findPilots(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "customerId" , value = "Customerid to retrive") @QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
 			@ApiParam(name = "techId" , value = "Customerid to retrive") @QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findPilots : Entry");
 	        LOGGER.debug("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId") , "customer" + getCustomerNameById(customerId) , "techId=" + techId);
 	    }
 	    List<ApplicationInfo> applicationInfos = new ArrayList<ApplicationInfo>();
 		try {
 		    List<ApplicationInfoDAO> appInfos = new ArrayList<ApplicationInfoDAO>();
 			Query query = new Query();
 			if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
 			query.addCriteria(Criteria.where(REST_QUERY_ISPILOT).is(true));
 			Converter<ApplicationInfoDAO, ApplicationInfo> pilotConverter = 
             		(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
 			if (StringUtils.isNotEmpty(techId)) {
                 query.addCriteria(Criteria.where(TECHINFO_VERSION).is(techId));
 			}
             appInfos = DbService.getMongoOperation().find(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
             for (ApplicationInfoDAO applicationInfoDAO : appInfos) {
 	            ApplicationInfo applicationInfo = pilotConverter.convertDAOToObject(applicationInfoDAO, DbService.getMongoOperation());
 	            applicationInfos.add(applicationInfo);
 			}
             
 			if(CollectionUtils.isEmpty(applicationInfos)) {
 				if (isDebugEnabled) {
 			        LOGGER.info("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId") , "status=\"Not Found\"");
 			    }
 				response.setStatus(204);
 				return applicationInfos;
 			}
 			if (isDebugEnabled) {
 		        LOGGER.info("ComponentService.findPilots" , "query=" + query.getQueryObject().toString());
 		    }
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findPilots : Exit");
 		    }
 			response.setStatus(200);
 			return applicationInfos;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findPilots", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPLICATION_INFO_COLLECTION_NAME);
 		}
 	}
 	
 	/**
      * Creates the list of pilots
      * @param projectInfos
      * @return 
      * @throws PhrescoException 
      */
 	@ApiOperation(value = " Creates new pilots ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Pilot creation failed")})
     @RequestMapping(value= REST_API_PILOTS, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
     public @ResponseBody void createPilots(HttpServletResponse response, MultipartHttpServletRequest request, 
     		@RequestPart(value = "pilot", required = false) ByteArrayResource pilotFile, 
     		@RequestParam("pilotInfo") byte[] pilotData) throws PhrescoException {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createPilots : Entry");
             LOGGER.debug("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
         }
         saveOrUpdatePilot(pilotFile, pilotData);
     }
 
 	private void saveOrUpdatePilot(ByteArrayResource pilotFile, byte[] pilotData)
 			throws PhrescoException {
 		ApplicationInfo applicationInfo = new Gson().fromJson(new String(pilotData), ApplicationInfo.class);
         if(pilotFile != null) {
         	boolean saveArtifactFile = saveArtifactFile(applicationInfo.getPilotContent(), pilotFile.getByteArray());
         	if(!saveArtifactFile) {
         		throw new PhrescoException("Unable to create pilot...");
         	}
         }
         saveApplicationInfo(applicationInfo);
 	}
     
 	private void createOrUpdatePilots(MultiPart pilotInfo, HttpServletResponse response) throws PhrescoException {
 		ApplicationInfo applicationInfo = null;
         BodyPartEntity bodyPartEntity = null;
         File pilotFile = null;
         
         List<BodyPart> bodyParts = pilotInfo.getBodyParts();
         if(CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_VALUE)) {
                     applicationInfo = bodyPart.getEntityAs(ApplicationInfo.class);
                 } else {
                     bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                 }
             }
         }
         
         if(bodyPartEntity != null) {
         	 pilotFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null, 
              		applicationInfo.getPilotContent().getPackaging(), applicationInfo.getName());
              boolean uploadBinary = uploadBinary(applicationInfo.getPilotContent(), pilotFile);
              if(uploadBinary) {
              	saveApplicationInfo(applicationInfo);
              } 
              FileUtil.delete(pilotFile);
         }
        
         
         if(bodyPartEntity == null && applicationInfo != null) {
         	saveApplicationInfo(applicationInfo);
         }
         response.setStatus(200);
 	}
 	
 	private void saveApplicationInfo(ApplicationInfo applicationInfo) throws PhrescoException {
 		if(!validate(applicationInfo)) {
 			return;
 		}
 		Converter<ApplicationInfoDAO, ApplicationInfo> appConverter = 
 		(Converter<ApplicationInfoDAO, ApplicationInfo>) ConvertersFactory.getConverter(ApplicationInfoDAO.class);
 		ApplicationInfoDAO applicationInfoDAO = appConverter.convertObjectToDAO(applicationInfo);
 		DbService.getMongoOperation().save(APPLICATION_INFO_COLLECTION_NAME, applicationInfoDAO);
 		ArtifactGroup pilotContent = applicationInfo.getPilotContent();
 		saveModuleGroup(pilotContent);
 	}
 	
 	/**
 	 * Updates the list of pilots
 	 * @param projectInfos
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Update pilots ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Pilot update failed")})
     @RequestMapping(value= REST_API_PILOTS, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updatePilots(HttpServletResponse response, MultipartHttpServletRequest request, 
     		@RequestPart(value = "pilot", required = false) ByteArrayResource pilotFile, 
     		@RequestParam("pilotInfo") byte[] pilotData) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updatePilots : Entry");
 	        LOGGER.debug("ComponentService.findPilots" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 	    saveOrUpdatePilot(pilotFile, pilotData);
 	}
 	
 	/**
 	 * Deletes the list of pilots
 	 * @param pilots
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Deletes list of pilots ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_PILOTS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deletePilots(HttpServletResponse response, 
 			@ApiParam(name = "pilots" ,	value = "List of pilots to delete") @RequestBody List<ApplicationInfo> pilots) 
 			throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilots : Entry");
 	    }
 		response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilots" , "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 	    }
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the pilot by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = " Retrive pilot based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Pilot not found")})
     @RequestMapping(value= REST_API_PILOTS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody ApplicationInfo getPilot(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of the pilot that needs to be retrive")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getPilot : Entry ");
 	        LOGGER.debug("ComponentService.getPilot" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			ApplicationInfo appInfo = DbService.getMongoOperation().findOne(APPLICATION_INFO_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), ApplicationInfo.class);
 			
 			if (appInfo == null) {
 				response.setStatus(204);
 				return appInfo;
 			}
 			response.setStatus(200);
 			return appInfo;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getPilot", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, APPLICATION_INFO_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the pilot given by the parameter
 	 * @param id
 	 * @param pilot
 	 * @return
 	 */
 	@ApiOperation(value = " Update pilot based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_PILOTS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updatePilot(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of the pilot that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "pilot" , value = "Pilot that needs to be update")@RequestBody ApplicationInfo pilot) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updatePilot : Entry"); 
 	        LOGGER.debug("ComponentService.updatePilot" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			if (id.equals(pilot.getId())) {
 				DbService.getMongoOperation().save(APPLICATION_INFO_COLLECTION_NAME, pilot);
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.updatePilot : Exit"); 
 			    }
 				response.setStatus(200);
 			}
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updatePilot", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the pilot by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Deletes pilot based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_PILOTS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deletePilot(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of the pilot that needs to be delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilot : Entry ");
 	        LOGGER.debug("ComponentService.deletePilot" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 	    Query query = new Query(Criteria.whereId().is(id));
 	    ApplicationInfoDAO applicationInfoDAO = DbService.getMongoOperation().findOne(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
 	    if(applicationInfoDAO != null) {
 	    	deleteAttifact(applicationInfoDAO.getArtifactGroupId(), response);
 	    	DbService.getMongoOperation().remove(APPLICATION_INFO_COLLECTION_NAME, query, ApplicationInfoDAO.class);
 	    } else {
 	    	if (isDebugEnabled) {
 		        LOGGER.info("ComponentService.deletePilot", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 		        		"user=" + request.getParameter("userId"), "status=\"Not Found\"");
 		        LOGGER.info("ComponentService.deletePilot", "query=" + query.getQueryObject().toString());
 		    }
 	    }
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deletePilot : Exit ");
 	    }
 	}
 
 	/**
 	 * Returns the list of webservices
 	 * @return
 	 */
 	@ApiOperation(value = " Retrives webservices ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Webservices not found")})
     @RequestMapping(value= REST_API_WEBSERVICES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<WebService> findWebServices(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "customerId" , value = "Customerid to retrive") @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findWebServices : Entry ");
 	        LOGGER.debug("ComponentService.findWebServices" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customer" + getCustomerNameById(customerId));
 	    }
 		try {
 			List<WebService> webServiceList = DbService.getMongoOperation().getCollection(WEBSERVICES_COLLECTION_NAME, WebService.class);
 			if(CollectionUtils.isEmpty(webServiceList)) {
 				if (isDebugEnabled) {
 			        LOGGER.debug("ComponentService.findWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 			        		"user=" + request.getParameter("userId"), "status=\"Not Found\"");
 			    }
 				response.setStatus(204);
 				return  webServiceList;
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findWebServices : Exit ");
 		    }
 			response.setStatus(204);
 			return  webServiceList;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of webservices
 	 * @param webServices
 	 * @return 
 	 */
 	@ApiOperation(value = " Creates list of webservices ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create webservice")})
     @RequestMapping(value= REST_API_WEBSERVICES, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody Response createWebServices(@Context HttpServletRequest request, HttpServletResponse response, 
 			@ApiParam(name = "webServices" , value = "List of webservices") @RequestBody List<WebService> webServices) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createWebServices : Entry");
 	        LOGGER.debug("ComponentService.createWebServices" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + webServices.get(0).getId());
 	    }
 		try {
 			for (WebService webService : webServices) {
 				if(validate(webService)) {
 					DbService.getMongoOperation().save(WEBSERVICES_COLLECTION_NAME , webService);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.createWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.createWebServices : Exit");
 	    }
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Updates the list of webservices
 	 * @param webServices
 	 * @return
 	 */
 	@ApiOperation(value = " Updates list of webservices ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to update webservice")})
     @RequestMapping(value= REST_API_WEBSERVICES, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateWebServices(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "webServices" , value = "List of webservices") @RequestBody List<WebService> webServices) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebServices : Entry");
 	        LOGGER.debug("ComponentService.updateWebServices" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + webServices.get(0).getId());
 	    }
 		try {
 			for (WebService webService : webServices) {
 				WebService webServiceInfo = DbService.getMongoOperation().findOne(WEBSERVICES_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(webService.getId())), WebService.class);
 				if (webServiceInfo != null) {
 					DbService.getMongoOperation().save(WEBSERVICES_COLLECTION_NAME , webService);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebServices : Exit");
 	    }
 	}
 	
 	/**
 	 * Deletes the list of webservices
 	 * @param webServices
 	 * @throws PhrescoException 
 	 */
 	@ApiOperation(value = " Deletes list of webservices ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_WEBSERVICES, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteWebServices(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "webServices" , value = "List of webservices") @RequestBody List<WebService> webServices) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteWebServices : Entry");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		if(isDebugEnabled) {
 			LOGGER.error("ComponentService.deleteWebServices", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
     		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + phrescoException.getLocalizedMessage() + "\"");
 		}
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the database by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@ApiOperation(value = " Retrive webservice based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failure to find webservice"), @ApiError(code=500, reason = "Webservice not found")})
     @RequestMapping(value= REST_API_WEBSERVICES + REST_API_PATH_ID, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody WebService getWebService(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of webservice that needs to be retrive") @PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.getWebService : Entry");
 	        LOGGER.debug("ComponentService.getWebService" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			WebService webService = DbService.getMongoOperation().findOne(WEBSERVICES_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
 			if (webService == null) {
 				response.setStatus(204);
 				return webService;
 			} 
 			response.setStatus(200);
 			return webService;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the database given by the parameter
 	 * @param id
 	 * @param webService
 	 * @return
 	 */
 	@ApiOperation(value = " Updates webservice based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failure to update")})
     @RequestMapping(value= REST_API_WEBSERVICES + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE,
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateWebService(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of webservice that needs to be update") @PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "webService" , value = "Webservice that needs to be update") @RequestBody WebService webService) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebService : Entry");
 	        LOGGER.debug("ComponentService.updateWebService" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			if (id.equals(webService.getId())) {
 				DbService.getMongoOperation().save(WEBSERVICES_COLLECTION_NAME, webService);
 				response.setStatus(200);
 			} 
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.updateWebService : Exit");
 	    }
 	}
 	
 	/**
 	 * Deletes the server by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@ApiOperation(value = " Deletes webservice based on their id ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failure to update")})
     @RequestMapping(value= REST_API_WEBSERVICES + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteWebService(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = "id" , value = "Id of webservice that needs to be delete") @PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteWebService : Entry ");
 	        LOGGER.debug("ComponentService.updateWebService" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
 	    }
 		try {
 			DbService.getMongoOperation().remove(WEBSERVICES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), WebService.class);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.deleteWebService : Exit ");
 	    }
 	}
 
 	/**
      * Returns the list of downloadInfo
      * @return
      */
 	@ApiOperation(value = " Retrives downloads ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failure to retrive"), @ApiError(code=204, reason = "Downloads not found")})
     @RequestMapping(value= REST_API_DOWNLOADS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody List<DownloadInfo> findDownloadInfo(@Context HttpServletRequest request, HttpServletResponse response,
     		@ApiParam(name = REST_QUERY_CUSTOMERID , value = "Customerid to retrive") @QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
     		@ApiParam(name = REST_QUERY_TECHID , value = "Techid to retrive") @QueryParam(REST_QUERY_TECHID) String techId, 
     		@ApiParam(name = REST_QUERY_TYPE , value = "Type to retrive") @QueryParam(REST_QUERY_TYPE) String type, 
     		@ApiParam(name = REST_QUERY_PLATFORM , value = "Platform to retrive") @QueryParam(REST_QUERY_PLATFORM) String platform) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.findDownloadInfo : Entry");
             LOGGER.debug("ComponentService.findDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "customerId=" + customerId, "techId=" + techId, "type=" + type, "platform=" + platform);
         }
         List<DownloadInfo> downloads = new ArrayList<DownloadInfo>();
         Query query = new Query();
         try {
         	if(StringUtils.isNotEmpty(customerId)) {
         		List<String> customers = new ArrayList<String>();
         		customers.add(customerId);
         		if(StringUtils.isNotEmpty(techId)) {
         			CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
         					new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
         			if(CollectionUtils.isNotEmpty(customer.getApplicableTechnologies())) {
         				if(customer.getApplicableTechnologies().contains(techId)) {
         					customers.add(DEFAULT_CUSTOMER_NAME);
         				}
         			}
         		}
         		Criteria customerCriteria = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(customers.toArray());
         		query.addCriteria(customerCriteria);
         	}
         	if(StringUtils.isNotEmpty(platform)) {
         		query.addCriteria(Criteria.where(DB_COLUMN_PLATFORM).is(platform));
         	}
         	if(StringUtils.isNotEmpty(techId)) {
         		Criteria techIdCriteria = Criteria.where(APPLIES_TO_TECHIDS).in(techId);
             	query.addCriteria(techIdCriteria);
         	}
         	if(StringUtils.isNotEmpty(type)) {
         		Criteria typeCriteria = Criteria.where(CATEGORY).is(type);
         		query.addCriteria(typeCriteria);
         	}
         	if(StringUtils.isEmpty(techId) && StringUtils.isEmpty(type) && StringUtils.isEmpty(platform)) {
         		Criteria customerCri = Criteria.where(DB_COLUMN_CUSTOMERIDS).in(Arrays.asList(DEFAULT_CUSTOMER_NAME, customerId).toArray());
 				query.addCriteria(customerCri);
         	}
         	List<DownloadsDAO> downloadList = DbService.getMongoOperation().find(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
         	if(CollectionUtils.isEmpty(downloadList)) {
         		LOGGER.debug("ComponentService.findDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
     	        		"user=" + request.getParameter("userId"), "status=\"Bad Request\"", "message=" + "Not Found");
         		response.setStatus(204);
             	return  downloads;
             }
         	Converter<DownloadsDAO, DownloadInfo> downloadConverter = 
         		(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
         	for (DownloadsDAO downloadsDAO : downloadList) {
 				DownloadInfo downloadInfo = downloadConverter.convertDAOToObject(downloadsDAO, DbService.getMongoOperation());
 				downloads.add(downloadInfo);
 			}
         	LOGGER.debug("ComponentService.findDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
         	response.setStatus(200);
         	return downloads;
         } catch (Exception e) {
         	response.setStatus(00);
         	if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateWebService", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00006, DOWNLOAD_COLLECTION_NAME);
         }
     }
 
     /**
      * Creates the list of downloads
      * @param downloads
      * @return 
      * @throws PhrescoException 
      */
 	@ApiOperation(value = " Creates new downloads ")
 	@ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_DOWNLOADS, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
     public @ResponseBody void createDownloads(HttpServletResponse response, MultipartHttpServletRequest request, 
     		@RequestPart(value = "download", required = false) ByteArrayResource downloadFile, 
     		@RequestPart(value = "icon", required = false) ByteArrayResource iconFile,
     		@RequestParam("downloads") byte[] downloadData) 
 			throws PhrescoException {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createDownloads : Entry");
             LOGGER.debug("ComponentService.createDownloads" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
         }
         saveOrUpdateDownloads(downloadFile, iconFile, downloadData);
     }
 
 	private void saveOrUpdateDownloads(ByteArrayResource downloadFile,
 			ByteArrayResource iconFile, byte[] downloadData)
 			throws PhrescoException {
 		DownloadInfo downloadInfo = new Gson().fromJson(new String(downloadData), DownloadInfo.class);
         if(downloadFile != null) {
         	boolean saveArtifactFile = saveArtifactFile(downloadInfo.getArtifactGroup(), downloadFile.getByteArray());
         	if(!saveArtifactFile) {
         		throw new PhrescoException("Unable to create download... ");
         	}
         }
         if(iconFile != null) {
         	boolean saveArtifactIcon = saveArtifactIcon(downloadInfo.getArtifactGroup(), iconFile.getByteArray());
         	if(!saveArtifactIcon) {
         		throw new PhrescoException("Unable to create download icon... ");
         	}
         }
         saveDownloads(downloadInfo);
 	}
     
 	
     private void createOrUpdateDownloads(MultiPart downloadPart) throws PhrescoException {
     	DownloadInfo downloadInfo = null;
         BodyPartEntity bodyPartEntity = null;
         File downloadFile = null;
         
         List<BodyPart> bodyParts = downloadPart.getBodyParts();
         if(CollectionUtils.isNotEmpty(bodyParts)) {
             for (BodyPart bodyPart : bodyParts) {
                 if (bodyPart.getMediaType().equals(MediaType.APPLICATION_JSON_VALUE)) {
                     downloadInfo = bodyPart.getEntityAs(DownloadInfo.class);
                 } else if(bodyPart.getMediaType().equals(MediaType.APPLICATION_OCTET_STREAM_VALUE)){
                     bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
                 }
             }
         }
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createDownloads " , "id=" + downloadInfo.getId() , "customer=" + 
             		getCustomerNameById(downloadInfo.getCustomerIds().get(0)));
         }
         if(bodyPartEntity != null) {
             downloadFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null,
             		downloadInfo.getArtifactGroup().getPackaging(), downloadInfo.getName());
             boolean uploadBinary = uploadBinary(downloadInfo.getArtifactGroup(), downloadFile);
             if(uploadBinary) {
                 saveDownloads(downloadInfo);
             }
             FileUtil.delete(downloadFile);
         }
         
         if(bodyPartEntity == null && downloadInfo != null) {
         	saveDownloads(downloadInfo);
         }
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.createDownloads : Exit");
         }
 	}
 
     private void saveDownloads(DownloadInfo info) throws PhrescoException {
     	if(!validate(info)) {
     		return;
     	}
     	Converter<DownloadsDAO, DownloadInfo> downlodConverter = 
     		(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
     	DownloadsDAO downloadDAO = downlodConverter.convertObjectToDAO(info);
     	DownloadsDAO dDao = DbService.getMongoOperation().findOne(DOWNLOAD_COLLECTION_NAME,  new Query(Criteria.whereId().is(info.getId())), DownloadsDAO.class);
     	if(dDao != null) {
     		List<String> appliesToTechIdsDB = dDao.getAppliesToTechIds();
     		List<String> appliesToTechIds = info.getAppliesToTechIds();
     		for (String techId : appliesToTechIds) {
 				if(!appliesToTechIdsDB.contains(techId)) {
 					appliesToTechIdsDB.add(techId);
 				}
 			}
     		downloadDAO.setAppliesToTechIds(appliesToTechIdsDB);
     		List<String> customerIdsInDb = dDao.getCustomerIds();
     		if(!customerIdsInDb.contains(info.getCustomerIds().get(0))) {
     			customerIdsInDb.add(info.getCustomerIds().get(0));
     		}
     		downloadDAO.setCustomerIds(customerIdsInDb);
     	}
     	ArtifactGroup artifactGroup = info.getArtifactGroup();
     	saveModuleGroup(artifactGroup);
     	DbService.getMongoOperation().save(DOWNLOAD_COLLECTION_NAME, downloadDAO);
     }
     
     /**
      * Updates the list of downloadInfos
      * @param downloads
      * @return
      * @throws PhrescoException 
      */
     @ApiOperation(value = " Updates new downloads ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_DOWNLOADS, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, 
     		MediaType.APPLICATION_JSON_VALUE,"multipart/mixed"}, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
     public @ResponseBody void updateDownloadInfo(HttpServletResponse response, MultipartHttpServletRequest request, 
     		@RequestPart(value = "download", required = false) ByteArrayResource downloadFile, 
     		@RequestPart(value = "icon", required = false) ByteArrayResource iconFile,
     		@RequestParam("downloads") byte[] downloadData)
     		throws PhrescoException {
     	if (isDebugEnabled) {
             LOGGER.debug("ComponentService.updateDownloadInfo : Entry");
             LOGGER.debug("ComponentService.createDownloads" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
         }
         saveOrUpdateDownloads(downloadFile, iconFile, downloadData);
     }
 
     /**
      * Deletes the list of DownloadInfo
      * @param downloads
      * @throws PhrescoException 
      */
     @ApiOperation(value = " Deletes list of downloads ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_DOWNLOADS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
     public void deleteDownloadInfo(HttpServletResponse response, @ApiParam(name = "downloads" , 
     		value = "List of downloads") @RequestBody List<DownloadInfo> downloads) throws PhrescoException {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.deleteDownloadInfo : Entry");
         }
         response.setStatus(500);
         PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
         if (isDebugEnabled) {
 			LOGGER.error("ComponentService.deleteDownloadInfo", "status=\"Failure\"", "message=\"" + 
 					phrescoException.getLocalizedMessage() + "\"");
         }
         throw phrescoException;
     }
 
     /**
      * Get the downloadInfo by id for the given parameter
      * @param id
      * @return
      */
     @ApiOperation(value = " Retrives downloads based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Download not found")})
     @RequestMapping(value= REST_API_DOWNLOADS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody DownloadInfo getDownloadInfo(@Context HttpServletRequest request, HttpServletResponse response,
     		@ApiParam(name = REST_API_PATH_PARAM_ID , value = "Id of download that needs to retrive")
     		@PathVariable (REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.getDownloadInfo : Entry ");
             LOGGER.debug("ComponentService.getDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
         }
         DownloadInfo downloadInfo = null;
         try {
         	DownloadsDAO downloadDAO = DbService.getMongoOperation().findOne(DOWNLOAD_COLLECTION_NAME, 
                     new Query(Criteria.whereId().is(id)), DownloadsDAO.class);
         	if(downloadDAO == null) {
         		response.setStatus(204);
         		return downloadInfo;
         	}
 			Converter<DownloadsDAO, DownloadInfo> downlodConverter = 
 					(Converter<DownloadsDAO, DownloadInfo>) ConvertersFactory.getConverter(DownloadsDAO.class);
 			downloadInfo = downlodConverter.convertDAOToObject(downloadDAO, DbService.getMongoOperation());
 			response.setStatus(200);
 			return downloadInfo;
         } catch (Exception e) {
         	response.setStatus(500);
         	if(isDebugEnabled) {
 				LOGGER.error("ComponentService.getDownloadInfo", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00005, DOWNLOAD_COLLECTION_NAME);
         }
         
     }
     
     /**
      * Updates the list of downloadInfo by id
      * @param downloadInfos
      * @return
      */
     @ApiOperation(value = " Updates downloads based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_DOWNLOADS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE,
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
     public @ResponseBody void updateDownloadInfo(@Context HttpServletRequest request, HttpServletResponse response,
     		@ApiParam(name = REST_API_PATH_PARAM_ID , value = "Id of download that need to be update" )
     		@PathVariable(REST_API_PATH_PARAM_ID) String id , @ApiParam(name = "download" , value = "Id of download that need to be update" )
     		@RequestBody DownloadInfo download) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.updateDownloadInfo : Entry ");
             LOGGER.debug("ComponentService.updateDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
         }
         try {
             if (id.equals(download.getId())) {
             	DbService.getMongoOperation().save(DOWNLOAD_COLLECTION_NAME, download);
             	response.setStatus(200);
             } 
         } catch (Exception e) {
         	response.setStatus(500);
         	if(isDebugEnabled) {
 				LOGGER.error("ComponentService.updateDownloadInfo", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
             throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
         }
         LOGGER.debug("ComponentService.updateDownloadInfo : Exit ");
     }
     
     /**
      * Deletes the user by id for the given parameter
      * @param id
      * @return 
      */
     @ApiOperation(value = " Deletes downloads based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_DOWNLOADS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
     public @ResponseBody void deleteDownloadInfo(@Context HttpServletRequest request, HttpServletResponse response,
     		@ApiParam(name = REST_API_PATH_PARAM_ID , value = "Id of download that need to be delete")
     		@PathVariable(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             LOGGER.debug("ComponentService.deleteDownloadInfo : Entry");
             LOGGER.debug("ComponentService.deleteDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "id=" + id);
         }
         Query query = new Query(Criteria.whereId().is(id));
         LOGGER.debug("ComponentService.deleteDownloadInfo" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "query=" + query.getQueryObject().toString());
 	    DownloadsDAO downloadsDAO = DbService.getMongoOperation().findOne(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 	    if(downloadsDAO != null) {
 	    	deleteAttifact(downloadsDAO.getArtifactGroupId(), response);
 	    	DbService.getMongoOperation().remove(DOWNLOAD_COLLECTION_NAME, query, DownloadsDAO.class);
 	    	response.setStatus(200);
 	    }
 	    LOGGER.debug("ComponentService.deleteDownloadInfo : Exit");
     }
     
     /**
 	 * Returns the list of platforms available
 	 * @return
 	 */
     @ApiOperation(value = " Retrives platforms from db ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Platforms not found")})
     @RequestMapping(value= REST_API_PLATFORMS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<PlatformType> findPlatforms(@Context HttpServletRequest request, HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findPlatforms : Entry ");
 	        LOGGER.debug("ComponentService.findPlatforms" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"));
 	    }
 	    List<PlatformType> platformList = null;
 		try {
 			platformList = DbService.getMongoOperation().getCollection(PLATFORMS_COLLECTION_NAME, PlatformType.class);
 			if(CollectionUtils.isEmpty(platformList)) {
 				response.setStatus(204);
 				return platformList;
 			}
 			response.setStatus(200);
 			LOGGER.debug("ComponentService.findPlatforms : Exit ");
 			return platformList;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findPlatforms", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, SETTINGS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Returns the list of Reports
 	 * @return
 	 */
     @ApiOperation(value = " Retrives reports")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Reports not found")})
     @RequestMapping(value= REST_API_REPORTS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<Reports> findReports(@Context HttpServletRequest request , HttpServletResponse response,
 			@ApiParam(name = REST_QUERY_TECHID , value = "Techid to retrive") @QueryParam(REST_QUERY_TECHID) String techId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findReports : Entry ");
 	        LOGGER.debug("ComponentService.findReports" , "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
 	        		"user=" + request.getParameter("userId"), "techId=" + techId);
 	    }
 	    List<Reports> reports = new ArrayList<Reports>();
 		try {
 			if(StringUtils.isEmpty(techId)) {
 				reports = DbService.getMongoOperation().getCollection(REPORTS_COLLECTION_NAME, Reports.class);
 			} else {
 				TechnologyDAO tech = DbService.getMongoOperation().findOne(TECHNOLOGIES_COLLECTION_NAME, 
 						new Query(Criteria.whereId().is(techId)), TechnologyDAO.class);
 				if(tech != null) {
 					List<String> reportIds = tech.getReports();
 					if(CollectionUtils.isNotEmpty(reportIds)) {
 						reports = DbService.getMongoOperation().find(REPORTS_COLLECTION_NAME, 
 								new Query(Criteria.whereId().in(reportIds.toArray())), Reports.class);
 					}
 				}
 			}
 			if(CollectionUtils.isEmpty(reports)) {
 				response.setStatus(204);
 				return  reports;
 			}
 			LOGGER.debug("ComponentService.findReports : Exit ");
 			response.setStatus(200);
 			return  reports;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if(isDebugEnabled) {
 				LOGGER.error("ComponentService.findReports", "remoteAddress=" + request.getRemoteAddr() , "endpoint=" + request.getRequestURI() , 
         		"user=" + request.getParameter("userId"), "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 			}
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, REPORTS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Reports
 	 * @param reports
 	 * @return 
 	 */
     @ApiOperation(value = " Creates list of  reports")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_REPORTS, consumes =MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createReports(HttpServletResponse response, @ApiParam(name = "reports" , 
 			value = "List of reports") @RequestBody List<Reports> reports) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.createReports(List<Reports> reports)");
 	    }
 		
 		try {
 			for (Reports report : reports) {
 				if(validate(report)) {
 					DbService.getMongoOperation().save(REPORTS_COLLECTION_NAME , report);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
 	/**
 	 * Updates the list of Reports
 	 * @param reports
 	 * @return
 	 */
     @ApiOperation(value = " Updates list of  reports")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_REPORTS, consumes =MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateReports(HttpServletResponse response, @ApiParam(name = "reports" , 
 			value = "List of reports") @RequestBody List<Reports> reports) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateReports(List<Reports> reports)");
 	    }
 		try {
 			for (Reports report : reports) {
 				DbService.getMongoOperation().save(REPORTS_COLLECTION_NAME, report);
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 	}
 	
 	/**
 	 * Deletes the list of Reports
 	 * @param webServices
 	 * @throws PhrescoException 
 	 */
     @ApiOperation(value = " Deletes list of  reports")
     @ApiErrors(value = {@ApiError(code=500, reason = "Unsupportd ooperation")})
     @RequestMapping(value= REST_API_REPORTS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteReports(HttpServletResponse response, @ApiParam(name = "reports" , 
 			value = "List of reports") @RequestBody List<Reports> reports) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteReports(List<Reports> reports)");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error(exceptionString + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the Reports by id for the given parameter
 	 * @param id
 	 * @return
 	 */
     @ApiOperation(value = " Retrives reports based on their id")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Report not found")})
     @RequestMapping(value= REST_API_REPORTS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody Reports getReports(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , 
 			value = "Id of the report that needs to be retrive")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.getReports(String id)" + id);
 	    }
 	    Reports reports = null;
 		try {
 			reports = DbService.getMongoOperation().findOne(REPORTS_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), Reports.class);
 			if(reports == null) {
 				response.setStatus(204);
 				return reports;
 			}
 			response.setStatus(200);
 			return reports;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the reports given by the parameter
 	 * @param id
 	 * @param report
 	 * @return
 	 */
     @ApiOperation(value = " Updates reports based on their id")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_REPORTS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateReports(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , 
 			value = "Id of the report that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id , 
 			@ApiParam(name = "report" , value = "Report that need to be update")Reports report) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateReports(String id, Reports reports)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().save(REPORTS_COLLECTION_NAME, report);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the Reports by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
     @ApiOperation(value = " Deletes reports based on their id")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_REPORTS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody Response deleteReports(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , 
 			value = "Id of the report that needs to be delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteReportse(String id)" + id);
 	    }
 		try {
 			DbService.getMongoOperation().remove(REPORTS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Reports.class);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of Properties
 	 * @return
 	 */
     @ApiOperation(value = " Retrives property from db")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Properties not found")})
     @RequestMapping(value= REST_API_PROPERTY, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<Property> findProperties(HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findProperties()");
 	    }
 	    List<Property> properties = null;
 		try {
 			properties = DbService.getMongoOperation().getCollection(PROPERTIES_COLLECTION_NAME, Property.class);
 			if(CollectionUtils.isEmpty(properties)) {
 				response.setStatus(204);
 				return properties;
 			}
 			response.setStatus(200);
 			return properties;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Properties
 	 * @param properties
 	 * @return 
 	 */
     @ApiOperation(value = " Create list of properties")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_PROPERTY, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createProperties(HttpServletResponse response, @ApiParam(name = "properties" , 
 			value = "List of property")@RequestBody List<Property> properties) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.createProperties(List<Property> properties)");
 	    }
 		try {
 			for (Property property : properties) {
 				if(validate(property)) {
 					DbService.getMongoOperation().save(PROPERTIES_COLLECTION_NAME , property);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
 	/**
 	 * Updates the list of Properties
 	 * @param reports
 	 * @return
 	 */
     @ApiOperation(value = " Updates list of properties")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_PROPERTY, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateProperties(HttpServletResponse response , @ApiParam(name = "properties" , 
 			value = "List of property")@RequestBody List<Property> properties) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateProperties(List<Property> properties)");
 	    }
 	   
 		try {
 			for (Property property : properties) {
 				DbService.getMongoOperation().save(PROPERTIES_COLLECTION_NAME, property);
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the list of Properties
 	 * @param properties
 	 * @throws PhrescoException 
 	 */
     @ApiOperation(value = " Deletes list of properties")
     @ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_PROPERTY, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteProperties(HttpServletResponse response,@ApiParam(name = "properties" , 
 			value = "List of property")@RequestBody List<Property> properties) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteProperties(List<Property> properties)");
 	    }
 	    response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error(exceptionString + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the Property by id for the given parameter
 	 * @param id
 	 * @return
 	 */
     @ApiOperation(value = " Retrive property based on their id")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Property not found")})
     @RequestMapping(value= REST_API_PROPERTY + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody Property getProperty(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , 
 			value = "Id of property that needs to be retrive")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.getProperty(String id)" + id);
 	    }
 	    Property property = null;
 		try {
 			property = DbService.getMongoOperation().findOne(PROPERTIES_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), Property.class);
 			if(property == null) {
 				response.setStatus(204);
 				return property;
 			}
 			response.setStatus(200);
 			return property;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, WEBSERVICES_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the property given by the parameter
 	 * @param id
 	 * @param property
 	 * @return
 	 */
     @ApiOperation(value = " Updates property based on their id")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_PROPERTY + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateProperty(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , 
 			value = "Id of property that needs to be update")@PathVariable(REST_API_PATH_PARAM_ID) String id , @ApiParam(name = "property" , 
 					value = "Property that needs to be update") @RequestBody Property property) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateProperty(String id, Property property)" + id);
 	    }
 	   	try {
 	   		DbService.getMongoOperation().save(PROPERTIES_COLLECTION_NAME, property);
 	   		response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 	}
 	
 	/**
 	 * Deletes the Property by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
     @ApiOperation(value = " Deletes property based on their id")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_PROPERTY + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody void deleteProperty(HttpServletResponse response, @ApiParam(name = REST_API_PATH_PARAM_ID , 
 			value = "Id of property that needs to be delete")@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteProperty(String id)" + id);
 	    }
 		try {
 	   		DbService.getMongoOperation().remove(PROPERTIES_COLLECTION_NAME, 
 			        new Query(Criteria.whereId().is(id)), Property.class);
 	   		response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 	}
 	
 	/**
 	 * Returns the list of Technologyoptions
 	 * @return
 	 */
     @ApiOperation(value = " Retrives technology options")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete"), @ApiError(code=204, reason = "Options not found")})
     @RequestMapping(value= REST_API_OPTIONS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<TechnologyOptions> findOptions(HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findOptions()");
 	    }
 	    List<TechnologyOptions> techOptions = null;
 		try {
 			techOptions = DbService.getMongoOperation().getCollection(OPTIONS_COLLECTION_NAME, TechnologyOptions.class);
 			if(CollectionUtils.isEmpty(techOptions)) {
 				response.setStatus(204);
 				return techOptions;
 			}
 			response.setStatus(200);
 			return techOptions;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Returns the list of Functional test frameworks
 	 * @return
 	 */
     @ApiOperation(value = " Retrives all functional frameworks ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to fetch"), @ApiError(code=204, reason = "Functional not found")})
     @RequestMapping(value= REST_API_OPTIONS_FUNCTIONAL, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<FunctionalFramework> findFunctionalTestFrameworks(HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findFunctionalTestFrameworks()");
 	    }
 	    List<FunctionalFramework> options = null;
 		try {
 			options = DbService.getMongoOperation().
 				getCollection(FUNCTIONAL_FRAMEWORK_COLLECTION_NAME, FunctionalFramework.class);
 			if(CollectionUtils.isEmpty(options)) {
 				response.setStatus(200);
 			}
 			response.setStatus(200);
 			return options;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, FUNCTIONAL_FRAMEWORK_COLLECTION_NAME);
 		}
 	}
 	
     /**
 	 * Returns the list of Functional test frameworks
 	 * @return
 	 */
     @ApiOperation(value = " Retrives functional frameworks with tech id and name")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to fetch"), @ApiError(code=204, reason = "Functional not found")})
     @RequestMapping(value= "/functionalframeworks/functional", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody FunctionalFramework findFunctionalTestFrameworks(HttpServletResponse response,
 			@ApiParam(name = REST_QUERY_TECHID , value = "Techid to retrive")@QueryParam(REST_QUERY_TECHID) String techId, 
 			@ApiParam(name = REST_API_NAME , value = "Name to retrive")@QueryParam(REST_API_NAME) String name) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findFunctionalTestFrameworks()");
 	    }
 	    FunctionalFramework functionalFramework = null;
 		try {
 			if (StringUtils.isNotEmpty(techId) && StringUtils.isNotEmpty(name)) {
 				functionalFramework = DbService.getMongoOperation().findOne(FUNCTIONAL_FRAMEWORK_COLLECTION_NAME, 
 						new Query(Criteria.where(REST_API_NAME).is(name)), FunctionalFramework.class);
 				List<FunctionalFrameworkProperties> funcFrameworkProperties = functionalFramework.getFuncFrameworkProperties();
 				if (CollectionUtils.isNotEmpty(funcFrameworkProperties)) {
 					for (FunctionalFrameworkProperties functionalFrameworkProperties : funcFrameworkProperties) {
 						if (functionalFrameworkProperties.getTechId().equals(techId)) {
 							functionalFramework.setFuncFrameworkProperties(Collections.singletonList(functionalFrameworkProperties));
 							break;
 						}
 					}
 				}
 			}
 			response.setStatus(200);
 			return functionalFramework;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, FUNCTIONAL_FRAMEWORK_COLLECTION_NAME);
 		}
 	}
     
 	/**
      * Returns the list of Technologyoptions
      * @return
      */
     @ApiOperation(value = " Retrives customer options ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete"), @ApiError(code=204, reason = "Customeroptions not found")})
     @RequestMapping(value= REST_API_OPTIONS_CUSTOMER, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
     public @ResponseBody List<TechnologyOptions> findCustomerOptions(HttpServletResponse response) {
         if (isDebugEnabled) {
             LOGGER.debug("Entered into ComponentService.findCustomerOptions()");
         }
         List<TechnologyOptions> techOptions = null;
         try {
         	techOptions = DbService.getMongoOperation().getCollection(CUSTOMER_OPTIONS_COLLECTION_NAME, TechnologyOptions.class);
             if(CollectionUtils.isEmpty(techOptions)) {
             	response.setStatus(204);
             	return techOptions;
             }
             response.setStatus(200);
         	return techOptions;
         } catch (Exception e) {
         	response.setStatus(500);
             throw new PhrescoWebServiceException(e, EX_PHEX00005, CUSTOMER_OPTIONS_COLLECTION_NAME);
         }
     }
 	
 	/**
 	 * Returns the list of Licenses
 	 * @return
 	 */
     @ApiOperation(value = " Retrives licenses options ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete"), @ApiError(code=204, reason = "Licenses not found")})
     @RequestMapping(value= REST_API_LICENSE, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<License> findLicenses(HttpServletResponse response) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findLicenses()");
 	    }
 	    List<License> licenses = null;
 		try {
 			licenses = DbService.getMongoOperation().getCollection(LICENSE_COLLECTION_NAME, License.class);
 			if(CollectionUtils.isEmpty(licenses)) {
 				response.setStatus(204);
 				return licenses;
 			}
 			response.setStatus(200);
 			return licenses;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Licenses
 	 * @param licenses
 	 * @return 
 	 */
     @ApiOperation(value = " Creates list of licenses ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_LICENSE, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createLicenses(HttpServletResponse response, @ApiParam(name = "licenses" , value = "List of license")
 			@RequestBody List<License> licenses) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.createLicenses(List<License> licenses)");
 	    }
 		try {
 			for (License license : licenses) {
 				if(validate(license)) {
 					DbService.getMongoOperation().save(LICENSE_COLLECTION_NAME , license);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
 	/**
 	 * Returns the list of Technology Options
 	 * @return
 	 */
     @ApiOperation(value = " Retrive technology groups ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "TechGroup not found")})
     @RequestMapping(value= REST_API_TECHGROUPS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<TechnologyGroup> findTechnologyGroups(HttpServletResponse response,
 			@ApiParam(name = REST_QUERY_CUSTOMERID , value = "Customerid to retrive")@QueryParam(REST_QUERY_CUSTOMERID) String customerId, 
 			@ApiParam(name = REST_QUERY_APPTYPEID , value = "Apptypeid to retrive")@QueryParam(REST_QUERY_APPTYPEID) String appTypeId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.findTechnologyGroups()");
 	    }
 	    List<TechnologyGroup> technologyGroups = new ArrayList<TechnologyGroup>();
 		try {
 			if(StringUtils.isNotEmpty(customerId) && StringUtils.isNotEmpty(appTypeId)) {
 				technologyGroups = getTechGroupByCustomer(customerId, appTypeId);
 			}
 			if(CollectionUtils.isEmpty(technologyGroups)) {
 				response.setStatus(204);
 				return technologyGroups;
 			}
 			response.setStatus(200);
 			return technologyGroups;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECH_GROUP_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Creates the list of Technology Groups
 	 * @param techGroups
 	 * @return 
 	 */
     @ApiOperation(value = " Create list of technology groups ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to create")})
     @RequestMapping(value= REST_API_TECHGROUPS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
 	public @ResponseBody void createTechnologyGroups(HttpServletResponse response , 
 			@ApiParam(name = "techGroups" , value = "List of technology group")	@RequestBody List<TechnologyGroup> techGroups) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.createTechnologyGroups(List<TechnologyGroup> techGroups)");
 	    }
 		try {
 			for (TechnologyGroup technologyGroup : techGroups) {
 				if(validate(technologyGroup)) {
 					DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME , technologyGroup);
 					
 					ApplicationTypeDAO type = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 							new Query(Criteria.whereId().is(technologyGroup.getAppTypeId())), ApplicationTypeDAO.class);
 					List<String> techGroupIds = type.getTechGroupIds();
 					techGroupIds.add(technologyGroup.getId());
 					type.setTechGroupIds(techGroupIds);
 					
 					DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , type);
 				}
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 	}
 	
 	/**
 	 * Updates the list of TechnologyGroups
 	 * @param techGroups
 	 * @return
 	 */
     @ApiOperation(value = " Updates list of technology groups ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_TECHGROUPS, consumes = MediaType.APPLICATION_JSON_VALUE, 
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody Response updateTechnologyGroups(HttpServletResponse response, 
 			@ApiParam(name = "techGroups" , value = "List of technology group")	@RequestBody List<TechnologyGroup> techGroups) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateTechnologyGroups(List<TechnologyGroup> techGroups)");
 	    }
 		
 		try {
 			for (TechnologyGroup techGroup : techGroups) {
 				DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, techGroup);
 			}
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(techGroups).build();
 	}
 	
 	/**
 	 * Deletes the list of TechnologyGroups
 	 * @param properties
 	 * @throws PhrescoException 
 	 */
     @ApiOperation(value = " Deletes list of technology groups ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Unsupported operation")})
     @RequestMapping(value= REST_API_TECHGROUPS, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public void deleteTechnologyGroups(HttpServletResponse response, 
 			@ApiParam(name = "techGroups" , value = "List of technology group") @RequestBody List<TechnologyGroup> techGroups) 
     		throws PhrescoException {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteTechnologyGroups(List<TechnologyGroup> techGroups)");
 	    }
 		response.setStatus(500);
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		LOGGER.error(exceptionString + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 	
 	/**
 	 * Get the TechnologyGroup by id for the given parameter
 	 * @param id
 	 * @return
 	 */
     @ApiOperation(value = " Retrives technology group based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Techgroup operation")})
     @RequestMapping(value= REST_API_TECHGROUPS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody TechnologyGroup getTechnologyGroup(HttpServletResponse response, 
 			@ApiParam(name = "id" , value = "Id of techgroup that needs to retrive") @PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.getTechnologyGroup(String id)" + id);
 	    }
 	    TechnologyGroup techGroup = null;
 		try {
 			techGroup = DbService.getMongoOperation().findOne(TECH_GROUP_COLLECTION_NAME, 
 					new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 			if(techGroup == null) {
 				response.setStatus(204);
 				return techGroup;
 			}
 			response.setStatus(200);
 			return techGroup;
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, TECH_GROUP_COLLECTION_NAME);
 		}
 	}
 	
 	/**
 	 * Updates the TechnologyGroup given by the parameter
 	 * @param id
 	 * @param techGroup
 	 * @return
 	 */
     @ApiOperation(value = " Updates technology group based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to update")})
     @RequestMapping(value= REST_API_TECHGROUPS + REST_API_PATH_ID, consumes = MediaType.APPLICATION_JSON_VALUE,
     		produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.PUT)
 	public @ResponseBody void updateTechnologyGroup(HttpServletResponse response, 
 			@ApiParam(name = "id" , value = "Id of techgroup that needs to retrive") @PathVariable(REST_API_PATH_PARAM_ID) String id, 
 			@ApiParam(name = "techGroup" , value = "TechGroup that needs to update") @RequestBody TechnologyGroup techGroup) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.updateTechnologyGroup(String id, TechnologyGroup techGroup)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().save(TECH_GROUP_COLLECTION_NAME, techGroup);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 	}
 	
 	/**
 	 * Deletes the TechnologyGroup by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
     @ApiOperation(value = " Deletes technology group based on their id ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to delete")})
     @RequestMapping(value= REST_API_TECHGROUPS + REST_API_PATH_ID, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.DELETE)
 	public @ResponseBody Response deleteTechnologyGroup(HttpServletResponse response, 
 			@ApiParam(name = "id" , value = "Id of techgroup that needs to delete")	@PathVariable(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("Entered into ComponentService.deleteTechnologyGroup((String id)" + id);
 	    }
 		
 		try {
 			DbService.getMongoOperation().remove(TECH_GROUP_COLLECTION_NAME, new Query(Criteria.whereId().is(id)), TechnologyGroup.class);
 			
 			ApplicationTypeDAO type = DbService.getMongoOperation().findOne(APPTYPES_COLLECTION_NAME, 
 					new Query(Criteria.where(TECH_GROUP_ID).is(id)), ApplicationTypeDAO.class);
 			List<String> techGroupIds = type.getTechGroupIds();
 			techGroupIds.remove(id);
 			type.setTechGroupIds(techGroupIds);
 			
 			DbService.getMongoOperation().save(APPTYPES_COLLECTION_NAME , type);
 			response.setStatus(200);
 		} catch (Exception e) {
 			response.setStatus(500);
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the list of Prebuilt Projects
 	 * @return
 	 */
     @ApiOperation(value = " Retrives prebuilt projects ")
     @ApiErrors(value = {@ApiError(code=500, reason = "Failed to retrive"), @ApiError(code=204, reason = "Prebuilt projects not found")})
     @RequestMapping(value= REST_API_PREBUILT, produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
 	public @ResponseBody List<ProjectInfo> findPreBuiltProjects(@Context HttpServletRequest request, HttpServletResponse response,
 			@ApiParam(name = REST_QUERY_CUSTOMERID , value = "Customerid to retrive") @QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
 	    if (isDebugEnabled) {
 	        LOGGER.debug("ComponentService.findPreBuiltProjects : Entry ");
 	        LOGGER.debug("ComponentService.findPreBuiltProjects ", "remoteAddress=" + request.getRemoteAddr() , "customer" + getCustomerNameById(customerId),
 	        		"endpoint=" + request.getRequestURI() , "user=" + request.getParameter("userId"), "customer=" + getCustomerNameById(customerId));
 	    }
 	    List<ProjectInfo> projectInfos = new ArrayList<ProjectInfo>();
 		try {
 			List<ProjectInfoDAO> projectDAOs = DbService.getMongoOperation().find(PROJECTINFO_COLLECTION_NAME, 
 					new Query(Criteria.where(DB_COLUMN_PREBUILT).is(true)), ProjectInfoDAO.class);
 			Converter<ProjectInfoDAO, ProjectInfo> converter = (Converter<ProjectInfoDAO, ProjectInfo>) 
 				ConvertersFactory.getFrameworkConverter(ProjectInfoDAO.class);
 			for (ProjectInfoDAO projectInfoDAO : projectDAOs) {
 				ProjectInfo pInfo = converter.convertDAOToObject(projectInfoDAO, DbService.getMongoOperation());
 				if(isApplicable(customerId, pInfo)) {
 					projectInfos.add(pInfo);
 				}
 			}
 			if (isDebugEnabled) {
 		        LOGGER.debug("ComponentService.findPreBuiltProjects : Exit ");
 		    }
 			if(CollectionUtils.isEmpty(projectInfos)) {
 				response.setStatus(204);
 				return projectInfos;
 			}
 			response.setStatus(200);
 			return projectInfos;
 		} catch (Exception e) {
 			response.setStatus(500);
 			if (isDebugEnabled) {
 		        LOGGER.error("ComponentService.findPreBuiltProjects", "status=\"Failure\"", "message=\"" + e.getLocalizedMessage() + "\"");
 		    }
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, OPTIONS_COLLECTION_NAME);
 		}
 		
 	}
 
 	private boolean isApplicable(String customerId, ProjectInfo pInfo) {
 		CustomerDAO customer = DbService.getMongoOperation().findOne(CUSTOMERS_COLLECTION_NAME, 
 				new Query(Criteria.whereId().is(customerId)), CustomerDAO.class);
 		if(customer == null) {
 			return false;
 		}
 		List<String> techs = customer.getApplicableTechnologies();
 		List<ApplicationInfo> appInfos = pInfo.getAppInfos();
 		if(CollectionUtils.isEmpty(appInfos)) {
 			return false;
 		}
 		for (ApplicationInfo applicationInfo : appInfos) {
 			if(!techs.contains(applicationInfo.getTechInfo().getId())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 }
