 package org.objectium.test.jenkins;
 
 import java.io.IOException;
 import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * @author Suren Rodrigo
  * Basic Jenkins Wrapper that is used only to obtain simple information from the CI Environment. This is not a mandatory class and will not be maintained
  * under the Objectium Proect.
  *
  */
 public class JenkinsWrapper {
 
     private static final int TIME_OUT = 10000;
     private URL jenkinsUrl = null;
     private Document jenkinsDoc = null;
     private DocumentBuilder builder = null;
     private String jenkinsUrlString = null;
 
     /**
      * @throws ParserConfigurationException
      * Main Constructor; This will always be initilized as a concreat class, we won't be using this for the Objectium project operations.
      */
     public JenkinsWrapper() throws ParserConfigurationException {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         builder = factory.newDocumentBuilder();
     }
 
     /**
      *      * 
      * Set the Jenkins URL we want to connect and also create a URL representation and String path
      * @param url - URL of the jenkins server
      * @throws SAXException - 
      * @throws IOException - 
      * @throws InterruptedException - 
      */
     public void setUrl(String url) throws SAXException, IOException, InterruptedException {
         char lastCharacter = url.charAt(url.length() - 1);
         jenkinsUrlString = (lastCharacter == '/') ? url + "api/xml" : url + "/api/xml";
         jenkinsUrl = (jenkinsUrl == null) ? new URL(jenkinsUrlString) : jenkinsUrl;
     }
 
     /**
      * Initialize the XML Document
      * @throws SAXException
      * @throws IOException
      * @throws InterruptedException
      */
     private void initializeJenkinsXmlApi() throws SAXException, IOException, InterruptedException {
         if (jenkinsDoc == null) {
            Thread.sleep(5000); 
             jenkinsDoc = builder.parse(jenkinsUrlString);
             jenkinsDoc.getDocumentElement().normalize();
         }
     }
 
     /**
      * @return - whether the Jenkins server is up and running
      */
     public boolean isAvailable() {
         HttpURLConnection connection = null;
         try {
             connection = getJenkinsConnection();
             int responseCode = connection.getResponseCode();
             return (200 <= responseCode && responseCode <= 399);
         } catch (IOException e) {
             return false;
         } finally {
             if (connection != null) {
                 connection.disconnect();
             }
         }
     }
 
     private HttpURLConnection getJenkinsConnection() throws IOException {
         HttpURLConnection connection = (HttpURLConnection) jenkinsUrl.openConnection();
         connection.setConnectTimeout(TIME_OUT);
         connection.setReadTimeout(TIME_OUT);
         connection.setRequestMethod("HEAD");
         return connection;
     }
 
 
     /**
      * @param jobName - Name of the job that we need to check if configured
      * @return
      * @throws SAXException
      * @throws IOException
      * @throws InterruptedException
      */
     public boolean isJobConfigured(String jobName) throws SAXException, IOException, InterruptedException {
         initializeJenkinsXmlApi();
         NodeList jobList = jenkinsDoc.getElementsByTagName("job");
         for (int i = 0; i < jobList.getLength(); i++) {
             Node jobNode = jobList.item(i);
             if (jobNode.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             Element jobElement = (Element) jobNode;
             NodeList nameList = jobElement.getElementsByTagName("name");
             for (int j = 0; j < nameList.getLength(); j++) {
                 Element nameElement = (Element) nameList.item(j);
                 if (nameElement.getChildNodes().item(0).getNodeValue().equals(jobName))
                     return true;
             }
         }
         return false;
     }
 }
