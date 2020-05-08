 package com.gentics.cr.lucene;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.resolving.Resolvable;
 import com.gentics.cr.CRException;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.CRServletConfig;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.lucene.search.LuceneRequestProcessor;
 import com.gentics.cr.template.FileTemplate;
 import com.gentics.cr.template.ITemplate;
 import com.gentics.cr.template.ITemplateManager;
 import com.gentics.cr.util.BeanWrapper;
 import com.gentics.cr.util.CRRequestBuilder;
 import com.gentics.cr.util.HttpSessionWrapper;
 
 
 /**
  * @author haymo
  * 
  * Used to render the Rest xml.
  * 
  */
 public class VelocitySearchServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8927256046849767956L;
 	private Logger log;
 	private CRServletConfig crConf;
 	private RequestProcessor rp;
 	private String response_encoding;
 	private String contenttype="text/html";
 	
 	private String querytemplate = "content:$query";
 	
 	private ITemplateManager vtl;
 	private ITemplate tpl;
 	
 	
 	public void init(ServletConfig config) throws ServletException {
 
 		super.init(config);
 		this.log = Logger.getLogger("com.gentics.cr");
 		this.crConf = new CRServletConfig(config);
 		this.response_encoding = crConf.getEncoding();
 		this.vtl = crConf.getTemplateManager();
 		String qt = (String)crConf.get("querytemplate");
 		if(qt!=null)this.querytemplate=qt;
 		String tmplate = (String)crConf.get("velocitytemplate");
 		try{
 			File f = new File(tmplate);
 			tpl = new FileTemplate(new FileInputStream(f));
 		}
 		catch(Exception ex)
 		{
 			log.error("FAILED TO LOAD VELOCITY TEMPLATE FROM "+tmplate);
 		}
 		try {
 			
 			this.rp = crConf.getNewRequestProcessorInstance(1);
 		} catch (CRException e) {
 			
 			log.error("FAILED TO INITIALIZE REQUEST PROCESSOR... "+e.getStringStackTrace());
 		}
 
 	}
 
 	/**
 	 * Wrapper Method for the doGet and doPost Methods
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	public void doService(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");
 		this.log.debug("Request:" + request.getQueryString());
 		
 		// starttime
 		long s = new Date().getTime();
 		// get the objects
 		
 		String query = request.getParameter("q");
 		if(query==null) query = request.getParameter("query");
 		if(query==null) query = request.getParameter("filter");
 		
 		String start_s = request.getParameter("start");
 		int start = 0;
 		if(start_s!=null)start = Integer.parseInt(start_s);
 		
 		String count_s = request.getParameter("count");
 		int count=10;
 		if(count_s!=null)count = Integer.parseInt(count_s);
 		String parsedquery = "";
 		try
 		{
 			this.vtl.put("query", query);
 			parsedquery = this.vtl.render("query", this.querytemplate);
 		}
 		catch(CRException ex)
 		{
 			ex.printStackTrace();
 			parsedquery="content:"+query;
 		}
 		
 		this.vtl.put("start", new Integer(start));
 		this.vtl.put("count", new Integer(count));
 		this.vtl.put("query", query);
 		
 		HashMap<String,Resolvable> objects = new HashMap<String,Resolvable>();
 		objects.put("request", new BeanWrapper(request));
 		objects.put("session", new HttpSessionWrapper(request.getSession()));
 		CRRequestBuilder myReqBuilder = new CRRequestBuilder(request);
 		//response.setContentType(rB.getContentRepository(this.crConf.getEncoding()).getContentType()+"; charset="+this.crConf.getEncoding());
 		Collection<CRResolvableBean> coll;
 		try {
 			response.setContentType(this.contenttype+"; charset="+this.response_encoding);
 			CRRequest req = myReqBuilder.getCRRequest();
 			req.setRequestFilter(parsedquery);
 			req.set(LuceneRequestProcessor.META_RESOLVABLE_KEY, true);
 			//DEPLOY OBJECTS TO REQUEST
 			for (Iterator<Map.Entry<String, Resolvable>> i = objects.entrySet().iterator() ; i.hasNext() ; ) {
 				Map.Entry<String,Resolvable> entry = (Entry<String,Resolvable>) i.next();
 				req.addObjectForFilterDeployment((String)entry.getKey(), entry.getValue());
 			}
 			// Query the Objects from RequestProcessor
 			coll = rp.getObjects(req);
 			
 			
 			Collection<CRResolvableBean> found = new ArrayList<CRResolvableBean>();
 			boolean first=true;
 			for(CRResolvableBean bean:coll)
 			{
 				if(first)
 				{
 					this.vtl.put("meta", bean);
 					first=false;
 				}
 				else
 					found.add(bean);
 			}
 			// add the objects to repository as serializeable beans
 			this.vtl.put("items", found);
 			this.vtl.put("error",false);
 			this.vtl.put("size", found.size());
 		} catch (CRException e1) {
 			
 			log.debug(e1.getMessage()+" : "+e1.getStringStackTrace());
 			this.vtl.put("notfound", true);
 			this.vtl.put("size",0);
 		}
 		catch(Exception ex)
 		{
 			CRException crex = new CRException(ex);
 			log.debug(ex.getMessage()+" : "+crex.getStringStackTrace());
 			this.vtl.put("error", true);
 		}
 		
 		try
 		{
 			String output = this.vtl.render(this.tpl.getKey(), this.tpl.getSource());
 			response.getWriter().write(output);
 		}
 		catch(Exception ex)
 		{
 			ex.printStackTrace();
 		}
 		response.getWriter().flush();
 		response.getWriter().close();
 		// endtimed
 		long e = new Date().getTime();
 		this.log.info("Executiontime for " + request.getQueryString() + ":" + (e - s));
 
 	}
 
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		doService(request, response);
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		doService(request, response);
 	}
 
 }
