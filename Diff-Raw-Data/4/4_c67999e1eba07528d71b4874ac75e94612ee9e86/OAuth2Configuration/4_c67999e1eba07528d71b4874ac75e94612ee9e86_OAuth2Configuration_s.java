 package com.brif.nix.oauth2;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import com.google.api.client.json.jackson2.JacksonFactory;
 
 public class OAuth2Configuration extends HashMap<String, String> {
 
 	private static final long serialVersionUID = 1L;
 
	private static String google_config_production = "{\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"client_secret\":\"7hV6CnQ-etu-QWLHXixoRwjB\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"client_email\":\"808248997275-ou0vtokaht54knr34697a1epd5m0j5rf@developer.gserviceaccount.com\",\"redirect_uris\":[\"http://api.brif.us/auth/signin\"],\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/808248997275-ou0vtokaht54knr34697a1epd5m0j5rf@developer.gserviceaccount.com\",\"client_id\":\"808248997275-ou0vtokaht54knr34697a1epd5m0j5rf.apps.googleusercontent.com\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"javascript_origins\":\"http://staging.brif.us\"}";
	private static String google_config_staging = "{\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"client_secret\":\"iCesKUB5OyjwnnCaKstAuZx4\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"client_email\":\"808248997275-ol6kol8h23j018iug3d5odi9vhrja9j5@developer.gserviceaccount.com\",\"redirect_uris\":\"http://api.brif.us/auth/signin\",\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/808248997275-ol6kol8h23j018iug3d5odi9vhrja9j5@developer.gserviceaccount.com\",\"client_id\":\"808248997275-ol6kol8h23j018iug3d5odi9vhrja9j5.apps.googleusercontent.com\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"javascript_origins\":\"http://staging.brif.us\"}";
 	private static String google_config_localhost = "{\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"client_secret\":\"CFDWd5gcdOzxCmsm5uTscTFh\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"client_email\":\"808248997275@developer.gserviceaccount.com\",\"redirect_uris\":\"http://api.brif.us/auth/signin\",\"client_x509_cert_url\":\"https://www.googleapis.com/robot/v1/metadata/x509/\",\"client_id\":\"808248997275.apps.googleusercontent.com\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"javascript_origins\":\"http://localhost\"}";
 	private static String google_config_ios = "{\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"client_secret\":\"Ucajgf8BPN4fXajscWXLdZ85\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"client_email\":\"\",\"redirect_uris\":\"urn:ietf:wg:oauth:2.0:oob\",\"oob\",\"client_x509_cert_url\":\"\",\"client_id\":\"808248997275-td1l666khkenuda7irdhr27ullu7svps.apps.googleusercontent.com\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\"}";
 	
 
 	// TODO switch with enum???
 	private final static OAuth2Configuration ios = new OAuth2Configuration(
 			"ios", google_config_ios);
 	private final static OAuth2Configuration staging = new OAuth2Configuration(
 			"staging", google_config_staging);
 	private final static OAuth2Configuration production = new OAuth2Configuration(
 			"staging", google_config_production);
 	private final static OAuth2Configuration localhost = new OAuth2Configuration(
 			"localhost", google_config_localhost);
 
 	public static final OAuth2Configuration getConfiguration(String forOrigin) {
 		if ("ios".equals(forOrigin))
 			return ios;
 		if ("localhost".equals(forOrigin))
 			return localhost;
 		if ("staging".equals(forOrigin))
 			return staging;
 		if ("production".equals(forOrigin))
 			return production;
 		throw new IllegalArgumentException("Unknown origin " + forOrigin);
 	}
 
 	public OAuth2Configuration(String name, String data) {
 		JacksonFactory f = new JacksonFactory();
 		try {
 			f.createJsonParser(data).parseAndClose(this);
 		} catch (IOException e) {
 			return;
 		}
 	}
 
 }
