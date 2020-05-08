 package org.mule.module.magento.automation.testcases;
 
import static org.junit.Assert.*;
 import static org.junit.Assert.fail;
 
 import java.util.HashMap;
import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 
import com.magento.api.CatalogProductEntity;

 public class CreateProductTestCases extends MagentoTestParent {
 
 	@SuppressWarnings("unchecked")
 	@Before
 	public void setUp() {
 		try {
 			testObjects = (HashMap<String, Object>) context.getBean("createProduct");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({SmokeTests.class, RegressionTests.class})
 	@Test
 	public void testCreateProduct() {
 		try {
 			MessageProcessor flow = lookupFlowConstruct("create-product");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			Integer productId = (Integer) response.getMessage().getPayload();
 			assertNotNull(productId);
 			
 			testObjects.put("productId", productId);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			int productId = (Integer) testObjects.get("productId");
 			deleteProduct(productId);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 }
