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
 
import java.net.URL;
import java.net.URLConnection;
 import java.util.Date;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.photon.phresco.commons.model.User;
 import com.photon.phresco.commons.model.User.AuthType;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.logger.SplunkLogger;
 import com.photon.phresco.service.api.DbManager;
 import com.photon.phresco.service.api.PhrescoServerFactory;
 import com.photon.phresco.service.api.RepositoryManager;
 import com.photon.phresco.service.impl.DbService;
 import com.photon.phresco.service.model.ServerConfiguration;
 import com.photon.phresco.service.rest.util.AuthenticationUtil;
 import com.photon.phresco.service.util.ServerConstants;
 import com.photon.phresco.service.util.ServerUtil;
 import com.photon.phresco.util.Credentials;
 import com.photon.phresco.util.ServiceConstants;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.wordnik.swagger.annotations.ApiOperation;
 import com.wordnik.swagger.annotations.ApiParam;
 
 @Controller
 @RequestMapping(value = ServiceConstants.REST_API_LOGIN, consumes = MediaType.APPLICATION_JSON_VALUE, 
 		produces = MediaType.APPLICATION_JSON_VALUE)
 public class LoginService extends DbService {
 	
 	private final String SERVICE_VIEW_ROLE_ID = "4e8c0bed7-fb39-4erta-ae73-2d1286ae4ad0";
 	private final String FRAMEWORK_VIEW_ROLE_ID = "4e8c0bd7-fb39-4aea-ae73-2d1286ae4ae0";
 	private static final SplunkLogger LOGGER = SplunkLogger.getSplunkLogger("SplunkLogger");
 	private static Boolean isDebugEnabled = LOGGER.isDebugEnabled();
 	
 	public LoginService() {
 		super();
 	}
 	
 	@ApiOperation(value = " Login service ")
     @RequestMapping(method = RequestMethod.POST)
     public @ResponseBody User login(HttpServletRequest request, HttpServletResponse response,
     		@ApiParam(value = "user credentials to login", name = "credentials")@RequestBody Credentials credentials) throws PhrescoException {
 		if(isDebugEnabled) {
 			LOGGER.debug("LoginService.login : Entry");
 			LOGGER.debug("LoginService.login ", "remoteAddress=" + request.getRemoteAddr() , "userName=" + credentials.getUsername(),
 					"endpoint=" + request.getRequestURI(),  "method=" + request.getMethod());
 		}
 		User user = null;
 		PhrescoServerFactory.initialize();
 		DbManager dbManager = PhrescoServerFactory.getDbManager();
 		user = dbManager.authenticate(credentials.getUsername(), credentials.getPassword());
         if(user != null){
         	user.setValidLogin(true);
         	user.setToken(createAuthToken(credentials.getUsername()));
         	user.setPhrescoEnabled(true);
             user.setValidLogin(true);
             user.setCustomers(findCustomersFromDB());
         	return user;
         }
         user = loginUsingAuth(credentials, response);
         if(isDebugEnabled) {
         	LOGGER.debug("LoginService.createProject : Exit");
 		}
 		return user;
     }
 	
 	private User loginUsingAuth(Credentials credentials, HttpServletResponse response) throws PhrescoException {
 		ServerConfiguration serverConfig = PhrescoServerFactory.getServerConfig();
 		Client client = Client.create();
 		PhrescoServerFactory.initialize();
		User user = null; 
 		RepositoryManager repoMgr = PhrescoServerFactory.getRepositoryManager();
		boolean checkLDAPAvailable = checkLDAPAvailable(repoMgr);
		if(!checkLDAPAvailable) {
			response.setStatus(204);
			return user;
		}
 		WebResource resource = client.resource(repoMgr.getAuthServiceURL() + ServerConstants.AUTHENTICATE);
         resource.accept(MediaType.APPLICATION_JSON_VALUE);
         ClientResponse clientResponse = resource.type(MediaType.APPLICATION_JSON_VALUE).post(ClientResponse.class, credentials);
         if(clientResponse.getStatus() == 204) {
         	response.setStatus(204);
         	LOGGER.info("LoginService.loginUsingAuth", "authUrl=" + repoMgr.getAuthServiceURL(), "userName=" + credentials.getUsername());
         	return null;
         }
         GenericType<User> genericType = new GenericType<User>() {};
        user = clientResponse.getEntity(genericType);
         user.setToken(createAuthToken(credentials.getUsername()));
         user.setPhrescoEnabled(true);
         user.setValidLogin(true);
         user.setRoleIds(serverConfig.getDefaultRoles());
         if(user != null) {
         	user.setCreationDate(new Date());
         	user.setPassword(ServerUtil.encodeUsingHash(credentials.getUsername(), 
         			ServerUtil.decryptString(credentials.getPassword())));
         	user.setAuthType(AuthType.LOCAL);
         	DbService.getMongoOperation().save(USERS_COLLECTION_NAME, user);
         }
         user.setCustomers(findCustomersFromDB());
         return user;
 	}

	private boolean checkLDAPAvailable(RepositoryManager repoMgr) {
		boolean isAlive = true;
		try {
			URL url = new URL(repoMgr.getAuthServiceURL());
			URLConnection connection = url.openConnection();
			connection.connect();
		} catch (Exception e) {
			isAlive = false;
		}
		return isAlive;
	}
 	
 	private String createAuthToken(String userName) throws PhrescoException {
 		AuthenticationUtil authTokenUtil = AuthenticationUtil.getInstance();
 	    return authTokenUtil.generateToken(userName);
 	}
 }
