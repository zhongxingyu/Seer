 package ch.inftec.ju.util.xml;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 import java.io.ByteArrayInputStream;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import ch.inftec.ju.util.IOUtil;
 import ch.inftec.ju.util.JuException;
 import ch.inftec.ju.util.TestUtils;
 
 /**
  * Class containing XML related unit tests.
  * @author tgdmemae
  *
  */
 public class XmlTest {
 	@Test
 	public void loadXml() throws Exception {
 		Document doc = XmlUtils.loadXml(IOUtil.getResourceURL("simple.xml"));
 		
 		Element root = doc.getDocumentElement();
 		assertEquals(root.getTagName(), "root");
 		
 		Element element = (Element)root.getElementsByTagName("element").item(0);
 		assertEquals(element.getNodeName(), "element");
 				
 		Element childElement1 = (Element)element.getElementsByTagName("childElement").item(0);
 		assertEquals(childElement1.getNodeName(), "childElement");
 		
 		Node textElement = element.getElementsByTagName("textElement").item(0);
 		assertEquals(textElement.getTextContent(), "This is a little text");
 	}
 	
 	@Test
 	public void loadXml_validated() throws Exception {
 		XmlUtils.loadXml(IOUtil.getResourceURL("simpleValidated.xml"), IOUtil.getResourceURL("simple.xsd"));
 	}
 	
 	@Test(expected=JuException.class)
 	public void loadXml_invalid() throws Exception {
 		try {
			XmlUtils.loadXml(IOUtil.getResourceURL("simpleValidated_invalid.xml"));
 		} catch (Exception ex) {
 			Assert.fail(ex.toString());
 		}
 		
		XmlUtils.loadXml(IOUtil.getResourceURL("simpleValidated_invalid.xml"), IOUtil.getResourceURL("simple.xsd"));
 	}
 	
 	private void xPathGetter(String resourceName) throws Exception {
 		Document doc = XmlUtils.loadXml(IOUtil.getResourceURL(resourceName));
 		XPathGetter xg = new XPathGetter(doc);
 		assertEquals("XPathGetter[node=#document,nodeValue=<null>]", xg.toString());
 		
 		assertArrayEquals(xg.getArray("//a1/*/@text"), new String[] {"B2", "B2", "B3"});
 		assertArrayEquals(xg.getArrayLong("//*/@value"), new Long[] {1L, 2L, 2L});
 		assertArrayEquals(xg.getDistinctArray("//b3/*/@value"), new String[] {"2"});
 		assertArrayEquals(xg.getDistinctArrayLong("//b3/*/@value"), new Long[] {2L});
 		assertEquals(xg.getGetter("//b3").getSingle("@text"), "B3");
 		
 		XPathGetter xgSubs[] = xg.getGetters("//b3/*");
 		assertEquals(xgSubs.length, 2);
 		assertEquals(xgSubs[0].getSingle("@text"), "C2");
 		assertEquals("XPathGetter[node=c2,nodeValue=<null>]", xgSubs[0].toString());
 		assertEquals(xgSubs[1].getSingle("@text"), "C3");
 		assertEquals("XPathGetter[node=c3,nodeValue=<null>]", xgSubs[1].toString());
 		
 		assertEquals(xg.getNode("root"), doc.getDocumentElement());
 		
 		Node bNodes[] = xg.getNodes("//a1/*");
 		assertEquals(bNodes.length, 3);
 		assertEquals(bNodes[2].getNodeName(), "b3");
 		
 		assertEquals(xg.getSingle("/root/a1/@text"), "A1");
 		assertEquals(xg.getSingleLong("/root/a1/b2/c1/@value"), new Long(1));
 	}
 	
 	/**
 	 * Test the XPathGetter on a XML without namespaces.
 	 */
 	@Test
 	public void xPathGetter() throws Exception {
 		this.xPathGetter("xPathGetter.xml");
 	}
 	
 	/**
 	 * Test the XPathGetter on a XML with namespace.
 	 */
 	@Test
 	public void xPathGetter_namespace() throws Exception {
 		this.xPathGetter("xPathGetter_namespace.xml");
 	}
 	
 	/**
 	 * Tests the toString method of XmlUtil.
 	 */
 	@Test
 	public void xmlTtoString() {
 		// Create XML document
 		Document doc = XmlUtils.buildXml("root")
 			.addChild("child1")
 				.setAttribute("childAttr1", "val1")
 				.setAttribute("childAttr2", "val2")
 				.addText("Text1")
 				.endChild()
 			.addChild("child2")
 				.addChild("subChild1")
 				.addText("Text2")
 				.endChild()
 			.endChild()
 		.getDocument();
 		
 		// Test simple XML, without declaration and indentation
 		String simpleXml = XmlUtils.toString(doc, false, false);
 		TestUtils.assertEqualsResource("xmlToString_simpleXml.xml", simpleXml);
 		
 		// Test complete XML, with declaration and indentation
 		String fullXml = XmlUtils.toString(doc, true, true);
 		TestUtils.assertEqualsResource("xmlToString_fullXml.xml", fullXml);
 	}
 	
 	/**
 	 * Tests the conversion of a String to an XML Document.
 	 */
 	@Test
 	public void stringToXml() throws Exception {
 		String xmlString = "<root><child>someText</child></root>";
 		Document xmlDoc = XmlUtils.loadXml(xmlString, null);
 		Assert.assertEquals(1, xmlDoc.getChildNodes().getLength());
 		Assert.assertEquals("root", xmlDoc.getChildNodes().item(0).getNodeName());
 		
 		// Try to convert the doc to a String again
 		Assert.assertEquals(xmlString, XmlUtils.toString(xmlDoc, false, false));
 	}
 	
 	/**
 	 * Tests XML comparison.
 	 */
 	@Test
 	public void equalsXml() throws Exception {
 		Document doc1 = XmlUtils.loadXml(IOUtil.getResourceURL("xmlToString_simpleXml.xml"));
 		Document doc2 = XmlUtils.loadXml(IOUtil.getResourceURL("xmlToString_fullXml.xml"));
 		TestUtils.assertEqualsXml(doc1, doc2);
 		
 		Document doc3 = XmlUtils.loadXml(IOUtil.getResourceURL("xmlToString_fullXml_diff.xml"));
 		try {
 			TestUtils.assertEqualsXml(doc1, doc3);
 			Assert.fail("XMLs are not equal");
 		} catch (AssertionError e) {
 			// Expected
 		}
 	}
 	
 	/**
 	 * Tests the XmlOutputConverter class.
 	 */
 	@Test
 	public void xmlOutputConverter() throws Exception {
 		Document doc1 = XmlUtils.loadXml(IOUtil.getResourceURL("simpleSpecialChars.xml"));
 		String xml1 = XmlUtils.toString(doc1, true, true);
 		
 		// We need to work with input streams and bytes to make sure the encoding is not messed with
 		ByteArrayInputStream is = new ByteArrayInputStream(xml1.getBytes("UTF-8"));
 		XmlOutputConverter xmlConv1 = new XmlOutputConverter();
 		IOUtils.copy(is, xmlConv1.getOutputStream());
 		
 		Document doc2 = xmlConv1.getDocument();
 		
 		TestUtils.assertEqualsXml(doc1, doc2);
 		
 		// Make sure special characters were handled correctly
 		XPathGetter xg = new XPathGetter(doc1);
 		Assert.assertEquals("This is a little text: äöü°+\"*ç%&/()=?`è!éà£><;:_,.-", xg.getSingle("//textElement"));
 	}
 }
 
