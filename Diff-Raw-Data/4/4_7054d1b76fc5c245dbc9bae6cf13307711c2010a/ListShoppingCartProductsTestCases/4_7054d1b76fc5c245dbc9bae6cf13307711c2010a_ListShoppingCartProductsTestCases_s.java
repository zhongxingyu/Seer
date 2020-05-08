 package org.mule.module.magento.automation.testcases;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.mule.api.MuleEvent;
 import org.mule.api.processor.MessageProcessor;
 
 import com.magento.api.CatalogProductCreateEntity;
 import com.magento.api.CatalogProductEntity;
 import com.magento.api.ShoppingCartProductEntity;
 
 public class ListShoppingCartProductsTestCases extends MagentoTestParent {
 

 	@Before
 	public void setUp() {
 		try {
			testObjects = (HashMap<String, Object>) context.getBean("removeShoppingCartProduct");
 
 			List<HashMap<String, Object>> productDefinitions = (List<HashMap<String, Object>>) testObjects.get("products");
 			
 			List<ShoppingCartProductEntity> shoppingCartEntities = new ArrayList<ShoppingCartProductEntity>();
 			List<Integer> productIds = new ArrayList<Integer>();
 			
 			// Iterate over each product definition and insert
 			for (HashMap<String, Object> productDefinition : productDefinitions) {
 				String productType = (String) productDefinition.get("type");
 				int productSet = (Integer) productDefinition.get("set");
 				String productSKU = (String) productDefinition.get("sku");
 				CatalogProductCreateEntity attributes = (CatalogProductCreateEntity) productDefinition.get("attributesRef");
 			
 				// Get the product ID and the number of items we want to place in the shopping cart
 				int productId = createProduct(productType, productSet, productSKU, attributes);
 				double qtyToPlace = (Double) productDefinition.get("qtyToPlace");
 				
 				productIds.add(productId);
 				
 				ShoppingCartProductEntity shoppingCartEntity = new ShoppingCartProductEntity();
 				shoppingCartEntity.setProduct_id(productId + "");
 				shoppingCartEntity.setQty(qtyToPlace);
 								
 				shoppingCartEntities.add(shoppingCartEntity);
 			}
 			
 			// Create the shopping cart
 			int shoppingCartId = createShoppingCart();
 			addProductsToShoppingCart(shoppingCartId, shoppingCartEntities);
 			
 			testObjects.put("quoteId", shoppingCartId);
 			testObjects.put("shoppingCartEntities", shoppingCartEntities);
 			testObjects.put("productIds", productIds);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@Category({RegressionTests.class})
 	@Test
 	public void testListShoppingCartProduct() {
 		try {
 			List<ShoppingCartProductEntity> products = (List<ShoppingCartProductEntity>) testObjects.get("shoppingCartEntities");
 			testObjects.put("productsRef", products);
 						
 			MessageProcessor flow = lookupFlowConstruct("list-shopping-cart-products");
 			MuleEvent response = flow.process(getTestEvent(testObjects));
 			
 			List<CatalogProductEntity> shoppingCartProducts = (List<CatalogProductEntity>) response.getMessage().getPayload();
 			assertTrue(shoppingCartProducts.size() == products.size());
 			
 			for (ShoppingCartProductEntity shoppingCartEntity : products) {
 				boolean inList = MagentoTestHelper.isProductInShoppingCart(shoppingCartProducts, shoppingCartEntity);
 				assertTrue(inList);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 	@After
 	public void tearDown() {
 		try {
 			List<Integer> productIds = (List<Integer>) testObjects.get("productIds");
 			for (Integer productId : productIds) {
 				deleteProductById(productId);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 	
 }
