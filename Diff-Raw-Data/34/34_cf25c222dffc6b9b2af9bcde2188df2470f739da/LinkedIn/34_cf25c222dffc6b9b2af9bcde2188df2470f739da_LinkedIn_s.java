 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.coffeeandpower.linkedin;
 
 import java.util.EnumSet;
 
 import android.content.Intent;
 import android.net.Uri;
 
 import com.coffeeandpower.AppCAP;
 import com.coffeeandpower.inter.OAuthService;
 import com.google.code.linkedinapi.client.LinkedInApiClient;
 import com.google.code.linkedinapi.client.LinkedInApiClientFactory;
 import com.google.code.linkedinapi.client.oauth.LinkedInAccessToken;
 import com.google.code.linkedinapi.client.oauth.LinkedInOAuthService;
 import com.google.code.linkedinapi.client.oauth.LinkedInOAuthServiceFactory;
 import com.google.code.linkedinapi.client.oauth.LinkedInRequestToken;
 import com.google.code.linkedinapi.schema.Person;
 
 /**
  * 
  * @author jrojas
  */
 public class LinkedIn implements OAuthService {
 
 	LinkedInOAuthService oAuthService;
 	LinkedInApiClientFactory factory;
 	LinkedInApiClient client;
 	LinkedInRequestToken liToken;
 	LinkedInAccessToken accessToken;
 	String apiKey;
 	String apiSec;
 
 	Person currUser;
 
 	public static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-linkedin";
 	public static final String OAUTH_CALLBACK_HOST = "callback";
 	public static final String OAUTH_CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;
 
 	public String getServiceNameSignUp() {
 		return "linkedin";
 	}
 
 	public String getServiceNameLogin() {
 		return "Linkedin";
 	}
 
 	public boolean isConnected() {
 		return client != null && accessToken != null;
 	}
 
 	public void initialize(String apiKey_, String apiSec_) {
 		apiKey = apiKey_;
 		apiSec = apiSec_;
 		// create service & factory with keys from LinkedIn developer
 		oAuthService = LinkedInOAuthServiceFactory.getInstance().createLinkedInOAuthService(apiKey, apiSec);
 		factory = LinkedInApiClientFactory.newInstance(apiKey, apiSec);
 	}
 
 	public Intent authorize() {
 		accessToken = null;
 		client = null;
 		try {
 			liToken = oAuthService.getOAuthRequestToken(OAUTH_CALLBACK_URL); // on
 											 // this
 											 // line
 			return new Intent(Intent.ACTION_VIEW, Uri.parse(liToken.getAuthorizationUrl()));
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public boolean verify(String verifier) {
 		accessToken = oAuthService.getOAuthAccessToken(liToken, verifier);
 		client = factory.createLinkedInApiClient(accessToken);
 		// client.postNetworkUpdate("LinkedIn Android app test, token = "
 		// +
 		// accessToken.getToken());
 		if (client != null)
 			currUser = client.getProfileForCurrentUser(EnumSet.of(
				com.google.code.linkedinapi.client.enumeration.ProfileField.ID, com.google.code.linkedinapi.client.enumeration.ProfileField.FIRST_NAME,
				com.google.code.linkedinapi.client.enumeration.ProfileField.LAST_NAME, com.google.code.linkedinapi.client.enumeration.ProfileField.HEADLINE,
				com.google.code.linkedinapi.client.enumeration.ProfileField.INDUSTRY, com.google.code.linkedinapi.client.enumeration.ProfileField.PICTURE_URL,
				com.google.code.linkedinapi.client.enumeration.ProfileField.DATE_OF_BIRTH, com.google.code.linkedinapi.client.enumeration.ProfileField.LOCATION_NAME,
				com.google.code.linkedinapi.client.enumeration.ProfileField.MAIN_ADDRESS, com.google.code.linkedinapi.client.enumeration.ProfileField.LOCATION_COUNTRY)
 				);
 
 		return client != null && accessToken.getToken() != null;
 	}
 
 	public boolean reconnect(String token, String tokenSecret) {
 		try {
 			accessToken = new LinkedInAccessToken(token, tokenSecret);
 			client = factory.createLinkedInApiClient(accessToken);
 			if (client != null)
//				currUser = client.getProfileForCurrentUser(EnumSet
//						.allOf(com.google.code.linkedinapi.client.enumeration.ProfileField.class));
 				currUser = client.getProfileForCurrentUser(EnumSet.of(
					com.google.code.linkedinapi.client.enumeration.ProfileField.ID, com.google.code.linkedinapi.client.enumeration.ProfileField.FIRST_NAME,
					com.google.code.linkedinapi.client.enumeration.ProfileField.LAST_NAME, com.google.code.linkedinapi.client.enumeration.ProfileField.HEADLINE,
					com.google.code.linkedinapi.client.enumeration.ProfileField.INDUSTRY, com.google.code.linkedinapi.client.enumeration.ProfileField.PICTURE_URL,
					com.google.code.linkedinapi.client.enumeration.ProfileField.DATE_OF_BIRTH, com.google.code.linkedinapi.client.enumeration.ProfileField.LOCATION_NAME,
					com.google.code.linkedinapi.client.enumeration.ProfileField.MAIN_ADDRESS, com.google.code.linkedinapi.client.enumeration.ProfileField.LOCATION_COUNTRY)
 					);
 		} catch (Exception e) {
 
 		}
 
 		return client != null && accessToken.getToken() != null;
 	}
 
 	public String getAccessToken() {
 		return accessToken.getToken();
 	}
 
 	public String getAccessTokenSecret() {
 		return accessToken.getTokenSecret();
 	}
 
 	public String getUserId() {
 		if (currUser == null)
 			return null;
 		return currUser.getId();
 	}
 
 	public String getUserName() {
 		return getUserId() + "@linkedin.com";
 	}
 
 	public String getUserNickName() {
 		return currUser.getFirstName() + " " + currUser.getLastName();
 	}
 
 	public String getUserPassword() {
 		return getAccessTokenSecret();
 	}
 
 	public void saveSettings() {
 		if (client == null)
 			return;
 		AppCAP.setUserLinkedInDetails(getAccessToken(), getAccessTokenSecret(), getUserId());
 	}
 
 	public void clearSettings() {
 		AppCAP.setUserLinkedInDetails("", "", "");
 	}
 }
