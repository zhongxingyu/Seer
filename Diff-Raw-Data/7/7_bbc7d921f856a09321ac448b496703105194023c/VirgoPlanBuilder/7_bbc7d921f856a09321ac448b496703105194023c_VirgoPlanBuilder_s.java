 package com.hesham.maven.virgo;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.zip.ZipException;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import org.apache.maven.plugin.logging.Log;
 
 import com.hesham.maven.virgo.jaxb.plan.ArtifactType;
 import com.hesham.maven.virgo.jaxb.plan.ObjectFactory;
 import com.hesham.maven.virgo.jaxb.plan.Plan;
 
 
 public class VirgoPlanBuilder {
 	
 	private VirgoPlanConfig planConfig;
 	
 	String SEP = File.separator;
 	
 	Log logger ;
 	
 	/**
 	 * Creates the default configuration
 	 */
 	public VirgoPlanBuilder(VirgoPlanConfig planConfig, Log logger){
 		
 		if (planConfig.getPlanFileName() == null)
 			throw new IllegalArgumentException("No plan file name was specified.");
 		
 		if (planConfig.getPlanEntity() == null)
 			throw new IllegalArgumentException("No plan entity was set.");
 		
 		this.planConfig = planConfig;
 		this.logger = logger;
 	}
 	
 	public boolean buildPlan(){
 		List<ArtifactType> planArtifacts = planConfig.getPlanEntity().getArtifact();
 		if (planArtifacts == null || planArtifacts.size() == 0)
 		{
 			// Try to get them from the plan configuration it self
 			if (planConfig.getModulesNames() != null){
 				List<ArtifactType> artifacts = prepareArtifactTypes(planConfig.getModulesNames());
 				planArtifacts.addAll(artifacts);
 			}
 		}
 		return writePlanFile();
 	}
 	
 	
 	private List<ArtifactType> prepareArtifactTypes(List<String> modules){
 		
 		if (modules == null)
 			return null;
 		
 		List<ArtifactType> artifacts = new ArrayList<ArtifactType>(modules.size());
 		
 		
 		for (String moduleName : modules)
 		{
 			ArtifactType artifact = new ArtifactType();
 			
 			String moduleDirPath = planConfig.getBasePath() + moduleName + File.separator ;
 			String moduleTargetDirPath = moduleDirPath + "target" + File.separator ;  
 			try {
 				
 				// Get the module deployable (jar/war) name
 				String deployableName = getModuleDeployableName(moduleDirPath);
 				
 				JarFile deployableJar ;
 				try{
 					// Look at getModuleDeployableName's TODO
 					deployableJar = new JarFile(moduleTargetDirPath + deployableName + ".jar");
 				}catch (ZipException e) {
 					deployableJar = new JarFile(moduleTargetDirPath + deployableName + ".war");
 				}
 				
 				// Get its Manifest 
 				Manifest deployableJarManifest = deployableJar.getManifest();
 				String moduleSymbolicName = deployableJarManifest.getMainAttributes().getValue("Bundle-SymbolicName").trim();
 				String moduleVersion = deployableJarManifest.getMainAttributes().getValue("Bundle-Version").trim();
 				String moduleType = "bundle";
 				
 				// Set the Artifact with values gotten from the original Manifest 
 				artifact.setName(moduleSymbolicName);
 				artifact.setVersion(moduleVersion);
 				artifact.setType(moduleType); // TODO: Inspect if there are any other artifact types
 				
 				artifacts.add(artifact);
 				
 			} catch (FileNotFoundException e) {
 				getLogger().error("The module " + moduleName + "'s deployable couldn't be found, however the building will be continued.");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return artifacts;
 	};
 	
 	
 	/**
 	 * This gets the deployable name of a module/project
 	 * @param modulePath The absolute path of the module/project
 	 * @return
 	 * @throws IOException 
 	 * @throws FileNotFoundException 
 	 */
 	private String getModuleDeployableName(String modulePath) throws FileNotFoundException, IOException{
 		
 		String moduleTargetDirPath = modulePath + File.separator + "target" + File.separator ;  
 
 		// Get the deployable name
 		Properties jarInfo = new Properties();
 		jarInfo.load(new FileInputStream(moduleTargetDirPath + "maven-archiver" + SEP + "pom.properties"));
 		
 		String deployableName = jarInfo.getProperty("artifactId") + "-" + jarInfo.getProperty("version");
 
 		// Get the deployable extension
 		// TODO For now, an assumption of it either being .jar or .war will be made by the caller of this method, later on enhance this by traversing the module's POM file for the packaging type directly
 		
 		
 		return deployableName;
 	}
 	
 	private boolean writePlanFile(){
 		// Write the plan JAXB to a file
 		try {
 			JAXBContext context = JAXBContext.newInstance("com.hesham.maven.virgo.jaxb.plan");
 			Marshaller marshaller = context.createMarshaller();
 			marshaller.setProperty("jaxb.formatted.output",Boolean.TRUE);
 			marshaller.setProperty("jaxb.schemaLocation","http://www.eclipse.org/virgo/schema/plan http://www.eclipse.org/virgo/schema/plan/eclipse-virgo-plan.xsd");
 			JAXBElement<Plan> planElemWrapper = new ObjectFactory().createPlan(planConfig.getPlanEntity());
 			
 			String filePath = planConfig.getBasePath() + planConfig.getOutputDirectoryName() + planConfig.getPlanFileName();
 			
 			FileWriter fileWriter = new FileWriter(new File(filePath));
 			marshaller.marshal(planElemWrapper, fileWriter);
 			fileWriter.close();
 			return true;
 			
 		} catch (JAXBException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public Log getLogger() {
 		return logger;
 	}
 
 	public void setLogger(Log logger) {
 		this.logger = logger;
 	}
 }
