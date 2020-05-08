 package com.gmail.at.zhuikov.aleksandr.driveddoc.filter;
 
 import java.io.IOException;
 
 import javax.inject.Singleton;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 @Singleton
 public class AuthenticationFilter implements Filter {
 
 	@Override
 	public void destroy() {
 	}
 
 	@Override
 	public void doFilter(ServletRequest req, ServletResponse resp,
 			FilterChain chain) throws IOException, ServletException {
 		
 		if (((HttpServletRequest) req).getSession().getAttribute("me") == null) {
			((HttpServletResponse) resp).sendError(401);
 			return;
 		}
 		
 		chain.doFilter(req, resp);
 	}
 
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 	}
 }
