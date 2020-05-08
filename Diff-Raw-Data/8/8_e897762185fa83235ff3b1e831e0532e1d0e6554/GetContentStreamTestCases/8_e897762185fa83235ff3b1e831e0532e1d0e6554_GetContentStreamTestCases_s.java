 package org.mule.module.cmis.automation.testcases;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.apache.chemistry.opencmis.commons.data.ContentStream;
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.module.cmis.VersioningState;
 
 public class GetContentStreamTestCases extends CMISTestParent {
 
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (HashMap<String, Object>) context
 					.getBean("getContentStream");
 			ObjectId result = createDocumentById(rootFolderId(),
 					(String) testObjects.get("filename"),
 					(String) testObjects.get("content"),
 					(String) testObjects.get("mimeType"),
 					(VersioningState) testObjects.get("versioningState"),
 					(String) testObjects.get("objectType"),
 					(Map<String, Object>) testObjects.get("propertiesRef"));
 
 			testObjects.put("objectId", result.getId());
 			testObjects.put("cmisObjectRef", getObjectById(result.getId()));
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({ RegressionTests.class })
 	@Test
 	public void testGetContentStream() {
 		try {
 			ContentStream result = getContentStream(
 					(CmisObject) testObjects.get("cmisObjectRef"),
 					(String) testObjects.get("objectId"));
 
 			assertNotNull(result);
 
 			String content = IOUtils.toString(result.getStream());
 			assertEquals((String) testObjects.get("content"), content);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({ RegressionTests.class })
 	@Test
	// test passes when following jira is resolved: https://www.mulesoft.org/jira/browse/CLDCONNECT-1051
 	public void testGetContentStream_HashMap_payload() {
 		try {
 			ContentStream result = getContentStream(
 					lookupFlowConstruct("get-content-stream-no-cmis-object-ref"),
 					testObjects,
 					(String) testObjects.get("objectId"));
 
 			assertNotNull(result);
 
 			String content = IOUtils.toString(result.getStream());
 			assertEquals((String) testObjects.get("content"), content);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({ RegressionTests.class })
 	@Test
 	public void testGetContentStream_assert_cmisObject_ref_is_valid_attribute() {
 		try {
 			ContentStream result = getContentStream(
 					lookupFlowConstruct("get-content-stream-with-cmis-object-ref"),
 					testObjects,
 					(String) testObjects.get("objectId"));
 
 			assertNotNull(result);
 
 			String content = IOUtils.toString(result.getStream());
 			assertEquals((String) testObjects.get("content"), content);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@After
 	public void tearDown() {
 		try {
 			String objectId = (String) testObjects.get("objectId");
 			delete(getObjectById(objectId), objectId, true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 }
