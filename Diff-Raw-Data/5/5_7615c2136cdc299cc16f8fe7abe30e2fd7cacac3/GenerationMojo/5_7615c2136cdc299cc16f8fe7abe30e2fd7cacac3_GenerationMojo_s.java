 package net.sf.sido.gen.maven;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.sido.gen.GenerationConfiguration;
 import net.sf.sido.gen.GenerationInput;
 import net.sf.sido.gen.GenerationTool;
 import net.sf.sido.gen.model.GenerationListener;
 import net.sf.sido.gen.model.GenerationOutput;
 import net.sf.sido.gen.model.support.DirectoryGenerationOutput;
 import net.sf.sido.gen.model.support.MapOptions;
 import net.sf.sido.gen.support.FileGenerationInput;
 import net.sf.sido.gen.support.GenerationConfigurationBuilder;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.SimpleSourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 
 /**
  * Generation of code from a SiDO schema.
  *
  * @goal generate
  * @phase generate-sources
  * @requiresDependencyResolution runtime
  * @requiresProject true
  */
 public class GenerationMojo extends AbstractMojo {
 
     /**
      * Project the plug-in executes into.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 	
 	/**
 	 * ID of the generation model.
 	 * 
 	 * @parameter expression="${sido.model}"
      * @required
 	 */
 	private String model;
 
     /**
      * Specifies the directory containing SiDOL files.
      *
      * @parameter expression="${sido.source.dir}" default-value="${basedir}/src/main/sido"
      * @required
      */
     private File sourceDirectory;
 
     /**
      * Location for generated Java files.
      *
      * @parameter expression="${sido.output.dir}" default-value="${project.build.directory}/generated-sources/sido"
      * @required
      */
     private File outputDirectory;
 
     /**
      * Location for generated registration files.
      *
      * @parameter expression="${sido.registration.dir}" default-value="${project.build.directory}/generated-sources/sido-resources"
      */
     private File registrationDirectory;
     
     /**
      * Flag used to indicate to generate the sources and resources in the test scope, instead
      * of the normal compilation scope
      * 
      * @parameter expression="${sido.test}" default-value="false"
      */
     private boolean test = false;
     
     /**
      * Options for the generation model.
      * 
      * @parameter
      */
     private Map<String, String> options = Collections.emptyMap();
 
 	@Override
 	public void execute() throws MojoExecutionException, MojoFailureException {
 
         // Summary
 		log("SiDO generation.");
     	log("Model: %s", model);
 
         // Gets all SiDOL files in the source directory
         Set<File> sidolFiles;
         try {
             SourceInclusionScanner scan = new SimpleSourceInclusionScanner(
                     Collections.singleton("**/*.sidol"),
                     Collections.emptySet());
             scan.addSourceMapping(new SuffixMapping(".sidol", Collections.<String>emptySet()));
             @SuppressWarnings("unchecked")
 			Set<File> includedSources = scan.getIncludedSources(sourceDirectory, null);
 			sidolFiles = includedSources;
         } catch (InclusionScanException ex) {
             throw new MojoExecutionException("Cannot get the list of SiDOL files", ex);
         }
         if (sidolFiles.isEmpty()) {
             log("No SiDOL file has been found in %s", sourceDirectory);
             return;
         }
         if (getLog().isInfoEnabled()) {
             log("List of files to include:");
             for (File sidolFile : sidolFiles) {
                 log(" * %s", sidolFile.getPath());
             }
         }
 
         // New class loading space
         ClassLoader formerClassLoader = Thread.currentThread().getContextClassLoader();
         List<URL> urls = new ArrayList<URL>();
         try {
             // Add all the artifacts
             Set<Artifact> artifacts = project.getArtifacts();
             log("Adding all artifacts to the plug-in class path:");
             for (Artifact artifact : artifacts) {
                 URL artifactURL = artifact.getFile().toURI().toURL();
                 log(" * %s [%s]", artifactURL, artifact.getScope());
                 urls.add(artifactURL);
             }
         } catch (Exception ex) {
             throw new MojoExecutionException("Cannot set the classpath for the dependencies", ex);
         }
         URLClassLoader depLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), formerClassLoader) {
             @Override
             protected Class<?> findClass(String name) throws ClassNotFoundException {
                 log("[CL] findClass: %s", name);
                 return super.findClass(name);
             }
 
             @Override
             public URL findResource(String name) {
                 log("[CL] findResource: %s", name);
                 return super.findResource(name);
             }
         };
         
         // Inputs
         Collection<GenerationInput> sidolInputs = Collections2.transform(sidolFiles, new Function<File, GenerationInput>() {
         	@Override
         	public GenerationInput apply(File file) {
         		return new FileGenerationInput(file);
         	}
 		});
         
         // Output
         GenerationOutput output = new DirectoryGenerationOutput(outputDirectory);
 
         // Starting the generation in a specific classloader
         log("Former class loader is: %s", formerClassLoader);
         try {
             // Make the child realm the ContextClassLoader
             log("Using class loader: %s", depLoader);
             Thread.currentThread().setContextClassLoader(depLoader);
             log("Actual class loader set: %s", Thread.currentThread().getContextClassLoader());
 
             // Instantiates the tool
             GenerationTool tool = new GenerationTool();
 
             // Configuration
             GenerationConfigurationBuilder configurationBuilder = 
             		GenerationConfigurationBuilder.create()
             			.modelId(model)
             			.sources(sidolInputs)
             			.output(output)
             			.options(new MapOptions(options));
             if (registrationDirectory != null) {
             	configurationBuilder.registrationOutput(new DirectoryGenerationOutput(registrationDirectory));
             }
             GenerationConfiguration configuration = configurationBuilder            
             			.build();
 
             // Listener
             GenerationListener toolListener = new GenerationListener() {
 				
 				@Override
 				public void log(String pattern, Object... params) {
                     GenerationMojo.this.log(pattern, params);
 				}
 			};
 
             // Generation
             try {
 				tool.generate(configuration, toolListener);
 			} catch (IOException e) {
 				throw new MojoExecutionException("Cannot proceed with generation", e);
 			}
 
             // Adds the output directory as sources
             if (test) {
             	project.addTestCompileSourceRoot(outputDirectory.getPath());
             } else {
             	project.addCompileSourceRoot(outputDirectory.getPath());
             }
 
             // Services directory as resources
 			Resource resource = new Resource();
 			resource.setDirectory(registrationDirectory.getPath());
 			resource.setFiltering(false);
 			if (test) {
 				project.addTestResource(resource);
 			} else {
 				project.addResource(resource);
 			}
 
         } finally {
             // Restores the class path
             Thread.currentThread().setContextClassLoader(formerClassLoader);
         }
 	}
 
     protected void log(String pattern, Object... params) {
         getLog().info(String.format(pattern, params));
     }
 
 }
