 package edu.msu.nscl.olog.api;
 
 import static org.junit.Assert.*;
 
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import edu.msu.nscl.olog.api.OlogClientImpl.OlogClientBuilder;
 import static edu.msu.nscl.olog.api.LogBuilder.*;
 import static edu.msu.nscl.olog.api.PropertyBuilder.*;
 import static edu.msu.nscl.olog.api.TagBuilder.*;
 import static edu.msu.nscl.olog.api.LogbookBuilder.*;
 
 public class ErrorIT {
 
 	private static LogbookBuilder validLogbook = logbook("Valid Logbook");
 	private static TagBuilder validTag = tag("Valid Tag");
 	private static PropertyBuilder validProperty = property("Valid Property")
 			.attribute("Valid Attribute");
	private static LogBuilder validLog = log().description(
			"Valid Log Entry for error condition tests.").appendToLogbook(
			validLogbook);
 
 	private static LogbookBuilder inValidLogbook = logbook("InValid Logbook");
 	private static TagBuilder inValidTag = tag("InValid Tag");
 	private static PropertyBuilder inValidProperty = property("InValid Property");
 
 	private static OlogClient client;
 
 	@BeforeClass
 	public static void setup() {
 		try {
 			client = OlogClientBuilder.serviceURL()
 					.withHTTPAuthentication(true).create();
 			client.set(validTag);
 			client.set(validLogbook.owner("test"));
 			client.set(validProperty);

 		} catch (Exception e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@AfterClass
 	public static void tearDown() {
 		client.deleteTag(validTag.build().getName());
 		client.deleteLogbook(validLogbook.build().getName());
 		client.deleteProperty(validProperty.build().getName());
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogWithNoLogbook() {
 		LogBuilder log = log().description("log with no logbook").level("Info");
 		client.set(log);
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogWithNoLevel() {
 		LogBuilder log = log().description("log with no level")
 				.appendToLogbook(validLogbook);
 		client.set(log);
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogWithInvalidLogbook() {
 		LogBuilder log = log().description("log with invalid logbook")
 				.level("Info").appendToLogbook(inValidLogbook);
 		client.set(log);
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogWithInvalidTag() {
 		LogBuilder log = log().description("log with invalid tag")
 				.level("Info").appendToLogbook(validLogbook)
 				.appendTag(inValidTag);
 		client.set(log);
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogWithInvalidProperty() {
 		LogBuilder log = log().description("log with invalid property")
 				.level("Info").appendToLogbook(validLogbook)
 				.appendProperty(inValidProperty);
 		client.set(log);
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogWithInvalidAttribute() {
 		LogBuilder log = log()
 				.description("log with invalid attribute")
 				.level("Info")
 				.appendToLogbook(validLogbook)
 				.appendProperty(validProperty.attribute("invalidAttribute", ""));
 		client.set(log);
 	}
 
	@Test
 	public void setTagOnInvalidLog() {

 	}
 
 	@Test
 	public void setInvalidTagOnLog() {

 	}
 
 }
