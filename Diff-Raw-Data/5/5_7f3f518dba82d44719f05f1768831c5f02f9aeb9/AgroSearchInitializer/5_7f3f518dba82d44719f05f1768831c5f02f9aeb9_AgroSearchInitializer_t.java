 package com.agroknow.search.web.bootstrap;
 
 import com.agroknow.search.config.CoreConfig;
 import com.agroknow.search.config.WebConfig;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumSet;
 import java.util.List;
 import javax.servlet.DispatcherType;
 
 import javax.servlet.FilterRegistration;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletRegistration;
 import javax.servlet.SessionCookieConfig;
 import javax.servlet.SessionTrackingMode;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.web.WebApplicationInitializer;
 import org.springframework.web.context.ContextCleanupListener;
 import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
 import org.springframework.web.filter.CharacterEncodingFilter;
 import org.springframework.web.servlet.DispatcherServlet;
 import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;
 
 /**
  *
  * @author aggelos
  */
 public class AgroSearchInitializer implements WebApplicationInitializer {
 
     private static final Logger LOG = LoggerFactory.getLogger(AgroSearchInitializer.class);
     private static final String DEFAULT_ENV = "dev";
 
     @Override
     public void onStartup(ServletContext container) {
         String environment = System.getProperty("com.agroknow.environment", DEFAULT_ENV).toLowerCase();
         String[] activeProfiles = getActiveProfiles(environment);
 
         LOG.info("Starting application context with the following active profiles: " + Arrays.toString(activeProfiles));
 
         //setup the parent context
         AnnotationConfigWebApplicationContext parentContext = new AnnotationConfigWebApplicationContext();
         parentContext.getEnvironment().setActiveProfiles(activeProfiles);
         parentContext.setServletContext(container);
         parentContext.register(CoreConfig.class);
         parentContext.refresh();
 
         //setup the web context
         AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
         webContext.setEnvironment(parentContext.getEnvironment());
         webContext.setParent(parentContext);
         webContext.getEnvironment().setActiveProfiles(activeProfiles);
         webContext.setServletContext(container);
         webContext.register(WebConfig.class);
         webContext.refresh();
 
         //add the servlet with the web context
         ServletRegistration.Dynamic dispatcher = container.addServlet("default", new DispatcherServlet(webContext));
         dispatcher.setLoadOnStartup(1);
         dispatcher.addMapping("/search-api");
 
         //add encoding filter
         CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
         encodingFilter.setEncoding("UTF-8");
         FilterRegistration.Dynamic encodingRegisteredFilter = container.addFilter("characterEncodingFilter", encodingFilter);
        encodingRegisteredFilter.addMappingForUrlPatterns(null, false, "/*");
 
         //add url rewrite filter - order of addition matters so keep this filter before securityFilter
         UrlRewriteFilter urlRewriteFilter = new UrlRewriteFilter();
         FilterRegistration.Dynamic urlRewriteRegisteredFilter = container.addFilter("urlRewriteFilter", urlRewriteFilter);
         urlRewriteRegisteredFilter.setInitParameter("confPath", "/WEB-INF/urlrewrite."+environment+".xml");
         urlRewriteRegisteredFilter.setInitParameter("statusEnabled", "false");
        urlRewriteRegisteredFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD), false, "/*");
 
         //add a shutdown listener that closes spring web and parent contexts
         container.addListener(ContextCleanupListener.class);
 
         //change the name of the session cookie
         SessionCookieConfig sessCookieConfig = container.getSessionCookieConfig();
         sessCookieConfig.setName("AGROSESSID");
         container.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
     }
 
     public String[] getActiveProfiles() {
         // get running environment
         String environment = System.getProperty("com.agroknow.environment", DEFAULT_ENV).toLowerCase();
 
         return this.getActiveProfiles(environment);
     }
 
     public String[] getActiveProfiles(String environment) {
         // get spring active profiles
         String activeProfilesProp = System.getProperty("spring.profiles.active");
         List<String> activeProfiles = (activeProfilesProp != null) ? new ArrayList<String>(Arrays.asList(activeProfilesProp.split(","))) : new ArrayList<String>(1);
 
         // add environment as the first active profile
         activeProfiles.add(0, environment);
 
         return activeProfiles.toArray(new String[activeProfiles.size()]);
     }
 }
