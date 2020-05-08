 package com.atlassian.maven.plugins.amps.product;
 
 import static com.atlassian.maven.plugins.amps.util.FileUtils.doesFileNameMatchArtifact;
 import static com.atlassian.maven.plugins.amps.util.ZipUtils.unzip;
 import static org.apache.commons.io.FileUtils.copyDirectory;
 import static org.apache.commons.io.FileUtils.copyFile;
 import static org.apache.commons.io.FileUtils.iterateFiles;
 import static org.apache.commons.io.FileUtils.moveDirectory;
 import static org.apache.commons.io.FileUtils.readFileToString;
 import static org.apache.commons.lang.StringUtils.isNotBlank;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import com.atlassian.maven.plugins.amps.Product;
 import com.atlassian.maven.plugins.amps.ProductArtifact;
 
 public abstract class AbstractProductHandler implements ProductHandler
 {
     protected final MavenGoals goals;
     protected final MavenProject project;
     private final PluginProvider pluginProvider;
 
     protected AbstractProductHandler(MavenProject project, MavenGoals goals, PluginProvider pluginProvider)
     {
         this.project = project;
         this.goals = goals;
         this.pluginProvider = pluginProvider;
     }
     
     public final int start(final Product ctx) throws MojoExecutionException
     {
         final File homeDir = extractAndProcessHomeDirectory(ctx);
         final File extractedApp = extractApplication(ctx, homeDir);
         final File finalApp = addArtifactsAndOverrides(ctx, homeDir, extractedApp);
         return startApplication(ctx, finalApp, homeDir, mergeSystemProperties(ctx));
     }
 
     protected final File extractAndProcessHomeDirectory(final Product ctx) throws MojoExecutionException
     {
         final File productHomeData = getProductHomeData(ctx);
         if (productHomeData != null)
         {
             final File homeDir = getHomeDirectory(ctx);
 
             // Only create the home dir if it doesn't exist
             if (!homeDir.exists())
             {
                 extractProductHomeData(productHomeData, homeDir, ctx);
 
                 // just in case
                 homeDir.mkdir();
                 processHomeDirectory(ctx, homeDir);
             }
 
             // Always override files regardless of home directory existing or not
             try
             {
                 overrideAndPatchHomeDir(homeDir, ctx);
             }
             catch (IOException e)
             {
                 throw new MojoExecutionException("Unable to override files using src/test/resources", e);
             }
 
             return homeDir;
         }
         else
         {
             return getHomeDirectory(ctx);
         }
     }
 
     private File getProductHomeData(final Product ctx) throws MojoExecutionException
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
         if (productHomeZip == null && getTestResourcesArtifact() != null)
         {
             ProductArtifact artifact = new ProductArtifact(
                 getTestResourcesArtifact().getGroupId(), getTestResourcesArtifact().getArtifactId(), ctx.getDataVersion());
             if (artifact != null)
             {
                 productHomeZip = goals.copyHome(getBaseDirectory(ctx), artifact);
             }
         }
 
         return productHomeZip;
     }
 
     protected void extractProductHomeData(File productHomeData, File homeDir, Product ctx)
             throws MojoExecutionException
     {
         final File tmpDir = new File(getBaseDirectory(ctx), "tmp-resources");
         tmpDir.mkdir();
 
         try
         {
             if (productHomeData.isFile())
             {
                File tmp = new File(getBaseDirectory(ctx), ctx.getId() + "-home");
                
                 unzip(productHomeData, tmpDir.getPath());
                 copyDirectory(tmpDir.listFiles()[0], getBaseDirectory(ctx), true);
                 moveDirectory(tmp, homeDir);
             }
             else if (productHomeData.isDirectory())
             {
                 copyDirectory(productHomeData, homeDir);
             }
         }
         catch (final IOException ex)
         {
             throw new MojoExecutionException("Unable to copy home directory", ex);
         }
     }
 
     private void overrideAndPatchHomeDir(File homeDir, final Product ctx) throws IOException
     {
         final File srcDir = new File(project.getBasedir(), "src/test/resources/" + ctx.getInstanceId() + "-home");
         if (srcDir.exists() && homeDir.exists())
         {
             copyDirectory(srcDir, homeDir);
         }
     }
 
     private final File addArtifactsAndOverrides(final Product ctx, final File homeDir, final File app) throws MojoExecutionException
     {
         try
         {
             final File appDir;
             if (app.isFile())
             {
                 appDir = new File(getBaseDirectory(ctx), "webapp");
                 if (!appDir.exists())
                 {
                     unzip(app, appDir.getAbsolutePath());
                 }
             }
             else
             {
                 appDir = app;
             }
 
             addArtifacts(ctx, homeDir, appDir);
 
             // override war files
             try
             {
                 addOverrides(appDir, ctx);
             }
             catch (IOException e)
             {
                 throw new MojoExecutionException("Unable to override WAR files using src/test/resources/" + ctx.getInstanceId() + "-app", e);
             }
 
             if (app.isFile())
             {
                 final File warFile = new File(app.getParentFile(), getId() + ".war");
                 com.atlassian.core.util.FileUtils.createZipFile(appDir, warFile);
                 return warFile;
             }
             else
             {
                 return appDir;
             }
 
         }
         catch (final Exception e)
         {
             e.printStackTrace();
             throw new MojoExecutionException(e.getMessage());
         }
     }
 
     private void addArtifacts(final Product ctx, final File homeDir, final File appDir)
             throws IOException, MojoExecutionException, Exception
     {
         File pluginsDir = getUserInstalledPluginsDirectory(appDir, homeDir);
         final File bundledPluginsDir = new File(getBaseDirectory(ctx), "bundled-plugins");
         
         bundledPluginsDir.mkdir();
         // add bundled plugins
         final File bundledPluginsZip = new File(appDir, getBundledPluginPath(ctx));
         if (bundledPluginsZip.exists())
         {
             unzip(bundledPluginsZip, bundledPluginsDir.getPath());
         }
         
         if (isStaticPlugin())
         {
             if (!supportsStaticPlugins())
             {
                   throw new MojoExecutionException("According to your atlassian-plugin.xml file, this plugin is not " +
                           "atlassian-plugins version 2. This app currently only supports atlassian-plugins " +
                           "version 2.");
             }
             pluginsDir = new File(appDir, "WEB-INF/lib");
         }
         
         if (pluginsDir == null)
         {
             pluginsDir = bundledPluginsDir;
         }
         
         createDirectory(pluginsDir);
         
         // add this plugin itself if enabled
         if (ctx.isInstallPlugin())
         {
             addThisPluginToDirectory(pluginsDir);
             addTestPluginToDirectory(pluginsDir);
         }
         
         // add plugins2 plugins if necessary
         if (!isStaticPlugin())
         {
             addArtifactsToDirectory(pluginProvider.provide(ctx), pluginsDir);
         }
         
         // add plugins1 plugins
         List<ProductArtifact> artifacts = new ArrayList<ProductArtifact>();
         artifacts.addAll(getDefaultLibPlugins());
         artifacts.addAll(ctx.getLibArtifacts());
         addArtifactsToDirectory(artifacts, new File(appDir, "WEB-INF/lib"));
         
         artifacts = new ArrayList<ProductArtifact>();
         artifacts.addAll(getDefaultBundledPlugins());
         artifacts.addAll(ctx.getBundledArtifacts());
         
         addArtifactsToDirectory(artifacts, bundledPluginsDir);
         
         if (bundledPluginsDir.list().length > 0)
         {
             com.atlassian.core.util.FileUtils.createZipFile(bundledPluginsDir, bundledPluginsZip);
         }
         
         if (ctx.getLog4jProperties() != null && getLog4jPropertiesPath() != null)
         {
             copyFile(ctx.getLog4jProperties(), new File(appDir, getLog4jPropertiesPath()));
         }
     }
     
     abstract protected void processHomeDirectory(Product ctx, File homeDir) throws MojoExecutionException;
     abstract protected ProductArtifact getTestResourcesArtifact();
     abstract protected File extractApplication(Product ctx, File homeDir) throws MojoExecutionException;
     abstract protected int startApplication(Product ctx, File app, File homeDir, Map<String, String> properties) throws MojoExecutionException;
     abstract protected boolean supportsStaticPlugins();
     abstract protected Collection<? extends ProductArtifact> getDefaultBundledPlugins();
     abstract protected Collection<? extends ProductArtifact> getDefaultLibPlugins();
     abstract protected String getBundledPluginPath(Product ctx);
     abstract protected File getUserInstalledPluginsDirectory(File webappDir, File homeDir);
     
     protected String getLog4jPropertiesPath()
     {
         return null;
     }
 
     protected boolean isStaticPlugin() throws IOException
     {
         final File atlassianPluginXml = new File(project.getBasedir(), "src/main/resources/atlassian-plugin.xml");
         if (atlassianPluginXml.exists())
         {
             String text = readFileToString(atlassianPluginXml);
             return !text.contains("pluginsVersion=\"2\"") && !text.contains("plugins-version=\"2\"");
         }
         else
         {
             // probably an osgi bundle
             return false;
         }
     }
 
     protected final void addThisPluginToDirectory(final File targetDir) throws IOException
     {
         final File thisPlugin = getPluginFile();
 
         // remove any existing version
         for (final Iterator<?> iterateFiles = iterateFiles(targetDir, null, false); iterateFiles.hasNext();)
         {
             final File file = (File) iterateFiles.next();
             if (doesFileNameMatchArtifact(file.getName(), project.getArtifactId()))
             {
                 file.delete();
             }
         }
 
         // add the plugin jar to the directory
         copyFile(thisPlugin, new File(targetDir, thisPlugin.getName()));
     }
 
     protected void addTestPluginToDirectory(final File targetDir) throws IOException
     {
         final File testPluginFile = getTestPluginFile();
         if (testPluginFile.exists())
         {
             // add the test plugin jar to the directory
             copyFile(testPluginFile, new File(targetDir, testPluginFile.getName()));
         }
 
     }
 
     protected final File getPluginFile()
     {
         return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".jar");
     }
 
     protected File getTestPluginFile()
     {
         return new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + "-tests.jar");
     }
 
     protected final void addArtifactsToDirectory(final List<ProductArtifact> artifacts, final File pluginsDir) throws MojoExecutionException
     {
         // first remove plugins from the webapp that we want to update
         if (pluginsDir.isDirectory() && pluginsDir.exists())
         {
             for (final Iterator<?> iterateFiles = iterateFiles(pluginsDir, null, false); iterateFiles.hasNext();)
             {
                 final File file = (File) iterateFiles.next();
                 for (final ProductArtifact webappArtifact : artifacts)
                 {
                     if (!file.isDirectory() && doesFileNameMatchArtifact(file.getName(), webappArtifact.getArtifactId()))
                     {
                         file.delete();
                     }
                 }
             }
         }
         // copy the all the plugins we want in the webapp
         if (!artifacts.isEmpty())
         {
             goals.copyPlugins(pluginsDir, artifacts);
         }
     }
 
     protected final void addOverrides(File appDir, final Product ctx) throws IOException
     {
         final File srcDir = new File(project.getBasedir(), "src/test/resources/" + ctx.getInstanceId() + "-app");
         if (srcDir.exists() && appDir.exists())
         {
             copyDirectory(srcDir, appDir);
         }
     }
     
     public final File getBaseDirectory(Product ctx)
     {
         return createDirectory(new File(project.getBuild().getDirectory(), ctx.getInstanceId()));
     }
 
     public final File getHomeDirectory(Product ctx)
     {
         return new File(getBaseDirectory(ctx), "home");
     }
     
     protected final File createHomeDirectory(Product ctx)
     {
         return createDirectory(getHomeDirectory(ctx));
     }
 
     protected final File createDirectory(File dir)
     {
         if (!dir.exists() && !dir.mkdirs())
         {
             throw new RuntimeException("Failed to create directory " + dir.getAbsolutePath());
         }
         return dir;
     }
 
     protected final Map<String, String> mergeSystemProperties(Product ctx)
     {
         final Map<String, String> properties = new HashMap<String, String>();
 
         properties.putAll(getSystemProperties(ctx));
         for (Map.Entry<String, Object> entry : ctx.getSystemPropertyVariables().entrySet())
         {
             properties.put(entry.getKey(), (String) entry.getValue());
         }
         return properties;
     }
 
     protected abstract Map<String, String> getSystemProperties(Product ctx);
 
 }
