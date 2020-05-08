 package org.consulo.lombok.pg.maven;
 
 import com.intellij.openapi.module.Module;
import org.consulo.lombok.pg.module.extension.LombokPgModuleExtension;
 import org.jetbrains.idea.maven.importing.MavenImporterFromDependency;
 import org.jetbrains.idea.maven.importing.MavenModifiableModelsProvider;
 import org.jetbrains.idea.maven.importing.MavenRootModelAdapter;
 import org.jetbrains.idea.maven.project.MavenProject;
 import org.jetbrains.idea.maven.project.MavenProjectChanges;
 import org.jetbrains.idea.maven.project.MavenProjectsProcessorTask;
 import org.jetbrains.idea.maven.project.MavenProjectsTree;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author VISTALL
  * @since 12.07.13.
  */
 public class LombokPgMavenImporter extends MavenImporterFromDependency {
   public LombokPgMavenImporter() {
     super("com.github.peichhorn", "lombok-pg");
   }
 
   @Override
   public void preProcess(Module module,
                          MavenProject mavenProject,
                          MavenProjectChanges mavenProjectChanges,
                          MavenModifiableModelsProvider mavenModifiableModelsProvider) {
 
   }
 
   @Override
   public void process(MavenModifiableModelsProvider mavenModifiableModelsProvider,
                       Module module,
                       MavenRootModelAdapter mavenRootModelAdapter,
                       MavenProjectsTree mavenProjectsTree,
                       MavenProject mavenProject,
                       MavenProjectChanges mavenProjectChanges,
                       Map<MavenProject, String> mavenProjectStringMap,
                       List<MavenProjectsProcessorTask> mavenProjectsProcessorTasks) {
    enableModuleExtension(module, mavenModifiableModelsProvider, LombokPgModuleExtension.class);
   }
 }
