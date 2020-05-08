 /*
  * Copyright 2010-2013, CloudBees Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.cloudbees.clickstack.liferay;
 
 import com.cloudbees.clickstack.domain.environment.Environment;
 import com.cloudbees.clickstack.domain.metadata.*;
 import com.cloudbees.clickstack.util.Files2;
 import com.google.common.base.Charsets;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
  */
 public class Setup2 {
     public static final String DEFAULT_JAVA_VERSION = "1.7";
     protected final Logger logger = LoggerFactory.getLogger(getClass());
     final Path appDir;
     final Path genappDir;
     final Path controlDir;
     final Path clickstackDir;
     final Path packageDir;
     final Path logDir;
     final Path tmpDir;
     @Nonnull
     final Metadata metadata;
     @Nonnull
     final Environment env;
     @Nonnull
     final Path appExtraFilesDir;
     /**
      * initialised in {@link #installLiferay()}
      */
     Path liferayHome;
     /**
      * initialised in {@link #installLiferay()}
      */
     Path liferayDeployFolder;
     /**
      * initialised in {@link #installLiferay()}
      */
     Path catalinaHome;
     /**
      * initialised in {@link #installLiferay()}
      */
     Path catalinaBase;
     /**
      * initialised in {@link #installLiferay()}
      */
     Path agentLibDir;
 
     public Setup2(@Nonnull Environment env, @Nonnull Metadata metadata) throws IOException {
         logger.info("Setup: {}, {}", env, metadata);
         this.env = env;
         this.metadata = metadata;
 
         this.appDir = env.appDir;
 
         this.genappDir = env.genappDir;
 
         this.controlDir = env.controlDir;
         this.logDir = env.logDir;
 
         this.tmpDir = Files.createDirectories(appDir.resolve("tmp"));
         Files2.chmodAddReadWrite(tmpDir);
 
         this.clickstackDir = env.clickstackDir;
         Preconditions.checkState(Files.exists(clickstackDir) && Files.isDirectory(clickstackDir));
 
         this.appExtraFilesDir = Files.createDirectories(appDir.resolve("app-extra-files"));
         Files2.chmodAddReadWrite(appExtraFilesDir);
 
         this.packageDir = env.packageDir;
     }
 
     public void setup() throws Exception {
 
         installSkeleton();
         installLiferay();
         installTomcatJavaOpts();
         installCatalinaBase();
         installCatalinaHome();
         installCloudBeesJavaAgent();
         installJmxTransAgent();
         writeJavaOpts();
         writeConfig();
         installControlScripts();
         configureLiferay();
         installLiferayJavaOpts();
         deployLiferayPackages();
 
         ContextXmlBuilder contextXmlBuilder = new ContextXmlBuilder(metadata);
         contextXmlBuilder.buildTomcatConfigurationFiles(this.catalinaBase);
 
         logger.info("Clickstack successfully installed");
     }
 
     public void installSkeleton() throws IOException {
         logger.debug("installSkeleton() {}", appDir);
 
         Files2.copyDirectoryContent(clickstackDir.resolve("dist"), appDir);
     }
 
     public void installLiferay() throws Exception {
         logger.debug("installLiferay zip");
         Path liferayPackage = Files2.findArtifact(clickstackDir, "liferay-portal-tomcat", "zip");
         Files2.unzip(liferayPackage, appDir);
 
 
         this.liferayHome = Files2.findUniqueFolderBeginningWith(appDir, "liferay-portal");
         this.liferayDeployFolder = Files.createDirectories(liferayHome.resolve("deploy"));
 
         Path dataDir = liferayHome.resolve("data");
         Files2.chmodAddReadWrite(dataDir);
 
         Path logsDir = Files.createDirectories(liferayHome.resolve("logs"));
         Files2.chmodAddReadWrite(logsDir);
 
         this.catalinaHome = Files2.findUniqueFolderBeginningWith(liferayHome, "tomcat");
         this.catalinaBase = catalinaHome;
         this.agentLibDir = Files.createDirectories(catalinaHome.resolve("agent-lib"));
     }
 
     public void configureLiferay() throws IOException {
         Path portalPropertiesFile = liferayHome.resolve("portal-ext.properties");
 
         logger.debug("configureLiferay(): {}", portalPropertiesFile);
 
         Properties portalProperties = new Properties();
 
         // Portal Context - http://www.liferay.com/documentation/liferay-portal/6.1/user-guide/-/ai/portal-context
         portalProperties.setProperty("portal.ctx", "/");
         portalProperties.setProperty("portal.instance.http.port", String.valueOf(env.appPort));
 
         // Disable "Browser Launcher"
         portalProperties.put("browser.launcher.url", "");
 
 
         if ("true".equals(metadata.getRuntimeParameter("liferay", "dev", "false"))) {
             logger.info("liferay dev mode, web.server.http.port={}", env.appPort);
             portalProperties.setProperty("web.server.http.port", String.valueOf(env.appPort));
         } else {
             portalProperties.setProperty("web.server.http.port", "80");
             portalProperties.setProperty("web.server.https.port", "443");
         }
 
         // Database Schema
         // http://www.liferay.com/documentation/liferay-portal/6.1/user-guide/-/ai/schema
         portalProperties.setProperty("schema.run.enabled", "true");
 
         // Set this to to true to populate with the minimal amount of data. Set this to false to populate with a larger amount of sample data.
         // http://www.liferay.com/documentation/liferay-portal/6.1/user-guide/-/ai/schema
         portalProperties.setProperty("schema.run.minimal", "true");
 
         // Set this to true to enable undeploying plugins.
         // http://www.liferay.com/documentation/liferay-portal/6.1/user-guide/-/ai/hot-undeploy
         portalProperties.setProperty("hot.undeploy.enabled", "false");
 
         // http://www.liferay.com/documentation/liferay-portal/6.1/user-guide/-/ai/jdbc
         Collection<Database> dbs = metadata.getResources(Database.class);
         if (dbs.isEmpty()) {
             logger.debug("No DB associated with app");
         } else if (dbs.size() == 1) {
             portalProperties.setProperty("jdbc.default.jndi.name", "jdbc/" + Iterables.getOnlyElement(dbs).getName());
         } else {
             logger.warn("More than 1 database bound to the application, don't auto select db: {}", dbs);
         }
 
         // SMTP
         Collection<Email> emailBindings = metadata.getResources(Email.class);
         if (emailBindings.isEmpty()) {
             logger.debug("No Email associated with app");
         } else if (emailBindings.size() == 1) {
             portalProperties.setProperty("mail.session.jndi.name", Iterables.getOnlyElement(emailBindings).getName());
         } else {
             logger.warn("More than 1 email bound to the application, don't auto select email: {}", emailBindings);
         }
 
         // Document Library Store - Amazon S3 Store
         // portalProperties.setProperty("dl.store.impl", "com.liferay.portlet.documentlibrary.store.S3Store");
         // portalProperties.put("dl.store.s3.access.key",);
         // portalProperties.put("dl.store.s3.secret.key",);
         // portalProperties.put("dl.store.s3.bucket.name",);
 
         Map<String, String> userDefinedValues = metadata.getRuntimeProperty("liferay");
         if (userDefinedValues == null) {
             userDefinedValues = new HashMap<>();
         }
         // TODO force to get these properties
         if (!userDefinedValues.containsKey("admin.email.from.name"))
             userDefinedValues.put("admin.email.from.name", "admin@example.com");
         if (!userDefinedValues.containsKey("admin.email.from.address"))
             userDefinedValues.put("admin.email.from.address", "admin@example.com");
 
         for (Map.Entry<String, String> property : userDefinedValues.entrySet()) {
             portalProperties.setProperty(property.getKey(), property.getValue());
         }
 
         if (logger.isTraceEnabled()) {
             logger.trace("Generate {}", portalPropertiesFile);
             portalProperties.store(System.out, "Config");
         }
         portalProperties.store(Files.newOutputStream(portalPropertiesFile), "Generated by genapp " + new Timestamp(System.currentTimeMillis()));
 
         Files2.chmodAddReadWrite(portalPropertiesFile);
 
 
         Path portalSetupWizardPath = liferayHome.resolve("portal-setup-wizard.properties");
         Properties portalSetupWizardProperties = new Properties();
         portalSetupWizardProperties.put("liferay.home", liferayHome.toString());
         //portalSetupWizardProperties.put("admin.email.from.name", portalProperties.get("admin.email.from.name"));
         //portalSetupWizardProperties.put("admin.email.from.address", portalProperties.get("admin.email.from.address"));
         //portalSetupWizardProperties.put("setup.wizard.enabled", "false");
 
         portalSetupWizardProperties.store(Files.newOutputStream(portalSetupWizardPath), "Generated by genapp " + new Timestamp(System.currentTimeMillis()));
         // ADD WRITE PERMISSION!
         Files2.chmodAddReadWrite(portalSetupWizardPath);
 
     }
 
     public void installCatalinaHome() throws Exception {
         logger.debug("installCatalinaHome() {}", catalinaHome);
 
         logger.trace("install cloudbees jars");
 
         Path targetLibDir = Files.createDirectories(catalinaBase.resolve("lib"));
         Files2.copyArtifactToDirectory(clickstackDir.resolve("deps/tomcat-lib"), "cloudbees-web-container-extras", targetLibDir);
 
         Files2.chmodSetReadOnly(catalinaHome.resolve("agent-lib"));
         Files2.chmodSetReadOnly(catalinaHome.resolve("bin"));
         Files2.chmodSetReadOnly(catalinaHome.resolve("conf"));
         Files2.chmodSetReadOnly(catalinaHome.resolve("lib"));
     }
 
     public void installCatalinaBase() throws IOException {
         logger.debug("installCatalinaBase(): {}", catalinaBase);
         Files2.copyDirectoryContent(clickstackDir.resolve("resources/catalina-base"), catalinaBase);
 
         Path workDir = Files.createDirectories(catalinaBase.resolve("work"));
         Files2.chmodAddReadWrite(workDir);
 
         Path logsDir = Files.createDirectories(catalinaBase.resolve("logs"));
         Files2.chmodAddReadWrite(logsDir);
 
 
         Files2.chmodAddReadWrite(catalinaBase.resolve("webapps"));
         Files2.chmodAddReadWrite(catalinaBase.resolve("temp"));
 
     }
 
     public void deployLiferayPackages() throws IOException {
         logger.debug("deployLiferayPackages(): {}", liferayDeployFolder);
 
         Files.createDirectories(liferayDeployFolder);
 
         if (Files.exists(packageDir.resolve("app.war"))) {
             Path deployedFile = Files.copy(packageDir.resolve("app.war"), liferayDeployFolder.resolve("app.war"));
             logger.info("Deployed war {}", deployedFile);
        } else if (Files.exists(packageDir.resolve("app.zip"))) {
            Files2.unzip(packageDir.resolve("app.zip"), liferayDeployFolder);
             logger.info("Expanded given zip archive to {}", liferayDeployFolder);
         } else {
             logger.warn("No app.war or app.zip found");
         }
         // ADD WRITE PERMISSION
         Files2.chmodAddReadWrite(liferayDeployFolder);
     }
 
     public void installTomcatJavaOpts() throws IOException {
         Path optsFile = controlDir.resolve("java-opts-20-tomcat-opts");
         logger.debug("installTomcatJavaOpts() {}", optsFile);
 
         String opts = "" +
                 "-Djava.io.tmpdir=\"" + tmpDir + "\" " +
                 "-Dcatalina.home=\"" + catalinaHome + "\" " +
                 "-Dcatalina.base=\"" + catalinaBase + "\" " +
                 "-Dapp_extra_files=\"" + appExtraFilesDir + "\" " +
                 "-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager " +
                 "-Djava.util.logging.config.file=\"" + catalinaBase + "/conf/logging.properties\"";
 
         Files.write(optsFile, Collections.singleton(opts), Charsets.UTF_8);
     }
 
     public void installLiferayJavaOpts() throws IOException {
         Path optsFile = controlDir.resolve("java-opts-21-liferay-opts");
 
         String opts = "" +
                 "-Dliferay.home=\"" + liferayHome + "\" " +
                 "-Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false";
 
         Files.write(optsFile, Collections.singleton(opts), Charsets.UTF_8);
     }
 
     public void installJmxTransAgent() throws IOException {
         logger.debug("installJmxTransAgent() {}", agentLibDir);
 
         Path jmxtransAgentJarFile = Files2.copyArtifactToDirectory(clickstackDir.resolve("deps/javaagent-lib"), "jmxtrans-agent", agentLibDir);
         Path jmxtransAgentConfigurationFile = catalinaBase.resolve("conf/liferay6-metrics.xml");
         Preconditions.checkState(Files.exists(jmxtransAgentConfigurationFile), "File %s does not exist", jmxtransAgentConfigurationFile);
         Path jmxtransAgentDataFile = logDir.resolve("liferay6-metrics.data");
 
         Path agentOptsFile = controlDir.resolve("java-opts-60-jmxtrans-agent");
 
         String agentOptsFileData =
                 "-javaagent:" + jmxtransAgentJarFile.toString() + "=" + jmxtransAgentConfigurationFile.toString() +
                         " -Dliferay6_metrics_data_file=" + jmxtransAgentDataFile.toString();
 
         Files.write(agentOptsFile, Collections.singleton(agentOptsFileData), Charsets.UTF_8);
     }
 
     public void installCloudBeesJavaAgent() throws IOException {
         logger.debug("installCloudBeesJavaAgent() {}", agentLibDir);
 
         Path cloudbeesJavaAgentJarFile = Files2.copyArtifactToDirectory(clickstackDir.resolve("deps/javaagent-lib"), "cloudbees-clickstack-javaagent", this.agentLibDir);
         Path agentOptsFile = controlDir.resolve("java-opts-20-javaagent");
 
         Path envFile = controlDir.resolve("env");
         if (!Files.exists(envFile)) {
             logger.error("Env file not found at {}", envFile);
         }
         String agentOptsFileData = "-javaagent:" +
                 cloudbeesJavaAgentJarFile +
                 "=sys_prop:" + envFile;
 
         Files.write(agentOptsFile, Collections.singleton(agentOptsFileData), Charsets.UTF_8);
     }
 
     public void writeJavaOpts() throws IOException {
 
         Path javaOptsFile = controlDir.resolve("java-opts-10-core");
         logger.debug("writeJavaOpts(): {}", javaOptsFile);
 
         String javaOpts = metadata.getRuntimeParameter("java", "opts", "");
         String javaVersion = metadata.getRuntimeParameter("java", "version", DEFAULT_JAVA_VERSION);
 
         if (!javaOpts.contains("-XX:MaxPermSize") && (javaVersion.equals("1.6") || javaVersion.equals("1.7"))) {
             String maxPermSize = "-XX:MaxPermSize=256m";
             logger.warn("MaxPermSize has not been defined in metadata.json for a JDK " + javaVersion + ", force " + maxPermSize);
             javaOpts += " " + maxPermSize;
         }
         Files.write(javaOptsFile, Collections.singleton(javaOpts), Charsets.UTF_8);
     }
 
     public void writeConfig() throws IOException {
 
         Path configFile = controlDir.resolve("config");
         logger.debug("writeConfig(): {}", configFile);
 
         PrintWriter writer = new PrintWriter(Files.newOutputStream(configFile));
 
         writer.println("app_dir=\"" + appDir + "\"");
         writer.println("app_tmp=\"" + appDir.resolve("tmp") + "\"");
         writer.println("log_dir=\"" + logDir + "\"");
         writer.println("catalina_home=\"" + catalinaHome + "\"");
         writer.println("catalina_base=\"" + catalinaBase + "\"");
 
         writer.println("port=" + env.appPort);
 
         Path javaExecutable = metadata.getJavaExecutable();
         writer.println("java=\"" + javaExecutable + "\"");
         Path javaHome = metadata.getJavaHome();
         writer.println("JAVA_HOME=\"" + javaHome + "\"");
         writer.println("genapp_dir=\"" + genappDir + "\"");
 
         writer.println("catalina_opts=\"-Dport.http=" + env.appPort + "\"");
 
         String classpath = "" +
                 catalinaHome.resolve("bin/bootstrap.jar") + ":" +
                 catalinaHome.resolve("bin/tomcat-juli.jar") + ":" +
                 catalinaHome.resolve("lib");
         writer.println("java_classpath=\"" + classpath + "\"");
 
         writer.println("liferay_home=\"" + appDir + "\"");
 
         writer.close();
     }
 
     public void installControlScripts() throws IOException {
         logger.debug("installControlScripts() {}", controlDir);
 
         Files2.chmodAddReadExecute(controlDir);
 
         Path genappLibDir = genappDir.resolve("lib");
         Files.createDirectories(genappLibDir);
 
         Path jmxInvokerPath = Files2.copyArtifactToDirectory(clickstackDir.resolve("deps/control-lib"), "cloudbees-jmx-invoker", genappLibDir);
         // create symlink without version to simplify jmx_invoker script
         Files.createSymbolicLink(genappLibDir.resolve("cloudbees-jmx-invoker-jar-with-dependencies.jar"), jmxInvokerPath);
     }
 }
