 /*
  * Copyright (c) 2012 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.google.drive.samples.dredit;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 
 import com.gmail.at.zhuikov.aleksandr.driveddoc.servlet.AuthorizationServlet;
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.googleapis.json.GoogleJsonResponseException;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.services.drive.Drive;
 import com.google.api.services.oauth2.Oauth2;
 import com.google.gson.Gson;
 
 /**
  * Abstract servlet that sets up credentials and provides some convenience
  * methods.
  *
  * @author vicfryzel@google.com (Vic Fryzel)
  * @author jbd@google.com (Burcu Dogan)
  */
 @SuppressWarnings("serial")
 public abstract class DrEditServlet extends AuthorizationServlet {
 	
 	
 	public DrEditServlet(JsonFactory jsonFactory) {
 		super(jsonFactory);
 	}
 
 /**
    * Default transportation layer for Google Apis Java client.
    */
   protected static final HttpTransport TRANSPORT = new NetHttpTransport();
   
   /**
    * Key to get/set userId from and to the session.
    */
   public static final String KEY_SESSION_USERID = "user_id";
 
   /**
    * Default MIME type of files created or handled by DrEdit.
    * This is also set in the Google APIs Console under the Drive SDK tab.
    */
   public static final String DEFAULT_MIMETYPE = "text/plain";
 
   @Inject
   public CredentialManager credentialManager;
   
   /**
    * Dumps the given object as JSON and responds with given HTTP status code.
    * @param resp  Response object.
    * @param code  HTTP status code to respond with.
    * @param obj   An object to be dumped as JSON.
    */
   protected void sendJson(HttpServletResponse resp, int code, Object obj) {
     try {
       // TODO(burcud): Initialize Gson instance for once.
       resp.setContentType("application/json");
       resp.setCharacterEncoding("utf-8");
       resp.getWriter().print(new Gson().toJson(obj).toString());
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
   }
 
   /**
    * Dumps the given object to JSON and responds with HTTP 200.
    * @param resp  Response object.
    * @param obj   An object to be dumped as JSON.
    */
   protected void sendJson(HttpServletResponse resp, Object obj) {
     sendJson(resp, 200, obj);
   }
 
   /**
    * Responds with the given HTTP status code and message.
    * @param resp  Response object.
    * @param code  HTTP status code to respond with.
    * @param message Message body.
    */
 	protected void sendError(HttpServletResponse resp, int code, String message) {
 		try {
 			resp.setStatus(code);
 			resp.getWriter().write(message);
 		} catch (IOException e) {
 			throw new RuntimeException(message);
 		}
 	}
 
 	/**
 	 * Transforms a GoogleJsonResponseException to an HTTP response.
 	 * 
 	 * @param resp
 	 *            Response object.
 	 * @param e
 	 *            Exception object to transform.
 	 */
 	protected void sendGoogleJsonResponseError(HttpServletResponse resp,
 			GoogleJsonResponseException e) {
		
 		resp.setContentType("application/json");
 		resp.setCharacterEncoding("utf-8");
 		sendError(resp, e.getStatusCode(), e.getLocalizedMessage());
 	}
 
   /**
    * Build and return a Drive service object based on given request parameters.
    * @param credential User credentials.
    * @return Drive service object that is ready to make requests, or null if
    *         there was a problem.
    */
   protected Drive getDriveService(Credential credential) {
     return new Drive.Builder(TRANSPORT, jsonFactory, credential)
     	.setApplicationName("Drive DigiDoc").build();
   }
 
   /**
    * Build and return an Oauth2 service object based on given request parameters.
    * @param credential User credentials.
    * @return Drive service object that is ready to make requests, or null if
    *         there was a problem.
    */
   protected Oauth2 getOauth2Service(Credential credential) {
     return new Oauth2.Builder(TRANSPORT, jsonFactory, credential)
     	.setApplicationName("Drive DigiDoc").build();
   }
 }
