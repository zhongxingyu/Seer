 package server;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.annotation.WebFilter;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 /**
  * Servlet Filter implementation class AdminFilter
  */
 @WebFilter("/AdminFilter")
 public class AdminFilter implements Filter {
 
     /**
      * Default constructor. 
      */
     public AdminFilter() {
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see Filter#destroy()
 	 */
 	public void destroy() {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
 	 */
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpSession session = ((HttpServletRequest)request).getSession(false);
 		
     	if (session != null) {
    		int loginId = Integer.parseInt((String) session.getAttribute("userId"));
     		if ( (loginId == 1) && (session.getAttribute("events") != null) 
     				&& (session.getAttribute("user") != null) ){
     			System.out.println ("FILTER user!= null");
     			System.out.println ("FILTER userId = "+ loginId);
         		System.out.println ("FILTER user "+ session.getAttribute("user"));
         		System.out.println ("FILTER events "+ session.getAttribute("events"));
 
         		// pass the request along the filter chain
         		chain.doFilter(request, response);
     		}
     		
     		else {
 	    		request.getRequestDispatcher("/Login.jsp").forward(request, response);
     		}
     	} else {
     		request.getRequestDispatcher("/Login.jsp").forward(request, response);
     	}
 	}
 
 	/**
 	 * @see Filter#init(FilterConfig)
 	 */
 	public void init(FilterConfig fConfig) throws ServletException {
 		// TODO Auto-generated method stub
 	}
 
 }
