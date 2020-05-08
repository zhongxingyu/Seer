 package org.mule.module.cmis.automation.testcases;
 
 import static org.junit.Assert.fail;
 
 import java.util.Map;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 import org.mule.module.cmis.VersioningState;
 
 public class UpdateObjectPropertiesTestCases extends CMISTestParent {
 
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (Map<String, Object>) context.getBean("updateObjectProperties");
 			
 			String rootFolderId = rootFolderId();
 			String filename = testObjects.get("filename").toString();
 			String mimeType = testObjects.get("mimeType").toString();
 			String content = testObjects.get("content").toString();
 			String objectType = testObjects.get("objectType").toString();
 			Map<String, Object> propertiesRef = (Map<String, Object>) testObjects.get("propertiesRef");
 			VersioningState versioningState = (VersioningState) testObjects.get("versioningState");
 			
 			ObjectId documentObjectId = createDocumentById(rootFolderId, filename, content, mimeType, versioningState, objectType, propertiesRef);
 			testObjects.put("documentObjectId", documentObjectId);
 			testObjects.put("objectId", documentObjectId.getId());
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({RegressionTests.class})
 	@Test
 	public void testUpdateObjectProperties() {
 		try {
 			Map<String, Object> updatedProperties = (Map<String, Object>) testObjects.get("propertiesRefUpdated");
 			testObjects.put("propertiesRef", updatedProperties);
 			
 			MessageProcessor flow = lookupFlowConstruct("update-object-properties");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			CmisObject cmisObject = (CmisObject) response.getMessage().getPayload();
			System.out.println(cmisObject);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			String objectId = (String) testObjects.get("objectId");
 			delete(getObjectById(objectId), objectId, true);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 }
