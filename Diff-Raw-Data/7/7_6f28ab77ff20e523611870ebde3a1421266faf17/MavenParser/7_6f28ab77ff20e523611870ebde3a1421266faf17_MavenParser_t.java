 package org.whitesource.bamboo.agent;
 
 import java.io.File;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.embedder.MavenEmbedder;
 import org.apache.maven.embedder.MavenEmbedderException;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuildingException;
 
 import com.google.common.collect.Sets;
 
 public class MavenParser
 {
     private static final Logger log = Logger.getLogger(MavenParser.class);
     public static final String DEFAULT_MAVEN_POM = "pom.xml";
     private final MavenEmbedder mavenEmbedder;
     private MavenProject mavenProject;
 
     public MavenParser()
     {
         mavenEmbedder = new MavenEmbedder();
         mavenEmbedder.setClassLoader(Thread.currentThread().getContextClassLoader());
     }
 
     public void parseProject(File file) throws MavenEmbedderException, ProjectBuildingException
     {
         mavenEmbedder.start();
         mavenProject = mavenEmbedder.readProject(file);
     }
 
     protected Set<MavenProject> getModules(MavenProject mavenProject)
     {
         Set<MavenProject> modules = Sets.newHashSet();
 
         // recursively add child modules
         for (Object module : mavenProject.getModules())
         {
             String name = (String) module;
             File pom = new File(mavenProject.getFile().getParent(), name + File.separator + DEFAULT_MAVEN_POM);
             try
             {
                 MavenProject project = mavenEmbedder.readProject(pom);
                modules.add(project);
                 modules.addAll(getModules(project));
             }
             catch (ProjectBuildingException e)
             {
                 log.warn("Can't read POM for module " + name, e);
             }
         }
 
         return modules;
     }
 
     protected Set<Artifact> getArtifacts(MavenProject mavenProject)
     {
         Set<Artifact> artifacts = Sets.newHashSet(mavenProject.getArtifact());
         artifacts.addAll(mavenProject.getArtifacts());
 
         return artifacts;
     }
 
     protected Set<Dependency> getDependencies(final MavenProject mavenProject)
     {
         Set<Dependency> dependencies = Sets.newHashSet();
         final List<Dependency> mavenProjectDependencies = mavenProject.getDependencies();
         if (mavenProjectDependencies != null)
         {
             dependencies.addAll(mavenProjectDependencies);
         }
 
         return dependencies;
     }
 
     protected Set<Artifact> getDependencyArtifacts(MavenProject mavenProject)
     {
         Set<Artifact> dependencyArtifacts = Sets.newHashSet();
         final Set<Artifact> mavenProjectDependencyArtifacts = mavenProject.getDependencyArtifacts();
         if (mavenProjectDependencyArtifacts != null)
         {
             dependencyArtifacts.addAll(mavenProjectDependencyArtifacts);
         }
 
         return dependencyArtifacts;
     }
 
     public MavenProject getMavenProject()
     {
         return mavenProject;
     }
 }
