 package org.padacore.ui.wizards;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.net.URI;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.padacore.core.AdaProjectNature;
 import org.padacore.core.GprProject;
 
 public class NewAdaProject {
 
 	private IProject project;
 	private IProjectDescription description;
 
 	private static final String[] NATURES = { AdaProjectNature.NATURE_ID };
 
 	/**
 	 * Create a new NewAdaProject instance.
 	 * 
 	 * @param project
 	 *            Project handle on the project to create.
 	 * @param location
 	 *            Project location.
 	 */
 	public NewAdaProject(String projectName, URI location) {
 
 		this.project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 		this.description = project.getWorkspace().newProjectDescription(project.getName());
 		this.description.setNatureIds(NATURES);
 		this.description.setLocationURI(location);
 	}
 
 	/**
 	 * Create and return an Ada project (IProject) or return existing one if it
 	 * has been already created.
 	 */
 	public IProject create(boolean addMainProcedure) {
 
 		if (!project.exists()) {
 			try {
				project.create(null);
 				project.open(null);
				project.setDescription(description, null);
 				addGprProject(addMainProcedure);
 
 			} catch (CoreException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return project;
 	}
 
 	private InputStream defaultMainContent () {
 		final String content = "procedure Main is\n" + "begin\n" + "\tnull;\n" + "end Main;\n";
 		return new ByteArrayInputStream(content.getBytes()); 
 	}
 	
 	/**
 	 * Add a default GPR project to new created Ada project.
 	 * @throws CoreException
 	 */
 	private void addGprProject(boolean addMainProcedure) throws CoreException {
 		GprProject gprProject = new GprProject(project.getName());
 		IFile      gprHandle  = project.getFile(gprProject.fileName());
 
 		if (addMainProcedure) {
 			gprProject.setExecutable(true);
 			gprProject.addExecutableName("main.adb");
 			IFile mainHandle = project.getFile("main.adb");
 			mainHandle.create(defaultMainContent(), false, null);
 		}
 		
 		gprHandle.create(new ByteArrayInputStream(gprProject.toString().getBytes()), false, null);
 	}
 }
