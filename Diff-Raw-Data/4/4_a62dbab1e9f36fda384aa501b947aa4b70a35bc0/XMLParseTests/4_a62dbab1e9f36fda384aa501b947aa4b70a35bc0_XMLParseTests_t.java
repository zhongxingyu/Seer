 package com.buglabs.common.tests.junit;
 
 import java.io.IOException;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import com.buglabs.util.XmlNode;
 import com.buglabs.util.XmlParser;
 import com.buglabs.util.XpathQuery;
 
 /**
  * Test the xml parser.
  * @author kgilmer
  *
  */
 public class XMLParseTests extends TestCase {
 	private String xml;
 
 	private String xml2;
 	
 	private String xml3;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		xml = "<Package>" + "<programType>sal</programType>" + "<packageVersion>1</packageVersion>" + "<program>" + "<instructions>"
 				+ "<Get>" + "<url>http://localhost:8082/service/location</url>" + "</Get>" + "<Get>"
 				+ "<url>http://localhost:8082/service/system/username</url>" + "</Get>" + "<Post>"
 				+ "<url>http://ticker:8090/com.buglabs.net.servlet-1.0.38/cache/bug123/myapp/</url>" + "</Post>" + "<Debug/>"
 				+ "</instructions>" + "</program>" + "<modified>Thu Sep 14 19:39:39 EDT 2006</modified>" + "<name>testpkg2</name>"
 				+ "<author>Ken</author>" + "<active>true</active>" + "<created>Thu Sep 14 19:39:39 EDT 2006</created>" + "</Package>";
 
 		xml2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><metadata xmlns=\"http://musicbrainz.org/ns/mmd-1.0#\" xmlns:ext=\"http://musicbrainz.org/ns/ext-1.0#\"><release-list><release id=\"6d931ac2-e389-4e99-8a01-1da65162c372\" type=\"Album Official\" ext:score=\"100\"><title>Wasp Star (Apple Venus, Volume II)</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><release-event-list><event date=\"2000-05-22\"/></release-event-list><disc-list count=\"2\"/><track-list count=\"12\"/></release><release id=\"ea9190fe-d7d9-4b82-a200-5e9874b61090\" type=\"Album Official\" ext:score=\"46\"><title>Apple Venus, Volume 1</title><text-representation language=\"ENG\" script=\"Latn\"/><asin>B00000I4JT</asin><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><release-event-list><event date=\"1999-02-22\"/></release-event-list><disc-list count=\"2\"/><track-list count=\"11\"/></release><release id=\"5c62721d-cf70-4c53-b929-15db1fa1f658\" type=\"Album Official\" ext:score=\"40\"><title>Homespun: The Apple Venus Volume One Home Demos</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><track-list count=\"11\"/></release><release id=\"e324cbc4-4206-43d9-999f-1be2212709fc\" type=\"Album Official\" ext:score=\"17\"><title>Go 2</title><text-representation language=\"ENG\" script=\"Latn\"/><asin>B00005ATHK</asin><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><release-event-list><event date=\"1978\"/></release-event-list><disc-list count=\"1\"/><track-list count=\"13\"/></release><release id=\"8d587543-b7ef-463c-b8b3-6e5273d31c91\" type=\"Album Official\" ext:score=\"17\"><title>Go 2</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><track-list count=\"13\"/></release><release id=\"5f39d0c8-74a7-4a76-91f3-0a9135730c4d\" type=\"Compilation Official\" ext:score=\"16\"><title>Coat of Many Cupboards (disc 2)</title><text-representation language=\"ENG\" script=\"Latn\"/><asin>B00005V94X</asin><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><track-list count=\"15\"/></release><release id=\"121ad31d-4612-4b01-acc4-82f7e5391341\" type=\"Compilation Official\" ext:score=\"16\"><title>Fossil Fuel: The XTC Singles 1977-92 (disc 2)</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><disc-list count=\"1\"/><track-list count=\"15\"/></release><release id=\"d0a288c2-a664-400d-85c3-2dab321966ff\" type=\"Live Official\" ext:score=\"16\"><title>Transistor Blast: The Best of the BBC Sessions (disc 2)</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><disc-list count=\"1\"/><track-list count=\"13\"/></release><release id=\"103e170a-2707-4f12-bd18-a92dd6ed1da4\" type=\"Live Bootleg\" ext:score=\"16\"><title>1980-01-16: The Rhythm: Hurrah's, New York City, NY, USA (disc 2)</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"97c86b2c-2765-46a2-aef8-76a7e24c430f\"><name>XTC</name></artist><track-list count=\"8\"/></release><release id=\"7ffde6dd-522c-462b-a14b-391c4a8f3fb5\" type=\"Album Official\" ext:score=\"12\"><title>WASP</title><text-representation language=\"ENG\" script=\"Latn\"/><artist id=\"171cbc8f-8ab3-4aec-9916-f0bdbe26bb79\"><name>W.A.S.P.</name></artist><release-event-list><event date=\"2003\"/></release-event-list><disc-list count=\"1\"/><track-list count=\"13\"/></release></release-list></metadata>";
 
 		xml3 = "<cars>" +
 			"<car name=\"mini\" color=\"yellow\"><wheel count=\"4\" code=\"ds2\"/><seat count=\"2\" type=\"bench\"/></car>" +
 			"<car name=\"land cruiser\" color=\"red\"><wheel count=\"4\" code=\"dn1\"/><seat count=\"4\" type=\"bucket\"/></car>" +
 			"<plane name=\"Boing 747\" color=\"silver\"><wheel count=\"5\" code=\"tt4\"/>" + "</plane>" +
 		"</cars>";
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	public void testSimpleXPath() {
 		try {
 			XmlNode root = XmlParser.parse(xml3);
 			
 			Object o = XpathQuery.evaluate("/cars/car", root, XpathQuery.NODE_LIST);
 			
 			assertTrue(o instanceof List);
 			
 			List elems = (List) o;
 			
 			assertTrue(elems.size() == 2);
 			
 			assertTrue(elems.get(0) instanceof XmlNode);
 			
 			XmlNode c1 = (XmlNode) elems.get(0);
 			
 			assertTrue(c1.getName().equals("car"));
 		} catch (IOException e) {
 			fail(e.toString());
 		}
 	}
 	
 	public void testAnynodeXPath() {
 		try {
 			XmlNode root = XmlParser.parse(xml3);
 			
 			Object o = XpathQuery.evaluate("//wheel", root, XpathQuery.NODE_LIST);
 			
 			assertTrue(o instanceof List);
 			
 			List elems = (List) o;
 			
 			assertTrue(elems.size() == 3);
 			
 			assertTrue(elems.get(0) instanceof XmlNode);
 			
 			XmlNode c1 = (XmlNode) elems.get(0);
 			
 			assertTrue(c1.getName().equals("wheel"));
 		} catch (IOException e) {
 			fail(e.toString());
 		}
 	}
 	
 	public void testAttrValueXPath() {
 		try {
 			XmlNode root = XmlParser.parse(xml3);
 			
 			Object o = XpathQuery.evaluate("//wheel[@code='dn1']", root, XpathQuery.NODE_LIST);
 			
 			assertTrue(o instanceof List);
 			
 			List elems = (List) o;
 			
 			assertTrue(elems.size() == 1);
 			
 			assertTrue(elems.get(0) instanceof XmlNode);
 			
 			XmlNode c1 = (XmlNode) elems.get(0);
 			
 			assertTrue(c1.getName().equals("wheel"));
 			
 			assertTrue(c1.getAttribute("code").equalsIgnoreCase("dn1"));
 			
 			System.out.println(root.toString());
 		} catch (IOException e) {
 			fail(e.toString());
 		}
 	}
 	
 	public void testAttrValueXPath2() {
 		try {
 			XmlNode root = XmlParser.parse(xml3);
 			
 			Object o = XpathQuery.evaluate("//plane/wheel[@code='dn1']", root, XpathQuery.NODE_LIST);
 			
 			assertTrue(o instanceof List);
 			
 			List elems = (List) o;
 			
 			assertTrue(elems.size() == 1);
 			
 			assertTrue(elems.get(0) instanceof XmlNode);
 			
 			XmlNode c1 = (XmlNode) elems.get(0);
 			
 			assertTrue(c1.getName().equals("plane"));
 			
 			assertTrue(c1.getAttribute("color").equalsIgnoreCase("silver"));
 			
 			System.out.println(root.toString());
 		} catch (IOException e) {
 			fail(e.toString());
 		}
 	}
 	
 	public void testSimpleXPath2() {
 		try {
 			XmlNode root = XmlParser.parse(xml3);
 			
 			Object o = XpathQuery.evaluate("/cars/car", root, XpathQuery.NODE);
 			
 			assertTrue(o instanceof XmlNode);
 			
 			XmlNode c1 = (XmlNode) o;
 			
 			assertTrue(c1.getName().equals("car"));
 		} catch (IOException e) {
 			fail(e.toString());
 		}
 	}
 
 	public void testParseString() {
 
 		XmlNode e = null;
 		try {
 			e = XmlParser.parse(xml);
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 
 		assertEquals(e.childExists("programType"), true);
 
 		assertEquals(e.getFirstElement("programType").getValue().equals("sal"), true);
 		
 		XmlNode e2 = null;
 		
 		try {
 			e2 = XmlParser.parse(xml2);
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 		
 		assertTrue(e2.childExists("release-list"));
 		
 		assertTrue(e2.getFirstElement("release-list/release/title") != null);
 		
 		System.out.println(e2.toString());
 	}
 	
 	public void testNamespaceXml() {
 		String namespaceXml="<obr:repository name='Untitled' obr:time='20051210072623.031' xmlns:obr=\"http://www.osgi.org/xmlns/scr/v1.0.0\"></obr:repository>";
 		String nonNamespaceXml="<repository name='Untitled' time='20051210072623.031' xmlns:obr='http://www.osgi.org/xmlns/scr/v1.0.0'></repository>";
 		
 		XmlNode e1 = null;
 		
 		try {
 			e1 = XmlParser.parse(namespaceXml, true);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		XmlNode e2 = null;
 		
 		try {
 			e2 = XmlParser.parse(nonNamespaceXml);
 		} catch (IOException e) {
 			fail(e.getMessage());
 		}
 		
 		assertTrue(e1.getName().equals(e2.getName()));
 		assertTrue(e1.getAttributes().size() == e2.getAttributes().size());
 	}
 
 	public void testPrintXML() {
 		XmlNode e = null;
 		try {
 			e = XmlParser.parse(xml);
 		} catch (IOException e1) {
 			fail(e1.getMessage());
 		}
 
 		System.out.println(e.toString());
 	}
 	
 	/**
 	 * Temporary test to verify failure works as expected.
 	 */
 	public void testFailure() {
 		assertTrue(false);
 	}
}
