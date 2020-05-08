 package com.rau.evoting.filters;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.rau.evoting.beans.TrusteeHomeBean;
 import com.sun.xml.internal.ws.util.StringUtils;
 
 public class SessionTimeOutFilter implements Filter {
 
 	private String timeOutPage = "Home.xhtml";
 	private ArrayList<String> trusteeHomeUrls;
 	
 	@Override
 	public void init(FilterConfig arg0) throws ServletException {
 		trusteeHomeUrls = new ArrayList<String>();
 		trusteeHomeUrls.add("/Evoting/TrusteeElection.xhtml");
 	}
 
 	@Override
 	public void destroy() {	
 	}
 
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
 		HttpServletRequest req = (HttpServletRequest)request;
 		HttpServletResponse res = (HttpServletResponse)response;
 
 		if(trusteeHomeUrls.contains(req.getRequestURI())) {
 			timeOutPage = "TrusteeHome.xhtml";
 		}
 		else {
 			timeOutPage = "Home.xhtml";
 		}
 		
 		if(!req.getRequestURI().equals("/Evoting/Home.xhtml") && !req.getRequestURI().equals("/Evoting/") && !req.getRequestURI().equals("/Evoting/TrusteeHome.xhtml")) {
 		
 			if(isSessionInvalid(req)) {
 				System.out.println("Session is invalid");
 				String timeOutUrl = req.getContextPath() + "/" + timeOutPage;
 				res.sendRedirect(timeOutUrl);
 				return ;
 			}
 		}
 
 		chain.doFilter(request, response);
 	}
 
 	public String getTimeOutPage() {
 		return timeOutPage;
 	}
 
 	public void setTimeOutPage(String timeOutPage) {
 		this.timeOutPage = timeOutPage;
 	}
 	
 	private boolean isSessionInvalid(HttpServletRequest httpServletRequest) {
 		boolean sessionInValid = (httpServletRequest.getRequestedSessionId() != null) && !httpServletRequest.isRequestedSessionIdValid();
		//System.out.println(httpServletRequest.isRequestedSessionIdValid());
 		return sessionInValid;
 	}
 	
 }
