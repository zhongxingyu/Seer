 package com.cedarsoft.maven.instrumentation.plugin;
 
 import com.cedarsoft.annotations.NonBlocking;
 import com.cedarsoft.annotations.instrumentation.NonBlockingAnnotationTransformer;
 import com.cedarsoft.annotations.instrumentation.NonNullAnnotationTransformer;
 import com.cedarsoft.annotations.instrumentation.NonNullGuavaAnnotationTransformer;
 import com.cedarsoft.annotations.instrumentation.ThreadAnnotationTransformer;
 import com.cedarsoft.annotations.meta.ThreadDescribingAnnotation;
 import com.cedarsoft.maven.instrumentation.plugin.util.ClassFile;
 import com.cedarsoft.maven.instrumentation.plugin.util.ClassFileLocator;
 import com.google.common.base.Preconditions;
 import com.google.common.io.Files;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.File;
 import java.io.IOException;
 import java.lang.instrument.ClassFileTransformer;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public abstract class AbstractInstrumentationMojo extends AbstractMojo {
   /**
    * The fully qualified class names of the transformers to apply.
    */
   @Parameter( required = false )
   @Nullable
   protected List<String> classTransformers;
   /**
    * The maven project
    */
   @Parameter(required = true, readonly = true, property = "project")
   protected MavenProject mavenProject;
 
   /**
    */
   @Parameter(required = true, readonly = true, property = "project.build.directory")
   protected File buildDirectory;
 
   /**
    * Whether to add the null checks
    */
   @Parameter( required = false, defaultValue = "true", property = "addNullChecks" )
   protected boolean addNullChecks = true;
 
   /**
    * Whether to use Guava for null checks.
    * If this property is set to "true"
    * {@link Preconditions#checkNotNull(Object)} statements are inserted.
    */
   @Parameter( required = false, defaultValue = "false", property = "useGuava" )
   protected boolean useGuava;
 
   /**
    * Whether thread verification code is added for all methods that are annotated
    * with an {@link ThreadDescribingAnnotation} annotation.
    */
   @Parameter( required = false, defaultValue = "true", property = "addThreadVerifications" )
   protected boolean addThreadVerifications = true;
 
   /**
    * Whether to insert verification code for methods annotated with {@link NonBlocking}.
    */
   @Parameter( required = false, defaultValue = "true", property = "addNonBlockingVerifications" )
   protected boolean addNonBlockingVerifications = true;
 
   @Nonnull
   protected File getLastInstrumentationDateFile() throws IOException {
     return new File(getWorkingDir(), "last_" + getGoal() + "_at");
   }
 
   /**
    *
    * @return
    */
   @Nonnull
   protected abstract String getGoal();
 
   @Nonnull
   private File getWorkingDir() throws IOException {
     File dir = new File(buildDirectory, "instrumentation-plugin");
     if (!dir.isDirectory()) {
       if (!dir.mkdir()) {
         throw new IOException("Could not create directory <" + dir + ">");
       }
     }
     return dir;
   }
 
 
   private void performClassTransformation(@Nonnull final Iterable<? extends ClassFile> classFiles, @Nonnull final Iterable<? extends ClassFileTransformer> agents) throws MojoExecutionException {
     for (final ClassFile classFile : classFiles) {
       for (final ClassFileTransformer agent : agents) {
         transformClass(classFile, agent);
       }
     }
   }
 
   private void transformClass(@Nonnull final ClassFile classFile, @Nonnull final ClassFileTransformer agent) throws MojoExecutionException {
     getLog().debug( "Transforming " + classFile.getClassName() + " using " + agent );
     try {
       classFile.transform( agent );
     } catch ( final ClassTransformationException e ) {
       final String message = MessageFormat.format( "Failed to transform class: {0}, using ClassFileTransformer, {1}", classFile, agent.getClass() );
       throw new MojoExecutionException( message, e );
     }
   }
 
   @Nonnull
   private static ClassFileTransformer createAgentInstance(@Nonnull final String className) throws MojoExecutionException {
     final Class<?> agentClass = resolveClass(className);
     if (!ClassFileTransformer.class.isAssignableFrom(agentClass)) {
       final String message = className + "is not an instance of " + ClassFileTransformer.class;
       throw new MojoExecutionException(message);
     }
     return toClassFileTransformerInstance(agentClass);
   }
 
   @Nonnull
   private static ClassFileTransformer toClassFileTransformerInstance(
     final Class<?> agentClass) throws MojoExecutionException {
     try {
       return (ClassFileTransformer) agentClass.getConstructor().newInstance();
     } catch (final InstantiationException e) {
       throw new MojoExecutionException("Failed to instantiate class: " + agentClass + ". Does it have a no-arg constructor?", e);
     } catch (final IllegalAccessException e) {
       throw new MojoExecutionException(agentClass + ". Does not have a public no-arg constructor?", e);
     } catch (NoSuchMethodException e) {
       throw new MojoExecutionException("Failed to instantiate class: " + agentClass + ". Does it have a no-arg constructor", e);
     } catch (InvocationTargetException e) {
       throw new MojoExecutionException("Failed to instantiate class: " + agentClass + ". Could not invoke constructor", e);
     }
   }
 
   @Nonnull
   private static Class<?> resolveClass(@Nonnull final String className) throws MojoExecutionException {
     try {
       return Class.forName(className);
     } catch (final ClassNotFoundException e) {
       final String message = MessageFormat.format("Could not find class: {0}. Is it a registered dependency of the project or the plugin?", className);
       throw new MojoExecutionException(message, e);
     }
   }
 
   @Nonnull
   protected MavenProject getProject() {
     return mavenProject;
   }
 
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     if (getProject().getPackaging().equals("pom")) {
       getLog().debug( "Skipping because <" + getProject().getName() + "> is a pom project" );
       return;
     }
 
     getLog().info("Starting InstrumentationMojo - instrumenting <" + getOutputDirectory() + ">");
 
     File outputDirectory = getOutputDirectory();
     if (!outputDirectory.isDirectory()) {
       getLog().info("Canceling since " + outputDirectory + " does not exist");
       return;
     }
 
     final Collection<ClassFileTransformer> agents = getAgents();
     final Collection<? extends ClassFile> classFiles = createLocator().findClasses(outputDirectory);
 
     //Now filter the classes based upon the compile date
     try {
       performClassTransformation(filterInstrumented(classFiles), agents);
       storeInstrumentationDate(System.currentTimeMillis());
     } catch (IOException e) {
       throw new MojoFailureException("error accessing instrumentation date file", e);
     }
   }
 
   /**
    * This method removes all files that still have been instrumented (based upon the modification date)
    * @param classFiles all class files
    * @return only those class files that have not yet instrumented
    */
   @Nonnull
   private Collection<? extends ClassFile> filterInstrumented(@Nonnull Collection<? extends ClassFile> classFiles) throws IOException {
     long lastInstrumentationDate;
     try {
       lastInstrumentationDate = getLastInstrumentationDate();
       getLog().debug("last instrumentation date: " + lastInstrumentationDate);
     } catch (NoLastInstrumentationDateFoundException ignore) {
       getLog().info("Instrumenting all files");
       //No instrumentation has yet happened, therefore return the complete list
       return classFiles;
     }
 
     List<ClassFile> unInstrumented = new ArrayList<ClassFile>();
 
     for (ClassFile classFile : classFiles) {
       long modificationDate = classFile.getClassFile().lastModified();
 
       if (modificationDate > lastInstrumentationDate) {
         unInstrumented.add(classFile);
       }else {
         getLog().debug("\tSkipping: " + classFile.getClassName() + "\t\t(" + modificationDate + ")");
       }
     }
 
     getLog().info("Instrumenting " + unInstrumented.size() + "/" + classFiles.size() + " files");
     return unInstrumented;
   }
 
   /**
    * Returns the last instrumentation date
    * @return the last instrumentation Date
    * @throws IOException
    */
   private long getLastInstrumentationDate() throws NoLastInstrumentationDateFoundException, IOException {
     File dateFile = getLastInstrumentationDateFile();
     if (!dateFile.exists()) {
       throw new NoLastInstrumentationDateFoundException();
     }
     return Long.parseLong(new String(Files.toByteArray(dateFile)));
   }
 
   /**
    * Stores the current time to the instrumentation date file
    * @param date the date
    * @throws IOException
    */
   private void storeInstrumentationDate(long date) throws IOException {
     File dateFile = getLastInstrumentationDateFile();
     Files.write(Long.toString(date).getBytes(), dateFile);
     getLog().debug("Stored last instrumentation date to <" + dateFile.getAbsolutePath() + ">: " + date);
   }
 
   @Nonnull
   protected abstract File getOutputDirectory();
 
   @Nonnull
   private ClassLoader createClassLoader() throws MojoExecutionException {
     List<URL> urls = new ArrayList<URL>();
     for (String classpathElement : getClasspathElements()) {
       File file = new File(classpathElement);
       if (file.equals(getOutputDirectory())) {
         continue;
       }
 
       try {
         urls.add(file.toURI().toURL());
       } catch (MalformedURLException e) {
         throw new MojoExecutionException("Could not convert <" + classpathElement + "> to url", e);
       }
     }
 
     return new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
   }
 
   @Nonnull
   protected abstract Iterable<? extends String> getClasspathElements();
 
   @Nonnull
   private Collection<ClassFileTransformer> getAgents() throws MojoExecutionException {
     final Collection<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();
 
     //First add the convenience transformers
     transformers.addAll( createConvenienceTransformers() );
 
     //Add the configured class transformers
     if ( classTransformers != null ) {
       for (final String className : classTransformers) {
         final ClassFileTransformer instance = createAgentInstance(className);
         transformers.add( instance );
       }
     }
     return transformers;
   }
 
   @Nonnull
   protected Collection<? extends ClassFileTransformer> createConvenienceTransformers(){
     try {
       final Collection<ClassFileTransformer> transformers = new ArrayList<ClassFileTransformer>();
 
       if ( isAddNullChecks() ) {
         if ( isUseGuava() ) {
          transformers.add( new NonNullAnnotationTransformer() );
        }else{
           transformers.add( new NonNullGuavaAnnotationTransformer() );
         }
       }
 
       if ( isAddThreadVerifications() ) {
         transformers.add( new ThreadAnnotationTransformer() );
       }
 
       if ( isAddNonBlockingVerifications() ) {
         transformers.add( new NonBlockingAnnotationTransformer() );
       }
 
       return transformers;
     } catch ( IOException e ) {
       throw new RuntimeException( e );
     }
   }
 
   @Nonnull
   private ClassFileLocator createLocator() throws MojoExecutionException {
     return new ClassFileLocator(getLog(), createClassLoader());
   }
 
   public boolean isUseGuava() {
     return useGuava;
   }
 
   public boolean isAddNullChecks() {
     return addNullChecks;
   }
 
   public boolean isAddNonBlockingVerifications() {
     return addNonBlockingVerifications;
   }
 
   public boolean isAddThreadVerifications() {
     return addThreadVerifications;
   }
 
   public static class NoLastInstrumentationDateFoundException extends Exception{
   }
 }
