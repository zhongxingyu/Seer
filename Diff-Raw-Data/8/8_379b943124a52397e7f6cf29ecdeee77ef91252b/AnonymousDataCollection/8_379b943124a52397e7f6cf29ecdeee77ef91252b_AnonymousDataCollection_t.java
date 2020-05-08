 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.opensimkit.utilities;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 /**
  *
  * @author ahmedmaawy
  */
 public class AnonymousDataCollection {
     private String binaryPath = null;
     private String pathToFile = "";
     
     private final String COLLECT_ABOUT_USE = "collect_about_use";
     private final String COLLECT_TURNED_ON = "collect_turned_on";
     private final String xmlFileName = "AppSettings.xml";
     
     private boolean settingsFileExists = false;
     
     private boolean collectAboutUseEnabled = false;
     private boolean collectTurnedOnEnabled = false;
     
     /**
      * Class constructor
      */
     
     public AnonymousDataCollection()
     {
         // Get the current running path
         
         String path = AnonymousDataCollection.class.getProtectionDomain().getCodeSource().getLocation().getPath();
         
         try {
             binaryPath = URLDecoder.decode(path, "UTF-8");
             
             pathToFile = StringUtil.trimRight(binaryPath, '/');
            
            int jarNameIndex = pathToFile.indexOf("/OpenSIMKit.jar");
            
            if(jarNameIndex > -1)
            {
                pathToFile = StringUtil.trimRight((pathToFile.substring(0, jarNameIndex)), '/');
            }
            
             pathToFile = pathToFile.concat("/" + xmlFileName);
             
             settingsFileExists = checkIfFileExists();
             
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(AnonymousDataCollection.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     /**
      * Checks to see if the settings file exists
      * 
      * @return 
      */
     
     private boolean checkIfFileExists()
     {
         File settingsFile = new File(pathToFile);
         
         // File exists ?
         if(settingsFile.exists()) {
             // Can we parse the XML for desired values ?
             return readXMLFile();
         }
         
         return false;
     }
     
     /**
      * Read settings XML file
      * 
      * @return 
      */
     
     private boolean readXMLFile()
     {
         try 
         {
             File fXmlFile = new File(pathToFile);
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(fXmlFile);
 
             doc.getDocumentElement().normalize();
 
             NodeList useList = doc.getElementsByTagName(this.COLLECT_ABOUT_USE);
             NodeList turnOnList = doc.getElementsByTagName(this.COLLECT_TURNED_ON);
             
             String useListValue = useList.item(0).getTextContent();
             String turnOnValue = turnOnList.item(0).getTextContent();
             
             collectAboutUseEnabled = Boolean.parseBoolean(useListValue);
             collectTurnedOnEnabled = Boolean.parseBoolean(turnOnValue);
             
         } catch (Exception ex) {
             Logger.getLogger(AnonymousDataCollection.class.getName()).log(Level.SEVERE, null, ex);
 
             return false;
         }
         
         return true;
     }
     
     /**
      * Saves settings to an XML File
      * 
      * @param collectAboutUse
      * @param collectTurnedOn
      * @return boolean
      */
     
     public boolean saveXMLSettings(boolean collectAboutUse, boolean collectTurnedOn)
     {
         try 
         {
             DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
             // root elements
             
             Document doc = docBuilder.newDocument();
             Element rootElement = doc.createElement("settings");
             doc.appendChild(rootElement);
 
             // Settings elements
             
             // Collect about use
             
             Element aboutUse = doc.createElement(this.COLLECT_ABOUT_USE);
             
             if(collectAboutUse) {
                 aboutUse.appendChild(doc.createTextNode("true"));
             }
             else {
                 aboutUse.appendChild(doc.createTextNode("false"));
             }
             
             rootElement.appendChild(aboutUse);
             
             // Collect if turned on
             
             Element turnedOn = doc.createElement(this.COLLECT_TURNED_ON);
             
             if(collectTurnedOn) {
                 turnedOn.appendChild(doc.createTextNode("true"));
             }
             else {
                 turnedOn.appendChild(doc.createTextNode("false"));
             }
             
             rootElement.appendChild(turnedOn);
             
             // write the content into xml file
             
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             DOMSource source = new DOMSource(doc);
             
             StreamResult result = new StreamResult(new File(pathToFile));
 
             // Output to console for testing
             // StreamResult result = new StreamResult(System.out);
 
             transformer.transform(source, result);
         } catch (ParserConfigurationException ex) {
             Logger.getLogger(AnonymousDataCollection.class.getName()).log(Level.SEVERE, null, ex);
             
             return false;
         } catch (TransformerException ex) {
             Logger.getLogger(AnonymousDataCollection.class.getName()).log(Level.SEVERE, null, ex);
             
             return false;
         }
         
         return true;
     }
     
     /**
      * File exists property
      * 
      * @return 
      */
 
     public boolean isSettingsFileExists() {
         return settingsFileExists;
     }
     
     /**
      * Collect data about the use property
      * 
      * @return 
      */
 
     public boolean isCollectAboutUseEnabled() {
         return collectAboutUseEnabled;
     }
     
     /**
      * Collect data on being turned on property
      * 
      * @return 
      */
 
     public boolean isCollectTurnedOnEnabled() {
         return collectTurnedOnEnabled;
     }
 }
