 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.utils;
 
 import java.io.File;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.dialogs.ListDialog;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.fedoraproject.eclipse.packager.FedoraPackagerLogger;
 import org.fedoraproject.eclipse.packager.FedoraPackagerText;
 import org.fedoraproject.eclipse.packager.IFpProjectBits;
 import org.fedoraproject.eclipse.packager.IProjectRoot;
 import org.fedoraproject.eclipse.packager.PackagerPlugin;
 import org.fedoraproject.eclipse.packager.api.FileDialogRunable;
 import org.fedoraproject.eclipse.packager.api.errors.FedoraPackagerExtensionPointException;
 import org.fedoraproject.eclipse.packager.api.errors.InvalidProjectRootException;
 
 /**
  * Utility class for Fedora Packager. Put commonly used code in here as long
  * as it's not RPM related. If it's RPM related, RPMUtils is the better choice.
  */
 public class FedoraPackagerUtils {
 	
 	private static final String PROJECT_ROOT_EXTENSIONPOINT_NAME =
 		"projectRootProvider"; //$NON-NLS-1$
 	private static final String PROJECT_ROOT_ELEMENT_NAME = "projectRoot"; //$NON-NLS-1$
 	private static final String PROJECT_ROOT_CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$
 
 	private static final String GIT_REPOSITORY = "org.eclipse.egit.core.GitProvider"; //$NON-NLS-1$
 	private static final String CVS_REPOSITORY = "org.eclipse.team.cvs.core.cvsnature"; //$NON-NLS-1$
 
 	/**
 	 * Type of the Fedora project root based on the underlying VCS system. 
 	 */
 	public static enum ProjectType {
 		/** Git project */
 		GIT,
 		/** Cvs project */
 		CVS,
 		/** Unknown */
 		UNKNOWN
 	}
 
 	/**
 	 * A valid project root contains a {@code .spec} file and a {@code sources}
 	 * file. The RPM spec-file must be of the form {@code package-name.spec}.
 	 * 
 	 * @param resource
 	 * @return True if the project root looks right.
 	 */
 	private static boolean isValidFedoraProjectRoot(IContainer resource) {
 		IFile sourceFile = resource.getFile(new Path("sources")); //$NON-NLS-1$
 		// FIXME: Determine rpm package name from a persistent property. In
 		// future the project name might not be equal to the RPM package name.
 		IFile specFile = resource.getFile(new Path(resource.getProject()
 				.getName() + ".spec")); //$NON-NLS-1$
 		if (sourceFile.exists() && specFile.exists()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a FedoraProjectRoot from the given resource after performing some
 	 * validations.
 	 * 
 	 * @param resource
 	 *            The container for this Fedora project root or a resource
 	 *            within it.
 	 * @throws InvalidProjectRootException
 	 *             If the project root does not contain a .spec with the proper
 	 *             name or doesn't contain a sources file.
 	 * 
 	 * @return The retrieved FedoraProjectRoot.
 	 */
 	public static IProjectRoot getProjectRoot(IResource resource)
 			throws InvalidProjectRootException {
 		IContainer candidate = null;
 		if (resource instanceof IFolder || resource instanceof IProject) {
 			candidate = (IContainer) resource;
 		} else if (resource instanceof IFile) {
 			candidate = resource.getParent();
 		}
 		ProjectType type = getProjectType(candidate);
 		if (candidate != null && isValidFedoraProjectRoot(candidate)
 				&& type != null) {
 			try {
 				return instantiateProjectRoot(candidate, type);
 			} catch (FedoraPackagerExtensionPointException e) {
 				FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 				logger.logError(e.getMessage(), e);
 				throw new InvalidProjectRootException(e.getMessage());
 			}
 		} else {
 			throw new InvalidProjectRootException(FedoraPackagerText.FedoraPackagerUtils_invalidProjectRootError);
 		}
 	}
 	
 	/**
 	 * Returns the project type determined from the given IResource.
 	 * @param resource The base for determining the project type.
 	 * @return The project type.
 	 */
 	public static ProjectType getProjectType(IResource resource) {
 
 		Map<?,?> persistentProperties = null;
 		try {
 			persistentProperties = resource.getProject()
 					.getPersistentProperties();
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		QualifiedName name = new QualifiedName("org.eclipse.team.core", //$NON-NLS-1$
 				"repository"); //$NON-NLS-1$
 		String repository = (String) persistentProperties.get(name);
 		if (GIT_REPOSITORY.equals(repository)) {
 			return ProjectType.GIT;
 		} else if (CVS_REPOSITORY.equals(repository)) {
 			return ProjectType.CVS;
 		}
 		return ProjectType.UNKNOWN;
 	}
 
 	/**
 	 * Returns the IFpProjectBits used to abstract vcs specific things.
 	 * 
 	 * @param fedoraprojectRoot The project for which to get the VCS specific parts.
 	 * @return The needed IFpProjectBits.
 	 */
 	public static IFpProjectBits getVcsHandler(IProjectRoot fedoraprojectRoot) {
 		IResource project = fedoraprojectRoot.getProject();
 		ProjectType type = getProjectType(project);
 		IExtensionPoint vcsExtensions = Platform.getExtensionRegistry()
 				.getExtensionPoint(PackagerPlugin.PLUGIN_ID, "vcsContribution"); //$NON-NLS-1$
 		if (vcsExtensions != null) {
 			IConfigurationElement[] elements = vcsExtensions
 					.getConfigurationElements();
 			for (int i = 0; i < elements.length; i++) {
 				if (elements[i].getName().equals("vcs") //$NON-NLS-1$
 						&& (elements[i].getAttribute("type") //$NON-NLS-1$
 								.equals(type.name()))) {
 					try {
 						IConfigurationElement bob = elements[i];
 						IFpProjectBits vcsContributor = (IFpProjectBits) bob
 								.createExecutableExtension("class");  //$NON-NLS-1$
 						// Do initialization
 						if (vcsContributor != null) {
 							vcsContributor.initialize(fedoraprojectRoot);
 						}
 						return vcsContributor;
 					} catch (CoreException e) {
 						e.printStackTrace();
 					}
 
 				}
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Checks if <code>candidate</code> is a valid file for uploading.
 	 * I.e. is non-empty and has a valid file extension. Valid file extensions
 	 * are: <code>'tar', 'gz', 'bz2', 'lzma', 'xz', 'Z', 'zip', 'tff', 'bin',
      *            'tbz', 'tbz2', 'tlz', 'txz', 'pdf', 'rpm', 'jar', 'war', 'db',
      *            'cpio', 'jisp', 'egg', 'gem'</code>
 	 * 
 	 * @param candidate
 	 * @return <code>true</code> if <code>candidate</code> is a valid file for uploading
 	 * 		   <code>false</code> otherwise.
 	 */
 	public static boolean isValidUploadFile(File candidate) {
 		if (candidate.length() != 0) {
 			Pattern extensionPattern = Pattern.compile("^.*\\.(?:tar|gz|bz2|lzma|xz|Z|zip|tff|bin|tbz|tbz2|tlz|txz|pdf|rpm|jar|war|db|cpio|jisp|egg|gem)$"); //$NON-NLS-1$
 			Matcher extMatcher = extensionPattern.matcher(candidate.getName());
 			if (extMatcher.matches()) {
 				// file extension seems to be good
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Instatiate a project root instance using the projectRoot extension point.
 	 * @param type 
 	 * @param container 
 	 * 
 	 * @return the newly created instance
 	 * @throws FedoraPackagerExtensionPointException 
 	 */
 	private static IProjectRoot instantiateProjectRoot(IContainer container, ProjectType type)
 			throws FedoraPackagerExtensionPointException {
 		IExtensionPoint projectRootExtension = Platform.getExtensionRegistry()
 				.getExtensionPoint(PackagerPlugin.PLUGIN_ID,
 						PROJECT_ROOT_EXTENSIONPOINT_NAME);
 		if (projectRootExtension != null) {
 			List<IProjectRoot> projectRootList = new ArrayList<IProjectRoot>();
 			for (IConfigurationElement projectRoot : projectRootExtension
 					.getConfigurationElements()) {
 				if (projectRoot.getName().equals(PROJECT_ROOT_ELEMENT_NAME)) {
 					// found extension point element
 					try {
 						IProjectRoot root = (IProjectRoot) projectRoot
 								.createExecutableExtension(PROJECT_ROOT_CLASS_ATTRIBUTE_NAME);
 						assert root != null;
 						projectRootList.add(root);
 					} catch (IllegalStateException e) {
 						throw new FedoraPackagerExtensionPointException(
 								e.getMessage(), e);
 					} catch (CoreException e) {
 						throw new FedoraPackagerExtensionPointException(
 								e.getMessage(), e);
 					}
 				}
 			}
 			// We need at least one project root
 			if (projectRootList.size() == 0) {
 				throw new FedoraPackagerExtensionPointException(NLS.bind(
 						FedoraPackagerText.extensionNotFoundError,
 						PROJECT_ROOT_EXTENSIONPOINT_NAME));
 			}
 			// Get the best matching project root
 			IProjectRoot projectRoot = findBestMatchingProjectRoot(projectRootList, container);
 			if (projectRoot == null) {
 				// can't continue
 				throw new FedoraPackagerExtensionPointException(NLS.bind(
 						FedoraPackagerText.extensionNotFoundError,
 						PROJECT_ROOT_EXTENSIONPOINT_NAME));
 			}
 			// Do initialization
 			projectRoot.initialize(container, type);
 			return projectRoot;
 		}
 		throw new FedoraPackagerExtensionPointException(NLS.bind(
 				FedoraPackagerText.extensionNotFoundError,
 				PROJECT_ROOT_EXTENSIONPOINT_NAME));
 	}
 
 	/**
 	 * Determine the project root, which is the best match for the given
 	 * container.
 	 * 
 	 * @param projectRootList
 	 * @param container
 	 * @return The project root which has support for the project property of
 	 *         the container or {@code null} if no such project root exists.
 	 */
 	private static IProjectRoot findBestMatchingProjectRoot(
 			List<IProjectRoot> projectRootList, IContainer container) {
 		for (IProjectRoot root: projectRootList) {
 			for (QualifiedName propName : root
 					.getSupportedProjectPropertyNames()) {
 				try {
 					String property = container.getProject()
 							.getPersistentProperty(propName);
 					if (property != null) {
 						// match found
 						FedoraPackagerLogger logger = FedoraPackagerLogger.getInstance();
 						logger.logInfo(NLS
 								.bind(FedoraPackagerText.FedoraPackagerUtils_projectRootClassNameMsg,
 										root.getClass().getName()));
 						return root;
 					}
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 	/**
 	 * @return A (probably) unique String.
 	 */
 	public static String getUniqueIdentifier(){
 		//ensure number is not in scientific notation and does not use grouping
 		NumberFormat nf = NumberFormat.getInstance();
 		nf.setMaximumIntegerDigits(9);
 		nf.setGroupingUsed(false);
 		//get time stamp for upload folder
 		String timestamp = nf.format(((double)System.currentTimeMillis()) / 1000);
 		//get random String to ensure that uploads that occur in the same millisecond don't collide
 		//two simultaneous uploads of the same srpm still have a 1 in 200 billion chance of collision
 		String randomDifferentiator = ""; //$NON-NLS-1$
 		for (int i = 0; i < 8; i++){
 			randomDifferentiator = randomDifferentiator.concat(Character.toString((char) (new Random().nextInt('Z' - 'A') + 'A')));
 		}
 		return timestamp + "." + randomDifferentiator; //$NON-NLS-1$
 	}
 	/**
 	 * This function gets the likely target from the SRPM name. 
 	 * @param srpmName 
 	 * @return The target build platform for the SRPM.
 	 */
 	public static String getTargetFromSRPM(String srpmName){
 		String[] splitSRPM = srpmName.split("\\."); //$NON-NLS-1$
 		String target = splitSRPM[splitSRPM.length - 3];
 		if (target.startsWith("fc")){ //$NON-NLS-1$
 			if (Integer.parseInt(target.substring(2)) < 16){
				return "f" + target.substring(2) + "-candidate"; //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
 				return "dist-rawhide"; //$NON-NLS-1$
 			}
 		} else if (target.startsWith("el")){ //$NON-NLS-1$
 			if (Integer.parseInt(target.substring(2)) < 6) {
 				return "dist-" + target.substring(2) + "E-epel-testing-candidate"; //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
 				return "dist-rawhide"; //$NON-NLS-1$
 			}
 		} else { 
 			return null;
 		}
 	}
 }
