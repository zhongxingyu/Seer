 /*
  * Created on Jun 11, 2006
  */
 package edu.duke.cabig.catrip.test.system.steps;
 
 import gov.nci.nih.cagrid.tests.core.util.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 import com.atomicobject.haste.framework.Step;
 
 /**
  * Sets hibernate configuration properties in the caTissue CORE grid service. 
  * @author Patrick McConnell
  */
 public class CaTissueCoreConfigureStep
 	extends Step
 {
 	private File serviceDir;
 	private File origFile;
 	private File configFile;
 	
 	public CaTissueCoreConfigureStep(File serviceDir) 
 	{
 		super();
 		
 		this.serviceDir = serviceDir;
 	}
 	
 	public void runStep() 
 		throws IOException
 	{
 		origFile = File.createTempFile("CaTissueCoreConfigureStep", ".cfg.xml");
 		configFile = new File(serviceDir, "catissuecore-hibernate.cfg.xml");
 		FileUtils.copy(configFile, origFile);
 		
 		String connectionUrl = System.getProperty("catissuecore.connectionurl", 
			"jdbc:mysql://catrip1.duhs.duke.edu:3306/catissuecore"
 		); 
 		String user = System.getProperty("catissuecore.username", 
 			"catissue_core"
 		); 
 		String password = System.getProperty("catissuecore.password", 
 			"catissue_core"
 		); 
 		
 		FileUtils.replace(
 			configFile,
			"<property name=\"connection.url\">jdbc:mysql://catrip1:3306/catissuecore</property>",
 			"<property name=\"connection.url\">" + connectionUrl + "</property>"			
 		);
 		FileUtils.replace(
 			configFile,
 			"<property name=\"connection.username\">catissue_core</property>",
 			"<property name=\"connection.username\">" + user + "</property>"			
 		);
 		FileUtils.replace(
 			configFile,
 			"<property name=\"connection.password\">catissue_core</property>",
 			"<property name=\"connection.password\">" + password + "</property>"			
 		);
 	}
 
 	public File getConfigFile()
 	{
 		return configFile;
 	}
 
 	public File getOrigFile()
 	{
 		return origFile;
 	}
 
 	public File getServiceDir()
 	{
 		return serviceDir;
 	}
 }
