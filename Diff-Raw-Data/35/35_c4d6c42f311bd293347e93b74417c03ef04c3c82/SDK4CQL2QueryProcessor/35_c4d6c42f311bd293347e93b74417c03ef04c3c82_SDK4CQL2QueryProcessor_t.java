 package gov.nih.nci.cagrid.sdkquery4.processor2;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.data.QueryProcessingException;
 import gov.nih.nci.cagrid.data.cql2.CQL2QueryProcessor;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 import gov.nih.nci.system.client.ApplicationServiceProvider;
 import gov.nih.nci.system.query.hibernate.HQLCriteria;
 
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.cacore.sdk4x.cql2.processor.CQL2ToParameterizedHQL;
 import org.cagrid.cacore.sdk4x.cql2.processor.HibernateConfigTypesInformationResolver;
 import org.cagrid.cacore.sdk4x.cql2.processor.ParameterizedHqlQuery;
 import org.cagrid.cacore.sdk4x.cql2.processor.TypesInformationResolver;
 import org.cagrid.cql.utilities.AnyNodeHelper;
 import org.cagrid.cql.utilities.CQL2SerializationUtil;
 import org.cagrid.cql2.Aggregation;
 import org.cagrid.cql2.CQLQuery;
 import org.cagrid.cql2.CQLQueryModifier;
 import org.cagrid.cql2.NamedAttribute;
 import org.cagrid.cql2.results.CQLAggregateResult;
 import org.cagrid.cql2.results.CQLAttributeResult;
 import org.cagrid.cql2.results.CQLObjectResult;
 import org.cagrid.cql2.results.CQLQueryResults;
 import org.cagrid.cql2.results.CQLResult;
 import org.cagrid.cql2.results.TargetAttribute;
 import org.exolab.castor.types.AnyNode;
 import org.globus.wsrf.security.SecurityManager;
 import org.hibernate.cfg.Configuration;
 
 
 public class SDK4CQL2QueryProcessor extends CQL2QueryProcessor {
     // configuration property keys
     public static final String PROPERTY_APPLICATION_NAME = "applicationName";
     public static final String PROPERTY_USE_LOCAL_API = "useLocalApiFlag";
     public static final String PROPERTY_ORM_JAR_NAME = "ormJarName"; // only for local
     public static final String PROPERTY_HOST_NAME = "applicationHostName"; // only for remote
     public static final String PROPERTY_HOST_PORT = "applicationHostPort"; // only for remote
     public static final String PROPERTY_CASE_INSENSITIVE_QUERYING = "queryCaseInsensitive";
     public static final String PROPERTY_USE_LOGIN = "useServiceLogin";
     public static final String PROPERTY_USE_GRID_IDENTITY_LOGIN = "useGridIdentityLogin";
     public static final String PROPERTY_STATIC_LOGIN_USERNAME = "staticLoginUsername";
     public static final String PROPERTY_STATIC_LOGIN_PASSWORD = "staticLoginPassword";
 
     // default values for properties
     public static final String DEFAULT_USE_LOCAL_API = String.valueOf(false);
     public static final String DEFAULT_CASE_INSENSITIVE_QUERYING = String.valueOf(false);
     public static final String DEFAULT_USE_LOGIN = String.valueOf(false);
     public static final String DEFAULT_USE_GRID_IDENTITY_LOGIN = String.valueOf(false);
 
     public static final String EMPTY_PASSWORD = "EMPTYPASSWORD";
 
     private static Log LOG = LogFactory.getLog(SDK4CQL2QueryProcessor.class);
 
     private QNameResolver qnameResolver = null;
     private CQL2ToParameterizedHQL cqlTranslator = null;
 
 
     public SDK4CQL2QueryProcessor() {
         super();
     }
 
 
     public Properties getRequiredParameters() {
         Properties props = new Properties();
         props.setProperty(PROPERTY_APPLICATION_NAME, "");
         props.setProperty(PROPERTY_HOST_NAME, "");
         props.setProperty(PROPERTY_HOST_PORT, "");
         props.setProperty(PROPERTY_ORM_JAR_NAME, "");
         props.setProperty(PROPERTY_USE_LOCAL_API, DEFAULT_USE_LOCAL_API);
         props.setProperty(PROPERTY_CASE_INSENSITIVE_QUERYING, DEFAULT_CASE_INSENSITIVE_QUERYING);
         props.setProperty(PROPERTY_USE_LOGIN, DEFAULT_USE_LOGIN);
         props.setProperty(PROPERTY_USE_GRID_IDENTITY_LOGIN, DEFAULT_USE_GRID_IDENTITY_LOGIN);
         props.setProperty(PROPERTY_STATIC_LOGIN_USERNAME, "");
         props.setProperty(PROPERTY_STATIC_LOGIN_PASSWORD, "");
         return props;
     }
     
     
     public CQLQueryResults processQuery(CQLQuery query) throws QueryProcessingException {
         Iterator<CQLResult> resultIter = processQueryAndIterate(query);
         CQLQueryResults results = new CQLQueryResults();
         results.setTargetClassname(query.getCQLTargetObject().getClassName());
         List<CQLObjectResult> objectResults = new LinkedList<CQLObjectResult>();
         List<CQLAttributeResult> attributeResults = new LinkedList<CQLAttributeResult>();
         CQLAggregateResult aggregateResult = null;
         while (resultIter.hasNext()) {
             CQLResult result = resultIter.next();
             if (result instanceof CQLObjectResult) {
                 objectResults.add((CQLObjectResult) result);
             } else if (result instanceof CQLAttributeResult) {
                 attributeResults.add((CQLAttributeResult) result);
             } else if (result instanceof CQLAggregateResult) {
                 aggregateResult = (CQLAggregateResult) result;
             }
         }
         if (objectResults.size() != 0) {
             results.setObjectResult(objectResults.toArray(new CQLObjectResult[0]));
         } else if (attributeResults.size() != 0) {
             results.setAttributeResult(attributeResults.toArray(new CQLAttributeResult[0]));
         } else {
             results.setAggregationResult(aggregateResult);
         }
         return results;
     }
 
 
     public Iterator<CQLResult> processQueryAndIterate(CQLQuery query) throws QueryProcessingException {
         CQLQuery runQuery = query;
         if (runQuery.getCQLQueryModifier() != null && runQuery.getCQLQueryModifier().getNamedAttribute() != null) {
             // HQL will return distinct tuples of attribute names, so we need to include
             // the id attribute in those tuples to get a 1:1 correspondence with
             // actual data instances in the database
             try {
                 runQuery = CQL2SerializationUtil.cloneQueryBean(query);
                 NamedAttribute[] namedAttributes = runQuery.getCQLQueryModifier().getNamedAttribute();
                 NamedAttribute idAttribute = new NamedAttribute("id");
                 namedAttributes = (NamedAttribute[]) Utils.appendToArray(namedAttributes, idAttribute);
                 runQuery.getCQLQueryModifier().setNamedAttribute(namedAttributes);
             } catch (Exception ex) {
                 String message = "Error pre-processing query modifier attribute names: " + ex.getMessage();
                 LOG.error(message, ex);
                 throw new QueryProcessingException(message, ex);
             }
         }
         // get an instance of the caCORE SDK ApplicationService
         ApplicationService sdkService = getApplicationService();
 
         // empty results object
         CQLQueryResults queryResults = new CQLQueryResults();
         queryResults.setTargetClassname(query.getCQLTargetObject().getClassName());
 
         // convert the CQL to HQL
         LOG.debug("Converting CQL query to HQL");
         ParameterizedHqlQuery hql = null;
         try {
             hql = getCqlTranslator().convertToHql(runQuery);
             LOG.debug("Created HQL: " + hql.toString());
         } catch (QueryProcessingException ex) {
             throw ex;
         } catch (Exception ex) {
             throw new QueryProcessingException("Error processing query: " + ex.getMessage(), ex);
         }
 
         HQLCriteria criteria = new HQLCriteria(hql.getHql(), hql.getParameters());
 
         // query the SDK
         LOG.debug("Querying application service");
         List<Object> rawResults = null;
         try {
             rawResults = sdkService.query(criteria);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error querying caCORE service: " + ex.getMessage(), ex);
         }
         LOG.debug("Results obtained from application service");
         
         Iterator<CQLResult> cqlResultsIter = null;
 
         // see if there is further processing to be done
         if (runQuery.getCQLQueryModifier() != null) {
             LOG.debug("Post-processing query modifiers");
             CQLQueryModifier mods = runQuery.getCQLQueryModifier();
             if (mods.getNamedAttribute() != null) {
                 LOG.debug("Detected named attribute results");
                 // trim off the extra id attribute we added earlier
                String[] attributeNames = new String[mods.getNamedAttribute().length - 1];
                for (int i = 0; i < mods.getNamedAttribute().length - 1; i++) {
                    attributeNames[i] = mods.getNamedAttribute(i).getAttributeName();
                }
                 // this will happily ignore the last value which is the extra ID attribute
                cqlResultsIter = wrapAttributeResult(attributeNames, rawResults);
             } else if (mods.getCountOnly() != null && mods.getCountOnly().booleanValue()) {
                 LOG.debug("Detected count only aggregate results");
                 Object resultValue = rawResults.size() != 0 ? rawResults.get(0) : null;
                 String valueAsString = attributeValueAsString(resultValue);
                 cqlResultsIter = wrapAggregateResult(Aggregation.COUNT, "id", valueAsString);
             } else if (mods.getDistinctAttribute() != null) {
                 LOG.debug("Detected discinct attribute results");
                 if (mods.getDistinctAttribute().getAggregation() != null) {
                     LOG.debug("Detected aggregate results");
                     Aggregation agg = mods.getDistinctAttribute().getAggregation();
                     Object resultValue = rawResults.size() != 0 ? rawResults.get(0) : null;
                     String valueAsString = attributeValueAsString(resultValue);
                     cqlResultsIter = wrapAggregateResult(agg, mods.getDistinctAttribute().getAttributeName(), valueAsString);
                 } else {
                     // standard attribute name / value pairs
                     cqlResultsIter = wrapAttributeResult(
                         new String[]{mods.getDistinctAttribute().getAttributeName()},
                        rawResults);
                 }
             }
         } else {
             LOG.debug("Detected object results");
             QName targetQName = null;
             try {
                 targetQName = getQNameResolver().getQName(query.getCQLTargetObject().getClassName());
             } catch (Exception ex) {
                 throw new QueryProcessingException("Error obtaining QName for target data type: " + ex.getMessage(), ex);
             }
             cqlResultsIter = wrapObjectResults(rawResults, targetQName);
         }
 
         return cqlResultsIter;
     }
 
 
     private CQL2ToParameterizedHQL getCqlTranslator() throws Exception {
         if (cqlTranslator == null) {
             InputStream configStream = getClass().getResourceAsStream("/hibernate.cfg.xml");
             Configuration config = new Configuration();
             config.addInputStream(configStream);
             config.configure();
             configStream.close();
             TypesInformationResolver resolver = new HibernateConfigTypesInformationResolver(config);
             cqlTranslator = new CQL2ToParameterizedHQL(resolver, useCaseInsensitiveQueries());
         }
         return cqlTranslator;
     }
 
 
     private Iterator<CQLResult> wrapObjectResults(List<Object> rawObjects, final QName targetQName) {
         final Iterator<Object> rawObjectIter = rawObjects.iterator();
         Iterator<CQLResult> objectIter = new Iterator<CQLResult>() {
             public boolean hasNext() {
                 return rawObjectIter.hasNext();
             }
 
 
             public CQLResult next() {
                 CQLObjectResult obj = new CQLObjectResult();
                 Object rawObject = rawObjectIter.next();
                 InputStream wsdd = getConfiguredWsddStream();
                 StringWriter writer = new StringWriter();
                 AnyNode node = null;
                 try {
                     wsdd.mark(Integer.MAX_VALUE);
                     Utils.serializeObject(rawObject, targetQName, writer, wsdd);
                     wsdd.reset();
                     node = AnyNodeHelper.convertStringToAnyNode(
                         writer.getBuffer().toString());
                 } catch (Exception ex) {
                     String message = "Error creating AnyNode for object results: " + ex.getMessage();
                     LOG.error(message, ex);
                     NoSuchElementException nse = new NoSuchElementException(message);
                     nse.initCause(ex);
                     throw nse;
                 }
                 obj.set_any(node);
                 return obj;
             }
 
 
             public void remove() {
                 throw new UnsupportedOperationException("remove() is not supported");
             }
         };
         return objectIter;
     }
 
 
     private Iterator<CQLResult> wrapAggregateResult(Aggregation agg, String attributeName, String value) {
         final CQLAggregateResult result = new CQLAggregateResult();
         result.setAggregation(agg);
         result.setAttributeName(attributeName);
         result.setValue(value);
         
         Iterator<CQLResult> iter = new Iterator<CQLResult>() {
             boolean hasBeenReturned = false;
             
             public boolean hasNext() {
                 return !hasBeenReturned;
             }
 
             
             public synchronized CQLResult next() {
                 if (!hasNext()) {
                     throw new NoSuchElementException();
                 }
                 hasBeenReturned = true;
                 return result;
             }
 
             
             public void remove() {
                 throw new UnsupportedOperationException("remove() is not supported");
             }
         };
         
         return iter;
     }
 
 
    private Iterator<CQLResult> wrapAttributeResult(final String[] attributeNames, List<Object> attributeValues) {
        final Iterator<Object> rawValueIter = attributeValues.iterator();
         Iterator<CQLResult> iter = new Iterator<CQLResult>() {
             public boolean hasNext() {
                 return rawValueIter.hasNext();
             }
 
             
             public synchronized CQLResult next() {
                Object tuple = rawValueIter.next();
                Object[] values = null;
                if (tuple != null && tuple.getClass().isArray()) {
                    values = (Object[]) tuple;
                } else {
                    values = new Object[] {tuple};
                }
                 CQLAttributeResult result = new CQLAttributeResult();
                 TargetAttribute[] ta = new TargetAttribute[attributeNames.length];
                 for (int i = 0; i < attributeNames.length; i++) {
                     Object val = values[i];
                     ta[i] = new TargetAttribute(attributeNames[i], attributeValueAsString(val));
                 }
                 result.setAttribute(ta);
                 return result;
             }
             
 
             public void remove() {
                 throw new UnsupportedOperationException("remove() is not supported");
             }
         };
         return iter;
     }
 
 
     private String attributeValueAsString(Object val) {
         String valAsString = null;
         if (val != null) {
             if (val instanceof Date) {
                 valAsString = DateFormat.getDateTimeInstance().format((Date) val);
             } else {
                 valAsString = String.valueOf(val);
             }
         }
         return valAsString;
     }
 
 
     private ApplicationService getApplicationService() throws QueryProcessingException {
         LOG.debug("Obtaining application service instance");
         ApplicationService service = null;
 
         boolean useLocal = useLocalApplicationService();
         boolean useLogin = useServiceLogin();
         boolean useStaticLogin = useStaticLogin();
         try {
             String username = null;
             String passwd = null;
             if (useLogin) {
                 if (useStaticLogin) {
                     LOG.trace("Application service using static login");
                     username = getConfiguredParameters().getProperty(PROPERTY_STATIC_LOGIN_USERNAME);
                     passwd = username = getConfiguredParameters().getProperty(PROPERTY_STATIC_LOGIN_PASSWORD);
                 } else {
                     LOG.trace("Application service using caller identity login");
                     SecurityManager securityManager = SecurityManager.getManager();
                     username = securityManager.getCaller();
                     passwd = EMPTY_PASSWORD;
                 }
             }
 
             if (useLocal) {
                 LOG.trace("Application service using local API");
                 if (useLogin) {
                     service = ApplicationServiceProvider.getApplicationService(username, passwd);
                 } else {
                     service = ApplicationServiceProvider.getApplicationService();
                 }
             } else {
                 String url = getRemoteApplicationUrl();
                 LOG.trace("Application service using remote API at " + url);
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
 
 
     private QNameResolver getQNameResolver() throws Exception {
         if (qnameResolver == null) {
             qnameResolver = new MappingFileQNameResolver(getClassToQnameMappings());
         }
         return qnameResolver;
     }
 
 
     private String getRemoteApplicationUrl() {
         String hostname = getConfiguredParameters().getProperty(PROPERTY_HOST_NAME);
         String port = getConfiguredParameters().getProperty(PROPERTY_HOST_PORT);
         while (hostname.endsWith("/")) {
             hostname = hostname.substring(0, hostname.length() - 1);
         }
         String urlPart = hostname + ":" + port;
         urlPart += "/";
         urlPart += getConfiguredParameters().getProperty(PROPERTY_APPLICATION_NAME);
         return urlPart;
     }
 
 
     private boolean useCaseInsensitiveQueries() throws QueryProcessingException {
         String caseInsensitiveValue = getConfiguredParameters().getProperty(PROPERTY_CASE_INSENSITIVE_QUERYING);
         try {
             return Boolean.parseBoolean(caseInsensitiveValue);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error determining case insensitivity: " + ex.getMessage(), ex);
         }
     }
 
 
     private boolean useLocalApplicationService() throws QueryProcessingException {
         String useLocalValue = getConfiguredParameters().getProperty(PROPERTY_USE_LOCAL_API);
         try {
             return Boolean.parseBoolean(useLocalValue);
         } catch (Exception ex) {
             throw new QueryProcessingException("Error determining local application service use: " + ex.getMessage(),
                 ex);
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
