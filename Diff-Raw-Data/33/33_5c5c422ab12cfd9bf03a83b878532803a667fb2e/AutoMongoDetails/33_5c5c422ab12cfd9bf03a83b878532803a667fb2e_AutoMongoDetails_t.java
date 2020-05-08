 package com.github.dansmithy.sanjuan.dao.mongo;
 
 import javax.inject.Named;
 
import org.springframework.data.authentication.UserCredentials;

 @Named
 public class AutoMongoDetails extends MongoDetails {
 
 	private static final String DEFAULT_URI = "mongodb://:@localhost:27017/test";
 	private static final String ENVIRONMENT_KEY = "MONGOLAB_URI";
 	
 	public AutoMongoDetails() {
 		String uri = getUri();
 		configureFromUri(this, uri);
 	}
 
 	private String getUri() {
 		String environmentValue = System.getenv(ENVIRONMENT_KEY);
 		if (environmentValue != null) {
 			return environmentValue;
 		} else {
 			return DEFAULT_URI;
 		}
 	}
 
	public UserCredentials createUserCredentials() {
		return new UserCredentials(this.getUsername(), this.getPassword());
	}

 }
 
