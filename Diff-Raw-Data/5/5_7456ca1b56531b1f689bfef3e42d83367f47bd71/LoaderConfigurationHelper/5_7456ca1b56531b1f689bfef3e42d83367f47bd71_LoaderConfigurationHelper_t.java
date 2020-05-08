 package sorcer.netlet.util;
 
 import org.rioproject.resolver.*;
 import org.rioproject.url.artifact.ArtifactURLConfiguration;
 import sorcer.resolver.*;
 import sorcer.resolver.Resolver;
 import sorcer.util.ArtifactCoordinates;
 import sorcer.util.JavaSystemProperties;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Used by LoaderConfiguration and ScriptThread to load jars from path
  * User: Pawel.Rubach
  * Date: 06.06.13
  * Time: 01:56
  * To change this template use File | Settings | File Templates.
  */
 public class LoaderConfigurationHelper {
 
     private static final char WILDCARD = '*';
     private static final String ALL_WILDCARD = "" + WILDCARD + WILDCARD;
     private static final String MATCH_FILE_NAME = "\\\\E[^/]+?\\\\Q";
     private static final String MATCH_ALL = "\\\\E.+?\\\\Q";
     public static final String LOAD_PREFIX = "load";
     public static final String CODEBASE_PREFIX = "codebase";
     static final Logger logger = Logger.getLogger(LoaderConfigurationHelper.class.getName());
     private static org.rioproject.resolver.Resolver resolver;
 
 
     public static List<URL> load(String str) {
         List<URL> urlsList = new ArrayList<URL>();
         URI uri;
         str = assignProperties(str);
         try {
             uri = new URI(str);
         } catch (URISyntaxException e) {
             logger.log(Level.SEVERE, "Error while parsing URL " + str, e);
             return urlsList;
         }
         String scheme = uri.getScheme();
         if ("file".equals(scheme)) {
             return getFilesFromFilteredPath(str);
         } else if ("mvn".equals(scheme)) {
             String url = str.substring(6);
             // Check if URL specifies ana artifact on a remote webster
             String[] urlEntries = url.split("@");
             String finalUrl = null;
             try {
                 if (urlEntries.length > 1) {
                     // Try different resolvers
                     ArtifactResolver resolver = Resolver.getResolver();
                     if (resolver instanceof HybridArtifactResolver) {
                         HybridArtifactResolver hResolver = (HybridArtifactResolver)resolver;
                         finalUrl = "http://" + urlEntries[1] + "/" +
                                 hResolver.resolveRepoRelative(ArtifactCoordinates.coords(urlEntries[0]));
                         if (existRemoteFile(new URL(finalUrl))) {
                             urlsList.add(new URL(finalUrl));
                         } else {
                             finalUrl = "http://" + urlEntries[1] + "/" +
                                     hResolver.resolveFlatRelative(ArtifactCoordinates.coords(urlEntries[0]));
                             if (existRemoteFile(new URL(finalUrl)))
                                 urlsList.add(new URL(finalUrl));
                         }
                     } else {
                         finalUrl = "http://" + urlEntries[1] + "/" +
                                 Resolver.resolveRelative(urlEntries[0]);
                         if (existRemoteFile(new URL(finalUrl))) {
                             urlsList.add(new URL(finalUrl));
                         }
                     }
                 } else {
                     finalUrl = Resolver.resolveAbsolute(urlEntries[0]);
                    if (finalUrl!=null && new File(finalUrl).exists())
                         urlsList.add(new File(finalUrl).toURI().toURL());
                    else
                        logger.severe("Problem adding library to codebase, file not found for: " + urlEntries[0]);
                 }
             } catch (MalformedURLException e) {
                     logger.severe("Problem creating URL: " + finalUrl);
             }
         } else if ("http".equals(scheme)) {
             try {
                 urlsList.add(new URL(str));
             } catch (MalformedURLException e) {
                 logger.severe("Problem creating URL: " + str);
             }
         } else {
             if ("artifact".equals(scheme)) {
                 ArtifactURLConfiguration artifactConf = new ArtifactURLConfiguration(uri.getSchemeSpecificPart());
                 try {
                     org.rioproject.resolver.Resolver resolver = getResolver();
                     RemoteRepository[] repos = artifactConf.getRepositories();
                     String[] classpath = ResolverHelper.resolve(artifactConf.getArtifact(), resolver, repos);
                     for (String s : classpath) {
                         urlsList.add(new URL(s));
                     }
                 } catch (ResolverException e) {
                     logger.log(Level.SEVERE, "Could not resolve " + str, e);
                 } catch (MalformedURLException e) {
                     throw new RuntimeException(e);
                 }
             }
         }
         return urlsList;
     }
 
     private static org.rioproject.resolver.Resolver getResolver() throws ResolverException {
         if (resolver == null)
             resolver = ResolverHelper.getResolver();
         return resolver;
     }
 
     public static String parseCodebase(URL websterUrl, String str) {
         if ((!str.startsWith("mvn://")) &&  (!str.startsWith("http://")) && (!str.startsWith("artifact:"))) {
             logger.severe("Codebase can only be specified using mvn://, http:// or artifact:");
             return null;
         }
         if (str.startsWith("mvn://")) {
             String url = str.substring(6);
             // Check if URL specifies as artifact on a remote webster
             String[] urlEntries = url.split("@");
             String finalUrl = null;
             try {
                 if (urlEntries.length > 1) {
                     // Try different resolvers
                     ArtifactResolver resolver = Resolver.getResolver();
                     if (resolver instanceof HybridArtifactResolver) {
                         HybridArtifactResolver hResolver = (HybridArtifactResolver)resolver;
                         finalUrl = "http://" + urlEntries[1] + "/" +
                                 hResolver.resolveRepoRelative(ArtifactCoordinates.coords(urlEntries[0]));
                         if (existRemoteFile(new URL(finalUrl))) {
                             return new URL(finalUrl).toString();
                         } else {
                             finalUrl = "http://" + urlEntries[1] + "/" +
                                     hResolver.resolveFlatRelative(ArtifactCoordinates.coords(urlEntries[0]));
                             if (existRemoteFile(new URL(finalUrl)))
                                 return new URL(finalUrl).toString();
                         }
                     } else {
                         finalUrl = "http://" + urlEntries[1] + "/" +
                                 Resolver.resolveRelative(urlEntries[0]);
                         if (existRemoteFile(new URL(finalUrl))) {
                             return new URL(finalUrl).toString();
                         }
                     }
                 } else if (websterUrl!=null) {
                     finalUrl = Resolver.resolveAbsolute(websterUrl, urlEntries[0]);
                     if (existRemoteFile(new URL(finalUrl)))
                         return new URL(finalUrl).toString();
                 }
             } catch (MalformedURLException e) {
                 logger.severe("Problem creating URL: " + finalUrl);
             }
         }
         return str;
     }
 
     public static List<URL> setCodebase(List<String> codebaseLines, String websterStrUrl, PrintStream out) {
         String curCodebase = System.getProperty(JavaSystemProperties.RMI_SERVER_CODEBASE);
         StringBuilder codebaseSb = new StringBuilder();
         if (curCodebase!=null) codebaseSb.append(curCodebase);
         List<URL> codebaseUrls = new ArrayList<URL>();
         URL websterUrl = null;
         try {
             if (websterStrUrl!=null)
                 websterUrl = new URL(websterStrUrl);
         } catch (MalformedURLException me) {
         }
         for (String codebaseStr : codebaseLines) {
             if (codebaseStr.startsWith(LoaderConfigurationHelper.CODEBASE_PREFIX))
                 codebaseStr = codebaseStr.substring(LoaderConfigurationHelper.CODEBASE_PREFIX.length()).trim();
             if ((!codebaseStr.startsWith("mvn://")) &&  (!codebaseStr.startsWith("http://")) && (!codebaseStr.startsWith("artifact:"))) {
                 if (out!=null) out.println("Codebase can only be specified using mvn://, http:// or artifact:");
                 else logger.severe("Codebase can only be specified using mvn://, http:// or artifact:");
                 return null;
             }
 
             String parsedCodebase = LoaderConfigurationHelper.parseCodebase(websterUrl, codebaseStr);
             try {
                 codebaseUrls.add(new URL(parsedCodebase));
             } catch (MalformedURLException me) {
                 if (out!=null) out.println("Codebase url is malformed: " + me.getMessage());
                 else logger.severe("Codebase url is malformed: " + me.getMessage());
             }
 
             if (parsedCodebase!=null)
                 codebaseSb.append(" ").append(parsedCodebase);
         }
         System.setProperty(JavaSystemProperties.RMI_SERVER_CODEBASE, codebaseSb.toString());
         return codebaseUrls;
     }
 
     /*
        * Expands the properties inside the given string to it's values.
        */
     public static String assignProperties(String str) {
         int propertyIndexStart = 0, propertyIndexEnd = 0;
         boolean requireProperty = false;
         String result = "";
 
         while (propertyIndexStart < str.length()) {
             {
                 int i1 = str.indexOf("${", propertyIndexStart);
                 int i2 = str.indexOf("!{", propertyIndexStart);
                 if (i1 == -1) {
                     propertyIndexStart = i2;
                 } else if (i2 == -1) {
                     propertyIndexStart = i1;
                 } else {
                     propertyIndexStart = Math.min(i1, i2);
                 }
                 requireProperty = propertyIndexStart == i2;
             }
             if (propertyIndexStart == -1) break;
             result += str.substring(propertyIndexEnd, propertyIndexStart);
 
             propertyIndexEnd = str.indexOf("}", propertyIndexStart);
             if (propertyIndexEnd == -1) break;
 
             String propertyKey = str.substring(propertyIndexStart + 2, propertyIndexEnd);
             String propertyValue = null;
 
             propertyValue = System.getProperty(propertyKey);
 
 
             // assume properties contain paths
             if (propertyValue == null) {
                 if (requireProperty) {
                     throw new IllegalArgumentException("Variable " + propertyKey + " in nsh.config references a non-existent System property! Try passing the property to the VM using -D" + propertyKey + "=myValue in JAVA_OPTS");
                 } else {
                     return null;
                 }
             }
             propertyValue = getSlashyPath(propertyValue);
             propertyValue = correctDoubleSlash(propertyValue,propertyIndexEnd,str);
             result += propertyValue;
 
             propertyIndexEnd++;
             propertyIndexStart = propertyIndexEnd;
         }
 
         if (propertyIndexStart == -1 || propertyIndexStart >= str.length()) {
             result += str.substring(propertyIndexEnd);
         } else if (propertyIndexEnd == -1) {
             result += str.substring(propertyIndexStart);
         }
 
         return result;
     }
 
 
     public static boolean existRemoteFile(URL url) {
         try {
             HttpURLConnection huc =  ( HttpURLConnection ) url.openConnection();
             huc.setRequestMethod("HEAD");
             if (huc.getResponseCode() == HttpURLConnection.HTTP_OK)
                 return true;
         } catch (ProtocolException e) {
             logger.severe("Problem with protocol while loading URL to classpath: " + url.toString() + "\n" + e.getMessage());
         } catch (IOException e) {
             logger.severe("Problem adding remote file to classpath, file does not exist: " + url.toString() + "\n" + e.getMessage());
         }
         return false;
     }
 
     /**
      * Get files to load a possibly filtered path. Filters are defined
      * by using the * wildcard like in any shell.
      */
     public static List<URL> getFilesFromFilteredPath(String filter) {
         List<URL> filesToLoad = new ArrayList<URL>();
         if (filter == null) return null;
         filter = getSlashyPath(filter);
         int starIndex = filter.indexOf(WILDCARD);
         if (starIndex == -1) {
             try {
                 filesToLoad.add(new File(filter).toURI().toURL());
             } catch (MalformedURLException e) {
                 logger.severe("Problem converting file to URL: " + e.getMessage());
             }
             //addFile(new File(filter));
             return filesToLoad;
         }
         boolean recursive = filter.indexOf(ALL_WILDCARD) != -1;
 
         if (filter.lastIndexOf('/')<starIndex) {
             starIndex=filter.lastIndexOf('/')+1;
         }
         String startDir = filter.substring(0, starIndex - 1);
         File root = new File(startDir);
 
         filter = Pattern.quote(filter);
         filter = filter.replaceAll("\\" + WILDCARD + "\\" + WILDCARD, MATCH_ALL);
         filter = filter.replaceAll("\\" + WILDCARD, MATCH_FILE_NAME);
         Pattern pattern = Pattern.compile(filter);
 
         final File[] files = root.listFiles();
         if (files != null) {
           //  filesToLoad.addAll(findMatchingFiles(files, pattern, recursive));
         }
         return filesToLoad;
     }
 
     private static List<File> findMatchingFiles(File[] files, Pattern pattern, boolean recursive) {
         List<File> filesToLoad = new ArrayList<File>();
         for (int i = 0; i < files.length; i++) {
             File file = files[i];
             String fileString = getSlashyPath(file.getPath());
             Matcher m = pattern.matcher(fileString);
             if (m.matches() && file.isFile()) {
                 filesToLoad.add(file);
                 //addFile(file);
             }
             if (file.isDirectory() && recursive) {
                 final File[] dirFiles = file.listFiles();
                 if (dirFiles != null) {
                     filesToLoad.addAll(findMatchingFiles(dirFiles, pattern, true));
                 }
             }
         }
         return filesToLoad;
     }
 
     private static String correctDoubleSlash(String propertyValue, int propertyIndexEnd, String str) {
         int index = propertyIndexEnd+1;
         if ( index<str.length() && str.charAt(index)=='/' &&
                 propertyValue.endsWith("/") &&
                 propertyValue.length()>0)
         {
             propertyValue = propertyValue.substring(0,propertyValue.length()-1);
         }
         return propertyValue;
     }
 
     // change path representation to something more system independent.
     // This solution is based on an absolute path
     private static String getSlashyPath(final String path) {
         String changedPath = path;
         if (File.separatorChar != '/')
             changedPath = changedPath.replace(File.separatorChar, '/');
 
         return changedPath;
     }
 
 }
