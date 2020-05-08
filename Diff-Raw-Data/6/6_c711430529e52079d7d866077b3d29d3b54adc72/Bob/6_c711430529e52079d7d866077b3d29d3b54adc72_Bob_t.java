 package com.github.wolfie.bob;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.tools.Diagnostic;
 import javax.tools.DiagnosticCollector;
 import javax.tools.DiagnosticListener;
 import javax.tools.JavaCompiler;
 import javax.tools.JavaFileObject;
 import javax.tools.StandardJavaFileManager;
 import javax.tools.StandardLocation;
 import javax.tools.ToolProvider;
 
 import com.github.wolfie.bob.BuildFileUtil.TargetInfo;
 import com.github.wolfie.bob.CompilationCache.Builder;
 import com.github.wolfie.bob.Log.LogLevel;
 import com.github.wolfie.bob.action.Action;
 import com.github.wolfie.bob.action.optional.JavaLauncher;
 import com.github.wolfie.bob.annotation.Target;
 import com.github.wolfie.bob.exception.BuildTargetException;
 import com.github.wolfie.bob.exception.CompilationFailedException;
 import com.github.wolfie.bob.exception.IncompatibleReturnTypeException;
 import com.github.wolfie.bob.exception.NoBuildFileFoundException;
 import com.github.wolfie.bob.exception.NoDefaultBuildTargetMethodFoundException;
 import com.github.wolfie.bob.exception.SeveralDefaultBuildTargetMethodsFoundException;
 import com.github.wolfie.bob.exception.UnexpectedArgumentAmountException;
 import com.github.wolfie.bob.exception.UnrecognizedArgumentException;
 
 /**
  * Non-XML Builder
  * 
  * @author Henrik Paul
  * @since 1.0.0
  */
 public final class Bob {
   
   private static class BobDiagnosticListener implements
       DiagnosticListener<JavaFileObject> {
     private final List<Diagnostic<? extends JavaFileObject>> diagnostics = new ArrayList<Diagnostic<? extends JavaFileObject>>();
     
     @Override
     public void report(final Diagnostic<? extends JavaFileObject> diagnostic) {
       diagnostics.add(diagnostic);
     }
     
     public boolean hasProblems() {
       return !diagnostics.isEmpty();
     }
     
     public List<Diagnostic<? extends JavaFileObject>> getProblems() {
       return Collections.unmodifiableList(diagnostics);
     }
   }
   
   public static final int VERSION_MAJOR = 0;
   public static final int VERSION_MINOR = 0;
   public static final int VERSION_MAINTENANCE = 0;
   public static final String VERSION_BUILD = "alpha2";
   public static final String VERSION;
   
   private static final String CACHE_SYSTEM_PROPERTY = "cache.location";
   
   private static boolean showHelp = false;
   private static boolean skipBuilding = false;
   private static boolean success = false;
   private static boolean listTargets = false;
   
   /**
    * A path to the desired build file. Guaranteed to have a non-
    * <code>null</code> value
    */
   private static String buildfile = Defaults.DEFAULT_BUILD_SRC_PATH;
  private static boolean buildfileIsExplicit;
   
   /**
    * The name of the desired build method. Can be <code>null</code>, which means
    * a suitable default target needs to be found manually.
    */
   private static String buildtarget = null;
   
   /** The arguments given to the application as-is. */
   private static String[] rawArgs;
   
   static {
     if (VERSION_BUILD != null && !VERSION_BUILD.isEmpty()) {
       VERSION = String.format("%s.%s.%s-%s", VERSION_MAJOR, VERSION_MINOR,
           VERSION_MAINTENANCE, VERSION_BUILD);
     } else {
       VERSION = String.format("%s.%s.%s", VERSION_MAJOR, VERSION_MINOR,
           VERSION_MAINTENANCE);
     }
   }
   
   public static final void main(final String[] args) {
     try {
       Log.get().enter("Bob");
       handleArgs(args);
       
       if (!skipBuilding) {
         if (shouldBeBootstrapped()) {
           Log.get().enter("Boot");
           try {
             bootstrap();
           } finally {
             Log.get().exit();
           }
         } else {
           run();
         }
       }
       
       if (showHelp) {
         showHelp();
       }
       
       if (listTargets) {
         listTargets();
       }
       
       // TODO: switch over to logging
     } catch (final NoBuildFileFoundException e) {
       e.printStackTrace();
       System.err.println("Are you sure you're in the right directory?");
     } catch (final UnrecognizedArgumentException e) {
       System.err.println(e.getMessage());
     } catch (final UnexpectedArgumentAmountException e) {
       System.err.println(e.getMessage());
     } catch (final Exception e) {
       e.printStackTrace();
     }
     
     if (!skipBuilding) {
       if (success) {
         Log.get().log("Build successful", LogLevel.VERBOSE);
       } else {
         Log.get().log("Build FAILED!", LogLevel.SEVERE);
         System.exit(1);
       }
     } else {
       Log.get().log("Didn't build anything", LogLevel.VERBOSE);
     }
     Log.get().exit();
   }
   
   private static boolean shouldBeBootstrapped() {
     return !System.getProperties().containsKey(CACHE_SYSTEM_PROPERTY);
   }
   
   private static void run() {
     final BootstrapInfo info = getBootstrapInfo();
     
     CompilationCache.set(info.getCache());
     
     final File buildFile = getBuildFile();
    if (!buildfileIsExplicit) {
       Log.get().log("Buildfile " + buildfile, LogLevel.INFO);
     }
     
     final File buildClassFile = compile(buildFile, info.getClasspath());
     
     Class<? extends BobBuild> buildClass;
     try {
       buildClass = getBuildClass(buildClassFile);
     } catch (final ClassNotFoundException e) {
       throw new BootstrapError(buildClassFile.getAbsolutePath()
           + " didn't contain a valid build class", e);
     }
     
     final Method buildMethod = getBuildMethod(buildClass);
     final Action action = getAction(buildMethod, buildClass);
     
     if (action != null) {
       Log.get().log("Processing " + action, LogLevel.DEBUG);
       Log.get().enter(action.getClass().getSimpleName());
       try {
         action.process();
       } finally {
         Log.get().exit();
       }
       success = true;
     } else {
       throw new NullPointerException(String.format("%s.%s() returned null.",
           buildClass.getName(), buildMethod.getName()));
     }
   }
   
   private static Class<? extends BobBuild> getBuildClass(
       final File buildClassFile) throws ClassNotFoundException {
     
     try {
       final URLClassLoader urlClassLoader = new URLClassLoader(
           new URL[] { buildClassFile.toURI().toURL() });
       @SuppressWarnings("unchecked")
       final Class<? extends BobBuild> buildClass =
           (Class<? extends BobBuild>) urlClassLoader
               .loadClass(getBuildClassName());
       return buildClass;
     } catch (final MalformedURLException e) {
       throw new BootstrapError("This really shouldn't happen", e);
     }
   }
   
   /**
    * @throws BootstrapError
    *           if there was an exception during retrieving the
    *           {@link CompilationCache}.
    */
   private static BootstrapInfo getBootstrapInfo() {
     final String cacheFilePath = System.getProperty(CACHE_SYSTEM_PROPERTY);
     final File cacheFile = new File(cacheFilePath);
     
     try {
       return deserializeBootstrapInfoFromFile(cacheFile);
     } catch (final IOException e) {
       throw new BootstrapError(e);
     } catch (final ClassNotFoundException e) {
       throw new BootstrapError(e);
     }
   }
   
   /**
    * Bootstraps Bob
    * <p/>
    * This method will try to compile the project, and restart Bob with the
    * compiled project in its classpath. It will also build a
    * {@link CompilationCache} object, which will be serialized into a file. This
    * file will, in turn, be given to the rebooted Bob as a system property
    * <tt>{@value #CACHE_SYSTEM_PROPERTY}</tt>.
    * <p/>
    * This method will never return, but terminate before returning.
    */
   private static void bootstrap() {
     
     Log.get().log("Bootstrapping", LogLevel.DEBUG);
     
     final File buildFile = getBuildFile();
     
     final ProjectDescription desc = BuildFileUtil
           .getProjectDescription(buildFile);
     
     try {
       final BootstrapInfo info = compileAndGetBootstrapInfo(desc);
       final File serializedCache = serializeBootstrapInfoIntoFile(info);
       
       Log.get().log("Serialized compilation cache into "
           + serializedCache.getAbsolutePath(), LogLevel.DEBUG);
       
       final JavaLauncher bobRebooter = new JavaLauncher(Bob.class);
       final String jvmArg = String.format("-D%s=%s", CACHE_SYSTEM_PROPERTY,
           serializedCache.getAbsolutePath());
       bobRebooter.addJvmArg(jvmArg);
       
       bobRebooter.addAppArgs(rawArgs);
       
       for (final File classpathFile : info.getClasspath()) {
         bobRebooter
             .userProvidedForcedClassPath(classpathFile.getAbsolutePath());
       }
       
       Log.get().log("Rebooting Bob with " + bobRebooter, LogLevel.DEBUG);
       System.exit(bobRebooter.run());
     } catch (final IOException e) {
       // TODO: print via the logger instead.
       e.printStackTrace();
       System.exit(1);
     }
   }
   
   /**
    * @see #deserializeBootstrapInfoFromFile(File)
    */
   private static File serializeBootstrapInfoIntoFile(
       final BootstrapInfo info) throws IOException {
     
     final File tempFile = File.createTempFile("BobBootstrapInfo", null);
     final FileOutputStream fos = new FileOutputStream(tempFile);
     final ObjectOutputStream oos = new ObjectOutputStream(fos);
     oos.writeObject(info);
     oos.flush();
     oos.close();
     fos.flush();
     fos.close();
     
     return tempFile;
   }
   
   /**
    * @throws IOException
    * @throws ClassNotFoundException
    * @see #serializeBootstrapInfoIntoFile(BootstrapInfo)
    */
   private static BootstrapInfo deserializeBootstrapInfoFromFile(
       final File file) throws IOException, ClassNotFoundException {
     final FileInputStream fis = new FileInputStream(file);
     final ObjectInputStream ois = new ObjectInputStream(fis);
     return (BootstrapInfo) ois.readObject();
   }
   
   private static Action getAction(final Method buildMethod,
       final Class<? extends BobBuild> buildClass) {
     try {
       try {
         return (Action) buildMethod.invoke(null);
       } catch (final NullPointerException e) {
         return (Action) buildMethod.invoke(buildClass
             .newInstance());
       }
     } catch (final Exception e) {
       throw new BootstrapError(e);
     }
   }
   
   private static BootstrapInfo compileAndGetBootstrapInfo(
       final ProjectDescription desc)
       throws IOException {
     final Collection<File> classPath = getClassPath(desc);
     final File classOutputDir = Util.getTemporaryDirectory();
     classPath.add(classOutputDir);
     
     final Builder cacheBuilder = new CompilationCache.Builder(classOutputDir);
     
     for (final String sourcePath : desc.getSourcePaths()) {
       final Touple<Set<File>, Set<URI>> touple = compile(sourcePath,
           classPath, classOutputDir);
       
       cacheBuilder.add(sourcePath, touple.getFirst(), touple.getSecond());
     }
     
     final BootstrapInfo info = new BootstrapInfo(cacheBuilder.commit(),
         classPath);
     return info;
   }
   
   /**
    * 
    * @param sourcePath
    * @param classPath
    * @param classOutputDir
    * @return A {@link Touple} of source files to their respective class file
    *         URIs
    * @throws IOException
    */
   private static Touple<Set<File>, Set<URI>> compile(
       final String sourcePath,
       final Iterable<File> classPath, final File classOutputDir)
       throws IOException {
     
     final Set<File> sourceFiles = Util.getFilesRecursively(new File(
         sourcePath), Util.JAVA_SOURCE_FILE);
     
     final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
     
     final BobDiagnosticListener diagnosticListener = new BobDiagnosticListener();
     final BobWrappedJavaFileManager fileManager = new BobWrappedJavaFileManager(
         compiler.getStandardFileManager(diagnosticListener, null, null));
     fileManager.setLocation(StandardLocation.CLASS_PATH, classPath);
     fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections
         .singleton(classOutputDir));
     final Iterable<? extends JavaFileObject> sourceFileObjects = fileManager
         .getJavaFileObjectsFromFiles(sourceFiles);
     compiler.getTask(null, fileManager, diagnosticListener, null, null,
         sourceFileObjects).call();
     
     if (diagnosticListener.hasProblems()) {
       for (final Diagnostic<? extends JavaFileObject> problem : diagnosticListener
           .getProblems()) {
         System.err.println(problem);
       }
       System.exit(1);
       return Touple.ofNull();
     } else {
       return Touple.of(sourceFiles, fileManager
           .getJavaFileURIs());
     }
   }
   
   private static Collection<File> getClassPath(final ProjectDescription desc) {
     final Collection<File> jarFiles = new HashSet<File>();
     
     for (final String jarFileName : desc.getJarFiles()) {
       final File jarFile = new File(jarFileName);
       if (jarFile.canRead()) {
         Log.get().log("Added " + jarFile.getAbsolutePath(), LogLevel.DEBUG);
         jarFiles.add(jarFile);
       } else {
         Log.get().log("Could not be read: " + jarFile.getAbsolutePath(),
             LogLevel.WARNING);
       }
     }
     
     for (final String jarPathName : desc.getJarPaths()) {
       final File jarPath = new File(jarPathName);
       if (jarPath.canRead() && jarPath.isDirectory()) {
         Log.get().log("Adding all jars from " + jarPath.getAbsolutePath(),
             LogLevel.DEBUG);
         for (final File jarFile : jarPath.listFiles()) {
           if (jarFile.getName().endsWith(".jar")) {
             Log.get().log("Added " + jarFile.getName(), LogLevel.DEBUG);
             jarFiles.add(jarFile);
           }
         }
       } else if (!desc.isJarPathOptional(jarPathName)) {
         Log.get().log("Could not read directory "
             + jarPath.getAbsolutePath(), LogLevel.WARNING);
       }
     }
     
     return jarFiles;
   }
   
   /**
    * <p>
    * Get the build method to call in the build class file.
    * </p>
    * 
    * <p>
    * If nothing else is specified, the chosen method will be, in the following
    * priority:
    * </p>
    * 
    * <ol>
    * <li>A method annotated as "
    * <code>@{@link Target}(defaultTarget = true)</code>"</li>
    * <li>The method <tt>build()</tt>, if appropriately annotated with
    * {@link Target}.
    * </ol>
    * 
    * @param buildClass
    *          The {@link Class} in which to look for said methods
    * @return The build {@link Method}.
    * @throws NoDefaultBuildTargetMethodFoundException
    *           If no suitable build method is found in <tt>buildClass</tt>.
    * @throws SeveralDefaultBuildTargetMethodsFoundException
    *           If <tt>buildClass</tt> has more than one method annotated as a
    *           default target.
    * @throws IncompatibleReturnTypeException
    *           If any defined build target method in <tt>buildClass</tt> returns
    *           something other than an implementation of {@link Action}.
    */
   private static Method getBuildMethod(final Class<?> buildClass)
       throws NoDefaultBuildTargetMethodFoundException,
       SeveralDefaultBuildTargetMethodsFoundException {
     
     final Method method;
     if (buildtarget != null) {
       method = getBuildTarget(buildClass, buildtarget);
     } else {
       method = getDefaultBuildTarget(buildClass);
       Log.get().log("Using " + method.getName() + " as build target",
           LogLevel.INFO);
     }
     
     return Util.verifyBuildTargetMethod(method);
   }
   
   private static Method getBuildTarget(final Class<?> buildClass,
       final String buildtarget) {
     try {
       return buildClass.getMethod(buildtarget);
     } catch (final SecurityException e) {
       throw new BuildTargetException("The JVM didn't allow access to target "
           + buildtarget + ".", e);
     } catch (final NoSuchMethodException e) {
       throw new BuildTargetException("No target by the name " + buildtarget
           + " was found", e);
     }
   }
   
   private static Method getDefaultBuildTarget(final Class<?> buildClass) {
     Method buildMethod = null;
     
     for (final Method method : buildClass.getMethods()) {
       
       final Target targetAnnotation = method.getAnnotation(Target.class);
       if (targetAnnotation != null) {
         if (targetAnnotation.defaultTarget()) {
           
           if (buildMethod == null) {
             buildMethod = method;
           } else {
             throw new SeveralDefaultBuildTargetMethodsFoundException(buildClass);
           }
         }
         
       }
     }
     
     if (buildMethod != null) {
       return buildMethod;
     } else {
       throw new NoDefaultBuildTargetMethodFoundException(buildClass);
     }
   }
   
   private static String getBuildClassName() {
     return buildfile.substring(buildfile.lastIndexOf(File.separator) + 1,
         buildfile.lastIndexOf("."));
   }
   
   private static File compile(final File buildFile,
       final Collection<File> classpath)
       throws CompilationFailedException {
     
     final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
     
     try {
       final File tempDir = Util.getTemporaryDirectory();
       
       final DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<JavaFileObject>();
       final StandardJavaFileManager fileManager = compiler
           .getStandardFileManager(diagnosticListener, null, null);
       fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Collections
           .singleton(tempDir));
       fileManager.setLocation(StandardLocation.CLASS_PATH, classpath);
       
       final Iterable<? extends JavaFileObject> javaFiles = fileManager
           .getJavaFileObjects(buildFile);
       
       // add debug info
       final List<String> options = Arrays.asList("-g");
       
       compiler.getTask(null, fileManager, diagnosticListener, options, null,
           javaFiles).call();
       
       final List<Diagnostic<? extends JavaFileObject>> diagnostics = diagnosticListener
           .getDiagnostics();
       if (diagnostics.isEmpty()) {
         return tempDir;
       } else {
         final StringBuilder causeBuilder = new StringBuilder(
             "The following halted compilation:\n");
         
         for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
           causeBuilder.append(String.format("%s! %s%n", diagnostic.getKind(),
               diagnostic.getMessage(null)));
         }
         
         throw new CompilationFailedException(causeBuilder.toString());
       }
     } catch (final IOException e) {
       throw new CompilationFailedException(e);
     }
     
   }
   
   /**
    * Get the {@link File} object that represents the build file to use.
    * 
    * @throws NoBuildFileFoundException
    *           if no suitable build file is found, or a given build file doesn't
    *           exist.
    */
   private static File getBuildFile() throws NoBuildFileFoundException {
     final File file = new File(buildfile);
     if (file.canRead()) {
       return file;
     } else {
       throw new NoBuildFileFoundException(buildfile);
     }
   }
   
   private static void handleArgs(final String[] args) {
     rawArgs = args;
     
     final Queue<String> argQueue = new LinkedList<String>(Arrays.asList(args));
     
     while (!argQueue.isEmpty()) {
       final String arg;
       if (argQueue.peek().startsWith("-")) {
         arg = argQueue.remove();
       } else {
         break;
       }
       
       if (Util.isAnyOf(arg, "-v", "--verbose")) {
         Log.get().setLogLevel(LogLevel.VERBOSE);
       }
 
       else if (Util.isAnyOf(arg, "-vv", "--debug")) {
         Log.get().setLogLevel(LogLevel.DEBUG);
       }
 
       else if (Util.isAnyOf(arg, "-q", "--quiet")) {
         Log.get().setLogLevel(LogLevel.SEVERE);
       }
 
       else if (Util.isAnyOf(arg, "-l", "--list-targets")) {
         skipBuilding = true;
         listTargets = true;
       }
 
       else if (Util.isAnyOf(arg, "-h", "--help")) {
         showHelp = true;
         skipBuilding = true;
       }
 
       else if (Util.isAnyOf(arg, "-f", "--build-file")) {
         try {
           buildfile = argQueue.remove();
          buildfileIsExplicit = true;
         } catch (final NoSuchElementException e) {
           showHelp = true;
           skipBuilding = true;
           throw new UnrecognizedArgumentException(arg
               + " was given without a proper argument");
         }
       }
 
       else {
         showHelp = true;
         skipBuilding = true;
         throw new UnrecognizedArgumentException(arg);
       }
     }
     
     if (!argQueue.isEmpty()) {
       buildtarget = argQueue.remove();
     }
     
     if (!argQueue.isEmpty()) {
       showHelp = true;
       skipBuilding = true;
       throw new UnexpectedArgumentAmountException(argQueue.size(), 1);
     }
   }
   
   private static void showHelp() {
     System.out.println("Usage: bob [<options>] [<buildtarget>]");
     System.out.println();
     System.out.println("Options:");
     System.out.println(" -v, --verbose          show additional build info");
     System.out.println(" -vv, --debug           show even debug information");
     System.out.println(" -q, --quiet            show only error messages");
     System.out.println(" -h, --help             show this help");
     System.out.println();
     System.out.println(" -l, --list-targets     ");
     System.out.println(Util.wordWrap("        list targets in a buildfile. " +
                     "Any defined buildtarget will be ignored."));
     System.out.println();
     System.out.println(" -f, --build-file <file>");
     System.out.println(Util.wordWrap("        the given file will be " +
                 "used as the build file, instead of the default "
                 + Defaults.DEFAULT_BUILD_SRC_PATH));
     System.out.println();
     System.out.println("Buildtarget:");
     System.out.println(Util.wordWrap("  The buildtarget " + "is the method "
         + "within buildfile that will be invoked to create whatever "
         + "you wish to be created. If this parameter is "
         + "omitted, a default value of \""
         + Defaults.DEFAULT_BUILD_METHOD_NAME + "\" will be used."));
     System.out.println();
   }
   
   private static void listTargets() {
     final File buildFile = getBuildFile();
     
     try {
       final Set<TargetInfo> targetInfos = BuildFileUtil
           .getTargetInfos(buildFile);
       
       if (!targetInfos.isEmpty()) {
         System.out.println("Build file " + buildFile.getPath()
             + " contains the following build targets:");
         TargetInfo.print(targetInfos, System.out);
       } else {
         System.out.println("Build file " + buildFile.getPath()
             + " contains no build targets.");
       }
     } catch (final IOException e) {
       System.err.println("Cannot list target info, " +
               "because of the following exception:");
       e.printStackTrace();
     }
   }
   
   public static String getVersionString() {
     return "Bob " + VERSION;
   }
 }
