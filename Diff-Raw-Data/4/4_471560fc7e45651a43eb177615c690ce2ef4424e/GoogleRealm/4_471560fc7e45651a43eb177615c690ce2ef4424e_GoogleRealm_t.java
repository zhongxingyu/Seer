 /**
  * Copyright 2013 Ryan Shaw (ryanfx1@gmail.com)
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 
 package com.blogspot.ryanfx.auth;
 
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.catalina.realm.GenericPrincipal;
 import org.apache.catalina.realm.RealmBase;
 import com.blogspot.ryanfx.dao.UserDAO;
 
 public class GoogleRealm extends RealmBase{
 
 	private String PASSWORD = "Password!";
 	
 	private UserDAO userDAO = null;
 	private List<String> garageRollList = new ArrayList<String>();
 	
 	/**
 	 * Instantiate a new GoogleRealm with a roll list of garage... this was done
 	 * so different roles in the future can be added
 	 */
 	public GoogleRealm(){
 		userDAO = new UserDAO();
 		garageRollList.add("garage");
 	}
 	
 	/**
 	 * Authenticates a user.
 	 * @param userName Google auth token that is requesting authentication
 	 * @param password the shared password used in the appplication
 	 * @return authenticated and authorized principal, if one exists.
 	 */
 	@Override
 	public Principal authenticate(String userName, String password) {
 		Principal principal = null;
 		try {
 			Logger.getLogger(getClass().getName()).info("Authenticating with Google...");
 			String email = GoogleUtil.submitUserToken(userName);
 			Logger.getLogger(getClass().getName()).info("Checking if user is valid...");
 			if (userDAO.isValidUser(email) && password.equals(PASSWORD)){
 				principal = new GenericPrincipal(email, null, garageRollList);
 			}
 		} catch (Exception e) {
 			Logger.getLogger(getClass().getName()).log(
 					Level.SEVERE, "Exception during submission of user token: " + userName, e);
 		}
 		return principal;
 	}
 	
 	@Override
 	protected String getName() {
 		return "GoogleRealm";
 	}
 
 	/**
 	 * Not needed.
 	 */
 	@Override
 	protected String getPassword(String arg0) {
 		return null;
 	}
 
 	/**
 	 * Given an email address, get the Principal associated with it.
 	 */
 	@Override
 	protected Principal getPrincipal(String emailAddress) {
 		Principal principal = null;
 		try {
 			if (userDAO.isValidUser(emailAddress)){
 				principal = new GenericPrincipal(emailAddress, null, null);
 			}
 		} catch (Exception e) {
 			Logger.getLogger(GoogleRealm.class.getName()).log(
 					Level.SEVERE, "Exception during local user authorization", e);
 		}
 		return principal;
 		
 	}
 
 }
