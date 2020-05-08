 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.maven.plugins.amps.util.ArtifactRetriever;
 
 import java.io.File;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Properties;
 
 public class Product
 {
     /**
      * Container to run in
      */
     protected String containerId;
     /**
      * HTTP port for the servlet containers
      */
     private int httpPort = 0;
 
     /**
      * Application context path
      */
     protected String contextPath;
 
     /**
      * Application server
      */
     protected String server;
 
     /**
      * Webapp version
      */
     protected String version;
 
     /**
      * JVM arguments to pass to cargo
      */
     protected String jvmArgs = null;
 
     /**
      * A log4j properties file
      */
     protected File log4jProperties;
 
     /**
      * The test resources version
      */
     protected String productDataVersion;
 
     /**
      * The path to a custom test resources zip
      */
     protected String productDataPath = "";
 
     /**
      */
     private List<ProductArtifact> pluginArtifacts = new ArrayList<ProductArtifact>();
 
     /**
      */
     private List<ProductArtifact> libArtifacts = new ArrayList<ProductArtifact>();
 
     /**
      */
     private List<ProductArtifact> bundledArtifacts = new ArrayList<ProductArtifact>();
 
     /**
      * SAL version
      */
     private String salVersion;
 
     /**
      * Atlassian Plugin Development Kit (PDK) version
      */
     private String pdkVersion;
 
     /**
      * Atlassian REST module version
      */
     private String restVersion;
 
     /**
      * Version of the Felix OSGi web console
      */
     private String webConsoleVersion;
 
     /**
      * Product id
     *
      */
     private String id;
 
     private ArtifactRetriever artifactRetriever;
     /**
      * Flag to indicate whether or not to install the plugin
      */
     private boolean installPlugin;
 
     /**
      * The system properties to set for the product
      */
     private Properties systemProperties = new Properties();
    
     /**
      * File the container should log to.
      */
     private String output;
 
     public String getContainerId()
     {
         return containerId;
     }
 
     public void setContainerId(String containerId)
     {
         this.containerId = containerId;
     }
 
     public String getServer()
     {
         return server;
     }
 
     public void setServer(String server)
     {
         this.server = server;
     }
 
     public int getHttpPort()
     {
         return httpPort;
     }
 
     public void setHttpPort(int httpPort)
     {
         this.httpPort = httpPort;
     }
 
     public String getContextPath()
     {
         return contextPath;
     }
 
     public void setContextPath(String contextPath)
     {
         this.contextPath = contextPath;
     }
 
     public String getJvmArgs()
     {
         return jvmArgs;
     }
 
     public void setJvmArgs(String jvmArgs)
     {
         this.jvmArgs = jvmArgs;
     }
 
     public ArtifactRetriever getArtifactRetriever()
     {
         return artifactRetriever;
     }
 
     public void setArtifactRetriever(ArtifactRetriever artifactRetriever)
     {
         this.artifactRetriever = artifactRetriever;
     }
 
     public String getVersion()
     {
         return version;
     }
 
     public void setVersion(String version)
     {
         this.version = version;
     }
 
     public String getProductDataVersion()
     {
         return productDataVersion;
     }
 
     public void setProductDataVersion(String productDataVersion)
     {
         this.productDataVersion = productDataVersion;
     }
 
     public String getProductDataPath()
     {
         return productDataPath;
     }
 
     public void setProductDataPath(String productDataPath)
     {
         this.productDataPath = productDataPath;
     }
 
     public List<ProductArtifact> getPluginArtifacts()
     {
         return pluginArtifacts;
     }
 
     public void setPluginArtifacts(List<ProductArtifact> pluginArtifacts)
     {
         this.pluginArtifacts = pluginArtifacts;
     }
 
     public List<ProductArtifact> getLibArtifacts()
     {
         return libArtifacts;
     }
 
     public void setLibArtifacts(List<ProductArtifact> libArtifacts)
     {
         this.libArtifacts = libArtifacts;
     }
 
     public List<ProductArtifact> getBundledArtifacts()
     {
         return bundledArtifacts;
     }
 
     public void setBundledArtifacts(List<ProductArtifact> bundledArtifacts)
     {
         this.bundledArtifacts = bundledArtifacts;
     }
 
     public File getLog4jProperties()
     {
         return log4jProperties;
     }
 
     public void setLog4jProperties(File log4jProperties)
     {
         this.log4jProperties = log4jProperties;
     }
 
     public String getRestVersion()
     {
         return restVersion;
     }
 
     public void setRestVersion(String restVersion)
     {
         this.restVersion = restVersion;
     }
 
     public String getSalVersion()
     {
         return salVersion;
     }
 
     public void setSalVersion(String salVersion)
     {
         this.salVersion = salVersion;
     }
 
     public String getPdkVersion()
     {
         return pdkVersion;
     }
 
     public void setPdkVersion(String pdkVersion)
     {
         this.pdkVersion = pdkVersion;
     }
 
     public String getId()
     {
         return id;
     }
 
     public void setId(String id)
     {
         this.id = id;
     }
 
     public boolean isInstallPlugin()
     {
         return installPlugin;
     }
 
     public void setInstallPlugin(final boolean installPlugin)
     {
         this.installPlugin = installPlugin;
     }
 
     public String getWebConsoleVersion()
     {
         return webConsoleVersion;
     }
 
     public void setWebConsoleVersion(String webConsoleVersion)
     {
         this.webConsoleVersion = webConsoleVersion;
     }
 
     public void setSystemProperties(Properties systemProperties)
     {
         this.systemProperties = systemProperties;
     }
 
     public Properties getSystemProperties()
     {
         return systemProperties;
     }
 
 	public String getOutput()
 	{
 		return output;
 	}
 	
 	public void setOutput(String output)
 	{
 		this.output = output;
 	}
 }
