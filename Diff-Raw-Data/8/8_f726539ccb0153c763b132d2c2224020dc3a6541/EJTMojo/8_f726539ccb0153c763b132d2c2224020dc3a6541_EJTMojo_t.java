 package net.pardini.ejtpkg;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Splitter;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.io.Files;
 import org.apache.commons.compress.archivers.ArchiveEntry;
 import org.apache.commons.compress.archivers.ArchiveInputStream;
 import org.apache.commons.compress.archivers.ArchiveStreamFactory;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.settings.MavenSettingsBuilder;
 import org.apache.maven.settings.Proxy;
 import org.apache.maven.settings.Settings;
 import org.apache.maven.wagon.proxy.ProxyInfo;
 import org.sonatype.inject.Nullable;
 import org.stringtemplate.v4.ST;
 
 import java.io.*;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.*;
 import java.util.zip.GZIPInputStream;
 
 /**
  * The Mojo.
  *
  * @goal ejt
  * @requiresProject
  */
 public class EJTMojo extends AbstractMojo {
 // ------------------------------ FIELDS ------------------------------
 
     /**
      * Directory the project.
      *
      * @parameter default-value="${project.build.directory}"
      * @required
      */
     protected File projectBuildDir;
 
 
     /**
      * The final name of the project
      *
      * @parameter default-value="${project.build.finalName}
      * @required
      */
     protected String finalName;
     private final Map<String, String> templateValues = new HashMap<String, String>();
 
     private final Log log = this.getLog();
 
 
     /**
      * @component
      * @required
      * @readonly
      */
     private MavenSettingsBuilder mavenSettingsBuilder;
 
 
     /**
      * @parameter
      */
     private String maxPermSizeMb;
     /**
      * @parameter
      */
     private String maxHeapSizeMb;
     /**
      * @parameter
      */
     private String serviceName;
     /**
      * @parameter
      */
     private String serviceDescription;
     /**
      * @parameter
      */
     private String httpPort;
     /**
      * @parameter
      */
     private String ajpPort;
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Mojo ---------------------
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         log.info(String.format("Build dir is %s", projectBuildDir));
         log.info(String.format("Final name is %s", finalName));
         File explodedWarDir = new File(projectBuildDir, finalName);
         createEjtDir(projectBuildDir, explodedWarDir, finalName);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     protected File createEjtDir(final File projectBuildDir, final File explodedWarDir, final String webAppDirName) {
         if ((projectBuildDir == null) || (!projectBuildDir.exists()))
             throw new RuntimeException("output dir not found");
         if ((explodedWarDir == null) || (!explodedWarDir.exists()))
             throw new RuntimeException("war:exploded dir not found");
 
        addToTemplate("webAppDirName", webAppDirName);
        addToTemplate("maxPermSizeMb", maxPermSizeMb, "256");
        addToTemplate("maxHeapSizeMb", maxHeapSizeMb, "2048");
         addToTemplate("httpPort", httpPort, "8080");
         addToTemplate("ajpPort", ajpPort, "8009");
         addToTemplate("serviceName", serviceName, String.format("%sTomcat7", webAppDirName));
         addToTemplate("serviceDescription", serviceDescription, String.format("%s - Tomcat 7", webAppDirName));
 
        File realOutputDir = new File(projectBuildDir, addToTemplate("outputDirName", String.format("%sJRETomcatRunnerWin64", webAppDirName)));
         try {
             if (realOutputDir.exists()) realOutputDir.mkdirs();
 
             Map<String, byte[]> finalFiles = new LinkedHashMap<String, byte[]>();
             final List<String> neededFilesList = getNeededFilesList();
 
             finalFiles.putAll(filterEntriesByList(readEntriesFromArchive(downloadJREFromOracle("http://download.oracle.com/otn-pub/java/jdk/7u11-b21/jre-7u11-windows-x64.tar.gz"), "jre"), neededFilesList));
             finalFiles.putAll(filterEntriesByList(readEntriesFromArchive(downloadTomcatFromApache("http://apache.mirror.pop-sc.rnp.br/apache/tomcat/tomcat-7/v7.0.35/bin/apache-tomcat-7.0.35-windows-x64.zip"), "tomcat"), neededFilesList));
 
             addResourceFileDirectly(finalFiles, "conf/catalina.policy");
             addResourceFileDirectly(finalFiles, "conf/catalina.properties");
             addResourceFileDirectly(finalFiles, "conf/context.xml");
             addResourceFileDirectly(finalFiles, "conf/logging.properties");
             addResourceFileDirectly(finalFiles, "conf/web.xml");
 
             addResourceFileFiltered(finalFiles, "conf/server.xml");
 
             addFilteredCombinedWindowsScriptFile(finalFiles, "installAsService.bat");
             addFilteredCombinedWindowsScriptFile(finalFiles, "removeService.bat");
             addFilteredCombinedWindowsScriptFile(finalFiles, "run.bat");
 
             addAllRealFilesFromDir(finalFiles, explodedWarDir, webAppDirName);
 
             log.info(finalFiles.keySet().toString());
 
             writeFilesToOutputDir(realOutputDir, finalFiles);
 
             return realOutputDir;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private String addToTemplate(final String key, final String value, final String defaultValue) {
         return addToTemplate(key, ((StringUtils.trimToNull(value) == null)) ? defaultValue : value);
     }
 
     private String addToTemplate(final String key, final String value) {
         templateValues.put(key, value);
         return value;
     }
 
     private List<String> getNeededFilesList() throws IOException {
         String fileToRead = getContentsOfResourceFileAsString("windows/neededfiles.txt");
         return Lists.newArrayList(Splitter.on("\n").omitEmptyStrings().trimResults().split(fileToRead));
     }
 
     private String getContentsOfResourceFileAsString(String fileToRead) {
         try {
             return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(fileToRead), "UTF-8");
         } catch (IOException e) {
             throw new RuntimeException(String.format("%s: %s", e.getMessage(), fileToRead), e);
         }
     }
 
     private File downloadJREFromOracle(String downloadURL) {
         return getFileToTemp(downloadURL);
     }
 
     private File getFileToTemp(final String downloadURL) {
         URL url = null;
         try {
             url = new URL(downloadURL);
         } catch (MalformedURLException e) {
             throw new RuntimeException(String.format("%s", e.getMessage()), e);
         }
 
         log.info("URL: " + url);
 
         File tempFile = new File(System.getProperty("java.io.tmpdir"), FilenameUtils.getName(url.getFile()));
         log.info("Temp file: " + tempFile.getAbsolutePath());
 
         if (tempFile.exists()) return tempFile;
 
         File downloadedFile = downloadFile(downloadURL, tempFile);
         return downloadedFile;
     }
 
     private File downloadFile(final String url, final File downloadToFile) {
         HttpClient client = new DefaultHttpClient();
         HttpGet httpGet = new HttpGet(url);
         try {
             httpGet.setHeader(new BasicHeader("Cookie", "gpw_e24=http%3A%2F%2Fwww.oracle.com"));
             HttpResponse execute = client.execute(httpGet);
             log.info("Resp code: " + execute.getStatusLine().toString());
             log.info("Downloading " + execute.getFirstHeader("Content-Length").getValue() + " bytes.");
 
             HttpEntity entity = execute.getEntity();
             if (entity != null) {
                 FileOutputStream fos = new FileOutputStream(downloadToFile);
                 entity.writeTo(fos);
                 fos.close();
             }
 
             return downloadToFile;
         } catch (Exception e) {
             log.error("IOException", e);
         }
         return null;
     }
 
     private Map<String, byte[]> filterEntriesByList(final Map<String, byte[]> allDownloadedFiles, final List<String> list) {
         return Maps.filterKeys(allDownloadedFiles, new Predicate<String>() {
             @Override
             public boolean apply(@Nullable final String entryName) {
                 return list.contains(entryName);
             }
         });
     }
 
     private Map<String, byte[]> readEntriesFromArchive(final File inputFile, final String firstDir) throws Exception {
         HashMap<String, byte[]> stringHashMap = new HashMap<String, byte[]>();
         InputStream is;
         ArchiveInputStream archiveInputStream;
         try {
             is = new BufferedInputStream(new FileInputStream(inputFile));
             archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(is);
         } catch (Exception e) {
             is = new BufferedInputStream(new GZIPInputStream(new FileInputStream((inputFile))));
             archiveInputStream = new ArchiveStreamFactory().createArchiveInputStream(is);
         }
 
         ArchiveEntry entry = null;
         while ((entry = archiveInputStream.getNextEntry()) != null) {
             if (!entry.isDirectory()) {
                 String fullName = entry.getName();
                 String key = removeFirstPartOfPath(fullName);
                 if (firstDir != null) key = String.format("%s/%s", firstDir, key);
                 stringHashMap.put(key, IOUtils.toByteArray(archiveInputStream));
             }
         }
         archiveInputStream.close();
         return stringHashMap;
     }
 
     private String removeFirstPartOfPath(final String fullName) {
         return fullName.substring(fullName.indexOf("/") + 1);
     }
 
     private File downloadTomcatFromApache(final String url) {
         return getFileToTemp(url);
     }
 
     private void addResourceFileDirectly(final Map<String, byte[]> filteredFiles, final String resourceFile) {
         filteredFiles.put(resourceFile, getContentsOfResourceFileAsByteArray(resourceFile));
     }
 
     private byte[] getContentsOfResourceFileAsByteArray(String fileToRead) {
         try {
             return IOUtils.toByteArray(this.getClass().getClassLoader().getResourceAsStream(fileToRead));
         } catch (IOException e) {
             throw new RuntimeException(String.format("%s", e.getMessage()), e);
         }
     }
 
     private void addResourceFileFiltered(final Map<String, byte[]> finalFiles, final String resourceFile) {
         try {
             finalFiles.put(resourceFile, runFiltersOnString(getContentsOfResourceFileAsString(resourceFile)).getBytes("UTF-8"));
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(String.format("%s", e.getMessage()), e);
         }
     }
 
     private void addFilteredCombinedWindowsScriptFile(final Map<String, byte[]> finalFiles, final String windowsScriptFile) {
         finalFiles.put(windowsScriptFile, String.format("%s%s", getWindowsCommonFiltered(), getContentsOfResourceFileAsString(String.format("windows/scripts/%s", windowsScriptFile))).getBytes(Charset.defaultCharset()));
     }
 
     private void addAllRealFilesFromDir(final Map<String, byte[]> finalFiles, final File explodedWarDir, final String rootwebapp) {
         Collection<File> files = FileUtils.listFiles(explodedWarDir, null, true);
         for (File file : files) {
             String relativeFileName = getRelativePathNameSimplistic(file, explodedWarDir);
             if (rootwebapp != null) relativeFileName = String.format("%s/%s", rootwebapp, relativeFileName);
             try {
                 finalFiles.put(relativeFileName, Files.toByteArray(file));
             } catch (IOException e) {
                 throw new RuntimeException(String.format("%s", e.getMessage()), e);
             }
         }
     }
 
     private String getRelativePathNameSimplistic(final File file, final File explodedWarDir) {
         return explodedWarDir.toURI().relativize(file.toURI()).getPath();
     }
 
     private void writeFilesToOutputDir(final File realOutputDir, final Map<String, byte[]> finalFiles) {
         for (String fileName : finalFiles.keySet()) {
             byte[] fileBytes = finalFiles.get(fileName);
             File outFile = new File(realOutputDir, fileName);
             File parentFile = outFile.getParentFile();
             if (!parentFile.exists()) parentFile.mkdirs();
             try {
                 Files.write(fileBytes, outFile);
             } catch (IOException e) {
                 throw new RuntimeException(String.format("%s: %s", e.getMessage(), outFile), e);
             }
         }
     }
 
     protected String getProxyFromMavenSettings() {
         try {
             Settings settings = mavenSettingsBuilder.buildSettings();
             ProxyInfo proxyInfo = null;
             if (settings != null && settings.getActiveProxy() != null) {
                 Proxy settingsProxy = settings.getActiveProxy();
                 return settingsProxy.getProtocol() + settingsProxy.getHost() + ":" + settingsProxy.getPort();
             }
             return null;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private String getWindowsCommonFiltered() {
         return runFiltersOnString(getContentsOfResourceFileAsString("windows/scripts/common.bat"));
     }
 
     private String runFiltersOnString(final String contentsOfResourceFileAsString) {
         return runTemplate(contentsOfResourceFileAsString).render();
     }
 
     private ST runTemplate(final String templateString) {
         ST template = new ST(templateString, '$', '$');
         for (String key : this.templateValues.keySet()) {
             String value = this.templateValues.get(key);
             template.add(key, value);
         }
         return template;
     }
 }
