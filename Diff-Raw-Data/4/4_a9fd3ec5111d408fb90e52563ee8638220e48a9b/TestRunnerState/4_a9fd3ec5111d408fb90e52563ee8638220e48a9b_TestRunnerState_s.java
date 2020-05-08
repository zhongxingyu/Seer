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
 package com.google.jstestdriver.idea;
 
 import com.google.jstestdriver.ActionRunner;
 import com.google.jstestdriver.ConfigurationParser;
 import com.google.jstestdriver.DefaultPathRewriter;
 import com.google.jstestdriver.IDEPluginActionBuilder;
 import com.google.jstestdriver.ResponseStreamFactory;
 import com.google.jstestdriver.idea.ui.ToolPanel;
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.ExecutionResult;
 import com.intellij.execution.Executor;
 import com.intellij.execution.configurations.ConfigurationPerRunnerSettings;
 import com.intellij.execution.configurations.JavaParameters;
 import com.intellij.execution.configurations.RunnableState;
 import com.intellij.execution.configurations.RunnerSettings;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.execution.runners.ProgramRunner;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.wm.ToolWindow;
 import com.intellij.openapi.wm.ToolWindowManager;
 import com.intellij.ui.content.Content;
 import com.intellij.util.concurrency.SwingWorker;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Arrays;
 
 /**
  * Encapsulates the execution state of the test runner.
  *
  * TODO(alexeagle): Should extend JavaCommandLineState so we can run in a ProcessHandler,
  * pass it to SMTestRunnerConnectionUtil.attachRunner, and display our results in the IDEA run UI
  *
  * @author alexeagle@google.com (Alex Eagle)
  */
 public class TestRunnerState implements RunnableState {
 
   private final JSTestDriverConfiguration jsTestDriverConfiguration;
   private final Project project;
 
   public TestRunnerState(JSTestDriverConfiguration jsTestDriverConfiguration, Project project, ExecutionEnvironment env) {
     // super(env);
     this.jsTestDriverConfiguration = jsTestDriverConfiguration;
     this.project = project;
   }
 
   protected JavaParameters createJavaParameters() throws ExecutionException {
     return new JavaParameters();
   }
 
   @Nullable
   public ExecutionResult execute(Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
 
 //    ProcessHandler processHandler = startProcess();
 //    ConsoleView consoleView = SMTestRunnerConnectionUtil.attachRunner(project, processHandler, this, jsTestDriverConfiguration, "JSTestDriver");
 //    return new DefaultExecutionResult(consoleView, processHandler, createActions(consoleView, processHandler));
     ToolWindow window =
         ToolWindowManager.getInstance(project).getToolWindow(JSTestDriverToolWindow.TOOL_WINDOW_ID);
     String serverURL = "http://localhost:" + jsTestDriverConfiguration.getServerPort();
     Content content = window.getContentManager().getContent(0);
 
     final ToolPanel toolPanel = (ToolPanel) content.getComponent();
     toolPanel.clearTestResults();
     toolPanel.setTestRunner(this);
     ResponseStreamFactory responseStreamFactory = toolPanel.createResponseStreamFactory();
     final ActionRunner dryRunRunner =
         makeActionBuilder(serverURL, responseStreamFactory)
           .dryRunFor(Arrays.asList("all")).build();
     final ActionRunner testRunner =
         makeActionBuilder(serverURL, responseStreamFactory)
             .addAllTests().build();
     final ActionRunner resetRunner =
         makeActionBuilder(serverURL, responseStreamFactory)
             .resetBrowsers().build();
     toolPanel.setResetRunner(resetRunner);
     final SwingWorker worker = new SwingWorker() {
       @Override
       public Object construct() {
         dryRunRunner.runActions();
         toolPanel.dryRunComplete();
         testRunner.runActions();
         return null;
       }
     };
     window.show(new Runnable() {
       public void run() {
         worker.start();
       }
     });
     return null;
   }
 
   public RunnerSettings getRunnerSettings() {
     return null;
   }
 
   public ConfigurationPerRunnerSettings getConfigurationSettings() {
     return null;
   }
 
   private IDEPluginActionBuilder makeActionBuilder(String serverURL,
                                                    ResponseStreamFactory responseStreamFactory)
       throws ExecutionException {
     File configFile = new File(jsTestDriverConfiguration.getSettingsFile());
     try {
       FileReader fileReader = new FileReader(jsTestDriverConfiguration.getSettingsFile());
      ConfigurationParser configurationParser = new ConfigurationParser(path, fileReader,
          new DefaultPathRewriter());
       return new IDEPluginActionBuilder(configurationParser, serverURL, responseStreamFactory);
     } catch (FileNotFoundException e) {
       throw new ExecutionException("Failed to read settings file " +
                                    jsTestDriverConfiguration.getSettingsFile(), e);
     }
   }
 }
