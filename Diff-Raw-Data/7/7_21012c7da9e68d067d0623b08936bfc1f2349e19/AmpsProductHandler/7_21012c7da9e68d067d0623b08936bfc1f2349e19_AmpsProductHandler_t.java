 package com.atlassian.maven.plugins.amps.product;
 
 
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 import com.atlassian.maven.plugins.amps.MavenContext;
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import com.atlassian.maven.plugins.amps.Product;
 import com.atlassian.maven.plugins.amps.ProductArtifact;
 import com.atlassian.maven.plugins.amps.util.ConfigFileUtils;
 import com.atlassian.maven.plugins.amps.util.ConfigFileUtils.Replacement;
 import com.atlassian.maven.plugins.amps.util.ProjectUtils;
 import com.atlassian.maven.plugins.amps.util.ZipUtils;
 import com.google.common.collect.Lists;
 
 /**
  * This abstract class is common to real applications (which inherit from AbstractProductHandler, like JIRA or Confluence)
  * and the fake application Studio.
  *
  * This class handles common operations
  *
  * @since 3.6
  */
 public abstract class AmpsProductHandler implements ProductHandler
 {
 
     protected final MavenGoals goals;
     protected final MavenProject project;
     protected final MavenContext context;
     protected final Log log;
 
     protected AmpsProductHandler(MavenContext context, MavenGoals goals)
     {
         this.project = context.getProject();
         this.context = context;
         this.goals = goals;
         this.log = context.getLog();
     }
 
     /**
      * Copies and creates a zip file of the previous run's home directory minus any installed plugins.
      *
      * @param homeDirectory
      *            The path to the previous run's home directory.
      * @param targetZip
      *            The path to the final zip file.
      * @param product
      *            The product
      *
      * @since 3.1-m3
      */
     public void createHomeZip(final File homeDirectory, final File targetZip, final Product product) throws MojoExecutionException
     {
         if (homeDirectory == null || !homeDirectory.exists())
         {
             String homePath = "null";
             if (homeDirectory != null)
             {
                 homePath = homeDirectory.getAbsolutePath();
             }
             context.getLog().info("home directory doesn't exist, skipping. [" + homePath + "]");
             return;
         }
 
 
         try
             {
             /*
              * The zip has /someRootFolder/{productId}-home/
              */
             final File appDir = getBaseDirectory(product);
             final File tmpDir = new File(appDir, "tmp-resources");
             final File homeSnapshot = new File(tmpDir, "generated-home");
             final String entryBase = "generated-resources/" + product.getId() + "-home";
 
             if (homeSnapshot.exists())
             {
                 FileUtils.deleteDirectory(homeSnapshot);
             }
 
             homeSnapshot.mkdirs();
             FileUtils.copyDirectory(homeDirectory, homeSnapshot, true);
 
             cleanupProductHomeForZip(product, homeSnapshot);
             ZipUtils.zipDir(targetZip, homeSnapshot, entryBase);
         }
         catch (IOException e)
         {
             throw new RuntimeException("Error zipping home directory", e);
         }
     }
 
     /**
      * Prepares the home directory to snapshot:
      * <ul>
      * <li>Removes all unnecessary files</li>
      * <li>Perform product-specific clean-up</li>
      * <ul>
      * This is a reference implementation. It is probable that each application has a different set of directories to delete.
      * @param product the product details
      * @param homeDirectory an image of the home which will be zipped. This is not the working home, so you're free to remove files and parametrise them.
      * @throws IOException
      */
     public void cleanupProductHomeForZip(Product product, File snapshotDir) throws MojoExecutionException, IOException
     {
         try {
             // we want to get rid of the plugins folders.
             FileUtils.deleteDirectory(new File(snapshotDir, "plugins")); // Not used by: fisheye, confluence, studio - Used by: crowd, bamboo, jira
             FileUtils.deleteDirectory(new File(snapshotDir, "bundled-plugins")); // Not used by: fisheye, jira - Used by: confluence, crowd, bamboo
 
             // Get rid of "studio-test-resources.zip", which is the homeZip that was used
             // when we started Amps.
            if (this.getTestResourcesArtifact() != null)
            {
                String originalHomeZip = this.getTestResourcesArtifact().getArtifactId() + ".zip";
                FileUtils.deleteQuietly(new File(snapshotDir, originalHomeZip));
            }
 
 
             // Proceed to replacements
             List<Replacement> replacements = getReplacements(product);
             // Sort by longer values first, so that the right keys are used.
             Collections.sort(replacements, new Comparator<Replacement>(){
                 @Override
                 public int compare(Replacement replacement1, Replacement replacement2)
                 {
                     // longest value < shortest value
                     int length1 = replacement1.getValue().length();
                     int length2 = replacement2.getValue().length();
                     return length2 - length1;
                 }
             });
             List<File> files = getConfigFiles(product, snapshotDir);
 
             ConfigFileUtils.replace(files, replacements, true, log);
         }
         catch (IOException ioe)
         {
             throw new MojoExecutionException("Could not delete home/plugins/ and /home/bundled-plugins/", ioe);
         }
     }
 
     abstract protected ProductArtifact getTestResourcesArtifact();
 
     protected File getProductHomeData(final Product ctx) throws MojoExecutionException
     {
         File productHomeZip = null;
         String dpath = ctx.getDataPath();
 
         //use custom zip if supplied
         if (isNotBlank(dpath))
         {
             File customHomeZip = new File(dpath);
 
             if (customHomeZip.exists())
             {
                 productHomeZip = customHomeZip;
             }
             else
             {
                 throw new MojoExecutionException("Unable to use custom test resources set by <productDataPath>. File '" +
                         customHomeZip.getAbsolutePath() + "' does not exist");
             }
         }
 
         //if we didn't find a custom zip, use the default
         ProductArtifact testResourcesArtifact = getTestResourcesArtifact();
         if (productHomeZip == null && testResourcesArtifact != null)
         {
             ProductArtifact artifact = new ProductArtifact(
                 testResourcesArtifact.getGroupId(), testResourcesArtifact.getArtifactId(), ctx.getDataVersion());
             productHomeZip = goals.copyHome(getBaseDirectory(ctx), artifact);
         }
 
         return productHomeZip;
     }
 
 
     /**
      * Lists parameters which must be replaced in the configuration files of the home directory.
      * <p/>
      * Used reversely when reading / when creating a home zip.
      */
     public List<ConfigFileUtils.Replacement> getReplacements(Product product)
     {
         // Standard replacements:
         List<Replacement> replacements = Lists.newArrayList();
         String buildDirectory = project.getBuild().getDirectory();
         String baseDirectory = getBaseDirectory(product).getAbsolutePath();
         String homeDirectory = getHomeDirectory(product).getAbsolutePath();
 
         replacements.add(new Replacement("%PROJECT_BUILD_DIR%", buildDirectory));
         replacements.add(new Replacement("%PRODUCT_BASE_DIR%", baseDirectory));
         replacements.add(new Replacement("%PRODUCT_HOME_DIR%", homeDirectory));
 
         // These replacements are especially for Fecru, but there's no reason not to find them in other config files
         try {
             replacements.add(new Replacement("%PROJECT_BUILD_DIR_URL_ENCODED%", URLEncoder.encode(propertiesEncode(buildDirectory), "UTF-8")));
             replacements.add(new Replacement("%PRODUCT_BASE_DIR_URL_ENCODED%", URLEncoder.encode(propertiesEncode(baseDirectory), "UTF-8")));
             replacements.add(new Replacement("%PRODUCT_HOME_DIR_URL_ENCODED%", URLEncoder.encode(propertiesEncode(homeDirectory), "UTF-8")));
         }
         catch (UnsupportedEncodingException badJvm)
         {
             throw new RuntimeException("UTF-8 should be supported on any JVM", badJvm);
         }
 
 
         return replacements;
     }
 
     /**
      * Encodes a String for a Properties file. Escapes : and =.
      */
     private String propertiesEncode(String decoded)
     {
         String replacement1 = decoded.replaceAll(":", "\\:");
         String replacement2 = replacement1.replaceAll("=", "\\=");
         return replacement2;
     }
 
     @Override
     public List<File> getConfigFiles(Product product, File snapshotDir)
     {
         return Lists.newArrayList();
     }
 
     public File getBaseDirectory(Product ctx)
     {
         return ProjectUtils.createDirectory(new File(project.getBuild().getDirectory(), ctx.getInstanceId()));
     }
 
     public File getHomeDirectory(Product ctx)
     {
         return new File(getBaseDirectory(ctx), "home");
     }
 
     public File getSnapshotDirectory(Product product)
     {
         return getHomeDirectory(product);
     }
 
     protected File createHomeDirectory(Product ctx)
     {
         return ProjectUtils.createDirectory(getHomeDirectory(ctx));
     }
 }
