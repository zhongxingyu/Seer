 /*
  * Copyright 2000-2013 JetBrains s.r.o.
  * Copyright 2013 Consulo
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.consulo.google.appengine.server;
 
 import java.awt.Dimension;
 import java.util.List;
 
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 
 import org.consulo.google.appengine.GoogleAppEngineIcons;
 import org.consulo.google.appengine.module.extension.GoogleAppEngineModuleExtension;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import com.intellij.openapi.module.Module;
 import com.intellij.openapi.module.ModuleManager;
 import com.intellij.openapi.module.ModuleUtilCore;
 import com.intellij.openapi.options.ConfigurationException;
 import com.intellij.openapi.options.SettingsEditor;
 import com.intellij.openapi.options.UnnamedConfigurable;
 import com.intellij.openapi.project.Project;
 import com.intellij.remoteServer.ServerType;
 import com.intellij.remoteServer.configuration.deployment.DeploymentConfigurator;
 import com.intellij.remoteServer.configuration.deployment.DeploymentSource;
 import com.intellij.remoteServer.configuration.deployment.DummyDeploymentConfiguration;
import com.intellij.remoteServer.configuration.localServer.LocalRunner;
 import com.intellij.remoteServer.runtime.ServerConnector;
 import com.intellij.remoteServer.runtime.ServerTaskExecutor;
 import com.intellij.remoteServer.runtime.deployment.DeploymentLogManager;
 import com.intellij.remoteServer.runtime.deployment.DeploymentTask;
 import com.intellij.remoteServer.runtime.deployment.ServerRuntimeInstance;
 import com.intellij.util.SmartList;
 import com.intellij.util.ui.FormBuilder;
 
 /**
  * @author nik
  */
 public class GoogleAppEngineServerType extends ServerType<GoogleAppEngineServerConfiguration>
 {
 
 	public GoogleAppEngineServerType()
 	{
 		super("google-app-engine");
 	}
 
 	@Nullable
 	@Override
 	public LocalRunner getLocalRunner()
 	{
 		return new GoogleAppEngineLocalRunner();
 	}
 
 	@NotNull
 	@Override
 	public String getPresentableName()
 	{
 		return "Google App Engine";
 	}
 
 	@NotNull
 	@Override
 	public Icon getIcon()
 	{
 		return GoogleAppEngineIcons.AppEngine;
 	}
 
 	@NotNull
 	@Override
 	public GoogleAppEngineServerConfiguration createDefaultConfiguration()
 	{
 		return new GoogleAppEngineServerConfiguration();
 	}
 
 	@NotNull
 	@Override
 	public UnnamedConfigurable createConfigurable(@NotNull GoogleAppEngineServerConfiguration configuration)
 	{
 		return new AppEngineCloudConfigurable(configuration);
 	}
 
 	@NotNull
 	@Override
 	public ServerConnector<?> createConnector(@NotNull GoogleAppEngineServerConfiguration configuration, @NotNull ServerTaskExecutor asyncTasksExecutor)
 	{
 		return new AppEngineServerConnector(configuration);
 	}
 
 	@NotNull
 	@Override
 	public DeploymentConfigurator createDeploymentConfigurator(Project project)
 	{
 		return new AppEngineDeploymentConfigurator(project);
 	}
 
 	private static class AppEngineCloudConfigurable implements UnnamedConfigurable
 	{
 		private final JTextField myEmailField;
 		private final GoogleAppEngineServerConfiguration myConfiguration;
 
 		public AppEngineCloudConfigurable(GoogleAppEngineServerConfiguration configuration)
 		{
 			myConfiguration = configuration;
 			myEmailField = new JTextField();
 			myEmailField.setPreferredSize(new Dimension(250, myEmailField.getPreferredSize().height));
 		}
 
 		@Nullable
 		@Override
 		public JComponent createComponent()
 		{
 			return FormBuilder.createFormBuilder().addLabeledComponent("E-mail:", myEmailField).getPanel();
 		}
 
 		@Override
 		public boolean isModified()
 		{
 			return !myEmailField.getText().equals(myConfiguration.getEmail());
 		}
 
 		@Override
 		public void apply() throws ConfigurationException
 		{
 			myConfiguration.setEmail(myEmailField.getText());
 		}
 
 		@Override
 		public void reset()
 		{
 			myEmailField.setText(myConfiguration.getEmail());
 		}
 
 		@Override
 		public void disposeUIResources()
 		{
 		}
 	}
 
 	private static class AppEngineDeploymentConfigurator extends DeploymentConfigurator<DummyDeploymentConfiguration>
 	{
 		private final Project myProject;
 
 		public AppEngineDeploymentConfigurator(Project project)
 		{
 			myProject = project;
 		}
 
 		@NotNull
 		@Override
 		public List<DeploymentSource> getAvailableDeploymentSources()
 		{
 			List<DeploymentSource> list = new SmartList<DeploymentSource>();
 			for(Module module : ModuleManager.getInstance(myProject).getModules())
 			{
 				GoogleAppEngineModuleExtension extension = ModuleUtilCore.getExtension(module, GoogleAppEngineModuleExtension.class);
 				if(extension == null)
 				{
 					continue;
 				}
 
 				DeploymentSource deploymentSource = extension.getDeploymentSource();
 				if(deploymentSource == null)
 				{
 					continue;
 				}
 				list.add(new GoogleAppEngineDeploymentSource(deploymentSource, extension));
 			}
 
 			return list;
 		}
 
 		@NotNull
 		@Override
 		public DummyDeploymentConfiguration createDefaultConfiguration(@NotNull DeploymentSource source)
 		{
 			return new DummyDeploymentConfiguration();
 		}
 
 		@Override
 		public SettingsEditor<DummyDeploymentConfiguration> createEditor(@NotNull DeploymentSource source)
 		{
 			return null;
 		}
 	}
 
 	private static class AppEngineServerConnector extends ServerConnector<DummyDeploymentConfiguration>
 	{
 		private final GoogleAppEngineServerConfiguration myConfiguration;
 
 		public AppEngineServerConnector(GoogleAppEngineServerConfiguration configuration)
 		{
 			myConfiguration = configuration;
 		}
 
 		@Override
 		public void connect(@NotNull final ConnectionCallback<DummyDeploymentConfiguration> callback)
 		{
 			callback.connected(new AppEngineRuntimeInstance(myConfiguration));
 		}
 	}
 
 	private static class AppEngineRuntimeInstance extends ServerRuntimeInstance<DummyDeploymentConfiguration>
 	{
 		private final GoogleAppEngineServerConfiguration myConfiguration;
 
 		public AppEngineRuntimeInstance(GoogleAppEngineServerConfiguration configuration)
 		{
 			myConfiguration = configuration;
 		}
 
 		@Override
 		public void deploy(@NotNull DeploymentTask<DummyDeploymentConfiguration> task, @NotNull DeploymentLogManager logManager, @NotNull DeploymentOperationCallback callback)
 		{
 			GoogleAppEngineDeploymentSource source = (GoogleAppEngineDeploymentSource) task.getSource();
 
 			GoogleAppEngineUploader uploader = GoogleAppEngineUploader.createUploader(source, myConfiguration, callback, logManager.getMainLoggingHandler());
 			if(uploader != null)
 			{
 				uploader.startUploading();
 			}
 		}
 
 		@Override
 		public void computeDeployments(@NotNull ComputeDeploymentsCallback callback)
 		{
 			callback.succeeded();
 		}
 
 		@Override
 		public void disconnect()
 		{
 		}
 	}
 }
