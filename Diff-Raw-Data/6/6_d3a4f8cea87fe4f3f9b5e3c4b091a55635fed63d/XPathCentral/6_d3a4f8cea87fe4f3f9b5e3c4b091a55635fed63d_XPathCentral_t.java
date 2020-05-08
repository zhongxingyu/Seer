 package com.operativus.senacrs.audit.properties.xpath;
 
 import java.io.IOException;
 
import com.operativus.senacrs.audit.exceptions.RuntimeExceptionFactory;
 import com.operativus.senacrs.audit.properties.PropertyKey;
 import com.operativus.senacrs.audit.properties.ResourceCentral;
 
 public final class XPathCentral {
 
 	private static final ResourceCentral central = new ResourceCentral();
 	private static final String[] LIST_FILES = new String[] {
 			"xpath.properties",
 	};
 
 	static {
 
 		central.populateCentral(LIST_FILES);
 	}
 
 	public static void addXPathFile(final String filename) throws IOException {
 
 		central.populateCentral(filename);
 	}
 
 	public static String getXPath(final PropertyKey key, final Object... arguments) {
 
 		return central.getMessage(key, arguments);
 	}
 	
 	public static String[] getXPathByPrefix(final XPathKeyPrefix prefix) {
 		
		if (prefix == null) {
			throw RuntimeExceptionFactory.getInstance().getNullArgumentException("prefix");
		}
		
 		//TODO
 		return null;
 	}
 
 	public static boolean hasKey(final PropertyKey key) {
 
 		return central.hasKey(key);
 	}
 
 	private XPathCentral() {
 
 		super();
 	}
 }
