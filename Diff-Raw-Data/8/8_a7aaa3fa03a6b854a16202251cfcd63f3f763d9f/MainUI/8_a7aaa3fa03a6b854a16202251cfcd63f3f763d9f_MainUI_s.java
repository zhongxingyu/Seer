 /*
  * Copyright 2009 Google Inc.
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
 package com.google.jstestdriver.idea.ui;
 
 import static com.google.inject.multibindings.Multibinder.newSetBinder;
 import static java.awt.BorderLayout.CENTER;
 import static java.awt.BorderLayout.NORTH;
 import static java.awt.BorderLayout.SOUTH;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Binder;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.google.inject.multibindings.Multibinder;
 import com.google.jstestdriver.ActionFactory;
 import com.google.jstestdriver.ActionRunner;
 import com.google.jstestdriver.Args4jFlagsParser;
 import com.google.jstestdriver.CapturedBrowsers;
 import com.google.jstestdriver.Flags;
 import com.google.jstestdriver.JsTestDriver;
 import com.google.jstestdriver.PluginLoader;
 import com.google.jstestdriver.ServerStartupAction;
 import com.google.jstestdriver.config.CmdFlags;
 import com.google.jstestdriver.config.CmdLineFlagsFactory;
 import com.google.jstestdriver.config.Configuration;
 import com.google.jstestdriver.config.InitializeModule;
 import com.google.jstestdriver.config.Initializer;
 import com.google.jstestdriver.config.YamlParser;
 import com.google.jstestdriver.guice.TestResultPrintingModule.TestResultPrintingInitializer;
 import com.google.jstestdriver.hooks.PluginInitializer;
 
 import org.apache.commons.logging.LogFactory;
 import org.mortbay.log.Slf4jLog;
 
 import java.awt.BorderLayout;
 import java.util.Collections;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 
 /**
  * This class provides an entry point for the Swing GUI of JSTestDriver, independent of running
  * as an IDEA plugin. Right now, this is not released in a form that anyone uses.
  * TODO(alexeagle): should we delete this, or document that it can be used?
  *
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class MainUI {
 
   private final class UiInitializer implements PluginInitializer {
     @Override
     public Module initializeModule(Flags flags, Configuration config) {
       return new Module() {
         @Override
         public void configure(Binder binder) {
           binder.bind(ResourceBundle.class).toInstance(messageBundle);
           binder.bind(StatusBar.Status.class).toInstance(StatusBar.Status.NOT_RUNNING);
         }
       };
     }
   }
 
   private static final String JCL_LOG_CONFIG_ATTR = "org.apache.commons.logging.Log";
   private static final String JETTY_LOG_CLASS_PROP = "org.mortbay.log.class";
   private static final String JCL_SIMPLELOG_SHOWLOGNAME = "org.apache.commons.logging.simplelog.showlogname";
   private static final String JCL_SIMPLELOG_SHOW_SHORT_LOGNAME = "org.apache.commons.logging.simplelog.showShortLogname";
   private final ResourceBundle messageBundle;
   private LogPanel logPanel;
   private StatusBar statusBar;
   private CapturedBrowsersPanel capturedBrowsersPanel;
   private JPanel infoPanel;
   private final Logger logger = Logger.getLogger(MainUI.class.getCanonicalName());
   private final CmdFlags preparsedFlags;
 
   public MainUI(CmdFlags preparsedFlags, ResourceBundle bundle) {
     this.preparsedFlags = preparsedFlags;
     this.messageBundle = bundle;
   }
 
   public static void main(String[] args) {
     CmdFlags preparsedFlags = new CmdLineFlagsFactory().create(args);
     configureLogging();
     ResourceBundle bundle = ResourceBundle.getBundle("com.google.jstestdriver.ui.messages");
     new MainUI(preparsedFlags, bundle).startUI();
   }
 
   private static void configureLogging() {
     // Configure commons logging to log to the Swing log panel logger
     LogFactory.getFactory().setAttribute(JCL_LOG_CONFIG_ATTR, LogPanelLog.class.getName());
     // Configure Jetty to log to SLF4J. Since slf4j-jcl.jar is in the classpath, SLF4J will be
     // configured to delegate logging to commons logging.
     System.setProperty(JETTY_LOG_CLASS_PROP, Slf4jLog.class.getName());
     System.setProperty(JCL_SIMPLELOG_SHOW_SHORT_LOGNAME, "false");
     System.setProperty(JCL_SIMPLELOG_SHOWLOGNAME, "false");
   }
 
   private void startUI() {
     try {
       // TODO(corysmith): Fix the set up to have an injection class and proper arrangements for listening.
       final Configuration configuration = preparsedFlags.getConfigurationSource().parse(
           preparsedFlags.getBasePath(), new YamlParser());
       List<Module> initializeModules = Lists.newLinkedList();
       initializeModules.add(
           new InitializeModule(new PluginLoader(),
               preparsedFlags.getBasePath(),
              new Args4jFlagsParser(preparsedFlags),
               preparsedFlags.getRunnerMode()));
       initializeModules.add(new Module() {
         public void configure(Binder binder) {
           final Multibinder<PluginInitializer> initBinder = newSetBinder(binder, PluginInitializer.class);
           initBinder.addBinding().to(TestResultPrintingInitializer.class);
           initBinder.addBinding().toInstance(new UiInitializer());
         }
       });
       final Injector initializeInjector = Guice.createInjector(initializeModules);
 
       final List<Module> actionRunnerModules =
           initializeInjector.getInstance(Initializer.class)
               .initialize(Collections.<Module>emptyList(), configuration, preparsedFlags.getRunnerMode(),
                   preparsedFlags.getUnusedFlagsAsArgs());
 
       final Injector injector = Guice.createInjector(actionRunnerModules);
 
       statusBar = injector.getInstance(StatusBar.class);
       logPanel = injector.getInstance(LogPanel.class);
       capturedBrowsersPanel = injector.getInstance(CapturedBrowsersPanel.class);
       LogPanelLog.LogPanelHolder.setLogPanel(logPanel);
       infoPanel =injector.getInstance(InfoPanel.class);
 
       ActionFactory actionFactory = injector.getInstance(ActionFactory.class);
       actionFactory.registerListener(ServerStartupAction.class, statusBar);
       actionFactory.registerListener(CapturedBrowsers.class, statusBar);
       actionFactory.registerListener(CapturedBrowsers.class, capturedBrowsersPanel);
       injector.getInstance(ActionRunner.class).runActions();
       JFrame appFrame = buildMainAppFrame();
       appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       appFrame.pack();
       appFrame.setVisible(true);
     } catch (Exception e) {
       logger.log(Level.SEVERE, "Failed to start the server: ", e);
     }
   }
 
   @SuppressWarnings("serial")
   private JFrame buildMainAppFrame() {
     return new JFrame("JSTestDriver Browser Manager") {{
       add(new JSplitPane(JSplitPane.VERTICAL_SPLIT) {{
         setTopComponent(new JPanel(new BorderLayout()) {{
           add(new JPanel(new BorderLayout()) {{
             add(statusBar, NORTH);
             add(infoPanel, CENTER);
             add(capturedBrowsersPanel, SOUTH);
           }}, SOUTH);
         }});
         setBottomComponent(logPanel);
       }});
     }};
   }
 }
