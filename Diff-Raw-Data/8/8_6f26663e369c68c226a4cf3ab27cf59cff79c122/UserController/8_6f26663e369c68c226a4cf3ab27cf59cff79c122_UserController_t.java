 /**
  * JBoss, Home of Professional Open Source
  * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
  * contributors by the @authors tag. See the copyright.txt in the
  * distribution for a full listing of individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package cz.muni.fi.pv243.controller;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.inject.Model;
 import javax.enterprise.inject.Produces;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.seam.security.BaseAuthenticator;
 import org.jboss.seam.security.Credentials;
 import org.picketlink.idm.impl.api.PasswordCredential;
 
 import cz.muni.fi.pv243.model.User;
 import cz.muni.fi.pv243.service.UserManager;
 
 @Model
 public class UserController extends BaseAuthenticator{
 
     @Inject
     private FacesContext facesContext;
 
     @Inject
     private UserManager userManager;
     
     @Inject
     private Credentials credentials;
     
     private User newUser;
 
     @Produces
     @Named
     public User getNewUser() {
 		return newUser;
 	}
 
 	
     @PostConstruct
     public void initNewUser() {
         newUser = new User();
     }
 
    public void register() throws Exception {
         try {
             userManager.create(newUser);
             FacesMessage m = new FacesMessage(FacesMessage.SEVERITY_INFO, "Registered!", "Registration successful");
            facesContext.addMessage("registerForm:registerMessages", m);
             initNewUser();
         } catch (Exception e) {
        	facesContext.addMessage("registerForm:registerMessages",
 					new FacesMessage("Registration failed."));
         }
     }
 
 
 	@Override
 	public void authenticate() {
 		try {
     		User userFromDb = userManager.findByEmail(credentials.getUsername());
     		if (userFromDb == null){
     			setStatus(AuthenticationStatus.FAILURE);
     			facesContext.addMessage("loginForm:username", new FacesMessage(
     					"Non existing user"));
     		} else {
     			if (userFromDb.getPasswordHash().equals(((PasswordCredential)credentials.getCredential()).getValue())) {
     				//login success
         			setStatus(AuthenticationStatus.SUCCESS);
         			setUser(userFromDb);
     			}else {
     				//redirect to login failed
         			setStatus(AuthenticationStatus.FAILURE);
     			}
     		}
     	} catch (Exception e) {
     		//TODO error message using facesContext
     	}
 	}
 
     
 }
