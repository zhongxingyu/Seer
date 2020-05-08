 package org.consulo.google.appengine.server;
 
 import org.consulo.google.appengine.module.extension.GoogleAppEngineModuleExtension;
 import org.jetbrains.annotations.NotNull;
 import com.intellij.execution.DefaultExecutionResult;
 import com.intellij.execution.ExecutionException;
 import com.intellij.execution.ExecutionResult;
 import com.intellij.execution.Executor;
 import com.intellij.execution.configurations.GeneralCommandLine;
 import com.intellij.execution.filters.TextConsoleBuilderFactory;
 import com.intellij.execution.process.OSProcessHandler;
 import com.intellij.execution.process.ProcessHandler;
 import com.intellij.execution.runners.ExecutionEnvironment;
 import com.intellij.execution.runners.ProgramRunner;
 import com.intellij.execution.ui.ConsoleView;
 import com.intellij.remoteServer.configuration.deployment.DeploymentConfiguration;
 import com.intellij.remoteServer.configuration.deployment.DeploymentSource;
import com.intellij.remoteServer.configuration.localServer.LocalRunner;
 
 /**
  * @author VISTALL
  * @since 01.10.13.
  */
 public class GoogleAppEngineLocalRunner implements LocalRunner
 {
 	@Override
 	public ExecutionResult execute(@NotNull DeploymentSource d,
 			DeploymentConfiguration deploymentConfiguration,
 			ExecutionEnvironment executionEnvironment,
 			@NotNull Executor executor,
 			@NotNull ProgramRunner programRunner) throws ExecutionException
 	{
 		GoogleAppEngineDeploymentSource deploymentSource = (GoogleAppEngineDeploymentSource) d;
 
 		GoogleAppEngineModuleExtension<DeploymentSource, ?> moduleExtension = deploymentSource.getModuleExtension();
 
 		final ConsoleView console = TextConsoleBuilderFactory.getInstance().createBuilder(moduleExtension.getModule().getProject()).getConsole();
 
 		GeneralCommandLine localServerCommandLine = moduleExtension.createLocalServerCommandLine(deploymentSource.getDelegate());
 
 		try
 		{
 			final ProcessHandler processHandler = new OSProcessHandler(localServerCommandLine.createProcess(), localServerCommandLine.getCommandLineString());
 
 			console.attachToProcess(processHandler);
 
 			return new DefaultExecutionResult(console, processHandler);
 		}
 		catch(ExecutionException e)
 		{
 			throw new ExecutionException(e);
 		}
 	}
 }
