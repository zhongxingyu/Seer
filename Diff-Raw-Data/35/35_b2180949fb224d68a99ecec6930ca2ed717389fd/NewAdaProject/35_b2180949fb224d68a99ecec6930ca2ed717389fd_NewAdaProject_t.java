 package org.padacore.core;
 
 import java.io.File;
 
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.antlr.runtime.RecognitionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 
 public class NewAdaProject {
 
 	private static final String GPR_PROJECT_SESSION_PROPERTY_QUALIFIER = "org.padacore";
 
 	private static final String[] NATURES = { AdaProjectNature.NATURE_ID };
 	private static final String DEFAULT_EXECUTABLE_NAME = "main.adb";
 	private static final String GNAT_PROJECT_EXTENSION = ".gpr";
 
 	/**
 	 * Create and return an Eclipse Ada project. A default GPR project is
 	 * created as well and associated to the IProject returned.
 	 * 
 	 * @param projectName
 	 *            Name of the project to create.
 	 * @param location
 	 *            Location where the project should be create or null to create
 	 *            the project in the workspace default location
 	 * @param addMainProjedure
 	 *            If true, a default main procedure will be added to the
 	 *            project.
 	 * @return The new created project.
 	 * 
 	 */
 	public static IProject Create(String projectName, IPath location,
 			boolean addMainProcedure) {
 
 		GprProject defaultGprProject = CreateDefaultGprProject(projectName,
 				GetProjectPath(projectName, location), addMainProcedure);
 
 		return CreateFrom(defaultGprProject, location);
 	}
 
 	/**
 	 * Create a project from GPR project at the specified location.
 	 * 
 	 * @param gprProjectAbsolutePath
 	 *            Absolute path of the GPR project
 	 * @return The IProject corresponding to the GPR project or null if no
 	 *         project were created.
 	 */
 	public static IProject CreateFrom(IPath gprProjectAbsolutePath) {
 
 		try {
 			IPath projectLocation = new Path(new File(
 					gprProjectAbsolutePath.toOSString()).getParent());
 			GprProject project = GprProjectFactory
 					.CreateGprProjectFromFile(gprProjectAbsolutePath);
 
 			return CreateFrom(project, projectLocation);
 
 		} catch (RecognitionException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Create a project from GPR project at the specified location.
 	 * 
 	 * @param gprProject
 	 *            Base GPR project use to create the corresponding IProject
 	 * @param location
 	 *            Location of the GPR project.
 	 * @return The IProject corresponding to the GPR project.
 	 */
 	public static IProject CreateFrom(GprProject gprProject, IPath location) {
 
 		IProject project = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject(gprProject.getName());
 		try {
 			IProjectDescription description = ResourcesPlugin.getWorkspace()
 					.newProjectDescription(gprProject.getName());
 
 			description.setLocation(location);
 			project.create(description, null);
 			project.open(null);
 			AddAdaNature(project);
 			AssociateIProjectToGprProject(project, gprProject);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 
 		return project;
 	}
 
 	/**
 	 * Add the Ada nature to the given IProject.
 	 * 
 	 * @param project
 	 *            Project to which the Ada nature shall be added.
 	 * @throws CoreException
 	 */
 	private static void AddAdaNature(IProject project) throws CoreException {
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(NATURES);
 		project.setDescription(description, null);
 	}
 
 	/**
 	 * Associate an existing GPR project to an Ada project.
 	 * 
 	 * @param project
 	 *            the project to which the GPR project will be associated.
 	 * @param associatedGpr
 	 *            the GPR project to associate.
 	 * @throws CoreException
 	 *             if GPR project could not be associated to Ada project.
 	 */
 	private static void AssociateIProjectToGprProject(IProject project,
 			GprProject associatedGpr) throws CoreException {
 		QualifiedName qualifiedName = new QualifiedName(
 				GPR_PROJECT_SESSION_PROPERTY_QUALIFIER, project.getName());
 
 		project.setSessionProperty(qualifiedName, associatedGpr);
 	}
 
 	/**
 	 * Associate a new GPR project (created from given GPR file) to an Ada
 	 * project.
 	 * 
 	 * @param project
 	 *            the project to which the GPR project will be associated.
 	 * @param gprFileAbsolutePath
 	 *            the absolute path of GPR file (filename included)
 	 * @throws IOException
 	 *             if the GPR file could not be opened.
 	 * @throws RecognitionException
 	 *             if the GPR file format is invalid.
 	 * @throws CoreException
 	 *             if GPR project could not be associated to Ada project.
 	 */
 	public static void AssociateIProjectToGprProject(IProject project,
 			IPath gprFileAbsolutePath) throws IOException,
 			RecognitionException, CoreException {
 		GprProject gprProjectToAssociate = GprProjectFactory
 				.CreateGprProjectFromFile(gprFileAbsolutePath);
 
 		AssociateIProjectToGprProject(project, gprProjectToAssociate);
 	}
 
 	/**
 	 * Returns the GPR project associated to given Ada project (or null if there
 	 * is none).
 	 * 
 	 * @param project
 	 *            the project from which to retrieve the associated GPR project.
 	 * @return the GPR project associated to given project or null if there is
 	 *         none
 	 * @throws CoreException
 	 */
 	public static GprProject GetAssociatedGprProject(IProject project)
 			throws CoreException {
 		QualifiedName qualifiedName = new QualifiedName(
 				GPR_PROJECT_SESSION_PROPERTY_QUALIFIER, project.getName());
 
 		return (GprProject) project.getSessionProperty(qualifiedName);
 	}
 
 	/**
 	 * Returns the project full path as a String.
 	 * 
 	 * @param projectName
 	 *            Name of the project
 	 * @param location
 	 *            A location or null for the default project location.
 	 * @return The project full path.
 	 */
 	private static String GetProjectPath(String projectName, IPath location) {
 		if (location == null) {
 			return ResourcesPlugin.getWorkspace().getRoot().getLocation()
 					+ System.getProperty("file.separator") + projectName;
 		} else {
 			return location.toOSString();
 		}
 	}
 
 	/**
 	 * Creates and returns a default GPR project.
 	 * 
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	private static GprProject CreateDefaultGprProject(String projectName,
 			String path, boolean addMainProcedure) {
 
 		GprProject gprProject = new GprProject(projectName);
 
 		new File(path).mkdir();
 
 		if (addMainProcedure) {
 			gprProject.setExecutable(true);
 			gprProject.addExecutableName(DEFAULT_EXECUTABLE_NAME);
 			AddFileToProject(path + System.getProperty("file.separator")
 					+ DEFAULT_EXECUTABLE_NAME, DefaultMainContents());
 		}
 		AddFileToProject(path + System.getProperty("file.separator")
 				+ projectName + ".gpr", gprProject.toString());
 
 		return gprProject;
 	}
 
 	/**
 	 * Add a file to the project with the specified content.
 	 * 
 	 * @param absolutefileName
 	 *            Absolute file name of the file to add.
 	 * @param content
 	 *            Content of the file to add.
 	 */
 	private static void AddFileToProject(String absolutefileName, String content) {
		FileWriter writer = null;

 		try {
 			File gprFile = new File(absolutefileName);
 			gprFile.createNewFile();
			writer = new FileWriter(gprFile);
 			writer.write(content);
 		} catch (IOException e) {
 			e.printStackTrace();
		} finally {
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
 		}
 	}
 
 	/**
 	 * Return the default contents for the file containing the main procedure.
 	 * 
	 * @return the default contents for main file as String.
 	 */
 	private static String DefaultMainContents() {
 		StringBuilder mainContents = new StringBuilder();
 		mainContents.append("with GNAT.IO;\n");
 		mainContents.append("procedure Main is\n");
 		mainContents.append("begin\n");
 		mainContents.append("GNAT.IO.Put_Line(\"Hello world\");\n");
 		mainContents.append("end Main;");

 		return mainContents.toString();
 	}
 
 	/**
	 * Return the file system absolute path for the GPR file associated to given
	 * project.
 	 * 
	 * @param project
	 *            the project for which the GPR file path is requested
	 * @return the file system absolute path for the GPR file associated to
	 *         given project.
 	 */
 	public static IPath GetGprAbsolutePath(IProject project) {
 
 		IWorkspaceRoot workspaceRoot = project.getWorkspace().getRoot();
 		StringBuilder pathBuilder = new StringBuilder();
 
 		pathBuilder.append(workspaceRoot.getLocation());
 		pathBuilder.append(project.getFullPath());
 		pathBuilder.append('/');
 		pathBuilder.append(project.getName());
 		pathBuilder.append(GNAT_PROJECT_EXTENSION);
 
 		return new Path(pathBuilder.toString());
 	}
 }
