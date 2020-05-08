 /*
    Copyright 2010 Kristian Andersen
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
  */
 package org.kriand.warpdrive.mojo;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.commons.io.IOUtils;
 import org.kriand.warpdrive.Runtime;
 import org.kriand.warpdrive.processors.AbstractProcessor;
 import org.kriand.warpdrive.processors.bundles.BundleProcessor;
 import org.kriand.warpdrive.processors.css.YuiCssProcessor;
 import org.kriand.warpdrive.processors.images.DefaultImageProcessor;
 import org.kriand.warpdrive.processors.js.YuiJsProcessor;
 import org.kriand.warpdrive.processors.upload.ExternalUploadProcessor;
 import org.kriand.warpdrive.processors.webxml.WebXmlProcessor;
 import org.kriand.warpdrive.versioning.VersionProvider;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * The warpdrive:warpspeed goal processes static resources in your webapps and
  * prepares it for warpspeed.
  * <p/>
  *
  * @author kriand <a href="http://mailhide.recaptcha.net/d?k=01r9lbYEAtg9V5s1Ru_jtZ1g==&c=-aIoeZ0yU0yPn2kdog349bCmN-h1pe5Ed0LsyuWMbEc=">Show email</a>
  *         Date: Mar 2, 2010
  *         Time: 5:46:05 PM
  * @goal warpspeed
  * @phase prepare-package
  * @requiresProject
  */
 public class WarpDriveMojo extends AbstractMojo {
 
 
     /**
      * Buffersize for filewrites.
      */
     public static final int WRITE_BUFFER_SIZE = 32768;
 
     /**
      * The Maven Project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * Webapp source directory.
      *
      * @parameter default-value="${basedir}/src/main/webapp"
      * @since 1.0
      */
     private String webappSourceDir;
 
     /**
      * Webapp target directory.
      *
      * @parameter default-value="${project.build.directory}/${project.build.finalName}"
      * @since 1.0
      */
     private String webappTargetDir;
 
     /**
      * Set WarpDrive in development mode. In development mode WarpDrive will stay out of your
      * way and not alter any files.
      *
      * @parameter default-value=false
      * @since 1.0
      */
     private boolean developmentMode;
 
     /**
      * Path to the projects web.xml. Used when <b>generateWebXml</b> is true.
      *
      * @parameter default-value="${basedir}/src/main/webapp/WEB-INF/web.xml"
      * @since 1.0
      */
     private String webXmlSource;
 
     /**
      * The directory where the projects javascript files are stored. Relative to webapp/
      *
      * @parameter default-value="js"
      * @since 1.0
      */
     private String jsDir;
 
     /**
      * The directory where the projects image files are stored. Relative to webapp/
      *
      * @parameter default-value="images"
      * @since 1.0
      */
     private String imageDir;
 
     /**
      * The directory where the projects stylesheets are stored. Relative to webapp/
      *
      * @parameter default-value="css"
      * @since 1.0
      */
     private String cssDir;
 
     /**
      *
     * If static resources are served from a separate domain, specify a list
      * list of URLs pointing to it. This wil be prepended to alle resources in page.
     * For example <code>&lt;externalhost&gt;http://cdn1.example.com&lt;/externalhost&gt;&lt;externalhost&gt;http://cdn2.example.com&lt;/externalhost&gt;</code>
      *
      *
      * @parameter
      * @since 1.0
      */
     private List<String> externalHosts;
 
     /**
      *
      * Combine multiple scripts into a single script to reduce number of requests.
      * For example &lt;jquery-all.js&gt;js/jquery-1.4.1.js, js/jquery-ui-1.7.2.js&lt;/jquery-all.js&gt;
      * will create a new file jquery-all.js which contains both jquery and jquery-ui.
      *
      * The new bundle is created in <code>jsDir</code>
      *
      * Scripts are concatenated in the order they appear in the list.
      *
      * @parameter
      * @since 1.0
      */
     private Map<String, String> jsBundles;
 
     /**
      *
      * Combine multiple stylesheets into a single stylesheets to reduce number of requests.
      * For example &lt;common-stylesheet.css&gt;css/reset.css, css/fonts.css&lt;/common-stylesheet.css&gt;
      * will create a new file common-stylesheet.css which contains both reset.css and fonts.css.
      *
      * The new bundle is created in <code>cssDir</code>
      *
      * Stylesheets are concatenated in the order they appear in the list.
      *
      * @parameter
      * @since 1.0
      */
     private Map<String, String> cssBundles;
 
     /**
      * Specifies if WarpDrive should generate a new web.xml including the
      * expires header filter for static resources.
      * Leave as default if dont have a separate handling to responseheaders for static resources.
      *
      * @parameter default-value=true
      * @since 1.0
      */
     private boolean generateWebXml;
 
     /**
      *
      * Passed to YUI Compressor when processing javascript files.
      * From the <a href="http://www.julienlecomte.net/yuicompressor/README">YUI Compressor documentation</a>:
      * <pre>
      * Some source control tools don't like files containing lines longer than,
      * say 8000 characters. The linebreak option is used in that case to split
      * long lines after a specific column. It can also be used to make the code
      * more readable, easier to debug (especially with the MS Script Debugger)
      * Specify 0 to get a line break after each semi-colon in JavaScript, and
      * after each rule in CSS.
      * </pre>
      *
      * @parameter default-value=8000
      * @since 1.0
      */
     private int yuiJsLineBreak;
 
     /**
      *
      * Passed to YUI Compressor when processing javascript files.
      * From the <a href="http://www.julienlecomte.net/yuicompressor/README">YUI Compressor documentation</a>:
      * <pre>
      * Minify only. Do not obfuscate local symbols.
      * </pre>
      *
      * @parameter default-value=true
      * @since 1.0
      */
     private boolean yuiJsMunge;
 
     /**
      *
      * Passed to YUI Compressor when processing javascript and css files.
      * From the <a href="http://www.julienlecomte.net/yuicompressor/README">YUI Compressor documentation</a>:
      * <pre>
      * Display informational messages and warnings.
      * </pre>
      *
      * @parameter default-value=false
      * @since 1.0
      */
     private boolean yuiJsVerbose;
 
     /**
      *
      * Passed to YUI Compressor when processing javascript files.
      * From the <a href="http://www.julienlecomte.net/yuicompressor/README">YUI Compressor documentation</a>:
      * <pre>
      * Preserve unnecessary semicolons (such as right before a '}') This option
      * is useful when compressed code has to be run through JSLint (which is the
      * case of YUI for example)
      * </pre>
      *
      * @parameter default-value=false
      * @since 1.0
      */
     private boolean yuiJsPreserveAllSemicolons;
 
     /**
      *
      * Passed to YUI Compressor when processing javascript files.
      * From the <a href="http://www.julienlecomte.net/yuicompressor/README">YUI Compressor documentation</a>:
      * <pre>
      * Disable all the built-in micro optimizations.
      * </pre>
      *
      * @parameter default-value=false
      * @since 1.0
      */
     private boolean yuiJsDisableOptimizations;
 
     /**
      *
      * Passed to YUI Compressor when processing css files.
      * From the <a href="http://www.julienlecomte.net/yuicompressor/README">YUI Compressor documentation</a>:
      * <pre>
      * Preserve unnecessary semicolons (such as right before a '}') This option
      * is useful when compressed code has to be run through JSLint (which is the
      * case of YUI for example)
      * </pre>
      *
      * @parameter default-value=8000
      * @since 1.0
      */
     private int yuiCssLineBreak;
 
     /**
      *
      * Indicates if processed files should be uploaded to an external location as part of build process.
      *
      * @parameter default-value=false
      * @since 1.0
      */
     private boolean uploadResourcesToExternal;
 
     /**
      *
      * For really simple uploading of static resources to <a href="http://aws.amazon.com/s3/">Amazon S3</a>.
      * This can be convenient when using <a href="http://aws.amazon.com/cloudfront/">Amazon CloudFront </a>.
      *
      * Should be a properties-file containting 3 properties: <b>bucket</b>, <b>accessKey</b> and <b>secretKey</b>
      *
      * @parameter
      * @since 1.0
      */
     private File s3SettingsFile;
 
     /**
      *
      * Specify the class used to generate versionnumber.
      * If a versiongenerator is not specified, the current rev.number
      * in the basedir will be used for projects using SVN. For projects not using SVN, the current time in millis is used.
      * You may implement your own version generator by extending org.kriand.warpdrive.versioning.AbstractVersionGenerator
      * 
      * @parameter
      * @since 1.0
      */
     private String versionGeneratorClass;
 
     /**
      * Holds the current version to use.
      */
     private String version;
 
     /**
      *
      * The main entrypoint for the plugin.
      *
      * @throws MojoExecutionException If something goes wrong during execution of the plugin.
      */
     public final void execute() throws MojoExecutionException {
         try {
             assertWarModule();
             normalizeDirectories();
             version = new VersionProvider(this).getVersion();
             writeWarpDriveConfigFile();
             if (isDevelopmentMode()) {
                 return;
             }
             printEyeCatcher();
             List<AbstractProcessor> processors = setupProcessors();
             for (AbstractProcessor processor : processors) {
                 processor.process();
             }
         } catch (Exception ex) {
             throw new MojoExecutionException("Caught Exception", ex);
         }
     }
 
     /**
      *
      * Getter for version
      *
      * @return version
      */
     public final String getVersion() {
         return version;
     }
 
      /**
      *
      * Getter for project
      *
      * @return project
      */
     public final MavenProject getProject() {
         return project;
     }
 
      /**
      *
      * Getter for webappSourceDir
      *
      * @return webappSourceDir
      */
     public final String getWebappSourceDir() {
         return webappSourceDir;
     }
 
      /**
      *
      * Getter for webappTargetDir
      *
      * @return webappTargetDir
      */
     public final String getWebappTargetDir() {
         return webappTargetDir;
     }
 
      /**
      *
      * Getter for developmentMode
      *
      * @return developmentMode
      */
     public final boolean isDevelopmentMode() {
         return developmentMode;
     }
 
      /**
      *
      * Getter for webXmlSource
      *
      * @return webXmlSource
      */
     public final String getWebXmlSource() {
         return webXmlSource;
     }
 
      /**
      *
      * Getter for jsDir
      *
      * @return jsDir
      */
     public final String getJsDir() {
         return jsDir;
     }
 
     /**
      *
      * Getter for imageDir
      *
      * @return imageDir
      */
     public final String getImageDir() {
         return imageDir;
     }
 
      /**
      *
      * Getter for cssDir
      *
      * @return cssDir
      */
     public final String getCssDir() {
         return cssDir;
     }
 
      /**
      *
      * Getter for externalHosts
      *
      * @return externalHosts
      */
     public final List<String> getExternalHosts() {
         return externalHosts;
     }
 
      /**
      *
      * Getter for jsBundles
      *
      * @return jsBundles
      */
     public final Map<String, String> getJsBundles() {
         return jsBundles;
     }
 
      /**
      *
      * Getter for cssBundles
      *
      * @return cssBundles
      */
     public final Map<String, String> getCssBundles() {
         return cssBundles;
     }
 
      /**
      *
      * Getter for generateWebXml
      *
      * @return generateWebXml
      */
     public final boolean isGenerateWebXml() {
         return generateWebXml;
     }
 
      /**
      *
      * Getter for yuiJsLineBreak
      *
      * @return yuiJsLineBreak
      */
     public final int getYuiJsLineBreak() {
         return yuiJsLineBreak;
     }
 
      /**
      *
      * Getter for yuiJsMunge
      *
      * @return yuiJsMunge
      */
     public final boolean isYuiJsMunge() {
         return yuiJsMunge;
     }
 
     /**
      *
      * Getter for yuiJsVerbose
      *
      * @return yuiJsVerbose
      */
     public final boolean isYuiJsVerbose() {
         return yuiJsVerbose;
     }
 
      /**
      *
      * Getter for yuiJsPreserveAllSemicolons
      *
      * @return yuiJsPreserveAllSemicolons
      */
     public final boolean isYuiJsPreserveAllSemicolons() {
         return yuiJsPreserveAllSemicolons;
     }
 
      /**
      *
      * Getter for yuiJsDisableOptimizations
      *
      * @return yuiJsDisableOptimizations
      */
     public final boolean isYuiJsDisableOptimizations() {
         return yuiJsDisableOptimizations;
     }
 
     /**
      *
      * Getter for yuiCssLineBreak
      *
      * @return yuiCssLineBreak
      */
     public final int getYuiCssLineBreak() {
         return yuiCssLineBreak;
     }
 
    /**
      *
      * Getter for uploadResourcesToExternal
      *
      * @return uploadResourcesToExternal
      */
     public final boolean isUploadResourcesToExternal() {
         return uploadResourcesToExternal;
     }
 
     /**
      *
      * Getter for s3SettingsFile
      *
      * @return s3SettingsFile
      */
     public final File getS3SettingsFile() {
         return s3SettingsFile;
     }
 
     /**
      *
      * Getter for versionGeneratorClass
      *
      * @return versionGeneratorClass
      */
     public final String getVersionGeneratorClass() {
         return versionGeneratorClass;
     }
 
     /**
      *
      * Setup the required processors for the current configuration.
      *
      * @return A list of processors that will be invoked in the order they appear in the list.
      */
     private List<AbstractProcessor> setupProcessors() {
         List<AbstractProcessor> processors = new ArrayList<AbstractProcessor>();
         processors.add(new YuiJsProcessor(this));
         processors.add(new YuiCssProcessor(this));
         processors.add(new DefaultImageProcessor(this));
         if (bundlesAreConfigured()) {
             processors.add(new BundleProcessor(this));
         }
         if (isGenerateWebXml()) {
             processors.add(new WebXmlProcessor(this));
         }
         if (isUploadResourcesToExternal()) {
             processors.add(new ExternalUploadProcessor(this));
         }
         return processors;
     }
 
     /**
      *
      * Checks if there are any bundles configured in the curent configuration.
      *
      * @return True if atleast one bundle is configured, false otherwise.
      */
     private boolean bundlesAreConfigured() {
         return (getCssBundles() != null && getCssBundles().size() > 0) || (getJsBundles() != null && getJsBundles().size() > 0);
     }
 
     /**
      *
      * Asserts that we are in a war module. This is the only place it makes sence to invoke
      * this plugin.
      *
      * TODO: Is there a better way (Maven-way) of doing this?
      *
      * @throws MojoExecutionException If we are not in a war-module.
      */
     private void assertWarModule() throws MojoExecutionException {
         if (!"war".equals(project.getPackaging())) {
             throw new MojoExecutionException("maven-warpdrive-plugin can only be used with war modules");
         }
     }
 
     /**
      *  Make sure the directories provided in the configuration starts
      *  and ends with a slash.
      */
     private void normalizeDirectories() {
         if (!getCssDir().endsWith("/")) {
             cssDir = getCssDir() + "/";
         }
         if (!getJsDir().endsWith("/")) {
             jsDir = jsDir + "/";
         }
         if (!getImageDir().endsWith("/")) {
             imageDir = getImageDir() + "/";
         }
         if (!getCssDir().startsWith("/")) {
             cssDir = "/" + getCssDir();
         }
         if (!getJsDir().startsWith("/")) {
             jsDir = "/" + getJsDir();
         }
         if (!getImageDir().startsWith("/")) {
             imageDir = "/" + imageDir;
         }
     }
 
     /**
      *
      * Writes the configuration file to be used by WarpDrive at runtime.
      *
      * @throws IOException If the file could not be written.
      */
     private void writeWarpDriveConfigFile() throws IOException {
         File file = new File(project.getBuild().getOutputDirectory(), Runtime.RUNTIME_CONFIG_FILE);
 
         boolean created = file.getParentFile().mkdirs();
         if (created) {
             getLog().info(String.format("Created directory: %s", file.getParentFile()));
         }
 
         FileWriter writer = null;
         getLog().info("Writing WarpDrive configfile to: " + file.getName());
         try {
             writer = new FileWriter(file);
             writeBooleanValue(Runtime.DEV_MODE_KEY, isDevelopmentMode(), writer);
             writeStringValue(org.kriand.warpdrive.Runtime.VERSION_KEY, version, writer);
             writeStringValue(Runtime.IMAGE_DIR_KEY, getImageDir(), writer);
             writeStringValue(Runtime.JS_DIR_KEY, getJsDir(), writer);
             writeStringValue(Runtime.CSS_DIR_KEY, getCssDir(), writer);
             writeExternalHostsConfig(writer);
             writeBundleConfig(getCssBundles(), writer);
             writeBundleConfig(getJsBundles(), writer);
 
         } finally {
             IOUtils.closeQuietly(writer);            
         }
     }
 
     /**
      *
      * Utilitymethod for writing a string property to a writer.
      *
      * @param key The config key.
      * @param value The string value.
      * @param writer The writer of write to.
      * @throws IOException If the value could not be written.
      */
     private void writeStringValue(final String key, final String value, final Writer writer) throws IOException {
         writer.write(key);
         writer.write('=');
         writer.write(value);
         writer.write('\n');
     }
 
     /**
      *
      * Utilitymethod for writing a boolean property to a writer.
      *
      * @param key The config key.
      * @param value The boolean value.
      * @param writer The writer of write to.
      * @throws IOException If the value could not be written.
      */
     private void writeBooleanValue(final String key, final boolean value, final Writer writer) throws IOException {
         writer.write(key);
         writer.write('=');
         writer.write(String.valueOf(value));
         writer.write('\n');
     }
 
     /**
      *
      * Utilitymethod for writing external hosts config to a writer.
      *
      * @param writer The writer of write to.
      * @throws IOException If the value could not be written.
      */
     private void writeExternalHostsConfig(final Writer writer) throws IOException {
         if (getExternalHosts() == null || getExternalHosts().isEmpty()) {
             return;
         }
         writer.write(Runtime.EXTERNAL_HOSTS_KEY + "=");
         for (int i = 0; i < getExternalHosts().size(); i++) {
             writer.write(getExternalHosts().get(i));
             if (i < getExternalHosts().size() - 1) {
                 writer.write(Runtime.MULTIVAL_SEPARATOR);
             }
         }
         writer.write('\n');
     }
 
     /**
      *
      * Utilitymethod for writing a bundle config to a writer.
      *
      * @param bundle The configured bundle to write to config.
      * @param writer The writer of write to.
      * @throws IOException If the value could not be written.
      */
     private void writeBundleConfig(final Map<String, String> bundle, final Writer writer) throws IOException {
         if (bundle == null || bundle.isEmpty()) {
             return;
         }
         for (Map.Entry<String, String> entry : bundle.entrySet()) {
             writer.write(Runtime.BUNDLE_PREFIX_KEY);
             writer.write(entry.getKey());
             writer.write('=');
             String[] bundleEntries = entry.getValue().split(",");
             for (int i = 0; i < bundleEntries.length; i++) {
                 writer.write(bundleEntries[i].trim());
                 if (i < bundleEntries.length - 1) {
                     writer.write(Runtime.MULTIVAL_SEPARATOR);
                 }
             }
             writer.write('\n');
         }
     }
 
     /**
      * Simply prints an &quot;eyecatcher&quot; to the log so we can easily spot if WarpDrive is active.
      */
     private void printEyeCatcher() {
         getLog().info("  +    .          .      +       .        *  .    .  . .. .........");
         getLog().info("             *        .                 .     .    . .  . .........");
         getLog().info(".     +          .         .       .          .   . . . . .........");
         getLog().info("      .        +     .       +     .      .  .  .  . .. ...........");
         getLog().info(".         +            .          +        .  +   .  . .. .........");
         getLog().info("  .    'warpdrive is      _____       .        .   . .  ... .......");
         getLog().info("     active, captain!'-__/.....\\__        +   .   . . .............");
         getLog().info("   .                     \\_____/            .    .  .. ............");
         getLog().info("      +    .     +                    +    .   .    . .. . . ......");
         getLog().info("  +    .          .      +       .            .    .. . ... .......");
         getLog().info("             *        .                 .    +   . .. . ...........");
         getLog().info("             .        .       *         .        . .  .. . ........");
         getLog().info(".     +          .         .       .          +    . . .. . .......");
     }
 }
