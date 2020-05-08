 package com.reckonlabs.reckoner.contentservices.utility;
 
 public class ServiceProps {
 	
 	boolean enableServiceAuthentication;
 	int votePersistenceSize;
 	String defaultBio;
 
 	String googleAppId;
 	String googleAppSecret;
 
 	public boolean isEnableServiceAuthentication() {
 		return enableServiceAuthentication;
 	}
 
 	public void setEnableServiceAuthentication(boolean enableServiceAuthentication) {
 		this.enableServiceAuthentication = enableServiceAuthentication;
 	}
 
 	public int getVotePersistenceSize() {
 		return votePersistenceSize;
 	}
 
 	public void setVotePersistenceSize(int votePersistenceSize) {
 		this.votePersistenceSize = votePersistenceSize;
 	}
 
 	public String getDefaultBio() {
 		return defaultBio;
 	}
 
 	public void setDefaultBio(String defaultBio) {
 		this.defaultBio = defaultBio;
 	}
 }
