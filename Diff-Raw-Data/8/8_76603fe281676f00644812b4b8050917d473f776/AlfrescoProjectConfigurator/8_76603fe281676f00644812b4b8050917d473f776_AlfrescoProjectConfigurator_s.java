 package org.eclipse.alfresco.publisher.core.configurator;
 
 import org.apache.maven.project.MavenProject;
 import org.eclipse.alfresco.publisher.core.AlfrescoPreferenceHelper;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
 import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;
 import org.osgi.service.prefs.BackingStoreException;
 import org.osgi.service.prefs.Preferences;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AlfrescoProjectConfigurator extends AbstractProjectConfigurator {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(AlfrescoProjectConfigurator.class);
 
 	public AlfrescoProjectConfigurator() {
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	public void configure(ProjectConfigurationRequest configurationRequest,
 			IProgressMonitor monitor) throws CoreException {
 		IProject project = configurationRequest.getProject();
 
 		MavenProject mavenProject = configurationRequest.getMavenProject();
 		LOGGER.info("Configuring " + mavenProject.getName());
 
 		Preferences projectPreferences = AlfrescoPreferenceHelper
 				.getProjectPreferences(configurationRequest.getProject());
 
 		IPath projectLocation = project.getLocation();
 		String projectLocationAsString = projectLocation.toString()+"/";
 
 		String targetDir = mavenProject.getBuild().getDirectory();
 
 //		
 		if (targetDir.startsWith(projectLocationAsString)) {
 			targetDir = targetDir.substring(projectLocationAsString.length());
 		} else {
 			throw new CoreException(Status.CANCEL_STATUS);
 		}
 
 		String artifactId = mavenProject.getArtifactId();
 		String version = mavenProject.getVersion();
 
 		String ampFolderPath = String.format("%s/%s-%s", targetDir, artifactId,
 				version);
 
 		String ampLibName = String.format("%s-%s.jar", artifactId, version);
 
 		
 
 		try {
 			//projectPreferences.sync();
 			projectPreferences.put(AlfrescoPreferenceHelper.AMP_FOLDER_RELATIVE_PATH, ampFolderPath);
 			
 			projectPreferences.put(AlfrescoPreferenceHelper.AMP_LIB_FILENAME, ampLibName);
 		
 			
 			
 			projectPreferences.flush();
 		} catch (BackingStoreException e) {
 			LOGGER.error("Could not save preferences.", e);
 		}
 		
 		IProjectDescription description = project.getDescription();
 		ICommand[] buildSpec = description.getBuildSpec();
 		ICommand[] iCommands = new ICommand[buildSpec.length];
 		int j=0;
 		ICommand alfrescoCommand = null;
 		for (int i = 0; i < buildSpec.length; i++) {
 			ICommand iCommand = buildSpec[i];
 			if("org.eclipse.alfresco.publisher.core.alfrescoResourceBuilder".equals(iCommand.getBuilderName())) {
 				j=1;
 				alfrescoCommand = iCommand;
 			}else {
 				iCommands[i-j]=iCommand;
 			}
 		}
 		if(j>0) {
 			iCommands[iCommands.length-1]=alfrescoCommand;
 		}
 		description.setBuildSpec(iCommands);
 		project.setDescription(description, monitor);

 	}
 
 //	private String getResourceMap(MavenProject mavenProject) {
 //		StringBuilder builder = new StringBuilder();
 //		for(Resource resource: mavenProject.getResources()) {
 //			String targetPath = resource.getTargetPath();
 //			boolean filtered = resource.isFiltering();
 //		}
 //		return builder.toString();
 //	}
 
 }
