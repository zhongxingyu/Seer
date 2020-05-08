 package gov.nih.nci.gss.api;
 
 import gov.nih.nci.gss.domain.DataService;
 import gov.nih.nci.gss.domain.DataServiceGroup;
 import gov.nih.nci.gss.domain.DomainClass;
 import gov.nih.nci.gss.domain.DomainModel;
 import gov.nih.nci.gss.domain.GridService;
 import gov.nih.nci.gss.domain.HostingCenter;
 import gov.nih.nci.gss.domain.PointOfContact;
 import gov.nih.nci.gss.domain.StatusChange;
 import gov.nih.nci.gss.util.Cab2bTranslator;
 import gov.nih.nci.gss.util.NamingUtil;
 import gov.nih.nci.gss.util.StringUtil;
 import gov.nih.nci.system.applicationservice.ApplicationException;
 import gov.nih.nci.system.dao.orm.ORMDAOImpl;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.hibernate.classic.Session;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.springframework.util.FileCopyUtils;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 /**
  * Custom JSON API for returning service metadata in bulk and querying services
  * via caB2B. 
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class JSONDataService extends HttpServlet {
 
     private static Logger log = Logger.getLogger(JSONDataService.class);
 
     // TODO: externalize this
     private static final String HOST_KEY = "Hosting Institution";
     
     private enum Verb {
     	GET,
     	POST
     };
     
     private enum QueryStatus {
     	RUNNING,
     	EXPIRED,
     	UNKNOWN,
     	FOUND
     };
     
     
     /** Date format for serializing dates into JSON. 
      * Must match the data format used by the iPhone client */
     private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz");
 
     private static final String GET_SERVICE_HQL_SELECT = 
              "select service from gov.nih.nci.gss.domain.GridService service ";
 
     private static final String GET_SERVICE_HQL_JOIN_STATUS = 
             "left join fetch service.statusHistory status ";
         
     private static final String GET_SERVICE_HQL_WHERE_STATUS = 
              "where ((status.changeDate is null) or (status.changeDate = (" +
              "  select max(changeDate) " +
              "  from gov.nih.nci.gss.domain.StatusChange s " +
              "  where s.gridService = service " +
              "))) ";
 
     private static final String GET_HOST_HQL_SELECT = 
              "select host from gov.nih.nci.gss.domain.HostingCenter host ";
     
     /** JSON string describing the usage of this service */
     private String usage;
     
     /** Hibernate session factory */
     private SessionFactory sessionFactory;
 
     /** Service that manages background queries and results */
     private QueryService queryService;
     
     private NamingUtil namingUtil;
     
     @Override
     public void init() throws ServletException {
         
         try {
             WebApplicationContext ctx =  
                 WebApplicationContextUtils.getWebApplicationContext(getServletContext());
             this.sessionFactory = ((ORMDAOImpl)ctx.getBean("ORMDAO")).getHibernateTemplate().getSessionFactory();
             this.queryService = new QueryService(sessionFactory);
             
             this.usage = FileCopyUtils.copyToString(new InputStreamReader(
                 JSONDataService.class.getResourceAsStream("/rest_api_usage.js")));
             
             this.namingUtil = new NamingUtil(sessionFactory);
         }
         catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     @Override
     public void doGet(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
     	doREST(Verb.GET, request, response);
     }
     
     @Override
     public void doPost(HttpServletRequest request, HttpServletResponse response) 
             throws ServletException, IOException {
     	doREST(Verb.POST, request, response);
     }
     
     @Override
     public void destroy() {
         queryService.close();
         super.destroy();
     }
 
     /**
      * 
      * @param verb
      * @param request
      * @param response
      * @throws ServletException
      * @throws IOException
      */
     private void doREST(Verb verb, HttpServletRequest request, 
     		HttpServletResponse response) throws ServletException, IOException {
 
         PrintWriter pw = new PrintWriter(response.getOutputStream());
 		try {
 			response.setContentType("application/json");
 
 			String path = request.getPathInfo();
 			if (path == null) {
 				pw.print(getJSONUsage());
 			}
 			else {
 			    String[] pathList = path.split("/");
 
 			    if (pathList.length < 2) {
 			    	pw.print(getJSONUsage());
 			    }
 			    else {
 			        String noun = pathList[1];                
 			        pw.print(getRESTResponse(verb, noun, pathList, request));
 			    }
 			}
 		} 
 		catch (Exception e) {
             log.error("JSON service error",e);
             pw.print(getJSONError(e.getClass().getName(), e.getMessage()));
 		}
 		finally {
 			pw.close();
 		}
     }
     
     /**
      * Process a REST request for a given noun.
      * @param noun
      * @param pathList
      * @param request
      * @return
      */
     private String getRESTResponse(Verb verb, String noun, String[] pathList, 
             HttpServletRequest request) throws Exception {
     	
         if ("service".equals(noun)) {
             // Return details about services, or a single service
             
             String id = null;
             if (pathList.length > 2) {
                 id = pathList[2];
             }
             
             boolean includeMetadata = "1".equals(request.getParameter("metadata"));
             boolean includeModel = "1".equals(request.getParameter("model"));
             return getServiceJSON(id, includeMetadata, includeModel);
         }
         if ("host".equals(noun)) {
             // Return details about hosts, or a single hosts
             
             String id = null;
             if (pathList.length > 2) {
                 id = pathList[2];
             }
             
             return getHostJSON(id);
         }
         else if ("runQuery".equals(noun)) { 
             // Query grid services using caB2B 
 
             String clientId = request.getParameter("clientId");
             String searchString = request.getParameter("searchString");
             String serviceGroup = request.getParameter("serviceGroup");
             String serviceIdConcat = request.getParameter("serviceIds");
             String[] serviceIds = request.getParameterValues("serviceId");
             String[] serviceUrls = request.getParameterValues("serviceUrl");
 
             if (StringUtil.isEmpty(clientId)) {
                 return getJSONError("UsageError", "Specify your clientId.");
             }
 
             if (StringUtil.isEmpty(searchString)) {
                 return getJSONError("UsageError", "Specify a searchString to search on.");
             }
              
             List<String> serviceUrlList = new ArrayList<String>();
             for(String serviceUrl : serviceUrls) {
                 serviceUrlList.add(serviceUrl);
             }
             
            if (serviceIds == null && serviceIdConcat != null) {
                 serviceIds = serviceIdConcat.split(",");
             }
             
            if (serviceIds != null && serviceIds.length > 0) {
                 Session s = sessionFactory.openSession();
                 
                 for(String serviceId : serviceIds) {
                     
                     String hql = GET_SERVICE_HQL_SELECT+" where service.id = ?";
                     List<GridService> services = s.createQuery(hql).setString(0, serviceId).list();
                     
                     if (services.isEmpty()) {
                         return getJSONError("UsageError", "Unknown service id: "+serviceId);
                     }
                     if (services.size() > 1) {
                         log.error("More than one matching service for service id: "+serviceId);
                     }
                     
                     serviceUrlList.add(services.get(0).getUrl());
                 }
                 
                 s.close();
             }
             
             QueryParams queryParams = null; 
 
             if (!serviceUrlList.isEmpty()) {
                 queryParams = new QueryParams(clientId, searchString, 
                     null, serviceUrlList);
             }
             else if (!StringUtil.isEmpty(serviceGroup)) {
                 if (serviceGroup == null) {
                     return getJSONError("UsageError", "Unrecognized serviceGroup '"+serviceGroup+"'");
                 }
                 
                 queryParams = new QueryParams(clientId, searchString, serviceGroup, null);
             }
             else {
                 return getJSONError("UsageError", "Specify serviceGroup or serviceId or serviceUrl.");
             }
 
             Cab2bQuery query = queryService.executeQuery(queryParams);
 
             log.info("Executing "+queryParams+" as job " +query.getJobId());
             
             JSONObject jsonObj = new JSONObject();
             jsonObj.put("status", QueryStatus.RUNNING);
             jsonObj.put("job_id", query.getJobId());
             return jsonObj.toString();
     	}
         else if ("query".equals(noun)) {
 
             String clientId = request.getParameter("clientId");
             String jobId = request.getParameter("jobId");
             boolean collapse = "1".equals(request.getParameter("collapse"));
 
             if (StringUtil.isEmpty(clientId)) {
                 return getJSONError("UsageError", "Specify your clientId.");
             }
 
             if (StringUtil.isEmpty(jobId)) {
                 return getJSONError("UsageError", "Specify a jobId.");
             }
             
             Cab2bQuery query = queryService.retrieveQuery(jobId);
             
             if (query == null) {
                 return getJSONStatus(QueryStatus.UNKNOWN);
             }
             
             if (!query.getQueryParams().getClientId().equals(clientId)) {
                 return getJSONStatus(QueryStatus.UNKNOWN);
             }
             
             log.info("Got query "+jobId+": "+query.getQueryParams());
             
             // What if the query isn't completed?
             if (!query.isDone()) {
                 
                 // Return nothing and let the client poll
                 if ("1".equals(request.getParameter("async"))) {
                     log.info("Returning asyncronously for query: "+jobId);
                     return getJSONStatus(QueryStatus.RUNNING);	 
                 }
                 
                 // Block until the query is completed
                 synchronized (query) {
                     try {
                         log.info("Blocking until query is complete: "+jobId);
                         query.wait();
                     }
                     catch (InterruptedException e) {
                         log.error("Interrupted wait",e);
                     }
                 }
             }
 
             return getQueryResultsJSON(query,collapse);
     	}
         
         // If the noun was good we would've returned by now
         return getJSONError("UsageError", "Unrecognized noun '"+noun+"'");
     }
 
 	/**
      * Returns a JSON object representing the basic attributes of a service.
      * @param service
      * @return
      * @throws JSONException
      */
     private JSONObject getJSONObjectForService(GridService service)
             throws JSONException {
 
         JSONObject jsonService = new JSONObject();
         jsonService.put("id", service.getId().toString());
         jsonService.put("name", service.getName());
         jsonService.put("version", service.getVersion());
         jsonService.put("class", service.getClass().getSimpleName());
         // TODO: move this into the scheduled job
         jsonService.put("simple_name", namingUtil.getSimpleName(service.getName()));
         //jsonService.put("simple_name", service.getSimpleName());
         jsonService.put("url", service.getUrl());
 
         HostingCenter host = service.getHostingCenter();
         if (host != null) {
             jsonService.put("host_id", host.getId().toString());
             jsonService.put("host_short_name", host.getShortName());
         }
         
 
         if (service instanceof DataService) {
             DataService dataService = (DataService)service;
             DataServiceGroup group = dataService.getGroup();
             if (group != null) jsonService.put("group", group.getName());
             if (dataService.getSearchDefault()) {
                 jsonService.put("search_default", "true");
             }
         }
         
         Collection<StatusChange> scs = service.getStatusHistory(); 
         if (scs.size() > 1) {
             log.warn("More than 1 status change was returned for service with id="+service.getId());
         }
 
         if (!scs.isEmpty()) {
             StatusChange statusChange = scs.iterator().next();
             jsonService.put("status", statusChange.getNewStatus());
             jsonService.put("last_update", df.format(statusChange.getChangeDate()));
             jsonService.put("publish_date", df.format(service.getPublishDate()));
         }
         
         return jsonService;
     }
     
     /**
      * Returns a JSON string with all the metadata about a particular service.
      * @return JSON-formatted String
      * @throws JSONException
      * @throws ApplicationException
      */
     private String getServiceJSON(String serviceId, 
             boolean includeMetadata, boolean includeModel) 
             throws JSONException, ApplicationException {
 
         Session s = sessionFactory.openSession();
         JSONObject json = new JSONObject();
         
         try {
             // Create the HQL query
             StringBuffer hql = new StringBuffer(GET_SERVICE_HQL_SELECT);
             hql.append(GET_SERVICE_HQL_JOIN_STATUS);
             hql.append("left join fetch service.hostingCenter ");
             if (includeModel) hql.append("left join fetch service.domainModel ");
             hql.append(GET_SERVICE_HQL_WHERE_STATUS);
             if (serviceId != null) hql.append("and service.id = ?");
             
             // Create the Hibernate Query
             Query q = s.createQuery(hql.toString());
             if (serviceId != null) q.setString(0, serviceId);
             
             // Execute the query
             List<GridService> services = q.list();
             
             JSONArray jsonArray = new JSONArray();
             json.put("services", jsonArray);
             
             for (GridService service : services) {
                 
                 JSONObject jsonService = getJSONObjectForService(service);
                 jsonArray.put(jsonService);
                 
                 // service host short name
 
                 if (includeMetadata) {
                     
                     // service details
                     
                     jsonService.put("description", service.getDescription());
                     
                     // service pocs
                     
                     JSONArray jsonPocs = new JSONArray();
                     for (PointOfContact poc : service.getPointOfContacts()) {
                         JSONObject jsonPoc = new JSONObject();
                         jsonPoc.put("name", poc.getName());
                         jsonPoc.put("role", poc.getRole());
                         jsonPoc.put("affiliation", poc.getAffiliation());
                         jsonPoc.put("email", poc.getEmail());
                         jsonPocs.put(jsonPoc);
                     }
                     jsonService.put("pocs", jsonPocs);
                     
                     // service host details
 
                     HostingCenter host = service.getHostingCenter();
                     JSONObject hostObj = new JSONObject();
                     jsonService.put("hosting_center", hostObj);
                     
                     if (host != null) {
 
                         hostObj.put("short_name", host.getShortName());
                         hostObj.put("long_name", host.getLongName());
                         hostObj.put("country_code", host.getCountryCode());
                         hostObj.put("state_province", host.getStateProvince());
                         hostObj.put("locality", host.getLocality());
                         hostObj.put("postal_code", host.getPostalCode());
                         hostObj.put("street", host.getStreet());
                         
                         // service host pocs
         
                         jsonPocs = new JSONArray();
                         for (PointOfContact poc : host.getPointOfContacts()) {
                             JSONObject jsonPoc = new JSONObject();
                             jsonPoc.put("name", poc.getName());
                             jsonPoc.put("role", poc.getRole());
                             jsonPoc.put("affiliation", poc.getAffiliation());
                             jsonPoc.put("email", poc.getEmail());
                             jsonPocs.put(jsonPoc);
                         }
                         hostObj.put("pocs", jsonPocs);
                     }
                 }
                 
                 if (includeModel && (service instanceof DataService)) {
                     
                     DomainModel model = ((DataService)service).getDomainModel();
 
                     JSONObject modelObj = new JSONObject();
                     jsonService.put("domain_model", modelObj);
                     
                     if (model != null) {
                         
                         // domain model
                         
                         modelObj.put("long_name", model.getLongName());
                         modelObj.put("version", model.getVersion());
                         modelObj.put("description", model.getDescription());
                         
                         // model classes
         
                         JSONArray jsonClasses = new JSONArray();
                         for (DomainClass dc : model.getClasses()) {
                             JSONObject jsonClass = new JSONObject();
                             jsonClass.put("name", dc.getClassName());
                             jsonClass.put("package", dc.getDomainPackage());
                             jsonClass.put("description", dc.getDescription());
                             jsonClasses.put(jsonClass);
                         }
                         modelObj.put("classes", jsonClasses);
                     }
                 }
             }
 
         }
         finally {
             s.close();
         }
         
         return json.toString();
     }
 
     /**
      * Returns a JSON string with all the metadata about a particular host.
      * @return JSON-formatted String
      * @throws JSONException
      * @throws ApplicationException
      */
     private String getHostJSON(String hostId) 
             throws JSONException, ApplicationException {
 
         Session s = sessionFactory.openSession();
         JSONObject json = new JSONObject();
         
         try {
             // Create the HQL query
             StringBuffer hql = new StringBuffer(GET_HOST_HQL_SELECT);
             if (hostId != null) hql.append("where host.id = ?");
             
             // Create the Hibernate Query
             Query q = s.createQuery(hql.toString());
             if (hostId != null) q.setString(0, hostId);
             
             // Execute the query
             List<HostingCenter> hosts = q.list();
             
             JSONArray jsonArray = new JSONArray();
             json.put("hosts", jsonArray);
             
             for (HostingCenter host : hosts) {
 
                 JSONObject hostObj = new JSONObject();
                 jsonArray.put(hostObj);
                                 
                 // service host details
                 
                 hostObj.put("id", host.getId().toString());
                 hostObj.put("short_name", host.getShortName());
                 hostObj.put("long_name", host.getLongName());
                 hostObj.put("country_code", host.getCountryCode());
                 hostObj.put("state_province", host.getStateProvince());
                 hostObj.put("locality", host.getLocality());
                 hostObj.put("postal_code", host.getPostalCode());
                 hostObj.put("street", host.getStreet());
                 
                 // service host pocs
 
                 JSONArray jsonPocs = new JSONArray();
                 for (PointOfContact poc : host.getPointOfContacts()) {
                     JSONObject jsonPoc = new JSONObject();
                     jsonPoc.put("name", poc.getName());
                     jsonPoc.put("role", poc.getRole());
                     jsonPoc.put("affiliation", poc.getAffiliation());
                     jsonPoc.put("email", poc.getEmail());
                     jsonPocs.put(jsonPoc);
                 }
                 hostObj.put("pocs", jsonPocs);
                 
             }
 
         }
         finally {
             s.close();
         }
         
         return json.toString();
     }
     
     /**
      * Returns a JSON string with the results (or error) produced by the given
      * query. The query should have already finished executing. If not, an
      * IllegalStateException is thrown (not as JSON!).
      * @param query
      * @return
      */
     private String getQueryResultsJSON(Cab2bQuery query, boolean collapse) 
     		throws JSONException {
         
         if (!query.isDone()) {
             throw new IllegalStateException("Query has not completed: "+query);
         }
             
         Exception e = query.getException();
         if (e != null) {
             return getJSONError(e.getClass().getName(), e.getMessage());
         }
 
     	JSONObject json = new JSONObject(query.getResultJson());
 
         if (json.has("error")) {
             return query.getResultJson();
         }
 
 		Cab2bTranslator translator = queryService.getCab2b().getCab2bTranslator();
     	String modelGroupName = json.getString("modelGroupName");
         String serviceGroup = translator.getServiceGroupForModelGroup(modelGroupName);
     	if (modelGroupName != null) {
             json.remove("modelGroupName");
     		json.put("serviceGroup", serviceGroup);
     	}
     	
         if (collapse) {
         	Map<String,Map<String,JSONObject>> servers = 
         	    new LinkedHashMap<String,Map<String,JSONObject>>();
         	
         	// the key to discriminate on for duplicates
         	String primaryKey = translator.getPrimaryKeyForServiceGroup(serviceGroup);
         	
         	JSONObject queries = json.getJSONObject("results");
         	for(Iterator i = queries.keys(); i.hasNext(); ) {
         		String queryName = (String)i.next();
         		JSONObject urls = queries.getJSONObject(queryName);
         		for(Iterator j = urls.keys(); j.hasNext(); ) {
             		String url = (String)j.next();
             		JSONArray objs = urls.getJSONArray(url);
 
                     Map<String,JSONObject> serverUnique = servers.get(url);
                         
             		if (serverUnique == null) {
             		    serverUnique = new LinkedHashMap<String,JSONObject>();
             		    servers.put(url, serverUnique);
             		}
 
             		for(int k=0; k<objs.length(); k++) {
             			JSONObject obj = objs.getJSONObject(k);
             			String key = null;
             			try {
             			    key = obj.getString(primaryKey);
             			}
             			catch (JSONException x) {
             				log.error("Error getting unique key",x);
             				key = obj.toString();
             			}
             			if (!serverUnique.containsKey(key)) {
             			    serverUnique.put(key, obj);
             			}
             		}
         		}
         	}
         	
         	JSONObject jsonUrls = new JSONObject();
         	for(String url : servers.keySet()) {
         	    JSONArray jsonObjs = new JSONArray();
         	    jsonUrls.put(url, jsonObjs);
         	    for (JSONObject obj : servers.get(url).values()) {
         	        jsonObjs.put(obj);
         	    }
         	}
         	json.put("results", jsonUrls);
         	
         }
         
         return json.toString();
     }
     
     /**
      * Returns a JSON string with the given error message.
      * @return JSON-formatted String
      */
     private String getJSONError(String exception, String message) {
         return "{\"error\":"+JSONObject.quote(exception)+
                 ",\"message\":"+JSONObject.quote(message)+"}";
     }
     
     /**
      * Get a JSON string with a simple query status message.
      * @param status
      * @return
      * @throws JSONException
      */
     private String getJSONStatus(Object status) throws JSONException {
         JSONObject jsonObj = new JSONObject();
         jsonObj.put("status", status);
         return jsonObj.toString();
     }
     
     /**
      * Returns a JSON string with usage instructions, or an error if a problem
      * occurs.
      * @return JSON-formatted String
      */
     private String getJSONUsage() {
         return usage;
     }
     
     
 }
