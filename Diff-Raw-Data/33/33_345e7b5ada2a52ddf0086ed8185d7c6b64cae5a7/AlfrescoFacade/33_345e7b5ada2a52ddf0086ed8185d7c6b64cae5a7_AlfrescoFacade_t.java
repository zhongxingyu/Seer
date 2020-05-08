 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.sourcesense.alfresco.opensso;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.alfresco.model.ContentModel;
 import org.alfresco.repo.security.authentication.AuthenticationComponent;
 import org.alfresco.service.ServiceRegistry;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.repository.NodeService;
 import org.alfresco.service.cmr.security.AuthenticationService;
 import org.alfresco.service.cmr.security.AuthorityService;
 import org.alfresco.service.cmr.security.AuthorityType;
 import org.alfresco.service.cmr.security.PermissionService;
 import org.alfresco.service.cmr.security.PersonService;
 import org.alfresco.service.namespace.QName;
 import org.alfresco.service.transaction.TransactionService;
 import org.alfresco.web.app.servlet.AuthenticationHelper;
 import org.alfresco.web.bean.LoginBean;
 import org.alfresco.web.bean.repository.User;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.sourcesense.alfresco.transaction.Transactionable;
 import com.sourcesense.alfresco.transaction.TransactionalHelper;
 
 /**
  * Facade for Alfresco operations, such as create user and groups
  * 
  * @author g.fernandes@sourcesense.com
  * 
  */
 public class AlfrescoFacade {
 
 	private static Log logger = LogFactory.getLog(AlfrescoFacade.class);
 
 	private TransactionService transactionService;
 	private NodeService nodeService;
 	private AuthenticationComponent authComponent;
 	private AuthenticationService authService;
 	private PersonService personService;
 	private PermissionService permissionService;
 	private AuthenticationService authenticationService;
 	private TransactionalHelper transactionalHelper;
 	private AuthorityService authorityService;
 
 	public AlfrescoFacade(ServletContext servletContext) {
 		WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
 		ServiceRegistry serviceRegistry = (ServiceRegistry) ctx.getBean(ServiceRegistry.SERVICE_REGISTRY);
 		transactionService = serviceRegistry.getTransactionService();
 		nodeService = serviceRegistry.getNodeService();
 		authComponent = (AuthenticationComponent) ctx.getBean("AuthenticationComponent");
 		authService = (AuthenticationService) ctx.getBean("AuthenticationService");
 		personService = (PersonService) ctx.getBean("personService");
 		permissionService = (PermissionService) ctx.getBean("permissionService");
 		authenticationService = (AuthenticationService) ctx.getBean("authenticationService");
 		authorityService = (AuthorityService) ctx.getBean("authorityService");
 		
 		transactionalHelper = new TransactionalHelper(transactionService);
 	}
 	
 	
 
 	protected void setAuthenticatedUser(HttpServletRequest req, final HttpSession httpSess, final String userName) {
 		authComponent.setCurrentUser(userName);
 		transactionalHelper.doInTransaction(new Transactionable() {
 			public Object execute() {
 				User user;
 				NodeRef homeSpaceRef = null;
 				user = new User(userName, authService.getCurrentTicket(), personService.getPerson(userName));
 				homeSpaceRef = (NodeRef) nodeService.getProperty(personService.getPerson(userName), ContentModel.PROP_HOMEFOLDER);
 				user.setHomeSpaceId(homeSpaceRef.getId());
 				populateSession(httpSess, user);
 				return null;
 			}
 		});
 	}
 
 	protected void populateSession(HttpSession httpSess, User user) {
 		httpSess.setAttribute(AuthenticationHelper.AUTHENTICATION_USER, user);
 		httpSess.setAttribute(LoginBean.LOGIN_EXTERNAL_AUTH, Boolean.TRUE);
 	}
 
 	public void createUser(final String username, final String email, final String firstName, final String lastName) {
 		transactionalHelper.doInTransaction(new Transactionable() {
 			public Object execute() {
 				authenticationService.createAuthentication(username, username.toCharArray());
 				HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
 				properties.put(ContentModel.PROP_USERNAME, username);
 				properties.put(ContentModel.PROP_FIRSTNAME, firstName);
 				properties.put(ContentModel.PROP_LASTNAME, lastName);
				properties.put(ContentModel.PROP_EMAIL, getNullSafe(email));
 				NodeRef newPerson = personService.createPerson(properties);
 				permissionService.setPermission(newPerson, username, permissionService.getAllPermission(), true);
 				authenticationService.setAuthenticationEnabled(username, true);
 				return null;
 			}

			private String getNullSafe(String email) {
				return (email==null||email.isEmpty())?username.concat("@"):email;
			}
 		});
 	}
 
 	public Boolean existUser(final String username) {
 		return (Boolean) transactionalHelper.doInTransaction(new Transactionable() {
 			public Object execute() {
 				return personService.personExists(username);
 			}
 		});
 	}
 
 	public ArrayList<String> getUserGroups(String username) {
 		return new ArrayList<String>();
 	}
 
 	public void createGroups(final String principal, final List<String> groups) {
 		if(groups==null || groups.size()==0) {
 			return;
 		}
 		transactionalHelper.doInTransaction(new Transactionable() {
 			public Object execute() {
 				for (String group : groups) {
 					String authority = "GROUP_".concat(group);
 					if(!authorityService.authorityExists(authority)) {
 						authority = authorityService.createAuthority(AuthorityType.GROUP, null, group);
 					}
 					authorityService.addAuthority(authority,principal);
 				}
 				return null;
 			}
 		});
 	}
 }
