 package org.esgf.filedownload;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 
 import org.apache.log4j.Logger;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Result;
 import org.junit.runner.notification.Failure;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 public class URLSElementTest {
     
     private final static Logger LOG = Logger.getLogger(URLSElementTest.class);
     
     //private final static String testInitializationFile = "src\\java\\test\\org\\esgf\\filedownload\\files\\URLSElement\\URLSInitializationElement.xml";
     //private final static String testAddRemoveFile = "src\\java\\test\\org\\esgf\\filedownload\\files\\URLSElement\\URLSAddElement.xml";
     
     //test runner
     public static void main(String [] args) {
         
         /*
         System.out.println("Running test for URLSElementTest");
         result = JUnitCore.runClasses(URLSElementTest.class);
         
         for (Failure failure : result.getFailures()) {
             System.out.println(failure.toString());
         }
         System.out.println("Total Tests: " + result.getRunCount());
         System.out.println("Failure count: " + result.getFailureCount());
          */
     }
     
     @Before
     public void setUp() throws JDOMException, IOException {
             
     }
     
     
     @Test
     public void testInitialization() throws JDOMException, IOException, DocumentException {
         System.out.println("Test initializaton");
 
         /*
         URLSElement ue = new URLSElement();
         
         String URLSfileName = testInitializationFile; 
         
         //compare sizes...initial size should be 1 
         int initialSize = 1;
         assertEquals(initialSize,ue.getURLS().size());
         
         
         //get XML string from the file
         String fileXML = getXMLStringFromFile(URLSfileName);
         
         //get XML string from the Element
         String urlXML = getXMLStringFromURLSElement(ue);
         
         //compare
         assertEquals(fileXML,urlXML);
         */
        
     }
     
     @Test
     public void testAddRemove() throws JDOMException, IOException, DocumentException {
         System.out.println("Test addRemove");
 
         /*
         URLSElement ue = new URLSElement();
         
         String URLSAddFileName = this.testAddRemoveFile; 
         
         String URLSRemoveFileName = this.testInitializationFile;
         
         //testing the add
         ue.addURL("url2");
     
         //get XML string from the file
         String fileXML = getXMLStringFromFile(URLSAddFileName);
         
         //get XML string from the Element
         String urlXML = getXMLStringFromURLSElement(ue);
         
         //compare
         assertEquals(fileXML,urlXML);
         int size = ue.getURLS().size();
         assertEquals(size,ue.getURLS().size());
         
         //testing the remove
         ue.removeURL("url2");
 
         //get XML string from the file
         fileXML = getXMLStringFromFile(URLSRemoveFileName);
 
         //get XML string from the Element
         urlXML = getXMLStringFromURLSElement(ue);
 
         //compare
         assertEquals(fileXML,urlXML);
         */
     }
     
     @Test
     public void testAddNull() {
         System.out.println("Test addNull");
         /*
 
         URLSElement ue = new URLSElement();
         
         String URLSFileName = this.testInitializationFile;
         
         String URLString = null;
         
         ue.addURL(URLString);
         
         //get XML string from the file
         String fileXML = getXMLStringFromFile(URLSFileName);
 
         //get XML string from the Element
         String urlXML = getXMLStringFromURLSElement(ue);
 
         //compare
         assertEquals(fileXML,urlXML);
         */
     }
     
     @Test
     public void testRemoveNull() {
         System.out.println("Test removeNull");
         
         /*
 
         URLSElement ue = new URLSElement();
         
         String URLSFileName = this.testInitializationFile;
         
         String URLString = null;
         
         ue.removeURL(URLString);
         
         //get XML string from the file
         String fileXML = getXMLStringFromFile(URLSFileName);
 
         //get XML string from the Element
         String urlXML = getXMLStringFromURLSElement(ue);
 
         //compare
         assertEquals(fileXML,urlXML);
         */
     }
     
     
     
     @Test
     public void testToElement() {
         System.out.println("Test ToElement");
         /*
 
         URLSElement ue = new URLSElement();
         
         ue.addURL("url2");
         
         String URLSFileName = this.testAddRemoveFile;
         
         //get XML string from the Element
         Element ueElement = ue.toElement();
         String ueElementXML = getXMLStringFromElement(ueElement);
 
         //get XML string from the file
         Element fromFileElement = getElementFromFile(URLSFileName);
         String fromFileElementXML = getXMLStringFromElement(fromFileElement);
 
         
         //compare
         assertEquals(ueElementXML,fromFileElementXML);
         */
     }
     
     @After
     public void tearDown() {
         LOG.debug("Tearing down URLSElementTest");
     }
     
     
    
     private Element getElementFromFile(String URLSfileName) {
         SAXBuilder parser = new SAXBuilder();
         Element rootElement = null;
         try {
             Document doc = parser.build(URLSfileName);
             rootElement = doc.getRootElement();
             
         } catch(Exception e) {
             e.printStackTrace();
         }
         
         return rootElement;
     }
     
     private String getXMLStringFromFile(String URLSfileName) {
         SAXBuilder parser = new SAXBuilder();
         String fileXML = null;
         try {
             Document doc = parser.build(URLSfileName);
             XMLOutputter fileOutputter = new XMLOutputter();
             fileXML = fileOutputter.outputString(doc.getRootElement());
         } catch(Exception e) {
             e.printStackTrace();
         }
         
         
         return fileXML;
     }
     
     private String getXMLStringFromElement(Element e) {
         
         XMLOutputter URLOutputter = new XMLOutputter();
         String xml = URLOutputter.outputString(e);
         
         return xml;
     }
     
     private String getXMLStringFromURLSElement(URLSElement me) {
         SAXBuilder builder = new SAXBuilder();
         Reader in = new StringReader(me.toXML());
         String URLXML = null;
         try {
             Document URLDoc = builder.build(in);
             
             XMLOutputter URLOutputter = new XMLOutputter();
             URLXML = URLOutputter.outputString(URLDoc.getRootElement());
         } catch(Exception e) {
             e.printStackTrace();
         }
         
         return URLXML;
     }
     
 }
 
