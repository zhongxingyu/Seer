 package ca.usask.ulc.filters;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 
 import ca.usask.gmcte.currimap.action.PermissionsManager;
 import ca.usask.gmcte.currimap.model.CourseOffering;
 import ca.usask.gmcte.currimap.model.Organization;
 
 
 
 public final class InitSessionFilter implements Filter
 {
 	private FilterConfig filterConfig=null;
 	
 	private static Logger logger = Logger.getLogger( InitSessionFilter.class );
 	
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
 	{
 		// grab the session and HttpServletRequest from the ServletRequest
 		javax.servlet.http.HttpServletRequest local=(javax.servlet.http.HttpServletRequest)request;
 		HttpSession session=local.getSession(true);
 		String userid=(String)session.getAttribute("edu.yale.its.tp.cas.client.filter.user");
 		if(userid == null)
 		{
 			chain.doFilter(request,response);
 			return;
 		}
 		Boolean sessionInitialized = (Boolean)session.getAttribute("sessionInitialized");
 		if(sessionInitialized != null && sessionInitialized)
 		{
 			chain.doFilter(request,response);
 			return;
 		}
 		session.setAttribute("userIsSysadmin",new Boolean(true));
		
		chain.doFilter(request,response);

		
 		PermissionsManager manager = PermissionsManager.instance();
 		
 		//if user is sys admin
 		if(manager.isSysadmin(userid))
 		{
 			//add sys admin flag to session
 			session.setAttribute("userIsSysadmin",new Boolean(true));
 		}
 		else
 		{
 			
 			//get organizations user has access to
 			 HashMap<String,Organization> userHasAccessToOrganizations = manager.getOrganizationsForUser(userid);
 			 session.setAttribute("userHasAccessToOrganizations",userHasAccessToOrganizations);
 	
 			//get offerings user has access to
 			HashMap<String,CourseOffering> userHasAccessToOfferings = manager.getOfferingsForUser(userid, userHasAccessToOrganizations);
 			session.setAttribute("userHasAccessToOfferings",userHasAccessToOfferings);
 		}
 	
 		session.setAttribute("sessionInitialized",new Boolean(true));
 		chain.doFilter(request,response);
 	}
 	
 	
 	public void destroy()
 	{
 		this.filterConfig=null;
 	}
 	
 	public void init(FilterConfig filterConfig)
 	{
 		this.filterConfig=filterConfig;
 	}
 	 
 }
