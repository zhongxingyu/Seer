 package org.dspace.springui.web.interceptor;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.dspace.services.api.configuration.ConfigurationService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.web.servlet.HandlerInterceptor;
 import org.springframework.web.servlet.ModelAndView;
 
 public class InstallationInterceptor implements HandlerInterceptor {
 	private static final String INSTALL_REQUEST = "/install";
 	@Autowired ConfigurationService configurationService;
 	
 	@Override
 	public boolean preHandle(HttpServletRequest request,
 			HttpServletResponse response, Object handler) throws Exception {
		if (!request.getPathInfo().contains(INSTALL_REQUEST) && !configurationService.isInstalled()) {
 			response.sendRedirect(request.getContextPath() + INSTALL_REQUEST);
 			return false;
 		}
		if (configurationService.isInstalled() && request.getPathInfo().contains(INSTALL_REQUEST)) {
 			response.sendRedirect(request.getContextPath());
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void postHandle(HttpServletRequest request,
 			HttpServletResponse response, Object handler,
 			ModelAndView modelAndView) throws Exception {
 		// Nothing
 	}
 
 	@Override
 	public void afterCompletion(HttpServletRequest request,
 			HttpServletResponse response, Object handler, Exception ex)
 			throws Exception {
 		// Nothing
 	}
 
 }
