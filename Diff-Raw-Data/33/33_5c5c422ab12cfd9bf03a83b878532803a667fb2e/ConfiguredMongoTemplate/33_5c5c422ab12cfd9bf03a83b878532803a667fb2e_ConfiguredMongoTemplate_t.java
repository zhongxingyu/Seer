 package com.github.dansmithy.sanjuan.dao.mongo;
 
 import java.net.UnknownHostException;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.springframework.data.document.mongodb.MongoTemplate;
 
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 @Named
 public class ConfiguredMongoTemplate extends MongoTemplate {
 
 	@Inject
 	public ConfiguredMongoTemplate(AutoMongoDetails mongoDetails) throws UnknownHostException, MongoException {
		super(new Mongo(mongoDetails.getHost(), mongoDetails.getPort()), mongoDetails.getDatabase(), mongoDetails.createUserCredentials());
 	}
 
 }
