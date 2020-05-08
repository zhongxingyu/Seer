 package org.jboss.pressgang.ccms.utils.common;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * A set of utilities to read from the local application resources.
  */
 public class ResourceUtilities {
     /**
      * Reads a resource file and converts it into a String
      *
      * @param location The location of the resource file.
      * @param fileName The name of the file.
      * @return The string that represents the contents of the file or null if an error occurred.
      */
     public static String resourceFileToString(final String location, final String fileName) {
         if (location == null || fileName == null) return null;
 
         final InputStream in = ResourceUtilities.class.getResourceAsStream(location + fileName);
         if (in == null) return null;
 
         final BufferedReader br = new BufferedReader(new InputStreamReader(in));
         String line;
         final StringBuilder output = new StringBuilder("");
         try {
             while ((line = br.readLine()) != null) {
                 output.append(line + "\n");
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return output.toString();
     }
 
     /**
      * Reads a resource file and converts it into a XML based Document object.
      *
      * @param location The location of the resource file.
      * @param fileName The name of the file.
      * @return The Document that represents the contents of the file or null if an error occurred.
      */
     public static Document resourceFileToXMLDocument(final String location, final String fileName) {
         if (location == null || fileName == null) return null;
 
         final InputStream in = ResourceUtilities.class.getResourceAsStream(location + fileName);
         if (in == null) return null;
 
         final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder dBuilder;
         Document doc = null;
         try {
             dBuilder = dbFactory.newDocumentBuilder();
             doc = dBuilder.parse(in);
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return doc;
     }
 }
