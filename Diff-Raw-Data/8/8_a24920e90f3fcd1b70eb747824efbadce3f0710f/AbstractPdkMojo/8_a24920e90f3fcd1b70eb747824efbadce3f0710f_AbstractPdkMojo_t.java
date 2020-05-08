 package com.atlassian.maven.plugins.amps.pdk;
 
 import com.atlassian.maven.plugins.amps.AbstractProductHandlerAwareMojo;
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 
 /**
  *
  */
 public abstract class AbstractPdkMojo extends AbstractProductHandlerAwareMojo
 {
     /**
      * @parameter expression="${atlassian.plugin.key}"
      */
     protected String pluginKey;
     /**
      * @parameter expression="${project.groupId}"
      */
     protected String groupId;
     /**
      * @parameter expression="${project.artifactId}"
      */
     protected String artifactId;
 
     /**
      * HTTP port for the servlet containers
      * @parameter expression="${http.port}"
      */
     private int httpPort;
 
     /**
      * Application context path
      * @parameter expression="${context.path}"
      */
     protected String contextPath;
 
     /**
      * Application server
      * @parameter expression="${server}" default-value="localhost"
      */
     protected String server;
 
     protected void ensurePluginKeyExists()
     {
         if (pluginKey == null)
         {
             pluginKey = groupId + "." + artifactId;
         }
     }
 
    protected int getHttpPort(final ProductHandler handler)
     {
         return httpPort == 0 ? handler.getDefaultHttpPort() : httpPort;
     }
 
    protected String getContextPath(final ProductHandler handler)
     {
        return contextPath == null ? "/" + handler.getId() : contextPath;
     }
 }
