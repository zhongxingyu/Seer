 /*
  * Copyright 2010 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import java.util.List;
 import java.util.logging.LogManager;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.jstestdriver.config.CmdFlags;
 import com.google.jstestdriver.config.CmdLineFlagsFactory;
 import com.google.jstestdriver.config.Configuration;
 import com.google.jstestdriver.config.InvalidFlagException;
 import com.google.jstestdriver.config.UnreadableFilesException;
 import com.google.jstestdriver.embedded.JsTestDriverBuilder;
 import com.google.jstestdriver.guice.TestResultPrintingModule.TestResultPrintingInitializer;
 import com.google.jstestdriver.util.RetryException;
 
 public class JsTestDriver {
 
   private static final Logger logger = LoggerFactory.getLogger(JsTestDriver.class);
   private final Injector injector;
 
   /**
    * @param injector
    */
   public JsTestDriver(Injector injector) {
     // TODO(corysmith): Stop passing the injector around! This is a temporary step
     // in refactoring the action based system to be controlled by the JsTestDriver
     // interface.
     this.injector = injector;
   }
 
   public static void main(String[] args) {
     try {
       // pre-parse parsing... These are the flags
       // that must be dealt with before we parse the flags.
       CmdFlags cmdLineFlags = new CmdLineFlagsFactory().create(args);
       List<Plugin> cmdLinePlugins = cmdLineFlags.getPlugins();
 
       // configure logging before we start seriously processing.
       LogManager.getLogManager().readConfiguration(cmdLineFlags.getRunnerMode().getLogConfig());
 
 
       final PluginLoader pluginLoader = new PluginLoader();
 
       // load all the command line plugins.
       final List<Module> pluginModules = pluginLoader.load(cmdLinePlugins);
       logger.debug("loaded plugins %s", pluginModules);
       List<Module> initializeModules = Lists.newLinkedList(pluginModules);
 
       JsTestDriverBuilder builder = new JsTestDriverBuilder();
       builder.setBaseDir(cmdLineFlags.getBasePath().getCanonicalFile());
       builder.setConfigurationSource(cmdLineFlags.getConfigurationSource());
       builder.addPluginModules(pluginModules);
       builder.withPluginInitializer(TestResultPrintingInitializer.class);
       builder.setRunnerMode(cmdLineFlags.getRunnerMode());
       builder.setCmdLineFlags(cmdLineFlags);
       JsTestDriver jstd = builder.build();
       jstd.runConfiguration();
 
       logger.info("Finished action run.");
     } catch (InvalidFlagException e) {
       e.printErrorMessages(System.out);
       CmdFlags.printUsage(System.out);
       System.exit(1);
     } catch (UnreadableFilesException e) {
       System.out.println("Configuration Error: \n" + e.getMessage());
       System.exit(1);
     } catch (RetryException e) {
       System.out.println(
           "Tests failed due to unexpected environment issue: " + e.getCause().getMessage());
       System.exit(1);
     } catch (FailureException e) {
       System.out.println("Tests failed: " + e.getMessage());
       System.exit(1);
     } catch (Exception e) {
       logger.debug("Error {}", e);
       e.printStackTrace();
       System.out.println("Unexpected Runner Condition: " + e.getMessage()
           + "\n Use --runnerMode DEBUG for more information.");
       System.exit(1);
     }
   }
   
   public void startServer() {
     injector.getInstance(ServerStartupAction.class).run(null);
   }
 
   public void stopServer() {
     injector.getInstance(ServerShutdownAction.class).run(null);
   }
   
   /**
    * Runs the built in confiugration.
    * @return
    */
   public List<TestCase> runConfiguration() {
     injector.getInstance(ActionRunner.class).runActions();
     return null;
   }
   
   public List<TestCase> runConfiguration(String path) {
     return null;
   }
   
   public List<TestCase> runConfiguration(Configuration config) {
     return null;
   }
   
   public List<TestCase> getTestCasesFor(String path) {
     return null;
   }
   
   public List<TestCase> getTestCasesFor(Configuration conifg) {
     return null;
   }
 }
