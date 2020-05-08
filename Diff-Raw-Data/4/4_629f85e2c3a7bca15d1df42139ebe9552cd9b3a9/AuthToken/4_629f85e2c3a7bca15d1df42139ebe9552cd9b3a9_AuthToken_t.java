 package com.bewkrop.auth.web.api;
 
 public class AuthToken {
 	
 	private String key;
 	private boolean authenticated;
 	
	public AuthToken() {
		// empty
	}
	
 	public AuthToken(String key, boolean authenticated) {
 		this.key = key;
 		this.authenticated = authenticated;
 	}
 	
 	public String getKey() {
 		return key;
 	}
 	public void setKey(String key) {
 		this.key = key;
 	}
 	public boolean isAuthenticated() {
 		return authenticated;
 	}
 	public void setAuthenticated(boolean authenticated) {
 		this.authenticated = authenticated;
 	}
 
 }
