 package org.opencab.config;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 
 import org.springframework.security.web.session.HttpSessionEventPublisher;
 import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
 
 public class WebAppInitializer extends
 		AbstractAnnotationConfigDispatcherServletInitializer {
 
 	@Override
 	public void onStartup(ServletContext servletContext)
 			throws ServletException {
 		servletContext.setInitParameter("spring.profiles.active", "prod");
 		super.onStartup(servletContext);
 	}
 
 	@Override
 	protected Class<?>[] getServletConfigClasses() {
		return new Class<?>[] {  SecurityConfig.class };
 	}
 
 	@Override
 	protected String[] getServletMappings() {
 		return new String[] { "/*" };
 	}
 
 	@Override
 	protected void registerDispatcherServlet(ServletContext servletContext) {
 		super.registerDispatcherServlet(servletContext);
 
 		servletContext.addListener(new HttpSessionEventPublisher());
 
 	}
 
 	@Override
 	protected Class<?>[] getRootConfigClasses() {
		return new Class<?>[] { AppConfig.class};
 
 	}
 
 }
