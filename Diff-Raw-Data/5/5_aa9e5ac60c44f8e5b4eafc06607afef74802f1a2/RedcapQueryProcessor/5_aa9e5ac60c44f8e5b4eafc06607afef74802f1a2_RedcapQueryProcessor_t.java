 package org.cagrid.redcap.processor;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.cqlquery.CQLQuery;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.redcap.Data;
 import org.cagrid.redcap.DataAuth;
 import org.cagrid.redcap.EventArms;
 import org.cagrid.redcap.Events;
 import org.cagrid.redcap.EventsCalendar;
 import org.cagrid.redcap.Forms;
 import org.cagrid.redcap.Projects;
 import org.cagrid.redcap.service.RedcapDataServiceConfiguration;
 import org.cagrid.redcap.util.PropertiesUtil;
 import org.cagrid.redcap.util.ProxyUtil;
 import org.cagrid.redcap.util.RedcapUtil;
 import org.cagrid.redcap.util.UserPrivilegesUtil;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import gov.nih.nci.cagrid.cqlquery.QueryModifier;
 import gov.nih.nci.cagrid.cqlresultset.CQLQueryResults;
 import gov.nih.nci.cagrid.data.InitializationException;
 import gov.nih.nci.cagrid.data.MalformedQueryException;
 import gov.nih.nci.cagrid.data.QueryProcessingException;
 import gov.nih.nci.cagrid.data.cql.CQLQueryProcessor;
 import gov.nih.nci.cagrid.data.cql.validation.DomainModelValidator;
 import gov.nih.nci.cagrid.data.cql.validation.ObjectWalkingCQLValidator;
 import gov.nih.nci.cagrid.data.mapping.Mappings;
 import gov.nih.nci.cagrid.data.service.ServiceConfigUtil;
 import gov.nih.nci.cagrid.data.utilities.CQLResultsCreationUtil;
 import gov.nih.nci.cagrid.data.utilities.ResultsCreationException;
 import gov.nih.nci.cagrid.metadata.MetadataUtils;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 
 /*
  * Query Processor to process queries against the
  * REDCap Database. Initializes the Database connection
  * with the configured parameters from the service.properties
  * Required service properties to connect to REDCap:
  * jdbcDriverName
  * jdbcConnectString
  * jdbcUserName
  * tableNamePrefix  
  * domainModelFileName(from etc)
  */
 public class RedcapQueryProcessor extends CQLQueryProcessor{
 	
 	private static final String JDBC_DRIVER_NAME = "jdbcDriverName";
 	private static final String JDBC_CONNECT_STRING = "jdbcConnectString";
 	private static final String JDBC_USERNAME = "jdbcUserName";
 	private static final String JDBC_PASSWORD = "jdbcPassword";
 		
 	private static final String HB_CONFIG_LOC = "hibernate.cfg.xml";
 	private static final String DOMAIN_MODEL_FILE_NAME = "domainModelFileName";
 	private static final String DEF_DOMAIN_MODEL = "RedcapDataServiceDomainModel.xml";
 	
 	private static final String HB_CONN_DRIVER = "hibernate.connection.driver_class";
 	private static final String HB_CONN_URL = "hibernate.connection.url";
 	private static final String HB_CONN_USERNAME = "hibernate.connection.username";
 	private static final String HB_CONN_PWD = "hibernate.connection.password";
 	private static final String HB_DIALECT = "org.hibernate.dialect.MySQLDialect";
 	
 	private static final String AUTHORIZATION = "authorization";
 	private static final String AUTHORIZATION_ON = "ON";
 	
 	private static final String RCGRID_USER_MAPPING_PROPS = "rcUserGridMapping.properties";
 	private static final String RCGRID_USER_MAPPING = "rcUserGridMapping";
 	
 	private static final String RC_USER_AUTH_QUERIES = "rcUserAuthQueries";
 	private static final String RC_USER_AUTH_QUERIES_PROPS = "rcUserAuthQueries.properties";
 	private SessionFactory sessionFactory;
 	private AnnotationConfiguration annotationConfig;
 	private DatabaseConnectionSource connectionSource;
 		
 	private String driverClassname;
 	private String connectString;
 	private String username; 
 	private String password;
 	private String authorization;
 	private String userMapping;
 	
 	private static final Log LOG = LogFactory.getLog(RedcapQueryProcessor.class);
 	
 	private QueryModifier queryModifier;
 	private List<String> distinctAttributeList;
 	private List<String> attributeList;
 	
 	public RedcapQueryProcessor() {
         super();
     }
 	
 	public void initialize(Properties parameters, InputStream wsdd) throws InitializationException {
        	super.initialize(parameters, wsdd);
        	initConnectionSource();
        	initConnection();
     }
     
 	/*
 	 * Validates the CQLQuery structure
 	 * and validates the CQL against the domain 
 	 * model to check if the cql is semantically 
 	 * correct.
 	 * @param CQLQuery: cql 1.0 query
 	 * @return MalformedQueryException - invalid query
 	 */
 	private void validateCqlStructure(CQLQuery cqlQuery) throws MalformedQueryException{
 		try{
 			LOG.debug("Validating CQLQuery..");
 			ObjectWalkingCQLValidator  validator = new ObjectWalkingCQLValidator();
 			validator.validateCqlStructure(cqlQuery);	
 			LOG.debug("Validating Domain Model..");
 			DomainModel model = getDomainModel();
 			DomainModelValidator  val = new DomainModelValidator();
 			val.validateDomainModel(cqlQuery, model);
 		}catch(MalformedQueryException exp){
 			LOG.error("Error validating.."+exp.getMessage(),exp);
 			throw new MalformedQueryException(exp);
 		}
 	}
 	
 	 public Set<String> getPropertiesFromEtc() {
         Set<String> fromEtc = new HashSet<String>();
         fromEtc.add(DOMAIN_MODEL_FILE_NAME);
         fromEtc.add(RCGRID_USER_MAPPING);
         fromEtc.add(RC_USER_AUTH_QUERIES);
         return fromEtc;
 	 }
 	 
 	 /*
 	  * Gets the configured domain model 
 	  */
 	 private DomainModel getDomainModel() {
         String domainModelFile = getConfiguredParameters().getProperty(DOMAIN_MODEL_FILE_NAME);
         LOG.debug("Domain model file name"+domainModelFile);
         DomainModel model = null;
         try {
             FileReader reader = new FileReader(domainModelFile);
             model = MetadataUtils.deserializeDomainModel(reader);
             reader.close();
         } catch (Exception ex) {
             String message = "Unable to deserialize specified domain model: " + ex.getMessage();
             LOG.error(message, ex);
         }
         return model;
 	}
 	
 	public Properties getRequiredParameters() {
         Properties props = new Properties();
         props.setProperty(JDBC_DRIVER_NAME, "");
         props.setProperty(JDBC_CONNECT_STRING, "");
         props.setProperty(JDBC_USERNAME, "");
         props.setProperty(JDBC_PASSWORD, "");
         props.setProperty(DOMAIN_MODEL_FILE_NAME,DEF_DOMAIN_MODEL);
         props.setProperty(AUTHORIZATION, AUTHORIZATION_ON);
         props.setProperty(RCGRID_USER_MAPPING,RCGRID_USER_MAPPING_PROPS);
         props.setProperty(RC_USER_AUTH_QUERIES, RC_USER_AUTH_QUERIES_PROPS);
         return props;
     }
     
     /*
      * Initializes the DB connection using the configured parameters.
      * jdbcDriverName, jdbcConnectString, jdbcUserName, jdbcPassword
      * have to be configured to connect to the DB.
      */
     private void initConnectionSource() throws InitializationException {
         
     	LOG.info("Initializing database conection..");
     	driverClassname = getConfiguredParameters().getProperty(JDBC_DRIVER_NAME);
         connectString = getConfiguredParameters().getProperty(JDBC_CONNECT_STRING);
         username = getConfiguredParameters().getProperty(JDBC_USERNAME);
         password = getConfiguredParameters().getProperty(JDBC_PASSWORD);
        try{
         authorization = RedcapDataServiceConfiguration.getConfiguration().getAuthorization();
        }catch(Exception e){
     	  e.printStackTrace();
        }
                
         StringBuffer logMessage = new StringBuffer();
         logMessage.append(JDBC_DRIVER_NAME).append("[").append(driverClassname).append("]");
         logMessage.append(JDBC_CONNECT_STRING ).append("[").append(connectString).append("]");
         logMessage.append(JDBC_USERNAME).append("[").append(username).append("]");
         logMessage.append(JDBC_DRIVER_NAME).append("[").append(password).append("]");
         LOG.info(logMessage);
         
         try {
             connectionSource = new PooledDatabaseConnectionSource(driverClassname, connectString, username, password);
           
         } catch (Exception ex) {
             String message = "Unable to initialize database connection source: " + ex.getMessage();
             LOG.error(message, ex);
             throw new InitializationException(message, ex);
         }
     }
     
     private void initConnection(){
 		try {
 		   	annotationConfig = new AnnotationConfiguration();
 			annotationConfig.addAnnotatedClass(Projects.class);
 			annotationConfig.addAnnotatedClass(EventArms.class);
 			annotationConfig.addAnnotatedClass(Events.class);
 			annotationConfig.addAnnotatedClass(EventsCalendar.class);
 			annotationConfig.addAnnotatedClass(Forms.class);
 			annotationConfig.addAnnotatedClass(Data.class);
 			LOG.debug("Attempting to get Hibernate Configuration file from ["+ HB_CONFIG_LOC +"]");
 			sessionFactory = getSessionFactory(annotationConfig);
 			//sessionFactory = annotationConfig.configure(getClass().getResource(HB_CONFIG_LOC)).buildSessionFactory();
 		} catch (Throwable ex) {
 			StringBuffer logMessage = new StringBuffer();
 	        logMessage.append(JDBC_DRIVER_NAME).append("[").append(driverClassname).append("]");
 	        logMessage.append(JDBC_CONNECT_STRING ).append("[").append(connectString).append("]");
 	        logMessage.append(JDBC_USERNAME).append("[").append(username).append("]");
 	        logMessage.append(JDBC_DRIVER_NAME).append("[").append(password).append("]");
 	        LOG.error("Failed to create session factory with service properties"+logMessage.toString()+ex.getMessage(),ex);
 			throw new ExceptionInInitializerError(ex);
 		}
 	}
     
     /*
      * Processes the CQLQuery by converting
      * CQL to ParameterizedHQL and querying against
      * the REDCap DB. 
      * @param CQLQuery: user request cql
      * @return CQLQueryResults: Object results to return top level objects
      */
 	public CQLQueryResults processQuery(CQLQuery cqlQuery) throws MalformedQueryException, QueryProcessingException {
 
 		LOG.info("Processing CQLQuery for target "+cqlQuery.getTarget().getName()+"with Authorization"+authorization);
 		queryModifier = null;
 		List<Object> postFilterResults = null;
 		RedcapUtil util = new RedcapUtil();
 		this.queryModifier = cqlQuery.getQueryModifier();
         
 		validateCqlStructure(cqlQuery);
 		
 		if(authorization!=null && authorization.equalsIgnoreCase(AUTHORIZATION_ON)){
 			QueryModifier newQueryModifier = preProcessQueryModifier(cqlQuery.getQueryModifier());
 	        cqlQuery.setQueryModifier(newQueryModifier);
         }
 		CQLQueryResults cqlQueryResults = null;
 		try{
 			Long startTime = System.currentTimeMillis();
 			List<Object> completeObjectsList = util.convert(cqlQuery, sessionFactory,annotationConfig, connectionSource);
 			
 			if(authorization!=null && authorization.equalsIgnoreCase(AUTHORIZATION_ON)){
 	        	cqlQuery.setQueryModifier(this.queryModifier);
 	            postFilterResults = postFilterResults(cqlQueryResults,cqlQuery,completeObjectsList);
 	            if(this.queryModifier!=null && this.queryModifier.isCountOnly()){
 	               	int count = postFilterResults.size();
 	            	postFilterResults = new ArrayList<Object>();
 	            	postFilterResults.add(count);
 	            }
 		        cqlQueryResults = getResults(cqlQuery, postFilterResults);
 			}else{
 				cqlQueryResults = getResults(cqlQuery, completeObjectsList);
 			}
 	        Long endTime = System.currentTimeMillis();
 	        LOG.debug("Total elapsed time RedcapQueryProcessor is :"+ (endTime-startTime));
 		}catch(SQLException sqlException){
 			LOG.error("Error processing the query"+sqlException.getMessage(),sqlException);
 			throw new QueryProcessingException(sqlException.getMessage());
 		}
 		return cqlQueryResults;
 	}
 	
 	/*
 	 * Wraps the results into CQLQueryResults
 	 * to be returned back to user.
 	 * CQLQueryResults can be iterated to get the
 	 * required object results.
 	 * @param CQLQuery
 	 * @param List<Object>: results to be wrapped to CQLQueryResults
 	 * @return CQLQueryResults
 	 */
 	private CQLQueryResults getResults(CQLQuery cqlQuery, List<Object> rawResults) throws MalformedQueryException, QueryProcessingException{
 		CQLQueryResults cqlResults = null;
         
         if (cqlQuery.getQueryModifier() != null) {
         	QueryModifier modifier = cqlQuery.getQueryModifier();
            	if(modifier.isCountOnly()){
                 long count = Long.parseLong(rawResults.get(0).toString());
                 cqlResults = CQLResultsCreationUtil.createCountResults(count, cqlQuery.getTarget().getName());
             }else{
             	String[] attributeNames = null;
                 List<Object[]> resultsAsArrays = null;
                 
                 if (modifier.getDistinctAttribute() != null) {
                     attributeNames = new String[] {modifier.getDistinctAttribute()};
                     resultsAsArrays = new LinkedList<Object[]>();
                     for(Object o : rawResults){
                         resultsAsArrays.add(new Object[] {o});
                     }
                 } else { // multiple attributes
                     attributeNames = modifier.getAttributeNames();
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
             	LOG.error("Error loading class to QName mappings:"+ex.getMessage(),ex);
                 throw new QueryProcessingException("Error loading class to QName mappings: " + ex.getMessage(), ex);
             }
             try {
             	List<Object> newRawList = new ArrayList<Object>();
             	ProxyUtil proxyUtil = new ProxyUtil();
             	for(int i=0;i<rawResults.size();i++){
             		Object o = rawResults.get(i);
             		newRawList.add(proxyUtil.getProxy(o));
             	}
             	rawResults = newRawList;
                 cqlResults = CQLResultsCreationUtil.createObjectResults(
                     rawResults, cqlQuery.getTarget().getName(), classToQname);
             } catch (ResultsCreationException ex) {
             	LOG.error("Error packaging query results: "+ex.getMessage(),ex);
                 throw new QueryProcessingException("Error packaging query results: " + ex.getMessage(), ex);
             }
         }
         return cqlResults;
 		
 	}
 	
 	private Mappings getClassToQnameMappings() throws Exception {
        String filename = ServiceConfigUtil.getClassToQnameMappingsFile();
        Mappings mappings = (Mappings) Utils.deserializeDocument(filename, Mappings.class);
        return mappings;
 	}
 	
 	public void finalize() throws Throwable {
         try {
             connectionSource.shutdown();
         } catch (Exception ex) {
             LOG.error("Error shutting down connection source: " + ex.getMessage(), ex);
         }
         super.finalize();
     }
 	
 	private SessionFactory getSessionFactory(AnnotationConfiguration configuration){
 		configuration.setProperty(HB_CONN_DRIVER, driverClassname);
 		configuration.setProperty(HB_CONN_URL, connectString);
 		configuration.setProperty(HB_CONN_USERNAME, username);
 		configuration.setProperty(HB_CONN_PWD, password);
 		configuration.setProperty(HB_DIALECT, "org.hibernate.dialect.MySQLDialect");
 		//configuration.setProperty("hibernate.show_sql","true");
 		
 		SessionFactory sessionFactory = configuration.buildSessionFactory();
 		return sessionFactory;
 	}
 	
 	 private List<Object> postFilterResults(CQLQueryResults cqlResults,CQLQuery cqlQuery,List<Object> completeObjectsList) throws QueryProcessingException {
 		LOG.debug("Post-filtering Redcap CQL results");
         String identity = getCallerId();
         ArrayList<Object> authorizedProjectsList = null;
         UserPrivilegesUtil util = new UserPrivilegesUtil();
         authorizedProjectsList = util.getAuthorizedProjects(identity,connectionSource,cqlQuery);
         
         List<Object> filteredList = new ArrayList<Object>();
         
         for(int i=0;i<completeObjectsList.size();i++){
         	if(completeObjectsList.get(i) instanceof org.cagrid.redcap.Projects){
         		Projects project = (org.cagrid.redcap.Projects)completeObjectsList.get(i);
         		boolean authorized = iterateUserCollectionProtocol(authorizedProjectsList, project.getProjectId());
         		if(authorized){
         			LOG.debug("User authorized to retrive Projects with project id"+project.getProjectId());
         			if(cqlQuery.getQueryModifier()!=null){
 	        			if(cqlQuery.getQueryModifier().getDistinctAttribute()!=null){
 	        				Object object = getDistinctAttributeValue(project,project.getClass());
 	        				if(!filteredList.contains(String.valueOf(object))){
 	        					filteredList.add(object);
 	        				}
 	        			}else if(cqlQuery.getQueryModifier().getAttributeNames()!=null){
 	        				Object object = getMultipleAttributeValues(project,project.getClass());
 	        				filteredList.add(object);
 	        			}else{
 	        				filteredList.add(completeObjectsList.get(i));
 	        			}
         			}else{
         				filteredList.add(completeObjectsList.get(i));
         			}
         		}else{
         			LOG.debug("User is not authorized to retrieve Projects with project id"+project.getProjectId());
         		}
         	}else if(completeObjectsList.get(i) instanceof org.cagrid.redcap.EventArms){
         		EventArms eventArms = (org.cagrid.redcap.EventArms)completeObjectsList.get(i);
         		boolean authorized = iterateUserCollectionProtocol(authorizedProjectsList, eventArms.getProjectId());
         		if(authorized){
         			LOG.debug("User authorized to retrive EventArms with project id"+eventArms.getProjectId());
         			if(cqlQuery.getQueryModifier()!=null){
 	        			if(cqlQuery.getQueryModifier().getDistinctAttribute()!=null){
 	        				Object object = getDistinctAttributeValue(eventArms,eventArms.getClass());
 	        				if(!filteredList.contains(String.valueOf(object))){
 	        					filteredList.add(object);
 	        				}
 	        			}else if(cqlQuery.getQueryModifier().getAttributeNames()!=null){
 	        				Object object = getMultipleAttributeValues(eventArms,eventArms.getClass());
 	        				filteredList.add(object);
 	        			}else{
 	        				filteredList.add(completeObjectsList.get(i));
 	        			}
         			}else{
         				filteredList.add(completeObjectsList.get(i));
         			}
         		}else{
         			LOG.debug("User is not authorized to retrieve EventArms with project id"+eventArms.getProjectId());
         		}
         	}else if(completeObjectsList.get(i) instanceof org.cagrid.redcap.Events){
         		Events events = (org.cagrid.redcap.Events)completeObjectsList.get(i);
         		boolean authorized = iterateUserCollectionProtocol(authorizedProjectsList, events.getEventId());
         		if(authorized){
         			LOG.debug("User authorized to retrive Events with project id"+events.getEventId());
         			if(cqlQuery.getQueryModifier()!=null){
 	        			if(cqlQuery.getQueryModifier().getDistinctAttribute()!=null){
 	        				Object object = getDistinctAttributeValue(events,events.getClass());
 	        				if(!filteredList.contains(String.valueOf(object))){
 	        					filteredList.add(object);
 	        				}
 	        			}else if(cqlQuery.getQueryModifier().getAttributeNames()!=null){
 	        				Object object = getMultipleAttributeValues(events,events.getClass());
 	        				filteredList.add(object);
 	        			}else{
 	        				filteredList.add(completeObjectsList.get(i));
 	        			}
         			}else{
         				filteredList.add(completeObjectsList.get(i));
         			}
         		}else{
         			LOG.debug("User is not authorized to retrieve Events with project id"+events.getEventId());
         		}
         	}else if(completeObjectsList.get(i) instanceof org.cagrid.redcap.Data){
         		Data data = (org.cagrid.redcap.Data)completeObjectsList.get(i);
         		//boolean authorized = iterateUserCollectionProtocol(authorizedProjectsList, data);
         		boolean authorized = false;
         		DataAuth dataInAuthList = null;
         		for(int index=0;index<authorizedProjectsList.size();index++){
         			dataInAuthList = (DataAuth)authorizedProjectsList.get(index);
         			if((dataInAuthList.getRecord() == data.getRecord())
 							&&(dataInAuthList.getEventId()==data.getEventId())&&(dataInAuthList.getProjectId()==data.getProjectId())
 							&&(dataInAuthList.getFieldName().equals(data.getFieldName()))
 							){
         				//data=dataInAuthList;
         				data = new Data();
         				data.setProjectId(dataInAuthList.getProjectId());
         				data.setFieldName(dataInAuthList.getElementLabel());
         				data.setRecord(dataInAuthList.getRecord());
         				data.setValue(dataInAuthList.getValue());
         				data.setEventId(dataInAuthList.getEventId());
         				authorized=true;
         				break;
         			}
         		}
         		if(authorized){
         			LOG.debug("User authorized to retrive Data with project id"+data.getProjectId()+" FieldName: "+data.getFieldName()+" Record: "+data.getRecord()+" Value: "+data.getValue());
         			if(cqlQuery.getQueryModifier()!=null){
 	        			if(cqlQuery.getQueryModifier().getDistinctAttribute()!=null){
 	        				Object object = getDistinctAttributeValue(data,data.getClass());
 	        				if(!filteredList.contains(String.valueOf(object))){
 	        					filteredList.add(object);
 	        				}
 	        			}else if(cqlQuery.getQueryModifier().getAttributeNames()!=null){
 	        				Object object = getMultipleAttributeValues(data,data.getClass());
 	        				filteredList.add(object);
 	        			}else{
 	        				filteredList.add(completeObjectsList.get(i));
 	        			}
         			}else{
         				//filteredList.add(completeObjectsList.get(i));
         				filteredList.add(data);
         			}
         		}else{
         			LOG.debug("User is not authorized to retrieve Data with project id"+data.getProjectId()+" Field Name:"+data.getFieldName()+" Record:"+data.getRecord()+" Value:"+data.getValue());
         		}
         	}else if(completeObjectsList.get(i) instanceof org.cagrid.redcap.Forms){
         		Forms forms = (org.cagrid.redcap.Forms)completeObjectsList.get(i);
         		boolean authorized = iterateUserCollectionProtocol(authorizedProjectsList, forms.getFormName());
         		if(authorized){
         			LOG.debug("User authorized to retrive Forms with project id"+forms.getFormName());
         			if(cqlQuery.getQueryModifier()!=null){
 	        			if(cqlQuery.getQueryModifier()!=null && cqlQuery.getQueryModifier().getDistinctAttribute()!=null){
 	        				Object object = getDistinctAttributeValue(forms,forms.getClass());
 	        				if(!filteredList.contains(String.valueOf(object))){
 	        					filteredList.add(object);
 	        				}
 	        			}else if(cqlQuery.getQueryModifier()!=null && cqlQuery.getQueryModifier().getAttributeNames()!=null){
 	        				Object object = getMultipleAttributeValues(forms,forms.getClass());
 	        				filteredList.add(object);
 	        			}else{
 	        				filteredList.add(completeObjectsList.get(i));
 	        			}
         			}else{
         				filteredList.add(completeObjectsList.get(i));
         			}
         		}else{
         			LOG.debug("User is not authorized to retrieve Forms with project id"+forms.getProjectId());
         		}
         	}else if(completeObjectsList.get(i) instanceof org.cagrid.redcap.EventsCalendar){
         		EventsCalendar eventsCalendar = (org.cagrid.redcap.EventsCalendar)completeObjectsList.get(i);
         		boolean authorized = iterateUserCollectionProtocol(authorizedProjectsList, eventsCalendar.getProjectId());
         		if(authorized){
         			LOG.debug("User authorized to retrive EventsCalendar with project id"+eventsCalendar.getProjectId());
         			if(cqlQuery.getQueryModifier()!=null){
 	        			if(cqlQuery.getQueryModifier().getDistinctAttribute()!=null){
 	        				Object object = getDistinctAttributeValue(eventsCalendar,eventsCalendar.getClass());
 	        				if(!filteredList.contains(String.valueOf(object))){
 	        					filteredList.add(object);
 	        				}
 	        			}else if(cqlQuery.getQueryModifier().getAttributeNames()!=null){
 	        				Object object = getMultipleAttributeValues(eventsCalendar,eventsCalendar.getClass());
 	        				filteredList.add(object);
 	        			}else{
 	        				filteredList.add(completeObjectsList.get(i));
 	        			}
         			}else{
         				filteredList.add(completeObjectsList.get(i));
         			}
         		}else{
         			LOG.debug("User is not authorized to retrieve EventsCalendar with project id"+eventsCalendar.getProjectId());
         		}
         	}else{
         		//do nothing return everything
         		LOG.debug("object is not instance of any avaliable data types");
         	}
         }
         LOG.debug("Filtered results list size"+filteredList.size());
         return filteredList;
 	 }
 	 
 	private String getCallerId() throws QueryProcessingException{
 		String rcUser = "";
 		try{
 			String caller = org.globus.wsrf.security.SecurityManager.getManager().getCaller();
 			int index = caller.lastIndexOf("=");
 			String gridUser = caller.substring(index+1, caller.length());
 	        userMapping = RedcapDataServiceConfiguration.getConfiguration().getRcUserGridMapping();
 	        rcUser = PropertiesUtil.getUsernames(gridUser,userMapping);
 	        LOG.debug("Mapping Grid User["+gridUser+"] with Redcap username["+rcUser);
 	        if(rcUser==null){
 	    	   throw new QueryProcessingException("Invalid user credentials. No mapping for grid user"+gridUser+" in Redcap "+rcUser);
 	        }
 		}catch(Exception e){
 			throw new QueryProcessingException(e);
 	    }
        return rcUser;
     }
 	
 	private boolean iterateUserCollectionProtocol(List<Object> userCollectionProtocol,Object value){
 		boolean present = false;
 		for(int i=0;i<userCollectionProtocol.size();i++){
 			try{
 				if(value.getClass().getName().equals(Data.class.getName())){
 					
 					Data data = (Data)userCollectionProtocol.get(i);
 					Data data1 = (Data)value;
 					if(
 							//(data1.getValue().equals(data.getValue()))&&
 							(data1.getRecord() == data.getRecord())
 							&&(data1.getEventId()==data.getEventId())&&(data1.getProjectId()==data.getProjectId())
 							&&(data1.getFieldName().equals(data.getFieldName()))
 							){
 						present=true;
 					}
 				}else{
 					if(userCollectionProtocol.get(i).equals(String.valueOf(value))){
 						present = true;
 					}
 				}
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 		return present;
 	}
 	
 	
 	/*
      * Pre-process the CQLRequest Query Modifier. 1. returns immediately if no
      * query modifier is present 2. create new query modifier 3. if query
      * modifier is a "count" query, set new query modifier's isCountOnly setting
      * to false to allow return of objects 4. If the query modifier has distinct attributes
      * add to distinct attribute list and return null 5. If query modifier has
      * multiple attributes add those to multiple attribute list
      * and return null.
      * 
      * @param queryModifier:CQL request QueryModifier
      * @return: CQLRequest QueryModifier
      */
     private QueryModifier preProcessQueryModifier(QueryModifier queryModifier) {
         LOG.debug("preProcessQueryModifier...");
 
         if (null == queryModifier) {
             return null;
         }
         QueryModifier newQueryModifier = new QueryModifier();
 
         // remove count modifier to allow return of objects or attributes
         // to allow filtering
         if (queryModifier.isCountOnly()) {
             newQueryModifier.setCountOnly(false);
         }
         
         if(queryModifier.getDistinctAttribute()!=null){
         	distinctAttributeList = new ArrayList<String>();
         	distinctAttributeList.add(queryModifier.getDistinctAttribute());
         }
         
         if(queryModifier.getAttributeNames()!=null){
         	attributeList = new ArrayList<String>();
         	attributeList = Arrays.asList(queryModifier.getAttributeNames());
         }
         return null;
     }
         
    private String getDistinctAttributeValue(Object object,Class cls) throws QueryProcessingException{
     	try{
 	    	if(distinctAttributeList!=null && distinctAttributeList.size()>0){
 	    		String name = this.distinctAttributeList.get(0).toString();
 	    		name = name.substring(0, 1).toUpperCase()+name.substring(1,name.length());
 	    		String methodName = "get"+name;
 	    		LOG.debug("Attribute "+name+" from "+object.getClass().getCanonicalName());
 	    		object = getObjectWithReqAttribute(methodName, object, object, cls);
 	    		LOG.debug("value:"+String.valueOf(object));
 	    	}else{
 	    		LOG.error("Invalid distinct attribute list");
 	    	}
     	}catch(Exception e){
     		throw new QueryProcessingException(e);
     	}
    	return String.valueOf(object);
     }
     
     private Object[] getMultipleAttributeValues(Object object,Class clas) throws QueryProcessingException{
     	Object temp = object;
     	String[] objList = new String[attributeList.size()];
     	try{
     		if(attributeList!=null && attributeList.size()>0){
     			LOG.debug("Attribute List size : "+attributeList.size());
     			for(int i=0;i<attributeList.size();i++){
     				String name = this.attributeList.get(i).toString();
     	    		name = name.substring(0, 1).toUpperCase()+name.substring(1,name.length());
     	    		String methodName = "get"+name;
     	    		
     	    		temp = getObjectWithReqAttribute(methodName, temp, object, clas);
     	    		objList[i]=String.valueOf(temp);
     	    		LOG.debug("Attribute "+name+" from "+object.getClass().getCanonicalName()+" value:"+String.valueOf(temp));
     			}
     		}
     	}catch(Exception e){
     		throw new QueryProcessingException(e);
     	}
     	return objList;
     }
     
     @SuppressWarnings("all")
     private Object getObjectWithReqAttribute(String methodName,Object tempObject,Object object,Class clas) throws QueryProcessingException{
     	try{
 	    	Method method = clas.getMethod(methodName, null);
 			if(object instanceof Projects){
 				tempObject = method.invoke((Projects)object, null);
 			}else if(object instanceof EventArms){
 				tempObject = method.invoke((EventArms)object, null);
 			}else if(object instanceof Events){
 				tempObject = method.invoke((Events)object, null);
 			}else if(object instanceof EventsCalendar){
 				tempObject = method.invoke((EventsCalendar)object, null);
 			}else if(object instanceof Forms){
 				tempObject = method.invoke((Forms)object, null);
 			}else if(object instanceof Data){
 				tempObject = method.invoke((Data)object, null);
 			}else{
 				LOG.error("Object not instance of any available types");
 			}
     	}catch(Exception exp){
     		LOG.error("Unable to invoke method on object",exp);
     		throw new QueryProcessingException("Unable to invoke method on object",exp);
     	}
     	return tempObject;
     }
 }
