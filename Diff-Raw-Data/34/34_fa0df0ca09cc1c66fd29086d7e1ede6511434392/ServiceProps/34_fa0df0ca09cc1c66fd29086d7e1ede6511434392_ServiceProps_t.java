 package com.reckonlabs.reckoner.contentservices.utility;
 
 public class ServiceProps {
 	
 	boolean enableServiceAuthentication;
 	int votePersistenceSize;
	int votePersistenceBytes;

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

	public int getVotePersistenceBytes() {
		return votePersistenceBytes;
	}

	public void setVotePersistenceBytes(int votePersistenceBytes) {
		this.votePersistenceBytes = votePersistenceBytes;
	}

	public String getGoogleAppId() {
		return googleAppId;
	}

	public void setGoogleAppId(String googleAppId) {
		this.googleAppId = googleAppId;
	}

	public String getGoogleAppSecret() {
		return googleAppSecret;
	}

	public void setGoogleAppSecret(String googleAppSecret) {
		this.googleAppSecret = googleAppSecret;
	}
 }
