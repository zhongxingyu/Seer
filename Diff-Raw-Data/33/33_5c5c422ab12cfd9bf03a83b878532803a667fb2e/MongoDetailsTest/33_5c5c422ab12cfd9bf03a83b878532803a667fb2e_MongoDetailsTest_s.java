 package com.github.dansmithy.sanjuan.dao.mongo;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 public class MongoDetailsTest {
 
 	@Test
 	public void testCreateFromUri() {
 		
 		MongoDetails details = MongoDetails.createFromUri("mongodb://crazy_username:longpassword@server.mongolab.com:27567/db_name");
 		MongoDetails expectedDetails = new MongoDetails().withUsername("crazy_username").withPassword("longpassword").withHost("server.mongolab.com").withPort(27567).withDatabase("db_name");
 		Assert.assertThat(details, is(equalTo(expectedDetails)));
 	}
 	
 	@Test
 	public void testCreateFromDefaultUri() {
 		
 		MongoDetails details = MongoDetails.createFromUri("mongodb://:@localhost:27017/test");
		MongoDetails expectedDetails = new MongoDetails().withUsername("").withPassword("").withHost("localhost").withPort(27017).withDatabase("test");
 		Assert.assertThat(details, is(equalTo(expectedDetails)));
 	}	
 	
 
 	@Test
 	public void testAutoMongoDetails() {
 		
 		AutoMongoDetails details = new AutoMongoDetails();
		MongoDetails expectedDetails = new MongoDetails().withUsername("").withPassword("").withHost("localhost").withPort(27017).withDatabase("test");
 		Assert.assertThat(details, is(equalTo(expectedDetails)));
 	}		
 }
