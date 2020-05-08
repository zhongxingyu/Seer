 /*
  User: Ophir
  Date: 29/04/13
  Time: 10:49
  */
 package com.moshavit.framework;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 public class CORSFilter implements Filter {
 
 	@Override
 	public void doFilter(ServletRequest request,
 						 ServletResponse response,
 						 FilterChain chain) throws IOException, ServletException {
		((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin", "*");
 		chain.doFilter(request, response);
 	}
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	@Override
 	public void destroy() {
 	}
 }
