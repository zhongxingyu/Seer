 /*
  *    Copyright 2011 The 99 Software Foundation
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package org.nnsoft.t2t;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.Properties;
 
 import org.nnsoft.t2t.configuration.ConfigurationManager;
 import org.nnsoft.t2t.configuration.MigratorConfiguration;
 import org.nnsoft.t2t.core.DefaultMigrator;
 import org.nnsoft.t2t.core.Migrator;
 import org.nnsoft.t2t.core.MigratorException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.beust.jcommander.JCommander;
 
 /**
  * @author Davide Palmisano ( dpalmisano@gmail.com )
  */
 public class Runner {
 
     public static void main(String[] args) {
         final Logger logger = LoggerFactory.getLogger(Runner.class);
 
         RunnerOptions options = new RunnerOptions();
 
         JCommander jCommander = new JCommander(options);
         jCommander.setProgramName("t2t");
         jCommander.parseWithoutValidation(args);
 
         if (options.isPrintHelp()) {
             jCommander.usage();
             System.exit(-1);
         }
 
         if (options.isPrintVersion()) {
             Properties properties = new Properties();
             InputStream input = Runner.class.getClassLoader().getResourceAsStream("META-INF/maven/org.99soft/t2t/pom.properties");
 
             if (input != null) {
                 try {
                     properties.load(input);
                 } catch (IOException e) {
                     // ignore, just don't load the properties
                 } finally {
                     try {
                         input.close();
                     } catch (IOException e) {
                         // close quietly
                     }
                 }
             }
 
             System.out.printf("99soft T2T %s (%s)%n",
                     properties.getProperty("version"),
                     properties.getProperty("build"));
             System.out.printf("Java version: %s, vendor: %s%n",
                     System.getProperty("java.version"),
                     System.getProperty("java.vendor"));
             System.out.printf("Java home: %s%n", System.getProperty("java.home"));
             System.out.printf("Default locale: %s_%s, platform encoding: %s%n",
                     System.getProperty("user.language"),
                     System.getProperty("user.country"),
                     System.getProperty("sun.jnu.encoding"));
             System.out.printf("OS name: \"%s\", version: \"%s\", arch: \"%s\", family: \"%s\"%n",
                     System.getProperty("os.name"),
                     System.getProperty("os.version"),
                     System.getProperty("os.arch"),
                     getOsFamily());
 
             System.exit(-1);
         }
 
         if (!options.getConfigurationFile().exists() || options.getConfigurationFile().isDirectory()) {
             System.out.println(String.format("Non-readable XML Configuration file: %s (No such file).",
                     options.getConfigurationFile()));
             System.exit(-1);
         }
 
         if (options.getEntryPoint() == null) {
             System.out.println("No URL entrypoint has been specified for this migration.");
             System.exit(-1);
         }
 
         logger.info("Loading configuration from: '{}'", options.getConfigurationFile());
 
         MigratorConfiguration configuration =
                 ConfigurationManager.getInstance(options.getConfigurationFile()).getConfiguration();
         final Migrator migrator = new DefaultMigrator(configuration);
 
         logger.info("Configuration load, starting migration...");
 
         long start = System.currentTimeMillis();
         int exit = 0;
 
         try {
             migrator.run(options.getEntryPoint());
         } catch (MigratorException e) {
             logger.error("An error occurred during the migration process", e);
             exit = -1;
         } finally {
             logger.info("------------------------------------------------------------------------");
             logger.info("T2T MIGRATION {}", (exit < 0) ? "FAILURE" : "SUCCESS");
             logger.info("Total time: {}s", ((System.currentTimeMillis() - start) / 1000));
             logger.info("Finished at: {}", new Date());
 
             Runtime runtime = Runtime.getRuntime();
             logger.info("Final Memory: {}M/{}M",
                    (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024),
                    runtime.totalMemory() / (1024 * 1024));
 
             logger.info("------------------------------------------------------------------------");
 
             System.exit(exit);
         }
     }
 
     private static final String getOsFamily() {
         String osName = System.getProperty("os.name").toLowerCase();
         String pathSep = System.getProperty("path.separator");
 
         if (osName.indexOf("windows") != -1) {
             return "windows";
         } else if (osName.indexOf("os/2") != -1) {
             return "os/2";
         } else if (osName.indexOf("z/os") != -1
                 || osName.indexOf("os/390") != -1) {
             return "z/os";
         } else if (osName.indexOf("os/400") != -1) {
             return "os/400";
         } else if (pathSep.equals(";")) {
             return "dos";
         } else if (osName.indexOf("mac") != -1) {
             if (osName.endsWith("x")) {
                 return "mac"; // MACOSX
             }
             return "unix";
         } else if (osName.indexOf("nonstop_kernel") != -1) {
             return "tandem";
         } else if (osName.indexOf("openvms") != -1) {
             return "openvms";
         } else if (pathSep.equals(":")) {
             return "unix";
         }
 
         return "undefined";
     }
 
 }
