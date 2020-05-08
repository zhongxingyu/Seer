 package gov.nih.nci.camod.util;
 
 import gov.nih.nci.camod.Constants;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.struts.action.Action;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.RequestProcessor;
 
 /**
  * Checks for User Login on any struts request
  * Overrides the processActionPerform() method
  * 
  * @author schroedlni
  *
  */
 public class CustomRequestProcessor extends RequestProcessor {
 	
 	protected ActionForward processActionPerform( HttpServletRequest request, 
 												  HttpServletResponse response, 
 												  Action action, 
 												  ActionForm form, 
 												  ActionMapping mapping ) 
 	throws IOException, ServletException 
 	{	
 		System.out.println( "<CustomRequestProcessor processActionPerform> Entering..." );
 		System.out.println( "<CustomRequestProcessor processActionPerform> mapping.getParameter=" + mapping.getParameter() );
 		
 		// Check custom ActionMapping Parameter
		if ( mapping.getParameter().toString().equals( "method") || mapping.getParameter().toString().indexOf("/protected") != -1 )
		{	
			
 			HttpSession session = request.getSession();
 			String user = (String) session.getAttribute( Constants.CURRENTUSER );
 					
 			if ( user == null ) {				
 				request.getSession().setAttribute( Constants.LOGINFAILED, "true" );				
 				return mapping.findForward( "login" );
 			}				
 		} 

 		return super.processActionPerform( request, response, action, form, mapping );
 	}
 }
