 package com.ijg.darklight.sdk.web.api;
 
 public enum APIEndpoint {
 	AUTH		("/api/auth"),
 	UPDATE		("/api/update"),
	INDIVIDUAL 	("/session/individual/"),
	TEAM		("/session/team/");
 	
 	private String endpoint;
 	
 	private APIEndpoint(String endpoint) {
 		this.endpoint = endpoint;
 	}
 	
 	public String endpoint() {
 		return endpoint;
 	}
 }
