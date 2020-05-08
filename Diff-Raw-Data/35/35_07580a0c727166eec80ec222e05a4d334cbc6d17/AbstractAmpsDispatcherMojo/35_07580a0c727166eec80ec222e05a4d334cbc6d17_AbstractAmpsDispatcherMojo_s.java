 package com.atlassian.maven.plugins.ampsdispatcher;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.PluginExecution;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoComponent;
 import org.shaded.mojoexecutor.MojoExecutor;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
 
 import java.util.List;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.HashSet;
 
 import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
 import com.atlassian.maven.plugins.amps.util.VersionUtils;
 
 /**
  * Dispatches to the appropriate amps product-specific plugin by detecting the plugin in the project.
  *
  * @since 3.0-beta2
  */
 public abstract class AbstractAmpsDispatcherMojo extends AbstractMojo
 {
 
     /**
      * The Maven Project Object
      */
     @MojoParameter(expression = "${project}", required = true, readonly = true)
     MavenProject project;
 
     /**
      * The Maven Session Object
      */
     @MojoParameter(expression = "${session}", required = true, readonly = true)
     MavenSession session;
 
     /**
      * The Maven PluginManager Object
      */
     @MojoComponent
     PluginManager pluginManager;
 
     public final void execute() throws MojoExecutionException, MojoFailureException
     {
         String targetArtifactId = detectAmpsProduct();
 
         if (targetArtifactId != null && session.getGoals().size() > 0)
         {
             // We only pass in the first goal since we know the shell scripts only pass in one goal
            String goal = (String) session.getGoals().get(0);
            goal = goal.substring(goal.indexOf(":") + 1);
 
             executeMojo(
                 plugin(
                         groupId("com.atlassian.maven.plugins"),
                         artifactId(targetArtifactId),
                         version(VersionUtils.getVersion())  //ignored anyway
                 ),
                 goal(goal),
                 configuration(),
                 executionEnvironment(project, session, pluginManager));
         }
         else
         {
             throw new MojoFailureException("Couldn't detect an AMPS product to dispatch to");
         }
     }
 
     final String detectAmpsProduct()
     {
         List buildPlugins = project.getBuildPlugins();
 
         Set<String> possiblePluginTypes = new HashSet<String>(ProductHandlerFactory.getIds());
         possiblePluginTypes.add("amps");
 
         if (buildPlugins != null)
         {
             for (Iterator iterator = buildPlugins.iterator(); iterator.hasNext();)
             {
                 Plugin pomPlugin = (Plugin) iterator.next();
 
                 if ("com.atlassian.maven.plugins".equals(pomPlugin.getGroupId()))
                 {
                     for (String type : possiblePluginTypes)
                     {
                         if (("maven-" + type + "-plugin").equals(pomPlugin.getArtifactId()))
                         {
                             return pomPlugin.getArtifactId();
                         }
                     }
                 }
             }
         }
         return null;
 
     }
 }
