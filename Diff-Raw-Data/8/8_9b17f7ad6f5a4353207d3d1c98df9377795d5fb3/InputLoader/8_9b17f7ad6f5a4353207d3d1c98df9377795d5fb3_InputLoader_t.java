 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cz.muni.fi.pb138.log4jconverter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.FilenameUtils;
 import org.w3c.dom.Document;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * This class represents basic operations for loading
  * files. 
  * 
  * @author fivekeyem
  */
 public class InputLoader {
     
     
     public enum Type {
         XML, PROPERTIES, OTHER
     }    
     private Document xmlDoc = null;
     private Properties propertiesDoc = null;
     
     private String nameOfFile;
     private BufferedReader in;
     private InputStream is;
         
     private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(InputLoader.class);
     
     
     public InputLoader(InputStream is) {
         if (is == null) {
             throw new IllegalArgumentException("No input stream");
         }
     	this.is = is;
         this.in = new BufferedReader(new InputStreamReader(is));
     }
     
     public InputLoader(String nameOfFile) throws FileNotFoundException {
         if (nameOfFile == null) {
             throw new IllegalArgumentException("No name of file");
         }
         if (nameOfFile.isEmpty()) {
             throw new IllegalArgumentException("Name is empty");
         }  
         this.nameOfFile = nameOfFile;
         this.is = new FileInputStream(new File(nameOfFile));
         this.in = new BufferedReader(new InputStreamReader(is)); 
         logger.info("File "+ nameOfFile +" is loaded");
     }
     
     
     // for now we recognize typ of input according to extension of file
     // in the future we'll do it according content
     
     
     /** 
      * Returns type of input file
      * 
      * @return             type of input file    
      * @throws IOException If an input exception occurred
      *    
      */
     public Type getType() throws IOException {
     	if(nameOfFile != null){
 	        String extension = FilenameUtils.getExtension(nameOfFile);
 	        if (extension.equals("xml")) {
 	            return Type.XML;
 	        } else if (extension.equals("properties")) {
 	            return Type.PROPERTIES;
 	        }
         }
     	return getInputStreamType();
     }
     /** 
      * Returns type of input Stream
      * 
      * @return             type of input Stream
      * @throws IOException If an input exception occurred
  *                      
      */
     private Type getInputStreamType() throws IOException{
     	Type t = Type.OTHER;
 		in.mark(1000);
     	String line = in.readLine();
     	
     	if(line == null) return Type.OTHER;
     
     	if(line.startsWith("<?xml ")){
     		t = Type.XML;
     	}else{
     		while(line.matches("\\s*")){
     			line = in.readLine();
     		}
     		//                            '\\'    '\ '  '\='  '\:'
     		if(line.matches("\\s*([#!]|(\\\\\\\\|\\\\ |\\\\=|\\\\:|[^ =:])+(\\s+|\\s*[=:]?\\s*).+)")){
         		t = Type.PROPERTIES;
     		}
     	}
 
     	in.reset();
 		return t;
     }
    /** Returns Document object model from InputStream
      * 
      * @return                               Document object model
      * @throws  IOException                  if input exception occured
      * @throws  ParserConfigurationException if parserConfigurationException exception occured
      * @throws  SAXException                 if some validation errors occured
      */
     
     public Document getDOM() throws ParserConfigurationException, IOException, SAXException {
     	
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         
         builder.setEntityResolver(new EntityResolver() {
             @Override
             public InputSource resolveEntity(String publicId, String systemId)
                     throws SAXException, IOException {
                 if (systemId.contains("log4j.dtd")) {
                    return new InputSource(this.getClass().getResourceAsStream("/log4j.dtd"));
                 } else {
                     return null;
                 }
             }
         });
         
         if (logger.isTraceEnabled()) { logger.trace("DocumentBuilder is ok"); }
         
         builder.setErrorHandler(new org.xml.sax.ErrorHandler() {
             //Ignore the fatal errors
             public void fatalError(SAXParseException exception) throws SAXException { }
             
             //Validation errors 
             public void error(SAXParseException e) throws SAXParseException {
                 System.out.println("Error at " +e.getLineNumber() + " line.");
                 System.out.println(e.getMessage());
                 System.exit(0);
             }
             
             //Show warnings
             public void warning(SAXParseException err) throws SAXParseException {
                 System.out.println(err.getMessage());
                 System.exit(0);
             }
         });
         if (logger.isTraceEnabled()) { logger.trace("setErroHanler is ok"); }
         
         xmlDoc = builder.parse(this.is);
         return xmlDoc;
     }
    /**
      * Returns Properties document loaded from Buffered reader
      * 
      * @return             Properties document
      * @throws IOException if input exception occured
      */
     
     public Properties getProperties() throws IOException {
         propertiesDoc = new Properties();
         propertiesDoc.load(this.in);
         return propertiesDoc;
     }
     
     public String getNameOfFile() {
         return nameOfFile;
     }
 }
