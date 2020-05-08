 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.configuration;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.core.io.Resource;
 import org.springframework.core.io.support.ResourcePatternResolver;
 
 import de.escidoc.core.common.exceptions.EscidocException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 
 /**
  * Handles properties.
  * 
  * @author Michael Hoppe
  */
 public final class EscidocConfiguration {
 
     private static final Pattern PATTERN_PROPERTY_SPLIT = Pattern.compile("\\}.*?\\$\\{");
 
     private static final Pattern PATTERN_PROPERTY_FIND = Pattern.compile(".*?\\$\\{(.+?)\\}.*");
 
     public static final String SEARCH_PROPERTIES_DIRECTORY = "search.properties.directory";
 
     public static final String GSEARCH_URL = "gsearch.url";
 
     public static final String GSEARCH_PASSWORD = "gsearch.fedoraPass";
 
     public static final String FEDORA_URL = "fedora.url";
 
     public static final String FEDORA_USER = "fedora.user";
 
     public static final String TRIPLESTORE_DDL_GENERATOR = "triplestore.ddlgenerator.class";
 
     public static final String BUILD_NUMBER = "escidoc-core.build";
 
     public static final String ADMIN_EMAIL = "escidoc-core.admin-email";
 
     public static final String ESCIDOC_REPOSITORY_NAME = "escidoc-core.repository-name";
 
     public static final String FEDORA_PASSWORD = "fedora.password";
 
     public static final String ESCIDOC_CORE_NOTIFY_INDEXER_ENABLED = "escidoc-core.notify.indexer.enabled";
 
     public static final String ESCIDOC_CORE_BASEURL = "escidoc-core.baseurl";
 
     // use method appendToSelfURL() to access the self URL
     private static final String ESCIDOC_CORE_SELFURL = "escidoc-core.selfurl";
 
     public static final String ESCIDOC_CORE_PROXY_HOST = "escidoc-core.proxyHost";
 
     public static final String ESCIDOC_CORE_PROXY_PORT = "escidoc-core.proxyPort";
 
     public static final String ESCIDOC_CORE_NON_PROXY_HOSTS = "escidoc-core.nonProxyHosts";
 
     public static final String ESCIDOC_CORE_XSD_PATH = "escidoc-core.xsd-path";
 
     public static final String ESCIDOC_CORE_OM_CONTENT_CHECKSUM_ALGORITHM =
         "escidoc-core.om.content.checksum-algorithm";
 
     public static final String ESCIDOC_CORE_XSLT_STD = "escidoc-core.xslt.std";
 
     public static final String ESCIDOC_CORE_IDENTIFIER_PREFIX = "escidoc-core.identifier.prefix";
 
     public static final String ESCIDOC_CORE_PID_SYSTEM_FACTORY = "escidoc-core.PidSystemFactory";
 
     public static final String ESCIDOC_CORE_DUMMYPID_GLOBALPREFIX = "escidoc-core.dummyPid.globalPrefix";
 
     public static final String ESCIDOC_CORE_DUMMYPID_LOCALPREFIX = "escidoc-core.dummyPid.localPrefix";
 
     public static final String ESCIDOC_CORE_DUMMYPID_NAMESPACE = "escidoc-core.dummyPid.pidNamespace";
 
     public static final String ESCIDOC_CORE_DUMMYPID_SEPARATOR = "escidoc-core.dummyPid.separator";
 
     public static final String ESCIDOC_CORE_USERHANDLE_LIFETIME = "escidoc-core.userHandle.lifetime";
 
     public static final String ESCIDOC_CORE_USERHANDLE_COOKIE_LIFETIME = "escidoc-core.userHandle.cookie.lifetime";
 
     public static final String ESCIDOC_CORE_USERHANDLE_COOKIE_VERSION = "escidoc-core.userHandle.cookie.version";
 
     public static final String ESCIDOC_CORE_PID_SERVICE_HOST = "escidoc-core.PidSystemRESTService.host";
 
     public static final String ESCIDOC_CORE_PID_RESTSERVICE_USER = "escidoc-core.PidSystemRESTService.user";
 
     public static final String ESCIDOC_CORE_PID_RESTSERVICE_PASSWORD = "escidoc-core.PidSystemRESTService.password";
 
     public static final String ESCIDOC_CORE_PID_GLOBALPREFIX = "escidoc-core.PidSystem.globalPrefix";
 
     public static final String ESCIDOC_CORE_PID_LOCALPREFIX = "escidoc-core.PidSystem.localPrefix";
 
     public static final String ESCIDOC_CORE_PID_NAMESPACE = "escidoc-core.PidSystem.namespace";
 
     public static final String ESCIDOC_CORE_PID_SEPARATOR = "escidoc-core.PidSystem.separator";
 
     public static final String DE_FIZ_ESCIDOC_OM_SERVICE_PROVIDER_URL = "de.escidoc.core.om.service.provider.url";
 
     public static final String DE_FIZ_ESCIDOC_SM_SERVICE_PROVIDER_URL = "de.escidoc.core.sm.service.provider.url";
 
     public static final String ESCIDOC_CORE_DEFAULT_JNDI_URL = "escidoc-core.default.jndi.url";
 
     public static final String SM_FRAMEWORK_SCOPE_ID = "sm.framework.scope.id";
 
     public static final String ESCIDOC_CORE_QUEUE_USER = "escidoc-core.queue.user";
 
     public static final String ESCIDOC_CORE_QUEUE_PASSWORD = "escidoc-core.queue.password";
 
     public static final String ESCIDOC_CORE_FILTER_DEFAULT_MAXIMUM_RECORDS =
         "escidoc-core.filter.default.maximumRecords";
 
     public static final String ESCIDOC_CORE_AA_OPENID_PROVIDER_REGEX = "escidoc-core.aa.openid.provider.regex";
 
     public static final String CONTENT_RELATIONS_URL = "escidoc-core.ontology.url";
 
     public static final String SRW_URL = "srw.url";
 
     public static final String ADMIN_TOOL_URL = "admin-tool.url";
 
     public static final String ESCIDOC_BROWSER_URL = "escidoc-browser.url";
 
     public static final String HTTP_CONNECTION_TIMEOUT = "http.connection.timeout";
 
     public static final String HTTP_SOCKET_TIMEOUT = "http.socket.timeout";
 
     private static final String TRUE = "true";
 
     private static final String ONE = "1";
 
     /**
      * This property should be set to the name of the user-attribute that defines the organizational unit the user
      * belongs to .
      */
     public static final String ESCIDOC_CORE_AA_OU_ATTRIBUTE_NAME = "escidoc-core.aa.attribute-name.ou";
 
     /**
      * This property should be set to the name of the user-attribute that defines the common name of the user.
      */
     public static final String ESCIDOC_CORE_AA_COMMON_NAME_ATTRIBUTE_NAME =
         "escidoc-core.aa.attribute-name.common-name";
 
     /**
      * This property should be set to the name of the user-attribute that defines the unique loginname of the user.
      */
     public static final String ESCIDOC_CORE_AA_PERSISTENT_ID_ATTRIBUTE_NAME =
         "escidoc-core.aa.attribute-name.persistent-id";
 
     /**
      * Digilib Server (URL).
      */
     public static final String DIGILIB_SCALER = "digilib.scaler";
 
     /**
      * Digilib Client (URL).
      */
     public static final String DIGILIB_CLIENT = "digilib.digimage";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(EscidocConfiguration.class);
 
     private static EscidocConfiguration instance = new EscidocConfiguration();
 
     private Properties properties = new Properties();
 
     private static final String PROPERTIES_FILENAME = "escidoc-core.custom.properties";
 
     private static final String PROPERTIES_DEFAULT_FILENAME = "escidoc-core.properties";
 
     private static final String PROPERTIES_CONSTANT_FILENAME = "escidoc-core.constant.properties";
 
     /**
      * Private Constructor, in order to prevent instantiation of this utility class. read the Properties and fill it in
      * properties attribute.
      * 
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      */
     private EscidocConfiguration() {
         System.setProperty("java.awt.headless", "true");
         try {
             this.properties = loadProperties();
         } catch (final EscidocException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Problem while loading properties.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Problem while loading properties.", e);
             }
         }
     }
 
     /**
      * Returns and perhaps initializes Object.
      * 
      * @return EscidocConfiguration self
      * @throws IOException
      *             Thrown if properties loading fails.
      */
     public static EscidocConfiguration getInstance() {
         return instance;
     }
 
     /**
      * Returns the property with the given name or null if property was not found.
      * 
      * @param name
      *            The name of the Property.
      * @return Value of the given Property as String.
      */
     public String get(final String name) {
         return (String) properties.get(name);
     }
 
     /**
      * Returns the property with the given name or the second parameter as default value if property was not found.
      * 
      * @param name
      *            The name of the Property.
      * @param defaultValue
      *            The default value if property isn't given.
      * @return Value of the given Property as String.
      */
     public String get(final String name, final String defaultValue) {
         String prop = (String) properties.get(name);
 
         if (prop == null) {
             prop = defaultValue;
         }
         return prop;
     }
 
     /**
      * Returns the property with the given name as a boolean value. The result is set to true if the property value as
      * String has the value "true" or "1".
      * 
      * @param name
      *            The name of the Property.
      * @return Value of the given Property as boolean.
      */
     public boolean getAsBoolean(final String name) {
         Boolean result = false;
         String prop = (String) this.properties.get(name);
         if (prop != null) {
             prop = prop.toLowerCase(Locale.ENGLISH);
             if (prop != null && (TRUE.equals(prop) || ONE.equals(prop))) {
                 result = true;
             }
         }
         return result;
     }
 
     /**
      * Returns the property with the given name as a long value.
      * 
      * @param name
      *            The name of the Property.
      * @return Value of the given Property as long value.
      */
     public Long getAsLong(final String name) {
         Long returnValue = null;
         try {
             returnValue = Long.parseLong(getProperty(name));
         }
         catch (final NumberFormatException e) {
             LOGGER.error("Error on parsing configuration property '" + name + "'. Property must be a long!.");
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on parsing configuration property '" + name + "'. Property must be a long!", e);
             }
         }
         return returnValue;
     }
 
     /**
      * Returns the property with the given name as a long value.
      * 
      * @param name
      *            The name of the Property.
      * @return Value of the given Property as long value.
      */
     public Integer getAsInt(final String name) {
         Integer returnValue = null;
         try {
             returnValue = Integer.parseInt(getProperty(name));
         }
         catch (final NumberFormatException e) {
             LOGGER.error("Error on parsing configuration property '" + name + "'. Property must be a integer!.");
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on parsing configuration property '" + name + "'. Property must be a integer!", e);
             }
         }
         return returnValue;
     }
 
     private String getProperty(final String name) {
         final String property = properties.getProperty(name);
         if (property == null) {
             LOGGER.error("Missing property '" + name + "'!");
             throw new IllegalStateException("Missing property '" + name + "'!");
         }
         return property;
     }
 
     /**
      * Loads the Properties from the possible files. First loads properties from the file escidoc-core.properties.
      * Afterwards tries to load specific properties from the file escidoc-core.custom.properties and merges them with
      * the default properties. If any key is included in default and specific properties, the value of the specific
      * property will overwrite the default property.
      * 
      * @return The properties
      * @throws SystemException
      *             If the loading of the default properties (file escidoc-core.properties) fails.
      */
     private static Properties loadProperties() throws SystemException {
         final Properties result;
         try {
             result = getProperties(PROPERTIES_DEFAULT_FILENAME);
         }
         catch (final IOException e) {
             throw new SystemException("properties not found.", e);
         }
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Default properties: " + result);
         }
         Properties specific;
         try {
             specific = getProperties(PROPERTIES_FILENAME);
         }
         catch (final IOException e) {
             specific = new Properties();
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on loading specific properties.", e);
             }
         }
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Specific properties: " + specific);
         }
         result.putAll(specific);
 
         // Load constant properties
         Properties constant = new Properties();
         try {
             constant = getInternProperties(PROPERTIES_CONSTANT_FILENAME);
         }
         catch (final IOException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on loading contant properties.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on loading contant properties.", e);
             }
         }
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Constant properties: " + constant);
         }
         result.putAll(constant);
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("Merged properties: " + result);
         }
         // set Properties as System-Variables
         for (final Object o : result.keySet()) {
             final String key = (String) o;
             String value = result.getProperty(key);
             value = replaceEnvVariables(value);
             System.setProperty(key, value);
         }
         return result;
     }
 
     /**
      * Get the properties from a file.
      * 
      * @param filename
      *            The name of the properties file.
      * @return The properties.
      * @throws IOException
      *             If access to the specified file fails.
      */
     private static Properties getProperties(final String filename) throws IOException {
 
         final Properties result = new Properties();
         final InputStream propertiesStream = getInputStream(filename);
         result.load(propertiesStream);
         return result;
     }
 
     /**
      * Get the properties from a classpath resource.
      * 
      * @param filename
      *            The name of the properties file.
      * @return The properties.
      * @throws IOException
      *             If access to the specified file fails.
      */
     private static Properties getInternProperties(final String filename) throws IOException {
 
        final Properties result = new Properties(); 
         
         final ResourcePatternResolver applicationContext = new ClassPathXmlApplicationContext(new String[] {});
        final Resource[] resource = applicationContext.getResources("classpath*:" + filename);
         if (resource.length == 0) {
             throw new FileNotFoundException("Unable to find file '" + filename + "' in classpath.");
         }
 
         result.load(resource[0].getInputStream());
         return result;
     }
 
     
     /**
      * Get an InputStream for the given file.
      * 
      * @param filename
      *            The name of the file.
      * @return The InputStream or null if the file could not be located.
      * @throws FileNotFoundException
      *             If access to the specified file fails.
      */
     private static InputStream getInputStream(final String filename) throws IOException {
         final ResourcePatternResolver applicationContext = new ClassPathXmlApplicationContext(new String[] {});
         String escidocHome = System.getenv("ESCIDOC_HOME");
         if(escidocHome == null) {
             escidocHome = System.getProperty("ESCIDOC_HOME");
         }
         final Resource[] resource = applicationContext.getResources("file:///" + escidocHome + "/conf/" + filename);
         if (resource.length == 0) {
             throw new FileNotFoundException("Unable to find file '" + filename + "' in classpath.");
         }
         return resource[0].getInputStream();
     }
 
     /**
      * Retrieves the Properties from File.
      * 
      * @param property
      *            value of property with env-variable-syntax (e.g. ${java.home})
      * @return String replaced env-variables
      */
     private static String replaceEnvVariables(final String property) {
         String replacedProperty = property;
         if (property.startsWith("${")) {
             final String[] envVariables = PATTERN_PROPERTY_SPLIT.split(property);
             if (envVariables != null) {
                 for (int i = 0; i < envVariables.length; i++) {
                     final Matcher m = PATTERN_PROPERTY_FIND.matcher(envVariables[i]);
                     if (m.find()) {
                         envVariables[i] = m.group(1);
                     }
                     if (System.getProperty(envVariables[i]) != null
                         && System.getProperty(envVariables[i]).length() != 0) {
                         String envVariable = System.getProperty(envVariables[i]);
                         envVariable = envVariable.replaceAll("\\\\", "/");
                         replacedProperty = replacedProperty.replaceAll("\\$\\{" + envVariables[i] + '}', envVariable);
                     }
                 }
             }
         }
         return replacedProperty;
     }
 
     /**
      * Get the full URL to the eSciDoc Infrastructure itself extend with the provided path. E.g. path =
      * "/xsd/schema1.xsd" leads to http://localhost:8080/xsd/schema1.xsd.
      * 
      * @param path
      *            path which is to append on the eSciDoc selfUrl.
      * @return baseUrl with appended path
      */
     public String appendToSelfURL(final String path) {
 
         String selfUrl = get(ESCIDOC_CORE_SELFURL);
 
         if (selfUrl != null) {
             if (selfUrl.endsWith("/")) {
                 selfUrl = selfUrl.substring(0, selfUrl.length() - 1);
             }
             selfUrl += path;
         }
         return selfUrl;
     }
 }
