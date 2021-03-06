 package io.osv;
 
 import io.osv.jul.IsolatingLogManager;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.logging.LogManager;
 import java.util.zip.ZipException;
 
 /*
  * Copyright (C) 2014 Cloudius Systems, Ltd.
  *
  * This work is open source software, licensed under the terms of the
  * BSD license as described in the LICENSE file in the top-level directory.
  */
 public class ContextIsolator {
     private static final ContextIsolator instance = new ContextIsolator();
 
     static {
         verifyLogManagerIsInstalled();
     }
 
     private static void verifyLogManagerIsInstalled() {
         LogManager manager = LogManager.getLogManager();
         if (!(manager instanceof IsolatingLogManager)) {
             throw new AssertionError("For isolation to work logging manager must be "
                     + IsolatingLogManager.class.getName() + " but is: " + manager.getClass().getName());
         }
     }
 
     private final InheritableThreadLocal<Context> currentContext = new InheritableThreadLocal<>();
 
     public static ContextIsolator getInstance() {
         return instance;
     }
 
     public ContextIsolator() {
         currentContext.set(new Context(ClassLoader.getSystemClassLoader()));
     }
 
     public Context getContext() {
         return currentContext.get();
     }
 
     public Context run(final ClassLoader classLoader, final SandBoxedProcess process) throws Throwable {
         final Context context = new Context(classLoader);
 
         Thread thread = new Thread() {
             @Override
             public void run() {
                 currentContext.set(context);
 
                 try {
                     process.run();
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                 } catch (Throwable throwable) {
                     getUncaughtExceptionHandler().uncaughtException(this, throwable);
                 }
             }
         };
 
         context.setMainThread(thread);
         thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             @Override
             public void uncaughtException(Thread t, Throwable e) {
                 e.printStackTrace();
             }
         });
         thread.start();
         return context;
     }
 
     public Context run(SandBoxedProcess process) throws Throwable {
         return run(ClassLoader.getSystemClassLoader(), process);
     }
 
     public void runSync(String... args) throws Throwable {
         Context context = run(args);
 
         while (true) {
             try {
                 context.join();
                 return;
             } catch (InterruptedException e) {
                 context.interrupt();
             }
         }
     }
 
     public Context run(String... args) throws Throwable {
         ArrayList<String> classpath = new ArrayList<>();
         for (int i = 0; i < args.length; i++) {
             if (args[i].equals("-jar")) {
                 if (i + 1 >= args.length) {
                     throw new IllegalArgumentException("Missing jar name after '-jar'.");
                 }
                 return runJar(args[i + 1], java.util.Arrays.copyOfRange(args, i + 2, args.length), classpath);
             } else if (args[i].equals("-classpath") || args[i].equals("-cp")) {
                 if (i + 1 >= args.length) {
                     throw new IllegalArgumentException("Missing parameter after '" + args[i] + "'");
                 }
                 for (String c : expandClassPath(args[i + 1])) {
                     classpath.add(c);
                 }
                 i++;
             } else if (args[i].startsWith("-D")) {
                 int eq = args[i].indexOf('=');
                 if (eq < 0) {
                     throw new IllegalArgumentException("Missing '=' in parameter '" + args[i] + "'");
                 }
                 String key = args[i].substring(2, eq);
                 String value = args[i].substring(eq + 1, args[i].length());
                 System.setProperty(key, value);
             } else if (!args[i].startsWith("-")) {
                 return runClass(args[i], java.util.Arrays.copyOfRange(args, i + 1, args.length), classpath);
             } else {
                 throw new IllegalArgumentException("Unknown parameter '" + args[i] + "'");
             }
         }
         throw new IllegalArgumentException("No jar or class specified to run.");
     }
 
     private Context runJar(String jarName, String[] args, ArrayList<String> classpath) throws Throwable {
         File jarFile = new File(jarName);
         try {
             JarFile jar = new JarFile(jarFile);
             Manifest mf = jar.getManifest();
             jar.close();
             String mainClass = mf.getMainAttributes().getValue("Main-Class");
             if (mainClass == null) {
                 throw new IllegalArgumentException("No 'Main-Class' attribute in manifest of " + jarName);
             }
             classpath.add(jarName);
             return runClass(mainClass, args, classpath);
         } catch (FileNotFoundException e) {
             throw new IllegalArgumentException("File not found: " + jarName);
         } catch (ZipException e) {
             throw new IllegalArgumentException("File is not a jar: " + jarName);
         }
     }
 
     private Context runClass(final String mainClass, final String[] args, final Iterable<String> classpath) throws Throwable {
         OsvSystemClassLoader osvClassLoader = getOsvClassLoader();
         ClassLoader appClassLoader = getClassLoader(classpath, osvClassLoader.getParent());
 
         return run(appClassLoader, new SandBoxedProcess() {
             @Override
             public void run() throws Throwable {
                 updateClassPathProperty(classpath);
                 runMain(loadClass(mainClass), args);
             }
         });
     }
 
     private static ClassLoader getClassLoader(Iterable<String> classpath, ClassLoader parent) throws MalformedURLException {
         List<URL> urls = toUrls(classpath);
 
         // If no classpath was specified, don't touch the classloader at
         // all, so we just inherit the one used to run us.
         if (urls.isEmpty()) {
             return parent;
         }
 
         URL[] urlArray = urls.toArray(new URL[urls.size()]);
         return new URLClassLoader(urlArray, parent);
     }
 
     private static List<URL> toUrls(Iterable<String> classpath) throws MalformedURLException {
         ArrayList<URL> urls = new ArrayList<>();
         for (String path : classpath) {
             urls.add(toUrl(path));
         }
         return urls;
     }
 
     static void runMain(Class<?> klass, String[] args) throws Throwable {
         Method main = klass.getMethod("main", String[].class);
         try {
             main.invoke(null, new Object[]{args});
         } catch (InvocationTargetException ex) {
             throw ex.getCause();
         }
     }
 
     private static OsvSystemClassLoader getOsvClassLoader() {
         ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
         if (!(systemClassLoader instanceof OsvSystemClassLoader)) {
             throw new AssertionError("System class loader should be an instance of "
                     + OsvSystemClassLoader.class.getName() + " but is "
                     + systemClassLoader.getClass().getName());
         }
 
         return (OsvSystemClassLoader) systemClassLoader;
     }
 
     private static void updateClassPathProperty(Iterable<String> classpath) {
         StringBuilder sb = new StringBuilder();
         boolean first = true;
         for (String path : classpath) {
             if (!first) {
                 sb.append(":");
             }
             first = false;
             sb.append(path);
         }
         System.setProperty("java.class.path", sb.toString());
     }
 
     private static URL toUrl(String path) throws MalformedURLException {
         return new URL("file:///" + path + (isDirectory(path) ? "/" : ""));
     }
 
     private static boolean isDirectory(String path) {
         return new File(path).isDirectory();
     }
 
     static Class<?> loadClass(String name) throws ClassNotFoundException {
         return Thread.currentThread().getContextClassLoader().loadClass(name);
     }
 
     // Expand classpath, as given in the "-classpath" option, to a list of
     // jars or directories. As in the traditional "java" command-line
     // launcher, components of the class path are separated by ":", and
     // we also support the traditional (but awkward) Java wildcard syntax,
     // where "dir/*" adds to the classpath all jar files in the given
     // directory.
     static Iterable<String> expandClassPath(String classpath) {
         ArrayList<String> ret = new ArrayList<>();
         for (String component : classpath.split(":")) {
             if (component.endsWith("/*")) {
                 File dir = new File(
                         component.substring(0, component.length() - 2));
                 if (dir.isDirectory()) {
                    for (File file : dir.listFiles()) {
                         String filename = file.getPath();
                         if (filename.endsWith(".jar")) {
                             ret.add(filename);
                         }
                     }
                     continue; // handled this path component
                 }
             }
             ret.add(component);
         }
         return ret;
     }
 }
