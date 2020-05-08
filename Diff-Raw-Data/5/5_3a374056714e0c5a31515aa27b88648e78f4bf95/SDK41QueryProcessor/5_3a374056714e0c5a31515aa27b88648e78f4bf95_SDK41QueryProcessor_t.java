 package org.cagrid.data.sdkquery41.processor;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import gov.nih.nci.cagrid.cqlquery.QueryModifier;
 import gov.nih.nci.cagrid.cqlresultset.CQLQueryResults;
 import gov.nih.nci.cagrid.data.MalformedQueryException;
 import gov.nih.nci.cagrid.data.QueryProcessingException;
 import gov.nih.nci.cagrid.data.cql.CQLQueryProcessor;
 import gov.nih.nci.cagrid.data.mapping.Mappings;
 import gov.nih.nci.cagrid.data.service.ServiceConfigUtil;
 import gov.nih.nci.cagrid.data.utilities.CQLResultsCreationUtil;
 import gov.nih.nci.cagrid.data.utilities.ResultsCreationException;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.globus.wsrf.security.SecurityManager;
 
 /**
  * SDK41QueryProcessor
  * Leverages the caCORE SDK v4.1's native CQL processing functionality
  * 
  * @author David
  */
 public class SDK41QueryProcessor extends CQLQueryProcessor {
     
     public static final Log logger = LogFactory.getLog(SDK41QueryProcessor.class);
     
     // general configuration options
     public static final String PROPERTY_APPLICATION_NAME = "applicationName";
     public static final String PROPERTY_USE_LOCAL_API = "useLocalApiFlag";
     
     // remote service configuration properties
     public static final String PROPERTY_HOST_NAME = "applicationHostName";
     public static final String PROPERTY_HOST_PORT = "applicationHostPort";
     public static final String PROPERTY_HOST_HTTPS = "useHttpsUrl";
     
     // security configuration properties
     public static final String PROPERTY_USE_LOGIN = "useServiceLogin";
     public static final String PROPERTY_USE_GRID_IDENTITY_LOGIN = "useGridIdentityLogin";
     public static final String PROPERTY_STATIC_LOGIN_USERNAME = "staticLoginUsername";
     public static final String PROPERTY_STATIC_LOGIN_PASSWORD = "staticLoginPassword";
     
     // default values for properties
     public static final String DEFAULT_HOST_HTTPS = String.valueOf(false);
     public static final String DEFAULT_USE_LOCAL_API = String.valueOf(false);
     public static final String DEFAULT_USE_LOGIN = String.valueOf(false);
     public static final String DEFAULT_USE_GRID_IDENTITY_LOGIN = String.valueOf(false);
     
    public static final String EMPTY_PASSWORD = "EMPTYPASSWORD";
    
     public SDK41QueryProcessor() {
         super();
     }
 
 
     public CQLQueryResults processQuery(CQLQuery cqlQuery) throws MalformedQueryException, QueryProcessingException {
         try {
             cqlQuery = CQLAttributeDefaultPredicateUtil.checkDefaultPredicates(cqlQuery);
         } catch (Exception ex) {
             throw new QueryProcessingException(
                 "Error checking query for default Attribute predicate values: " + ex.getMessage(), ex);
         }
         ApplicationService applicationService = getApplicationService();
         List rawResults = null;
         try {
             rawResults = applicationService.query(cqlQuery);
         } catch (ApplicationException ex) {
             String message = "Error processing CQL query in the caCORE ApplicationService: " + ex.getMessage();
             logger.error(message, ex);
             throw new QueryProcessingException(message, ex);
         }
         
         CQLQueryResults cqlResults = null;
         // determine which type of results to package up
         if (cqlQuery.getQueryModifier() != null) {
             QueryModifier mods = cqlQuery.getQueryModifier();
             if (mods.isCountOnly()) {
                 long count = Long.parseLong(rawResults.get(0).toString());
                 cqlResults = CQLResultsCreationUtil.createCountResults(count, cqlQuery.getTarget().getName());
             } else { // attributes
                 String[] attributeNames = null;
                 List<Object[]> resultsAsArrays = null;
                 if (mods.getDistinctAttribute() != null) {
                     attributeNames = new String[] {mods.getDistinctAttribute()};
                     resultsAsArrays = new LinkedList<Object[]>();
                     for (Object o : rawResults) {
                         resultsAsArrays.add(new Object[] {o});
                     }
                 } else { // multiple attributes
                     attributeNames = mods.getAttributeNames();
                     resultsAsArrays = new LinkedList<Object[]>();
                     for (Object o : rawResults) {
                         Object[] array = null;
                         if (o.getClass().isArray()) {
                             array = (Object[]) o;
                         } else {
                             array = new Object[] {o};
                         }
                         resultsAsArrays.add(array);
                     }
                 }
                 cqlResults = CQLResultsCreationUtil.createAttributeResults(
                     resultsAsArrays, cqlQuery.getTarget().getName(), attributeNames);
             }
         } else {
             Mappings classToQname = null;
             try {
                 classToQname = getClassToQnameMappings();
             } catch (Exception ex) {
                 throw new QueryProcessingException("Error loading class to QName mappings: " + ex.getMessage(), ex);
             }
             try {
                 cqlResults = CQLResultsCreationUtil.createObjectResults(
                     rawResults, cqlQuery.getTarget().getName(), classToQname);
             } catch (ResultsCreationException ex) {
                 throw new QueryProcessingException("Error packaging query results: " + ex.getMessage(), ex);
             }
         }
         return cqlResults;
     }
     
     
     public Properties getRequiredParameters() {
         Properties props = new Properties();
         props.setProperty(PROPERTY_APPLICATION_NAME, "");
         props.setProperty(PROPERTY_HOST_NAME, "");
         props.setProperty(PROPERTY_HOST_PORT, "");
         props.setProperty(PROPERTY_HOST_HTTPS, DEFAULT_HOST_HTTPS);
         props.setProperty(PROPERTY_USE_LOCAL_API, DEFAULT_USE_LOCAL_API);
         props.setProperty(PROPERTY_USE_LOGIN, DEFAULT_USE_LOGIN);
         props.setProperty(PROPERTY_USE_GRID_IDENTITY_LOGIN, DEFAULT_USE_GRID_IDENTITY_LOGIN);
         props.setProperty(PROPERTY_STATIC_LOGIN_USERNAME, "");
         props.setProperty(PROPERTY_STATIC_LOGIN_PASSWORD, "");
         return props;
     }
     
     
     public String getConfigurationUiClassname() {
         // TODO: return the UI classname once the class is written
         return null;
     }
     
     
     private ApplicationService getApplicationService() throws QueryProcessingException {
         ApplicationService service = null;
         
         boolean useLocal = useLocalApplicationService();
         boolean useLogin = useServiceLogin();
         boolean useStaticLogin = useStaticLogin();
         try {
             String username = null;
             String passwd = null;
             if (useLogin) {
                 if (useStaticLogin) {
                     username = getConfiguredParameters().getProperty(PROPERTY_STATIC_LOGIN_USERNAME);
                     passwd = username = getConfiguredParameters().getProperty(PROPERTY_STATIC_LOGIN_PASSWORD);
                 } else {
                     SecurityManager securityManager = SecurityManager.getManager();
                     username = securityManager.getCaller();
                    passwd = EMPTY_PASSWORD;
                 }
             }
             
             if (useLocal) {
                 if (useLogin) {
                     service = ApplicationServiceProvider.getApplicationService(username, passwd);
                 } else {
                     service = ApplicationServiceProvider.getApplicationService();   
                 }
             } else {
                 String url = getRemoteApplicationUrl();
                 if (useLogin) {
                     service = ApplicationServiceProvider.getApplicationServiceFromUrl(url, username, passwd);
                 } else {
                     service = ApplicationServiceProvider.getApplicationServiceFromUrl(url);   
                 }
             }
         } catch (Exception ex) {
             throw new QueryProcessingException("Error obtaining application service instance: " + ex.getMessage(), ex);
         }
         
         return service;
     }
     
 
     
     
     private Mappings getClassToQnameMappings() throws Exception {
         // get the mapping file name
         String filename = ServiceConfigUtil.getClassToQnameMappingsFile();
         // String filename = "mapping.xml";
         Mappings mappings = (Mappings) Utils.deserializeDocument(filename, Mappings.class);
         return mappings;
     }
     
     
     private String getRemoteApplicationUrl() throws QueryProcessingException {
         StringBuffer url = new StringBuffer();
         if (useHttpsUrl()) {
             url.append("https://");
         } else {
             url.append("http://");
         }
         url.append(getConfiguredParameters().getProperty(PROPERTY_HOST_NAME));
         url.append(":");
         url.append(getConfiguredParameters().getProperty(PROPERTY_HOST_PORT));
         url.append("/");
         url.append(getConfiguredParameters().getProperty(PROPERTY_APPLICATION_NAME));
         String completedUrl = url.toString();
         logger.debug("Application Service remote URL determined to be: " + completedUrl);
         return completedUrl;
     }
     
     
     private boolean useHttpsUrl() throws QueryProcessingException {
         String useHttpsValue = getConfiguredParameters().getProperty(PROPERTY_HOST_HTTPS);
         try {
             return Boolean.parseBoolean(useHttpsValue);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error determining HTTPS use: " + ex.getMessage(), ex);
         }
     }
     
     
     private boolean useLocalApplicationService() throws QueryProcessingException {
         String useLocalValue = getConfiguredParameters().getProperty(PROPERTY_USE_LOCAL_API);
         try {
             return Boolean.parseBoolean(useLocalValue);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error determining local application service use: " + ex.getMessage(), ex);
         }
     }
     
     
     private boolean useServiceLogin() throws QueryProcessingException {
         String useLoginValue = getConfiguredParameters().getProperty(PROPERTY_USE_LOGIN);
         try {
             return Boolean.parseBoolean(useLoginValue);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error determining login use flag: " + ex.getMessage(), ex);
         }
     }
     
     
     private boolean useStaticLogin() throws QueryProcessingException {
         String useGridIdentLogin = getConfiguredParameters().getProperty(PROPERTY_USE_GRID_IDENTITY_LOGIN);
         try {
             return !Boolean.parseBoolean(useGridIdentLogin);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error determining use of static login: " + ex.getMessage(), ex);
         }
     }
 }
