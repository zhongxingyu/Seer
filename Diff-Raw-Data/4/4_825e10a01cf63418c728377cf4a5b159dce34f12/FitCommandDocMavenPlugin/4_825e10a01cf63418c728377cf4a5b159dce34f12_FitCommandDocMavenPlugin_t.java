 package documentor;
 
 import core.ClassLoaderUtils;
import core.service.FileService;
 import docGenerator.model.DocPathNamePair;
 import docGenerator.services.DocGeneratorService;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @goal install
  * @phase process-classes
  * @requiresDependencyResolution compile+runtime
  */
 public class FitCommandDocMavenPlugin extends AbstractMojo {
 
   /**
    * Directory where compiled classes will be
    *
    * @parameter expression="${project.build.outputDirectory}"
    * @required
    * @readonly
    */
   private String outputDirectory;
   /**
    * Directory where compiled classes will be
    *
    * @parameter expression="${project.compileClasspathElements}"
    * @required
    * @readonly
    */
   private List<String> compileClasspathElements;
 
   /**
    * Target Directory
    *
    * @parameter expression="${project.build.directory}"
    * @required
    * @readonly
    */
   private String targetDirectory;
 
   /**
    * Artifact ID of compile project
    *
    * @parameter expression="${project.artifactId}"
    * @required
    * @readonly
    */
   private String artifactId;
 
   /**
    * @parameter
    */
   private String explicitDefinedOutputDirectory;
 
   private ClassLoader loader;
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     this.loader = ClassLoaderUtils.buildClassLoader(getLog(), this.getClass().getClassLoader(), compileClasspathElements);
 
     getLog().info("Loading classes from directory: " + outputDirectory);
     File outputDirectoryFile = new File(outputDirectory);
 
     if (outputDirectoryFile.exists()) {
       List<Class<?>> allClasses = ClassLoaderUtils.loadClassesRecursivelyFromDirectory(loader, getLog(), outputDirectoryFile, new ArrayList<Class<?>>());
       DocPathNamePair pair = buildDocGenDescription();
      DocGeneratorService docGeneratorService = new DocGeneratorService(new FileService());
       docGeneratorService.generateDocsByClasses(pair, allClasses);
     }
   }
 
 
   private DocPathNamePair buildDocGenDescription() {
     String usedResultDirectory = targetDirectory;
     if (explicitDefinedOutputDirectory != null) {
       usedResultDirectory = explicitDefinedOutputDirectory;
     }
     getLog().info("Using " + usedResultDirectory + " as ouput directory for documentation");
     File directoryFile = new File(usedResultDirectory);
     if (!directoryFile.exists()) {
       directoryFile.mkdir();
     }
     String resultFilePath = directoryFile.getAbsolutePath()
             + File.separator + artifactId + "FitCommandDocs";
 
     DocPathNamePair pair = new DocPathNamePair(outputDirectory,
             resultFilePath);
     return pair;
   }
 }
