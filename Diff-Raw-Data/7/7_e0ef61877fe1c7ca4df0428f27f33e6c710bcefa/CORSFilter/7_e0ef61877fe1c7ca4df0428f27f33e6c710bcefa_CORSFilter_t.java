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
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		httpResponse.addHeader("Access-Control-Allow-Origin", "*");
 		chain.doFilter(request, response);
		httpResponse.addHeader("Access-Control-Allow-Origin", "*");
		httpResponse.addHeader("Access-Control-Allow-Methods", "OPTIONS, GET, POST, PUT, PATCH, DELETE");
		httpResponse.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
 	}
 
 	@Override
 	public void init(FilterConfig filterConfig) throws ServletException {
 	}
 
 	@Override
 	public void destroy() {
 	}
 }
