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
 package com.cloudbees.genapp.liferay6;
 
 import com.cloudbees.genapp.Files2;
 import com.cloudbees.genapp.metadata.Metadata;
 import com.cloudbees.genapp.metadata.resource.Database;
 import com.cloudbees.genapp.metadata.resource.Email;
 import com.cloudbees.genapp.metadata.resource.Resource;
 import com.cloudbees.genapp.metadata.resource.SessionStore;
 import com.google.common.base.Charsets;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Sets;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.file.*;
 import java.sql.Timestamp;
 import java.util.*;
 
 /**
  * @author <a href="mailto:cleclerc@cloudbees.com">Cyrille Le Clerc</a>
  */
 public class Setup2 {
     public static final String DEFAULT_JAVA_VERSION = "1.7";
     protected final Logger logger = LoggerFactory.getLogger(getClass());
     final Path appDir;
     final Path liferayHome;
     /**
      * ./liferay6
      */
     final Path catalinaHome;
     final Path genappDir;
     final Path controlDir;
     final Path clickstackDir;
     /**
      * ./server
      */
     final Path catalinaBase;
     final Path warFile;
     final Path genappLogDir;
     final Path tmpDir;
     final Path agentLibDir;
 
     /**
      * @param appDir        parent folder of the instantiated app with {@code catalina.home}, {@code catalina.base}, ...
      * @param clickstackDir parent folder of the liferay6-clickstack
      * @param packageDir    parent folder of {@code app.war}
      */
     public Setup2(@Nonnull Path appDir, @Nonnull Path clickstackDir, @Nonnull Path packageDir) throws IOException {
         this.appDir = appDir;
 
         this.genappDir = Files.createDirectories(appDir.resolve(".genapp"));
 
         this.controlDir = Files.createDirectories(genappDir.resolve("control"));
         this.genappLogDir = Files.createDirectories(genappDir.resolve("log"));
         Files2.chmodReadWrite(genappLogDir);
 
         this.liferayHome = appDir;
         this.catalinaHome = Files.createDirectories(liferayHome.resolve("tomcat-7.0.27"));
         this.catalinaBase = Files.createDirectories(liferayHome.resolve("tomcat-7.0.27"));
         this.agentLibDir = Files.createDirectories(catalinaHome.resolve("agent-lib"));
 
         this.tmpDir = Files.createDirectories(appDir.resolve("tmp"));
         Files2.chmodReadWrite(tmpDir);
 
         this.clickstackDir = clickstackDir;
         Preconditions.checkState(Files.exists(clickstackDir) && Files.isDirectory(clickstackDir));
 
         this.warFile = packageDir.resolve("app.war");
         Preconditions.checkState(Files.exists(warFile) && !Files.isDirectory(warFile));
     }
 
     public void installLiferay() throws Exception {
         logger.debug("installLiferay zip");
         Files2.unzip(clickstackDir.resolve("lib/liferay6.zip"), appDir);
 
         // http://www.liferay.com/community/forums/-/message_boards/message/26368932
 //        logger.warn("FIX LPS-37433");
 //        Set<String> filesToDelete = Sets.newHashSet(
 //                "./ROOT/html/themes/_unstyled/images/calendar/calendar_drop_shadow.png",
 //                "./ROOT/html/themes/_unstyled/images/calendar/calendar_day_drop_shadow.png",
 //                "./ROOT/html/themes/classic/images/calendar/calendar_drop_shadow.png",
 //                "./ROOT/html/themes/classic/images/calendar/calendar_day_drop_shadow.png",
 //                "./welcome-theme/images/calendar/calendar_drop_shadow.png",
 //                "./welcome-theme/images/calendar/calendar_day_drop_shadow.png");
 //        for (String file : filesToDelete) {
 //            Path path = catalinaBase.resolve("webapps").resolve(file);
 //            if (Files.exists(path)) {
 //                Files.delete(path);
 //                logger.warn("FIX LPS-37433: Delete {}", path);
 //            } else {
 //                logger.warn("FIX LPS-37433: file could not found {}");
 //            }
 //        }
     }
 
     public void configureLiferay(Metadata metadata, String appPort) throws IOException {
         Path portalPropertiesFile = liferayHome.resolve("portal-ext.properties");
 
         logger.debug("configureLiferay(): {}", portalPropertiesFile);
 
         Properties portalProperties = new Properties();
 
         // Portal Context - http://www.liferay.com/documentation/liferay-portal/6.1/user-guide/-/ai/portal-context
         portalProperties.setProperty("portal.ctx", "/");
         portalProperties.setProperty("portal.instance.http.port", appPort);
 
         if ("true".equals(metadata.getRuntimeParameter("liferay", "dev", "false"))) {
             logger.info("liferay dev mode, web.server.http.port={}", appPort);
             portalProperties.setProperty("web.server.http.port", appPort);
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
         List<Database> dbs = new ArrayList<>();
 
         for (Resource resource : metadata.getResources().values()) {
             if (resource instanceof Database) {
                 dbs.add((Database) resource);
             }
         }
         if (dbs.isEmpty()) {
             logger.debug("No DB associated with app");
         } else if (dbs.size() == 1) {
             portalProperties.setProperty("jdbc.default.jndi.name", "jdbc/" + dbs.get(0).getName());
         } else {
             logger.warn("More than 1 database bound to the application, don't auto select db: {}", dbs);
         }
 
         Map<String, String> userDefinedValues = metadata.getRuntimeProperty("liferay");
         if (userDefinedValues == null) {
             userDefinedValues = new HashMap<>();
         }
 
         for (Map.Entry<String, String> property : userDefinedValues.entrySet()) {
             portalProperties.setProperty(property.getKey(), property.getValue());
         }
 
         if (logger.isTraceEnabled()) {
             logger.trace("Generate {}", portalPropertiesFile);
             portalProperties.store(System.out, "Config");
         }
         portalProperties.store(Files.newOutputStream(portalPropertiesFile), "Generated by genapp " + new Timestamp(System.currentTimeMillis()));
 
 
     }
 
     public void installCatalinaHome() throws Exception {
         logger.debug("installCatalinaHome() {}", catalinaHome);
 
         logger.trace("install cloudbees jars");
 
         // echo "Installing external libraries"
         Path targetLibDir = Files.createDirectories(catalinaHome.resolve("lib"));
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "cloudbees-web-container-extras", targetLibDir);
 
         logger.trace("install jdbc drivers and mail session");
         // JDBC Drivers
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "mysql-connector-java", targetLibDir);
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "postgresql", targetLibDir);
 
         // Mail
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "mail", targetLibDir);
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "activation", targetLibDir);
 
         Files2.chmodReadOnly(catalinaHome);
     }
 
     public void installCatalinaBase() throws IOException {
         logger.debug("installCatalinaBase(): {}", catalinaBase);
         Files2.copyDirectoryContent(clickstackDir.resolve("catalinaBase"), catalinaBase);
 
         Path workDir = Files.createDirectories(catalinaBase.resolve("work"));
         Files2.chmodReadWrite(workDir);
 
         Path logsDir = Files.createDirectories(catalinaBase.resolve("logs"));
         Files2.chmodReadWrite(logsDir);
 
     }
 
     public void installLiferayPortlets() {
 
         // INSTALL PORTLETS
         // Path rootWebAppDir = Files.createDirectories(catalinaBase.resolve("webapps/ROOT"));
         // Files2.unzip(warFile, rootWebAppDir);
         // Files2.chmodReadWrite(rootWebAppDir);
     }
 
     public void installTomcatJavaOpts() throws IOException {
         Path optsFile = controlDir.resolve("java-opts-20-tomcat-opts");
 
         String opts = "" +
                 "-Djava.io.tmpdir=\"" + tmpDir + "\" " +
                 "-Dcatalina.home=\"" + catalinaHome + "\" " +
                 "-Dcatalina.base=\"" + catalinaBase + "\" " +
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
         logger.debug("installJmxTransAgent()");
 
 
         Path jmxtransAgentJarFile = Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "jmxtrans-agent", agentLibDir);
         Path jmxtransAgentConfigurationFile = catalinaBase.resolve("conf/liferay6-metrics.xml");
         Preconditions.checkState(Files.exists(jmxtransAgentConfigurationFile), "File %s does not exist", jmxtransAgentConfigurationFile);
         Path jmxtransAgentDataFile = genappLogDir.resolve("liferay6-metrics.data");
 
         Path agentOptsFile = controlDir.resolve("java-opts-60-jmxtrans-agent");
 
         String agentOptsFileData =
                 "-javaagent:" + jmxtransAgentJarFile.toString() + "=" + jmxtransAgentConfigurationFile.toString() +
                         " -Dliferay6_metrics_data_file=" + jmxtransAgentDataFile.toString();
 
         Files.write(agentOptsFile, Collections.singleton(agentOptsFileData), Charsets.UTF_8);
     }
 
     public void installCloudBeesJavaAgent() throws IOException {
         logger.debug("installCloudBeesJavaAgent()");
         Path cloudbeesJavaAgentJarFile = Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "run-javaagent", this.agentLibDir);
         Path agentOptsFile = controlDir.resolve("java-opts-20-java-agent");
 
         String agentOptsFileData = "-javaagent:" +
                 cloudbeesJavaAgentJarFile +
                 "=sys_prop:" + controlDir.resolve("env");
 
         Files.write(agentOptsFile, Collections.singleton(agentOptsFileData), Charsets.UTF_8);
     }
 
     public void writeJavaOpts(Metadata metadata) throws IOException {
 
         Path javaOptsFile = controlDir.resolve("java-opts-10-core");
         logger.debug("writeJavaOpts(): {}", javaOptsFile);
 
         String javaOpts = metadata.getRuntimeParameter("java", "opts", "");
         Files.write(javaOptsFile, Collections.singleton(javaOpts), Charsets.UTF_8);
     }
 
     public void writeConfig(Metadata metadata, String appPort) throws IOException {
 
         Path configFile = controlDir.resolve("config");
         logger.debug("writeConfig(): {}", configFile);
 
         PrintWriter writer = new PrintWriter(Files.newOutputStream(configFile));
 
         writer.println("app_dir=\"" + appDir + "\"");
         writer.println("app_tmp=\"" + appDir.resolve("tmp") + "\"");
         writer.println("log_dir=\"" + genappLogDir + "\"");
         writer.println("catalina_home=\"" + catalinaHome + "\"");
         writer.println("catalina_base=\"" + catalinaBase + "\"");
 
         writer.println("port=" + appPort);
 
         Path javaPath = findJava(metadata);
         writer.println("java=\"" + javaPath + "\"");
         Path javaHome = findJavaHome(metadata);
         writer.println("JAVA_HOME=\"" + javaHome + "\"");
         writer.println("genapp_dir=\"" + genappDir + "\"");
 
         writer.println("catalina_opts=\"-Dport.http=" + appPort + "\"");
 
         String classpath = "" +
                 catalinaHome.resolve("bin/bootstrap.jar") + ":" +
                 catalinaHome.resolve("bin/tomcat-juli.jar") + ":" +
                 catalinaHome.resolve("lib");
         writer.println("java_classpath=\"" + classpath + "\"");
 
         writer.println("liferay_home=\"" + appDir + "\"");
 
         writer.close();
     }
 
     public void installControlScripts() throws IOException {
         logger.debug("installControlScripts(): {}", controlDir);
         Files2.copyDirectoryContent(clickstackDir.resolve("control"), controlDir);
         Files2.chmodReadExecute(controlDir);
 
 
         Path genappLibDir = genappDir.resolve("lib");
         logger.debug("installControlScripts libs", genappLibDir);
         Files.createDirectories(genappLibDir);
 
         Files2.copyArtifactToDirectory(clickstackDir.resolve("lib"), "cloudbees-jmx-invoker", genappLibDir);
     }
 
     public Path findJava(Metadata metadata) {
         Path javaPath = findJavaHome(metadata).resolve("bin/java");
         Preconditions.checkState(Files.exists(javaPath), "Java executable %s does not exist");
         Preconditions.checkState(!Files.isDirectory(javaPath), "Java executable %s is not a file");
 
         return javaPath;
     }
 
     public Path findJavaHome(Metadata metadata) {
        String javaVersion = metadata.getRuntimeParameter("javaHome", "version", DEFAULT_JAVA_VERSION);
         Map<String, String> javaHomePerVersion = new HashMap<>();
         javaHomePerVersion.put("1.6", "/opt/java6");
         javaHomePerVersion.put("1.7", "/opt/java7");
         javaHomePerVersion.put("1.8", "/opt/java8");
 
         String javaHome = javaHomePerVersion.get(javaVersion);
         if (javaHome == null) {
             javaHome = javaHomePerVersion.get(DEFAULT_JAVA_VERSION);
         }
         Path javaHomePath = FileSystems.getDefault().getPath(javaHome);
         Preconditions.checkState(Files.exists(javaHomePath), "JavaHome %s does not exist");
         Preconditions.checkState(Files.isDirectory(javaHomePath), "JavaHome %s is not a directory");
         return javaHomePath;
     }
 
 }
