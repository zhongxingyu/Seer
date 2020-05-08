 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
   EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 
 package org.easetech.easytest.codegen;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.sun.javadoc.DocErrorReporter;
 
 /**
  * An implementation of {@link IConfigurableStrategy,JUnitDocletProperties}. 
  * This class is responsible for providing configurations, loading property files like Template and seed data properties
  * 
  * @author Ravi Polampelli
  * 
  */
 
 public class ConfigurableStrategy extends BaseObject implements IConfigurableStrategy, JUnitDocletProperties {
 
     /**
      * An instance of logger associated.
      */
     protected static final Logger LOG = LoggerFactory.getLogger(ConfigurableStrategy.class);
 	
 	protected static final String DEFAULT_PROPERTY_FILE_NAME  = "junit4.properties";
 	protected static final String DEFAULT_SEED_DATA_FILE_NAME  = "seed_data.properties";
 
     private static Properties          properties;
     private static Properties          filterProperties;
     private static Properties          seedData;
     private static Map<String, String> templateCache;
 
     private        String              propertyFileName;
     private        String              filterPropertyFileName;
     private        String              seedDataFileName;
     private        DocErrorReporter    docErrorReporter;
 
     public ConfigurableStrategy() {
         init();
     }
 
     public void init() {
         templateCache = null;
         setProperties(null);
         setPropertyFileName(DEFAULT_PROPERTY_FILE_NAME);
         setDocErrorReporter(null);
     }
 
     public String getPropertyFileName() {
         return propertyFileName;
     }
 
     public void setPropertyFileName(String propertyFileName) {
         this.propertyFileName = propertyFileName;
     }
     
 	
 	
 	public String getSeedDataFileName() {
 		return seedDataFileName;
 	}
 	
 	public void setSeedDataFileName(String seedDataFileName) {
 		this.seedDataFileName = seedDataFileName;
 		
 	}
     
 
     public Properties getProperties() {
         if (properties == null) {
             properties = loadProperties(getPropertyFileName());
         }
         return properties;
     }
 
     public void setProperties(Properties properties) {
         this.properties = properties;
     }
 
 	public String getFilterPropertyFileName() {
 		return filterPropertyFileName;
 	}
 	
     public void setFilterPropertyFileName(String filterPropertyFileName) {
         this.filterPropertyFileName = filterPropertyFileName;
     }
 
 
 	public void setFilterProperties(Properties filterProperties) {
 		this.filterProperties = filterProperties;
 		
 	}
 
 	public Properties getFilterProperties() {
 		if (filterProperties == null) {
 			filterProperties = loadFilterProperties(getFilterPropertyFileName());
         }
         return filterProperties;
 	}
 
 
 
 	public void setSeedData(Properties seedData) {
 		this.seedData = seedData;
 		
 	}
 	
 	/**
      * Loads seed data
      */
 	public Properties getSeedData() {
 		if (seedData == null) {
 			seedData = loadSeedDataProperties(getSeedDataFileName());
         }
         return seedData;
 	}
 
     private Properties loadFilterProperties(String filterPropertyFileName) {
     	// get properties object containing system properties already
         Properties filterProperties = new Properties(System.getProperties());
 
 
         // read properties from file
         try {
             InputStream inputStream;
 
             inputStream = getPropertyInputStream(filterPropertyFileName);
             filterProperties.load(inputStream);
             
             LOG.debug("filterProperties after load:"+filterProperties);
 
 
         } catch (Exception e) {
             printError("Could not find property file " + filterPropertyFileName + ".");
         }
         return filterProperties;
 	}
   
 	/**
      * Loads seed data
      * 
      * @param seedDataFileName the input filename to load the seed data from
      */
     private Properties loadSeedDataProperties(String seedDataFileName) {
     	if(seedDataFileName == null) 
     		return null;
     	// get properties object containing system properties already
         Properties seedData = new Properties(System.getProperties());
 
         // read properties from file
         try {
             InputStream inputStream;
 
             inputStream = getPropertyInputStream(seedDataFileName);
             seedData.load(inputStream);
             
             LOG.debug("seedData after load:"+seedData);
 
 
         } catch (Exception e) {
             printError("Could not find property file " + seedDataFileName + ".");
         }
         return seedData;
 	}
     
 	/**
      * Loads template file into properties
      * 
      * @param propertyFileName the input template filename to load from
      */
     
 	public Properties loadProperties(String propertyFileName) {
         String licence;
 
         // get properties object containing system properties already
         Properties properties = new Properties(System.getProperties());
 
         properties.setProperty(LICENSE, VALUE_LICENSE);
         properties.setProperty(MARKER_IMPORT_BEGIN,             VALUE_MARKER_IMPORT_BEGIN);
         properties.setProperty(MARKER_IMPORT_END,               VALUE_MARKER_IMPORT_END);
         properties.setProperty(MARKER_EXTENDS_IMPLEMENTS_BEGIN, VALUE_MARKER_EXTENDS_IMPLEMENTS_BEGIN);
         properties.setProperty(MARKER_EXTENDS_IMPLEMENTS_END,   VALUE_MARKER_EXTENDS_IMPLEMENTS_END);
         properties.setProperty(MARKER_CLASS_BEGIN,              VALUE_MARKER_CLASS_BEGIN);
         properties.setProperty(MARKER_CLASS_END,                VALUE_MARKER_CLASS_END);
         properties.setProperty(MARKER_METHOD_BEGIN,             VALUE_MARKER_METHOD_BEGIN);
         properties.setProperty(MARKER_METHOD_END,               VALUE_MARKER_METHOD_END);
         properties.setProperty(MARKER_JAVADOC_CLASS_BEGIN,      VALUE_MARKER_JAVADOC_CLASS_BEGIN);
         properties.setProperty(MARKER_JAVADOC_CLASS_END,        VALUE_MARKER_JAVADOC_CLASS_END);
         properties.setProperty(MARKER_JAVADOC_METHOD_BEGIN,     VALUE_MARKER_JAVADOC_METHOD_BEGIN);
         properties.setProperty(MARKER_JAVADOC_METHOD_END,       VALUE_MARKER_JAVADOC_METHOD_END);
 
         // read properties from file
         try {
             InputStream inputStream;
 
             inputStream = getPropertyInputStream(propertyFileName);
             properties.load(inputStream);
             licence = getTemplate(properties, "licence", null);
             properties.setProperty(LICENSE, licence);
         } catch (Exception e) {
             printError("Could not find property file " + propertyFileName + ".");
         }
         return properties;
     }
 
 	/**
      * Gets the stream from property file name
      * 
      * @param propertyFileName the input template filename to load from
      */
     private InputStream getPropertyInputStream(String propertyFileName) {
         InputStream returnValue = null;
         File file;
 
         file = new File(propertyFileName);
         if (file.exists()) {
             try {
                 returnValue = new BufferedInputStream(new FileInputStream(file));
                 printNotice("Loading properties from "+file.getAbsoluteFile()+".");
             } catch (IOException e) {
                 printError("Could not load properties from "+file.getAbsoluteFile()+".");
             }
         } else {
             returnValue = getClass().getClassLoader().getResourceAsStream(propertyFileName);
             if (returnValue != null) {
                 printNotice("Loading properties "+propertyFileName+" from class path.");
             }
         }
 
         return returnValue;
     }
 
 	/**
      * Gets the particular part of template from properties and '.' separated template name and attribute name
      * Try to load the template from cache if available, otherwise creates it and puts it into cache
      * 
      * @param Properties properties the loaded properties
      * @param String templateName the root name of template
      * @param String attribute branch name inside of the root
      * @return String the particular part of template
      */
     public String getTemplate(Properties properties, String templateName, String attribute) {
 
         String returnValue       = null;
         String qualifiedTemplate;
 
         if (isNull(templateCache)) {
             templateCache = new HashMap<String, String>();
         }
 
         if (isNotNull(templateName)) {
             if (isNotNull(attribute)) {
                 qualifiedTemplate = "template." + templateName + "." + attribute;
             } else {
                 qualifiedTemplate = "template." + templateName;
             }
 
             if (!templateCache.containsKey(qualifiedTemplate)) {
                 templateCache.put(qualifiedTemplate, createTemplate(properties, qualifiedTemplate));
             }
 
             returnValue = (String) templateCache.get(qualifiedTemplate);
         } else {
             printError("ConfigurableStrategy.getTemplate() templateName == " + templateName + " attribute == " + attribute);
         }
 
         return returnValue;
     }
     
     /**
      * Create the template from properties and branch template name
      *       
      * @param Properties properties the loaded properties
      * @param String templateName the root name of template
      * @return String the particular part of template
      */
     
     public String createTemplate(Properties properties, String templateName) {
     	LOG.debug("createTemplate started, properties:"+properties+"\ntemplateName:"+templateName);
         Map<Integer, String> lines;
         Enumeration  enumeration;
         String       propLine;
         Iterator     iterator;
         Integer      key;
         StringBuffer sb;
 
         // using TreeMap, because it's just cool to have everything sorted
         // Hint: template properties look like this
         // template.name.attribute.000=//first line of template
 
         lines       = new TreeMap<Integer, String>();
         enumeration = properties.propertyNames();
 
         while (enumeration.hasMoreElements()) {
             propLine = (String) enumeration.nextElement();
             //LOG.debug("propLine :"+propLine);
             if (propLine.startsWith(templateName + ".")) {
                 key = new Integer(propLine.substring(templateName.length() + 1));
                 LOG.debug("key :"+key);
                 LOG.debug("properties.getProperty(propLine) :"+properties.getProperty(propLine));
                 lines.put(key, properties.getProperty(propLine));
             }
         }
 
         sb       = new StringBuffer();
         iterator = lines.values().iterator();
 
         while (iterator.hasNext()) {
             sb.append((String) iterator.next());
             sb.append("\n");
         }
         LOG.debug("createTemplate finished:"+sb.toString());
         return sb.toString();
     }
 
     public void setDocErrorReporter(DocErrorReporter reporter) {
         docErrorReporter = reporter;
     }
 
     public void printNotice(String msg) {
         if (isNotNull(docErrorReporter)) {
             docErrorReporter.printNotice(msg);
         } else {
             System.out.println(msg);
         }
     }
 
     public void printWarning(String msg) {
         if (isNotNull(docErrorReporter)) {
             docErrorReporter.printWarning(msg);
         } else {
             System.err.println(msg);
         }
     }
 
     public void printError(String msg) {
         if (isNotNull(docErrorReporter)) {
             docErrorReporter.printError(msg);
         } else {
             System.err.println(msg);
         }
     }
 
 
 
 
 }

