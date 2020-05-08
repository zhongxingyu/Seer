 /**
  * Copyright 2010-2011 apius.org
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * 
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apius.server.identity.session.openam.client;
 
 import java.io.IOException;
 
 import org.apius.server.identity.session.Session;
 import org.restlet.data.ClientInfo;
 import org.restlet.data.Form;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ClientResource;
 import org.restlet.resource.ResourceException;
 import org.restlet.security.User;
 
 /**
  * <p>
  * This <code>ClientResource</code> implements the <code>Session</code> interface and 
  * acts as a proxy to the <a href="forgerock.com/openam.html">OpenAM</a> session 
  * provisioner, authenticator and authorizer application.
  * </p>
  * 
  * @author Paul Morris
  * 
  */
 public final class SessionProvisionerProxy extends ClientResource implements Session {
 	
     private ResponseHelper responseHelper;
     private String token;
     
     /**
      * Constructor
      * 
      * @param baseUri
      * @param responseHelper
      */
     public SessionProvisionerProxy(String baseUri, ResponseHelper responseHelper) {
         super(baseUri);
         this.responseHelper = responseHelper;
     }
     
     /**
      * Request OpenAM to create session and return token representing that session.
      * 
      * @param username
      * @param password
      * @return StringRepresentation containing the token
      */
     public Representation createSession(Form form) {
         Representation representation = null;
     	
         setReference(getReference().
                 addSegment("identity").
                 addSegment("authenticate"));
     
         // DO NOT call this service with a GET and query parameters. Elevating this to a POST
         // and sending the parameters in the entity body will prevent OpenAM from logging 
         // the sensitive username and password parameters.
         //
         // http://blogs.sun.com/docteger/entry/opensso_entitlements_service_rest_interfaces
         try {
             token = this.post(form).getText().replace("token.id=", "").trim(); //Yes, there is a white space.
             representation = new StringRepresentation(token);
         } catch (ResourceException e) {
     	    handleResourceException(e);
     	} catch (IOException e) {
     	    handleException(e.getMessage());
     	}
     	
     	return representation;
     }
     
     /**
      * Requests OpenAM to respond with all known attributes associated with the session
      * token. Uses the <code>ResponseHelper</code> object to convert the response string
      * to XML.
      * 
      * @param token
      * @return Representation of the session attributes 
      */
     public Representation getAttributes() {  
         Representation representation = null;
         
         try {
             representation = responseHelper.writeSessionAttributesFeed(getSessionAttributesResponseString());
         } catch (ResourceException e) {
             handleResourceException(e);
         }
     	
         return representation;
     }
     
     private String getSessionAttributesResponseString() {
         String responseString = "";
         
         setReference(getReference().
                 addSegment("identity").
                 addSegment("attributes").
                 addQueryParameter("subjectid", token));      
         try {
             responseString = this.get().getText();
         } catch (ResourceException e) {
             handleResourceException(e);
         } catch (IOException e) {
             handleException(e.getMessage());
         }
     	
         return responseString;
     }
     
     /**
      * Can be used to authenticate the OpenAM session represented by the token
      * as well as to refresh the time left before an inactive timeout. Updates
      * the <code>ClientInfo</code> and its <code>User</code> on successful 
      * authentication.
      * 
      * @return void
      */
     public void authenticateToken() {
         ClientInfo clientInfo = getRequest().getClientInfo();
    	 
         try {
            String identifier = this.getIdentifier();
            clientInfo.setUser(new User(identifier));
             clientInfo.setAuthenticated(true);
            getRequest().getChallengeResponse().setIdentifier(identifier);
         } catch (ResourceException e) {
             handleResourceException(e);
         }	
     }
     
     /**
      * Invalidates the session and effectively logs out the user.
      * 
      * @param token
      * @return void
      */
     public void logout() {
         setReference(getReference().
                 addSegment("identity").
                 addSegment("logout").
                 addQueryParameter("subjectid", token));
         
         try {
             this.get().getText();
         } catch (ResourceException e) {
             handleResourceException(e);
         } catch (IOException e) {
             handleException(e.getMessage());
         }
     }
     
     /**
      * Requests OpenAM to check whether its internal policies allow the user represented
      * by the session token to perform the HTTP operation against the resource addressed
      * by the URI.
      * 
      * @param uri
      * @param methodName
      * @param token
      * @return boolean indicating whether the session represented by the token is
      * 		   authorized to perform the given method on the resource at the uri 
      */
     public boolean isAuthorized(String token, String uri, String method) {
         boolean isAuthorized  = false;
     	
         setReference(getReference().
                 addSegment("identity").
                 addSegment("authorize").
                 addQueryParameter("uri", uri).
                 addQueryParameter("action", method).
                 addQueryParameter("subjectid", token));
         try {
             isAuthorized = responseHelper.extractBooleanFromResponseString(this.get().getText());
         } catch (ResourceException e) {
             handleResourceException(e);
         } catch (IOException e) {
             handleException(e.getMessage());
         }
     	
         return isAuthorized;
     }
     
     /**
      * Extracts the identifier (or username) from the response of the 
      * <code>getSessionAttributesResponseString</code> method.
      * 
      * @param token
      * @return identifier string (i.e. username)
      */
     public String getIdentifier() {
         String identifier = "";
     	
         try {
             identifier = responseHelper.extractUsernameFromSessionAttributes(getSessionAttributesResponseString());
         } catch (ResourceException e) {
             handleResourceException(e);
         }
     	
         return identifier;
     }
     
     /**
      * The <code>ServerResource</code> extracts this value from the 
      * Authorization header and sets it here so that it will be available 
      * for the <code>ClientResource</code> calls to OpenAM.
      * 
      * @param token
      */
     public void setToken(String token) {
         this.token = token;
     }
     
     private void handleResourceException(ResourceException e) {
         // If the user passes in an invalid session token to the logout service, 
         // OpenAM returns a 500. The APIUS framework prefers to send back a 401 
         // since the reality is that the user is trying to access a resource with 
         // an unauthorized credential. So we check the entity for "Invalid session 
         // ID" and if we find it we change the status code to 401 before throwing 
         // the exception back to our Session resource.
         if (getResponse().getEntityAsText().contains("Invalid session ID")) {
             e = new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED);
         }
         
         throw e;
     }
     
     private void handleException(String message) {
         handleResourceException(new ResourceException(Status.SERVER_ERROR_INTERNAL, message));
     }
 
 }
