 package edu.msu.nscl.olog.api;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
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
 	private static LogbookBuilder validLogbook2 = logbook("Valid Logbook2");
 	private static TagBuilder validTag = tag("Valid Tag");
 	private static PropertyBuilder validProperty = property("Valid Property")
 			.attribute("Valid Attribute");
 	private static Log validLog;
 
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
 			client.set(validLogbook2.owner("test"));
 			client.set(validProperty);
 			validLog = client.set(log()
 					.description("Valid Log Entry for error condition tests.")
 					.appendToLogbook(validLogbook).level("Info"));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@AfterClass
 	public static void tearDown() {
 		client.deleteTag(validTag.build().getName());
 		client.deleteLogbook(validLogbook.build().getName());
 		client.deleteLogbook(validLogbook2.build().getName());
 		client.deleteProperty(validProperty.build().getName());
 		client.delete(validLog.getId());
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
 
 	@Test(expected = OlogException.class)
 	public void setTagOnInvalidLog() {
 		Collection<Long> logIds = new ArrayList<Long>();
 		logIds.add(validLog.getId());
 		logIds.add(12345L);
 		client.set(validTag, logIds);
 		Log queryLog = client.getLog(validLog.getId());
 		assertTrue(
 				"invalid request to add tag to logs partially executed, unexpected modification of validLog",
 				validLog.equals(queryLog));
 	}
 
 	@Test(expected = OlogException.class)
 	public void setInvalidTagOnLog() {
 		client.update(inValidTag, validLog.getId());
 		assertTrue("invalid request to add invalid tag to log",
 				validLog.equals(client.getLog(validLog.getId())));
 	}
 
 	@Test(expected = OlogException.class)
 	public void setLogbookOnInvalidLog() {
 		Collection<Long> logIds = new ArrayList();
 		logIds.add(validLog.getId());
 		logIds.add(12345L);
 		client.set(validLogbook2, logIds);
 		Log queryLog = client.getLog(validLog.getId());
 		assertTrue(
 				"invalid request to add logbook2 to logs partially executed, unexpected modification of validLog",
 				validLog.equals(queryLog));
 	}
 
 	@Test(expected = OlogException.class)
 	public void deleteNonExistingLog() {
 		client.delete(1234567890L);
 	}
 
 	/**
 	 * Delete a tag from a log which does not have it
 	 */
 	@Test(expected = OlogException.class)
 	public void deleteNonExistingTagFromLog() {
 		assertTrue("Tag is present on the log", client.getLog(validLog.getId())
 				.getTags() == null
 				|| !client.getLog(validLog.getId()).getTags()
 						.contains(validTag));
 		client.delete(validTag, validLog.getId());
 	}
 
 	/**
 	 * Delete a Logbook which is not attached to a log entry
 	 */
 	@Test(expected = OlogException.class)
 	public void deleteNonExistingLogbookFromLog() {
 		assertTrue("Logbook is present on the log",
 				client.getLog(validLog.getId()).getLogbooks() == null
 						|| !client.getLog(validLog.getId()).getLogbooks()
 								.contains(validLogbook2));
 		client.delete(validLogbook2, validLog.getId());
 	}
 
	@Test (expected = OlogException.class)
 	public void deleteNonExistingPropertyFromLog() {
		assertTrue("validProperty is present on the log",
				client.getLog(validLog.getId()).getProperties() == null
						|| !client.getLog(validLog.getId()).getProperties()
								.contains(validProperty));
		client.delete(validProperty, validLog.getId());
 	}
 
 	/**
 	 * Delete the last logbook assocaited with a log entry
 	 */
 	@Test(expected = OlogException.class)
 	public void deleteOnlyLogbookFromLog() {
 		assertTrue("Logbook is present on the log",
 				client.getLog(validLog.getId()).getLogbooks().size() == 1
 						|| !client.getLog(validLog.getId()).getLogbooks()
 								.contains(validLogbook));
 		client.delete(validLogbook, validLog.getId());
 		assertTrue("Last valid Logbook is was removed from  validLog",
 				client.getLog(validLog.getId()).getLogbooks().size() == 1
 						|| !client.getLog(validLog.getId()).getLogbooks()
 								.contains(validLogbook));
 	}
 
 }
