 package org.esup.portlet.intranet.web.springmvc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletPreferences;
 import javax.portlet.RenderRequest;
 import javax.portlet.RenderResponse;
 import javax.portlet.ResourceRequest;
 import javax.portlet.ResourceResponse;
 
 import org.esup.portlet.intranet.nuxeo.NuxeoService;
 import org.esup.portlet.intranet.services.auth.Authenticator;
 import org.esup.portlet.intranet.web.Breadcrumb;
 import org.esup.portlet.intranet.web.NuxeoResource;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Document;
 import org.nuxeo.ecm.automation.client.jaxrs.model.Documents;
 import org.nuxeo.ecm.automation.client.jaxrs.model.FileBlob;
 import org.nuxeo.ecm.automation.client.jaxrs.util.IOUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.support.PagedListHolder;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.portlet.ModelAndView;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 import org.springframework.web.portlet.bind.annotation.RenderMapping;
 import org.springframework.web.portlet.bind.annotation.ResourceMapping;
 
 @Scope("session")
 @Controller
 @RequestMapping(value = "VIEW")
 public class WebController extends AbastractExceptionController{
 	@Value("${rowcount}")
 	int rowcount;
 	
 	@Value("${rowcount.mobile}")
 	int rowcount_mobile;
 	
     @Autowired
     private NuxeoService nuxeoService;
 	public void setNuxeoService(NuxeoService nuxeoService) {
 		this.nuxeoService = nuxeoService;
 	}
 	@Autowired
 	private Authenticator authenticator;
 	public void setAuthenticator(Authenticator authenticator) {
 		this.authenticator = authenticator;
 	}
 	
 	@Autowired
 	private NuxeoResource nuxeoResource;
     public void setNuxeoResource(NuxeoResource nuxeoResource) {
 		this.nuxeoResource = nuxeoResource;
 	}
     
 	@Autowired
     private ViewSelectorDefault viewSelector;
     
     @RenderMapping
     public ModelAndView init(RenderRequest request, RenderResponse response) throws Exception {
     	if(shouldSetPreferences(request)){
     		return new ModelAndView(viewSelector.getViewName(request, "init"), null);
     	}
         return getList(request,response);
     }
     
 	@RenderMapping(params="action=list")
     public ModelAndView getList(RenderRequest request, RenderResponse response) throws Exception {
     	ModelMap model = new ModelMap();
     	model.put("isuPortal", request.getPortalContext().getPortalInfo().contains("uPortal"));
     	nuxeoResource.init(request, authenticator);
     	String intranetPath = request.getParameter("intranetPath");
     	if(intranetPath != null)
     		intranetPath = new String(intranetPath.getBytes("ISO-8859-1"), Charset.forName("UTF-8"));
     	model.put("docs", nuxeoService.getList(nuxeoResource, intranetPath));
     	model.put("mode", "list");
     	setBreadcrumb(model,intranetPath);
         return new ModelAndView(viewSelector.getViewName(request, "view"), model);
     }
 	@RenderMapping(params="action=search-form")
	public String showSearchForm(RenderRequest request) {
 		ModelMap model = new ModelMap();
 		model.put("isuPortal", request.getPortalContext().getPortalInfo().contains("uPortal"));
 		model.put("mode", "search-form");
		return viewSelector.getViewName(request, "search");
 	}
 
     @ActionMapping(params="action=search")
 	public void searchDocs(ActionRequest request, ActionResponse response) throws Exception {
     	nuxeoResource.init(request, authenticator);
     	response.setRenderParameter("key", request.getParameter("key"));
     	response.setRenderParameter("action","search");
 	}
 	@RenderMapping(params="action=search")
 	public ModelAndView searchDocs(@RequestParam(required=false) String key, RenderRequest request, RenderResponse response) throws Exception {
     	ModelMap model =  new ModelMap(); 
     	model.put("isuPortal", request.getPortalContext().getPortalInfo().contains("uPortal"));
     	String viewName = viewSelector.getViewName(request, "view");
     	boolean isMobileMode = viewName.startsWith("mobile");
     	if(key != null)
     		key = new String(key.getBytes("ISO-8859-1"), Charset.forName("UTF-8"));
     	Documents docs = nuxeoService.search(nuxeoResource, key);
     	model.put("docs", docs);
     	if(isMobileMode){
     		viewName = viewSelector.getViewName(request, "search");
     		model.put("rowCnt", rowcount_mobile); 
     		model.put("leftCnt", docs.size()-rowcount_mobile);
     	}
     	model.put("mode", "search");
     	setBreadcrumb(model);
         return new ModelAndView(viewName, model);
     }	
     
     @RenderMapping(params="action=new")
     public ModelAndView getNew(RenderRequest request, RenderResponse response) throws Exception {
     	nuxeoResource.init(request, authenticator);
     	ModelMap model = new ModelMap();
     	model.put("isuPortal", request.getPortalContext().getPortalInfo().contains("uPortal"));
     	Documents docs = nuxeoService.getNews(nuxeoResource);
     	String viewName = viewSelector.getViewName(request, "view");
     	boolean isMobileMode = viewName.startsWith("mobile");
     	PagedListHolder<Document> productList = new PagedListHolder<Document>(docs);
     	if(!isMobileMode){
     		productList.setPageSize(rowcount);
     	}else{
         	productList.setPageSize(rowcount_mobile);
     	}
     	model.put("docs", productList.getPageList());
      	model.put("mode", "new");
         return new ModelAndView(viewName, model);
     }
     
     @ResourceMapping
     public void fileDown(ResourceRequest request, ResourceResponse response) throws Exception {
     	nuxeoResource.init(request, authenticator);
     	String uid = request.getParameter("uid");
     	FileBlob f = nuxeoService.getFile(nuxeoResource, uid);
     	File file = f.getFile();
     	String fileName = f.getFileName();
     	
     	OutputStream outStream = response.getPortletOutputStream();
 		if (!file.exists() || !file.canRead()) {
 			outStream.write("<i>Unable to find the specified file</i>".getBytes());
 		} else {
 			FileInputStream inStream = new FileInputStream(file);
 			String mimetype = f.getMimeType();
 			response.setContentType(mimetype);
 			response.setProperty("Content-disposition", "attachment; filename=\"" + fileName + "\"");
 			response.setContentLength((int) file.length());
 			
 			IOUtils.copy(inStream, response.getPortletOutputStream());
 		    response.flushBuffer();		
 		}
 		outStream.flush();
 		outStream.close();
     }
     
 	private void setBreadcrumb(ModelMap model){
     	setBreadcrumb(model, nuxeoResource.getIntranetPath());
     }
     private void setBreadcrumb(ModelMap model, String intranetPath){
     	if(intranetPath == null)
     		intranetPath = nuxeoResource.getIntranetPath();
     	Breadcrumb b = new Breadcrumb();
 		b.setBreadcrumb(nuxeoResource.getRootPath(), intranetPath);
 		model.put("breadcrumb", b.getPathList());
     }
     
     private boolean shouldSetPreferences(RenderRequest request){
     	PortletPreferences prefs = request.getPreferences();
     	if(!prefs.isReadOnly("nuxeoHost") && prefs.getValue("nuxeoHost","${nuxeoHost}").equals("${nuxeoHost}")){
     		return true;
     	}
     	if(!prefs.isReadOnly("intranetPath") && prefs.getValue("intranetPath","${intranetPath}").equals("${intranetPath}")){
     		return true;
     	}
     	return false;
     }
 }
