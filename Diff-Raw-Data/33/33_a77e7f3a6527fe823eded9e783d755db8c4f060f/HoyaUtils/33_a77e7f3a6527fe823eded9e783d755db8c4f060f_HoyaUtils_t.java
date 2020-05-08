 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hoya.tools;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.FileUtil;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.hoya.HoyaExitCodes;
 import org.apache.hadoop.hoya.HoyaKeys;
 import org.apache.hadoop.hoya.api.ClusterDescription;
 import org.apache.hadoop.hoya.api.RoleKeys;
 import org.apache.hadoop.hoya.exceptions.BadCommandArgumentsException;
 import org.apache.hadoop.hoya.exceptions.BadConfigException;
 import org.apache.hadoop.hoya.exceptions.HoyaException;
 import org.apache.hadoop.hoya.exceptions.MissingArgException;
 import org.apache.hadoop.hoya.providers.hbase.HBaseConfigFileOptions;
 import org.apache.hadoop.hoya.yarn.client.HoyaClient;
 import org.apache.hadoop.net.NetUtils;
 import org.apache.hadoop.security.UserGroupInformation;
 import org.apache.hadoop.util.ExitUtil;
 import org.apache.hadoop.yarn.api.records.ApplicationReport;
 import org.apache.hadoop.yarn.api.records.LocalResource;
 import org.apache.hadoop.yarn.api.records.LocalResourceType;
 import org.apache.hadoop.yarn.api.records.LocalResourceVisibility;
 import org.apache.hadoop.yarn.api.records.YarnApplicationState;
 import org.apache.hadoop.yarn.conf.YarnConfiguration;
 import org.apache.hadoop.yarn.util.ConverterUtils;
 import org.apache.hadoop.yarn.util.Records;
import org.apache.zookeeper.server.util.KerberosUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * These are hoya-specific Util methods
  */
 public final class HoyaUtils {
 
   private static final Logger log = LoggerFactory.getLogger(HoyaUtils.class);
 
   private HoyaUtils() {
   }
 
   /**
    * Implementation of set-ness, groovy definition of true/false for a string
    * @param s string
    * @return true iff the string is neither null nor empty
    */
   public static boolean isUnset(String s) {
     return s == null || s.isEmpty();
   }
 
   public static boolean isSet(String s) {
     return !isUnset(s);
   }
 
   /**
    * recursive directory delete
    * @param dir dir to delete
    * @throws IOException on any problem
    */
   public static void deleteDirectoryTree(File dir) throws IOException {
     if (dir.exists()) {
       if (dir.isDirectory()) {
         log.info("Cleaning up {}", dir);
         //delete the children
         File[] files = dir.listFiles();
         if (files == null) {
           throw new IOException("listfiles() failed for " + dir);
         }
         for (File file : files) {
           log.info("deleting {}", file);
           file.delete();
         }
         dir.delete();
       } else {
         throw new IOException("Not a directory " + dir);
       }
     } else {
       //not found, do nothing
       log.debug("No output dir yet");
     }
   }
 
   /**
    * Find a containing JAR
    * @param my_class class to find
    * @return the file or null if it is not found
    * @throws IOException any IO problem, including the class not having a 
    * classloader
    */
   public static File findContainingJar(Class my_class) throws IOException {
     ClassLoader loader = my_class.getClassLoader();
     if (loader == null) {
       throw new IOException(
         "Class " + my_class + " does not have a classloader!");
     }
     String class_file = my_class.getName().replaceAll("\\.", "/") + ".class";
     Enumeration<URL> urlEnumeration = loader.getResources(class_file);
     if (urlEnumeration == null) {
       throw new IOException("Unable to find resources for class " + my_class);
     }
 
     for (Enumeration itr = urlEnumeration; itr.hasMoreElements(); ) {
       URL url = (URL) itr.nextElement();
       if ("jar".equals(url.getProtocol())) {
         String toReturn = url.getPath();
         if (toReturn.startsWith("file:")) {
           toReturn = toReturn.substring("file:".length());
         }
         // URLDecoder is a misnamed class, since it actually decodes
         // x-www-form-urlencoded MIME type rather than actual
         // URL encoding (which the file path has). Therefore it would
         // decode +s to ' 's which is incorrect (spaces are actually
         // either unencoded or encoded as "%20"). Replace +s first, so
         // that they are kept sacred during the decoding process.
         toReturn = toReturn.replaceAll("\\+", "%2B");
         toReturn = URLDecoder.decode(toReturn, "UTF-8");
         String jarFilePath = toReturn.replaceAll("!.*$", "");
         return new File(jarFilePath);
       } else {
         log.info("could not locate JAR containing {} URL={}", my_class, url);
       }
     }
     return null;
   }
 
   public static void checkPort(String hostname, int port, int connectTimeout)
     throws IOException {
     InetSocketAddress addr = new InetSocketAddress(hostname, port);
     checkPort(hostname, addr, connectTimeout);
   }
 
   @SuppressWarnings("SocketOpenedButNotSafelyClosed")
   public static void checkPort(String name,
                                InetSocketAddress address,
                                int connectTimeout)
     throws IOException {
     Socket socket = null;
     try {
       socket = new Socket();
       socket.connect(address, connectTimeout);
     } catch (Exception e) {
       throw new IOException("Failed to connect to " + name
                             + " at " + address
                             + " after " + connectTimeout + "millisconds"
                             + ": " + e,
                             e);
     } finally {
       IOUtils.closeQuietly(socket);
     }
   }
 
   public static void checkURL(String name, String url, int timeout) throws
                                                                     IOException {
     InetSocketAddress address = NetUtils.createSocketAddr(url);
     checkPort(name, address, timeout);
   }
 
   /**
    * A required file
    * @param role role of the file (for errors)
    * @param filename the filename
    * @throws ExitUtil.ExitException if the file is missing
    * @return the file
    */
   public static File requiredFile(String filename, String role) throws
                                                                 IOException {
     if (filename.isEmpty()) {
       throw new ExitUtil.ExitException(-1, role + " file not defined");
     }
     File file = new File(filename);
     if (!file.exists()) {
       throw new ExitUtil.ExitException(-1,
                                        role + " file not found: " +
                                        file.getCanonicalPath());
     }
     return file;
   }
 
   /**
    * Normalize a cluster name then verify that it is valid
    * @param name proposed cluster name
    * @return true iff it is valid
    */
   public static boolean isClusternameValid(String name) {
     if (name == null || name.isEmpty()) {
       return false;
     }
     name = normalizeClusterName(name);
     int first = name.charAt(0);
     if (!Character.isLetter(first)) {
       return false;
     }
 
     for (int i = 0; i < name.length(); i++) {
       int elt = (int) name.charAt(i);
       if (!Character.isLetterOrDigit(elt) && elt != '-') {
         return false;
       }
     }
     return true;
   }
 
   /**
    * Perform whatever operations are needed to make different
    * case cluster names consistent
    * @param name cluster name
    * @return the normalized one (currently: the lower case name)
    */
   public static String normalizeClusterName(String name) {
     return name.toLowerCase(Locale.ENGLISH);
   }
 
   /**
    * Copy a directory to a new FS -both paths must be qualified
    * @param conf conf file
    * @param srcDirPath src dir
    * @param destDirPath dest dir
    * @return # of files copies
    */
   public static int copyDirectory(Configuration conf,
                                   Path srcDirPath,
                                   Path destDirPath) throws IOException {
     FileSystem srcFS = FileSystem.get(srcDirPath.toUri(), conf);
     FileSystem destFS = FileSystem.get(destDirPath.toUri(), conf);
     //list all paths in the src.
     if (!srcFS.exists(srcDirPath)) {
       throw new FileNotFoundException("Source dir not found " + srcDirPath);
     }
     if (!srcFS.isDirectory(srcDirPath)) {
       throw new FileNotFoundException("Source dir not a directory " + srcDirPath);
     }
     FileStatus[] entries = srcFS.listStatus(srcDirPath);
     int srcFileCount = entries.length;
     if (srcFileCount == 0) {
       return 0;
     }
     if (!destFS.exists(destDirPath)) {
       destFS.mkdirs(destDirPath);
     }
     Path[] sourcePaths = new Path[srcFileCount];
     for (int i = 0; i < srcFileCount; i++) {
       FileStatus e = entries[i];
       Path srcFile = e.getPath();
       if (srcFS.isDirectory(srcFile)) {
         throw new IOException("Configuration dir " + srcDirPath
                               + " contains a directory " + srcFile);
       }
       log.debug("copying src conf file {}", srcFile);
       sourcePaths[i] = srcFile;
     }
     log.debug("Copying {} files to dest dir {}", srcFileCount, destDirPath);
     FileUtil.copy(srcFS, sourcePaths, destFS, destDirPath, false, true, conf);
     return srcFileCount;
   }
 
   /**
    * Create the Hoya cluster path for a named cluster.
    * This is a directory; a mkdirs() operation is executed
    * to ensure that it is there.
    * @param fs filesystem
    * @param clustername name of the cluster
    * @return the path for persistent data
    */
   public static Path createHoyaClusterDirPath(FileSystem fs,
                                               String clustername) throws
                                                                   IOException {
     Path instancePath = buildHoyaClusterDirPath(fs, clustername);
     fs.mkdirs(instancePath);
     return instancePath;
   }
 
   /**
    * Build up the path string for a cluster instance -no attempt to
    * create the directory is made
    * @param fs filesystem
    * @param clustername name of the cluster
    * @return the path for persistent data
    */
   public static Path buildHoyaClusterDirPath(FileSystem fs,
                                               String clustername) {
     Path hoyaPath = getBaseHoyaPath(fs);
     return new Path(hoyaPath, HoyaKeys.CLUSTER_DIRECTORY +"/" + clustername);
   }
 
   /**
    * Create the application-instance specific temporary directory
    * in the DFS
    * @param fs filesystem
    * @param clustername name of the cluster
    * @param appID appliation ID
    * @return the path; this directory will already have been created
    */
   public static Path createHoyaAppInstanceTempPath(FileSystem fs,
                                                    String clustername,
                                                    String appID) throws
                                                                  IOException {
     Path hoyaPath = getBaseHoyaPath(fs);
     Path instancePath = new Path(hoyaPath, "tmp/" + clustername + "/" + appID);
     fs.mkdirs(instancePath);
     return instancePath;
   }
 
   /**
    * Get the base path for hoya
    * @param fs
    * @return
    */
   public static Path getBaseHoyaPath(FileSystem fs) {
     return new Path(fs.getHomeDirectory(), ".hoya");
   }
 
   public static String stringify(Throwable t) {
     StringWriter sw = new StringWriter();
     sw.append(t.toString()).append('\n');
     t.printStackTrace(new PrintWriter(sw));
     return sw.toString();
   }
 
   /**
    * Create a configuration with Hoya-specific tuning.
    * This is done rather than doing custom configs.
    * @return the config
    */
   public static YarnConfiguration createConfiguration() {
     YarnConfiguration conf = new YarnConfiguration();
     patchConfiguration(conf);
     return conf;
   }
 
   /**
    * Take an existing conf and patch it for Hoya's needs. Useful
    * in Service.init & RunService methods where a shared config is being
    * passed in
    * @param conf configuration
    * @return the patched configuration
    */
   
   public static Configuration patchConfiguration(Configuration conf) {
 
     //if the fallback option is NOT set, enable it.
     //if it is explicitly set to anything -leave alone
     if (conf.get(HBaseConfigFileOptions.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH) == null) {
       conf.set(HBaseConfigFileOptions.IPC_CLIENT_FALLBACK_TO_SIMPLE_AUTH, "true");
     }
     return conf;
   }
 
   /**
    * Overwrite a cluster specification. This code
    * attempts to do this atomically by writing the updated specification
    * to a new file, renaming the original and then updating the original.
    * There's special handling for one case: the original file doesn't exist
    * @param clusterFS
    * @param clusterSpec
    * @param clusterDirectory
    * @param clusterSpecPath
    * @return true if the original cluster specification was updated.
    */
   public static boolean updateClusterSpecification(FileSystem clusterFS,
                                                    Path clusterDirectory,
                                                    Path clusterSpecPath,
                                                    ClusterDescription clusterSpec) throws
                                                                                    IOException {
 
     //it is not currently there -do a write with overwrite disabled, so that if
     //it appears at this point this is picked up
     if (!clusterFS.exists(clusterSpecPath) &&
         writeSpecWithoutOverwriting(clusterFS, clusterSpecPath, clusterSpec)) {
       return true;
     }
 
     //save to a renamed version
     String specTimestampedFilename = "spec-" + System.currentTimeMillis();
     Path specSavePath =
       new Path(clusterDirectory, specTimestampedFilename + ".json");
     Path specOrigPath =
       new Path(clusterDirectory, specTimestampedFilename + "-orig.json");
 
     //roll the specification. The (atomic) rename may fail if there is 
     //an overwrite, which is how we catch re-entrant calls to this
     if (!writeSpecWithoutOverwriting(clusterFS, specSavePath, clusterSpec)) {
       return false;
     }
     if (!clusterFS.rename(clusterSpecPath, specOrigPath)) {
       return false;
     }
     try {
       if (!clusterFS.rename(specSavePath, clusterSpecPath)) {
         return false;
       }
     } finally {
       clusterFS.delete(specOrigPath, false);
     }
     return true;
   }
 
   public static boolean writeSpecWithoutOverwriting(FileSystem clusterFS,
                                                     Path clusterSpecPath,
                                                     ClusterDescription clusterSpec) {
     try {
       clusterSpec.save(clusterFS, clusterSpecPath, false);
     } catch (IOException e) {
       log.debug("Failed to save cluster specification -race condition? " + e,
                 e);
       return false;
     }
     return true;
   }
 
   public static boolean maybeAddImagePath(FileSystem clusterFS,
                                           Map<String, LocalResource> localResources,
                                           Path imagePath) throws IOException {
     if (imagePath != null) {
       LocalResource resource = createAmResource(clusterFS,
                                                 imagePath,
                                                 LocalResourceType.ARCHIVE);
       localResources.put(HoyaKeys.LOCAL_TARBALL_INSTALL_SUBDIR, resource);
       return true;
     } else {
       return false;
     }
   }
 
   /**
    * Take a collection, return a list containing the string value of every
    * element in the collection.
    * @param c collection
    * @return a stringified list
    */
   public static List<String> collectionToStringList(Collection c) {
     List<String> l = new ArrayList<String>(c.size());
     for (Object o : c) {
       l.add(o.toString());
     }
     return l;
   }
 
   public static String join(Collection collection, String separator) {
     StringBuilder b = new StringBuilder();
     for (Object o : collection) {
       b.append(o);
       b.append(separator);
     }
     return b.toString();
   }
 
   /**
    * Join an array of strings with a separator that appears after every
    * instance in the list -including at the end
    * @param collection strings
    * @param separator separator string
    * @return the list
    */
   public static String join(String[] collection, String separator) {
     StringBuilder b = new StringBuilder();
     for (String o : collection) {
       b.append(o);
       b.append(separator);
     }
     return b.toString();
   }
 
   /**
    * Join an array of strings with a separator that appears after every
    * instance in the list -except at the end
    * @param collection strings
    * @param separator separator string
    * @return the list
    */
   public static String joinWithInnerSeparator(String separator,
                                               String... collection) {
     StringBuilder b = new StringBuilder();
     boolean first = true;
 
     for (String o : collection) {
       if (first) {
         first = false;
       } else {
         b.append(separator);
       }
       b.append(o);
       b.append(separator);
     }
     return b.toString();
   }
 
 
   public static String mandatoryEnvVariable(String key) {
     String v = System.getenv(key);
     if (v == null) {
       throw new MissingArgException("Missing Environment variable " + key);
     }
     return v;
   }
   
   public static String appReportToString(ApplicationReport r, String separator) {
     StringBuilder builder = new StringBuilder(512);
     builder.append("application ").append(
       r.getName()).append("/").append(r.getApplicationType());
     builder.append(separator).append(
       "state: ").append(r.getYarnApplicationState());
     builder.append(separator).append("URL: ").append(r.getTrackingUrl());
     builder.append(separator).append("Started ").append(new Date(r.getStartTime()).toLocaleString());
     long finishTime = r.getFinishTime();
     if (finishTime>0) {
       builder.append(separator).append("Finished ").append(new Date(finishTime).toLocaleString());
     }
     builder.append(separator).append("RPC :").append(r.getHost()).append(':').append(r.getRpcPort());
     String diagnostics = r.getDiagnostics();
     if (!diagnostics.isEmpty()) {
       builder.append(separator).append("Diagnostics :").append(diagnostics);
     }
     return builder.toString();
   }
 
   /**
    * Merge in one map to another -all entries in the second map are
    * merged into the first -overwriting any duplicate keys.
    * @param first first map -the updated one.
    * @param second the map that is merged in
    * @return the first map
    */
   public static Map<String, String>  mergeMap(Map<String, String> first,
            Map<String, String> second) {
     for (Map.Entry<String, String> entry: second.entrySet()) {
       first.put(entry.getKey(), entry.getValue());
     }
     return first;
   }
   
   public static <T1, T2> Map<T1, T2>  mergeMaps(Map<T1, T2> first,
            Map<T1, T2> second) {
     for (Map.Entry<T1, T2> entry: second.entrySet()) {
       first.put(entry.getKey(), entry.getValue());
     }
     return first;
   }
 
   
   /**
    * Convert a map to a multi-line string for printing
    * @param map map to stringify
    * @return a string representation of the map
    */
   public static String stringifyMap(Map<String, String> map) {
     StringBuilder builder =new StringBuilder();
     for (Map.Entry<String, String> entry: map.entrySet()) {
       builder.append(entry.getKey())
              .append("=\"")
              .append(entry.getValue())
              .append("\"\n");
       
     }
     return builder.toString();
   }
 
 
   /**
    * Get the int value of a role
    * @param roleMap map of role key->val entries
    * @param key key the key to look for
    * @param defVal default value to use if the key is not in the map
    * @param min min value or -1 for do not check
    * @param max max value or -1 for do not check
    * @return the int value the integer value
    * @throws BadConfigException if the value could not be parsed
    */
   public static int getIntValue(Map<String, String> roleMap,
                          String key,
                          int defVal,
                          int min,
                          int max
                         ) throws BadConfigException {
     String valS = roleMap.get(key);
     return parseAndValidate(key, valS, defVal, min, max);
 
   }
 
   /**
    * Parse an int value, replacing it with defval if undefined;
    * @param errorKey key to use in exceptions
    * @param defVal default value to use if the key is not in the map
    * @param min min value or -1 for do not check
    * @param max max value or -1 for do not check
    * @return the int value the integer value
    * @throws BadConfigException if the value could not be parsed
    */
   public static int parseAndValidate(String errorKey,
                                      String valS,
                                      int defVal,
                                      int min, int max) throws
                                                        BadConfigException {
     if (valS == null) {
       valS = Integer.toString(defVal);
     }
     String trim = valS.trim();
     int val;
     try {
       val = Integer.decode(trim);
     } catch (NumberFormatException e) {
       throw new BadConfigException("Failed to parse value of "
                                    + errorKey + ": \"" + trim + "\"");
     }
     if (min>=0 && val<min) {
       throw new BadConfigException("Value of "
                                    + errorKey + ": " + val+ "" 
       + "is less than the minimum of " + min);
 
     }
     if (max>=0 && val>max) {
       throw new BadConfigException("Value of "
                                    + errorKey + ": " + val+ "" 
       + "is more than the maximum of " + max);
 
     }
     return val;
   }
 
   /**
    * Create an AM resource from the 
    * @param hdfs HDFS or other filesystem in use
    * @param destPath dest path in filesystem
    * @param resourceType resource type
    * @return the resource set up wih application-level visibility and the
    * timestamp & size set from the file stats.
    */
   public static LocalResource createAmResource(FileSystem hdfs,
                                                Path destPath,
                                                LocalResourceType resourceType) throws
                                                                                IOException {
     FileStatus destStatus = hdfs.getFileStatus(destPath);
     LocalResource amResource = Records.newRecord(LocalResource.class);
     amResource.setType(resourceType);
     // Set visibility of the resource 
     // Setting to most private option
     amResource.setVisibility(LocalResourceVisibility.APPLICATION);
     // Set the resource to be copied over
     amResource.setResource(ConverterUtils.getYarnUrlFromPath(destPath));
     // Set timestamp and length of file so that the framework 
     // can do basic sanity checks for the local resource 
     // after it has been copied over to ensure it is the same 
     // resource the client intended to use with the application
     amResource.setTimestamp(destStatus.getModificationTime());
     amResource.setSize(destStatus.getLen());
     return amResource;
   }
 
   public static InetSocketAddress getRmAddress(Configuration conf) {
     return conf.getSocketAddr(YarnConfiguration.RM_ADDRESS,
                               YarnConfiguration.DEFAULT_RM_ADDRESS,
                               YarnConfiguration.DEFAULT_RM_PORT);
   }
 
   public static InetSocketAddress getRmSchedulerAddress(Configuration conf) {
     return conf.getSocketAddr(YarnConfiguration.RM_SCHEDULER_ADDRESS,
                               YarnConfiguration.DEFAULT_RM_SCHEDULER_ADDRESS,
                               YarnConfiguration.DEFAULT_RM_SCHEDULER_PORT);
   }
 
   /**
    * probe to see if the RM scheduler is defined
    * @param conf config
    * @return true if the RM scheduler address is set to
    * something other than 0.0.0.0
    */
   public static boolean isRmSchedulerAddressDefined(Configuration conf) {
     InetSocketAddress address = getRmSchedulerAddress(conf);
     return isAddressDefined(address);
   }
 
   /**
    * probe to see if the address
    * @param address
    * @return true if the scheduler address is set to
    * something other than 0.0.0.0
    */
   public static boolean isAddressDefined(InetSocketAddress address) {
     return !(address.getHostName().equals("0.0.0.0"));
   }
 
   public static void setRmAddress(Configuration conf, String rmAddr) {
     conf.set(YarnConfiguration.RM_ADDRESS, rmAddr);
   }
 
   public static void setRmSchedulerAddress(Configuration conf, String rmAddr) {
     conf.set(YarnConfiguration.RM_SCHEDULER_ADDRESS, rmAddr);
   }
 
   public static boolean hasAppFinished(ApplicationReport report) {
     return report == null ||
            report.getYarnApplicationState().ordinal() >=
            YarnApplicationState.FINISHED.ordinal();
   }
 
   public static String reportToString(ApplicationReport report) {
     if (report == null) {
       return "Null application report";
     }
 
     return "App " + report.getName() + "/" + report.getApplicationType() +
            "# " +
            report.getApplicationId() + " user " + report.getUser() +
            " is in state " + report.getYarnApplicationState() +
            "RPC: " + report.getHost() + ":" + report.getRpcPort();
   }
 
   /**
    * Register all files under a fs path as a directory to push out 
    * @param clusterFS cluster filesystem
    * @param srcDir src dir
    * @param destRelativeDir dest dir (no trailing /)
    * @return the list of entries
    */
   public static Map<String, LocalResource> submitDirectory(FileSystem clusterFS,
                                                            Path srcDir,
                                                            String destRelativeDir) throws
                                                                                    IOException {
     //now register each of the files in the directory to be
     //copied to the destination
     FileStatus[] fileset = clusterFS.listStatus(srcDir);
     Map<String, LocalResource> localResources =
       new HashMap<String, LocalResource>(fileset.length);
     for (FileStatus entry : fileset) {
 
       LocalResource resource = createAmResource(clusterFS,
                                                 entry.getPath(),
                                                 LocalResourceType.FILE);
       String relativePath = destRelativeDir + "/" + entry.getPath().getName();
       localResources.put(relativePath, resource);
     }
     return localResources;
   }
 
   public static String stringify(org.apache.hadoop.yarn.api.records.URL url) {
     return url.getScheme() + ":/" +
            (url.getHost() != null ? url.getHost() : "") + "/" + url.getFile();
   }
 
   public static int findFreePort(int start, int limit) {
     if (start == 0) {
       //bail out if the default is "dont care"
       return 0;
     }
     int found = 0;
     int port = start;
     int finish = start + limit;
     while (found == 0 && port < finish) {
       if (isPortAvailable(port)) {
         found = port;
       } else {
         port++;
       }
     }
     return found;
   }
 
   /**
    * See if a port is available for listening on by trying to listen
    * on it and seeing if that works or fails.
    * @param port port to listen to
    * @return true if the port was available for listening on
    */
   public static boolean isPortAvailable(int port) {
     try {
       ServerSocket socket = new ServerSocket(port);
       socket.close();
       return true;
     } catch (IOException e) {
       return false;
     }
   }
 
   /**
    * Build the environment map from a role option map, finding all entries
    * beginning with "env.", adding them to a map of (prefix-removed)
    * env vars
    * @param roleOpts role options. This can be null, meaning the
    * role is undefined
    * @return a possibly empty map of environment variables.
    */
   public static Map<String, String> buildEnvMap(Map<String, String> roleOpts) {
     Map<String, String> env = new HashMap<String, String>();
     if (roleOpts != null) {
       for (Map.Entry<String, String> entry:roleOpts.entrySet()) {
         String key = entry.getKey();
         if (key.startsWith(RoleKeys.ENV_PREFIX)) {
           String envName = key.substring(RoleKeys.ENV_PREFIX.length());
           if (!envName.isEmpty()) {
             env.put(envName,entry.getValue());
           }
         }
       }
     }
     return env;
   }
 
   /**
    * Apply a set of command line options to a cluster role map
    * @param clusterRoleMap cluster role map to merge onto
    * @param commandOptions command opts
    */
   public static void applyCommandLineOptsToRoleMap(Map<String, Map<String, String>> clusterRoleMap,
                                                    Map<String, Map<String, String>> commandOptions) {
     for (String key: commandOptions.keySet()) {
       Map<String, String> optionMap = commandOptions.get(key);
       Map<String, String> existingMap = clusterRoleMap.get(key);
       if (existingMap == null) {
         existingMap = new HashMap<String, String>();
       }
       log.debug("Overwriting role options with command line values {}",
                 stringifyMap(optionMap));
       mergeMap(existingMap, optionMap);
       //set or overwrite the role
       clusterRoleMap.put(key, existingMap);
     }
   }
 
   /**
    * Perform any post-load cluster validation. This may include loading
    * a provider and having it check it
    * @param fileSystem FS
    * @param clusterSpecPath path to cspec
    * @param clusterSpec the cluster spec to validate
    */
   public static void verifySpecificationValidity(FileSystem fileSystem,
                                                  Path clusterSpecPath,
                                                  ClusterDescription clusterSpec) throws
                                                                           HoyaException {
     if (clusterSpec.state == ClusterDescription.STATE_INCOMPLETE) {
       throw new HoyaException(HoyaExitCodes.EXIT_BAD_CLUSTER_STATE,
                               HoyaClient.E_INCOMPLETE_CLUSTER_SPEC + clusterSpecPath);
     }
   }
 
   /**
    * Load a cluster spec then validate it
    * @param fileSystem FS
    * @param clusterSpecPath path to cspec
    * @return the cluster spec
    * @throws IOException IO problems
    * @throws HoyaException cluster location, spec problems
    */
   public static ClusterDescription loadAndValidateClusterSpec(FileSystem filesystem,
                       Path clusterSpecPath) throws IOException, HoyaException {
     ClusterDescription clusterSpec =
       ClusterDescription.load(filesystem, clusterSpecPath);
     //spec is loaded, just look at its state;
     verifySpecificationValidity(filesystem, clusterSpecPath, clusterSpec);
     return clusterSpec;
   }
 
   /**
    * Locate a cluster specification in the FS. This includes a check to verify
    * that the file is there.
    * @param filesystem filesystem
    * @param clustername name of the cluster
    * @return the path to the spec.
    * @throws IOException IO problems
    * @throws HoyaException if the path isn't there
    */
   public static Path locateClusterSpecification(FileSystem filesystem,
                                                 String clustername) throws
                                                                     IOException,
                                                                     HoyaException {
     Path clusterDirectory =
       buildHoyaClusterDirPath(filesystem, clustername);
     Path clusterSpecPath =
       new Path(clusterDirectory, HoyaKeys.CLUSTER_SPECIFICATION_FILE);
     ClusterDescription.verifyClusterSpecExists(clustername, filesystem,
                                                clusterSpecPath);
     return clusterSpecPath;
   }
 
   /**
    * verify that the supplied cluster name is valid
    * @param clustername cluster name
    * @throws BadCommandArgumentsException if it is invalid
    */
   public static void validateClusterName(String clustername) throws
                                                          BadCommandArgumentsException {
     if (!isClusternameValid(clustername)) {
       throw new BadCommandArgumentsException(
         "Illegal cluster name: " + clustername);
     }
   }
 
   /**
    * Verify that a Kerberos principal has been set -if not fail
    * with an error message that actually tells you what is missing
    * @param conf configuration to look at
    * @param principal key of principal
    * @throws BadConfigException if the key is not set
    */
   public static void verifyPrincipalSet(Configuration conf,
                                         String principal) throws
                                                            BadConfigException {
     if (conf.get(principal) == null) {
       throw new BadConfigException("Unset Kerberos principal : %s",
                                    principal);
     }
   }
 
   /**
    * This wrapps ApplicationReports and generates a string version
    * iff the toString() operator is invoked
    */
   public static class OnDemandReportStringifier {
     private final ApplicationReport report;
 
     public OnDemandReportStringifier(ApplicationReport report) {
       this.report = report;
     }
 
     @Override
     public String toString() {
       return appReportToString(report, "\n");
     }
   }
 
   public static Map<String, Map<String, String>> deepClone(Map<String, Map<String, String>> src) {
     Map<String, Map<String, String>> dest =
       new HashMap<String, Map<String, String>>();
     for (Map.Entry<String, Map<String, String>> entry : src.entrySet()) {
       dest.put(entry.getKey(), stringMapClone(entry.getValue()));
     }
     return dest;
   }
 
   public static Map<String, String> stringMapClone(Map<String, String> src) {
     Map<String, String> dest =  new HashMap<String, String>();
     for (Map.Entry<String, String> entry : src.entrySet()) {
       dest.put(entry.getKey(), entry.getValue());
     }
     return dest;
   }
   
   public static String listDir(File dir) {
     StringBuilder builder = new StringBuilder();
     String[] confDirEntries = dir.list();
     for (String entry : confDirEntries) {
       builder.append(entry).append("\n");
     }
     return builder.toString();
   }
 
   /**
    * Create a file:// path from a local file
    * @param file file to point the path
    * @return a new Path
    */
   public static Path createLocalPath(File file) {
     return new Path(file.toURI());
   }
 
   /**
    * Get the current user -relays to
    * {@link UserGroupInformation#getCurrentUser()}
    * with any Hoya-specific post processing and exception handling
    * @return user info
    * @throws IOException on a failure to get the credentials
    */
   public static UserGroupInformation getCurrentUser() throws IOException {
 
     try {
       UserGroupInformation currentUser = UserGroupInformation.getCurrentUser();
       log.debug("Current user is {}", currentUser);
       return currentUser;
     } catch (IOException e) {
       log.info("Failed to grt user info", e);
       throw e;
     }
   }
 
  public static String getKerberosRealm() {
    try {
      return KerberosUtil.getDefaultRealm();
    } catch (Exception e) {
      log.debug("introspection into JVM internals failed", e);
      return "(unknown)";

    }
  }
 }
