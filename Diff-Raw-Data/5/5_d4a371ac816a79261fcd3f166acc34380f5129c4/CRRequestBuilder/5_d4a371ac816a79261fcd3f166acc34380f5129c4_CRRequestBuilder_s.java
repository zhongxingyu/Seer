 package com.gentics.cr.util;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 import javax.portlet.Portlet;
 import javax.portlet.PortletRequest;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRConfigUtil;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.configuration.GenericConfiguration;
 import com.gentics.cr.rest.ContentRepository;
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 543 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRRequestBuilder {
 
   protected String repotype;
   protected boolean isDebug=false;
   protected boolean metaresolvable=false;
   protected String highlightquery;
   protected String filter;
   protected String query_and;
   protected String query_or;
   protected String query_not;
   protected String query_group;
   protected String start;
   protected String count;
   protected String type;
   protected String contentid;
   protected String wordmatch;
   protected String[] node_id;
   protected String[] attributes;
   protected String[] sorting;
   protected String[] plinkattributes;
   protected String[] permissions;
   protected String[] options;
   protected RequestWrapper request;
   protected Object response;
   
   protected GenericConfiguration config;
 
   private static Logger logger = Logger.getLogger(CRRequestBuilder.class);
 
   /**
    * Initializes the CRRequestBuilder from a {@link Portlet}.
    * @param portletRequest request from the {@link Portlet}
    */
   public CRRequestBuilder(final PortletRequest portletRequest) {
     this(portletRequest, null);
   }
 
   /**
    * Initializes the CRRequestBuilder from a {@link Portlet}.
    * @param portletRequest request from the {@link Portlet}
    * @param requestBuilderConfiguration configuration for the request builder
    */
   public CRRequestBuilder(final PortletRequest portletRequest,
       final GenericConfiguration requestBuilderConfiguration) {
     this(new RequestWrapper(portletRequest), requestBuilderConfiguration);
   }
 
 
   /**
    * Initializes the CRRequestBuilder from a {@link Servlet}.
    * @param servletRequest request from the {@link Servlet}
    */
   public CRRequestBuilder(final HttpServletRequest servletRequest) {
     this(servletRequest, null);
   }
 
 
   /**
    * Initializes the CRRequestBuilder from a {@link Servlet}.
    * @param servletRequest request from the {@link Servlet}
    * @param conf configuration for the request builder where we get the default
    * parameters from.
    */
   public CRRequestBuilder(final HttpServletRequest servletRequest,
       final GenericConfiguration conf) {
     this(new RequestWrapper(servletRequest), conf);
   }
 
   /**
    * Initializes the CRRequestBuilder in a general manner that is compatible
    * with {@link Portlet}s and {@link Servlet}s.
    * @param requestWrapper wrapped request from a {@link Servlet} or a
    * {@link Portlet}
    * @param requestBuilderConfiguration configuration for the request builder
    */
   public CRRequestBuilder(final RequestWrapper requestWrapper,
       final GenericConfiguration requestBuilderConfiguration) {
 
     this.config = requestBuilderConfiguration;
     GenericConfiguration defaultparameters = null;
     if (this.config != null) {
       defaultparameters = (GenericConfiguration)this.config.get(DEFAULPARAMETERS_KEY);
     }
     this.request = requestWrapper;
     this.filter = (String) requestWrapper.getParameter("filter");
     this.contentid = (String) requestWrapper.getParameter("contentid");
     this.count = requestWrapper.getParameter("count");
     this.start = (String) requestWrapper.getParameter("start");
     this.sorting = requestWrapper.getParameterValues("sorting");
     this.attributes =  this.prepareAttributesArray(requestWrapper.getParameterValues("attributes"));
     this.plinkattributes =  requestWrapper.getParameterValues("plinkattributes");
     this.permissions = requestWrapper.getParameterValues("permissions"); 
     this.options = requestWrapper.getParameterValues("options");
     this.type=requestWrapper.getParameter("type");
     this.isDebug = (requestWrapper.getParameter("debug")!=null && requestWrapper.getParameter("debug").equals("true"));
     this.metaresolvable = Boolean.parseBoolean(requestWrapper.getParameter(RequestProcessor.META_RESOLVABLE_KEY));
     this.highlightquery = requestWrapper.getParameter(RequestProcessor.HIGHLIGHT_QUERY_KEY);
     this.node_id = requestWrapper.getParameterValues("node");
     this.query_and = requestWrapper.getParameter("q_and");
     this.query_or = requestWrapper.getParameter("q_or");
     this.query_not = requestWrapper.getParameter("q_not");
     this.query_group = requestWrapper.getParameter("q_group");
 
     //Parameters used in mnoGoSearch for easier migration (Users should use
     //type=MNOGOSEARCHXML)
     if (this.filter == null) {
       this.filter = requestWrapper.getParameter("q");
     }
     if (this.count == null) {
       this.count = requestWrapper.getParameter("ps");
     }
     if (this.start == null && this.count != null) {
       String numberOfPageStr = (String) requestWrapper.getParameter("np");
       int numberOfPage;
       if (numberOfPageStr != null) {
         numberOfPage = Integer.parseInt(numberOfPageStr);
       } else {
         numberOfPage = 0;
       }
       int intCount = Integer.parseInt(this.count);
       this.start = (numberOfPage * intCount) + "";
     }
 
     //if filter is not set and contentid is => use contentid instad
     if (("".equals(filter) || filter == null) && contentid != null
         && !contentid.equals("")) {
       filter = "object.contentid == '" + contentid + "'";
     }
 
     addAdvancedSearchParameters();
 
     //SET PERMISSIONS-RULE
     filter = this.createPermissionsRule(filter, permissions);
 
     setRepositoryType(this.type);
 
 
     if (defaultparameters != null) {
       if (this.type == null) {
         this.type = (String) defaultparameters.get("type");
         setRepositoryType(this.type);
       }
       if (this.node_id == null) {
         String defaultNode = (String) defaultparameters.get("node");
         if (defaultNode != null) {
           this.node_id = defaultNode.split("^");
         }
       }
       if (this.attributes == null || this.attributes.length == 0) {
         String defaultAttributes =
           (String) defaultparameters.get("attributes");
         if (defaultAttributes != null) {
           this.attributes = defaultAttributes.split(",[ ]*");
         }
       }
       addAdvancedSearchParameters();
     }
   }
 
   /**
    * Returns String Array of Attributes to request.
    * @return string array with the attributes
    */
   public final String[] getAttributeArray() {
     return this.attributes;
   }
 
   /**
    * Get array of options.
    * @return array with options
    */
   public final String[] getOptionArray() {
     return this.options;
   }
 
   /**
    * Get Type of ContentRepository.
    * @return type of the contentrepository
    */
   public final String getRepositoryType() {
     if (this.repotype == null) {
       Properties props = this.getConfiguredContentRepositories();
       if (props != null) {
         String v = props.getProperty("DEFAULT");
         if (v != null) {
           this.repotype =  v;
         }
       }
       if (this.repotype == null) {
         this.repotype = "XML";
       }
 
     }
     return this.repotype;
   }
 
   /**
    * Returns true if this is a debug request.
    * @return true if debug is enabled
    */
   public final boolean isDebug() {
     return this.isDebug;
   }
 
   private void addAdvancedSearchParameters() {
     if (filter == null || "".equals(filter)) {
       if (query_and != null && !"".equals(query_and)) {
         String filter_and = "";
         for (String query : query_and.split(" ")) {
           if (!"".equals(filter_and)) {
             filter_and+=" AND ";
           }
           filter_and+=query.toLowerCase();
         }
         if(!"".equals(filter_and))filter = "(" + filter_and + ")";
         query_and = "";
       }
       if(query_or != null && !"".equals(query_or)){
         String filter_or = "";
         for(String query:query_or.split(" ")){
           if(!"".equals(filter_or))filter_or+=" OR ";
           filter_or+=query.toLowerCase();
         }
         if(!"".equals(filter_or)) {
           if(!"".equals(filter))filter+= " AND ";
           filter += "(" + filter_or + ")";
         }
         query_or = "";
       }
       if(query_not != null && !"".equals(query_not)){
         String filter_not = "";
         for(String query:query_not.split(" ")){
           if(!"".equals(filter_not))filter_not+=" OR ";
           filter_not+=query.toLowerCase();
         }
         if(!"".equals(filter_not)) {
           if(!"".equals(filter))filter+= " AND ";
           filter += "NOT (" + filter_not + ")";
         }
         query_not = "";
       }
       if(query_group != null && !"".equals(query_group)){
         if(!"".equals(filter))filter+= " AND ";
         filter += " \"" + query_group.toLowerCase() + "\"";
         query_group = "";
       }
       
       
     }
     if ((filter != null && !"".equals(filter)) && this.node_id != null && this.node_id.length != 0 && !filter.matches("(.+[ (])?node_id\\:[0-9]+.*")){
       String node_filter = "";
       for(int i = 0; i < node_id.length; i++){
         if(node_filter != "") node_filter += " OR ";
         node_filter += "node_id:"+node_id[i];
       }
       node_id = new String[]{};
       filter = "(" + filter + ") AND ("+node_filter+")";
     }
   }
   
   private void setRepositoryType(String type) {
     //Initialize RepositoryType
     
     this.repotype = type;
     
   }
   
   
   
   private final static String DEFAULPARAMETERS_KEY = "defaultparameters";
 
 
 
 
   /**
    * Creates a CRRequest from the configuration.
    * @return created CRRequest
    */
   public final CRRequest getCRRequest() {
     CRRequest req = new CRRequest(filter, start, count, sorting, attributes,
         plinkattributes);
     req.setContentid(this.contentid);
     req.setRequest(this.request);
     req.setResponse(this.response);
     req.set(RequestProcessor.META_RESOLVABLE_KEY, this.metaresolvable);
     if (this.highlightquery != null) {
       req.set(RequestProcessor.HIGHLIGHT_QUERY_KEY, this.highlightquery);
     }
     return req;
   }
 
   /**
    * returns the request object.
    * @return the request
    */
   public final Object getRequest() {
     return this.request;
   }
 
   /**
    * Wrapps filter rule with the given set of permissions.
    * @param objectFilter TODO javadoc
    * @param userPermissions TODO javadoc
    * @return TODO javadoc
    */
   protected final String createPermissionsRule(final String objectFilter,
       final String[] userPermissions) {
     String ret = objectFilter;
     if ((userPermissions != null) && (userPermissions.length > 0)) {
       if ((objectFilter != null) && (!objectFilter.equals(""))) {
         ret = "(" + objectFilter + ") AND object.permissions CONTAINSONEOF "
           + CRUtil.prepareParameterArrayForRule(userPermissions);
       } else {
         ret = "object.permissions CONTAINSONEOF "
           + CRUtil.prepareParameterArrayForRule(userPermissions);
       }
     }
     return ret;
   }
 
   protected final String[] prepareAttributesArray(final String[] attributes) {
     ArrayList<String> ret = new ArrayList<String>();
     if (attributes != null) {
       for (String item : attributes) {
         if (item.contains(",")) {
           String[] items = item.split(",");
           for (String subatt : items) {
             ret.add(subatt);
           }
         } else {
           ret.add(item);
         }
       }
     }
     return ret.toArray(new String[ret.size()]);
   }
 
 
   private final Properties getConfiguredContentRepositories() {
     Object crs = this.config.get(REPOSITORIES_KEY);
     if (crs != null && crs instanceof GenericConfiguration) {
       GenericConfiguration crConf = (GenericConfiguration) crs;
       Properties crProperties = crConf.getProperties();
       return crProperties;
     }
     return null;
   }
 
   private static final String REPOSITORIES_KEY = "cr";
 
 
 
   private Hashtable<String,String> getRepositoryClassMap()
   {
     Hashtable<String,String> classmap = new Hashtable<String,String>();
     
     //ADD DEFAULT ENTRIES
     classmap.put("JSON", "com.gentics.cr.rest.json.JSONContentRepository");
     classmap.put("PHP", "com.gentics.cr.rest.php.PHPContentRepository");
     classmap.put("JAVAXML", "com.gentics.cr.rest.javaxml.JavaXmlContentRepository");
     classmap.put("JAVABIN", "com.gentics.cr.rest.javabin.JavaBinContentRepository");
     classmap.put("VELOCITY", "com.gentics.cr.rest.velocity.VelocityContentRepository");
     classmap.put("XML", "com.gentics.cr.rest.xml.XmlContentRepository");
     
     Properties confs = getConfiguredContentRepositories();
     if(confs!=null)
     {
       for(Entry<Object,Object> e:confs.entrySet())
       {
         String key = (String)e.getKey();
         if(!"default".equalsIgnoreCase(key))
         {
          classmap.put(key, (String)e.getValue());
         }
       }
     }
     
     return classmap;
   }
   
   
   /**
    * Create the ContentRepository for this request and give it the configuration. This is needed for the VelocityContentRepository
    * @param encoding Output encoding should be used
    * @param configUtil Config to get the Velocity Engine from
    * @return ContentRepository with the given settings.
    */
   
   public ContentRepository getContentRepository(String encoding, CRConfigUtil configUtil)
   {
     ContentRepository cr=null;
     
     Hashtable<String,String> classmap = getRepositoryClassMap();
     
    String cls = classmap.get(this.getRepositoryType());
     if(cls!=null)
     {
       //XmlContentRepository(String[] attr, String encoding)
       try {
         cr = (ContentRepository) Class.forName(cls).getConstructor(new Class[] {String[].class,String.class}).newInstance(this.getAttributeArray(),encoding);
       } catch (Exception e) {
         try{
           cr = (ContentRepository) Class.forName(cls).getConstructor(new Class[] {String[].class,String.class,CRConfigUtil.class}).newInstance(this.getAttributeArray(),encoding,configUtil);
         } catch(Exception ex) {
           try {
             cr = (ContentRepository) Class.forName(cls).getConstructor(new Class[] {String[].class,String.class,String[].class,CRConfigUtil.class}).newInstance(this.getAttributeArray(),encoding,null,configUtil);
           } catch(Exception exc) {
             logger.error("Could net create ContentRepository instance from class: "+cls, exc);
           }
         }
       }
     } else {
       logger.error("Could net create ContentRepository instance. No Type is set to the RequestBuilder");
     }
     return cr;
   }
 }
