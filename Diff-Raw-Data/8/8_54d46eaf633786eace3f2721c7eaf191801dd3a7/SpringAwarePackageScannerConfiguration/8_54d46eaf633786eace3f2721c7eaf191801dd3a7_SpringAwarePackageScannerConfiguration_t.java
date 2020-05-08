 package com.atlassian.plugin.spring;
 
 import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
 import org.springframework.web.context.ServletContextAware;
 
import javax.servlet.ServletContext;

 /**
  * Spring-aware extension of the package scanner configuration that instructs spring to inject the servlet context
  */
 public class SpringAwarePackageScannerConfiguration extends DefaultPackageScannerConfiguration implements ServletContextAware
 {
    @Override
    public void setServletContext(ServletContext servletContext)
    {
        super.setServletContext(servletContext);
    }
 }
