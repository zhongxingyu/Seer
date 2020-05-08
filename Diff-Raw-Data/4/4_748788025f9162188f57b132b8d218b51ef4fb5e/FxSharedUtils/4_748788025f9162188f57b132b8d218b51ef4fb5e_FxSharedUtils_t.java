 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
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
 
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.structure.FxAssignment;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import static org.apache.commons.lang.StringUtils.defaultString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.Collator;
 import java.util.*;
 import java.util.jar.JarEntry;
 import java.util.jar.JarInputStream;
 
 /**
  * Flexive shared utility functions.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class FxSharedUtils {
     private static final Log LOG = LogFactory.getLog(FxSharedUtils.class);
 
     /**
      * Shared message resources bundle
      */
     public static final String SHARED_BUNDLE = "FxSharedMessages";
 
     private static String fxVersion = "3.1";
     private static String fxEdition = "framework";
     private static String fxProduct = "[fleXive]";
     private static String fxBuild = "unknown";
     private static long fxDBVersion = -1L;
     private static String fxBuildDate = "unknown";
     private static String fxBuildUser = "unknown";
     private static String fxHeader = "[fleXive]";
     private static String bundledGroovyVersion = "unknown";
     private static List<String> translatedLocales = Arrays.asList("en");
 
 
     /**
      * The character(s) representing a "xpath slash" (/) in a public URL.
      */
     public static final String XPATH_ENCODEDSLASH = "#";
     /**
      * Browser tests set this cookie to force using the test division instead of the actual division
      * defined by the URL domain.
      * TODO: security?
      */
     public static final String COOKIE_FORCE_TEST_DIVISION = "ForceTestDivision";
 
     private static List<String> drops;
     private static List<FxDropApplication> dropApplications;
 
     public static MessageDigest digest = null;
 
     /**
      * Are JDK 6+ extensions allowed to be run on the current VM?
      */
     public final static boolean USE_JDK6_EXTENSION;
     public final static boolean WINDOWS = System.getProperty("os.name").indexOf("Windows") >= 0;
     private static final String FLEXIVE_PROPERTIES = "flexive-application.properties";
 
     static {
         int major = -1, minor = -1;
         try {
             String[] ver = System.getProperty("java.specification.version").split("\\.");
             if (ver.length >= 2) {
                 major = Integer.valueOf(ver[0]);
                 minor = Integer.valueOf(ver[1]);
             }
         } catch (Exception e) {
             LOG.error(e);
         }
         USE_JDK6_EXTENSION = major > 1 || (major == 1 && minor >= 6);
 
         try {
             PropertyResourceBundle bundle = (PropertyResourceBundle) PropertyResourceBundle.getBundle("flexive");
             fxVersion = bundle.getString("flexive.version");
             fxEdition = bundle.getString("flexive.edition");
             fxProduct = bundle.getString("flexive.product");
             fxBuild = bundle.getString("flexive.buildnumber");
             fxDBVersion = Long.valueOf(bundle.getString("flexive.dbversion"));
             fxBuildDate = bundle.getString("flexive.builddate");
             fxBuildUser = bundle.getString("flexive.builduser");
             fxHeader = bundle.getString("flexive.header");
             bundledGroovyVersion = bundle.getString("flexive.bundledGroovyVersion");
             final String languagesValue = bundle.getString("flexive.translatedLocales");
             if (StringUtils.isNotBlank(languagesValue)) {
                 final String[] languages = StringUtils.split(languagesValue, ",");
                 for (int i = 0; i < languages.length; i++) {
                     languages[i] = languages[i].trim();
                 }
                 translatedLocales = Arrays.asList(languages);
             }
         } catch (Exception e) {
             LOG.error(e);
         }
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
      */
     public static Map<String, String> getContentsFromJarStream(JarInputStream jarStream, String searchPattern, boolean isFile) {
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
                 LOG.info("Found " + found + " entries in the JarInputStream for the pattern " + searchPattern);
             }
         } catch (Exception e) {
             LOG.error("Failed to load entries from JAR file: " + e.getMessage(), e);
         } finally {
             if (jarStream != null) {
                 try {
                     jarStream.close();
                 } catch (IOException e) {
                     LOG.warn("Failed to close stream: " + e.getMessage(), e);
                 }
             } else {
                 LOG.error("JarInputStream parameter was null, no search performed");
             }
         }
 
         if (jarContents.isEmpty())
                 jarContents = null;
         return jarContents;
     }
 
     /**
      * Reads the content of a given entry in a Jar file (JarInputStream) and returns it as a String
      * 
      * @param jarStream the given JarInputStream
      * @param entry     the given entry in the jar file
      * @return the entry's content as a String
      */
     public static String readFromJarEntry(JarInputStream jarStream, JarEntry entry) {
         String fileContent = "";
         try {
             if (entry.getSize() >= 0) {
                 // allocate buffer for the entire (uncompressed) script code
                 final byte[] buffer = new byte[(int) entry.getSize()];
                 // decompress JAR entry
                 if (jarStream.read(buffer, 0, (int) entry.getSize()) != entry.getSize()) {
                     LOG.error("Failed to read complete script code for script: " + entry.getName());
                 }
                 fileContent = new String(buffer, "UTF-8").trim();
             } else {
                 // use this method if the file size cannot be determined
                 //(might be the case with jar files created with some jar tools)
                 int currentByte;
                 while ((currentByte = jarStream.read()) != -1) {
                     fileContent = fileContent + (char) currentByte;
                 }
             }
         } catch (Exception e) {
             LOG.error(e.getMessage());
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
                     Thread.currentThread().getContextClassLoader().getResources(FLEXIVE_PROPERTIES);
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
                     String path = url.getPath().replace("/" + FLEXIVE_PROPERTIES, "");
                     if (path.endsWith("!")) {
                         path = StringUtils.chop(path);
                     }
                     dropApplications.add(
                             new FxDropApplication(
                                     name,
                                     defaultString(contextRoot, name),
                                     defaultString(displayName, name),
                                     path
                             )
                     );
                 }
             }
         } catch (IOException e) {
             LOG.error("Failed to initialize drops from the classpath: " + e.getMessage(), e);
         }
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
         for (int i = 0; i < columnNames.length; i++) {
             final String columnName = columnNames[i];
             final String upperColumn = columnName.toUpperCase();
             if (upperColumn.equals(upperName) || upperColumn.endsWith("." + upperName)) {
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
      * Compute the hash of the given flexive password.
      *
      * @param accountId the user account ID
      * @param password  the cleartext password
      * @return a hashed password
      */
     public synchronized static String hashPassword(long accountId, String password) {
         try {
             return sha1Hash(getBytes("FX-SALT" + accountId + password));
         } catch (NoSuchAlgorithmException e) {
             throw new FxCreateException("Failed to load the SHA1 algorithm.").asRuntimeException();
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
      * Helperclass holding the result of the <code>executeCommand</code> method
      *
      * @see FxSharedUtils#executeCommand(String,String...)
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
         StringBuilder sb = new StringBuilder(length > 0 ? length : 5000);
         try {
             int read;
             byte[] buffer = new byte[1024];
             while ((read = in.read(buffer)) != -1) {
                 sb.append(new String(buffer, 0, read, "UTF-8"));
             }
         } catch (IOException e) {
             LOG.error(e.getMessage(), e);
         } finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     // ignore
                 }
             }
         }
         return sb.toString();
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
     public static String getBundledGroovyVersion() {
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
             LOG.error("No key given!", new Throwable());
             return "##NO_KEY_GIVEN";
         }
         String resource = lookupResource(resourceBundle, key, localeIso);
         if (resource == null) {
             LOG.warn("Called with unlocalized Message [" + key + "]. See StackTrace for origin!", new Throwable());
             return key;
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
      * Useful for calling parameterized functions from JSF-EL. Values returned by the mapper
      * are cached in the hash map.
      *
      * @param mapper the parameter mapper wrapping the function to be called
      * @return a hashmap projected on the given parameter mapper
      */
     public static <K, V> Map<K, V> getMappedFunction(final ParameterMapper<K, V> mapper) {
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
      */
     public static long[] getSelectableObjectIds(Collection<? extends SelectableObject> values) {
         final long[] result = new long[values.size()];
         int idx = 0;
         for (SelectableObject object : values) {
             result[idx++] = object.getId();
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
      * Comparator for sorting Assignments according to their position.
      */
     public static class AssignmentPositionSorter implements Comparator<FxAssignment>, Serializable {
         private static final long serialVersionUID = 9197582519027523108L;
 
         public int compare(FxAssignment o1, FxAssignment o2) {
             if (o1.getPosition() < o2.getPosition())
                 return -1;
             else if (o1.getPosition() == o2.getPosition())
                 return 0;
             else return 1;
         }
     }
 
     /**
      * Comparator for sorting {@link SelectableObjectWithName} instances by ID.
      */
     public static class SelectableObjectSorter implements Comparator<SelectableObject>, Serializable {
         private static final long serialVersionUID = -1786371691872260074L;
 
         public int compare(SelectableObject o1, SelectableObject o2) {
             return o1.getId() > o2.getId()
                     ? 1 : o1.getId() < o2.getId()
                     ? -1 : 0;
         }
     }
 }
