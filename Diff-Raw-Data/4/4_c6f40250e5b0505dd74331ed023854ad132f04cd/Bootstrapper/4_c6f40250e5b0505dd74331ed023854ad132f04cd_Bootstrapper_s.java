 package com.openaf.bootstrapper;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.net.Proxy;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.security.*;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class Bootstrapper {
     public static void main(String[] args) throws Exception {
         if (args.length < 1) {
             System.err.println("Usage: javaw -jar bootstrapper.jar <serverURL>");
             Thread.sleep(10 * 1000);
             System.exit(1);
         }
 
         Proxy proxy = Proxy.NO_PROXY;
         URL url = new URL(args[0]);
         URL configURL = new URL(url + "/gui/config.txt");
 
         List<String> configLines = readLines(openConnection(configURL, proxy));
         String[] programArgs = configLines.get(0).split(" ");
         String instanceName = programArgs[0];
         String mainClass = programArgs[1];
         String[] configArgsArray = Arrays.copyOfRange(programArgs, 2, programArgs.length);
         String[] argsToPassToGUI = new String[configArgsArray.length + 2];
         argsToPassToGUI[0] = url.toExternalForm();
         argsToPassToGUI[1] = instanceName;
         System.arraycopy(configArgsArray, 0, argsToPassToGUI, 2, configArgsArray.length);
 
         File tmpDir = new File(System.getProperty("user.home"));
         File rootCacheDir = new File(tmpDir, ".openaf");
         if (!rootCacheDir.exists()) rootCacheDir.mkdir();
 
         String cacheDirName = instanceName + "-" + url.getHost() + (url.getPort() == 80 ? "" : ("-" + url.getPort())) +
                 (url.getPath().equals("") ? "" : "-" + url.getPath()).replaceAll("/", "-");
         File cacheDir = new File(rootCacheDir, cacheDirName);
         if (!cacheDir.exists()) cacheDir.mkdir();
         System.out.println("Cache Dir: " + cacheDir.getAbsolutePath());
         clearOldLogFiles(cacheDir);
         Date currentTime = new Date();
         String currentTimeString = new SimpleDateFormat("HH-mm-ss--dd-MM-yyyy").format(currentTime);
         File logFile = new File(cacheDir, "__log-" + currentTimeString + ".txt");
         System.setOut(new java.io.PrintStream(new TeeOutputStream(System.out, new FileOutputStream(logFile))));
         System.setErr(new java.io.PrintStream(new TeeOutputStream(System.err, new FileOutputStream(logFile))));
 
         List<String> latestJARLines = configLines.subList(1, configLines.size());
         Map<String,String> latestJARsToMD5 = jarsWithMD5(latestJARLines);
         File localConfigFile = new File(cacheDir, "config.txt");
         Map<String, String> localJARsToMD5 = new HashMap<>();
         if (localConfigFile.exists()) {
             List<String> allLocalConfigLines = readLines(new FileInputStream(localConfigFile));
             List<String> localConfigLines = allLocalConfigLines.subList(1, allLocalConfigLines.size());
             localJARsToMD5 = jarsWithMD5(localConfigLines);
         }
 
         Map<String, String> missingOrOutOfDateJARs = new HashMap<>();
         for (Map.Entry<String, String> entry : latestJARsToMD5.entrySet()) {
             String jar = entry.getKey();
             String md5 = entry.getValue();
             if (!localJARsToMD5.containsKey(jar) || !md5.equals(localJARsToMD5.get(jar))) {
                 missingOrOutOfDateJARs.put(jar, md5);
             }
         }
 
         if (missingOrOutOfDateJARs.size() > 0) {
             for (String jarName : missingOrOutOfDateJARs.keySet()) {
                 String md5 = missingOrOutOfDateJARs.get(jarName);
                 URL jarURL = new URL(url + "/gui/" + jarName + "?md5=" + md5);
                 InputStream jarInputStream = openConnection(jarURL, proxy);
                 File localJARFile = new File(cacheDir, jarName);
                 BufferedOutputStream localJAROutputStream = new BufferedOutputStream(new FileOutputStream(localJARFile));
                 copyStreams(jarInputStream, localJAROutputStream);
                 jarInputStream.close();
                 localJAROutputStream.flush();
                 localJAROutputStream.close();
             }
         }
 
         FileWriter localConfigFileWriter = new FileWriter(localConfigFile);
         String ls = System.getProperty("line.separator");
         for (String configLine : configLines) {
             localConfigFileWriter.write(configLine + ls);
         }
         localConfigFileWriter.close();
 
         Set<String> jarsToRemove = new HashSet<>(localJARsToMD5.keySet());
         jarsToRemove.removeAll(latestJARsToMD5.keySet());
         for (String jarName : jarsToRemove) {
             new File(cacheDir, jarName).delete();
         }
 
         List<URL> urlsOfLatestJARs = new LinkedList<>();
         for (String jarName : latestJARsToMD5.keySet()) {
             urlsOfLatestJARs.add(new File(cacheDir, jarName).toURI().toURL());
         }

         URLClassLoader urlClassLoader = new URLClassLoader(urlsOfLatestJARs.toArray(new URL[urlsOfLatestJARs.size()]));
         Policy.setPolicy(new Policy() {
             public PermissionCollection getPermissions(CodeSource codesource) {
                 Permissions permissions = new Permissions();
                 permissions.add(new AllPermission());
                 return permissions;
             }
             public void refresh() {}
         });
         Thread.currentThread().setContextClassLoader(urlClassLoader);
         Class launcher = urlClassLoader.loadClass(mainClass);
         @SuppressWarnings("unchecked")
         Method mainMethod = launcher.getMethod("main", new Class[]{String[].class});
         mainMethod.invoke(null, new Object[]{argsToPassToGUI});
     }
 
     private static InputStream openConnection(URL url, Proxy proxy) throws Exception {
         return url.openConnection(proxy).getInputStream();
     }
 
     private static long copyStreams(InputStream input, OutputStream output) throws IOException {
         byte[] buffer = new byte[1024];
         long count = 0;
         int n;
         while ((n = input.read(buffer)) != -1) {
             output.write(buffer, 0, n);
             count += n;
         }
         return count;
     }
 
     private static List<String> readLines(InputStream in) throws Exception {
         BufferedReader reader = new BufferedReader(new InputStreamReader(in));
         List<String> lines = new LinkedList<>();
         String line;
         while ((line = reader.readLine()) != null) {
             lines.add(line);
         }
         in.close();
         return lines;
     }
 
     private static Map<String,String> jarsWithMD5(List<String> lines) {
         Map<String, String> jarsToMd5s = new HashMap<>();
         for (String line : lines) {
             String[] components = line.split(" ");
             jarsToMd5s.put(components[0], components[1]);
         }
         return jarsToMd5s;
     }
 
     private static void clearOldLogFiles(File dir) {
         File[] files = dir.listFiles();
         ArrayList<File> logFiles = new ArrayList<>();
         if (files != null) {
             for (File file : files) {
                 String fileName = file.getName().toLowerCase();
                 if (fileName.contains("_log") && fileName.endsWith(".txt")) {
                     logFiles.add(file);
                 }
             }
         }
         int maxLogFiles = 5;
         if (logFiles.size() > maxLogFiles) {
             Collections.sort(logFiles, new Comparator<File>() {
                 @Override
                 public int compare(File file1, File file2) {
                     if (file1.lastModified() < file2.lastModified()) {
                         return -1;
                     } else {
                         return 1;
                     }
                 }
             });
             int numberToRemove = logFiles.size() - maxLogFiles;
             for (int i = 0; i < numberToRemove; i++) {
                 File logFile = logFiles.get(i);
                 logFile.delete();
             }
         }
         for (File logFile : logFiles) {
             if (logFile.getName().startsWith("__log")) {
                 File newFile = new File(logFile.getParent(), logFile.getName().replaceFirst("_", ""));
                 logFile.renameTo(newFile);
             }
         }
     }
 }
 
 class TeeOutputStream extends OutputStream {
     private OutputStream a;
     private OutputStream b;
 
     public TeeOutputStream(OutputStream a, OutputStream b) {
         this.a = a;
         this.b = b;
     }
 
     public void write(int c) throws IOException {
         a.write(c);
         b.write(c);
     }
 
     public void flush() throws IOException {
         a.flush();
         b.flush();
     }
 
     @Override
     public void close() throws IOException {
         flush();
         a.close();
         b.close();
     }
 }
