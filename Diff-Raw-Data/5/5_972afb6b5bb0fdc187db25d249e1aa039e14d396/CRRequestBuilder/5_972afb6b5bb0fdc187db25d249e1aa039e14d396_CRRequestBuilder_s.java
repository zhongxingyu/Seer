 package com.gentics.cr.util;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Properties;
 import java.util.Map.Entry;
 
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
 	protected String debug;
 	protected String type;
 	protected String contentid;
 	protected String[] node_id;
 	protected String[] attributes;
 	protected String[] sorting;
 	protected String[] plinkattributes;
 	protected String[] permissions;
 	protected String[] options;
 	protected Object request;
 	protected Object response;
 	
 	protected GenericConfiguration config;
 	
 	private Logger logger = Logger.getLogger(CRRequestBuilder.class);
 	
 	/**
 	 * Returns String Array of Attributes to request
 	 * @return
 	 */
 	public String[] getAttributeArray()
 	{
 		return(this.attributes);
 	}
 	
 	/**
 	 * Returns Array of Options
 	 * @return
 	 */
 	public String[] getOptionArray()
 	{
 		return(this.options);
 	}
 	
 	/**
 	 * Get Type of ContentRepository
 	 * @return
 	 */
 	public String getRepositoryType()
 	{
 		if(this.repotype==null)
 		{
 			Properties props= this.getConfiguredContentRepositories();
 			if(props!=null)
 			{
 				String v = props.getProperty("DEFAULT");
 				if(v!=null)
 					this.repotype =  v;
 				
 			}
 			if(this.repotype==null)this.repotype="XML";
 			
 		}
 		return this.repotype;
 	}
 	
 	/**
 	 * Returns true if this is a debug request
 	 * @return
 	 */
 	public boolean isDebug()
 	{
 		return this.isDebug;
 	}
 	
 	
 	public CRRequestBuilder(PortletRequest request, GenericConfiguration config)
 	{
 		this.config = config;
 		this.request = request;
 		this.filter = (String) request.getParameter("filter");
 		String q = (String)request.getParameter("q");
 		if((this.filter==null || "".equals(filter)) && q!=null && !"".equals(q))
 		{
 			this.filter = q;
 		}
 		this.contentid = (String) request.getParameter("contentid");
 		this.start = (String) request.getParameter("start");
 		this.count = request.getParameter("count");
 		this.sorting = request.getParameterValues("sorting");
 		this.attributes =  this.prepareAttributesArray(request.getParameterValues("attributes"));
 		this.plinkattributes =  request.getParameterValues("plinkattributes");
 		this.debug = (String)request.getParameter("debug");
 		this.permissions = request.getParameterValues("permissions"); 
 		this.options = request.getParameterValues("options");
 		this.type=request.getParameter("type");
 		this.isDebug = (request.getParameter("debug")!=null && request.getParameter("debug").equals("true"));
 		this.metaresolvable = Boolean.parseBoolean(request.getParameter(RequestProcessor.META_RESOLVABLE_KEY));
 		this.highlightquery = request.getParameter(RequestProcessor.HIGHLIGHT_QUERY_KEY);
 		//if filter is not set and contentid is => use contentid instad
 		if (("".equals(filter) || filter == null)&& contentid!=null && !contentid.equals("")){
 			filter = "object.contentid ==" + contentid;
 		}
 		//SET PERMISSIONS-RULE
 		filter = this.createPermissionsRule(filter, permissions);
 		
 		setRepositoryType(this.type);
 		
 		//Set debug flag
 		if("true".equals(debug))
 			this.isDebug=true;
 	}
 	
 	/**
 	 * Create new Instance
 	 * @param request
 	 */
 	public CRRequestBuilder(PortletRequest request)
 	{
 		this(request,null);
 	}
 	
 		
 	/**
 	 * 
 	 *	Create New Instance
 	 * @param request
 	 */
 	public CRRequestBuilder(HttpServletRequest request)
 	{
 		this(request,null);
 		
 	}
 	
 	private void addAdvancedSearchParameters(){
 		if( filter == null || "" .equals(filter)){
 			if(query_and != null && !"".equals(query_and)){
 				String filter_and = "";
 				for(String query:query_and.split(" ")){
 					if(!"".equals(filter_and))filter_and+=" AND ";
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
 	 * 
 	 * @param request
 	 * @param defaultparameters
 	 */
 	public CRRequestBuilder(HttpServletRequest request,GenericConfiguration conf){
 
 		this.config = conf;
 		GenericConfiguration defaultparameters = null;
 		if(config != null){
 			defaultparameters = (GenericConfiguration)this.config.get(DEFAULPARAMETERS_KEY);
 		}
 		this.request = request;
 		this.filter = (String) request.getParameter("filter");
 		this.contentid = (String) request.getParameter("contentid");
 		this.count = request.getParameter("count");
 		this.start = (String) request.getParameter("start");
 		this.sorting = request.getParameterValues("sorting");
 		this.attributes =  this.prepareAttributesArray(request.getParameterValues("attributes"));
 		this.plinkattributes =  request.getParameterValues("plinkattributes");
 		this.debug = (String)request.getParameter("debug");
 		this.permissions = request.getParameterValues("permissions"); 
 		this.options = request.getParameterValues("options");
 		this.type=request.getParameter("type");
 		this.isDebug = (request.getParameter("debug")!=null && request.getParameter("debug").equals("true"));
 		this.metaresolvable = Boolean.parseBoolean(request.getParameter(RequestProcessor.META_RESOLVABLE_KEY));
 		this.highlightquery = request.getParameter(RequestProcessor.HIGHLIGHT_QUERY_KEY);
 		this.node_id = request.getParameterValues("node");
 		this.query_and = request.getParameter("q_and");
 		this.query_or = request.getParameter("q_or");
 		this.query_not = request.getParameter("q_not");
 		this.query_group = request.getParameter("q_group");
 		
 		//Parameters used in mnoGoSearch for easier migration (Users should use type=MNOGOSEARCHXML)
 		if ( this.filter == null ) { this.filter = request.getParameter("q"); }
 		if ( this.count == null ) { this.count = request.getParameter("ps"); }
 		if ( this.start == null && this.count != null) {
 			String numberOfPageStr = (String) request.getParameter("np");
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
 		if (("".equals(filter) || filter == null)&& contentid!=null && !contentid.equals("")){
 			filter = "object.contentid == '" + contentid+"'";
 		}
 		
 		addAdvancedSearchParameters();
 		
 		//SET PERMISSIONS-RULE
 		filter = this.createPermissionsRule(filter, permissions);
 		
 		setRepositoryType(this.type);
 		
 		//Set debug flag
 		if("true".equals(debug))
 			this.isDebug=true;
 		
 		
 		if(defaultparameters != null){
 			if(this.type == null){
 				this.type = (String) defaultparameters.get("type");
 				setRepositoryType(this.type);
 			}
 			if(this.node_id == null){
 				String default_node = (String) defaultparameters.get("node");
				this.node_id = default_node.split("^");
 			}
 			addAdvancedSearchParameters();
 		}
 	}
 	
 	
 	/**
 	 * Creates a CRRequest
 	 * @return
 	 */
 	public CRRequest getCRRequest()
 	{
 		CRRequest req = new CRRequest(filter,start,count,sorting,attributes,plinkattributes);
 		req.setContentid(this.contentid);
 		req.setRequest(this.request);
 		req.setResponse(this.response);
 		req.set(RequestProcessor.META_RESOLVABLE_KEY, this.metaresolvable);
 		if(this.highlightquery!=null)req.set(RequestProcessor.HIGHLIGHT_QUERY_KEY, this.highlightquery);
 		return req;
 	}
 	
 	/**
 	 * returns the Request Object
 	 * @return
 	 */
 	public Object getRequest(){
 		return this.request;
 	}
 	
 	//Wrapps filter rule with the given set of permissions
 	protected String createPermissionsRule(String filter, String[] permissions)
 	{
 		String ret=filter;
 		if((permissions != null) && (permissions.length>0))
 		{
 			if((filter!=null)&&(!filter.equals("")))
 			{
 				ret="("+filter+") AND object.permissions CONTAINSONEOF "+CRUtil.prepareParameterArrayForRule(permissions);
 			}
 			else
 			{
 				ret="object.permissions CONTAINSONEOF "+CRUtil.prepareParameterArrayForRule(permissions);
 			}
 		}
 		return ret;
 	}
 	
 	protected String[] prepareAttributesArray(String[] attributes)
 	{
 		ArrayList<String> ret = new ArrayList<String>();
 		if(attributes!=null)
 		{
 			for(String item: attributes)
 			{
 				if(item.contains(","))
 				{
 					String[] items = item.split(",");
 					for(String subatt:items)
 					{
 						ret.add(subatt);
 					}
 				}
 				else
 				{
 					ret.add(item);
 				}
 			}
 		}
 		return ret.toArray(new String[ret.size()]);
 	}
 	
 	
 	private Properties getConfiguredContentRepositories()
 	{
 		Object crs = this.config.get(REPOSITORIES_KEY);
 		if(crs!=null && crs instanceof GenericConfiguration)
 		{
 			GenericConfiguration cr_conf = (GenericConfiguration)crs;
 			Properties cr_confs = cr_conf.getProperties();
 			return cr_confs;
 			
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
 				}
 				catch(Exception ex)
 				{
 					logger.error("Could net create ContentRepository instance from class: "+cls, ex);
 				}
 			}
 		}
 		else
 		{
 			logger.error("Could net create ContentRepository instance. No Type is set to the RequestBuilder");
 		}
 		return cr;
 	}
 }
