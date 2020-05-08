 package com.papteco.web.filters;
 
import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import com.papteco.web.utils.WebUtils;
 
 public class SecurityFilter extends HandlerInterceptorAdapter  {
 
     @Override
     public boolean preHandle(HttpServletRequest request,
             HttpServletResponse response, Object handler) throws Exception {
         System.out.println("==>>Begin to Filter session====");
         HttpSession session = request.getSession();
         List functionList = (List) session.getAttribute("allowFunctions");
        functionList = (functionList == null) ? new ArrayList():functionList;
        
         System.out.println("===??Current functionList=="+functionList);
         
         String[] curPath=request.getRequestURL().toString().split("/");
         String actionName = curPath[curPath.length-1];
         
         System.out.println("===>> action:"+actionName);
         
         //special handle
         if(actionName.equals("submitUploadFile")){
         	
         	// find out which type it processing
         	String doc_type = request.getParameter("upload_doctype");
         	System.out.println("===>> uploading:"+doc_type);
         	if(!WebUtils.isDocTypeInEditFunctionList(doc_type,functionList)){
         		request.getRequestDispatcher("/forbidUpload").forward(request, response);
             	return false;
         	}
         }else if(actionName.equals("viewDocs")){
         	
         	// find out which type it processing
         	String filename = request.getParameter("filename");
         	System.out.println("===>> viewing:"+filename);
         	
         	if(!WebUtils.isFilenameInViewFunctionList(filename,functionList)){
         		request.getRequestDispatcher("/forbid").forward(request, response);
             	return false;
         	}
        }else if(!WebUtils.isActionInFunctionList(actionName, functionList)){
         	request.getRequestDispatcher("/forbid").forward(request, response);
         	return false;
         }
            
         return super.preHandle(request, response, handler);
     }
 
 	@Override
 	public void afterCompletion(HttpServletRequest request,
 			HttpServletResponse response, Object handler, Exception ex)
 			throws Exception {
 		// TODO Auto-generated method stub
 		System.out.println("==>>afterCompletion====");
 		super.afterCompletion(request, response, handler, ex);
 	}
 
 	@Override
 	public void postHandle(HttpServletRequest request,
 			HttpServletResponse response, Object handler,
 			ModelAndView modelAndView) throws Exception {
 		System.out.println("==>>postHandle====");
 		// TODO Auto-generated method stub
 		super.postHandle(request, response, handler, modelAndView);
 	}
    
    
 
 }
