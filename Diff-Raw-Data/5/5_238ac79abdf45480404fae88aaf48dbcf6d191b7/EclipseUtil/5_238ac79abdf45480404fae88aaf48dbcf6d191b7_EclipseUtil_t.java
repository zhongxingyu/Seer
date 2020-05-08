 /***************************************************************************
  * Copyright (c) 2013 Codestorming.org.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Codestorming - initial API and implementation
  ****************************************************************************/
 package org.codestorming.eclipse.util;
 
 import static org.eclipse.core.runtime.Assert.isNotNull;
 
 import java.io.File;
 
 import org.codestorming.util.collection.Arrays2;
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.osgi.service.datalocation.Location;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Utility and convenient methods for the Eclipse platform.
  * 
  * @author Thaedrik <thaedrik@gmail.com>
  */
 public class EclipseUtil {
 
 	// Suppressing default constructor, ensuring non-instantiability
 	private EclipseUtil() {}
 
 	/**
 	 * Add/Remove the specified nature on the given project.
 	 * 
 	 * @param project The project.
 	 * @param natureId The id of the nature to add or remove.
 	 */
 	public static void toggleProjectNature(IProject project, String natureId) {
 		isNotNull(project);
 		isNotNull(natureId);
 		if (project.exists() && project.isOpen()) {
 			try {
 				if (project.hasNature(natureId)) {
 					internalRemoveProjectNature(project, natureId);
 				} else {
 					internalAddProjectNature(project, natureId);
 				}
 			} catch (CoreException e) {
 				EclipseUtilActivator.log(e);
 			}
 		}
 	}
 
 	/**
 	 * Remove the specified nature from the given {@link IProject}.
 	 * 
 	 * @param project The {@link IProject project}.
 	 * @param natureId The ID of the {@link IProjectNature nature} to remove.
 	 */
 	public static void removeProjectNature(IProject project, String natureId) {
 		isNotNull(project);
 		isNotNull(natureId);
 		if (project.exists() && project.isOpen()) {
 			try {
 				internalRemoveProjectNature(project, natureId);
 			} catch (CoreException e) {
 				EclipseUtilActivator.log(e);
 			}
 		}
 	}
 
 	/**
 	 * Add the specified nature to the given {@link IProject}.
 	 * 
 	 * @param project The {@link IProject project}.
 	 * @param natureId The ID of the {@link IProjectNature nature} to add.
 	 */
 	public static void addProjectNature(IProject project, String natureId) {
 		isNotNull(project);
 		isNotNull(natureId);
 		if (project.exists() && project.isOpen()) {
 			try {
 				internalAddProjectNature(project, natureId);
 			} catch (CoreException e) {
 				EclipseUtilActivator.log(e);
 			}
 		}
 	}
 
 	private static void internalRemoveProjectNature(IProject project, String natureId) throws CoreException {
 		IProjectDescription description = project.getDescription();
 		String[] natures = description.getNatureIds();
 		if (natures.length > 0) {
 			String[] newNatures = new String[natures.length - 1];
 			int i = 0;
 			for (String nature : natures) {
 				if (!natureId.equals(nature)) {
 					newNatures[i++] = nature;
 				}
 			}
 			description.setNatureIds(newNatures);
 			project.setDescription(description, null);
 		}
 	}
 
 	private static void internalAddProjectNature(IProject project, String natureId) throws CoreException {
 		IProjectDescription description = project.getDescription();
 		String[] natures = description.getNatureIds();
 		String[] newNatures = new String[natures.length + 1];
 		System.arraycopy(natures, 0, newNatures, 0, natures.length);
 		newNatures[natures.length] = natureId;
 		description.setNatureIds(newNatures);
 		project.setDescription(description, null);
 	}
 
 	/**
 	 * Indicates if the given {@link IProject project} has the specified builder.
 	 * 
 	 * @param project The {@link IProject project}.
 	 * @param builderId The builder's ID.
 	 * @return {@code true} if the given {@link IProject project} has the specified
 	 *         builder;<br>
 	 *         {@code false} otherwise.
 	 * @throws CoreException if this method fails. Reasons include:
 	 *         <ul>
 	 *         <li>The project does not exist.</li>
 	 *         <li>The project is not open.</li>
 	 *         </ul>
 	 * @since 2.0
 	 */
 	public static boolean projectHasBuilder(IProject project, String builderId) throws CoreException {
 		isNotNull(project);
 		isNotNull(builderId);
 		final IProjectDescription projectDescription = project.getDescription();
 		ICommand[] buildSpec = projectDescription.getBuildSpec();
 		for (final ICommand command : buildSpec) {
 			if (builderId.equals(command.getBuilderName())) {
 				return true;
 			}
 		}// else
 		return false;
 	}
 
 	/**
 	 * Add the builder with the specified id to the given {@link IProject project}.
 	 * 
 	 * @param project The {@link IProject project}.
 	 * @param builderId The builder's id.
 	 * @throws CoreException if this method fails. Reasons include:
 	 *         <ul>
 	 *         <li>The project does not exist.</li>
 	 *         <li>The project is not open.</li>
 	 *         </ul>
 	 * @since 2.0
 	 */
 	public static void addProjectBuilder(IProject project, String builderId) throws CoreException {
		if (!projectHasBuilder(project, builderId)) {
 			internalAddProjectBuilder(project, builderId, null);
 		}
 	}
 
 	/**
 	 * Add the builder with the specified id to the given {@link IProject project} after
 	 * the builder corresponding to the {@code afterBuilderId}.
 	 * 
 	 * @param project The {@link IProject project}.
 	 * @param builderId The builder's id.
 	 * @param afterBuilderId The builder's id after which to insert the builder.
 	 * @throws CoreException if this method fails. Reasons include:
 	 *         <ul>
 	 *         <li>The project does not exist.</li>
 	 *         <li>The project is not open.</li>
 	 *         </ul>
 	 * @since 2.0
 	 */
 	public static void addProjectBuilder(IProject project, String builderId, String afterBuilderId)
 			throws CoreException {
 		isNotNull(afterBuilderId);
		if (!projectHasBuilder(project, builderId)) {
 			internalAddProjectBuilder(project, builderId, afterBuilderId);
 		}
 	}
 
 	/**
 	 * Remove the builder with the specified id from the given {@link IProject project}.
 	 * 
 	 * @param project The {@link IProject project}.
 	 * @param builderId The builder's id.
 	 * @throws CoreException if this method fails. Reasons include:
 	 *         <ul>
 	 *         <li>The project does not exist.</li>
 	 *         <li>The project is not open.</li>
 	 *         </ul>
 	 * @since 2.0
 	 */
 	public static void removeProjectBuilder(IProject project, String builderId) throws CoreException {
 		isNotNull(project);
 		isNotNull(builderId);
 		internalRemoveProjectBuilder(project, builderId);
 	}
 
 	private static void internalAddProjectBuilder(IProject project, String builderId, String afterBuilderId)
 			throws CoreException {
 		final IProjectDescription projectDescription = project.getDescription();
 		ICommand[] buildSpec = projectDescription.getBuildSpec();
 		int insertIndex = 0;
 		for (int i = 0; i < buildSpec.length && insertIndex == 0; i++) {
 			if (afterBuilderId.equals(buildSpec[i].getBuilderName())) {
 				insertIndex = i + 1;
 			}
 		}
 		final ICommand command = projectDescription.newCommand();
 		command.setBuilderName(builderId);
 		buildSpec = Arrays2.insert(buildSpec, insertIndex, command);
 		projectDescription.setBuildSpec(buildSpec);
 		project.setDescription(projectDescription, null);
 	}
 
 	private static void internalRemoveProjectBuilder(IProject project, String builderId) throws CoreException {
 		final IProjectDescription projectDescription = project.getDescription();
 		ICommand[] buildSpec = projectDescription.getBuildSpec();
 		int removeIndex = -1;
 		for (int i = 0; i < buildSpec.length; i++) {
 			if (builderId.equals(buildSpec[i].getBuilderName())) {
 				removeIndex = i;
 				break;
 			}
 		}
 		if (removeIndex >= 0) {
 			buildSpec = Arrays2.remove(buildSpec, removeIndex);
 			projectDescription.setBuildSpec(buildSpec);
 			project.setDescription(projectDescription, null);
 		}
 	}
 
 	/**
 	 * Transforms the given path into an absolute path.
 	 * <p>
 	 * Does nothing if the given path is relative to the current workspace.
 	 * 
 	 * @param path The the path to transform.
 	 * @return the absolute path of the given one.
 	 */
 	public static String getAbsolutePath(String path) {
 		IResource resource = getWorkspace().getRoot().findMember(path);
 		if (resource != null) {
 			return resource.getLocation().toOSString();
 		}// else
 		File pathFile = new File(path);
 		if (pathFile.exists()) {
 			return pathFile.toString();
 		}// else
 		final Location location = Platform.getInstanceLocation();
 		if (location != null) {
 			File file = new File(location.getURL().getFile());
 			final String workspacePath = file.toString();
 			if (!path.startsWith(workspacePath) && path.startsWith("/")) {
 				path = workspacePath + path;
 			}
 		}
 		return path;
 	}
 
 	/**
 	 * Returns the current {@link IWorkspace workspace}.
 	 * <p>
 	 * Convenient method for {@link ResourcesPlugin#getWorkspace()}.
 	 * 
 	 * @return the current {@link IWorkspace workspace}.
 	 */
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	/**
 	 * Returns the current {@link Display} if this method is called in an SWT Thread,
 	 * otherwise the default display ({@link Display#getDefault()}).
 	 * 
 	 * @return the current {@link Display} if this method is called in an SWT Thread;<br>
 	 *         the default display otherwise.
 	 * @since 2.0
 	 */
 	public static Display getDisplay() {
 		Display display = Display.getCurrent();
 		return display != null ? display : Display.getDefault();
 	}
 }
