 package com.pingidentity.efazendin.pingpong.sp;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.pingidentity.efazendin.pingpong.sp.model.IdentityProviderPager;
 
 public class PongStatusServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final Logger _logger = Logger.getLogger(PongStatusServlet.class);
     
 	private String GET_IDPS = "get idps";
 	private String CHECK_BACK = "check back";
 	private String NEXT_PAGE = "next page";
 	private String NO_USER_IDPS = "no user idps";
 
     public PongStatusServlet() {
         super();
     }
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		
 		IdentityProviderPager idpPager = (IdentityProviderPager)req.getSession().getAttribute(IdentityProviderPager.IDP_PAGER);
 		
 		String status = null;
 		
 		if (idpPager != null) {
 			
			boolean haveAllPonged = idpPager.haveAllPagedPonged();
			_logger.debug("idpPager.haveAllPagedPonged(): " + haveAllPonged);
			if (haveAllPonged || idpPager.hasPageExpired()) {
 				if (idpPager.haveAnyAuthnedUser())
 					status = GET_IDPS;
 				else {
 					if (idpPager.hasNextPage())
 						status = NEXT_PAGE;
 					else
 						status = NO_USER_IDPS;	
 				}
 			} else
 				status = CHECK_BACK;
 		}
 		
 		_logger.debug("Pong status: " + status);
 		
 		resp.getOutputStream().print(status);
 		
 		resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
 		resp.setHeader("Pragma", "no-cache");
 	}
 }
