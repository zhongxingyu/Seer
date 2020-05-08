 /**
  * Framework Web Archive
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
 package com.photon.phresco.framework.rest.api;
 
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Response;
 
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.rest.api.util.ActionResponse;
 import com.photon.phresco.service.client.api.ServiceManager;
 import com.photon.phresco.util.Utility;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class RestBaseTest extends RestBase {
 	ActionService actionservice = new ActionService();
 	
 	protected static ServiceManager serviceManager = null;
 	protected String userId = "";
 	protected String password = "";
 	protected String customerId = "";
 	protected String appDirName = "";
 	protected String appCode = "";
 	protected String techId = "";
 	protected String projectId = "";
 	protected String appId = "";
 	protected String dotPhrescoFolder = "";
 	protected String projectInfo = "";
 	
 	public RestBaseTest() {
		userId = "saravanakumar_g";
		password = "S@ravana89";
 		customerId = "photon";
 		serviceManager = getServiceManager(userId, password);
 		appDirName = "TestProject";
 		appCode = "TestProject";
 		techId = "tech-java-webservice";
 		projectId = "TestProject";
 		appId    = "TestProject";
 		dotPhrescoFolder = ".phresco";
 		projectInfo = "project.info";
 	}
 	
 	protected List<String> getCustomer() {
 		return Arrays.asList("photon");
 	}
 	
 	protected File getProjectInfoPath() {
 		File projectHome = new File(Utility.getProjectHome() + File.separator + appDirName + File.separator + dotPhrescoFolder + File.separator+ projectInfo);
 		return projectHome;
 	}
 	
 	protected File getTempPath() {
 		File projectHome = new File(Utility.getProjectHome() + File.separator + appDirName + File.separator + dotPhrescoFolder + File.separator+ "temp.info");
 		return projectHome;
 	}
 	
 	protected Client createClient() {
 		ClientConfig cfg = new DefaultClientConfig();
 		cfg.getClasses().add(JacksonJsonProvider.class);
 		Client client = Client.create(cfg);
 		return client;
 	}
 	
 	public Boolean readLog(String uniqueKey) throws PhrescoException {
 		MockHttpServletRequest request = new MockHttpServletRequest();
 		request.setParameter("uniquekey", uniqueKey);
 		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
 		Response build = actionservice.read(httpServletRequest);
 		ActionResponse output = (ActionResponse) build.getEntity();
 		System.out.println(output.getLog());
 		if (output.getLog() != null) {
 
 			if (output.getLog().contains("BUILD FAILURE")) {
 				fail("Error occured ");
 			}
 			if ("INPROGRESS".equalsIgnoreCase(output.getStatus())) {
 				readLog(uniqueKey);
 				return true;
 			} else if ("COMPLETED".equalsIgnoreCase(output.getStatus())) {
 				System.out.println("***** Log finished ***********");
 				return true;
 			} else if ("ERROR".equalsIgnoreCase(output.getStatus())) {
 				fail("Error occured while retrieving the logs");
 				return false;
 			}
 		}
 		return false;
 	}
 }
