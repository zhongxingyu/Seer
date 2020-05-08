 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cyrilliclanguagehelper;
 
 import java.io.File;
 import java.util.ArrayList;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.SAXException;
 
 /**
  * @class Language
  * @author Dean Thomas
  */
 public class Language extends ArrayList {
     private File fileName;
     private XMLParser xmlParser;
     
     /**
      * Default constructor
      * 
      * Create an empty language structure
      */
     public Language()
     {
        xmlParser = new XMLParser();
        
         System.out.println("New language object created");
     }
     
     /** Load a language definition from an XML document
      * 
      * @param fileName the XML document containing the definitions
      */
     public Language(File fileName)
     {
         this();
      
         this.fileName = fileName;
         
         System.out.println(this.fileName.getName());
         
         xmlParser.parseXMLFile(fileName);
     }
     
 }
