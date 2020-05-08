 package com.atlassian.maven.plugins.amps.product;
 
 import com.atlassian.maven.plugins.amps.MavenGoals;
 import com.atlassian.maven.plugins.amps.Product;
 import com.atlassian.maven.plugins.amps.ProductArtifact;
 import com.atlassian.maven.plugins.amps.util.ConfigFileUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import java.io.File;
 import java.util.*;
 
 public class BambooProductHandler extends AbstractWebappProductHandler
 {
     public BambooProductHandler(MavenProject project, MavenGoals goals)
     {
         super(project, goals, new BambooPluginProvider());
     }
 
     public String getId()
     {
         return "bamboo";
     }
 
     public ProductArtifact getArtifact()
     {
         return new ProductArtifact("com.atlassian.bamboo", "atlassian-bamboo-web-app", "RELEASE");
     }
 
     protected Collection<ProductArtifact> getSalArtifacts(String salVersion)
     {
         return Arrays.asList(
                 new ProductArtifact("com.atlassian.sal", "sal-api", salVersion),
                 new ProductArtifact("com.atlassian.sal", "sal-bamboo-plugin", salVersion));
     }
 
     public ProductArtifact getTestResourcesArtifact()
     {
         return new ProductArtifact("com.atlassian.bamboo.plugins", "bamboo-plugin-test-resources", "LATEST");
     }
 
     public int getDefaultHttpPort()
     {
         return 6990;
     }
 
     public Map<String, String> getSystemProperties(Product ctx)
     {
         return Collections.singletonMap("bamboo.home", getHomeDirectory(ctx).getPath());
     }
 
     @Override
     public File getUserInstalledPluginsDirectory(final File webappDir, final File homeDir)
     {
         return new File(homeDir, "plugins");
     }
 
     public List<ProductArtifact> getExtraContainerDependencies()
     {
         return Collections.emptyList();
     }
 
     public String getBundledPluginPath(Product ctx)
     {
         return "WEB-INF/classes/atlassian-bundled-plugins.zip";
     }
 
     public void processHomeDirectory(final Product ctx, final File homeDir) throws MojoExecutionException
     {
         ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "@project-dir@", homeDir.getParent());
         ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "/bamboo-home/", "/home/");
         ConfigFileUtils.replace(new File(homeDir, "bamboo.cfg.xml"), "${bambooHome}", homeDir.getAbsolutePath());
         ConfigFileUtils.replaceAll(new File(homeDir, "/xml-data/configuration/administration.xml"),
                "http://(?:[^:]|\\[.+])+:8085", "http://" + ctx.getServer() + ":" + ctx.getHttpPort() + "/" + ctx.getContextPath().replaceAll("^/|/$", ""));
     }
 
     public List<ProductArtifact> getDefaultLibPlugins()
     {
         return Collections.emptyList();
     }
 
     public List<ProductArtifact> getDefaultBundledPlugins()
     {
         return Collections.emptyList();
     }
 
     private static class BambooPluginProvider extends AbstractPluginProvider
     {
 
         @Override
         protected Collection<ProductArtifact> getSalArtifacts(String salVersion)
         {
             return Arrays.asList(
                 new ProductArtifact("com.atlassian.sal", "sal-api", salVersion),
                 new ProductArtifact("com.atlassian.sal", "sal-bamboo-plugin", salVersion));
         }
 
     }
 }
