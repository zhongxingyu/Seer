 /**
  * $Id: $
  * $URL: $
  * *************************************************************************
  * Copyright (c) 2002-2009, DuraSpace.  All rights reserved
  * Licensed under the DuraSpace License.
  *
  * A copy of the DuraSpace License has been included in this
  * distribution and is available at: http://scm.dspace.org/svn/repo/licenses/LICENSE.txt
  */
 
 package org.dspace.webmvc.servlet;
 
 import org.dspace.webmvc.view.freemarker.SpringThemeAwareFreemarkerConfiguration;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;
 
 import com.opensymphony.module.sitemesh.freemarker.FreemarkerDecoratorServlet;
 
 import freemarker.template.Configuration;
 
 import javax.servlet.ServletException;
 
 /**
  * Spring-aware Freemarker decorator - should be wrapped as a controller in the Spring configuration
  */
 public class SpringFreemarkerDecoratorServlet extends FreemarkerDecoratorServlet
 {
     private transient Configuration fmConfiguration;
 
 	@Override
 	public void init() throws ServletException
     {
 		super.init();
 
        if  (getSpringConfiguration() != null) {
             getConfiguration().setTemplateLoader(getSpringConfiguration().getTemplateLoader());
         }
     }
 
     @Override
     protected Configuration createConfiguration() {
         if (getSpringConfiguration() != null) {
             return (Configuration)getSpringConfiguration().clone();
         }
         
         return new SpringThemeAwareFreemarkerConfiguration();
     }
 
     private Configuration getSpringConfiguration() {
         if (fmConfiguration != null) {
             return fmConfiguration;
         }
 
         WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
         String[] names = ctx.getBeanNamesForType(freemarker.template.Configuration.class);
         if (names != null && names.length > 0) {
             fmConfiguration = (Configuration)ctx.getBean(names[0]);
         }
 
         return fmConfiguration;
     }
 }
