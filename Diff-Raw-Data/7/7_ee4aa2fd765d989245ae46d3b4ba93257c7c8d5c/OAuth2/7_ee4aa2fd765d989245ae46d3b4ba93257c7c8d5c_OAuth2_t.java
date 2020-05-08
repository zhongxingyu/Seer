 /*
  *  @author Daniel Strebel
  *  
  *  Copyright 2012 University of Zurich
  *      
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *  
  *         http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *  
  */
 
 package com.signalcollect.google.spreadsheet;
 
 import java.io.File;
import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import com.google.api.client.auth.oauth2.Credential;
 import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
 import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore;
 import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
 import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
 import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
 import com.google.api.client.http.HttpTransport;
 import com.google.api.client.http.javanet.NetHttpTransport;
 import com.google.api.client.json.JsonFactory;
 import com.google.api.client.json.jackson2.JacksonFactory;
 
 
 public class OAuth2 {
 
 	/**
 	 * Config
 	 */
 	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
 	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
 
 	/**
 	 * Scopes used to write to Google spreadsheets
 	 */
 	private static final List<String> SCOPES = Arrays.asList(
 			"https://spreadsheets.google.com/feeds",
 			"https://docs.google.com/feeds");
 
 	private static GoogleClientSecrets clientSecrets;
 
 	private static Credential authorize() throws Exception {
 
 		clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,
 				OAuth2.class.getResourceAsStream("/client_secrets.json"));
 
 		//Check if json with client secrets is present
 		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
 				|| clientSecrets.getDetails().getClientSecret()
 						.startsWith("Enter ")) {
 			System.err
 					.println("Enter Client ID and Secret from https://code.google.com/apis/console/ "
 							+ "into client_secrets.json");
 			System.exit(1);
 		}
 
 
 		FileCredentialStore credentialStore = new FileCredentialStore(new File(
 				System.getProperty("user.home"), ".credentials/oauth2.json"),
 				JSON_FACTORY);
 
 		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
 				HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
 				.setCredentialStore(credentialStore).build();
 
 		return new AuthorizationCodeInstalledApp(flow,
 				new LocalServerReceiver()).authorize("user");
 	}
 
 	/**
 	 * Creates an access token and stores it locally
 	 */
 	public void authorizeAccess() {
 		try {
 			authorize();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getAccessToken() {
 		Credential credential = null;
 		try {
 			credential = authorize();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
		try {
			credential.refreshToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
 		return credential.getAccessToken();
 	}
 
 	public static void main(String[] args) {
 		System.out.println(new OAuth2().getAccessToken());		
 		System.exit(1);
 	}
 }
