 package cz.muni.fi.pb138.log4jconverter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
import org.apache.log4j.PropertyConfigurator;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import cz.muni.fi.pb138.log4jconverter.InputLoader.Type;
 import cz.muni.fi.pb138.log4jconverter.configuration.Configuration;
 
 
 public class Main {
 	public static Type confOut = Type.OTHER;
     private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Main.class);
 
 
     public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
       PropertyConfigurator.configure("log4j.properties"); 
   
        Config config = new Config(args);
        
        setVerbose(config);
 
        InputLoader il;
        if(config.inputFile != null){
     	   il = new InputLoader(config.inputFile);
        }else{
     	   il = new InputLoader(System.in);
        }
        
        Parser p = null;
        Type itype = config.inputType;
        if( itype == null){
     	   itype = il.getType();
        }
 	   switch(itype){
 	   case PROPERTIES:
 		   p = new PropertiesParser(il.getProperties()); break;
 	   case XML:
 		   p = new XMLParser(il.getDOM()); break;
 	   case OTHER:
 		   logger.error("Unrecognized type of input file");
 		   System.exit(1);
 		   return;
 	   }
        
        Configuration c = p.parse();
        
        InputLoader.Type targetType = config.outputType;
        if(targetType == null){
     	   switch(itype){
     	   case PROPERTIES:
     		   targetType = InputLoader.Type.XML; break;
     	   case XML:
     		   targetType = InputLoader.Type.PROPERTIES; break;
     	   }
        }
        
        OutputStream os;
        if(config.outputFile != null){
     	   os = new FileOutputStream(new File(config.outputFile)); 
        }else{
     	   os = System.out;
        }
        
        
 	   switch(targetType){
 	   case PROPERTIES:
 		   Properties props = c.generateProperties();
 		   props.store(os, null);
 		   break;
 	   case XML:
 		   Document doc = c.generateXML();
 		   try {
 			serializetoXML(os,doc);
 		} catch (TransformerException e) {
 			logger.error("Failed to serialize XML.",e);
 		}
 		   break;
 	   }
 
        
        
                
     }
 	
 	private static void setVerbose(Config config) {
 		int verbose = config.verbose;
 		if(verbose > 3)
 			verbose = 3;
 		if(verbose < -2)
 			verbose = -2;
 		
 		switch(verbose){
 		case 1:
 			org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.INFO); break;
 		case 2:
 			org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.DEBUG); break;
 		case 3:
 			org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.TRACE); break;
 		case -1:
 			org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ERROR); break;
 		case -2:
 			org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF); break;
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private static void writeDifferentItems(Properties fromFileInput, Properties fromConfigurationInput) {
 		if (fromFileInput.equals(fromConfigurationInput)){
 			System.out.println("Properties are equals");
 			return;
 		}
 
 		for(Entry<Object, Object> e : fromFileInput.entrySet()){
 			String key = (String) e.getKey();
 			String value = (String) e.getValue();
 			if(!fromConfigurationInput.containsKey(key)){
 				System.out.println("-" + key + "=" + value);
 			}else if(!fromConfigurationInput.getProperty(key).equals(value)){
 				System.out.println("-" + key + "=" + value);
 				System.out.println("+" + key + "=" + fromConfigurationInput.getProperty(key));
 			}
 		}
 		for(Entry<Object, Object> e : fromConfigurationInput.entrySet()){
 			String key = (String) e.getKey();
 			String value = (String) e.getValue();
 			if(!fromFileInput.containsKey(key)){
 				System.out.println("+" + key + "=" + value);
 			}
 		}
 		
 	}
     
     // nvm ci sa to hodi do tejto classy
     private static void serializetoXML(OutputStream output, Document doc) throws TransformerException {
        // Vytvorime instanci tovarni tridy
        TransformerFactory factory = TransformerFactory.newInstance();
        // Pomoci tovarni tridy ziskame instanci tzv. kopirovaciho transformeru
        Transformer transformer = factory.newTransformer();
        // set DOCTYPE
        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "log4j.dtd");
        // nastaveni odsazovani
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        // Vstupem transformace bude dokument v pameti
        DOMSource source = new DOMSource(doc);
        // Vystupem transformace bude vystupni soubor
        StreamResult result = new StreamResult(output);
        // Provedeme transformaci
        transformer.transform(source, result);
    }
     
 }
