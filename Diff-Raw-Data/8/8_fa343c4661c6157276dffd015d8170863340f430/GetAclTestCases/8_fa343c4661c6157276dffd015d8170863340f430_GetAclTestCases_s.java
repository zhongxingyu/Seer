 package org.mule.module.cmis.automation.testcases;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
 
 import org.apache.chemistry.opencmis.client.api.CmisObject;
 import org.apache.chemistry.opencmis.client.api.ObjectId;
 import org.apache.chemistry.opencmis.commons.data.Acl;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 
 public class GetAclTestCases extends CMISTestParent {
 	
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (HashMap<String, Object>) context.getBean("getAcl");
 			testObjects.put("parentObjectId", rootFolderId());
 			ObjectId result = createFolder((String) testObjects.get("folderName"), (String) testObjects.get("parentObjectId"));
 			
 			testObjects.put("objectId", result.getId());
 			testObjects.put("cmisObjectRef", getObjectById(result.getId()));
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testGetAcl() {
 		try {
 			Acl result = getAcl((CmisObject) testObjects.get("cmisObjectRef"), (String) testObjects.get("objectId"));
 			assertNotNull(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
	// If this test works then this jira is resolved: https://www.mulesoft.org/jira/browse/CLDCONNECT-1046
 	public void testGetAcl_null_cmisObject() {
 		try {
 			Acl result = getAcl(null, (String) testObjects.get("objectId"));
 			assertNotNull(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}				
 	
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testGetAcl_with_cmisObjectRef() {
 		try {
 			Acl result = getAcl(lookupFlowConstruct("get-acl-with-cmis-object-ref"), testObjects, (String) testObjects.get("objectId"));
 			assertNotNull(result);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			delete((CmisObject) testObjects.get("cmisObjectRef"), (String) testObjects.get("objectId"), true);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 }
