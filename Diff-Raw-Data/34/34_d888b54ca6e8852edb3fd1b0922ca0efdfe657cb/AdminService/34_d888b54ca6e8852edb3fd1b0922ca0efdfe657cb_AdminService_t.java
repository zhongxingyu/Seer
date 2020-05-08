 /*
  * ###
  * Phresco Service Implemenation
  * 
  * Copyright (C) 1999 - 2012 Photon Infotech Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ###
  */
 package com.photon.phresco.service.rest.api;
 
 import java.io.File;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.log4j.Logger;
 import org.springframework.data.document.mongodb.query.Criteria;
 import org.springframework.data.document.mongodb.query.Query;
 import org.springframework.stereotype.Component;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ArtifactGroup;
 import com.photon.phresco.commons.model.Customer;
 import com.photon.phresco.commons.model.DownloadInfo;
 import com.photon.phresco.commons.model.Permission;
 import com.photon.phresco.commons.model.Property;
 import com.photon.phresco.commons.model.Role;
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.VideoInfo;
 import com.photon.phresco.commons.model.VideoType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.exception.PhrescoWebServiceException;
 import com.photon.phresco.service.api.Converter;
 import com.photon.phresco.service.api.DbService;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.client.impl.ClientHelper;
 import com.photon.phresco.service.converters.ConvertersFactory;
 import com.photon.phresco.service.dao.ApplicationInfoDAO;
 import com.photon.phresco.service.dao.ArtifactGroupDAO;
 import com.photon.phresco.service.dao.DownloadsDAO;
 import com.photon.phresco.service.dao.VideoInfoDAO;
 import com.photon.phresco.service.dao.VideoTypeDAO;
 import com.photon.phresco.service.model.ArtifactInfo;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.ServiceConstants;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.multipart.BodyPart;
 import com.sun.jersey.multipart.BodyPartEntity;
 import com.sun.jersey.multipart.MultiPart;
 import com.sun.jersey.multipart.MultiPartMediaTypes;
 
 @Component
 @Path(ServiceConstants.REST_API_ADMIN)
 public class AdminService extends DbService {
 	
 	private static final Logger S_LOGGER = Logger.getLogger(AdminService.class);
 	private static Boolean isDebugEnabled = S_LOGGER.isDebugEnabled();
 	
     public AdminService() throws PhrescoException {
     	super();
     }
     
     /**
      * Returns the list of customers
      * @return
      */
     @GET
     @Path (REST_API_CUSTOMERS)
     @Produces (MediaType.APPLICATION_JSON)
     public Response findCustomer() {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.findCustomer()");
         }
     	try {
     		List<Customer> customers = mongoOperation.getCollection(CUSTOMERDAO_COLLECTION_NAME, Customer.class);
     		if (CollectionUtils.isNotEmpty(customers)) {
     		    return Response.status(Response.Status.OK).entity(customers).build();
     		}
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00005, CUSTOMERS_COLLECTION_NAME);
 		}
     	return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
     }
 
     /**
      * Creates the list of customers
      * @param customer
      * @return 
      */
     @POST
     @Consumes (MediaType.APPLICATION_JSON)
     @Path (REST_API_CUSTOMERS)
     public Response createCustomer(List<Customer> customers) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.createCustomer(List<Customer> customer)");
         }
         
     	try {
 	        mongoOperation.insertList(CUSTOMERDAO_COLLECTION_NAME, customers);
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
     	
     	return Response.status(Response.Status.OK).build();
     }
 
     /**
      * Updates the list of customers
      * @param customers
      * @return
      */
     @PUT
     @Consumes (MediaType.APPLICATION_JSON)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_CUSTOMERS)
     public Response updateCustomer(List<Customer> customers) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.updateCustomer(List<Customer> customers)");
         }
     	
     	try {
     		for (Customer customer : customers) {
         		Customer updateCustomers = mongoOperation.findOne(CUSTOMERS_COLLECTION_NAME , 
         		        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(customer.getId())), Customer.class);
         		if (updateCustomers != null) {
         			mongoOperation.save(CUSTOMERS_COLLECTION_NAME, customer);
         		}
         	}
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
     	
     	return Response.status(Response.Status.OK).entity(customers).build();
     }
 
     /**
      * Deletes the list of customers
      * @param deleteCustomers
      * @throws PhrescoException 
      */
     @DELETE
     @Path (REST_API_CUSTOMERS)
     public void deleteCustomer(List<Customer> deleteCustomers) throws PhrescoException {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.deleteCustomer(List<Customer> deleteCustomers)");
         }
         
     	PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
     	S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
     	throw phrescoException;
     }
 
     /**
      * Get the customer by id for the given parameter
      * @param id
      * @return
      */
     @GET
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_CUSTOMERS + REST_API_PATH_ID)
     public Response getCustomer(@PathParam(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.getCustomer(String id)" + id);
         }
     	
     	try {
     		Customer customer = mongoOperation.findOne(CUSTOMERS_COLLECTION_NAME, 
     		        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Customer.class);
     		if (customer != null) {
     			return Response.status(Response.Status.OK).entity(customer).build();
     		}
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00005, CUSTOMERS_COLLECTION_NAME);
     	}
     	
     	return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
     }
     
     /**
      * Updates the list of customers by Id
      * @param updateCustomers
      * @return
      */
     @PUT
     @Consumes (MediaType.APPLICATION_JSON)
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_CUSTOMERS + REST_API_PATH_ID)
     public Response updateCustomer(@PathParam(REST_API_PATH_PARAM_ID) String id , Customer updateCustomers) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.updateCustomer(String id , Customer updateCustomers)" + id);
         }
     	
     	try {
     		if (id.equals(updateCustomers.getId())) {
         		mongoOperation.save(CUSTOMERS_COLLECTION_NAME, updateCustomers);
         		return Response.status(Response.Status.OK).entity(updateCustomers).build();
          	} 
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
     	
     	return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
     }
 
     
     /**
      * Deletes the customer by id for the given parameter
      * @param id
      * @return 
      */
     @DELETE
     @Path (REST_API_CUSTOMERS + REST_API_PATH_ID)
     public Response deleteCustomer(@PathParam(REST_API_PATH_PARAM_ID) String id) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.deleteCustomer(String id)" + id);
         }
     	
     	try {
     		mongoOperation.remove(CUSTOMERS_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Customer.class);
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
     	}
     	
     	return Response.status(Response.Status.OK).build();
     }
 
 	
 	/**
 	 * Returns the list of videos
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_VIDEOS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findVideos() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findVideos()");
 	    }
 		
 		try {
 			List<VideoInfo> videoList = mongoOperation.getCollection(VIDEOS_COLLECTION_NAME , VideoInfo.class);
 			if (videoList != null) {
 				return Response.status(Response.Status.OK).entity(videoList).build(); 
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, VIDEOS_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.OK).entity(ERROR_MSG_NOT_FOUND).build(); 
 	}
 
 	/**
 	 * Creates the list of videos
 	 * @param videos
 	 * @return 
 	 * @throws PhrescoException 
 	 */
 	@POST
 	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
 	@Path (REST_API_VIDEOS)
 	public Response createVideo(MultiPart videosinfo) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createVideo(List<VideoInfo> videos)");
 	    }
 	    return createOrUpdateVideo(videosinfo);
 	}
 
 	private Response createOrUpdateVideo(MultiPart videosinfo)
 			throws PhrescoException {
 		VideoInfo video = null;
 		BodyPartEntity bodyPartEntity = null;
 		File videoFile = null;
 		List<BodyPart> bodyParts = videosinfo.getBodyParts();
 
 		if (CollectionUtils.isNotEmpty(bodyParts)) {
 			for (BodyPart bodyPart : bodyParts) {
 				if (bodyPart.getMediaType().equals(
 						MediaType.APPLICATION_JSON_TYPE)) {
 					video = bodyPart.getEntityAs(VideoInfo.class);
 				} else {
 					bodyPartEntity = (BodyPartEntity) bodyPart.getEntity();
 				}
 			}
 		}
 
 		if (video == null) {
 			// TODO:Throw exception
 		}
 
 		if (bodyPartEntity != null) {
 			videoFile = ServerUtil.writeFileFromStream(bodyPartEntity.getInputStream(), null);
 		}
 		
 		if(bodyPartEntity != null) {
 			List<VideoType> videoTypeList = video.getVideoList();
 			VideoType videoType = videoTypeList.get(0);
 			ArtifactGroup artifactGroup = videoType.getArtifactGroup();
 			if (artifactGroup != null) {
 				boolean uploadBinary = uploadBinary(video.getVideoList().get(0).getArtifactGroup(), videoFile);
 				if (uploadBinary) {
 					saveVideos(video);
 				}
 			}
 		}
 		
 		if(bodyPartEntity == null && video != null) {
 			saveVideos(video);
 		}
 		
 		return Response.status(Response.Status.OK).entity(video).build();
 	}
 
 	private void saveVideos(VideoInfo video) throws PhrescoException {
 		Converter<VideoInfoDAO, VideoInfo> converter = 
 	            (Converter<VideoInfoDAO, VideoInfo>) ConvertersFactory.getConverter(VideoInfoDAO.class);
 		VideoInfoDAO videoDAO = converter.convertObjectToDAO(video);
 		mongoOperation.save(VIDEODAO_COLLECTION_NAME, videoDAO);
 		Converter<VideoTypeDAO, VideoType> videoTypeConverter = 
 				(Converter<VideoTypeDAO, VideoType>)ConvertersFactory.getConverter(VideoTypeDAO.class);
		List<VideoType> videoTypes = video.getVideoList();
		for(VideoType videoType : videoTypes){
 		VideoTypeDAO videoTypeDAO = videoTypeConverter.convertObjectToDAO(videoType);
 		
 		VideoTypeDAO findOne = mongoOperation.findOne(VIDEOTYPESDAO_COLLECTION_NAME, 
 				new Query(Criteria.where("name").is(videoTypeDAO.getName())), VideoTypeDAO.class);
 		if(findOne!=null){
 			videoTypeDAO.setArtifactGroupId(findOne.getArtifactGroupId());
 			videoTypeDAO.setId(findOne.getId());
 		}
 		ArtifactGroup artifactGroup = videoType.getArtifactGroup();
 		saveModuleGroup(artifactGroup);
 		mongoOperation.save(VIDEOTYPESDAO_COLLECTION_NAME, videoTypeDAO);
		}
 		mongoOperation.save(VIDEODAO_COLLECTION_NAME, videoDAO);
 	}
 
 	private boolean uploadBinary(ArtifactGroup archetypeInfo, File videoFile) throws PhrescoException {
 		
         File pomFile = ServerUtil.createPomFile(archetypeInfo);
         
         //Assuming there will be only one version for the artifactGroup
         List<com.photon.phresco.commons.model.ArtifactInfo> versions = archetypeInfo.getVersions();
         com.photon.phresco.commons.model.ArtifactInfo artifactInfo = versions.get(0);
         
 		ArtifactInfo info = new ArtifactInfo(archetypeInfo.getGroupId(), archetypeInfo.getArtifactId(), archetypeInfo.getClassifier(), 
                 archetypeInfo.getPackaging(), artifactInfo.getVersion());
 		
         info.setPomFile(pomFile);
         boolean addArtifact = true;
         
         List<String> customerIds = archetypeInfo.getCustomerIds();
         //TODO:Need to upload the content into the repository
 //        boolean addArtifact = repositoryManager.addArtifact(info, artifactFile, customerId);
         FileUtil.delete(pomFile);
         return addArtifact;
 	}
 	
 	private void saveModuleGroup(ArtifactGroup moduleGroup) throws PhrescoException {
         Converter<ArtifactGroupDAO, ArtifactGroup> converter = 
             (Converter<ArtifactGroupDAO, ArtifactGroup>) ConvertersFactory.getConverter(ArtifactGroupDAO.class);
         ArtifactGroupDAO moduleGroupDAO = converter.convertObjectToDAO(moduleGroup);
         
         List<com.photon.phresco.commons.model.ArtifactInfo> moduleGroupVersions = moduleGroup.getVersions();
         List<String> versionIds = new ArrayList<String>();
         
         ArtifactGroupDAO moduleDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 		        new Query(Criteria.where("name").is(moduleGroupDAO.getName())), ArtifactGroupDAO.class);
         
         com.photon.phresco.commons.model.ArtifactInfo newVersion = moduleGroup.getVersions().get(0);
         if(moduleDAO != null) {
         	moduleGroupDAO.setId(moduleDAO.getId());
         	versionIds.addAll(moduleDAO.getVersionIds());
         	List<com.photon.phresco.commons.model.ArtifactInfo> info = mongoOperation.find(ARTIFACT_INFO_COLLECTION_NAME, 
         			new Query(Criteria.where("artifactGroupId").is(moduleDAO.getId())), com.photon.phresco.commons.model.ArtifactInfo.class);
         	
         	List<com.photon.phresco.commons.model.ArtifactInfo> versions = new ArrayList<com.photon.phresco.commons.model.ArtifactInfo>();
         	newVersion.setArtifactGroupId(moduleDAO.getId());
         	versions.add(newVersion);
         	info.addAll(versions);
         	
         	String id = checkVersionAvailable(info, newVersion.getVersion());
         	if(id == newVersion.getId()) {
         		versionIds.add(newVersion.getId());
         	}
 			newVersion.setId(id);
     		mongoOperation.save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }  else {
         		versionIds.add(newVersion.getId());
         		newVersion.setArtifactGroupId(moduleGroupDAO.getId());
                 mongoOperation.save(ARTIFACT_INFO_COLLECTION_NAME, newVersion);
         }
         moduleGroupDAO.setVersionIds(versionIds);
         mongoOperation.save(ARTIFACT_GROUP_COLLECTION_NAME, moduleGroupDAO);
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
 	 * Updates the list of Videos
 	 * @param videos
 	 * @return
 	 * @throws PhrescoException 
 	 */
 	@PUT
 	@Consumes (MultiPartMediaTypes.MULTIPART_MIXED)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_VIDEOS)
 	public Response updateVideos(MultiPart videos) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateVideos(List<VideoInfo> videos)");
 	    }
 		return createOrUpdateVideo(videos);
 	}
 
 	/**
 	 * Deletes the list of Videos
 	 * @param videos
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path(REST_API_VIDEOS)
 	public void deleteVideos(List<VideoInfo> videos) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteVideos(List<VideoInfo> videos)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the videos by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_VIDEOS + REST_API_PATH_ID)
 	public Response getVideo(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.getVideo(String id)" + id);
 	    }
 		
 		try {
 			VideoInfo videoInfo = mongoOperation.findOne(VIDEOS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), VideoInfo.class);
 			if (videoInfo != null) {
 				return Response.status(Response.Status.OK).entity(videoInfo).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, VIDEOS_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the list of video bu Id
 	 * @param videoInfo
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_VIDEOS + REST_API_PATH_ID)
 	public Response updateVideo(@PathParam(REST_API_PATH_PARAM_ID) String id , VideoInfo videoInfo) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateVideo(String id , VideoInfo videoInfo)" + id);
 	    }
 		
 		try {
 			if (id.equals(videoInfo.getId())) {
 				mongoOperation.save(VIDEOS_COLLECTION_NAME, videoInfo);
 				return Response.status(Response.Status.OK).entity(videoInfo).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		 
 		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
 	}
 
 	
 	/**
 	 * Deletes the Video by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path(REST_API_VIDEOS + REST_API_PATH_ID)
 	public Response deleteVideo(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteVideo(String id)" + id);
 	    }
 		
 		try {
 			VideoInfoDAO videoInfoDAO = mongoOperation.findOne(VIDEODAO_COLLECTION_NAME, 
 					new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), VideoInfoDAO.class);
 			String videoListId = videoInfoDAO.getVideoListId();
 			VideoTypeDAO videoTypeDAO = mongoOperation.findOne(VIDEOTYPESDAO_COLLECTION_NAME, 
 					new Query(Criteria.where("videoInfoId").is(videoListId)), VideoTypeDAO.class);
 			String artifactGroupId = videoTypeDAO.getArtifactGroupId();
 			deleteArtifacts(artifactGroupId);
 			mongoOperation.remove(VIDEOTYPESDAO_COLLECTION_NAME, new Query(Criteria.where("videoInfoId").is(videoListId)),VideoType.class);
 			mongoOperation.remove(VIDEODAO_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), VideoInfo.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	private boolean deleteArtifacts(String artifactGroupId) {
 		ArtifactGroupDAO artifactGroupDAO = mongoOperation.findOne(ARTIFACT_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(artifactGroupId)), ArtifactGroupDAO.class);
 		List<String> versionIds = artifactGroupDAO.getVersionIds();
 		mongoOperation.remove(ARTIFACT_INFO_COLLECTION_NAME, 
 				new Query(Criteria.where(REST_API_PATH_PARAM_ID).in(versionIds.toArray())), 
 				com.photon.phresco.commons.model.ArtifactInfo.class);
 		mongoOperation.remove(ARTIFACT_GROUP_COLLECTION_NAME, 
 				new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(artifactGroupId)), ArtifactGroupDAO.class);
 	    return true;    
 	}
 
 	/**
 	 * Returns the list of users
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_USERS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findUsers() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findUsers()");
 	    }
 		
 		try {
 			List<User> userList = mongoOperation.getCollection(USERS_COLLECTION_NAME, User.class);
 			if (userList.isEmpty()) {
 				return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 			}
 			
 			return Response.status(Response.Status.OK).entity(userList).build();
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, USERS_COLLECTION_NAME);
 		}
 	}
 
 	/**
 	 * Creates the list of users
 	 * @param users
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_USERS)
 	public Response createUser(List<User> users) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createUser(List<User> users)");
 	    }
 		
 		try {
 			mongoOperation.insertList(USERS_COLLECTION_NAME, users);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns List Of users form LDAP
 	 * @param users
 	 * @return 
 	 * @throws PhrescoException 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_USERS_IMPORT)
 	public Response importUsers(User user) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createUser(List<User> users)");
 	    }
 	    PhrescoServerFactory.initialize();
         RepositoryManager repoMgr = PhrescoServerFactory.getRepositoryManager();
     	Client client = ClientHelper.createClient();
         WebResource resource = client.resource(repoMgr.getAuthServiceURL() + "/ldap/import");
         resource.accept(MediaType.APPLICATION_JSON);
         ClientResponse response = resource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, user);
         GenericType<List<User>> genericType = new GenericType<List<User>>() {};
         List<User> users = response.getEntity(genericType);
         
         //To save the users into user table
         mongoOperation.insertList(USERS_COLLECTION_NAME, users);
 		return Response.status(Response.Status.OK).entity(users).build();
 	}
 
 	
 	/**
 	 * Updates the list of Users
 	 * @param users
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_USERS)
 	public Response updateUsers(List<User> users) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateUsers(List<User> users)");
 	    }
 		
 		try {
 			for (User user : users) {
 				User userInfo = mongoOperation.findOne(USERS_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(user.getId())), User.class);
 				if (userInfo  != null) {
 					mongoOperation.save(USERS_COLLECTION_NAME, user);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(users).build();
 	}
 
 	/**
 	 * Deletes the list of Users
 	 * @param users
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_USERS)
 	public void deleteUsers(List<User> users) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteUsers(List<User> users)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is" + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the user by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_USERS + REST_API_PATH_ID)
 	public Response getUser(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.getUser(String id)" + id);
 	    }
 		
 		try {
 			User userInfo = mongoOperation.findOne(USERS_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), User.class);
 			if (userInfo != null) {
 				return Response.status(Response.Status.OK).entity(userInfo).build(); 
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, USERS_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the list of User by id
 	 * @param users
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_USERS + REST_API_PATH_ID)
 	public Response updateUser(@PathParam(REST_API_PATH_PARAM_ID) String id , User user) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateUser(String id , User user)" + id);
 	    }
 		
 		try {
 			if (id.equals(user.getId())) {
 				mongoOperation.save(USERS_COLLECTION_NAME, user);
 				return Response.status(Response.Status.OK).entity(user).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build(); 
 	}
 	
 	/**
 	 * Deletes the user by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_USERS + REST_API_PATH_ID)
 	public Response deleteUser(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteUser(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(USERS_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), User.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
 	 * Returns the Roles
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_ROLES)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findRoles() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findRoles()");
 	    }
 		
 		try {
 			List<Role> roleList = mongoOperation.getCollection(ROLES_COLLECTION_NAME , Role.class);
 			if (roleList != null) {
 				return Response.status(Response.Status.OK).entity(roleList).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, ROLES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 
 	/**
 	 * Creates the list of Roles
 	 * @param roles
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_ROLES)
 	public Response createRoles(List<Role> roles) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createRoles(List<Role> roles)");
 	    }
 		
 		try {
 			mongoOperation.insertList(ROLES_COLLECTION_NAME , roles);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 
 	/**
 	 * Updates the list of Roles
 	 * @param roles
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_ROLES)
 	public Response updateRoles(List<Role> roles) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateRoles(List<Role> roles)");
 	    }
 		
 		try {
 			for (Role role : roles) {
 				Role roleInfo = mongoOperation.findOne(ROLES_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(role.getId())), Role.class);
 				if (roleInfo  != null) {
 					mongoOperation.save(ROLES_COLLECTION_NAME, role);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(roles).build();
 	}
 
 	/**
 	 * Deletes the list of Roles
 	 * @param roles
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_ROLES)
 	public void deleteRoles(List<Role> roles) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteRoles(List<Role> roles)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is"  + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the Role by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_ROLES + REST_API_PATH_ID)
 	public Response getRole(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.Response getRole(String id)" + id);
 	    }
 		
 		try {
 			Role role = mongoOperation.findOne(ROLES_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Role.class);
 			if (role != null) {
 				return Response.status(Response.Status.OK).entity(role).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, ROLES_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the list of role by id
 	 * @param role
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_ROLES + REST_API_PATH_ID)
 	public Response updateRole(@PathParam(REST_API_PATH_PARAM_ID) String id , Role role) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updateRole(String id , Role role)" + id);
 	    }
 		
 		try {
 			if (id.equals(role.getId())) {
 				mongoOperation.save(ROLES_COLLECTION_NAME, role);
 				return Response.status(Response.Status.OK).entity(role).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
 	}
 	
 	/**
 	 * Deletes the role by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_ROLES + REST_API_PATH_ID)
 	public Response deleteRole(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deleteRole(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(ROLES_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Role.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 
 	/**
 	 * Returns the Permisions
 	 * @return
 	 */
 	@GET
 	@Path (REST_API_PERMISSIONS)
 	@Produces (MediaType.APPLICATION_JSON)
 	public Response findPermissions() {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.findPermissions()");
 	    }
 		
 		try {
 			List<Permission> permissionList = mongoOperation.getCollection(PERMISSION_COLLECTION_NAME , Permission.class);
 			if (permissionList != null) {
 				return Response.status(Response.Status.OK).entity(permissionList).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, PERMISSION_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 
 	/**
 	 * Creates the list of permissions
 	 * @param permission
 	 * @return 
 	 */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PERMISSIONS)
 	public Response createPermission(List<Permission> permissions) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createPermission(List<Permission> permissions)");
 	    }
 		
 		try {
 			mongoOperation.insertList(PERMISSION_COLLECTION_NAME , permissions);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 
 	/**
 	 * Updates the list of permissions
 	 * @param permission
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PERMISSIONS)
 	public Response updatePermissions(List<Permission> permissions) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updatePermissions(List<Permission> permissions)");
 	    }
 		
 		try {
 			for (Permission permission : permissions) {
 				Permission permisonInfo = mongoOperation.findOne(PERMISSION_COLLECTION_NAME , 
 				        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(permission.getId())), Permission.class);
 				if (permisonInfo  != null) {
 					mongoOperation.save(PERMISSION_COLLECTION_NAME, permission);
 				}
 			}
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.OK).entity(permissions).build();
 	}
 
 	/**
 	 * Deletes the list of permissions
 	 * @param permission
 	 * @throws PhrescoException 
 	 */
 	@DELETE
 	@Path (REST_API_PERMISSIONS)
 	public void deletePermissions(List<Permission> permissions) throws PhrescoException {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deletePermissions(List<Permission> permissions)");
 	    }
 		
 		PhrescoException phrescoException = new PhrescoException(EX_PHEX00001);
 		S_LOGGER.error("PhrescoException Is"  + phrescoException.getErrorMessage());
 		throw phrescoException;
 	}
 
 	/**
 	 * Get the Role by id for the given parameter
 	 * @param id
 	 * @return
 	 */
 	@GET
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PERMISSIONS + REST_API_PATH_ID)
 	public Response getPermission(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.Response getPermission(String id)" + id);
 	    }
 		
 		try {
 			Permission permission = mongoOperation.findOne(PERMISSION_COLLECTION_NAME, 
 			        new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Permission.class);
 			if (permission != null) {
 				return Response.status(Response.Status.OK).entity(permission).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00005, PERMISSION_COLLECTION_NAME);
 		}
 		
 		return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
 	}
 	
 	/**
 	 * Updates the list of permissions by id
 	 * @param permission
 	 * @return
 	 */
 	@PUT
 	@Consumes (MediaType.APPLICATION_JSON)
 	@Produces (MediaType.APPLICATION_JSON)
 	@Path (REST_API_PERMISSIONS + REST_API_PATH_ID)
 	public Response updatePermission(@PathParam(REST_API_PATH_PARAM_ID) String id , Permission permission) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.updatePermission(String id , Permission permission)" + id);
 	    }
 		
 		try {
 			if (id.equals(permission.getId())) {
 				mongoOperation.save(PERMISSION_COLLECTION_NAME, permission);
 				return Response.status(Response.Status.OK).entity(permission).build();
 			} 
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, UPDATE);
 		}
 		
 		return Response.status(Response.Status.BAD_REQUEST).entity(ERROR_MSG_ID_NOT_EQUAL).build();
 	}
 	
 	/**
 	 * Deletes the permission by id for the given parameter
 	 * @param id
 	 * @return 
 	 */
 	@DELETE
 	@Path (REST_API_PERMISSIONS + REST_API_PATH_ID)
 	public Response deletePermission(@PathParam(REST_API_PATH_PARAM_ID) String id) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.deletePermission(String id)" + id);
 	    }
 		
 		try {
 			mongoOperation.remove(PERMISSION_COLLECTION_NAME, new Query(Criteria.where(REST_API_PATH_PARAM_ID).is(id)), Permission.class);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, DELETE);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	/**
      * Creating the jforum 
      * @param id
      * @return
      */
 	@POST
 	@Consumes (MediaType.APPLICATION_JSON)
     @Path (REST_API_FORUMS)
     public Response createForum(List<Property> adminConfigInfo) {
 	    if (isDebugEnabled) {
 	        S_LOGGER.debug("Entered into AdminService.createForum(List<AdminConfigInfo> adminConfigInfo)");
 	    }
 		
 		try {
 			mongoOperation.insertList(FORUM_COLLECTION_NAME , adminConfigInfo);
 		} catch (Exception e) {
 			throw new PhrescoWebServiceException(e, EX_PHEX00006, INSERT);
 		}
 		
 		return Response.status(Response.Status.OK).build();
 	}
 	
 	 /**
      * Get the customer by id for the given parameter
      * @param id
      * @return
      */
     @GET
     @Produces (MediaType.APPLICATION_JSON)
     @Path (REST_API_FORUMS)
     public Response getForum(@QueryParam(REST_QUERY_CUSTOMERID) String customerId) {
         if (isDebugEnabled) {
             S_LOGGER.debug("Entered into AdminService.getForum(String id)");
         }
     	
     	try {
     		Property adminConfig = mongoOperation.findOne(FORUM_COLLECTION_NAME, 
     		        new Query(Criteria.where(REST_QUERY_CUSTOMERID).is(customerId)), Property.class);
     		if (adminConfig != null) {
     			return Response.status(Response.Status.OK).entity(adminConfig).build();
     		}
     	} catch (Exception e) {
     		throw new PhrescoWebServiceException(e, EX_PHEX00005, FORUM_COLLECTION_NAME);
     	}
     	
     	return Response.status(Response.Status.NO_CONTENT).entity(ERROR_MSG_NOT_FOUND).build();
     }
  }
