 package hemera.core.utility.unittest;
 
 import hemera.core.utility.URIParser;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import junit.framework.TestCase;
 
 public class TestURIParser extends TestCase {
 
 	public void testNoArguments() throws Exception {
 		final String uri = "/resource/subresource/processor";
 		final Map<String, String> arguments = URIParser.instance.parseURIArguments(uri);
 		assertEquals(null, arguments);
 	}
 	
 	public void testInvalidArguments() throws Exception {
 		final String uri = "/resource/subresource/processor?";
 		final Map<String, String> arguments = URIParser.instance.parseURIArguments(uri);
 		assertEquals(null, arguments);
 	}
 	
 	public void testHasArguments() throws Exception {
 		String uri = "/resource/subresource/processor?";
 		final Random random = new Random();
 		final Map<String, String> expected = new HashMap<String, String>();
 		final int count = 17;
 		for (int i = 0; i < count; i++) {
 			final String key = String.valueOf(random.nextDouble());
 			final String value = String.valueOf(random.nextDouble());
 			expected.put(key, value);
 			uri += key + "=" + value;
 			if (i != count-1) uri += "&";
 		}
 		
 		final Map<String, String> arguments = URIParser.instance.parseURIArguments(uri);
 		final Iterable<String> keys = arguments.keySet();
 		for (final String key : keys) {
 			final String value = arguments.get(key);
 			final String expectedvalue = expected.get(key);
 			assertEquals(expectedvalue, value);
 		}
 	}
 	
 	public void testPathNoArguments() {
 		final String uri = "/resource/subresource/processor";
 		final String path = URIParser.instance.parsePath(uri);
 		assertEquals(uri, path);
 	}
 	
 	public void testPathWithArguments() {
 		final String expected = "/resource/subresource/processor";
 		final String uri = expected + "?arg1=1&arg2=2";
 		final String path = URIParser.instance.parsePath(uri);
		assertEquals(expected.substring(1), path);
 	}
 	
 	public void testPathInvalidArguments() {
 		final String base = "/resource/subresource/processor";
 		final String uri = base + "arg1=1&arg2=2";
 		final String path = URIParser.instance.parsePath(uri);
 		assertEquals(uri, path);
 	}
 }
