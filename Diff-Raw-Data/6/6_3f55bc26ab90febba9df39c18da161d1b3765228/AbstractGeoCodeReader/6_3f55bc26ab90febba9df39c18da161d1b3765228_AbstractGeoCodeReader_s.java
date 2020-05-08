 package com.codebytes.readers;
 
 import com.codebytes.Coordinates;
 import com.codebytes.readers.google.GoogleErrorEnum;
 import com.codebytes.readers.yahoo.PlaceFinderErrorEnum;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Lee
  * Date: 1/4/13
  * Time: 10:38 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class AbstractGeoCodeReader {
 
     private DocumentBuilder db = null;
     protected DocumentBuilder getDocumentBuilder() {
         if (db == null) {
             try {
                 db = getDocumentBuilderFactory().newDocumentBuilder();
             } catch (ParserConfigurationException e) {
                 e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             }
         }
         return db;
     }
     protected void setDocumentBuilder(DocumentBuilder db) {
         this.db = db;
     }
 
    public String makeTargetUrl(String urlFormat, String address) {
         return String.format(urlFormat, address.replaceAll(" ", "+"));
     }
 
     private DocumentBuilderFactory dbf = null;
     protected DocumentBuilderFactory getDocumentBuilderFactory() {
         if (dbf == null)
             dbf = DocumentBuilderFactory.newInstance();
         return dbf;
     }
     protected void setDocumentBuilderFactory(DocumentBuilderFactory dbf) {
         this.dbf = dbf;
     }
 
 
    public String getResponse(String url) {
         HttpURLConnection connection = null;
         OutputStreamWriter wr = null;
         BufferedReader rd  = null;
         StringBuilder sb = null;
         String line = null;
 
         URL geocodingUrl = null;
 
         try {
             //set up out communications stuff
             connection = null;
             geocodingUrl = new URL(url);
 
             //Set up the initial connection
             connection = (HttpURLConnection)geocodingUrl.openConnection();
             connection.setRequestMethod("GET");
             connection.setDoOutput(true);
             connection.setReadTimeout(10000);
             connection.connect();
 
             //read the result from the server
             rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             sb = new StringBuilder();
 
             while ((line = rd.readLine()) != null) {
                 sb.append(line + '\n');
             }
 
         } catch (MalformedURLException e) {
             e.printStackTrace();
         } catch (ProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         finally {
             //close the connection, set all objects to null
             connection.disconnect();
             rd = null;
             wr = null;
             connection = null;
         }
 
         return sb.toString();
     }
 
 
 
 
     protected Coordinates getGpsCoordinatesForAddress(String response, String latXpath, String lonXpath) {
         Coordinates coords = null;
 
         try {
             Document doc = getDocumentBuilder().parse(new ByteArrayInputStream(response.getBytes()));
 
             XPathFactory xPathFactory =  XPathFactory.newInstance();
 
             XPathExpression latXpathExpression = xPathFactory.newXPath().compile(latXpath);
             XPathExpression lonXpathExpression = xPathFactory.newXPath().compile(lonXpath);
 
             String lat = ((Node)latXpathExpression.evaluate(doc, XPathConstants.NODE)).getNodeValue();
             String lon = ((Node)lonXpathExpression.evaluate(doc, XPathConstants.NODE)).getNodeValue();
 
             coords = new Coordinates(lat, lon);
         } catch (IOException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (XPathExpressionException e) {
             e.printStackTrace();
         }
 
         return coords;
     }
 
 }
