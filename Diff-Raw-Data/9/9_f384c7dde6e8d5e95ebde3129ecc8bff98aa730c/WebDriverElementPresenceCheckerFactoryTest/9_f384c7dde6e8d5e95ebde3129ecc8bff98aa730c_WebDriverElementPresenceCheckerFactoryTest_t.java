 package com.operativus.senacrs.audit.graph.nodes.webdriver.checkers;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.properties.xpath.XPathPrefixesEnum;
 
 public class WebDriverElementPresenceCheckerFactoryTest {
 
 	@Test
 	public void testCreateCheckerAllTypes() {
 
 		WebDriverElementPresenceChecker result = null;
 
 		for (XPathPrefixesEnum t : XPathPrefixesEnum.values()) {
			try {
				result = WebDriverElementPresenceCheckerFactory.createChecker(t);
				Assert.assertNotNull(result);
				Assert.assertEquals(0, t.paramAmount());
			} catch (IllegalArgumentException e) {
				Assert.assertTrue(t.paramAmount() > 0);
			}
 		}
 	}
 
 	@Test
 	public void testCreateCheckerNull() {
 
 		try {
 			WebDriverElementPresenceCheckerFactory.createChecker(null);
 			Assert.fail();
 		} catch (IllegalArgumentException e) {
 			Assert.assertTrue(true);
 		}
 	}
 }
