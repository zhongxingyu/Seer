 package com.jetbrains.nunitjs;
 
 import com.intellij.execution.*;
 import com.intellij.execution.configurations.ConfigurationFactory;
 import com.intellij.execution.configurations.RunConfiguration;
 import com.intellij.execution.executors.DefaultRunExecutor;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.execution.runners.ProgramRunner;
 import com.intellij.ide.util.PropertiesComponent;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.InvalidDataException;
 import com.jetbrains.nodejs.debug.NodeJSDebugRunner;
 import com.jetbrains.nodejs.run.NodeJSRunConfigurationType;
 import com.jetbrains.nunitjs.runner.RunnerManager;
 import org.jdom.Element;
 
 import java.io.File;
 
 public class NUnitLauncher {
 
     private final ProgramRunner _ProgramRunner;
     private final Project _Project;
 	private final PropertiesComponent _propertiesComponent;
 
     public NUnitLauncher(ProgramRunner programRunner, Project project){
         _ProgramRunner = programRunner;
         _Project = project;
 		_propertiesComponent = PropertiesComponent.getInstance(project);
     }
 
     public void runAll(){
 		run("--path=" + formatPathParam(new String[]{ getTestPath() }));
 	}
 
 	public void runFixture(String[] paths){
 		run("--path=" + formatPathParam(paths));
 	}
 
 	public void runTest(String path, String testName){
 		run("--path=" + formatPathParam(new String[]{ path }) + " --test=" + testName);
 	}
 
 	private String formatPathParam(String[] paths){
 		String pathParam = "";
 		for (String path : paths){
 			if (pathParam != "")
 				pathParam += ",";
 
 			pathParam += '"' + path + '"';
 		}
 
 		return pathParam;
 	}
 
 	private void run(String appParameters){
 
 		RunnerManager runnerManager = new RunnerManager();
 		runnerManager.deployRunner(getNUnitPath());
 
         RunManager runManager = RunManager.getInstance(_Project);
 		ConfigurationFactory factory = new NodeJSRunConfigurationType().getConfigurationFactories()[0];
 		RunnerAndConfigurationSettings nodeUnitSettings = runManager.createRunConfiguration("NUnitJS", factory);
         RunConfiguration runConfiguration = nodeUnitSettings.getConfiguration();
 
 		if (_ProgramRunner instanceof NodeJSDebugRunner)
 			appParameters += " --delay=1000";
 
         Element newElement = new Element("NUnitJS");
 		newElement.setAttribute("path-to-node", getNodePath());
 		newElement.setAttribute("path-to-js-file", getNUnitPath());
         newElement.setAttribute("application-parameters", appParameters);
 
         try {
             runConfiguration.readExternal(newElement);
         } catch (InvalidDataException e) {
             e.printStackTrace();
             return;
         }
 
        ExecutionEnvironment executionEnvironment = new ExecutionEnvironment(_ProgramRunner, nodeUnitSettings, _Project);
         Executor executor = DefaultRunExecutor.getRunExecutorInstance();
 
         try {
            _ProgramRunner.execute(executor, executionEnvironment);
         } catch (ExecutionException e1) {
             JavaExecutionUtil.showExecutionErrorMessage(e1, "Error", _Project);
         }
 
 
     }
 
 	private String getNodePath(){
 		String nodePath = _propertiesComponent.getValue("NUnitJS_NodePath");
 		if (nodePath == null || nodePath == "")
 			nodePath = "node";
 
 		return nodePath;
 	}
 
 	private String getNUnitPath(){
 		String nunitPath = _propertiesComponent.getValue("NUnitJS_NUnitPath");
 		if (nunitPath == null || nunitPath == "")
 			nunitPath = _Project.getBaseDir().getPath();
 
 		File file = new File(nunitPath, "nunit.js");
 
 		return file.getAbsolutePath();
 	}
 
 	private String getTestPath(){
 		String testPath = _propertiesComponent.getValue("NUnitJS_TestPath");
 
 		if (testPath == null || testPath == "")
 			return ".";
 
 		return testPath;
 	}
 }
