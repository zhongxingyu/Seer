 package com.operativus.senacrs.audit.properties.xpath;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.properties.PropertyKey;
 
 public class XPathCentralTest {
 
 	private static enum TestMessagesEnum implements PropertyKey {
 
 		TEST_XPATH_PROPERTIES("test.xpath.properties"), ;
 
 		private final String key;
 
 		private TestMessagesEnum(final String key) {
 
 			this.key = key;
 		}
 
 		@Override
 		public String getKey() {
 
 			return this.key;
 		}
 
 	}
 
 	private static final String TEST_XPATH = "//xpath/test";
 	private static final PropertyKey[] PRE_LOADED_KEYS = new PropertyKey[] {
 			TestMessagesEnum.TEST_XPATH_PROPERTIES,
 	};
 
 	@Test
 	public void testGetMessagePreLoaded() {
 
 		String result = null;
 
 		for (PropertyKey k : PRE_LOADED_KEYS) {
 			result = XPathCentral.getXPath(k);
 			Assert.assertEquals(TEST_XPATH, result);
 		}
 	}
 }
