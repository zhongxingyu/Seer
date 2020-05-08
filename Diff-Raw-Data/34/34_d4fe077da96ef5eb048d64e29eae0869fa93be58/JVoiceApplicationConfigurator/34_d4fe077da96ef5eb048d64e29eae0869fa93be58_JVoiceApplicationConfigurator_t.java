 package com.vectorsf.jvoice.core.project;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.model.*;
 import org.apache.maven.model.Plugin;
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.m2e.core.MavenPlugin;
 import org.eclipse.m2e.core.project.ProjectImportConfiguration;
 
 public final class JVoiceApplicationConfigurator {
 
 	private JVoiceApplicationConfigurator() {
 	}
 
 	public static void configureApplication(final IProject project) throws CoreException {
 		assert project.exists();
 
 		ResourcesPlugin.getWorkspace()
 				.run(new AddJVoiceNatureRunnable(project), project, IWorkspace.AVOID_UPDATE, null);
 
 	}
 
 	public static IProject createApplication(final String groupId, final String artifactId, final String projectName, final String descProject)
 			throws CoreException {
 		final IProject result[] = new IProject[1];
 		IWorkspaceRunnable action = new IWorkspaceRunnable() {
 
 			@Override
 			public void run(IProgressMonitor monitor) throws CoreException {
 				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 				MavenPlugin.getProjectConfigurationManager().createSimpleProject(project, null,
 						getModel(groupId, artifactId, projectName, descProject),
 						new String[] { "src/" }, new ProjectImportConfiguration(),
 						monitor);
 
 				result[0] = project;
 			}
 		};
 		ResourcesPlugin.getWorkspace().run(action, null);
 		IProject project = result[0];
 		ResourcesPlugin.getWorkspace().run(new AddJVoiceNatureRunnable(project), null);
 
 		return result[0];
 	}
 
 	private static Model getModel(String groupId, String artifactId, String projectName, String descProject) {
 		Model model = new Model();
 		model.setModelVersion("4.0.0"); //$NON-NLS-1$
 
 		model.setGroupId(groupId);
 		model.setArtifactId(artifactId);
 		model.setVersion("1.0.0");
 		model.setPackaging("war");
 
 		model.setName(projectName);
 		model.setDescription(descProject);
 		
 		Plugin dsl_builder = new Plugin();
 		dsl_builder.setGroupId("com.vectorsf.jvoice");
 		dsl_builder.setArtifactId("application-builder");
 		dsl_builder.setVersion("0.0.2-SNAPSHOT");
 		PluginExecution voiceDSL = new PluginExecution();
 		voiceDSL.setPhase("process-resources");
 		voiceDSL.addGoal("copyXML");
 
 		dsl_builder.addExecution(voiceDSL);
 
 		Build build = new Build();
 		build.addPlugin(dsl_builder);
 
 		model.setBuild(build);
 
 		Repository repository = new Repository();
 		repository.setId("jvoice");
 		repository.setName("JVoice repository");
 		repository.setUrl("http://isbks208510504s.scisb.isban.corp/nexus/content/groups/jvoiceGroup/");
 
 		model.addPluginRepository(repository);
 		model.addRepository(repository);
 
 		return model;
 	}
 
 	private static final class AddJVoiceNatureRunnable implements IWorkspaceRunnable {
 		private final IProject project;
 
 		private AddJVoiceNatureRunnable(IProject project) {
 			this.project = project;
 		}
 
 		@Override
 		public void run(IProgressMonitor monitor) throws CoreException {
 			IProjectDescription description = project.getDescription();
 			String[] natureIds = description.getNatureIds();
 
 			String[] newNatureIds = new String[natureIds.length + 2];
 			System.arraycopy(natureIds, 0, newNatureIds, 1, natureIds.length);
 			newNatureIds[0] = JVoiceModuleNature.NATURE_ID;
 			newNatureIds[newNatureIds.length-1] = JVoiceApplicationNature.NATURE_ID;
 			description.setNatureIds(newNatureIds);
 			
 
 			this.project.setDescription(description, null);
 		}
 	}
 
 }
