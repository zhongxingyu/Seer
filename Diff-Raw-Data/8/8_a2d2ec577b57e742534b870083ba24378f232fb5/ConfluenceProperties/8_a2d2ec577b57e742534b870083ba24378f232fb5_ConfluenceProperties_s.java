 package org.iplantc.de.server;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 
 /**
  * Contains methods for accessing the confluence.properties file.
  * 
  * @author hariolf
  * 
  */
 public class ConfluenceProperties {
     private final Logger LOGGER = Logger.getLogger(ConfluenceProperties.class);
 
     // The prefix for all of the properties.
     private static final String PREFIX = "org.iplantc.discoveryenvironment.confluence"; //$NON-NLS-1$
 
     private static final String CONFLUENCE_BASE_URL = PREFIX + ".baseUrl"; //$NON-NLS-1$
     private static final String CONFLUENCE_PARENT_PAGE = PREFIX + ".parentPageName"; //$NON-NLS-1$
     private static final String CONFLUENCE_USER = PREFIX + ".user"; //$NON-NLS-1$
     private static final String CONFLUENCE_PASSWORD = PREFIX + ".password"; //$NON-NLS-1$
     private static final String CONFLUENCE_SPACE_NAME = PREFIX + ".spaceName"; //$NON-NLS-1$
     private static final String CONFLUENCE_SPACE_URL = PREFIX + ".spaceUrl"; //$NON-NLS-1$
     /** this is an internationalized string #translate #i18n */
     private static final String CONFLUENCE_COMMENT_SUFFIX = PREFIX + ".ratingCommentSuffix"; //$NON-NLS-1$
     private Properties properties;
 
     /**
      * The list of required properties.
      */
     private final static String[] REQUIRED_PROPERTIES = {CONFLUENCE_BASE_URL, CONFLUENCE_PARENT_PAGE,
             CONFLUENCE_USER, CONFLUENCE_PASSWORD, CONFLUENCE_SPACE_NAME, CONFLUENCE_SPACE_URL,
             CONFLUENCE_COMMENT_SUFFIX};
 
     /**
      * Creates a new ConfluenceProperties object and loads properties.
      * 
      * @param propFileName the name of the properties file
      */
     public ConfluenceProperties(String propFileName) {
         loadProperties(propFileName);
         validateProperties(REQUIRED_PROPERTIES);
     }
 
     /**
      * Validates that we have values for all required properties.
      * 
      * @throws RuntimeException if a required property isn't found in the file
      */
     private void validateProperties(String[] propertyNames) {
         for (String propertyName : propertyNames) {
             String propertyValue = properties.getProperty(propertyName);
             if (propertyValue == null || propertyValue.equals("")) { //$NON-NLS-1$
                 throw new RuntimeException("missing required property: " + propertyName); //$NON-NLS-1$
             }
         }
     }
 
     /**
      * Loads the discovery environment properties. If an error occurs while loading the file, we log the
      * message, but do not throw an exception; the property validation will catch any required properties
      * that are missing.
      * 
      * @param propFileName the name of the properties file
      */
     private void loadProperties(String propFileName) {
         properties = new Properties();
         InputStream stream = ConfluenceProperties.class.getResourceAsStream(propFileName);
         try {
             properties.load(stream);
         } catch (IOException e) {
             String msg = "unable to load discovery environment properties"; //$NON-NLS-1$
             LOGGER.error(msg, e);
        }
 finally { // FIXME eclipse auto indent fail
             if (stream != null) {
                 try {
                     stream.close();
                 } catch (IOException e) {
                     LOGGER.error("Cannot close input stream for file " + propFileName, e); //$NON-NLS-1$
        }
    }
         }
     }
 
     /**
      * Gets the base URL used to access the Confluence wiki.
      * 
      * @return the URL as a string.
      */
     public String getConfluenceBaseUrl() {
         return properties.getProperty(CONFLUENCE_BASE_URL);
     }
 
     /**
      * Gets the name of the 'List of Applications' page.
      * 
      * @return the name as a string.
      */
     public String getConfluenceParentPage() {
         return properties.getProperty(CONFLUENCE_PARENT_PAGE);
     }
 
     /**
      * Gets the Confluence user for adding documentation pages.
      * 
      * @return the user name
      */
     public String getConfluenceUser() {
         return properties.getProperty(CONFLUENCE_USER);
     }
 
     /**
      * Gets the Confluence password for adding documentation pages.
      * 
      * @return the password
      */
     public String getConfluencePassword() {
         return properties.getProperty(CONFLUENCE_PASSWORD);
     }
 
     /**
      * Gets the name of the 'DE Applications' space in Confluence.
      * 
      * @return the name as a string.
      */
     public String getConfluenceSpaceName() {
         return properties.getProperty(CONFLUENCE_SPACE_NAME);
     }
 
     /**
      * Gets the URL of the 'DE Applications' space in Confluence.
      * 
      * @return the URL as a string.
      */
     public String getConfluenceSpaceUrl() {
         return properties.getProperty(CONFLUENCE_SPACE_URL);
     }
 
     /**
      * Gets localized text that is added to rating comments for the wiki; contains a placeholder for the
      * current DE user.
      * 
      * @return a string representing the localized text.
      */
     public String getRatingCommentSuffix() {
         return properties.getProperty(CONFLUENCE_COMMENT_SUFFIX);
     }
 }
