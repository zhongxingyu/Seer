 package org.cipango.plugin;
 
 import java.io.File;
 import java.util.List;
 
 import org.cipango.sipapp.SipAppContext;
 import org.mortbay.jetty.plus.webapp.EnvConfiguration;
 import org.mortbay.jetty.webapp.Configuration;
 import org.mortbay.util.LazyList;
 
 public class CipangoPluginSipAppContext extends SipAppContext
 {
 
     private List<File> classpathFiles;
     private File jettyEnvXmlFile;
     private File webXmlFile;
     private boolean annotationsEnabled = true;
 
     private String[] configs = 
     	new String[]{
     		"org.mortbay.jetty.webapp.WebInfConfiguration",
     		"org.mortbay.jetty.plus.webapp.EnvConfiguration",
     		"org.mortbay.jetty.webapp.WebXmlConfiguration",
     		"org.cipango.plugin.CipangoMavenConfiguration",
     		"org.mortbay.jetty.webapp.JettyWebXmlConfiguration",
     		"org.mortbay.jetty.webapp.TagLibConfiguration"
     };
     
     public CipangoPluginSipAppContext()
     {
         super();
         setConfigurationClasses(configs);
     }
     
     public void addConfiguration(String configuration)
     {
     	 if (isRunning())
              throw new IllegalStateException("Running");
     	 configs = (String[]) LazyList.addToArray(configs, configuration, String.class);
     	 setConfigurationClasses(configs);
     }
 
     
     public void setClassPathFiles(List<File> classpathFiles)
     {
         this.classpathFiles = classpathFiles;
     }
 
     public List<File> getClassPathFiles()
     {
         return this.classpathFiles;
     }
     
     public void setWebXmlFile(File webXmlFile)
     {
         this.webXmlFile = webXmlFile;
     }
     
     public File getWebXmlFile()
     {
         return this.webXmlFile;
     }
     
     public void setJettyEnvXmlFile (File jettyEnvXmlFile)
     {
         this.jettyEnvXmlFile = jettyEnvXmlFile;
     }
     
     public File getJettyEnvXmlFile()
     {
         return this.jettyEnvXmlFile;
     }
     
     @SuppressWarnings("deprecation")
 	public void configure () throws Exception
     {
     	loadConfigurations();
     	Configuration[] configurations = getConfigurations();
     	for (int i = 0; i < configurations.length; i++)
     	{
     		if (configurations[i] instanceof CipangoMavenConfiguration)
    			((CipangoMavenConfiguration) configurations[i]).setClassPathConfiguration(classpathFiles);
 
             if (this.jettyEnvXmlFile != null && configurations[i] instanceof  EnvConfiguration)
             	((EnvConfiguration) configurations[i]).setJettyEnvXml(this.jettyEnvXmlFile.toURL());
 
 	   }
 	        
     }
 
 
     public void doStart () throws Exception
     {
         setShutdown(false);
         super.doStart();
     }
      
     public void doStop () throws Exception
     {
         setShutdown(true);
         //just wait a little while to ensure no requests are still being processed
         Thread.sleep(500L);
         super.doStop();
     }
 
 	public boolean isAnnotationsEnabled()
 	{
 		return annotationsEnabled;
 	}
 
 	public void setAnnotationsEnabled(boolean annotationsEnabled)
 	{
 		this.annotationsEnabled = annotationsEnabled;
 	}
 }
