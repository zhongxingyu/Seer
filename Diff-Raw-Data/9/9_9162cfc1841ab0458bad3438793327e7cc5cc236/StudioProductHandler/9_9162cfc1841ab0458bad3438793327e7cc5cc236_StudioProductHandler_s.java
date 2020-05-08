 package com.atlassian.maven.plugins.amps.product.studio;
 
 import static com.atlassian.maven.plugins.amps.product.ProductHandlerFactory.STUDIO;
 import static com.atlassian.maven.plugins.amps.product.ProductHandlerFactory.STUDIO_BAMBOO;
 import static com.atlassian.maven.plugins.amps.product.ProductHandlerFactory.STUDIO_CONFLUENCE;
 import static com.atlassian.maven.plugins.amps.product.ProductHandlerFactory.STUDIO_CROWD;
 import static com.atlassian.maven.plugins.amps.product.ProductHandlerFactory.STUDIO_FECRU;
 import static com.atlassian.maven.plugins.amps.product.ProductHandlerFactory.STUDIO_JIRA;
 import static com.atlassian.maven.plugins.amps.util.ZipUtils.unzip;
 import static org.apache.commons.io.FileUtils.copyDirectory;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 import com.atlassian.maven.plugins.amps.MavenContext;
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import com.atlassian.maven.plugins.amps.Product;
 import com.atlassian.maven.plugins.amps.ProductArtifact;
 import com.atlassian.maven.plugins.amps.ProductExecution;
 import com.atlassian.maven.plugins.amps.product.AmpsProductHandler;
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
 import com.atlassian.maven.plugins.amps.util.ConfigFileUtils;
 import com.atlassian.maven.plugins.amps.util.ConfigFileUtils.Replacement;
 import com.atlassian.maven.plugins.amps.util.ProjectUtils;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.inject.internal.util.ImmutableMap;
 
 /**
  * This product handler is a 'ghost'. It doesn't start a product, but it prepares the environment
  * for all studio-based products.
  */
 final public class StudioProductHandler extends AmpsProductHandler
 {
     private static final String STUDIO_PROPERTIES = "home/studio.properties";
     private static final String STUDIO_INITIAL_DATA_PROPERTIES = "home/studio-initial-data.properties";
     private static final String DEVMODE_HAL_LICENSES_XML = "home/devmode-hal-licenses.xml";
     private static final String STUDIO_INITIAL_DATA_XML = "home/studio-initial-data.xml";
 
     /** This token is used in product's <version> when they want to reuse the Studio product's version */
     private static final String STUDIO_VERSION_TOKEN = "STUDIO-VERSION";
 
     private final static String LAUNCH_INSTANCES_SYSTEM_PROPERTY = "studio.instanceIds";
 
    private final static Map<String, String> defaultContextPaths = ImmutableMap.<String, String>builder()
             .put(STUDIO_BAMBOO, "/builds")
             .put(STUDIO_CONFLUENCE, "/wiki")
             .put(STUDIO_CROWD, "/crowd")
             .put(STUDIO_FECRU, "/")
             .put(STUDIO_JIRA, "/jira")
             .build();
 
     public StudioProductHandler(MavenContext context, MavenGoals goals)
     {
         super(context, goals);
     }
 
 
 
     @Override
     protected ProductArtifact getTestResourcesArtifact()
     {
         return new ProductArtifact("com.atlassian.studio", "studio-test-resources");
     }
 
 
 
     @Override
     public String getId()
     {
         return STUDIO;
     }
 
     /**
      * Returns the list of products that are configured in this studio instance, as defined in 'instanceIds'
      *
      * @param studioContext
      *            the Studio product
      * @return a list of instance ids. Never null.
      * @throws MojoExecutionException
      *             if the Studio product is misconfigured
      */
     public List<String> getDependantInstances(Product studioContext) throws MojoExecutionException
     {
         StudioProperties studioProperties = getStudioProperties(studioContext);
         studioProperties.setStudioProduct(studioContext);
         List<String> instanceIds = studioContext.getInstanceIds();
         if (instanceIds.isEmpty())
         {
             instanceIds.add(STUDIO_CROWD);
             instanceIds.add(STUDIO_JIRA);
             instanceIds.add(STUDIO_CONFLUENCE);
             instanceIds.add(STUDIO_FECRU);
             instanceIds.add(STUDIO_BAMBOO);
         }
         return instanceIds;
     }
 
     /**
      * System property 'studio.instanceIds': If defined, only runs those applications.
      *
      * They must be comma-separated, eg: {@code -Dstudio.instanceids=studio-crowd,studio-jira}.
      * The studio configuration will be built using the pom.xml configuration.
      */
     public Set<String> getExcludedInstances(Product studioContext)
     {
         String restriction = System.getProperty(LAUNCH_INSTANCES_SYSTEM_PROPERTY);
         if (restriction == null)
         {
             return null;
         }
         String[] restrictionList = restriction.split(",");
         log.info(String.format("Excluding %s from the %s instance.", Arrays.toString(restrictionList), studioContext.getInstanceId()));
         return Sets.newHashSet(restrictionList);
     }
 
     /**
      * Prepares the studio home. Does not start any application.
      *
      */
     @Override
     public int start(Product ctx) throws MojoExecutionException
     {
         // Sanity check
         sanityCheck(ctx);
 
         // Launch the product
         createStudioHome(ctx);
 
         // The symlink is pretty much constrained
         // - must be in /target (the work dir for Bamboo)
         // - must be 2 levels up from the studio home
         File symlink = new File(context.getProject().getBuild().getDirectory(), "svn");
         if (!symlink.exists())
         {
             // Create a symlink so that Bamboo can work
             createSymlink(ctx.getStudioProperties().getSvnRoot(), symlink);
         }
 
         return 0;
     }
 
     /**
      * Checks the configuration to throw exceptions early for the few most common problems
      */
     private void sanityCheck(Product studioProduct) throws MojoExecutionException
     {
         StudioProperties properties = studioProduct.getStudioProperties();
         if (properties == null)
         {
             throw new MojoExecutionException(String.format("Something went wrong when starting %s. The 'studio' handler was not initialised propertly.",
                     studioProduct.getInstanceId()));
         }
         if (properties.getCrowd() == null || properties.getCrowd().getStudioProperties() == null)
         {
             log.error(String.format(
                     "You won't be able to run %s, Studio-Crowd was not configured properly.", studioProduct.getInstanceId()));
         }
         if (properties.isJiraEnabled() && (properties.getJira() == null || properties.getJira().getStudioProperties() == null))
         {
             log.error(String.format(
                     "You won't be able to run %s, Studio-JIRA was not configured properly.", studioProduct.getInstanceId()));
         }
         if (properties.isConfluenceEnabled() && (properties.getConfluence() == null || properties.getConfluence().getStudioProperties() == null))
         {
             log.error(String.format(
                     "You won't be able to run %s, Studio-Confluence was not configured properly.", studioProduct.getInstanceId()));
         }
         if (properties.isFisheyeEnabled() && (properties.getFisheye() == null || properties.getFisheye().getStudioProperties() == null))
         {
             log.error(String.format(
                     "You won't be able to run %s, Studio-Fisheye was not configured properly.", studioProduct.getInstanceId()));
         }
         if (properties.isBambooEnabled() && (properties.getBamboo() == null || properties.getBamboo().getStudioProperties() == null))
         {
             log.error(String.format(
                     "You won't be able to run %s, Studio-Bamboo was not configured properly.", studioProduct.getInstanceId()));
         }
     }
 
     @Override
     public void stop(Product ctx) throws MojoExecutionException
     {
         // Delete the symlink so that the mvn clean:clean works properly
         File symlink = new File(context.getProject().getBuild().getDirectory(), "svn");
         symlink.deleteOnExit();
 
         // Nothing to stop
     }
 
     @Override
     public int getDefaultHttpPort()
     {
         // No default - this product can't be launched
         return 0;
     }
 
     /**
      * Does nothing for non-studios products.
      * For Studio products, defaults the studio-specific properties.
      *
      * @param product
      *            a product. All products are accepted but not all of the will be
      *            modified. The product must have an instanceId.
      */
     public static void setDefaultValues(MavenContext context, Product product)
     {
         String defaultContextPath = defaultContextPaths.get(product.getId());
         if (defaultContextPath != null)
         {
             // It's a Studio product
             if (product.getOutput() == null)
             {
                 product.setOutput(new File(context.getProject().getBuild().getDirectory(), product.getInstanceId() + ".log").getAbsolutePath());
             }
             if (product.getContextPath() == null)
             {
                 product.setContextPath(defaultContextPath);
             }
             if (product.getVersion() == null)
             {
                 // This value will be replaced with the version given by the studio product.
                 // We can't leave it empty because the value will be defaulted to RELEASE.
                 product.setVersion(STUDIO_VERSION_TOKEN);
             }
         }
     }
 
     /**
      * Requests the Studio instance to configure its fellow products (home directory, ...)
      *
      * Not thread safe.
      *
      * @param studioContext
      *            the studio instance. There may be several Studio instances, so the products should be configured
      *            with this instance in mind.
      *
      * @param dependantProducts
      *            the list of products running 'in' this instance of studio (same home & applinked).
      *            The client should guarantee it calls this method once and only once for all the product on one
      *            Studio instance.
      * @throws MojoExecutionException
      */
     public void configure(Product studioContext, List<ProductExecution> dependantProducts) throws MojoExecutionException
     {
         StudioProperties studioProperties = getStudioProperties(studioContext);
 
         boolean confluenceStandalone = true;
 
         // Sets properties for each product
         for (ProductExecution execution : dependantProducts)
         {
             // Each product provides some configuration info
             Product product = execution.getProduct();
             if (STUDIO_CROWD.equals(product.getId()))
             {
                 studioProperties.setCrowd(product);
             }
             else if (STUDIO_CONFLUENCE.equals(product.getId()))
             {
                 studioProperties.setConfluence(product);
             }
             else if (STUDIO_JIRA.equals(product.getId()))
             {
                 studioProperties.setJira(product);
                 confluenceStandalone = false;
             }
             else if (STUDIO_FECRU.equals(product.getId()))
             {
                 studioProperties.setFisheye(product);
                 confluenceStandalone = false;
             }
             else if (STUDIO_BAMBOO.equals(product.getId()))
             {
                 studioProperties.setBamboo(product);
                 confluenceStandalone = false;
             }
             else
             {
                 throw new MojoExecutionException("A non-studio product was listed in a Studio instance: " + product.getInstanceId());
             }
 
             studioProperties.setModeConfluenceStandalone(confluenceStandalone);
 
             // And share the bean between all products
             product.setStudioProperties(studioProperties);
 
             // Set the common Studio version
             if (STUDIO_VERSION_TOKEN.equals(product.getVersion()))
             {
                 product.setVersion(studioProperties.getVersion());
             }
         }
     }
 
     /**
      * Return the studio properties. If it doesn't exist, create the bean.
      * Not thread safe.
      *
      * @param studioContext
      *            the Studio product
      * @return the properties, never null.
      */
     private static StudioProperties getStudioProperties(Product studioContext)
     {
         StudioProperties properties = studioContext.getStudioProperties();
         if (properties == null)
         {
             properties = new StudioProperties(studioContext);
             studioContext.setStudioProperties(properties);
         }
         return properties;
     }
 
     /**
      * Studio returns the parent of studio-home, to ship other application's homes:
      * <ul>
      * <li>studioInstance <b>(&lt;- the snapshot)</b></li>
      * <li>studioInstance/confluence-home</li>
      * <li>studioInstance/jira-home</li>
      * <li>studioInstance/...</li>
      * <li>studioInstance/home <b>(&lt;- the home)</b></li>
      * <ul>
      */
     @Override
     public File getSnapshotDirectory(Product studio)
     {
         return getHomeDirectory(studio).getParentFile();
     }
 
 
     /**
      * Fills the properties with the studio configuration.
      *
      * If the studio1-home directory does not exist, creates it and fills it with the right contents.
      * If this studio1-home exists, do not change the contents
      *
      * This method must be guaranteed to be called:
      * <ul>
      * <li>Exactly once for this StudioProperties bean.</li>
      * <li>After {@link #configure(Product, List)}.</li>
      * <li>Before any product's home is created or any product is started</li>
      * </ul>
      *
      * <p>
      * It also adds the svn home and the webdav home. The final tree is:
      * <ul>
      * <li>studioInstance1
      * <ul>
      * <li>home</li>
      * <li>svn-home</li>
      * <li>webdav-home</li>
      * </ul>
      * </li>
      * </ul>
      * </p>
      *
      * @param studio
      *            the Studio properties. Must not be null.
      * @param buildirectory
      *            the base directory (you can obtain it using ((MavenProject)project).getBuild().getDirectory())
      * @throws MojoExecutionException
      *
      */
     // This method reproduces PrepareStudioMojo.groovy
     public void createStudioHome(Product studio) throws MojoExecutionException
     {
         StudioProperties properties = getStudioProperties(studio);
 
         // All homes are exported, including the studioInstanceId/home
         File studioHomeDir = getHomeDirectory(studio);
         File studioCommonsDir = studioHomeDir.getParentFile();
         String studioProductDataOrigin = studioCommonsDir.getAbsolutePath();
 
         // Extracts the zip / copies the homes to studioInstanceId/
         if (!studioHomeDir.exists())
         {
             extractHome(studioCommonsDir, studio);
             if (!studioHomeDir.exists())
             {
                 throw new MojoExecutionException(studioProductDataOrigin + "studio-test-resources.zip must contain a 'xx/xx/home' folder");
             }
         }
 
         File svnHomeDir = new File(studioCommonsDir, "svn-home");
         if (!svnHomeDir.exists())
         {
             throw new MojoExecutionException(studioProductDataOrigin + " must contain a 'xx/xx/svn-home' folder");
         }
 
         File webDavDir = new File(studioCommonsDir, "webdav-home");
         if (!webDavDir.exists())
         {
             throw new MojoExecutionException(studioProductDataOrigin + " must contain a 'xx/xx/webdav-home' folder");
         }
 
         String svnPublicUrl;
         if (System.getProperty("os.name").toLowerCase(Locale.ENGLISH).contains("windows"))
         {
             svnPublicUrl = "file:///" + svnHomeDir.getAbsolutePath();
             log.warn("Studio is only designed to run on Linux systems.");
         }
         else
         {
             svnPublicUrl = "file://" + svnHomeDir.getAbsolutePath();
         }
 
         properties.setStudioHome(studioHomeDir.getAbsolutePath());
         properties.setSvnRoot(svnHomeDir.getAbsolutePath());
         properties.setSvnPublicUrl(svnPublicUrl);
         properties.setWebDavHome(webDavDir.getAbsolutePath());
 
         // Parametrise the files
         parameteriseFiles(studioCommonsDir, studio);
 
         // Set the system properties
         properties.overrideSystemProperty("studio.home", studioHomeDir.getAbsolutePath());
         properties.overrideSystemProperty("studio.initial.data.xml", new File(studioCommonsDir, STUDIO_INITIAL_DATA_XML).getAbsolutePath());
         properties.overrideSystemProperty("studio.initial.data.properties", new File(studioCommonsDir, STUDIO_INITIAL_DATA_PROPERTIES).getAbsolutePath());
         properties.overrideSystemProperty("studio.hal.instance.uri", new File(studioCommonsDir, DEVMODE_HAL_LICENSES_XML).getAbsolutePath());
 
         // Sets the home data for the products - we don't override productDataVersion because we
         // won't ship separate studio versions of homes.
         Product crowd = properties.getCrowd();
         if (crowd.getDataPath() == null)
         {
             crowd.setDataPath(new File(studioCommonsDir, "crowd-home").getAbsolutePath());
         }
 
         Product confluence = properties.getConfluence();
         if (confluence != null && confluence.getDataPath() == null)
         {
             confluence.setDataPath(new File(studioCommonsDir, "confluence-home").getAbsolutePath());
         }
 
         Product jira = properties.getJira();
         if (jira != null && jira.getDataPath() == null)
         {
             jira.setDataPath(new File(studioCommonsDir, "jira-home").getAbsolutePath());
         }
 
         Product bamboo = properties.getBamboo();
         if (bamboo != null && bamboo.getDataPath() == null)
         {
             bamboo.setDataPath(new File(studioCommonsDir, "bamboo-home").getAbsolutePath());
         }
 
         Product fecru = properties.getFisheye();
         if (fecru != null && fecru.getDataPath() == null)
         {
             fecru.setDataPath(new File(studioCommonsDir, "fecru-home").getAbsolutePath());
         }
     }
 
     private void parameteriseFiles(File studioSnapshotCopyDir, Product studio) throws MojoExecutionException
     {
         ConfigFileUtils.replace(getConfigFiles(studio, studioSnapshotCopyDir), getReplacements(studio), false, log);
     }
 
     @Override
     public List<File> getConfigFiles(Product studio, File studioSnapshotDir)
     {
         List<File> list = Lists.newArrayList();
         list.add(new File(studioSnapshotDir, STUDIO_PROPERTIES));
         list.add(new File(studioSnapshotDir, STUDIO_INITIAL_DATA_PROPERTIES));
         list.add(new File(studioSnapshotDir, STUDIO_INITIAL_DATA_XML));
         list.add(new File(studioSnapshotDir, DEVMODE_HAL_LICENSES_XML));
         return list;
     }
 
     /**
      * Both used to unzip and rezip the home
      */
     @Override
     public List<Replacement> getReplacements(final Product studio)
     {
         List<Replacement> replacements = super.getReplacements(studio);
         replacements.addAll(new ArrayList<Replacement>()
         {
             private void putNonReversibleIfNotNull(String key, String value)
             {
                 if (value != null)
                 {
                     add(new Replacement(key, value, false));
                 }
             }
 
             private void putIfNotNull(String key, String value)
             {
                 if (value != null)
                 {
                     add(new Replacement(key, value));
                 }
             }
 
             // Static bloc for the anonymous subclass of ArrayList
             {
                 StudioProperties properties = studio.getStudioProperties();
                 putIfNotNull("%GREENHOPPER-LICENSE%", "test-classes/greenhopper.license");
 
                 if (properties.isJiraEnabled())
                 {
                     File attachmentsFolder = new File(getHomeDirectory(properties.getJira()), "attachments");
                     putIfNotNull("%JIRA-ATTACHMENTS%", attachmentsFolder.getAbsolutePath());
                     putIfNotNull("%JIRA-BASE-URL%", properties.getJiraUrl());
                     putIfNotNull("%JIRA-HOST-URL%", properties.getJiraHostUrl());
                     putIfNotNull("%JIRA-CONTEXT%", properties.getJiraContextPath());
                 }
 
                 if (properties.isConfluenceEnabled())
                 {
                     putIfNotNull("%CONFLUENCE-BASE-URL%", properties.getConfluenceUrl());
                     putIfNotNull("%CONFLUENCE-HOST-URL%", properties.getConfluenceHostUrl());
                     putIfNotNull("%CONFLUENCE-CONTEXT%", properties.getConfluenceContextPath());
                 }
 
                 if (properties.isFisheyeEnabled())
                 {
                     putIfNotNull("%FISHEYE-BASE-URL%", properties.getFisheyeUrl());
                     putIfNotNull("%FISHEYE-HOST-URL%", properties.getFisheyeHostUrl());
                     putIfNotNull("%FISHEYE-CONTROL-PORT%", properties.getFisheyeControlPort());
                     putNonReversibleIfNotNull("%FISHEYE-CONTEXT%", properties.getFisheyeContextPath());
                     putIfNotNull("%FISHEYE-SHUTDOWN-ENABLED%", properties.getFisheyeShutdownEnabled());
                 }
 
                 if (properties.isBambooEnabled())
                 {
                     putIfNotNull("%BAMBOO-BASE-URL%", properties.getBambooUrl());
                     putIfNotNull("%BAMBOO-HOST-URL%", properties.getBambooHostUrl());
                     putIfNotNull("%BAMBOO-CONTEXT%", properties.getBambooContextPath());
                     putNonReversibleIfNotNull("%BAMBOO-ENABLED%", "true");
                 }
                 else
                 {
                     putNonReversibleIfNotNull("%BAMBOO-ENABLED%", "false");
                 }
 
                 putIfNotNull("%CROWD-BASE-URL%", properties.getCrowdUrl());
                 putIfNotNull("%CROWD-HOST-URL%", properties.getCrowdHostUrl());
                 putIfNotNull("%CROWD-CONTEXT%", properties.getCrowdContextPath());
 
                 putIfNotNull("%SVN-BASE-URL%", properties.getSvnRoot());
                 putIfNotNull("%SVN-PUBLIC-URL%", properties.getSvnPublicUrl());
                 putIfNotNull("%SVN-HOOKS%", properties.getSvnHooks());
 
                 putNonReversibleIfNotNull("%STUDIO-DATA-LOCATION%", "");
                 putIfNotNull("%STUDIO-HOME%", properties.getStudioHome());
                 putNonReversibleIfNotNull("%GAPPS-ENABLED%", Boolean.toString(properties.isGappsEnabled()));
                 if (properties.isGappsEnabled())
                 {
                     putNonReversibleIfNotNull("%GAPPS-ENABLED%", Boolean.toString(true));
                     putIfNotNull("%STUDIO-GAPPS-DOMAIN%", properties.getGappsDomain());
                 }
                 else
                 {
                     putNonReversibleIfNotNull("%GAPPS-ENABLED%", Boolean.toString(false));
                 }
                 putIfNotNull("%STUDIO-WEBDAV-DIRECTORY%", properties.getWebDavHome());
                 putIfNotNull("%STUDIO-SVN-IMPORT-STAGING-DIRECTORY%", properties.getSvnImportStagingDirectory());
             }
         });
         return replacements;
     }
 
     private void createSymlink(String source, File target) throws MojoExecutionException
     {
         String[] systemCommand = {
                 "ln",
                 "-s",
                 source,
                 target.getAbsolutePath()
         };
         try
         {
 
             Process symlinkCreation = Runtime.getRuntime().exec(systemCommand);
 
             // In case of errors, write the message and wait for the user to acknowledge
             BufferedReader errorStream = new BufferedReader(
                     new InputStreamReader(symlinkCreation.getErrorStream()));
             String errorLine = null;
             boolean hasErrors = false;
             while ((errorLine = errorStream.readLine()) != null)
             {
                 if (!hasErrors)
                 {
                     System.err.println("Error while executing " + systemCommand + ": ");
                     hasErrors = true;
                 }
                 System.err.println(errorLine);
             }
             if (hasErrors)
             {
                 System.out.println("Please execute this command in your command line and press a key to continue");
                 System.in.read();
             }
         }
         catch (IOException e)
         {
             throw new MojoExecutionException("Could not create the symlink: " + systemCommand, e);
         }
     }
 
     /**
      * Copies/Extracts the data into parent/directoryName
      * @throws MojoExecutionException
      */
     private void extractHome(File target, Product studio) throws MojoExecutionException
     {
         // Take whichever is provided by the user (dataPath or productDataVersion zip to download)
         File testResourcesZip = getProductHomeData(studio);
 
         try
         {
             if (!testResourcesZip.exists())
             {
                 throw new MojoExecutionException(String.format("This source doesn't exist: %s", testResourcesZip.getAbsoluteFile()));
             }
             if (testResourcesZip.isDirectory())
             {
                 copyDirectory(testResourcesZip, target);
             }
             else
             {
                 unzip(testResourcesZip, target.getAbsolutePath(), 2);
             }
         }
         catch (IOException ioe)
         {
             throw new MojoExecutionException(String.format("Unable to copy/unzip the studio home from %s to %s", testResourcesZip.getAbsolutePath(),
                     target.getAbsolutePath()), ioe);
         }
     }
 
     /**
      * Performs the necessary initialisation for Studio products' homes
      */
     static void processProductsHomeDirectory(Log log, Product ctx, File homeDir) throws MojoExecutionException
     {
         // Nothing to process in the home.
         // Just check Studio has been configured.
         if (ctx.getStudioProperties() == null)
         {
             throw new MojoExecutionException(String.format("%s product is dependant on Studio. You must include the Studio product in your execution.",
                     ctx.getInstanceId()));
         }
     }
 
     /**
      * Performs the necessary initialisation for Studio products
      *
      * @param log
      * @param ctx
      * @param homeDir
      * @param explodedWarDir
      * @param crowdPropertiesPath
      *            the path from the explodedWarDir to the crowd.properties
      * @throws MojoExecutionException
      */
     static void addProductHandlerOverrides(Log log, Product ctx, File homeDir, File explodedWarDir) throws MojoExecutionException
     {
         addProductHandlerOverrides(log, ctx, homeDir, explodedWarDir, "WEB-INF/classes/crowd.properties");
     }
 
     /**
      * Performs the necessary initialisation for Studio products
      *
      * @param log
      * @param ctx
      * @param homeDir
      * @param explodedWarDir
      * @param crowdPropertiesPath
      *            the path from the explodedWarDir to the crowd.properties
      * @throws MojoExecutionException
      */
     static void addProductHandlerOverrides(Log log, Product ctx, File homeDir, File explodedWarDir, String crowdPropertiesPath) throws MojoExecutionException
     {
         File crowdProperties = new File(explodedWarDir, crowdPropertiesPath);
         if (checkFileExists(crowdProperties, log))
         {
             parametriseCrowdFile(crowdProperties, ctx.getStudioProperties().getCrowdUrl(), log);
         }
     }
 
     /**
      * Replaces the crowd url in the the crowd.properties of the current application
      *
      * @param crowdProperties
      *            the file "crowd.properties"
      * @param crowdUrl
      *            the Crowd url, example: "http://localhost:4990/crowd"
      * @throws MojoExecutionException
      *             if an error is encountered during the replacement
      */
     private static void parametriseCrowdFile(File crowdProperties, String crowdUrl, Log log) throws MojoExecutionException
     {
         List<Replacement> replacements = Lists.newArrayList();
         replacements.add(new Replacement("%CROWD-INTERNAL-URL%", crowdUrl));
         replacements.add(new Replacement("%CROWD-URL%", crowdUrl));
 
         ConfigFileUtils.replace(crowdProperties, replacements, false, log);
     }
 
 
     public void cleanupProductHomeForZip(Product studioProduct, File studioHome) throws MojoExecutionException
     {
         try
         {
             // Get products of this Studio instance
             StudioProperties studioProperties = studioProduct.getStudioProperties();
 
             // The key of this map is the name of the home folder for this application
             // Unused applications are "null", so they will not be seen in the map
             Map<String, Product> products = Maps.newHashMap();
             products.put("crowd-home", studioProperties.getCrowd());
             products.put("confluence-home", studioProperties.getConfluence());
             products.put("jira-home", studioProperties.getJira());
             products.put("fecru-home", studioProperties.getFisheye());
             products.put("bamboo-home", studioProperties.getBamboo());
 
             // Make each product's home
             Iterator<String> productKeys = products.keySet().iterator();
             while (productKeys.hasNext())
             {
                 String productHomeName = productKeys.next();
                 Product product = products.get(productHomeName);
                 if (product != null)
                 {
                     File productDestinationDirectory = new File(studioHome, productHomeName);
                     copyAndCleanProductHome(product, productDestinationDirectory);
                 }
             }
 
 
             // Un-parametrise the files
             super.cleanupProductHomeForZip(studioProduct, studioHome);
         }
         catch (IOException ioe)
         {
             throw new MojoExecutionException("Could not copy a product home directory.", ioe);
         }
 
     }
 
     private void copyAndCleanProductHome(Product product, File productDestinationDirectory) throws IOException, MojoExecutionException
     {
         ProductHandler handler = ProductHandlerFactory.create(product.getId(), context, goals);
         File productHomeDirectory = getHomeDirectory(product);
 
         // Delete studio1/{product}-home and replace it with the current product's home
         if (productDestinationDirectory.exists())
         {
             FileUtils.deleteDirectory(productDestinationDirectory);
         }
         ProjectUtils.createDirectory(productDestinationDirectory);
         copyDirectory(productHomeDirectory, productDestinationDirectory);
 
         // Request the product to clean up
         handler.cleanupProductHomeForZip(product, productDestinationDirectory);
     }
 
     static String fixWindowsSlashes(final String path)
     {
         return path.replaceAll("\\\\", "/");
     }
 
     static boolean checkFileExists(File file, Log log)
     {
         if (!file.exists())
         {
             log.warn(String.format("%s does not exist. Will skip customisation", file.getAbsolutePath()));
             return false;
         }
         return true;
     }
 
     /**
      * Returns the first value which is not null. Useful to set default values
      *
      * @param t
      * @return the first non-null value, or null if all values are null
      */
     public static <T> T firstNotNull(T... values)
     {
         for (T t : values)
         {
             if (t != null)
             {
                 return t;
             }
         }
         return null;
     }
 }
