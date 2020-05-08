 
 package de.philweb.bubblr.tools;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.badlogic.gdx.Gdx;
  
 public class LanguageManager {
         private static final String DEFAULT_LANGUAGE = "en_UK";
        
         private String m_languagesFile = null;
         private HashMap<String, String> m_language = null;
         private String m_languageName = null;
        
 //        private static final Logger m_logger = Game.getLogger();
 //        private static final PlatformResolver m_platformResolver = Game.getPlarformResolver();
        
         public LanguageManager(String languagesName) {
                 this("data/languages.xml", languagesName);
         }
        
         public LanguageManager(String languagesFile, String languagesName) {           
                 // Languages file
                 m_languagesFile = languagesFile;
                
                 // Create language map
                 m_language = new HashMap<String, String>();
                
                 // Default language (system language)
                 m_languageName = languagesName;
                
 //                if (m_languageName.equals("") && m_platformResolver != null) {
 //                        m_languageName = m_platformResolver.getDefaultLanguage();
 //                }
                
               if (m_languageName.equals("")) m_languageName = java.util.Locale.getDefault().toString();
                 
                 //  Try to load selected language, if it fails, load default one
                 if (!loadLanguage(m_languageName)) {
                         loadLanguage(DEFAULT_LANGUAGE);
                 }
         }
        
         public String getLanguagesFile() {
                 return m_languagesFile;
         }
        
         public void setLanguagesFile(String languagesFile) {
 //        	System.out.println("LanguageManager: setting languages file to " + languagesFile);
                 m_languagesFile = languagesFile;
         }
        
         public String getLanguage() {
                 return m_languageName;
         }
        
         public boolean loadLanguage() {
                 return loadLanguage(m_languageName);
         }
        
         
         public boolean loadLanguage(String languageName) {
 //        	System.out.println("LanguageManager: try to load: " + languageName);
                       
         	
         	
             try {
                 DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                 DocumentBuilder builder = factory.newDocumentBuilder();
                 InputSource input = new InputSource(Gdx.files.internal(m_languagesFile).read());
                 input.setEncoding("UTF-8");
                 Document document = builder.parse(input);
 
                 NodeList languages = document.getElementsByTagName("language");
                 
 //                Gdx.app.log("languages", "items: " + languages.getLength());
                 
              // Iterate over languages, trying to find the selected one
                 for (int i = 0, j = languages.getLength(); i < j; i++) {
                    
                 	Node language = languages.item(i);
                    
 //                	Gdx.app.log("languages", "language: " + language.getAttributes().getNamedItem("name").getNodeValue());
                 	
                	if (language.getAttributes().getNamedItem("name").getTextContent().equals(languageName)) {
                 		
                 		NodeList strings = language.getChildNodes();	// get all strings
                 		
                 		for (int k = 0, l = strings.getLength(); k < l; k++) {
                 			
                 			Node string = strings.item(k);
                 			NamedNodeMap attributes = string.getAttributes();
                 			
                 			if (attributes != null) {
                 				
                 				String key = attributes.getNamedItem("key").getNodeValue();
     							String value = attributes.getNamedItem("value").getNodeValue();
 //    							String key = attributes.getNamedItem("key").getTextContent();
 //    							String value = attributes.getNamedItem("value").getTextContent();
 //    							value = value.replace("<br />", "\n");
     							m_language.put(key, value);
 //    							System.out.println("LanguageManager: loading key " + key);
                 			}
                 		}
                 		
 						m_languageName = languageName;
 //						System.out.println("LanguageManager: " + languageName + " language sucessfully loaded");
 						
 						return true; 
                 	}
                 }
                 
              } catch (ParserConfigurationException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
              } catch (SAXException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
              } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
              }
             
             return false; 
         }
  
         
         
         public String getString(String key) {
                 if (m_language != null) {
                         // Look for string in selected language
                         String string = m_language.get(key);
                        
                         if (string != null) {
                                 return string;
                         }
                 }
        
                 // Key not found, return the key itself
 //                System.out.println("LanguageManager: string " + key + " not found");		// TODO: activate for testing
                 return key;
         }
  
         // Not compatible with HTML5
         public String getString(String key, Object... args) {
                 return String.format(getString(key), args);
         }
         
         
         
         
         
 //===== methods for parsing DOM Xml==========================================
 //        
 // http://www.drdobbs.com/jvm/easy-dom-parsing-in-java/231002580
         
         
         protected Node getNode(String tagName, NodeList nodes) {
             for ( int x = 0; x < nodes.getLength(); x++ ) {
                 Node node = nodes.item(x);
                 if (node.getNodeName().equalsIgnoreCase(tagName)) {
                     return node;
                 }
             }
          
             return null;
         }
          
         protected String getNodeValue( Node node ) {
             NodeList childNodes = node.getChildNodes();
             for (int x = 0; x < childNodes.getLength(); x++ ) {
                 Node data = childNodes.item(x);
                 if ( data.getNodeType() == Node.TEXT_NODE )
                     return data.getNodeValue();
             }
             return "";
         }
          
         protected String getNodeValue(String tagName, NodeList nodes ) {
             for ( int x = 0; x < nodes.getLength(); x++ ) {
                 Node node = nodes.item(x);
                 if (node.getNodeName().equalsIgnoreCase(tagName)) {
                     NodeList childNodes = node.getChildNodes();
                     for (int y = 0; y < childNodes.getLength(); y++ ) {
                         Node data = childNodes.item(y);
                         if ( data.getNodeType() == Node.TEXT_NODE )
                             return data.getNodeValue();
                     }
                 }
             }
             return "";
         }
          
         protected String getNodeAttr(String attrName, Node node ) {
             NamedNodeMap attrs = node.getAttributes();
             for (int y = 0; y < attrs.getLength(); y++ ) {
                 Node attr = attrs.item(y);
                 if (attr.getNodeName().equalsIgnoreCase(attrName)) {
                     return attr.getNodeValue();
                 }
             }
             return "";
         }
          
         protected String getNodeAttr(String tagName, String attrName, NodeList nodes ) {
             for ( int x = 0; x < nodes.getLength(); x++ ) {
                 Node node = nodes.item(x);
                 if (node.getNodeName().equalsIgnoreCase(tagName)) {
                     NodeList childNodes = node.getChildNodes();
                     for (int y = 0; y < childNodes.getLength(); y++ ) {
                         Node data = childNodes.item(y);
                         if ( data.getNodeType() == Node.ATTRIBUTE_NODE ) {
                             if ( data.getNodeName().equalsIgnoreCase(attrName) )
                                 return data.getNodeValue();
                         }
                     }
                 }
             }
          
             return "";
         }
 }
 
 
 
 
 //------------------------------------------------
 //package de.philweb.bubblr.tools;
 //
 //import java.util.HashMap;
 //
 //import com.badlogic.gdx.Gdx;
 //import com.badlogic.gdx.utils.Array;
 //import com.badlogic.gdx.utils.XmlReader;
 //import com.badlogic.gdx.utils.XmlReader.Element;
 // 
 //public class LanguageManager {
 //        private static final String DEFAULT_LANGUAGE = "en_UK";
 //       
 //        private String m_languagesFile = null;
 //        private HashMap<String, String> m_language = null;
 //        private String m_languageName = null;
 //       
 ////        private static final Logger m_logger = Game.getLogger();
 ////        private static final PlatformResolver m_platformResolver = Game.getPlarformResolver();
 //       
 //        public LanguageManager(String languagesName) {
 //                this("data/languages.xml", languagesName);
 //        }
 //       
 //        public LanguageManager(String languagesFile, String languagesName) {           
 //                // Languages file
 //                m_languagesFile = languagesFile;
 //               
 //                // Create language map
 //                m_language = new HashMap<String, String>();
 //               
 //                // Default language (system language)
 //                m_languageName = languagesName;
 //               
 ////                if (m_languageName.equals("") && m_platformResolver != null) {
 ////                        m_languageName = m_platformResolver.getDefaultLanguage();
 ////                }
 //               
 //              if (m_languageName.equals("")) m_languageName = java.util.Locale.getDefault().toString();
 //                
 //                //  Try to load selected language, if it fails, load default one
 //                if (!loadLanguage(m_languageName)) {
 //                        loadLanguage(DEFAULT_LANGUAGE);
 //                }
 //        }
 //       
 //        public String getLanguagesFile() {
 //                return m_languagesFile;
 //        }
 //       
 //        public void setLanguagesFile(String languagesFile) {
 ////        	System.out.println("LanguageManager: setting languages file to " + languagesFile);
 //                m_languagesFile = languagesFile;
 //        }
 //       
 //        public String getLanguage() {
 //                return m_languageName;
 //        }
 //       
 //        public boolean loadLanguage() {
 //                return loadLanguage(m_languageName);
 //        }
 //       
 //        public boolean loadLanguage(String languageName) {
 ////        	System.out.println("LanguageManager: try to load: " + languageName);
 //                try {
 //                        // Parse xml document
 //                        XmlReader reader = new XmlReader();
 ////                        System.out.println("LanguageManager: new XmlReader() done.");
 //                        	
 //                        Element root = reader.parse(Gdx.files.internal(m_languagesFile).read());
 ////                        System.out.println("LanguageManager: reader.parse(...) done.");
 //                        
 //                        Array<Element> languages = root.getChildrenByName("language");
 ////                        System.out.println("LanguageManager: Array<Element> languages =.... done.");
 //                        
 //                        // Iterate over languages, trying to find the selected one
 //                        for (int i = 0; i < languages.size; ++i) {
 //                                Element language = languages.get(i);
 //                               
 //                                if (language.getAttribute("name").equals(languageName)) {
 //                                        // Clear the previous language
 //                                        m_language.clear();
 //                                        Element languageElement = (Element)language;
 //                                        Array<Element> strings = languageElement.getChildrenByName("string");
 //                                       
 //                                        // Load all the strings for that language
 //                                        for (int j = 0; j < strings.size; ++j) {
 //                                                Element stringNode = strings.get(j);
 //                                                String key = stringNode.getAttribute("key");
 //                                                String value = stringNode.getAttribute("value");
 //                                                value = value.replace("<br />", "\n");
 //                                                m_language.put(key, value);
 ////                                                System.out.println("LanguageManager: loading key " + key);
 //                                        }
 //                                       
 //                                        m_languageName = languageName;
 //                                       
 ////                                        System.out.println("LanguageManager: " + languageName + " language sucessfully loaded");
 //                                       
 //                                        return true;
 //                                }
 //                        }
 //                }
 //                catch (Exception e) {
 ////                	System.out.println("LanguageManager: error loading File: " + m_languagesFile + " language: " + languageName);
 ////                	System.out.println("error: " + e);
 //                        return false;
 //                }
 //               
 ////                System.out.println("LanguageManager: couldn´t load language " + languageName);
 //               
 //                return false;
 //        }
 // 
 //        public String getString(String key) {
 //                if (m_language != null) {
 //                        // Look for string in selected language
 //                        String string = m_language.get(key);
 //                       
 //                        if (string != null) {
 //                                return string;
 //                        }
 //                }
 //       
 //                // Key not found, return the key itself
 ////                System.out.println("LanguageManager: string " + key + " not found");		// TODO: activate for testing
 //                return key;
 //        }
 // 
 //        // Not compatible with HTML5
 //        public String getString(String key, Object... args) {
 //                return String.format(getString(key), args);
 //        }
 //}
 
 
