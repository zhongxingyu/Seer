 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared;
 
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.structure.FxAssignment;
 import com.flexive.shared.structure.FxSelectListItem;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.google.common.base.Charsets;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.io.CharStreams;
 import groovy.lang.GroovySystem;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.management.ObjectName;
 import java.io.*;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.*;
 import java.text.CollationKey;
 import java.text.Collator;
 import java.util.*;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 import static org.apache.commons.lang.StringUtils.defaultString;
 
 /**
  * Flexive shared utility functions.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @SuppressWarnings({"ThrowableInstanceNeverThrown"})
 public final class FxSharedUtils {
     private static final Log LOG = LogFactory.getLog(FxSharedUtils.class);
 
     /**
      * Shared message resources bundle
      */
     public static final String SHARED_BUNDLE = "FxSharedMessages";
 
     private static String fxVersion = "3.1";
     private static String fxEdition = "repository";
     private static String fxProduct = "[fleXive]";
     private static String fxBuild = "unknown";
     private static long fxDBVersion = -1L;
     private static String fxBuildDate = "unknown";
     private static String fxBuildUser = "unknown";
     private static String fxHeader = "[fleXive]";
     private static boolean fxSnapshotVersion = false;
     private static String bundledGroovyVersion;
     private static List<String> translatedLocales = Collections.unmodifiableList(Arrays.asList("en"));
     private static String hostname = null;
     private static String appserver = null;
     private static final String JBOSS6_VERSION_PROPERTIES = "org/jboss/version.properties";
 
 
     /**
      * The character(s) representing a "xpath slash" (/) in a public URL.
      */
     public static final String XPATH_ENCODEDSLASH = "@@";
     /**
      * Browser tests set this cookie to force using the test division instead of the actual division
      * defined by the URL domain.
      * TODO: security?
      */
     public static final String COOKIE_FORCE_TEST_DIVISION = "ForceTestDivision";
 
     private static List<String> drops;
     private static List<FxDropApplication> dropApplications;
 
     /**
      * Are JDK 6+ extensions allowed to be run on the current VM?
      */
     public static final boolean USE_JDK6_EXTENSION;
     public static final boolean WINDOWS = System.getProperty("os.name").contains("Windows");
     public static final String FLEXIVE_DROP_PROPERTIES = "flexive-application.properties";
     public static final String FLEXIVE_STORAGE_PROPERTIES = "flexive-storage.properties";
     /**
      * System property to force a minimum set of runonce scripts when set (e.g. UI icons are not installed).
      * This should not be enabled for flexive's own test suite, but for tests in external projects
      * that can assume that the stock run-once scripts work.
      */
     public static final String PROP_RUNONCE_MINIMAL = "flexive.runonce.minimal";
     private static String NODE_ID;
 
     static {
         int major = -1, minor = -1;
         try {
             String[] ver = System.getProperty("java.specification.version").split("\\.");
             if (ver.length >= 2) {
                 major = Integer.parseInt(ver[0]);
                 minor = Integer.parseInt(ver[1]);
             }
         } catch (Exception e) {
             LOG.error(e);
         }
         USE_JDK6_EXTENSION = major > 1 || (major == 1 && minor >= 6);
 
         try {
             PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle("flexive");
             fxVersion = bundle.getString("flexive.version");
             fxSnapshotVersion = fxVersion != null && fxVersion.endsWith("-SNAPSHOT");
             fxEdition = bundle.getString("flexive.edition");
             fxProduct = bundle.getString("flexive.product");
             fxBuild = bundle.getString("flexive.buildnumber");
             fxDBVersion = Long.parseLong(bundle.getString("flexive.dbversion"));
             fxBuildDate = bundle.getString("flexive.builddate");
             fxBuildUser = bundle.getString("flexive.builduser");
             fxHeader = bundle.getString("flexive.header");
             final String languagesValue = bundle.getString("flexive.translatedLocales");
             if (StringUtils.isNotBlank(languagesValue)) {
                 final String[] languages = StringUtils.split(languagesValue, ",");
                 for (int i = 0; i < languages.length; i++) {
                     languages[i] = languages[i].trim().toLowerCase();
                 }
                 translatedLocales = Collections.unmodifiableList(Arrays.asList(languages));
             }
         } catch (Exception e) {
             LOG.error(e);
         }
     }
 
     /**
      * Get the current hosts name
      *
      * @return current hosts name
      * @since 3.1
      */
     public synchronized static String getHostName() {
         if (hostname != null)
             return hostname;
         String _hostname;
         try {
             _hostname = InetAddress.getLocalHost().getHostName();
             if (StringUtils.isBlank(_hostname)) {
                 _hostname = "localhost";
                 LOG.warn("Hostname was empty, using \"localhost\"");
             }
         } catch (UnknownHostException e) {
             LOG.warn("Failed to determine node ID, using \"localhost\": " + e.getMessage(), e);
             _hostname = "localhost";
         }
         hostname = _hostname;
         return hostname;
     }
 
     /**
      * Get the name of the application server [fleXive] is running on
      *
      * @return name of the application server [fleXive] is running on
      * @since 3.1
      */
     public synchronized static String getApplicationServerName() {
         if (appserver != null)
             return appserver;
         if (System.getProperty("product.name") != null) {
             // Glassfish 2 / Sun AS
             String ver = System.getProperty("product.name");
             if (System.getProperty("com.sun.jbi.domain.name") != null)
                 ver += " (Domain: " + System.getProperty("com.sun.jbi.domain.name") + ")";
             appserver = ver;
         } else if (System.getProperty("glassfish.version") != null) {
             // Glassfish 3+
             appserver = System.getProperty("glassfish.version");
         } else if (System.getProperty("jboss.home.dir") != null) {
             appserver = "JBoss (unknown version)";
             boolean found = false;
             try {
                 final Class<?> cls = Class.forName("org.jboss.Version");
                 Method m = cls.getMethod("getInstance");
                 Object v = m.invoke(null);
                 Method pr = cls.getMethod("getProperties");
                 Map props = (Map) pr.invoke(v);
                 String ver = inspectJBossVersionProperties(props);
                 found = true;
                 appserver = ver;
             } catch (ClassNotFoundException e) {
                 //ignore
             } catch (NoSuchMethodException e) {
                 //ignore
             } catch (IllegalAccessException e) {
                 //ignore
             } catch (InvocationTargetException e) {
                 //ignore
             }
             if (!found) {
                 // try JBoss 7 MBean lookup
                 try {
                     final ObjectName name = new ObjectName("jboss.as:management-root=server");
                     final Object version = ManagementFactory.getPlatformMBeanServer().getAttribute(name, "releaseVersion");
                     if (version != null) {
                         appserver = "JBoss (" + version + ")";
                         found = true;
                     }
                 } catch (Exception e) {
                     // ignore
                 }
             }
             if (!found) {
                 //try again with a JBoss 6.x specific locations
                 try {
                     final ClassLoader jbossCL = Class.forName("org.jboss.Main").getClassLoader();
                     if (jbossCL.getResource(JBOSS6_VERSION_PROPERTIES) != null) {
                         Properties prop = new Properties();
                         prop.load(jbossCL.getResourceAsStream(JBOSS6_VERSION_PROPERTIES));
                         if (prop.containsKey("version.name")) {
                             appserver = inspectJBossVersionProperties(prop);
                             //noinspection UnusedAssignment
                             found = true;
                         }
                     }
                 } catch (ClassNotFoundException e) {
                     //ignore
                 } catch (IOException e) {
                     //ignore
                 }
             }
         } else if (System.getProperty("openejb.version") != null) {
             // try to get Jetty version
             String jettyVersion = "";
             try {
                 final Class<?> cls = Class.forName("org.mortbay.jetty.Server");
                 jettyVersion = " (Jetty "
                         + cls.getPackage().getImplementationVersion()
                         + ")";
             } catch (ClassNotFoundException e) {
                 // no Jetty version...
             }
             appserver = "OpenEJB " + System.getProperty("openejb.version") + jettyVersion;
         } else if (System.getProperty("weblogic.home") != null) {
             String server = System.getProperty("weblogic.Name");
             String wlVersion = "";
             try {
                 final Class<?> cls = Class.forName("weblogic.common.internal.VersionInfo");
                 Method m = cls.getMethod("theOne");
                 Object serverVersion = m.invoke(null);
                 Method sv = m.invoke(null).getClass().getMethod("getImplementationVersion");
                 wlVersion = " " + String.valueOf(sv.invoke(serverVersion));
             } catch (ClassNotFoundException e) {
                 //ignore
             } catch (NoSuchMethodException e) {
                 //ignore
             } catch (InvocationTargetException e) {
                 //ignore
             } catch (IllegalAccessException e) {
                 //ignore
             }
             if (StringUtils.isEmpty(server))
                 appserver = "WebLogic" + wlVersion;
             else
                 appserver = "WebLogic" + wlVersion + " (server: " + server + ")";
         } else if (System.getProperty("org.apache.geronimo.home.dir") != null) {
             String gVersion = "";
             try {
                 final Class<?> cls = Class.forName("org.apache.geronimo.system.serverinfo.ServerConstants");
                 Method m = cls.getMethod("getVersion");
                 gVersion = " " + String.valueOf(m.invoke(null));
                 m = cls.getMethod("getBuildDate");
                 gVersion = gVersion + " (" + String.valueOf(m.invoke(null)) + ")";
             } catch (ClassNotFoundException e) {
                 //ignore
             } catch (NoSuchMethodException e) {
                 //ignore
             } catch (InvocationTargetException e) {
                 //ignore
             } catch (IllegalAccessException e) {
                 //ignore
             }
             appserver = "Apache Geronimo " + gVersion;
         } else {
             appserver = "unknown";
         }
         return appserver;
     }
 
     /**
      * Inspect a map of JBoss specific properties and build a version String
      * @param props properties
      * @return JBoss version string
      */
     private static String inspectJBossVersionProperties(Map props) {
         String ver = "JBoss";
         if (props.containsKey("version.major") && props.containsKey("version.minor")) {
             if (props.containsKey("version.name"))
                 ver = ver + " [" + props.get("version.name") + "]";
             ver = ver + " " + props.get("version.major") + "." + props.get("version.minor");
             if (props.containsKey("version.revision"))
                 ver = ver + "." + props.get("version.revision");
             if (props.containsKey("version.tag"))
                 ver = ver + " " + props.get("version.tag");
             if (props.containsKey("build.day"))
                 ver = ver + " built " + props.get("build.day");
         } else
             ver = ver + " (unknown version)";
         return ver;
     }
 
     /**
      * Get the named resource from the current thread's classloader
      *
      * @param name name of the resource
      * @return inputstream for the resource
      */
     public static InputStream getResourceStream(String name) {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         return cl.getResourceAsStream(name);
     }
 
     /**
      * This method returns all entries in a JarInputStream for a given search pattern within the jar as a Map
      * having the filename as the key and the file content as its respective value (String).
      * The boolean flag "isFile" marks the search pattern as a file, otherwise the pattern will be treated as
      * a path to be found in the jarStream
      * A successful search either returns a map of all entries for a given path or a map of all entries for a given file
      * (again, depending on the "isFile" flag).
      * Null will be returned if no occurrences of the search pattern were found.
      *
      * @param jarStream     the given JarInputStream
      * @param searchPattern the pattern to be examined as a String
      * @param isFile        if true, the searchPattern is treated as a file name, if false, the searchPattern will be treated as a path
      * @return Returns all entries found for the given search pattern as a Map<String, String>, or null if no matches were found
      * @throws IOException on I/O errors
      */
     public static Map<String, String> getContentsFromJarStream(JarInputStream jarStream, String searchPattern, boolean isFile) throws IOException {
         Map<String, String> jarContents = new HashMap<String, String>();
         int found = 0;
         try {
             if (jarStream != null) {
                 JarEntry entry;
                 while ((entry = jarStream.getNextJarEntry()) != null) {
                     if (isFile) {
                         if (!entry.isDirectory() && entry.getName().endsWith(searchPattern)) {
                             final String name = entry.getName().substring(entry.getName().lastIndexOf("/") + 1);
                             jarContents.put(name, readFromJarEntry(jarStream, entry));
                             found++;
                         }
                     } else {
                         if (!entry.isDirectory() && entry.getName().startsWith(searchPattern)) {
                             jarContents.put(entry.getName(), readFromJarEntry(jarStream, entry));
                             found++;
                         }
                     }
                 }
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Found " + found + " entries in the JarInputStream for the pattern " + searchPattern);
                 }
             }
         } finally {
             if (jarStream != null) {
                 try {
                     jarStream.close();
                 } catch (IOException e) {
                     LOG.warn("Failed to close stream: " + e.getMessage(), e);
                 }
             } else {
                 LOG.warn("JarInputStream parameter was null, no search performed");
             }
         }
 
         return jarContents.isEmpty() ? null : jarContents;
     }
 
     /**
      * Reads the content of a given entry in a Jar file (JarInputStream) and returns it as a String
      *
      * @param jarStream the given JarInputStream
      * @param entry     the given entry in the jar file
      * @return the entry's content as a String
      * @throws java.io.IOException on errors
      */
     public static String readFromJarEntry(JarInputStream jarStream, JarEntry entry) throws IOException {
         final String fileContent;
         if (entry.getSize() >= 0) {
             // allocate buffer for the entire (uncompressed) script code
             final byte[] buffer = new byte[(int) entry.getSize()];
             // decompress JAR entry
             int offset = 0;
             int readBytes;
             while ((readBytes = jarStream.read(buffer, offset, (int) entry.getSize() - offset)) > 0) {
                 offset += readBytes;
             }
             if (offset != entry.getSize()) {
                 throw new IOException("Failed to read complete script code for script: " + entry.getName());
             }
             fileContent = new String(buffer, "UTF-8").trim();
         } else {
             // use this method if the file size cannot be determined
             //(might be the case with jar files created with some jar tools)
             final StringBuilder out = new StringBuilder();
             final byte[] buf = new byte[1024];
             int readBytes;
             while ((readBytes = jarStream.read(buf, 0, buf.length)) > 0) {
                 out.append(new String(buf, 0, readBytes, "UTF-8"));
             }
             fileContent = out.toString();
         }
         return fileContent;
     }
 
     /**
      * Get a list of all installed and deployed drops
      *
      * @return list of all installed and deployed drops
      */
     public static synchronized List<String> getDrops() {
         if (drops == null) {
             initDropApplications();
         }
 
         return drops;
     }
 
     private static synchronized void initDropApplications() {
         final List<FxDropApplication> apps = new ArrayList<FxDropApplication>();
 
         addDropsFromArchiveIndex(apps);
         addDropsFromClasspath(apps);
 
         // sort lexically
         Collections.sort(apps, new Comparator<FxDropApplication>() {
             public int compare(FxDropApplication o1, FxDropApplication o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         });
         dropApplications = Collections.unmodifiableList(apps);
         drops = new ArrayList<String>(dropApplications.size());
         for (FxDropApplication dropApplication : dropApplications) {
             drops.add(dropApplication.getName());
         }
         drops = Collections.unmodifiableList(drops);
 
         if (LOG.isInfoEnabled()) {
             LOG.info("Detected [fleXive] drop applications: " + drops);
         }
     }
 
     /**
      * Get a list of all installed and deployed drops.
      *
      * @return a list of all installed and deployed drops.
      * @since 3.0.2
      */
     public static synchronized List<FxDropApplication> getDropApplications() {
         getDrops();
         return dropApplications;
     }
 
     /**
      * Returns the drop application with the given name.
      *
      * @param name the application name
      * @return the drop application with the given name.
      * @since 3.0.2
      */
     public static synchronized FxDropApplication getDropApplication(String name) {
         for (FxDropApplication dropApplication : getDropApplications()) {
             if (dropApplication.getName().equalsIgnoreCase(name)) {
                 return dropApplication;
             }
         }
         throw new FxNotFoundException("ex.sharedUtils.drop.notFound", name).asRuntimeException();
     }
 
     /**
      * Add drop applications explicitly mentioned in the drops.archives file.
      *
      * @param dropApplications list of drop application info objects to be populated
      */
     private static void addDropsFromArchiveIndex(List<FxDropApplication> dropApplications) {
         try {
             final String dropsList = loadFromInputStream(
                     Thread.currentThread().getContextClassLoader().getResourceAsStream("drops.archives")
             );
             if (StringUtils.isNotEmpty(dropsList)) {
                 for (String name : dropsList.split(",")) {
                     dropApplications.add(new FxDropApplication(name));
                 }
             }
         } catch (Exception e) {
             LOG.error("Failed to parse drops.archives: " + e.getMessage(), e);
         }
     }
 
     /**
      * Add drop applications from the classpath (based on a file called flexive.properties).
      *
      * @param dropApplications list of drop application info objects to be populated
      */
     private static void addDropsFromClasspath(List<FxDropApplication> dropApplications) {
         try {
             final Enumeration<URL> fileUrls =
                     Thread.currentThread().getContextClassLoader().getResources(FLEXIVE_DROP_PROPERTIES);
             while (fileUrls.hasMoreElements()) {
                 final URL url = fileUrls.nextElement();
 
                 // load properties from file
                 final Properties properties = new Properties();
                 properties.load(url.openStream());
 
                 // load drop configuration parameters
                 final String name = properties.getProperty("name");
                 final String contextRoot = properties.getProperty("contextRoot");
                 final String displayName = properties.getProperty("displayName");
                 if (StringUtils.isNotBlank(name)) {
                     dropApplications.add(
                             new FxDropApplication(
                                     name,
                                     contextRoot,
                                     defaultString(displayName, name),
                                     url
                             )
                     );
                 }
             }
         } catch (IOException e) {
             LOG.error("Failed to initialize drops from the classpath: " + e.getMessage(), e);
         }
     }
 
     /**
      * Scan all flexive storage implementations to get the name of the factory classes
      *
      * @return list containing the name of all storage factory classes
      */
     public static List<String> getStorageImplementations() {
         List<String> found = new ArrayList<String>(10);
         try {
             final Enumeration<URL> fileUrls =
                     Thread.currentThread().getContextClassLoader().getResources(FLEXIVE_STORAGE_PROPERTIES);
             while (fileUrls.hasMoreElements()) {
                 final URL url = fileUrls.nextElement();
 
                 // load properties from file
                 final Properties properties = new Properties();
                 properties.load(url.openStream());
 
                 // load factory parameter
                 final String factory = properties.getProperty("storage.factory");
                 if (StringUtils.isNotBlank(factory))
                     found.add(factory);
             }
         } catch (IOException e) {
             LOG.error("Failed to initialize storage implementations from the classpath: " + e.getMessage(), e);
         }
         return found;
     }
 
 
     /**
      * Add all resources found in the resource subfolder for the requested vendor to the map (key=script name, value=script code)
      *
      * @param storageVendor requested vendor
      * @return map with key=script name and value=script code
      */
     public static Map<String, String> getStorageScriptResources(String storageVendor) {
         Map<String, String> result = new HashMap<String, String>(100);
         try {
             final String indexFileName = "storageindex-" + storageVendor + ".flexive";
             final String indexPath = "resources/" + indexFileName;
 
             // open the storage index file
             final URL url = Thread.currentThread().getContextClassLoader().getResource(indexPath);
             if (url == null) {
                 LOG.error("No storage index found for vendor " + storageVendor);
                 return Maps.newHashMap();
             }
 
             // get a base URL (JAR or VFS-based) for the storage JAR entry
             final int indexFilePos = url.getPath().indexOf(indexFileName);
             if (indexFilePos == -1) {
                 LOG.warn("Failed to build base URL for storage based on URL: " + url.getPath());
             }
             final String basePath = url.getProtocol() + ":" + url.getPath().substring(0, indexFilePos);
 
             final String[] resources = loadFromInputStream(url.openStream()).split("\n");
             for (String line : resources) {
                 final int splitPos = line.indexOf('|');
                 if (splitPos == -1) {
                     LOG.warn("Failed to parse storage index line: " + line);
                     continue;
                 }
                 final String name = line.substring(0, splitPos);
                 if (name.endsWith(".sql")) {
                     try {
                         final URL resourceURL = new URL(basePath + name);
                         final String code = loadFromInputStream(resourceURL.openStream());
                         result.put(name, code);
                     } catch (FileNotFoundException e) {
                         LOG.warn(e);
                     }
                 }
             }
         } catch (IOException e) {
             LOG.error(e);
         }
         return result;
     }
 
     /**
      * Return the index of the given column name. If <code>name</code> has no
      * prefix (e.g. "co."), then only a suffix match is performed (e.g.
      * "name" matches "co.name" or "abc.name", whichever comes first.)
      *
      * @param columnNames all column names to be searched
      * @param name        the requested column name
      * @return the 1-based index of the given column, or -1 if it does not exist
      */
     public static int getColumnIndex(String[] columnNames, String name) {
         final String upperName = name.toUpperCase();
         final String upperPropertyName = "." + upperName;
         final String upperAliasName = "AS " + upperName;
         for (int i = 0; i < columnNames.length; i++) {
             final String columnName = columnNames[i];
             final String upperColumn = columnName.toUpperCase();
             if (upperColumn.equals(upperName) || upperColumn.endsWith(upperPropertyName)
                     || upperColumn.endsWith(upperAliasName)) {
                 return i + 1;
             }
         }
         return -1;
     }
 
     /**
      * Return the index of the given column name. If <code>name</code> has no
      * prefix (e.g. "co."), then only a suffix match is performed (e.g.
      * "name" matches "co.name" or "abc.name", whichever comes first.)
      *
      * @param columnNames all column names to be searched
      * @param name        the requested column name
      * @return the 1-based index of the given column, or -1 if it does not exist
      */
     public static int getColumnIndex(List<String> columnNames, String name) {
         return getColumnIndex(columnNames.toArray(new String[columnNames.size()]), name);
     }
 
     /**
      * Compute the hash of the given flexive password, using the user ID.
      *
      * @param accountId the user account ID
      * @param password  the cleartext password
      * @return a hashed password
      */
     public static String hashPassword(long accountId, String password) {
         try {
             return sha1Hash(getBytes("FX-SALT" + accountId + password));
         } catch (NoSuchAlgorithmException e) {
             throw new FxCreateException("Failed to load the SHA1 algorithm.").asRuntimeException();
         }
     }
 
     /**
      * Compute the hash of the given flexive password using the login name. This is the default algorithm
      * for installations since flexive 3.1.6.
      *
      * @param loginName the user login name
      * @param password  the cleartext password
      * @return          the hashed password
      * @since 3.1.6
      */
     public static String hashPassword(String loginName, String password) {
         try {
             return sha1Hash(getBytes("FX-SALT-" + loginName + "-" + password));
         } catch (NoSuchAlgorithmException e) {
             throw new FxCreateException("Failed to load the SHA1 algorithm.").asRuntimeException();
         }
     }
 
     /**
      * Compute the hash of the given password according to the setting in
      * {@link com.flexive.shared.configuration.SystemParameters#PASSWORD_SALT_METHOD} (either with the user ID, or the user name).
      *
      * @param accountId the user account ID
      * @param loginName the login name
      * @param password  the cleartext password
      * @return          the hashed password
      * @since 3.1.6
      */
     public static String hashPassword(long accountId, String loginName, String password) {
         final String method;
         try {
             method = EJBLookup.getConfigurationEngine().get(SystemParameters.PASSWORD_SALT_METHOD);
         } catch (FxApplicationException ex) {
             throw ex.asRuntimeException();
         }
         if ("userid".equals(method)) {
             if (accountId == -1) {
                 throw new IllegalArgumentException("Account-ID not set, but hash method set to userid");
             }
             return hashPassword(accountId, password);
         } else if ("loginname".equals(method)) {
             FxSharedUtils.checkParameterEmpty(loginName, "loginName");
             return hashPassword(loginName, password);
         } else {
             throw new IllegalArgumentException("Unknown hash method: " + method);
         }
     }
 
     /**
      * Returns a collator for the calling user's locale.
      *
      * @return a collator for the calling user's locale.
      */
     public static Collator getCollator() {
         return Collator.getInstance(FxContext.getUserTicket().getLanguage().getLocale());
     }
 
     /**
      * Is the script (most likely) a groovy script?
      *
      * @param name script name to check
      * @return if this script could be a groovy script
      */
     public static boolean isGroovyScript(String name) {
         return name.toLowerCase().endsWith(".gy") || name.toLowerCase().endsWith(".groovy");
     }
 
     /**
      * Check if the given parameter is multilingual and throw an exception if not
      *
      * @param value     the value to check
      * @param paramName name of the parameter
      */
     public static void checkParameterMultilang(FxValue value, String paramName) {
         if (value != null && !value.isMultiLanguage())
             throw new FxInvalidParameterException(paramName, "ex.general.parameter.notMultilang", paramName).asRuntimeException();
     }
 
     /**
      * Maps keys to values. Used for constructing JSF-EL parameter
      * mapper objects for assicative lookups.
      */
     public static interface ParameterMapper<K, V> extends Serializable {
         V get(Object key);
     }
 
     /**
      * Private constructor
      */
     private FxSharedUtils() {
     }
 
     /**
      * Creates a SHA-1 hash for the given data and returns it
      * in hexadecimal string encoding.
      *
      * @param bytes data to be hashed
      * @return hex-encoded hash
      * @throws java.security.NoSuchAlgorithmException
      *          if the SHA-1 provider does not exist
      */
     public static String sha1Hash(byte[] bytes) throws NoSuchAlgorithmException {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         md.update(bytes);
         return FxFormatUtils.encodeHex(md.digest());
     }
 
     /**
      * Calculate an MD5 checksum for a file
      *
      * @param file file to calculate checksum for
      * @return MD5 checksum (16 characters)
      */
     public static String getMD5Sum(File file) {
         InputStream is = null;
         String md5sum = "unknown";
         try {
             MessageDigest digest = MessageDigest.getInstance("MD5");
             is = new FileInputStream(file);
             byte[] buffer = new byte[8192];
             int read;
             while ((read = is.read(buffer)) > 0)
                 digest.update(buffer, 0, read);
             BigInteger bigInt = new BigInteger(1, digest.digest());
             md5sum = bigInt.toString(16);
         } catch (IOException e) {
             LOG.error("Unable calculate MD5 checksum!", e);
         } catch (NoSuchAlgorithmException e) {
             LOG.error("No MD5 algorithm found!", e);
         } finally {
             try {
                 if (is != null)
                     is.close();
             } catch (IOException e) {
                 //ignore
             }
         }
         return md5sum;
     }
 
     /**
      * Helperclass holding the result of the <code>executeCommand</code> method
      *
      * @see FxSharedUtils#executeCommand(String, String...)
      */
     public static final class ProcessResult {
         private String commandLine;
         private int exitCode;
         private String stdOut, stdErr;
 
         /**
          * Constructor
          *
          * @param commandLine the commandline executed
          * @param exitCode    exit code
          * @param stdOut      result from stdOut
          * @param stdErr      result from stdErr
          */
         public ProcessResult(String commandLine, int exitCode, String stdOut, String stdErr) {
             this.commandLine = commandLine;
             this.exitCode = exitCode;
             this.stdOut = stdOut;
             this.stdErr = stdErr;
         }
 
         /**
          * Getter for the commandline
          *
          * @return commandline
          */
         public String getCommandLine() {
             return commandLine;
         }
 
         /**
          * Getter for the exit code
          *
          * @return exit code
          */
         public int getExitCode() {
             return exitCode;
         }
 
         /**
          * Getter for stdOut
          *
          * @return stdOut
          */
         public String getStdOut() {
             return stdOut;
         }
 
         /**
          * Getter for stdErr
          *
          * @return stdErr
          */
         public String getStdErr() {
             return stdErr;
         }
     }
 
     /**
      * Helper thread to asynchronously read and buffer an InputStream
      */
     static final class AsyncStreamBuffer extends Thread {
         protected InputStream in;
         protected StringBuffer sb = new StringBuffer();
 
         /**
          * Constructor
          *
          * @param in the InputStream to buffer
          */
         AsyncStreamBuffer(InputStream in) {
             this.in = in;
         }
 
         /**
          * Getter for the buffered result
          *
          * @return buffered result
          */
         public String getResult() {
             return sb.toString();
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void run() {
             try {
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
                 String line;
                 while ((line = br.readLine()) != null)
                     sb.append(line).append('\n');
             } catch (IOException e) {
                 sb.append("[Error: ").append(e.getMessage()).append("]");
             }
         }
     }
 
     /**
      * Execute a command on the operating system
      *
      * @param command   name of the command
      * @param arguments arguments to pass to the command (one argument per String!)
      * @return result
      */
 
     public static ProcessResult executeCommand(String command, String... arguments) {
         Runtime r = Runtime.getRuntime();
         String[] cmd = new String[arguments.length + (WINDOWS ? 3 : 1)];
         if (WINDOWS) {
             //have to run a shell on windows
             cmd[0] = "cmd";
             cmd[1] = "/c";
         }
 
         cmd[WINDOWS ? 2 : 0] = command;
         System.arraycopy(arguments, 0, cmd, (WINDOWS ? 3 : 1), arguments.length);
         StringBuilder cmdline = new StringBuilder(200);
         cmdline.append(command);
         for (String argument : arguments) cmdline.append(" ").append(argument);
         Process p = null;
         AsyncStreamBuffer out = null;
         AsyncStreamBuffer err = null;
         try {
             p = r.exec(cmd);
 //            p = r.exec(cmdline);
             out = new AsyncStreamBuffer(p.getInputStream());
             err = new AsyncStreamBuffer(p.getErrorStream());
             out.start();
             err.start();
             p.waitFor();
             while (out.isAlive()) Thread.sleep(10);
             while (err.isAlive()) Thread.sleep(10);
         } catch (Exception e) {
             String error = e.getMessage();
             if (err != null && err.getResult() != null && err.getResult().trim().length() > 0)
                 error = error + "(" + err.getResult() + ")";
             return new ProcessResult(cmdline.toString(), (p == null ? -1 : p.exitValue()), (out == null ? "" : out.getResult()), error);
         } finally {
             if (p != null) {
                 try {
                     p.getInputStream().close();
                 } catch (Exception e1) {
                     //bad luck
                 }
                 try {
                     p.getErrorStream().close();
                 } catch (Exception e1) {
                     //bad luck
                 }
                 try {
                     p.getOutputStream().close();
                 } catch (Exception e1) {
                     //bad luck
                 }
             }
         }
         return new ProcessResult(cmdline.toString(), p.exitValue(), out.getResult(), err.getResult());
     }
 
     /**
      * Load the contents of a file, returning it as a String.
      * This method should only be used when really necessary since no real error handling is performed!!!
      *
      * @param file the File to load
      * @return file contents
      */
     public static String loadFile(File file) {
         try {
             return loadFromInputStream(new FileInputStream(file), (int) file.length());
         } catch (FileNotFoundException e) {
             LOG.error(e);
             return "";
         }
     }
 
     /**
      * Load a String from an InputStream (until end of stream)
      *
      * @param in InputStream
      * @return the input stream contents, or an empty string if {@code in} was null.
      * @since 3.0.2
      */
     public static String loadFromInputStream(InputStream in) {
         return loadFromInputStream(in, -1);
     }
 
     /**
      * Load a String from an InputStream (until end of stream)
      *
      * @param in     InputStream
      * @param length length of the string if &gt; -1 (NOT number of bytes to read!)
      * @return the input stream contents, or an empty string if {@code in} was null.
      */
     public static String loadFromInputStream(InputStream in, int length) {
         if (in == null) {
             return "";
         }
         final BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charsets.UTF_8));
         try {
             return CharStreams.toString(reader);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         } finally {
             close(reader);
         }
     }
 
     /**
      * Rather primitive "write String to file" helper, returns <code>false</code> if failed
      *
      * @param contents the String to store
      * @param file     the file, if existing it will be overwritten
      * @return if successful
      */
     public static boolean storeFile(String contents, File file) {
         if (file.exists()) {
             LOG.warn("Warning: " + file.getName() + " already exists! Overwriting!");
         }
         FileOutputStream out = null;
         try {
             out = new FileOutputStream(file);
             out.write(FxSharedUtils.getBytes(contents));
             out.flush();
             out.close();
             return true;
         } catch (IOException e) {
             LOG.error("Failed to store " + file.getAbsolutePath() + ": " + e.getMessage());
             return false;
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     //ignore
                 }
             }
         }
     }
 
     /**
      * Get the flexive version
      *
      * @return flexive version
      */
     public static String getFlexiveVersion() {
         return fxVersion;
     }
 
     /**
      * Returns true if this instance is a -SNAPSHOT version.
      *
      * @return true if this instance is a -SNAPSHOT version.
      * @since 3.1
      */
     public static boolean isSnapshotVersion() {
         return fxSnapshotVersion;
     }
 
     /**
      * Get the subversion build number
      *
      * @return subversion build number
      */
     public static String getBuildNumber() {
         return fxBuild;
     }
 
     /**
      * Get the database version
      *
      * @return database version
      */
     public static long getDBVersion() {
         return fxDBVersion;
     }
 
     /**
      * Get the date flexive was compiled
      *
      * @return compile date
      */
     public static String getBuildDate() {
         return fxBuildDate;
     }
 
     /**
      * Get the name of this flexive edition
      *
      * @return flexive edition
      */
     public static String getFlexiveEdition() {
         return fxEdition;
     }
 
     /**
      * Get the name of this flexive edition with the product name
      *
      * @return flexive edition with product name
      */
     public static String getFlexiveEditionFull() {
         return fxEdition + "." + fxProduct;
     }
 
     /**
      * Get the name of the user that built flexive
      *
      * @return build user
      */
     public static String getBuildUser() {
         return fxBuildUser;
     }
 
     /**
      * Get the default html header title
      *
      * @return html header title
      */
     public static String getHeader() {
         return fxHeader;
     }
 
     /**
      * Get the version of the bundled groovy runtime
      *
      * @return version of the bundled groovy runtime
      */
     public static synchronized String getBundledGroovyVersion() {
         if (bundledGroovyVersion == null) {
             // lazy loading to avoid Groovy initialization at deployment
             bundledGroovyVersion = GroovySystem.getVersion();
         }
         return bundledGroovyVersion;
     }
 
     /**
      * Returns the localized "empty" message for empty result fields
      *
      * @return the localized "empty" message for empty result fields
      */
     public static String getEmptyResultMessage() {
         final FxLanguage language = FxContext.getUserTicket().getLanguage();
         return getLocalizedMessage(SHARED_BUNDLE, language.getId(), language.getIso2digit(), "shared.result.emptyValue");
     }
 
     /**
      * Check if the given value is empty (empty string or null for String objects, empty collection,
      * null for other objects) and throw an exception if empty.
      *
      * @param value         value to check
      * @param parameterName name of the value (for the exception)
      */
     public static void checkParameterNull(Object value, String parameterName) {
         if (value == null) {
             throw new FxInvalidParameterException(parameterName, "ex.general.parameter.null", parameterName).asRuntimeException();
         }
     }
 
     /**
      * Check if the given value is empty (empty string or null for String objects, empty collection,
      * null for other objects) and throw an exception if empty.
      *
      * @param value         value to check
      * @param parameterName name of the value (for the exception)
      */
     public static void checkParameterEmpty(Object value, String parameterName) {
         if (value == null
                 || (value instanceof String && StringUtils.isBlank((String) value))
                 || (value instanceof Collection && ((Collection) value).isEmpty())) {
             throw new FxInvalidParameterException(parameterName, "ex.general.parameter.empty", parameterName).asRuntimeException();
         }
     }
 
     /**
      * Try to find a localized resource messages
      *
      * @param resourceBundle the name of the resource bundle to retrieve the message from
      * @param key            resource key
      * @param localeIso      locale of the resource bundle
      * @return resource from a localized bundle
      */
     public static String lookupResource(String resourceBundle, String key, String localeIso) {
         String result = _lookupResource(resourceBundle, key, localeIso);
         if (result == null) {
             for (String drop : getDrops()) {
                 result = _lookupResource(drop + "Resources/" + resourceBundle, key, localeIso);
                 if (result != null)
                     return result;
             }
         }
         return result;
     }
 
     private static String _lookupResource(String resourceBundle, String key, String localeIso) {
         try {
             String isoCode = localeIso != null ? localeIso : Locale.getDefault().getLanguage();
             PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(resourceBundle, new Locale(isoCode));
             return bundle.getString(key);
         } catch (MissingResourceException e) {
             //try default (english) locale
             try {
                 PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle(resourceBundle, Locale.ENGLISH);
                 return bundle.getString(key);
             } catch (MissingResourceException e1) {
                 return null;
             }
         }
     }
 
     /**
      * Get the localized message for a given language code and ISO
      *
      * @param resourceBundle the resource bundle to use
      * @param localeId       used locale if args contain FxString instances
      * @param localeIso      ISO code of the requested locale
      * @param key            the key in the resource bundle
      * @param args           arguments to replace in the message ({n})
      * @return localized message
      */
     public static String getLocalizedMessage(String resourceBundle, long localeId, String localeIso, String key, Object... args) {
         if (key == null) {
             //noinspection ThrowableInstanceNeverThrown
             LOG.error("No key given!", new Throwable());
             return "??NO_KEY_GIVEN";
         }
         String resource = lookupResource(resourceBundle, key, localeIso);
         if (resource == null) {
             //try to fallback to PluginMessages ...
             resource = lookupResource("PluginMessages", key, localeIso);
             if (resource == null) {
 //                LOG.warn("Called with unlocalized Message [" + key + "]. See StackTrace for origin!", new Throwable());
                 return key;
             }
         }
 
         //lookup possible resource keys in values (they may not have placeholders like {n} though)
         String tmp;
         for (int i = 0; i < args.length; i++) {
             Object o = args[i];
             if (o instanceof String && ((String) o).indexOf(' ') == -1 && ((String) o).indexOf('.') > 0)
                 if ((tmp = lookupResource(resourceBundle, (String) o, localeIso)) != null)
                     args[i] = tmp;
         }
         return FxFormatUtils.formatResource(resource, localeId, args);
     }
 
     /**
      * Returns true if the given locale is localized in the flexive application
      * (compile-time parameter flexive.translatedLocales set in flexive.properties).
      *
      * @param localeIsoCode the locale ISO code, e.g. "en" or "de"
      * @return true if the given locale is localized (at least for some messages).
      * @since 3.1
      */
     public static boolean isTranslatedLocale(String localeIsoCode) {
         for (String locale : translatedLocales) {
             if (locale.equalsIgnoreCase(localeIsoCode)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns the list of translated locales, as specified in flexive.properties
      * (flexive.translatedLocales).
      *
      * @return the list of translated locales, in lowercase
      * @since 3.1
      */
     public static List<String> getTranslatedLocales() {
         return translatedLocales;
     }
 
     /**
      * Returns a multilingual FxString with all translations for the given property key.
      *
      * @param resourceBundle the resource bundle to be used
      * @param key            the message key
      * @param args           optional parameters to be replaced in the property translations
      * @return a multilingual FxString with all translations for the given property key.
      */
     public static FxString getMessage(String resourceBundle, String key, Object... args) {
         Map<Long, String> translations = new HashMap<Long, String>();
         for (String localeIso : translatedLocales) {
             final long localeId = new FxLanguage(localeIso).getId();
             translations.put(localeId, getLocalizedMessage(resourceBundle, localeId, localeIso, key, args));
         }
         return new FxString(translations);
     }
 
     /**
      * Returns the localized label for the given enum value. The enum translations are
      * stored in FxSharedMessages.properties and are standardized as
      * {@code FQCN.value},
      * e.g. {@code com.flexive.shared.search.query.ValueComparator.LIKE}.
      *
      * @param value the enum value to be translated
      * @param args  optional arguments to be replaced in the localized messages
      * @return the localized label for the given enum value
      */
     public static FxString getEnumLabel(Enum<?> value, Object... args) {
         final Class<? extends Enum> valueClass = value.getClass();
         final String clsName;
         if (valueClass.getEnclosingClass() != null && Enum.class.isAssignableFrom(valueClass.getEnclosingClass())) {
             // don't include anonymous inner class definitions often used by enums in class name
             clsName = valueClass.getEnclosingClass().getName();
         } else {
             clsName = valueClass.getName();
         }
         return getMessage(SHARED_BUNDLE, clsName + "." + value.name(), args);
     }
 
 
     /**
      * Returns a list of all used step definitions for the given steps
      *
      * @param steps           list of steps to be examined
      * @param stepDefinitions all defined step definitions
      * @return a list of all used step definitions for this workflow
      */
     public static List<StepDefinition> getUsedStepDefinitions(List<? extends Step> steps, List<? extends StepDefinition> stepDefinitions) {
         List<StepDefinition> result = new ArrayList<StepDefinition>(steps.size());
         for (Step step : steps) {
             for (StepDefinition stepDefinition : stepDefinitions) {
                 if (step.getStepDefinitionId() == stepDefinition.getId()) {
                     result.add(stepDefinition);
                     break;
                 }
             }
         }
         return result;
     }
 
     /**
      * Splits the given text using separator. String literals are supported, e.g.
      * abc,def yields two elements, but 'abc,def' yields one (stringDelims = ['\''], separator = ',').
      *
      * @param text         the text to be splitted
      * @param stringDelims delimiters for literal string values, usually ' and "
      * @param separator    separator between tokens
      * @return split string
      */
     public static String[] splitLiterals(String text, char[] stringDelims, char separator) {
         if (text == null) {
             return new String[0];
         }
         List<String> result = new ArrayList<String>();
         Character currentStringDelim = null;
         int startIndex = 0;
         for (int i = 0; i < text.length(); i++) {
             char character = text.charAt(i);
             if (character == separator && currentStringDelim == null) {
                 // not in string
                 if (startIndex != -1) {
                     result.add(text.substring(startIndex, i).trim());
                 }
                 startIndex = i + 1;
             } else if (currentStringDelim != null && currentStringDelim == character) {
                 // end string
                 result.add(text.substring(startIndex, i).trim());
                 currentStringDelim = null;
                 startIndex = -1;
             } else if (currentStringDelim != null) {
                 // continue in string literal
             } else if (ArrayUtils.contains(stringDelims, character)) {
                 // begin string literal
                 currentStringDelim = character;
                 // skip string delim
                 startIndex = i + 1;
             }
         }
         if (startIndex != -1 && startIndex <= text.length()) {
             // add last parameter
             result.add(text.substring(startIndex, text.length()).trim());
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Splits the given comma-separated text. String literals are supported, e.g.
      * abc,def yields two elements, but 'abc,def' yields one.
      *
      * @param text the text to be splitted
      * @return split string
      */
     public static String[] splitLiterals(String text) {
         return splitLiterals(text, new char[]{'\'', '"'}, ',');
     }
 
     /**
      * Projects a single-parameter function on a hashmap.
      * Useful for calling parameterized functions from JSF-EL. Results are not cached by default, use
      * {@link #getMappedFunction(com.flexive.shared.FxSharedUtils.ParameterMapper, boolean)}
      * to create a caching mapper.
      *
      * @param mapper the parameter mapper wrapping the function to be called
      * @return a hashmap projected on the given parameter mapper
      */
     public static <K, V> Map<K, V> getMappedFunction(final ParameterMapper<K, V> mapper) {
         return new HashMap<K, V>() {
             private static final long serialVersionUID = 1051489436850755246L;
 
             @Override
             public V get(Object key) {
                 return mapper.get(key);
             }
         };
     }
 
     /**
      * Projects a single-parameter function on a hashmap.
      * Useful for calling parameterized functions from JSF-EL. Values returned by the mapper
      * can be cached in the hash map.
      *
      * @param mapper the parameter mapper wrapping the function to be called
      * @param cacheResults  if the mapper results should be cached by the map
      * @return a hashmap projected on the given parameter mapper
      * @since 3.1
      */
     public static <K, V> Map<K, V> getMappedFunction(final ParameterMapper<K, V> mapper, boolean cacheResults) {
         if (cacheResults) {
             return new HashMap<K, V>() {
                 private static final long serialVersionUID = 1051489436850755246L;
 
                 @SuppressWarnings({"unchecked"})
                 @Override
                 public V get(Object key) {
                     if (!containsKey(key)) {
                         put((K) key, mapper.get(key));
                     }
                     return super.get(key);
                 }
             };
         } else {
             return getMappedFunction(mapper);
         }
     }
 
     /**
      * Escape a path for usage on the current operating systems shell
      *
      * @param path path to escape
      * @return escaped path
      */
     public static String escapePath(String path) {
         if (WINDOWS)
             return "\"" + path + "\"";
         else
             return path.replace(" ", "\\ ");
 
     }
 
     /**
      * Escapes the given XPath for use in a public URI.
      *
      * @param xpath the xpath to be escaped
      * @return the given XPath for use in a public URI.
      * @see #decodeXPath(String)
      */
     public static String escapeXPath(String xpath) {
         return StringUtils.replace(xpath, "/", XPATH_ENCODEDSLASH);
     }
 
     /**
      * Decodes a previously escaped XPath.
      *
      * @param escapedXPath the escaped XPath
      * @return the decoded XPath
      * @see #escapeXPath(String)
      */
     public static String decodeXPath(String escapedXPath) {
         return StringUtils.replace(escapedXPath, XPATH_ENCODEDSLASH, "/");
     }
 
 
     /**
      * Returns <code>map.get(key)</code> if <code>key</code> exists, <code>defaultValue</code> otherwise.
      *
      * @param map          a map
      * @param key          the required key
      * @param defaultValue the default value to be returned if <code>key</code> does not exist in <code>map</code>
      * @return <code>map.get(key)</code> if <code>key</code> exists, <code>defaultValue</code> otherwise.
      */
     public static <K, V> V get(Map<K, V> map, K key, V defaultValue) {
         return map.containsKey(key) ? map.get(key) : defaultValue;
     }
 
     /**
      * Returns true if the given string value is quoted with the given character (e.g. 'value').
      *
      * @param value     the string value to be checked
      * @param quoteChar the quote character, for example '
      * @return true if the given string value is quoted with the given character (e.g. 'value').
      */
     public static boolean isQuoted(String value, char quoteChar) {
         return value != null && value.length() >= 2
                 && value.charAt(0) == quoteChar && value.charAt(value.length() - 1) == quoteChar;
     }
 
     /**
      * Strips the quotes from the given string if it is quoted, otherwise it returns
      * the input value itself.
      *
      * @param value     the value to be "unquoted"
      * @param quoteChar the quote character, for example '
      * @return the unquoted string, or <code>value</code>, if it was not quoted
      */
     public static String stripQuotes(String value, char quoteChar) {
         if (isQuoted(value, quoteChar)) {
             return value.substring(1, value.length() - 1);
         }
         return value;
     }
 
     /**
      * Returns the UTF-8 byte representation of the given string. Use this instead of
      * {@link String#getBytes()}, since the latter will fail if the system locale is not UTF-8.
      *
      * @param s the string to be processed
      * @return the UTF-8 byte representation of the given string
      */
     public static byte[] getBytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Failed to decode UTF-8 string: " + e.getMessage(), e);
            }
            return s.getBytes();
        }
     }
 
     /**
      * Extracts the names of the given enum elements and returns them as string.
      * Useful if the toString() method of the Enum class was overwritten.
      *
      * @param values the enum values
      * @return the names of the given enum elements
      */
     public static List<String> getEnumNames(Collection<? extends Enum> values) {
         final List<String> result = new ArrayList<String>(values.size());
         for (final Enum value : values) {
             result.add(value.name());
         }
         return result;
     }
 
     /**
      * Extract the unique IDs of the given {@link SelectableObject} collection.
      *
      * @param values the input values
      * @return the IDs of the input values
      * @since 3.1
      */
     public static List<Long> getSelectableObjectIdList(Iterable<? extends SelectableObject> values) {
         final List<Long> result = new ArrayList<Long>();
         for (SelectableObject value : values) {
             result.add(value.getId());
         }
         return result;
     }
 
     /**
      * Extract the unique names of the given {@link SelectableObject} collection.
      *
      * @param values the input values
      * @return the IDs of the input values
      * @since 3.1
      */
     public static List<String> getSelectableObjectNameList(Iterable<? extends SelectableObjectWithName> values) {
         final List<String> result = new ArrayList<String>();
         for (SelectableObjectWithName value : values) {
             result.add(value.getName());
         }
         return result;
     }
 
     /**
      * Returns the index of the {@link SelectableObject} with the given ID, or -1 if none was found.
      *
      * @param values the values to be examined
      * @param id     the target ID
      * @return the index of the {@link SelectableObject} with the given ID, or -1 if none was found.
      */
     public static int indexOfSelectableObject(List<? extends SelectableObject> values, long id) {
         for (int i = 0; i < values.size(); i++) {
             final SelectableObject object = values.get(i);
             if (object.getId() == id) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Return a {@link SelectableObject} by its ID.
      *
      * @param values    the values to search
      * @param id        the ID to look for
      * @param <T>       the value type
      * @return  the first matching value
      * @throws com.flexive.shared.exceptions.FxRuntimeException if no element with the given ID was found
      * @since 3.2.0
      */
     public static <T extends SelectableObject> T getSelectableObject(List<T> values, long id) {
         for (T value : values) {
             if (value.getId() == id) {
                 return value;
             }
         }
         throw new FxNotFoundException("ex.selectable.id.notFound", id).asRuntimeException();
     }
 
     /**
      * Return the elements of {@code values} that match the given {@code ids}.
      *
      * @param values the values to be search
      * @param ids    the required IDs
      * @param <T>    the value type
      * @return the elements of {@code values} that match the given {@code ids}.
      * @since 3.1
      */
     public static <T extends SelectableObject> List<T> filterSelectableObjectsById(Iterable<T> values, Collection<Long> ids) {
         final List<T> result = Lists.newArrayListWithCapacity(ids.size());
         for (T value : values) {
             if (ids.contains(value.getId())) {
                 result.add(value);
             }
         }
         return result;
     }
 
     /**
      * Return the elements of {@code values} that match the given {@code names}.
      *
      * @param values the values to be search
      * @param names  the required IDs
      * @param <T>    the value type
      * @return the elements of {@code values} that match the given {@code names}.
      * @since 3.1
      */
     public static <T extends SelectableObjectWithName> List<T> filterSelectableObjectsByName(Iterable<T> values, Collection<String> names) {
         final List<T> result = Lists.newArrayListWithCapacity(names.size());
         for (T value : values) {
             if (names.contains(value.getName())) {
                 result.add(value);
             }
         }
         return result;
     }
 
     /**
      * Primitive int comparison method (when JDK7's Integer#compare cannot be used).
      * For float and double, see {@link org.apache.commons.lang.NumberUtils}.
      *
      * @param i1    the first value
      * @param i2    the second value
      * @return      see {@link Integer#compareTo}
      * @since       3.2.0
      */
     public static int compare(int i1, int i2) {
         if (i1 < i2) {
             return -1;
         } else if (i1 == i2) {
             return 0;
         } else {
             return 1;
         }
     }
 
     /**
      * Primitive long comparison method (when JDK7's Long#compare cannot be used).
      * For float and double, see {@link org.apache.commons.lang.NumberUtils}.
      *
      * @param i1    the first value
      * @param i2    the second value
      * @return      see {@link Integer#compareTo}
      * @since       3.2.0
      */
     public static int compare(long i1, long i2) {
         if (i1 < i2) {
             return -1;
         } else if (i1 == i2) {
             return 0;
         } else {
             return 1;
         }
     }
 
     /**
      * Comparator for sorting Assignments according to their position.
      */
     public static class AssignmentPositionSorter implements Comparator<FxAssignment>, Serializable {
         private static final long serialVersionUID = 9197582519027523108L;
 
         public int compare(FxAssignment o1, FxAssignment o2) {
             return FxSharedUtils.compare(o1.getPosition(), o2.getPosition());
         }
     }
 
     /**
      * Comparator for sorting {@link SelectableObjectWithName} instances by ID.
      */
     public static class SelectableObjectSorter implements Comparator<SelectableObject>, Serializable {
         private static final long serialVersionUID = -1786371691872260074L;
 
         public int compare(SelectableObject o1, SelectableObject o2) {
             return FxSharedUtils.compare(o1.getId(), o2.getId());
         }
     }
 
     /**
      * Comparator for sorting {@link SelectableObjectWithName} instances by name.
      */
     public static class SelectableObjectWithNameSorter implements Comparator<SelectableObjectWithName>, Serializable {
         private static final long serialVersionUID = -1786371691872260074L;
 
         public int compare(SelectableObjectWithName o1, SelectableObjectWithName o2) {
             return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
         }
     }
 
     /**
      * Comparator for sorting {@link SelectableObjectWithLabel} instances by label.
      */
     public static class SelectableObjectWithLabelSorter implements Comparator<SelectableObjectWithLabel>, Serializable {
         public int compare(SelectableObjectWithLabel o1, SelectableObjectWithLabel o2) {
             return o1.getLabel().getBestTranslation().toLowerCase().compareTo(o2.getLabel().getBestTranslation().toLowerCase());
         }
     }
 
     /**
      * Item sorter by position
      */
     public static class ItemPositionSorter implements Comparator<FxSelectListItem>, Serializable {
         private static final long serialVersionUID = 3366660003069358959L;
 
         public int compare(FxSelectListItem i1, FxSelectListItem i2) {
             return FxSharedUtils.compare(i1.getPosition(), i2.getPosition());
         }
     }
 
     /**
      * Item sorter by label
      */
     public static class ItemLabelSorter implements Comparator<FxSelectListItem>, Serializable {
         private static final long serialVersionUID = 2366364003069358945L;
         private final FxLanguage language;
         private final Collator collator;
         private final Map<String, CollationKey> uppercaseLabels;
 
         /**
          * Ctor
          *
          * @param language the language used for sorting
          */
         public ItemLabelSorter(FxLanguage language) {
             this.language = language;
             this.collator = Collator.getInstance(language.getLocale());
             this.uppercaseLabels = Maps.newHashMap();   // cache collation keys for case-insensitive comparisons
         }
 
         /**
          * {@inheritDoc}
          */
         public int compare(FxSelectListItem i1, FxSelectListItem i2) {
             final String label1 = i1.getLabel().getBestTranslation(language);
             final String label2 = i2.getLabel().getBestTranslation(language);
             if (!uppercaseLabels.containsKey(label1)) {
                 uppercaseLabels.put(label1, collator.getCollationKey(StringUtils.defaultString(label1).toUpperCase(language.getLocale())));
             }
             if (!uppercaseLabels.containsKey(label2)) {
                 uppercaseLabels.put(label2, collator.getCollationKey(StringUtils.defaultString(label2).toUpperCase(language.getLocale())));
             }
             return uppercaseLabels.get(label1).compareTo(uppercaseLabels.get(label2));
         }
     }
 
     /**
      * An SQL executor, similar to ant's sql task
      * An important addition are raw blocks:
      * lines starting with '-- @START@' indicate the start of a raw block and lines starting with '-- @END@'
      * indicate the end of a raw block.
      * Raw blocks are passed "as is" to the database as one string
      */
     public static class SQLExecutor {
         private Connection con;
         private Statement stmt = null;
         private String code;
         private int count = 0;
         private String delimiter;
         private boolean keepformat;
         private boolean rowDelimiter;
         private PrintStream out;
 
         /**
          * Ctor
          *
          * @param con          an open and valid connection
          * @param code         the source sql code
          * @param delimiter    delimiter to use
          * @param rowDelimiter is the delimiter or row delimiter?
          * @param keepformat   keep original format?
          * @param out          stream for messages
          */
         public SQLExecutor(Connection con, String code, String delimiter, boolean rowDelimiter, boolean keepformat, PrintStream out) {
             this.con = con;
             this.code = code;
             this.delimiter = delimiter;
             this.rowDelimiter = rowDelimiter;
             this.keepformat = keepformat;
             this.out = out;
         }
 
         /**
          * Main execute method, returns number of updates
          *
          * @return number of updates
          * @throws SQLException on errors
          * @throws IOException  on errors
          */
         public int execute() throws SQLException, IOException {
             stmt = con.createStatement();
             try {
                 StringBuffer sql = new StringBuffer();
                 String line;
                 BufferedReader in = new BufferedReader(new StringReader(code));
                 boolean inRawBlock = false;
                 while ((line = in.readLine()) != null) {
                     line = line.trim();
                     if (inRawBlock) {
                         if (line.startsWith("--") && line.indexOf("@END@") > 0) {
                             inRawBlock = false;
                             if (sql.length() > 0) {
 //                                System.out.println("Executing raw block:\n" + sql.toString());
                                 execute(sql.toString());
                                 sql.replace(0, sql.length(), "");
                             }
                         } else
                             sql.append(line).append('\n');
                         continue;
                     }
                     if (line.startsWith("//") || line.startsWith("--")) {
                         if (line.indexOf("@START@") > 0)
                             inRawBlock = true;
                         continue;
                     }
                     StringTokenizer st = new StringTokenizer(line);
                     if (st.hasMoreTokens()) {
                         String token = st.nextToken();
                         if ("REM".equalsIgnoreCase(token))
                             continue;
                     }
                     sql.append("\n").append(line);
                     if (!keepformat && line.indexOf("--") >= 0)
                         sql.append("\n");
                     if (!rowDelimiter && StringUtils.endsWith(sql.toString(), delimiter)
                             || (rowDelimiter && line.equals(delimiter))) {
                         execute(sql.substring(0, sql.length() - delimiter.length()));
                         sql.replace(0, sql.length(), "");
                     }
                 }
                 if (sql.length() > 0)
                     execute(sql.toString());
             } finally {
                 if (stmt != null)
                     stmt.close();
             }
             return count;
         }
 
         /**
          * Execute a single SQL statement
          *
          * @param sql statement
          * @throws SQLException on errors
          */
         private void execute(String sql) throws SQLException {
             if ("".equals(sql.trim()))
                 return;
 
             ResultSet rs = null;
             try {
                 count++;
 
                 boolean ret;
                 stmt.execute(sql);
                 rs = stmt.getResultSet();
                 do {
                     ret = stmt.getMoreResults();
                     if (ret) {
                         rs = stmt.getResultSet();
                     }
                 } while (ret);
 
                 @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"}) SQLWarning warning = con.getWarnings();
                 while (warning != null) {
                     out.println("Warning: " + warning);
                     warning = warning.getNextWarning();
                 }
                 con.clearWarnings();
             } catch (SQLException e) {
                 out.println("Failed to execute: " + sql);
                 throw e;
             } finally {
                 if (rs != null) {
                     try {
                         rs.close();
                     } catch (SQLException e) {
                         //ignore
                     }
                 }
             }
 
         }
     }
 
     /**
      * A resource bundle reference.
      */
     public static class BundleReference {
         private final String baseName;
         private final URL resourceURL;
 
         /**
          * Create a new bundle reference.
          *
          * @param baseName    the fully qualified base name (e.g. "ApplicationResources")
          * @param resourceURL the resource URL to be used for loading the resource bundle. If null,
          *                    the context class loader will be used.
          */
         public BundleReference(String baseName, URL resourceURL) {
             this.baseName = baseName;
             this.resourceURL = resourceURL;
         }
 
         /**
          * Returns the base name of the resource bundle (e.g. "ApplicationResources").
          *
          * @return the base name of the resource bundle (e.g. "ApplicationResources").
          */
         public String getBaseName() {
             return baseName;
         }
 
         /**
          * Returns the class loader to be used for loading the bundle.
          *
          * @return the class loader to be used for loading the bundle.
          */
         public URL getResourceURL() {
             return resourceURL;
         }
 
         /**
          * Return the resource bundle in the given locale.
          *
          * @param locale the requested locale
          * @return the resource bundle in the given locale.
          */
         public ResourceBundle getBundle(Locale locale) {
             if (this.resourceURL == null) {
                 return ResourceBundle.getBundle(baseName, locale);
             } else {
                 try {
                     return ResourceBundle.getBundle(baseName, locale, new URLClassLoader(new URL[]{resourceURL}));
                 } catch (MissingResourceException mre) {
                     //fix for JBoss 5 vfs which doesn't work with classloader
                     try {
                         //try to find in the desired locale
                         Enumeration<URL> e = Thread.currentThread().getContextClassLoader().getResources(baseName + "_" + locale.getLanguage() + ".properties");
                         String orgPath = resourceURL.toExternalForm().substring(0, resourceURL.toExternalForm().lastIndexOf("/"));
                         while (e.hasMoreElements()) {
                             URL resource = e.nextElement();
                             if (orgPath.equals(resource.toExternalForm().substring(0, resource.toExternalForm().lastIndexOf("/")))) {
                                 return new PropertyResourceBundle(resource.openStream());
                             }
                         }
                         //Fallback to the default locale
                         return new PropertyResourceBundle(resourceURL.openStream());
                     } catch (IOException e) {
                         LOG.warn("Failed to retrieve bundle " + baseName + " directly from stream");
                     }
                     //last resort
                     return ResourceBundle.getBundle(baseName, locale);
                 }
             }
         }
 
         /**
          * Return a cache key unique for this resource bundle and locale.
          *
          * @param locale the requested locale
          * @return a cache key unique for this resource bundle and locale.
          */
         public String getCacheKey(Locale locale) {
             final String localeSuffix = locale == null ? "" : "_" + locale.toString();
             if (this.resourceURL == null) {
                 return baseName + localeSuffix;
             } else {
                 return baseName + this.toString() + localeSuffix;
             }
         }
     }
 
     /**
      * Add a resource reference for the given resource base name.
      *
      * @param baseName the resource name (e.g. "ApplicationResources")
      * @return a List of BundleReferences
      * @throws IOException if an I/O error occured while looking for resources
      */
     public static List<BundleReference> addMessageResources(String baseName) throws IOException {
         // scan classpath
         final Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(baseName + ".properties");
         List<FxSharedUtils.BundleReference> refs = new ArrayList<FxSharedUtils.BundleReference>(5);
         while (resources.hasMoreElements()) {
             final URL resourceURL = resources.nextElement();
             try {
                 if ("vfszip".equals(resourceURL.getProtocol()) || "vfs".equals(resourceURL.getProtocol())) {
                     refs.add(new BundleReference(baseName, resourceURL));
                     continue;
                 }
                 // expected format: file:/some/path/to/file.jar!{baseName}.properties if this is no JBoss 5 vfs zipfile
                 final int jarDelim = resourceURL.getPath().lastIndexOf(".jar!");
 
                 String path = resourceURL.getPath();
                 if (!path.startsWith("file:")) {
                     if (path.startsWith("/") || path.charAt(1) == ':') {
                         LOG.warn("Trying a filesystem message resource without an explicit file: protocol identifier for " + path);
                         refs.add(new BundleReference(baseName, resourceURL));
                         continue;
                     } else {
                         LOG.warn("Cannot use message resources because they are not served from the file system: " + resourceURL.getPath());
                         continue;
                     }
                 } else if (jarDelim != -1) {
                     path = path.substring("file:".length(), jarDelim + 4);
                 } 
 
                 // "file:" and everything after ".jar" gets stripped for the class loader URL
                 final URL jarURL = new URL("file", null, path);
                 refs.add(new BundleReference(baseName, jarURL));
 
                 LOG.info("Added message resources for " + resourceURL.getPath());
             } catch (Exception e) {
                 LOG.error("Failed to add message resources for URL " + resourceURL.getPath() + ": " + e.getMessage(), e);
             }
         }
         return refs;
     }
 
     /**
      * Close the given resources and log a warning message if closing fails.
      *
      * @param resources the resource(s) to be closed
      * @since 3.1
      */
     public static void close(Closeable... resources) {
         if (resources != null) {
             for (Closeable resource : resources) {
                 if (resource != null) {
                     try {
                         resource.close();
                     } catch (IOException e) {
                         if (LOG.isWarnEnabled()) {
                             LOG.warn("Failed to close resource " + resource + ": " + e.getMessage(), e);
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * @return true if only a minimum set of runonce scripts should be installed.
      * @see #PROP_RUNONCE_MINIMAL
      * @since 3.1
      */
     public static boolean isMinimalRunOnceScripts() {
         return FxContext.get().getDivisionId() == DivisionData.DIVISION_TEST
                 && System.getProperty(PROP_RUNONCE_MINIMAL) != null;
     }
 
     /**
      * Resource message key for caching
      */
     public static class MessageKey {
         private final Locale locale;
         private final String key;
 
         public MessageKey(Locale locale, String key) {
             this.locale = locale;
             this.key = key;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             MessageKey that = (MessageKey) o;
 
             if (key != null ? !key.equals(that.key) : that.key != null) return false;
             if (locale != null ? !locale.equals(that.locale) : that.locale != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = locale != null ? locale.hashCode() : 0;
             result = 31 * result + (key != null ? key.hashCode() : 0);
             return result;
         }
     }
 
     /**
      * This method checks if the current assignment is a derived assignment subject to the following conditions:
      * 1.) must be assigned to a derived type
      * 2.) must be inherited from the derived type's parent
      * 3.) XPaths must match
      *
      * @param assignment an FxAssignment
      * @param <T>        extends FxAssignment
      * @return true if conditions are met
      * @since 3.1.1
      */
     public static <T extends FxAssignment> boolean checkAssignmentInherited(T assignment) {
         if (assignment == null)
             return false;
         // "REAL" inheritance only works for derived types
         if (assignment.getAssignedType().isDerived()) {
             final FxAssignment baseAssignment;
             // temp. assignments might not be found (id = -1)  
             try {
                 baseAssignment = CacheAdmin.getEnvironment().getAssignment(assignment.getBaseAssignmentId());
                 long baseTypeId = baseAssignment.getAssignedType().getId();
                 // type must be derived and the assignment must be part of the inheritance chain
                 return assignment.getAssignedType().isDerivedFrom(baseTypeId) &&
                         XPathElement.stripType(baseAssignment.getXPath()).equals(XPathElement.stripType(assignment.getXPath()));
             } catch (Exception e) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Assignment inheritance could not be determined (probably due to a temp. assignment id of -1");
                 }
             }
         }
         return false;
     }
 
     /**
      * @return  the name of the current network node (used by flexive for the {@link com.flexive.shared.interfaces.NodeConfigurationEngine}.
      * @since   3.2.0
      */
     public static synchronized String getNodeId() {
         if (NODE_ID == null) {
             NODE_ID = System.getProperty("flexive.nodename");
             if (StringUtils.isBlank(NODE_ID)) {
                 NODE_ID = getHostName();
             }
             if (LOG.isInfoEnabled()) {
                 LOG.info("Determined nodename (override with system property flexive.nodename): " + NODE_ID);
             }
         }
         return NODE_ID;
     }
 }
