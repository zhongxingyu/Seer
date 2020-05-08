 /*
  * Copyright 2012 INRIA
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mymed.controller.core.requesthandler.v2;
 
 import static com.mymed.utils.MiscUtils.json_to_map;
 
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.JsonSyntaxException;
 import com.mymed.controller.core.exception.AbstractMymedException;
 import com.mymed.controller.core.exception.IOBackEndException;
 import com.mymed.controller.core.exception.InternalBackEndException;
 import com.mymed.controller.core.manager.profile.ProfileManager;
 import com.mymed.controller.core.requesthandler.message.JsonMessageOut;
 
 /**
  * Servlet implementation class UsersRequestHandler
  */
 @WebServlet("/v2/ProfileRequestHandler")
 public class ProfileRequestHandler extends AbstractRequestHandler {
 
     /**
      * Generated serial ID.
      */
     private static final long serialVersionUID = -7350632718579822436L;
 
     /**
      * JSON 'id' attribute.
      */
     @Deprecated
     private static final String JSON_ID = JSON.get("json.id");
 
     /**
      * JSON 'profile' attribute.
      */
     private static final String JSON_PROFILE = JSON.get("json.profile");
 
     private ProfileManager profileManager;
 
     /**
      * @throws ServletException
      * @see HttpServlet#HttpServlet()
      */
     public ProfileRequestHandler() throws ServletException {
         super();
 
         try {
             profileManager = new ProfileManager();
         } catch (final InternalBackEndException e) {
             throw new ServletException("ProfileManager is not accessible because: " + e.getMessage());
         }
     }
 
     /*
      * (non-Javadoc)
      * @see
      * com.mymed.controller.core.requesthandler.AbstractRequestHandler#doGet
      * (javax.servlet.http.HttpServletRequest,
      * javax.servlet.http.HttpServletResponse)
      */
     @Override
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessageOut<Object> message = new JsonMessageOut<Object>(200, this.getClass().getName());
 
         try {
             final Map<String, String> parameters = getParameters(request);
             // Check the access token
             validateToken(parameters.get(JSON_ACCESS_TKN));
 
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
             final String userID = parameters.get(JSON_USERID) != null ? parameters.get(JSON_USERID) : parameters.get(JSON_ID);
 
             if (userID == null) {
                 throw new InternalBackEndException("missing id argument!");
             }
 
             switch (code) {
                 case READ :
                     message.setMethod(JSON_CODE_READ);
                     LOGGER.info("reading {}", userID);
                     final Map<String, String> user = profileManager.readSimple(userID);
                     LOGGER.info("found {}", user);
                     message.addDataObject(JSON_USER, user);
                     break;
                 case DELETE :
                     message.setMethod(JSON_CODE_DELETE);
                     profileManager.deleteSimple(userID);
                     message.setDescription("User " + userID + " deleted");
                     LOGGER.info("User '{}' deleted", userID);
                     break;
                 default :
                     throw new InternalBackEndException("ProfileRequestHandler.doGet(" + code + ") not exist!");
             }
 
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doGet operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * com.mymed.controller.core.requesthandler.AbstractRequestHandler#doPost
      * (javax.servlet.http.HttpServletRequest,
      * javax.servlet.http.HttpServletResponse)
      */
     @Override
     protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
         final JsonMessageOut<Object> message = new JsonMessageOut<Object>(200, this.getClass().getName());
         try {
             final Map<String, String> parameters = getParameters(request);
             // Check the access token
             validateToken(parameters.get(JSON_ACCESS_TKN));
 
             final RequestCode code = REQUEST_CODE_MAP.get(parameters.get(JSON_CODE));
             final String user = parameters.get(JSON_USER), temporary = parameters.get("temporary");
 
             if (user == null) {
                 throw new InternalBackEndException("missing user argument!");
             }
             switch (code) {
                 case CREATE :
                     message.setMethod(JSON_CODE_CREATE);
                     try {
                         LOGGER.info("User:\n", user);
                         
                         Map<String, String> usr = json_to_map(user);
                         //MUserBean userBean = gson.fromJson(user, MUserBean.class);
                         LOGGER.info("Trying to create a new user:\n {}", usr);
                         profileManager.update(usr.get("id"), usr); //@TODO check if id
                         LOGGER.info("User created!");
                         message.setDescription("User created!");
                         message.addDataObject(JSON_PROFILE, usr);
                     } catch (final JsonSyntaxException e) {
                         throw new InternalBackEndException("user jSon format is not valid");
                     }
                     break;
                 case UPDATE :
                     message.setMethod(JSON_CODE_UPDATE);
                     LOGGER.info("WTF -------------------");
                     if (user.startsWith("{")) {
                     	LOGGER.info("HELLO IM THERE");
                     	Map<String, String> usr = json_to_map(user);
                     	
                     	if (usr.containsKey("socialNetworkName")){ //append all possible providers for knowing when merges has be done
                     		String ssn = "";
                     		try{
                     			ssn = profileManager.readSimpleField(usr.get("id"), "socialNetworkName");
                     		}catch (IOBackEndException e) {
                     		}
                     		if (!ssn.contains(usr.get("socialNetworkName"))){
                     			usr.put("socialNetworkName", ssn+" "+usr.get("socialNetworkName"));
                     		}
                     	}
                         LOGGER.info("Trying to update user:\n {}", usr);
                        profileManager.update(usr.get("id"), usr, temporary == null);
     
                         //message.addDataObject(JSON_PROFILE, userBean);
                         message.setDescription("User updated!");
                         LOGGER.info("User updated!");
                         
                     } else { // one field update, should be removed now
                     	LOGGER.info("HELLO IM ");
                     	
                     	String key = parameters.get("key");
                     	String value = parameters.get("value");
                     	if (key == null) {
                             throw new InternalBackEndException("missing key argument!");
                         }
                         LOGGER.info("Trying to update user {} with {}",user, key+"="+value );
                         profileManager.update(user, key, value);
                         message.setDescription("User "+user+" updated: "+key+"="+value);
                         LOGGER.info("User updated!");
                     }
                     break;
                 default :
                     throw new InternalBackEndException("ProfileRequestHandler.doPost(" + code + ") not exist!");
             }
 
         } catch (final AbstractMymedException e) {
             LOGGER.debug("Error in doPost operation", e);
             message.setStatus(e.getStatus());
             message.setDescription(e.getMessage());
         }
 
         printJSonResponse(message, response);
     }
 }
