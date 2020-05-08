 package com.operativus.senacrs.audit.properties.xpath;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.operativus.senacrs.audit.properties.PropertyKey;
 
 public class XPathCentralTest {
 
 	private static enum TestXPathEnum implements PropertyKey {
 
 		TEST_XPATH_PROPERTIES("test.xpath.properties"), ;
 
 		private final String key;
 
 		private TestXPathEnum(final String key) {
 
 			this.key = key;
 		}
 
 		@Override
 		public String getKey() {
 
 			return this.key;
 		}
 
 	}
 
 	private static enum TestXPathPrefixEnum implements XPathKeyPrefix {
 
 		XPATH_TEST("xpath.test"),
 		NONE("none.none"),
 		;
 
 		private final String keyPrefix;
 
 		private TestXPathPrefixEnum(final String keyPrefix) {
 
 			this.keyPrefix = keyPrefix;
 		}
 
 		@Override
 		public String getKeyPrefix() {
 
 			return this.keyPrefix;
 		}
 	}
 
 	private static final String XPATH_TEST = "//xpath/test";
 	private static final PropertyKey[] PRE_LOADED_KEYS = new PropertyKey[] {
 			TestXPathEnum.TEST_XPATH_PROPERTIES,
 	};
 	private static final String[] XPATH_TEST_PREFIX_CONTENT = new String[] {
 		"//xpath/path1",
 		"//xpath/path2",
 	};
 
 	@Test
 	public void testGetMessagePreLoaded() {
 
 		String result = null;
 
 		for (PropertyKey k : PRE_LOADED_KEYS) {
 			result = XPathCentral.getXPath(k);
 			Assert.assertEquals(XPATH_TEST, result);
 		}
 	}
 
 	@Test
 	public void testGetXPathByPrefixNull() throws Exception {
 
 		try {
 			XPathCentral.getXPathByPrefix(null);
 			Assert.fail();
 		} catch (IllegalArgumentException e) {
 			Assert.assertTrue(true);
 		}
 	}
 
 	@Test
 	public void testGetXPathByPrefixInexistent() throws Exception {
 
 		String[] result = null;
 
 		result = XPathCentral.getXPathByPrefix(TestXPathPrefixEnum.NONE);		
 		Assert.assertNotNull(result);
 		Assert.assertEquals(0, result.length);
 	}
 	
 	@Test
 	public void testGetXPathByPrefix() throws Exception {
 
 		String[] result = null;
 
 		result = XPathCentral.getXPathByPrefix(TestXPathPrefixEnum.XPATH_TEST);		
 		Assert.assertNotNull(result);
 		Assert.assertArrayEquals(XPATH_TEST_PREFIX_CONTENT, result);
 	}
 }
