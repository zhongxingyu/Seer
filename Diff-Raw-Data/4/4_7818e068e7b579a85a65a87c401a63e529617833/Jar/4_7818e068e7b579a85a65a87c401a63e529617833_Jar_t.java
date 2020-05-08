 package com.github.wolfie.bob.action;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarEntry;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 import com.github.wolfie.bob.Bob;
 import com.github.wolfie.bob.Defaults;
 import com.github.wolfie.bob.Util;
 import com.github.wolfie.bob.exception.InternalConsistencyException;
 import com.github.wolfie.bob.exception.NoManifestFileFoundException;
 import com.github.wolfie.bob.exception.NoSourcesToIncludeException;
 import com.github.wolfie.bob.exception.NotAReadableDirectoryException;
 import com.github.wolfie.bob.exception.ProcessingException;
 
 /**
  * <p>
  * Package the project into a JAR file.
  * </p>
  * 
  * <h1>Assumptions</h1>
  * <p>
  * <i>There are no assumptions.</i>
  * </p>
  * 
  * <h1>Conventions</h1>
  * 
  * <ul>
  * <li>The classes are compiled from a default instance of {@link Compilation}</li>
  * <li>The resulting file is <tt>artifacts/build.jar</tt></li>
  * <li>Sources are not included</li>
  * <li>Manifest file is taken from <tt>META-INF/MANIFEST.MF</tt>, if exists</li>
  * </ul>
  * 
  * @author Henrik Paul
  * @since 1.0.0
  */
 public class Jar implements Action {
   
   protected Compilation fromCompilation;
   private String fromPath;
   protected String toPath;
   protected String manifestPath;
   private boolean sourcesFromChain;
   private String sourcesFromPath;
   
   /**
    * Where in the Java archive the classes and sources will be placed. JAR add
    * files in the root by default.
    */
   protected String archiveClassSourceDestination = "";
   
   @Override
   public final void process() {
     setDefaults();
     
     // A map from jar entry name to file representation
     final Map<String, File> entryMap = new HashMap<String, File>();
     
     final File classesDir = getClassesDirectory();
     
     System.out.println("Finding classfiles from "
         + classesDir.getAbsolutePath());
     final Collection<File> classFiles = Util.getFilesRecursively(classesDir,
         Util.JAVA_CLASS_FILE);
     for (final File classFile : classFiles) {
       final String entryName = archiveClassSourceDestination
           + Util.relativeFileName(classesDir, classFile);
       System.out.println(entryName + " <- " + classFile.getAbsolutePath());
       entryMap.put(entryName, classFile);
     }
     
     try {
       final File sourcesDir = getSourcesDirectory();
       System.out
           .println("Finding sources from " + sourcesDir.getAbsolutePath());
       final Collection<File> sourceFiles = Util.getFilesRecursively(
           sourcesDir, Util.JAVA_SOURCE_FILE);
       for (final File sourceFile : sourceFiles) {
         final String entryName = archiveClassSourceDestination
             + Util.relativeFileName(sourcesDir, sourceFile);
         System.out.println(entryName + " <- " + sourceFile.getAbsolutePath());
         entryMap.put(entryName, sourceFile);
       }
     } catch (final NoSourcesToIncludeException e) {
       // okay, fine, no sources then.
     }
     
     // let subclasses add their own files.
     subClassProcessHook(entryMap);
     
     try {
       final File destination = getDestination();
       
       final FileOutputStream fileOutputStream = new FileOutputStream(
           destination);
       
       // can't be final, since Java doesn't understand the structure of the
       // try/catch
       JarOutputStream jarOutputStream;
       
       try {
         final File manifestFile = getManifestFile();
         final Manifest manifest = new Manifest(
             new FileInputStream(manifestFile));
         manifest.getMainAttributes().put(new Attributes.Name("Created-By"),
             Bob.getVersionString());
         jarOutputStream = new JarOutputStream(fileOutputStream, manifest);
         
       } catch (final NoManifestFileFoundException e) {
         final Manifest emptyManifest = new Manifest();
         emptyManifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,
             "1");
         emptyManifest.getMainAttributes().put(
             new Attributes.Name("Created-By"), Bob.getVersionString());
         jarOutputStream = new JarOutputStream(fileOutputStream, emptyManifest);
       }
       
       // since the manifest will be handled by the JarOutputStream, remove the
       // duplicate entry for that.
       entryMap.remove("META-INF/MANIFEST.MF");
       
       /*
        * To make the Jar file look neater, let's sort the entries. Just for
        * shits'n'giggles.
        */
       final List<String> entries = new ArrayList<String>(entryMap.keySet());
       Collections.sort(entries);
       
       for (final String entry : entries) {
         final File file = entryMap.get(entry);
         add(entry, file, jarOutputStream);
       }
       
       jarOutputStream.close();
       
       System.out.println("Wrote " + destination.getAbsolutePath());
     } catch (final Exception e) {
       throw new ProcessingException(e);
     }
   }
   
   /**
    * <p>
    * A method to provide subclasses a hook for adding their own files into the
    * Jar-format file.
    * </p>
    * 
    * <p>
    * <strong>Note:</strong> All overriding methods must, at some point, call the
    * method in the superclass.</em>
    * </p>
    * 
    * <p>
    * <strong>Note:</strong> To avoid overwriting existing entries, prefer
    * {@link Util#putIfNotExists(Object, Object, Map)}
    * </p>
    * 
    * @param filesToPackage
    *          A mutable {@link Map} of <em>Jar entry name</em> &rarr;
    *          <em>{@link File}
    *          instance</em>.
    */
   protected void subClassProcessHook(final Map<String, File> filesToPackage) {
     // nothing to implement here.
   }
   
   private File getSourcesDirectory() throws NoSourcesToIncludeException {
     if (sourcesFromPath != null) {
       return new File(sourcesFromPath);
     } else if (sourcesFromChain) {
       if (fromCompilation != null) {
         return fromCompilation.getSourceDirectory();
       } else {
         System.out
             .println("Although requested, no sources will be included, since " +
                 "there are no available chains to take sources from.");
         throw new NoSourcesToIncludeException();
       }
     } else {
       throw new NoSourcesToIncludeException();
     }
   }
   
   private File getManifestFile() throws NoManifestFileFoundException {
     final File manifestFile = new File(manifestPath);
     if (manifestFile.exists() && manifestFile.canRead()) {
       System.out.println("Using manifest file from "
           + manifestFile.getAbsolutePath());
       return manifestFile;
     } else {
       System.out.println("Could not include manifest from file "
           + manifestFile.getAbsolutePath());
       throw new NoManifestFileFoundException();
     }
   }
   
   /**
    * Setting the defaults just before starting to process the action itself.
    */
   protected void setDefaults() {
     if (fromCompilation == null && fromPath == null) {
       fromCompilation = new Compilation();
     }
     
     if (manifestPath == null) {
       manifestPath = Defaults.JAR_MANIFEST_PATH;
     }
     
     // no sources added by default
     
     if (toPath == null) {
       toPath = Defaults.JAR_PATH;
     }
   }
   
   private File getDestination() {
     if (toPath != null) {
       final File destination = new File(toPath);
       
       if (destination.exists()) {
         Util.delete(destination);
       }
       
       final File parentFile = destination.getParentFile();
       if (!parentFile.exists()) {
         Util.createDir(parentFile);
       }
       
       return destination;
       
     } else {
       throw new InternalConsistencyException("No destination path defined");
     }
   }
   
   /**
    * Resolve the directory in which the classes will be found.
    * 
    * @return a {@link File} representing the directory containing the classfiles
    * @throws NotAReadableDirectoryException
    *           if the resulting directory is not, in fact, a readable directory
    *           at all.
    */
   private File getClassesDirectory() {
     if (fromCompilation != null) {
       return getClassesDirectoryFromCompilation(fromCompilation);
     } else if (fromPath != null) {
       return getClassesDirectoryFromPath(fromPath);
     } else {
       throw new InternalConsistencyException("No class source defined");
     }
   }
   
   /**
    * @param path
    * @return
    * @throws NotAReadableDirectoryException
    *           if the resulting directory is not, in fact, a readable directory
    *           at all.
    */
   private static File getClassesDirectoryFromPath(final String path) {
     return Util.checkedDirectory(new File(path));
   }
   
   /**
    * @param compilation
    * @return
    * @throws NotAReadableDirectoryException
    *           if the resulting directory is not, in fact, a readable directory
    *           at all.
    */
   private static File getClassesDirectoryFromCompilation(
       final Compilation compilation) {
     try {
       compilation.to(Util.getTemporaryDirectory().getAbsolutePath());
       compilation.process();
       
       final File directory = compilation.getDestinationDirectory();
       return Util.checkedDirectory(directory);
     } catch (final IOException e) {
      throw new ProcessingException("Could not create a temporary directory.", e);
     }
   }
   
   /**
    * See <a href="http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file"
    * >Stack Overflow</a> for explanation on method.
    * 
    * @param baseDir
    * @param source
    *          The {@link File} to add to the Jar.
    * @param archiveDestinationPrefix
    * @param target
    *          The Jar's {@link JarOutputStream}.
    * @throws IOException
    */
   protected static void add(final String entryName, final File source,
       final JarOutputStream target) throws IOException {
     
     System.out.println("Compressing " + entryName);
     
     final JarEntry entry = new JarEntry(entryName);
     entry.setTime(source.lastModified());
     target.putNextEntry(entry);
     
     final BufferedInputStream in = new BufferedInputStream(new FileInputStream(
         source));
     
     try {
       final byte[] buffer = new byte[1024];
       while (true) {
         final int count = in.read(buffer);
         if (count == -1) {
           break;
         }
         target.write(buffer, 0, count);
       }
       target.closeEntry();
     } finally {
       in.close();
     }
   }
   
   /**
    * Get classes to package from a {@link Compilation}
    * 
    * @param compilation
    * @return <code>this</code>
    * @throws IllegalStateException
    *           if any <tt>from()</tt> method was previously called
    */
   public Jar from(final Compilation compilation) {
     if (fromPath == null && fromCompilation == null) {
       fromCompilation = compilation;
       return this;
     } else {
       throw new IllegalStateException("from was already set, cannot reset");
     }
   }
   
   /**
    * Get classes to package from the filesystem
    * 
    * @param path
    *          The path at which the classes will be found.
    * @return <code>this</code>
    * @throws IllegalStateException
    *           if any <tt>from()</tt> method was previously called
    */
   public Jar from(final String path) {
     if (fromPath == null && fromCompilation == null) {
       fromPath = path;
       return this;
     } else {
       throw new IllegalStateException("from was already set, cannot reset");
     }
   }
   
   /**
    * The path of the resulting jar file
    * 
    * @param path
    *          a file path
    * @return <code>this</code>
    */
   public Jar to(final String path) {
     toPath = path;
     return this;
   }
   
   /**
    * Use a manifest file from an explicit path.
    * 
    * @param path
    *          the file path to the manifest file.
    * @return <code>this</code>
    */
   public Jar withManifestFrom(final String path) {
     manifestPath = path;
     return this;
   }
   
   /**
    * Include sources to the resulting jar from a chained supplier.
    * 
    * @return <code>this</code>
    * @throws IllegalStateException
    *           if {@link #withSourcesFrom(String)} was called previously.
    * @see #from(Compilation)
    */
   public Jar withSources() {
     if (sourcesFromPath == null) {
       sourcesFromChain = true;
       return this;
     } else {
       throw new IllegalStateException("Sources were already being taken from " +
           "a path. Can't take sources from two places at once");
     }
   }
   
   /**
    * Include sources to the resulting jar from the filesystem.
    * 
    * @param path
    *          The path on the filesystem where the source folder for the Jar
    *          package are found.
    * @return <code>this</code>
    * @throws IllegalStateException
    *           if {@link #withSources()} was called previously.
    */
   public Jar withSourcesFrom(final String path) {
     if (!sourcesFromChain) {
       sourcesFromPath = path;
       return this;
     } else {
       throw new IllegalStateException("Sources were already being taken " +
           "from the chain. Can't take sources from two places at once");
     }
   }
 }
