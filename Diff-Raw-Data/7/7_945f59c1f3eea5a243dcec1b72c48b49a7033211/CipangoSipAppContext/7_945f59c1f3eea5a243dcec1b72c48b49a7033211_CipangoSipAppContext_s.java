 package org.cipango.plugin;
 
 import java.io.File;
 import java.util.List;
 
 import org.cipango.sipapp.SipAppContext;
 import org.eclipse.jetty.plus.webapp.EnvConfiguration;
 import org.eclipse.jetty.webapp.Configuration;
 import org.eclipse.jetty.util.LazyList;
 import org.eclipse.jetty.util.resource.Resource;
 
 public class CipangoSipAppContext extends SipAppContext
 {
 
     private List<File> classpathFiles;
     private File jettyEnvXmlFile;
     private File webXmlFile;
 
     private String[] configs = 
     	new String[]{
     		"org.mortbay.jetty.plugin.MavenWebInfConfiguration",
     		"org.eclipse.jetty.webapp.WebXmlConfiguration",
     		"org.eclipse.jetty.webapp.MetaInfConfiguration",
     		"org.eclipse.jetty.webapp.FragmentConfiguration",
     		"org.eclipse.jetty.plus.webapp.EnvConfiguration",
     		"org.eclipse.jetty.webapp.JettyWebXmlConfiguration",
     		"org.eclipse.jetty.webapp.TagLibConfiguration",
     		"org.cipango.sipapp.SipXmlConfiguration",
     		"org.cipango.plus.sipapp.Configuration"
     };
     
     private String jettyEnvXml;
     private List<Resource> overlays;
     private boolean unpackOverlays;
     
     public CipangoSipAppContext()
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
     
 	public void configure () throws Exception
     {
     	loadConfigurations();
     	Configuration[] configurations = getConfigurations();
     	for (int i = 0; i < configurations.length; i++)
     	{
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
 		for (int i = 0; i < configs.length; i++)
 			if (configs[i].equals("org.cipango.plugin.MavenAnnotationConfiguration"))
 				return true;
 		return false;
 	}
 
 	public void setAnnotationsEnabled(boolean annotationsEnabled)
 	{
 		if (annotationsEnabled)
 			addConfiguration("org.cipango.plugin.MavenAnnotationConfiguration");
 	}
 	
 	public boolean getUnpackOverlays()
     {
         return unpackOverlays;
     }
 
     public void setUnpackOverlays(boolean unpackOverlays)
     {
         this.unpackOverlays = unpackOverlays;
     }
 	
     public void setOverlays (List<Resource> overlays)
     {
         this.overlays = overlays;
     }
     
     public List<Resource> getOverlays ()
     {
         return this.overlays;
     }
     
     public void setJettyEnvXml (String jettyEnvXml)
     {
         this.jettyEnvXml = jettyEnvXml;
     }
     
     public String getJettyEnvXml()
     {
         return this.jettyEnvXml;
     }
 }
