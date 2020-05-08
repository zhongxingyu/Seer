 package org.daisy.pipeline.client.test;
 
 import static org.junit.Assert.*;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import junit.framework.Assert;
 
 import org.daisy.pipeline.client.Jobs;
 import org.daisy.pipeline.client.Pipeline2WS;
 import org.daisy.pipeline.client.Pipeline2WSException;
 import org.daisy.pipeline.client.Pipeline2WSLogger;
 import org.daisy.pipeline.client.Pipeline2WSResponse;
 import org.daisy.pipeline.client.Scripts;
 import org.daisy.pipeline.client.models.Script;
 import org.daisy.pipeline.client.models.script.Argument;
 import org.daisy.pipeline.utils.NamespaceContextMap;
 import org.daisy.pipeline.utils.XML;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 
 public class Pipeline2WSTest {
 
 	@Test
 	public void getScripts() {
 		try {
 			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
 			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret");
 			if (response.status != 200)
 				fail(response.status+": "+response.statusName+" ("+response.statusDescription+")");
 			
 			List<Script> scripts = Script.getScripts(response);
 			if (scripts.size() == 0)
 				fail("no scripts in response");
 			if (scripts.get(0).id == null || scripts.get(0).id.length() == 0)
 				fail("empty script id");
 			if (scripts.get(0).nicename == null || scripts.get(0).nicename.length() == 0)
 				fail("empty nicename id");
 			if (scripts.get(0).desc == null || scripts.get(0).desc.length() == 0)
 				fail("empty script description");
 			assertNotNull(scripts.get(0).id);
 
 		} catch (Pipeline2WSException e) {
 			fail(e.getMessage());
 		}
 	}
 	
	@SuppressWarnings("unused")
 	private boolean containsArgument(List<Argument> arguments, String argName) {
 		for (Argument arg : arguments) {
 			if (argName.equals(arg.name))
 				return true;
 		}
 		return false;
 	}
 	
 //	Pattern xmlDeclaration = Pattern.compile(".*<\\?xml.*");
 	
 	@Test
 	public void testParseJobRequest() {
 		try {
 			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
 			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
 			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret", "dtbook-to-zedai");
 			System.out.println("response: "+response.asText());
 			Script script = new Script(response);
 			
 			for (Argument arg : script.arguments) {
 				assertNotNull(arg.name);
 				assertNotNull(arg.name, arg.nicename);
 				assertNotNull(arg.name, arg.desc);
 				assertNotNull(arg.name, arg.required);
 				assertNotNull(arg.name, arg.sequence);
 				assertNotNull(arg.name, arg.ordered);
 				assertNotNull(arg.name, arg.mediaTypes);
 				assertNotNull(arg.name, arg.xsdType);
 				assertNotNull(arg.name, arg.kind);
 				
 				assertEquals(arg.name+" has a decription", true, arg.desc.length() > 0);
 				assertEquals(arg.name+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));
 				
 				if ("source".equals(arg.name)) {
 					assertEquals("input", arg.kind);
 					assertEquals("application/x-dtbook+xml", arg.mediaTypes.get(0));
 					assertEquals(true, arg.ordered);
 					assertEquals(true, arg.required);
 					assertEquals(true, arg.sequence);
 					
 					arg.add("file1.xml");
 					arg.add("file2.xml");
 				}
 				else if ("zedai-filename".equals(arg.name)) {
 					assertEquals("option", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 					assertEquals("string", arg.xsdType);
 					
 					arg.set("zedai.xml");
 				}
 				else if ("assert-valid".equals(arg.name)) {
 					assertEquals("option", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 					assertEquals("boolean", arg.xsdType);
 					
 					arg.set(true);
 				}
 				else if ("output-dir".equals(arg.name)) {
 					assertEquals("option", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(true, arg.required);
 					assertEquals(false, arg.sequence);
 					assertEquals("anyDirURI", arg.xsdType);
 					assertEquals("result", arg.output);
 					
 					arg.set("file:/tmp/text/");
 				}
 				else if ("mods-filename".equals(arg.name)) {
 					assertEquals("option", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 					assertEquals("string", arg.xsdType);
 					
 					arg.set("mods.xml");
 				}
 				else if ("lang".equals(arg.name)) {
 					assertEquals("option", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 					assertEquals("string", arg.xsdType);
 					
 					arg.set("en");
 				}
 				else if ("css-filename".equals(arg.name)) {
 					assertEquals("option", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 					assertEquals("string", arg.xsdType);
 					
 					arg.set("main.css");
 				}
 				else fail("Unknown argument: "+arg.name);
 			}
 			Document jobRequest = Jobs.createJobRequestDocument(script.href, script.arguments, null);
 			
 			script = new Script(response);
 			script.parseFromJobRequest(jobRequest);
 			for (Argument arg : script.arguments) {
 				if ("source".equals(arg.name)) {
 					assertEquals(true, "file1.xml".equals(arg.get(0)));
 					assertEquals(true, "file2.xml".equals(arg.get(1)));
 				}
 				else if ("zedai-filename".equals(arg.name)) assertEquals("zedai.xml", arg.get());
 				else if ("assert-valid".equals(arg.name)) assertEquals("true", arg.get());
 				else if ("output-dir".equals(arg.name)) assertEquals("file:/tmp/text/", arg.get());
 				else if ("mods-filename".equals(arg.name)) assertEquals("mods.xml", arg.get());
 				else if ("lang".equals(arg.name)) assertEquals("en", arg.get());
 				else if ("css-filename".equals(arg.name)) assertEquals("main.css", arg.get());
 			}
 
 		} catch (Pipeline2WSException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testOutputPorts() {
 		try {
 			Pipeline2WS.logger().setLevel(Pipeline2WSLogger.LEVEL.ALL);
 			Pipeline2WS.setHttpClientImplementation(new MockHttpClient());
 			Pipeline2WSResponse response = Scripts.get("http://localhost:8182/ws", "clientid", "supersecret", "dtbook-validator");
 			System.out.println("response: "+response.asText());
 			Script script = new Script(response);
 			
 			for (Argument arg : script.arguments) {
 				assertNotNull(arg.name);
 				assertNotNull(arg.name, arg.nicename);
 				assertNotNull(arg.name, arg.desc);
 				assertNotNull(arg.name, arg.required);
 				assertNotNull(arg.name, arg.sequence);
 				assertNotNull(arg.name, arg.ordered);
 				assertNotNull(arg.name, arg.mediaTypes);
 				assertNotNull(arg.name, arg.xsdType);
 				assertNotNull(arg.name, arg.kind);
 				
 				assertEquals(arg.name+" has a decription", true, arg.desc.length() > 0);
 				assertEquals(arg.name+" has xml declaration", false, arg.toString().replaceAll("\\n", " ").matches(".*<\\?xml.*"));
 				
 				if ("result".equals(arg.name)) {
 					assertEquals("output", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 				}
 				else if ("schematron-report".equals(arg.name)) {
 					assertEquals("output", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 				}
 				else if ("relaxng-report".equals(arg.name)) {
 					assertEquals("output", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(true, arg.sequence);
 				}
 				else if ("html-report".equals(arg.name)) {
 					assertEquals("output", arg.kind);
 					assertEquals(true, arg.ordered);
 					assertEquals(false, arg.required);
 					assertEquals(false, arg.sequence);
 				}
 			}
 
 		} catch (Pipeline2WSException e) {
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testXPath() {
 		XPathFactory factory = XPathFactory.newInstance();
 		javax.xml.xpath.XPath xpath = factory.newXPath();
 		NamespaceContext context = new NamespaceContextMap(
 				"foo", "http://foo", 
 				"bar", "http://bar");
 
 		xpath.setNamespaceContext(context);
 
 
 		Document xml = XML.getXml("<foo:data xmlns:foo='http://foo' xmlns:bar='http://bar'><bar:foo bar=\"hello\" /></foo:data>");
 		try {
 			XPathExpression compiled = xpath.compile("/foo:data/bar:foo/attribute::bar");
 			NodeList nodeList = (NodeList)compiled.evaluate(xml, XPathConstants.NODESET);
 			assertEquals("bar=\"hello\"", nodeList.item(0).toString());
 		} catch (XPathExpressionException e) {
 			fail(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testXPathContext() {
 		Map<String, String> mappings = new HashMap<String, String>();
 		mappings.put("foo", "http://foo");
 		mappings.put("altfoo", "http://foo");
 		mappings.put("bar", "http://bar");
 		mappings.put(XMLConstants.XML_NS_PREFIX,XMLConstants.XML_NS_URI);
 
 		NamespaceContext context = new NamespaceContextMap(mappings);
 		for (Map.Entry<String, String> entry : mappings.entrySet()) {
 			String prefix = entry.getKey();
 			String namespaceURI = entry.getValue();
 
 			Assert.assertEquals("namespaceURI", namespaceURI, context.getNamespaceURI(prefix));
 			boolean found = false;
 			Iterator<?> prefixes = context.getPrefixes(namespaceURI);
 			while (prefixes.hasNext()) {
 				if (prefix.equals(prefixes.next())) {
 					found = true;
 					break;
 				}
 				try {
 					prefixes.remove();
 					Assert.fail("rw");
 				} catch (UnsupportedOperationException e) {
 				}
 			}
 			Assert.assertTrue("prefix: " + prefix, found);
 			Assert.assertNotNull("prefix: " + prefix, context.getPrefix(namespaceURI));
 		}
 
 		Map<String, String> ctxtMap = ((NamespaceContextMap) context).getMap();
 		for (Map.Entry<String, String> entry : mappings.entrySet()) {
 			Assert.assertEquals(entry.getValue(), ctxtMap.get(entry.getKey()));
 		}
 	}
 
 	@Test
 	public void testXPathModify() {
 		NamespaceContextMap context = new NamespaceContextMap();
 
 		try {
 			Map<String, String> ctxtMap = context.getMap();
 			ctxtMap.put("a", "b");
 			Assert.fail("rw");
 		} catch (UnsupportedOperationException e) {
 		}
 
 		try {
 			Iterator<String> it = context.getPrefixes(XMLConstants.XML_NS_URI);
 			it.next();
 			it.remove();
 			Assert.fail("rw");
 		} catch (UnsupportedOperationException e) {
 		}
 	}
 
 	@Test
 	public void testXPathConstants() {
 		NamespaceContext context = new NamespaceContextMap();
 		Assert.assertEquals(XMLConstants.XML_NS_URI, context.getNamespaceURI(XMLConstants.XML_NS_PREFIX));
 		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
 		Assert.assertEquals(XMLConstants.XML_NS_PREFIX, context.getPrefix(XMLConstants.XML_NS_URI));
 		Assert.assertEquals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, context.getNamespaceURI(XMLConstants.XMLNS_ATTRIBUTE));
 	}
 	
 }
