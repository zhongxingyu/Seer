 /*
  * Copyright (c) 2002-2009 Gargoyle Software Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.gargoylesoftware.htmlunit.xml;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.Servlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.w3c.dom.Node;
 
 import com.gargoylesoftware.htmlunit.BrowserRunner;
 import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
 import com.gargoylesoftware.htmlunit.MockWebConnection;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.WebServerTestCase;
 import com.gargoylesoftware.htmlunit.BrowserRunner.Alerts;
 import com.gargoylesoftware.htmlunit.BrowserRunner.Browser;
 import com.gargoylesoftware.htmlunit.BrowserRunner.Browsers;
 import com.gargoylesoftware.htmlunit.html.DomNode;
 import com.gargoylesoftware.htmlunit.html.HtmlBody;
 
 /**
  * Tests for {@link XmlPage}.
  *
  * @version $Revision$
  * @author Marc Guillemot
  * @author Ahmed Ashour
  */
 @RunWith(BrowserRunner.class)
 public class XmlPageTest extends WebServerTestCase {
 
     /**
      * Tests namespace.
      * @throws Exception if the test fails
      */
     @Test
     public void namespace() throws Exception {
         final String content
             = "<?xml version='1.0'?>\n"
             + "<RDF xmlns='http://www.w3.org/1999/02/22-rdf-syntax-ns#' "
             + "xmlns:em='http://www.mozilla.org/2004/em-rdf#'>"
             + "<Description about='urn:mozilla:install-manifest'>"
             + "<em:name>My Plugin</em:name>"
             + "</Description>\n"
             + "</RDF>";
 
         final XmlPage xmlPage = testXmlDocument(content, "text/xml");
         final Node node = xmlPage.getXmlDocument().getFirstChild().getFirstChild().getFirstChild();
         assertEquals("em:name", node.getNodeName());
         assertEquals("name", node.getLocalName());
         assertEquals("http://www.mozilla.org/2004/em-rdf#", node.getNamespaceURI());
     }
 
     /**
      * Tests a simple valid XML document.
      * @throws Exception if the test fails
      */
     @Test
     public void validDocument() throws Exception {
         final String content
             = "<?xml version=\"1.0\"?>\n"
              + "<foo>\n"
              + "    <foofoo name='first'>something</foofoo>\n"
              + "    <foofoo name='second'>something else</foofoo>\n"
              + "</foo>";
 
         final XmlPage xmlPage = testXmlDocument(content, "text/xml");
         assertEquals("foo", xmlPage.getXmlDocument().getFirstChild().getNodeName());
     }
 
     /**
      * Utility method to test XML page of different MIME types.
      * @param content the XML content
      * @param mimeType the MIME type
      * @return the page returned by the WebClient
      * @throws Exception if a problem occurs
      */
     private XmlPage testXmlDocument(final String content, final String mimeType) throws Exception {
         final WebClient client = getWebClient();
         final MockWebConnection webConnection = new MockWebConnection();
         webConnection.setDefaultResponse(content, 200, "OK", mimeType);
         client.setWebConnection(webConnection);
         final Page page = client.getPage(URL_FIRST);
         assertEquals(URL_FIRST, page.getWebResponse().getRequestSettings().getUrl());
         assertEquals("OK", page.getWebResponse().getStatusMessage());
         assertEquals(HttpStatus.SC_OK, page.getWebResponse().getStatusCode());
         assertEquals(mimeType, page.getWebResponse().getContentType());
         assertTrue(XmlPage.class.isInstance(page));
         final XmlPage xmlPage = (XmlPage) page;
         assertEquals(content, xmlPage.getContent());
         Assert.assertNotNull(xmlPage.getXmlDocument());
         return xmlPage;
     }
 
     /**
      * Tests a simple invalid (badly formed) XML document.
      * @throws Exception if the test fails
      */
     @Test
     public void invalidDocument() throws Exception {
         final WebClient client = getWebClient();
         final MockWebConnection webConnection = new MockWebConnection();
 
         final String content
             = "<?xml version=\"1.0\"?>\n"
             + "<foo>\n"
             + "    <foofoo invalid\n"
             + "    <foofoo name='first'>something</foofoo>\n"
             + "    <foofoo name='second'>something else</foofoo>\n"
             + "</foo>";
 
         webConnection.setDefaultResponse(content, 200, "OK", "text/xml");
         client.setWebConnection(webConnection);
 
         final Page page = client.getPage(URL_FIRST);
         assertEquals(URL_FIRST, page.getWebResponse().getRequestSettings().getUrl());
         assertEquals("OK", page.getWebResponse().getStatusMessage());
         assertEquals(HttpStatus.SC_OK, page.getWebResponse().getStatusCode());
         assertEquals("text/xml", page.getWebResponse().getContentType());
 
         assertTrue(Page.class.isInstance(page));
         final XmlPage xmlPage = (XmlPage) page;
         assertEquals(content, xmlPage.getContent());
         assertNull(xmlPage.getXmlDocument());
     }
 
     /**
      * @throws Exception if the test fails
      */
     @Test
     public void voiceXML() throws Exception {
         final String content =
             "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             + "<vxml xmlns=\"http://www.w3.org/2001/vxml\""
             + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
             + "  xsi:schemaLocation=\"http://www.w3.org/2001/vxml "
             + "   http://www.w3.org/TR/voicexml20/vxml.xsd\""
             + "   version=\"2.0\">\n"
             + "  <form>\n"
             + "    <block>Hello World!</block>\n"
             + "  </form>\n"
             + "</vxml>";
 
         final XmlPage xmlPage = testXmlDocument(content, "application/voicexml+xml");
         assertEquals("vxml", xmlPage.getXmlDocument().getFirstChild().getNodeName());
     }
 
     /**
      * @throws Exception if the test fails
      */
     @Test
     public void load_XMLComment() throws Exception {
         final URL firstURL = new URL("http://htmlunit/first.html");
         final URL secondURL = new URL("http://htmlunit/second.xml");
 
         final String html = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    var doc = createXmlDocument();\n"
             + "    doc.async = false;\n"
             + "    alert(doc.load('" + "second.xml" + "'));\n"
             + "    alert(doc.documentElement.childNodes[0].nodeType);\n"
             + "  }\n"
             + "  function createXmlDocument() {\n"
             + "    if (document.implementation && document.implementation.createDocument)\n"
             + "      return document.implementation.createDocument('', '', null);\n"
             + "    else if (window.ActiveXObject)\n"
             + "      return new ActiveXObject('Microsoft.XMLDOM');\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
 
         final String xml = "<test><!-- --></test>";
 
         final String[] expectedAlerts = new String[] {"true", "8"};
         final List<String> collectedAlerts = new ArrayList<String>();
         final WebClient client = getWebClient();
         client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));
         final MockWebConnection conn = new MockWebConnection();
         conn.setResponse(firstURL, html);
         conn.setResponse(secondURL, xml, "text/xml");
         client.setWebConnection(conn);
 
         client.getPage(firstURL);
         assertEquals(expectedAlerts, collectedAlerts);
     }
 
     /**
      * @throws Exception if the test fails
      */
     @Test
     public void createElement() throws Exception {
         final String[] expectedAlerts;
         if (getBrowserVersion().isIE()) {
             expectedAlerts = new String[] {"true", "16"};
         }
         else {
             expectedAlerts = new String[] {"true", "14"};
         }
 
         final String content = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    var doc = createXmlDocument();\n"
             + "    doc.appendChild(doc.createElement('elementName'));\n"
             + "    var xml;\n"
             + "    if (window.ActiveXObject)\n"
             + "      xml = doc.xml;\n"
             + "    else\n"
             + "      xml = new XMLSerializer().serializeToString(doc.documentElement);\n"
             + "    alert(xml.indexOf('<elementName/>') != -1);\n"
             + "    alert(xml.length);\n"
             + "  }\n"
             + "  function createXmlDocument() {\n"
             + "    if (document.implementation && document.implementation.createDocument)\n"
             + "      return document.implementation.createDocument('', '', null);\n"
             + "    else if (window.ActiveXObject)\n"
             + "      return new ActiveXObject('Microsoft.XMLDOM');\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
 
         final List<String> collectedAlerts = new ArrayList<String>();
         loadPage(getBrowserVersion(), content, collectedAlerts);
         assertEquals(expectedAlerts, collectedAlerts);
     }
 
     /**
      * Tests a simplified real-life response from Ajax4jsf.
      * @throws Exception if the test fails
      */
     @Test
     public void a4jResponse() throws Exception {
         final String content = "<html xmlns='http://www.w3.org/1999/xhtml'><head>"
             + "<script src='/a4j_3_2_0-SNAPSHOTorg.ajax4jsf.javascript.PrototypeScript.jsf'></script>"
             + "</head><body><span id='j_id216:outtext'>Echo Hello World</span></body></html>";
         testXmlDocument(content, "text/xml");
     }
 
     /**
      * @throws Exception if the test fails
      */
     @Test
     public void xpath() throws Exception {
         final String html
             = "<?xml version=\"1.0\"?>\n"
              + "<foo>\n"
              + "    <foofoo name='first'>something</foofoo>\n"
              + "    <foofoo name='second'>something else</foofoo>\n"
              + "</foo>";
         final XmlPage xmlPage = testXmlDocument(html, "text/xml");
         assertEquals(1, xmlPage.getByXPath("//foofoo[@name='first']").size());
     }
 
     /**
      * Verifies that case sensitivity in XPath expressions is governed by the type of page, not by
      * the type of elements contained by the page (ie, HTML elements in an XML page behave in a
      * case-sensitive way, because that's how XML behaves). See bug 2515873.
      * @throws Exception if an error occurs
      */
     @Test
     public void xpathForXmlPageContainingHtmlElements() throws Exception {
         final String html
             = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
             + "<!DOCTYPE html PUBLIC \n"
             + "    \"-//W3C//DTD XHTML 1.0 Strict//EN\" \n"
             + "    \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n"
             + "<html xmlns='http://www.w3.org/1999/xhtml' xmlns:xhtml='http://www.w3.org/1999/xhtml'>\n"
             + "<body><DIV>foo</DIV></body>\n"
             + "</html>";
 
         final WebClient client = getWebClient();
         final List<String> actual = new ArrayList<String>();
         client.setAlertHandler(new CollectingAlertHandler(actual));
 
         final MockWebConnection conn = new MockWebConnection();
         conn.setResponse(URL_GARGOYLE, html, "text/xml");
         client.setWebConnection(conn);
 
         final XmlPage page = client.getPage(URL_GARGOYLE);
         final DomNode body = page.getDocumentElement().getFirstChild().getNextSibling();
         final DomNode div = body.getFirstChild();
 
         assertEquals(HtmlBody.class, body.getClass());
         assertEquals("body", body.getLocalName());
         assertEquals("DIV", div.getLocalName());
         assertNotNull(page.getFirstByXPath(".//xhtml:body"));
         assertNotNull(page.getFirstByXPath(".//xhtml:DIV"));
         assertNull(page.getFirstByXPath(".//xhtml:div"));
     }
 
     /**
      * @throws Exception if the test fails
      */
     @Test
     @Browsers(Browser.FF)
     @Alerts("[object Element]")
     public void createElementNS() throws Exception {
         final String html = "<html><head><title>foo</title><script>\n"
             + "  function test() {\n"
             + "    var doc = createXmlDocument();\n"
             + "    alert(doc.createElementNS('myNS', 'ppp:eee'));\n"
             + "  }\n"
             + "  function createXmlDocument() {\n"
             + "    if (document.implementation && document.implementation.createDocument)\n"
             + "      return document.implementation.createDocument('', '', null);\n"
             + "    else if (window.ActiveXObject)\n"
             + "      return new ActiveXObject('Microsoft.XMLDOM');\n"
             + "  }\n"
             + "</script></head><body onload='test()'>\n"
             + "</body></html>";
 
         loadPageWithAlerts(html);
     }
 
     /**
      * @throws Exception if the test fails
      */
     @Test
     public void noResponse() throws Exception {
         final Map<String, Class< ? extends Servlet>> servlets = new HashMap<String, Class< ? extends Servlet>>();
         servlets.put("/test", NoResponseServlet.class);
         startWebServer("./", null, servlets);
 
        final WebClient client = new WebClient();
         client.getPage("http://localhost:" + PORT + "/test");
     }
 
     /**
     * Servlet for {@link #comma()}.
      */
     public static class NoResponseServlet extends HttpServlet {
 
         private static final long serialVersionUID = 714328190645334742L;
 
         /**
          * {@inheritDoc}
          */
         @Override
         protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
             response.setContentType("text/xml");
             response.setStatus(HttpServletResponse.SC_NO_CONTENT);
         }
     }
 }
