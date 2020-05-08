 package com.meadowhawk.homepi.service;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.HeaderParam;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.ResponseBuilder;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriBuilder;
 import javax.ws.rs.core.UriInfo;
 
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.stereotype.Component;
 
 import com.meadowhawk.homepi.exception.HomePiAppException;
 import com.meadowhawk.homepi.model.HomePiUser;
 import com.meadowhawk.homepi.model.LogData;
 import com.meadowhawk.homepi.model.ManagedApp;
 import com.meadowhawk.homepi.model.PiProfile;
 import com.meadowhawk.homepi.service.business.DeviceManagementService;
 import com.meadowhawk.homepi.service.business.HomePiUserService;
 import com.meadowhawk.homepi.service.business.LogDataService;
 import com.meadowhawk.homepi.service.business.ManagedAppsService;
 import com.meadowhawk.homepi.util.StringUtil;
 import com.meadowhawk.homepi.util.model.PublicRESTDoc;
 import com.meadowhawk.homepi.util.model.PublicRESTDocMethod;
 import com.meadowhawk.homepi.util.model.TODO;
 
 @Path("/homepi")
 @Component
 @PublicRESTDoc(serviceName = "HomePiService", description = "Pi focused management services specifically for Pis.")
 public class HomePiRestService {
 	private static final String APP_ID = "app_id";
 	private static Logger log = Logger.getLogger( HomePiRestService.class );
 	private static final String ACCESS_TOKEN = "access_token";
 	
 	@Context UriInfo uriInfo;
 	
 	@Autowired
 	ClassPathResource updateFile;
 
 	@Autowired
 	HomePiUserService userService;
 	
 	@Value("${update.mainFile}")
 	String mainUpdateFileName;
 	
 	@Value("${update.mainFileVersion}")
 	Integer mainUpdateFileVersion;
 	
 	
 	@Autowired
 	DeviceManagementService deviceManagementService;
 
 	@Autowired
 	ManagedAppsService managedAppsService;
 	
 	@Autowired
 	LogDataService logDataService;
 	
 	@POST
 	@Path("/user/{user_id}/pi/{piSerialId}/api")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName="Update Pi API Key", description="Updated the API key for the Pi. This can only be called by an auth user. Sadly for security reasons the user has to change the API stored on the PI manually. Returns 204 if sucessful.", sampleLinks={"/homepi/pi/01r735ds720/reg/api/de4d9e75-d6b3-43d7-9fef-3fb958356ded"})
 	public Response updatePiApiKey(@PathParam("user_id") String userId, @PathParam("piSerialId") String piSerialId, @HeaderParam(ACCESS_TOKEN) String authToken) {
 		deviceManagementService.updateApiKey(userId, authToken, piSerialId);
 		return Response.noContent().build();
 	}
 
 	@GET
 	@Path("/user/{user_id}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName = "User Profile", description = "Retrieve user profile. Include access_token in head to gain owner view.", sampleLinks = { "/user/profile/test_user" })
 	public Response getUser(@PathParam("user_id") String userId, @HeaderParam(ACCESS_TOKEN) String authToken){
 		if(!StringUtil.isNullOrEmpty(userId)){
 			//get authfrom request or set to null
 			HomePiUser hUser = userService.getUserData(userId, authToken);
 			return Response.ok(hUser).build(); 
 		} else {
 			throw new HomePiAppException(Status.NOT_FOUND,"Invalid user ID");
 		}
 	}
 	
 	@GET
 	@Path("/user/{user_id}/pi/{pi_serial_id}/log/{app_name}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName="Log Pi Message", group="Logs", description="Retrieves logs entries for given Pi. Pi API key or user auth may be required.", sampleLinks={"/homepi/pi/8lhdfenm1x/log"})
	public List<LogData> getLogsForApp(@PathParam("pi_serial_id") String piSerialId, @PathParam("app_name") String appName, @QueryParam("") String type){
 		return logDataService.getLogDataByKey(LogDataService.SEARCH_TYPE.PI_SERIAL, piSerialId);
 	}
 	
 	@GET
 	@Path("/user/{user_id}/pi/{pi_serial_id}/log")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName="Log Pi Message", group="Logs", description="Retrieves logs entries for given Pi. Pi API key or user auth may be required.", sampleLinks={"/homepi/pi/8lhdfenm1x/log"})
 	public List<LogData> getLogsForPi(@PathParam("pi_serial_id") String piSerialId){
 		return logDataService.getLogDataByKey(LogDataService.SEARCH_TYPE.PI_SERIAL, piSerialId);
 	}
 	
 	
 	//ProfileManagement
 	@GET
 	@Path("/user/{user_id}/pi/{piSerialId}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName = "Get PiProfile", description = "Retrieve pi profile by pi serial id. Include access_token for owner view.", sampleLinks = { "/homepi/user/test_user/pi/8lhdfenm1x" })
 	public Response getUserPiProfile(@PathParam("user_id") String userName,@PathParam("piSerialId") String piSerialId, @HeaderParam(ACCESS_TOKEN) String authToken){
 		PiProfile profile = userService.getPiProfile(userName, authToken,piSerialId);
 		return Response.ok(profile).build();
 	}
 	
 	@POST
 	@Path("/user/{user_id}/pi/{piSerialId}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName = "Update PiProfile", description = "Retrieve users pi profiles. access_token is required or returns NoAccess.", sampleLinks = { "/homepi/user/test_user/pi/8lhdfenm1x" })
 	public Response updatePiProfile(@PathParam("user_id") String userName,@PathParam("piSerialId") String piSerialId, @HeaderParam(ACCESS_TOKEN) String authToken, PiProfile piProfile){
 		userService.updatePiProfile(userName, authToken,piSerialId, piProfile);
 		log.debug("Redirection to:"+getUriRedirect("getUserPiProfile"));
 //		return Response.seeOther(getUriRedirect("getUserPiProfile")).build();
 		return Response.noContent().build();
 	}
 	
 	@POST
 	@Path("/user/{user_name}/pi/{pi_serial_id}/app")
 	@PublicRESTDocMethod(endPointName = "Assign App to PiProfile", description = "Assignes the specified Managed App to the Pi Profile by passing the app_id header param. 'access_token' is also required.", sampleLinks = { "/homepi/user/test_user/pi/8lhdfenm1x" })
 	public Response assignAppToPiProfile(@PathParam("user_name") String userName,@PathParam("pi_serial_id") String piSerialId, @HeaderParam(ACCESS_TOKEN) String authToken, @HeaderParam(APP_ID) Long appId){
 		userService.addAppToProfile(userName, authToken,piSerialId, appId);
 		return Response.noContent().build();
 	}
 	
 	@DELETE
 	@Path("/user/{user_name}/pi/{pi_serial_id}/app")
 	@PublicRESTDocMethod(endPointName = "Remove Assignment App to PiProfile", description = "Assignes the specified Managed App to the Pi Profile by passing the app_id header param. 'access_token' is also required.", sampleLinks = { "/homepi/user/test_user/pi/8lhdfenm1x" })
 	public Response deleteAppToPiProfile(@PathParam("user_name") String userName,@PathParam("pi_serial_id") String piSerialId, @HeaderParam(ACCESS_TOKEN) String authToken, @HeaderParam(APP_ID) Long appId){
 		userService.deleteAppToProfile(userName, authToken,piSerialId, appId);
 		return Response.noContent().build();
 	}
 	
 	
 	@GET
 	@Path("/user/{user_id}/pi")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(endPointName = "Get User PiProfiles", description = "Retrieve users pi profiles. Include access_token in head to gain owner view.", sampleLinks = { "/homepi/user/test_user/pi" })
 	public Response getUserPiProfiles(@PathParam("user_id") String userId, @HeaderParam(ACCESS_TOKEN) String authToken){
 		HomePiUser hUser = userService.getUserData(userId, authToken);
 		return Response.ok(hUser.getPiProfiles()).build();
 	}
 	
 	
 	@POST
 	@Path("/user/{user_id}/app")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(group="Managed Apps", endPointName="Create New App", description="Allows User to define new managed applications which can then be assigned to a given PI.", sampleLinks="/user/test_user/app")
 	public Response createManagedApp(@PathParam("user_id") String userName, @HeaderParam(ACCESS_TOKEN) String authToken, ManagedApp managedApp){
 		
 		ManagedApp ma =  managedAppsService.createUserApp(userName, authToken, managedApp);
 		//TODO: Add redirect to newly created App.
 		log.debug("   >> redirect to:  "+getUriRedirect("getApp"));
 //		return Response.seeOther().build();
 		return Response.ok(ma).build();
 	}
 	
 	@GET
 	@Path("/user/{user_id}/app/")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(group="Managed Apps", endPointName="Get Managed Apps", description="Returns all managed apps that a user has created. User access_token required for viewing some data.", sampleLinks="/user/test_user/app/")
 	public Response getAllApps(@PathParam("user_id") String userId, @HeaderParam(ACCESS_TOKEN) String authToken){
 		return Response.ok(managedAppsService.getUserApps(userId, authToken)).build();
 	}
 	
 	@GET
 	@Path("/user/{user_id}/app/{app_name}")
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(group="Managed Apps", endPointName="", description="", sampleLinks="/user/test_user/app/TestApp")
 	public Response getApp(@PathParam("user_id") String userId, @PathParam("app_name") String appName,  @HeaderParam(ACCESS_TOKEN) String authToken){
 		ManagedApp ma = managedAppsService.getUserApp(userId, authToken, appName);
 		return Response.ok(ma).build();
 	}
 	
 	@POST
 	@Path("/user/{user_id}/app/{app_name}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(group="Managed Apps", endPointName="Update App", description="", sampleLinks="/user/test_user/app/TestApp")
 	public Response updateApp(@PathParam("user_id") String userId, @PathParam("app_name") String appName,  @HeaderParam(ACCESS_TOKEN) String authToken, ManagedApp managedApp){
 	  
 		managedAppsService.updateUserApps(userId, authToken, appName, managedApp);
 		//TODO: Add redirect
 		
 		return Response.ok(new TODO()).build();
 	}
 	
 	@DELETE
 	@Path("/user/{user_name}/app/{app_name}")
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.APPLICATION_JSON)
 	@PublicRESTDocMethod(group="Managed Apps", endPointName="Delete App", description="Permanantly deletes managed app. Users access_token is required for processing.", sampleLinks="/user/test_user/app/TestApp")
 	public Response deleteApp(@PathParam("user_name") String userName, @PathParam("app_name") String appName,  @HeaderParam(ACCESS_TOKEN) String authToken){
 	  managedAppsService.deleteManageApp(userName, authToken, appName);
 		return Response.noContent().build();
 	}
 	
 	//TODO: REMOVE
 	@GET
 	@Path("/update")
 	@Produces("text/x-python")
 	@PublicRESTDocMethod(endPointName="Update Pi", description="EndPoint a Pi will call to request updates. Pi API key is required.", sampleLinks={"/homepi/pi/update"})
 	@Deprecated
 	public Response getScriptUpdate(){
 		//TODO: Remove this once PY script is updated.
 		File file;
 		try {
 			file = updateFile.getFile();
 			ResponseBuilder response = Response.ok((Object) file);
 			response.header("Content-Disposition", "attachment; filename=homePi.py");
 			response.header("file-version", mainUpdateFileVersion);
 			log.debug("mainFile name=" + this.mainUpdateFileName);
 			return response.build();
 		} catch (IOException e) {
 			log.warn("Error while trying to get update file.", e);
 		}
 		 
 		return Response.noContent().build();
 	}
 	
 	/**
 	 * Generates a URI for redirect to one of the other endpoints in the current Service.
 	 * @param methodName - one of the public methods in this service.
 	 * @return - URI for redirect.
 	 */
 	protected URI getUriRedirect(String methodName){
 		UriBuilder ub = uriInfo.getBaseUriBuilder().path(HomePiRestService.class);
 		URI redirectURI = ub.path(HomePiRestService.class, methodName).build();
 		
 		return redirectURI;
 	}
 }
