 /**
  * 
  */
 package com.example.springforgeplugin.facet;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.jboss.forge.maven.MavenCoreFacet;
 import org.jboss.forge.project.facets.DependencyFacet;
 import org.jboss.forge.project.facets.JavaSourceFacet;
 import org.jboss.forge.shell.plugins.Alias;
 import org.jboss.forge.shell.plugins.RequiresFacet;
 
 import se.miman.forge.plugin.util.MimanBaseFacet;
 import se.miman.forge.plugin.util.VelocityUtil;
 import se.miman.forge.plugin.util.helpers.DomFileHelper;
 import se.miman.forge.plugin.util.helpers.DomFileHelperImpl;
 
 import com.example.springforgeplugin.completer.DatabaseType;
 import com.example.springforgeplugin.completer.HibernateAutoCreateType;
 
 /**
  * Adds the persistence artifact to the project.
  * - Add dependencies to the pom file
  * - Add persistence.xml to resource/META-INF folder
  * - Add orm.xml to resource/META-INF folder
  * - Add applicationContext.xml to resource/META-INF folder
  * 
  * @author Mikael
  *
  */
 @Alias("spring-persistence-facet")
 @RequiresFacet({ MavenCoreFacet.class, JavaSourceFacet.class,
       DependencyFacet.class })
 public class SpringPersistencePluginFacet extends MimanBaseFacet
 {
 
    DomFileHelper domFileHelper;
 
    private final VelocityEngine velocityEngine;
    private VelocityUtil velocityUtil;
 
    // Used to transfer info from the plugin, change this from static when another way has been found
    static public String connectionUrl;
    static public DatabaseType database;
    static public String username;
    static public String password;
    static public HibernateAutoCreateType autoCreate;
 
    
    public SpringPersistencePluginFacet()
    {
       super();
       domFileHelper = new DomFileHelperImpl();
 
       velocityUtil = new VelocityUtil();
 
       velocityEngine = new VelocityEngine();
       velocityEngine.setProperty(RuntimeConstants.RESOURCE_LOADER,
             "classpath");
       velocityEngine.setProperty("classpath.resource.loader.class",
             ClasspathResourceLoader.class.getName());
       velocityEngine.setProperty(
             RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS,
             "org.apache.velocity.runtime.log.JdkLogChute");
    }
 
    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.forge.project.Facet#install()
     */
    @Override
    public boolean install()
    {
       configureProject();
       return true;
    }
 
    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.forge.project.Facet#isInstalled()
     */
    @Override
    public boolean isInstalled()
    {
       final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
       Model pom = mvnFacet.getPOM();
 
       List<Dependency> deps = pom.getDependencies();
       boolean dependenciesOk = false;
       for (Dependency dependency : deps)
       {
          if (dependency.getGroupId().equals("org.hibernate") && dependency.getArtifactId().equals("hibernate-core"))
          {
             dependenciesOk = true;
          }
          // TODO more checks should be added here
       }
 
       return dependenciesOk;
    }
 
    // Helper functions ****************************************
    /**
     * Configures the project to be a JBoss Forge plugin project.
     * Adds the necessary dependencies to the pom.xml file.
     * Creates the Forge.xml file
     */
    private void configureProject()
    {
       final MavenCoreFacet mvnFacet = project.getFacet(MavenCoreFacet.class);
       Model pom = mvnFacet.getPOM();
 
       mergePomFileWithTemplate(pom);
       mvnFacet.setPOM(pom);
       
       createPersistenceXmlFile(pom.getProjectDirectory().getAbsolutePath(), 
     		  		pom.getArtifactId());
       createOrmXmlFile(pom.getProjectDirectory().getAbsolutePath());
       createApplicationContextXmlFile(pom.getProjectDirectory().getAbsolutePath(), 
     		  		pom.getArtifactId(), 
     		  		mvnFacet.getProject().getProjectRoot().getName());
    }
 
    /**
     * Creates the persistence.xml.
     */
    private void createPersistenceXmlFile(String prjAbsolutePath, String artifactId)
    {
 	  
       String sourceUri = "/template-files/META-INF/persistence.xml";
       String targetUri = "META-INF/persistence.xml";
 
       Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
       velocityPlaceholderMap.put("persistenceUnitName", artifactId+"-db");
       velocityPlaceholderMap.put("connectionUrl", connectionUrl);
       velocityPlaceholderMap.put("driverClass", getDbDriverClassName(database));
       velocityPlaceholderMap.put("username", username);
       velocityPlaceholderMap.put("password", password);
       velocityPlaceholderMap.put("autoCreate", getAutoCreateString(autoCreate));
 
       VelocityContext velocityContext = velocityUtil
             .createVelocityContext(velocityPlaceholderMap);
       velocityUtil.createResourceAbsolute(sourceUri, velocityContext, targetUri, project, velocityEngine);
    }
 
 	private Object getAutoCreateString(HibernateAutoCreateType autoCreateType) {
 		if (autoCreateType == null) {
 			return HibernateAutoCreateType.CREATE.name();
 		} else {
 			return autoCreateType.name();
 		}
 	}
 
 	private Object getDbDriverClassName(DatabaseType dbType) {
 		if (dbType == null) {
 			return "com.mysql.jdbc.Driver";	// Default is MySql
 		} else if (dbType == DatabaseType.MYSQL) {
 			return "com.mysql.jdbc.Driver";
 		}
 		return "com.mysql.jdbc.Driver";
 	}
 
 /**
     * Creates the orm.xml.
     */
    private void createOrmXmlFile(String prjAbsolutePath)
    {
       String sourceUri = "/template-files/META-INF/orm.xml";
       String targetUri = "META-INF/orm.xml";
 
       Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
 
       VelocityContext velocityContext = velocityUtil
             .createVelocityContext(velocityPlaceholderMap);
       velocityUtil.createResourceAbsolute(sourceUri, velocityContext, targetUri, project, velocityEngine);
    }
 
    /**
     * Creates the applicationContext.xml.
     * TODO: merge with existing applicationContext file, now we overwrite any existing file
     */
    private void createApplicationContextXmlFile(String prjAbsolutePath, String artifactId, String prjName)
    {
       String sourceUri = "/template-files/META-INF/applicationContext.xml";
       String targetUri = "META-INF/" + artifactId + "-applicationContext.xml";
 
      String onlyCharsPrjName = prjName.replaceAll("[^\\p{L}\\p{Nd}]", "");;
      
       Map<String, Object> velocityPlaceholderMap = new HashMap<String, Object>();
      velocityPlaceholderMap.put("moduleName", onlyCharsPrjName);
       velocityPlaceholderMap.put("persistenceUnitName", artifactId+"-db");
 
       VelocityContext velocityContext = velocityUtil
             .createVelocityContext(velocityPlaceholderMap);
       velocityUtil.createResourceAbsolute(sourceUri, velocityContext, targetUri, project, velocityEngine);
    }
 
    @Override
    protected String getTargetPomFilePath()
    {
       return "/template-files/pom.xml";
    }
 }
