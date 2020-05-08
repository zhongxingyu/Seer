 /**
  * XWeb project
  * Created by Hamed Abdollahpour
  * https://github.com/abdollahpour/xweb
  */
 
 package ir.xweb.module;
 
 import java.io.IOException;
 import java.lang.IllegalArgumentException;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 public class Module {
 
     private Manager manager;
 
 	private ModuleInfo info;
 
     private ModuleParam properties;
 
     private RoleManager roleManager;
 
     private Map<String, ModuleInfoValidator> validators = new HashMap<String, ModuleInfoValidator>();
 
     private List<String> requireParams;
 
     public Module(Manager manager, ModuleInfo info, ModuleParam properties) throws ModuleException {
         if(manager == null) {
             throw new IllegalArgumentException("null manager");
         }
 
         this.manager = manager;
         this.info = info;
         this.properties = properties;
 
         roleManager = new RoleManager(info.getRoles());
 
         validators =  new HashMap<String, ModuleInfoValidator>(info.getValidators().size());
         requireParams = new ArrayList<String>(validators.size());
 
         for(ModuleInfoValidator validator:info.getValidators()) {
             validators.put(validator.getParam(), validator);
             if(validator.isRequire()) {
                 requireParams.add(validator.getParam());
             }
         }
     }
 
     public void init(ServletContext context) {
     }
 
     public void destroy() {
     }
 
 	public ModuleInfo getInfo() {
 		return info;
 	}
 	
 	public void process(
 			ServletContext context, 
 			HttpServletRequest request, 
 			HttpServletResponse response,
             String role) throws IOException {
 		
 		String contentType = request.getHeader("Content-Type");
 		
 		HashMap<String, String> params = new HashMap<String, String>();
 		HashMap<String, FileItem> files = new HashMap<String, FileItem>();
 		
 		if(contentType != null && contentType.indexOf("multipart/form-data") > -1) {
 			try {
 		        List<?> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
 		        for (Object o : items) {
 		        	FileItem item = (FileItem)o;
 		            if (item.isFormField()) {
 		                String fieldname = item.getFieldName();
 		                String fieldvalue = item.getString();
 
                         // DAMN! There's problem for Apache commons-fileupload && UTF-8. We should to fix it manually
                         // http://stackoverflow.com/questions/546365/utf-8-text-is-garbled-when-form-is-posted-as-multipart-form-data
 
                         fieldname = new String (fieldname.getBytes ("iso-8859-1"), "UTF-8");
                         fieldvalue = new String (fieldvalue.getBytes ("iso-8859-1"), "UTF-8");
 		                
 		                params.put(fieldname, fieldvalue);
 		            } else {
                        final String filename = item.getName();
                        /** FormData for HTML5 will send files but with empty files name! **/
                        if(filename != null && filename.length() > 0) {
                            String fieldname = item.getFieldName();
                            files.put(fieldname, item);
                        }
 		            }
 		        }
 		    } catch (FileUploadException e) {
 		    }
 		}
 		
 		Enumeration<?> names = request.getParameterNames();
 		while(names.hasMoreElements()) {
 			String name = names.nextElement().toString();
 			String value = request.getParameter(name);
 			params.put(name, value);
 		}
 
         ModuleParam moduleParam = new ModuleParam(params);
 
         // validate params
         List<String> requires = new ArrayList<String>(requireParams);
 
         for(String name:params.keySet()) {
             ModuleInfoValidator validator = validators.get(name);
             if(validator != null) {
                 moduleParam.validate(name, validator.getRegex(), validator.isRequire());
                 requires.remove(name);
             }
         }
 
         if(requires.size() > 0) {
             throw new IllegalArgumentException("Parameter needed: " + requireParams.get(0));
         }
 
         // Check for roles
         try {
             if(!roleManager.hasPermission(params, role)) {
                 if(role == null) {
                     response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You do not have permission to access with this role: " + role);
                 } else {
                     response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You do not have permission to access with this role: " + role);
                 }
                 return;
             }
         } catch (Exception ex) {
             throw new IOException(ex);
         }
 		
 		process(context, request, response, moduleParam, files);
 
         response.flushBuffer();
 	}
 	
 	public void process(
             ServletContext context,
 			HttpServletRequest request, 
 			HttpServletResponse response, 
 			ModuleParam param,
 			HashMap<String, FileItem> files) throws IOException {
 
     }
 
     public void doFilter(
             ServletContext context,
             HttpServletRequest request,
             HttpServletResponse response,
             FilterChain filterChain) throws IOException, ServletException {
         filterChain.doFilter(request, response);
     }
 
     public void initFilter(FilterConfig filterConfig) throws ServletException {
     }
 
     public void destroyFilter() {
     }
 
 	/*public boolean hasPermission(ModuleParam params, String role) {
 		if(info != null && info.getRoles() != null) {
             for(Role r:info.getRoles()) {
                 if(r.actions == null) {
                     if(r.name.equals(role)) {
                         return true;
                     }
                 } else {
                     if(r.name.equals(role)) {
                         return action == null || r.actions.contains(action);
                     }
                 }
             }
 		}
 		return false;
 	}*/
 	
 	public boolean hasPermission(String role) {
 		return true;
 	}
 
     public ServletContext getContext() {
         return manager.getContext();
     }
 
     public Manager getManager() {
         return manager;
     }
 
     public ModuleParam getProperties() {
         return this.properties;
     }
     
     public boolean redirectAuthFail() {
     	return false;
     }
 
 }
